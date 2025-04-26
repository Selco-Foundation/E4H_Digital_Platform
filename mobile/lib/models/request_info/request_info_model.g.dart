// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'request_info_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$RequestInfoMapperModelImpl _$$RequestInfoMapperModelImplFromJson(
        Map<String, dynamic> json) =>
    _$RequestInfoMapperModelImpl(
      requestInfo: json['requestInfo'] == null
          ? null
          : RequestInfoModel.fromJson(
              json['requestInfo'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$RequestInfoMapperModelImplToJson(
        _$RequestInfoMapperModelImpl instance) =>
    <String, dynamic>{
      'requestInfo': instance.requestInfo,
    };

_$RequestInfoModelImpl _$$RequestInfoModelImplFromJson(
        Map<String, dynamic> json) =>
    _$RequestInfoModelImpl(
      apiId: json['apiId'] as String?,
      ver: json['ver'] as String?,
      ts: json['ts'] as num?,
      action: json['action'] as String?,
      did: json['did'] as String?,
      key: json['key'] as String?,
      msgId: json['msgId'] as String?,
      authToken: json['authToken'] as String?,
      userInfo: json['userInfo'] == null
          ? null
          : UserRequestModel.fromJson(json['userInfo'] as Map<String, dynamic>),
      tenantId: json['tenantId'] as String?,
    );

Map<String, dynamic> _$$RequestInfoModelImplToJson(
    _$RequestInfoModelImpl instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('apiId', instance.apiId);
  writeNotNull('ver', instance.ver);
  writeNotNull('ts', instance.ts);
  writeNotNull('action', instance.action);
  writeNotNull('did', instance.did);
  writeNotNull('key', instance.key);
  writeNotNull('msgId', instance.msgId);
  writeNotNull('authToken', instance.authToken);
  writeNotNull('userInfo', instance.userInfo);
  writeNotNull('tenantId', instance.tenantId);
  return val;
}
