import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:ui' show DartPluginRegistrant;

import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:flutter/material.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_amc_installation_form.dart';
import '../data/nosql/cache_amc_media_upload.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../data/nosql/cache_completion_report.dart';
import '../data/nosql/cache_schedule_visit_form_values.dart';
import '../data/nosql/cache_specification.dart';
import '../data/nosql/cache_submission_job.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/asset/asset.dart';
import '../model/audit_details/audit_details.dart';
import '../model/document/document.dart';
import '../model/transaction/transaction.dart';
import '../repositories/activity_facility_repo.dart';
import '../repositories/activity_facility_workflow_repo.dart';
import '../repositories/app_init_repo.dart';
import '../repositories/asset_repo.dart';
import '../repositories/dynamic_form_repo.dart';
import '../repositories/scheduled_visit_repo.dart';
import '../utils/utils.dart';
import 'constants.dart';

const String kMethodSubmit = 'submit_project';
const String kEvtProgress = 'submission_progress';
const String kEvtError = 'submission_error';
const String kEvtDone = 'submission_done';
const String kCmdStop = 'stopService';

const String kMethodReject = 'reject_project';
const String kEvtRejectDone = 'rejection_done';
const String kEvtRejectError = 'rejection_error';

const String kMethodSubmitVisit = 'submit_schedule_visit';
const String kEvtScheduleVisitDone = 'schedule_visit_done';
const String kEvtScheduleVisitError = 'schedule_visit_error';

const String kEvtReady = 'bg_ready';

const String kCmdForeground = 'bring_to_foreground';

const String _svcChannelId = 'asset_submission_channel';
const String _svcChannelName = 'Asset Submission';
const int _svcNotifId = 728331;

String installationReportBom = "INSTALLATION_REPORT_BOM";

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
  await envConfig.initialize();
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
    final pid = data?['activityFacilityId'] as String?;
    final msg = data?['message']?.toString();
    AppLogger.instance.info('[UI] kEvtError received: pid=$pid msg=$msg');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      status: 'failed',
      error: msg,
    );
  });

  _uiDoneSub?.cancel();
  _uiDoneSub = uiService.on(kEvtDone).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    AppLogger.instance.info('[UI] kEvtDone received: pid=$pid');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
        isar: uiIsar, activityFacilityId: pid, status: 'success');
  });

  _uiRejErrSub?.cancel();
  _uiRejErrSub = uiService.on(kEvtRejectError).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    final msg = data?['message']?.toString();
    AppLogger.instance.info('[UI] kEvtRejectError received: pid=$pid msg=$msg');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
        isar: uiIsar, activityFacilityId: pid, status: 'failed', error: msg);
  });

  _uiRejDoneSub?.cancel();
  _uiRejDoneSub = uiService.on(kEvtRejectDone).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    AppLogger.instance.info('[UI] kEvtRejectDone received: pid=$pid');
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
        isar: uiIsar, activityFacilityId: pid, status: 'success');
  });

  AppLogger.instance
      .info('[UI] setupBackgroundService complete: BG listeners bound');
}

class BackgroundServiceController {
  BackgroundServiceController._();
  static final BackgroundServiceController I = BackgroundServiceController._();

  late Isar _isar;
  Future<void> init({required Isar isar}) async {
    _isar = isar;
  }

  Future<void> enqueueSubmission({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
    required bool fromDraft,
  }) async {
    final service = FlutterBackgroundService();

    if (await service.isRunning()) {
      AppLogger.instance
          .info('[UI] service already running -> invoke directly');

      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {'content': 'Preparing…'});

      service.invoke(kMethodSubmit, {
        'activityFacilityId': activityFacilityId,
        'facilityId': facilityId,
        'userType': userType,
        'fromDraft': fromDraft,
      });
      return;
    }

    final readyStream = service.on(kEvtReady);
    await service.startService();
    final running = await service.isRunning();
    AppLogger.instance.info('[UI] service.startService() -> running=$running');

    try {
      await readyStream.first.timeout(const Duration(seconds: 8));
      AppLogger.instance.info('[UI] kEvtReady received. Submitting job...');
    } catch (_) {
      AppLogger.instance
          .info('[UI] kEvtReady timeout; invoking after short delay');
      await Future.delayed(const Duration(milliseconds: 300));
    }

    service.invoke(kMethodSubmit, {
      'activityFacilityId': activityFacilityId,
      'facilityId': facilityId,
      'userType': userType,
      'fromDraft': fromDraft,
    });
  }

  Future<void> enqueueRejection({
    required String activityFacilityId,
    required String userType,
    required List<Map<String, dynamic>> transactions,
  }) async {
    final service = FlutterBackgroundService();

    if (await service.isRunning()) {
      AppLogger.instance
          .info('[UI] service already running -> invoke REJECTION directly');

      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {'content': 'Preparing rejection…'});

      service.invoke(kMethodReject, <String, dynamic>{
        'activityFacilityId': activityFacilityId,
        'userType': userType,
        'transactions': transactions,
      });
      return;
    }

    final readyStream = service.on(kEvtReady);
    await service.startService();
    final running = await service.isRunning();
    AppLogger.instance.info('[UI] service.startService() -> running=$running');

    try {
      await readyStream.first.timeout(const Duration(seconds: 8));
      AppLogger.instance
          .info('[UI] kEvtReady received. Submitting REJECTION job...');
    } catch (_) {
      AppLogger.instance
          .info('[UI] kEvtReady timeout; proceeding after 300ms fallback');
      await Future.delayed(const Duration(milliseconds: 300));
    }

    service.invoke(kCmdForeground, {'content': 'Preparing rejection…'});

    service.invoke(kMethodReject, <String, dynamic>{
      'activityFacilityId': activityFacilityId,
      'userType': userType,
      'transactions': transactions,
    });
  }

  Future<void> enqueueScheduleVisitSubmission({
    required String scheduledVisitId,
    required String userType,
  }) async {
    final svc = FlutterBackgroundService();

    if (await svc.isRunning()) {
      AppLogger.instance.info(
        '[BG-CTL] service already running -> submit_schedule_visit directly',
      );

      await ensureAndroidNotificationPermission();
      svc.invoke(kCmdForeground, {
        'title': 'Submitting visit report',
        'content': 'Preparing visit submission…',
      });

      svc.invoke(kMethodSubmitVisit, {
        'scheduledVisitId': scheduledVisitId,
        'userType': userType,
      });
      return;
    }

    final readyStream = svc.on(kEvtReady);

    await svc.startService();
    final running = await svc.isRunning();
    AppLogger.instance.info(
      '[BG-CTL] service.startService() -> running=$running (visit submit)',
    );

    try {
      await readyStream.first.timeout(const Duration(seconds: 5));
    } catch (e) {
      AppLogger.instance.info(
        '[BG-CTL] kEvtReady timeout for visit submit; invoking anyway',
      );
      await Future.delayed(const Duration(milliseconds: 300));
    }

    svc.invoke(kCmdForeground, {
      'title': 'Submitting visit report',
      'content': 'Preparing visit submission…',
    });

    svc.invoke(kMethodSubmitVisit, {
      'scheduledVisitId': scheduledVisitId,
      'userType': userType,
    });
  }

  Future<void> stopNow() async {
    final service = FlutterBackgroundService();
    if (await service.isRunning()) {
      AppLogger.instance.info('[UI] stopNow() -> kCmdStop');
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
  final envFuture = envConfig.initialize();
  final isarFuture = Constants().isar;

  if (service is AndroidServiceInstance) {
    service.setAsForegroundService();
    await service.setForegroundNotificationInfo(
      title: 'Submitting assets',
      content: 'Preparing…',
    );
  }

  service.on(kMethodSubmit).listen((payload) async {
    AppLogger.instance.info('[BG] submit received: $payload');
    final isar = await isarFuture;
    await envFuture;
    AppLogger.instance
        .info('[BG] onStart ready. isar#${identityHashCode(isar)}');

    final activityFacilityId = payload?['activityFacilityId'] as String?;
    final facilityId = payload?['facilityId'] as String?;
    final userType = payload?['userType'] as String?;
    if (activityFacilityId == null || userType == null) return;

    try {
      await writeJobStatus(
          isar: isar, activityFacilityId: activityFacilityId, status: 'queued');
      await writeJobStatus(
          isar: isar,
          activityFacilityId: activityFacilityId,
          status: 'running');

      AppLogger.instance.info('[BG] entering _performSubmissionForProject');
      await _performSubmissionForActivityFacility(
        isar: isar,
        activityFacilityId: activityFacilityId,
        facilityId: facilityId!,
        userType: userType,
      );
      AppLogger.instance.info('[BG] _performSubmissionForProject done');

      await writeJobStatus(
          isar: isar,
          activityFacilityId: activityFacilityId,
          status: 'success');
      service.invoke(kEvtProgress, {'completed': 1, 'total': 1});

      AppLogger.instance.info('[BG] invoke kEvtDone pid=$activityFacilityId');
      service.invoke(kEvtDone, {'activityFacilityId': activityFacilityId});
    } catch (e, st) {
      AppLogger.instance.info('$e\n$st', title: "[BG] ERROR:");

      final msg = _pretty(e);
      await writeJobStatus(
        isar: isar,
        activityFacilityId: activityFacilityId!,
        status: 'failed',
        error: msg,
      );

      AppLogger.instance.info('[BG] invoke kEvtError pid=$activityFacilityId');
      service.invoke(kEvtError,
          {'activityFacilityId': activityFacilityId, 'message': msg});
    }
  });

  service.on(kMethodReject).listen((payload) async {
    final isar = await isarFuture;
    await envFuture;
    AppLogger.instance
        .info('[BG] onStart ready. isar#${identityHashCode(isar)}');
    final activityFacilityId = payload?['activityFacilityId'] as String?;
    final userType = payload?['userType'] as String?;
    final txList = (payload?['transactions'] as List?)?.cast<Map>() ?? const [];
    if (activityFacilityId == null || userType == null) return;

    try {
      await writeJobStatus(
          isar: isar,
          activityFacilityId: activityFacilityId,
          status: 'running');

      await _performRejectionForActivityFacility(
        isar: isar,
        activityFacilityId: activityFacilityId,
        userType: userType,
        transactions: txList.map((m) => Map<String, dynamic>.from(m)).toList(),
      );

      service
          .invoke(kEvtRejectDone, {'activityFacilityId': activityFacilityId});
    } catch (e, st) {
      AppLogger.instance.info('$e\n$st', title: "[BG][REJECT] ERROR: ");

      await writeJobStatus(
        isar: isar,
        activityFacilityId: activityFacilityId,
        status: 'failed',
        error: _pretty(e),
      );

      service.invoke(kEvtRejectError, {
        'activityFacilityId': activityFacilityId,
        'message': _pretty(e),
      });
    }
  });

  service.on(kMethodSubmitVisit).listen((payload) async {
    final isar = await isarFuture;
    await envFuture;

    final visitId = payload?['scheduledVisitId'] as String?;
    final userType = payload?['userType'] as String?;

    AppLogger.instance.info(
      '[BG] submit_schedule_visit received payload=$payload',
    );

    if (visitId == null || userType == null) {
      AppLogger.instance.info(
        '[BG] submit_schedule_visit missing visitId or userType',
      );
      return;
    }

    try {
      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        status: 'queued',
      );

      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        status: 'running',
      );

      await _performScheduleVisitSubmission(
        isar: isar,
        scheduledVisitId: visitId,
        userType: userType,
      );

      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        status: 'success',
      );

      service.invoke(kEvtScheduleVisitDone, {
        'scheduledVisitId': visitId,
      });
    } catch (e, st) {
      AppLogger.instance.error(
        title: '[BG] _performScheduleVisitSubmission failed',
        message: e.toString(),
        stackTrace: st,
      );

      String msg;
      final str = e.toString();
      if (str.contains('FORM_NOT_FILLED')) {
        msg =
            'Visit form is not filled yet. Please fill the AMC form before submitting.';
      } else {
        msg = 'Something went wrong while submitting the visit report.';
      }

      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        status: 'failed',
        error: msg,
      );

      service.invoke(kEvtScheduleVisitError, {
        'scheduledVisitId': visitId,
        'message': msg,
      });
    }
  });

  service.on(kCmdStop).listen((_) async {
    AppLogger.instance.info('[BG] stop requested');
    if (service is AndroidServiceInstance) {
      service.setAsBackgroundService();
    }
    await service.stopSelf();
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
  required String activityFacilityId,
  required String status,
  String? error,
}) async {
  await isar.writeTxn(() async {
    final existing = await isar.cacheSubmissionJobs
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst();

    if (existing == null) {
      final job = CacheSubmissionJob(
        activityFacilityId: activityFacilityId,
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

bool _isInstallBomPdfNameOrPath(String value) {
  final v = value.trim();
  if (v.isEmpty) return false;

  final normalized = normalizeReportNameType(v);
  if ((normalized ?? '').toUpperCase() == installationReportBom) {
    return true;
  }

  final s = v.toLowerCase();
  return s.contains('installation') && s.contains('bom');
}

Future<void> _deleteLocalFileIfExists(String path) async {
  if (path.trim().isEmpty) return;
  try {
    final f = File(path);
    if (await f.exists()) {
      await f.delete();
    }
  } catch (_) {}
}

Future<void> _performSubmissionForActivityFacility({
  required Isar isar,
  required String activityFacilityId,
  required String facilityId,
  required String userType,
}) async {
  try {
    final repo = AssetRepository();
    const types = ['inverter', 'battery', 'panel'];

    for (final type in types) {
      final assets = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findAll();

      if (assets.isEmpty) {
        throw Exception("No cached assets found for type $type.");
      }

      final spec = await isar.cacheSpecifications
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findFirst();

      final detail = await isar.cacheAssetDetails
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
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
              id: saved.documentId,
              documentType: "ASSET",
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
        final years = parseWarrantyYears(detail.warranty);
        final startIso = years > 0 ? now.toIso8601String() : "";
        final endIso = (years > 0)
            ? now.add(Duration(days: 365 * years)).toIso8601String()
            : "";

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
          activityFacilityID: activityFacilityId,
          facilityID: facilityId,
          assetTypeID: type.toUpperCase(),
          system: spec.system,
          serialNumber: saved.serialNumber,
          brandID: detail.brand,
          assetDetails: assetDetails,
          warrantyStartDate: startIso,
          warrantyDuration: years,
          warrantyEndDate: endIso,
          modelNumber: detail.model,
          wfStatus: "CREATED",
          isActive: true,
          documents: documents,
          auditDetails: (saved.assetId?.isNotEmpty ?? false) ? audit : null,
        );

        await repo.createOrUpdateAsset(asset: assetModel, isar: isar);
      }
    }

    final remoteRepo = ActivityFacilityRemoteRepository();
    final workflowDocuments = <Document>[];

    const typesForDocs = ['inverter', 'battery', 'panel'];
    final workflowDocumentFromCache =
        await ActivityFacilityWorkflowRepository().collectWorkflowMediaDocs(
      isar: isar,
      activityFacilityId: activityFacilityId,
      types: typesForDocs,
      userType: userType,
    );
    workflowDocuments.addAll(workflowDocumentFromCache);

    final completionReports = await isar.cacheCompletionReports
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();

    final completionDocuments = <Document>[];
    for (final report in completionReports) {
      if (report.filePath.isEmpty) continue;
      // if (((report.fileName ?? '')
      //     .toUpperCase()
      //     .contains(installationReportBom))) {
      //   continue;
      // }
      final fileName = (report.fileName ?? '').trim().isNotEmpty
          ? report.fileName!.trim()
          : report.filePath;

      final isBomPdf = _isInstallBomPdfNameOrPath(fileName);

      if (isBomPdf) {
        await _deleteLocalFileIfExists(report.filePath);
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

    final resolvedBomUserType = await BomRepository().resolveBomUserType(
        isar: isar, activityFacilityId: activityFacilityId, userType: userType);

    if (resolvedBomUserType != null) {
      try {
        workflowDocuments.removeWhere((d) => (d.documentType ?? '')
            .toUpperCase()
            .contains(installationReportBom));
        final bomFileStoreId = await BomRepository().generateBomPdf(
          isar: isar,
          activityFacilityId: activityFacilityId,
          userType: userType,
        );

        String lat = "", lon = "";
        if (workflowDocuments.isNotEmpty) {
          lat = workflowDocuments.first.geoLocation?.latitude ?? "";
          lon = workflowDocuments.first.geoLocation?.longitude ?? "";
        }

        workflowDocuments.add(
          Document(
            documentType: installationReportBom,
            fileStore: bomFileStoreId,
            documentUid:
                "BOM-$activityFacilityId-${DateTime.now().millisecondsSinceEpoch}",
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
          activityFacilityId: activityFacilityId,
          tenantId: tenantId,
          facilityId: facilityId,
          assignUserUuid: assignUserUuid,
        );
      } catch (_) {
        throw Exception("BOM submission error");
      }
    }

    await remoteRepo.updateActivityFacilityWorkflow(
      activityFacilityId: activityFacilityId,
      action: userType == USER_TYPES.FIELD_STAFF.name
          ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
          : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
      documents: [...workflowDocuments, ...completionDocuments],
    );

    await UnsubmittedActivityFacilityRepository(isar)
        .delete(activityFacilityId, userType);
    await UnsubmittedActivityFacilityRepository(isar)
        .deleteAddNewAsset(activityFacilityId);
    await PrefilledActivityFacilityRepository(isar)
        .delete(activityFacilityId: activityFacilityId, userType: userType);
    await CompletionReportRepository(isar)
        .delete(projectId: activityFacilityId);
    await BomRepository()
        .delete(isar: isar, activityFacilityId: activityFacilityId);
    await BomRepository()
        .deleteAllBomDocs(isar: isar, activityFacilityId: activityFacilityId);
    await ActivityFacilityWorkflowRepository().deleteWorkflowMediaDocs(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );
    return;
  } catch (e) {
    AppLogger.instance.info("e ${e.toString()}");
    throw PlainError(_pretty(e));
  }
}

Future<void> _performRejectionForActivityFacility({
  required Isar isar,
  required String activityFacilityId,
  required String userType,
  required List<Map<String, dynamic>> transactions,
}) async {
  try {
    const types = ['inverter', 'battery', 'panel'];
    final workflowDocuments = <Document>[];

    final fromCache =
        await ActivityFacilityWorkflowRepository().collectWorkflowDocs(
      isar: isar,
      activityFacilityId: activityFacilityId,
    );
    workflowDocuments.addAll(fromCache);

    await AssetRepository().submitRejection(
      activityFacilityId: activityFacilityId,
      transactions: transactions.map((m) => Transaction.fromJson(m)).toList(),
      documents: workflowDocuments,
    );

    await UnsubmittedActivityFacilityRepository(isar)
        .delete(activityFacilityId, userType);
    await PrefilledActivityFacilityRepository(isar)
        .delete(activityFacilityId: activityFacilityId, userType: userType);
    await CompletionReportRepository(isar)
        .delete(projectId: activityFacilityId);
    await BomRepository()
        .delete(isar: isar, activityFacilityId: activityFacilityId);
    await BomRepository()
        .deleteAllBomDocs(isar: isar, activityFacilityId: activityFacilityId);
    await ActivityFacilityWorkflowRepository().deleteWorkflowMediaDocs(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );
  } catch (e) {
    throw PlainError(_pretty(e));
  }
}

Future<void> _performScheduleVisitSubmission({
  required Isar isar,
  required String scheduledVisitId,
  required String userType,
}) async {
  AppLogger.instance.info(
    '[BG] _performScheduleVisitSubmission started for visit=$scheduledVisitId userType=$userType',
  );

  final form = await isar.cacheScheduleVisitFormValues
      .where()
      .scheduledVisitIdEqualTo(scheduledVisitId)
      .filter()
      .userTypeEqualTo(userType)
      .findFirst();

  if (form == null || form.dataJson.trim().isEmpty) {
    throw Exception('Form not filled');
  }

  final responses = jsonDecode(form.dataJson) as Map<String, dynamic>;
  final amcFormRepo = AmcDynamicFormRepository();

  final pdfFileStoreId = await amcFormRepo.generateFormPdf(
      isar: isar, scheduledVisitId: scheduledVisitId, userType: userType);

  if (pdfFileStoreId == null || pdfFileStoreId.isEmpty) {
    throw Exception('Failed to generate AMC PDF');
  }

  final cachedMedia = await isar.cacheAmcMediaUploads
      .where()
      .scheduledVisitIdEqualTo(scheduledVisitId)
      .filter()
      .userTypeEqualTo(userType)
      .findAll();

  final visitDocuments = <Document>[];

  for (final media in cachedMedia) {
    if (media.filePath.isEmpty) continue;

    final fileStoreId = await getFilestoreUrl(media.filePath);

    visitDocuments.add(
      Document(
        documentType: media.itemType,
        fileStore: fileStoreId,
        documentUid:
            'DOC-AMC-${media.itemType}-${DateTime.now().millisecondsSinceEpoch}',
        geoLocation: GeoLocation(
          latitude: media.latitude,
          longitude: media.longitude,
        ),
      ),
    );
  }

  final workflowDocuments = <Document>[];

  final String? mediaLat =
      cachedMedia.isNotEmpty ? cachedMedia.first.latitude : null;
  final String? mediaLon =
      cachedMedia.isNotEmpty ? cachedMedia.first.longitude : null;

  workflowDocuments.add(
    Document(
      documentType: 'AMC_INSTALLATION_FORM',
      fileStore: pdfFileStoreId,
      documentUid:
          'AMC-FORM-$scheduledVisitId-${DateTime.now().millisecondsSinceEpoch}',
      geoLocation: GeoLocation(
        latitude: mediaLat,
        longitude: mediaLon,
      ),
    ),
  );

  final remote = ScheduledVisitRemoteRepository();
  await remote.updateVisitWorkflow(
    visitId: scheduledVisitId,
    schemaCode: "12345678",
    version: 1,
    responses: responses,
    workflowDocuments: workflowDocuments,
    visitDocuments: visitDocuments,
  );

  await PrefilledScheduledVisitRepository(isar)
      .addOrTouch(scheduledVisitId: scheduledVisitId, userType: userType);
  final installationForm = workflowDocuments.first;
  await ScheduledVisitRepository(isar).upsertCacheAmcInstallationForm(
      isar,
      new CacheAmcInstallationForm(
        scheduledVisitId: scheduledVisitId,
        filePath: installationForm.fileStore ?? '',
        latitude: installationForm.geoLocation?.latitude ?? '',
        longitude: installationForm.geoLocation?.longitude ?? '',
        userType: userType,
      ));

  AppLogger.instance.info(
    '[BG] _performScheduleVisitSubmission completed for visit=$scheduledVisitId',
  );
}

class PlainError implements Exception {
  final String message;
  PlainError(this.message);
  @override
  String toString() => message;
}
