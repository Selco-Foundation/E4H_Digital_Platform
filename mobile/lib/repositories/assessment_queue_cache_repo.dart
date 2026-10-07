import 'dart:convert';

import 'package:isar/isar.dart';

import '../data/nosql/cache_assessment_queue.dart';
import '../model/assessment/assessment_queue.dart';
import 'cache_fallback.dart';

abstract class AssessmentQueueCache {
  Future<void> save({
    required String tenantId,
    required String assessorId,
    required String phase,
    required List<AssessmentQueueFacility> facilities,
    bool overwrite = true,
  });

  Future<AssessmentQueueResponse> search({
    required String tenantId,
    required String assessorId,
    required String phase,
    required String query,
    required String sortOrder,
    required int offset,
    required int limit,
  });
}

class IsarAssessmentQueueCache implements AssessmentQueueCache {
  final Future<Isar> Function() _open;

  IsarAssessmentQueueCache(Isar isar) : _open = (() async => isar);
  IsarAssessmentQueueCache.lazy(this._open);

  @override
  Future<void> save({
    required String tenantId,
    required String assessorId,
    required String phase,
    required List<AssessmentQueueFacility> facilities,
    bool overwrite = true,
  }) async {
    if (assessorId.isEmpty || facilities.isEmpty) return;
    final isar = await _open();
    final col = isar.cacheAssessmentQueues;
    await isar.writeTxn(() async {
      for (final facility in facilities) {
        final id = facility.planFacilityId;
        if (id == null || id.isEmpty) continue;
        final key = jsonEncode([tenantId, assessorId, phase, id]);
        if (!overwrite &&
            await col.where().cacheKeyEqualTo(key).findFirst() != null) {
          continue;
        }
        await col.put(CacheAssessmentQueue(
          cacheKey: key,
          tenantId: tenantId,
          assessorId: assessorId,
          phase: phase,
          planFacilityId: id,
          facilityName: facility.facilityName ?? '',
          facilityJson: jsonEncode(facility.toJson()),
          lastActionTime: facility.lastActionTime,
        ));
      }
    });
  }

  @override
  Future<AssessmentQueueResponse> search({
    required String tenantId,
    required String assessorId,
    required String phase,
    required String query,
    required String sortOrder,
    required int offset,
    required int limit,
  }) async {
    final isar = await _open();
    final rows = await isar.cacheAssessmentQueues
        .filter()
        .tenantIdEqualTo(tenantId)
        .assessorIdEqualTo(assessorId)
        .phaseEqualTo(phase)
        .findAll();
    final matching = rows
        .where((row) => matchesFacilityName(row.facilityName, query))
        .toList();
    matching.sort((a, b) {
      final order = (a.lastActionTime ?? 0).compareTo(b.lastActionTime ?? 0);
      if (order == 0) return a.planFacilityId.compareTo(b.planFacilityId);
      return sortOrder == 'ASC' ? order : -order;
    });
    return AssessmentQueueResponse(
      facilities: matching
          .skip(offset)
          .take(limit)
          .map((row) => AssessmentQueueFacility.fromJson(
              Map<String, dynamic>.from(jsonDecode(row.facilityJson) as Map)))
          .toList(),
      count: matching.length,
      pagination: AssessmentQueuePagination(
          offset: offset, limit: limit, total: matching.length),
    );
  }
}
