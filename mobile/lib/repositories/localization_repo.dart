import 'dart:convert';

import 'package:dio/dio.dart';

import '../model/localization/localizationModel.dart';
import '../utils/envConfig.dart';

class LocalizationRepository {
  final Dio authClient;

  LocalizationRepository({Dio? client})
      : authClient = client ??
            Dio(BaseOptions(
              connectTimeout: const Duration(seconds: 20),
              receiveTimeout: const Duration(seconds: 20),
              sendTimeout: const Duration(seconds: 20),
            ));

  Future<LocalizationModel> getLocalizationsList(
      Map<String, String> queryParameters) async {
    final body = {
      "RequestInfo": {
        "apiId": "Rainmaker",
        "authToken": null,
        "msgId": "1755851952491|en_IN",
        "plainAccessRequest": {}
      }
    };

    try {
      final response = await authClient.post(
          '${envConfig.variables.baseUrl}localization/messages/v1/_search',
          queryParameters: queryParameters,
          data: jsonEncode(body));

      final responseBody = LocalizationModel.fromJson(response.data);

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }
}
