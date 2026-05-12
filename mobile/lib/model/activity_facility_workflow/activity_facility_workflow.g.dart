// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'activity_facility_workflow.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ActivityFacilityWorkflowImpl _$$ActivityFacilityWorkflowImplFromJson(
        Map<String, dynamic> json) =>
    _$ActivityFacilityWorkflowImpl(
      activityFacility: const ActivityFacilityConverter()
          .fromJson(json['activityFacility'] as Map<String, dynamic>),
      status: json['status'] as String?,
      transactions: (json['transactions'] as List<dynamic>?)
          ?.map((e) => Transaction.fromJson(e as Map<String, dynamic>))
          .toList(),
      workflow: const WorkflowFlexConverter().fromJson(json['workflow']),
    );

Map<String, dynamic> _$$ActivityFacilityWorkflowImplToJson(
        _$ActivityFacilityWorkflowImpl instance) =>
    <String, dynamic>{
      'activityFacility':
          const ActivityFacilityConverter().toJson(instance.activityFacility),
      'status': instance.status,
      'transactions': instance.transactions,
      'workflow': const WorkflowFlexConverter().toJson(instance.workflow),
    };
