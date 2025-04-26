import 'dart:async';
import 'dart:convert';

import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';

import '../../models/request_info/request_info_model.dart';
import '../../utils/constants.dart';
import '../../utils/environment_config.dart';
import '../local_store/secure_store/secure_store.dart';

class AuthTokenInterceptor extends Interceptor {
  final LocalSecureStore localSecureStore;

  AuthTokenInterceptor({
    LocalSecureStore? localSecureStore,
  }) : localSecureStore = localSecureStore ?? LocalSecureStore.instance;

  @override
  Future<dynamic> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final authToken = await localSecureStore.accessToken;
    final userInfo = await localSecureStore.userRequestModel;
    if (options.data is Map) {
      options.data = {
        ...options.data,
        "RequestInfo": RequestInfoModel(
                apiId: RequestInfoData.apiId,
                ver: RequestInfoData.ver,
                ts: DateTime.now().millisecondsSinceEpoch,
                action: options.path.split('/').last,
                did: RequestInfoData.did,
                key: "1",
                authToken: authToken,
                userInfo: userInfo,
                tenantId: envConfig.variables.tenantId)
            .toJson(),
      };
    }
    super.onRequest(options, handler);
  }
}

class ApiLoggerInterceptor extends Interceptor {
  @override
  Future<dynamic> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    if (options.data is Map || options.data is List) {
      AppLogger.instance.info(
        _getIndentedJson(json.encode(options.data)),
        title: '[REQUEST] ${options.uri.toString()}',
      );
    }
    super.onRequest(options, handler);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    super.onResponse(response, handler);

    if (response.requestOptions.path.contains('boundarys')) return;

    try {
      AppLogger.instance.info(
        _getIndentedJson(json.encode(response.data)),
        title:
            '[RESPONSE - ${response.statusCode}] ${response.requestOptions.uri.toString()}',
      );
    } catch (error) {
      AppLogger.instance.info(
        // ignore: avoid_dynamic_calls
        '${response.data.runtimeType} ${response.statusCode.toString()}',
        title: '[RESPONSE (error)] ${response.requestOptions.path}',
      );
    }
  }

  String _getIndentedJson(String json) {
    return const JsonEncoder.withIndent('  ').convert(jsonDecode(json));
  }
}
