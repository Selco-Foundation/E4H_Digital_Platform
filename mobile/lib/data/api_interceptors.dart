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
    // return super.onRequest(options, handler);
    return handler.next(options);
  }

  @override
  void onError(DioError err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401) {
      await _lock.synchronized(() async {
        final authRepo = AuthRepository();
        await authRepo.refreshToken();
      });
      // After refresh, retry the request
      final dio = DioClient().dio;
      final newResponse = await dio.fetch(err.requestOptions);
      return handler.resolve(newResponse);
    } else {
      return handler.next(err);
    }
  }
}
