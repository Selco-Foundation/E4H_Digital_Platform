// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SystemImpl _$$SystemImplFromJson(Map<String, dynamic> json) => _$SystemImpl(
      code: json['code'] as String,
      name: json['name'] as String,
      active: json['active'] as bool,
    );

Map<String, dynamic> _$$SystemImplToJson(_$SystemImpl instance) =>
    <String, dynamic>{
      'code': instance.code,
      'name': instance.name,
      'active': instance.active,
    };

_$SystemDataImpl _$$SystemDataImplFromJson(Map<String, dynamic> json) =>
    _$SystemDataImpl(
      id: (json['id'] as num).toInt(),
      system: (json['System'] as List<dynamic>)
          .map((e) => System.fromJson(e as Map<String, dynamic>))
          .toList(),
      module: json['module'] as String,
      tenantId: json['tenantId'] as String,
    );

Map<String, dynamic> _$$SystemDataImplToJson(_$SystemDataImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'System': instance.system,
      'module': instance.module,
      'tenantId': instance.tenantId,
    };
