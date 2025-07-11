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
import '../model/asset/asset.dart';
import '../model/entities/project_facility.dart';
import '../model/transaction/transaction.dart';
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

  Future<void> createOrUpdateAsset2(
      {required Map<String, dynamic> payload,
      String? assetId,
      required Isar isar}) async {
    try {
      final isCreate = (assetId != null && assetId.isNotEmpty) ? false : true;
      print(jsonEncode(payload)); //todo to be removed
      final url = isCreate ? "_create" : "$assetId/_update";
      print("url $url");
      final response =
          await _dio.post("/asset-registry/v1/asset/$url", data: payload);
      print("response ${response.data}");
      print("response statusCode ${response.statusCode}");
      print("response statusMessage ${response.statusMessage}");
      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception(
            "${isCreate ? 'Create' : 'Update'} Asset responded with status ${response.statusCode}");
      } else {
        // extract the payload of assetId and serialNumber, and save it
        // ✅ Handle response data: could be "asset" or "Asset"
        final responseData = response.data;
        final assetData = responseData['asset'] ?? responseData['Asset'];

        if (assetData == null) {
          throw Exception("Asset data missing in response");
        }

        final serialNumber = assetData['serialNumber']?.toString();
        final assetType = assetData['assetTypeID']?.toString();

        if (serialNumber == null || assetType == null) {
          throw Exception("Missing one of serialNumber or assetType");
        }

        // ✅ Update assetId in Isar this applies to create, but also done for update just incase Id cases
        await isar.writeTxn(() async {
          final existing = await isar.cacheAddNewAssets
              .where()
              .assetTypeEqualTo(assetType.toLowerCase())
              .filter()
              .serialNumberEqualTo(serialNumber)
              .findFirst();

          if (existing != null) {
            existing.assetId = assetData['assetId'];
            await isar.cacheAddNewAssets.put(existing);
          }
        });
      }
    } on DioError catch (e) {
      print(e.message);
      throw DioErrorParser.parse(e);
    }
  }

  /// Creates or updates an asset on the server, then writes back the returned
  /// assetId into Isar and returns the server's canonical Asset model.
  Future<Asset> createOrUpdateAsset({
    required Asset asset,
    required Isar isar,
  }) async {
    // Determine create vs update
    final isCreate = asset.assetId == null || asset.assetId!.isEmpty;
    final endpoint = isCreate ? '_create' : '${asset.assetId}/_update';

    // Build the nested payload
    final payload = {
      'assetDetail': {
        'Asset': asset.toJson(),
      },
    };

    try {
      print('Request payload: ${jsonEncode(payload)}');
      final response = await _dio.post(
        '/asset-registry/v1/asset/$endpoint',
        data: payload,
      );
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

      // Persist the returned assetId back into Isar
      if (updatedAsset.assetId != null && updatedAsset.assetId!.isNotEmpty) {
        await isar.writeTxn(() async {
          final existing = await isar.cacheAddNewAssets
              .where()
              .assetTypeEqualTo(asset.assetTypeID!)
              .filter()
              .serialNumberEqualTo(asset.serialNumber!)
              .findFirst();

          if (existing != null) {
            existing.assetId = updatedAsset.assetId;
            await isar.cacheAddNewAssets.put(existing);
          }
        });
      }

      return updatedAsset;
    } on DioError catch (e) {
      // Bubble up a parsed error
      print(e.message);
      throw DioErrorParser.parse(e);
    }
  }

  /// Fetch remote assets and upsert into all your Isar caches,
  /// clearing only the matching CacheAddNewAsset per serialNumber,
  /// and wholesale-clearing media before re-inserting.
  Future<void> syncRemoteToLocal2(String projectId, Isar isar) async {
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
              print("document Type ${d['documentType']}");
              print("fileStore ${d['fileStore']}");
              if (d['documentType'] == 'PHOTO') {
                print("document Type ${d['documentType']}");
                //todo CHANGE TO 'ASSET'
                await isar.cacheAddNewAssets.put(
                  CacheAddNewAsset(
                    projectId: projectId,
                    assetType: type,
                    itemNumber: asset['assetDetails']?['inverterCapacity']
                            ?.toString() ??
                        '',
                    serialNumber: serial,
                    photoPath: d['fileStore'] != null
                        //    ? //'${envConfig.variables.baseUrl}/filestore/v1/files/file?tenantId=in&fileStoreId=${d['fileStore']}'
                        // '${envConfig.variables.baseUrl}/filestore/v1/files/file?tenantId=in&fileStoreId=4d4a5ad7-7c9e-4953-b095-271c9c34ffe6'
                        //    'https://nanoskill.s3.eu-west-2.amazonaws.com/1.png'
                        //    : '', //
                        ? d['fileStore']?.String()
                        : '',
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

  Future<void> syncRemoteToLocal(String projectId, Isar isar) async {
    try {
      // 1) facilityId lookup as before
      final facilityId = (await ProjectFacilityRepository().search(
        ProjectFacilitySearchModel(projectId: [projectId]),
        isar,
      ))
          .facilityId;

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
                //todo CHANGE TO 'ASSET'
                print("document Type ${doc.documentType}");
                print("fileStore ${doc.fileStore}");
                await isar.cacheAddNewAssets.put(
                  CacheAddNewAsset(
                    projectId: projectId,
                    assetType: type,
                    itemNumber:
                        asset.assetDetails?.inverterCapacity?.toString() ?? '',
                    serialNumber: serial ?? '',
                    photoPath: doc.fileStore ?? '',
                    latitude: doc.geoLocation?.latitude?.toString() ?? '',
                    longitude: doc.geoLocation?.longitude?.toString() ?? '',
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
          print("oldMedia $oldMedia");
          for (var asset in list) {
            for (var doc in asset.documents ?? []) {
              if (doc.documentType != 'ASSET' && doc.documentType != 'PHOTO') {
                //todo CHANGE TO 'ASSET'
                await isar.cacheMediaUploads.put(
                  CacheMediaUpload(
                    projectId: projectId,
                    assetType: type,
                    itemNumber: '',
                    itemType: doc.documentType ?? '',
                    filePath: doc.fileStore ?? '',
                    latitude: doc.geoLocation?.latitude?.toString() ?? '',
                    longitude: doc.geoLocation?.longitude?.toString() ?? '',
                  ),
                );
              }
            }
          }
        }
      });
    } on DioError catch (e) {
      print(e);
      throw DioErrorParser.parse(e);
    }
  }

  /// Submit “reject” with reasons
  Future<void> submitRejection({
    required String projectId,
    String action = "REJECT",
    required List<Transaction> transactions,
  }) async {
    final payload = {
      'projectId': projectId,
      'workflow': {'action': action},
      'transactions': transactions.map((t) {
        final jsonString = t.toJson();
        final m = jsonDecode(jsonString) as Map<String, dynamic>;
        m.removeWhere((k, v) => v == null);
      }).toList(),
    };

    try {
      final resp = await _dio.post(
        '/project/v1/project/workflow/update',
        data: payload,
      );
      if (resp.statusCode != 200 && resp.statusCode != 201) {
        throw Exception('Reject responded ${resp.statusCode}');
      }
    } on DioError catch (dioErr) {
      final msg = dioErr.response?.data?.toString() ?? dioErr.message;
      throw DioErrorParser.parse(dioErr);
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
