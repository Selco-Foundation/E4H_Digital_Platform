import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:http_parser/http_parser.dart';
import 'package:isar/isar.dart';
import 'package:mime/mime.dart';

import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../data/nosql/cache_media_upload.dart';
import '../data/nosql/cache_specification.dart';
import '../data/remote_client.dart';
import '../model/asset/asset.dart';
import '../model/document/document.dart';
import '../model/entities/project_facility.dart';
import '../model/project_workflow/project_workflow.dart';
import '../model/transaction/transaction.dart';
import '../repositories/project_facility_repo.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'project_repo.dart';

class FileStoreResponse {
  final String fileStoreId;
  FileStoreResponse({required this.fileStoreId});

  factory FileStoreResponse.fromJson(Map<String, dynamic> json) {
    final files = json['files'] as List<dynamic>?;
    if (files == null || files.isEmpty) {
      throw Exception("Filestore returned no files array.");
    }

    final first = files.first as Map<String, dynamic>;
    return FileStoreResponse(fileStoreId: first['fileStoreId'] as String);
  }
}

class AssetRepository {
  AssetRepository() {
    _dio.options.baseUrl = envConfig.variables.baseUrl;
  }

  final Dio _dio = DioClient().dio;

  Future<String> uploadFile(File file) async {
    String fileName = file.path.split(Platform.pathSeparator).last;
    String? mimeType = lookupMimeType(fileName);

    // Determine MIME type from content if needed
    if (mimeType == null) {
      try {
        final bytes = await file.readAsBytes();
        mimeType = lookupMimeType('', headerBytes: bytes);
      } catch (e) {
        print("Error reading file for MIME type: $e");
      }
    }

    // Handle extension for files without one
    if (!fileName.contains('.')) {
      final ext = getExtensionFromMime(mimeType ?? 'application/octet-stream');
      fileName = '$fileName.$ext';
    }

    final formData = FormData.fromMap({
      "file": await MultipartFile.fromFile(
        file.path,
        filename: fileName,
        contentType: mimeType != null ? MediaType.parse(mimeType) : null,
      ),
      "tenantId": envConfig.variables.tenantId,
      "module": "Incident",
    });

    print("formdata $formData");
    final tenantId = formData.fields
        .firstWhere(
          (field) => field.key == "tenantId",
          orElse: () => const MapEntry("tenantId", "NOT_FOUND"),
        )
        .value;

    print("tenantId: $tenantId");
    try {
      final response = await _dio.post("/filestore/v1/files", data: formData);
      if (response.statusCode == 200 || response.statusCode == 201) {
        return FileStoreResponse.fromJson(response.data).fileStoreId;
      } else {
        throw Exception(
            "Filestore responded with status ${response.statusCode}");
      }
    } on DioError catch (e) {
      throw DioErrorParser.parse(e);
    }
  }

  /// Creates or updates an asset on the server, then writes back the returned
  /// assetId into Isar and returns the server's canonical Asset model.
  Future<Asset> createOrUpdateAsset({
    required Asset asset,
    required Isar isar,
    required facilityId,
  }) async {
    // Determine create vs update
    final isCreate = asset.assetId == null || asset.assetId!.isEmpty;
    final endpoint = isCreate ? '_create' : '_update?assetID=${asset.assetId}';

    // Build the nested payload
    final payload = {
      'assetDetail': {
        'Asset': asset.toJson(),
      },
    };

    try {
      print('Request payload: ${jsonEncode(payload)}');
      final response =
          await _dio.post('/asset-registry/v1/asset/$endpoint', data: payload);
      print('Response status: ${response.statusCode}');
      print('Response body: ${response.data}');

      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception(
          '${isCreate ? 'Create' : 'Update'} Asset responded with status '
          '${response.statusCode}',
        );
      }

      // The API may wrap the asset under "asset" or "Asset"
      final data = response.data as Map<String, dynamic>;
      final assetJson = data['asset'] ?? data['Asset'];
      if (assetJson == null) {
        throw Exception('Asset data missing in response');
      }

      // Parse the returned JSON into our model
      final updatedAsset = Asset.fromJson(
        Map<String, dynamic>.from(assetJson as Map),
      );

      print("updatedAsset $updatedAsset");
      print("updated AssetId ${updatedAsset.assetId}");

      if ((updatedAsset.assetId ?? '').isNotEmpty) {
        await _writeBackAssetIdToCache(isar: isar, asset: updatedAsset);
      }

      return updatedAsset;
    } on DioError catch (e) {
      final code = _errorCodeFromDio(e);
      final isDuplicate = code == 'ERR_ASSET_DUPLICATE_VALIDATION';

      print("Starting Duplicate Fetch and resending");
      if (isCreate && isDuplicate) {
        final remote = await _fetchAssetBySerial(
          facilityId: facilityId,
          serialNumber: asset.serialNumber ?? '',
        );

        final remoteAssetId =
            (remote?['assetId'] ?? remote?['assetID'] ?? '').toString();

        if (remoteAssetId.isNotEmpty) {
          await _writeBackAssetIdToCache(
            isar: isar,
            asset: asset.copyWith(assetId: remoteAssetId),
          );

          final retryAssetMap = Map<String, dynamic>.from(asset.toJson());
          retryAssetMap['assetId'] = remoteAssetId;
          final updatePayload = {
            'assetDetail': {
              'Asset': retryAssetMap,
            },
          };

          final updateResp = await _dio.post(
            '/asset-registry/v1/asset/_update?assetID=$remoteAssetId',
            data: updatePayload,
          );
          print('Request payload: ${jsonEncode(payload)}');
          if (updateResp.statusCode == 200 || updateResp.statusCode == 201) {
            final m = updateResp.data as Map<String, dynamic>;
            final aj = m['asset'] ?? m['Asset'];
            final updated =
                Asset.fromJson(Map<String, dynamic>.from(aj as Map));
            return updated;
          }
        }
      }
      print("error message in duplicate ${e.message}");
      throw DioErrorParser.parse(e);
    }
  }

  /// Fetch remote assets and upsert into all your Isar caches,
  /// clearing only the matching CacheAddNewAsset per serialNumber,
  /// and wholesale-clearing media before re-inserting.
  Future<void> syncRemoteToLocal(
      {required ProjectWorkflow project,
      required String projectId,
      required String userType,
      required Isar isar}) async {
    try {
      final draft = await PrefilledProjectRepository(isar).exists(
        projectId: projectId,
        userType: userType,
      );
      if (draft) return;

      // 1) facilityId lookup
      final facilityId = (await ProjectFacilityRepository().search(
        ProjectFacilitySearchModel(projectId: [projectId]),
        isar,
      ))
          .facilityId;
      print("projectId $projectId");
      print("facilityId $facilityId");

      // 2) fetch JSON, parse into Asset models
      final resp = await _dio.post(
        '/asset-registry/v1/asset/_search?tenantId=${envConfig.variables.tenantId}',
        data: {
          'criteria': {
            'tenantId': envConfig.variables.tenantId,
            'facilityID': facilityId,
          }
        },
      );
      print("resp ${resp.data}");
      if (resp.statusCode != 200 && resp.statusCode != 201) {
        throw Exception('Failed to fetch assets');
      }
      final List<dynamic> rawList = resp.data as List<dynamic>;

      // parse each raw Map into our Asset model
      final assets = rawList
          .cast<Map<String, dynamic>>()
          .map((m) => Asset.fromJson(m))
          .toList();

      // 3) group by assetTypeID
      final byType = <String, List<Asset>>{};
      for (var asset in assets) {
        final type = asset.assetTypeID?.toLowerCase() ?? 'unknown';
        byType.putIfAbsent(type, () => []).add(asset);
      }

      print("byType $byType");

      // 4) perform one big Isar transaction
      await isar.writeTxn(() async {
        for (var entry in byType.entries) {
          final type = entry.key;
          final list = entry.value;

          print("entry $entry");
          print("type $type");
          print("assets $assets");

          // — upsert count
          final countValue = list.length;
          print("countValue $countValue of ${entry.key}");
          var countEntry = await isar.cacheAssetCounts
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findFirst();
          print("existingCount $countEntry");
          if (countEntry != null) {
            countEntry
              ..count = countValue
              ..updatedAt = DateTime.now();
            await isar.cacheAssetCounts.put(countEntry);
          } else {
            await isar.cacheAssetCounts.put(
              CacheAssetCount(
                projectId: projectId,
                assetType: type,
                count: countValue,
              ),
            );
          }

          // — upsert spec & detail
          final first = list.first;
          final det = first.assetDetails!;
          final spec = CacheSpecification(
            projectId: projectId,
            assetType: type,
            totalCapacity: det.totalCapacity ?? 0,
            totalCapacityUnit:
                det.totalCapacityUnit ?? det.totalCapacityUOM ?? '',
            system: first.system ?? '',
          );
          var specEntry = await isar.cacheSpecifications
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findFirst();
          print("specEntry $specEntry");
          if (specEntry != null) {
            specEntry
              ..system = spec.system
              ..totalCapacity = spec.totalCapacity
              ..totalCapacityUnit = spec.totalCapacityUnit
              ..updatedAt = DateTime.now();
            await isar.cacheSpecifications.put(specEntry);
          } else {
            await isar.cacheSpecifications.put(spec);
          }

          final detail = CacheAssetDetail(
            projectId: projectId,
            assetType: type,
            brand: first.brandID ?? '',
            model: first.modelNumber ?? '',
            warranty: first.warrantyDuration?.toString(),
          );
          var detailEntry = await isar.cacheAssetDetails
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findFirst();
          print("detailEntry $detailEntry");
          if (detailEntry != null) {
            detailEntry
              ..brand = detail.brand
              ..model = detail.model
              ..warranty = detail.warranty
              ..updatedAt = DateTime.now();
            await isar.cacheAssetDetails.put(detailEntry);
          } else {
            await isar.cacheAssetDetails.put(detail);
          }

          // — upsert each asset’s “main photo” in CacheAddNewAsset
          for (var asset in list) {
            final serial = asset.serialNumber ?? '';
            // delete old by serial
            final oldList = await isar.cacheAddNewAssets
                .where()
                .projectIdEqualTo(projectId)
                .filter()
                .assetTypeEqualTo(type)
                .and()
                .serialNumberEqualTo(serial)
                .findAll();
            for (var old in oldList) {
              await isar.cacheAddNewAssets.delete(old.id);
            }

            // find all PHOTO documents
            for (var doc in asset.documents ?? []) {
              if (doc.documentType == 'PHOTO' || doc.documentType == 'ASSET') {
                await isar.cacheAddNewAssets.put(
                  CacheAddNewAsset(
                    assetId: asset.assetId,
                    projectId: projectId,
                    assetType: type,
                    itemNumber:
                        asset.assetDetails?.inverterCapacity?.toString() ?? '',
                    serialNumber: serial ?? '',
                    photoPath: doc.fileStore ?? '',
                    latitude: doc.geoLocation?.latitude?.toString() ?? '',
                    longitude: doc.geoLocation?.longitude?.toString() ?? '',
                    capacityUnit: asset.assetDetails?.capacityUnit ?? '',
                    panelCapacity:
                        asset.assetDetails?.panelCapacity?.toString() ?? '',
                    batteryCapacity:
                        asset.assetDetails?.batteryCapacity?.toString() ?? '',
                    batteryVoltage:
                        asset.assetDetails?.batteryVoltage?.toString() ?? '',
                    batteryType: asset.assetDetails?.batteryType ?? '',
                    voltageUnit: asset.assetDetails?.voltageUnit ?? '',
                    inverterCapacity:
                        asset.assetDetails?.inverterCapacity.toString() ?? '',
                    inverterCapacityUnit:
                        asset.assetDetails?.inverterCapacityUnit ?? '',
                  ),
                );
              }
            }
          }

          // — clear & re‑insert media uploads
          final oldMedia = await isar.cacheMediaUploads
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findAll();
          for (var m in oldMedia) {
            await isar.cacheMediaUploads.delete(m.id);
          }
        }

        print("project.workflow ${project.workflow}");
        print("Documents ${project.workflow?.documents}");
        for (var doc in project.workflow?.documents ?? []) {
          if (doc.documentType != 'ASSET' &&
              doc.documentType != 'PHOTO' &&
              doc.documentType != 'INSTALLATION_REPORT') {
            // we only want strings like "inverter-image", i.e. exactly two parts
            final parts = doc.documentType?.split('-') ?? [];
            if (parts.length != 2) continue;

            final assetTypeFromDoc = parts[0];
            print("assetTypeFromDoc $assetTypeFromDoc");
            final itemTypeFromDoc = parts[1];
            print("itemTypeFromDoc $itemTypeFromDoc");

            await isar.cacheMediaUploads.put(
              CacheMediaUpload(
                userType: userType,
                projectId: projectId,
                assetType: assetTypeFromDoc,
                itemNumber: '',
                itemType: itemTypeFromDoc ?? '',
                filePath: doc.fileStore ?? '',
                latitude: doc.geoLocation?.latitude?.toString() ?? '',
                longitude: doc.geoLocation?.longitude?.toString() ?? '',
              ),
            );
          }
        }
      });
    } on DioError catch (e) {
      print(e);
      throw DioErrorParser.parse(e);
    }
  }

  Future<void> submitRejection({
    required String projectId,
    String action = "REJECT",
    List<Document>? documents,
    required List<Transaction> transactions,
  }) async {
    final payload = {
      'projectId': projectId,
      'workflow': {
        'action': action,
        if (documents != null) ...{
          'documents': documents.map((d) => d.toJsonForWorkflow()).toList()
        }
      },
      'transactions': transactions.toList()
    };

    print("payload ${jsonEncode(payload)}");

    try {
      final resp = await _dio.post('/project/v1/project/workflow/update',
          data: payload,
          options: Options(contentType: Headers.jsonContentType));
      if (resp.statusCode != 200 &&
          resp.statusCode != 201 &&
          resp.statusCode != 204) {
        throw Exception('Rejection Failed with ${resp.statusCode}');
      }
    } on DioError catch (dioErr) {
      final msg = dioErr.response?.data?.toString() ?? dioErr.message;
      throw DioErrorParser.parse(dioErr);
    }
  }

  // Add this helper to AssetRepository
  Future<Map<String, dynamic>?> _fetchAssetBySerial({
    required String facilityId,
    required String serialNumber,
  }) async {
    final resp = await _dio.post(
      '/asset-registry/v1/asset/_search?tenantId=${envConfig.variables.tenantId}',
      data: {
        'criteria': {
          'tenantId': envConfig.variables.tenantId,
          'facilityID': facilityId,
          'serialNumber': serialNumber,
        }
      },
    );

    if (resp.statusCode == 200 || resp.statusCode == 201) {
      final data = resp.data;
      if (data is List) {
        return data.cast<Map<String, dynamic>?>().firstWhere(
              (m) => (m?['serialNumber'] ?? '') == serialNumber,
              orElse: () => null,
            );
      }
      // if (data is Map && data['assets'] is List) {
      //   final list = (data['assets'] as List).cast<Map<String, dynamic>>();
      //   return list.firstWhere(
      //     (m) => (m['serialNumber'] ?? '') == serialNumber,
      //     orElse: () => null,
      //   );
      // }
    }
    return null;
  }

// Optional: robust error-code extractor
  String? _errorCodeFromDio(DioException e) {
    try {
      final data = e.response?.data;
      if (data is Map &&
          data['Errors'] is List &&
          (data['Errors'] as List).isNotEmpty) {
        final first = (data['Errors'] as List).first;
        if (first is Map && first['code'] is String)
          return first['code'] as String;
      }
      if (data is List &&
          data.isNotEmpty &&
          data.first is Map &&
          (data.first as Map)['code'] is String) {
        return (data.first as Map)['code'] as String;
      }
    } catch (_) {}
    return null;
  }

  Future<void> _writeBackAssetIdToCache({
    required Isar isar,
    required Asset asset,
  }) async {
    final typeKey = (asset.assetTypeID ?? '').toLowerCase();
    final serial = asset.serialNumber ?? '';
    if (typeKey.isEmpty || serial.isEmpty || (asset.assetId ?? '').isEmpty)
      return;

    await isar.writeTxn(() async {
      final existing = await isar.cacheAddNewAssets
          .where()
          .assetTypeEqualTo(typeKey)
          .filter()
          .serialNumberEqualTo(serial)
          .findFirst();
      if (existing != null) {
        existing.assetId = asset.assetId;
        await isar.cacheAddNewAssets.put(existing);
      }
    });
  }
}
