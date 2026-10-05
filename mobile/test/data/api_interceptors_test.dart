import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/data/api_interceptors.dart';

void main() {
  group('DebugHttpBodyLoggingInterceptor', () {
    test('logs request and successful response bodies', () async {
      final logs = <String>[];
      final dio = _dioWithResponse(
        statusCode: 200,
        responseBody: {'result': 'ok'},
        logs: logs,
      );

      await dio.post('/assets/_search', data: {'tenantId': 'default'});

      expect(logs, hasLength(2));
      expect(logs.first, contains('[HTTP REQUEST] POST'));
      expect(logs.first, contains('{tenantId: default}'));
      expect(logs.last, contains('[HTTP RESPONSE] 200 POST'));
      expect(logs.last, contains('{result: ok}'));
    });

    test('logs failed response bodies', () async {
      final logs = <String>[];
      final dio = _dioWithResponse(
        statusCode: 400,
        responseBody: {'message': 'invalid asset'},
        logs: logs,
      );

      await expectLater(
        dio.post('/assets/_create', data: {'asset': 'bad'}),
        throwsA(isA<DioException>()),
      );

      expect(logs, hasLength(2));
      expect(logs.last, contains('[HTTP ERROR RESPONSE] 400 POST'));
      expect(logs.last, contains('{message: invalid asset}'));
    });

    test('does not log MDMS requests', () async {
      final logs = <String>[];
      final dio = _dioWithResponse(
        statusCode: 200,
        responseBody: {'mdms': []},
        logs: logs,
      );

      await dio.post(
        '/egov-mdms-service/v2/_search',
        data: {'MdmsCriteria': {}},
      );

      expect(logs, isEmpty);
    });

    test('does not log explicitly suppressed requests', () async {
      final logs = <String>[];
      final dio = _dioWithResponse(
        statusCode: 200,
        responseBody: {'result': 'ok'},
        logs: logs,
      );

      await dio.post(
        '/assets/_search',
        data: {'tenantId': 'default'},
        options: Options(
          extra: const {suppressBodyLoggingExtraKey: true},
        ),
      );

      expect(logs, isEmpty);
    });
  });
}

Dio _dioWithResponse({
  required int statusCode,
  required Map<String, dynamic> responseBody,
  required List<String> logs,
}) {
  final dio = Dio(BaseOptions(baseUrl: 'https://example.test'));
  dio.httpClientAdapter = _StubHttpClientAdapter(
    statusCode: statusCode,
    responseBody: responseBody,
  );
  dio.interceptors.add(
    DebugHttpBodyLoggingInterceptor(logWriter: logs.add),
  );
  return dio;
}

class _StubHttpClientAdapter implements HttpClientAdapter {
  const _StubHttpClientAdapter({
    required this.statusCode,
    required this.responseBody,
  });

  final int statusCode;
  final Map<String, dynamic> responseBody;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    return ResponseBody.fromString(
      jsonEncode(responseBody),
      statusCode,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}
