// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'warranty.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

Warranty _$WarrantyFromJson(Map<String, dynamic> json) {
  return _Warranty.fromJson(json);
}

/// @nodoc
mixin _$Warranty {
  bool get active => throw _privateConstructorUsedError;
  String get duration => throw _privateConstructorUsedError;
  String get format => throw _privateConstructorUsedError;
  @JsonKey(name: 'asset_type_code')
  String get assetTypeCode => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $WarrantyCopyWith<Warranty> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $WarrantyCopyWith<$Res> {
  factory $WarrantyCopyWith(Warranty value, $Res Function(Warranty) then) =
      _$WarrantyCopyWithImpl<$Res, Warranty>;
  @useResult
  $Res call(
      {bool active,
      String duration,
      String format,
      @JsonKey(name: 'asset_type_code') String assetTypeCode});
}

/// @nodoc
class _$WarrantyCopyWithImpl<$Res, $Val extends Warranty>
    implements $WarrantyCopyWith<$Res> {
  _$WarrantyCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? active = null,
    Object? duration = null,
    Object? format = null,
    Object? assetTypeCode = null,
  }) {
    return _then(_value.copyWith(
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      duration: null == duration
          ? _value.duration
          : duration // ignore: cast_nullable_to_non_nullable
              as String,
      format: null == format
          ? _value.format
          : format // ignore: cast_nullable_to_non_nullable
              as String,
      assetTypeCode: null == assetTypeCode
          ? _value.assetTypeCode
          : assetTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$WarrantyImplCopyWith<$Res>
    implements $WarrantyCopyWith<$Res> {
  factory _$$WarrantyImplCopyWith(
          _$WarrantyImpl value, $Res Function(_$WarrantyImpl) then) =
      __$$WarrantyImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {bool active,
      String duration,
      String format,
      @JsonKey(name: 'asset_type_code') String assetTypeCode});
}

/// @nodoc
class __$$WarrantyImplCopyWithImpl<$Res>
    extends _$WarrantyCopyWithImpl<$Res, _$WarrantyImpl>
    implements _$$WarrantyImplCopyWith<$Res> {
  __$$WarrantyImplCopyWithImpl(
      _$WarrantyImpl _value, $Res Function(_$WarrantyImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? active = null,
    Object? duration = null,
    Object? format = null,
    Object? assetTypeCode = null,
  }) {
    return _then(_$WarrantyImpl(
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      duration: null == duration
          ? _value.duration
          : duration // ignore: cast_nullable_to_non_nullable
              as String,
      format: null == format
          ? _value.format
          : format // ignore: cast_nullable_to_non_nullable
              as String,
      assetTypeCode: null == assetTypeCode
          ? _value.assetTypeCode
          : assetTypeCode // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$WarrantyImpl implements _Warranty {
  const _$WarrantyImpl(
      {required this.active,
      required this.duration,
      required this.format,
      @JsonKey(name: 'asset_type_code') required this.assetTypeCode});

  factory _$WarrantyImpl.fromJson(Map<String, dynamic> json) =>
      _$$WarrantyImplFromJson(json);

  @override
  final bool active;
  @override
  final String duration;
  @override
  final String format;
  @override
  @JsonKey(name: 'asset_type_code')
  final String assetTypeCode;

  @override
  String toString() {
    return 'Warranty(active: $active, duration: $duration, format: $format, assetTypeCode: $assetTypeCode)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$WarrantyImpl &&
            (identical(other.active, active) || other.active == active) &&
            (identical(other.duration, duration) ||
                other.duration == duration) &&
            (identical(other.format, format) || other.format == format) &&
            (identical(other.assetTypeCode, assetTypeCode) ||
                other.assetTypeCode == assetTypeCode));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode =>
      Object.hash(runtimeType, active, duration, format, assetTypeCode);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$WarrantyImplCopyWith<_$WarrantyImpl> get copyWith =>
      __$$WarrantyImplCopyWithImpl<_$WarrantyImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$WarrantyImplToJson(
      this,
    );
  }
}

abstract class _Warranty implements Warranty {
  const factory _Warranty(
      {required final bool active,
      required final String duration,
      required final String format,
      @JsonKey(name: 'asset_type_code')
      required final String assetTypeCode}) = _$WarrantyImpl;

  factory _Warranty.fromJson(Map<String, dynamic> json) =
      _$WarrantyImpl.fromJson;

  @override
  bool get active;
  @override
  String get duration;
  @override
  String get format;
  @override
  @JsonKey(name: 'asset_type_code')
  String get assetTypeCode;
  @override
  @JsonKey(ignore: true)
  _$$WarrantyImplCopyWith<_$WarrantyImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
