// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'mdms.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

Mdms<T> _$MdmsFromJson<T>(
    Map<String, dynamic> json, T Function(Object?) fromJsonT) {
  return _Mdms<T>.fromJson(json, fromJsonT);
}

/// @nodoc
mixin _$Mdms<T> {
  String get id => throw _privateConstructorUsedError;
  String get tenantId => throw _privateConstructorUsedError;
  String get schemaCode => throw _privateConstructorUsedError;
  String get uniqueIdentifier => throw _privateConstructorUsedError;
  T get data => throw _privateConstructorUsedError;
  bool get isActive => throw _privateConstructorUsedError;
  AuditDetails get auditDetails => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson(Object? Function(T) toJsonT) =>
      throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $MdmsCopyWith<T, Mdms<T>> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MdmsCopyWith<T, $Res> {
  factory $MdmsCopyWith(Mdms<T> value, $Res Function(Mdms<T>) then) =
      _$MdmsCopyWithImpl<T, $Res, Mdms<T>>;
  @useResult
  $Res call(
      {String id,
      String tenantId,
      String schemaCode,
      String uniqueIdentifier,
      T data,
      bool isActive,
      AuditDetails auditDetails});

  $AuditDetailsCopyWith<$Res> get auditDetails;
}

/// @nodoc
class _$MdmsCopyWithImpl<T, $Res, $Val extends Mdms<T>>
    implements $MdmsCopyWith<T, $Res> {
  _$MdmsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? tenantId = null,
    Object? schemaCode = null,
    Object? uniqueIdentifier = null,
    Object? data = freezed,
    Object? isActive = null,
    Object? auditDetails = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
      schemaCode: null == schemaCode
          ? _value.schemaCode
          : schemaCode // ignore: cast_nullable_to_non_nullable
              as String,
      uniqueIdentifier: null == uniqueIdentifier
          ? _value.uniqueIdentifier
          : uniqueIdentifier // ignore: cast_nullable_to_non_nullable
              as String,
      data: freezed == data
          ? _value.data
          : data // ignore: cast_nullable_to_non_nullable
              as T,
      isActive: null == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool,
      auditDetails: null == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $AuditDetailsCopyWith<$Res> get auditDetails {
    return $AuditDetailsCopyWith<$Res>(_value.auditDetails, (value) {
      return _then(_value.copyWith(auditDetails: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$MdmsImplCopyWith<T, $Res> implements $MdmsCopyWith<T, $Res> {
  factory _$$MdmsImplCopyWith(
          _$MdmsImpl<T> value, $Res Function(_$MdmsImpl<T>) then) =
      __$$MdmsImplCopyWithImpl<T, $Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String tenantId,
      String schemaCode,
      String uniqueIdentifier,
      T data,
      bool isActive,
      AuditDetails auditDetails});

  @override
  $AuditDetailsCopyWith<$Res> get auditDetails;
}

/// @nodoc
class __$$MdmsImplCopyWithImpl<T, $Res>
    extends _$MdmsCopyWithImpl<T, $Res, _$MdmsImpl<T>>
    implements _$$MdmsImplCopyWith<T, $Res> {
  __$$MdmsImplCopyWithImpl(
      _$MdmsImpl<T> _value, $Res Function(_$MdmsImpl<T>) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? tenantId = null,
    Object? schemaCode = null,
    Object? uniqueIdentifier = null,
    Object? data = freezed,
    Object? isActive = null,
    Object? auditDetails = null,
  }) {
    return _then(_$MdmsImpl<T>(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      tenantId: null == tenantId
          ? _value.tenantId
          : tenantId // ignore: cast_nullable_to_non_nullable
              as String,
      schemaCode: null == schemaCode
          ? _value.schemaCode
          : schemaCode // ignore: cast_nullable_to_non_nullable
              as String,
      uniqueIdentifier: null == uniqueIdentifier
          ? _value.uniqueIdentifier
          : uniqueIdentifier // ignore: cast_nullable_to_non_nullable
              as String,
      data: freezed == data
          ? _value.data
          : data // ignore: cast_nullable_to_non_nullable
              as T,
      isActive: null == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool,
      auditDetails: null == auditDetails
          ? _value.auditDetails
          : auditDetails // ignore: cast_nullable_to_non_nullable
              as AuditDetails,
    ));
  }
}

/// @nodoc

@JsonSerializable(genericArgumentFactories: true)
class _$MdmsImpl<T> implements _Mdms<T> {
  const _$MdmsImpl(
      {required this.id,
      required this.tenantId,
      required this.schemaCode,
      required this.uniqueIdentifier,
      required this.data,
      required this.isActive,
      required this.auditDetails});

  factory _$MdmsImpl.fromJson(
          Map<String, dynamic> json, T Function(Object?) fromJsonT) =>
      _$$MdmsImplFromJson(json, fromJsonT);

  @override
  final String id;
  @override
  final String tenantId;
  @override
  final String schemaCode;
  @override
  final String uniqueIdentifier;
  @override
  final T data;
  @override
  final bool isActive;
  @override
  final AuditDetails auditDetails;

  @override
  String toString() {
    return 'Mdms<$T>(id: $id, tenantId: $tenantId, schemaCode: $schemaCode, uniqueIdentifier: $uniqueIdentifier, data: $data, isActive: $isActive, auditDetails: $auditDetails)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MdmsImpl<T> &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.tenantId, tenantId) ||
                other.tenantId == tenantId) &&
            (identical(other.schemaCode, schemaCode) ||
                other.schemaCode == schemaCode) &&
            (identical(other.uniqueIdentifier, uniqueIdentifier) ||
                other.uniqueIdentifier == uniqueIdentifier) &&
            const DeepCollectionEquality().equals(other.data, data) &&
            (identical(other.isActive, isActive) ||
                other.isActive == isActive) &&
            (identical(other.auditDetails, auditDetails) ||
                other.auditDetails == auditDetails));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      tenantId,
      schemaCode,
      uniqueIdentifier,
      const DeepCollectionEquality().hash(data),
      isActive,
      auditDetails);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$MdmsImplCopyWith<T, _$MdmsImpl<T>> get copyWith =>
      __$$MdmsImplCopyWithImpl<T, _$MdmsImpl<T>>(this, _$identity);

  @override
  Map<String, dynamic> toJson(Object? Function(T) toJsonT) {
    return _$$MdmsImplToJson<T>(this, toJsonT);
  }
}

abstract class _Mdms<T> implements Mdms<T> {
  const factory _Mdms(
      {required final String id,
      required final String tenantId,
      required final String schemaCode,
      required final String uniqueIdentifier,
      required final T data,
      required final bool isActive,
      required final AuditDetails auditDetails}) = _$MdmsImpl<T>;

  factory _Mdms.fromJson(
          Map<String, dynamic> json, T Function(Object?) fromJsonT) =
      _$MdmsImpl<T>.fromJson;

  @override
  String get id;
  @override
  String get tenantId;
  @override
  String get schemaCode;
  @override
  String get uniqueIdentifier;
  @override
  T get data;
  @override
  bool get isActive;
  @override
  AuditDetails get auditDetails;
  @override
  @JsonKey(ignore: true)
  _$$MdmsImplCopyWith<T, _$MdmsImpl<T>> get copyWith =>
      throw _privateConstructorUsedError;
}

AuditDetails _$AuditDetailsFromJson(Map<String, dynamic> json) {
  return _AuditDetails.fromJson(json);
}

/// @nodoc
mixin _$AuditDetails {
  String get createdBy => throw _privateConstructorUsedError;
  String get lastModifiedBy => throw _privateConstructorUsedError;
  int get createdTime => throw _privateConstructorUsedError;
  int get lastModifiedTime => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $AuditDetailsCopyWith<AuditDetails> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AuditDetailsCopyWith<$Res> {
  factory $AuditDetailsCopyWith(
          AuditDetails value, $Res Function(AuditDetails) then) =
      _$AuditDetailsCopyWithImpl<$Res, AuditDetails>;
  @useResult
  $Res call(
      {String createdBy,
      String lastModifiedBy,
      int createdTime,
      int lastModifiedTime});
}

/// @nodoc
class _$AuditDetailsCopyWithImpl<$Res, $Val extends AuditDetails>
    implements $AuditDetailsCopyWith<$Res> {
  _$AuditDetailsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? createdBy = null,
    Object? lastModifiedBy = null,
    Object? createdTime = null,
    Object? lastModifiedTime = null,
  }) {
    return _then(_value.copyWith(
      createdBy: null == createdBy
          ? _value.createdBy
          : createdBy // ignore: cast_nullable_to_non_nullable
              as String,
      lastModifiedBy: null == lastModifiedBy
          ? _value.lastModifiedBy
          : lastModifiedBy // ignore: cast_nullable_to_non_nullable
              as String,
      createdTime: null == createdTime
          ? _value.createdTime
          : createdTime // ignore: cast_nullable_to_non_nullable
              as int,
      lastModifiedTime: null == lastModifiedTime
          ? _value.lastModifiedTime
          : lastModifiedTime // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AuditDetailsImplCopyWith<$Res>
    implements $AuditDetailsCopyWith<$Res> {
  factory _$$AuditDetailsImplCopyWith(
          _$AuditDetailsImpl value, $Res Function(_$AuditDetailsImpl) then) =
      __$$AuditDetailsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String createdBy,
      String lastModifiedBy,
      int createdTime,
      int lastModifiedTime});
}

/// @nodoc
class __$$AuditDetailsImplCopyWithImpl<$Res>
    extends _$AuditDetailsCopyWithImpl<$Res, _$AuditDetailsImpl>
    implements _$$AuditDetailsImplCopyWith<$Res> {
  __$$AuditDetailsImplCopyWithImpl(
      _$AuditDetailsImpl _value, $Res Function(_$AuditDetailsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? createdBy = null,
    Object? lastModifiedBy = null,
    Object? createdTime = null,
    Object? lastModifiedTime = null,
  }) {
    return _then(_$AuditDetailsImpl(
      createdBy: null == createdBy
          ? _value.createdBy
          : createdBy // ignore: cast_nullable_to_non_nullable
              as String,
      lastModifiedBy: null == lastModifiedBy
          ? _value.lastModifiedBy
          : lastModifiedBy // ignore: cast_nullable_to_non_nullable
              as String,
      createdTime: null == createdTime
          ? _value.createdTime
          : createdTime // ignore: cast_nullable_to_non_nullable
              as int,
      lastModifiedTime: null == lastModifiedTime
          ? _value.lastModifiedTime
          : lastModifiedTime // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AuditDetailsImpl implements _AuditDetails {
  const _$AuditDetailsImpl(
      {required this.createdBy,
      required this.lastModifiedBy,
      required this.createdTime,
      required this.lastModifiedTime});

  factory _$AuditDetailsImpl.fromJson(Map<String, dynamic> json) =>
      _$$AuditDetailsImplFromJson(json);

  @override
  final String createdBy;
  @override
  final String lastModifiedBy;
  @override
  final int createdTime;
  @override
  final int lastModifiedTime;

  @override
  String toString() {
    return 'AuditDetails(createdBy: $createdBy, lastModifiedBy: $lastModifiedBy, createdTime: $createdTime, lastModifiedTime: $lastModifiedTime)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AuditDetailsImpl &&
            (identical(other.createdBy, createdBy) ||
                other.createdBy == createdBy) &&
            (identical(other.lastModifiedBy, lastModifiedBy) ||
                other.lastModifiedBy == lastModifiedBy) &&
            (identical(other.createdTime, createdTime) ||
                other.createdTime == createdTime) &&
            (identical(other.lastModifiedTime, lastModifiedTime) ||
                other.lastModifiedTime == lastModifiedTime));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType, createdBy, lastModifiedBy, createdTime, lastModifiedTime);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AuditDetailsImplCopyWith<_$AuditDetailsImpl> get copyWith =>
      __$$AuditDetailsImplCopyWithImpl<_$AuditDetailsImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AuditDetailsImplToJson(
      this,
    );
  }
}

abstract class _AuditDetails implements AuditDetails {
  const factory _AuditDetails(
      {required final String createdBy,
      required final String lastModifiedBy,
      required final int createdTime,
      required final int lastModifiedTime}) = _$AuditDetailsImpl;

  factory _AuditDetails.fromJson(Map<String, dynamic> json) =
      _$AuditDetailsImpl.fromJson;

  @override
  String get createdBy;
  @override
  String get lastModifiedBy;
  @override
  int get createdTime;
  @override
  int get lastModifiedTime;
  @override
  @JsonKey(ignore: true)
  _$$AuditDetailsImplCopyWith<_$AuditDetailsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
