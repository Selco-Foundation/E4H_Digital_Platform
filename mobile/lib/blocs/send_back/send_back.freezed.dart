// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'send_back.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SendBackEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String activityFacilityId, String userType, bool isRetry)
        submit,
    required TResult Function(String activityFacilityId, String userType) retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult? Function(String activityFacilityId, String userType)? retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult Function(String activityFacilityId, String userType)? retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SendBackEventCopyWith<$Res> {
  factory $SendBackEventCopyWith(
          SendBackEvent value, $Res Function(SendBackEvent) then) =
      _$SendBackEventCopyWithImpl<$Res, SendBackEvent>;
}

/// @nodoc
class _$SendBackEventCopyWithImpl<$Res, $Val extends SendBackEvent>
    implements $SendBackEventCopyWith<$Res> {
  _$SendBackEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$SubmitImplCopyWith<$Res> {
  factory _$$SubmitImplCopyWith(
          _$SubmitImpl value, $Res Function(_$SubmitImpl) then) =
      __$$SubmitImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String userType, bool isRetry});
}

/// @nodoc
class __$$SubmitImplCopyWithImpl<$Res>
    extends _$SendBackEventCopyWithImpl<$Res, _$SubmitImpl>
    implements _$$SubmitImplCopyWith<$Res> {
  __$$SubmitImplCopyWithImpl(
      _$SubmitImpl _value, $Res Function(_$SubmitImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? userType = null,
    Object? isRetry = null,
  }) {
    return _then(_$SubmitImpl(
      activityFacilityId: null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
      isRetry: null == isRetry
          ? _value.isRetry
          : isRetry // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$SubmitImpl implements _Submit {
  const _$SubmitImpl(
      {required this.activityFacilityId,
      required this.userType,
      this.isRetry = false});

  @override
  final String activityFacilityId;
  @override
  final String userType;
  @override
  @JsonKey()
  final bool isRetry;

  @override
  String toString() {
    return 'SendBackEvent.submit(activityFacilityId: $activityFacilityId, userType: $userType, isRetry: $isRetry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType) &&
            (identical(other.isRetry, isRetry) || other.isRetry == isRetry));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, activityFacilityId, userType, isRetry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitImplCopyWith<_$SubmitImpl> get copyWith =>
      __$$SubmitImplCopyWithImpl<_$SubmitImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String activityFacilityId, String userType, bool isRetry)
        submit,
    required TResult Function(String activityFacilityId, String userType) retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return submit(activityFacilityId, userType, isRetry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult? Function(String activityFacilityId, String userType)? retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return submit?.call(activityFacilityId, userType, isRetry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult Function(String activityFacilityId, String userType)? retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (submit != null) {
      return submit(activityFacilityId, userType, isRetry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return submit(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return submit?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (submit != null) {
      return submit(this);
    }
    return orElse();
  }
}

abstract class _Submit implements SendBackEvent {
  const factory _Submit(
      {required final String activityFacilityId,
      required final String userType,
      final bool isRetry}) = _$SubmitImpl;

  String get activityFacilityId;
  String get userType;
  bool get isRetry;
  @JsonKey(ignore: true)
  _$$SubmitImplCopyWith<_$SubmitImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$RetryImplCopyWith<$Res> {
  factory _$$RetryImplCopyWith(
          _$RetryImpl value, $Res Function(_$RetryImpl) then) =
      __$$RetryImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String userType});
}

/// @nodoc
class __$$RetryImplCopyWithImpl<$Res>
    extends _$SendBackEventCopyWithImpl<$Res, _$RetryImpl>
    implements _$$RetryImplCopyWith<$Res> {
  __$$RetryImplCopyWithImpl(
      _$RetryImpl _value, $Res Function(_$RetryImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? userType = null,
  }) {
    return _then(_$RetryImpl(
      activityFacilityId: null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$RetryImpl implements _Retry {
  const _$RetryImpl({required this.activityFacilityId, required this.userType});

  @override
  final String activityFacilityId;
  @override
  final String userType;

  @override
  String toString() {
    return 'SendBackEvent.retry(activityFacilityId: $activityFacilityId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RetryImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$RetryImplCopyWith<_$RetryImpl> get copyWith =>
      __$$RetryImplCopyWithImpl<_$RetryImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String activityFacilityId, String userType, bool isRetry)
        submit,
    required TResult Function(String activityFacilityId, String userType) retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return retry(activityFacilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult? Function(String activityFacilityId, String userType)? retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return retry?.call(activityFacilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult Function(String activityFacilityId, String userType)? retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (retry != null) {
      return retry(activityFacilityId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return retry(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return retry?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (retry != null) {
      return retry(this);
    }
    return orElse();
  }
}

abstract class _Retry implements SendBackEvent {
  const factory _Retry(
      {required final String activityFacilityId,
      required final String userType}) = _$RetryImpl;

  String get activityFacilityId;
  String get userType;
  @JsonKey(ignore: true)
  _$$RetryImplCopyWith<_$RetryImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$WatchImplCopyWith<$Res> {
  factory _$$WatchImplCopyWith(
          _$WatchImpl value, $Res Function(_$WatchImpl) then) =
      __$$WatchImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId});
}

/// @nodoc
class __$$WatchImplCopyWithImpl<$Res>
    extends _$SendBackEventCopyWithImpl<$Res, _$WatchImpl>
    implements _$$WatchImplCopyWith<$Res> {
  __$$WatchImplCopyWithImpl(
      _$WatchImpl _value, $Res Function(_$WatchImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
  }) {
    return _then(_$WatchImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$WatchImpl implements _Watch {
  const _$WatchImpl(this.activityFacilityId);

  @override
  final String activityFacilityId;

  @override
  String toString() {
    return 'SendBackEvent.watch(activityFacilityId: $activityFacilityId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$WatchImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$WatchImplCopyWith<_$WatchImpl> get copyWith =>
      __$$WatchImplCopyWithImpl<_$WatchImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String activityFacilityId, String userType, bool isRetry)
        submit,
    required TResult Function(String activityFacilityId, String userType) retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return watch(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult? Function(String activityFacilityId, String userType)? retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return watch?.call(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult Function(String activityFacilityId, String userType)? retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (watch != null) {
      return watch(activityFacilityId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return watch(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return watch?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (watch != null) {
      return watch(this);
    }
    return orElse();
  }
}

abstract class _Watch implements SendBackEvent {
  const factory _Watch(final String activityFacilityId) = _$WatchImpl;

  String get activityFacilityId;
  @JsonKey(ignore: true)
  _$$WatchImplCopyWith<_$WatchImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$JobChangedImplCopyWith<$Res> {
  factory _$$JobChangedImplCopyWith(
          _$JobChangedImpl value, $Res Function(_$JobChangedImpl) then) =
      __$$JobChangedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheSubmissionJob? job});
}

/// @nodoc
class __$$JobChangedImplCopyWithImpl<$Res>
    extends _$SendBackEventCopyWithImpl<$Res, _$JobChangedImpl>
    implements _$$JobChangedImplCopyWith<$Res> {
  __$$JobChangedImplCopyWithImpl(
      _$JobChangedImpl _value, $Res Function(_$JobChangedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? job = freezed,
  }) {
    return _then(_$JobChangedImpl(
      freezed == job
          ? _value.job
          : job // ignore: cast_nullable_to_non_nullable
              as CacheSubmissionJob?,
    ));
  }
}

/// @nodoc

class _$JobChangedImpl implements _JobChanged {
  const _$JobChangedImpl(this.job);

  @override
  final CacheSubmissionJob? job;

  @override
  String toString() {
    return 'SendBackEvent.jobChanged(job: $job)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$JobChangedImpl &&
            (identical(other.job, job) || other.job == job));
  }

  @override
  int get hashCode => Object.hash(runtimeType, job);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$JobChangedImplCopyWith<_$JobChangedImpl> get copyWith =>
      __$$JobChangedImplCopyWithImpl<_$JobChangedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String activityFacilityId, String userType, bool isRetry)
        submit,
    required TResult Function(String activityFacilityId, String userType) retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return jobChanged(job);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult? Function(String activityFacilityId, String userType)? retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return jobChanged?.call(job);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult Function(String activityFacilityId, String userType)? retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (jobChanged != null) {
      return jobChanged(job);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return jobChanged(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return jobChanged?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (jobChanged != null) {
      return jobChanged(this);
    }
    return orElse();
  }
}

abstract class _JobChanged implements SendBackEvent {
  const factory _JobChanged(final CacheSubmissionJob? job) = _$JobChangedImpl;

  CacheSubmissionJob? get job;
  @JsonKey(ignore: true)
  _$$JobChangedImplCopyWith<_$JobChangedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DismissImplCopyWith<$Res> {
  factory _$$DismissImplCopyWith(
          _$DismissImpl value, $Res Function(_$DismissImpl) then) =
      __$$DismissImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$DismissImplCopyWithImpl<$Res>
    extends _$SendBackEventCopyWithImpl<$Res, _$DismissImpl>
    implements _$$DismissImplCopyWith<$Res> {
  __$$DismissImplCopyWithImpl(
      _$DismissImpl _value, $Res Function(_$DismissImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$DismissImpl implements _Dismiss {
  const _$DismissImpl();

  @override
  String toString() {
    return 'SendBackEvent.dismiss()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$DismissImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String activityFacilityId, String userType, bool isRetry)
        submit,
    required TResult Function(String activityFacilityId, String userType) retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return dismiss();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult? Function(String activityFacilityId, String userType)? retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return dismiss?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType, bool isRetry)?
        submit,
    TResult Function(String activityFacilityId, String userType)? retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (dismiss != null) {
      return dismiss();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Submit value) submit,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return dismiss(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Submit value)? submit,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return dismiss?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Submit value)? submit,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (dismiss != null) {
      return dismiss(this);
    }
    return orElse();
  }
}

abstract class _Dismiss implements SendBackEvent {
  const factory _Dismiss() = _$DismissImpl;
}

/// @nodoc
mixin _$SendBackState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_InProgress value) inProgress,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Success value) success,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SendBackStateCopyWith<$Res> {
  factory $SendBackStateCopyWith(
          SendBackState value, $Res Function(SendBackState) then) =
      _$SendBackStateCopyWithImpl<$Res, SendBackState>;
}

/// @nodoc
class _$SendBackStateCopyWithImpl<$Res, $Val extends SendBackState>
    implements $SendBackStateCopyWith<$Res> {
  _$SendBackStateCopyWithImpl(this._value, this._then);

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
    extends _$SendBackStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'SendBackState.initial()';
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
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
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
    required TResult Function(_InProgress value) inProgress,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Success value) success,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements SendBackState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$InProgressImplCopyWith<$Res> {
  factory _$$InProgressImplCopyWith(
          _$InProgressImpl value, $Res Function(_$InProgressImpl) then) =
      __$$InProgressImplCopyWithImpl<$Res>;
  @useResult
  $Res call({OperationProgressModel progress});
}

/// @nodoc
class __$$InProgressImplCopyWithImpl<$Res>
    extends _$SendBackStateCopyWithImpl<$Res, _$InProgressImpl>
    implements _$$InProgressImplCopyWith<$Res> {
  __$$InProgressImplCopyWithImpl(
      _$InProgressImpl _value, $Res Function(_$InProgressImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? progress = null,
  }) {
    return _then(_$InProgressImpl(
      null == progress
          ? _value.progress
          : progress // ignore: cast_nullable_to_non_nullable
              as OperationProgressModel,
    ));
  }
}

/// @nodoc

class _$InProgressImpl implements _InProgress {
  const _$InProgressImpl(this.progress);

  @override
  final OperationProgressModel progress;

  @override
  String toString() {
    return 'SendBackState.inProgress(progress: $progress)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InProgressImpl &&
            (identical(other.progress, progress) ||
                other.progress == progress));
  }

  @override
  int get hashCode => Object.hash(runtimeType, progress);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$InProgressImplCopyWith<_$InProgressImpl> get copyWith =>
      __$$InProgressImplCopyWithImpl<_$InProgressImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
  }) {
    return inProgress(progress);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
  }) {
    return inProgress?.call(progress);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
    required TResult orElse(),
  }) {
    if (inProgress != null) {
      return inProgress(progress);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_InProgress value) inProgress,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Success value) success,
  }) {
    return inProgress(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
  }) {
    return inProgress?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    required TResult orElse(),
  }) {
    if (inProgress != null) {
      return inProgress(this);
    }
    return orElse();
  }
}

abstract class _InProgress implements SendBackState {
  const factory _InProgress(final OperationProgressModel progress) =
      _$InProgressImpl;

  OperationProgressModel get progress;
  @JsonKey(ignore: true)
  _$$InProgressImplCopyWith<_$InProgressImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FailureImplCopyWith<$Res> {
  factory _$$FailureImplCopyWith(
          _$FailureImpl value, $Res Function(_$FailureImpl) then) =
      __$$FailureImplCopyWithImpl<$Res>;
  @useResult
  $Res call({OperationProgressModel progress});
}

/// @nodoc
class __$$FailureImplCopyWithImpl<$Res>
    extends _$SendBackStateCopyWithImpl<$Res, _$FailureImpl>
    implements _$$FailureImplCopyWith<$Res> {
  __$$FailureImplCopyWithImpl(
      _$FailureImpl _value, $Res Function(_$FailureImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? progress = null,
  }) {
    return _then(_$FailureImpl(
      null == progress
          ? _value.progress
          : progress // ignore: cast_nullable_to_non_nullable
              as OperationProgressModel,
    ));
  }
}

/// @nodoc

class _$FailureImpl implements _Failure {
  const _$FailureImpl(this.progress);

  @override
  final OperationProgressModel progress;

  @override
  String toString() {
    return 'SendBackState.failure(progress: $progress)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FailureImpl &&
            (identical(other.progress, progress) ||
                other.progress == progress));
  }

  @override
  int get hashCode => Object.hash(runtimeType, progress);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      __$$FailureImplCopyWithImpl<_$FailureImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
  }) {
    return failure(progress);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
  }) {
    return failure?.call(progress);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(progress);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_InProgress value) inProgress,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Success value) success,
  }) {
    return failure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
  }) {
    return failure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements SendBackState {
  const factory _Failure(final OperationProgressModel progress) = _$FailureImpl;

  OperationProgressModel get progress;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
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
    extends _$SendBackStateCopyWithImpl<$Res, _$SuccessImpl>
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
    return 'SendBackState.success()';
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
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
  }) {
    return success();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
  }) {
    return success?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
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
    required TResult Function(_InProgress value) inProgress,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Success value) success,
  }) {
    return success(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
  }) {
    return success?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(this);
    }
    return orElse();
  }
}

abstract class _Success implements SendBackState {
  const factory _Success() = _$SuccessImpl;
}
