const int remoteLogMaxLength = 500;
const String redactedLogValue = '[REDACTED]';

final RegExp _credentialPattern = RegExp(
  r'''(?:(authorization|access[_-]?token|refresh[_-]?token|password|passcode|otp|secret|api[_-]?key)["']?\s*[:=]\s*)["']?[^\s,;}&\]]+''',
  caseSensitive: false,
);
final RegExp _authorizationPattern = RegExp(
  r'\b(Bearer|Basic)\s+[A-Za-z0-9._~+/=-]+',
  caseSensitive: false,
);
final RegExp _emailPattern = RegExp(
  r'\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b',
  caseSensitive: false,
);
final RegExp _phonePattern = RegExp(r'\+?\d[\d\s().-]{7,}\d');
final RegExp _controlCharacterPattern = RegExp(r'[\u0000-\u001F\u007F]');

String sanitizeRemoteLog(Object? input, {int maxLength = remoteLogMaxLength}) {
  var sanitized = (input ?? '').toString();

  sanitized = sanitized.replaceAllMapped(
    _authorizationPattern,
    (match) => '${match.group(1)} $redactedLogValue',
  );
  sanitized = sanitized.replaceAllMapped(
    _credentialPattern,
    (match) => '${match.group(1)}=$redactedLogValue',
  );
  sanitized = sanitized.replaceAll(_emailPattern, redactedLogValue);
  sanitized = sanitized.replaceAll(_phonePattern, redactedLogValue);
  sanitized = sanitized.replaceAll(_controlCharacterPattern, ' ').trim();

  if (sanitized.length <= maxLength) {
    return sanitized;
  }
  return sanitized.substring(0, maxLength);
}
