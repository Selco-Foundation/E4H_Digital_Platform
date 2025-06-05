// lib/repositories/asset_repository.dart

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:http_parser/http_parser.dart';

import '../data/remote_client.dart';
import '../utils/envConfig.dart';

/// A simple DTO for the filestore response: we only care about the first file’s `fileStoreId`.
class FileStoreResponse {
  final String fileStoreId;
  FileStoreResponse({required this.fileStoreId});

  factory FileStoreResponse.fromJson(Map<String, dynamic> json) {
    final files = (json['files'] as List<dynamic>?);
    if (files == null || files.isEmpty) {
      throw Exception("Filestore returned no files array.");
    }
    final first = files.first as Map<String, dynamic>;
    return FileStoreResponse(
      fileStoreId: first['fileStoreId'] as String,
    );
  }
}

/// AssetRepository handles:
///  1. uploading a file to `/filestore/v1/files`
///  2. creating an asset via `/asset‐registry/v1/asset/_create`
///
/// This version uses `DioClient().dio` rather than instantiating a fresh `Dio()`.
class AssetRepository {
  AssetRepository({
    required this.tenantId,
    required this.authToken,
  }) {
    // Ensure the base URL is set from EnvConfig
    _dio.options.baseUrl = envConfig.variables.baseUrl;
  }

  final Dio _dio = DioClient().dio;
  final String tenantId;
  final String authToken;

  /// Uploads [file] to filestore and returns its `fileStoreId`.
  Future<String> uploadFile(File file) async {
    final fileName = file.path.split(Platform.pathSeparator).last;
    final mimeType = _lookupMimeType(fileName);

    final formData = FormData.fromMap({
      "file": await MultipartFile.fromFile(
        file.path,
        filename: fileName,
        contentType: MediaType.parse(mimeType),
      ),
      "tenantId": tenantId,
      "module": "Incident", // adjust if needed
    });

    try {
      final response = await _dio.post(
        "/filestore/v1/files",
        data: formData,
        options: Options(
          headers: {
            "auth-token": authToken,
            "Accept": "application/json, text/plain, */*",
          },
          contentType: "multipart/form-data",
        ),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final jsonMap = response.data as Map<String, dynamic>;
        final fsr = FileStoreResponse.fromJson(jsonMap);
        return fsr.fileStoreId;
      } else {
        throw Exception(
            "Filestore responded with status ${response.statusCode}");
      }
    } on DioError catch (dioErr) {
      debugPrint("Filestore upload error: ${dioErr.response?.data ?? dioErr}");
      rethrow;
    }
  }

  /// Creates a new asset using the given [payload].
  Future<void> createAsset(Map<String, dynamic> payload) async {
    try {
      final response = await _dio.post(
        "/asset‐registry/v1/asset/_create",
        data: payload,
        options: Options(
          headers: {
            "Content-Type": "application/json",
            "auth-token": authToken,
          },
        ),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        // success
        return;
      } else {
        throw Exception(
            "Create Asset responded with status ${response.statusCode}");
      }
    } on DioError catch (dioErr) {
      debugPrint("Create asset error: ${dioErr.response?.data ?? dioErr}");
      rethrow;
    }
  }

  /// Very simple MIME lookup based on file extension.
  String _lookupMimeType(String fileName) {
    final ext = fileName.split(".").last.toLowerCase();
    switch (ext) {
      case "png":
        return "image/png";
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "mp4":
        return "video/mp4";
      case "mov":
        return "video/quicktime";
      default:
        return "application/octet-stream";
    }
  }
}
