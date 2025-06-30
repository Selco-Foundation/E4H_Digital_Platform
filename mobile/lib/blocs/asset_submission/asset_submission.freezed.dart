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
  String get userType => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) submitAll,
    required TResult Function(String userType) submitAllDrafts,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? submitAll,
    TResult? Function(String userType)? submitAllDrafts,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? submitAll,
    TResult Function(String userType)? submitAllDrafts,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $AssetSubmissionEventCopyWith<AssetSubmissionEvent> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AssetSubmissionEventCopyWith<$Res> {
  factory $AssetSubmissionEventCopyWith(AssetSubmissionEvent value,
          $Res Function(AssetSubmissionEvent) then) =
      _$AssetSubmissionEventCopyWithImpl<$Res, AssetSubmissionEvent>;
  @useResult
  $Res call({String userType});
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

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? userType = null,
  }) {
    return _then(_value.copyWith(
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SubmitAllImplCopyWith<$Res>
    implements $AssetSubmissionEventCopyWith<$Res> {
  factory _$$SubmitAllImplCopyWith(
          _$SubmitAllImpl value, $Res Function(_$SubmitAllImpl) then) =
      __$$SubmitAllImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId, String userType});
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
    Object? projectId = null,
    Object? userType = null,
  }) {
    return _then(_$SubmitAllImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SubmitAllImpl implements _SubmitAll {
  const _$SubmitAllImpl({required this.projectId, required this.userType});

  @override
  final String projectId;
  @override
  final String userType;

  @override
  String toString() {
    return 'AssetSubmissionEvent.submitAll(projectId: $projectId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitAllImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitAllImplCopyWith<_$SubmitAllImpl> get copyWith =>
      __$$SubmitAllImplCopyWithImpl<_$SubmitAllImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) submitAll,
    required TResult Function(String userType) submitAllDrafts,
  }) {
    return submitAll(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? submitAll,
    TResult? Function(String userType)? submitAllDrafts,
  }) {
    return submitAll?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? submitAll,
    TResult Function(String userType)? submitAllDrafts,
    required TResult orElse(),
  }) {
    if (submitAll != null) {
      return submitAll(projectId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitAll value) submitAll,
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
  }) {
    return submitAll(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
  }) {
    return submitAll?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
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
      {required final String projectId,
      required final String userType}) = _$SubmitAllImpl;

  String get projectId;
  @override
  String get userType;
  @override
  @JsonKey(ignore: true)
  _$$SubmitAllImplCopyWith<_$SubmitAllImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$SubmitAllDraftsImplCopyWith<$Res>
    implements $AssetSubmissionEventCopyWith<$Res> {
  factory _$$SubmitAllDraftsImplCopyWith(_$SubmitAllDraftsImpl value,
          $Res Function(_$SubmitAllDraftsImpl) then) =
      __$$SubmitAllDraftsImplCopyWithImpl<$Res>;
  @override
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
    required TResult Function(String projectId, String userType) submitAll,
    required TResult Function(String userType) submitAllDrafts,
  }) {
    return submitAllDrafts(userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? submitAll,
    TResult? Function(String userType)? submitAllDrafts,
  }) {
    return submitAllDrafts?.call(userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? submitAll,
    TResult Function(String userType)? submitAllDrafts,
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
    required TResult Function(_SubmitAllDrafts value) submitAllDrafts,
  }) {
    return submitAllDrafts(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitAll value)? submitAll,
    TResult? Function(_SubmitAllDrafts value)? submitAllDrafts,
  }) {
    return submitAllDrafts?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitAll value)? submitAll,
    TResult Function(_SubmitAllDrafts value)? submitAllDrafts,
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

  @override
  String get userType;
  @override
  @JsonKey(ignore: true)
  _$$SubmitAllDraftsImplCopyWith<_$SubmitAllDraftsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$AssetSubmissionState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
    required TResult Function(int completed, int total) progress,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
    TResult? Function(int completed, int total)? progress,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
    TResult Function(int completed, int total)? progress,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Progress value) progress,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Progress value)? progress,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_Progress value)? progress,
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
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
    required TResult Function(int completed, int total) progress,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
    TResult? Function(int completed, int total)? progress,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
    TResult Function(int completed, int total)? progress,
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
    required TResult Function(_Progress value) progress,
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
    TResult? Function(_Progress value)? progress,
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
    TResult Function(_Progress value)? progress,
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
abstract class _$$LoadingImplCopyWith<$Res> {
  factory _$$LoadingImplCopyWith(
          _$LoadingImpl value, $Res Function(_$LoadingImpl) then) =
      __$$LoadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$LoadingImplCopyWithImpl<$Res>
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'AssetSubmissionState.loading()';
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
    required TResult Function(String errorMessage) failure,
    required TResult Function(int completed, int total) progress,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
    TResult? Function(int completed, int total)? progress,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
    TResult Function(int completed, int total)? progress,
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
    required TResult Function(_Progress value) progress,
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
    TResult? Function(_Progress value)? progress,
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
    TResult Function(_Progress value)? progress,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _Loading implements AssetSubmissionState {
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
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
    required TResult Function(int completed, int total) progress,
  }) {
    return success();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
    TResult? Function(int completed, int total)? progress,
  }) {
    return success?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
    TResult Function(int completed, int total)? progress,
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
    required TResult Function(_Progress value) progress,
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
    TResult? Function(_Progress value)? progress,
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
    TResult Function(_Progress value)? progress,
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
abstract class _$$FailureImplCopyWith<$Res> {
  factory _$$FailureImplCopyWith(
          _$FailureImpl value, $Res Function(_$FailureImpl) then) =
      __$$FailureImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String errorMessage});
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
    Object? errorMessage = null,
  }) {
    return _then(_$FailureImpl(
      null == errorMessage
          ? _value.errorMessage
          : errorMessage // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$FailureImpl implements _Failure {
  const _$FailureImpl(this.errorMessage);

  @override
  final String errorMessage;

  @override
  String toString() {
    return 'AssetSubmissionState.failure(errorMessage: $errorMessage)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FailureImpl &&
            (identical(other.errorMessage, errorMessage) ||
                other.errorMessage == errorMessage));
  }

  @override
  int get hashCode => Object.hash(runtimeType, errorMessage);

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
    required TResult Function(String errorMessage) failure,
    required TResult Function(int completed, int total) progress,
  }) {
    return failure(errorMessage);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
    TResult? Function(int completed, int total)? progress,
  }) {
    return failure?.call(errorMessage);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
    TResult Function(int completed, int total)? progress,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(errorMessage);
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
    required TResult Function(_Progress value) progress,
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
    TResult? Function(_Progress value)? progress,
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
    TResult Function(_Progress value)? progress,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements AssetSubmissionState {
  const factory _Failure(final String errorMessage) = _$FailureImpl;

  String get errorMessage;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ProgressImplCopyWith<$Res> {
  factory _$$ProgressImplCopyWith(
          _$ProgressImpl value, $Res Function(_$ProgressImpl) then) =
      __$$ProgressImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int completed, int total});
}

/// @nodoc
class __$$ProgressImplCopyWithImpl<$Res>
    extends _$AssetSubmissionStateCopyWithImpl<$Res, _$ProgressImpl>
    implements _$$ProgressImplCopyWith<$Res> {
  __$$ProgressImplCopyWithImpl(
      _$ProgressImpl _value, $Res Function(_$ProgressImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? completed = null,
    Object? total = null,
  }) {
    return _then(_$ProgressImpl(
      completed: null == completed
          ? _value.completed
          : completed // ignore: cast_nullable_to_non_nullable
              as int,
      total: null == total
          ? _value.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$ProgressImpl implements _Progress {
  const _$ProgressImpl({required this.completed, required this.total});

  @override
  final int completed;
  @override
  final int total;

  @override
  String toString() {
    return 'AssetSubmissionState.progress(completed: $completed, total: $total)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProgressImpl &&
            (identical(other.completed, completed) ||
                other.completed == completed) &&
            (identical(other.total, total) || other.total == total));
  }

  @override
  int get hashCode => Object.hash(runtimeType, completed, total);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProgressImplCopyWith<_$ProgressImpl> get copyWith =>
      __$$ProgressImplCopyWithImpl<_$ProgressImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
    required TResult Function(int completed, int total) progress,
  }) {
    return progress(completed, total);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
    TResult? Function(int completed, int total)? progress,
  }) {
    return progress?.call(completed, total);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
    TResult Function(int completed, int total)? progress,
    required TResult orElse(),
  }) {
    if (progress != null) {
      return progress(completed, total);
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
    required TResult Function(_Progress value) progress,
  }) {
    return progress(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Progress value)? progress,
  }) {
    return progress?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_Progress value)? progress,
    required TResult orElse(),
  }) {
    if (progress != null) {
      return progress(this);
    }
    return orElse();
  }
}

abstract class _Progress implements AssetSubmissionState {
  const factory _Progress(
      {required final int completed,
      required final int total}) = _$ProgressImpl;

  int get completed;
  int get total;
  @JsonKey(ignore: true)
  _$$ProgressImplCopyWith<_$ProgressImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
