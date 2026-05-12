// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'audit_details.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AuditDetailsImpl _$$AuditDetailsImplFromJson(Map<String, dynamic> json) =>
    _$AuditDetailsImpl(
      createdBy: _anyToString(json['createdBy']),
      lastModifiedBy: _anyToString(json['lastModifiedBy']),
      createdTime: _intToDateTime(json['createdTime']),
      lastModified: _intToDateTime(json['lastModified']),
    );

Map<String, dynamic> _$$AuditDetailsImplToJson(_$AuditDetailsImpl instance) =>
    <String, dynamic>{
      'createdBy': instance.createdBy,
      'lastModifiedBy': instance.lastModifiedBy,
      'createdTime': _dateTimeToInt(instance.createdTime),
      'lastModified': _dateTimeToInt(instance.lastModified),
    };
