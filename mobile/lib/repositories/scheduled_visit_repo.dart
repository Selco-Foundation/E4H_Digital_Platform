import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_scheduled_visit.dart';
import '../data/remote_client.dart';
import '../model/document/document.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../utils/envConfig.dart';

class PaginatedScheduledVisits {
  final List<ScheduledVisit> items;
  final int totalCount;
  final bool fromCache;

  PaginatedScheduledVisits({
    required this.items,
    required this.totalCount,
    this.fromCache = false,
  });
}

/// Remote-only repository (like ActivityFacilityRemoteRepository)
class ScheduledVisitRemoteRepository {
  ScheduledVisitRemoteRepository();

  final dio = DioClient().dio;

  Future<ScheduledVisitSearchResponse> search({
    required ScheduledVisitSearchCriteria criteria,
    required int limit,
    required int offset,
  }) async {
    try {
      const searchPath = 'asset-amc/v1/visit/_search';
      final response = await dio.post(
        searchPath,
        queryParameters: {
          'tenantId': envConfig.variables.tenantId,
          'limit': limit,
          'offset': offset,
        },
        data: {
          'searchCriteria': {
            'tenantId': envConfig.variables.tenantId,
            // ...criteria.toApiMap(),
            //'statuses': ['DRAFT']
            "ids": ["540ed531-5f5a-413f-bd28-b99d2b2eab82"]
          },
        },
      );

      return ScheduledVisitSearchResponse.fromJson(
        Map<String, dynamic>.from(response.data as Map),
      );
    } on DioError catch (err) {
      AppLogger.instance
          .info('ScheduledVisitRemoteRepository.search error: $err');
      rethrow;
    }
  }

  Future<void> updateVisitWorkflow({
    required String visitId,
    required String schemaCode,
    required int version,
    required Map<String, dynamic> responses,
    List<Document>? workflowDocuments,
    List<Document>? visitDocuments,
  }) async {
    const path = 'asset-amc/v1/visit/workflow/_update';

    try {
      final body = {
        'visitId': visitId,
        'workflow': {
          'action': 'SUBMIT_VISIT_REPORT',
          'comment': 'Submit Visit Report Action',
          if (workflowDocuments != null) ...{
            'documents':
                workflowDocuments.map((d) => d.toJsonForWorkflow()).toList(),
          },
          'additionalDetails': <String, dynamic>{},
        },
        'visitReport': {
          'schemaCode': schemaCode,
          'version': version,
          'otpReference': null,
          'responses': responses,
          if (visitDocuments != null) ...{
            'documents':
                visitDocuments.map((d) => d.toJsonForWorkflow()).toList(),
          },
          'additionalDetails': <String, dynamic>{},
        },
      };

      final resp = await dio.post(
        path,
        queryParameters: {
          'tenantId': envConfig.variables.tenantId,
        },
        data: body,
      );

      AppLogger.instance.info(
        'ScheduledVisitRemoteRepository.updateVisitWorkflow status=${resp.statusCode}',
      );
    } on DioError catch (e) {
      AppLogger.instance.info(
        'ScheduledVisitRemoteRepository.updateVisitWorkflow DioError=$e',
      );
      rethrow;
    } catch (e) {
      AppLogger.instance.info(
        'ScheduledVisitRemoteRepository.updateVisitWorkflow error=$e',
      );
      rethrow;
    }
  }
}

/// Combined remote + cache, with pagination and fallback
class ScheduledVisitRepository {
  static const int defaultPageSize = 10;

  final Isar _isar;
  final ScheduledVisitRemoteRepository _remote;

  ScheduledVisitRepository(this._isar)
      : _remote = ScheduledVisitRemoteRepository();

  /// Main entry point for UI / bloc.
  ///
  /// Behaviour:
  /// - Try remote with pagination.
  ///   - If offset == 0, clear cache for this facility, write fresh list.
  ///   - If offset > 0, append to cache.
  /// - If remote fails, read from cache and paginate in memory.
  Future<PaginatedScheduledVisits> fetchByWorkflowStatus({
    required String status,
    int limit = defaultPageSize,
    int offset = 0,
  }) async {
    final criteria = ScheduledVisitSearchCriteria(
      tenantId: envConfig.variables.tenantId,
      status: status,
    );

    try {
      final remoteResp = await _remote.search(
        criteria: criteria,
        limit: limit,
        offset: offset,
      );

      final items = remoteResp.scheduledVisits;

      if (offset == 0) {
        await _replaceCache(status, items);
      } else {
        await _appendCache(status, items);
      }

      return PaginatedScheduledVisits(
        items: items,
        totalCount: remoteResp.totalCount,
        fromCache: false,
      );
    } catch (e, st) {
      AppLogger.instance.info(
        'Failed to fetch scheduled visits remotely, falling back to cache: $e',
      );
      AppLogger.instance.debug(st.toString());

      final cachedItems = await _readCache(
        status: status,
        limit: limit,
        offset: offset,
      );
      final total = await _countCache(status);

      return PaginatedScheduledVisits(
        items: cachedItems,
        totalCount: total,
        fromCache: true,
      );
    }
  }

  Future<void> _replaceCache(
    String status,
    List<ScheduledVisit> visits,
  ) async {
    final col = _isar.cacheScheduledVisits;
    await _isar.writeTxn(() async {
      // Clear everything for this facility
      final existing = await col.where().statusEqualTo(status).findAll();
      for (final row in existing) {
        await col.delete(row.id);
      }

      // Insert new list
      for (final v in visits) {
        if ((v.id ?? '').isEmpty) continue;
        await col.put(CacheScheduledVisit.fromModel(v));
      }
    });
  }

  Future<void> _appendCache(
    String status,
    List<ScheduledVisit> visits,
  ) async {
    if (visits.isEmpty) return;
    final col = _isar.cacheScheduledVisits;
    await _isar.writeTxn(() async {
      for (final v in visits) {
        if ((v.id ?? '').isEmpty) continue;
        await col.put(CacheScheduledVisit.fromModel(v));
      }
    });
  }

  Future<List<ScheduledVisit>> _readCache({
    required String status,
    required int limit,
    required int offset,
  }) async {
    final col = _isar.cacheScheduledVisits;
    final all = await col.where().statusEqualTo(status).findAll();

    // Sort in memory by scheduledDate DESC for deterministic order
    all.sort(
      (a, b) => b.scheduledDate.compareTo(a.scheduledDate),
    );

    final slice = all.skip(offset).take(limit);
    return slice.map((c) => c.toModel()).toList();
  }

  Future<int> _countCache(String status) async {
    final col = _isar.cacheScheduledVisits;
    return await col.where().statusEqualTo(status).count();
  }
}
