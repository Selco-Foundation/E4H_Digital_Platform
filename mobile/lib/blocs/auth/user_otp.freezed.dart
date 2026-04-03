// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'user_otp.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$UserOtpEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UserOtpEventCopyWith<$Res> {
  factory $UserOtpEventCopyWith(
          UserOtpEvent value, $Res Function(UserOtpEvent) then) =
      _$UserOtpEventCopyWithImpl<$Res, UserOtpEvent>;
}

/// @nodoc
class _$UserOtpEventCopyWithImpl<$Res, $Val extends UserOtpEvent>
    implements $UserOtpEventCopyWith<$Res> {
  _$UserOtpEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$SendOtpEventImplCopyWith<$Res> {
  factory _$$SendOtpEventImplCopyWith(
          _$SendOtpEventImpl value, $Res Function(_$SendOtpEventImpl) then) =
      __$$SendOtpEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String phone});
}

/// @nodoc
class __$$SendOtpEventImplCopyWithImpl<$Res>
    extends _$UserOtpEventCopyWithImpl<$Res, _$SendOtpEventImpl>
    implements _$$SendOtpEventImplCopyWith<$Res> {
  __$$SendOtpEventImplCopyWithImpl(
      _$SendOtpEventImpl _value, $Res Function(_$SendOtpEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? phone = null,
  }) {
    return _then(_$SendOtpEventImpl(
      phone: null == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SendOtpEventImpl implements _SendOtpEvent {
  const _$SendOtpEventImpl({required this.phone});

  @override
  final String phone;

  @override
  String toString() {
    return 'UserOtpEvent.sendOtp(phone: $phone)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SendOtpEventImpl &&
            (identical(other.phone, phone) || other.phone == phone));
  }

  @override
  int get hashCode => Object.hash(runtimeType, phone);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SendOtpEventImplCopyWith<_$SendOtpEventImpl> get copyWith =>
      __$$SendOtpEventImplCopyWithImpl<_$SendOtpEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) {
    return sendOtp(phone);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) {
    return sendOtp?.call(phone);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) {
    if (sendOtp != null) {
      return sendOtp(phone);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) {
    return sendOtp(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) {
    return sendOtp?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) {
    if (sendOtp != null) {
      return sendOtp(this);
    }
    return orElse();
  }
}

abstract class _SendOtpEvent implements UserOtpEvent {
  const factory _SendOtpEvent({required final String phone}) =
      _$SendOtpEventImpl;

  String get phone;
  @JsonKey(ignore: true)
  _$$SendOtpEventImplCopyWith<_$SendOtpEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$StoreOtpEventImplCopyWith<$Res> {
  factory _$$StoreOtpEventImplCopyWith(
          _$StoreOtpEventImpl value, $Res Function(_$StoreOtpEventImpl) then) =
      __$$StoreOtpEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String otp});
}

/// @nodoc
class __$$StoreOtpEventImplCopyWithImpl<$Res>
    extends _$UserOtpEventCopyWithImpl<$Res, _$StoreOtpEventImpl>
    implements _$$StoreOtpEventImplCopyWith<$Res> {
  __$$StoreOtpEventImplCopyWithImpl(
      _$StoreOtpEventImpl _value, $Res Function(_$StoreOtpEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? otp = null,
  }) {
    return _then(_$StoreOtpEventImpl(
      otp: null == otp
          ? _value.otp
          : otp // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$StoreOtpEventImpl implements _StoreOtpEvent {
  const _$StoreOtpEventImpl({required this.otp});

  @override
  final String otp;

  @override
  String toString() {
    return 'UserOtpEvent.storeOtp(otp: $otp)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$StoreOtpEventImpl &&
            (identical(other.otp, otp) || other.otp == otp));
  }

  @override
  int get hashCode => Object.hash(runtimeType, otp);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$StoreOtpEventImplCopyWith<_$StoreOtpEventImpl> get copyWith =>
      __$$StoreOtpEventImplCopyWithImpl<_$StoreOtpEventImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) {
    return storeOtp(otp);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) {
    return storeOtp?.call(otp);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) {
    if (storeOtp != null) {
      return storeOtp(otp);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) {
    return storeOtp(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) {
    return storeOtp?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) {
    if (storeOtp != null) {
      return storeOtp(this);
    }
    return orElse();
  }
}

abstract class _StoreOtpEvent implements UserOtpEvent {
  const factory _StoreOtpEvent({required final String otp}) =
      _$StoreOtpEventImpl;

  String get otp;
  @JsonKey(ignore: true)
  _$$StoreOtpEventImplCopyWith<_$StoreOtpEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$GetOtpEventImplCopyWith<$Res> {
  factory _$$GetOtpEventImplCopyWith(
          _$GetOtpEventImpl value, $Res Function(_$GetOtpEventImpl) then) =
      __$$GetOtpEventImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$GetOtpEventImplCopyWithImpl<$Res>
    extends _$UserOtpEventCopyWithImpl<$Res, _$GetOtpEventImpl>
    implements _$$GetOtpEventImplCopyWith<$Res> {
  __$$GetOtpEventImplCopyWithImpl(
      _$GetOtpEventImpl _value, $Res Function(_$GetOtpEventImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$GetOtpEventImpl implements _GetOtpEvent {
  const _$GetOtpEventImpl();

  @override
  String toString() {
    return 'UserOtpEvent.getOtp()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$GetOtpEventImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) {
    return getOtp();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) {
    return getOtp?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) {
    if (getOtp != null) {
      return getOtp();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) {
    return getOtp(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) {
    return getOtp?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) {
    if (getOtp != null) {
      return getOtp(this);
    }
    return orElse();
  }
}

abstract class _GetOtpEvent implements UserOtpEvent {
  const factory _GetOtpEvent() = _$GetOtpEventImpl;
}

/// @nodoc
abstract class _$$ClearOtpEventImplCopyWith<$Res> {
  factory _$$ClearOtpEventImplCopyWith(
          _$ClearOtpEventImpl value, $Res Function(_$ClearOtpEventImpl) then) =
      __$$ClearOtpEventImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ClearOtpEventImplCopyWithImpl<$Res>
    extends _$UserOtpEventCopyWithImpl<$Res, _$ClearOtpEventImpl>
    implements _$$ClearOtpEventImplCopyWith<$Res> {
  __$$ClearOtpEventImplCopyWithImpl(
      _$ClearOtpEventImpl _value, $Res Function(_$ClearOtpEventImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ClearOtpEventImpl implements _ClearOtpEvent {
  const _$ClearOtpEventImpl();

  @override
  String toString() {
    return 'UserOtpEvent.clearOtp()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$ClearOtpEventImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) {
    return clearOtp();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) {
    return clearOtp?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) {
    if (clearOtp != null) {
      return clearOtp();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) {
    return clearOtp(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) {
    return clearOtp?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) {
    if (clearOtp != null) {
      return clearOtp(this);
    }
    return orElse();
  }
}

abstract class _ClearOtpEvent implements UserOtpEvent {
  const factory _ClearOtpEvent() = _$ClearOtpEventImpl;
}

/// @nodoc
abstract class _$$StorePhoneEventImplCopyWith<$Res> {
  factory _$$StorePhoneEventImplCopyWith(_$StorePhoneEventImpl value,
          $Res Function(_$StorePhoneEventImpl) then) =
      __$$StorePhoneEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String phone});
}

/// @nodoc
class __$$StorePhoneEventImplCopyWithImpl<$Res>
    extends _$UserOtpEventCopyWithImpl<$Res, _$StorePhoneEventImpl>
    implements _$$StorePhoneEventImplCopyWith<$Res> {
  __$$StorePhoneEventImplCopyWithImpl(
      _$StorePhoneEventImpl _value, $Res Function(_$StorePhoneEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? phone = null,
  }) {
    return _then(_$StorePhoneEventImpl(
      phone: null == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$StorePhoneEventImpl implements _StorePhoneEvent {
  const _$StorePhoneEventImpl({required this.phone});

  @override
  final String phone;

  @override
  String toString() {
    return 'UserOtpEvent.storePhone(phone: $phone)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$StorePhoneEventImpl &&
            (identical(other.phone, phone) || other.phone == phone));
  }

  @override
  int get hashCode => Object.hash(runtimeType, phone);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$StorePhoneEventImplCopyWith<_$StorePhoneEventImpl> get copyWith =>
      __$$StorePhoneEventImplCopyWithImpl<_$StorePhoneEventImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) {
    return storePhone(phone);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) {
    return storePhone?.call(phone);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) {
    if (storePhone != null) {
      return storePhone(phone);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) {
    return storePhone(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) {
    return storePhone?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) {
    if (storePhone != null) {
      return storePhone(this);
    }
    return orElse();
  }
}

abstract class _StorePhoneEvent implements UserOtpEvent {
  const factory _StorePhoneEvent({required final String phone}) =
      _$StorePhoneEventImpl;

  String get phone;
  @JsonKey(ignore: true)
  _$$StorePhoneEventImplCopyWith<_$StorePhoneEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ResetPasswordEventImplCopyWith<$Res> {
  factory _$$ResetPasswordEventImplCopyWith(_$ResetPasswordEventImpl value,
          $Res Function(_$ResetPasswordEventImpl) then) =
      __$$ResetPasswordEventImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String newPassword});
}

/// @nodoc
class __$$ResetPasswordEventImplCopyWithImpl<$Res>
    extends _$UserOtpEventCopyWithImpl<$Res, _$ResetPasswordEventImpl>
    implements _$$ResetPasswordEventImplCopyWith<$Res> {
  __$$ResetPasswordEventImplCopyWithImpl(_$ResetPasswordEventImpl _value,
      $Res Function(_$ResetPasswordEventImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? newPassword = null,
  }) {
    return _then(_$ResetPasswordEventImpl(
      newPassword: null == newPassword
          ? _value.newPassword
          : newPassword // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ResetPasswordEventImpl implements _ResetPasswordEvent {
  const _$ResetPasswordEventImpl({required this.newPassword});

  @override
  final String newPassword;

  @override
  String toString() {
    return 'UserOtpEvent.resetPassword(newPassword: $newPassword)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ResetPasswordEventImpl &&
            (identical(other.newPassword, newPassword) ||
                other.newPassword == newPassword));
  }

  @override
  int get hashCode => Object.hash(runtimeType, newPassword);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ResetPasswordEventImplCopyWith<_$ResetPasswordEventImpl> get copyWith =>
      __$$ResetPasswordEventImplCopyWithImpl<_$ResetPasswordEventImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String phone) sendOtp,
    required TResult Function(String otp) storeOtp,
    required TResult Function() getOtp,
    required TResult Function() clearOtp,
    required TResult Function(String phone) storePhone,
    required TResult Function(String newPassword) resetPassword,
  }) {
    return resetPassword(newPassword);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String phone)? sendOtp,
    TResult? Function(String otp)? storeOtp,
    TResult? Function()? getOtp,
    TResult? Function()? clearOtp,
    TResult? Function(String phone)? storePhone,
    TResult? Function(String newPassword)? resetPassword,
  }) {
    return resetPassword?.call(newPassword);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String phone)? sendOtp,
    TResult Function(String otp)? storeOtp,
    TResult Function()? getOtp,
    TResult Function()? clearOtp,
    TResult Function(String phone)? storePhone,
    TResult Function(String newPassword)? resetPassword,
    required TResult orElse(),
  }) {
    if (resetPassword != null) {
      return resetPassword(newPassword);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SendOtpEvent value) sendOtp,
    required TResult Function(_StoreOtpEvent value) storeOtp,
    required TResult Function(_GetOtpEvent value) getOtp,
    required TResult Function(_ClearOtpEvent value) clearOtp,
    required TResult Function(_StorePhoneEvent value) storePhone,
    required TResult Function(_ResetPasswordEvent value) resetPassword,
  }) {
    return resetPassword(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SendOtpEvent value)? sendOtp,
    TResult? Function(_StoreOtpEvent value)? storeOtp,
    TResult? Function(_GetOtpEvent value)? getOtp,
    TResult? Function(_ClearOtpEvent value)? clearOtp,
    TResult? Function(_StorePhoneEvent value)? storePhone,
    TResult? Function(_ResetPasswordEvent value)? resetPassword,
  }) {
    return resetPassword?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SendOtpEvent value)? sendOtp,
    TResult Function(_StoreOtpEvent value)? storeOtp,
    TResult Function(_GetOtpEvent value)? getOtp,
    TResult Function(_ClearOtpEvent value)? clearOtp,
    TResult Function(_StorePhoneEvent value)? storePhone,
    TResult Function(_ResetPasswordEvent value)? resetPassword,
    required TResult orElse(),
  }) {
    if (resetPassword != null) {
      return resetPassword(this);
    }
    return orElse();
  }
}

abstract class _ResetPasswordEvent implements UserOtpEvent {
  const factory _ResetPasswordEvent({required final String newPassword}) =
      _$ResetPasswordEventImpl;

  String get newPassword;
  @JsonKey(ignore: true)
  _$$ResetPasswordEventImplCopyWith<_$ResetPasswordEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$UserOtpState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UserOtpStateCopyWith<$Res> {
  factory $UserOtpStateCopyWith(
          UserOtpState value, $Res Function(UserOtpState) then) =
      _$UserOtpStateCopyWithImpl<$Res, UserOtpState>;
}

/// @nodoc
class _$UserOtpStateCopyWithImpl<$Res, $Val extends UserOtpState>
    implements $UserOtpStateCopyWith<$Res> {
  _$UserOtpStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$InitialImplCopyWith<$Res> {
  factory _$$InitialImplCopyWith(
          _$InitialImpl value, $Res Function(_$InitialImpl) then) =
      __$$InitialImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$InitialImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$InitialImpl>
    implements _$$InitialImplCopyWith<$Res> {
  __$$InitialImplCopyWithImpl(
      _$InitialImpl _value, $Res Function(_$InitialImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$InitialImpl implements _Initial {
  const _$InitialImpl();

  @override
  String toString() {
    return 'UserOtpState.initial()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$InitialImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements UserOtpState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$LoadingImplCopyWith<$Res> {
  factory _$$LoadingImplCopyWith(
          _$LoadingImpl value, $Res Function(_$LoadingImpl) then) =
      __$$LoadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$LoadingImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$LoadingImpl>
    implements _$$LoadingImplCopyWith<$Res> {
  __$$LoadingImplCopyWithImpl(
      _$LoadingImpl _value, $Res Function(_$LoadingImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$LoadingImpl implements _Loading {
  const _$LoadingImpl();

  @override
  String toString() {
    return 'UserOtpState.loading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$LoadingImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _Loading implements UserOtpState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$SentImplCopyWith<$Res> {
  factory _$$SentImplCopyWith(
          _$SentImpl value, $Res Function(_$SentImpl) then) =
      __$$SentImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SentImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$SentImpl>
    implements _$$SentImplCopyWith<$Res> {
  __$$SentImplCopyWithImpl(_$SentImpl _value, $Res Function(_$SentImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$SentImpl implements _Sent {
  const _$SentImpl();

  @override
  String toString() {
    return 'UserOtpState.sent()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$SentImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return sent();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return sent?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (sent != null) {
      return sent();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return sent(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return sent?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (sent != null) {
      return sent(this);
    }
    return orElse();
  }
}

abstract class _Sent implements UserOtpState {
  const factory _Sent() = _$SentImpl;
}

/// @nodoc
abstract class _$$OtpStoredImplCopyWith<$Res> {
  factory _$$OtpStoredImplCopyWith(
          _$OtpStoredImpl value, $Res Function(_$OtpStoredImpl) then) =
      __$$OtpStoredImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String otp});
}

/// @nodoc
class __$$OtpStoredImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$OtpStoredImpl>
    implements _$$OtpStoredImplCopyWith<$Res> {
  __$$OtpStoredImplCopyWithImpl(
      _$OtpStoredImpl _value, $Res Function(_$OtpStoredImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? otp = null,
  }) {
    return _then(_$OtpStoredImpl(
      null == otp
          ? _value.otp
          : otp // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$OtpStoredImpl implements _OtpStored {
  const _$OtpStoredImpl(this.otp);

  @override
  final String otp;

  @override
  String toString() {
    return 'UserOtpState.otpStored(otp: $otp)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$OtpStoredImpl &&
            (identical(other.otp, otp) || other.otp == otp));
  }

  @override
  int get hashCode => Object.hash(runtimeType, otp);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$OtpStoredImplCopyWith<_$OtpStoredImpl> get copyWith =>
      __$$OtpStoredImplCopyWithImpl<_$OtpStoredImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return otpStored(otp);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return otpStored?.call(otp);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (otpStored != null) {
      return otpStored(otp);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return otpStored(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return otpStored?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (otpStored != null) {
      return otpStored(this);
    }
    return orElse();
  }
}

abstract class _OtpStored implements UserOtpState {
  const factory _OtpStored(final String otp) = _$OtpStoredImpl;

  String get otp;
  @JsonKey(ignore: true)
  _$$OtpStoredImplCopyWith<_$OtpStoredImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$SuccessImplCopyWith<$Res> {
  factory _$$SuccessImplCopyWith(
          _$SuccessImpl value, $Res Function(_$SuccessImpl) then) =
      __$$SuccessImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SuccessImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$SuccessImpl>
    implements _$$SuccessImplCopyWith<$Res> {
  __$$SuccessImplCopyWithImpl(
      _$SuccessImpl _value, $Res Function(_$SuccessImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$SuccessImpl implements _Success {
  const _$SuccessImpl();

  @override
  String toString() {
    return 'UserOtpState.success()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$SuccessImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return success();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return success?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return success(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return success?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(this);
    }
    return orElse();
  }
}

abstract class _Success implements UserOtpState {
  const factory _Success() = _$SuccessImpl;
}

/// @nodoc
abstract class _$$ErrorImplCopyWith<$Res> {
  factory _$$ErrorImplCopyWith(
          _$ErrorImpl value, $Res Function(_$ErrorImpl) then) =
      __$$ErrorImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String message});
}

/// @nodoc
class __$$ErrorImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$ErrorImpl>
    implements _$$ErrorImplCopyWith<$Res> {
  __$$ErrorImplCopyWithImpl(
      _$ErrorImpl _value, $Res Function(_$ErrorImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? message = null,
  }) {
    return _then(_$ErrorImpl(
      null == message
          ? _value.message
          : message // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ErrorImpl implements _Error {
  const _$ErrorImpl(this.message);

  @override
  final String message;

  @override
  String toString() {
    return 'UserOtpState.error(message: $message)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ErrorImpl &&
            (identical(other.message, message) || other.message == message));
  }

  @override
  int get hashCode => Object.hash(runtimeType, message);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      __$$ErrorImplCopyWithImpl<_$ErrorImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return error(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return error?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (error != null) {
      return error(message);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return error(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return error?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (error != null) {
      return error(this);
    }
    return orElse();
  }
}

abstract class _Error implements UserOtpState {
  const factory _Error(final String message) = _$ErrorImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$PhoneStoredImplCopyWith<$Res> {
  factory _$$PhoneStoredImplCopyWith(
          _$PhoneStoredImpl value, $Res Function(_$PhoneStoredImpl) then) =
      __$$PhoneStoredImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String phone});
}

/// @nodoc
class __$$PhoneStoredImplCopyWithImpl<$Res>
    extends _$UserOtpStateCopyWithImpl<$Res, _$PhoneStoredImpl>
    implements _$$PhoneStoredImplCopyWith<$Res> {
  __$$PhoneStoredImplCopyWithImpl(
      _$PhoneStoredImpl _value, $Res Function(_$PhoneStoredImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? phone = null,
  }) {
    return _then(_$PhoneStoredImpl(
      null == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$PhoneStoredImpl implements _PhoneStored {
  const _$PhoneStoredImpl(this.phone);

  @override
  final String phone;

  @override
  String toString() {
    return 'UserOtpState.phoneStored(phone: $phone)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$PhoneStoredImpl &&
            (identical(other.phone, phone) || other.phone == phone));
  }

  @override
  int get hashCode => Object.hash(runtimeType, phone);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$PhoneStoredImplCopyWith<_$PhoneStoredImpl> get copyWith =>
      __$$PhoneStoredImplCopyWithImpl<_$PhoneStoredImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() sent,
    required TResult Function(String otp) otpStored,
    required TResult Function() success,
    required TResult Function(String message) error,
    required TResult Function(String phone) phoneStored,
  }) {
    return phoneStored(phone);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? sent,
    TResult? Function(String otp)? otpStored,
    TResult? Function()? success,
    TResult? Function(String message)? error,
    TResult? Function(String phone)? phoneStored,
  }) {
    return phoneStored?.call(phone);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? sent,
    TResult Function(String otp)? otpStored,
    TResult Function()? success,
    TResult Function(String message)? error,
    TResult Function(String phone)? phoneStored,
    required TResult orElse(),
  }) {
    if (phoneStored != null) {
      return phoneStored(phone);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Sent value) sent,
    required TResult Function(_OtpStored value) otpStored,
    required TResult Function(_Success value) success,
    required TResult Function(_Error value) error,
    required TResult Function(_PhoneStored value) phoneStored,
  }) {
    return phoneStored(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Sent value)? sent,
    TResult? Function(_OtpStored value)? otpStored,
    TResult? Function(_Success value)? success,
    TResult? Function(_Error value)? error,
    TResult? Function(_PhoneStored value)? phoneStored,
  }) {
    return phoneStored?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Sent value)? sent,
    TResult Function(_OtpStored value)? otpStored,
    TResult Function(_Success value)? success,
    TResult Function(_Error value)? error,
    TResult Function(_PhoneStored value)? phoneStored,
    required TResult orElse(),
  }) {
    if (phoneStored != null) {
      return phoneStored(this);
    }
    return orElse();
  }
}

abstract class _PhoneStored implements UserOtpState {
  const factory _PhoneStored(final String phone) = _$PhoneStoredImpl;

  String get phone;
  @JsonKey(ignore: true)
  _$$PhoneStoredImplCopyWith<_$PhoneStoredImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
