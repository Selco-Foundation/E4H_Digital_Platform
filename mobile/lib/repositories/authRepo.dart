import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../model/login/loginModel.dart';
import '../model/response/responsemodel.dart';
import '../model/role_actions/role_actions_model.dart';
import '../utils/envConfig.dart';

class AuthRepository {
  AuthRepository();
  Future<ResponseModel> validateLogin(LoginModel body) async {
    final formData = body.toJson();

    if (envConfig.variables.envType == EnvType.dev) {
      // return _loadLocalAuth();
    }

    //make a custom Dio client which will not send the request with the interceptor
    final authClient = Dio();
    authClient.options.baseUrl = envConfig.variables.baseUrl;

    final headers = <String, String>{
      "content-type": 'application/x-www-form-urlencoded',
      "Access-Control-Allow-Origin": "*",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    try {
      final response = await authClient.post('user/oauth/token',
          data: formData, options: Options(headers: headers));
      final responseBody = ResponseModel.fromJson(response.data);

      //close this client so it doesnt interfere with other instances of DioClient
      authClient.close();

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }

  Future<RoleActionsWrapperModel> searchRoleActions(
    Map<String, dynamic> body,
  ) async {
    String url = envConfig.variables.actionMapApiPath;
    final client = DioClient().dio;

    try {
      final Response response = await client.post(url, data: body);
      return RoleActionsWrapperModel.fromJson(json.decode(response.toString()));
    } catch (_) {
      rethrow;
    }
  }

  Future<ResponseModel> _loadLocalAuth() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockLogin.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = ResponseModel.fromJson(jsonResponse);

      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }
}
