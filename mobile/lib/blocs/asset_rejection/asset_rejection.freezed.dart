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
  String get projectId => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String projectId, String userType, List<dynamic> transactions)
        submitRejection,
    required TResult Function(String projectId) bgRejectDone,
    required TResult Function(String projectId, String? message) bgRejectError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult? Function(String projectId)? bgRejectDone,
    TResult? Function(String projectId, String? message)? bgRejectError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult Function(String projectId)? bgRejectDone,
    TResult Function(String projectId, String? message)? bgRejectError,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
    required TResult Function(_BgRejectDone value) bgRejectDone,
    required TResult Function(_BgRejectError value) bgRejectError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitRejection value)? submitRejection,
    TResult? Function(_BgRejectDone value)? bgRejectDone,
    TResult? Function(_BgRejectError value)? bgRejectError,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitRejection value)? submitRejection,
    TResult Function(_BgRejectDone value)? bgRejectDone,
    TResult Function(_BgRejectError value)? bgRejectError,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $RejectionEventCopyWith<RejectionEvent> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RejectionEventCopyWith<$Res> {
  factory $RejectionEventCopyWith(
          RejectionEvent value, $Res Function(RejectionEvent) then) =
      _$RejectionEventCopyWithImpl<$Res, RejectionEvent>;
  @useResult
  $Res call({String projectId});
}

/// @nodoc
class _$RejectionEventCopyWithImpl<$Res, $Val extends RejectionEvent>
    implements $RejectionEventCopyWith<$Res> {
  _$RejectionEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
  }) {
    return _then(_value.copyWith(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SubmitRejectionImplCopyWith<$Res>
    implements $RejectionEventCopyWith<$Res> {
  factory _$$SubmitRejectionImplCopyWith(_$SubmitRejectionImpl value,
          $Res Function(_$SubmitRejectionImpl) then) =
      __$$SubmitRejectionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId, String userType, List<dynamic> transactions});
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
    Object? projectId = null,
    Object? userType = null,
    Object? transactions = null,
  }) {
    return _then(_$SubmitRejectionImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
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

class _$SubmitRejectionImpl implements _SubmitRejection {
  const _$SubmitRejectionImpl(
      {required this.projectId,
      required this.userType,
      required final List<dynamic> transactions})
      : _transactions = transactions;

  @override
  final String projectId;
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
    return 'RejectionEvent.submitRejection(projectId: $projectId, userType: $userType, transactions: $transactions)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SubmitRejectionImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.userType, userType) ||
                other.userType == userType) &&
            const DeepCollectionEquality()
                .equals(other._transactions, _transactions));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, userType,
      const DeepCollectionEquality().hash(_transactions));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SubmitRejectionImplCopyWith<_$SubmitRejectionImpl> get copyWith =>
      __$$SubmitRejectionImplCopyWithImpl<_$SubmitRejectionImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String projectId, String userType, List<dynamic> transactions)
        submitRejection,
    required TResult Function(String projectId) bgRejectDone,
    required TResult Function(String projectId, String? message) bgRejectError,
  }) {
    return submitRejection(projectId, userType, transactions);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult? Function(String projectId)? bgRejectDone,
    TResult? Function(String projectId, String? message)? bgRejectError,
  }) {
    return submitRejection?.call(projectId, userType, transactions);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult Function(String projectId)? bgRejectDone,
    TResult Function(String projectId, String? message)? bgRejectError,
    required TResult orElse(),
  }) {
    if (submitRejection != null) {
      return submitRejection(projectId, userType, transactions);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
    required TResult Function(_BgRejectDone value) bgRejectDone,
    required TResult Function(_BgRejectError value) bgRejectError,
  }) {
    return submitRejection(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitRejection value)? submitRejection,
    TResult? Function(_BgRejectDone value)? bgRejectDone,
    TResult? Function(_BgRejectError value)? bgRejectError,
  }) {
    return submitRejection?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitRejection value)? submitRejection,
    TResult Function(_BgRejectDone value)? bgRejectDone,
    TResult Function(_BgRejectError value)? bgRejectError,
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
      {required final String projectId,
      required final String userType,
      required final List<dynamic> transactions}) = _$SubmitRejectionImpl;

  @override
  String get projectId;
  String get userType;
  List<dynamic> get transactions;
  @override
  @JsonKey(ignore: true)
  _$$SubmitRejectionImplCopyWith<_$SubmitRejectionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$BgRejectDoneImplCopyWith<$Res>
    implements $RejectionEventCopyWith<$Res> {
  factory _$$BgRejectDoneImplCopyWith(
          _$BgRejectDoneImpl value, $Res Function(_$BgRejectDoneImpl) then) =
      __$$BgRejectDoneImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId});
}

/// @nodoc
class __$$BgRejectDoneImplCopyWithImpl<$Res>
    extends _$RejectionEventCopyWithImpl<$Res, _$BgRejectDoneImpl>
    implements _$$BgRejectDoneImplCopyWith<$Res> {
  __$$BgRejectDoneImplCopyWithImpl(
      _$BgRejectDoneImpl _value, $Res Function(_$BgRejectDoneImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
  }) {
    return _then(_$BgRejectDoneImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$BgRejectDoneImpl implements _BgRejectDone {
  const _$BgRejectDoneImpl({required this.projectId});

  @override
  final String projectId;

  @override
  String toString() {
    return 'RejectionEvent.bgRejectDone(projectId: $projectId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BgRejectDoneImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BgRejectDoneImplCopyWith<_$BgRejectDoneImpl> get copyWith =>
      __$$BgRejectDoneImplCopyWithImpl<_$BgRejectDoneImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String projectId, String userType, List<dynamic> transactions)
        submitRejection,
    required TResult Function(String projectId) bgRejectDone,
    required TResult Function(String projectId, String? message) bgRejectError,
  }) {
    return bgRejectDone(projectId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult? Function(String projectId)? bgRejectDone,
    TResult? Function(String projectId, String? message)? bgRejectError,
  }) {
    return bgRejectDone?.call(projectId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult Function(String projectId)? bgRejectDone,
    TResult Function(String projectId, String? message)? bgRejectError,
    required TResult orElse(),
  }) {
    if (bgRejectDone != null) {
      return bgRejectDone(projectId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
    required TResult Function(_BgRejectDone value) bgRejectDone,
    required TResult Function(_BgRejectError value) bgRejectError,
  }) {
    return bgRejectDone(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitRejection value)? submitRejection,
    TResult? Function(_BgRejectDone value)? bgRejectDone,
    TResult? Function(_BgRejectError value)? bgRejectError,
  }) {
    return bgRejectDone?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitRejection value)? submitRejection,
    TResult Function(_BgRejectDone value)? bgRejectDone,
    TResult Function(_BgRejectError value)? bgRejectError,
    required TResult orElse(),
  }) {
    if (bgRejectDone != null) {
      return bgRejectDone(this);
    }
    return orElse();
  }
}

abstract class _BgRejectDone implements RejectionEvent {
  const factory _BgRejectDone({required final String projectId}) =
      _$BgRejectDoneImpl;

  @override
  String get projectId;
  @override
  @JsonKey(ignore: true)
  _$$BgRejectDoneImplCopyWith<_$BgRejectDoneImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$BgRejectErrorImplCopyWith<$Res>
    implements $RejectionEventCopyWith<$Res> {
  factory _$$BgRejectErrorImplCopyWith(
          _$BgRejectErrorImpl value, $Res Function(_$BgRejectErrorImpl) then) =
      __$$BgRejectErrorImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId, String? message});
}

/// @nodoc
class __$$BgRejectErrorImplCopyWithImpl<$Res>
    extends _$RejectionEventCopyWithImpl<$Res, _$BgRejectErrorImpl>
    implements _$$BgRejectErrorImplCopyWith<$Res> {
  __$$BgRejectErrorImplCopyWithImpl(
      _$BgRejectErrorImpl _value, $Res Function(_$BgRejectErrorImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? message = freezed,
  }) {
    return _then(_$BgRejectErrorImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      message: freezed == message
          ? _value.message
          : message // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc

class _$BgRejectErrorImpl implements _BgRejectError {
  const _$BgRejectErrorImpl({required this.projectId, this.message});

  @override
  final String projectId;
  @override
  final String? message;

  @override
  String toString() {
    return 'RejectionEvent.bgRejectError(projectId: $projectId, message: $message)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BgRejectErrorImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.message, message) || other.message == message));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, message);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$BgRejectErrorImplCopyWith<_$BgRejectErrorImpl> get copyWith =>
      __$$BgRejectErrorImplCopyWithImpl<_$BgRejectErrorImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String projectId, String userType, List<dynamic> transactions)
        submitRejection,
    required TResult Function(String projectId) bgRejectDone,
    required TResult Function(String projectId, String? message) bgRejectError,
  }) {
    return bgRejectError(projectId, message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult? Function(String projectId)? bgRejectDone,
    TResult? Function(String projectId, String? message)? bgRejectError,
  }) {
    return bgRejectError?.call(projectId, message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String projectId, String userType, List<dynamic> transactions)?
        submitRejection,
    TResult Function(String projectId)? bgRejectDone,
    TResult Function(String projectId, String? message)? bgRejectError,
    required TResult orElse(),
  }) {
    if (bgRejectError != null) {
      return bgRejectError(projectId, message);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SubmitRejection value) submitRejection,
    required TResult Function(_BgRejectDone value) bgRejectDone,
    required TResult Function(_BgRejectError value) bgRejectError,
  }) {
    return bgRejectError(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SubmitRejection value)? submitRejection,
    TResult? Function(_BgRejectDone value)? bgRejectDone,
    TResult? Function(_BgRejectError value)? bgRejectError,
  }) {
    return bgRejectError?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SubmitRejection value)? submitRejection,
    TResult Function(_BgRejectDone value)? bgRejectDone,
    TResult Function(_BgRejectError value)? bgRejectError,
    required TResult orElse(),
  }) {
    if (bgRejectError != null) {
      return bgRejectError(this);
    }
    return orElse();
  }
}

abstract class _BgRejectError implements RejectionEvent {
  const factory _BgRejectError(
      {required final String projectId,
      final String? message}) = _$BgRejectErrorImpl;

  @override
  String get projectId;
  String? get message;
  @override
  @JsonKey(ignore: true)
  _$$BgRejectErrorImplCopyWith<_$BgRejectErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$RejectionState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function()? success,
    TResult? Function(String errorMessage)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function()? success,
    TResult Function(String errorMessage)? failure,
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
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
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

abstract class _Initial implements RejectionState {
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
    extends _$RejectionStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'RejectionState.loading()';
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

abstract class _Loading implements RejectionState {
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
    required TResult Function() loading,
    required TResult Function() success,
    required TResult Function(String errorMessage) failure,
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

abstract class _Success implements RejectionState {
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
    extends _$RejectionStateCopyWithImpl<$Res, _$FailureImpl>
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
    return 'RejectionState.failure(errorMessage: $errorMessage)';
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

abstract class _Failure implements RejectionState {
  const factory _Failure(final String errorMessage) = _$FailureImpl;

  String get errorMessage;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
