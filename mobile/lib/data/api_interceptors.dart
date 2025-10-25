import 'package:dio/dio.dart';
import 'package:synchronized/synchronized.dart';

import '../model/request/requestInfo.dart';
import '../model/response/responsemodel.dart';
import '../repositories/authRepo.dart';
import '../utils/constants.dart';
import 'network_manager.dart';
import 'remote_client.dart';
import 'secure_storage/secureStore.dart';

class AuthTokenInterceptor extends Interceptor {
  final _lock = Lock();
  static const _maxRetries = 5;

  @override
  Future<dynamic> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    try {
      await NetworkService().ensureOnlineOrThrow();
    } on NetworkException catch (e) {
      return handler.reject(
        DioError(
          requestOptions: options,
          type: DioErrorType.unknown,
          error: e,
          message: e.message,
        ),
      );
    }

    final secureStore = SecureStore();
    final authToken = await secureStore.getAccessToken();
    final ResponseModel? accessInfo = await secureStore.getAccessInfo();
    print("options path ${options.path}");
    print("options data ${options.data}");
    print("options data 2 ${(options.data.toString())}");
    if (options.data is Map) {
      options.data = {
        ...options.data,
        "RequestInfo": RequestInfoModel(
                apiId: RequestInfoData.apiId,
                ver: RequestInfoData.ver,
                ts: DateTime.now().millisecondsSinceEpoch,
                action: options.path.split('/').last,
                did: RequestInfoData.did,
                key: RequestInfoData.key,
                msgId: "${DateTime.now().millisecondsSinceEpoch}|en_IN",
                authToken: authToken,
                userInfo: accessInfo?.userRequest)
            .toJson(),
      };
      final d = options.data;
      print('RequestInfo: ${d is FormData ? d.fields.firstWhere(
            (f) => f.key == 'RequestInfo',
            orElse: () => const MapEntry('', ''),
          ).value : (d is Map<String, dynamic> ? d['RequestInfo'] : null)}');
    }
    return handler.next(options);
  }

  @override
  void onError(DioError err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode != 401) {
      return handler.next(err);
    }

    final attempts = (err.requestOptions.extra['retryAttempts'] as int?) ?? 0;
    if (attempts >= _maxRetries) {
      return handler.next(err);
    }

    try {
      // Ensure only one refresh happens at a time
      await _lock.synchronized(() async {
        final authRepo = AuthRepository();
        await authRepo.refreshToken();
      });
    } catch (e) {
      return handler.next(err);
    }

    try {
      final dio = DioClient().dio;

      final RequestOptions ro = err.requestOptions;
      ro.extra = Map<String, dynamic>.from(ro.extra)
        ..update('retryAttempts', (v) => (v as int) + 1, ifAbsent: () => 1);

      final newResponse = await dio.fetch(ro);
      return handler.resolve(newResponse);
    } on DioError catch (e) {
      return handler.next(e);
    }
  }
}
