// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'cache_asset_detail.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$CacheAssetDetailEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheAssetDetail entry) add,
    required TResult Function(CacheAssetDetail entry) update,
    required TResult Function(int id) delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheAssetDetail entry)? add,
    TResult? Function(CacheAssetDetail entry)? update,
    TResult? Function(int id)? delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheAssetDetail entry)? add,
    TResult Function(CacheAssetDetail entry)? update,
    TResult Function(int id)? delete,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAssetDetailEventGet value) get,
    required TResult Function(CacheAssetDetailEventAdd value) add,
    required TResult Function(CacheAssetDetailEventUpdate value) update,
    required TResult Function(CacheAssetDetailEventDelete value) delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAssetDetailEventGet value)? get,
    TResult? Function(CacheAssetDetailEventAdd value)? add,
    TResult? Function(CacheAssetDetailEventUpdate value)? update,
    TResult? Function(CacheAssetDetailEventDelete value)? delete,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAssetDetailEventGet value)? get,
    TResult Function(CacheAssetDetailEventAdd value)? add,
    TResult Function(CacheAssetDetailEventUpdate value)? update,
    TResult Function(CacheAssetDetailEventDelete value)? delete,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheAssetDetailEventCopyWith<$Res> {
  factory $CacheAssetDetailEventCopyWith(CacheAssetDetailEvent value,
          $Res Function(CacheAssetDetailEvent) then) =
      _$CacheAssetDetailEventCopyWithImpl<$Res, CacheAssetDetailEvent>;
}

/// @nodoc
class _$CacheAssetDetailEventCopyWithImpl<$Res,
        $Val extends CacheAssetDetailEvent>
    implements $CacheAssetDetailEventCopyWith<$Res> {
  _$CacheAssetDetailEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$CacheAssetDetailEventGetImplCopyWith<$Res> {
  factory _$$CacheAssetDetailEventGetImplCopyWith(
          _$CacheAssetDetailEventGetImpl value,
          $Res Function(_$CacheAssetDetailEventGetImpl) then) =
      __$$CacheAssetDetailEventGetImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId, String assetType});
}

/// @nodoc
class __$$CacheAssetDetailEventGetImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailEventCopyWithImpl<$Res,
        _$CacheAssetDetailEventGetImpl>
    implements _$$CacheAssetDetailEventGetImplCopyWith<$Res> {
  __$$CacheAssetDetailEventGetImplCopyWithImpl(
      _$CacheAssetDetailEventGetImpl _value,
      $Res Function(_$CacheAssetDetailEventGetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? assetType = null,
  }) {
    return _then(_$CacheAssetDetailEventGetImpl(
      null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      null == assetType
          ? _value.assetType
          : assetType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$CacheAssetDetailEventGetImpl implements CacheAssetDetailEventGet {
  const _$CacheAssetDetailEventGetImpl(this.projectId, this.assetType);

  @override
  final String projectId;
  @override
  final String assetType;

  @override
  String toString() {
    return 'CacheAssetDetailEvent.get(projectId: $projectId, assetType: $assetType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAssetDetailEventGetImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.assetType, assetType) ||
                other.assetType == assetType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, assetType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAssetDetailEventGetImplCopyWith<_$CacheAssetDetailEventGetImpl>
      get copyWith => __$$CacheAssetDetailEventGetImplCopyWithImpl<
          _$CacheAssetDetailEventGetImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheAssetDetail entry) add,
    required TResult Function(CacheAssetDetail entry) update,
    required TResult Function(int id) delete,
  }) {
    return get(projectId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheAssetDetail entry)? add,
    TResult? Function(CacheAssetDetail entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return get?.call(projectId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheAssetDetail entry)? add,
    TResult Function(CacheAssetDetail entry)? update,
    TResult Function(int id)? delete,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(projectId, assetType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAssetDetailEventGet value) get,
    required TResult Function(CacheAssetDetailEventAdd value) add,
    required TResult Function(CacheAssetDetailEventUpdate value) update,
    required TResult Function(CacheAssetDetailEventDelete value) delete,
  }) {
    return get(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAssetDetailEventGet value)? get,
    TResult? Function(CacheAssetDetailEventAdd value)? add,
    TResult? Function(CacheAssetDetailEventUpdate value)? update,
    TResult? Function(CacheAssetDetailEventDelete value)? delete,
  }) {
    return get?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAssetDetailEventGet value)? get,
    TResult Function(CacheAssetDetailEventAdd value)? add,
    TResult Function(CacheAssetDetailEventUpdate value)? update,
    TResult Function(CacheAssetDetailEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(this);
    }
    return orElse();
  }
}

abstract class CacheAssetDetailEventGet implements CacheAssetDetailEvent {
  const factory CacheAssetDetailEventGet(
          final String projectId, final String assetType) =
      _$CacheAssetDetailEventGetImpl;

  String get projectId;
  String get assetType;
  @JsonKey(ignore: true)
  _$$CacheAssetDetailEventGetImplCopyWith<_$CacheAssetDetailEventGetImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAssetDetailEventAddImplCopyWith<$Res> {
  factory _$$CacheAssetDetailEventAddImplCopyWith(
          _$CacheAssetDetailEventAddImpl value,
          $Res Function(_$CacheAssetDetailEventAddImpl) then) =
      __$$CacheAssetDetailEventAddImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAssetDetail entry});
}

/// @nodoc
class __$$CacheAssetDetailEventAddImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailEventCopyWithImpl<$Res,
        _$CacheAssetDetailEventAddImpl>
    implements _$$CacheAssetDetailEventAddImplCopyWith<$Res> {
  __$$CacheAssetDetailEventAddImplCopyWithImpl(
      _$CacheAssetDetailEventAddImpl _value,
      $Res Function(_$CacheAssetDetailEventAddImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheAssetDetailEventAddImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAssetDetail,
    ));
  }
}

/// @nodoc

class _$CacheAssetDetailEventAddImpl implements CacheAssetDetailEventAdd {
  const _$CacheAssetDetailEventAddImpl(this.entry);

  @override
  final CacheAssetDetail entry;

  @override
  String toString() {
    return 'CacheAssetDetailEvent.add(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAssetDetailEventAddImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAssetDetailEventAddImplCopyWith<_$CacheAssetDetailEventAddImpl>
      get copyWith => __$$CacheAssetDetailEventAddImplCopyWithImpl<
          _$CacheAssetDetailEventAddImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheAssetDetail entry) add,
    required TResult Function(CacheAssetDetail entry) update,
    required TResult Function(int id) delete,
  }) {
    return add(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheAssetDetail entry)? add,
    TResult? Function(CacheAssetDetail entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return add?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheAssetDetail entry)? add,
    TResult Function(CacheAssetDetail entry)? update,
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
    required TResult Function(CacheAssetDetailEventGet value) get,
    required TResult Function(CacheAssetDetailEventAdd value) add,
    required TResult Function(CacheAssetDetailEventUpdate value) update,
    required TResult Function(CacheAssetDetailEventDelete value) delete,
  }) {
    return add(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAssetDetailEventGet value)? get,
    TResult? Function(CacheAssetDetailEventAdd value)? add,
    TResult? Function(CacheAssetDetailEventUpdate value)? update,
    TResult? Function(CacheAssetDetailEventDelete value)? delete,
  }) {
    return add?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAssetDetailEventGet value)? get,
    TResult Function(CacheAssetDetailEventAdd value)? add,
    TResult Function(CacheAssetDetailEventUpdate value)? update,
    TResult Function(CacheAssetDetailEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (add != null) {
      return add(this);
    }
    return orElse();
  }
}

abstract class CacheAssetDetailEventAdd implements CacheAssetDetailEvent {
  const factory CacheAssetDetailEventAdd(final CacheAssetDetail entry) =
      _$CacheAssetDetailEventAddImpl;

  CacheAssetDetail get entry;
  @JsonKey(ignore: true)
  _$$CacheAssetDetailEventAddImplCopyWith<_$CacheAssetDetailEventAddImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAssetDetailEventUpdateImplCopyWith<$Res> {
  factory _$$CacheAssetDetailEventUpdateImplCopyWith(
          _$CacheAssetDetailEventUpdateImpl value,
          $Res Function(_$CacheAssetDetailEventUpdateImpl) then) =
      __$$CacheAssetDetailEventUpdateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAssetDetail entry});
}

/// @nodoc
class __$$CacheAssetDetailEventUpdateImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailEventCopyWithImpl<$Res,
        _$CacheAssetDetailEventUpdateImpl>
    implements _$$CacheAssetDetailEventUpdateImplCopyWith<$Res> {
  __$$CacheAssetDetailEventUpdateImplCopyWithImpl(
      _$CacheAssetDetailEventUpdateImpl _value,
      $Res Function(_$CacheAssetDetailEventUpdateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheAssetDetailEventUpdateImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAssetDetail,
    ));
  }
}

/// @nodoc

class _$CacheAssetDetailEventUpdateImpl implements CacheAssetDetailEventUpdate {
  const _$CacheAssetDetailEventUpdateImpl(this.entry);

  @override
  final CacheAssetDetail entry;

  @override
  String toString() {
    return 'CacheAssetDetailEvent.update(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAssetDetailEventUpdateImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAssetDetailEventUpdateImplCopyWith<_$CacheAssetDetailEventUpdateImpl>
      get copyWith => __$$CacheAssetDetailEventUpdateImplCopyWithImpl<
          _$CacheAssetDetailEventUpdateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheAssetDetail entry) add,
    required TResult Function(CacheAssetDetail entry) update,
    required TResult Function(int id) delete,
  }) {
    return update(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheAssetDetail entry)? add,
    TResult? Function(CacheAssetDetail entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return update?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheAssetDetail entry)? add,
    TResult Function(CacheAssetDetail entry)? update,
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
    required TResult Function(CacheAssetDetailEventGet value) get,
    required TResult Function(CacheAssetDetailEventAdd value) add,
    required TResult Function(CacheAssetDetailEventUpdate value) update,
    required TResult Function(CacheAssetDetailEventDelete value) delete,
  }) {
    return update(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAssetDetailEventGet value)? get,
    TResult? Function(CacheAssetDetailEventAdd value)? add,
    TResult? Function(CacheAssetDetailEventUpdate value)? update,
    TResult? Function(CacheAssetDetailEventDelete value)? delete,
  }) {
    return update?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAssetDetailEventGet value)? get,
    TResult Function(CacheAssetDetailEventAdd value)? add,
    TResult Function(CacheAssetDetailEventUpdate value)? update,
    TResult Function(CacheAssetDetailEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (update != null) {
      return update(this);
    }
    return orElse();
  }
}

abstract class CacheAssetDetailEventUpdate implements CacheAssetDetailEvent {
  const factory CacheAssetDetailEventUpdate(final CacheAssetDetail entry) =
      _$CacheAssetDetailEventUpdateImpl;

  CacheAssetDetail get entry;
  @JsonKey(ignore: true)
  _$$CacheAssetDetailEventUpdateImplCopyWith<_$CacheAssetDetailEventUpdateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAssetDetailEventDeleteImplCopyWith<$Res> {
  factory _$$CacheAssetDetailEventDeleteImplCopyWith(
          _$CacheAssetDetailEventDeleteImpl value,
          $Res Function(_$CacheAssetDetailEventDeleteImpl) then) =
      __$$CacheAssetDetailEventDeleteImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int id});
}

/// @nodoc
class __$$CacheAssetDetailEventDeleteImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailEventCopyWithImpl<$Res,
        _$CacheAssetDetailEventDeleteImpl>
    implements _$$CacheAssetDetailEventDeleteImplCopyWith<$Res> {
  __$$CacheAssetDetailEventDeleteImplCopyWithImpl(
      _$CacheAssetDetailEventDeleteImpl _value,
      $Res Function(_$CacheAssetDetailEventDeleteImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
  }) {
    return _then(_$CacheAssetDetailEventDeleteImpl(
      null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$CacheAssetDetailEventDeleteImpl implements CacheAssetDetailEventDelete {
  const _$CacheAssetDetailEventDeleteImpl(this.id);

  @override
  final int id;

  @override
  String toString() {
    return 'CacheAssetDetailEvent.delete(id: $id)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAssetDetailEventDeleteImpl &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAssetDetailEventDeleteImplCopyWith<_$CacheAssetDetailEventDeleteImpl>
      get copyWith => __$$CacheAssetDetailEventDeleteImplCopyWithImpl<
          _$CacheAssetDetailEventDeleteImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheAssetDetail entry) add,
    required TResult Function(CacheAssetDetail entry) update,
    required TResult Function(int id) delete,
  }) {
    return delete(id);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheAssetDetail entry)? add,
    TResult? Function(CacheAssetDetail entry)? update,
    TResult? Function(int id)? delete,
  }) {
    return delete?.call(id);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheAssetDetail entry)? add,
    TResult Function(CacheAssetDetail entry)? update,
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
    required TResult Function(CacheAssetDetailEventGet value) get,
    required TResult Function(CacheAssetDetailEventAdd value) add,
    required TResult Function(CacheAssetDetailEventUpdate value) update,
    required TResult Function(CacheAssetDetailEventDelete value) delete,
  }) {
    return delete(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAssetDetailEventGet value)? get,
    TResult? Function(CacheAssetDetailEventAdd value)? add,
    TResult? Function(CacheAssetDetailEventUpdate value)? update,
    TResult? Function(CacheAssetDetailEventDelete value)? delete,
  }) {
    return delete?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAssetDetailEventGet value)? get,
    TResult Function(CacheAssetDetailEventAdd value)? add,
    TResult Function(CacheAssetDetailEventUpdate value)? update,
    TResult Function(CacheAssetDetailEventDelete value)? delete,
    required TResult orElse(),
  }) {
    if (delete != null) {
      return delete(this);
    }
    return orElse();
  }
}

abstract class CacheAssetDetailEventDelete implements CacheAssetDetailEvent {
  const factory CacheAssetDetailEventDelete(final int id) =
      _$CacheAssetDetailEventDeleteImpl;

  int get id;
  @JsonKey(ignore: true)
  _$$CacheAssetDetailEventDeleteImplCopyWith<_$CacheAssetDetailEventDeleteImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheAssetDetailState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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
abstract class $CacheAssetDetailStateCopyWith<$Res> {
  factory $CacheAssetDetailStateCopyWith(CacheAssetDetailState value,
          $Res Function(CacheAssetDetailState) then) =
      _$CacheAssetDetailStateCopyWithImpl<$Res, CacheAssetDetailState>;
}

/// @nodoc
class _$CacheAssetDetailStateCopyWithImpl<$Res,
        $Val extends CacheAssetDetailState>
    implements $CacheAssetDetailStateCopyWith<$Res> {
  _$CacheAssetDetailStateCopyWithImpl(this._value, this._then);

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
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'CacheAssetDetailState.initial()';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Initial implements CacheAssetDetailState {
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
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'CacheAssetDetailState.loading()';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Loading implements CacheAssetDetailState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$LoadedImplCopyWith<$Res> {
  factory _$$LoadedImplCopyWith(
          _$LoadedImpl value, $Res Function(_$LoadedImpl) then) =
      __$$LoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CacheAssetDetail> entries});
}

/// @nodoc
class __$$LoadedImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$LoadedImpl>
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
              as List<CacheAssetDetail>,
    ));
  }
}

/// @nodoc

class _$LoadedImpl implements _Loaded {
  const _$LoadedImpl(final List<CacheAssetDetail> entries) : _entries = entries;

  final List<CacheAssetDetail> _entries;
  @override
  List<CacheAssetDetail> get entries {
    if (_entries is EqualUnmodifiableListView) return _entries;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entries);
  }

  @override
  String toString() {
    return 'CacheAssetDetailState.loaded(entries: $entries)';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Loaded implements CacheAssetDetailState {
  const factory _Loaded(final List<CacheAssetDetail> entries) = _$LoadedImpl;

  List<CacheAssetDetail> get entries;
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
  $Res call({CacheAssetDetail entry});
}

/// @nodoc
class __$$AddedImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$AddedImpl>
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
              as CacheAssetDetail,
    ));
  }
}

/// @nodoc

class _$AddedImpl implements _Added {
  const _$AddedImpl(this.entry);

  @override
  final CacheAssetDetail entry;

  @override
  String toString() {
    return 'CacheAssetDetailState.added(entry: $entry)';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Added implements CacheAssetDetailState {
  const factory _Added(final CacheAssetDetail entry) = _$AddedImpl;

  CacheAssetDetail get entry;
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
  $Res call({CacheAssetDetail entry});
}

/// @nodoc
class __$$UpdatedImplCopyWithImpl<$Res>
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$UpdatedImpl>
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
              as CacheAssetDetail,
    ));
  }
}

/// @nodoc

class _$UpdatedImpl implements _Updated {
  const _$UpdatedImpl(this.entry);

  @override
  final CacheAssetDetail entry;

  @override
  String toString() {
    return 'CacheAssetDetailState.updated(entry: $entry)';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Updated implements CacheAssetDetailState {
  const factory _Updated(final CacheAssetDetail entry) = _$UpdatedImpl;

  CacheAssetDetail get entry;
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
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$DeletedImpl>
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
    return 'CacheAssetDetailState.deleted()';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Deleted implements CacheAssetDetailState {
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
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$NotFoundImpl>
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
    return 'CacheAssetDetailState.notFound()';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _NotFound implements CacheAssetDetailState {
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
    extends _$CacheAssetDetailStateCopyWithImpl<$Res, _$ErrorImpl>
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
    return 'CacheAssetDetailState.error(message: $message)';
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
    required TResult Function(List<CacheAssetDetail> entries) loaded,
    required TResult Function(CacheAssetDetail entry) added,
    required TResult Function(CacheAssetDetail entry) updated,
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
    TResult? Function(List<CacheAssetDetail> entries)? loaded,
    TResult? Function(CacheAssetDetail entry)? added,
    TResult? Function(CacheAssetDetail entry)? updated,
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
    TResult Function(List<CacheAssetDetail> entries)? loaded,
    TResult Function(CacheAssetDetail entry)? added,
    TResult Function(CacheAssetDetail entry)? updated,
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

abstract class _Error implements CacheAssetDetailState {
  const factory _Error(final String message) = _$ErrorImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
