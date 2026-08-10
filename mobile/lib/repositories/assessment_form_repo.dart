import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_mode.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'assessment_api_paths.dart';

typedef AssessmentSchemaLoader = Future<String> Function();

class AssessmentFormRepository {
  static const resolvePath = AssessmentApiPaths.formResolve;
  static const phoneSubmissionPath = AssessmentApiPaths.phoneSubmission;
  static const phoneUnableToContactPath =
      AssessmentApiPaths.phoneUnableToContact;
  static const fieldSubmissionPath = AssessmentApiPaths.fieldSubmission;
  static const facilitySearchPath = AssessmentApiPaths.facilitySearch;
  static const mobileSchemaAsset =
      'assets/forms/assessment_mobile_form_schema.json';

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
            schemaLoader ?? (() => rootBundle.loadString(mobileSchemaAsset));

  Future<AssessmentFormResolution> resolveForm({
    required String planFacilityId,
    required String facilityCategory,
    required AssessmentMode assessmentMode,
  }) async {
    final expected = AssessmentFormType.expectedFor(
      facilityCategory: facilityCategory,
      mode: assessmentMode,
    );
    if (expected == null) {
      throw const AssessmentApiException(
        code: 'ASSESSMENT_UNSUPPORTED_FACILITY_CATEGORY',
        message: 'Unsupported assessment facility category',
      );
    }
    try {
      final response = await _dio.post(
        resolvePath,
        data: {
          'planFacilityId': planFacilityId,
          'facilityCategory': expected.facilityCategory,
          'assessmentPhase': expected.phase.name,
          'tenantId': _tenantId ?? envConfig.variables.tenantId,
        },
      );
      final resolution = AssessmentFormResolution.fromJson(
        _responseMap(response.data),
      );
      if (resolution.formType != expected) {
        throw AssessmentApiException(
          statusCode: response.statusCode,
          code: 'ASSESSMENT_UNSUPPORTED_FORM_TYPE',
          message: 'Unsupported assessment form: ${resolution.formType.name}',
        );
      }
      return resolution;
    } on DioException catch (error) {
      throw _parseDioError(error);
    }
  }

  Future<AssessmentFacilityDetails?> getFacilityDetails({
    required String facilityId,
  }) async {
    try {
      final response = await _dio.get(
        facilitySearchPath,
        queryParameters: {
          'tenantId': _tenantId ?? envConfig.variables.tenantId,
          'facilityId': facilityId,
        },
        data: <String, dynamic>{},
        options: Options(
          headers: {
            Headers.acceptHeader: Headers.jsonContentType,
            Headers.contentTypeHeader: Headers.jsonContentType,
          },
        ),
      );
      final facilities = _responseMap(response.data)['facilities'];
      if (facilities is! List || facilities.isEmpty) return null;
      final first = facilities.first;
      if (first is! Map) return null;
      return AssessmentFacilityDetails.fromJson(
        Map<String, dynamic>.from(first),
      );
    } on DioException catch (error) {
      throw _parseDioError(error);
    }
  }

  Future<Map<String, dynamic>> loadMobileSchema(
    AssessmentFormType formType,
  ) async {
    final decoded = jsonDecode(await _schemaLoader());
    if (decoded is! Map) {
      throw const FormatException('Invalid assessment mobile schema');
    }
    final root = Map<String, dynamic>.from(decoded);
    final docs = root['mdms'];
    if (docs is! List) {
      throw const FormatException('Assessment mobile schema is missing');
    }
    final matches = <Map<String, dynamic>>[];
    for (final value in docs.whereType<Map>()) {
      final candidate = Map<String, dynamic>.from(value);
      final data = candidate['data'];
      if (data is! Map) continue;
      final dataFormType =
          AssessmentFormType.fromCode(data['formType']?.toString());
      final identifierFormType = AssessmentFormType.fromCode(
        candidate['uniqueIdentifier']?.toString(),
      );
      if (dataFormType == formType || identifierFormType == formType) {
        if (dataFormType != formType || identifierFormType != formType) {
          throw FormatException(
            '${formType.name} mobile schema identifiers do not match',
          );
        }
        matches.add(candidate);
      }
    }
    if (matches.isEmpty) {
      throw FormatException('${formType.name} mobile schema is missing');
    }
    if (matches.length > 1) {
      throw FormatException('${formType.name} mobile schema is duplicated');
    }
    final record = matches.single;
    final transformed = transformSelcoFormMdmsDocToSchema(record);
    transformed['name'] = formType.schemaName;
    transformed['uniqueIdentifier'] = formType.name;
    return transformed;
  }

  Future<void> markPhoneUnableToContact({
    required String planFacilityId,
    required AssessmentUnableToContactReason reason,
  }) async {
    try {
      final response = await _dio.post(
        phoneUnableToContactPath,
        data: {
          'planFacilityId': planFacilityId,
          'reason': reason.name,
        },
      );
      final statusCode = response.statusCode ?? 0;
      if (statusCode < 200 || statusCode >= 300) {
        throw AssessmentApiException(
          statusCode: response.statusCode,
          code: 'ASSESSMENT_UNABLE_TO_CONTACT_FAILED',
          message: 'Unable to update facility contact status',
        );
      }
    } on DioException catch (error) {
      throw _parseDioError(error);
    }
  }

  Future<AssessmentSubmissionResponse> submitAssessment(
    AssessmentSubmissionRequest request,
  ) async {
    final path = switch (request.assessmentPhase) {
      AssessmentPhase.PHONE => phoneSubmissionPath,
      AssessmentPhase.FIELD => fieldSubmissionPath,
    };
    try {
      final response = await _dio.post(path, data: request.toJson());
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
