import "package:dio/dio.dart";

import '../utils/envConfig.dart';
import 'api_interceptors.dart';

BaseOptions configuredApiOptions() => BaseOptions(
      baseUrl: envConfig.variables.baseUrl,
      connectTimeout:
          Duration(milliseconds: envConfig.variables.connectTimeout),
      sendTimeout: Duration(milliseconds: envConfig.variables.sendTimeout),
      receiveTimeout:
          Duration(milliseconds: envConfig.variables.receiveTimeout),
    );

class DioClient {
  late Dio _dio;

  static final DioClient _instance = DioClient._internal();

  factory DioClient() {
    return _instance;
  }

  DioClient._internal() {
    init();
  }

  Dio get dio => _dio;

  void init() {
    _dio = Dio()
      ..interceptors.addAll([
        AuthTokenInterceptor(),
        DebugHttpBodyLoggingInterceptor(),
      ])
      ..options = configuredApiOptions();

    _dio.options.baseUrl = envConfig.variables.baseUrl;
  }
}
