import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../model/assessment/assessment_mode.dart';
import '../model/assessment/assessment_queue.dart';
import '../utils/envConfig.dart';
import 'assessment_api_paths.dart';

class AssessmentQueueRepository {
  static const int defaultPageSize = 10;
  static const String queueSearchPath = AssessmentApiPaths.queueSearch;

  final Dio _dio;
  final String? _tenantId;

  AssessmentQueueRepository({Dio? dio, String? tenantId})
      : _dio = dio ?? DioClient().dio,
        _tenantId = tenantId;

  Future<AssessmentQueueResponse> search({
    required AssessmentMode assessmentMode,
    String? searchText,
    String sortOrder = 'DESC',
    int offset = 0,
    int limit = defaultPageSize,
  }) async {
    final normalizedSearch = searchText?.trim();
    final response = await _dio.post(
      queueSearchPath,
      data: <String, dynamic>{
        'assessmentPhase': assessmentMode.assessmentPhase,
        'tenantId': _tenantId ?? envConfig.variables.tenantId,
        if (normalizedSearch != null && normalizedSearch.isNotEmpty)
          'searchText': normalizedSearch,
        'sortBy': 'lastActionTime',
        'sortOrder': sortOrder,
        'offset': offset,
        'limit': limit,
      },
    );

    final data = response.data;
    if (data is! Map) {
      throw const FormatException('Invalid assessment queue response');
    }

    return AssessmentQueueResponse.fromJson(
      Map<String, dynamic>.from(data),
      requestedOffset: offset,
      requestedLimit: limit,
    );
  }
}

extension AssessmentModeApiValue on AssessmentMode {
  String get assessmentPhase =>
      this == AssessmentMode.remote ? 'PHONE' : 'FIELD';
}
