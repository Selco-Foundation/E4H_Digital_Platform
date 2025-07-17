import 'dart:convert';

import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../model/localization/localizationModel.dart';
import '../utils/envConfig.dart';

class LocalizationRepository {
  final client = DioClient().dio;

  Future<LocalizationModel> getLocalizationsList(
      Map<String, String> queryParameters) async {
    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalLocalization();
    }

    try {
      final response = await client.post(
          '${envConfig.variables.baseUrl}localization/messages/v1/_search',
          queryParameters: queryParameters,
          data: {});

      print("${response.data}");

      final responseBody = LocalizationModel.fromJson(response.data);

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }

  Future<LocalizationModel> _loadLocalLocalization() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockLocalization.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = LocalizationModel.fromJson(jsonResponse);
      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock localization: $e');
    }
  }
}
