// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'asset_count.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AssetCountImpl _$$AssetCountImplFromJson(Map<String, dynamic> json) =>
    _$AssetCountImpl(
      max: (json['max'] as num).toInt(),
      min: (json['min'] as num).toInt(),
      active: json['active'] as bool,
      assetTypeCode: json['asset_type_code'] as String,
    );

Map<String, dynamic> _$$AssetCountImplToJson(_$AssetCountImpl instance) =>
    <String, dynamic>{
      'max': instance.max,
      'min': instance.min,
      'active': instance.active,
      'asset_type_code': instance.assetTypeCode,
    };

_$AssetCountDataImpl _$$AssetCountDataImplFromJson(Map<String, dynamic> json) =>
    _$AssetCountDataImpl(
      id: (json['id'] as num).toInt(),
      module: json['module'] as String,
      tenantId: json['tenantId'] as String,
      assetCount: (json['AssetCount'] as List<dynamic>)
          .map((e) => AssetCount.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$$AssetCountDataImplToJson(
        _$AssetCountDataImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'module': instance.module,
      'tenantId': instance.tenantId,
      'AssetCount': instance.assetCount,
    };
