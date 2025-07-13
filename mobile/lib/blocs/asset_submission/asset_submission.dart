import 'dart:async';
import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_add_new_asset.dart';
import '../../data/nosql/cache_asset_detail.dart';
import '../../data/nosql/cache_completion_report.dart';
import '../../data/nosql/cache_media_upload.dart';
import '../../data/nosql/cache_specification.dart';
import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_project.dart';
import '../../model/asset/asset.dart';
import '../../model/document/document.dart';
import '../../model/entities/project_facility.dart';
import '../../model/project_workflow/project_workflow.dart';
import '../../repositories/app_init_Repo.dart';
import '../../repositories/assetRepo.dart';
import '../../repositories/project_facility_repo.dart';
import '../../repositories/project_repo.dart';
import '../../utils/utils.dart';

part 'asset_submission.freezed.dart';

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
        deleteDraftAfter: false,
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

    final total = localWorkflows.length;
    int completed = 0;

    for (final draft in localWorkflows) {
      emit(AssetSubmissionState.progress(
        completed: completed * 2 + 1,
        total: total * 2,
      ));

      final success = await _handleSubmit(
        projectId: draft.project.id,
        userType: event.userType,
        emit: emit,
        deleteDraftAfter: true,
      );

      if (!success) return;

      completed++;
      emit(AssetSubmissionState.progress(
        completed: completed * 2,
        total: total * 2,
      ));
    }

    emit(const AssetSubmissionState.success());
  }

  Future<bool> _handleSubmit({
    required String projectId,
    required String userType,
    required Emitter<AssetSubmissionState> emit,
    required bool deleteDraftAfter,
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

        final documents = <Document>[];
        for (final saved in assets) {
          if (saved.photoPath.isNotEmpty) {
            final file = File(saved.photoPath);
            if (await file.exists()) {
              final photoId = await repo.uploadFile(file);
              print("photoId $photoId");
              documents.add(Document(
                documentType: saved.documentType,
                fileStore: photoId,
                documentUid: "DOC-ASSET-${saved.serialNumber}",
                additionalDetails: null,
                geoLocation: GeoLocation(
                  latitude: saved.latitude,
                  longitude: saved.longitude,
                  //additionalDetails: null,
                ),
              ));
            }
          }

          final mediaEntries = await _isar.cacheMediaUploads
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findAll();

          print("[$type] found ${mediaEntries.length} cached media uploads");
          for (var m in mediaEntries) {
            print(
                "    media id=${m.id} filePath='${m.filePath}' itemType='${m.itemType}'");
          }

          for (final m in mediaEntries) {
            if (m.filePath.isEmpty) continue;
            final mediaFile = File(m.filePath);
            if (!await mediaFile.exists()) continue;
            final mediaId = await repo.uploadFile(mediaFile);

            documents.add(Document(
              // id: 'DOCUMENT-0199',
              documentType: m.itemType,
              fileStore: mediaId,
              documentUid:
                  "DOC-${m.itemType}-${m.id}-${m.itemNumber}-${m.assetType}-${DateTime.now().toUtc().toIso8601String()}",
              additionalDetails: null,
              geoLocation: GeoLocation(
                latitude: m.latitude,
                longitude: m.longitude,
                //additionalDetails: null,
              ),
            ));
          }

          print("documents $documents");

          final now = DateTime.now().toUtc();
          final startIso = now.toIso8601String();
          final years = userType == USER_TYPES.FIELD_STAFF.name
              ? 0
              : parseWarrantyYears(detail.warranty!);
          final endIso = userType == USER_TYPES.FIELD_STAFF.name
              ? ""
              : now.add(Duration(days: 365 * years)).toIso8601String();

          // 1) Build AssetDetails with every field explicitly
          final assetDetails = AssetDetails(
            totalCapacity: spec.totalCapacity,
            totalCapacityUnit: spec.totalCapacityUnit,
            totalCapacityUOM: spec.totalCapacityUnit,

            capacityUnit: type == 'panel'
                ? "Wp"
                : type == 'battery'
                    ? "Ah"
                    : null, // from mdms
            panelCapacity: type == 'panel'
                ? 34.1
                : null, // saved.itemNumber, // "panelCapacity": saved.itemNumber,

            // totalCapacity: 67.2, //todo update values in mdms data to be removed
            // totalCapacityUnit: "kWp", //todo update values in mdms data to be removed

            batteryVoltage: type == 'battery' ? 12 : null,
            batteryCapacity: type == 'battery'
                ? 125
                : null, // double.parse(saved.itemNumber)
            voltageUnit:
                (type == 'battery' || type == 'inverter') ? "Volts" : null,
            batteryType: type == 'battery' ? "Lithium" : null,

            inverterCapacity:
                type == 'inverter' ? double.parse(saved.itemNumber) : null,
            inverterCapacityUnit: type == 'inverter' ? 'kVA' : null,
            currentUnit: type == 'inverter' ? '1' : null,
          );

          print("assetDetails $assetDetails");

          // String myId = type == "inverter"
          //     ? 'ASSET-0212'
          //     : type == 'battery'
          //         ? 'ASSET-0213'
          //         : type == 'panel'
          //             ? 'ASSET-0214'
          //             : '';

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
            warrantyStartDate: userType == USER_TYPES.SUPERVISOR.name
                ? startIso
                : "", // : null,
            warrantyDuration: userType == USER_TYPES.SUPERVISOR.name
                ? parseWarrantyYears(detail.warranty)
                : 1, //null,
            warrantyEndDate:
                userType == USER_TYPES.SUPERVISOR.name ? endIso : "",
            modelNumber: detail.model,
            wfStatus: "CREATED",
            isActive: true,
            documents: documents,
          );
          print("assetModel $assetModel");
          await repo.createOrUpdateAsset(asset: assetModel, isar: _isar);
        }
      }

      final remoteRepo = ProjectRemoteRepository();
      final completionDocuments = <Document>[];
      final completionReport = await _isar.cacheCompletionReports
          .where()
          .projectIdEqualTo(projectId)
          .findFirst();
      print("completionReport $completionReport");
      //print("completionReport ${completionReport!.filePath}");
      if (completionReport != null) {
        if (completionReport.filePath.isNotEmpty) {
          final completionFile = await getCachedFile(
              completionReport.filePath); // File(completionReport.filePath);
          if (await completionFile != null) {
            final photoId = await repo.uploadFile(completionFile!);
            completionDocuments.add(Document(
              // id: 'DOCUMENT-0199',
              documentType: "INSTALLATION_REPORT",
              fileStore: photoId,
              documentUid: "INSTALLATION-REPORT-${photoId}",
              // additionalDetails: null,
              geoLocation: GeoLocation(
                latitude: completionReport.latitude,
                longitude: completionReport.longitude,
                //additionalDetails: null,
              ),
            ));
            print("completionReport $completionReport");
          }
        }
        await remoteRepo.updateProjectWorkflow(
          projectId: projectId,
          action: userType == USER_TYPES.FIELD_STAFF.name
              ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
              : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
          documents: completionDocuments,
        );
      } else {
        await remoteRepo.updateProjectWorkflow(
          projectId: projectId,
          action: userType == USER_TYPES.FIELD_STAFF.name
              ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
              : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
        );
      }

      if (deleteDraftAfter) {
        await _draftRepo.delete(projectId, userType);
      }

      if (!deleteDraftAfter) emit(const AssetSubmissionState.success());
      return true;
    } catch (e) {
      emit(AssetSubmissionState.failure(e.toString()));
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
