// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'scheduled_visit_submission.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$ScheduleVisitSubmitEvent {
  String get scheduledVisitId => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) submit,
    required TResult Function(String scheduledVisitId) bgDone,
    required TResult Function(String scheduledVisitId, String? message) bgError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? submit,
    TResult? Function(String scheduledVisitId)? bgDone,
    TResult? Function(String scheduledVisitId, String? message)? bgError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? submit,
    TResult Function(String scheduledVisitId)? bgDone,
    TResult Function(String scheduledVisitId, String? message)? bgError,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_BgDone value) bgDone,
    required TResult Function(_BgError value) bgError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_BgDone value)? bgDone,
    TResult? Function(_BgError value)? bgError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_BgDone value)? bgDone,
    TResult Function(_BgError value)? bgError,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ScheduleVisitSubmitEventCopyWith<ScheduleVisitSubmitEvent> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduleVisitSubmitEventCopyWith<$Res> {
  factory $ScheduleVisitSubmitEventCopyWith(ScheduleVisitSubmitEvent value,
          $Res Function(ScheduleVisitSubmitEvent) then) =
      _$ScheduleVisitSubmitEventCopyWithImpl<$Res, ScheduleVisitSubmitEvent>;
  @useResult
  $Res call({String scheduledVisitId});
}

/// @nodoc
class _$ScheduleVisitSubmitEventCopyWithImpl<$Res,
        $Val extends ScheduleVisitSubmitEvent>
    implements $ScheduleVisitSubmitEventCopyWith<$Res> {
  _$ScheduleVisitSubmitEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisitId = null,
  }) {
    return _then(_value.copyWith(
      scheduledVisitId: null == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SubmitImplCopyWith<$Res>
    implements $ScheduleVisitSubmitEventCopyWith<$Res> {
  factory _$$SubmitImplCopyWith(
          _$SubmitImpl value, $Res Function(_$SubmitImpl) then) =
      __$$SubmitImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String scheduledVisitId, String userType});
}

/// @nodoc
class __$$SubmitImplCopyWithImpl<$Res>
    extends _$ScheduleVisitSubmitEventCopyWithImpl<$Res, _$SubmitImpl>
    implements _$$SubmitImplCopyWith<$Res> {
  __$$SubmitImplCopyWithImpl(
      _$SubmitImpl _value, $Res Function(_$SubmitImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisitId = null,
    Object? userType = null,
  }) {
    return _then(_$SubmitImpl(
      scheduledVisitId: null == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SubmitImpl implements _Submit {
  const _$SubmitImpl({required this.scheduledVisitId, required this.userType});

  @override
  final String scheduledVisitId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ScheduleVisitSubmitEvent.submit(scheduledVisitId: $scheduledVisitId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitImpl &&
            (identical(other.scheduledVisitId, scheduledVisitId) ||
                other.scheduledVisitId == scheduledVisitId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisitId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitImplCopyWith<_$SubmitImpl> get copyWith =>
      __$$SubmitImplCopyWithImpl<_$SubmitImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) submit,
    required TResult Function(String scheduledVisitId) bgDone,
    required TResult Function(String scheduledVisitId, String? message) bgError,
  }) {
    return submit(scheduledVisitId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? submit,
    TResult? Function(String scheduledVisitId)? bgDone,
    TResult? Function(String scheduledVisitId, String? message)? bgError,
  }) {
    return submit?.call(scheduledVisitId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? submit,
    TResult Function(String scheduledVisitId)? bgDone,
    TResult Function(String scheduledVisitId, String? message)? bgError,
    required TResult orElse(),
  }) {
    if (submit != null) {
      return submit(scheduledVisitId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_BgDone value) bgDone,
    required TResult Function(_BgError value) bgError,
  }) {
    return submit(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_BgDone value)? bgDone,
    TResult? Function(_BgError value)? bgError,
  }) {
    return submit?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_BgDone value)? bgDone,
    TResult Function(_BgError value)? bgError,
    required TResult orElse(),
  }) {
    if (submit != null) {
      return submit(this);
    }
    return orElse();
  }
}

abstract class _Submit implements ScheduleVisitSubmitEvent {
  const factory _Submit(
      {required final String scheduledVisitId,
      required final String userType}) = _$SubmitImpl;

  @override
  String get scheduledVisitId;
  String get userType;
  @override
  @JsonKey(ignore: true)
  _$$SubmitImplCopyWith<_$SubmitImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$BgDoneImplCopyWith<$Res>
    implements $ScheduleVisitSubmitEventCopyWith<$Res> {
  factory _$$BgDoneImplCopyWith(
          _$BgDoneImpl value, $Res Function(_$BgDoneImpl) then) =
      __$$BgDoneImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String scheduledVisitId});
}

/// @nodoc
class __$$BgDoneImplCopyWithImpl<$Res>
    extends _$ScheduleVisitSubmitEventCopyWithImpl<$Res, _$BgDoneImpl>
    implements _$$BgDoneImplCopyWith<$Res> {
  __$$BgDoneImplCopyWithImpl(
      _$BgDoneImpl _value, $Res Function(_$BgDoneImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisitId = null,
  }) {
    return _then(_$BgDoneImpl(
      scheduledVisitId: null == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$BgDoneImpl implements _BgDone {
  const _$BgDoneImpl({required this.scheduledVisitId});

  @override
  final String scheduledVisitId;

  @override
  String toString() {
    return 'ScheduleVisitSubmitEvent.bgDone(scheduledVisitId: $scheduledVisitId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BgDoneImpl &&
            (identical(other.scheduledVisitId, scheduledVisitId) ||
                other.scheduledVisitId == scheduledVisitId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisitId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BgDoneImplCopyWith<_$BgDoneImpl> get copyWith =>
      __$$BgDoneImplCopyWithImpl<_$BgDoneImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) submit,
    required TResult Function(String scheduledVisitId) bgDone,
    required TResult Function(String scheduledVisitId, String? message) bgError,
  }) {
    return bgDone(scheduledVisitId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? submit,
    TResult? Function(String scheduledVisitId)? bgDone,
    TResult? Function(String scheduledVisitId, String? message)? bgError,
  }) {
    return bgDone?.call(scheduledVisitId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? submit,
    TResult Function(String scheduledVisitId)? bgDone,
    TResult Function(String scheduledVisitId, String? message)? bgError,
    required TResult orElse(),
  }) {
    if (bgDone != null) {
      return bgDone(scheduledVisitId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_BgDone value) bgDone,
    required TResult Function(_BgError value) bgError,
  }) {
    return bgDone(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_BgDone value)? bgDone,
    TResult? Function(_BgError value)? bgError,
  }) {
    return bgDone?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_BgDone value)? bgDone,
    TResult Function(_BgError value)? bgError,
    required TResult orElse(),
  }) {
    if (bgDone != null) {
      return bgDone(this);
    }
    return orElse();
  }
}

abstract class _BgDone implements ScheduleVisitSubmitEvent {
  const factory _BgDone({required final String scheduledVisitId}) =
      _$BgDoneImpl;

  @override
  String get scheduledVisitId;
  @override
  @JsonKey(ignore: true)
  _$$BgDoneImplCopyWith<_$BgDoneImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$BgErrorImplCopyWith<$Res>
    implements $ScheduleVisitSubmitEventCopyWith<$Res> {
  factory _$$BgErrorImplCopyWith(
          _$BgErrorImpl value, $Res Function(_$BgErrorImpl) then) =
      __$$BgErrorImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String scheduledVisitId, String? message});
}

/// @nodoc
class __$$BgErrorImplCopyWithImpl<$Res>
    extends _$ScheduleVisitSubmitEventCopyWithImpl<$Res, _$BgErrorImpl>
    implements _$$BgErrorImplCopyWith<$Res> {
  __$$BgErrorImplCopyWithImpl(
      _$BgErrorImpl _value, $Res Function(_$BgErrorImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisitId = null,
    Object? message = freezed,
  }) {
    return _then(_$BgErrorImpl(
      scheduledVisitId: null == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String,
      message: freezed == message
          ? _value.message
          : message // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc

class _$BgErrorImpl implements _BgError {
  const _$BgErrorImpl({required this.scheduledVisitId, this.message});

  @override
  final String scheduledVisitId;
  @override
  final String? message;

  @override
  String toString() {
    return 'ScheduleVisitSubmitEvent.bgError(scheduledVisitId: $scheduledVisitId, message: $message)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BgErrorImpl &&
            (identical(other.scheduledVisitId, scheduledVisitId) ||
                other.scheduledVisitId == scheduledVisitId) &&
            (identical(other.message, message) || other.message == message));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisitId, message);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BgErrorImplCopyWith<_$BgErrorImpl> get copyWith =>
      __$$BgErrorImplCopyWithImpl<_$BgErrorImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) submit,
    required TResult Function(String scheduledVisitId) bgDone,
    required TResult Function(String scheduledVisitId, String? message) bgError,
  }) {
    return bgError(scheduledVisitId, message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? submit,
    TResult? Function(String scheduledVisitId)? bgDone,
    TResult? Function(String scheduledVisitId, String? message)? bgError,
  }) {
    return bgError?.call(scheduledVisitId, message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? submit,
    TResult Function(String scheduledVisitId)? bgDone,
    TResult Function(String scheduledVisitId, String? message)? bgError,
    required TResult orElse(),
  }) {
    if (bgError != null) {
      return bgError(scheduledVisitId, message);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_BgDone value) bgDone,
    required TResult Function(_BgError value) bgError,
  }) {
    return bgError(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_BgDone value)? bgDone,
    TResult? Function(_BgError value)? bgError,
  }) {
    return bgError?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_BgDone value)? bgDone,
    TResult Function(_BgError value)? bgError,
    required TResult orElse(),
  }) {
    if (bgError != null) {
      return bgError(this);
    }
    return orElse();
  }
}

abstract class _BgError implements ScheduleVisitSubmitEvent {
  const factory _BgError(
      {required final String scheduledVisitId,
      final String? message}) = _$BgErrorImpl;

  @override
  String get scheduledVisitId;
  String? get message;
  @override
  @JsonKey(ignore: true)
  _$$BgErrorImplCopyWith<_$BgErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ScheduleVisitSubmitState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String message) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String message)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ScheduleVisitSubmitStateCopyWith<$Res> {
  factory $ScheduleVisitSubmitStateCopyWith(ScheduleVisitSubmitState value,
          $Res Function(ScheduleVisitSubmitState) then) =
      _$ScheduleVisitSubmitStateCopyWithImpl<$Res, ScheduleVisitSubmitState>;
}

/// @nodoc
class _$ScheduleVisitSubmitStateCopyWithImpl<$Res,
        $Val extends ScheduleVisitSubmitState>
    implements $ScheduleVisitSubmitStateCopyWith<$Res> {
  _$ScheduleVisitSubmitStateCopyWithImpl(this._value, this._then);

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
    extends _$ScheduleVisitSubmitStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'ScheduleVisitSubmitState.initial()';
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
    required TResult Function() success,
    required TResult Function(String message) failure,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String message)? failure,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
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
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements ScheduleVisitSubmitState {
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
    extends _$ScheduleVisitSubmitStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'ScheduleVisitSubmitState.loading()';
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
    required TResult Function() success,
    required TResult Function(String message) failure,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String message)? failure,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String message)? failure,
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
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _Loading implements ScheduleVisitSubmitState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$SuccessImplCopyWith<$Res> {
  factory _$$SuccessImplCopyWith(
          _$SuccessImpl value, $Res Function(_$SuccessImpl) then) =
      __$$SuccessImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SuccessImplCopyWithImpl<$Res>
    extends _$ScheduleVisitSubmitStateCopyWithImpl<$Res, _$SuccessImpl>
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
    return 'ScheduleVisitSubmitState.success()';
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
    required TResult Function() success,
    required TResult Function(String message) failure,
  }) {
    return success();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String message)? failure,
  }) {
    return success?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String message)? failure,
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
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
  }) {
    return success(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
  }) {
    return success?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(this);
    }
    return orElse();
  }
}

abstract class _Success implements ScheduleVisitSubmitState {
  const factory _Success() = _$SuccessImpl;
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
    extends _$ScheduleVisitSubmitStateCopyWithImpl<$Res, _$FailureImpl>
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
    return 'ScheduleVisitSubmitState.failure(message: $message)';
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
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String message) failure,
  }) {
    return failure(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String message)? failure,
  }) {
    return failure?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
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
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
  }) {
    return failure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
  }) {
    return failure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements ScheduleVisitSubmitState {
  const factory _Failure(final String message) = _$FailureImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
