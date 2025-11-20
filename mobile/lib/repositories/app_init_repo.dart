import 'dart:convert';

import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/appconfig/mdmsResponse.dart';
import '../model/asset_count/asset_count.dart';
import '../model/asset_type/asset_type.dart';
import '../model/brand/brand.dart';
import '../model/mdms/mdms.dart';
import '../model/solution_design_type/solution_design_type.dart';
import '../model/solution_design_type_bom/solution_design_type_bom.dart';
import '../model/system/system.dart';
import '../model/warranty/warranty.dart';
import '../utils/envConfig.dart';

EnvironmentConfiguration envConfig = EnvironmentConfiguration.instance;

const String mdmsV2Url = "egov-mdms-service/v2/_search";

class AppInitRepo {
  Future<MdmsResponseModel> searchAppConfiguration(
      MdmsRequestModel mdmsRequestBody) async {
    final client = DioClient().dio;
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();
    String? localAppConfig = await storage.getAppConfig();
    if (localAppConfig != null) {
      return MdmsResponseModel.fromJson(json.decode(localAppConfig));
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalAppConfig();
    }

    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(envConfig.variables.completeMdmsApiUrl,
          data: body, options: Options(headers: headers));

      final responseBody = MdmsResponseModel.fromJson(
        json.decode(response.toString())['MdmsRes'],
      );
      storage.setAppConfig(responseBody);

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }

  Future<List<Mdms<AssetCountData>>> searchAssetCount(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();
    final SecureStore storage = SecureStore();

    String? localAssetCount = await storage.getAssetCount();
    if (localAssetCount != null) {
      final List<dynamic> decodedList =
          json.decode(localAssetCount) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<AssetCountData>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetCountData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<AssetCountData>> result = payloadList
          .map((item) => Mdms<AssetCountData>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetCountData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setAssetCount(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<AssetTypeData>>> searchAssetType(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    String? localAssetType = await storage.getAssetType();
    if (localAssetType != null) {
      final List<dynamic> decodedList =
          json.decode(localAssetType) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<AssetTypeData>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetTypeData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<AssetTypeData>> result = payloadList
          .map((item) => Mdms<AssetTypeData>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetTypeData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setAssetType(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<SystemData>>> searchSystem(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    String? localSystem = await storage.getSystem();
    if (localSystem != null) {
      final List<dynamic> decodedList =
          json.decode(localSystem) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<SystemData>.fromJson(
                item as Map<String, dynamic>,
                (json) => SystemData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<SystemData>> result = payloadList
          .map((item) => Mdms<SystemData>.fromJson(
                item as Map<String, dynamic>,
                (json) => SystemData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setSystem(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<WarrantyData>>> searchWarranty(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    String? localWarranty = await storage.getWarranty();
    if (localWarranty != null) {
      final List<dynamic> decodedList =
          json.decode(localWarranty) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<WarrantyData>.fromJson(
                item as Map<String, dynamic>,
                (json) => WarrantyData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<WarrantyData>> result = payloadList
          .map((item) => Mdms<WarrantyData>.fromJson(
                item as Map<String, dynamic>,
                (json) => WarrantyData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setWarranty(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<BrandData>>> searchBrand(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    String? localBrand = await storage.getBrand();
    if (localBrand != null) {
      final List<dynamic> decodedList =
          json.decode(localBrand) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<BrandData>.fromJson(
                item as Map<String, dynamic>,
                (json) => BrandData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<BrandData>> result = payloadList
          .map((item) => Mdms<BrandData>.fromJson(
                item as Map<String, dynamic>,
                (json) => BrandData.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setBrand(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<SolutionDesignType>>> searchSolutionDesign(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    String? localSolutionDesign = await storage.getSolutionDesignType();
    if (localSolutionDesign != null) {
      final List<dynamic> decodedList =
          json.decode(localSolutionDesign) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<SolutionDesignType>.fromJson(
                item as Map<String, dynamic>,
                (json) =>
                    SolutionDesignType.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<SolutionDesignType>> result = payloadList
          .map((item) => Mdms<SolutionDesignType>.fromJson(
                item as Map<String, dynamic>,
                (json) =>
                    SolutionDesignType.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setSolutionDesignType(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<SolutionDesignTypeBom>>> searchSolutionDesignTypeBom(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    String? localSolutionDesignBom = await storage.getSolutionDesignTypeBom();
    if (localSolutionDesignBom != null) {
      final List<dynamic> decodedList =
          json.decode(localSolutionDesignBom) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<SolutionDesignTypeBom>.fromJson(
                item as Map<String, dynamic>,
                (json) => SolutionDesignTypeBom.fromJson(
                    json as Map<String, dynamic>),
              ))
          .toList();
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post(mdmsV2Url,
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<SolutionDesignTypeBom>> result = payloadList
          .map((item) => Mdms<SolutionDesignTypeBom>.fromJson(
                item as Map<String, dynamic>,
                (json) => SolutionDesignTypeBom.fromJson(
                    json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setSolutionDesignTypeBom(result);
      return result;
    } catch (e) {
      rethrow;
    }
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

  Future<MdmsResponseModel> _loadLocalAppConfig() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockAppConfig.json');
      final jsonResponse = json.decode(jsonString);
      return MdmsResponseModel.fromJson(jsonResponse['MdmsRes']);
    } catch (e) {
      throw Exception('Failed to load mock app config: $e');
    }
  }

  Future<List<Map<String, dynamic>>> _loadLocalMdmsRaw(String filePath) async {
    final jsonString = await rootBundle.loadString(filePath);
    final decoded = json.decode(jsonString);

    if (decoded is Map<String, dynamic>) {
      final list = decoded['mdms'] ?? decoded['MdmsRes'];
      if (list is List) {
        return list
            .cast<Map>()
            .map((e) => Map<String, dynamic>.from(e as Map))
            .toList();
      }
    }
    throw Exception('No "mdms" (or MdmsRes) array found in $filePath');
  }

  Future<List<Map<String, dynamic>>> searchFormConfigsRaw(
      MdmsRequestModel mdmsRequestBody) async {
    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdmsRaw('assets/mocks/mockBOMFormConfig.json');
    }

    final body = mdmsRequestBody.toJson();
    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    final response = await client.post(
      "egov-mdms-service/v2/_search",
      data: body,
      options: Options(headers: headers),
    );

    final raw = response.data['mdms'];
    if (raw is! List) {
      throw Exception('MDMS v2 response missing "mdms" array');
    }
    return raw
        .cast<Map>()
        .map((e) => Map<String, dynamic>.from(e as Map))
        .toList();
  }
}
