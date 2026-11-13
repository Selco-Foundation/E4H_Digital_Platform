import 'dart:convert';

import 'package:dio/dio.dart';

import 'app_logger.dart';

/// Backend-owned error copy. These values are keys, never English fallbacks.
class ErrorKeys {
  static const scanLimit = 'COMMON_ERROR_SCAN_LIMIT';
  static const scanPattern = 'COMMON_ERROR_SCAN_PATTERN';
  static const scanDuplicate = 'COMMON_ERROR_SCAN_DUPLICATE';
  static const scannerFailed = 'COMMON_ERROR_SCANNER_FAILED';
  static const noDrafts = 'COMMON_ERROR_NO_DRAFTS';
  static const mobileNumberMissing = 'COMMON_ERROR_MOBILE_NUMBER_MISSING';
  static const otpMissing = 'COMMON_ERROR_OTP_MISSING';
  static const photoPreview = 'COMMON_ERROR_PHOTO_PREVIEW';
  static const requiredImageCount = 'COMMON_ERROR_REQUIRED_IMAGE_COUNT';
  static const filePick = 'COMMON_ERROR_FILE_PICK';
  static const bomRequiredTitle = 'COMMON_ERROR_BOM_REQUIRED_TITLE';
  static const generic = 'COMMON_ERROR_GENERIC';
  static const requestFailed = 'COMMON_ERROR_REQUEST_FAILED';
  static const noNetwork = 'COMMON_ERROR_NO_NETWORK';
  static const noInternet = 'COMMON_ERROR_NO_INTERNET';
  static const reportConnectionInterrupted =
      'COMMON_REPORT_CONNECTION_INTERRUPTED';
  static const connectionInterrupted = 'COMMON_ERROR_CONNECTION_INTERRUPTED';
  static const requestTimeout = 'COMMON_ERROR_REQUEST_TIMEOUT';
  static const serverConnection = 'COMMON_ERROR_SERVER_CONNECTION';
  static const configurationLoad = 'COMMON_ERROR_CONFIGURATION_LOAD';
  static const requiredField = 'COMMON_ERROR_REQUIRED_FIELD';
  static const invalidValue = 'COMMON_ERROR_INVALID_VALUE';
  static const cameraBusy = 'COMMON_ERROR_CAMERA_BUSY';
  static const cameraOpening = 'COMMON_ERROR_CAMERA_OPENING';
  static const cameraPermission = 'COMMON_ERROR_CAMERA_PERMISSION';
  static const cameraStart = 'COMMON_ERROR_CAMERA_START';
  static const cameraGalleryOpen = 'COMMON_ERROR_CAMERA_GALLERY_OPEN';
  static const photoCapture = 'COMMON_ERROR_PHOTO_CAPTURE';
  static const cameraResultLost = 'COMMON_ERROR_CAMERA_RESULT_LOST';
  static const cameraResultRestore = 'COMMON_ERROR_CAMERA_RESULT_RESTORE';
  static const imageProcess = 'COMMON_ERROR_IMAGE_PROCESS';
  static const imagePick = 'COMMON_ERROR_IMAGE_PICK';
  static const videoPick = 'COMMON_ERROR_VIDEO_PICK';
  static const photoRead = 'COMMON_ERROR_PHOTO_READ';
  static const invalidGs1 = 'COMMON_ERROR_INVALID_GS1';
  static const reportLoad = 'COMMON_ERROR_REPORT_LOAD';
  static const reportSave = 'COMMON_ERROR_REPORT_SAVE';
  static const reportsSave = 'COMMON_ERROR_REPORTS_SAVE';
  static const reportDelete = 'COMMON_ERROR_REPORT_DELETE';
  static const reportsDelete = 'COMMON_ERROR_REPORTS_DELETE';
  static const reportsClear = 'COMMON_ERROR_REPORTS_CLEAR';
  static const reportsReplace = 'COMMON_ERROR_REPORTS_REPLACE';
  static const visitSubmission = 'COMMON_ERROR_VISIT_SUBMISSION';
  static const partialSubmission = 'COMMON_ERROR_PARTIAL_SUBMISSION';
  static const completedAssetRequired = 'COMMON_ERROR_COMPLETED_ASSET_REQUIRED';
  static const assetDetailsMissing = 'COMMON_ERROR_ASSET_DETAILS_MISSING';
  static const formIncomplete = 'COMMON_ERROR_FORM_INCOMPLETE';
  static const bomIncomplete = 'COMMON_ERROR_BOM_INCOMPLETE';
  static const bomSync = 'COMMON_ERROR_BOM_SYNC';
  static const workflowUpdate = 'COMMON_ERROR_WORKFLOW_UPDATE';
  static const sendBackUnavailable = 'COMMON_ERROR_SEND_BACK_UNAVAILABLE';
  static const assetsLoad = 'COMMON_ERROR_ASSETS_LOAD';
  static const assetRejection = 'COMMON_ERROR_ASSET_REJECTION';
  static const installationImagesLoad = 'COMMON_ERROR_INSTALLATION_IMAGES_LOAD';
  static const amcCodeResend = 'COMMON_ERROR_AMC_CODE_RESEND';
  static const amcCodeVerification = 'COMMON_ERROR_AMC_CODE_VERIFICATION';
  static const savedEntryMissing = 'COMMON_ERROR_SAVED_ENTRY_MISSING';
  static const loginResponse = 'COMMON_ERROR_LOGIN_RESPONSE';
  static const assessmentFormUnavailable =
      'COMMON_ERROR_ASSESSMENT_FORM_UNAVAILABLE';
  static const assessmentFormLoad = 'COMMON_ERROR_ASSESSMENT_FORM_LOAD';
  static const facilityContactUpdate = 'COMMON_ERROR_FACILITY_CONTACT_UPDATE';
  static const assessmentSubmission = 'COMMON_ERROR_ASSESSMENT_SUBMISSION';
  static const assessmentRequest = 'COMMON_ERROR_ASSESSMENT_REQUEST';
  static const imageLimit = 'COMMON_ERROR_IMAGE_LIMIT';
  static const visitFormIncomplete = 'COMMON_ERROR_VISIT_FORM_INCOMPLETE';
}

/// Converts client failures to keys without exposing exception or plugin text.
/// API codes must go through [apiErrorKey] rather than being displayed directly.
String errorMessageKey(Object? error, {String fallback = ErrorKeys.generic}) {
  if (error is DioException) return dioErrorKey(error);
  final text = (error?.toString() ?? '').trim().replaceFirst(
        RegExp(r'^(Exception:\s*)+'),
        '',
      );
  if (text.isEmpty) return fallback;
  final lower = text.toLowerCase();
  if (lower.contains('session_expired')) return 'SESSION_EXPIRED';
  if (lower.contains('no network connection')) return ErrorKeys.noNetwork;
  if (lower.contains('no internet access')) return ErrorKeys.noInternet;
  if (lower.contains('failed host lookup') ||
      lower.contains('socketexception') ||
      lower.contains('software caused connection abort') ||
      lower.contains('connection error') ||
      lower.contains('connection reset') ||
      lower.contains('network is unreachable') ||
      // Cached failures written by older app versions.
      lower.contains('internet connection was interrupted')) {
    return ErrorKeys.connectionInterrupted;
  }
  if (RegExp(r'^[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+$').hasMatch(text)) {
    return text;
  }
  AppLogger.instance.info(error, title: 'Unmapped user-facing error');
  return fallback;
}

/// Only recognized API codes are allowed to select user-facing copy.
String apiErrorKey(Object? code) => switch (code) {
      'SESSION_EXPIRED' => 'SESSION_EXPIRED',
      'WORKFLOW_TRANSITION_FAILED' => ErrorKeys.workflowUpdate,
      'ASSESSMENT_UNABLE_TO_CONTACT_FAILED' => ErrorKeys.facilityContactUpdate,
      'ASSESSMENT_SUBMISSION_FAILED' => ErrorKeys.assessmentSubmission,
      'ASSESSMENT_INVALID_FORM_DATA' => ErrorKeys.invalidValue,
      'ASSESSMENT_UNSUPPORTED_FORM_TYPE' => ErrorKeys.assessmentFormUnavailable,
      _ => ErrorKeys.requestFailed,
    };

bool isConnectionErrorKey(String key) => const {
      ErrorKeys.noNetwork,
      ErrorKeys.noInternet,
      ErrorKeys.connectionInterrupted,
      ErrorKeys.serverConnection,
      ErrorKeys.requestTimeout,
    }.contains(key);

/// The offline-save claim is only valid after the caller confirms persistence.
String reportErrorKey(String key, {required bool savedLocally}) =>
    savedLocally && isConnectionErrorKey(key)
        ? ErrorKeys.reportConnectionInterrupted
        : key;

String dioErrorKey(DioException error) {
  AppLogger.instance.info(error, title: 'API error');
  if ((error.message ?? '').contains('SESSION_EXPIRED') ||
      error.response?.statusCode == 401) {
    return 'SESSION_EXPIRED';
  }

  dynamic body = error.response?.data;
  try {
    if (body is List<int>) body = utf8.decode(body);
    if (body is String) body = jsonDecode(body);
  } on FormatException {
    body = null;
  }

  if (body is Map) {
    final errors = body['Errors'];
    final nestedError = body['error'];
    final codes = [
      if (errors is List)
        for (final item in errors)
          if (item is Map) item['code'],
      if (nestedError is Map) nestedError['code'],
      if (nestedError is String) nestedError,
      body['code'],
    ];
    for (final code in codes) {
      final key = apiErrorKey(code);
      if (key != ErrorKeys.requestFailed) return key;
    }
  }

  final original = error.error?.toString() ?? error.message;
  final friendly = errorMessageKey(original);
  if (friendly != ErrorKeys.generic) {
    return friendly;
  }
  if (error.type == DioExceptionType.connectionTimeout ||
      error.type == DioExceptionType.sendTimeout ||
      error.type == DioExceptionType.receiveTimeout) {
    return ErrorKeys.requestTimeout;
  }
  if (error.type == DioExceptionType.connectionError) {
    return ErrorKeys.serverConnection;
  }
  return ErrorKeys.requestFailed;
}
