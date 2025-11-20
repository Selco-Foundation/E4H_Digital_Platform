// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'audit_details.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

AuditDetails _$AuditDetailsFromJson(Map<String, dynamic> json) {
  return _AuditDetails.fromJson(json);
}

/// @nodoc
mixin _$AuditDetails {
  @JsonKey(fromJson: _anyToString)
  String? get createdBy => throw _privateConstructorUsedError;
  @JsonKey(fromJson: _anyToString)
  String? get lastModifiedBy => throw _privateConstructorUsedError;
  @JsonKey(
      name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
  DateTime? get createdTime => throw _privateConstructorUsedError;
  @JsonKey(
      name: 'lastModified', fromJson: _intToDateTime, toJson: _dateTimeToInt)
  DateTime? get lastModified => throw _privateConstructorUsedError;

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
      {@JsonKey(fromJson: _anyToString) String? createdBy,
      @JsonKey(fromJson: _anyToString) String? lastModifiedBy,
      @JsonKey(
          name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
      DateTime? createdTime,
      @JsonKey(
          name: 'lastModified',
          fromJson: _intToDateTime,
          toJson: _dateTimeToInt)
      DateTime? lastModified});
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
    Object? createdBy = freezed,
    Object? lastModifiedBy = freezed,
    Object? createdTime = freezed,
    Object? lastModified = freezed,
  }) {
    return _then(_value.copyWith(
      createdBy: freezed == createdBy
          ? _value.createdBy
          : createdBy // ignore: cast_nullable_to_non_nullable
              as String?,
      lastModifiedBy: freezed == lastModifiedBy
          ? _value.lastModifiedBy
          : lastModifiedBy // ignore: cast_nullable_to_non_nullable
              as String?,
      createdTime: freezed == createdTime
          ? _value.createdTime
          : createdTime // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      lastModified: freezed == lastModified
          ? _value.lastModified
          : lastModified // ignore: cast_nullable_to_non_nullable
              as DateTime?,
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
      {@JsonKey(fromJson: _anyToString) String? createdBy,
      @JsonKey(fromJson: _anyToString) String? lastModifiedBy,
      @JsonKey(
          name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
      DateTime? createdTime,
      @JsonKey(
          name: 'lastModified',
          fromJson: _intToDateTime,
          toJson: _dateTimeToInt)
      DateTime? lastModified});
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
    Object? createdBy = freezed,
    Object? lastModifiedBy = freezed,
    Object? createdTime = freezed,
    Object? lastModified = freezed,
  }) {
    return _then(_$AuditDetailsImpl(
      createdBy: freezed == createdBy
          ? _value.createdBy
          : createdBy // ignore: cast_nullable_to_non_nullable
              as String?,
      lastModifiedBy: freezed == lastModifiedBy
          ? _value.lastModifiedBy
          : lastModifiedBy // ignore: cast_nullable_to_non_nullable
              as String?,
      createdTime: freezed == createdTime
          ? _value.createdTime
          : createdTime // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      lastModified: freezed == lastModified
          ? _value.lastModified
          : lastModified // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AuditDetailsImpl implements _AuditDetails {
  const _$AuditDetailsImpl(
      {@JsonKey(fromJson: _anyToString) this.createdBy,
      @JsonKey(fromJson: _anyToString) this.lastModifiedBy,
      @JsonKey(
          name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
      this.createdTime,
      @JsonKey(
          name: 'lastModified',
          fromJson: _intToDateTime,
          toJson: _dateTimeToInt)
      this.lastModified});

  factory _$AuditDetailsImpl.fromJson(Map<String, dynamic> json) =>
      _$$AuditDetailsImplFromJson(json);

  @override
  @JsonKey(fromJson: _anyToString)
  final String? createdBy;
  @override
  @JsonKey(fromJson: _anyToString)
  final String? lastModifiedBy;
  @override
  @JsonKey(
      name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
  final DateTime? createdTime;
  @override
  @JsonKey(
      name: 'lastModified', fromJson: _intToDateTime, toJson: _dateTimeToInt)
  final DateTime? lastModified;

  @override
  String toString() {
    return 'AuditDetails(createdBy: $createdBy, lastModifiedBy: $lastModifiedBy, createdTime: $createdTime, lastModified: $lastModified)';
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
            (identical(other.lastModified, lastModified) ||
                other.lastModified == lastModified));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType, createdBy, lastModifiedBy, createdTime, lastModified);

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
      {@JsonKey(fromJson: _anyToString) final String? createdBy,
      @JsonKey(fromJson: _anyToString) final String? lastModifiedBy,
      @JsonKey(
          name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
      final DateTime? createdTime,
      @JsonKey(
          name: 'lastModified',
          fromJson: _intToDateTime,
          toJson: _dateTimeToInt)
      final DateTime? lastModified}) = _$AuditDetailsImpl;

  factory _AuditDetails.fromJson(Map<String, dynamic> json) =
      _$AuditDetailsImpl.fromJson;

  @override
  @JsonKey(fromJson: _anyToString)
  String? get createdBy;
  @override
  @JsonKey(fromJson: _anyToString)
  String? get lastModifiedBy;
  @override
  @JsonKey(
      name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
  DateTime? get createdTime;
  @override
  @JsonKey(
      name: 'lastModified', fromJson: _intToDateTime, toJson: _dateTimeToInt)
  DateTime? get lastModified;
  @override
  @JsonKey(ignore: true)
  _$$AuditDetailsImplCopyWith<_$AuditDetailsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
