// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'solution_design_type.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SolutionDesignTypeImpl _$$SolutionDesignTypeImplFromJson(
        Map<String, dynamic> json) =>
    _$SolutionDesignTypeImpl(
      active: json['active'] as bool,
      code: json['code'] as String,
      name: json['name'] as String,
      url: json['url'] as String,
      systemCode: json['system_code'] as String,
    );

Map<String, dynamic> _$$SolutionDesignTypeImplToJson(
        _$SolutionDesignTypeImpl instance) =>
    <String, dynamic>{
      'active': instance.active,
      'code': instance.code,
      'name': instance.name,
      'url': instance.url,
      'system_code': instance.systemCode,
    };
