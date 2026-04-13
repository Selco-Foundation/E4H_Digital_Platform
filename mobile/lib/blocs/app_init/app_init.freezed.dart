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
    required TResult Function() fetchMdms,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? onLaunch,
    TResult? Function()? fetchMdms,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? onLaunch,
    TResult Function()? fetchMdms,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_AppLaunchEvent value) onLaunch,
    required TResult Function(_FetchMdmsEvent value) fetchMdms,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_AppLaunchEvent value)? onLaunch,
    TResult? Function(_FetchMdmsEvent value)? fetchMdms,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_AppLaunchEvent value)? onLaunch,
    TResult Function(_FetchMdmsEvent value)? fetchMdms,
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
    required TResult Function() fetchMdms,
  }) {
    return onLaunch();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? onLaunch,
    TResult? Function()? fetchMdms,
  }) {
    return onLaunch?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? onLaunch,
    TResult Function()? fetchMdms,
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
    required TResult Function(_FetchMdmsEvent value) fetchMdms,
  }) {
    return onLaunch(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_AppLaunchEvent value)? onLaunch,
    TResult? Function(_FetchMdmsEvent value)? fetchMdms,
  }) {
    return onLaunch?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_AppLaunchEvent value)? onLaunch,
    TResult Function(_FetchMdmsEvent value)? fetchMdms,
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
abstract class _$$FetchMdmsEventImplCopyWith<$Res> {
  factory _$$FetchMdmsEventImplCopyWith(_$FetchMdmsEventImpl value,
          $Res Function(_$FetchMdmsEventImpl) then) =
      __$$FetchMdmsEventImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$FetchMdmsEventImplCopyWithImpl<$Res>
    extends _$InitEventCopyWithImpl<$Res, _$FetchMdmsEventImpl>
    implements _$$FetchMdmsEventImplCopyWith<$Res> {
  __$$FetchMdmsEventImplCopyWithImpl(
      _$FetchMdmsEventImpl _value, $Res Function(_$FetchMdmsEventImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$FetchMdmsEventImpl implements _FetchMdmsEvent {
  const _$FetchMdmsEventImpl();

  @override
  String toString() {
    return 'InitEvent.fetchMdms()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$FetchMdmsEventImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() onLaunch,
    required TResult Function() fetchMdms,
  }) {
    return fetchMdms();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? onLaunch,
    TResult? Function()? fetchMdms,
  }) {
    return fetchMdms?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? onLaunch,
    TResult Function()? fetchMdms,
    required TResult orElse(),
  }) {
    if (fetchMdms != null) {
      return fetchMdms();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_AppLaunchEvent value) onLaunch,
    required TResult Function(_FetchMdmsEvent value) fetchMdms,
  }) {
    return fetchMdms(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_AppLaunchEvent value)? onLaunch,
    TResult? Function(_FetchMdmsEvent value)? fetchMdms,
  }) {
    return fetchMdms?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_AppLaunchEvent value)? onLaunch,
    TResult Function(_FetchMdmsEvent value)? fetchMdms,
    required TResult orElse(),
  }) {
    if (fetchMdms != null) {
      return fetchMdms(this);
    }
    return orElse();
  }
}

abstract class _FetchMdmsEvent implements InitEvent {
  const factory _FetchMdmsEvent() = _$FetchMdmsEventImpl;
}

/// @nodoc
mixin _$InitState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(MdmsResponseModel appConfig) defaulted,
    required TResult Function(MdmsResponseModel appConfig) loadingMdms,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)
        initialized,
    required TResult Function(String message) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(MdmsResponseModel appConfig)? defaulted,
    TResult? Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult? Function(String message)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(MdmsResponseModel appConfig)? defaulted,
    TResult Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Defaulted value) defaulted,
    required TResult Function(LoadingMdms value) loadingMdms,
    required TResult Function(Initialized value) initialized,
    required TResult Function(Error value) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Defaulted value)? defaulted,
    TResult? Function(LoadingMdms value)? loadingMdms,
    TResult? Function(Initialized value)? initialized,
    TResult? Function(Error value)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Defaulted value)? defaulted,
    TResult Function(LoadingMdms value)? loadingMdms,
    TResult Function(Initialized value)? initialized,
    TResult Function(Error value)? error,
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
    required TResult Function(MdmsResponseModel appConfig) defaulted,
    required TResult Function(MdmsResponseModel appConfig) loadingMdms,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)
        initialized,
    required TResult Function(String message) error,
  }) {
    return uninitialized();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(MdmsResponseModel appConfig)? defaulted,
    TResult? Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult? Function(String message)? error,
  }) {
    return uninitialized?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(MdmsResponseModel appConfig)? defaulted,
    TResult Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult Function(String message)? error,
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
    required TResult Function(Defaulted value) defaulted,
    required TResult Function(LoadingMdms value) loadingMdms,
    required TResult Function(Initialized value) initialized,
    required TResult Function(Error value) error,
  }) {
    return uninitialized(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Defaulted value)? defaulted,
    TResult? Function(LoadingMdms value)? loadingMdms,
    TResult? Function(Initialized value)? initialized,
    TResult? Function(Error value)? error,
  }) {
    return uninitialized?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Defaulted value)? defaulted,
    TResult Function(LoadingMdms value)? loadingMdms,
    TResult Function(Initialized value)? initialized,
    TResult Function(Error value)? error,
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
abstract class _$$DefaultedImplCopyWith<$Res> {
  factory _$$DefaultedImplCopyWith(
          _$DefaultedImpl value, $Res Function(_$DefaultedImpl) then) =
      __$$DefaultedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({MdmsResponseModel appConfig});

  $MdmsResponseModelCopyWith<$Res> get appConfig;
}

/// @nodoc
class __$$DefaultedImplCopyWithImpl<$Res>
    extends _$InitStateCopyWithImpl<$Res, _$DefaultedImpl>
    implements _$$DefaultedImplCopyWith<$Res> {
  __$$DefaultedImplCopyWithImpl(
      _$DefaultedImpl _value, $Res Function(_$DefaultedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? appConfig = null,
  }) {
    return _then(_$DefaultedImpl(
      appConfig: null == appConfig
          ? _value.appConfig
          : appConfig // ignore: cast_nullable_to_non_nullable
              as MdmsResponseModel,
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

class _$DefaultedImpl extends Defaulted {
  const _$DefaultedImpl({required this.appConfig}) : super._();

  @override
  final MdmsResponseModel appConfig;

  @override
  String toString() {
    return 'InitState.defaulted(appConfig: $appConfig)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DefaultedImpl &&
            (identical(other.appConfig, appConfig) ||
                other.appConfig == appConfig));
  }

  @override
  int get hashCode => Object.hash(runtimeType, appConfig);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DefaultedImplCopyWith<_$DefaultedImpl> get copyWith =>
      __$$DefaultedImplCopyWithImpl<_$DefaultedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(MdmsResponseModel appConfig) defaulted,
    required TResult Function(MdmsResponseModel appConfig) loadingMdms,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)
        initialized,
    required TResult Function(String message) error,
  }) {
    return defaulted(appConfig);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(MdmsResponseModel appConfig)? defaulted,
    TResult? Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult? Function(String message)? error,
  }) {
    return defaulted?.call(appConfig);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(MdmsResponseModel appConfig)? defaulted,
    TResult Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (defaulted != null) {
      return defaulted(appConfig);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Defaulted value) defaulted,
    required TResult Function(LoadingMdms value) loadingMdms,
    required TResult Function(Initialized value) initialized,
    required TResult Function(Error value) error,
  }) {
    return defaulted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Defaulted value)? defaulted,
    TResult? Function(LoadingMdms value)? loadingMdms,
    TResult? Function(Initialized value)? initialized,
    TResult? Function(Error value)? error,
  }) {
    return defaulted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Defaulted value)? defaulted,
    TResult Function(LoadingMdms value)? loadingMdms,
    TResult Function(Initialized value)? initialized,
    TResult Function(Error value)? error,
    required TResult orElse(),
  }) {
    if (defaulted != null) {
      return defaulted(this);
    }
    return orElse();
  }
}

abstract class Defaulted extends InitState {
  const factory Defaulted({required final MdmsResponseModel appConfig}) =
      _$DefaultedImpl;
  const Defaulted._() : super._();

  MdmsResponseModel get appConfig;
  @JsonKey(ignore: true)
  _$$DefaultedImplCopyWith<_$DefaultedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$LoadingMdmsImplCopyWith<$Res> {
  factory _$$LoadingMdmsImplCopyWith(
          _$LoadingMdmsImpl value, $Res Function(_$LoadingMdmsImpl) then) =
      __$$LoadingMdmsImplCopyWithImpl<$Res>;
  @useResult
  $Res call({MdmsResponseModel appConfig});

  $MdmsResponseModelCopyWith<$Res> get appConfig;
}

/// @nodoc
class __$$LoadingMdmsImplCopyWithImpl<$Res>
    extends _$InitStateCopyWithImpl<$Res, _$LoadingMdmsImpl>
    implements _$$LoadingMdmsImplCopyWith<$Res> {
  __$$LoadingMdmsImplCopyWithImpl(
      _$LoadingMdmsImpl _value, $Res Function(_$LoadingMdmsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? appConfig = null,
  }) {
    return _then(_$LoadingMdmsImpl(
      appConfig: null == appConfig
          ? _value.appConfig
          : appConfig // ignore: cast_nullable_to_non_nullable
              as MdmsResponseModel,
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

class _$LoadingMdmsImpl extends LoadingMdms {
  const _$LoadingMdmsImpl({required this.appConfig}) : super._();

  @override
  final MdmsResponseModel appConfig;

  @override
  String toString() {
    return 'InitState.loadingMdms(appConfig: $appConfig)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$LoadingMdmsImpl &&
            (identical(other.appConfig, appConfig) ||
                other.appConfig == appConfig));
  }

  @override
  int get hashCode => Object.hash(runtimeType, appConfig);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$LoadingMdmsImplCopyWith<_$LoadingMdmsImpl> get copyWith =>
      __$$LoadingMdmsImplCopyWithImpl<_$LoadingMdmsImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(MdmsResponseModel appConfig) defaulted,
    required TResult Function(MdmsResponseModel appConfig) loadingMdms,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)
        initialized,
    required TResult Function(String message) error,
  }) {
    return loadingMdms(appConfig);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(MdmsResponseModel appConfig)? defaulted,
    TResult? Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult? Function(String message)? error,
  }) {
    return loadingMdms?.call(appConfig);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(MdmsResponseModel appConfig)? defaulted,
    TResult Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (loadingMdms != null) {
      return loadingMdms(appConfig);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Defaulted value) defaulted,
    required TResult Function(LoadingMdms value) loadingMdms,
    required TResult Function(Initialized value) initialized,
    required TResult Function(Error value) error,
  }) {
    return loadingMdms(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Defaulted value)? defaulted,
    TResult? Function(LoadingMdms value)? loadingMdms,
    TResult? Function(Initialized value)? initialized,
    TResult? Function(Error value)? error,
  }) {
    return loadingMdms?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Defaulted value)? defaulted,
    TResult Function(LoadingMdms value)? loadingMdms,
    TResult Function(Initialized value)? initialized,
    TResult Function(Error value)? error,
    required TResult orElse(),
  }) {
    if (loadingMdms != null) {
      return loadingMdms(this);
    }
    return orElse();
  }
}

abstract class LoadingMdms extends InitState {
  const factory LoadingMdms({required final MdmsResponseModel appConfig}) =
      _$LoadingMdmsImpl;
  const LoadingMdms._() : super._();

  MdmsResponseModel get appConfig;
  @JsonKey(ignore: true)
  _$$LoadingMdmsImplCopyWith<_$LoadingMdmsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$InitializedImplCopyWith<$Res> {
  factory _$$InitializedImplCopyWith(
          _$InitializedImpl value, $Res Function(_$InitializedImpl) then) =
      __$$InitializedImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {MdmsResponseModel appConfig,
      List<Mdms<AssetCountData>> assetCount,
      List<Mdms<AssetTypeData>> assetType,
      List<Mdms<SystemData>> system,
      List<Mdms<WarrantyData>> warranty,
      List<Mdms<BrandData>> brand,
      List<Mdms<SolutionDesignType>> solutionDesign,
      List<Mdms<SolutionDesignTypeBom>> solutionDesignBom});

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
    Object? solutionDesign = null,
    Object? solutionDesignBom = null,
  }) {
    return _then(_$InitializedImpl(
      appConfig: null == appConfig
          ? _value.appConfig
          : appConfig // ignore: cast_nullable_to_non_nullable
              as MdmsResponseModel,
      assetCount: null == assetCount
          ? _value._assetCount
          : assetCount // ignore: cast_nullable_to_non_nullable
              as List<Mdms<AssetCountData>>,
      assetType: null == assetType
          ? _value._assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as List<Mdms<AssetTypeData>>,
      system: null == system
          ? _value._system
          : system // ignore: cast_nullable_to_non_nullable
              as List<Mdms<SystemData>>,
      warranty: null == warranty
          ? _value._warranty
          : warranty // ignore: cast_nullable_to_non_nullable
              as List<Mdms<WarrantyData>>,
      brand: null == brand
          ? _value._brand
          : brand // ignore: cast_nullable_to_non_nullable
              as List<Mdms<BrandData>>,
      solutionDesign: null == solutionDesign
          ? _value._solutionDesign
          : solutionDesign // ignore: cast_nullable_to_non_nullable
              as List<Mdms<SolutionDesignType>>,
      solutionDesignBom: null == solutionDesignBom
          ? _value._solutionDesignBom
          : solutionDesignBom // ignore: cast_nullable_to_non_nullable
              as List<Mdms<SolutionDesignTypeBom>>,
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
      required final List<Mdms<AssetCountData>> assetCount,
      required final List<Mdms<AssetTypeData>> assetType,
      required final List<Mdms<SystemData>> system,
      required final List<Mdms<WarrantyData>> warranty,
      required final List<Mdms<BrandData>> brand,
      required final List<Mdms<SolutionDesignType>> solutionDesign,
      required final List<Mdms<SolutionDesignTypeBom>> solutionDesignBom})
      : _assetCount = assetCount,
        _assetType = assetType,
        _system = system,
        _warranty = warranty,
        _brand = brand,
        _solutionDesign = solutionDesign,
        _solutionDesignBom = solutionDesignBom,
        super._();

  @override
  final MdmsResponseModel appConfig;
  final List<Mdms<AssetCountData>> _assetCount;
  @override
  List<Mdms<AssetCountData>> get assetCount {
    if (_assetCount is EqualUnmodifiableListView) return _assetCount;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetCount);
  }

  final List<Mdms<AssetTypeData>> _assetType;
  @override
  List<Mdms<AssetTypeData>> get assetType {
    if (_assetType is EqualUnmodifiableListView) return _assetType;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_assetType);
  }

  final List<Mdms<SystemData>> _system;
  @override
  List<Mdms<SystemData>> get system {
    if (_system is EqualUnmodifiableListView) return _system;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_system);
  }

  final List<Mdms<WarrantyData>> _warranty;
  @override
  List<Mdms<WarrantyData>> get warranty {
    if (_warranty is EqualUnmodifiableListView) return _warranty;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_warranty);
  }

  final List<Mdms<BrandData>> _brand;
  @override
  List<Mdms<BrandData>> get brand {
    if (_brand is EqualUnmodifiableListView) return _brand;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_brand);
  }

  final List<Mdms<SolutionDesignType>> _solutionDesign;
  @override
  List<Mdms<SolutionDesignType>> get solutionDesign {
    if (_solutionDesign is EqualUnmodifiableListView) return _solutionDesign;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_solutionDesign);
  }

  final List<Mdms<SolutionDesignTypeBom>> _solutionDesignBom;
  @override
  List<Mdms<SolutionDesignTypeBom>> get solutionDesignBom {
    if (_solutionDesignBom is EqualUnmodifiableListView)
      return _solutionDesignBom;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_solutionDesignBom);
  }

  @override
  String toString() {
    return 'InitState.initialized(appConfig: $appConfig, assetCount: $assetCount, assetType: $assetType, system: $system, warranty: $warranty, brand: $brand, solutionDesign: $solutionDesign, solutionDesignBom: $solutionDesignBom)';
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
            const DeepCollectionEquality().equals(other._brand, _brand) &&
            const DeepCollectionEquality()
                .equals(other._solutionDesign, _solutionDesign) &&
            const DeepCollectionEquality()
                .equals(other._solutionDesignBom, _solutionDesignBom));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      appConfig,
      const DeepCollectionEquality().hash(_assetCount),
      const DeepCollectionEquality().hash(_assetType),
      const DeepCollectionEquality().hash(_system),
      const DeepCollectionEquality().hash(_warranty),
      const DeepCollectionEquality().hash(_brand),
      const DeepCollectionEquality().hash(_solutionDesign),
      const DeepCollectionEquality().hash(_solutionDesignBom));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$InitializedImplCopyWith<_$InitializedImpl> get copyWith =>
      __$$InitializedImplCopyWithImpl<_$InitializedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(MdmsResponseModel appConfig) defaulted,
    required TResult Function(MdmsResponseModel appConfig) loadingMdms,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)
        initialized,
    required TResult Function(String message) error,
  }) {
    return initialized(appConfig, assetCount, assetType, system, warranty,
        brand, solutionDesign, solutionDesignBom);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(MdmsResponseModel appConfig)? defaulted,
    TResult? Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult? Function(String message)? error,
  }) {
    return initialized?.call(appConfig, assetCount, assetType, system, warranty,
        brand, solutionDesign, solutionDesignBom);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(MdmsResponseModel appConfig)? defaulted,
    TResult Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (initialized != null) {
      return initialized(appConfig, assetCount, assetType, system, warranty,
          brand, solutionDesign, solutionDesignBom);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Defaulted value) defaulted,
    required TResult Function(LoadingMdms value) loadingMdms,
    required TResult Function(Initialized value) initialized,
    required TResult Function(Error value) error,
  }) {
    return initialized(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Defaulted value)? defaulted,
    TResult? Function(LoadingMdms value)? loadingMdms,
    TResult? Function(Initialized value)? initialized,
    TResult? Function(Error value)? error,
  }) {
    return initialized?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Defaulted value)? defaulted,
    TResult Function(LoadingMdms value)? loadingMdms,
    TResult Function(Initialized value)? initialized,
    TResult Function(Error value)? error,
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
          required final List<Mdms<AssetCountData>> assetCount,
          required final List<Mdms<AssetTypeData>> assetType,
          required final List<Mdms<SystemData>> system,
          required final List<Mdms<WarrantyData>> warranty,
          required final List<Mdms<BrandData>> brand,
          required final List<Mdms<SolutionDesignType>> solutionDesign,
          required final List<Mdms<SolutionDesignTypeBom>> solutionDesignBom}) =
      _$InitializedImpl;
  const Initialized._() : super._();

  MdmsResponseModel get appConfig;
  List<Mdms<AssetCountData>> get assetCount;
  List<Mdms<AssetTypeData>> get assetType;
  List<Mdms<SystemData>> get system;
  List<Mdms<WarrantyData>> get warranty;
  List<Mdms<BrandData>> get brand;
  List<Mdms<SolutionDesignType>> get solutionDesign;
  List<Mdms<SolutionDesignTypeBom>> get solutionDesignBom;
  @JsonKey(ignore: true)
  _$$InitializedImplCopyWith<_$InitializedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ErrorImplCopyWith<$Res> {
  factory _$$ErrorImplCopyWith(
          _$ErrorImpl value, $Res Function(_$ErrorImpl) then) =
      __$$ErrorImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String message});
}

/// @nodoc
class __$$ErrorImplCopyWithImpl<$Res>
    extends _$InitStateCopyWithImpl<$Res, _$ErrorImpl>
    implements _$$ErrorImplCopyWith<$Res> {
  __$$ErrorImplCopyWithImpl(
      _$ErrorImpl _value, $Res Function(_$ErrorImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? message = null,
  }) {
    return _then(_$ErrorImpl(
      null == message
          ? _value.message
          : message // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ErrorImpl extends Error {
  const _$ErrorImpl(this.message) : super._();

  @override
  final String message;

  @override
  String toString() {
    return 'InitState.error(message: $message)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ErrorImpl &&
            (identical(other.message, message) || other.message == message));
  }

  @override
  int get hashCode => Object.hash(runtimeType, message);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      __$$ErrorImplCopyWithImpl<_$ErrorImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() uninitialized,
    required TResult Function(MdmsResponseModel appConfig) defaulted,
    required TResult Function(MdmsResponseModel appConfig) loadingMdms,
    required TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)
        initialized,
    required TResult Function(String message) error,
  }) {
    return error(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? uninitialized,
    TResult? Function(MdmsResponseModel appConfig)? defaulted,
    TResult? Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult? Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult? Function(String message)? error,
  }) {
    return error?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? uninitialized,
    TResult Function(MdmsResponseModel appConfig)? defaulted,
    TResult Function(MdmsResponseModel appConfig)? loadingMdms,
    TResult Function(
            MdmsResponseModel appConfig,
            List<Mdms<AssetCountData>> assetCount,
            List<Mdms<AssetTypeData>> assetType,
            List<Mdms<SystemData>> system,
            List<Mdms<WarrantyData>> warranty,
            List<Mdms<BrandData>> brand,
            List<Mdms<SolutionDesignType>> solutionDesign,
            List<Mdms<SolutionDesignTypeBom>> solutionDesignBom)?
        initialized,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (error != null) {
      return error(message);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Uninitialized value) uninitialized,
    required TResult Function(Defaulted value) defaulted,
    required TResult Function(LoadingMdms value) loadingMdms,
    required TResult Function(Initialized value) initialized,
    required TResult Function(Error value) error,
  }) {
    return error(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Uninitialized value)? uninitialized,
    TResult? Function(Defaulted value)? defaulted,
    TResult? Function(LoadingMdms value)? loadingMdms,
    TResult? Function(Initialized value)? initialized,
    TResult? Function(Error value)? error,
  }) {
    return error?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Uninitialized value)? uninitialized,
    TResult Function(Defaulted value)? defaulted,
    TResult Function(LoadingMdms value)? loadingMdms,
    TResult Function(Initialized value)? initialized,
    TResult Function(Error value)? error,
    required TResult orElse(),
  }) {
    if (error != null) {
      return error(this);
    }
    return orElse();
  }
}

abstract class Error extends InitState {
  const factory Error(final String message) = _$ErrorImpl;
  const Error._() : super._();

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
