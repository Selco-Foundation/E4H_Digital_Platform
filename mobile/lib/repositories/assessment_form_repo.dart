import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../model/assessment/assessment_form.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'assessment_api_paths.dart';

typedef AssessmentSchemaLoader = Future<String> Function();

class AssessmentFormRepository {
  static const resolvePath = AssessmentApiPaths.formResolve;
  static const phoneSubmissionPath = AssessmentApiPaths.phoneSubmission;
  static const phoneFormType = 'HF_PHONE';
  static const localSchemaAsset = 'assets/forms/assessment_hf_phone.json';

  final Dio _dio;
  final AssessmentSchemaLoader _schemaLoader;
  final String? _tenantId;

  AssessmentFormRepository({
    Dio? dio,
    AssessmentSchemaLoader? schemaLoader,
    String? tenantId,
  })  : _dio = dio ?? DioClient().dio,
        _tenantId = tenantId,
        _schemaLoader =
            schemaLoader ?? (() => rootBundle.loadString(localSchemaAsset));

  Future<AssessmentFormResolution> resolvePhoneForm({
    required String planFacilityId,
    required String facilityCategory,
  }) async {
    try {
      final response = await _dio.post(
        resolvePath,
        data: {
          'planFacilityId': planFacilityId,
          'facilityCategory': facilityCategory,
          'assessmentPhase': 'PHONE',
          'tenantId': _tenantId ?? envConfig.variables.tenantId,
        },
      );
      final data = _responseMap(response.data);
      final resolution = AssessmentFormResolution.fromJson(data);
      if (resolution.formType.toUpperCase() != phoneFormType) {
        throw AssessmentApiException(
          statusCode: response.statusCode,
          code: 'ASSESSMENT_UNSUPPORTED_FORM_TYPE',
          message: 'Unsupported assessment form: ${resolution.formType}',
        );
      }
      return resolution;
    } on DioException catch (error) {
      throw _parseDioError(error);
    }
  }

  Future<Map<String, dynamic>> loadPhoneMobileSchema() async {
    final decoded = jsonDecode(await _schemaLoader());
    if (decoded is! Map) {
      throw const FormatException('Invalid assessment mobile schema');
    }
    final root = Map<String, dynamic>.from(decoded);
    final docs = root['mdms'];
    if (docs is! List) {
      throw const FormatException('Assessment mobile schema is missing');
    }
    Map<String, dynamic>? record;
    for (final value in docs.whereType<Map>()) {
      final candidate = Map<String, dynamic>.from(value);
      final data = candidate['data'];
      if (data is Map &&
          data['formType']?.toString().toUpperCase() == phoneFormType) {
        record = candidate;
        break;
      }
    }
    if (record == null) {
      throw const FormatException('HF_PHONE mobile schema is missing');
    }
    final transformed = transformSelcoFormMdmsDocToSchema(record);
    transformed['uniqueIdentifier'] = phoneFormType;
    return transformed;
  }

  Future<AssessmentSubmissionResponse> submitPhoneAssessment(
    AssessmentSubmissionRequest request,
  ) async {
    try {
      final response = await _dio.post(
        phoneSubmissionPath,
        data: request.toJson(),
      );
      if (response.statusCode != 200 && response.statusCode != 201) {
        throw AssessmentApiException(
          statusCode: response.statusCode,
          code: 'ASSESSMENT_SUBMISSION_FAILED',
          message: 'Assessment submission failed',
        );
      }
      final result = AssessmentSubmissionResponse.fromJson(
        _responseMap(response.data),
      );
      AppLogger.instance.info(
        {
          'statusCode': response.statusCode,
          'submissionId': result.submissionId,
          'idempotentReplay': result.idempotentReplay,
        },
        title: 'assessmentSubmissionResult',
      );
      return result;
    } on DioException catch (error) {
      throw _parseDioError(error);
    }
  }

  Map<String, dynamic> _responseMap(Object? data) {
    if (data is! Map) {
      throw const FormatException('Invalid assessment API response');
    }
    return Map<String, dynamic>.from(data);
  }

  AssessmentApiException _parseDioError(DioException error) {
    final data = error.response?.data;
    final errors = data is Map ? data['Errors'] : null;
    final first = errors is List && errors.isNotEmpty && errors.first is Map
        ? Map<String, dynamic>.from(errors.first as Map)
        : const <String, dynamic>{};
    final rawFields = first['params'];
    return AssessmentApiException(
      statusCode: error.response?.statusCode,
      code: first['code']?.toString() ??
          (error.message == 'SESSION_EXPIRED'
              ? 'SESSION_EXPIRED'
              : 'ASSESSMENT_NETWORK_ERROR'),
      message: first['message']?.toString() ??
          error.message ??
          'Assessment request failed',
      fields: rawFields is List
          ? rawFields.map((value) => value.toString()).toList()
          : const [],
    );
  }
}
