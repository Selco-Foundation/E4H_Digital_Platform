// lib/blocs/asset_submission/asset_submission_event.dart

// lib/blocs/asset_submission/asset_submission_bloc.dart

import 'dart:async';
import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:dio/dio.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:selco/data/nosql/cache_add_new_asset.dart';
import 'package:selco/data/nosql/cache_asset_detail.dart';
import 'package:selco/data/nosql/cache_media_upload.dart';
import 'package:selco/data/nosql/cache_specification.dart';

import '../../repositories/assetRepo.dart';
import '../../utils/utils.dart';

part 'asset_submission.freezed.dart';

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

  // FutureOr<void> _onSubmitAll(
  //   _SubmitAll event,
  //   Emitter<AssetSubmissionState> emit,
  // ) async {
  //   emit(const AssetSubmissionState.loading());
  //
  //   try {
  //     // get the facilityId
  //
  //     // 1) Fetch cached assets from Isar
  //     final allAssets = await _fetchAllCachedAssets(event.projectId);
  //
  //     if (allAssets.isEmpty) {
  //       emit(const AssetSubmissionState.failure(
  //           "No assets found in cache for this project."));
  //       return;
  //     }
  //
  //     // 2) Construct an AssetRepository instance (using global EnvConfig)
  //     final repo = AssetRepository();
  //
  //     // 3) Loop over each cached asset, upload → create
  //     for (final saved in allAssets) {
  //       final localPath = saved.photoPath;
  //       if (localPath == null || localPath.isEmpty) {
  //         continue; // no image, skip
  //       }
  //
  //       final fileOnDisk = File(localPath);
  //       if (!await fileOnDisk.exists()) {
  //         continue; // file does not exist, skip
  //       }
  //
  //       // 3a) Upload photo → get fileStoreId
  //       late final String fileStoreId;
  //       try {
  //         fileStoreId = await repo.uploadFile(fileOnDisk);
  //       } catch (e) {
  //         emit(AssetSubmissionState.failure(
  //             "Failed to upload file for ${saved.serialNumber}: $e"));
  //         return;
  //       }
  //
  //       // 3b) Build the JSON payload for “create asset”.
  //       //     Fill in all required fields. This skeleton assumes you provided
  //       //     everything via the event. Adjust as your API expects.
  //       final createPayload = <String, dynamic>{
  //         "assetDetail": {
  //           "Asset": {
  //             "tenantId": "pg", // envConfig.variables.tenantId,
  //             "facilityID": "FAC/2025/000106",
  //             "assetTypeID": saved.assetType.toUpperCase(),
  //             "system": event.systemCode,
  //             "serialNumber": saved.serialNumber,
  //             "modelNumber": event.modelNumber ?? "",
  //             "brandID": event.brandId ?? "",
  //             "assetDetails": {
  //               "totalCapacity": event.totalCapacity ?? 0.0,
  //               "totalCapacityUnit": event.totalCapacityUnit ?? "",
  //               "panelCapacity": event.panelCapacity ?? 0.0,
  //               "capacityUnit": event.capacityUnit ?? "",
  //             },
  //             "warrantyStartDate": event.warrantyStartDate ?? "",
  //             "warrantyDuration": event.warrantyDuration ?? 0,
  //             "warrantyEndDate": event.warrantyEndDate ?? "",
  //             "wfStatus": "CREATED",
  //             "isActive": true,
  //             "documents": [
  //               {
  //                 "documentType": "PHOTO",
  //                 "fileStore": fileStoreId,
  //                 "documentUid": "DOC-${saved.serialNumber}",
  //                 "additionalDetails": {},
  //                 "geoLocation": {
  //                   "latitude": saved.latitude,
  //                   "longitude": saved.longitude
  //                 },
  //               }
  //             ],
  //             "additionalDetails": event.additionalDetails ?? {}
  //           }
  //         }
  //       };
  //
  //       // 3c) Actually call createAsset
  //       try {
  //         await repo.createAsset(createPayload);
  //       } catch (err) {
  //         String errorMessage = 'Unknown error occurred';
  //         if (err is DioException) {
  //           errorMessage = err.response?.data?['error_description'] ??
  //               err.response?.data?['error'] ??
  //               err.message ??
  //               'Network error occurred';
  //         } else if (err is Exception) {
  //           errorMessage = err.toString();
  //         }
  //         emit(AssetSubmissionState.failure(
  //             "Failed to create asset for ${saved.serialNumber}: ${errorMessage}"));
  //         return;
  //       }
  //     }
  //
  //     // 4) If we reach here, everything succeeded
  //     emit(const AssetSubmissionState.success());
  //   } catch (e) {
  //     emit(AssetSubmissionState.failure(e.toString()));
  //   }
  // }

  FutureOr<void> _onSubmitAll(
    _SubmitAll event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    emit(const AssetSubmissionState.loading());
    try {
      final projectId = event.projectId;
      final facilityId = "FAC/2025/000106";

      final repo = AssetRepository();
      // Define types
      const types = ['inverter', 'battery', 'panel'];

      for (final type in types) {
        // 1) Fetch cached assets
        final assets = await _isar.cacheAddNewAssets
            .where()
            .projectIdEqualTo(projectId)
            .filter()
            .assetTypeEqualTo(type)
            .findAll();
        if (assets.isEmpty) {
          emit(AssetSubmissionState.failure(
              "No cached assets found for type $type."));
          return;
        }
        // 2) Fetch spec & detail
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
          return;
        }
        // 3) For each asset of this type
        for (final saved in assets) {
          final localPath = saved.photoPath;
          if (localPath.isEmpty) continue;
          final fileOnDisk = File(localPath);
          if (!await fileOnDisk.exists()) continue;

          // 3a) Upload main photo
          late String photoFileStoreId;
          try {
            photoFileStoreId = await repo.uploadFile(fileOnDisk);
          } catch (e) {
            emit(AssetSubmissionState.failure(
                "Failed to upload photo for ${saved.serialNumber} ($type): $e"));
            return;
          }

          // 3b) Fetch any other cached media for this asset to include in documents
          // For example, invoices or warranty cards stored in CacheMediaUpload.
          // Assume CacheMediaUpload.itemNumber matches saved.serialNumber, or adjust as needed.
          final mediaEntries = await _isar.cacheMediaUploads
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .and()
              .itemNumberEqualTo(saved.serialNumber)
              .findAll();

          // Build documents array: start with any additional docs, then the photo
          final List<Map<String, dynamic>> documents = [];

          // a) Add other media entries first
          for (final m in mediaEntries) {
            // Upload each file
            late String mediaFileStoreId;
            try {
              final mediaFile = File(m.photoPath);
              if (!await mediaFile.exists()) {
                continue;
              }
              mediaFileStoreId = await repo.uploadFile(mediaFile);
            } catch (e) {
              emit(AssetSubmissionState.failure(
                  "Failed to upload document for ${saved.serialNumber} ($type): $e"));
              return;
            }
            // Geo if present
            final geoMap = <String, dynamic>{};
            if (m.latitude.isNotEmpty && m.longitude.isNotEmpty) {
              geoMap['latitude'] = m.latitude;
              geoMap['longitude'] = m.longitude;
            }
            // Determine documentType and additionalDetails from m.itemType or other fields.
            // E.g., if m.itemType == 'INVOICE', additionalDetails might come from CacheMediaUpload?
            Map<String, dynamic> additionalDetails = {};
            // If CacheMediaUpload has fields for invoiceNumber etc, include here.
            // For now assume none; adapt if you store those in cache.
            documents.add({
              "documentType": m.itemType, // e.g. "INVOICE" or "WARRANTY_CARD"
              "fileStore": mediaFileStoreId,
              "documentUid": "DOC-${m.itemType}-${saved.serialNumber}",
              if (geoMap.isNotEmpty) "geoLocation": geoMap,
              if (additionalDetails.isNotEmpty)
                "additionalDetails": additionalDetails,
            });
          }

          // b) Add main photo as a document (type "PHOTO")
          final photoGeo = <String, dynamic>{};
          if (saved.latitude.isNotEmpty && saved.longitude.isNotEmpty) {
            photoGeo['latitude'] = saved.latitude;
            photoGeo['longitude'] = saved.longitude;
          }
          documents.add({
            "documentType": "PHOTO",
            "fileStore": photoFileStoreId,
            "documentUid": "DOC-PHOTO-${saved.serialNumber}",
            if (photoGeo.isNotEmpty) "geoLocation": photoGeo,
            "additionalDetails": {}, // or include if needed
          });

          // 3c) Prepare warranty dates: if detail.warranty is a duration string or date string?
          // Suppose detail.warranty stores duration in months as string; use now + duration.
          final now = DateTime.now().toUtc();
          final durYear = parseWarrantyYears(detail.warranty);
          final isoStart = now.toIso8601String();
          final isoEnd =
              now.add(Duration(days: 30 * 12 * durYear)).toIso8601String();

          // 3d) Build Asset map matching sample payload
          final assetMap = <String, dynamic>{
            "tenantId": 'pg', // or envConfig.variables.tenantId
            "facilityID": facilityId,
            "assetTypeID": type.toUpperCase(),
            "system": spec.system,
            "serialNumber": saved.serialNumber,
            "modelNumber": detail.model,
            "brandID": detail.brand,
            "assetDetails": {
              "totalCapacity": spec.totalCapacity,
              "totalCapacityUnit": spec.totalCapacityUnit,
              "totalCapacityUOM": spec.totalCapacityUnit,
              // If panel-specific fields:
              if (type == 'PANEL') ...{
                "panelCapacity": saved.itemNumber,
                "capacityUnit": 'Wp',
              },
              if (type == 'BATTERY') ...{
                "batteryVoltage": '1',
                "batteryCapacity": '125',
                "voltageUnit": 'Volts',
                "capacityUnit": 'Ah',
                "batteryType": 'Lithium'
              },
              if (type == 'INVERTER') ...{
                // inverterDetails.setCurrentUnit((String) map.get("currentUnit"));
                // inverterDetails.setVoltageUnit((String) map.get("voltageUnit"));
                // String inverterCap = (String) map.get("invertorCapacity");
                // if (inverterCap == null) {
                // inverterCap = (String) map.get("inverterCapacity");
                // }
                // inverterDetails.setInverterCapacity(inverterCap);
                //
                // inverterDetails.setInverterCapacityUnit((String) map.get("invertorCapacityUnit"));
                // inverterDetails.setOutputPhase((String) map.get("outputPhase"));
                //
                // inverterDetails.setChargeControllerCurrent(getDoubleValue(map.get("chargeControllerCurrent")));
                // inverterDetails.setChargeControllerVoltage(getDoubleValue(map.get("chargeControllerVoltage")));

                "currentUnit": '1',
                "voltageUnit": 'Volts',
                "inverterCapacity": saved.itemNumber,
                "invertorCapacityUnit": 'kVA',
                "outputPhase": '',
                "chargeControllerCurrent": '',
                "chargeControllerVoltage": ''
              }
            },
            "warrantyStartDate": isoStart,
            "warrantyDuration": durYear,
            "warrantyEndDate": isoEnd,
            "wfStatus": "CREATED",
            "isActive": true,
            "documents": documents,
            "additionalDetails": {
              // You can fetch additionalDetails from another cache if you have
            },
          };

          final fullPayload = {
            "assetDetail": {"Asset": assetMap},
          };

          // 3e) Send create request
          try {
            // Use data: fullPayload so Dio serializes JSON
            await repo.createAsset(fullPayload);
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
                "Failed to create asset for ${saved.serialNumber}: ${errorMessage}"));
            return;
          }
        }
      }

      // If all loops succeed:
      emit(const AssetSubmissionState.success());
    } catch (e) {
      emit(AssetSubmissionState.failure(e.toString()));
    }
  }
}

@freezed
class AssetSubmissionEvent with _$AssetSubmissionEvent {
  const factory AssetSubmissionEvent.submitAll({
    required String projectId,
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
