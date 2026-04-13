// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'selected_amc_origin.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SelectedAmcOriginEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(FormOrigin origin) select,
    required TResult Function() deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(FormOrigin origin)? select,
    TResult? Function()? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(FormOrigin origin)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(AmcOriginSelected value) select,
    required TResult Function(AmcOriginDeselected value) deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(AmcOriginSelected value)? select,
    TResult? Function(AmcOriginDeselected value)? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(AmcOriginSelected value)? select,
    TResult Function(AmcOriginDeselected value)? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SelectedAmcOriginEventCopyWith<$Res> {
  factory $SelectedAmcOriginEventCopyWith(SelectedAmcOriginEvent value,
          $Res Function(SelectedAmcOriginEvent) then) =
      _$SelectedAmcOriginEventCopyWithImpl<$Res, SelectedAmcOriginEvent>;
}

/// @nodoc
class _$SelectedAmcOriginEventCopyWithImpl<$Res,
        $Val extends SelectedAmcOriginEvent>
    implements $SelectedAmcOriginEventCopyWith<$Res> {
  _$SelectedAmcOriginEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$AmcOriginSelectedImplCopyWith<$Res> {
  factory _$$AmcOriginSelectedImplCopyWith(_$AmcOriginSelectedImpl value,
          $Res Function(_$AmcOriginSelectedImpl) then) =
      __$$AmcOriginSelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({FormOrigin origin});
}

/// @nodoc
class __$$AmcOriginSelectedImplCopyWithImpl<$Res>
    extends _$SelectedAmcOriginEventCopyWithImpl<$Res, _$AmcOriginSelectedImpl>
    implements _$$AmcOriginSelectedImplCopyWith<$Res> {
  __$$AmcOriginSelectedImplCopyWithImpl(_$AmcOriginSelectedImpl _value,
      $Res Function(_$AmcOriginSelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? origin = null,
  }) {
    return _then(_$AmcOriginSelectedImpl(
      null == origin
          ? _value.origin
          : origin // ignore: cast_nullable_to_non_nullable
              as FormOrigin,
    ));
  }
}

/// @nodoc

class _$AmcOriginSelectedImpl implements AmcOriginSelected {
  const _$AmcOriginSelectedImpl(this.origin);

  @override
  final FormOrigin origin;

  @override
  String toString() {
    return 'SelectedAmcOriginEvent.select(origin: $origin)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AmcOriginSelectedImpl &&
            (identical(other.origin, origin) || other.origin == origin));
  }

  @override
  int get hashCode => Object.hash(runtimeType, origin);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AmcOriginSelectedImplCopyWith<_$AmcOriginSelectedImpl> get copyWith =>
      __$$AmcOriginSelectedImplCopyWithImpl<_$AmcOriginSelectedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(FormOrigin origin) select,
    required TResult Function() deselect,
  }) {
    return select(origin);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(FormOrigin origin)? select,
    TResult? Function()? deselect,
  }) {
    return select?.call(origin);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(FormOrigin origin)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(origin);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(AmcOriginSelected value) select,
    required TResult Function(AmcOriginDeselected value) deselect,
  }) {
    return select(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(AmcOriginSelected value)? select,
    TResult? Function(AmcOriginDeselected value)? deselect,
  }) {
    return select?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(AmcOriginSelected value)? select,
    TResult Function(AmcOriginDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(this);
    }
    return orElse();
  }
}

abstract class AmcOriginSelected implements SelectedAmcOriginEvent {
  const factory AmcOriginSelected(final FormOrigin origin) =
      _$AmcOriginSelectedImpl;

  FormOrigin get origin;
  @JsonKey(ignore: true)
  _$$AmcOriginSelectedImplCopyWith<_$AmcOriginSelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$AmcOriginDeselectedImplCopyWith<$Res> {
  factory _$$AmcOriginDeselectedImplCopyWith(_$AmcOriginDeselectedImpl value,
          $Res Function(_$AmcOriginDeselectedImpl) then) =
      __$$AmcOriginDeselectedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$AmcOriginDeselectedImplCopyWithImpl<$Res>
    extends _$SelectedAmcOriginEventCopyWithImpl<$Res,
        _$AmcOriginDeselectedImpl>
    implements _$$AmcOriginDeselectedImplCopyWith<$Res> {
  __$$AmcOriginDeselectedImplCopyWithImpl(_$AmcOriginDeselectedImpl _value,
      $Res Function(_$AmcOriginDeselectedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$AmcOriginDeselectedImpl implements AmcOriginDeselected {
  const _$AmcOriginDeselectedImpl();

  @override
  String toString() {
    return 'SelectedAmcOriginEvent.deselect()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AmcOriginDeselectedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(FormOrigin origin) select,
    required TResult Function() deselect,
  }) {
    return deselect();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(FormOrigin origin)? select,
    TResult? Function()? deselect,
  }) {
    return deselect?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(FormOrigin origin)? select,
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
    required TResult Function(AmcOriginSelected value) select,
    required TResult Function(AmcOriginDeselected value) deselect,
  }) {
    return deselect(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(AmcOriginSelected value)? select,
    TResult? Function(AmcOriginDeselected value)? deselect,
  }) {
    return deselect?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(AmcOriginSelected value)? select,
    TResult Function(AmcOriginDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (deselect != null) {
      return deselect(this);
    }
    return orElse();
  }
}

abstract class AmcOriginDeselected implements SelectedAmcOriginEvent {
  const factory AmcOriginDeselected() = _$AmcOriginDeselectedImpl;
}

/// @nodoc
mixin _$SelectedAmcOriginState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(FormOrigin origin) selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(FormOrigin origin)? selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(FormOrigin origin)? selected,
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
abstract class $SelectedAmcOriginStateCopyWith<$Res> {
  factory $SelectedAmcOriginStateCopyWith(SelectedAmcOriginState value,
          $Res Function(SelectedAmcOriginState) then) =
      _$SelectedAmcOriginStateCopyWithImpl<$Res, SelectedAmcOriginState>;
}

/// @nodoc
class _$SelectedAmcOriginStateCopyWithImpl<$Res,
        $Val extends SelectedAmcOriginState>
    implements $SelectedAmcOriginStateCopyWith<$Res> {
  _$SelectedAmcOriginStateCopyWithImpl(this._value, this._then);

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
    extends _$SelectedAmcOriginStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'SelectedAmcOriginState.initial()';
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
    required TResult Function(FormOrigin origin) selected,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(FormOrigin origin)? selected,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(FormOrigin origin)? selected,
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

abstract class _Initial implements SelectedAmcOriginState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$SelectedImplCopyWith<$Res> {
  factory _$$SelectedImplCopyWith(
          _$SelectedImpl value, $Res Function(_$SelectedImpl) then) =
      __$$SelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({FormOrigin origin});
}

/// @nodoc
class __$$SelectedImplCopyWithImpl<$Res>
    extends _$SelectedAmcOriginStateCopyWithImpl<$Res, _$SelectedImpl>
    implements _$$SelectedImplCopyWith<$Res> {
  __$$SelectedImplCopyWithImpl(
      _$SelectedImpl _value, $Res Function(_$SelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? origin = null,
  }) {
    return _then(_$SelectedImpl(
      null == origin
          ? _value.origin
          : origin // ignore: cast_nullable_to_non_nullable
              as FormOrigin,
    ));
  }
}

/// @nodoc

class _$SelectedImpl implements _Selected {
  const _$SelectedImpl(this.origin);

  @override
  final FormOrigin origin;

  @override
  String toString() {
    return 'SelectedAmcOriginState.selected(origin: $origin)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SelectedImpl &&
            (identical(other.origin, origin) || other.origin == origin));
  }

  @override
  int get hashCode => Object.hash(runtimeType, origin);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      __$$SelectedImplCopyWithImpl<_$SelectedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(FormOrigin origin) selected,
  }) {
    return selected(origin);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(FormOrigin origin)? selected,
  }) {
    return selected?.call(origin);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(FormOrigin origin)? selected,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(origin);
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

abstract class _Selected implements SelectedAmcOriginState {
  const factory _Selected(final FormOrigin origin) = _$SelectedImpl;

  FormOrigin get origin;
  @JsonKey(ignore: true)
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
