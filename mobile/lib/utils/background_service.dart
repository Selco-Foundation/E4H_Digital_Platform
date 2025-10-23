import 'dart:async';
import 'dart:io';
import 'dart:ui' show DartPluginRegistrant;

import 'package:flutter/material.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../data/nosql/cache_completion_report.dart';
import '../data/nosql/cache_specification.dart';
import '../data/nosql/cache_submission_job.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/asset/asset.dart';
import '../model/audit_details/audit_details.dart';
import '../model/document/document.dart';
import '../model/entities/project_facility.dart';
import '../model/transaction/transaction.dart';
import '../repositories/app_init_Repo.dart'; // envConfig
import '../repositories/assetRepo.dart';
import '../repositories/bom_repo.dart';
import '../repositories/project_facility_repo.dart';
import '../repositories/project_repo.dart';
import '../repositories/project_workflow.dart';
import '../utils/utils.dart';
import 'constants.dart';

// project submisison
const String kMethodSubmit = 'submit_project';
const String kEvtProgress = 'submission_progress';
const String kEvtError = 'submission_error';
const String kEvtDone = 'submission_done';
const String kCmdStop = 'stopService';

// project rejection
const String kMethodReject = 'reject_project';
const String kEvtRejectDone = 'rejection_done';
const String kEvtRejectError = 'rejection_error';

// Handshake so UI knows the service is ready
const String kEvtReady = 'bg_ready';

const String kCmdForeground = 'bring_to_foreground';

// ===== Android notification channel =====
const String _svcChannelId = 'asset_submission_channel';
const String _svcChannelName = 'Asset Submission';
const int _svcNotifId = 728331;

final FlutterLocalNotificationsPlugin _fln = FlutterLocalNotificationsPlugin();

StreamSubscription? _uiErrSub;
StreamSubscription? _uiDoneSub;
StreamSubscription? _uiRejErrSub;
StreamSubscription? _uiRejDoneSub;

Future<void> ensureAndroidNotificationPermission() async {
  if (!Platform.isAndroid) return;

  final androidPlugin = _fln.resolvePlatformSpecificImplementation<
      AndroidFlutterLocalNotificationsPlugin>();

  final granted = await androidPlugin?.areNotificationsEnabled() ?? true;
  if (!granted) {
    await androidPlugin?.requestNotificationsPermission();
  }
}

Future<void> setupBackgroundService() async {
  WidgetsFlutterBinding.ensureInitialized();
  await envConfig.initialize(); // UI isolate init
  final isar = await Constants().isar;

  const androidInit = AndroidInitializationSettings('@mipmap/ic_launcher');
  const iosInit = DarwinInitializationSettings();
  await _fln.initialize(const InitializationSettings(
    android: androidInit,
    iOS: iosInit,
  ));
  const androidChannel = AndroidNotificationChannel(
    _svcChannelId,
    _svcChannelName,
    description: 'Submitting assets in background',
    importance: Importance.low,
  );
  await _fln
      .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>()
      ?.createNotificationChannel(androidChannel);

  await BackgroundServiceController.I.init(isar: isar);

  await FlutterBackgroundService().configure(
    androidConfiguration: AndroidConfiguration(
      onStart: onStart,
      isForegroundMode: true,
      autoStart: false,
      notificationChannelId: _svcChannelId,
      initialNotificationTitle: 'Submitting assets',
      initialNotificationContent: 'Preparing…',
      foregroundServiceNotificationId: _svcNotifId,
    ),
    iosConfiguration: IosConfiguration(
      onForeground: onStart,
      onBackground: _onIosBackground,
    ),
  );

  final uiService = FlutterBackgroundService();

  _uiErrSub?.cancel();
  _uiErrSub = uiService.on(kEvtError).listen((data) async {
    final pid = data?['projectId'] as String?;
    final msg = data?['message']?.toString();
    debugPrint('[UI] kEvtError received: pid=$pid msg=$msg');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      projectId: pid,
      status: 'failed',
      error: msg,
    );
  });

  _uiDoneSub?.cancel();
  _uiDoneSub = uiService.on(kEvtDone).listen((data) async {
    final pid = data?['projectId'] as String?;
    debugPrint('[UI] kEvtDone received: pid=$pid');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(isar: uiIsar, projectId: pid, status: 'success');
  });

  _uiRejErrSub?.cancel();
  _uiRejErrSub = uiService.on(kEvtRejectError).listen((data) async {
    final pid = data?['projectId'] as String?;
    final msg = data?['message']?.toString();
    debugPrint('[UI] kEvtRejectError received: pid=$pid msg=$msg');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
        isar: uiIsar, projectId: pid, status: 'failed', error: msg);
  });

  _uiRejDoneSub?.cancel();
  _uiRejDoneSub = uiService.on(kEvtRejectDone).listen((data) async {
    final pid = data?['projectId'] as String?;
    debugPrint('[UI] kEvtRejectDone received: pid=$pid');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(isar: uiIsar, projectId: pid, status: 'success');
  });

  debugPrint('[UI] setupBackgroundService complete: BG listeners bound');
}

class BackgroundServiceController {
  BackgroundServiceController._();
  static final BackgroundServiceController I = BackgroundServiceController._();

  late Isar _isar;
  Future<void> init({required Isar isar}) async {
    _isar = isar;
  }

  Future<void> enqueueSubmission({
    required String projectId,
    required String userType,
    required bool fromDraft,
  }) async {
    final service = FlutterBackgroundService();

    if (await service.isRunning()) {
      debugPrint('[UI] service already running -> invoke directly');

      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {'content': 'Preparing…'});

      service.invoke(kMethodSubmit, {
        'projectId': projectId,
        'userType': userType,
        'fromDraft': fromDraft,
      });
      return;
    }

    final readyStream = service.on(kEvtReady);
    await service.startService();
    final running = await service.isRunning();
    debugPrint('[UI] service.startService() -> running=$running');

    try {
      await readyStream.first.timeout(const Duration(seconds: 5));
      debugPrint('[UI] kEvtReady received. Submitting job...');
    } catch (_) {
      debugPrint('[UI] kEvtReady timeout; invoking after short delay');
      await Future.delayed(const Duration(milliseconds: 300));
    }

    service.invoke(kMethodSubmit, {
      'projectId': projectId,
      'userType': userType,
      'fromDraft': fromDraft,
    });
  }

  Future<void> enqueueRejection({
    required String projectId,
    required String userType,
    required List<Map<String, dynamic>> transactions, // serialize in BLoC
  }) async {
    final service = FlutterBackgroundService();

    // If the service is already running, bring it to foreground and invoke immediately.
    if (await service.isRunning()) {
      debugPrint('[UI] service already running -> invoke REJECTION directly');

      // Make sure the notification is visible again.
      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {'content': 'Preparing rejection…'});

      service.invoke(kMethodReject, <String, dynamic>{
        'projectId': projectId,
        'userType': userType,
        'transactions': transactions,
      });
      return;
    }

    // Otherwise start it, then wait for kEvtReady (emitted from onStart)
    final readyStream = service.on(kEvtReady);
    await service.startService();
    final running = await service.isRunning();
    debugPrint('[UI] service.startService() -> running=$running');

    try {
      await readyStream.first.timeout(const Duration(seconds: 5));
      debugPrint('[UI] kEvtReady received. Submitting REJECTION job...');
    } catch (_) {
      debugPrint('[UI] kEvtReady timeout; proceeding after 300ms fallback');
      await Future.delayed(const Duration(milliseconds: 300));
    }

    service.invoke(kCmdForeground, {'content': 'Preparing rejection…'});

    service.invoke(kMethodReject, <String, dynamic>{
      'projectId': projectId,
      'userType': userType,
      'transactions': transactions,
    });
  }

  Future<void> stopNow() async {
    final service = FlutterBackgroundService();
    if (await service.isRunning()) {
      debugPrint('[UI] stopNow() -> kCmdStop');
      service.invoke(kCmdStop);
    }
  }
}

String _pretty(Object? e) {
  final s = e?.toString() ?? 'Failed.';
  return s.replaceFirst(RegExp(r'^(Exception:\s*)+'), '');
}

@pragma('vm:entry-point')
bool _onIosBackground(ServiceInstance service) {
  WidgetsFlutterBinding.ensureInitialized();
  return true;
}

@pragma('vm:entry-point')
void onStart(ServiceInstance service) async {
  WidgetsFlutterBinding.ensureInitialized();

  DartPluginRegistrant.ensureInitialized();

  await envConfig.initialize();

  final isar = await Constants().isar;
  debugPrint('[BG] onStart ready. isar#${identityHashCode(isar)}');

  if (service is AndroidServiceInstance) {
    service.setAsForegroundService();
    await service.setForegroundNotificationInfo(
      title: 'Submitting assets',
      content: 'Preparing…',
    );
  }

  service.on(kMethodSubmit).listen((payload) async {
    debugPrint('[BG] submit received: $payload');

    final projectId = payload?['projectId'] as String?;
    final userType = payload?['userType'] as String?;
    if (projectId == null || userType == null) return;

    try {
      await writeJobStatus(isar: isar, projectId: projectId, status: 'queued');
      await writeJobStatus(isar: isar, projectId: projectId, status: 'running');

      debugPrint('[BG] entering _performSubmissionForProject');
      await _performSubmissionForProject(
        isar: isar,
        projectId: projectId,
        userType: userType,
      );
      debugPrint('[BG] _performSubmissionForProject done');

      await writeJobStatus(isar: isar, projectId: projectId, status: 'success');
      service.invoke(kEvtProgress, {'completed': 1, 'total': 1});

      // Include projectId so UI can mirror status
      debugPrint('[BG] invoke kEvtDone pid=$projectId');
      service.invoke(kEvtDone, {'projectId': projectId});

      // DO NOT stop the service here; let UI stop it after consuming the event.
    } catch (e, st) {
      debugPrint('[BG] ERROR: $e\n$st');

      final msg = _pretty(e);
      await writeJobStatus(
        isar: isar,
        projectId: projectId!,
        status: 'failed',
        error: msg,
      );

      // Notify UI/BLoC (include projectId)
      debugPrint('[BG] invoke kEvtError pid=$projectId');
      service.invoke(kEvtError, {'projectId': projectId, 'message': msg});

      // DO NOT stop here; UI stops after it receives failure.
    }
  });

  service.on(kMethodReject).listen((payload) async {
    final projectId = payload?['projectId'] as String?;
    final userType = payload?['userType'] as String?;
    final txList = (payload?['transactions'] as List?)?.cast<Map>() ?? const [];
    if (projectId == null || userType == null) return;

    try {
      // optional: reflect a “running” status in the same job table
      await writeJobStatus(isar: isar, projectId: projectId, status: 'running');

      await _performRejectionForProject(
        isar: isar,
        projectId: projectId,
        userType: userType,
        transactions: txList.map((m) => Map<String, dynamic>.from(m)).toList(),
      );

      // success -> notify UI
      service.invoke(kEvtRejectDone, {'projectId': projectId});
    } catch (e, st) {
      debugPrint('[BG][REJECT] ERROR: $e\n$st');

      await writeJobStatus(
        isar: isar,
        projectId: projectId!,
        status: 'failed',
        error: _pretty(e),
      );

      service.invoke(kEvtRejectError, {
        'projectId': projectId,
        'message': _pretty(e),
      });
    }
  });

  service.on(kCmdStop).listen((_) async {
    debugPrint('[BG] stop requested');
    if (service is AndroidServiceInstance) {
      service.setAsBackgroundService();
    }
    await service.stopSelf(); // stopping foreground removes notification
  });

  service.on(kCmdForeground).listen((data) async {
    if (service is AndroidServiceInstance) {
      service.setAsForegroundService();
      await service.setForegroundNotificationInfo(
        title: 'Submitting assets',
        content: (data?['content'] as String?) ?? 'Working…',
      );
    }
  });

  service.invoke(kEvtReady);
}

Future<void> writeJobStatus({
  required Isar isar,
  required String projectId,
  required String status,
  String? error,
}) async {
  await isar.writeTxn(() async {
    final existing = await isar.cacheSubmissionJobs
        .where()
        .projectIdEqualTo(projectId)
        .findFirst();

    if (existing == null) {
      final job = CacheSubmissionJob(
        projectId: projectId,
        status: status,
        error: error,
      );
      await isar.cacheSubmissionJobs.put(job);
    } else {
      existing
        ..status = status
        ..error = error;
      await isar.cacheSubmissionJobs.put(existing);
    }
  });
}

Future<void> _performSubmissionForProject({
  required Isar isar,
  required String projectId,
  required String userType,
}) async {
  try {
    final facilityId = (await ProjectFacilityRepository().search(
      ProjectFacilitySearchModel(projectId: [projectId]),
      isar,
    ))
        .facilityId;

    final repo = AssetRepository();
    const types = ['inverter', 'battery', 'panel'];

    for (final type in types) {
      final assets = await isar.cacheAddNewAssets
          .where()
          .projectIdEqualTo(projectId)
          .filter()
          .assetTypeEqualTo(type)
          .findAll();

      if (assets.isEmpty) {
        throw Exception("No cached assets found for type $type.");
      }

      final spec = await isar.cacheSpecifications
          .where()
          .projectIdEqualTo(projectId)
          .filter()
          .assetTypeEqualTo(type)
          .findFirst();

      final detail = await isar.cacheAssetDetails
          .where()
          .projectIdEqualTo(projectId)
          .filter()
          .assetTypeEqualTo(type)
          .findFirst();

      if (spec == null || detail == null) {
        throw Exception("Missing specification or detail for type $type.");
      }

      for (final saved in assets) {
        final documents = <Document>[];
        if (saved.photoPath.isNotEmpty) {
          final photoId = await getFilestoreUrl(saved.photoPath);
          documents.add(
            Document(
              documentType: saved.documentType,
              fileStore: photoId,
              documentUid: "DOC-ASSET-${saved.serialNumber}",
              additionalDetailsJson: null,
              geoLocation: GeoLocation(
                latitude: saved.latitude,
                longitude: saved.longitude,
              ),
            ),
          );
        }

        final now = DateTime.now().toUtc();
        final startIso = now.toIso8601String();
        final years = userType == USER_TYPES.FIELD_STAFF.name
            ? 0
            : parseWarrantyYears(detail.warranty!);
        final endIso = userType == USER_TYPES.FIELD_STAFF.name
            ? ""
            : now.add(Duration(days: 365 * years)).toIso8601String();

        final assetDetails = AssetDetails(
          totalCapacity: spec.totalCapacity,
          totalCapacityUnit: spec.totalCapacityUnit,
          totalCapacityUOM: spec.totalCapacityUnit,
          currentUnit:
              type == ASSET_TYPES.INVERTER.name.toLowerCase() ? '1' : null,
          capacityUnit: (type == ASSET_TYPES.BATTERY.name.toLowerCase() ||
                  type == ASSET_TYPES.PANEL.name.toLowerCase())
              ? saved.capacityUnit
              : null,
          panelCapacity: type == ASSET_TYPES.PANEL.name.toLowerCase()
              ? double.parse(saved.panelCapacity!)
              : null,
          batteryCapacity: type == ASSET_TYPES.BATTERY.name.toLowerCase()
              ? double.parse(saved.batteryCapacity!)
              : null,
          batteryVoltage: type == ASSET_TYPES.BATTERY.name.toLowerCase()
              ? double.parse(saved.batteryVoltage!)
              : null,
          batteryType: type == ASSET_TYPES.BATTERY.name.toLowerCase()
              ? saved.batteryType
              : null,
          voltageUnit: (type == ASSET_TYPES.BATTERY.name.toLowerCase() ||
                  type == ASSET_TYPES.INVERTER.name.toLowerCase())
              ? saved.voltageUnit
              : null,
          inverterCapacity: type == ASSET_TYPES.INVERTER.name.toLowerCase()
              ? double.parse(saved.inverterCapacity!)
              : null,
          inverterCapacityUnit: type == ASSET_TYPES.INVERTER.name.toLowerCase()
              ? saved.inverterCapacityUnit
              : null,
        );

        final userId = await SecureStore().getSelectedIndividual();
        final audit = AuditDetails(lastModifiedBy: userId, lastModified: now);

        final assetModel = Asset(
          assetId: saved.assetId,
          tenantId: envConfig.variables.tenantId,
          facilityID: facilityId,
          assetTypeID: type.toUpperCase(),
          system: spec.system,
          serialNumber: saved.serialNumber,
          brandID: detail.brand,
          assetDetails: assetDetails,
          warrantyStartDate:
              userType == USER_TYPES.SUPERVISOR.name ? startIso : "",
          warrantyDuration: userType == USER_TYPES.SUPERVISOR.name
              ? parseWarrantyYears(detail.warranty)
              : 0,
          warrantyEndDate: userType == USER_TYPES.SUPERVISOR.name ? endIso : "",
          modelNumber: detail.model,
          wfStatus: "CREATED",
          isActive: true,
          documents: documents,
          auditDetails: (saved.assetId?.isNotEmpty ?? false) ? audit : null,
        );

        await repo.createOrUpdateAsset(
            asset: assetModel, isar: isar, facilityId: facilityId);
      }
    }

    final remoteRepo = ProjectRemoteRepository();
    final workflowDocuments = <Document>[];

    const typesForDocs = ['inverter', 'battery', 'panel'];
    final workflowDocumentFromCache =
        await ProjectWorkflowRepository().collectWorkflowMediaDocs(
      isar: isar,
      projectId: projectId,
      types: typesForDocs,
    );
    workflowDocuments.addAll(workflowDocumentFromCache);

    final completionReports = await isar.cacheCompletionReports
        .where()
        .projectIdEqualTo(projectId)
        .findAll();

    final completionDocuments = <Document>[];
    for (final report in completionReports) {
      if (report.filePath.isEmpty) continue;
      if (((report.fileName ?? '')
          .toLowerCase()
          .contains('installation_report_bom'))) {
        continue;
      }
      final mediaId = await getFilestoreUrl(report.filePath);
      completionDocuments.add(
        Document(
          documentType: "INSTALLATION_REPORT",
          fileStore: mediaId,
          documentUid: "INSTALLATION-REPORT-${report.fileType}-$mediaId",
          geoLocation: GeoLocation(
            latitude: report.latitude,
            longitude: report.longitude,
          ),
        ),
      );
    }

    if (userType == USER_TYPES.SUPERVISOR.name) {
      try {
        final bomFileStoreId = await BomRepository().generateBomPdf(
          isar: isar,
          projectId: projectId,
          userType: userType,
        );

        String lat = "", lon = "";
        if (workflowDocuments.isNotEmpty) {
          lat = workflowDocuments.first.geoLocation?.latitude ?? "";
          lon = workflowDocuments.first.geoLocation?.longitude ?? "";
        }

        workflowDocuments.add(
          Document(
            documentType: "INSTALLATION_REPORT_BOM",
            fileStore: bomFileStoreId,
            documentUid:
                "BOM-$projectId-${DateTime.now().millisecondsSinceEpoch}",
            geoLocation: GeoLocation(latitude: lat, longitude: lon),
          ),
        );
      } catch (_) {
        throw Exception("Failed to attach BOM PDF:");
      }

      try {
        final tenantId = envConfig.variables.tenantId;
        final assignUserUuid =
            await SecureStore().getSelectedIndividual() ?? '';

        await BomRepository().submitMergedForProject(
          isar: isar,
          projectId: projectId,
          tenantId: tenantId,
          facilityId: facilityId,
          assignUserUuid: assignUserUuid,
        );
      } catch (_) {
        throw Exception("BOM submission error");
      }
    }

    await remoteRepo.updateProjectWorkflow(
      projectId: projectId,
      action: userType == USER_TYPES.FIELD_STAFF.name
          ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
          : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
      documents: [...workflowDocuments, ...completionDocuments],
    );

    await UnsubmittedProjectRepository(isar).delete(projectId, userType);
    await UnsubmittedProjectRepository(isar).deleteAddNewAsset(projectId);
    await PrefilledProjectRepository(isar)
        .delete(projectId: projectId, userType: userType);
    await CompletionReportRepository(isar).delete(projectId: projectId);
    await BomRepository().delete(isar: isar, projectId: projectId);

    return;
  } catch (e) {
    throw PlainError(_pretty(e));
  }
}

Future<void> _performRejectionForProject({
  required Isar isar,
  required String projectId,
  required String userType,
  required List<Map<String, dynamic>> transactions,
}) async {
  try {
    const types = ['inverter', 'battery', 'panel'];
    final workflowDocuments = <Document>[];

    final fromCache =
        await ProjectWorkflowRepository().collectWorkflowMediaDocs(
      isar: isar,
      projectId: projectId,
      types: types,
    );
    workflowDocuments.addAll(fromCache);

    await AssetRepository().submitRejection(
      projectId: projectId,
      transactions: transactions.map((m) => Transaction.fromJson(m)).toList(),
      documents: workflowDocuments,
    );

    await UnsubmittedProjectRepository(isar).delete(projectId, userType);
    await PrefilledProjectRepository(isar)
        .delete(projectId: projectId, userType: userType);
    await CompletionReportRepository(isar).delete(projectId: projectId);
    await BomRepository().delete(isar: isar, projectId: projectId);
  } catch (e) {
    throw PlainError(_pretty(e));
  }
}

class PlainError implements Exception {
  final String message;
  PlainError(this.message);
  @override
  String toString() => message;
}
