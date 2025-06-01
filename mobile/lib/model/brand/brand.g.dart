// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'brand.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$BrandImpl _$$BrandImplFromJson(Map<String, dynamic> json) => _$BrandImpl(
      active: json['active'] as bool,
      code: json['code'] as String,
      name: json['name'] as String,
      assetTypeCode: json['asset_type_code'] as String,
    );

Map<String, dynamic> _$$BrandImplToJson(_$BrandImpl instance) =>
    <String, dynamic>{
      'active': instance.active,
      'code': instance.code,
      'name': instance.name,
      'asset_type_code': instance.assetTypeCode,
    };
