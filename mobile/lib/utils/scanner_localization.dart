import 'error_localization.dart';

class ScannerKeys {
  static const codeScanned = 'SCANNER_CODE_SCANNED';
  static const codesScanned = 'SCANNER_CODES_SCANNED';
  static const codeRequired = 'SCANNER_CODE_REQUIRED';
  static const codesRequired = 'SCANNER_CODES_REQUIRED';
}

const scannerErrorDefaults = <String, String>{
  'scanLimit': ErrorKeys.scanLimit,
  'pattern': ErrorKeys.scanPattern,
  'duplicate': ErrorKeys.scanDuplicate,
  'invalidGS1': ErrorKeys.invalidGs1,
  'scannerFailed': ErrorKeys.scannerFailed,
};

String? _legacyScannerKey(String message) => switch (message.toUpperCase()) {
      'COMMON_ERROR_SCAN_LIMIT' ||
      'SCANNED_QTY_EXCEED_THAN_REQUIRED' ||
      'SCANNED_RESOURCE_COUNT_MISMATCH' =>
        ErrorKeys.scanLimit,
      'COMMON_ERROR_SCAN_PATTERN' => ErrorKeys.scanPattern,
      'COMMON_ERROR_SCAN_DUPLICATE' ||
      'RESOURCE_ALREADY_SCANNED' ||
      'RESOURCES_ALREADY_SCANNED' =>
        ErrorKeys.scanDuplicate,
      'COMMON_ERROR_INVALID_GS1' || 'INVALID_BARCODE' => ErrorKeys.invalidGs1,
      'COMMON_ERROR_SCANNER_FAILED' ||
      'RESOURCE_SCAN_FAILED' =>
        ErrorKeys.scannerFailed,
      _ => null,
    };

/// MDMS overrides must be keys present in the selected locale's cached messages.
/// Known historical spellings always use the canonical key.
String scannerValidationKey(
    String type, String? configured, String Function(String) translate) {
  final fallback = scannerErrorDefaults[type] ?? ErrorKeys.scannerFailed;
  final key = configured?.trim() ?? '';
  return _legacyScannerKey(key) ??
      (key.isNotEmpty && translate(key) != key ? key : fallback);
}

String scannerErrorKey(String message, String Function(String) translate) {
  final key = message.trim();
  return _legacyScannerKey(key) ??
      (const {
                'NO_CODE_FOUND_IN_PHOTO',
                'FAILED_TO_READ_PHOTO',
                ErrorKeys.requiredField,
                ErrorKeys.cameraStart
              }.contains(key) ||
              translate(key) != key
          ? key
          : ErrorKeys.scannerFailed);
}

String scannedCountLabel(int count, String Function(String) translate) =>
    translate(count == 1 ? ScannerKeys.codeScanned : ScannerKeys.codesScanned)
        .replaceAll('{count}', '$count');
