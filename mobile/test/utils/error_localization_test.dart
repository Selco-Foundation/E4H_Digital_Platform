import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/utils/error_localization.dart';

void main() {
  test('keeps keys and session expiry while suppressing technical details', () {
    expect(
        errorMessageKey(Exception(ErrorKeys.reportSave)), ErrorKeys.reportSave);
    expect(errorMessageKey('LOGIN_ERROR_INVALID_CREDENTIALS'),
        'LOGIN_ERROR_INVALID_CREDENTIALS');
    expect(errorMessageKey(Exception('SESSION_EXPIRED')), 'SESSION_EXPIRED');
    expect(errorMessageKey(StateError('/private/file: database failure')),
        ErrorKeys.generic);
    expect(errorMessageKey(null), ErrorKeys.generic);
    expect(errorMessageKey(''), ErrorKeys.generic);
  });

  test('API errors use recognized codes, never backend prose or unknown codes',
      () {
    final request = RequestOptions(path: '/test');
    DioException failure(Object body, {int status = 400}) => DioException(
        requestOptions: request,
        type: DioExceptionType.badResponse,
        response:
            Response(requestOptions: request, statusCode: status, data: body));
    expect(
        errorMessageKey(failure({
          'Errors': [
            {
              'code': 'ASSESSMENT_INVALID_FORM_DATA',
              'message': 'Private details'
            }
          ]
        })),
        ErrorKeys.invalidValue);
    expect(
        errorMessageKey(failure({
          'Errors': [
            {'code': 'UNKNOWN_INTERNAL_ERROR', 'message': 'Private details'}
          ]
        })),
        ErrorKeys.requestFailed);
    expect(errorMessageKey(failure({'message': 'Private details'})),
        ErrorKeys.requestFailed);
    expect(errorMessageKey(failure({}, status: 401)), 'SESSION_EXPIRED');
  });

  test('offline-save copy requires confirmed persistence and a network failure',
      () {
    for (final key in [
      ErrorKeys.noNetwork,
      ErrorKeys.noInternet,
      ErrorKeys.connectionInterrupted,
      ErrorKeys.serverConnection,
      ErrorKeys.requestTimeout
    ]) {
      expect(reportErrorKey(key, savedLocally: false), key);
      expect(reportErrorKey(key, savedLocally: true),
          ErrorKeys.reportConnectionInterrupted);
    }
    expect(reportErrorKey(ErrorKeys.reportSave, savedLocally: false),
        ErrorKeys.reportSave);
    expect(reportErrorKey(ErrorKeys.workflowUpdate, savedLocally: true),
        ErrorKeys.workflowUpdate);
    expect(reportErrorKey('SESSION_EXPIRED', savedLocally: true),
        'SESSION_EXPIRED');
  });

  test(
      'legacy persisted connection failures use a key without claiming persistence',
      () {
    expect(
        errorMessageKey(
            'We could not submit because the internet connection was interrupted. Please try again.'),
        ErrorKeys.connectionInterrupted);
  });

  test('backend JSON covers every new constant and contains no duplicate codes',
      () {
    final rows = (jsonDecode(File('docs/localization/error_messages.en_IN.json')
            .readAsStringSync()) as List)
        .cast<Map>();
    final codes = rows.map((row) => row['code']).toSet();
    expect(codes.length, rows.length);
    final source = File('lib/utils/error_localization.dart').readAsStringSync();
    final constants = RegExp(r"static const \w+\s*=\s*'(COMMON_[A-Z0-9_]+)';")
        .allMatches(source)
        .map((match) => match.group(1))
        .toSet();
    expect(codes, constants);
    for (final row in rows) {
      expect(row.keys.toSet(), {'code', 'message', 'module', 'locale'});
      expect(row['module'], 'rainmaker-common');
      expect(row['locale'], 'en_IN');
      expect(row['message'], isNotEmpty);
    }
    final reportMessage = rows.singleWhere((row) =>
            row['code'] == ErrorKeys.reportConnectionInterrupted)['message']
        as String;
    expect(reportMessage,
        'Poor internet connection. Your report has been saved offline. Please try again with a stable connection');
    expect(reportMessage.contains('\n'), isFalse);
  });
}
