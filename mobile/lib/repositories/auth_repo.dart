import 'dart:convert';

import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/login/loginModel.dart';
import '../model/request/requestInfo.dart';
import '../model/response/otp_response.dart';
import '../model/response/responsemodel.dart';
import '../model/role_actions/role_actions_model.dart';
import '../utils/envConfig.dart';

class AuthRepository {
  AuthRepository();
  Future<ResponseModel> validateLogin(LoginModel body) async {
    final formData = body.toJson();

    if (envConfig.variables.envType == EnvType.dev) {
      if (body.username == '1234567893' && body.password == 'Beehyv@123')
        return _loadLocalAuth();
    }

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
      authClient.close();

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }

  Future<void> logout() async {
    final secureStore = SecureStore();
    secureStore.deleteAccessToken();
    secureStore.deleteAccessInfo();
    secureStore.deleteSelectedIndividual();
  }

  Future<String> refreshToken() async {
    final secureStore = SecureStore();
    final ResponseModel? accessInfo = await secureStore.getAccessInfo();

    AppLogger.instance.info("refreshing token accessInfo ${accessInfo}");
    if (accessInfo!.refresh_token == null) {
      throw Exception("No refresh token stored");
    }

    final dio = Dio()..options.baseUrl = envConfig.variables.baseUrl;
    final form = {
      'grant_type': 'refresh_token',
      'refresh_token': accessInfo.refresh_token,
    };
    final headers = {
      "content-type": "application/x-www-form-urlencoded",
      "authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo=",
    };

    final resp = await dio.post('user/oauth/token',
        data: form, options: Options(headers: headers));
    final body = ResponseModel.fromJson(resp.data);

    await secureStore.setAccessToken(body.access_token);
    await secureStore.setAccessInfo(body);

    return body.access_token;
  }

  Future<SendOtpResponse> sendOtp(Map<String, dynamic> body) async {
    final dio = DioClient().dio;
    try {
      final response = await dio.post('user-otp/v1/_send', data: {"otp": body});
      final responseBody = SendOtpResponse.fromJson(response.data);

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }

  Future<RequestInfoModel> resetPassword(Map<String, dynamic> body) async {
    final dio = DioClient().dio;
    try {
      final response =
          await dio.post('user/password/nologin/_update', data: body);
      final responseBody = RequestInfoModel.fromJson(response.data);

      return responseBody;
    } catch (err) {
      if (err is DioException) {
        final data = err.response?.data;
        String errorMessage = 'Unknown error';
        if (data is Map<String, dynamic>) {
          if (data.containsKey('error') &&
              data['error'] is Map<String, dynamic>) {
            final errObj = data['error'] as Map<String, dynamic>;
            errorMessage = errObj['message']?.toString() ??
                errObj['description']?.toString() ??
                errorMessage;
          } else if (data.containsKey('Errors') && data['Errors'] is List) {
            final errors = data['Errors'] as List;
            if (errors.isNotEmpty && errors.first is Map<String, dynamic>) {
              final first = errors.first as Map<String, dynamic>;
              errorMessage =
                  (first['code'] ?? "") + " " + first['message']?.toString() ??
                      first['description']?.toString() ??
                      errorMessage;
            }
          }
        }
        throw Exception(errorMessage);
      }
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
          await rootBundle.loadString('assets/mocks/mockLoginAMC.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = ResponseModel.fromJson(jsonResponse);

      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }
}
