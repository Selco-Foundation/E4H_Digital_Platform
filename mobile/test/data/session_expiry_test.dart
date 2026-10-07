import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../lib/data/api_interceptors.dart';
import '../../lib/data/secure_storage/secureStore.dart';
import '../../lib/repositories/auth_repo.dart';
import '../../lib/utils/envConfig.dart';

class FakeAuthRepository extends AuthRepository {
  int refreshes = 0;
  int logouts = 0;
  Object? refreshError;

  @override
  Future<String> refreshToken() async {
    refreshes++;
    if (refreshError != null) throw refreshError!;
    await SecureStore().setAccessToken('refreshed-token');
    return 'refreshed-token';
  }

  @override
  Future<void> logout() async {
    logouts++;
    await super.logout();
  }
}

class SessionAdapter implements HttpClientAdapter {
  SessionAdapter(this.status);
  final int Function(int call) status;
  final requests = <RequestOptions>[];
  @override
  Future<ResponseBody> fetch(RequestOptions options,
      Stream<Uint8List>? requestStream, Future<void>? cancelFuture) async {
    requests.add(options.copyWith(data: jsonDecode(jsonEncode(options.data))));
    return ResponseBody.fromString('{}', status(requests.length), headers: {
      Headers.contentTypeHeader: [Headers.jsonContentType]
    });
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late FakeAuthRepository auth;
  late Dio dio;
  late SessionAdapter adapter;
  var notifications = 0;

  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await envConfig.initialize();
    await SecureStore().setAccessToken('old-token');
    AuthTokenInterceptor.resetLogoutGuard();
    notifications = 0;
    AuthTokenInterceptor.onSessionExpired = () async => notifications++;
    auth = FakeAuthRepository();
    dio = Dio(BaseOptions(baseUrl: 'https://example.test'));
    dio.interceptors
        .add(AuthTokenInterceptor(authRepository: auth, retryClient: dio));
  });
  tearDown(() {
    dio.close();
    AuthTokenInterceptor.onSessionExpired = null;
    AuthTokenInterceptor.resetLogoutGuard();
  });

  void respond(int Function(int) status) {
    adapter = SessionAdapter(status);
    dio.httpClientAdapter = adapter;
  }

  Future<Response<dynamic>> request({bool suppressed = false}) =>
      dio.post('/assets/_search',
          data: {'query': 'clinic'},
          options: Options(extra: {suppressSessionExpiryExtraKey: suppressed}));
  Matcher expired() => isA<DioException>()
      .having((e) => e.message, 'message', 'SESSION_EXPIRED');

  test('401 refreshes and replays with new credentials and original payload',
      () async {
    respond((call) => call == 1 ? 401 : 200);
    expect((await request()).statusCode, 200);
    expect(auth.refreshes, 1);
    expect(auth.logouts, 0);
    expect(adapter.requests.last.data['RequestInfo']['authToken'],
        'refreshed-token');
    expect(adapter.requests.last.data['query'], 'clinic');
  });
  test('persistent 401 allows five refreshes then logs out once', () async {
    respond((_) => 401);
    await expectLater(request(), throwsA(expired()));
    expect(auth.refreshes, 5);
    expect(adapter.requests.length, 6);
    expect(auth.logouts, 1);
    expect(notifications, 1);
    expect(await SecureStore().getAccessToken(), isNull);
  });
  for (final status in [401, 403, 400]) {
    test('rejected refresh $status immediately expires the session', () async {
      respond((_) => 401);
      auth.refreshError = DioException(
          requestOptions: RequestOptions(path: '/user/oauth/token'),
          response: Response(
              requestOptions: RequestOptions(path: '/user/oauth/token'),
              statusCode: status,
              data: {'error': 'invalid_grant'}));
      await expectLater(request(), throwsA(expired()));
      expect(auth.refreshes, 1);
      expect(notifications, 1);
    });
  }
  test('missing refresh credentials expire without retry', () async {
    respond((_) => 401);
    auth.refreshError = const MissingRefreshCredentials();
    await expectLater(request(), throwsA(expired()));
    expect(notifications, 1);
    await expectLater(AuthRepository().refreshToken(),
        throwsA(isA<MissingRefreshCredentials>()));
  });
  test('concurrent expired requests notify and clear the session once',
      () async {
    respond((_) => 401);
    auth.refreshError = const MissingRefreshCredentials();
    await Future.wait(
        List.generate(3, (_) => expectLater(request(), throwsA(expired()))));
    expect(notifications, 1);
    expect(auth.logouts, 1);
  });
  test('fresh login guard reset permits a later session to expire', () async {
    respond((_) => 401);
    auth.refreshError = const MissingRefreshCredentials();
    await expectLater(request(), throwsA(expired()));
    AuthTokenInterceptor.resetLogoutGuard();
    await SecureStore().setAccessToken('new-session');
    await expectLater(request(), throwsA(expired()));
    expect(notifications, 2);
  });
  for (final type in [
    DioExceptionType.connectionError,
    DioExceptionType.receiveTimeout,
    DioExceptionType.badResponse
  ]) {
    test('refresh $type retains session', () async {
      respond((_) => 401);
      auth.refreshError = DioException(
          type: type,
          requestOptions: RequestOptions(path: '/user/oauth/token'),
          response: type == DioExceptionType.badResponse
              ? Response(
                  requestOptions: RequestOptions(path: '/user/oauth/token'),
                  statusCode: 500)
              : null);
      await expectLater(request(), throwsA(isA<DioException>()));
      expect(notifications, 0);
      expect(await SecureStore().getAccessToken(), 'old-token');
    });
  }
  test('non-401 and suppressed requests bypass refresh and logout', () async {
    respond((_) => 403);
    await expectLater(request(), throwsA(isA<DioException>()));
    respond((_) => 401);
    await expectLater(request(suppressed: true), throwsA(isA<DioException>()));
    expect(auth.refreshes, 0);
    expect(notifications, 0);
  });
}
