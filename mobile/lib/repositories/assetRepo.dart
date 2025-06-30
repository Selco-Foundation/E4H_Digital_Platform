// lib/repositories/asset_repository.dart

import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:http_parser/http_parser.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../data/nosql/cache_media_upload.dart';
import '../data/nosql/cache_specification.dart';
import '../data/remote_client.dart';
import '../model/entities/project_facility.dart';
import '../repositories/project_facility_repo.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';

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
    final fileName = file.path.split(Platform.pathSeparator).last;
    final mimeType = _lookupMimeType(fileName);

    final formData = FormData.fromMap({
      "file": await MultipartFile.fromFile(
        file.path,
        filename: fileName,
        contentType: MediaType.parse(mimeType),
      ),
      "tenantId": envConfig.variables.tenantId,
      "module": "Incident", //todo confirm this is correct
    });

    try {
      final response = await _dio.post("/filestore/v1/files", data: formData);

      if (response.statusCode == 200 || response.statusCode == 201) {
        final jsonMap = response.data as Map<String, dynamic>;
        return FileStoreResponse.fromJson(jsonMap).fileStoreId;
      } else {
        throw Exception(
            "Filestore responded with status ${response.statusCode}");
      }
    } on DioError catch (e) {
      throw DioErrorParser.parse(e);
    }
  }

  Future<void> createOrUpdateAsset(
      {required Map<String, dynamic> payload, String? assetId}) async {
    try {
      final isCreate = (assetId != null && assetId.isNotEmpty) ? false : true;
      print(jsonEncode(payload)); //todo to be removed
      final url = isCreate ? "_create" : "$assetId/_update";
      print("url $url");
      final response =
          await _dio.post("/asset-registry/v1/asset/$url", data: payload);
      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception(
            "${isCreate ? 'Create' : 'Update'} Asset responded with status ${response.statusCode}");
      }
    } on DioError catch (e) {
      throw DioErrorParser.parse(e);
    }
  }

  // Future<void> syncRemoteToLocal(String projectId, Isar isar) async {
  //   final facilityId = (await ProjectFacilityRepository().search(
  //     ProjectFacilitySearchModel(projectId: [projectId]),
  //     isar,
  //   ))
  //       .facilityId;
  //
  //   final response = await _dio.post(
  //       "/asset-registry/v1/asset/_search?tenantId=${envConfig.variables.tenantId}",
  //       data: {
  //         "criteria": {
  //           "tenantId": "${envConfig.variables.tenantId}",
  //           "facilityID": "$facilityId"
  //         }
  //       });
  //
  //   if (response.statusCode != 200) {
  //     throw Exception("Failed to fetch project assets");
  //   }
  //
  //   final json = jsonDecode(response.data); // Expected: List/Map of assets
  //
  //   await isar.writeTxn(() async {
  //     for (final asset in json) {
  //       final type = asset['assetTypeID']?.toLowerCase();
  //
  //       // ✅ Add Specification
  //       final spec = CacheSpecification(
  //         projectId: projectId,
  //         assetType: type,
  //         totalCapacity: asset['assetDetails']?['totalCapacity'],
  //         totalCapacityUnit: asset['assetDetails']?['totalCapacityUnit'] ??
  //             asset['assetDetails']?['totalCapacityUOM'],
  //         system: asset['system'],
  //       );
  //       await isar.cacheSpecifications.put(spec);
  //
  //       // ✅ Add Detail
  //       final detail = CacheAssetDetail(
  //         projectId: projectId,
  //         assetType: type,
  //         model: asset['modelNumber'],
  //         brand: asset['brandID'],
  //         warranty: asset['warrantyDuration']?.toString(),
  //       );
  //       await isar.cacheAssetDetails.put(detail);
  //
  //       // ✅ Add Count
  //       final count = CacheAssetCount(
  //         projectId: projectId,
  //         assetType: type,
  //         count: asset['count'],
  //         progress: asset['progress'],
  //       );
  //       await isar.cacheAssetCounts.put(count);
  //
  //       // ✅ Add Media uploads (if any)
  //       for (final media in asset['documents']) {
  //         if (media['documentType'] == "ASSET") {
  //           // ✅ Add CacheAddNewAsset
  //           final newAsset = CacheAddNewAsset(
  //             projectId: projectId,
  //             assetType: type,
  //             itemNumber: asset[
  //                 'itemNumber'], // todo confirm and update should be index
  //             serialNumber: asset['serialNumber'],
  //             photoPath: media['fileStore'], // Optional
  //             latitude: media['geoLocation']?['latitude'] ?? "",
  //             longitude: media['geoLocation']?['longitude'] ?? "",
  //           );
  //           await isar.cacheAddNewAssets.put(newAsset);
  //         } else {
  //           final mediaEntry = CacheMediaUpload(
  //             projectId: projectId,
  //             assetType: type,
  //             filePath:
  //                 media['fileStore'], // You may want to cache this locally
  //             itemType: media['documentType'],
  //             latitude: media['geoLocation']?['latitude'] ?? "",
  //             longitude: media['geoLocation']?['longitude'] ?? "",
  //             itemNumber: '',
  //           );
  //           await isar.cacheMediaUploads.put(mediaEntry);
  //         }
  //       }
  //     }
  //   });
  // }

  /// Fetch remote assets and upsert into all your Isar caches,
  /// clearing only the matching CacheAddNewAsset per serialNumber,
  /// and wholesale-clearing media before re-inserting.
  Future<void> syncRemoteToLocal(String projectId, Isar isar) async {
    try {
      // 1) look up facilityId
      final facilityId = (await ProjectFacilityRepository()
              .search(ProjectFacilitySearchModel(projectId: [projectId]), isar))
          .facilityId;

      print("facilityId $facilityId");

      // 2) fetch from server
      final resp = await _dio.post(
        "/asset-registry/v1/asset/_search?tenantId=${envConfig.variables.tenantId}",
        data: {
          "criteria": {
            "tenantId": envConfig.variables.tenantId,
            "facilityID": facilityId,
          }
        },
      );
      print("resp ${resp.data}");
      if (resp.statusCode != 200 && resp.statusCode != 201) {
        throw Exception("Failed to fetch assets");
      }
      final List<dynamic> jsonList = resp.data;

      // 3) group by assetTypeID
      final Map<String, List<Map<String, dynamic>>> byType = {};
      for (final raw in jsonList.cast<Map<String, dynamic>>()) {
        final type = (raw['assetTypeID'] as String).toLowerCase();
        byType.putIfAbsent(type, () => []).add(raw);
        print("type $type");
      }

      print("byType $byType");

      // 4) write everything in one txn
      await isar.writeTxn(() async {
        for (final entry in byType.entries) {
          final type = entry.key;
          final assets = entry.value;

          print("entry $entry");
          print("type $type");
          print("assets $assets");
          // — upsert count
          final countValue = assets.length;
          print("countValue $countValue of ${entry.key}");
          final existingCount = await isar.cacheAssetCounts
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findFirst();
          print("existingCount $existingCount");
          if (existingCount != null) {
            print("existingCount.assetType ${existingCount.assetType}");
            print("existingCount.count ${existingCount.count}");
            existingCount
              ..count = countValue
              ..updatedAt = DateTime.now();
            await isar.cacheAssetCounts.put(existingCount);
          } else {
            await isar.cacheAssetCounts.put(
              CacheAssetCount(
                projectId: projectId,
                assetType: type,
                count: countValue,
              ),
            );
          }

          // — upsert spec & detail for each asset (they all share same spec/detail per type)
          for (final asset in assets) {
            // SPEC
            print("cacheSpecifications $asset");
            final specEntry = await isar.cacheSpecifications
                .where()
                .projectIdEqualTo(projectId)
                .filter()
                .assetTypeEqualTo(type)
                .findFirst();
            print("specEntry $specEntry");
            final spec = CacheSpecification(
              projectId: projectId,
              assetType: type,
              totalCapacity: (asset['assetDetails']?['totalCapacity'] as num?)
                      ?.toDouble() ??
                  0,
              totalCapacityUnit:
                  asset['assetDetails']?['totalCapacityUnit'] as String? ??
                      asset['assetDetails']?['totalCapacityUOM'] as String? ??
                      '',
              system: asset['system'] as String? ?? '',
            );
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

            // DETAIL
            final detailEntry = await isar.cacheAssetDetails
                .where()
                .projectIdEqualTo(projectId)
                .filter()
                .assetTypeEqualTo(type)
                .findFirst();
            print("detailEntry $detailEntry");
            final detail = CacheAssetDetail(
              projectId: projectId,
              assetType: type,
              brand: asset['brandID'] as String? ?? '',
              model: asset['modelNumber'] as String?,
              warranty: (asset['warrantyDuration'] as int?)?.toString(),
            );
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
          }

          // — per‐asset upsert of “main photo” entries
          //    delete only existing entries matching this serialNumber
          for (final asset in assets) {
            print("assets cacheAddNewAssets $asset");
            final serial = asset['serialNumber'] as String? ?? '';
            // delete old
            final oldList = await isar.cacheAddNewAssets
                .where()
                .projectIdEqualTo(projectId)
                .filter()
                .assetTypeEqualTo(type)
                .and()
                .serialNumberEqualTo(serial)
                .findAll();
            for (final old in oldList) {
              await isar.cacheAddNewAssets.delete(old.id);
            }
            // insert new ASSET docs
            for (final doc in (asset['documents'] as List<dynamic>)) {
              final d = doc as Map<String, dynamic>;
              if (d['documentType'] == 'PHOTO') {
                //todo CHANGE TO 'ASSET'
                await isar.cacheAddNewAssets.put(
                  CacheAddNewAsset(
                    projectId: projectId,
                    assetType: type,
                    itemNumber: asset['assetDetails']?['inverterCapacity']
                            ?.toString() ??
                        '',
                    serialNumber: serial,
                    photoPath: d['fileStore'] as String? ?? '',
                    latitude: d['geoLocation']?['latitude']?.toString() ?? '',
                    longitude: d['geoLocation']?['longitude']?.toString() ?? '',
                  ),
                );
              }
            }
          }

          // — clear all old non‐ASSET media, then re-insert
          final oldMedia = await isar.cacheMediaUploads
              .where()
              .projectIdEqualTo(projectId)
              .filter()
              .assetTypeEqualTo(type)
              .findAll();
          print("oldMedia $oldMedia");
          for (final m in oldMedia) {
            await isar.cacheMediaUploads.delete(m.id);
          }

          // 2. Insert the first non-`ASSET` document from the first asset of this assetType
          if (assets.isNotEmpty) {
            final firstAsset = assets.first;
            final documents = firstAsset['documents'] as List<dynamic>?;

            if (documents != null) {
              final firstNonAssetDoc =
                  documents.cast<Map<String, dynamic>>().firstWhere(
                        (doc) => doc['documentType'] != 'ASSET',
                        orElse: () => {},
                      );

              if (firstNonAssetDoc.isNotEmpty) {
                await isar.cacheMediaUploads.put(
                  CacheMediaUpload(
                    projectId: projectId,
                    assetType: type,
                    itemNumber: '',
                    itemType: firstNonAssetDoc['documentType'] as String? ?? '',
                    filePath: firstNonAssetDoc['fileStore'] as String? ?? '',
                    latitude: firstNonAssetDoc['geoLocation']?['latitude']
                            ?.toString() ??
                        '',
                    longitude: firstNonAssetDoc['geoLocation']?['longitude']
                            ?.toString() ??
                        '',
                  ),
                );
              }
            }
          }
        }
      });
    } on DioError catch (e) {
      throw DioErrorParser.parse(e);
    }
  }

  String _lookupMimeType(String fileName) {
    final ext = fileName.split(".").last.toLowerCase();
    switch (ext) {
      case "png":
        return "image/png";
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "mp4":
        return "video/mp4";
      case "mov":
        return "video/quicktime";
      default:
        return "application/octet-stream";
    }
  }
}
