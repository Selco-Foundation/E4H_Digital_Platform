import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_mode.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'app_init_repo.dart' hide envConfig;
import 'cache_fallback.dart';
import 'assessment_api_paths.dart';
import 'assessment_bookmark_repo.dart';

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
  final SecureStore _storage;

  AssessmentFormRepository({
    Dio? dio,
    AppInitRepo? appInitRepo,
    String? tenantId,
    SecureStore? storage,
  })  : _dio = dio ?? DioClient().dio,
        _tenantId = tenantId,
        _storage = storage ?? SecureStore(),
        _appInitRepo = appInitRepo ?? AppInitRepo();

  Future<String> selectFormCode({
    required String facilityCategory,
    required AssessmentMode mode,
    String? questionnaire,
  }) async {
    final assigned = questionnaire?.trim();
    if (assigned != null && assigned.isNotEmpty) return assigned;
    final prefix = switch (facilityCategory.trim().toUpperCase()) {
      'HEALTH' => 'HF',
      'ANGANWADI' => 'AWC',
      _ =>
        throw const FormatException('Unsupported assessment facility category'),
    };
    return '${prefix}_${mode == AssessmentMode.remote ? 'PHONE' : 'FIELD'}';
  }

  Future<AssessmentFormResolution> resolveForm({
    required String planFacilityId,
    required String facilityCategory,
    required AssessmentMode assessmentMode,
    String? tenantId,
    String? formType,
  }) async {
    final expected = await selectFormCode(
      facilityCategory: facilityCategory,
      mode: assessmentMode,
      questionnaire: formType,
    );
    try {
      final response = await _dio.post(
        resolvePath,
        data: {
          'planFacilityId': planFacilityId,
          'facilityCategory': facilityCategory.trim().toUpperCase(),
          'assessmentPhase':
              assessmentMode == AssessmentMode.remote ? 'PHONE' : 'FIELD',
          'tenantId': tenantId ?? _tenantId ?? envConfig.variables.tenantId,
        },
      );
      final resolution = AssessmentFormResolution.fromJson(
        _responseMap(response.data),
      );
      if (resolution.formType != expected) {
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

  Future<AssessmentFacilityDetails?> getFacilityDetails({
    required String facilityId,
  }) async {
    final cacheKey = <Object>[
      'facility',
      _tenantId ?? envConfig.variables.tenantId,
      facilityId,
    ];
    final cached = await _storage.getAssessmentResponse(cacheKey);
    if (cached != null) return AssessmentFacilityDetails.fromJson(cached);
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
      final data = Map<String, dynamic>.from(first);
      final details = AssessmentFacilityDetails.fromJson(data);
      await _storage.setAssessmentResponse(cacheKey, data);
      return details;
    } on DioException catch (error) {
      throw _parseDioError(error);
    }
  }

  Future<Map<String, dynamic>> loadMobileSchema(
    String formType,
  ) async {
    final cached = await _appInitRepo.loadByName('Assessment.$formType');
    if (_isValidTransformedSchema(cached, formType)) return cached!;
    final documents = await _searchMobileSchemas();
    await _transformAndCache(documents);
    return loadCachedMobileSchema(formType);
  }

  Future<Map<String, dynamic>> loadCachedMobileSchema(String formType) async {
    final cached = await _appInitRepo.loadByName('Assessment.$formType');
    if (!_isValidTransformedSchema(cached, formType)) {
      throw FormatException(
          'Assessment mobile schema is unavailable: $formType');
    }
    return cached!;
  }

  Future<void> preloadMobileSchemas({bool forceRefresh = true}) async {
    if (!forceRefresh && await _loadCompleteTransformedCache() != null) return;

    if (forceRefresh) {
      try {
        final documents = await _searchMobileSchemas();
        await _transformAndCache(documents);
        if (await _loadCompleteTransformedCache() == null) {
          throw const FormatException('Assessment mobile schemas are missing');
        }
        return;
      } catch (remoteError, stackTrace) {
        if (isAuthenticationFailure(remoteError)) rethrow;
        AppLogger.instance.error(
          title: 'Assessment mobile schema refresh',
          message: remoteError.toString(),
          stackTrace: stackTrace,
        );
        if (await _loadCompleteTransformedCache() != null) return;
        try {
          final cachedDocuments = await _searchMobileSchemas(cacheOnly: true);
          await _transformAndCache(cachedDocuments);
          if (await _loadCompleteTransformedCache() == null) {
            throw const FormatException(
                'Assessment mobile schemas are missing');
          }
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
    if (documents.isEmpty) {
      throw const FormatException('Assessment mobile schemas are missing');
    }
    final found = <String>{};
    for (final document in documents) {
      if (document['schemaCode']?.toString() != mobileSchemaCode ||
          document['isActive'] == false) {
        throw const FormatException('Invalid assessment mobile schema record');
      }
      final identifier = document['uniqueIdentifier']?.toString().trim();
      final data = document['data'];
      if (identifier == null || identifier.isEmpty || data is! Map) {
        throw const FormatException('Unknown assessment mobile form');
      }
      final form = Map<String, dynamic>.from(data);
      final dataType = form['formType']?.toString().trim();
      if (dataType != identifier ||
          form['name']?.toString() != 'Assessment.$identifier' ||
          !found.add(identifier)) {
        throw FormatException(
          '$identifier mobile schema identifiers do not match',
        );
      }
      _validatePages(identifier, form['pages']);
    }
  }

  void _validatePages(String formType, Object? value) {
    if (value is! List || value.isEmpty) {
      throw FormatException('$formType mobile schema has no pages');
    }
    final pageNames = <String>{};
    for (final rawPage in value) {
      if (rawPage is! Map) {
        throw FormatException('$formType contains an invalid page');
      }
      final page = Map<String, dynamic>.from(rawPage);
      final pageName = page['page']?.toString().trim();
      final properties = page['properties'];
      if (pageName == null ||
          pageName.isEmpty ||
          !pageNames.add(pageName) ||
          properties is! List ||
          properties.isEmpty) {
        throw FormatException('$formType contains an invalid page');
      }
      final fieldNames = <String>{};
      for (final rawField in properties) {
        if (rawField is! Map) {
          throw FormatException('$formType contains an invalid field');
        }
        final fieldName = rawField['fieldName']?.toString().trim();
        if (fieldName == null ||
            fieldName.isEmpty ||
            !fieldNames.add(fieldName)) {
          throw FormatException('$formType contains an invalid field');
        }
      }
    }
  }

  Future<void> _transformAndCache(
    List<Map<String, dynamic>> documents,
  ) async {
    _validateMobileSchemaDocuments(documents);
    for (final document in documents) {
      final formType = document['uniqueIdentifier'].toString().trim();
      final transformed = transformSelcoFormMdmsDocToSchema(document);
      transformed['name'] = 'Assessment.$formType';
      transformed['uniqueIdentifier'] = formType;
      await _appInitRepo.upsertTransformedSchema(transformed);
    }
  }

  Future<Map<String, Map<String, dynamic>>?>
      _loadCompleteTransformedCache() async {
    final result = <String, Map<String, dynamic>>{};
    final List<Map<String, dynamic>> documents;
    try {
      documents = await _searchMobileSchemas(cacheOnly: true);
    } catch (_) {
      return null;
    }
    final codes =
        documents.map((doc) => doc['uniqueIdentifier'].toString().trim());
    for (final formType in codes) {
      final cached = await _appInitRepo.loadByName('Assessment.$formType');
      if (!_isValidTransformedSchema(cached, formType)) return null;
      result[formType] = cached!;
    }
    return result;
  }

  bool _isValidTransformedSchema(
    Map<String, dynamic>? schema,
    String formType,
  ) {
    if (schema == null ||
        schema['name']?.toString() != 'Assessment.$formType' ||
        schema['uniqueIdentifier']?.toString() != formType) {
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
    AssessmentSubmissionRequest request, {
    String? formType,
  }) async {
    await resolveForm(
      formType: formType,
      tenantId: request.tenantId,
      planFacilityId: request.planFacilityId,
      facilityCategory: request.facilityCategory,
      assessmentMode: request.assessmentPhase == AssessmentPhase.PHONE
          ? AssessmentMode.remote
          : AssessmentMode.onSite,
    );
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
      try {
        final user = (await _storage.getAccessInfo())?.userRequest;
        final assessorId = user?.uuid ?? user?.userName;
        if (assessorId != null && assessorId.isNotEmpty) {
          await AssessmentBookmarkRepository(
                  storage: _storage,
                  tenantId: request.tenantId,
                  assessorId: assessorId,
                  phase: request.assessmentPhase)
              .invalidate(request.planFacilityId);
        }
      } catch (error) {
        AppLogger.instance.error(
            title: 'Assessment bookmark snapshot invalidation',
            message: error.toString());
      }
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
