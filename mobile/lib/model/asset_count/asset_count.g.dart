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
