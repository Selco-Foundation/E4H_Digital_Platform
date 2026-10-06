// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'selected_scheduled_visit.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SelectedScheduledVisitEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ScheduledVisit scheduledVisit) select,
    required TResult Function() deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ScheduledVisit scheduledVisit)? select,
    TResult? Function()? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ScheduledVisit scheduledVisit)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ScheduledVisitSelected value) select,
    required TResult Function(ScheduledVisitDeselected value) deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ScheduledVisitSelected value)? select,
    TResult? Function(ScheduledVisitDeselected value)? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ScheduledVisitSelected value)? select,
    TResult Function(ScheduledVisitDeselected value)? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SelectedScheduledVisitEventCopyWith<$Res> {
  factory $SelectedScheduledVisitEventCopyWith(
          SelectedScheduledVisitEvent value,
          $Res Function(SelectedScheduledVisitEvent) then) =
      _$SelectedScheduledVisitEventCopyWithImpl<$Res,
          SelectedScheduledVisitEvent>;
}

/// @nodoc
class _$SelectedScheduledVisitEventCopyWithImpl<$Res,
        $Val extends SelectedScheduledVisitEvent>
    implements $SelectedScheduledVisitEventCopyWith<$Res> {
  _$SelectedScheduledVisitEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ScheduledVisitSelectedImplCopyWith<$Res> {
  factory _$$ScheduledVisitSelectedImplCopyWith(
          _$ScheduledVisitSelectedImpl value,
          $Res Function(_$ScheduledVisitSelectedImpl) then) =
      __$$ScheduledVisitSelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ScheduledVisit scheduledVisit});

  $ScheduledVisitCopyWith<$Res> get scheduledVisit;
}

/// @nodoc
class __$$ScheduledVisitSelectedImplCopyWithImpl<$Res>
    extends _$SelectedScheduledVisitEventCopyWithImpl<$Res,
        _$ScheduledVisitSelectedImpl>
    implements _$$ScheduledVisitSelectedImplCopyWith<$Res> {
  __$$ScheduledVisitSelectedImplCopyWithImpl(
      _$ScheduledVisitSelectedImpl _value,
      $Res Function(_$ScheduledVisitSelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisit = null,
  }) {
    return _then(_$ScheduledVisitSelectedImpl(
      null == scheduledVisit
          ? _value.scheduledVisit
          : scheduledVisit // ignore: cast_nullable_to_non_nullable
              as ScheduledVisit,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ScheduledVisitCopyWith<$Res> get scheduledVisit {
    return $ScheduledVisitCopyWith<$Res>(_value.scheduledVisit, (value) {
      return _then(_value.copyWith(scheduledVisit: value));
    });
  }
}

/// @nodoc

class _$ScheduledVisitSelectedImpl implements ScheduledVisitSelected {
  const _$ScheduledVisitSelectedImpl(this.scheduledVisit);

  @override
  final ScheduledVisit scheduledVisit;

  @override
  String toString() {
    return 'SelectedScheduledVisitEvent.select(scheduledVisit: $scheduledVisit)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitSelectedImpl &&
            (identical(other.scheduledVisit, scheduledVisit) ||
                other.scheduledVisit == scheduledVisit));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisit);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ScheduledVisitSelectedImplCopyWith<_$ScheduledVisitSelectedImpl>
      get copyWith => __$$ScheduledVisitSelectedImplCopyWithImpl<
          _$ScheduledVisitSelectedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ScheduledVisit scheduledVisit) select,
    required TResult Function() deselect,
  }) {
    return select(scheduledVisit);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ScheduledVisit scheduledVisit)? select,
    TResult? Function()? deselect,
  }) {
    return select?.call(scheduledVisit);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ScheduledVisit scheduledVisit)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(scheduledVisit);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ScheduledVisitSelected value) select,
    required TResult Function(ScheduledVisitDeselected value) deselect,
  }) {
    return select(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ScheduledVisitSelected value)? select,
    TResult? Function(ScheduledVisitDeselected value)? deselect,
  }) {
    return select?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ScheduledVisitSelected value)? select,
    TResult Function(ScheduledVisitDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(this);
    }
    return orElse();
  }
}

abstract class ScheduledVisitSelected implements SelectedScheduledVisitEvent {
  const factory ScheduledVisitSelected(final ScheduledVisit scheduledVisit) =
      _$ScheduledVisitSelectedImpl;

  ScheduledVisit get scheduledVisit;
  @JsonKey(ignore: true)
  _$$ScheduledVisitSelectedImplCopyWith<_$ScheduledVisitSelectedImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ScheduledVisitDeselectedImplCopyWith<$Res> {
  factory _$$ScheduledVisitDeselectedImplCopyWith(
          _$ScheduledVisitDeselectedImpl value,
          $Res Function(_$ScheduledVisitDeselectedImpl) then) =
      __$$ScheduledVisitDeselectedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ScheduledVisitDeselectedImplCopyWithImpl<$Res>
    extends _$SelectedScheduledVisitEventCopyWithImpl<$Res,
        _$ScheduledVisitDeselectedImpl>
    implements _$$ScheduledVisitDeselectedImplCopyWith<$Res> {
  __$$ScheduledVisitDeselectedImplCopyWithImpl(
      _$ScheduledVisitDeselectedImpl _value,
      $Res Function(_$ScheduledVisitDeselectedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ScheduledVisitDeselectedImpl implements ScheduledVisitDeselected {
  const _$ScheduledVisitDeselectedImpl();

  @override
  String toString() {
    return 'SelectedScheduledVisitEvent.deselect()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ScheduledVisitDeselectedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ScheduledVisit scheduledVisit) select,
    required TResult Function() deselect,
  }) {
    return deselect();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ScheduledVisit scheduledVisit)? select,
    TResult? Function()? deselect,
  }) {
    return deselect?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ScheduledVisit scheduledVisit)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) {
    if (deselect != null) {
      return deselect();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ScheduledVisitSelected value) select,
    required TResult Function(ScheduledVisitDeselected value) deselect,
  }) {
    return deselect(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ScheduledVisitSelected value)? select,
    TResult? Function(ScheduledVisitDeselected value)? deselect,
  }) {
    return deselect?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ScheduledVisitSelected value)? select,
    TResult Function(ScheduledVisitDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (deselect != null) {
      return deselect(this);
    }
    return orElse();
  }
}

abstract class ScheduledVisitDeselected implements SelectedScheduledVisitEvent {
  const factory ScheduledVisitDeselected() = _$ScheduledVisitDeselectedImpl;
}

/// @nodoc
mixin _$SelectedScheduledVisitState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(ScheduledVisit scheduledVisit) selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ScheduledVisit scheduledVisit)? selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ScheduledVisit scheduledVisit)? selected,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Selected value) selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Selected value)? selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Selected value)? selected,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SelectedScheduledVisitStateCopyWith<$Res> {
  factory $SelectedScheduledVisitStateCopyWith(
          SelectedScheduledVisitState value,
          $Res Function(SelectedScheduledVisitState) then) =
      _$SelectedScheduledVisitStateCopyWithImpl<$Res,
          SelectedScheduledVisitState>;
}

/// @nodoc
class _$SelectedScheduledVisitStateCopyWithImpl<$Res,
        $Val extends SelectedScheduledVisitState>
    implements $SelectedScheduledVisitStateCopyWith<$Res> {
  _$SelectedScheduledVisitStateCopyWithImpl(this._value, this._then);

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
    extends _$SelectedScheduledVisitStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'SelectedScheduledVisitState.initial()';
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
    required TResult Function(ScheduledVisit scheduledVisit) selected,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ScheduledVisit scheduledVisit)? selected,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ScheduledVisit scheduledVisit)? selected,
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
    required TResult Function(_Selected value) selected,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Selected value)? selected,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Selected value)? selected,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements SelectedScheduledVisitState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$SelectedImplCopyWith<$Res> {
  factory _$$SelectedImplCopyWith(
          _$SelectedImpl value, $Res Function(_$SelectedImpl) then) =
      __$$SelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ScheduledVisit scheduledVisit});

  $ScheduledVisitCopyWith<$Res> get scheduledVisit;
}

/// @nodoc
class __$$SelectedImplCopyWithImpl<$Res>
    extends _$SelectedScheduledVisitStateCopyWithImpl<$Res, _$SelectedImpl>
    implements _$$SelectedImplCopyWith<$Res> {
  __$$SelectedImplCopyWithImpl(
      _$SelectedImpl _value, $Res Function(_$SelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisit = null,
  }) {
    return _then(_$SelectedImpl(
      null == scheduledVisit
          ? _value.scheduledVisit
          : scheduledVisit // ignore: cast_nullable_to_non_nullable
              as ScheduledVisit,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ScheduledVisitCopyWith<$Res> get scheduledVisit {
    return $ScheduledVisitCopyWith<$Res>(_value.scheduledVisit, (value) {
      return _then(_value.copyWith(scheduledVisit: value));
    });
  }
}

/// @nodoc

class _$SelectedImpl implements _Selected {
  const _$SelectedImpl(this.scheduledVisit);

  @override
  final ScheduledVisit scheduledVisit;

  @override
  String toString() {
    return 'SelectedScheduledVisitState.selected(scheduledVisit: $scheduledVisit)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SelectedImpl &&
            (identical(other.scheduledVisit, scheduledVisit) ||
                other.scheduledVisit == scheduledVisit));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisit);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      __$$SelectedImplCopyWithImpl<_$SelectedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(ScheduledVisit scheduledVisit) selected,
  }) {
    return selected(scheduledVisit);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ScheduledVisit scheduledVisit)? selected,
  }) {
    return selected?.call(scheduledVisit);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ScheduledVisit scheduledVisit)? selected,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(scheduledVisit);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Selected value) selected,
  }) {
    return selected(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Selected value)? selected,
  }) {
    return selected?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Selected value)? selected,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(this);
    }
    return orElse();
  }
}

abstract class _Selected implements SelectedScheduledVisitState {
  const factory _Selected(final ScheduledVisit scheduledVisit) = _$SelectedImpl;

  ScheduledVisit get scheduledVisit;
  @JsonKey(ignore: true)
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
