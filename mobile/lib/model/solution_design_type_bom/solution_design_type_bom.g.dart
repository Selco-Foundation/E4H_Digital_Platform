// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'solution_design_type_bom.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SolutionDesignTypeBomImpl _$$SolutionDesignTypeBomImplFromJson(
        Map<String, dynamic> json) =>
    _$SolutionDesignTypeBomImpl(
      solutionDesignTypeCode: json['solutionDesignTypeCode'] as String,
      bomForms: (json['bomForms'] as List<dynamic>)
          .map((e) => BomEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$$SolutionDesignTypeBomImplToJson(
        _$SolutionDesignTypeBomImpl instance) =>
    <String, dynamic>{
      'solutionDesignTypeCode': instance.solutionDesignTypeCode,
      'bomForms': instance.bomForms,
    };

_$BomEntryImpl _$$BomEntryImplFromJson(Map<String, dynamic> json) =>
    _$BomEntryImpl(
      name: json['name'] as String,
    );

Map<String, dynamic> _$$BomEntryImplToJson(_$BomEntryImpl instance) =>
    <String, dynamic>{
      'name': instance.name,
    };
