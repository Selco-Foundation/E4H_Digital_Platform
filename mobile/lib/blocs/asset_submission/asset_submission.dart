import 'dart:async';

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
        fromDraft: true,
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

        final documents = <Document>[];
        for (final saved in assets) {
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
            auditDetails: (saved.assetId?.isNotEmpty ?? false) ? audit : null,
          );
          print(
              "assetModel audit ${assetModel.auditDetails?.toJson() ?? '— none —'}");
          print("assetModel $assetModel");
          await repo.createOrUpdateAsset(asset: assetModel, isar: _isar);
        }
      }

      print("about starting completion reports");

      final remoteRepo = ProjectRemoteRepository();
      final workflowDocuments = <Document>[];

      for (final type in types) {
        final mediaEntries = await _isar.cacheMediaUploads
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findAll();

        print("[$type] found ${mediaEntries.length} cached media uploads");
        for (var m in mediaEntries) {
          print(
              "    media id=${m.id} filePath='${m.filePath}' itemType='${m.itemType}' media id=${m.id} projectId='${m.projectId}'");
        }

        for (final m in mediaEntries) {
          if (m.filePath.isEmpty) continue;
          String mediaId = await getFilestoreUrl(m.filePath);
          print("mediaId $mediaId");
          workflowDocuments.add(Document(
            documentType: "${m.assetType}-${m.itemType}",
            fileStore: mediaId,
            documentUid:
                "DOC-${m.assetType}-${m.itemType}-${DateTime.now().toUtc().millisecondsSinceEpoch}",
            geoLocation: GeoLocation(
              latitude: m.latitude,
              longitude: m.longitude,
            ),
          ));
        }
        print("documents $workflowDocuments");
      }

      final completionReports = await _isar.cacheCompletionReports
          .where()
          .projectIdEqualTo(projectId)
          .findAll();

      final completionDocuments = <Document>[];

      for (final report in completionReports) {
        if (report.filePath.isEmpty) continue;
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
        emit(AssetSubmissionState.failure("Failed to attach BOM PDF: $e"));
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
        emit(AssetSubmissionState.failure("BOM submission error: $e"));
        return false;
      }

      await remoteRepo.updateProjectWorkflow(
        projectId: projectId,
        action: userType == USER_TYPES.FIELD_STAFF.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
        documents: [...workflowDocuments, ...completionDocuments],
      );

      await _draftRepo.delete(projectId, userType);
      await PrefilledProjectRepository(_isar).delete(
        projectId: projectId,
        userType: userType,
      );
      await CompletionReportRepository(_isar).delete(projectId: projectId);
      if (!fromDraft) emit(const AssetSubmissionState.success());
      return true;
    } catch (e) {
      print("e ${e.toString()}");
      String? errorMessage = "We are facing an issues please try again";
      if ((e.toString() == "Exception: No network connection") ||
          (e.toString() == "Exception: No internet access")) {
        errorMessage =
            "For some Reason you have bad internet connectivity, we have saved your data, please try to sync the data later";
      }
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
