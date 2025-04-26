// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'auth_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AuthModelImpl _$$AuthModelImplFromJson(Map<String, dynamic> json) =>
    _$AuthModelImpl(
      accessToken: json['access_token'] as String,
      tokenType: json['token_type'] as String,
      refreshToken: json['refresh_token'] as String,
      expiresIn: (json['expires_in'] as num).toInt(),
      userRequestModel: UserRequestModel.fromJson(
          json['UserRequest'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$AuthModelImplToJson(_$AuthModelImpl instance) =>
    <String, dynamic>{
      'access_token': instance.accessToken,
      'token_type': instance.tokenType,
      'refresh_token': instance.refreshToken,
      'expires_in': instance.expiresIn,
      'UserRequest': instance.userRequestModel,
    };

_$LoginModelImpl _$$LoginModelImplFromJson(Map<String, dynamic> json) =>
    _$LoginModelImpl(
      username: json['username'] as String,
      password: json['password'] as String,
      userType: json['userType'] as String? ?? 'EMPLOYEE',
      tenantId: json['tenantId'] as String,
      scope: json['scope'] as String? ?? 'read',
      grantType: json['grant_type'] as String? ?? 'password',
    );

Map<String, dynamic> _$$LoginModelImplToJson(_$LoginModelImpl instance) =>
    <String, dynamic>{
      'username': instance.username,
      'password': instance.password,
      'userType': instance.userType,
      'tenantId': instance.tenantId,
      'scope': instance.scope,
      'grant_type': instance.grantType,
    };

_$UserRequestModelImpl _$$UserRequestModelImplFromJson(
        Map<String, dynamic> json) =>
    _$UserRequestModelImpl(
      id: (json['id'] as num?)?.toInt(),
      uuid: json['uuid'] as String,
      userName: json['userName'] as String?,
      name: json['name'] as String?,
      mobileNumber: json['mobileNumber'] as String?,
      emailId: json['emailId'] as String?,
      locale: json['locale'] as String?,
      active: json['active'] as bool?,
      tenantId: json['tenantId'] as String?,
      permanentCity: json['permanentCity'] as String?,
      gender: json['gender'] as String?,
      roles: (json['roles'] as List<dynamic>?)
              ?.map((e) => UserRoleModel.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
    );

Map<String, dynamic> _$$UserRequestModelImplToJson(
        _$UserRequestModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'uuid': instance.uuid,
      'userName': instance.userName,
      'name': instance.name,
      'mobileNumber': instance.mobileNumber,
      'emailId': instance.emailId,
      'locale': instance.locale,
      'active': instance.active,
      'tenantId': instance.tenantId,
      'permanentCity': instance.permanentCity,
      'gender': instance.gender,
      'roles': instance.roles,
    };

_$UserRoleModelImpl _$$UserRoleModelImplFromJson(Map<String, dynamic> json) =>
    _$UserRoleModelImpl(
      name: json['name'] as String? ?? '',
      code: json['code'] as String,
      tenantId: json['tenantId'] as String?,
    );

Map<String, dynamic> _$$UserRoleModelImplToJson(_$UserRoleModelImpl instance) =>
    <String, dynamic>{
      'name': instance.name,
      'code': instance.code,
      'tenantId': instance.tenantId,
    };
