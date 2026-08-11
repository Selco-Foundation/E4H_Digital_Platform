import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_mode.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'app_init_repo.dart' hide envConfig;
import 'assessment_api_paths.dart';

class AssessmentFormRepository {
  static const mobileSchemaCode = 'assessment.AssessmentMobileFormSchema';
  static const resolvePath = AssessmentApiPaths.formResolve;
  static const phoneSubmissionPath = AssessmentApiPaths.phoneSubmission;
  static const phoneUnableToContactPath =
      AssessmentApiPaths.phoneUnableToContact;
  static const fieldSubmissionPath = AssessmentApiPaths.fieldSubmission;
  static const facilitySearchPath = AssessmentApiPaths.facilitySearch;

  final Dio _dio;
  final AppInitRepo _appInitRepo;
  final String? _tenantId;

  AssessmentFormRepository({
    Dio? dio,
    AppInitRepo? appInitRepo,
    String? tenantId,
  })  : _dio = dio ?? DioClient().dio,
        _tenantId = tenantId,
        _appInitRepo = appInitRepo ?? AppInitRepo();

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
    final cached = await _loadCompleteTransformedCache();
    if (cached != null) return cached[formType]!;

    final documents = await _searchMobileSchemas(useCacheRead: true);
    await _transformAndCache(documents);
    final refreshed = await _loadCompleteTransformedCache();
    if (refreshed == null) {
      throw const FormatException(
        'Assessment mobile schema cache is incomplete',
      );
    }
    return refreshed[formType]!;
  }

  Future<void> preloadMobileSchemas({bool forceRefresh = true}) async {
    if (!forceRefresh && await _loadCompleteTransformedCache() != null) {
      return;
    }

    if (forceRefresh) {
      try {
        final documents = await _searchMobileSchemas();
        await _transformAndCache(documents);
        return;
      } catch (remoteError, stackTrace) {
        AppLogger.instance.error(
          title: 'Assessment mobile schema refresh',
          message: remoteError.toString(),
          stackTrace: stackTrace,
        );
        if (await _loadCompleteTransformedCache() != null) return;
        try {
          final cachedDocuments = await _searchMobileSchemas(cacheOnly: true);
          await _transformAndCache(cachedDocuments);
          return;
        } catch (_) {
          Error.throwWithStackTrace(remoteError, stackTrace);
        }
      }
    }

    final documents = await _searchMobileSchemas(useCacheRead: true);
    await _transformAndCache(documents);
  }

  Future<List<Map<String, dynamic>>> _searchMobileSchemas({
    bool useCacheRead = false,
    bool cacheOnly = false,
  }) {
    return _appInitRepo.searchAssessmentFormConfigsRaw(
      MdmsRequestModel(
        mdmsCriteria: MdmsCriteriaModel(
          tenantId: _tenantId ?? envConfig.variables.tenantId,
          schemaCode: mobileSchemaCode,
          moduleDetails: const [],
        ),
      ),
      useCacheRead: useCacheRead,
      cacheOnly: cacheOnly,
      validator: _validateMobileSchemaDocuments,
    );
  }

  void _validateMobileSchemaDocuments(
    List<Map<String, dynamic>> documents,
  ) {
    if (documents.length != AssessmentFormType.values.length) {
      throw const FormatException(
        'Assessment mobile schema must contain exactly four forms',
      );
    }
    final found = <AssessmentFormType>{};
    for (final document in documents) {
      if (document['schemaCode']?.toString() != mobileSchemaCode ||
          document['isActive'] == false) {
        throw const FormatException('Invalid assessment mobile schema record');
      }
      final identifier = AssessmentFormType.fromCode(
        document['uniqueIdentifier']?.toString(),
      );
      final data = document['data'];
      if (identifier == null || data is! Map) {
        throw const FormatException('Unknown assessment mobile form');
      }
      final form = Map<String, dynamic>.from(data);
      final dataType = AssessmentFormType.fromCode(
        form['formType']?.toString(),
      );
      if (dataType != identifier ||
          form['name']?.toString() != identifier.schemaName ||
          !found.add(identifier)) {
        throw FormatException(
          '${identifier.name} mobile schema identifiers do not match',
        );
      }
      _validatePages(identifier, form['pages']);
    }
    if (found.length != AssessmentFormType.values.length) {
      throw const FormatException('Assessment mobile forms are incomplete');
    }
  }

  void _validatePages(AssessmentFormType formType, Object? value) {
    if (value is! List || value.isEmpty) {
      throw FormatException('${formType.name} mobile schema has no pages');
    }
    final pageNames = <String>{};
    for (final rawPage in value) {
      if (rawPage is! Map) {
        throw FormatException('${formType.name} contains an invalid page');
      }
      final page = Map<String, dynamic>.from(rawPage);
      final pageName = page['page']?.toString().trim();
      final properties = page['properties'];
      if (pageName == null ||
          pageName.isEmpty ||
          !pageNames.add(pageName) ||
          properties is! List ||
          properties.isEmpty) {
        throw FormatException('${formType.name} contains an invalid page');
      }
      final fieldNames = <String>{};
      for (final rawField in properties) {
        if (rawField is! Map) {
          throw FormatException('${formType.name} contains an invalid field');
        }
        final fieldName = rawField['fieldName']?.toString().trim();
        if (fieldName == null ||
            fieldName.isEmpty ||
            !fieldNames.add(fieldName)) {
          throw FormatException('${formType.name} contains an invalid field');
        }
      }
    }
  }

  Future<void> _transformAndCache(
    List<Map<String, dynamic>> documents,
  ) async {
    _validateMobileSchemaDocuments(documents);
    for (final document in documents) {
      final formType = AssessmentFormType.fromCode(
        document['uniqueIdentifier']?.toString(),
      )!;
      final transformed = transformSelcoFormMdmsDocToSchema(document);
      transformed['name'] = formType.schemaName;
      transformed['uniqueIdentifier'] = formType.name;
      await _appInitRepo.upsertTransformedSchema(transformed);
    }
  }

  Future<Map<AssessmentFormType, Map<String, dynamic>>?>
      _loadCompleteTransformedCache() async {
    final result = <AssessmentFormType, Map<String, dynamic>>{};
    for (final formType in AssessmentFormType.values) {
      final cached = await _appInitRepo.loadByName(formType.schemaName);
      if (!_isValidTransformedSchema(cached, formType)) return null;
      result[formType] = cached!;
    }
    return result;
  }

  bool _isValidTransformedSchema(
    Map<String, dynamic>? schema,
    AssessmentFormType formType,
  ) {
    if (schema == null ||
        schema['name']?.toString() != formType.schemaName ||
        schema['uniqueIdentifier']?.toString() != formType.name) {
      return false;
    }
    final pages = schema['pages'];
    if (pages is! Map || pages.isEmpty) return false;
    for (final rawPage in pages.values) {
      if (rawPage is! Map) return false;
      final properties = rawPage['properties'];
      if (properties is! Map || properties.isEmpty) return false;
      for (final rawProperty in properties.values) {
        if (rawProperty is! Map ||
            rawProperty['fieldName']?.toString().trim().isEmpty != false ||
            rawProperty['type'] == null) {
          return false;
        }
      }
    }
    return true;
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
