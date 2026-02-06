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

class ActivityFacilityConverter
    implements JsonConverter<ActivityFacility, Map<String, dynamic>> {
  const ActivityFacilityConverter();

  Map<String, dynamic> _asMap(Map<String, dynamic> v) =>
      jsonDecode(jsonEncode(v)) as Map<String, dynamic>;

  @override
  ActivityFacility fromJson(Map<String, dynamic> json) {
    final m = _asMap(json);

    final model = ActivityFacility(
      id: (m['id'] ?? '').toString(),
      tenantId: m['tenantId']?.toString(),
      activityId: m['activityId']?.toString(),
      fieldPlanId: m['fieldPlanId']?.toString(),
      facilityId: m['facilityId']?.toString(),
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
      assignedUser: m['assignedUser']?.toString(),
      assignedEmployeeUser: m['assignedEmployeeUser']?.toString(),
    );

    if (m['facility'] is Map) {
      try {
        final f = Map<String, dynamic>.from(m['facility'] as Map);
        model.facility = Facility.fromMap(f);
      } catch (_) {
        // ignore bad shapes
      }
    }

    if (m['address'] is Map) {
      try {
        model.address = AddressModelMapper.fromMap(
          Map<String, dynamic>.from(m['address'] as Map),
        );
      } catch (_) {}
    }
    if (m['additionalDetails'] is Map) {
      try {
        model.additionalDetails = AdditionalDetails.fromMap(
          Map<String, dynamic>.from(m['additionalDetails'] as Map),
        );
      } catch (_) {}
    }
    // ✅ FieldPlan + Project (for AMC PDF / submission context)

    if (m['fieldPlan'] is Map) {
      try {
        model.fieldPlan = FieldPlan.fromMap(
          Map<String, dynamic>.from(m['fieldPlan'] as Map),
        );
      } catch (_) {}
    }

    return model;
  }

  @override
  Map<String, dynamic> toJson(ActivityFacility model) {
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
