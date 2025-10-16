import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:flutter/material.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:workmanager/workmanager.dart';

import '../../data/nosql/cache_add_new_asset.dart';
import '../../data/nosql/cache_asset_detail.dart';
import '../../data/nosql/cache_completion_report.dart';
import '../../data/nosql/cache_specification.dart';
import '../../data/nosql/cache_submission_job.dart';
import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_project.dart';
import '../../data/secure_storage/secureStore.dart';
import '../../model/asset/asset.dart';
import '../../model/audit_details/audit_details.dart';
import '../../model/document/document.dart';
import '../../model/entities/project_facility.dart';
import '../../model/project_workflow/project_workflow.dart';
import '../../repositories/app_init_Repo.dart';
import '../../repositories/assetRepo.dart';
import '../../repositories/bom_repo.dart';
import '../../repositories/project_facility_repo.dart';
import '../../repositories/project_repo.dart';
import '../../repositories/project_workflow.dart';
import '../../utils/constants.dart';
import '../../utils/utils.dart';

part 'asset_submission.freezed.dart';

const _kAssetSubmitTaskName = 'asset_submission_job';
StreamSubscription? _jobSub;
StreamSubscription<void>? _bulkJobsSub;

Future<void> _writeJobStatus({
  required Isar isar,
  required String projectId,
  required String status, // 'queued' | 'running' | 'success' | 'failed'
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

Future<bool> _fail(Isar isar, String projectId, String message,
    [Object? e]) async {
  await _writeJobStatus(
    isar: isar,
    projectId: projectId,
    status: 'failed',
    error: e == null ? message : '$message: $e',
  );
  // return false;
  return true;
}

@pragma('vm:entry-point')
void assetSubmissionCallbackDispatcher() {
  Workmanager().executeTask((task, inputData) async {
    final _isar = await Constants().isar;
    WidgetsFlutterBinding.ensureInitialized();
    await envConfig.initialize();
    final secureStore = SecureStore();
    if (task != _kAssetSubmitTaskName) return Future.value(true);

    final projectId = (inputData?['projectId'] as String?) ?? '';
    final userType = (inputData?['userType'] as String?) ?? '';
    final fromDraft = (inputData?['fromDraft'] as bool?) ?? false;
    if (projectId.isEmpty || userType.isEmpty) return Future.value(false);

    await _writeJobStatus(projectId: projectId, status: 'running', isar: _isar);

    try {
      final facilityId = (await ProjectFacilityRepository().search(
        ProjectFacilitySearchModel(projectId: [projectId]),
        _isar,
      ))
          .facilityId;

      final repo = AssetRepository();
      const types = ['inverter', 'battery', 'panel'];

      for (final type in types) {
        final assets = await _isar.cacheAddNewAssets
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findAll();

        if (assets.isEmpty) {
          return _fail(
              _isar, projectId, "No cached assets found for type $type");
        }

        print("[$type] found ${assets.length} cached assets");
        for (var a in assets) {
          print("    serial=${a.serialNumber} photoPath='${a.photoPath}'");
        }

        final spec = await _isar.cacheSpecifications
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findFirst();
        final detail = await _isar.cacheAssetDetails
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findFirst();
        if (spec == null || detail == null) {
          return _fail(_isar, projectId,
              "Missing specification or detail for type $type.");
        }

        for (final saved in assets) {
          final documents = <Document>[];
          if (saved.photoPath.isNotEmpty) {
            String photoId = await getFilestoreUrl(saved.photoPath);
            print("photoId $photoId");
            documents.add(Document(
              documentType: saved.documentType,
              fileStore: photoId,
              documentUid: "DOC-ASSET-${saved.serialNumber}",
              additionalDetailsJson: null,
              geoLocation: GeoLocation(
                latitude: saved.latitude,
                longitude: saved.longitude,
                //additionalDetails: null,
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

          // 1) Build AssetDetails with every field explicitly

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

          print("assetDetails $assetDetails");

          final userId = await SecureStore().getSelectedIndividual();
          final audit = AuditDetails(lastModifiedBy: userId, lastModified: now);

          // 2) Build the Asset itself
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
          print(
              "assetModel audit ${assetModel.auditDetails?.toJson() ?? '— none —'}");
          print("assetModel $assetModel");
          print("assetModel.warrantyDuration ${assetModel.warrantyDuration}");
          print("facilityId $facilityId");
          await repo.createOrUpdateAsset(
              asset: assetModel, isar: _isar, facilityId: facilityId);
        }
      }

      print("about starting completion reports");

      final remoteRepo = ProjectRemoteRepository();
      final workflowDocuments = <Document>[];

      final workflowDocumentFromCache =
          await ProjectWorkflowRepository().collectWorkflowMediaDocs(
        isar: _isar,
        projectId: projectId,
        types: types,
      );

      workflowDocuments.addAll(workflowDocumentFromCache);

      final completionReports = await _isar.cacheCompletionReports
          .where()
          .projectIdEqualTo(projectId)
          .findAll();

      final completionDocuments = <Document>[];

      for (final report in completionReports) {
        if (report.filePath.isEmpty) continue;
        if (((report.fileName ?? '')
            .toLowerCase()
            .contains('installation_report_bom'))) continue;
        String mediaId = await getFilestoreUrl(report.filePath);
        print("mediaId $mediaId");
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
      print("completionDocuments ${completionDocuments.toString()}");

      print("projectId $projectId");
      print("document1 $workflowDocuments");
      print("document2 ${workflowDocuments.toString()}");

      if (userType == USER_TYPES.SUPERVISOR.name) {
        String bomFileStoreId;
        try {
          // fetch bytes
          final bomBytes = await BomRepository().generateBomPdf(
            isar: _isar,
            projectId: projectId,
            userType: userType,
          );
          // upload to file store as PDF
          final bomFileName =
              "bom_${projectId}_${DateTime.now().millisecondsSinceEpoch}.pdf";
          bomFileStoreId = await BomRepository().uploadPdfToFileStore(
            bomBytes!,
            bomFileName,
          );
          // determine lat/lon for BOM doc: use first media file if exists
          String lat = "", lon = "";
          if (workflowDocuments.isNotEmpty) {
            lat = workflowDocuments.first.geoLocation?.latitude ?? "";
            lon = workflowDocuments.first.geoLocation?.longitude ?? "";
          }
          // add BOM document
          workflowDocuments.add(
            Document(
              documentType: "INSTALLATION_REPORT_BOM",
              fileStore: bomFileStoreId,
              documentUid:
                  "BOM-${projectId}-${DateTime.now().millisecondsSinceEpoch}",
              geoLocation: GeoLocation(latitude: lat, longitude: lon),
            ),
          );
        } catch (e) {
          print("Error fetching/uploading BOM PDF: $e");
          return _fail(_isar, projectId, "Failed to attach BOM PDF:");
        }

        try {
          final tenantId = envConfig.variables.tenantId;
          final assignUserUuid = await SecureStore().getSelectedIndividual();

          print(
              '[BOM:submit] isarInstance=${identityHashCode(_isar)} project=$projectId');

          await BomRepository().submitMergedForProject(
            isar: _isar,
            projectId: projectId,
            tenantId: tenantId,
            facilityId: facilityId,
            assignUserUuid: assignUserUuid ?? '',
          );
        } catch (e) {
          print('BOM submission error: $e');
          return _fail(_isar, projectId, "BOM submission error");
        }
      }

      await remoteRepo.updateProjectWorkflow(
        projectId: projectId,
        action: userType == USER_TYPES.FIELD_STAFF.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
        documents: [...workflowDocuments, ...completionDocuments],
      );

      final draftRepo = UnsubmittedProjectRepository(_isar);

      // caches to clear
      await draftRepo.delete(projectId, userType);
      await draftRepo.deleteAddNewAsset(projectId);
      await PrefilledProjectRepository(_isar).delete(
        projectId: projectId,
        userType: userType,
      );
      await CompletionReportRepository(_isar).delete(projectId: projectId);
      await BomRepository().delete(isar: _isar, projectId: projectId);
      if (!fromDraft) {
        await _writeJobStatus(
            isar: _isar, projectId: projectId, status: 'success');
      }
      return Future.value(true);
    } catch (e) {
      print("e ${e.toString()}");
      String? errorMessage = e.toString();
      // "We are facing an issues please try again";
      // if ((e.toString() == "Exception: No network connection") ||
      //     (e.toString() == "Exception: No internet access")) {
      //   errorMessage =
      //       "For some Reason you have bad internet connectivity, we have saved your data, please try to sync the data later";
      // }
      return _fail(_isar, projectId, errorMessage);
    }
  });
}

class AssetSubmissionBloc
    extends Bloc<AssetSubmissionEvent, AssetSubmissionState> {
  final Isar _isar;
  final UnsubmittedProjectRepository _draftRepo;

  AssetSubmissionBloc(this._isar)
      : _draftRepo = UnsubmittedProjectRepository(_isar),
        super(const AssetSubmissionState.initial()) {
    on<_SubmitAll>(_onSubmitAll);
    on<_SubmitAllDrafts>(_onSubmitAllDrafts);
  }

  Future<void> _onSubmitAll(
    _SubmitAll event,
    Emitter<AssetSubmissionState> emit,
  ) =>
      _handleSubmit(
        projectId: event.projectId,
        userType: event.userType,
        emit: emit,
        fromDraft: false,
      );

  Future<void> upsertSyncRecord(String userType) async {
    final now = DateTime.now().toUtc();

    await _isar.writeTxn(() async {
      final existing = await _isar.cacheSyncRecords
          .where()
          .userTypeEqualTo(userType)
          .findFirst();

      if (existing != null) {
        existing.syncedAt = now;
        await _isar.cacheSyncRecords.put(existing);
      } else {
        final record = CacheSyncRecord(userType: userType, syncedAt: now);
        await _isar.cacheSyncRecords.put(record);
      }
    });
  }

  Future<void> _onSubmitAllDrafts(
    _SubmitAllDrafts event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    emit(const AssetSubmissionState.loading());
    // save last sync date as now
    await upsertSyncRecord(event.userType);
    final localEntries = await _isar.cacheUnsubmittedProjects
        .where()
        .filter()
        .userTypeEqualTo(event.userType)
        .findAll();

    final localWorkflows = localEntries
        .map((e) => ProjectWorkflow(project: e.project, status: e.status))
        .toList();

    if (localWorkflows.isEmpty) {
      emit(const AssetSubmissionState.failure("No drafts to sync."));
      return;
    }

    // final total = localWorkflows.length;
    // int completed = 0;
    //
    // for (final draft in localWorkflows) {
    //   emit(AssetSubmissionState.progress(
    //     completed: completed * 2 + 1,
    //     total: total * 2,
    //   ));
    //
    //   final success = await _handleSubmit(
    //     projectId: draft.project.id,
    //     userType: event.userType,
    //     emit: emit,
    //     fromDraft: true,
    //   );
    //
    //   if (!success) return;
    //
    //   completed++;
    //   emit(AssetSubmissionState.progress(
    //     completed: completed * 2,
    //     total: total * 2,
    //   ));
    // }
    //
    // emit(const AssetSubmissionState.success());

    // collect projectIds we’re submitting
    final projectIds = localWorkflows.map((w) => w.project.id).toList();
    final total = projectIds.length;

    // 1) Fire-and-forget queueing (don’t await long jobs)
    int queued = 0;
    for (final pid in projectIds) {
      emit(AssetSubmissionState.progress(completed: queued, total: total));
      // _handleSubmit will mark status=queued and schedule the Workmanager task.
      // We do not await the final result here.
      // ignore: unawaited_futures
      _handleSubmit(
        projectId: pid,
        userType: event.userType,
        emit: emit,
        fromDraft: true,
      );
      queued++;
    }

    // 2) Non-blocking watcher: observe cacheSubmissionJobs and emit when all done
    // Cancel old bulk watcher if any
    await _bulkJobsSub?.cancel();

    _bulkJobsSub = _isar.cacheSubmissionJobs.watchLazy().listen((_) async {
      // Recompute statuses for our submitted set
      int done = 0;
      bool anyFailed = false;

      // If your Isar has a `.anyOf` helper, use that; otherwise, loop.
      for (final pid in projectIds) {
        final row = await _isar.cacheSubmissionJobs
            .where()
            .projectIdEqualTo(pid)
            .findFirst();

        if (row == null) continue;

        if (row.status == 'success' || row.status == 'failed') {
          done++;
          if (row.status == 'failed') anyFailed = true;
        }
      }

      // Emit progress as background jobs complete
      emit(AssetSubmissionState.progress(completed: done, total: total));

      if (done >= total) {
        // All finished: emit final state then stop watching
        if (anyFailed) {
          emit(const AssetSubmissionState.failure("Some submissions failed."));
        } else {
          emit(const AssetSubmissionState.success());
        }
        await _bulkJobsSub?.cancel();
        _bulkJobsSub = null;
      }
    });

    // Note: we do NOT emit success here; success will be emitted by the watcher
    // when all jobs reach a terminal state.
  }

  Future<bool> _handleSubmit({
    required String projectId,
    required String userType,
    required Emitter<AssetSubmissionState> emit,
    required bool fromDraft,
  }) async {
    emit(const AssetSubmissionState.loading());
    await _writeJobStatus(projectId: projectId, status: 'queued', isar: _isar);

    final id =
        'asset-submit-${projectId}-${DateTime.now().millisecondsSinceEpoch}';
    await Workmanager().registerOneOffTask(
      id,
      _kAssetSubmitTaskName,
      inputData: {
        'projectId': projectId,
        'userType': userType,
        'fromDraft': fromDraft,
      },
      constraints: Constraints(networkType: NetworkType.connected),
    );

    _jobSub?.cancel();
    _jobSub = _isar.cacheSubmissionJobs
        .where()
        .projectIdEqualTo(projectId)
        .watch(fireImmediately: true)
        .listen((rows) {
      if (rows.isEmpty) return;
      final job = rows.first;
      switch (job.status) {
        case 'running':
          // optional: keep showing loading
          break;
        case 'success':
          emit(const AssetSubmissionState.success());
          _jobSub?.cancel();
          break;
        case 'failed':
          emit(AssetSubmissionState.failure(job.error ?? 'Submission failed'));
          _jobSub?.cancel();
          break;
        default:
          // queued -> no-op
          break;
      }
    });
    return true;
  }

  @override
  Future<void> close() {
    _bulkJobsSub?.cancel();
    _jobSub?.cancel();
    return super.close();
  }

  Future<bool> _handleSubmit2({
    required String projectId,
    required String userType,
    required Emitter<AssetSubmissionState> emit,
    required bool fromDraft,
  }) async {
    emit(const AssetSubmissionState.loading());
    try {
      final facilityId = (await ProjectFacilityRepository().search(
        ProjectFacilitySearchModel(projectId: [projectId]),
        _isar,
      ))
          .facilityId;

      final repo = AssetRepository();
      const types = ['inverter', 'battery', 'panel'];

      for (final type in types) {
        final assets = await _isar.cacheAddNewAssets
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findAll();

        if (assets.isEmpty) {
          emit(AssetSubmissionState.failure(
              "No cached assets found for type $type."));
          return false;
        }

        print("[$type] found ${assets.length} cached assets");
        for (var a in assets) {
          print("    serial=${a.serialNumber} photoPath='${a.photoPath}'");
        }

        final spec = await _isar.cacheSpecifications
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findFirst();
        final detail = await _isar.cacheAssetDetails
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findFirst();
        if (spec == null || detail == null) {
          emit(AssetSubmissionState.failure(
              "Missing specification or detail for type $type."));
          return false;
        }

        for (final saved in assets) {
          final documents = <Document>[];
          if (saved.photoPath.isNotEmpty) {
            String photoId = await getFilestoreUrl(saved.photoPath);
            print("photoId $photoId");
            documents.add(Document(
              documentType: saved.documentType,
              fileStore: photoId,
              documentUid: "DOC-ASSET-${saved.serialNumber}",
              additionalDetailsJson: null,
              geoLocation: GeoLocation(
                latitude: saved.latitude,
                longitude: saved.longitude,
                //additionalDetails: null,
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

          // 1) Build AssetDetails with every field explicitly

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

          print("assetDetails $assetDetails");

          final userId = await SecureStore().getSelectedIndividual();
          final audit = AuditDetails(lastModifiedBy: userId, lastModified: now);

          // 2) Build the Asset itself
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
          print(
              "assetModel audit ${assetModel.auditDetails?.toJson() ?? '— none —'}");
          print("assetModel $assetModel");
          print("assetModel.warrantyDuration ${assetModel.warrantyDuration}");
          print("facilityId $facilityId");
          await repo.createOrUpdateAsset(
              asset: assetModel, isar: _isar, facilityId: facilityId);
        }
      }

      print("about starting completion reports");

      final remoteRepo = ProjectRemoteRepository();
      final workflowDocuments = <Document>[];

      final workflowDocumentFromCache =
          await ProjectWorkflowRepository().collectWorkflowMediaDocs(
        isar: _isar,
        projectId: projectId,
        types: types,
      );

      workflowDocuments.addAll(workflowDocumentFromCache);

      final completionReports = await _isar.cacheCompletionReports
          .where()
          .projectIdEqualTo(projectId)
          .findAll();

      final completionDocuments = <Document>[];

      for (final report in completionReports) {
        if (report.filePath.isEmpty) continue;
        if (((report.fileName ?? '')
            .toLowerCase()
            .contains('installation_report_bom'))) continue;
        String mediaId = await getFilestoreUrl(report.filePath);
        print("mediaId $mediaId");
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
      print("completionDocuments ${completionDocuments.toString()}");

      print("projectId $projectId");
      print("document1 $workflowDocuments");
      print("document2 ${workflowDocuments.toString()}");

      if (userType == USER_TYPES.SUPERVISOR.name) {
        String bomFileStoreId;
        try {
          // fetch bytes
          final bomBytes = await BomRepository().generateBomPdf(
            isar: _isar,
            projectId: projectId,
            userType: userType,
          );
          // upload to file store as PDF
          final bomFileName =
              "bom_${projectId}_${DateTime.now().millisecondsSinceEpoch}.pdf";
          bomFileStoreId = await BomRepository().uploadPdfToFileStore(
            bomBytes!,
            bomFileName,
          );
          // determine lat/lon for BOM doc: use first media file if exists
          String lat = "", lon = "";
          if (workflowDocuments.isNotEmpty) {
            lat = workflowDocuments.first.geoLocation?.latitude ?? "";
            lon = workflowDocuments.first.geoLocation?.longitude ?? "";
          }
          // add BOM document
          workflowDocuments.add(
            Document(
              documentType: "INSTALLATION_REPORT_BOM",
              fileStore: bomFileStoreId,
              documentUid:
                  "BOM-${projectId}-${DateTime.now().millisecondsSinceEpoch}",
              geoLocation: GeoLocation(latitude: lat, longitude: lon),
            ),
          );
        } catch (e) {
          print("Error fetching/uploading BOM PDF: $e");
          emit(const AssetSubmissionState.failure("Failed to attach BOM PDF:"));
          return false;
        }

        try {
          final tenantId = envConfig.variables.tenantId;
          final assignUserUuid = await SecureStore().getSelectedIndividual();

          print(
              '[BOM:submit] isarInstance=${identityHashCode(_isar)} project=$projectId');

          await BomRepository().submitMergedForProject(
            isar: _isar,
            projectId: projectId,
            tenantId: tenantId,
            facilityId: facilityId,
            assignUserUuid: assignUserUuid ?? '',
          );
        } catch (e) {
          print('BOM submission error: $e');
          emit(const AssetSubmissionState.failure("BOM submission error"));
          return false;
        }
      }

      await remoteRepo.updateProjectWorkflow(
        projectId: projectId,
        action: userType == USER_TYPES.FIELD_STAFF.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
        documents: [...workflowDocuments, ...completionDocuments],
      );

      // caches to clear
      await _draftRepo.delete(projectId, userType);
      await _draftRepo.deleteAddNewAsset(projectId);
      await PrefilledProjectRepository(_isar).delete(
        projectId: projectId,
        userType: userType,
      );
      await CompletionReportRepository(_isar).delete(projectId: projectId);
      await BomRepository().delete(isar: _isar, projectId: projectId);
      if (!fromDraft) emit(const AssetSubmissionState.success());
      return true;
    } catch (e) {
      print("e ${e.toString()}");
      String? errorMessage = e.toString();
      // "We are facing an issues please try again";
      // if ((e.toString() == "Exception: No network connection") ||
      //     (e.toString() == "Exception: No internet access")) {
      //   errorMessage =
      //       "For some Reason you have bad internet connectivity, we have saved your data, please try to sync the data later";
      // }
      emit(AssetSubmissionState.failure("$errorMessage"));
      return false;
    }
  }
}

@freezed
class AssetSubmissionEvent with _$AssetSubmissionEvent {
  const factory AssetSubmissionEvent.submitAll({
    required String projectId,
    required String userType,
  }) = _SubmitAll;

  const factory AssetSubmissionEvent.submitAllDrafts({
    required String userType,
  }) = _SubmitAllDrafts;
}

@freezed
class AssetSubmissionState with _$AssetSubmissionState {
  const factory AssetSubmissionState.initial() = _Initial;
  const factory AssetSubmissionState.loading() = _Loading;
  const factory AssetSubmissionState.success() = _Success;
  const factory AssetSubmissionState.failure(String errorMessage) = _Failure;

  const factory AssetSubmissionState.progress({
    required int completed,
    required int total,
  }) = _Progress;
}
