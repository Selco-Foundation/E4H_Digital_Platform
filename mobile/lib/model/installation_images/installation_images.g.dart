// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'installation_images.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$InstallationImageItemImpl _$$InstallationImageItemImplFromJson(
        Map<String, dynamic> json) =>
    _$InstallationImageItemImpl(
      code: json['code'] as String,
      active: json['active'] as bool,
      description: json['description'] as String,
      requiredCount: (json['required_count'] as num).toInt(),
    );

Map<String, dynamic> _$$InstallationImageItemImplToJson(
        _$InstallationImageItemImpl instance) =>
    <String, dynamic>{
      'code': instance.code,
      'active': instance.active,
      'description': instance.description,
      'required_count': instance.requiredCount,
    };

_$InstallationImagesDataImpl _$$InstallationImagesDataImplFromJson(
        Map<String, dynamic> json) =>
    _$InstallationImagesDataImpl(
      id: (json['id'] as num).toInt(),
      tenantId: json['tenantId'] as String,
      installationImage: (json['InstallationImage'] as List<dynamic>)
          .map((e) => InstallationImageItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$$InstallationImagesDataImplToJson(
        _$InstallationImagesDataImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'tenantId': instance.tenantId,
      'InstallationImage': instance.installationImage,
    };
