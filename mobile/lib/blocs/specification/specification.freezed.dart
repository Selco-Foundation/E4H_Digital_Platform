// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'specification.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SpecificationState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)
        loaded,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        loaded,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        loaded,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(SpecificationInitial value) initial,
    required TResult Function(SpecificationLoaded value) loaded,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(SpecificationInitial value)? initial,
    TResult? Function(SpecificationLoaded value)? loaded,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(SpecificationInitial value)? initial,
    TResult Function(SpecificationLoaded value)? loaded,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SpecificationStateCopyWith<$Res> {
  factory $SpecificationStateCopyWith(
          SpecificationState value, $Res Function(SpecificationState) then) =
      _$SpecificationStateCopyWithImpl<$Res, SpecificationState>;
}

/// @nodoc
class _$SpecificationStateCopyWithImpl<$Res, $Val extends SpecificationState>
    implements $SpecificationStateCopyWith<$Res> {
  _$SpecificationStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$SpecificationInitialImplCopyWith<$Res> {
  factory _$$SpecificationInitialImplCopyWith(_$SpecificationInitialImpl value,
          $Res Function(_$SpecificationInitialImpl) then) =
      __$$SpecificationInitialImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SpecificationInitialImplCopyWithImpl<$Res>
    extends _$SpecificationStateCopyWithImpl<$Res, _$SpecificationInitialImpl>
    implements _$$SpecificationInitialImplCopyWith<$Res> {
  __$$SpecificationInitialImplCopyWithImpl(_$SpecificationInitialImpl _value,
      $Res Function(_$SpecificationInitialImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$SpecificationInitialImpl implements SpecificationInitial {
  const _$SpecificationInitialImpl();

  @override
  String toString() {
    return 'SpecificationState.initial()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SpecificationInitialImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)
        loaded,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        loaded,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        loaded,
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
    required TResult Function(SpecificationInitial value) initial,
    required TResult Function(SpecificationLoaded value) loaded,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(SpecificationInitial value)? initial,
    TResult? Function(SpecificationLoaded value)? loaded,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(SpecificationInitial value)? initial,
    TResult Function(SpecificationLoaded value)? loaded,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class SpecificationInitial implements SpecificationState {
  const factory SpecificationInitial() = _$SpecificationInitialImpl;
}

/// @nodoc
abstract class _$$SpecificationLoadedImplCopyWith<$Res> {
  factory _$$SpecificationLoadedImplCopyWith(_$SpecificationLoadedImpl value,
          $Res Function(_$SpecificationLoadedImpl) then) =
      __$$SpecificationLoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String systemName, double totalCapacity, String totalCapacityUom});
}

/// @nodoc
class __$$SpecificationLoadedImplCopyWithImpl<$Res>
    extends _$SpecificationStateCopyWithImpl<$Res, _$SpecificationLoadedImpl>
    implements _$$SpecificationLoadedImplCopyWith<$Res> {
  __$$SpecificationLoadedImplCopyWithImpl(_$SpecificationLoadedImpl _value,
      $Res Function(_$SpecificationLoadedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? systemName = null,
    Object? totalCapacity = null,
    Object? totalCapacityUom = null,
  }) {
    return _then(_$SpecificationLoadedImpl(
      systemName: null == systemName
          ? _value.systemName
          : systemName // ignore: cast_nullable_to_non_nullable
              as String,
      totalCapacity: null == totalCapacity
          ? _value.totalCapacity
          : totalCapacity // ignore: cast_nullable_to_non_nullable
              as double,
      totalCapacityUom: null == totalCapacityUom
          ? _value.totalCapacityUom
          : totalCapacityUom // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SpecificationLoadedImpl implements SpecificationLoaded {
  const _$SpecificationLoadedImpl(
      {required this.systemName,
      required this.totalCapacity,
      required this.totalCapacityUom});

  @override
  final String systemName;
  @override
  final double totalCapacity;
  @override
  final String totalCapacityUom;

  @override
  String toString() {
    return 'SpecificationState.loaded(systemName: $systemName, totalCapacity: $totalCapacity, totalCapacityUom: $totalCapacityUom)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SpecificationLoadedImpl &&
            (identical(other.systemName, systemName) ||
                other.systemName == systemName) &&
            (identical(other.totalCapacity, totalCapacity) ||
                other.totalCapacity == totalCapacity) &&
            (identical(other.totalCapacityUom, totalCapacityUom) ||
                other.totalCapacityUom == totalCapacityUom));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, systemName, totalCapacity, totalCapacityUom);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SpecificationLoadedImplCopyWith<_$SpecificationLoadedImpl> get copyWith =>
      __$$SpecificationLoadedImplCopyWithImpl<_$SpecificationLoadedImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)
        loaded,
  }) {
    return loaded(systemName, totalCapacity, totalCapacityUom);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        loaded,
  }) {
    return loaded?.call(systemName, totalCapacity, totalCapacityUom);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        loaded,
    required TResult orElse(),
  }) {
    if (loaded != null) {
      return loaded(systemName, totalCapacity, totalCapacityUom);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(SpecificationInitial value) initial,
    required TResult Function(SpecificationLoaded value) loaded,
  }) {
    return loaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(SpecificationInitial value)? initial,
    TResult? Function(SpecificationLoaded value)? loaded,
  }) {
    return loaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(SpecificationInitial value)? initial,
    TResult Function(SpecificationLoaded value)? loaded,
    required TResult orElse(),
  }) {
    if (loaded != null) {
      return loaded(this);
    }
    return orElse();
  }
}

abstract class SpecificationLoaded implements SpecificationState {
  const factory SpecificationLoaded(
      {required final String systemName,
      required final double totalCapacity,
      required final String totalCapacityUom}) = _$SpecificationLoadedImpl;

  String get systemName;
  double get totalCapacity;
  String get totalCapacityUom;
  @JsonKey(ignore: true)
  _$$SpecificationLoadedImplCopyWith<_$SpecificationLoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$SpecificationEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)
        save,
    required TResult Function() load,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        save,
    TResult? Function()? load,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        save,
    TResult Function()? load,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(SpecificationSave value) save,
    required TResult Function(SpecificationLoad value) load,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(SpecificationSave value)? save,
    TResult? Function(SpecificationLoad value)? load,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(SpecificationSave value)? save,
    TResult Function(SpecificationLoad value)? load,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SpecificationEventCopyWith<$Res> {
  factory $SpecificationEventCopyWith(
          SpecificationEvent value, $Res Function(SpecificationEvent) then) =
      _$SpecificationEventCopyWithImpl<$Res, SpecificationEvent>;
}

/// @nodoc
class _$SpecificationEventCopyWithImpl<$Res, $Val extends SpecificationEvent>
    implements $SpecificationEventCopyWith<$Res> {
  _$SpecificationEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$SpecificationSaveImplCopyWith<$Res> {
  factory _$$SpecificationSaveImplCopyWith(_$SpecificationSaveImpl value,
          $Res Function(_$SpecificationSaveImpl) then) =
      __$$SpecificationSaveImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String systemName, double totalCapacity, String totalCapacityUom});
}

/// @nodoc
class __$$SpecificationSaveImplCopyWithImpl<$Res>
    extends _$SpecificationEventCopyWithImpl<$Res, _$SpecificationSaveImpl>
    implements _$$SpecificationSaveImplCopyWith<$Res> {
  __$$SpecificationSaveImplCopyWithImpl(_$SpecificationSaveImpl _value,
      $Res Function(_$SpecificationSaveImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? systemName = null,
    Object? totalCapacity = null,
    Object? totalCapacityUom = null,
  }) {
    return _then(_$SpecificationSaveImpl(
      systemName: null == systemName
          ? _value.systemName
          : systemName // ignore: cast_nullable_to_non_nullable
              as String,
      totalCapacity: null == totalCapacity
          ? _value.totalCapacity
          : totalCapacity // ignore: cast_nullable_to_non_nullable
              as double,
      totalCapacityUom: null == totalCapacityUom
          ? _value.totalCapacityUom
          : totalCapacityUom // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SpecificationSaveImpl implements SpecificationSave {
  const _$SpecificationSaveImpl(
      {required this.systemName,
      required this.totalCapacity,
      required this.totalCapacityUom});

  @override
  final String systemName;
  @override
  final double totalCapacity;
  @override
  final String totalCapacityUom;

  @override
  String toString() {
    return 'SpecificationEvent.save(systemName: $systemName, totalCapacity: $totalCapacity, totalCapacityUom: $totalCapacityUom)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SpecificationSaveImpl &&
            (identical(other.systemName, systemName) ||
                other.systemName == systemName) &&
            (identical(other.totalCapacity, totalCapacity) ||
                other.totalCapacity == totalCapacity) &&
            (identical(other.totalCapacityUom, totalCapacityUom) ||
                other.totalCapacityUom == totalCapacityUom));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, systemName, totalCapacity, totalCapacityUom);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SpecificationSaveImplCopyWith<_$SpecificationSaveImpl> get copyWith =>
      __$$SpecificationSaveImplCopyWithImpl<_$SpecificationSaveImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)
        save,
    required TResult Function() load,
  }) {
    return save(systemName, totalCapacity, totalCapacityUom);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        save,
    TResult? Function()? load,
  }) {
    return save?.call(systemName, totalCapacity, totalCapacityUom);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        save,
    TResult Function()? load,
    required TResult orElse(),
  }) {
    if (save != null) {
      return save(systemName, totalCapacity, totalCapacityUom);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(SpecificationSave value) save,
    required TResult Function(SpecificationLoad value) load,
  }) {
    return save(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(SpecificationSave value)? save,
    TResult? Function(SpecificationLoad value)? load,
  }) {
    return save?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(SpecificationSave value)? save,
    TResult Function(SpecificationLoad value)? load,
    required TResult orElse(),
  }) {
    if (save != null) {
      return save(this);
    }
    return orElse();
  }
}

abstract class SpecificationSave implements SpecificationEvent {
  const factory SpecificationSave(
      {required final String systemName,
      required final double totalCapacity,
      required final String totalCapacityUom}) = _$SpecificationSaveImpl;

  String get systemName;
  double get totalCapacity;
  String get totalCapacityUom;
  @JsonKey(ignore: true)
  _$$SpecificationSaveImplCopyWith<_$SpecificationSaveImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$SpecificationLoadImplCopyWith<$Res> {
  factory _$$SpecificationLoadImplCopyWith(_$SpecificationLoadImpl value,
          $Res Function(_$SpecificationLoadImpl) then) =
      __$$SpecificationLoadImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$SpecificationLoadImplCopyWithImpl<$Res>
    extends _$SpecificationEventCopyWithImpl<$Res, _$SpecificationLoadImpl>
    implements _$$SpecificationLoadImplCopyWith<$Res> {
  __$$SpecificationLoadImplCopyWithImpl(_$SpecificationLoadImpl _value,
      $Res Function(_$SpecificationLoadImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$SpecificationLoadImpl implements SpecificationLoad {
  const _$SpecificationLoadImpl();

  @override
  String toString() {
    return 'SpecificationEvent.load()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$SpecificationLoadImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)
        save,
    required TResult Function() load,
  }) {
    return load();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        save,
    TResult? Function()? load,
  }) {
    return load?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(
            String systemName, double totalCapacity, String totalCapacityUom)?
        save,
    TResult Function()? load,
    required TResult orElse(),
  }) {
    if (load != null) {
      return load();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(SpecificationSave value) save,
    required TResult Function(SpecificationLoad value) load,
  }) {
    return load(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(SpecificationSave value)? save,
    TResult? Function(SpecificationLoad value)? load,
  }) {
    return load?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(SpecificationSave value)? save,
    TResult Function(SpecificationLoad value)? load,
    required TResult orElse(),
  }) {
    if (load != null) {
      return load(this);
    }
    return orElse();
  }
}

abstract class SpecificationLoad implements SpecificationEvent {
  const factory SpecificationLoad() = _$SpecificationLoadImpl;
}
