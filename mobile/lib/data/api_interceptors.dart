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
    return super.onRequest(options, handler);
  }
}
