// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'cache_activity_facility_asset.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$CacheActivityFacilityAssetEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) get,
    required TResult Function(CacheActivityFacilityAsset entry) add,
    required TResult Function(CacheActivityFacilityAsset entry) update,
    required TResult Function(int id) delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? get,
    TResult? Function(CacheActivityFacilityAsset entry)? add,
    TResult? Function(CacheActivityFacilityAsset entry)? update,
    TResult? Function(int id)? delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? get,
    TResult Function(CacheActivityFacilityAsset entry)? add,
    TResult Function(CacheActivityFacilityAsset entry)? update,
    TResult Function(int id)? delete,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheActivityFacilityAssetEventGet value) get,
    required TResult Function(CacheActivityFacilityAssetEventAdd value) add,
    required TResult Function(CacheActivityFacilityAssetEventUpdate value)
        update,
    required TResult Function(CacheActivityFacilityAssetEventDelete value)
        delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult? Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult? Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult? Function(CacheActivityFacilityAssetEventDelete value)? delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult Function(CacheActivityFacilityAssetEventDelete value)? delete,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheActivityFacilityAssetEventCopyWith<$Res> {
  factory $CacheActivityFacilityAssetEventCopyWith(
          CacheActivityFacilityAssetEvent value,
          $Res Function(CacheActivityFacilityAssetEvent) then) =
      _$CacheActivityFacilityAssetEventCopyWithImpl<$Res,
          CacheActivityFacilityAssetEvent>;
}

/// @nodoc
class _$CacheActivityFacilityAssetEventCopyWithImpl<$Res,
        $Val extends CacheActivityFacilityAssetEvent>
    implements $CacheActivityFacilityAssetEventCopyWith<$Res> {
  _$CacheActivityFacilityAssetEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$CacheActivityFacilityAssetEventGetImplCopyWith<$Res> {
  factory _$$CacheActivityFacilityAssetEventGetImplCopyWith(
          _$CacheActivityFacilityAssetEventGetImpl value,
          $Res Function(_$CacheActivityFacilityAssetEventGetImpl) then) =
      __$$CacheActivityFacilityAssetEventGetImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String activityFacilityId});
}

/// @nodoc
class __$$CacheActivityFacilityAssetEventGetImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetEventCopyWithImpl<$Res,
        _$CacheActivityFacilityAssetEventGetImpl>
    implements _$$CacheActivityFacilityAssetEventGetImplCopyWith<$Res> {
  __$$CacheActivityFacilityAssetEventGetImplCopyWithImpl(
      _$CacheActivityFacilityAssetEventGetImpl _value,
      $Res Function(_$CacheActivityFacilityAssetEventGetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? activityFacilityId = null,
  }) {
    return _then(_$CacheActivityFacilityAssetEventGetImpl(
      null == activityFacilityId
          ? _value.activityFacilityId
          : activityFacilityId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$CacheActivityFacilityAssetEventGetImpl
    implements CacheActivityFacilityAssetEventGet {
  const _$CacheActivityFacilityAssetEventGetImpl(this.activityFacilityId);

  @override
  final String activityFacilityId;

  @override
  String toString() {
    return 'CacheActivityFacilityAssetEvent.get(activityFacilityId: $activityFacilityId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheActivityFacilityAssetEventGetImpl &&
            (identical(other.activityFacilityId, activityFacilityId) ||
                other.activityFacilityId == activityFacilityId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, activityFacilityId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheActivityFacilityAssetEventGetImplCopyWith<
          _$CacheActivityFacilityAssetEventGetImpl>
      get copyWith => __$$CacheActivityFacilityAssetEventGetImplCopyWithImpl<
          _$CacheActivityFacilityAssetEventGetImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) get,
    required TResult Function(CacheActivityFacilityAsset entry) add,
    required TResult Function(CacheActivityFacilityAsset entry) update,
    required TResult Function(int id) delete,
  }) {
    return get(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? get,
    TResult? Function(CacheActivityFacilityAsset entry)? add,
    TResult? Function(CacheActivityFacilityAsset entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return get?.call(activityFacilityId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? get,
    TResult Function(CacheActivityFacilityAsset entry)? add,
    TResult Function(CacheActivityFacilityAsset entry)? update,
    TResult Function(int id)? delete,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(activityFacilityId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheActivityFacilityAssetEventGet value) get,
    required TResult Function(CacheActivityFacilityAssetEventAdd value) add,
    required TResult Function(CacheActivityFacilityAssetEventUpdate value)
        update,
    required TResult Function(CacheActivityFacilityAssetEventDelete value)
        delete,
  }) {
    return get(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult? Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult? Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult? Function(CacheActivityFacilityAssetEventDelete value)? delete,
  }) {
    return get?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult Function(CacheActivityFacilityAssetEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(this);
    }
    return orElse();
  }
}

abstract class CacheActivityFacilityAssetEventGet
    implements CacheActivityFacilityAssetEvent {
  const factory CacheActivityFacilityAssetEventGet(
          final String activityFacilityId) =
      _$CacheActivityFacilityAssetEventGetImpl;

  String get activityFacilityId;
  @JsonKey(ignore: true)
  _$$CacheActivityFacilityAssetEventGetImplCopyWith<
          _$CacheActivityFacilityAssetEventGetImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheActivityFacilityAssetEventAddImplCopyWith<$Res> {
  factory _$$CacheActivityFacilityAssetEventAddImplCopyWith(
          _$CacheActivityFacilityAssetEventAddImpl value,
          $Res Function(_$CacheActivityFacilityAssetEventAddImpl) then) =
      __$$CacheActivityFacilityAssetEventAddImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheActivityFacilityAsset entry});
}

/// @nodoc
class __$$CacheActivityFacilityAssetEventAddImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetEventCopyWithImpl<$Res,
        _$CacheActivityFacilityAssetEventAddImpl>
    implements _$$CacheActivityFacilityAssetEventAddImplCopyWith<$Res> {
  __$$CacheActivityFacilityAssetEventAddImplCopyWithImpl(
      _$CacheActivityFacilityAssetEventAddImpl _value,
      $Res Function(_$CacheActivityFacilityAssetEventAddImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheActivityFacilityAssetEventAddImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheActivityFacilityAsset,
    ));
  }
}

/// @nodoc

class _$CacheActivityFacilityAssetEventAddImpl
    implements CacheActivityFacilityAssetEventAdd {
  const _$CacheActivityFacilityAssetEventAddImpl(this.entry);

  @override
  final CacheActivityFacilityAsset entry;

  @override
  String toString() {
    return 'CacheActivityFacilityAssetEvent.add(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheActivityFacilityAssetEventAddImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheActivityFacilityAssetEventAddImplCopyWith<
          _$CacheActivityFacilityAssetEventAddImpl>
      get copyWith => __$$CacheActivityFacilityAssetEventAddImplCopyWithImpl<
          _$CacheActivityFacilityAssetEventAddImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) get,
    required TResult Function(CacheActivityFacilityAsset entry) add,
    required TResult Function(CacheActivityFacilityAsset entry) update,
    required TResult Function(int id) delete,
  }) {
    return add(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? get,
    TResult? Function(CacheActivityFacilityAsset entry)? add,
    TResult? Function(CacheActivityFacilityAsset entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return add?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? get,
    TResult Function(CacheActivityFacilityAsset entry)? add,
    TResult Function(CacheActivityFacilityAsset entry)? update,
    TResult Function(int id)? delete,
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
    required TResult Function(CacheActivityFacilityAssetEventGet value) get,
    required TResult Function(CacheActivityFacilityAssetEventAdd value) add,
    required TResult Function(CacheActivityFacilityAssetEventUpdate value)
        update,
    required TResult Function(CacheActivityFacilityAssetEventDelete value)
        delete,
  }) {
    return add(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult? Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult? Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult? Function(CacheActivityFacilityAssetEventDelete value)? delete,
  }) {
    return add?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult Function(CacheActivityFacilityAssetEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (add != null) {
      return add(this);
    }
    return orElse();
  }
}

abstract class CacheActivityFacilityAssetEventAdd
    implements CacheActivityFacilityAssetEvent {
  const factory CacheActivityFacilityAssetEventAdd(
          final CacheActivityFacilityAsset entry) =
      _$CacheActivityFacilityAssetEventAddImpl;

  CacheActivityFacilityAsset get entry;
  @JsonKey(ignore: true)
  _$$CacheActivityFacilityAssetEventAddImplCopyWith<
          _$CacheActivityFacilityAssetEventAddImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheActivityFacilityAssetEventUpdateImplCopyWith<$Res> {
  factory _$$CacheActivityFacilityAssetEventUpdateImplCopyWith(
          _$CacheActivityFacilityAssetEventUpdateImpl value,
          $Res Function(_$CacheActivityFacilityAssetEventUpdateImpl) then) =
      __$$CacheActivityFacilityAssetEventUpdateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheActivityFacilityAsset entry});
}

/// @nodoc
class __$$CacheActivityFacilityAssetEventUpdateImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetEventCopyWithImpl<$Res,
        _$CacheActivityFacilityAssetEventUpdateImpl>
    implements _$$CacheActivityFacilityAssetEventUpdateImplCopyWith<$Res> {
  __$$CacheActivityFacilityAssetEventUpdateImplCopyWithImpl(
      _$CacheActivityFacilityAssetEventUpdateImpl _value,
      $Res Function(_$CacheActivityFacilityAssetEventUpdateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheActivityFacilityAssetEventUpdateImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheActivityFacilityAsset,
    ));
  }
}

/// @nodoc

class _$CacheActivityFacilityAssetEventUpdateImpl
    implements CacheActivityFacilityAssetEventUpdate {
  const _$CacheActivityFacilityAssetEventUpdateImpl(this.entry);

  @override
  final CacheActivityFacilityAsset entry;

  @override
  String toString() {
    return 'CacheActivityFacilityAssetEvent.update(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheActivityFacilityAssetEventUpdateImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheActivityFacilityAssetEventUpdateImplCopyWith<
          _$CacheActivityFacilityAssetEventUpdateImpl>
      get copyWith => __$$CacheActivityFacilityAssetEventUpdateImplCopyWithImpl<
          _$CacheActivityFacilityAssetEventUpdateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) get,
    required TResult Function(CacheActivityFacilityAsset entry) add,
    required TResult Function(CacheActivityFacilityAsset entry) update,
    required TResult Function(int id) delete,
  }) {
    return update(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? get,
    TResult? Function(CacheActivityFacilityAsset entry)? add,
    TResult? Function(CacheActivityFacilityAsset entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return update?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? get,
    TResult Function(CacheActivityFacilityAsset entry)? add,
    TResult Function(CacheActivityFacilityAsset entry)? update,
    TResult Function(int id)? delete,
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
    required TResult Function(CacheActivityFacilityAssetEventGet value) get,
    required TResult Function(CacheActivityFacilityAssetEventAdd value) add,
    required TResult Function(CacheActivityFacilityAssetEventUpdate value)
        update,
    required TResult Function(CacheActivityFacilityAssetEventDelete value)
        delete,
  }) {
    return update(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult? Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult? Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult? Function(CacheActivityFacilityAssetEventDelete value)? delete,
  }) {
    return update?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult Function(CacheActivityFacilityAssetEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (update != null) {
      return update(this);
    }
    return orElse();
  }
}

abstract class CacheActivityFacilityAssetEventUpdate
    implements CacheActivityFacilityAssetEvent {
  const factory CacheActivityFacilityAssetEventUpdate(
          final CacheActivityFacilityAsset entry) =
      _$CacheActivityFacilityAssetEventUpdateImpl;

  CacheActivityFacilityAsset get entry;
  @JsonKey(ignore: true)
  _$$CacheActivityFacilityAssetEventUpdateImplCopyWith<
          _$CacheActivityFacilityAssetEventUpdateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheActivityFacilityAssetEventDeleteImplCopyWith<$Res> {
  factory _$$CacheActivityFacilityAssetEventDeleteImplCopyWith(
          _$CacheActivityFacilityAssetEventDeleteImpl value,
          $Res Function(_$CacheActivityFacilityAssetEventDeleteImpl) then) =
      __$$CacheActivityFacilityAssetEventDeleteImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int id});
}

/// @nodoc
class __$$CacheActivityFacilityAssetEventDeleteImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetEventCopyWithImpl<$Res,
        _$CacheActivityFacilityAssetEventDeleteImpl>
    implements _$$CacheActivityFacilityAssetEventDeleteImplCopyWith<$Res> {
  __$$CacheActivityFacilityAssetEventDeleteImplCopyWithImpl(
      _$CacheActivityFacilityAssetEventDeleteImpl _value,
      $Res Function(_$CacheActivityFacilityAssetEventDeleteImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
  }) {
    return _then(_$CacheActivityFacilityAssetEventDeleteImpl(
      null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$CacheActivityFacilityAssetEventDeleteImpl
    implements CacheActivityFacilityAssetEventDelete {
  const _$CacheActivityFacilityAssetEventDeleteImpl(this.id);

  @override
  final int id;

  @override
  String toString() {
    return 'CacheActivityFacilityAssetEvent.delete(id: $id)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheActivityFacilityAssetEventDeleteImpl &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheActivityFacilityAssetEventDeleteImplCopyWith<
          _$CacheActivityFacilityAssetEventDeleteImpl>
      get copyWith => __$$CacheActivityFacilityAssetEventDeleteImplCopyWithImpl<
          _$CacheActivityFacilityAssetEventDeleteImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String activityFacilityId) get,
    required TResult Function(CacheActivityFacilityAsset entry) add,
    required TResult Function(CacheActivityFacilityAsset entry) update,
    required TResult Function(int id) delete,
  }) {
    return delete(id);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String activityFacilityId)? get,
    TResult? Function(CacheActivityFacilityAsset entry)? add,
    TResult? Function(CacheActivityFacilityAsset entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return delete?.call(id);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String activityFacilityId)? get,
    TResult Function(CacheActivityFacilityAsset entry)? add,
    TResult Function(CacheActivityFacilityAsset entry)? update,
    TResult Function(int id)? delete,
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
    required TResult Function(CacheActivityFacilityAssetEventGet value) get,
    required TResult Function(CacheActivityFacilityAssetEventAdd value) add,
    required TResult Function(CacheActivityFacilityAssetEventUpdate value)
        update,
    required TResult Function(CacheActivityFacilityAssetEventDelete value)
        delete,
  }) {
    return delete(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult? Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult? Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult? Function(CacheActivityFacilityAssetEventDelete value)? delete,
  }) {
    return delete?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheActivityFacilityAssetEventGet value)? get,
    TResult Function(CacheActivityFacilityAssetEventAdd value)? add,
    TResult Function(CacheActivityFacilityAssetEventUpdate value)? update,
    TResult Function(CacheActivityFacilityAssetEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (delete != null) {
      return delete(this);
    }
    return orElse();
  }
}

abstract class CacheActivityFacilityAssetEventDelete
    implements CacheActivityFacilityAssetEvent {
  const factory CacheActivityFacilityAssetEventDelete(final int id) =
      _$CacheActivityFacilityAssetEventDeleteImpl;

  int get id;
  @JsonKey(ignore: true)
  _$$CacheActivityFacilityAssetEventDeleteImplCopyWith<
          _$CacheActivityFacilityAssetEventDeleteImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheActivityFacilityAssetState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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
abstract class $CacheActivityFacilityAssetStateCopyWith<$Res> {
  factory $CacheActivityFacilityAssetStateCopyWith(
          CacheActivityFacilityAssetState value,
          $Res Function(CacheActivityFacilityAssetState) then) =
      _$CacheActivityFacilityAssetStateCopyWithImpl<$Res,
          CacheActivityFacilityAssetState>;
}

/// @nodoc
class _$CacheActivityFacilityAssetStateCopyWithImpl<$Res,
        $Val extends CacheActivityFacilityAssetState>
    implements $CacheActivityFacilityAssetStateCopyWith<$Res> {
  _$CacheActivityFacilityAssetStateCopyWithImpl(this._value, this._then);

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
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'CacheActivityFacilityAssetState.initial()';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Initial implements CacheActivityFacilityAssetState {
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
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'CacheActivityFacilityAssetState.loading()';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Loading implements CacheActivityFacilityAssetState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$LoadedImplCopyWith<$Res> {
  factory _$$LoadedImplCopyWith(
          _$LoadedImpl value, $Res Function(_$LoadedImpl) then) =
      __$$LoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CacheActivityFacilityAsset> entries});
}

/// @nodoc
class __$$LoadedImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$LoadedImpl>
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
              as List<CacheActivityFacilityAsset>,
    ));
  }
}

/// @nodoc

class _$LoadedImpl implements _Loaded {
  const _$LoadedImpl(final List<CacheActivityFacilityAsset> entries)
      : _entries = entries;

  final List<CacheActivityFacilityAsset> _entries;
  @override
  List<CacheActivityFacilityAsset> get entries {
    if (_entries is EqualUnmodifiableListView) return _entries;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entries);
  }

  @override
  String toString() {
    return 'CacheActivityFacilityAssetState.loaded(entries: $entries)';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Loaded implements CacheActivityFacilityAssetState {
  const factory _Loaded(final List<CacheActivityFacilityAsset> entries) =
      _$LoadedImpl;

  List<CacheActivityFacilityAsset> get entries;
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
  $Res call({CacheActivityFacilityAsset entry});
}

/// @nodoc
class __$$AddedImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$AddedImpl>
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
              as CacheActivityFacilityAsset,
    ));
  }
}

/// @nodoc

class _$AddedImpl implements _Added {
  const _$AddedImpl(this.entry);

  @override
  final CacheActivityFacilityAsset entry;

  @override
  String toString() {
    return 'CacheActivityFacilityAssetState.added(entry: $entry)';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Added implements CacheActivityFacilityAssetState {
  const factory _Added(final CacheActivityFacilityAsset entry) = _$AddedImpl;

  CacheActivityFacilityAsset get entry;
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
  $Res call({CacheActivityFacilityAsset entry});
}

/// @nodoc
class __$$UpdatedImplCopyWithImpl<$Res>
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$UpdatedImpl>
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
              as CacheActivityFacilityAsset,
    ));
  }
}

/// @nodoc

class _$UpdatedImpl implements _Updated {
  const _$UpdatedImpl(this.entry);

  @override
  final CacheActivityFacilityAsset entry;

  @override
  String toString() {
    return 'CacheActivityFacilityAssetState.updated(entry: $entry)';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Updated implements CacheActivityFacilityAssetState {
  const factory _Updated(final CacheActivityFacilityAsset entry) =
      _$UpdatedImpl;

  CacheActivityFacilityAsset get entry;
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
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$DeletedImpl>
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
    return 'CacheActivityFacilityAssetState.deleted()';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Deleted implements CacheActivityFacilityAssetState {
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
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$NotFoundImpl>
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
    return 'CacheActivityFacilityAssetState.notFound()';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _NotFound implements CacheActivityFacilityAssetState {
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
    extends _$CacheActivityFacilityAssetStateCopyWithImpl<$Res, _$ErrorImpl>
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
    return 'CacheActivityFacilityAssetState.error(message: $message)';
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
    required TResult Function(List<CacheActivityFacilityAsset> entries) loaded,
    required TResult Function(CacheActivityFacilityAsset entry) added,
    required TResult Function(CacheActivityFacilityAsset entry) updated,
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
    TResult? Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult? Function(CacheActivityFacilityAsset entry)? added,
    TResult? Function(CacheActivityFacilityAsset entry)? updated,
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
    TResult Function(List<CacheActivityFacilityAsset> entries)? loaded,
    TResult Function(CacheActivityFacilityAsset entry)? added,
    TResult Function(CacheActivityFacilityAsset entry)? updated,
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

abstract class _Error implements CacheActivityFacilityAssetState {
  const factory _Error(final String message) = _$ErrorImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
