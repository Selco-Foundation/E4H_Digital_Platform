import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../lib/repositories/activity_facility_repo.dart';
import '../../lib/utils/utils.dart';

const message =
    'Failed to transition workflow for facility: 8f267afc-f270-4c77-97d1-2999dbffdd97';
final serverBody = {
  'ResponseInfo': null,
  'Errors': [
    {'code': 'WORKFLOW_TRANSITION_FAILED', 'message': message}
  ]
};
DioException failure(dynamic body,
    {int status = 400,
    String? text,
    DioExceptionType type = DioExceptionType.badResponse}) {
  final options = RequestOptions(path: '/workflow/update');
  return DioException(
      requestOptions: options,
      response:
          Response(requestOptions: options, statusCode: status, data: body),
      type: type,
      message: text ??
          'RequestOptions.validateStatus rejected status code of $status');
}

String readable(DioException error) => normalizeFriendlyNetworkErrorMessage(
    DioErrorParser.parse(error).toString());

class WorkflowAdapter implements HttpClientAdapter {
  @override
  Future<ResponseBody> fetch(RequestOptions options,
          Stream<Uint8List>? requestStream, Future<void>? cancelFuture) async =>
      ResponseBody.fromString(jsonEncode(serverBody), 400, headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType]
      });
  @override
  void close({bool force = false}) {}
}

void main() {
  for (final body in [
    serverBody,
    jsonEncode(serverBody),
    utf8.encode(jsonEncode(serverBody)),
    Uint8List.fromList(utf8.encode(jsonEncode(serverBody)))
  ]) {
    test('extracts workflow message from ${body.runtimeType}', () {
      expect(readable(failure(body)), message);
    });
  }
  test('uses first non-empty server message and supported alternative formats',
      () {
    for (final body in [
      {
        'Errors': [
          null,
          'invalid',
          {'message': null},
          {'message': ' '},
          {'message': 'Readable'},
          {'message': 'Later'}
        ]
      },
      {
        'error': {'message': 'Readable'}
      },
      {'error_description': 'Readable'},
      {'message': 'Readable'},
      {'Errors': 'invalid', 'message': 'Readable'},
    ]) {
      expect(readable(failure(body)), 'Readable');
    }
  });
  test('malformed or empty bodies use a short fallback', () {
    for (final body in [
      null,
      {},
      [],
      '',
      '<html>error</html>',
      [255],
      {'Errors': []},
      {
        'Errors': [
          {'message': 123}
        ]
      }
    ]) {
      expect(readable(failure(body)), 'Request failed. Please try again.');
    }
  });
  test('session expiry takes precedence over server messages', () {
    expect(readable(failure(serverBody, status: 401)), 'SESSION_EXPIRED');
    expect(readable(failure(serverBody, text: 'SESSION_EXPIRED')),
        'SESSION_EXPIRED');
  });
  test('keeps friendly network errors and gives concise timeout messages', () {
    expect(
        readable(failure(null,
            text: 'No internet access', type: DioExceptionType.unknown)),
        "You're offline. Please reconnect to the internet and try again.");
    expect(
        readable(failure(null,
            text: 'Failed host lookup',
            type: DioExceptionType.connectionError)),
        'We could not submit because the internet connection was interrupted. Please try again.');
    expect(readable(failure(null, type: DioExceptionType.receiveTimeout)),
        'The request timed out. Please try again.');
  });
  test(
      'workflow repository byte response reaches submission display without Dio text',
      () async {
    final dio = Dio(BaseOptions(baseUrl: 'https://example.test'))
      ..httpClientAdapter = WorkflowAdapter();
    final repository = ActivityFacilityRemoteRepository(dio: dio);
    try {
      await repository.updateActivityFacilityWorkflow(
          activityFacilityId: 'facility', action: 'SUBMIT_REPORT_B');
      fail('Expected workflow failure');
    } catch (error) {
      expect(normalizeFriendlyNetworkErrorMessage(error.toString()), message);
    } finally {
      dio.close();
    }
  });
}
