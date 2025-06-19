import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:selco/model/request/requestInfo.dart';
import 'package:selco/model/response/otp_response.dart';

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

  Future<SendOtpResponse> sendOtp(Map<String, dynamic> body) async {
    final dio = DioClient().dio;
    if (envConfig.variables.envType == EnvType.dev) {
      return _loadLocalSendOtp();
    }
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
    if (envConfig.variables.envType == EnvType.dev) {
      // return _loadLocalRestPassword1();
    }
    try {
      final response =
          await dio.post('user/password/nologin/_update', data: body);
      final responseBody = RequestInfoModel.fromJson(response.data);

      return responseBody;
    } catch (err) {
      if (err is DioException) {
        final data = err.response?.data;
        debugPrint("data $data");
        String errorMessage = 'Unknown error';
        if (data is Map<String, dynamic>) {
          // Case 1: {"responseInfo": null, "error": { "code":400, "message":"OTP validation unsuccessful", ... }}
          if (data.containsKey('error') &&
              data['error'] is Map<String, dynamic>) {
            final errObj = data['error'] as Map<String, dynamic>;
            errorMessage = errObj['message']?.toString() ??
                errObj['description']?.toString() ??
                errorMessage;
          }
          // Case 2: {"ResponseInfo": null, "Errors": [ { "code":"UserNotFoundException", "message":"...", "description":"..." } ]}
          else if (data.containsKey('Errors') && data['Errors'] is List) {
            final errors = data['Errors'] as List;
            if (errors.isNotEmpty && errors.first is Map<String, dynamic>) {
              final first = errors.first as Map<String, dynamic>;
              errorMessage = first['message']?.toString() ??
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
          await rootBundle.loadString('assets/mocks/mockLogin.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = ResponseModel.fromJson(jsonResponse);

      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }

  Future<SendOtpResponse> _loadLocalSendOtp() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockSendOtp.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = SendOtpResponse.fromJson(jsonResponse);

      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }

  Future<RequestInfoModel> _loadLocalRestPassword1() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockVerifyOtpError1.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = RequestInfoModel.fromJson(jsonResponse);
      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }

  Future<RequestInfoModel> _loadLocalRestPassword2() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockVerifyOtpError2.json');
      final jsonResponse = json.decode(jsonString);
      final responseBody = RequestInfoModel.fromJson(jsonResponse);
      return responseBody;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }
}
