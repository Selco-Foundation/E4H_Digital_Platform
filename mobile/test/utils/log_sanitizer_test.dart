import 'package:flutter_test/flutter_test.dart';
import 'package:selco/utils/log_sanitizer.dart';

void main() {
  group('sanitizeRemoteLog', () {
    test('redacts credential fields and authorization values', () {
      const input = 'authorization: Bearer abc.123 access_token=access-value '
          'refresh_token: refresh-value password=hunter2 otp=123456 '
          'api_key=key-value';

      final result = sanitizeRemoteLog(input);

      expect(result, isNot(contains('abc.123')));
      expect(result, isNot(contains('access-value')));
      expect(result, isNot(contains('refresh-value')));
      expect(result, isNot(contains('hunter2')));
      expect(result, isNot(contains('123456')));
      expect(result, isNot(contains('key-value')));
      expect(result, contains(redactedLogValue));
    });

    test('redacts structured request data', () {
      final result = sanitizeRemoteLog({
        'access_token': 'token-value',
        'password': 'password-value',
        'message': 'safe-value',
      });

      expect(result, isNot(contains('token-value')));
      expect(result, isNot(contains('password-value')));
      expect(result, contains('safe-value'));
    });

    test('redacts email addresses and phone-like values', () {
      const input = 'Contact person@example.com or +234 801 234 5678';

      final result = sanitizeRemoteLog(input);

      expect(result, isNot(contains('person@example.com')));
      expect(result, isNot(contains('+234 801 234 5678')));
      expect(result, contains(redactedLogValue));
    });

    test('removes control characters', () {
      expect(
        sanitizeRemoteLog('first\nsecond\tthird\u0000'),
        'first second third',
      );
    });

    test('caps long messages', () {
      final result = sanitizeRemoteLog('a' * (remoteLogMaxLength + 50));

      expect(result.length, remoteLogMaxLength);
    });

    test('preserves ordinary diagnostic messages', () {
      const input = 'Upload retry failed with status 503';

      expect(sanitizeRemoteLog(input), input);
    });
  });
}
