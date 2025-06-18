// lib/repositories/asset_repository.dart

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:http_parser/http_parser.dart';

import '../data/remote_client.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';

class FileStoreResponse {
  final String fileStoreId;
  FileStoreResponse({required this.fileStoreId});

  factory FileStoreResponse.fromJson(Map<String, dynamic> json) {
    final files = json['files'] as List<dynamic>?;
    if (files == null || files.isEmpty) {
      throw Exception("Filestore returned no files array.");
    }

    final first = files.first as Map<String, dynamic>;
    return FileStoreResponse(fileStoreId: first['fileStoreId'] as String);
  }
}

class AssetRepository {
  AssetRepository({
    required this.tenantId,
    required this.authToken,
  }) {
    _dio.options.baseUrl = envConfig.variables.baseUrl;
  }

  final Dio _dio = DioClient().dio;
  final String tenantId;
  final String authToken;

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
      "module": "Incident",
    });

    try {
      final response = await _dio.post("/filestore/v1/files", data: formData);

      if (response.statusCode == 200 || response.statusCode == 201) {
        final jsonMap = response.data as Map<String, dynamic>;
        return FileStoreResponse.fromJson(jsonMap).fileStoreId;
      } else {
        throw Exception(
            "Filestore responded with status ${response.statusCode}");
      }
    } on DioError catch (e) {
      throw DioErrorParser.parse(e);
    }
  }

  Future<void> createAsset(Map<String, dynamic> payload) async {
    try {
      final response =
          await _dio.post("/asset‐registry/v1/asset/_create", data: payload);

      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception(
            "Create Asset responded with status ${response.statusCode}");
      }
    } on DioError catch (e) {
      throw DioErrorParser.parse(e);
    }
  }

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
