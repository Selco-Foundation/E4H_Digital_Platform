import 'package:dio/dio.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_amc_failed_scheduled_visit.dart';
import '../data/nosql/cache_amc_media_upload.dart';
import '../data/nosql/cache_prefilled_scheduled_visit.dart';
import '../data/nosql/cache_scheduled_visit.dart';
import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/document/document.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';

import 'report_bookmark_repo.dart';
import 'cache_fallback.dart';

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

class ScheduledVisitRemoteRepository {
  final Dio dio;
  final SecureStore bookmarkStorage;
  final String? bookmarkTenantId;
  ScheduledVisitRemoteRepository(
      {Dio? dio, SecureStore? bookmarkStorage, this.bookmarkTenantId})
      : dio = dio ?? DioClient().dio,
        bookmarkStorage = bookmarkStorage ?? SecureStore();

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
            ...criteria.toApiMap(),
          },
        },
      );

      return ScheduledVisitSearchResponse.fromJson(
        Map<String, dynamic>.from(response.data as Map),
      );
    } on DioError catch (err) {
      rethrow;
    }
  }

  Future<void> updateVisitWorkflow({
    required String visitId,
    required String schemaCode,
    required int version,
    String? otp,
    String? status,
    Map<String, dynamic>? responses,
    List<Document>? visitDocuments,
  }) async {
    const path = 'asset-amc/v1/visit/workflow/_update';

    try {
      final body = {
        'visitId': visitId,
        'workflow': {
          'action': status ?? 'SUBMIT_VISIT_REPORT',
          'comment': 'Submit Visit Report Action',
          'additionalDetails': <String, dynamic>{},
        },
        'visitReport': {
          'schemaCode': schemaCode,
          'version': version,
          'otpReference': otp,
          if (responses != null) ...{
            'responses': responses,
          },
          if (visitDocuments != null) ...{
            'documents':
                visitDocuments.map((d) => d.toJsonForWorkflow()).toList(),
          },
          'additionalDetails': <String, dynamic>{},
        },
      };

      await dio.post(path, data: body);
      await removeSubmittedReportBookmark(
          id: visitId,
          action: status ?? 'SUBMIT_VISIT_REPORT',
          storage: bookmarkStorage,
          tenantId: bookmarkTenantId);
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

  Future<void> resendVisitOtp({
    required String visitId,
  }) async {
    const path = 'asset-amc/v1/visit/_resend_otp';

    try {
      await dio.post(
        path,
        data: <String, dynamic>{
          'visitId': visitId,
        },
      );
    } on DioError catch (e) {
      AppLogger.instance.info(
        'ScheduledVisitRemoteRepository.resendVisitOtp DioError=$e',
      );
      rethrow;
    } catch (e) {
      AppLogger.instance.info(
        'ScheduledVisitRemoteRepository.resendVisitOtp error=$e',
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

  ScheduledVisitRepository(this._isar, {ScheduledVisitRemoteRepository? remote})
      : _remote = remote ?? ScheduledVisitRemoteRepository();

  Future<PaginatedScheduledVisits> fetchByWorkflowStatus({
    required List<String> statuses,
    String? facilityName,
    String? sortDirection,
    int limit = defaultPageSize,
    int offset = 0,
  }) async {
    final accessInfo = await SecureStore().getAccessInfo();
    final assignedUserUuid = accessInfo?.userRequest?.uuid;
    final criteria = ScheduledVisitSearchCriteria(
      tenantId: envConfig.variables.tenantId,
      facilityName: facilityName,
      assignedUsers: assignedUserUuid == null ? null : [assignedUserUuid],
      statuses: statuses,
      sortDirection: sortDirection,
    );

    try {
      final remoteResp = await _remote.search(
        criteria: criteria,
        limit: limit,
        offset: offset,
      );

      var items = remoteResp.scheduledVisits;

      final prefilledRepo = PrefilledScheduledVisitRepository(_isar);
      final prefilledIds = await prefilledRepo.getPrefilledVisitIds();
      List<ScheduledVisit> cachedPrefilled = <ScheduledVisit>[];
      if (statuses.contains(
              WORKFLOW_STATUS_AMC_FIELD_STAFF.PENDING_OTP_APPROVAL.name) &&
          prefilledIds.isNotEmpty) {
        cachedPrefilled =
            await prefilledRepo.getPrefilledVisitsFromCache(prefilledIds);
      }

      items = prefilledRepo.filterByPrefilledRules(
        remoteVisits: items,
        statuses: statuses,
        prefilledVisitIds: prefilledIds,
        cachedPrefilledVisits: cachedPrefilled,
      );

      await _upsertCache(items);

      return PaginatedScheduledVisits(
        items: items,
        totalCount: remoteResp.totalCount,
        fromCache: false,
      );
    } catch (e) {
      if (isAuthenticationFailure(e)) rethrow;
      AppLogger.instance.info(
        'Failed to fetch scheduled visits remotely, falling back to cache: $e',
      );

      final prefilledRepo = PrefilledScheduledVisitRepository(_isar);
      final prefilledIds = await prefilledRepo.getPrefilledVisitIds();
      final cached = await _readCache(statuses: statuses);
      final eligible = prefilledRepo
          .filterByPrefilledRules(
            remoteVisits: cached,
            statuses: statuses,
            prefilledVisitIds: prefilledIds,
            cachedPrefilledVisits:
                await prefilledRepo.getPrefilledVisitsFromCache(prefilledIds),
          )
          .where((visit) =>
              matchesFacilityName(visit.facility?.facilityName, facilityName))
          .toList();
      eligible.sort((a, b) {
        final order = (a.scheduledDate ?? DateTime(1970))
            .compareTo(b.scheduledDate ?? DateTime(1970));
        return sortDirection == 'ASC' ? order : -order;
      });
      return PaginatedScheduledVisits(
        items: eligible.skip(offset).take(limit).toList(),
        totalCount: eligible.length,
        fromCache: true,
      );
    }
  }

  Future<void> _upsertCache(
    List<ScheduledVisit> visits,
  ) async {
    if (visits.isEmpty) return;
    final col = _isar.cacheScheduledVisits;
    await _isar.writeTxn(() async {
      for (final v in visits) {
        if ((v.id ?? '').isEmpty) continue;
        final existing =
            await col.where().scheduledVisitIdEqualTo(v.id!).findAll();
        for (final row in existing) {
          await col.delete(row.id);
        }
        await col.put(CacheScheduledVisit.fromModel(v));
      }
    });
  }

  Future<List<ScheduledVisit>> _readCache({
    required List<String> statuses,
  }) async {
    final all = await _isar.cacheScheduledVisits.where().findAll();
    final byId = <String, CacheScheduledVisit>{};
    for (final row in all) {
      final previous = byId[row.scheduledVisitId];
      if (previous == null || row.id > previous.id) {
        byId[row.scheduledVisitId] = row;
      }
    }
    return byId.values
        .where((row) => statuses.contains(row.status))
        .map((row) => row.toModel())
        .toList();
  }

  Future<void> deleteAmcMediaUploads({required String scheduledVisitId}) async {
    await _isar.writeTxn(() async {
      final col = _isar.cacheAmcMediaUploads;
      final rec =
          await col.where().scheduledVisitIdEqualTo(scheduledVisitId).findAll();
      for (final r in rec) {
        await col.delete(r.id);
      }
    });
  }

  Future<void> addFailedScheduledVisitToCache({
    required String scheduledVisitId,
  }) async {
    if (scheduledVisitId.trim().isEmpty) return;

    final col = _isar.cacheAmcFailedScheduledVisits;
    await _isar.writeTxn(() async {
      final existing = await col
          .where()
          .scheduledVisitIdEqualTo(scheduledVisitId)
          .findFirst();

      if (existing != null) return;
      await col.put(
        CacheAmcFailedScheduledVisit(scheduledVisitId: scheduledVisitId),
      );
    });
  }

  Future<bool> isFailedScheduledVisitInCache({
    required String scheduledVisitId,
  }) async {
    if (scheduledVisitId.trim().isEmpty) return false;

    final row = await _isar.cacheAmcFailedScheduledVisits
        .where()
        .scheduledVisitIdEqualTo(scheduledVisitId)
        .findFirst();
    return row != null;
  }

  Future<void> removeFailedScheduledVisitFromCache({
    required String scheduledVisitId,
  }) async {
    if (scheduledVisitId.trim().isEmpty) return;

    final col = _isar.cacheAmcFailedScheduledVisits;
    await _isar.writeTxn(() async {
      final row = await col
          .where()
          .scheduledVisitIdEqualTo(scheduledVisitId)
          .findFirst();
      if (row != null) {
        await col.delete(row.id);
      }
    });
  }
}

class PrefilledScheduledVisitRepository {
  final Isar _isar;
  PrefilledScheduledVisitRepository(this._isar);

  Future<CachePrefilledScheduledVisit> addOrTouch({
    required String scheduledVisitId,
    required String userType,
  }) async {
    final col = _isar.cachePrefilledScheduledVisits;
    final existing = await col
        .where()
        .scheduledVisitIdUserTypeEqualTo(scheduledVisitId, userType)
        .findFirst();

    final now = DateTime.now();
    return _isar.writeTxn(() async {
      if (existing != null) {
        existing.updatedAt = now;
        await col.put(existing);
        return existing;
      } else {
        final row = CachePrefilledScheduledVisit(
            scheduledVisitId: scheduledVisitId, userType: userType)
          ..createdAt = now
          ..updatedAt = now;
        await col.put(row);
        return row;
      }
    });
  }

  Future<bool> exists({
    required String scheduledVisitId,
    required String userType,
  }) async {
    final col = _isar.cachePrefilledScheduledVisits;
    final row = await col
        .where()
        .scheduledVisitIdUserTypeEqualTo(scheduledVisitId, userType)
        .findFirst();
    return row != null;
  }

  Future<void> delete({
    required String scheduledVisitId,
    required String userType,
  }) async {
    final col = _isar.cachePrefilledScheduledVisits;
    final row = await col
        .where()
        .scheduledVisitIdUserTypeEqualTo(scheduledVisitId, userType)
        .findFirst();
    if (row != null) {
      await _isar.writeTxn(() async {
        await col.delete(row.id);
      });
    }
  }

  Future<Set<String>> getPrefilledVisitIds() async {
    final col = _isar.cachePrefilledScheduledVisits;
    final all = await col.where().findAll();

    final ids = <String>{};
    for (final row in all) {
      if ((row.scheduledVisitId ?? '').isEmpty) continue;
      ids.add(row.scheduledVisitId);
    }
    return ids;
  }

  Future<List<ScheduledVisit>> getPrefilledVisitsFromCache(
    Set<String> prefilledIds,
  ) async {
    if (prefilledIds.isEmpty) return <ScheduledVisit>[];

    final col = _isar.cacheScheduledVisits;
    final result = <ScheduledVisit>[];

    for (final id in prefilledIds) {
      final rows = await col.where().scheduledVisitIdEqualTo(id).findAll();
      if (rows.isNotEmpty) {
        rows.sort((a, b) => a.id.compareTo(b.id));
        result.add(rows.last.toModel());
      }
    }
    return result;
  }

  List<ScheduledVisit> filterByPrefilledRules({
    required List<ScheduledVisit> remoteVisits,
    required List<String> statuses,
    required Set<String> prefilledVisitIds,
    required List<ScheduledVisit> cachedPrefilledVisits,
  }) {
    final hasScheduled =
        statuses.contains(WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name);
    final hasPendingOtp = statuses
        .contains(WORKFLOW_STATUS_AMC_FIELD_STAFF.PENDING_OTP_APPROVAL.name);

    var result = remoteVisits;

    if (hasScheduled && prefilledVisitIds.isNotEmpty) {
      result = result.where((v) {
        final id = v.id ?? '';
        if (id.isEmpty) return false;

        final isPrefilled = prefilledVisitIds.contains(id);
        if (v.status == WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name &&
            isPrefilled) {
          return false;
        }
        return true;
      }).toList();
    }

    if (hasPendingOtp && cachedPrefilledVisits.isNotEmpty) {
      final existingIds = <String>{};
      for (final v in result) {
        final id = v.id;
        if (id != null) {
          existingIds.add(id);
        }
      }

      final newFromCache = <ScheduledVisit>[];
      for (final v in cachedPrefilledVisits) {
        final id = v.id;
        if (id == null) continue;
        if (!prefilledVisitIds.contains(id)) continue;
        if (existingIds.contains(id)) continue;
        newFromCache.add(v);
      }

      // optional: order the new ones among themselves by scheduledDate desc
      newFromCache.sort((a, b) {
        final ad = a.scheduledDate ?? DateTime.fromMillisecondsSinceEpoch(0);
        final bd = b.scheduledDate ?? DateTime.fromMillisecondsSinceEpoch(0);
        return bd.compareTo(ad);
      });

      // put new cached ones on top, keep remote order intact
      result = [...newFromCache, ...result];
    }

    return result;
  }
}
