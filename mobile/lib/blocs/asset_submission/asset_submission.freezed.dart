// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'asset_submission.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$AssetSubmissionEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetSubmissionEventCopyWith<$Res> {
  factory $AssetSubmissionEventCopyWith(AssetSubmissionEvent value,
          $Res Function(AssetSubmissionEvent) then) =
      _$AssetSubmissionEventCopyWithImpl<$Res, AssetSubmissionEvent>;
}

/// @nodoc
class _$AssetSubmissionEventCopyWithImpl<$Res,
        $Val extends AssetSubmissionEvent>
    implements $AssetSubmissionEventCopyWith<$Res> {
  _$AssetSubmissionEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$SubmitAllImplCopyWith<$Res> {
  factory _$$SubmitAllImplCopyWith(
          _$SubmitAllImpl value, $Res Function(_$SubmitAllImpl) then) =
      __$$SubmitAllImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {String activityFacilityId,
      String facilityId,
      String userType,
      bool isRetry});
}

/// @nodoc
class __$$SubmitAllImplCopyWithImpl<$Res>
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$SubmitAllImpl>
    implements _$$SubmitAllImplCopyWith<$Res> {
  __$$SubmitAllImplCopyWithImpl(
      _$SubmitAllImpl _value, $Res Function(_$SubmitAllImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? facilityId = null,
    Object? userType = null,
    Object? isRetry = null,
  }) {
    return _then(_$SubmitAllImpl(
      activityFacilityId: null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      facilityId: null == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
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

class _$SubmitAllImpl implements _SubmitAll {
  const _$SubmitAllImpl(
      {required this.activityFacilityId,
      required this.facilityId,
      required this.userType,
      this.isRetry = false});

  @override
  final String activityFacilityId;
  @override
  final String facilityId;
  @override
  final String userType;
  @override
  @JsonKey()
  final bool isRetry;

  @override
  String toString() {
    return 'AssetSubmissionEvent.submitAll(activityFacilityId: $activityFacilityId, facilityId: $facilityId, userType: $userType, isRetry: $isRetry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitAllImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.facilityId, facilityId) ||
                other.facilityId == facilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType) &&
            (identical(other.isRetry, isRetry) || other.isRetry == isRetry));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, activityFacilityId, facilityId, userType, isRetry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitAllImplCopyWith<_$SubmitAllImpl> get copyWith =>
      __$$SubmitAllImplCopyWithImpl<_$SubmitAllImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return submitAll(activityFacilityId, facilityId, userType, isRetry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return submitAll?.call(activityFacilityId, facilityId, userType, isRetry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (submitAll != null) {
      return submitAll(activityFacilityId, facilityId, userType, isRetry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return submitAll(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return submitAll?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (submitAll != null) {
      return submitAll(this);
    }
    return orElse();
  }
}

abstract class _SubmitAll implements AssetSubmissionEvent {
  const factory _SubmitAll(
      {required final String activityFacilityId,
      required final String facilityId,
      required final String userType,
      final bool isRetry}) = _$SubmitAllImpl;

  String get activityFacilityId;
  String get facilityId;
  String get userType;
  bool get isRetry;
  @JsonKey(ignore: true)
  _$$SubmitAllImplCopyWith<_$SubmitAllImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$RetryImplCopyWith<$Res> {
  factory _$$RetryImplCopyWith(
          _$RetryImpl value, $Res Function(_$RetryImpl) then) =
      __$$RetryImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String facilityId, String userType});
}

/// @nodoc
class __$$RetryImplCopyWithImpl<$Res>
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$RetryImpl>
    implements _$$RetryImplCopyWith<$Res> {
  __$$RetryImplCopyWithImpl(
      _$RetryImpl _value, $Res Function(_$RetryImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? facilityId = null,
    Object? userType = null,
  }) {
    return _then(_$RetryImpl(
      activityFacilityId: null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      facilityId: null == facilityId
          ? _value.facilityId
          : facilityId // ignore: cast_nullable_to_non_nullable
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
  const _$RetryImpl(
      {required this.activityFacilityId,
      required this.facilityId,
      required this.userType});

  @override
  final String activityFacilityId;
  @override
  final String facilityId;
  @override
  final String userType;

  @override
  String toString() {
    return 'AssetSubmissionEvent.retry(activityFacilityId: $activityFacilityId, facilityId: $facilityId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RetryImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.facilityId, facilityId) ||
                other.facilityId == facilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, activityFacilityId, facilityId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$RetryImplCopyWith<_$RetryImpl> get copyWith =>
      __$$RetryImplCopyWithImpl<_$RetryImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return retry(activityFacilityId, facilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return retry?.call(activityFacilityId, facilityId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (retry != null) {
      return retry(activityFacilityId, facilityId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return retry(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return retry?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (retry != null) {
      return retry(this);
    }
    return orElse();
  }
}

abstract class _Retry implements AssetSubmissionEvent {
  const factory _Retry(
      {required final String activityFacilityId,
      required final String facilityId,
      required final String userType}) = _$RetryImpl;

  String get activityFacilityId;
  String get facilityId;
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
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$WatchImpl>
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
    return 'AssetSubmissionEvent.watch(activityFacilityId: $activityFacilityId)';
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
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return watch(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return watch?.call(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
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
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return watch(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return watch?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (watch != null) {
      return watch(this);
    }
    return orElse();
  }
}

abstract class _Watch implements AssetSubmissionEvent {
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
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$JobChangedImpl>
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
    return 'AssetSubmissionEvent.jobChanged(job: $job)';
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
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return jobChanged(job);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return jobChanged?.call(job);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
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
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return jobChanged(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return jobChanged?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (jobChanged != null) {
      return jobChanged(this);
    }
    return orElse();
  }
}

abstract class _JobChanged implements AssetSubmissionEvent {
  const factory _JobChanged(final CacheSubmissionJob? job) = _$JobChangedImpl;

  CacheSubmissionJob? get job;
  @JsonKey(ignore: true)
  _$$JobChangedImplCopyWith<_$JobChangedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$SubmitAllDraftsImplCopyWith<$Res> {
  factory _$$SubmitAllDraftsImplCopyWith(_$SubmitAllDraftsImpl value,
          $Res Function(_$SubmitAllDraftsImpl) then) =
      __$$SubmitAllDraftsImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String userType});
}

/// @nodoc
class __$$SubmitAllDraftsImplCopyWithImpl<$Res>
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$SubmitAllDraftsImpl>
    implements _$$SubmitAllDraftsImplCopyWith<$Res> {
  __$$SubmitAllDraftsImplCopyWithImpl(
      _$SubmitAllDraftsImpl _value, $Res Function(_$SubmitAllDraftsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? userType = null,
  }) {
    return _then(_$SubmitAllDraftsImpl(
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SubmitAllDraftsImpl implements _SubmitAllDrafts {
  const _$SubmitAllDraftsImpl({required this.userType});

  @override
  final String userType;

  @override
  String toString() {
    return 'AssetSubmissionEvent.submitAllDrafts(userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitAllDraftsImpl &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitAllDraftsImplCopyWith<_$SubmitAllDraftsImpl> get copyWith =>
      __$$SubmitAllDraftsImplCopyWithImpl<_$SubmitAllDraftsImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return submitAllDrafts(userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return submitAllDrafts?.call(userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (submitAllDrafts != null) {
      return submitAllDrafts(userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return submitAllDrafts(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return submitAllDrafts?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (submitAllDrafts != null) {
      return submitAllDrafts(this);
    }
    return orElse();
  }
}

abstract class _SubmitAllDrafts implements AssetSubmissionEvent {
  const factory _SubmitAllDrafts({required final String userType}) =
      _$SubmitAllDraftsImpl;

  String get userType;
  @JsonKey(ignore: true)
  _$$SubmitAllDraftsImplCopyWith<_$SubmitAllDraftsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$BulkJobsChangedImplCopyWith<$Res> {
  factory _$$BulkJobsChangedImplCopyWith(_$BulkJobsChangedImpl value,
          $Res Function(_$BulkJobsChangedImpl) then) =
      __$$BulkJobsChangedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CacheSubmissionJob> jobs, int watchToken});
}

/// @nodoc
class __$$BulkJobsChangedImplCopyWithImpl<$Res>
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$BulkJobsChangedImpl>
    implements _$$BulkJobsChangedImplCopyWith<$Res> {
  __$$BulkJobsChangedImplCopyWithImpl(
      _$BulkJobsChangedImpl _value, $Res Function(_$BulkJobsChangedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? jobs = null,
    Object? watchToken = null,
  }) {
    return _then(_$BulkJobsChangedImpl(
      jobs: null == jobs
          ? _value._jobs
          : jobs // ignore: cast_nullable_to_non_nullable
              as List<CacheSubmissionJob>,
      watchToken: null == watchToken
          ? _value.watchToken
          : watchToken // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$BulkJobsChangedImpl implements _BulkJobsChanged {
  const _$BulkJobsChangedImpl(
      {required final List<CacheSubmissionJob> jobs, required this.watchToken})
      : _jobs = jobs;

  final List<CacheSubmissionJob> _jobs;
  @override
  List<CacheSubmissionJob> get jobs {
    if (_jobs is EqualUnmodifiableListView) return _jobs;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_jobs);
  }

  @override
  final int watchToken;

  @override
  String toString() {
    return 'AssetSubmissionEvent.bulkJobsChanged(jobs: $jobs, watchToken: $watchToken)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BulkJobsChangedImpl &&
            const DeepCollectionEquality().equals(other._jobs, _jobs) &&
            (identical(other.watchToken, watchToken) ||
                other.watchToken == watchToken));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, const DeepCollectionEquality().hash(_jobs), watchToken);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BulkJobsChangedImplCopyWith<_$BulkJobsChangedImpl> get copyWith =>
      __$$BulkJobsChangedImplCopyWithImpl<_$BulkJobsChangedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return bulkJobsChanged(jobs, watchToken);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return bulkJobsChanged?.call(jobs, watchToken);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (bulkJobsChanged != null) {
      return bulkJobsChanged(jobs, watchToken);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return bulkJobsChanged(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return bulkJobsChanged?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (bulkJobsChanged != null) {
      return bulkJobsChanged(this);
    }
    return orElse();
  }
}

abstract class _BulkJobsChanged implements AssetSubmissionEvent {
  const factory _BulkJobsChanged(
      {required final List<CacheSubmissionJob> jobs,
      required final int watchToken}) = _$BulkJobsChangedImpl;

  List<CacheSubmissionJob> get jobs;
  int get watchToken;
  @JsonKey(ignore: true)
  _$$BulkJobsChangedImplCopyWith<_$BulkJobsChangedImpl> get copyWith =>
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
    extends _$AssetSubmissionEventCopyWithImpl<$Res, _$DismissImpl>
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
    return 'AssetSubmissionEvent.dismiss()';
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
    required TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)
        submitAll,
    required TResult Function(
            String activityFacilityId, String facilityId, String userType)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function(String userType) submitAllDrafts,
    required TResult Function(List<CacheSubmissionJob> jobs, int watchToken)
        bulkJobsChanged,
    required TResult Function() dismiss,
  }) {
    return dismiss();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult? Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function(String userType)? submitAllDrafts,
    TResult? Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
    TResult? Function()? dismiss,
  }) {
    return dismiss?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String facilityId,
            String userType, bool isRetry)?
        submitAll,
    TResult Function(
            String activityFacilityId, String facilityId, String userType)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function(String userType)? submitAllDrafts,
    TResult Function(List<CacheSubmissionJob> jobs, int watchToken)?
        bulkJobsChanged,
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
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
    required TResult Function(_BulkJobsChanged value) bulkJobsChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return dismiss(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult? Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return dismiss?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    TResult Function(_BulkJobsChanged value)? bulkJobsChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (dismiss != null) {
      return dismiss(this);
    }
    return orElse();
  }
}

abstract class _Dismiss implements AssetSubmissionEvent {
  const factory _Dismiss() = _$DismissImpl;
}

/// @nodoc
mixin _$AssetSubmissionState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_InProgress value) inProgress,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Success value) success,
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetSubmissionStateCopyWith<$Res> {
  factory $AssetSubmissionStateCopyWith(AssetSubmissionState value,
          $Res Function(AssetSubmissionState) then) =
      _$AssetSubmissionStateCopyWithImpl<$Res, AssetSubmissionState>;
}

/// @nodoc
class _$AssetSubmissionStateCopyWithImpl<$Res,
        $Val extends AssetSubmissionState>
    implements $AssetSubmissionStateCopyWith<$Res> {
  _$AssetSubmissionStateCopyWithImpl(this._value, this._then);

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
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'AssetSubmissionState.initial()';
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
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
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
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
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
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
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
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
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
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
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
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements AssetSubmissionState {
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
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$InProgressImpl>
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
    return 'AssetSubmissionState.inProgress(progress: $progress)';
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
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
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
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
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
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
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
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
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
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
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
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) {
    if (inProgress != null) {
      return inProgress(this);
    }
    return orElse();
  }
}

abstract class _InProgress implements AssetSubmissionState {
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
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$FailureImpl>
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
    return 'AssetSubmissionState.failure(progress: $progress)';
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
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
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
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
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
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
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
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
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
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
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
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements AssetSubmissionState {
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
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$SuccessImpl>
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
    return 'AssetSubmissionState.success()';
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
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
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
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
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
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
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
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
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
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
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
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(this);
    }
    return orElse();
  }
}

abstract class _Success implements AssetSubmissionState {
  const factory _Success() = _$SuccessImpl;
}

/// @nodoc
abstract class _$$BulkProgressImplCopyWith<$Res> {
  factory _$$BulkProgressImplCopyWith(
          _$BulkProgressImpl value, $Res Function(_$BulkProgressImpl) then) =
      __$$BulkProgressImplCopyWithImpl<$Res>;
  @useResult
  $Res call({BulkOperationProgressModel progress});
}

/// @nodoc
class __$$BulkProgressImplCopyWithImpl<$Res>
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$BulkProgressImpl>
    implements _$$BulkProgressImplCopyWith<$Res> {
  __$$BulkProgressImplCopyWithImpl(
      _$BulkProgressImpl _value, $Res Function(_$BulkProgressImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? progress = null,
  }) {
    return _then(_$BulkProgressImpl(
      null == progress
          ? _value.progress
          : progress // ignore: cast_nullable_to_non_nullable
              as BulkOperationProgressModel,
    ));
  }
}

/// @nodoc

class _$BulkProgressImpl implements _BulkProgress {
  const _$BulkProgressImpl(this.progress);

  @override
  final BulkOperationProgressModel progress;

  @override
  String toString() {
    return 'AssetSubmissionState.bulkProgress(progress: $progress)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BulkProgressImpl &&
            (identical(other.progress, progress) ||
                other.progress == progress));
  }

  @override
  int get hashCode => Object.hash(runtimeType, progress);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BulkProgressImplCopyWith<_$BulkProgressImpl> get copyWith =>
      __$$BulkProgressImplCopyWithImpl<_$BulkProgressImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
  }) {
    return bulkProgress(progress);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
  }) {
    return bulkProgress?.call(progress);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
    required TResult orElse(),
  }) {
    if (bulkProgress != null) {
      return bulkProgress(progress);
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
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
  }) {
    return bulkProgress(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
  }) {
    return bulkProgress?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) {
    if (bulkProgress != null) {
      return bulkProgress(this);
    }
    return orElse();
  }
}

abstract class _BulkProgress implements AssetSubmissionState {
  const factory _BulkProgress(final BulkOperationProgressModel progress) =
      _$BulkProgressImpl;

  BulkOperationProgressModel get progress;
  @JsonKey(ignore: true)
  _$$BulkProgressImplCopyWith<_$BulkProgressImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$BulkFailureImplCopyWith<$Res> {
  factory _$$BulkFailureImplCopyWith(
          _$BulkFailureImpl value, $Res Function(_$BulkFailureImpl) then) =
      __$$BulkFailureImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String errorMessage});
}

/// @nodoc
class __$$BulkFailureImplCopyWithImpl<$Res>
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$BulkFailureImpl>
    implements _$$BulkFailureImplCopyWith<$Res> {
  __$$BulkFailureImplCopyWithImpl(
      _$BulkFailureImpl _value, $Res Function(_$BulkFailureImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? errorMessage = null,
  }) {
    return _then(_$BulkFailureImpl(
      null == errorMessage
          ? _value.errorMessage
          : errorMessage // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$BulkFailureImpl implements _BulkFailure {
  const _$BulkFailureImpl(this.errorMessage);

  @override
  final String errorMessage;

  @override
  String toString() {
    return 'AssetSubmissionState.bulkFailure(errorMessage: $errorMessage)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BulkFailureImpl &&
            (identical(other.errorMessage, errorMessage) ||
                other.errorMessage == errorMessage));
  }

  @override
  int get hashCode => Object.hash(runtimeType, errorMessage);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BulkFailureImplCopyWith<_$BulkFailureImpl> get copyWith =>
      __$$BulkFailureImplCopyWithImpl<_$BulkFailureImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(OperationProgressModel progress) inProgress,
    required TResult Function(OperationProgressModel progress) failure,
    required TResult Function() success,
    required TResult Function(BulkOperationProgressModel progress) bulkProgress,
    required TResult Function(String errorMessage) bulkFailure,
  }) {
    return bulkFailure(errorMessage);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(OperationProgressModel progress)? inProgress,
    TResult? Function(OperationProgressModel progress)? failure,
    TResult? Function()? success,
    TResult? Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult? Function(String errorMessage)? bulkFailure,
  }) {
    return bulkFailure?.call(errorMessage);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(OperationProgressModel progress)? inProgress,
    TResult Function(OperationProgressModel progress)? failure,
    TResult Function()? success,
    TResult Function(BulkOperationProgressModel progress)? bulkProgress,
    TResult Function(String errorMessage)? bulkFailure,
    required TResult orElse(),
  }) {
    if (bulkFailure != null) {
      return bulkFailure(errorMessage);
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
    required TResult Function(_BulkProgress value) bulkProgress,
    required TResult Function(_BulkFailure value) bulkFailure,
  }) {
    return bulkFailure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_InProgress value)? inProgress,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Success value)? success,
    TResult? Function(_BulkProgress value)? bulkProgress,
    TResult? Function(_BulkFailure value)? bulkFailure,
  }) {
    return bulkFailure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_InProgress value)? inProgress,
    TResult Function(_Failure value)? failure,
    TResult Function(_Success value)? success,
    TResult Function(_BulkProgress value)? bulkProgress,
    TResult Function(_BulkFailure value)? bulkFailure,
    required TResult orElse(),
  }) {
    if (bulkFailure != null) {
      return bulkFailure(this);
    }
    return orElse();
  }
}

abstract class _BulkFailure implements AssetSubmissionState {
  const factory _BulkFailure(final String errorMessage) = _$BulkFailureImpl;

  String get errorMessage;
  @JsonKey(ignore: true)
  _$$BulkFailureImplCopyWith<_$BulkFailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
