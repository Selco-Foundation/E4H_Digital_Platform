// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'selected_project.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SelectedProjectEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ProjectWorkflow project) select,
    required TResult Function() deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ProjectWorkflow project)? select,
    TResult? Function()? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ProjectWorkflow project)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelected value) select,
    required TResult Function(ProjectDeselected value) deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelected value)? select,
    TResult? Function(ProjectDeselected value)? deselect,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelected value)? select,
    TResult Function(ProjectDeselected value)? deselect,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SelectedProjectEventCopyWith<$Res> {
  factory $SelectedProjectEventCopyWith(SelectedProjectEvent value,
          $Res Function(SelectedProjectEvent) then) =
      _$SelectedProjectEventCopyWithImpl<$Res, SelectedProjectEvent>;
}

/// @nodoc
class _$SelectedProjectEventCopyWithImpl<$Res,
        $Val extends SelectedProjectEvent>
    implements $SelectedProjectEventCopyWith<$Res> {
  _$SelectedProjectEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$ProjectSelectedImplCopyWith<$Res> {
  factory _$$ProjectSelectedImplCopyWith(_$ProjectSelectedImpl value,
          $Res Function(_$ProjectSelectedImpl) then) =
      __$$ProjectSelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ProjectWorkflow project});

  $ProjectWorkflowCopyWith<$Res> get project;
}

/// @nodoc
class __$$ProjectSelectedImplCopyWithImpl<$Res>
    extends _$SelectedProjectEventCopyWithImpl<$Res, _$ProjectSelectedImpl>
    implements _$$ProjectSelectedImplCopyWith<$Res> {
  __$$ProjectSelectedImplCopyWithImpl(
      _$ProjectSelectedImpl _value, $Res Function(_$ProjectSelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? project = null,
  }) {
    return _then(_$ProjectSelectedImpl(
      null == project
          ? _value.project
          : project // ignore: cast_nullable_to_non_nullable
              as ProjectWorkflow,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ProjectWorkflowCopyWith<$Res> get project {
    return $ProjectWorkflowCopyWith<$Res>(_value.project, (value) {
      return _then(_value.copyWith(project: value));
    });
  }
}

/// @nodoc

class _$ProjectSelectedImpl implements ProjectSelected {
  const _$ProjectSelectedImpl(this.project);

  @override
  final ProjectWorkflow project;

  @override
  String toString() {
    return 'SelectedProjectEvent.select(project: $project)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProjectSelectedImpl &&
            (identical(other.project, project) || other.project == project));
  }

  @override
  int get hashCode => Object.hash(runtimeType, project);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ProjectSelectedImplCopyWith<_$ProjectSelectedImpl> get copyWith =>
      __$$ProjectSelectedImplCopyWithImpl<_$ProjectSelectedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ProjectWorkflow project) select,
    required TResult Function() deselect,
  }) {
    return select(project);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ProjectWorkflow project)? select,
    TResult? Function()? deselect,
  }) {
    return select?.call(project);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ProjectWorkflow project)? select,
    TResult Function()? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(project);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(ProjectSelected value) select,
    required TResult Function(ProjectDeselected value) deselect,
  }) {
    return select(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelected value)? select,
    TResult? Function(ProjectDeselected value)? deselect,
  }) {
    return select?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelected value)? select,
    TResult Function(ProjectDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (select != null) {
      return select(this);
    }
    return orElse();
  }
}

abstract class ProjectSelected implements SelectedProjectEvent {
  const factory ProjectSelected(final ProjectWorkflow project) =
      _$ProjectSelectedImpl;

  ProjectWorkflow get project;
  @JsonKey(ignore: true)
  _$$ProjectSelectedImplCopyWith<_$ProjectSelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ProjectDeselectedImplCopyWith<$Res> {
  factory _$$ProjectDeselectedImplCopyWith(_$ProjectDeselectedImpl value,
          $Res Function(_$ProjectDeselectedImpl) then) =
      __$$ProjectDeselectedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$ProjectDeselectedImplCopyWithImpl<$Res>
    extends _$SelectedProjectEventCopyWithImpl<$Res, _$ProjectDeselectedImpl>
    implements _$$ProjectDeselectedImplCopyWith<$Res> {
  __$$ProjectDeselectedImplCopyWithImpl(_$ProjectDeselectedImpl _value,
      $Res Function(_$ProjectDeselectedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$ProjectDeselectedImpl implements ProjectDeselected {
  const _$ProjectDeselectedImpl();

  @override
  String toString() {
    return 'SelectedProjectEvent.deselect()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$ProjectDeselectedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(ProjectWorkflow project) select,
    required TResult Function() deselect,
  }) {
    return deselect();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(ProjectWorkflow project)? select,
    TResult? Function()? deselect,
  }) {
    return deselect?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(ProjectWorkflow project)? select,
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
    required TResult Function(ProjectSelected value) select,
    required TResult Function(ProjectDeselected value) deselect,
  }) {
    return deselect(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(ProjectSelected value)? select,
    TResult? Function(ProjectDeselected value)? deselect,
  }) {
    return deselect?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(ProjectSelected value)? select,
    TResult Function(ProjectDeselected value)? deselect,
    required TResult orElse(),
  }) {
    if (deselect != null) {
      return deselect(this);
    }
    return orElse();
  }
}

abstract class ProjectDeselected implements SelectedProjectEvent {
  const factory ProjectDeselected() = _$ProjectDeselectedImpl;
}

/// @nodoc
mixin _$SelectedProjectState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(ProjectWorkflow project) selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ProjectWorkflow project)? selected,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ProjectWorkflow project)? selected,
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
abstract class $SelectedProjectStateCopyWith<$Res> {
  factory $SelectedProjectStateCopyWith(SelectedProjectState value,
          $Res Function(SelectedProjectState) then) =
      _$SelectedProjectStateCopyWithImpl<$Res, SelectedProjectState>;
}

/// @nodoc
class _$SelectedProjectStateCopyWithImpl<$Res,
        $Val extends SelectedProjectState>
    implements $SelectedProjectStateCopyWith<$Res> {
  _$SelectedProjectStateCopyWithImpl(this._value, this._then);

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
    extends _$SelectedProjectStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'SelectedProjectState.initial()';
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
    required TResult Function(ProjectWorkflow project) selected,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ProjectWorkflow project)? selected,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ProjectWorkflow project)? selected,
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

abstract class _Initial implements SelectedProjectState {
  const factory _Initial() = _$InitialImpl;
}

/// @nodoc
abstract class _$$SelectedImplCopyWith<$Res> {
  factory _$$SelectedImplCopyWith(
          _$SelectedImpl value, $Res Function(_$SelectedImpl) then) =
      __$$SelectedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({ProjectWorkflow project});

  $ProjectWorkflowCopyWith<$Res> get project;
}

/// @nodoc
class __$$SelectedImplCopyWithImpl<$Res>
    extends _$SelectedProjectStateCopyWithImpl<$Res, _$SelectedImpl>
    implements _$$SelectedImplCopyWith<$Res> {
  __$$SelectedImplCopyWithImpl(
      _$SelectedImpl _value, $Res Function(_$SelectedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? project = null,
  }) {
    return _then(_$SelectedImpl(
      null == project
          ? _value.project
          : project // ignore: cast_nullable_to_non_nullable
              as ProjectWorkflow,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $ProjectWorkflowCopyWith<$Res> get project {
    return $ProjectWorkflowCopyWith<$Res>(_value.project, (value) {
      return _then(_value.copyWith(project: value));
    });
  }
}

/// @nodoc

class _$SelectedImpl implements _Selected {
  const _$SelectedImpl(this.project);

  @override
  final ProjectWorkflow project;

  @override
  String toString() {
    return 'SelectedProjectState.selected(project: $project)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SelectedImpl &&
            (identical(other.project, project) || other.project == project));
  }

  @override
  int get hashCode => Object.hash(runtimeType, project);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      __$$SelectedImplCopyWithImpl<_$SelectedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(ProjectWorkflow project) selected,
  }) {
    return selected(project);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(ProjectWorkflow project)? selected,
  }) {
    return selected?.call(project);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(ProjectWorkflow project)? selected,
    required TResult orElse(),
  }) {
    if (selected != null) {
      return selected(project);
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

abstract class _Selected implements SelectedProjectState {
  const factory _Selected(final ProjectWorkflow project) = _$SelectedImpl;

  ProjectWorkflow get project;
  @JsonKey(ignore: true)
  _$$SelectedImplCopyWith<_$SelectedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
