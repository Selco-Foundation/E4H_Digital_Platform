import 'package:dio/dio.dart';

import '../model/request/requestInfo.dart';
import '../model/response/responsemodel.dart';
import '../utils/constants.dart';
import 'secure_storage/secureStore.dart';

class AuthTokenInterceptor extends Interceptor {
  @override
  Future<dynamic> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final secureStore = SecureStore();
    final authToken = await secureStore.getAccessToken();
    final ResponseModel? accessInfo = await secureStore.getAccessInfo();

    // print("accessInfo ${accessInfo?.toJson()}");

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
      print("option.data ${options.data}");
    }

    return super.onRequest(options, handler);
  }
}
