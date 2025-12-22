// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'amc_otp.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$AmcOtpEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() resend,
    required TResult Function(String visitId, String schemaCode, int version,
            String otp, ScheduledVisit? scheduledVisit)
        submit,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? resend,
    TResult? Function(String visitId, String schemaCode, int version,
            String otp, ScheduledVisit? scheduledVisit)?
        submit,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? resend,
    TResult Function(String visitId, String schemaCode, int version, String otp,
            ScheduledVisit? scheduledVisit)?
        submit,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(AmcOtpEventResend value) resend,
    required TResult Function(AmcOtpEventSubmit value) submit,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(AmcOtpEventResend value)? resend,
    TResult? Function(AmcOtpEventSubmit value)? submit,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(AmcOtpEventResend value)? resend,
    TResult Function(AmcOtpEventSubmit value)? submit,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AmcOtpEventCopyWith<$Res> {
  factory $AmcOtpEventCopyWith(
          AmcOtpEvent value, $Res Function(AmcOtpEvent) then) =
      _$AmcOtpEventCopyWithImpl<$Res, AmcOtpEvent>;
}

/// @nodoc
class _$AmcOtpEventCopyWithImpl<$Res, $Val extends AmcOtpEvent>
    implements $AmcOtpEventCopyWith<$Res> {
  _$AmcOtpEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$AmcOtpEventResendImplCopyWith<$Res> {
  factory _$$AmcOtpEventResendImplCopyWith(_$AmcOtpEventResendImpl value,
          $Res Function(_$AmcOtpEventResendImpl) then) =
      __$$AmcOtpEventResendImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$AmcOtpEventResendImplCopyWithImpl<$Res>
    extends _$AmcOtpEventCopyWithImpl<$Res, _$AmcOtpEventResendImpl>
    implements _$$AmcOtpEventResendImplCopyWith<$Res> {
  __$$AmcOtpEventResendImplCopyWithImpl(_$AmcOtpEventResendImpl _value,
      $Res Function(_$AmcOtpEventResendImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$AmcOtpEventResendImpl implements AmcOtpEventResend {
  const _$AmcOtpEventResendImpl();

  @override
  String toString() {
    return 'AmcOtpEvent.resend()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$AmcOtpEventResendImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() resend,
    required TResult Function(String visitId, String schemaCode, int version,
            String otp, ScheduledVisit? scheduledVisit)
        submit,
  }) {
    return resend();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? resend,
    TResult? Function(String visitId, String schemaCode, int version,
            String otp, ScheduledVisit? scheduledVisit)?
        submit,
  }) {
    return resend?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? resend,
    TResult Function(String visitId, String schemaCode, int version, String otp,
            ScheduledVisit? scheduledVisit)?
        submit,
    required TResult orElse(),
  }) {
    if (resend != null) {
      return resend();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(AmcOtpEventResend value) resend,
    required TResult Function(AmcOtpEventSubmit value) submit,
  }) {
    return resend(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(AmcOtpEventResend value)? resend,
    TResult? Function(AmcOtpEventSubmit value)? submit,
  }) {
    return resend?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(AmcOtpEventResend value)? resend,
    TResult Function(AmcOtpEventSubmit value)? submit,
    required TResult orElse(),
  }) {
    if (resend != null) {
      return resend(this);
    }
    return orElse();
  }
}

abstract class AmcOtpEventResend implements AmcOtpEvent {
  const factory AmcOtpEventResend() = _$AmcOtpEventResendImpl;
}

/// @nodoc
abstract class _$$AmcOtpEventSubmitImplCopyWith<$Res> {
  factory _$$AmcOtpEventSubmitImplCopyWith(_$AmcOtpEventSubmitImpl value,
          $Res Function(_$AmcOtpEventSubmitImpl) then) =
      __$$AmcOtpEventSubmitImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {String visitId,
      String schemaCode,
      int version,
      String otp,
      ScheduledVisit? scheduledVisit});

  $ScheduledVisitCopyWith<$Res>? get scheduledVisit;
}

/// @nodoc
class __$$AmcOtpEventSubmitImplCopyWithImpl<$Res>
    extends _$AmcOtpEventCopyWithImpl<$Res, _$AmcOtpEventSubmitImpl>
    implements _$$AmcOtpEventSubmitImplCopyWith<$Res> {
  __$$AmcOtpEventSubmitImplCopyWithImpl(_$AmcOtpEventSubmitImpl _value,
      $Res Function(_$AmcOtpEventSubmitImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? visitId = null,
    Object? schemaCode = null,
    Object? version = null,
    Object? otp = null,
    Object? scheduledVisit = freezed,
  }) {
    return _then(_$AmcOtpEventSubmitImpl(
      visitId: null == visitId
          ? _value.visitId
          : visitId // ignore: cast_nullable_to_non_nullable
              as String,
      schemaCode: null == schemaCode
          ? _value.schemaCode
          : schemaCode // ignore: cast_nullable_to_non_nullable
              as String,
      version: null == version
          ? _value.version
          : version // ignore: cast_nullable_to_non_nullable
              as int,
      otp: null == otp
          ? _value.otp
          : otp // ignore: cast_nullable_to_non_nullable
              as String,
      scheduledVisit: freezed == scheduledVisit
          ? _value.scheduledVisit
          : scheduledVisit // ignore: cast_nullable_to_non_nullable
              as ScheduledVisit?,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ScheduledVisitCopyWith<$Res>? get scheduledVisit {
    if (_value.scheduledVisit == null) {
      return null;
    }

    return $ScheduledVisitCopyWith<$Res>(_value.scheduledVisit!, (value) {
      return _then(_value.copyWith(scheduledVisit: value));
    });
  }
}

/// @nodoc

class _$AmcOtpEventSubmitImpl implements AmcOtpEventSubmit {
  const _$AmcOtpEventSubmitImpl(
      {required this.visitId,
      required this.schemaCode,
      required this.version,
      required this.otp,
      this.scheduledVisit});

  @override
  final String visitId;
  @override
  final String schemaCode;
  @override
  final int version;
  @override
  final String otp;
  @override
  final ScheduledVisit? scheduledVisit;

  @override
  String toString() {
    return 'AmcOtpEvent.submit(visitId: $visitId, schemaCode: $schemaCode, version: $version, otp: $otp, scheduledVisit: $scheduledVisit)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AmcOtpEventSubmitImpl &&
            (identical(other.visitId, visitId) || other.visitId == visitId) &&
            (identical(other.schemaCode, schemaCode) ||
                other.schemaCode == schemaCode) &&
            (identical(other.version, version) || other.version == version) &&
            (identical(other.otp, otp) || other.otp == otp) &&
            (identical(other.scheduledVisit, scheduledVisit) ||
                other.scheduledVisit == scheduledVisit));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, visitId, schemaCode, version, otp, scheduledVisit);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AmcOtpEventSubmitImplCopyWith<_$AmcOtpEventSubmitImpl> get copyWith =>
      __$$AmcOtpEventSubmitImplCopyWithImpl<_$AmcOtpEventSubmitImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() resend,
    required TResult Function(String visitId, String schemaCode, int version,
            String otp, ScheduledVisit? scheduledVisit)
        submit,
  }) {
    return submit(visitId, schemaCode, version, otp, scheduledVisit);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? resend,
    TResult? Function(String visitId, String schemaCode, int version,
            String otp, ScheduledVisit? scheduledVisit)?
        submit,
  }) {
    return submit?.call(visitId, schemaCode, version, otp, scheduledVisit);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? resend,
    TResult Function(String visitId, String schemaCode, int version, String otp,
            ScheduledVisit? scheduledVisit)?
        submit,
    required TResult orElse(),
  }) {
    if (submit != null) {
      return submit(visitId, schemaCode, version, otp, scheduledVisit);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(AmcOtpEventResend value) resend,
    required TResult Function(AmcOtpEventSubmit value) submit,
  }) {
    return submit(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(AmcOtpEventResend value)? resend,
    TResult? Function(AmcOtpEventSubmit value)? submit,
  }) {
    return submit?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(AmcOtpEventResend value)? resend,
    TResult Function(AmcOtpEventSubmit value)? submit,
    required TResult orElse(),
  }) {
    if (submit != null) {
      return submit(this);
    }
    return orElse();
  }
}

abstract class AmcOtpEventSubmit implements AmcOtpEvent {
  const factory AmcOtpEventSubmit(
      {required final String visitId,
      required final String schemaCode,
      required final int version,
      required final String otp,
      final ScheduledVisit? scheduledVisit}) = _$AmcOtpEventSubmitImpl;

  String get visitId;
  String get schemaCode;
  int get version;
  String get otp;
  ScheduledVisit? get scheduledVisit;
  @JsonKey(ignore: true)
  _$$AmcOtpEventSubmitImplCopyWith<_$AmcOtpEventSubmitImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$AmcOtpState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AmcOtpStateCopyWith<$Res> {
  factory $AmcOtpStateCopyWith(
          AmcOtpState value, $Res Function(AmcOtpState) then) =
      _$AmcOtpStateCopyWithImpl<$Res, AmcOtpState>;
}

/// @nodoc
class _$AmcOtpStateCopyWithImpl<$Res, $Val extends AmcOtpState>
    implements $AmcOtpStateCopyWith<$Res> {
  _$AmcOtpStateCopyWithImpl(this._value, this._then);

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
    extends _$AmcOtpStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'AmcOtpState.initial()';
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
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
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
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements AmcOtpState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$ResendLoadingImplCopyWith<$Res> {
  factory _$$ResendLoadingImplCopyWith(
          _$ResendLoadingImpl value, $Res Function(_$ResendLoadingImpl) then) =
      __$$ResendLoadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ResendLoadingImplCopyWithImpl<$Res>
    extends _$AmcOtpStateCopyWithImpl<$Res, _$ResendLoadingImpl>
    implements _$$ResendLoadingImplCopyWith<$Res> {
  __$$ResendLoadingImplCopyWithImpl(
      _$ResendLoadingImpl _value, $Res Function(_$ResendLoadingImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ResendLoadingImpl implements _ResendLoading {
  const _$ResendLoadingImpl();

  @override
  String toString() {
    return 'AmcOtpState.resendLoading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$ResendLoadingImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) {
    return resendLoading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) {
    return resendLoading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (resendLoading != null) {
      return resendLoading();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) {
    return resendLoading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) {
    return resendLoading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (resendLoading != null) {
      return resendLoading(this);
    }
    return orElse();
  }
}

abstract class _ResendLoading implements AmcOtpState {
  const factory _ResendLoading() = _$ResendLoadingImpl;
}

/// @nodoc
abstract class _$$ResendSuccessImplCopyWith<$Res> {
  factory _$$ResendSuccessImplCopyWith(
          _$ResendSuccessImpl value, $Res Function(_$ResendSuccessImpl) then) =
      __$$ResendSuccessImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ResendSuccessImplCopyWithImpl<$Res>
    extends _$AmcOtpStateCopyWithImpl<$Res, _$ResendSuccessImpl>
    implements _$$ResendSuccessImplCopyWith<$Res> {
  __$$ResendSuccessImplCopyWithImpl(
      _$ResendSuccessImpl _value, $Res Function(_$ResendSuccessImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ResendSuccessImpl implements _ResendSuccess {
  const _$ResendSuccessImpl();

  @override
  String toString() {
    return 'AmcOtpState.resendSuccess()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$ResendSuccessImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) {
    return resendSuccess();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) {
    return resendSuccess?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (resendSuccess != null) {
      return resendSuccess();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) {
    return resendSuccess(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) {
    return resendSuccess?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (resendSuccess != null) {
      return resendSuccess(this);
    }
    return orElse();
  }
}

abstract class _ResendSuccess implements AmcOtpState {
  const factory _ResendSuccess() = _$ResendSuccessImpl;
}

/// @nodoc
abstract class _$$SubmitLoadingImplCopyWith<$Res> {
  factory _$$SubmitLoadingImplCopyWith(
          _$SubmitLoadingImpl value, $Res Function(_$SubmitLoadingImpl) then) =
      __$$SubmitLoadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SubmitLoadingImplCopyWithImpl<$Res>
    extends _$AmcOtpStateCopyWithImpl<$Res, _$SubmitLoadingImpl>
    implements _$$SubmitLoadingImplCopyWith<$Res> {
  __$$SubmitLoadingImplCopyWithImpl(
      _$SubmitLoadingImpl _value, $Res Function(_$SubmitLoadingImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$SubmitLoadingImpl implements _SubmitLoading {
  const _$SubmitLoadingImpl();

  @override
  String toString() {
    return 'AmcOtpState.submitLoading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$SubmitLoadingImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) {
    return submitLoading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) {
    return submitLoading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (submitLoading != null) {
      return submitLoading();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) {
    return submitLoading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) {
    return submitLoading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (submitLoading != null) {
      return submitLoading(this);
    }
    return orElse();
  }
}

abstract class _SubmitLoading implements AmcOtpState {
  const factory _SubmitLoading() = _$SubmitLoadingImpl;
}

/// @nodoc
abstract class _$$SubmitSuccessImplCopyWith<$Res> {
  factory _$$SubmitSuccessImplCopyWith(
          _$SubmitSuccessImpl value, $Res Function(_$SubmitSuccessImpl) then) =
      __$$SubmitSuccessImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SubmitSuccessImplCopyWithImpl<$Res>
    extends _$AmcOtpStateCopyWithImpl<$Res, _$SubmitSuccessImpl>
    implements _$$SubmitSuccessImplCopyWith<$Res> {
  __$$SubmitSuccessImplCopyWithImpl(
      _$SubmitSuccessImpl _value, $Res Function(_$SubmitSuccessImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$SubmitSuccessImpl implements _SubmitSuccess {
  const _$SubmitSuccessImpl();

  @override
  String toString() {
    return 'AmcOtpState.submitSuccess()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$SubmitSuccessImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) {
    return submitSuccess();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) {
    return submitSuccess?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (submitSuccess != null) {
      return submitSuccess();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) {
    return submitSuccess(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) {
    return submitSuccess?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (submitSuccess != null) {
      return submitSuccess(this);
    }
    return orElse();
  }
}

abstract class _SubmitSuccess implements AmcOtpState {
  const factory _SubmitSuccess() = _$SubmitSuccessImpl;
}

/// @nodoc
abstract class _$$FailureImplCopyWith<$Res> {
  factory _$$FailureImplCopyWith(
          _$FailureImpl value, $Res Function(_$FailureImpl) then) =
      __$$FailureImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String message});
}

/// @nodoc
class __$$FailureImplCopyWithImpl<$Res>
    extends _$AmcOtpStateCopyWithImpl<$Res, _$FailureImpl>
    implements _$$FailureImplCopyWith<$Res> {
  __$$FailureImplCopyWithImpl(
      _$FailureImpl _value, $Res Function(_$FailureImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? message = null,
  }) {
    return _then(_$FailureImpl(
      null == message
          ? _value.message
          : message // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$FailureImpl implements _Failure {
  const _$FailureImpl(this.message);

  @override
  final String message;

  @override
  String toString() {
    return 'AmcOtpState.failure(message: $message)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FailureImpl &&
            (identical(other.message, message) || other.message == message));
  }

  @override
  int get hashCode => Object.hash(runtimeType, message);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      __$$FailureImplCopyWithImpl<_$FailureImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() resendLoading,
    required TResult Function() resendSuccess,
    required TResult Function() submitLoading,
    required TResult Function() submitSuccess,
    required TResult Function(String message) failure,
  }) {
    return failure(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? resendLoading,
    TResult? Function()? resendSuccess,
    TResult? Function()? submitLoading,
    TResult? Function()? submitSuccess,
    TResult? Function(String message)? failure,
  }) {
    return failure?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? resendLoading,
    TResult Function()? resendSuccess,
    TResult Function()? submitLoading,
    TResult Function()? submitSuccess,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(message);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_ResendLoading value) resendLoading,
    required TResult Function(_ResendSuccess value) resendSuccess,
    required TResult Function(_SubmitLoading value) submitLoading,
    required TResult Function(_SubmitSuccess value) submitSuccess,
    required TResult Function(_Failure value) failure,
  }) {
    return failure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_ResendLoading value)? resendLoading,
    TResult? Function(_ResendSuccess value)? resendSuccess,
    TResult? Function(_SubmitLoading value)? submitLoading,
    TResult? Function(_SubmitSuccess value)? submitSuccess,
    TResult? Function(_Failure value)? failure,
  }) {
    return failure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_ResendLoading value)? resendLoading,
    TResult Function(_ResendSuccess value)? resendSuccess,
    TResult Function(_SubmitLoading value)? submitLoading,
    TResult Function(_SubmitSuccess value)? submitSuccess,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements AmcOtpState {
  const factory _Failure(final String message) = _$FailureImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
