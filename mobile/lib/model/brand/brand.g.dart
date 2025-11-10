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

_$BrandDataImpl _$$BrandDataImplFromJson(Map<String, dynamic> json) =>
    _$BrandDataImpl(
      id: (json['id'] as num).toInt(),
      brand: (json['Brand'] as List<dynamic>)
          .map((e) => Brand.fromJson(e as Map<String, dynamic>))
          .toList(),
      module: json['module'] as String,
      tenantId: json['tenantId'] as String,
    );

Map<String, dynamic> _$$BrandDataImplToJson(_$BrandDataImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'Brand': instance.brand,
      'module': instance.module,
      'tenantId': instance.tenantId,
    };
