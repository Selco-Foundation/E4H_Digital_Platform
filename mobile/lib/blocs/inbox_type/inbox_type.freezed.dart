// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'inbox_type.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$InboxTypeState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() submitted,
    required TResult Function() rejected,
    required TResult Function() approved,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? submitted,
    TResult? Function()? rejected,
    TResult? Function()? approved,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? submitted,
    TResult Function()? rejected,
    TResult Function()? approved,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(InboxTypeSubmitted value) submitted,
    required TResult Function(InboxTypeRejected value) rejected,
    required TResult Function(InboxTypeApproved value) approved,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(InboxTypeSubmitted value)? submitted,
    TResult? Function(InboxTypeRejected value)? rejected,
    TResult? Function(InboxTypeApproved value)? approved,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(InboxTypeSubmitted value)? submitted,
    TResult Function(InboxTypeRejected value)? rejected,
    TResult Function(InboxTypeApproved value)? approved,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxTypeStateCopyWith<$Res> {
  factory $InboxTypeStateCopyWith(
          InboxTypeState value, $Res Function(InboxTypeState) then) =
      _$InboxTypeStateCopyWithImpl<$Res, InboxTypeState>;
}

/// @nodoc
class _$InboxTypeStateCopyWithImpl<$Res, $Val extends InboxTypeState>
    implements $InboxTypeStateCopyWith<$Res> {
  _$InboxTypeStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$InboxTypeSubmittedImplCopyWith<$Res> {
  factory _$$InboxTypeSubmittedImplCopyWith(_$InboxTypeSubmittedImpl value,
          $Res Function(_$InboxTypeSubmittedImpl) then) =
      __$$InboxTypeSubmittedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$InboxTypeSubmittedImplCopyWithImpl<$Res>
    extends _$InboxTypeStateCopyWithImpl<$Res, _$InboxTypeSubmittedImpl>
    implements _$$InboxTypeSubmittedImplCopyWith<$Res> {
  __$$InboxTypeSubmittedImplCopyWithImpl(_$InboxTypeSubmittedImpl _value,
      $Res Function(_$InboxTypeSubmittedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$InboxTypeSubmittedImpl implements InboxTypeSubmitted {
  const _$InboxTypeSubmittedImpl();

  @override
  String toString() {
    return 'InboxTypeState.submitted()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$InboxTypeSubmittedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() submitted,
    required TResult Function() rejected,
    required TResult Function() approved,
  }) {
    return submitted();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? submitted,
    TResult? Function()? rejected,
    TResult? Function()? approved,
  }) {
    return submitted?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? submitted,
    TResult Function()? rejected,
    TResult Function()? approved,
    required TResult orElse(),
  }) {
    if (submitted != null) {
      return submitted();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(InboxTypeSubmitted value) submitted,
    required TResult Function(InboxTypeRejected value) rejected,
    required TResult Function(InboxTypeApproved value) approved,
  }) {
    return submitted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(InboxTypeSubmitted value)? submitted,
    TResult? Function(InboxTypeRejected value)? rejected,
    TResult? Function(InboxTypeApproved value)? approved,
  }) {
    return submitted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(InboxTypeSubmitted value)? submitted,
    TResult Function(InboxTypeRejected value)? rejected,
    TResult Function(InboxTypeApproved value)? approved,
    required TResult orElse(),
  }) {
    if (submitted != null) {
      return submitted(this);
    }
    return orElse();
  }
}

abstract class InboxTypeSubmitted implements InboxTypeState {
  const factory InboxTypeSubmitted() = _$InboxTypeSubmittedImpl;
}

/// @nodoc
abstract class _$$InboxTypeRejectedImplCopyWith<$Res> {
  factory _$$InboxTypeRejectedImplCopyWith(_$InboxTypeRejectedImpl value,
          $Res Function(_$InboxTypeRejectedImpl) then) =
      __$$InboxTypeRejectedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$InboxTypeRejectedImplCopyWithImpl<$Res>
    extends _$InboxTypeStateCopyWithImpl<$Res, _$InboxTypeRejectedImpl>
    implements _$$InboxTypeRejectedImplCopyWith<$Res> {
  __$$InboxTypeRejectedImplCopyWithImpl(_$InboxTypeRejectedImpl _value,
      $Res Function(_$InboxTypeRejectedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$InboxTypeRejectedImpl implements InboxTypeRejected {
  const _$InboxTypeRejectedImpl();

  @override
  String toString() {
    return 'InboxTypeState.rejected()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$InboxTypeRejectedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() submitted,
    required TResult Function() rejected,
    required TResult Function() approved,
  }) {
    return rejected();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? submitted,
    TResult? Function()? rejected,
    TResult? Function()? approved,
  }) {
    return rejected?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? submitted,
    TResult Function()? rejected,
    TResult Function()? approved,
    required TResult orElse(),
  }) {
    if (rejected != null) {
      return rejected();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(InboxTypeSubmitted value) submitted,
    required TResult Function(InboxTypeRejected value) rejected,
    required TResult Function(InboxTypeApproved value) approved,
  }) {
    return rejected(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(InboxTypeSubmitted value)? submitted,
    TResult? Function(InboxTypeRejected value)? rejected,
    TResult? Function(InboxTypeApproved value)? approved,
  }) {
    return rejected?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(InboxTypeSubmitted value)? submitted,
    TResult Function(InboxTypeRejected value)? rejected,
    TResult Function(InboxTypeApproved value)? approved,
    required TResult orElse(),
  }) {
    if (rejected != null) {
      return rejected(this);
    }
    return orElse();
  }
}

abstract class InboxTypeRejected implements InboxTypeState {
  const factory InboxTypeRejected() = _$InboxTypeRejectedImpl;
}

/// @nodoc
abstract class _$$InboxTypeApprovedImplCopyWith<$Res> {
  factory _$$InboxTypeApprovedImplCopyWith(_$InboxTypeApprovedImpl value,
          $Res Function(_$InboxTypeApprovedImpl) then) =
      __$$InboxTypeApprovedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$InboxTypeApprovedImplCopyWithImpl<$Res>
    extends _$InboxTypeStateCopyWithImpl<$Res, _$InboxTypeApprovedImpl>
    implements _$$InboxTypeApprovedImplCopyWith<$Res> {
  __$$InboxTypeApprovedImplCopyWithImpl(_$InboxTypeApprovedImpl _value,
      $Res Function(_$InboxTypeApprovedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$InboxTypeApprovedImpl implements InboxTypeApproved {
  const _$InboxTypeApprovedImpl();

  @override
  String toString() {
    return 'InboxTypeState.approved()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$InboxTypeApprovedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() submitted,
    required TResult Function() rejected,
    required TResult Function() approved,
  }) {
    return approved();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? submitted,
    TResult? Function()? rejected,
    TResult? Function()? approved,
  }) {
    return approved?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? submitted,
    TResult Function()? rejected,
    TResult Function()? approved,
    required TResult orElse(),
  }) {
    if (approved != null) {
      return approved();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(InboxTypeSubmitted value) submitted,
    required TResult Function(InboxTypeRejected value) rejected,
    required TResult Function(InboxTypeApproved value) approved,
  }) {
    return approved(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(InboxTypeSubmitted value)? submitted,
    TResult? Function(InboxTypeRejected value)? rejected,
    TResult? Function(InboxTypeApproved value)? approved,
  }) {
    return approved?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(InboxTypeSubmitted value)? submitted,
    TResult Function(InboxTypeRejected value)? rejected,
    TResult Function(InboxTypeApproved value)? approved,
    required TResult orElse(),
  }) {
    if (approved != null) {
      return approved(this);
    }
    return orElse();
  }
}

abstract class InboxTypeApproved implements InboxTypeState {
  const factory InboxTypeApproved() = _$InboxTypeApprovedImpl;
}

/// @nodoc
mixin _$InboxTypeEvent {
  int get inboxType => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(int inboxType) typeSelected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(int inboxType)? typeSelected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(int inboxType)? typeSelected,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(InboxTypeSelected value) typeSelected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(InboxTypeSelected value)? typeSelected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(InboxTypeSelected value)? typeSelected,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $InboxTypeEventCopyWith<InboxTypeEvent> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxTypeEventCopyWith<$Res> {
  factory $InboxTypeEventCopyWith(
          InboxTypeEvent value, $Res Function(InboxTypeEvent) then) =
      _$InboxTypeEventCopyWithImpl<$Res, InboxTypeEvent>;
  @useResult
  $Res call({int inboxType});
}

/// @nodoc
class _$InboxTypeEventCopyWithImpl<$Res, $Val extends InboxTypeEvent>
    implements $InboxTypeEventCopyWith<$Res> {
  _$InboxTypeEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? inboxType = null,
  }) {
    return _then(_value.copyWith(
      inboxType: null == inboxType
          ? _value.inboxType
          : inboxType // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$InboxTypeSelectedImplCopyWith<$Res>
    implements $InboxTypeEventCopyWith<$Res> {
  factory _$$InboxTypeSelectedImplCopyWith(_$InboxTypeSelectedImpl value,
          $Res Function(_$InboxTypeSelectedImpl) then) =
      __$$InboxTypeSelectedImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int inboxType});
}

/// @nodoc
class __$$InboxTypeSelectedImplCopyWithImpl<$Res>
    extends _$InboxTypeEventCopyWithImpl<$Res, _$InboxTypeSelectedImpl>
    implements _$$InboxTypeSelectedImplCopyWith<$Res> {
  __$$InboxTypeSelectedImplCopyWithImpl(_$InboxTypeSelectedImpl _value,
      $Res Function(_$InboxTypeSelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? inboxType = null,
  }) {
    return _then(_$InboxTypeSelectedImpl(
      null == inboxType
          ? _value.inboxType
          : inboxType // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$InboxTypeSelectedImpl implements InboxTypeSelected {
  const _$InboxTypeSelectedImpl(this.inboxType);

  @override
  final int inboxType;

  @override
  String toString() {
    return 'InboxTypeEvent.typeSelected(inboxType: $inboxType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InboxTypeSelectedImpl &&
            (identical(other.inboxType, inboxType) ||
                other.inboxType == inboxType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, inboxType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$InboxTypeSelectedImplCopyWith<_$InboxTypeSelectedImpl> get copyWith =>
      __$$InboxTypeSelectedImplCopyWithImpl<_$InboxTypeSelectedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(int inboxType) typeSelected,
  }) {
    return typeSelected(inboxType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(int inboxType)? typeSelected,
  }) {
    return typeSelected?.call(inboxType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(int inboxType)? typeSelected,
    required TResult orElse(),
  }) {
    if (typeSelected != null) {
      return typeSelected(inboxType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(InboxTypeSelected value) typeSelected,
  }) {
    return typeSelected(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(InboxTypeSelected value)? typeSelected,
  }) {
    return typeSelected?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(InboxTypeSelected value)? typeSelected,
    required TResult orElse(),
  }) {
    if (typeSelected != null) {
      return typeSelected(this);
    }
    return orElse();
  }
}

abstract class InboxTypeSelected implements InboxTypeEvent {
  const factory InboxTypeSelected(final int inboxType) =
      _$InboxTypeSelectedImpl;

  @override
  int get inboxType;
  @override
  @JsonKey(ignore: true)
  _$$InboxTypeSelectedImplCopyWith<_$InboxTypeSelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
