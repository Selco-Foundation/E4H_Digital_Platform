// Importing necessary packages and files
import "package:dio/dio.dart"; // Dio package for HTTP requests

import '../utils/envConfig.dart';
import 'api_interceptors.dart';

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
      ])
      ..options = BaseOptions(
        connectTimeout: Duration(
          milliseconds: envConfig.variables.connectTimeout,
        ),
        sendTimeout: Duration(
          milliseconds: envConfig.variables.sendTimeout,
        ),
        receiveTimeout: Duration(
          milliseconds: envConfig.variables.receiveTimeout,
        ),
        baseUrl: envConfig.variables.baseUrl,
      );

    _dio.options.baseUrl = envConfig.variables.baseUrl;
  }
}
