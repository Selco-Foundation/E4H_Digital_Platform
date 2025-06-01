// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'system.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

System _$SystemFromJson(Map<String, dynamic> json) {
  return _System.fromJson(json);
}

/// @nodoc
mixin _$System {
  String get code => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  bool get active => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $SystemCopyWith<System> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SystemCopyWith<$Res> {
  factory $SystemCopyWith(System value, $Res Function(System) then) =
      _$SystemCopyWithImpl<$Res, System>;
  @useResult
  $Res call({String code, String name, bool active});
}

/// @nodoc
class _$SystemCopyWithImpl<$Res, $Val extends System>
    implements $SystemCopyWith<$Res> {
  _$SystemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? name = null,
    Object? active = null,
  }) {
    return _then(_value.copyWith(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SystemImplCopyWith<$Res> implements $SystemCopyWith<$Res> {
  factory _$$SystemImplCopyWith(
          _$SystemImpl value, $Res Function(_$SystemImpl) then) =
      __$$SystemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String code, String name, bool active});
}

/// @nodoc
class __$$SystemImplCopyWithImpl<$Res>
    extends _$SystemCopyWithImpl<$Res, _$SystemImpl>
    implements _$$SystemImplCopyWith<$Res> {
  __$$SystemImplCopyWithImpl(
      _$SystemImpl _value, $Res Function(_$SystemImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? name = null,
    Object? active = null,
  }) {
    return _then(_$SystemImpl(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      active: null == active
          ? _value.active
          : active // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SystemImpl implements _System {
  const _$SystemImpl(
      {required this.code, required this.name, required this.active});

  factory _$SystemImpl.fromJson(Map<String, dynamic> json) =>
      _$$SystemImplFromJson(json);

  @override
  final String code;
  @override
  final String name;
  @override
  final bool active;

  @override
  String toString() {
    return 'System(code: $code, name: $name, active: $active)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SystemImpl &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.active, active) || other.active == active));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, code, name, active);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SystemImplCopyWith<_$SystemImpl> get copyWith =>
      __$$SystemImplCopyWithImpl<_$SystemImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SystemImplToJson(
      this,
    );
  }
}

abstract class _System implements System {
  const factory _System(
      {required final String code,
      required final String name,
      required final bool active}) = _$SystemImpl;

  factory _System.fromJson(Map<String, dynamic> json) = _$SystemImpl.fromJson;

  @override
  String get code;
  @override
  String get name;
  @override
  bool get active;
  @override
  @JsonKey(ignore: true)
  _$$SystemImplCopyWith<_$SystemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
