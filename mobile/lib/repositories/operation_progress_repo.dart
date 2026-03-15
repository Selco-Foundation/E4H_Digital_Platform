import 'dart:convert';

import 'package:collection/collection.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_operation_checkpoint.dart';
import '../data/nosql/cache_submission_job.dart';
import '../utils/operation_progress.dart';

class OperationProgressRepository {
  final Isar _isar;

  OperationProgressRepository(this._isar);

  String checkpointEntryKey({
    required String activityFacilityId,
    required String operationType,
    required String checkpointKey,
    required String itemKey,
  }) {
    return '$activityFacilityId::$operationType::$checkpointKey::$itemKey';
  }

  Stream<CacheSubmissionJob?> watchJob(String activityFacilityId) {
    return _isar.cacheSubmissionJobs
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .watch(fireImmediately: true)
        .map((rows) => rows.firstOrNull);
  }

  Future<CacheSubmissionJob?> getJob(String activityFacilityId) {
    return _isar.cacheSubmissionJobs
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst();
  }

  Future<void> upsertJob({
    required String activityFacilityId,
    required String operationType,
    required String status,
    required String stageKey,
    required int completedSteps,
    required int totalSteps,
    bool isBlocking = true,
    String? errorMessage,
    bool incrementRetry = false,
  }) async {
    final label = stageForKey(operationType, stageKey).label;

    await _isar.writeTxn(() async {
      final existing = await _isar.cacheSubmissionJobs
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findFirst();

      final job = existing ??
          CacheSubmissionJob(
            activityFacilityId: activityFacilityId,
            operationType: operationType,
            status: status,
            stageKey: stageKey,
            stageLabel: label,
            completedSteps: completedSteps,
            totalSteps: totalSteps,
            progressPercent: progressPercent(
              completedSteps: completedSteps,
              totalSteps: totalSteps,
            ),
            isBlocking: isBlocking,
          );

      job
        ..operationType = operationType
        ..status = status
        ..stageKey = stageKey
        ..stageLabel = label
        ..completedSteps = completedSteps
        ..totalSteps = totalSteps
        ..progressPercent = progressPercent(
          completedSteps: completedSteps,
          totalSteps: totalSteps,
        )
        ..isBlocking = isBlocking
        ..lastError = errorMessage
        ..updatedAt = DateTime.now();

      if (incrementRetry) {
        job.retryCount += 1;
      }

      await _isar.cacheSubmissionJobs.put(job);
    });
  }

  Future<void> saveCheckpoint({
    required String activityFacilityId,
    required String operationType,
    required String checkpointKey,
    required String itemKey,
    required String status,
    String? remoteId,
    String? error,
    Map<String, dynamic>? payload,
  }) async {
    await _isar.writeTxn(() async {
      await _isar.cacheOperationCheckpoints.put(
        CacheOperationCheckpoint(
          entryKey: checkpointEntryKey(
            activityFacilityId: activityFacilityId,
            operationType: operationType,
            checkpointKey: checkpointKey,
            itemKey: itemKey,
          ),
          activityFacilityId: activityFacilityId,
          operationType: operationType,
          checkpointKey: checkpointKey,
          itemKey: itemKey,
          status: status,
          remoteId: remoteId,
          error: error,
          payloadJson: payload == null ? null : jsonEncode(payload),
        )..updatedAt = DateTime.now(),
      );
    });
  }

  Future<CacheOperationCheckpoint?> getCheckpoint({
    required String activityFacilityId,
    required String operationType,
    required String checkpointKey,
    required String itemKey,
  }) {
    return _isar.cacheOperationCheckpoints
        .filter()
        .activityFacilityIdEqualTo(activityFacilityId)
        .operationTypeEqualTo(operationType)
        .checkpointKeyEqualTo(checkpointKey)
        .itemKeyEqualTo(itemKey)
        .findFirst();
  }

  Future<List<CacheOperationCheckpoint>> getCheckpoints({
    required String activityFacilityId,
    required String operationType,
    String? checkpointKey,
  }) {
    final query = _isar.cacheOperationCheckpoints
        .filter()
        .activityFacilityIdEqualTo(activityFacilityId)
        .operationTypeEqualTo(operationType);

    if (checkpointKey != null) {
      return query.checkpointKeyEqualTo(checkpointKey).findAll();
    }

    return query.findAll();
  }

  Future<void> clearOperationCheckpoints({
    required String activityFacilityId,
    required String operationType,
  }) async {
    await _isar.writeTxn(() async {
      final rows = await _isar.cacheOperationCheckpoints
          .filter()
          .activityFacilityIdEqualTo(activityFacilityId)
          .operationTypeEqualTo(operationType)
          .findAll();
      for (final row in rows) {
        await _isar.cacheOperationCheckpoints.delete(row.id);
      }
    });
  }
}
