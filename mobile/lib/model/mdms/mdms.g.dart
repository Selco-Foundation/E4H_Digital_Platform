// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mdms.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$MdmsImpl<T> _$$MdmsImplFromJson<T>(
  Map<String, dynamic> json,
  T Function(Object? json) fromJsonT,
) =>
    _$MdmsImpl<T>(
      id: json['id'] as String,
      tenantId: json['tenantId'] as String,
      schemaCode: json['schemaCode'] as String,
      uniqueIdentifier: json['uniqueIdentifier'] as String,
      data: fromJsonT(json['data']),
      isActive: json['isActive'] as bool,
      auditDetails:
          AuditDetails.fromJson(json['auditDetails'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$MdmsImplToJson<T>(
  _$MdmsImpl<T> instance,
  Object? Function(T value) toJsonT,
) =>
    <String, dynamic>{
      'id': instance.id,
      'tenantId': instance.tenantId,
      'schemaCode': instance.schemaCode,
      'uniqueIdentifier': instance.uniqueIdentifier,
      'data': toJsonT(instance.data),
      'isActive': instance.isActive,
      'auditDetails': instance.auditDetails,
    };

_$AuditDetailsImpl _$$AuditDetailsImplFromJson(Map<String, dynamic> json) =>
    _$AuditDetailsImpl(
      createdBy: json['createdBy'] as String,
      lastModifiedBy: json['lastModifiedBy'] as String,
      createdTime: (json['createdTime'] as num).toInt(),
      lastModifiedTime: (json['lastModifiedTime'] as num).toInt(),
    );

Map<String, dynamic> _$$AuditDetailsImplToJson(_$AuditDetailsImpl instance) =>
    <String, dynamic>{
      'createdBy': instance.createdBy,
      'lastModifiedBy': instance.lastModifiedBy,
      'createdTime': instance.createdTime,
      'lastModifiedTime': instance.lastModifiedTime,
    };
