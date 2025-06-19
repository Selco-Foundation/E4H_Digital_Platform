// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'otp_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

SendOtpResponse _$SendOtpResponseFromJson(Map<String, dynamic> json) {
  return _SendOtpResponse.fromJson(json);
}

/// @nodoc
mixin _$SendOtpResponse {
  bool? get isSuccessful => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $SendOtpResponseCopyWith<SendOtpResponse> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SendOtpResponseCopyWith<$Res> {
  factory $SendOtpResponseCopyWith(
          SendOtpResponse value, $Res Function(SendOtpResponse) then) =
      _$SendOtpResponseCopyWithImpl<$Res, SendOtpResponse>;
  @useResult
  $Res call({bool? isSuccessful});
}

/// @nodoc
class _$SendOtpResponseCopyWithImpl<$Res, $Val extends SendOtpResponse>
    implements $SendOtpResponseCopyWith<$Res> {
  _$SendOtpResponseCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? isSuccessful = freezed,
  }) {
    return _then(_value.copyWith(
      isSuccessful: freezed == isSuccessful
          ? _value.isSuccessful
          : isSuccessful // ignore: cast_nullable_to_non_nullable
              as bool?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SendOtpResponseImplCopyWith<$Res>
    implements $SendOtpResponseCopyWith<$Res> {
  factory _$$SendOtpResponseImplCopyWith(_$SendOtpResponseImpl value,
          $Res Function(_$SendOtpResponseImpl) then) =
      __$$SendOtpResponseImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({bool? isSuccessful});
}

/// @nodoc
class __$$SendOtpResponseImplCopyWithImpl<$Res>
    extends _$SendOtpResponseCopyWithImpl<$Res, _$SendOtpResponseImpl>
    implements _$$SendOtpResponseImplCopyWith<$Res> {
  __$$SendOtpResponseImplCopyWithImpl(
      _$SendOtpResponseImpl _value, $Res Function(_$SendOtpResponseImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? isSuccessful = freezed,
  }) {
    return _then(_$SendOtpResponseImpl(
      isSuccessful: freezed == isSuccessful
          ? _value.isSuccessful
          : isSuccessful // ignore: cast_nullable_to_non_nullable
              as bool?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SendOtpResponseImpl implements _SendOtpResponse {
  const _$SendOtpResponseImpl({required this.isSuccessful});

  factory _$SendOtpResponseImpl.fromJson(Map<String, dynamic> json) =>
      _$$SendOtpResponseImplFromJson(json);

  @override
  final bool? isSuccessful;

  @override
  String toString() {
    return 'SendOtpResponse(isSuccessful: $isSuccessful)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SendOtpResponseImpl &&
            (identical(other.isSuccessful, isSuccessful) ||
                other.isSuccessful == isSuccessful));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, isSuccessful);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SendOtpResponseImplCopyWith<_$SendOtpResponseImpl> get copyWith =>
      __$$SendOtpResponseImplCopyWithImpl<_$SendOtpResponseImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SendOtpResponseImplToJson(
      this,
    );
  }
}

abstract class _SendOtpResponse implements SendOtpResponse {
  const factory _SendOtpResponse({required final bool? isSuccessful}) =
      _$SendOtpResponseImpl;

  factory _SendOtpResponse.fromJson(Map<String, dynamic> json) =
      _$SendOtpResponseImpl.fromJson;

  @override
  bool? get isSuccessful;
  @override
  @JsonKey(ignore: true)
  _$$SendOtpResponseImplCopyWith<_$SendOtpResponseImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
