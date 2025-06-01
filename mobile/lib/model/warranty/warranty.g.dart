// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'warranty.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$WarrantyImpl _$$WarrantyImplFromJson(Map<String, dynamic> json) =>
    _$WarrantyImpl(
      active: json['active'] as bool,
      duration: json['duration'] as String,
      format: json['format'] as String,
      assetTypeCode: json['asset_type_code'] as String,
    );

Map<String, dynamic> _$$WarrantyImplToJson(_$WarrantyImpl instance) =>
    <String, dynamic>{
      'active': instance.active,
      'duration': instance.duration,
      'format': instance.format,
      'asset_type_code': instance.assetTypeCode,
    };
