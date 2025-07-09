import 'dart:async';
import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:selco/model/document/document.dart';

import '../../data/nosql/cache_add_new_asset.dart';
import '../../data/nosql/cache_asset_detail.dart';
import '../../data/nosql/cache_completion_report.dart';
import '../../data/nosql/cache_media_upload.dart';
import '../../data/nosql/cache_specification.dart';
import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_project.dart';
import '../../model/asset/asset.dart';
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
              // final geo = <String, dynamic>{};
              // if (saved.latitude.isNotEmpty && saved.longitude.isNotEmpty) {
              //   geo['latitude'] = saved.latitude;
              //   geo['longitude'] = saved.longitude;
              // }
              // documents.add({
              //   "documentType": saved.documentType,
              //   "fileStore": photoId,
              //   "documentUid": "DOC-PHOTO-${saved.serialNumber}",
              //   if (geo.isNotEmpty) "geoLocation": geo,
              // });
              documents.add(Document(
                // id: 'DOCUMENT-0199',
                documentType: saved.documentType,
                fileStore: photoId,
                documentUid: "DOC-PHOTO-${saved.serialNumber}",
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
          for (final m in mediaEntries) {
            if (m.filePath.isEmpty) continue;
            final mediaFile = File(m.filePath);
            if (!await mediaFile.exists()) continue;
            final mediaId = await repo.uploadFile(mediaFile);
            // final geo = <String, dynamic>{};
            // if (m.latitude.isNotEmpty && m.longitude.isNotEmpty) {
            //   geo['latitude'] = m.latitude;
            //   geo['longitude'] = m.longitude;
            // }
            // documents.add({
            //   "documentType": m.itemType,
            //   "fileStore": mediaId,
            //   "documentUid":
            //       "DOC-${m.itemType}-${m.id}-${m.itemNumber}-${m.assetType}-${DateTime.now().toUtc().toIso8601String()}",
            //   if (geo.isNotEmpty) "geoLocation": geo,
            // });

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

          final now = DateTime.now().toUtc();
          final startIso = now.toIso8601String();
          final years = userType == USER_TYPES.FIELD_STAFF.name
              ? 0
              : parseWarrantyYears(detail.warranty!);
          final endIso = userType == USER_TYPES.FIELD_STAFF.name
              ? ""
              : now.add(Duration(days: 365 * years)).toIso8601String();

          final payload2 = {
            "assetDetail": {
              "Asset": {
                if (saved.assetId != null) ...{
                  "assetId": saved.assetId,
                },
                "tenantId": envConfig.variables.tenantId,
                "facilityID": facilityId,
                "assetTypeID": type.toUpperCase(),
                "system": spec.system,
                "serialNumber": saved.serialNumber,
                "brandID": detail.brand,
                "assetDetails": {
                  "totalCapacity": spec.totalCapacity,
                  "totalCapacityUnit": spec.totalCapacityUnit,
                  if (type == 'panel') ...{
                    // "panelCapacity": saved.itemNumber, //todo update values in mdms data
                    "capacityUnit": "Wp",

                    "panelCapacity": "34.1", // saved.itemNumber,

                    "totalCapacity":
                        67.2, //todo update values in mdms data to be removed
                    "totalCapacityUnit":
                        "kWp", //todo update values in mdms data to be removed
                  },
                  if (type == 'battery') ...{
                    "batteryVoltage": "12",
                    "batteryCapacity": "125",
                    "voltageUnit": "Volts",
                    "capacityUnit": "Ah",
                    "batteryType": "Lithium",
                    "totalCapacityUOM": "kWh",
                  },
                  if (type == 'inverter') ...{
                    "totalCapacityUOM": spec.totalCapacityUnit,
                    "inverterCapacity": saved.itemNumber,
                    "invertorCapacityUnit": "kVA",
                    "voltageUnit": "Volts",
                    "currentUnit": "1",
                  },
                },
                if (userType == USER_TYPES.SUPERVISOR) ...{
                  "warrantyStartDate": startIso,
                  "warrantyDuration":
                      parseWarrantyYears(detail.warranty!), // years,
                  "warrantyEndDate": endIso,
                  "modelNumber": detail.model,
                } else ...{
                  //todo to be removed completely as only supervisors can submit below task
                  "warrantyStartDate": "2025-06-26T09:05:44.877103Z",
                  "warrantyDuration": "25",
                  "warrantyEndDate": "2050-06-20T09:05:44.877103Z",
                  "modelNumber": detail.model ?? "",
                },
                "wfStatus": "CREATED",
                "isActive": true,
                "documents": documents,
              }
            }
          };

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
                userType == USER_TYPES.SUPERVISOR ? startIso : null,
            warrantyDuration: userType == USER_TYPES.SUPERVISOR
                ? parseWarrantyYears(detail.warranty!)
                : null,
            warrantyEndDate: userType == USER_TYPES.SUPERVISOR ? endIso : null,
            modelNumber: detail.model,
            wfStatus: "CREATED",
            isActive: true,
            documents: documents,
          );

          await repo.createOrUpdateAsset(asset: assetModel, isar: _isar);

          // final payload = {
          //   'assetDetail': {
          //     'Asset': assetModel.toJson(),
          //   },
          // };
          //
          // await repo.createOrUpdateAsset(
          //     payload: payload, assetId: saved.assetId, isar: _isar);
        }
      }

      final remoteRepo = ProjectRemoteRepository();
      // await remoteRepo.updateProjectWorkflow(
      //   projectId: projectId,
      //   action: WORKFLOW_ACTIONS.CREATE_AND_SAVE_DRAFT.name,
      // );
      final completionDocuments = <Document>[];
      final completionReport = await _isar.cacheCompletionReports
          .where()
          .projectIdEqualTo(projectId)
          .findFirst();
      if (completionReport != null) {
        if (completionReport.filePath.isNotEmpty) {
          final completionFile = File(completionReport.filePath);
          if (await completionFile.exists()) {
            final photoId = await repo.uploadFile(completionFile);
            // final geo = <String, dynamic>{};
            // if (completionReport.latitude.isNotEmpty &&
            //     completionReport.longitude.isNotEmpty) {
            //   geo['latitude'] = completionReport.latitude;
            //   geo['longitude'] = completionReport.longitude;
            // }
            completionDocuments.add(
                //     {
                //   "documentType": "INSTALLATION REPORT",
                //   "fileStore": photoId,
                //   "documentUid": "INSTALLATION-REPORT-${photoId}",
                //   if (geo.isNotEmpty) "geoLocation": geo,
                // }
                Document(
              // id: 'DOCUMENT-0199',
              documentType: "INSTALLATION REPORT",
              fileStore: photoId,
              documentUid: "INSTALLATION-REPORT-${photoId}",
              // additionalDetails: null,
              geoLocation: GeoLocation(
                latitude: completionReport.latitude,
                longitude: completionReport.longitude,
                //additionalDetails: null,
              ),
            ));
          }
        }
        await remoteRepo.updateProjectWorkflow(
          projectId: projectId,
          action: userType == USER_TYPES.FIELD_STAFF
              ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
              : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
          documents: completionDocuments,
        );
      } else {
        await remoteRepo.updateProjectWorkflow(
          projectId: projectId,
          action: userType == USER_TYPES.FIELD_STAFF
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
