import 'dart:convert';

import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/workflow/workflow.dart';
import '../activity_facility/activity_facility.dart';
import '../entities/address.dart';
import '../transaction/transaction.dart';

part 'activity_facility_workflow.freezed.dart';
part 'activity_facility_workflow.g.dart';

@freezed
class ActivityFacilityWorkflow with _$ActivityFacilityWorkflow {
  const factory ActivityFacilityWorkflow({
    @ActivityFacilityConverter() required ActivityFacility activityFacility,
    String? status,
    List<Transaction>? transactions,
    @WorkflowFlexConverter() Workflow? workflow,
  }) = _ActivityFacilityWorkflow;

  factory ActivityFacilityWorkflow.fromJson(Map<String, dynamic> json) =>
      _$ActivityFacilityWorkflowFromJson(json);
}

/// Converts between a loosely-typed API map and our strongly-typed ActivityFacility.
class ActivityFacilityConverter
    implements JsonConverter<ActivityFacility, Map<String, dynamic>> {
  const ActivityFacilityConverter();

  Map<String, dynamic> _asMap(Map<String, dynamic> v) =>
      jsonDecode(jsonEncode(v)) as Map<String, dynamic>;

  @override
  ActivityFacility fromJson(Map<String, dynamic> json) {
    // Normalize nested maps
    final m = _asMap(json);

    // API now sends Facility INSIDE the payload root (not in additionalDetails)
    // Example shape (see uploaded response):
    // { id, tenantId, activityId, status, scheduledAt, ..., facility: {...}, ... }

    final model = ActivityFacility(
      id: (m['id'] ?? '').toString(),
      tenantId: m['tenantId']?.toString(),
      activityId: m['activityId']?.toString(),
      fieldPlanId: m['fieldPlanId']?.toString(),
      facilityId: m['facilityId']?.toString(), // present on wrapper sometimes
      status: m['status']?.toString(),
      scheduledAt: (m['scheduledAt'] is int)
          ? DateTime.fromMillisecondsSinceEpoch(m['scheduledAt'] as int)
          : null,
      activatedAt: (m['activatedAt'] is int)
          ? DateTime.fromMillisecondsSinceEpoch(m['activatedAt'] as int)
          : null,
      completedAt: (m['completedAt'] is int)
          ? DateTime.fromMillisecondsSinceEpoch(m['completedAt'] as int)
          : null,
      // audit, assigned fields (optional)
      assignedUser: m['assignedUser']?.toString(),
      assignedEmployeeUser: m['assignedEmployeeUser']?.toString(),
    );

    // ---- Facility (required for UI) ----
    if (m['facility'] is Map) {
      try {
        final f = Map<String, dynamic>.from(m['facility'] as Map);
        model.facility = Facility.fromMap(f);
      } catch (_) {
        // ignore bad shapes
      }
    }

    // ---- Address (legacy parity: if present at root) ----
    if (m['address'] is Map) {
      try {
        model.address = AddressModelMapper.fromMap(
          Map<String, dynamic>.from(m['address'] as Map),
        );
      } catch (_) {}
    }

    return model;
  }

  @override
  Map<String, dynamic> toJson(ActivityFacility model) {
    // round-trip if needed back to API
    return model.toMap();
  }
}

/// Accepts {} | [] | null and returns the first workflow element or null
class WorkflowFlexConverter implements JsonConverter<Workflow?, Object?> {
  const WorkflowFlexConverter();

  @override
  Workflow? fromJson(Object? json) {
    if (json == null) return null;

    if (json is Map) {
      if (json.isEmpty) return null;
      return Workflow.fromJson(Map<String, dynamic>.from(json));
    }

    if (json is List && json.isNotEmpty) {
      final first = json.first;
      if (first is Map<String, dynamic>) return Workflow.fromJson(first);
      if (first is Map)
        return Workflow.fromJson(Map<String, dynamic>.from(first));
    }

    return null;
  }

  @override
  Object? toJson(Workflow? value) => value?.toJson();
}
