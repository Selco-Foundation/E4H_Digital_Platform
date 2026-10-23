import 'package:synchronized/synchronized.dart';

import '../data/secure_storage/secureStore.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_queue.dart';

class AssessmentBookmarkRepository {
  static final _lock = Lock();
  final SecureStore storage;
  final String tenantId;
  final String assessorId;
  final AssessmentPhase phase;

  AssessmentBookmarkRepository({
    SecureStore? storage,
    required this.tenantId,
    required this.assessorId,
    required this.phase,
  }) : storage = storage ?? SecureStore();

  Future<Map<String, dynamic>> _read() async {
    if (assessorId.trim().isEmpty) return {};
    return storage.getAssessmentBookmarks(tenantId, assessorId, phase.name);
  }

  Future<List<AssessmentQueueFacility>> list(
      {String query = '', String sortOrder = 'DESC'}) async {
    final values = (await _read())
        .values
        .map((value) => Map<String, dynamic>.from(value as Map))
        .toList();
    values.sort((a, b) {
      final order =
          (a['bookmarkedAt'] as int).compareTo(b['bookmarkedAt'] as int);
      return sortOrder == 'ASC' ? order : -order;
    });
    final search = query.trim().toLowerCase();
    return values
        .where((entry) => entry['facility'] is Map)
        .map((entry) => AssessmentQueueFacility.fromJson(
            Map<String, dynamic>.from(entry['facility'] as Map)))
        .where((facility) =>
            (facility.facilityName ?? '').toLowerCase().contains(search))
        .toList();
  }

  Future<Set<String>> ids() async {
    final entries = (await _read()).entries.toList();
    entries.sort((a, b) => ((b.value as Map)['bookmarkedAt'] as int)
        .compareTo((a.value as Map)['bookmarkedAt'] as int));
    return entries.map((entry) => entry.key).toSet();
  }

  Future<int> count() async => (await _read()).length;

  Future<void> save(AssessmentQueueFacility facility) =>
      _lock.synchronized(() async {
        final id = facility.planFacilityId?.trim();
        if (id == null || id.isEmpty || assessorId.trim().isEmpty) {
          throw const FormatException('Missing assessment bookmark identity');
        }
        final entries = await _read();
        final existing = entries[id] as Map?;
        entries[id] = {
          'facility': facility.toJson(),
          'bookmarkedAt': existing?['bookmarkedAt'] ??
              DateTime.now().microsecondsSinceEpoch,
        };
        await storage.setAssessmentBookmarks(
            tenantId, assessorId, phase.name, entries);
      });

  Future<void> invalidate(String id) => _lock.synchronized(() async {
        final entries = await _read();
        final entry = entries[id.trim()] as Map?;
        if (entry == null) return;
        entries[id.trim()] = {...entry, 'facility': null};
        await storage.setAssessmentBookmarks(
            tenantId, assessorId, phase.name, entries);
      });

  Future<void> refreshSnapshots(List<AssessmentQueueFacility> facilities) =>
      _lock.synchronized(() async {
        final entries = await _read();
        var changed = false;
        for (final facility in facilities) {
          final id = facility.planFacilityId?.trim();
          final entry = entries[id] as Map?;
          if (entry == null) continue;
          entries[id!] = {...entry, 'facility': facility.toJson()};
          changed = true;
        }
        if (changed) {
          await storage.setAssessmentBookmarks(
              tenantId, assessorId, phase.name, entries);
        }
      });

  Future<void> remove(String planFacilityId) => _lock.synchronized(() async {
        final entries = await _read();
        if (entries.remove(planFacilityId.trim()) == null) return;
        await storage.setAssessmentBookmarks(
            tenantId, assessorId, phase.name, entries);
      });
}
