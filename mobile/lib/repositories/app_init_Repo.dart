import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:selco/model/solution_design_type/solution_design_type.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/appconfig/mdmsResponse.dart';
import '../model/asset_count/asset_count.dart';
import '../model/asset_type/asset_type.dart';
import '../model/brand/brand.dart';
import '../model/mdms/mdms.dart';
import '../model/system/system.dart';
import '../model/warranty/warranty.dart';
import '../utils/envConfig.dart';

//create an instance of the environmentConfiguration class
//envConfig is used everywhere to get certain variables, either from the .env file or by using certain predefined fallback values
EnvironmentConfiguration envConfig = EnvironmentConfiguration.instance;

class AppInitRepo {
  Future<MdmsResponseModel> searchAppConfiguration(
      MdmsRequestModel mdmsRequestBody) async {
    final client = DioClient().dio;
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    // try to fetch locally
    String? localAppConfig = await storage.getAppConfig();
    if (localAppConfig != null) {
      return MdmsResponseModel.fromJson(json.decode(localAppConfig));
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalAppConfig();
    }

    final headers = <String, String>{
      // "content-type": 'application/x-www-form-urlencoded',
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      //make an api call

      final response = await client.post(envConfig.variables.completeMdmsApiUrl,
          data: body, options: Options(headers: headers));

      final responseBody = MdmsResponseModel.fromJson(
        json.decode(response.toString())['MdmsRes'],
      );

      //storage locally to avoid fetching in future
      storage.setAppConfig(responseBody);

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }

  Future<List<Mdms<AssetCount>>> searchAssetCount(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    // try to fetch locally
    String? localAssetCount = await storage.getAssetCount();
    if (localAssetCount != null) {
      final List<dynamic> decodedList =
          json.decode(localAssetCount) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<AssetCount>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetCount.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdms<AssetCount>(
        'assets/mocks/mockAssetCount.json',
        (json) => AssetCount.fromJson(json),
      );
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post("egov-mdms-service/v2/_search",
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<AssetCount>> result = payloadList
          .map((item) => Mdms<AssetCount>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetCount.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setAssetCount(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<AssetType>>> searchAssetType(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    // try to fetch locally
    String? localAssetType = await storage.getAssetType();
    if (localAssetType != null) {
      final List<dynamic> decodedList =
          json.decode(localAssetType) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<AssetType>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetType.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdms<AssetType>(
        'assets/mocks/mockAssetType.json',
        (json) => AssetType.fromJson(json),
      );
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post("egov-mdms-service/v2/_search",
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<AssetType>> result = payloadList
          .map((item) => Mdms<AssetType>.fromJson(
                item as Map<String, dynamic>,
                (json) => AssetType.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setAssetType(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<System>>> searchSystem(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    // try to fetch locally
    String? localSystem = await storage.getSystem();
    if (localSystem != null) {
      final List<dynamic> decodedList =
          json.decode(localSystem) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<System>.fromJson(
                item as Map<String, dynamic>,
                (json) => System.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdms<System>(
        'assets/mocks/mockSystem.json',
        (json) => System.fromJson(json),
      );
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post("egov-mdms-service/v2/_search",
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<System>> result = payloadList
          .map((item) => Mdms<System>.fromJson(
                item as Map<String, dynamic>,
                (json) => System.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setSystem(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<Warranty>>> searchWarranty(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    // try to fetch locally
    String? localWarranty = await storage.getWarranty();
    if (localWarranty != null) {
      final List<dynamic> decodedList =
          json.decode(localWarranty) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<Warranty>.fromJson(
                item as Map<String, dynamic>,
                (json) => Warranty.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdms<Warranty>(
        'assets/mocks/mockWarranty.json',
        (json) => Warranty.fromJson(json),
      );
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post("egov-mdms-service/v2/_search",
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<Warranty>> result = payloadList
          .map((item) => Mdms<Warranty>.fromJson(
                item as Map<String, dynamic>,
                (json) => Warranty.fromJson(json as Map<String, dynamic>),
              ))
          .toList();

      await storage.setWarranty(result);
      return result;
    } catch (e) {
      rethrow;
    }
  }

  Future<List<Mdms<Brand>>> searchBrand(
      MdmsRequestModel mdmsRequestBody) async {
    final body = mdmsRequestBody.toJson();

    final SecureStore storage = SecureStore();

    // try to fetch locally
    String? localBrand = await storage.getBrand();
    if (localBrand != null) {
      final List<dynamic> decodedList =
          json.decode(localBrand) as List<dynamic>;
      return decodedList
          .map((item) => Mdms<Brand>.fromJson(
                item as Map<String, dynamic>,
                (json) => Brand.fromJson(json as Map<String, dynamic>),
              ))
          .toList();
    }

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdms<Brand>(
        'assets/mocks/mockBrand.json',
        (json) => Brand.fromJson(json),
      );
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post("egov-mdms-service/v2/_search",
          data: body, options: Options(headers: headers));

      final List<dynamic> payloadList = response.data['mdms'] as List<dynamic>;
      final List<Mdms<Brand>> result = payloadList
          .map((item) => Mdms<Brand>.fromJson(
                item as Map<String, dynamic>,
                (json) => Brand.fromJson(json as Map<String, dynamic>),
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

    // try to fetch locally
    String? localSolutionDesign = await storage.getBrand();
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

    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalMdms<SolutionDesignType>(
        'assets/mocks/mockSolutionDesignType.json',
        (json) => SolutionDesignType.fromJson(json),
      );
    }

    final client = DioClient().dio;
    final headers = <String, String>{
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await client.post("egov-mdms-service/v2/_search",
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

  Future<List<Mdms<T>>> _loadLocalMdms<T>(
    String filePath,
    T Function(Map<String, dynamic>) fromJsonT,
  ) async {
    try {
      // 1) Load the raw JSON string from assets
      final jsonString = await rootBundle.loadString(filePath);
      final Map<String, dynamic> decoded =
          json.decode(jsonString) as Map<String, dynamic>;

      // 2) Figure out whether the array is under "mdms" or "MdmsRes"
      String? arrayKey;
      if (decoded.containsKey('mdms')) {
        arrayKey = 'mdms';
      } else if (decoded.containsKey('MdmsRes')) {
        arrayKey = 'MdmsRes';
      } else {
        throw Exception('No "mdms" or "MdmsRes" key found in $filePath');
      }

      final rawList = decoded[arrayKey];
      if (rawList is! List) {
        throw Exception('"$arrayKey" is not a List in $filePath');
      }

      // 3) Map each entry into `Mdms<T>.fromJson(...)`
      return rawList
          .cast<Map<String, dynamic>>()
          .map((entry) => Mdms<T>.fromJson(
                entry,
                (inner) => fromJsonT(inner as Map<String, dynamic>),
              ))
          .toList();
    } catch (e) {
      throw Exception('Failed to load mock data from $filePath: $e');
    }
  }
}
