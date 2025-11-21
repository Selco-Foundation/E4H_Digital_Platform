// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'userModel.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserSearchModel _$UserSearchModelFromJson(Map<String, dynamic> json) =>
    UserSearchModel(
      id: json['id'] as String?,
      userName: json['userName'] as String?,
      tenantId: json['tenantId'] as String?,
      uuid: (json['uuid'] as List<dynamic>?)?.map((e) => e as String).toList(),
    );

Map<String, dynamic> _$UserSearchModelToJson(UserSearchModel instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('id', instance.id);
  writeNotNull('userName', instance.userName);
  writeNotNull('tenantId', instance.tenantId);
  writeNotNull('uuid', instance.uuid);
  return val;
}
