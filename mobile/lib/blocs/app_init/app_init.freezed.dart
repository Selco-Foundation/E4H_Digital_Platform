// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'app_init.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$InitEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() onLaunch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? onLaunch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? onLaunch,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_AppLaunchEvent value) onLaunch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_AppLaunchEvent value)? onLaunch,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_AppLaunchEvent value)? onLaunch,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InitEventCopyWith<$Res> {
  factory $InitEventCopyWith(InitEvent value, $Res Function(InitEvent) then) =
      _$InitEventCopyWithImpl<$Res, InitEvent>;
}

/// @nodoc
class _$InitEventCopyWithImpl<$Res, $Val extends InitEvent>
    implements $InitEventCopyWith<$Res> {
  _$InitEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$AppLaunchEventImplCopyWith<$Res> {
  factory _$$AppLaunchEventImplCopyWith(_$AppLaunchEventImpl value,
          $Res Function(_$AppLaunchEventImpl) then) =
      __$$AppLaunchEventImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$AppLaunchEventImplCopyWithImpl<$Res>
    extends _$InitEventCopyWithImpl<$Res, _$AppLaunchEventImpl>
    implements _$$AppLaunchEventImplCopyWith<$Res> {
  __$$AppLaunchEventImplCopyWithImpl(
      _$AppLaunchEventImpl _value, $Res Function(_$AppLaunchEventImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$AppLaunchEventImpl implements _AppLaunchEvent {
  const _$AppLaunchEventImpl();

  @override
  String toString() {
    return 'InitEvent.onLaunch()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$AppLaunchEventImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() onLaunch,
  }) {
    return onLaunch();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? onLaunch,
  }) {
    return onLaunch?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? onLaunch,
    required TResult orElse(),
  }) {
    if (onLaunch != null) {
      return onLaunch();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_AppLaunchEvent value) onLaunch,
  }) {
    return onLaunch(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_AppLaunchEvent value)? onLaunch,
  }) {
    return onLaunch?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_AppLaunchEvent value)? onLaunch,
    required TResult orElse(),
  }) {
    if (onLaunch != null) {
      return onLaunch(this);
    }
    return orElse();
  }
}

abstract class _AppLaunchEvent implements InitEvent {
  const factory _AppLaunchEvent() = _$AppLaunchEventImpl;
}

/// @nodoc
mixin _$InitState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)
        initialized,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)?
        initialized,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)?
        initialized,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Initialized value) initialized,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Initialized value)? initialized,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Initialized value)? initialized,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InitStateCopyWith<$Res> {
  factory $InitStateCopyWith(InitState value, $Res Function(InitState) then) =
      _$InitStateCopyWithImpl<$Res, InitState>;
}

/// @nodoc
class _$InitStateCopyWithImpl<$Res, $Val extends InitState>
    implements $InitStateCopyWith<$Res> {
  _$InitStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$UninitializedImplCopyWith<$Res> {
  factory _$$UninitializedImplCopyWith(
          _$UninitializedImpl value, $Res Function(_$UninitializedImpl) then) =
      __$$UninitializedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$UninitializedImplCopyWithImpl<$Res>
    extends _$InitStateCopyWithImpl<$Res, _$UninitializedImpl>
    implements _$$UninitializedImplCopyWith<$Res> {
  __$$UninitializedImplCopyWithImpl(
      _$UninitializedImpl _value, $Res Function(_$UninitializedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$UninitializedImpl extends _Uninitialized {
  const _$UninitializedImpl() : super._();

  @override
  String toString() {
    return 'InitState.uninitialized()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$UninitializedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)
        initialized,
  }) {
    return uninitialized();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)?
        initialized,
  }) {
    return uninitialized?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)?
        initialized,
    required TResult orElse(),
  }) {
    if (uninitialized != null) {
      return uninitialized();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Initialized value) initialized,
  }) {
    return uninitialized(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Initialized value)? initialized,
  }) {
    return uninitialized?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Initialized value)? initialized,
    required TResult orElse(),
  }) {
    if (uninitialized != null) {
      return uninitialized(this);
    }
    return orElse();
  }
}

abstract class _Uninitialized extends InitState {
  const factory _Uninitialized() = _$UninitializedImpl;
  const _Uninitialized._() : super._();
}

/// @nodoc
abstract class _$$InitializedImplCopyWith<$Res> {
  factory _$$InitializedImplCopyWith(
          _$InitializedImpl value, $Res Function(_$InitializedImpl) then) =
      __$$InitializedImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {MdmsResponseModel appConfig,
      List<Mdms<AssetCount>> assetCount,
      List<Mdms<AssetType>> assetType,
      List<Mdms<System>> system,
      List<Mdms<Warranty>> warranty,
      List<Mdms<Brand>> brand});

  $MdmsResponseModelCopyWith<$Res> get appConfig;
}

/// @nodoc
class __$$InitializedImplCopyWithImpl<$Res>
    extends _$InitStateCopyWithImpl<$Res, _$InitializedImpl>
    implements _$$InitializedImplCopyWith<$Res> {
  __$$InitializedImplCopyWithImpl(
      _$InitializedImpl _value, $Res Function(_$InitializedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? appConfig = null,
    Object? assetCount = null,
    Object? assetType = null,
    Object? system = null,
    Object? warranty = null,
    Object? brand = null,
  }) {
    return _then(_$InitializedImpl(
      appConfig: null == appConfig
          ? _value.appConfig
          : appConfig // ignore: cast_nullable_to_non_nullable
              as MdmsResponseModel,
      assetCount: null == assetCount
          ? _value._assetCount
          : assetCount // ignore: cast_nullable_to_non_nullable
              as List<Mdms<AssetCount>>,
      assetType: null == assetType
          ? _value._assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as List<Mdms<AssetType>>,
      system: null == system
          ? _value._system
          : system // ignore: cast_nullable_to_non_nullable
              as List<Mdms<System>>,
      warranty: null == warranty
          ? _value._warranty
          : warranty // ignore: cast_nullable_to_non_nullable
              as List<Mdms<Warranty>>,
      brand: null == brand
          ? _value._brand
          : brand // ignore: cast_nullable_to_non_nullable
              as List<Mdms<Brand>>,
    ));
  }

  @override
  @pragma('vm:prefer-inline')
  $MdmsResponseModelCopyWith<$Res> get appConfig {
    return $MdmsResponseModelCopyWith<$Res>(_value.appConfig, (value) {
      return _then(_value.copyWith(appConfig: value));
    });
  }
}

/// @nodoc

class _$InitializedImpl extends Initialized {
  const _$InitializedImpl(
      {required this.appConfig,
      required final List<Mdms<AssetCount>> assetCount,
      required final List<Mdms<AssetType>> assetType,
      required final List<Mdms<System>> system,
      required final List<Mdms<Warranty>> warranty,
      required final List<Mdms<Brand>> brand})
      : _assetCount = assetCount,
        _assetType = assetType,
        _system = system,
        _warranty = warranty,
        _brand = brand,
        super._();

  @override
  final MdmsResponseModel appConfig;
  final List<Mdms<AssetCount>> _assetCount;
  @override
  List<Mdms<AssetCount>> get assetCount {
    if (_assetCount is EqualUnmodifiableListView) return _assetCount;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetCount);
  }

  final List<Mdms<AssetType>> _assetType;
  @override
  List<Mdms<AssetType>> get assetType {
    if (_assetType is EqualUnmodifiableListView) return _assetType;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetType);
  }

  final List<Mdms<System>> _system;
  @override
  List<Mdms<System>> get system {
    if (_system is EqualUnmodifiableListView) return _system;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_system);
  }

  final List<Mdms<Warranty>> _warranty;
  @override
  List<Mdms<Warranty>> get warranty {
    if (_warranty is EqualUnmodifiableListView) return _warranty;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_warranty);
  }

  final List<Mdms<Brand>> _brand;
  @override
  List<Mdms<Brand>> get brand {
    if (_brand is EqualUnmodifiableListView) return _brand;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_brand);
  }

  @override
  String toString() {
    return 'InitState.initialized(appConfig: $appConfig, assetCount: $assetCount, assetType: $assetType, system: $system, warranty: $warranty, brand: $brand)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InitializedImpl &&
            (identical(other.appConfig, appConfig) ||
                other.appConfig == appConfig) &&
            const DeepCollectionEquality()
                .equals(other._assetCount, _assetCount) &&
            const DeepCollectionEquality()
                .equals(other._assetType, _assetType) &&
            const DeepCollectionEquality().equals(other._system, _system) &&
            const DeepCollectionEquality().equals(other._warranty, _warranty) &&
            const DeepCollectionEquality().equals(other._brand, _brand));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      appConfig,
      const DeepCollectionEquality().hash(_assetCount),
      const DeepCollectionEquality().hash(_assetType),
      const DeepCollectionEquality().hash(_system),
      const DeepCollectionEquality().hash(_warranty),
      const DeepCollectionEquality().hash(_brand));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$InitializedImplCopyWith<_$InitializedImpl> get copyWith =>
      __$$InitializedImplCopyWithImpl<_$InitializedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)
        initialized,
  }) {
    return initialized(
        appConfig, assetCount, assetType, system, warranty, brand);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)?
        initialized,
  }) {
    return initialized?.call(
        appConfig, assetCount, assetType, system, warranty, brand);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCount>> assetCount,
            List<Mdms<AssetType>> assetType,
            List<Mdms<System>> system,
            List<Mdms<Warranty>> warranty,
            List<Mdms<Brand>> brand)?
        initialized,
    required TResult orElse(),
  }) {
    if (initialized != null) {
      return initialized(
          appConfig, assetCount, assetType, system, warranty, brand);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Initialized value) initialized,
  }) {
    return initialized(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Initialized value)? initialized,
  }) {
    return initialized?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Initialized value)? initialized,
    required TResult orElse(),
  }) {
    if (initialized != null) {
      return initialized(this);
    }
    return orElse();
  }
}

abstract class Initialized extends InitState {
  const factory Initialized(
      {required final MdmsResponseModel appConfig,
      required final List<Mdms<AssetCount>> assetCount,
      required final List<Mdms<AssetType>> assetType,
      required final List<Mdms<System>> system,
      required final List<Mdms<Warranty>> warranty,
      required final List<Mdms<Brand>> brand}) = _$InitializedImpl;
  const Initialized._() : super._();

  MdmsResponseModel get appConfig;
  List<Mdms<AssetCount>> get assetCount;
  List<Mdms<AssetType>> get assetType;
  List<Mdms<System>> get system;
  List<Mdms<Warranty>> get warranty;
  List<Mdms<Brand>> get brand;
  @JsonKey(ignore: true)
  _$$InitializedImplCopyWith<_$InitializedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
