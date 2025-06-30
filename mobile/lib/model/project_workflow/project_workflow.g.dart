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
    );

Map<String, dynamic> _$$ProjectWorkflowImplToJson(
        _$ProjectWorkflowImpl instance) =>
    <String, dynamic>{
      'project': const ProjectModelConverter().toJson(instance.project),
      'status': instance.status,
    };
