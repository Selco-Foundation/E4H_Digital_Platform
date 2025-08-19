// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'project_workflow.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ProjectWorkflowImpl _$$ProjectWorkflowImplFromJson(
        Map<String, dynamic> json) =>
    _$ProjectWorkflowImpl(
      project: const ProjectModelConverter()
          .fromJson(json['project'] as Map<String, dynamic>),
      status: json['status'] as String?,
      transactions: (json['transactions'] as List<dynamic>?)
          ?.map((e) => Transaction.fromJson(e as Map<String, dynamic>))
          .toList(),
      workflow: const WorkflowFlexConverter().fromJson(json['workflow']),
    );

Map<String, dynamic> _$$ProjectWorkflowImplToJson(
        _$ProjectWorkflowImpl instance) =>
    <String, dynamic>{
      'project': const ProjectModelConverter().toJson(instance.project),
      'status': instance.status,
      'transactions': instance.transactions,
      'workflow': const WorkflowFlexConverter().toJson(instance.workflow),
    };
