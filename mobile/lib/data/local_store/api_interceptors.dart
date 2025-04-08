import 'package:selco/data/local_store/secure_storage/secure_store.dart';
import 'package:selco/models/request/request_info.dart';
import 'package:selco/utils/constants.dart';
import 'package:dio/dio.dart';

class AuthTokenInterceptor extends Interceptor {
  @override
  Future<dynamic> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final secureStore = SecureStore();
    final authToken = await secureStore.getAccessToken();

    if (options.data is Map) {
      options.data = {
        ...options.data,
        "RequestInfo":
            RequestInfoModel(
              apiId: RequestInfoData.apiId,
              ver: RequestInfoData.ver,
              ts: DateTime.now().millisecondsSinceEpoch,
              action: options.path.split('/').last,
              did: RequestInfoData.did,
              key: RequestInfoData.key,
              authToken: authToken,
            ).toJson(),
      };
    }
    return super.onRequest(options, handler);
  }
}
