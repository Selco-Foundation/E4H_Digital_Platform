import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:isar/isar.dart';
import 'package:permission_handler/permission_handler.dart';

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
import '../repositories/app_init_Repo.dart';
import '../repositories/assetRepo.dart';
import '../repositories/bom_repo.dart';
import '../repositories/project_facility_repo.dart';
import '../repositories/project_repo.dart';
import '../repositories/project_workflow.dart';
import '../utils/constants.dart';
import '../utils/utils.dart';

/// Service channel constants
const _svcChannelId = 'asset_submit_channel';
const _svcChannelName = 'Asset submission';
const _svcNotifId = 9001;

/// Keys used when invoking service methods
const _kMethodSubmit = 'submit_assets';

final FlutterLocalNotificationsPlugin _fln = FlutterLocalNotificationsPlugin();

/// Simple singleton to interact with the background service from the app/BLoC.
class BackgroundServiceController {
  BackgroundServiceController._();
  static final BackgroundServiceController I = BackgroundServiceController._();

  late Isar _isar;

  Future<void> init({required Isar isar}) async {
    _isar = isar;

    // --- ADD: create a notification channel on the UI isolate (Android 8+)
    const AndroidInitializationSettings androidInit =
        AndroidInitializationSettings('@mipmap/ic_launcher');
    await _fln.initialize(const InitializationSettings(android: androidInit));

    const AndroidNotificationChannel channel = AndroidNotificationChannel(
      _svcChannelId,
      _svcChannelName,
      description: 'Uploads assets & completion reports in background',
      importance: Importance.low, // low so it won’t be intrusive
    );

    final androidFln = _fln.resolvePlatformSpecificImplementation<
        AndroidFlutterLocalNotificationsPlugin>();
    await androidFln?.createNotificationChannel(channel);

    // --- ADD: handle POST_NOTIFICATIONS permission on Android 13+
    if (await Permission.notification.isDenied ||
        await Permission.notification.isPermanentlyDenied) {
      // ask once (you can move this elsewhere if you prefer)
      await Permission.notification.request();
    }

    final service = FlutterBackgroundService();
    await service.configure(
      androidConfiguration: AndroidConfiguration(
        onStart: _onStart,
        autoStart: false,
        isForegroundMode: true,
        notificationChannelId: _svcChannelId,
        initialNotificationTitle: 'Submitting assets',
        initialNotificationContent: 'Preparing…',
        foregroundServiceNotificationId: _svcNotifId,
      ),
      iosConfiguration: IosConfiguration(
        autoStart: false,
        onForeground: _onStart,
        onBackground: _onIosBackground,
      ),
    );
  }

  Future<void> enqueueSubmission({
    required String projectId,
    required String userType,
    required bool fromDraft,
  }) async {
    final service = FlutterBackgroundService();

    // Ensure the service is running before invoke()
    var isRunning = await service.isRunning();
    if (!isRunning) {
      await service.startService();
      // wait up to ~1s for onStart to attach handlers
      for (int i = 0; i < 5; i++) {
        await Future.delayed(const Duration(milliseconds: 200));
        isRunning = await service.isRunning();
        if (isRunning) break;
      }
    }

    // now tell the service to run the job
    service.invoke(_kMethodSubmit, <String, dynamic>{
      'projectId': projectId,
      'userType': userType,
      'fromDraft': fromDraft,
    });
  }

  Future<void> stopNow() async {
    final service = FlutterBackgroundService();
    final running = await service.isRunning();
    if (running) {
      // tell the service isolate to stop itself
      service.invoke('stopService');
    }
  }

  Future<void> startOrKick() async {
    final service = FlutterBackgroundService();
    final isRunning = await service.isRunning();
    if (!isRunning) {
      await service.startService(); // spins up isolate + foreground notif
    } else {
      service.invoke('kick'); // nudge existing isolate to check queue
    }
  }
}

/// iOS background entrypoint
@pragma('vm:entry-point')
bool _onIosBackground(ServiceInstance service) {
  WidgetsFlutterBinding.ensureInitialized();
  return true;
}

// --- ADD: helpers to keep the foreground notification tidy
Future<void> _notifSet(
    ServiceInstance service, String title, String content) async {
  if (service is AndroidServiceInstance) {
    await service.setForegroundNotificationInfo(title: title, content: content);
  }
}

Future<void> _notifStopIfIdle(ServiceInstance service, Isar isar) async {
  // if no job is 'queued' or 'running', we can stop the foreground service
  final running = await isar.cacheSubmissionJobs
      .filter()
      .anyOf(['queued', 'running'], (q, s) => q.statusEqualTo(s)).findAll();

  if (service is AndroidServiceInstance) {
    if (running.isEmpty) {
      await service.stopSelf();
    } else {
      await _notifSet(service, 'Submitting assets', 'Submitting…');
    }
  }
}

/// ANDROID/IOS service entrypoint
@pragma('vm:entry-point')
Future<void> _onStart(ServiceInstance service) async {
  WidgetsFlutterBinding.ensureInitialized();

  // Make sure env & db are ready in the isolate
  await envConfig.initialize();
  final isar = await Constants().isar;

  await _notifSet(service, 'Submitting assets', 'Submitting…');

  // Listen for submit requests coming from the app isolate
  service.on(_kMethodSubmit).listen((event) async {
    if (event == null) return;
    final String projectId = (event['projectId'] as String?) ?? '';
    final String userType = (event['userType'] as String?) ?? '';
    final bool fromDraft = (event['fromDraft'] as bool?) ?? false;
    if (projectId.isEmpty || userType.isEmpty) return;

    await writeJobStatus(isar: isar, projectId: projectId, status: 'running');
    await _notifSet(service, 'Submitting assets', 'Project $projectId…');

    try {
      // ====== BEGIN (your original submission body) ======
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
          await fail(isar, projectId, "No cached assets found for type $type");
          return;
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
          await fail(isar, projectId,
              "Missing specification or detail for type $type.");
          return;
        }

        for (final saved in assets) {
          final documents = <Document>[];
          if (saved.photoPath.isNotEmpty) {
            final photoId = await getFilestoreUrl(saved.photoPath);
            documents.add(Document(
              documentType: saved.documentType,
              fileStore: photoId,
              documentUid: "DOC-ASSET-${saved.serialNumber}",
              geoLocation: GeoLocation(
                latitude: saved.latitude,
                longitude: saved.longitude,
              ),
            ));
          }

          final now = DateTime.now().toUtc();
          final startIso = now.toIso8601String();
          final years = userType == USER_TYPES.FIELD_STAFF.name
              ? 0
              : parseWarrantyYears(detail.warranty!);
          final endIso = userType == USER_TYPES.FIELD_STAFF.name
              ? ""
              : now.add(Duration(days: 365 * years)).toIso8601String();

          print("projectId $projectId");
          print("type $type");
          print("spec ${spec.totalCapacity}");
          print("spec ${spec.totalCapacityUnit}");
          print("spec $spec");

          print(
              "ASSET_TYPES.INVERTER.name.toLowerCase() ${ASSET_TYPES.INVERTER.name.toLowerCase()}");
          print("capacityUnit ${saved.capacityUnit}");
          if (type == ASSET_TYPES.BATTERY.name.toLowerCase()) {
            print("saved.batteryCapacity! ${saved.batteryCapacity!}");
            print("saved.batteryVoltage! ${saved.batteryVoltage!}");
            print("saved.batteryType ${saved.batteryType}");
          }
          if (type == ASSET_TYPES.INVERTER.name.toLowerCase()) {
            print("saved.inverterCapacity! ${saved.inverterCapacity}");
            print("saved.inverterCapacityUnit ${saved.inverterCapacityUnit}");
          }

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
            inverterCapacityUnit:
                type == ASSET_TYPES.INVERTER.name.toLowerCase()
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
            warrantyEndDate:
                userType == USER_TYPES.SUPERVISOR.name ? endIso : "",
            modelNumber: detail.model,
            wfStatus: "CREATED",
            isActive: true,
            documents: documents,
            auditDetails: (saved.assetId?.isNotEmpty ?? false) ? audit : null,
          );

          await repo.createOrUpdateAsset(
            asset: assetModel,
            isar: isar,
            facilityId: facilityId,
          );
        }
      }

      final remoteRepo = ProjectRemoteRepository();
      final workflowDocuments = <Document>[];

      final workflowDocumentFromCache =
          await ProjectWorkflowRepository().collectWorkflowMediaDocs(
        isar: isar,
        projectId: projectId,
        types: const ['inverter', 'battery', 'panel'],
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
            .contains('installation_report_bom'))) continue;
        final mediaId = await getFilestoreUrl(report.filePath);
        completionDocuments.add(Document(
          documentType: "INSTALLATION_REPORT",
          fileStore: mediaId,
          documentUid: "INSTALLATION-REPORT-${report.fileType}-$mediaId",
          geoLocation: GeoLocation(
            latitude: report.latitude,
            longitude: report.longitude,
          ),
        ));
      }

      if (userType == USER_TYPES.SUPERVISOR.name) {
        try {
          final bomBytes = await BomRepository().generateBomPdf(
            isar: isar,
            projectId: projectId,
            userType: userType,
          );
          final bomFileName =
              "bom_${projectId}_${DateTime.now().millisecondsSinceEpoch}.pdf";
          final bomFileStoreId = await BomRepository()
              .uploadPdfToFileStore(bomBytes!, bomFileName);

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
                  "BOM-${projectId}-${DateTime.now().millisecondsSinceEpoch}",
              geoLocation: GeoLocation(latitude: lat, longitude: lon),
            ),
          );

          final tenantId = envConfig.variables.tenantId;
          final assignUserUuid = await SecureStore().getSelectedIndividual();

          await BomRepository().submitMergedForProject(
            isar: isar,
            projectId: projectId,
            tenantId: tenantId,
            facilityId: facilityId,
            assignUserUuid: assignUserUuid ?? '',
          );
        } catch (e) {
          await fail(isar, projectId, "BOM submission error: $e");
          return;
        }
      }

      await remoteRepo.updateProjectWorkflow(
        projectId: projectId,
        action: userType == USER_TYPES.FIELD_STAFF.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
        documents: [...workflowDocuments, ...completionDocuments],
      );

      // clear caches
      final draftRepo = UnsubmittedProjectRepository(isar);
      await draftRepo.delete(projectId, userType);
      await draftRepo.deleteAddNewAsset(projectId);
      await PrefilledProjectRepository(isar).delete(
        projectId: projectId,
        userType: userType,
      );
      await CompletionReportRepository(isar).delete(projectId: projectId);
      await BomRepository().delete(isar: isar, projectId: projectId);

      await writeJobStatus(isar: isar, projectId: projectId, status: 'success');
      await _notifSet(service, 'Submission finished', 'Project $projectId ✓');
      // ====== END (your original submission body) ======
    } catch (e) {
      await fail(isar, projectId, e.toString());
      await _notifSet(service, 'Submission failed', 'Project $projectId ✗');
    }

    await _notifStopIfIdle(service, isar);
  });
  service.on('stopService').listen((_) async {
    if (service is AndroidServiceInstance) {
      await service.stopSelf();
    }
  });
}

/// ------- small helpers (copied from your bloc) -------

Future<void> writeJobStatus({
  required Isar isar,
  required String projectId,
  required String status,
  String? error,
}) async {
  await isar.writeTxn(() async {
    final row = await isar.cacheSubmissionJobs
        .where()
        .projectIdEqualTo(projectId)
        .findFirst();
    if (row == null) {
      await isar.cacheSubmissionJobs.put(CacheSubmissionJob(
        projectId: projectId,
        status: status,
        error: error,
      ));
    } else {
      row
        ..status = status
        ..error = error
        ..updatedAt = DateTime.now();
      await isar.cacheSubmissionJobs.put(row);
    }
  });
}

Future<void> fail(Isar isar, String projectId, String message) async {
  await writeJobStatus(
    isar: isar,
    projectId: projectId,
    status: 'failed',
    error: message,
  );
}
