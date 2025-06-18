// lib/blocs/asset_submission/asset_submission_event.dart

// lib/blocs/asset_submission/asset_submission_bloc.dart

import 'dart:async';
import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:dio/dio.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:selco/data/nosql/cache_add_new_asset.dart';

import '../../repositories/app_init_Repo.dart';
import '../../repositories/assetRepo.dart';

part 'asset_submission.freezed.dart';

/// AssetSubmissionBloc coordinates:
///  1. Fetching all CacheAddNewAsset entries for a project from Isar
///  2. Uploading each asset’s photo to filestore (via AssetRepository.uploadFile)
///  3. Sending a createPayload to AssetRepository.createAsset
///  4. Emitting success or failure states accordingly
class AssetSubmissionBloc
    extends Bloc<AssetSubmissionEvent, AssetSubmissionState> {
  final Isar _isar;

  AssetSubmissionBloc(this._isar)
      : super(const AssetSubmissionState.initial()) {
    on<_SubmitAll>(_onSubmitAll);
  }

  /// Reads from Isar: all CacheAddNewAsset where projectId == [projectId]
  Future<List<CacheAddNewAsset>> _fetchAllCachedAssets(String projectId) async {
    return await _isar.cacheAddNewAssets
        .where()
        .projectIdEqualTo(projectId)
        .findAll();
  }

  FutureOr<void> _onSubmitAll(
    _SubmitAll event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    emit(const AssetSubmissionState.loading());

    try {
      // 1) Fetch cached assets from Isar
      final allAssets = await _fetchAllCachedAssets(event.projectId);

      if (allAssets.isEmpty) {
        emit(const AssetSubmissionState.failure(
            "No assets found in cache for this project."));
        return;
      }

      // 2) Construct an AssetRepository instance (using global EnvConfig)
      final baseUrl = envConfig.variables.baseUrl;
      final repo = AssetRepository(
        tenantId: event.tenantId,
        authToken: event.authToken,
      );

      // 3) Loop over each cached asset, upload → create
      for (final saved in allAssets) {
        final localPath = saved.photoPath;
        if (localPath == null || localPath.isEmpty) {
          continue; // no image, skip
        }

        final fileOnDisk = File(localPath);
        if (!await fileOnDisk.exists()) {
          continue; // file does not exist, skip
        }

        // 3a) Upload photo → get fileStoreId
        late final String fileStoreId;
        try {
          fileStoreId = await repo.uploadFile(fileOnDisk);
        } catch (e) {
          emit(AssetSubmissionState.failure(
              "Failed to upload file for ${saved.serialNumber}: $e"));
          return;
        }

        // 3b) Build the JSON payload for “create asset”.
        //     Fill in all required fields. This skeleton assumes you provided
        //     everything via the event. Adjust as your API expects.
        final createPayload = <String, dynamic>{
          "assetDetail": {
            "Asset": {
              "tenantId": event.tenantId,
              "facilityID": event.facilityId,
              "assetTypeID": saved.assetType.toUpperCase(),
              "system": event.systemCode,
              "serialNumber": saved.serialNumber,
              "modelNumber": event.modelNumber ?? "",
              "brandID": event.brandId ?? "",
              "assetDetails": {
                "totalCapacity": event.totalCapacity ?? 0.0,
                "totalCapacityUnit": event.totalCapacityUnit ?? "",
                "panelCapacity": event.panelCapacity ?? 0.0,
                "capacityUnit": event.capacityUnit ?? "",
              },
              "warrantyStartDate": event.warrantyStartDate ?? "",
              "warrantyDuration": event.warrantyDuration ?? 0,
              "warrantyEndDate": event.warrantyEndDate ?? "",
              "wfStatus": "CREATED",
              "isActive": true,
              "documents": [
                {
                  "documentType": "PHOTO",
                  "fileStore": fileStoreId,
                  "documentUid": "DOC-${saved.serialNumber}",
                  "additionalDetails": {}
                }
              ],
              "additionalDetails": event.additionalDetails ?? {}
            }
          }
        };

        // 3c) Actually call createAsset
        try {
          await repo.createAsset(createPayload);
        } catch (err) {
          String errorMessage = 'Unknown error occurred';
          if (err is DioException) {
            errorMessage = err.response?.data?['error_description'] ??
                err.response?.data?['error'] ??
                err.message ??
                'Network error occurred';
          } else if (err is Exception) {
            errorMessage = err.toString();
          }
          emit(AssetSubmissionState.failure(
              "Failed to create asset for ${saved.serialNumber}:"));
          return;
        }
      }

      // 4) If we reach here, everything succeeded
      emit(const AssetSubmissionState.success());
    } catch (e) {
      emit(AssetSubmissionState.failure(e.toString()));
    }
  }
}

@freezed
class AssetSubmissionEvent with _$AssetSubmissionEvent {
  /// Submits all cached assets for the given [projectId]. You must supply:
  ///  - [authToken], [tenantId], [facilityId], [systemCode], etc.
  ///  - Any other fields your “create asset” JSON needs.
  const factory AssetSubmissionEvent.submitAll({
    required String projectId,
    required String authToken,
    required String tenantId,
    required String facilityId,
    required String systemCode,
    String? modelNumber,
    String? brandId,
    double? totalCapacity,
    String? totalCapacityUnit,
    double? panelCapacity,
    String? capacityUnit,
    String? warrantyStartDate,
    int? warrantyDuration,
    String? warrantyEndDate,
    Map<String, dynamic>? additionalDetails,
    Map<String, dynamic>? userInfo,
  }) = _SubmitAll;
}

@freezed
class AssetSubmissionState with _$AssetSubmissionState {
  /// Initial: not yet started
  const factory AssetSubmissionState.initial() = _Initial;

  /// In progress: uploading/creating
  const factory AssetSubmissionState.loading() = _Loading;

  /// All assets successfully created
  const factory AssetSubmissionState.success() = _Success;

  /// Some failure occurred; [errorMessage] explains why.
  const factory AssetSubmissionState.failure(String errorMessage) = _Failure;
}
