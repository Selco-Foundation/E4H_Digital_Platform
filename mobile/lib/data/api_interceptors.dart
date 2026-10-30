import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../model/request/requestInfo.dart';
import '../model/response/responsemodel.dart';
import '../repositories/auth_repo.dart';
import '../utils/app_logger.dart';
import '../utils/constants.dart';
import 'network_manager.dart';
import 'remote_client.dart';
import 'secure_storage/secureStore.dart';

typedef SessionExpiredCallback = Future<void> Function();

const String suppressSessionExpiryExtraKey = 'suppressSessionExpiry';
const String suppressBodyLoggingExtraKey = 'suppressBodyLogging';

typedef HttpBodyLogWriter = void Function(String message);

class DebugHttpBodyLoggingInterceptor extends Interceptor {
  DebugHttpBodyLoggingInterceptor({HttpBodyLogWriter? logWriter})
      : _logWriter = logWriter ?? debugPrint;

  final HttpBodyLogWriter _logWriter;

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) {
    if (_shouldLog(options)) {
      _logWriter(
        '[HTTP REQUEST] ${options.method} ${options.uri}\n'
        'Body: ${options.data}',
      );
    }
    handler.next(options);
  }

  @override
  void onResponse(
    Response response,
    ResponseInterceptorHandler handler,
  ) {
    if (_shouldLog(response.requestOptions)) {
      _logWriter(
        '[HTTP RESPONSE] ${response.statusCode} '
        '${response.requestOptions.method} ${response.requestOptions.uri}\n'
        'Body: ${response.data}',
      );
    }
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (_shouldLog(err.requestOptions)) {
      _logWriter(
        '[HTTP ERROR RESPONSE] ${err.response?.statusCode} '
        '${err.requestOptions.method} ${err.requestOptions.uri}\n'
        'Body: ${err.response?.data}',
      );
    }
    handler.next(err);
  }

  bool _shouldLog(RequestOptions options) {
    if (!kDebugMode || options.extra[suppressBodyLoggingExtraKey] == true) {
      return false;
    }

    return !options.uri.path.toLowerCase().contains('mdms');
  }
}

class AuthTokenInterceptor extends Interceptor {
  AuthTokenInterceptor({AuthRepository? authRepository, Dio? retryClient})
      : _authRepository = authRepository ?? AuthRepository(),
        _retryClient = retryClient;

  final AuthRepository _authRepository;
  final Dio? _retryClient;
  Future<String>? _refreshing;
  int? _refreshingSession;
  static int _sessionGeneration = 0;
  static int get sessionGeneration => _sessionGeneration;

  static void invalidateSession() => _sessionGeneration++;

  static SessionExpiredCallback? onSessionExpired;

  static bool _logoutTriggered = false;

  static void resetLogoutGuard() {
    invalidateSession();
    _logoutTriggered = false;
  }

  Future<void> _triggerLogoutOnce(int generation) async {
    if (_logoutTriggered || generation != sessionGeneration) return;
    _logoutTriggered = true;

    // 1) clear stored tokens
    try {
      await _authRepository.logout();
    } catch (_) {}

    // A new login may have completed while credentials were being cleared.
    if (sessionGeneration != generation + 1) return;
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
    if (e is MissingRefreshCredentials) return true;
    if (e is DioError) {
      final data = e.response?.data;
      if (data is Map) {
        final err = (data['error'] ?? '').toString().toLowerCase();
        final desc = (data['error_description'] ?? '').toString().toLowerCase();

        return err == 'invalid_grant' ||
            err == 'invalid_token' ||
            desc.contains('invalid refresh token') ||
            desc.contains('expired refresh token');
      }
    }
    return false;
  }

  @override
  Future<dynamic> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    options.extra.putIfAbsent('sessionGeneration', () => sessionGeneration);
    if (options.extra['sessionGeneration'] != sessionGeneration) {
      return handler.reject(DioException(
        requestOptions: options,
        type: DioExceptionType.cancel,
        message: 'Session changed',
      ));
    }
    final secureStore = SecureStore();
    final authToken = await secureStore.getAccessToken();
    final ResponseModel? accessInfo = await secureStore.getAccessInfo();
    AppLogger.instance.info(options.path, title: "path");
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
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    if (response.requestOptions.extra['sessionGeneration'] !=
        sessionGeneration) {
      return handler.reject(DioException(
        requestOptions: response.requestOptions,
        type: DioExceptionType.cancel,
        message: 'Session changed',
      ));
    }
    handler.next(response);
  }

  @override
  void onError(DioError err, ErrorInterceptorHandler handler) async {
    if (err.requestOptions.extra[suppressSessionExpiryExtraKey] == true) {
      return handler.next(err);
    }

    AppLogger.instance.error(
        title: "statusCode", message: err.response?.statusCode?.toString());
    if (err.response?.statusCode != 401) {
      return handler.next(err);
    }

    final generation = err.requestOptions.extra['sessionGeneration'];
    if (generation != sessionGeneration ||
        err.requestOptions.extra['authRetried'] == true) {
      return handler.next(err);
    }

    try {
      final currentToken = await SecureStore().getAccessToken();
      final sentInfo = err.requestOptions.data is Map
          ? err.requestOptions.data['RequestInfo']
          : null;
      final sentToken = sentInfo is Map ? sentInfo['authToken'] : null;
      if (generation != sessionGeneration) return handler.next(err);
      if (currentToken == null || currentToken == sentToken) {
        if (_refreshingSession != generation) {
          _refreshing = null;
          _refreshingSession = generation as int;
        }
        final refresh = _refreshing ??= _authRepository.refreshToken();
        try {
          await refresh;
        } finally {
          if (identical(_refreshing, refresh)) _refreshing = null;
        }
      }
    } catch (e) {
      AppLogger.instance.info(
        'Refresh failed for ${err.requestOptions.uri.path}: '
        '${e is DioException ? e.response?.statusCode : e.runtimeType}',
      );
      if (generation == sessionGeneration && _refreshTokenLooksExpired(e)) {
        await _triggerLogoutOnce(generation as int);
        return handler.reject(_sessionExpiredError(err));
      }
      return handler.next(err);
    }
    if (generation != sessionGeneration) return handler.next(err);

    try {
      final dio = _retryClient ?? DioClient().dio;

      final ro = err.requestOptions;
      ro.extra = Map<String, dynamic>.from(ro.extra)..['authRetried'] = true;

      final newResponse = await dio.fetch(ro);
      return handler.resolve(newResponse);
    } on DioError catch (e) {
      return handler.next(e);
    }
  }
}

class NetworkErrorNormalizerInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.error is AppNetworkException) {
      handler.next(err);
      return;
    }

    final code = _resolve(err);
    final mapped = DioException(
      requestOptions: err.requestOptions,
      response: err.response,
      type: err.type,
      error: AppNetworkException(code, rawMessage: err.message),
      message: code.name,
    );

    handler.next(mapped);
  }

  LoginErrorCode _resolve(DioException e) {
    if (_isInvalidCredentialError(e)) {
      return LoginErrorCode.invalidCredentials;
    }

    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.sendTimeout ||
        e.type == DioExceptionType.receiveTimeout) {
      return LoginErrorCode.requestTimeout;
    }

    if (e.type == DioExceptionType.connectionError ||
        _looksLikeTransportFailure(e.message)) {
      return LoginErrorCode.connectionFailed;
    }

    final status = e.response?.statusCode ?? 0;
    if (status >= 500) return LoginErrorCode.serverError;

    return LoginErrorCode.unknown;
  }

  bool _isInvalidCredentialError(DioException e) {
    final data = e.response?.data;
    final status = e.response?.statusCode ?? 0;

    if (status == 401) return true;

    if (data is Map<String, dynamic>) {
      final err = (data['error'] ?? '').toString().toLowerCase();
      final desc = (data['error_description'] ?? '').toString().toLowerCase();
      if (err.contains('invalid_grant') || desc.contains('bad credentials')) {
        return true;
      }
      if (err.contains('invalid') && desc.contains('credential')) {
        return true;
      }
    }

    return false;
  }

  bool _looksLikeTransportFailure(String? message) {
    final msg = (message ?? '').toLowerCase();
    if (msg.isEmpty) return false;
    return msg.contains('failed host lookup') ||
        msg.contains('socketexception') ||
        msg.contains('connection error') ||
        msg.contains('network is unreachable') ||
        msg.contains('connection reset');
  }
}
