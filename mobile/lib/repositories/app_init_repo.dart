import 'dart:convert';

import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/appconfig/mdmsResponse.dart';
import '../model/asset_count/asset_count.dart';
import '../model/asset_type/asset_type.dart';
import '../model/brand/brand.dart';
import '../model/installation_images/installation_images.dart';
import '../model/mdms/mdms.dart';
import '../model/rejection_reason/rejection_reason.dart';
import '../model/required_bom_form_keys/required_bom_form_keys.dart';
import '../model/solution_design_type/solution_design_type.dart';
import '../model/solution_design_type_bom/solution_design_type_bom.dart';
import '../model/system/system.dart';
import '../model/warranty/warranty.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';

EnvironmentConfiguration envConfig = EnvironmentConfiguration.instance;

const String mdmsV2Url = "egov-mdms-service/v2/_search";

typedef MdmsRawDocsValidator = void Function(
  List<Map<String, dynamic>> documents,
);

class AppInitRepo {
  Future<MdmsResponseModel> searchAppConfiguration() async {
    final client = Dio();
    final SecureStore storage = SecureStore();

    try {
      final response = await client.get(envConfig.variables.mobileAppGlobalUrl);

      final responseBody = MdmsResponseModel.fromJson(
        json.decode(response.toString())['MdmsRes'],
      );
      storage.setAppConfig(responseBody);

      return responseBody;
    } catch (remoteError) {
      final localAppConfig = await storage.getAppConfig();
      if (localAppConfig == null) {
        rethrow;
      }

      try {
        return MdmsResponseModel.fromJson(json.decode(localAppConfig));
      } catch (_) {
        rethrow;
      }
    }
  }

  Future<List<Mdms<AssetCountData>>> searchAssetCount(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<AssetCountData>(
      request: mdmsRequestBody,
      readCache: storage.getAssetCount,
      writeCache: (list) => storage.setAssetCount(list),
      dataFromJson: AssetCountData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<AssetTypeData>>> searchAssetType(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<AssetTypeData>(
      request: mdmsRequestBody,
      readCache: storage.getAssetType,
      writeCache: (list) => storage.setAssetType(list),
      dataFromJson: AssetTypeData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<SystemData>>> searchSystem(MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false, bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<SystemData>(
      request: mdmsRequestBody,
      readCache: storage.getSystem,
      writeCache: (list) => storage.setSystem(list),
      dataFromJson: SystemData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<WarrantyData>>> searchWarranty(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<WarrantyData>(
      request: mdmsRequestBody,
      readCache: storage.getWarranty,
      writeCache: (list) => storage.setWarranty(list),
      dataFromJson: WarrantyData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<BrandData>>> searchBrand(MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false, bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<BrandData>(
      request: mdmsRequestBody,
      readCache: storage.getBrand,
      writeCache: (list) => storage.setBrand(list),
      dataFromJson: BrandData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<SolutionDesignType>>> searchSolutionDesign(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<SolutionDesignType>(
      request: mdmsRequestBody,
      readCache: storage.getSolutionDesignType,
      writeCache: (list) => storage.setSolutionDesignType(list),
      dataFromJson: SolutionDesignType.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<SolutionDesignTypeBom>>> searchSolutionDesignTypeBom(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<SolutionDesignTypeBom>(
      request: mdmsRequestBody,
      readCache: storage.getSolutionDesignTypeBom,
      writeCache: (list) => storage.setSolutionDesignTypeBom(list),
      dataFromJson: SolutionDesignTypeBom.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Map<String, dynamic>>> searchFormConfigsRaw(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedRawDocs(
      request: mdmsRequestBody,
      readCache: storage.getFormConfigsRaw,
      writeCache: (list) => storage.setFormConfigsRaw(list),
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Map<String, dynamic>>> searchAMCFormConfigsRaw(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedRawDocs(
      request: mdmsRequestBody,
      readCache: storage.getAMCFormConfigsRaw,
      writeCache: (list) => storage.setAMCFormConfigsRaw(list),
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Map<String, dynamic>>> searchAssessmentFormConfigsRaw(
    MdmsRequestModel mdmsRequestBody, {
    bool useCacheRead = false,
    bool cacheOnly = false,
    MdmsRawDocsValidator? validator,
  }) async {
    final storage = SecureStore();
    return _searchCachedRawDocs(
      request: mdmsRequestBody,
      readCache: storage.getAssessmentFormConfigsRaw,
      writeCache: (list) => storage.setAssessmentFormConfigsRaw(list),
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
      validator: validator,
    );
  }

  Future<List<Mdms<InstallationImagesData>>> searchInstallationImages(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<InstallationImagesData>(
      request: mdmsRequestBody,
      readCache: storage.getInstallationImages,
      writeCache: (list) => storage.setInstallationImages(list),
      dataFromJson: InstallationImagesData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<RejectionReasonData>>> searchRejectionReasons(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<RejectionReasonData>(
      request: mdmsRequestBody,
      readCache: storage.getRejectionReasons,
      writeCache: (list) => storage.setRejectionReasons(list),
      dataFromJson: RejectionReasonData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Future<List<Mdms<RequiredBomFormKeysData>>> searchRequiredBomFormKeys(
      MdmsRequestModel mdmsRequestBody,
      {bool useCacheRead = false,
      bool cacheOnly = false}) async {
    final storage = SecureStore();
    return _searchCachedMdms<RequiredBomFormKeysData>(
      request: mdmsRequestBody,
      readCache: storage.getRequiredBomFormKeys,
      writeCache: (list) => storage.setRequiredBomFormKeys(list),
      dataFromJson: RequiredBomFormKeysData.fromJson,
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
    );
  }

  Map<String, String> _defaultMdmsHeaders() {
    return const <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };
  }

  Future<List<dynamic>> _fetchMdmsRawList(MdmsRequestModel request) async {
    final client = DioClient().dio;
    final response = await client.post(
      mdmsV2Url,
      data: request.toJson(),
      options: Options(headers: _defaultMdmsHeaders()),
    );
    final data = response.data;
    if (data is! Map) {
      throw Exception('MDMS v2 response is not a JSON object');
    }
    final raw = response.data['mdms'];
    if (raw is! List) {
      throw Exception('MDMS v2 response missing "mdms" array');
    }
    return raw;
  }

  List<dynamic>? _readCachedMdmsList(String? raw) {
    if (raw == null) return null;
    final decoded = json.decode(raw);
    if (decoded is! List) return null;
    return decoded;
  }

  Future<List<Mdms<T>>> _searchCachedMdms<T>({
    required MdmsRequestModel request,
    required Future<String?> Function() readCache,
    required Future<void> Function(List<Mdms<T>>) writeCache,
    required T Function(Map<String, dynamic>) dataFromJson,
    required bool useCacheRead,
    required bool cacheOnly,
  }) async {
    if (useCacheRead || cacheOnly) {
      final cachedRaw = await readCache();
      try {
        final cachedList = _readCachedMdmsList(cachedRaw);
        if (cachedList != null && cachedList.isNotEmpty) {
          return cachedList
              .map((item) => Mdms<T>.fromJson(
                    item as Map<String, dynamic>,
                    (json) => dataFromJson(json as Map<String, dynamic>),
                  ))
              .toList();
        }
      } catch (_) {
        if (cacheOnly) {
          throw Exception('Cached MDMS data is invalid');
        }
      }

      if (cacheOnly) {
        throw Exception('Cached MDMS data is missing');
      }
    }

    final payloadList = await _fetchMdmsRawList(request);
    final result = payloadList
        .map((item) => Mdms<T>.fromJson(
              item as Map<String, dynamic>,
              (json) => dataFromJson(json as Map<String, dynamic>),
            ))
        .toList();

    await writeCache(result);
    return result;
  }

  Future<List<Map<String, dynamic>>> _searchCachedRawDocs({
    required MdmsRequestModel request,
    required Future<String?> Function() readCache,
    required Future<void> Function(List<Map<String, dynamic>>) writeCache,
    required bool useCacheRead,
    required bool cacheOnly,
    MdmsRawDocsValidator? validator,
  }) async {
    if (useCacheRead || cacheOnly) {
      final cachedRaw = await readCache();
      try {
        final cachedList = _readCachedMdmsList(cachedRaw);
        if (cachedList != null && cachedList.isNotEmpty) {
          final result = cachedList
              .whereType<Map>()
              .map((e) => Map<String, dynamic>.from(e))
              .toList();
          validator?.call(result);
          return result;
        }
      } catch (_) {
        if (cacheOnly) {
          throw Exception('Cached MDMS form config is invalid');
        }
      }

      if (cacheOnly) {
        throw Exception('Cached MDMS form config is missing');
      }
    }

    final payloadList = await _fetchMdmsRawList(request);
    final result = payloadList
        .whereType<Map>()
        .map((e) => Map<String, dynamic>.from(e))
        .toList();

    validator?.call(result);
    await writeCache(result);
    return result;
  }

  Future<Map<String, dynamic>?> loadByName(String name) async {
    final SecureStore storage = SecureStore();
    final raw = await storage.getFormSchemas();
    if (raw == null) return null;

    try {
      final Map<String, dynamic> all = json.decode(raw);
      final entry = all[name];
      if (entry is Map && entry['data'] is Map) {
        return Map<String, dynamic>.from(entry['data'] as Map);
      }
    } catch (e, st) {
      AppLogger.instance
          .info('FormsSchemaRepository.loadByName error: $e\n$st');
    }
    return null;
  }

  Future<Map<String, dynamic>?> loadByUniqueIdentifier(String uniqueId) async {
    final SecureStore storage = SecureStore();
    final raw = await storage.getFormSchemas();
    if (raw == null) return null;

    try {
      final Map<String, dynamic> all = json.decode(raw);
      for (final value in all.values) {
        if (value is Map && value['data'] is Map) {
          final data = Map<String, dynamic>.from(value['data'] as Map);
          if (data['uniqueIdentifier']?.toString() == uniqueId) {
            return data;
          }
        }
      }
    } catch (e, st) {
      AppLogger.instance
          .info('FormsSchemaRepository.loadByUniqueIdentifier error: $e\n$st');
    }
    return null;
  }

  Future<void> upsertTransformedSchema(Map<String, dynamic> transformed) async {
    final name = transformed['name']?.toString();
    final newVersion = transformed['version'];

    if (name == null || name.isEmpty) return;

    final SecureStore storage = SecureStore();
    final raw = await storage.getFormSchemas();
    final Map<String, dynamic> existing = raw == null
        ? <String, dynamic>{}
        : (json.decode(raw) as Map<String, dynamic>);

    final existingEntry = existing[name] as Map<String, dynamic>?;

    final updatedEntry = {
      'data': transformed,
      'currentVersion': newVersion,
      'previousVersion': existingEntry?['currentVersion'],
    };

    existing[name] = updatedEntry;
    await storage.setFormSchemas(existing);
  }
}
