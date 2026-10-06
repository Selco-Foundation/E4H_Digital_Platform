// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'asset_rejection.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$RejectionEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)
        submitRejection,
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitRejection value)? submitRejection,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitRejection value)? submitRejection,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RejectionEventCopyWith<$Res> {
  factory $RejectionEventCopyWith(
          RejectionEvent value, $Res Function(RejectionEvent) then) =
      _$RejectionEventCopyWithImpl<$Res, RejectionEvent>;
}

/// @nodoc
class _$RejectionEventCopyWithImpl<$Res, $Val extends RejectionEvent>
    implements $RejectionEventCopyWith<$Res> {
  _$RejectionEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$SubmitRejectionImplCopyWith<$Res> {
  factory _$$SubmitRejectionImplCopyWith(_$SubmitRejectionImpl value,
          $Res Function(_$SubmitRejectionImpl) then) =
      __$$SubmitRejectionImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {String activityFacilityId,
      String userType,
      List<dynamic> transactions,
      bool isRetry});
}

/// @nodoc
class __$$SubmitRejectionImplCopyWithImpl<$Res>
    extends _$RejectionEventCopyWithImpl<$Res, _$SubmitRejectionImpl>
    implements _$$SubmitRejectionImplCopyWith<$Res> {
  __$$SubmitRejectionImplCopyWithImpl(
      _$SubmitRejectionImpl _value, $Res Function(_$SubmitRejectionImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? userType = null,
    Object? transactions = null,
    Object? isRetry = null,
  }) {
    return _then(_$SubmitRejectionImpl(
      activityFacilityId: null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
      transactions: null == transactions
          ? _value._transactions
          : transactions // ignore: cast_nullable_to_non_nullable
              as List<dynamic>,
      isRetry: null == isRetry
          ? _value.isRetry
          : isRetry // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$SubmitRejectionImpl implements _SubmitRejection {
  const _$SubmitRejectionImpl(
      {required this.activityFacilityId,
      required this.userType,
      required final List<dynamic> transactions,
      this.isRetry = false})
      : _transactions = transactions;

  @override
  final String activityFacilityId;
  @override
  final String userType;
  final List<dynamic> _transactions;
  @override
  List<dynamic> get transactions {
    if (_transactions is EqualUnmodifiableListView) return _transactions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_transactions);
  }

  @override
  @JsonKey()
  final bool isRetry;

  @override
  String toString() {
    return 'RejectionEvent.submitRejection(activityFacilityId: $activityFacilityId, userType: $userType, transactions: $transactions, isRetry: $isRetry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitRejectionImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType) &&
            const DeepCollectionEquality()
                .equals(other._transactions, _transactions) &&
            (identical(other.isRetry, isRetry) || other.isRetry == isRetry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, userType,
      const DeepCollectionEquality().hash(_transactions), isRetry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitRejectionImplCopyWith<_$SubmitRejectionImpl> get copyWith =>
      __$$SubmitRejectionImplCopyWithImpl<_$SubmitRejectionImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)
        submitRejection,
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return submitRejection(activityFacilityId, userType, transactions, isRetry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return submitRejection?.call(
        activityFacilityId, userType, transactions, isRetry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (submitRejection != null) {
      return submitRejection(
          activityFacilityId, userType, transactions, isRetry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
    required TResult Function(_Retry value) retry,
    required TResult Function(_Watch value) watch,
    required TResult Function(_JobChanged value) jobChanged,
    required TResult Function(_Dismiss value) dismiss,
  }) {
    return submitRejection(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitRejection value)? submitRejection,
    TResult? Function(_Retry value)? retry,
    TResult? Function(_Watch value)? watch,
    TResult? Function(_JobChanged value)? jobChanged,
    TResult? Function(_Dismiss value)? dismiss,
  }) {
    return submitRejection?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitRejection value)? submitRejection,
    TResult Function(_Retry value)? retry,
    TResult Function(_Watch value)? watch,
    TResult Function(_JobChanged value)? jobChanged,
    TResult Function(_Dismiss value)? dismiss,
    required TResult orElse(),
  }) {
    if (submitRejection != null) {
      return submitRejection(this);
    }
    return orElse();
  }
}

abstract class _SubmitRejection implements RejectionEvent {
  const factory _SubmitRejection(
      {required final String activityFacilityId,
      required final String userType,
      required final List<dynamic> transactions,
      final bool isRetry}) = _$SubmitRejectionImpl;

  String get activityFacilityId;
  String get userType;
  List<dynamic> get transactions;
  bool get isRetry;
  @JsonKey(ignore: true)
  _$$SubmitRejectionImplCopyWith<_$SubmitRejectionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$RetryImplCopyWith<$Res> {
  factory _$$RetryImplCopyWith(
          _$RetryImpl value, $Res Function(_$RetryImpl) then) =
      __$$RetryImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {String activityFacilityId, String userType, List<dynamic> transactions});
}

/// @nodoc
class __$$RetryImplCopyWithImpl<$Res>
    extends _$RejectionEventCopyWithImpl<$Res, _$RetryImpl>
    implements _$$RetryImplCopyWith<$Res> {
  __$$RetryImplCopyWithImpl(
      _$RetryImpl _value, $Res Function(_$RetryImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? userType = null,
    Object? transactions = null,
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
      transactions: null == transactions
          ? _value._transactions
          : transactions // ignore: cast_nullable_to_non_nullable
              as List<dynamic>,
    ));
  }
}

/// @nodoc

class _$RetryImpl implements _Retry {
  const _$RetryImpl(
      {required this.activityFacilityId,
      required this.userType,
      required final List<dynamic> transactions})
      : _transactions = transactions;

  @override
  final String activityFacilityId;
  @override
  final String userType;
  final List<dynamic> _transactions;
  @override
  List<dynamic> get transactions {
    if (_transactions is EqualUnmodifiableListView) return _transactions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_transactions);
  }

  @override
  String toString() {
    return 'RejectionEvent.retry(activityFacilityId: $activityFacilityId, userType: $userType, transactions: $transactions)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RetryImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.userType, userType) ||
                other.userType == userType) &&
            const DeepCollectionEquality()
                .equals(other._transactions, _transactions));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, userType,
      const DeepCollectionEquality().hash(_transactions));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$RetryImplCopyWith<_$RetryImpl> get copyWith =>
      __$$RetryImplCopyWithImpl<_$RetryImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)
        submitRejection,
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return retry(activityFacilityId, userType, transactions);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return retry?.call(activityFacilityId, userType, transactions);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult Function(String activityFacilityId)? watch,
    TResult Function(CacheSubmissionJob? job)? jobChanged,
    TResult Function()? dismiss,
    required TResult orElse(),
  }) {
    if (retry != null) {
      return retry(activityFacilityId, userType, transactions);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
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
    TResult? Function(_SubmitRejection value)? submitRejection,
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
    TResult Function(_SubmitRejection value)? submitRejection,
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

abstract class _Retry implements RejectionEvent {
  const factory _Retry(
      {required final String activityFacilityId,
      required final String userType,
      required final List<dynamic> transactions}) = _$RetryImpl;

  String get activityFacilityId;
  String get userType;
  List<dynamic> get transactions;
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
    extends _$RejectionEventCopyWithImpl<$Res, _$WatchImpl>
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
    return 'RejectionEvent.watch(activityFacilityId: $activityFacilityId)';
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
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)
        submitRejection,
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return watch(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return watch?.call(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
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
    required TResult Function(_SubmitRejection value) submitRejection,
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
    TResult? Function(_SubmitRejection value)? submitRejection,
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
    TResult Function(_SubmitRejection value)? submitRejection,
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

abstract class _Watch implements RejectionEvent {
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
    extends _$RejectionEventCopyWithImpl<$Res, _$JobChangedImpl>
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
    return 'RejectionEvent.jobChanged(job: $job)';
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
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)
        submitRejection,
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return jobChanged(job);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return jobChanged?.call(job);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
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
    required TResult Function(_SubmitRejection value) submitRejection,
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
    TResult? Function(_SubmitRejection value)? submitRejection,
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
    TResult Function(_SubmitRejection value)? submitRejection,
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

abstract class _JobChanged implements RejectionEvent {
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
    extends _$RejectionEventCopyWithImpl<$Res, _$DismissImpl>
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
    return 'RejectionEvent.dismiss()';
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
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)
        submitRejection,
    required TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)
        retry,
    required TResult Function(String activityFacilityId) watch,
    required TResult Function(CacheSubmissionJob? job) jobChanged,
    required TResult Function() dismiss,
  }) {
    return dismiss();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult? Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
    TResult? Function(String activityFacilityId)? watch,
    TResult? Function(CacheSubmissionJob? job)? jobChanged,
    TResult? Function()? dismiss,
  }) {
    return dismiss?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions, bool isRetry)?
        submitRejection,
    TResult Function(String activityFacilityId, String userType,
            List<dynamic> transactions)?
        retry,
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
    required TResult Function(_SubmitRejection value) submitRejection,
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
    TResult? Function(_SubmitRejection value)? submitRejection,
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
    TResult Function(_SubmitRejection value)? submitRejection,
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

abstract class _Dismiss implements RejectionEvent {
  const factory _Dismiss() = _$DismissImpl;
}

/// @nodoc
mixin _$RejectionState {
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
abstract class $RejectionStateCopyWith<$Res> {
  factory $RejectionStateCopyWith(
          RejectionState value, $Res Function(RejectionState) then) =
      _$RejectionStateCopyWithImpl<$Res, RejectionState>;
}

/// @nodoc
class _$RejectionStateCopyWithImpl<$Res, $Val extends RejectionState>
    implements $RejectionStateCopyWith<$Res> {
  _$RejectionStateCopyWithImpl(this._value, this._then);

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
    extends _$RejectionStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'RejectionState.initial()';
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

abstract class _Initial implements RejectionState {
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
    extends _$RejectionStateCopyWithImpl<$Res, _$InProgressImpl>
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
    return 'RejectionState.inProgress(progress: $progress)';
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

abstract class _InProgress implements RejectionState {
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
    extends _$RejectionStateCopyWithImpl<$Res, _$FailureImpl>
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
    return 'RejectionState.failure(progress: $progress)';
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

abstract class _Failure implements RejectionState {
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
    extends _$RejectionStateCopyWithImpl<$Res, _$SuccessImpl>
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
    return 'RejectionState.success()';
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

abstract class _Success implements RejectionState {
  const factory _Success() = _$SuccessImpl;
}
