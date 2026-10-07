import 'dart:convert';

import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/assessment/assessment_mode.dart';
import '../model/assessment/assessment_queue.dart';
import '../utils/envConfig.dart';
import '../utils/constants.dart';
import 'assessment_queue_cache_repo.dart';
import 'cache_fallback.dart';
import 'assessment_api_paths.dart';

class AssessmentQueueRepository {
  static const int defaultPageSize = 10;
  static const String queueSearchPath = AssessmentApiPaths.queueSearch;

  final Dio _dio;
  final String? _tenantId;
  final String? _assessorId;
  final SecureStore _storage;
  final AssessmentQueueCache _cache;
  final Set<String> _importedScopes = {};

  AssessmentQueueRepository(
      {Dio? dio,
      String? tenantId,
      String? assessorId,
      SecureStore? storage,
      AssessmentQueueCache? cache})
      : _dio = dio ?? DioClient().dio,
        _tenantId = tenantId,
        _assessorId = assessorId,
        _storage = storage ?? SecureStore(),
        _cache = cache ?? IsarAssessmentQueueCache.lazy(() => Constants().isar);

  Future<AssessmentQueueResponse> search({
    required AssessmentMode assessmentMode,
    String? searchText,
    String sortOrder = 'DESC',
    int offset = 0,
    int limit = defaultPageSize,
  }) async {
    final normalizedSearch = searchText?.trim();
    final filters = <String, dynamic>{
      if (normalizedSearch != null && normalizedSearch.isNotEmpty)
        'facilityName': normalizedSearch,
    };
    final tenantId = _tenantId ?? envConfig.variables.tenantId;
    final assessorId =
        _assessorId ?? (await _storage.getAccessInfo())?.userRequest?.uuid;
    final cacheKey = <Object>[
      'queue',
      tenantId,
      assessorId ?? '',
      assessmentMode.assessmentPhase,
      normalizedSearch ?? '',
      sortOrder,
      offset,
      limit,
    ];
    final phase = assessmentMode.assessmentPhase;
    if (assessorId != null && assessorId.isNotEmpty) {
      final scope = jsonEncode(cacheKey.take(4).toList());
      if (!_importedScopes.contains(scope)) {
        final legacy = await _storage.getAssessmentQueueResponses(
            tenantId, assessorId, phase);
        final facilities = <String, AssessmentQueueFacility>{};
        for (final response in legacy) {
          try {
            final page = AssessmentQueueResponse.fromJson(response,
                requestedOffset: 0, requestedLimit: defaultPageSize);
            for (final facility in page.facilities) {
              final id = facility.planFacilityId;
              if (id == null || id.isEmpty) continue;
              final previous = facilities[id];
              if (previous == null ||
                  (facility.lastActionTime ?? 0) >
                      (previous.lastActionTime ?? 0)) {
                facilities[id] = facility;
              }
            }
          } on FormatException {
            // A malformed old response must not prevent other imports.
          }
        }
        await _cache.save(
            tenantId: tenantId,
            assessorId: assessorId,
            phase: phase,
            facilities: facilities.values.toList(),
            overwrite: false);
        _importedScopes.add(scope);
      }
    }
    Map<String, dynamic> data;
    try {
      final response = await _dio.post(
        queueSearchPath,
        data: <String, dynamic>{
          'assessmentPhase': assessmentMode.assessmentPhase,
          'tenantId': tenantId,
          'filters': filters,
          'sort': <String, dynamic>{
            'sortOrder': sortOrder,
          },
          'offset': offset,
          'limit': limit,
        },
      );

      if (response.data is! Map) {
        throw const FormatException('Invalid assessment queue response');
      }
      data = Map<String, dynamic>.from(response.data as Map);
      final page = AssessmentQueueResponse.fromJson(data,
          requestedOffset: offset, requestedLimit: limit);
      if (assessorId != null && assessorId.isNotEmpty) {
        await _cache.save(
            tenantId: tenantId,
            assessorId: assessorId,
            phase: phase,
            facilities: page.facilities);
        await _storage.setAssessmentResponse(cacheKey, data);
      }
    } catch (error) {
      if (isAuthenticationFailure(error)) rethrow;
      if (assessorId == null || assessorId.isEmpty) rethrow;
      return _cache.search(
          tenantId: tenantId,
          assessorId: assessorId,
          phase: phase,
          query: normalizedSearch ?? '',
          sortOrder: sortOrder,
          offset: offset,
          limit: limit);
    }

    return AssessmentQueueResponse.fromJson(
      Map<String, dynamic>.from(data),
      requestedOffset: offset,
      requestedLimit: limit,
    );
  }

  Future<int> count({required AssessmentMode assessmentMode}) async {
    final response = await search(
      assessmentMode: assessmentMode,
      sortOrder: 'DESC',
      offset: 0,
      limit: 0,
    );
    return response.count;
  }
}

extension AssessmentModeApiValue on AssessmentMode {
  String get assessmentPhase =>
      this == AssessmentMode.remote ? 'PHONE' : 'FIELD';
}
