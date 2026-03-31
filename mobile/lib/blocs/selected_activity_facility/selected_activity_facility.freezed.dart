// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'selected_activity_facility.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SelectedActivityFacilityEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ActivityFacilityWorkflow activityFacility) select,
    required TResult Function() deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilityWorkflow activityFacility)? select,
    TResult? Function()? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ActivityFacilityWorkflow activityFacility)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelected value) select,
    required TResult Function(ActivityFacilityDeselected value) deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelected value)? select,
    TResult? Function(ActivityFacilityDeselected value)? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelected value)? select,
    TResult Function(ActivityFacilityDeselected value)? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SelectedActivityFacilityEventCopyWith<$Res> {
  factory $SelectedActivityFacilityEventCopyWith(
          SelectedActivityFacilityEvent value,
          $Res Function(SelectedActivityFacilityEvent) then) =
      _$SelectedActivityFacilityEventCopyWithImpl<$Res,
          SelectedActivityFacilityEvent>;
}

/// @nodoc
class _$SelectedActivityFacilityEventCopyWithImpl<$Res,
        $Val extends SelectedActivityFacilityEvent>
    implements $SelectedActivityFacilityEventCopyWith<$Res> {
  _$SelectedActivityFacilityEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ActivityFacilitySelectedImplCopyWith<$Res> {
  factory _$$ActivityFacilitySelectedImplCopyWith(
          _$ActivityFacilitySelectedImpl value,
          $Res Function(_$ActivityFacilitySelectedImpl) then) =
      __$$ActivityFacilitySelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ActivityFacilityWorkflow activityFacility});

  $ActivityFacilityWorkflowCopyWith<$Res> get activityFacility;
}

/// @nodoc
class __$$ActivityFacilitySelectedImplCopyWithImpl<$Res>
    extends _$SelectedActivityFacilityEventCopyWithImpl<$Res,
        _$ActivityFacilitySelectedImpl>
    implements _$$ActivityFacilitySelectedImplCopyWith<$Res> {
  __$$ActivityFacilitySelectedImplCopyWithImpl(
      _$ActivityFacilitySelectedImpl _value,
      $Res Function(_$ActivityFacilitySelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacility = null,
  }) {
    return _then(_$ActivityFacilitySelectedImpl(
      null == activityFacility
          ? _value.activityFacility
          : activityFacility // ignore: cast_nullable_to_non_nullable
              as ActivityFacilityWorkflow,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ActivityFacilityWorkflowCopyWith<$Res> get activityFacility {
    return $ActivityFacilityWorkflowCopyWith<$Res>(_value.activityFacility,
        (value) {
      return _then(_value.copyWith(activityFacility: value));
    });
  }
}

/// @nodoc

class _$ActivityFacilitySelectedImpl implements ActivityFacilitySelected {
  const _$ActivityFacilitySelectedImpl(this.activityFacility);

  @override
  final ActivityFacilityWorkflow activityFacility;

  @override
  String toString() {
    return 'SelectedActivityFacilityEvent.select(activityFacility: $activityFacility)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilitySelectedImpl &&
            (identical(other.activityFacility, activityFacility) ||
                other.activityFacility == activityFacility));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacility);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ActivityFacilitySelectedImplCopyWith<_$ActivityFacilitySelectedImpl>
      get copyWith => __$$ActivityFacilitySelectedImplCopyWithImpl<
          _$ActivityFacilitySelectedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ActivityFacilityWorkflow activityFacility) select,
    required TResult Function() deselect,
  }) {
    return select(activityFacility);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilityWorkflow activityFacility)? select,
    TResult? Function()? deselect,
  }) {
    return select?.call(activityFacility);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ActivityFacilityWorkflow activityFacility)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(activityFacility);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ActivityFacilitySelected value) select,
    required TResult Function(ActivityFacilityDeselected value) deselect,
  }) {
    return select(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelected value)? select,
    TResult? Function(ActivityFacilityDeselected value)? deselect,
  }) {
    return select?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelected value)? select,
    TResult Function(ActivityFacilityDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilitySelected
    implements SelectedActivityFacilityEvent {
  const factory ActivityFacilitySelected(
          final ActivityFacilityWorkflow activityFacility) =
      _$ActivityFacilitySelectedImpl;

  ActivityFacilityWorkflow get activityFacility;
  @JsonKey(ignore: true)
  _$$ActivityFacilitySelectedImplCopyWith<_$ActivityFacilitySelectedImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ActivityFacilityDeselectedImplCopyWith<$Res> {
  factory _$$ActivityFacilityDeselectedImplCopyWith(
          _$ActivityFacilityDeselectedImpl value,
          $Res Function(_$ActivityFacilityDeselectedImpl) then) =
      __$$ActivityFacilityDeselectedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ActivityFacilityDeselectedImplCopyWithImpl<$Res>
    extends _$SelectedActivityFacilityEventCopyWithImpl<$Res,
        _$ActivityFacilityDeselectedImpl>
    implements _$$ActivityFacilityDeselectedImplCopyWith<$Res> {
  __$$ActivityFacilityDeselectedImplCopyWithImpl(
      _$ActivityFacilityDeselectedImpl _value,
      $Res Function(_$ActivityFacilityDeselectedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ActivityFacilityDeselectedImpl implements ActivityFacilityDeselected {
  const _$ActivityFacilityDeselectedImpl();

  @override
  String toString() {
    return 'SelectedActivityFacilityEvent.deselect()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ActivityFacilityDeselectedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ActivityFacilityWorkflow activityFacility) select,
    required TResult Function() deselect,
  }) {
    return deselect();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilityWorkflow activityFacility)? select,
    TResult? Function()? deselect,
  }) {
    return deselect?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ActivityFacilityWorkflow activityFacility)? select,
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
    required TResult Function(ActivityFacilitySelected value) select,
    required TResult Function(ActivityFacilityDeselected value) deselect,
  }) {
    return deselect(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ActivityFacilitySelected value)? select,
    TResult? Function(ActivityFacilityDeselected value)? deselect,
  }) {
    return deselect?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ActivityFacilitySelected value)? select,
    TResult Function(ActivityFacilityDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (deselect != null) {
      return deselect(this);
    }
    return orElse();
  }
}

abstract class ActivityFacilityDeselected
    implements SelectedActivityFacilityEvent {
  const factory ActivityFacilityDeselected() = _$ActivityFacilityDeselectedImpl;
}

/// @nodoc
mixin _$SelectedActivityFacilityState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(ActivityFacilityWorkflow activityFacility)
        selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ActivityFacilityWorkflow activityFacility)? selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ActivityFacilityWorkflow activityFacility)? selected,
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
abstract class $SelectedActivityFacilityStateCopyWith<$Res> {
  factory $SelectedActivityFacilityStateCopyWith(
          SelectedActivityFacilityState value,
          $Res Function(SelectedActivityFacilityState) then) =
      _$SelectedActivityFacilityStateCopyWithImpl<$Res,
          SelectedActivityFacilityState>;
}

/// @nodoc
class _$SelectedActivityFacilityStateCopyWithImpl<$Res,
        $Val extends SelectedActivityFacilityState>
    implements $SelectedActivityFacilityStateCopyWith<$Res> {
  _$SelectedActivityFacilityStateCopyWithImpl(this._value, this._then);

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
    extends _$SelectedActivityFacilityStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'SelectedActivityFacilityState.initial()';
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
    required TResult Function(ActivityFacilityWorkflow activityFacility)
        selected,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ActivityFacilityWorkflow activityFacility)? selected,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ActivityFacilityWorkflow activityFacility)? selected,
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

abstract class _Initial implements SelectedActivityFacilityState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$SelectedImplCopyWith<$Res> {
  factory _$$SelectedImplCopyWith(
          _$SelectedImpl value, $Res Function(_$SelectedImpl) then) =
      __$$SelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ActivityFacilityWorkflow activityFacility});

  $ActivityFacilityWorkflowCopyWith<$Res> get activityFacility;
}

/// @nodoc
class __$$SelectedImplCopyWithImpl<$Res>
    extends _$SelectedActivityFacilityStateCopyWithImpl<$Res, _$SelectedImpl>
    implements _$$SelectedImplCopyWith<$Res> {
  __$$SelectedImplCopyWithImpl(
      _$SelectedImpl _value, $Res Function(_$SelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacility = null,
  }) {
    return _then(_$SelectedImpl(
      null == activityFacility
          ? _value.activityFacility
          : activityFacility // ignore: cast_nullable_to_non_nullable
              as ActivityFacilityWorkflow,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ActivityFacilityWorkflowCopyWith<$Res> get activityFacility {
    return $ActivityFacilityWorkflowCopyWith<$Res>(_value.activityFacility,
        (value) {
      return _then(_value.copyWith(activityFacility: value));
    });
  }
}

/// @nodoc

class _$SelectedImpl implements _Selected {
  const _$SelectedImpl(this.activityFacility);

  @override
  final ActivityFacilityWorkflow activityFacility;

  @override
  String toString() {
    return 'SelectedActivityFacilityState.selected(activityFacility: $activityFacility)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SelectedImpl &&
            (identical(other.activityFacility, activityFacility) ||
                other.activityFacility == activityFacility));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacility);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      __$$SelectedImplCopyWithImpl<_$SelectedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(ActivityFacilityWorkflow activityFacility)
        selected,
  }) {
    return selected(activityFacility);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ActivityFacilityWorkflow activityFacility)? selected,
  }) {
    return selected?.call(activityFacility);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ActivityFacilityWorkflow activityFacility)? selected,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(activityFacility);
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

abstract class _Selected implements SelectedActivityFacilityState {
  const factory _Selected(final ActivityFacilityWorkflow activityFacility) =
      _$SelectedImpl;

  ActivityFacilityWorkflow get activityFacility;
  @JsonKey(ignore: true)
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
