// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'asset_type.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AssetTypeImpl _$$AssetTypeImplFromJson(Map<String, dynamic> json) =>
    _$AssetTypeImpl(
      code: json['code'] as String,
      name: json['name'] as String,
      active: json['active'] as bool,
      formFields: (json['form_fields'] as List<dynamic>)
          .map((e) => FormField.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$$AssetTypeImplToJson(_$AssetTypeImpl instance) =>
    <String, dynamic>{
      'code': instance.code,
      'name': instance.name,
      'active': instance.active,
      'form_fields': instance.formFields,
    };

_$FormFieldImpl _$$FormFieldImplFromJson(Map<String, dynamic> json) =>
    _$FormFieldImpl(
      key: json['key'] as String?,
      name: json['name'] as String?,
      system: json['system'] as String?,
      options:
          (json['options'] as List<dynamic>?)?.map((e) => e as String).toList(),
      types:
          (json['types'] as List<dynamic>?)?.map((e) => e as String).toList(),
    );

Map<String, dynamic> _$$FormFieldImplToJson(_$FormFieldImpl instance) =>
    <String, dynamic>{
      'key': instance.key,
      'name': instance.name,
      'system': instance.system,
      'options': instance.options,
      'types': instance.types,
    };

_$AssetTypeDataImpl _$$AssetTypeDataImplFromJson(Map<String, dynamic> json) =>
    _$AssetTypeDataImpl(
      id: (json['id'] as num).toInt(),
      module: json['module'] as String,
      tenantId: json['tenantId'] as String,
      assetType: (json['AssetType'] as List<dynamic>)
          .map((e) => AssetType.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$$AssetTypeDataImplToJson(_$AssetTypeDataImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'module': instance.module,
      'tenantId': instance.tenantId,
      'AssetType': instance.assetType,
    };
