import 'dart:io';

import 'package:digit_ui_components/utils/validators/file_validator.dart';
import 'package:file_picker/file_picker.dart';

const int documentUploadMaxFileSizeBytes = 5 * 1024 * 1024;

class RequiredDocumentLocalizationKeys {
  final String requiredInstallationCompletionCertificateTitle;
  final String uploadRequiredInstallationCompletionCertificateMessage;
  final String requiredAssetHandoverDocumentTitle;
  final String uploadRequiredAssetHandoverDocumentMessage;
  final String requiredBothDocumentsTitle;
  final String uploadRequiredBothDocumentsMessage;

  const RequiredDocumentLocalizationKeys({
    required this.requiredInstallationCompletionCertificateTitle,
    required this.uploadRequiredInstallationCompletionCertificateMessage,
    required this.requiredAssetHandoverDocumentTitle,
    required this.uploadRequiredAssetHandoverDocumentMessage,
    required this.requiredBothDocumentsTitle,
    required this.uploadRequiredBothDocumentsMessage,
  });
}

class MissingRequiredDocumentMessage {
  final String titleKey;
  final String messageKey;

  const MissingRequiredDocumentMessage({
    required this.titleKey,
    required this.messageKey,
  });
}

MissingRequiredDocumentMessage? missingRequiredDocumentMessage({
  required bool hasInstallationCompletionCertificate,
  required bool hasAssetHandoverDocument,
  required RequiredDocumentLocalizationKeys localizationKeys,
}) {
  if (hasInstallationCompletionCertificate && hasAssetHandoverDocument) {
    return null;
  }

  if (!hasInstallationCompletionCertificate && !hasAssetHandoverDocument) {
    return MissingRequiredDocumentMessage(
      titleKey: localizationKeys.requiredBothDocumentsTitle,
      messageKey: localizationKeys.uploadRequiredBothDocumentsMessage,
    );
  }

  if (!hasInstallationCompletionCertificate) {
    return MissingRequiredDocumentMessage(
      titleKey: localizationKeys.requiredInstallationCompletionCertificateTitle,
      messageKey: localizationKeys
          .uploadRequiredInstallationCompletionCertificateMessage,
    );
  }

  return MissingRequiredDocumentMessage(
    titleKey: localizationKeys.requiredAssetHandoverDocumentTitle,
    messageKey: localizationKeys.uploadRequiredAssetHandoverDocumentMessage,
  );
}

List<FileValidator> documentUploadMaxSizeValidators(String errorMessage) {
  return [
    FileValidator(
      FileValidatorType.maxSize,
      documentUploadMaxFileSizeBytes,
      errorMessage: errorMessage,
    ),
  ];
}

Future<bool> isFileOverDocumentUploadLimit(File file) async {
  try {
    return await file.length() > documentUploadMaxFileSizeBytes;
  } catch (_) {
    return false;
  }
}

Future<bool> isPlatformFileOverDocumentUploadLimit(PlatformFile file) async {
  final size = file.size;
  if (size > documentUploadMaxFileSizeBytes) return true;
  if (size > 0) return false;

  final path = file.path;
  if (path == null || path.isEmpty) return false;
  return isFileOverDocumentUploadLimit(File(path));
}

Future<bool> hasOversizedDocumentUploadFiles({
  required List<File> images,
  required List<PlatformFile> pdfs,
}) async {
  for (final image in images) {
    if (await isFileOverDocumentUploadLimit(image)) return true;
  }

  for (final pdf in pdfs) {
    if (await isPlatformFileOverDocumentUploadLimit(pdf)) return true;
  }

  return false;
}
