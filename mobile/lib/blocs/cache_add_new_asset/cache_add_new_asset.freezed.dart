// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'cache_add_new_asset.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$CacheAddNewAssetEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheAddNewAssetEventCopyWith<$Res> {
  factory $CacheAddNewAssetEventCopyWith(CacheAddNewAssetEvent value,
          $Res Function(CacheAddNewAssetEvent) then) =
      _$CacheAddNewAssetEventCopyWithImpl<$Res, CacheAddNewAssetEvent>;
}

/// @nodoc
class _$CacheAddNewAssetEventCopyWithImpl<$Res,
        $Val extends CacheAddNewAssetEvent>
    implements $CacheAddNewAssetEventCopyWith<$Res> {
  _$CacheAddNewAssetEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$CacheAddNewAssetEventGetImplCopyWith<$Res> {
  factory _$$CacheAddNewAssetEventGetImplCopyWith(
          _$CacheAddNewAssetEventGetImpl value,
          $Res Function(_$CacheAddNewAssetEventGetImpl) then) =
      __$$CacheAddNewAssetEventGetImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String assetType});
}

/// @nodoc
class __$$CacheAddNewAssetEventGetImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetEventCopyWithImpl<$Res,
        _$CacheAddNewAssetEventGetImpl>
    implements _$$CacheAddNewAssetEventGetImplCopyWith<$Res> {
  __$$CacheAddNewAssetEventGetImplCopyWithImpl(
      _$CacheAddNewAssetEventGetImpl _value,
      $Res Function(_$CacheAddNewAssetEventGetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? assetType = null,
  }) {
    return _then(_$CacheAddNewAssetEventGetImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      null == assetType
          ? _value.assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$CacheAddNewAssetEventGetImpl implements CacheAddNewAssetEventGet {
  const _$CacheAddNewAssetEventGetImpl(this.activityFacilityId, this.assetType);

  @override
  final String activityFacilityId;
  @override
  final String assetType;

  @override
  String toString() {
    return 'CacheAddNewAssetEvent.get(activityFacilityId: $activityFacilityId, assetType: $assetType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAddNewAssetEventGetImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.assetType, assetType) ||
                other.assetType == assetType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, assetType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAddNewAssetEventGetImplCopyWith<_$CacheAddNewAssetEventGetImpl>
      get copyWith => __$$CacheAddNewAssetEventGetImplCopyWithImpl<
          _$CacheAddNewAssetEventGetImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) {
    return get(activityFacilityId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) {
    return get?.call(activityFacilityId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(activityFacilityId, assetType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) {
    return get(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) {
    return get?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(this);
    }
    return orElse();
  }
}

abstract class CacheAddNewAssetEventGet implements CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEventGet(
          final String activityFacilityId, final String assetType) =
      _$CacheAddNewAssetEventGetImpl;

  String get activityFacilityId;
  String get assetType;
  @JsonKey(ignore: true)
  _$$CacheAddNewAssetEventGetImplCopyWith<_$CacheAddNewAssetEventGetImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAddNewAssetEventAddImplCopyWith<$Res> {
  factory _$$CacheAddNewAssetEventAddImplCopyWith(
          _$CacheAddNewAssetEventAddImpl value,
          $Res Function(_$CacheAddNewAssetEventAddImpl) then) =
      __$$CacheAddNewAssetEventAddImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAddNewAsset entry});
}

/// @nodoc
class __$$CacheAddNewAssetEventAddImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetEventCopyWithImpl<$Res,
        _$CacheAddNewAssetEventAddImpl>
    implements _$$CacheAddNewAssetEventAddImplCopyWith<$Res> {
  __$$CacheAddNewAssetEventAddImplCopyWithImpl(
      _$CacheAddNewAssetEventAddImpl _value,
      $Res Function(_$CacheAddNewAssetEventAddImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheAddNewAssetEventAddImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAddNewAsset,
    ));
  }
}

/// @nodoc

class _$CacheAddNewAssetEventAddImpl implements CacheAddNewAssetEventAdd {
  const _$CacheAddNewAssetEventAddImpl(this.entry);

  @override
  final CacheAddNewAsset entry;

  @override
  String toString() {
    return 'CacheAddNewAssetEvent.add(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAddNewAssetEventAddImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAddNewAssetEventAddImplCopyWith<_$CacheAddNewAssetEventAddImpl>
      get copyWith => __$$CacheAddNewAssetEventAddImplCopyWithImpl<
          _$CacheAddNewAssetEventAddImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) {
    return add(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) {
    return add?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) {
    if (add != null) {
      return add(entry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) {
    return add(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) {
    return add?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) {
    if (add != null) {
      return add(this);
    }
    return orElse();
  }
}

abstract class CacheAddNewAssetEventAdd implements CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEventAdd(final CacheAddNewAsset entry) =
      _$CacheAddNewAssetEventAddImpl;

  CacheAddNewAsset get entry;
  @JsonKey(ignore: true)
  _$$CacheAddNewAssetEventAddImplCopyWith<_$CacheAddNewAssetEventAddImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAddNewAssetEventUpdateImplCopyWith<$Res> {
  factory _$$CacheAddNewAssetEventUpdateImplCopyWith(
          _$CacheAddNewAssetEventUpdateImpl value,
          $Res Function(_$CacheAddNewAssetEventUpdateImpl) then) =
      __$$CacheAddNewAssetEventUpdateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAddNewAsset entry});
}

/// @nodoc
class __$$CacheAddNewAssetEventUpdateImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetEventCopyWithImpl<$Res,
        _$CacheAddNewAssetEventUpdateImpl>
    implements _$$CacheAddNewAssetEventUpdateImplCopyWith<$Res> {
  __$$CacheAddNewAssetEventUpdateImplCopyWithImpl(
      _$CacheAddNewAssetEventUpdateImpl _value,
      $Res Function(_$CacheAddNewAssetEventUpdateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheAddNewAssetEventUpdateImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAddNewAsset,
    ));
  }
}

/// @nodoc

class _$CacheAddNewAssetEventUpdateImpl implements CacheAddNewAssetEventUpdate {
  const _$CacheAddNewAssetEventUpdateImpl(this.entry);

  @override
  final CacheAddNewAsset entry;

  @override
  String toString() {
    return 'CacheAddNewAssetEvent.update(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAddNewAssetEventUpdateImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAddNewAssetEventUpdateImplCopyWith<_$CacheAddNewAssetEventUpdateImpl>
      get copyWith => __$$CacheAddNewAssetEventUpdateImplCopyWithImpl<
          _$CacheAddNewAssetEventUpdateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) {
    return update(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) {
    return update?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) {
    if (update != null) {
      return update(entry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) {
    return update(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) {
    return update?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) {
    if (update != null) {
      return update(this);
    }
    return orElse();
  }
}

abstract class CacheAddNewAssetEventUpdate implements CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEventUpdate(final CacheAddNewAsset entry) =
      _$CacheAddNewAssetEventUpdateImpl;

  CacheAddNewAsset get entry;
  @JsonKey(ignore: true)
  _$$CacheAddNewAssetEventUpdateImplCopyWith<_$CacheAddNewAssetEventUpdateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAddNewAssetEventDeleteImplCopyWith<$Res> {
  factory _$$CacheAddNewAssetEventDeleteImplCopyWith(
          _$CacheAddNewAssetEventDeleteImpl value,
          $Res Function(_$CacheAddNewAssetEventDeleteImpl) then) =
      __$$CacheAddNewAssetEventDeleteImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int id});
}

/// @nodoc
class __$$CacheAddNewAssetEventDeleteImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetEventCopyWithImpl<$Res,
        _$CacheAddNewAssetEventDeleteImpl>
    implements _$$CacheAddNewAssetEventDeleteImplCopyWith<$Res> {
  __$$CacheAddNewAssetEventDeleteImplCopyWithImpl(
      _$CacheAddNewAssetEventDeleteImpl _value,
      $Res Function(_$CacheAddNewAssetEventDeleteImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
  }) {
    return _then(_$CacheAddNewAssetEventDeleteImpl(
      null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$CacheAddNewAssetEventDeleteImpl implements CacheAddNewAssetEventDelete {
  const _$CacheAddNewAssetEventDeleteImpl(this.id);

  @override
  final int id;

  @override
  String toString() {
    return 'CacheAddNewAssetEvent.delete(id: $id)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAddNewAssetEventDeleteImpl &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAddNewAssetEventDeleteImplCopyWith<_$CacheAddNewAssetEventDeleteImpl>
      get copyWith => __$$CacheAddNewAssetEventDeleteImplCopyWithImpl<
          _$CacheAddNewAssetEventDeleteImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) {
    return delete(id);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) {
    return delete?.call(id);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) {
    if (delete != null) {
      return delete(id);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) {
    return delete(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) {
    return delete?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) {
    if (delete != null) {
      return delete(this);
    }
    return orElse();
  }
}

abstract class CacheAddNewAssetEventDelete implements CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEventDelete(final int id) =
      _$CacheAddNewAssetEventDeleteImpl;

  int get id;
  @JsonKey(ignore: true)
  _$$CacheAddNewAssetEventDeleteImplCopyWith<_$CacheAddNewAssetEventDeleteImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAddNewAssetEventDeleteAllImplCopyWith<$Res> {
  factory _$$CacheAddNewAssetEventDeleteAllImplCopyWith(
          _$CacheAddNewAssetEventDeleteAllImpl value,
          $Res Function(_$CacheAddNewAssetEventDeleteAllImpl) then) =
      __$$CacheAddNewAssetEventDeleteAllImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId, String assetType});
}

/// @nodoc
class __$$CacheAddNewAssetEventDeleteAllImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetEventCopyWithImpl<$Res,
        _$CacheAddNewAssetEventDeleteAllImpl>
    implements _$$CacheAddNewAssetEventDeleteAllImplCopyWith<$Res> {
  __$$CacheAddNewAssetEventDeleteAllImplCopyWithImpl(
      _$CacheAddNewAssetEventDeleteAllImpl _value,
      $Res Function(_$CacheAddNewAssetEventDeleteAllImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? assetType = null,
  }) {
    return _then(_$CacheAddNewAssetEventDeleteAllImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      null == assetType
          ? _value.assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$CacheAddNewAssetEventDeleteAllImpl
    implements CacheAddNewAssetEventDeleteAll {
  const _$CacheAddNewAssetEventDeleteAllImpl(
      this.activityFacilityId, this.assetType);

  @override
  final String activityFacilityId;
  @override
  final String assetType;

  @override
  String toString() {
    return 'CacheAddNewAssetEvent.deleteAll(activityFacilityId: $activityFacilityId, assetType: $assetType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAddNewAssetEventDeleteAllImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.assetType, assetType) ||
                other.assetType == assetType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, assetType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAddNewAssetEventDeleteAllImplCopyWith<
          _$CacheAddNewAssetEventDeleteAllImpl>
      get copyWith => __$$CacheAddNewAssetEventDeleteAllImplCopyWithImpl<
          _$CacheAddNewAssetEventDeleteAllImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) {
    return deleteAll(activityFacilityId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) {
    return deleteAll?.call(activityFacilityId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) {
    if (deleteAll != null) {
      return deleteAll(activityFacilityId, assetType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) {
    return deleteAll(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) {
    return deleteAll?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) {
    if (deleteAll != null) {
      return deleteAll(this);
    }
    return orElse();
  }
}

abstract class CacheAddNewAssetEventDeleteAll implements CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEventDeleteAll(
          final String activityFacilityId, final String assetType) =
      _$CacheAddNewAssetEventDeleteAllImpl;

  String get activityFacilityId;
  String get assetType;
  @JsonKey(ignore: true)
  _$$CacheAddNewAssetEventDeleteAllImplCopyWith<
          _$CacheAddNewAssetEventDeleteAllImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAddNewAssetEventReplaceAllImplCopyWith<$Res> {
  factory _$$CacheAddNewAssetEventReplaceAllImplCopyWith(
          _$CacheAddNewAssetEventReplaceAllImpl value,
          $Res Function(_$CacheAddNewAssetEventReplaceAllImpl) then) =
      __$$CacheAddNewAssetEventReplaceAllImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {String activityFacilityId,
      String assetType,
      List<CacheAddNewAsset> entries});
}

/// @nodoc
class __$$CacheAddNewAssetEventReplaceAllImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetEventCopyWithImpl<$Res,
        _$CacheAddNewAssetEventReplaceAllImpl>
    implements _$$CacheAddNewAssetEventReplaceAllImplCopyWith<$Res> {
  __$$CacheAddNewAssetEventReplaceAllImplCopyWithImpl(
      _$CacheAddNewAssetEventReplaceAllImpl _value,
      $Res Function(_$CacheAddNewAssetEventReplaceAllImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
    Object? assetType = null,
    Object? entries = null,
  }) {
    return _then(_$CacheAddNewAssetEventReplaceAllImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
      null == assetType
          ? _value.assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as String,
      null == entries
          ? _value._entries
          : entries // ignore: cast_nullable_to_non_nullable
              as List<CacheAddNewAsset>,
    ));
  }
}

/// @nodoc

class _$CacheAddNewAssetEventReplaceAllImpl
    implements CacheAddNewAssetEventReplaceAll {
  const _$CacheAddNewAssetEventReplaceAllImpl(this.activityFacilityId,
      this.assetType, final List<CacheAddNewAsset> entries)
      : _entries = entries;

  @override
  final String activityFacilityId;
  @override
  final String assetType;
  final List<CacheAddNewAsset> _entries;
  @override
  List<CacheAddNewAsset> get entries {
    if (_entries is EqualUnmodifiableListView) return _entries;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entries);
  }

  @override
  String toString() {
    return 'CacheAddNewAssetEvent.replaceAll(activityFacilityId: $activityFacilityId, assetType: $assetType, entries: $entries)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAddNewAssetEventReplaceAllImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId) &&
            (identical(other.assetType, assetType) ||
                other.assetType == assetType) &&
            const DeepCollectionEquality().equals(other._entries, _entries));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId, assetType,
      const DeepCollectionEquality().hash(_entries));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAddNewAssetEventReplaceAllImplCopyWith<
          _$CacheAddNewAssetEventReplaceAllImpl>
      get copyWith => __$$CacheAddNewAssetEventReplaceAllImplCopyWithImpl<
          _$CacheAddNewAssetEventReplaceAllImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId, String assetType) get,
    required TResult Function(CacheAddNewAsset entry) add,
    required TResult Function(CacheAddNewAsset entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String activityFacilityId, String assetType)
        deleteAll,
    required TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)
        replaceAll,
  }) {
    return replaceAll(activityFacilityId, assetType, entries);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId, String assetType)? get,
    TResult? Function(CacheAddNewAsset entry)? add,
    TResult? Function(CacheAddNewAsset entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String activityFacilityId, String assetType)? deleteAll,
    TResult? Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
  }) {
    return replaceAll?.call(activityFacilityId, assetType, entries);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId, String assetType)? get,
    TResult Function(CacheAddNewAsset entry)? add,
    TResult Function(CacheAddNewAsset entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String activityFacilityId, String assetType)? deleteAll,
    TResult Function(String activityFacilityId, String assetType,
            List<CacheAddNewAsset> entries)?
        replaceAll,
    required TResult orElse(),
  }) {
    if (replaceAll != null) {
      return replaceAll(activityFacilityId, assetType, entries);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAddNewAssetEventGet value) get,
    required TResult Function(CacheAddNewAssetEventAdd value) add,
    required TResult Function(CacheAddNewAssetEventUpdate value) update,
    required TResult Function(CacheAddNewAssetEventDelete value) delete,
    required TResult Function(CacheAddNewAssetEventDeleteAll value) deleteAll,
    required TResult Function(CacheAddNewAssetEventReplaceAll value) replaceAll,
  }) {
    return replaceAll(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAddNewAssetEventGet value)? get,
    TResult? Function(CacheAddNewAssetEventAdd value)? add,
    TResult? Function(CacheAddNewAssetEventUpdate value)? update,
    TResult? Function(CacheAddNewAssetEventDelete value)? delete,
    TResult? Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult? Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
  }) {
    return replaceAll?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAddNewAssetEventGet value)? get,
    TResult Function(CacheAddNewAssetEventAdd value)? add,
    TResult Function(CacheAddNewAssetEventUpdate value)? update,
    TResult Function(CacheAddNewAssetEventDelete value)? delete,
    TResult Function(CacheAddNewAssetEventDeleteAll value)? deleteAll,
    TResult Function(CacheAddNewAssetEventReplaceAll value)? replaceAll,
    required TResult orElse(),
  }) {
    if (replaceAll != null) {
      return replaceAll(this);
    }
    return orElse();
  }
}

abstract class CacheAddNewAssetEventReplaceAll
    implements CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEventReplaceAll(final String activityFacilityId,
          final String assetType, final List<CacheAddNewAsset> entries) =
      _$CacheAddNewAssetEventReplaceAllImpl;

  String get activityFacilityId;
  String get assetType;
  List<CacheAddNewAsset> get entries;
  @JsonKey(ignore: true)
  _$$CacheAddNewAssetEventReplaceAllImplCopyWith<
          _$CacheAddNewAssetEventReplaceAllImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheAddNewAssetState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheAddNewAssetStateCopyWith<$Res> {
  factory $CacheAddNewAssetStateCopyWith(CacheAddNewAssetState value,
          $Res Function(CacheAddNewAssetState) then) =
      _$CacheAddNewAssetStateCopyWithImpl<$Res, CacheAddNewAssetState>;
}

/// @nodoc
class _$CacheAddNewAssetStateCopyWithImpl<$Res,
        $Val extends CacheAddNewAssetState>
    implements $CacheAddNewAssetStateCopyWith<$Res> {
  _$CacheAddNewAssetStateCopyWithImpl(this._value, this._then);

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
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'CacheAddNewAssetState.initial()';
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
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
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
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements CacheAddNewAssetState {
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
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'CacheAddNewAssetState.loading()';
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
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
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
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _Loading implements CacheAddNewAssetState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$LoadedImplCopyWith<$Res> {
  factory _$$LoadedImplCopyWith(
          _$LoadedImpl value, $Res Function(_$LoadedImpl) then) =
      __$$LoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CacheAddNewAsset> entries});
}

/// @nodoc
class __$$LoadedImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$LoadedImpl>
    implements _$$LoadedImplCopyWith<$Res> {
  __$$LoadedImplCopyWithImpl(
      _$LoadedImpl _value, $Res Function(_$LoadedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entries = null,
  }) {
    return _then(_$LoadedImpl(
      null == entries
          ? _value._entries
          : entries // ignore: cast_nullable_to_non_nullable
              as List<CacheAddNewAsset>,
    ));
  }
}

/// @nodoc

class _$LoadedImpl implements _Loaded {
  const _$LoadedImpl(final List<CacheAddNewAsset> entries) : _entries = entries;

  final List<CacheAddNewAsset> _entries;
  @override
  List<CacheAddNewAsset> get entries {
    if (_entries is EqualUnmodifiableListView) return _entries;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entries);
  }

  @override
  String toString() {
    return 'CacheAddNewAssetState.loaded(entries: $entries)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$LoadedImpl &&
            const DeepCollectionEquality().equals(other._entries, _entries));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, const DeepCollectionEquality().hash(_entries));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$LoadedImplCopyWith<_$LoadedImpl> get copyWith =>
      __$$LoadedImplCopyWithImpl<_$LoadedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return loaded(entries);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return loaded?.call(entries);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (loaded != null) {
      return loaded(entries);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return loaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return loaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (loaded != null) {
      return loaded(this);
    }
    return orElse();
  }
}

abstract class _Loaded implements CacheAddNewAssetState {
  const factory _Loaded(final List<CacheAddNewAsset> entries) = _$LoadedImpl;

  List<CacheAddNewAsset> get entries;
  @JsonKey(ignore: true)
  _$$LoadedImplCopyWith<_$LoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$AddedImplCopyWith<$Res> {
  factory _$$AddedImplCopyWith(
          _$AddedImpl value, $Res Function(_$AddedImpl) then) =
      __$$AddedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAddNewAsset entry});
}

/// @nodoc
class __$$AddedImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$AddedImpl>
    implements _$$AddedImplCopyWith<$Res> {
  __$$AddedImplCopyWithImpl(
      _$AddedImpl _value, $Res Function(_$AddedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$AddedImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAddNewAsset,
    ));
  }
}

/// @nodoc

class _$AddedImpl implements _Added {
  const _$AddedImpl(this.entry);

  @override
  final CacheAddNewAsset entry;

  @override
  String toString() {
    return 'CacheAddNewAssetState.added(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AddedImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AddedImplCopyWith<_$AddedImpl> get copyWith =>
      __$$AddedImplCopyWithImpl<_$AddedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return added(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return added?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (added != null) {
      return added(entry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return added(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return added?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (added != null) {
      return added(this);
    }
    return orElse();
  }
}

abstract class _Added implements CacheAddNewAssetState {
  const factory _Added(final CacheAddNewAsset entry) = _$AddedImpl;

  CacheAddNewAsset get entry;
  @JsonKey(ignore: true)
  _$$AddedImplCopyWith<_$AddedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$UpdatedImplCopyWith<$Res> {
  factory _$$UpdatedImplCopyWith(
          _$UpdatedImpl value, $Res Function(_$UpdatedImpl) then) =
      __$$UpdatedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAddNewAsset entry});
}

/// @nodoc
class __$$UpdatedImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$UpdatedImpl>
    implements _$$UpdatedImplCopyWith<$Res> {
  __$$UpdatedImplCopyWithImpl(
      _$UpdatedImpl _value, $Res Function(_$UpdatedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$UpdatedImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAddNewAsset,
    ));
  }
}

/// @nodoc

class _$UpdatedImpl implements _Updated {
  const _$UpdatedImpl(this.entry);

  @override
  final CacheAddNewAsset entry;

  @override
  String toString() {
    return 'CacheAddNewAssetState.updated(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UpdatedImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$UpdatedImplCopyWith<_$UpdatedImpl> get copyWith =>
      __$$UpdatedImplCopyWithImpl<_$UpdatedImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return updated(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return updated?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (updated != null) {
      return updated(entry);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return updated(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return updated?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (updated != null) {
      return updated(this);
    }
    return orElse();
  }
}

abstract class _Updated implements CacheAddNewAssetState {
  const factory _Updated(final CacheAddNewAsset entry) = _$UpdatedImpl;

  CacheAddNewAsset get entry;
  @JsonKey(ignore: true)
  _$$UpdatedImplCopyWith<_$UpdatedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DeletedImplCopyWith<$Res> {
  factory _$$DeletedImplCopyWith(
          _$DeletedImpl value, $Res Function(_$DeletedImpl) then) =
      __$$DeletedImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$DeletedImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$DeletedImpl>
    implements _$$DeletedImplCopyWith<$Res> {
  __$$DeletedImplCopyWithImpl(
      _$DeletedImpl _value, $Res Function(_$DeletedImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$DeletedImpl implements _Deleted {
  const _$DeletedImpl();

  @override
  String toString() {
    return 'CacheAddNewAssetState.deleted()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$DeletedImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return deleted();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return deleted?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (deleted != null) {
      return deleted();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return deleted(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return deleted?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (deleted != null) {
      return deleted(this);
    }
    return orElse();
  }
}

abstract class _Deleted implements CacheAddNewAssetState {
  const factory _Deleted() = _$DeletedImpl;
}

/// @nodoc
abstract class _$$NotFoundImplCopyWith<$Res> {
  factory _$$NotFoundImplCopyWith(
          _$NotFoundImpl value, $Res Function(_$NotFoundImpl) then) =
      __$$NotFoundImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$NotFoundImplCopyWithImpl<$Res>
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$NotFoundImpl>
    implements _$$NotFoundImplCopyWith<$Res> {
  __$$NotFoundImplCopyWithImpl(
      _$NotFoundImpl _value, $Res Function(_$NotFoundImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$NotFoundImpl implements _NotFound {
  const _$NotFoundImpl();

  @override
  String toString() {
    return 'CacheAddNewAssetState.notFound()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$NotFoundImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return notFound();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return notFound?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
    TResult Function(String message)? error,
    required TResult orElse(),
  }) {
    if (notFound != null) {
      return notFound();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return notFound(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return notFound?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (notFound != null) {
      return notFound(this);
    }
    return orElse();
  }
}

abstract class _NotFound implements CacheAddNewAssetState {
  const factory _NotFound() = _$NotFoundImpl;
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
    extends _$CacheAddNewAssetStateCopyWithImpl<$Res, _$ErrorImpl>
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

class _$ErrorImpl implements _Error {
  const _$ErrorImpl(this.message);

  @override
  final String message;

  @override
  String toString() {
    return 'CacheAddNewAssetState.error(message: $message)';
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
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAddNewAsset> entries) loaded,
    required TResult Function(CacheAddNewAsset entry) added,
    required TResult Function(CacheAddNewAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) {
    return error(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAddNewAsset> entries)? loaded,
    TResult? Function(CacheAddNewAsset entry)? added,
    TResult? Function(CacheAddNewAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) {
    return error?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAddNewAsset> entries)? loaded,
    TResult Function(CacheAddNewAsset entry)? added,
    TResult Function(CacheAddNewAsset entry)? updated,
    TResult Function()? deleted,
    TResult Function()? notFound,
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
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Added value) added,
    required TResult Function(_Updated value) updated,
    required TResult Function(_Deleted value) deleted,
    required TResult Function(_NotFound value) notFound,
    required TResult Function(_Error value) error,
  }) {
    return error(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Added value)? added,
    TResult? Function(_Updated value)? updated,
    TResult? Function(_Deleted value)? deleted,
    TResult? Function(_NotFound value)? notFound,
    TResult? Function(_Error value)? error,
  }) {
    return error?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Added value)? added,
    TResult Function(_Updated value)? updated,
    TResult Function(_Deleted value)? deleted,
    TResult Function(_NotFound value)? notFound,
    TResult Function(_Error value)? error,
    required TResult orElse(),
  }) {
    if (error != null) {
      return error(this);
    }
    return orElse();
  }
}

abstract class _Error implements CacheAddNewAssetState {
  const factory _Error(final String message) = _$ErrorImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
