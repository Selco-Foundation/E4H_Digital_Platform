import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:synchronized/synchronized.dart';

import '../model/request/requestInfo.dart';
import '../model/response/responsemodel.dart';
import '../repositories/auth_repo.dart';
import '../utils/constants.dart';
import 'network_manager.dart';
import 'remote_client.dart';
import 'secure_storage/secureStore.dart';

typedef SessionExpiredCallback = Future<void> Function();

class AuthTokenInterceptor extends Interceptor {
  final _lock = Lock();
  static const _maxRetries = 5;

  /// Set this from UI (so you can dispatch AuthBloc.logout + route to login).
  static SessionExpiredCallback? onSessionExpired;

  static bool _logoutTriggered = false;

  static Future<void> _triggerLogoutOnce() async {
    if (_logoutTriggered) return;
    _logoutTriggered = true;

    // 1) clear stored tokens
    try {
      await AuthRepository().logout();
    } catch (_) {}

    // 2) notify UI layer (navigation + bloc)
    try {
      await onSessionExpired?.call();
    } catch (_) {}
  }

  DioError _sessionExpiredError(DioError original) {
    return DioError(
      requestOptions: original.requestOptions,
      response: original.response,
      type: original.type,
      error: original.error,
      message: 'SESSION_EXPIRED',
    );
  }

  bool _refreshTokenLooksExpired(Object e) {
    if (e is DioError) {
      final code = e.response?.statusCode;

      if (code == 400 || code == 401 || code == 403) return true;

      final data = e.response?.data;
      if (data is Map) {
        final err = (data['error'] ?? '').toString().toLowerCase();
        final desc = (data['error_description'] ?? '').toString().toLowerCase();

        if (err.contains('invalid') || desc.contains('invalid')) return true;
        if (err.contains('expired') || desc.contains('expired')) return true;
      }
    }
    return false;
  }

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
    AppLogger.instance.info(options.path, title: "path");
    AppLogger.instance.info(options.data, title: "data");
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
      // return handler.next(err);
      await _triggerLogoutOnce();
      return handler.reject(_sessionExpiredError(err));
    }

    try {
      await _lock.synchronized(() async {
        await AuthRepository().refreshToken();
      });
    } catch (e) {
      // If refresh token is invalid/expired -> logout immediately
      if (_refreshTokenLooksExpired(
          e is Object ? e : Exception(e.toString()))) {
        await _triggerLogoutOnce();
        return handler.reject(_sessionExpiredError(err));
      }

      return handler.next(err);
    }

    try {
      final dio = DioClient().dio;

      final ro = err.requestOptions;
      ro.extra = Map<String, dynamic>.from(ro.extra)
        ..update('retryAttempts', (v) => (v as int) + 1, ifAbsent: () => 1);

      final newResponse = await dio.fetch(ro);
      return handler.resolve(newResponse);
    } on DioError catch (e) {
      return handler.next(e);
    }
  }
}
