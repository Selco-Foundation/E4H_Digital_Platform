import 'dart:convert';

import 'package:isar/isar.dart';

import '../data/nosql/cache_assessment_draft.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_queue.dart';

class AssessmentDraftStatus {
  static const pending = 'PENDING';
  static const blocked = 'BLOCKED';
}

class AssessmentDraftRepository {
  final Isar isar;

  const AssessmentDraftRepository(this.isar);

  static String key(
    String tenantId,
    String planFacilityId,
    AssessmentPhase phase,
  ) =>
      '$tenantId::$planFacilityId::${phase.name}';

  Future<CacheAssessmentDraft> save({
    required AssessmentSubmissionRequest request,
    required AssessmentQueueFacility facility,
    required String assessorId,
    required bool blocked,
    required String error,
    Map<String, dynamic>? facilityDefaults,
  }) async {
    final draftKey = key(
      request.tenantId,
      request.planFacilityId,
      request.assessmentPhase,
    );
    late CacheAssessmentDraft saved;
    await isar.writeTxn(() async {
      final existing = await isar.cacheAssessmentDrafts
          .where()
          .draftKeyEqualTo(draftKey)
          .findFirst();
      final draft = existing ??
          CacheAssessmentDraft(
            draftKey: draftKey,
            tenantId: request.tenantId,
            assessorId: assessorId,
            phase: request.assessmentPhase.name,
            status: blocked
                ? AssessmentDraftStatus.blocked
                : AssessmentDraftStatus.pending,
            planFacilityId: request.planFacilityId,
            facilityName: facility.facilityName ?? '---',
            facilityType: facility.facilityType ?? '---',
            state: facility.state,
            district: facility.district,
            block: facility.block,
            facilityDefaultsJson:
                facilityDefaults == null ? null : jsonEncode(facilityDefaults),
            requestJson: jsonEncode(request.toJson()),
          );
      draft
        ..assessorId = assessorId
        ..phase = request.assessmentPhase.name
        ..status = blocked
            ? AssessmentDraftStatus.blocked
            : AssessmentDraftStatus.pending
        ..facilityName = facility.facilityName ?? '---'
        ..facilityType = facility.facilityType ?? '---'
        ..state = facility.state
        ..district = facility.district
        ..block = facility.block
        ..requestJson = jsonEncode(request.toJson())
        ..lastError = error
        ..attemptCount += 1
        ..updatedAt = DateTime.now();
      if (facilityDefaults != null) {
        draft.facilityDefaultsJson = jsonEncode(facilityDefaults);
      }
      await isar.cacheAssessmentDrafts.put(draft);
      saved = draft;
    });
    return saved;
  }

  Future<List<CacheAssessmentDraft>> listDrafts(String assessorId) {
    return isar.cacheAssessmentDrafts
        .filter()
        .assessorIdEqualTo(assessorId, caseSensitive: false)
        .sortByUpdatedAtDesc()
        .findAll();
  }

  Future<Set<String>> draftedPlanFacilityIds({
    required String assessorId,
    required AssessmentPhase phase,
  }) async {
    final normalizedAssessorId = assessorId.trim();
    if (normalizedAssessorId.isEmpty) return <String>{};

    final drafts = await isar.cacheAssessmentDrafts
        .filter()
        .assessorIdEqualTo(normalizedAssessorId, caseSensitive: false)
        .phaseEqualTo(phase.name, caseSensitive: false)
        .findAll();
    return drafts
        .map((draft) => draft.planFacilityId.trim())
        .where((id) => id.isNotEmpty)
        .toSet();
  }

  Future<int> countDrafts({
    required String assessorId,
    required Set<AssessmentPhase> phases,
  }) async {
    final normalizedAssessorId = assessorId.trim();
    if (normalizedAssessorId.isEmpty || phases.isEmpty) return 0;

    final allowedPhases = phases.map((phase) => phase.name).toSet();
    final drafts = await listDrafts(normalizedAssessorId);
    return drafts
        .where(
          (draft) => allowedPhases.contains(draft.phase.trim().toUpperCase()),
        )
        .length;
  }

  Future<void> delete(
    String tenantId,
    String planFacilityId,
    AssessmentPhase phase,
  ) async {
    final draft = await isar.cacheAssessmentDrafts
        .where()
        .draftKeyEqualTo(key(tenantId, planFacilityId, phase))
        .findFirst();
    if (draft == null) return;
    await isar.writeTxn(() => isar.cacheAssessmentDrafts.delete(draft.id));
  }

  Future<void> markBlocked(CacheAssessmentDraft draft, String error) async {
    await isar.writeTxn(() async {
      draft
        ..status = AssessmentDraftStatus.blocked
        ..lastError = error
        ..attemptCount += 1
        ..updatedAt = DateTime.now();
      await isar.cacheAssessmentDrafts.put(draft);
    });
  }

  Future<void> markPendingFailure(
    CacheAssessmentDraft draft,
    String error,
  ) async {
    await isar.writeTxn(() async {
      draft
        ..lastError = error
        ..attemptCount += 1
        ..updatedAt = DateTime.now();
      await isar.cacheAssessmentDrafts.put(draft);
    });
  }

  AssessmentSubmissionRequest requestOf(CacheAssessmentDraft draft) =>
      AssessmentSubmissionRequest.fromJson(
        Map<String, dynamic>.from(jsonDecode(draft.requestJson) as Map),
      );

  Map<String, dynamic>? facilityDefaultsOf(CacheAssessmentDraft draft) {
    final encoded = draft.facilityDefaultsJson;
    if (encoded == null || encoded.trim().isEmpty) return null;
    try {
      final decoded = jsonDecode(encoded);
      return decoded is Map ? Map<String, dynamic>.from(decoded) : null;
    } catch (_) {
      return null;
    }
  }
}
