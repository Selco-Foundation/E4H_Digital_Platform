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

_$WarrantyDataImpl _$$WarrantyDataImplFromJson(Map<String, dynamic> json) =>
    _$WarrantyDataImpl(
      id: (json['id'] as num).toInt(),
      module: json['module'] as String,
      tenantId: json['tenantId'] as String,
      warrantyDuration: (json['WarrantyDuration'] as List<dynamic>)
          .map((e) => Warranty.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$$WarrantyDataImplToJson(_$WarrantyDataImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'module': instance.module,
      'tenantId': instance.tenantId,
      'WarrantyDuration': instance.warrantyDuration,
    };
