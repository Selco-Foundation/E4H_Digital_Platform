// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'solution_design_type.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

SolutionDesignType _$SolutionDesignTypeFromJson(Map<String, dynamic> json) {
  return _SolutionDesignType.fromJson(json);
}

/// @nodoc
mixin _$SolutionDesignType {
  bool get active => throw _privateConstructorUsedError;
  String get code => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String get url => throw _privateConstructorUsedError;
  @JsonKey(name: 'system_code')
  String get systemCode => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $SolutionDesignTypeCopyWith<SolutionDesignType> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SolutionDesignTypeCopyWith<$Res> {
  factory $SolutionDesignTypeCopyWith(
          SolutionDesignType value, $Res Function(SolutionDesignType) then) =
      _$SolutionDesignTypeCopyWithImpl<$Res, SolutionDesignType>;
  @useResult
  $Res call(
      {bool active,
      String code,
      String name,
      String url,
      @JsonKey(name: 'system_code') String systemCode});
}

/// @nodoc
class _$SolutionDesignTypeCopyWithImpl<$Res, $Val extends SolutionDesignType>
    implements $SolutionDesignTypeCopyWith<$Res> {
  _$SolutionDesignTypeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? active = null,
    Object? code = null,
    Object? name = null,
    Object? url = null,
    Object? systemCode = null,
  }) {
    return _then(_value.copyWith(
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      url: null == url
          ? _value.url
          : url // ignore: cast_nullable_to_non_nullable
              as String,
      systemCode: null == systemCode
          ? _value.systemCode
          : systemCode // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SolutionDesignTypeImplCopyWith<$Res>
    implements $SolutionDesignTypeCopyWith<$Res> {
  factory _$$SolutionDesignTypeImplCopyWith(_$SolutionDesignTypeImpl value,
          $Res Function(_$SolutionDesignTypeImpl) then) =
      __$$SolutionDesignTypeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {bool active,
      String code,
      String name,
      String url,
      @JsonKey(name: 'system_code') String systemCode});
}

/// @nodoc
class __$$SolutionDesignTypeImplCopyWithImpl<$Res>
    extends _$SolutionDesignTypeCopyWithImpl<$Res, _$SolutionDesignTypeImpl>
    implements _$$SolutionDesignTypeImplCopyWith<$Res> {
  __$$SolutionDesignTypeImplCopyWithImpl(_$SolutionDesignTypeImpl _value,
      $Res Function(_$SolutionDesignTypeImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? active = null,
    Object? code = null,
    Object? name = null,
    Object? url = null,
    Object? systemCode = null,
  }) {
    return _then(_$SolutionDesignTypeImpl(
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      url: null == url
          ? _value.url
          : url // ignore: cast_nullable_to_non_nullable
              as String,
      systemCode: null == systemCode
          ? _value.systemCode
          : systemCode // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SolutionDesignTypeImpl implements _SolutionDesignType {
  const _$SolutionDesignTypeImpl(
      {required this.active,
      required this.code,
      required this.name,
      required this.url,
      @JsonKey(name: 'system_code') required this.systemCode});

  factory _$SolutionDesignTypeImpl.fromJson(Map<String, dynamic> json) =>
      _$$SolutionDesignTypeImplFromJson(json);

  @override
  final bool active;
  @override
  final String code;
  @override
  final String name;
  @override
  final String url;
  @override
  @JsonKey(name: 'system_code')
  final String systemCode;

  @override
  String toString() {
    return 'SolutionDesignType(active: $active, code: $code, name: $name, url: $url, systemCode: $systemCode)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SolutionDesignTypeImpl &&
            (identical(other.active, active) || other.active == active) &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.url, url) || other.url == url) &&
            (identical(other.systemCode, systemCode) ||
                other.systemCode == systemCode));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode =>
      Object.hash(runtimeType, active, code, name, url, systemCode);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SolutionDesignTypeImplCopyWith<_$SolutionDesignTypeImpl> get copyWith =>
      __$$SolutionDesignTypeImplCopyWithImpl<_$SolutionDesignTypeImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SolutionDesignTypeImplToJson(
      this,
    );
  }
}

abstract class _SolutionDesignType implements SolutionDesignType {
  const factory _SolutionDesignType(
          {required final bool active,
          required final String code,
          required final String name,
          required final String url,
          @JsonKey(name: 'system_code') required final String systemCode}) =
      _$SolutionDesignTypeImpl;

  factory _SolutionDesignType.fromJson(Map<String, dynamic> json) =
      _$SolutionDesignTypeImpl.fromJson;

  @override
  bool get active;
  @override
  String get code;
  @override
  String get name;
  @override
  String get url;
  @override
  @JsonKey(name: 'system_code')
  String get systemCode;
  @override
  @JsonKey(ignore: true)
  _$$SolutionDesignTypeImplCopyWith<_$SolutionDesignTypeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
