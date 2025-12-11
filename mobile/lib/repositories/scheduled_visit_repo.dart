import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_amc_installation_form.dart';
import '../data/nosql/cache_amc_media_upload.dart';
import '../data/nosql/cache_prefilled_scheduled_visit.dart';
import '../data/nosql/cache_scheduled_visit.dart';
import '../data/remote_client.dart';
import '../model/document/document.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';

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
      print('Criteria: ${criteria.toApiMap()}');
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
            //'statuses': ['DRAFT'],
            // "ids": ["540ed531-5f5a-413f-bd28-b99d2b2eab82"]
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
    String? otp,
    String? status,
    Map<String, dynamic>? responses,
    List<Document>? workflowDocuments,
    List<Document>? visitDocuments,
  }) async {
    const path = 'asset-amc/v1/visit/workflow/_update';

    try {
      final body = {
        'visitId': visitId,
        'workflow': {
          'action': status ?? 'SUBMIT_VISIT_REPORT',
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

      final resp = await dio.post(path, data: body);

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

  Future<void> resendVisitOtp() async {
    const path = 'asset-amc/v1/visit/_resend_otp';

    try {
      final resp = await dio.post(path, data: <String, dynamic>{});

      AppLogger.instance.info(
        'ScheduledVisitRemoteRepository.resendVisitOtp status=${resp.statusCode} ${resp.data}',
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

  ScheduledVisitRepository(this._isar)
      : _remote = ScheduledVisitRemoteRepository();

  Future<PaginatedScheduledVisits> fetchByWorkflowStatus({
    required List<String> statuses,
    int limit = defaultPageSize,
    int offset = 0,
  }) async {
    final criteria = ScheduledVisitSearchCriteria(
      tenantId: envConfig.variables.tenantId,
      statuses: statuses,
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

      if (offset == 0) {
        await _replaceCache(statuses, items);
      } else {
        await _appendCache(items);
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
        statuses: statuses,
        limit: limit,
        offset: offset,
      );
      final total = await _countCache(statuses);

      return PaginatedScheduledVisits(
        items: cachedItems,
        totalCount: total,
        fromCache: true,
      );
    }
  }

  Future<void> _replaceCache(
    List<String> statuses,
    List<ScheduledVisit> visits,
  ) async {
    final col = _isar.cacheScheduledVisits;
    await _isar.writeTxn(() async {
      // 1. Delete EVERYTHING in cache for these statuses
      for (final status in statuses) {
        final toDelete = await col.where().statusEqualTo(status).findAll();
        for (final row in toDelete) {
          await col.delete(row.id);
        }
      }

      // 2. Insert new list (even if visits is empty, the delete above already wiped)
      for (final v in visits) {
        if ((v.id ?? '').isEmpty) continue;
        await col.put(CacheScheduledVisit.fromModel(v));
      }
    });
  }

  Future<void> _appendCache(
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
    required List<String> statuses,
    required int limit,
    required int offset,
  }) async {
    final col = _isar.cacheScheduledVisits;
    final all = <CacheScheduledVisit>[];

    for (final status in statuses) {
      final matches = await col.where().statusEqualTo(status).findAll();
      all.addAll(matches);
    }

    all.sort((a, b) => b.scheduledDate.compareTo(a.scheduledDate));

    final slice = all.skip(offset).take(limit);
    return slice.map((c) => c.toModel()).toList();
  }

  Future<int> _countCache(List<String> statuses) async {
    final col = _isar.cacheScheduledVisits;
    var total = 0;
    for (final status in statuses) {
      total += await col.where().statusEqualTo(status).count();
    }
    return total;
  }

  Future<void> upsertCacheAmcInstallationForm(
    Isar isar,
    CacheAmcInstallationForm entry,
  ) async {
    await isar.writeTxn(() async {
      final existing = await isar.cacheAmcInstallationForms
          .where()
          .scheduledVisitIdEqualTo(entry.scheduledVisitId)
          .filter()
          .userTypeEqualTo(entry.userType)
          .findFirst();

      if (existing != null) {
        existing.filePath = entry.filePath;
        existing.latitude = entry.latitude;
        existing.longitude = entry.longitude;
        existing.updatedAt = DateTime.now();

        await isar.cacheAmcInstallationForms.put(existing);
      } else {
        await isar.cacheAmcInstallationForms.put(entry);
      }
    });
  }

  Future<void> deleteInstallationForm(
      {required String scheduledVisitId}) async {
    final col = _isar.cacheAmcInstallationForms;
    final row =
        await col.where().scheduledVisitIdEqualTo(scheduledVisitId).findFirst();
    if (row != null) {
      await _isar.writeTxn(() async {
        await col.delete(row.id);
      });
    }
  }

  Future<CacheAmcInstallationForm?> getCacheAmcInstallationForm({
    required String scheduledVisitId,
    required String userType,
  }) async {
    return _isar.cacheAmcInstallationForms
        .where()
        .scheduledVisitIdEqualTo(scheduledVisitId)
        .filter()
        .userTypeEqualTo(userType)
        .findFirst();
  }

  Future<void> deleteAmcMediaUploads(
      {required Isar isar, required String scheduledVisitId}) async {
    await isar.writeTxn(() async {
      final col = isar.cacheAmcMediaUploads;
      final rec =
          await col.where().scheduledVisitIdEqualTo(scheduledVisitId).findAll();
      for (final r in rec) {
        await col.delete(r.id);
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
      final row = await col.where().scheduledVisitIdEqualTo(id).findFirst();
      if (row != null) {
        result.add(row.toModel());
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

    // Start from what remote returned (assume already sorted as you want)
    var result = remoteVisits;

    // 1️⃣ For SCHEDULED: exclude visits that are already prefilled
    if (hasScheduled && prefilledVisitIds.isNotEmpty) {
      result = result.where((v) {
        final id = v.id ?? '';
        if (id.isEmpty) return false;

        final isPrefilled = prefilledVisitIds.contains(id);

        // Drop SCHEDULED + prefilled
        if (v.status == WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name &&
            isPrefilled) {
          return false;
        }
        return true;
      }).toList();
    }

    // 2️⃣ For PENDING_OTP_APPROVAL:
    //     prepend cached visits whose id is prefilled and not already in result
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

      // 👇 put new cached ones on top, keep remote order intact
      result = [...newFromCache, ...result];
    }

    return result;
  }
}
