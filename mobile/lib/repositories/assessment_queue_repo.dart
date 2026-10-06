import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/assessment/assessment_mode.dart';
import '../model/assessment/assessment_queue.dart';
import '../utils/envConfig.dart';
import 'assessment_api_paths.dart';

class AssessmentQueueRepository {
  static const int defaultPageSize = 10;
  static const String queueSearchPath = AssessmentApiPaths.queueSearch;

  final Dio _dio;
  final String? _tenantId;
  final String? _assessorId;
  final SecureStore _storage;

  AssessmentQueueRepository(
      {Dio? dio, String? tenantId, String? assessorId, SecureStore? storage})
      : _dio = dio ?? DioClient().dio,
        _tenantId = tenantId,
        _assessorId = assessorId,
        _storage = storage ?? SecureStore();

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
      AssessmentQueueResponse.fromJson(data,
          requestedOffset: offset, requestedLimit: limit);
      if (assessorId != null && assessorId.isNotEmpty) {
        await _storage.setAssessmentResponse(cacheKey, data);
      }
    } catch (error) {
      if (error is DioException &&
          (error.response?.statusCode == 401 ||
              error.response?.statusCode == 403 ||
              error.message == 'SESSION_EXPIRED')) {
        rethrow;
      }
      if (assessorId == null || assessorId.isEmpty) rethrow;
      final cached = await _storage.getAssessmentResponse(cacheKey);
      if (cached == null) rethrow;
      data = cached;
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
