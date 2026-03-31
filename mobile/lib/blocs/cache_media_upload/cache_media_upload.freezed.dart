// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'cache_media_upload.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$CacheMediaUploadEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheMediaUpload entry) add,
    required TResult Function(CacheMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String projectId, String assetType) deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheMediaUpload entry)? add,
    TResult? Function(CacheMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String projectId, String assetType)? deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheMediaUpload entry)? add,
    TResult Function(CacheMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String projectId, String assetType)? deleteAll,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheMediaUploadEventGet value) get,
    required TResult Function(CacheMediaUploadEventAdd value) add,
    required TResult Function(CacheMediaUploadEventUpdate value) update,
    required TResult Function(CacheMediaUploadEventDelete value) delete,
    required TResult Function(CacheMediaUploadEventDeleteAll value) deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheMediaUploadEventGet value)? get,
    TResult? Function(CacheMediaUploadEventAdd value)? add,
    TResult? Function(CacheMediaUploadEventUpdate value)? update,
    TResult? Function(CacheMediaUploadEventDelete value)? delete,
    TResult? Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheMediaUploadEventGet value)? get,
    TResult Function(CacheMediaUploadEventAdd value)? add,
    TResult Function(CacheMediaUploadEventUpdate value)? update,
    TResult Function(CacheMediaUploadEventDelete value)? delete,
    TResult Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheMediaUploadEventCopyWith<$Res> {
  factory $CacheMediaUploadEventCopyWith(CacheMediaUploadEvent value,
          $Res Function(CacheMediaUploadEvent) then) =
      _$CacheMediaUploadEventCopyWithImpl<$Res, CacheMediaUploadEvent>;
}

/// @nodoc
class _$CacheMediaUploadEventCopyWithImpl<$Res,
        $Val extends CacheMediaUploadEvent>
    implements $CacheMediaUploadEventCopyWith<$Res> {
  _$CacheMediaUploadEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$CacheMediaUploadEventGetImplCopyWith<$Res> {
  factory _$$CacheMediaUploadEventGetImplCopyWith(
          _$CacheMediaUploadEventGetImpl value,
          $Res Function(_$CacheMediaUploadEventGetImpl) then) =
      __$$CacheMediaUploadEventGetImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId, String assetType});
}

/// @nodoc
class __$$CacheMediaUploadEventGetImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadEventCopyWithImpl<$Res,
        _$CacheMediaUploadEventGetImpl>
    implements _$$CacheMediaUploadEventGetImplCopyWith<$Res> {
  __$$CacheMediaUploadEventGetImplCopyWithImpl(
      _$CacheMediaUploadEventGetImpl _value,
      $Res Function(_$CacheMediaUploadEventGetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? assetType = null,
  }) {
    return _then(_$CacheMediaUploadEventGetImpl(
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

class _$CacheMediaUploadEventGetImpl implements CacheMediaUploadEventGet {
  const _$CacheMediaUploadEventGetImpl(this.projectId, this.assetType);

  @override
  final String projectId;
  @override
  final String assetType;

  @override
  String toString() {
    return 'CacheMediaUploadEvent.get(projectId: $projectId, assetType: $assetType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheMediaUploadEventGetImpl &&
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
  _$$CacheMediaUploadEventGetImplCopyWith<_$CacheMediaUploadEventGetImpl>
      get copyWith => __$$CacheMediaUploadEventGetImplCopyWithImpl<
          _$CacheMediaUploadEventGetImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheMediaUpload entry) add,
    required TResult Function(CacheMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String projectId, String assetType) deleteAll,
  }) {
    return get(projectId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheMediaUpload entry)? add,
    TResult? Function(CacheMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String projectId, String assetType)? deleteAll,
  }) {
    return get?.call(projectId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheMediaUpload entry)? add,
    TResult Function(CacheMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String projectId, String assetType)? deleteAll,
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
    required TResult Function(CacheMediaUploadEventGet value) get,
    required TResult Function(CacheMediaUploadEventAdd value) add,
    required TResult Function(CacheMediaUploadEventUpdate value) update,
    required TResult Function(CacheMediaUploadEventDelete value) delete,
    required TResult Function(CacheMediaUploadEventDeleteAll value) deleteAll,
  }) {
    return get(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheMediaUploadEventGet value)? get,
    TResult? Function(CacheMediaUploadEventAdd value)? add,
    TResult? Function(CacheMediaUploadEventUpdate value)? update,
    TResult? Function(CacheMediaUploadEventDelete value)? delete,
    TResult? Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return get?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheMediaUploadEventGet value)? get,
    TResult Function(CacheMediaUploadEventAdd value)? add,
    TResult Function(CacheMediaUploadEventUpdate value)? update,
    TResult Function(CacheMediaUploadEventDelete value)? delete,
    TResult Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(this);
    }
    return orElse();
  }
}

abstract class CacheMediaUploadEventGet implements CacheMediaUploadEvent {
  const factory CacheMediaUploadEventGet(
          final String projectId, final String assetType) =
      _$CacheMediaUploadEventGetImpl;

  String get projectId;
  String get assetType;
  @JsonKey(ignore: true)
  _$$CacheMediaUploadEventGetImplCopyWith<_$CacheMediaUploadEventGetImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheMediaUploadEventAddImplCopyWith<$Res> {
  factory _$$CacheMediaUploadEventAddImplCopyWith(
          _$CacheMediaUploadEventAddImpl value,
          $Res Function(_$CacheMediaUploadEventAddImpl) then) =
      __$$CacheMediaUploadEventAddImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheMediaUpload entry});
}

/// @nodoc
class __$$CacheMediaUploadEventAddImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadEventCopyWithImpl<$Res,
        _$CacheMediaUploadEventAddImpl>
    implements _$$CacheMediaUploadEventAddImplCopyWith<$Res> {
  __$$CacheMediaUploadEventAddImplCopyWithImpl(
      _$CacheMediaUploadEventAddImpl _value,
      $Res Function(_$CacheMediaUploadEventAddImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheMediaUploadEventAddImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheMediaUpload,
    ));
  }
}

/// @nodoc

class _$CacheMediaUploadEventAddImpl implements CacheMediaUploadEventAdd {
  const _$CacheMediaUploadEventAddImpl(this.entry);

  @override
  final CacheMediaUpload entry;

  @override
  String toString() {
    return 'CacheMediaUploadEvent.add(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheMediaUploadEventAddImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheMediaUploadEventAddImplCopyWith<_$CacheMediaUploadEventAddImpl>
      get copyWith => __$$CacheMediaUploadEventAddImplCopyWithImpl<
          _$CacheMediaUploadEventAddImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheMediaUpload entry) add,
    required TResult Function(CacheMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String projectId, String assetType) deleteAll,
  }) {
    return add(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheMediaUpload entry)? add,
    TResult? Function(CacheMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String projectId, String assetType)? deleteAll,
  }) {
    return add?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheMediaUpload entry)? add,
    TResult Function(CacheMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String projectId, String assetType)? deleteAll,
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
    required TResult Function(CacheMediaUploadEventGet value) get,
    required TResult Function(CacheMediaUploadEventAdd value) add,
    required TResult Function(CacheMediaUploadEventUpdate value) update,
    required TResult Function(CacheMediaUploadEventDelete value) delete,
    required TResult Function(CacheMediaUploadEventDeleteAll value) deleteAll,
  }) {
    return add(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheMediaUploadEventGet value)? get,
    TResult? Function(CacheMediaUploadEventAdd value)? add,
    TResult? Function(CacheMediaUploadEventUpdate value)? update,
    TResult? Function(CacheMediaUploadEventDelete value)? delete,
    TResult? Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return add?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheMediaUploadEventGet value)? get,
    TResult Function(CacheMediaUploadEventAdd value)? add,
    TResult Function(CacheMediaUploadEventUpdate value)? update,
    TResult Function(CacheMediaUploadEventDelete value)? delete,
    TResult Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (add != null) {
      return add(this);
    }
    return orElse();
  }
}

abstract class CacheMediaUploadEventAdd implements CacheMediaUploadEvent {
  const factory CacheMediaUploadEventAdd(final CacheMediaUpload entry) =
      _$CacheMediaUploadEventAddImpl;

  CacheMediaUpload get entry;
  @JsonKey(ignore: true)
  _$$CacheMediaUploadEventAddImplCopyWith<_$CacheMediaUploadEventAddImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheMediaUploadEventUpdateImplCopyWith<$Res> {
  factory _$$CacheMediaUploadEventUpdateImplCopyWith(
          _$CacheMediaUploadEventUpdateImpl value,
          $Res Function(_$CacheMediaUploadEventUpdateImpl) then) =
      __$$CacheMediaUploadEventUpdateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheMediaUpload entry});
}

/// @nodoc
class __$$CacheMediaUploadEventUpdateImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadEventCopyWithImpl<$Res,
        _$CacheMediaUploadEventUpdateImpl>
    implements _$$CacheMediaUploadEventUpdateImplCopyWith<$Res> {
  __$$CacheMediaUploadEventUpdateImplCopyWithImpl(
      _$CacheMediaUploadEventUpdateImpl _value,
      $Res Function(_$CacheMediaUploadEventUpdateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheMediaUploadEventUpdateImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheMediaUpload,
    ));
  }
}

/// @nodoc

class _$CacheMediaUploadEventUpdateImpl implements CacheMediaUploadEventUpdate {
  const _$CacheMediaUploadEventUpdateImpl(this.entry);

  @override
  final CacheMediaUpload entry;

  @override
  String toString() {
    return 'CacheMediaUploadEvent.update(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheMediaUploadEventUpdateImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheMediaUploadEventUpdateImplCopyWith<_$CacheMediaUploadEventUpdateImpl>
      get copyWith => __$$CacheMediaUploadEventUpdateImplCopyWithImpl<
          _$CacheMediaUploadEventUpdateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheMediaUpload entry) add,
    required TResult Function(CacheMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String projectId, String assetType) deleteAll,
  }) {
    return update(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheMediaUpload entry)? add,
    TResult? Function(CacheMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String projectId, String assetType)? deleteAll,
  }) {
    return update?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheMediaUpload entry)? add,
    TResult Function(CacheMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String projectId, String assetType)? deleteAll,
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
    required TResult Function(CacheMediaUploadEventGet value) get,
    required TResult Function(CacheMediaUploadEventAdd value) add,
    required TResult Function(CacheMediaUploadEventUpdate value) update,
    required TResult Function(CacheMediaUploadEventDelete value) delete,
    required TResult Function(CacheMediaUploadEventDeleteAll value) deleteAll,
  }) {
    return update(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheMediaUploadEventGet value)? get,
    TResult? Function(CacheMediaUploadEventAdd value)? add,
    TResult? Function(CacheMediaUploadEventUpdate value)? update,
    TResult? Function(CacheMediaUploadEventDelete value)? delete,
    TResult? Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return update?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheMediaUploadEventGet value)? get,
    TResult Function(CacheMediaUploadEventAdd value)? add,
    TResult Function(CacheMediaUploadEventUpdate value)? update,
    TResult Function(CacheMediaUploadEventDelete value)? delete,
    TResult Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (update != null) {
      return update(this);
    }
    return orElse();
  }
}

abstract class CacheMediaUploadEventUpdate implements CacheMediaUploadEvent {
  const factory CacheMediaUploadEventUpdate(final CacheMediaUpload entry) =
      _$CacheMediaUploadEventUpdateImpl;

  CacheMediaUpload get entry;
  @JsonKey(ignore: true)
  _$$CacheMediaUploadEventUpdateImplCopyWith<_$CacheMediaUploadEventUpdateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheMediaUploadEventDeleteImplCopyWith<$Res> {
  factory _$$CacheMediaUploadEventDeleteImplCopyWith(
          _$CacheMediaUploadEventDeleteImpl value,
          $Res Function(_$CacheMediaUploadEventDeleteImpl) then) =
      __$$CacheMediaUploadEventDeleteImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int id});
}

/// @nodoc
class __$$CacheMediaUploadEventDeleteImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadEventCopyWithImpl<$Res,
        _$CacheMediaUploadEventDeleteImpl>
    implements _$$CacheMediaUploadEventDeleteImplCopyWith<$Res> {
  __$$CacheMediaUploadEventDeleteImplCopyWithImpl(
      _$CacheMediaUploadEventDeleteImpl _value,
      $Res Function(_$CacheMediaUploadEventDeleteImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
  }) {
    return _then(_$CacheMediaUploadEventDeleteImpl(
      null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$CacheMediaUploadEventDeleteImpl implements CacheMediaUploadEventDelete {
  const _$CacheMediaUploadEventDeleteImpl(this.id);

  @override
  final int id;

  @override
  String toString() {
    return 'CacheMediaUploadEvent.delete(id: $id)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheMediaUploadEventDeleteImpl &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheMediaUploadEventDeleteImplCopyWith<_$CacheMediaUploadEventDeleteImpl>
      get copyWith => __$$CacheMediaUploadEventDeleteImplCopyWithImpl<
          _$CacheMediaUploadEventDeleteImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheMediaUpload entry) add,
    required TResult Function(CacheMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String projectId, String assetType) deleteAll,
  }) {
    return delete(id);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheMediaUpload entry)? add,
    TResult? Function(CacheMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String projectId, String assetType)? deleteAll,
  }) {
    return delete?.call(id);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheMediaUpload entry)? add,
    TResult Function(CacheMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String projectId, String assetType)? deleteAll,
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
    required TResult Function(CacheMediaUploadEventGet value) get,
    required TResult Function(CacheMediaUploadEventAdd value) add,
    required TResult Function(CacheMediaUploadEventUpdate value) update,
    required TResult Function(CacheMediaUploadEventDelete value) delete,
    required TResult Function(CacheMediaUploadEventDeleteAll value) deleteAll,
  }) {
    return delete(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheMediaUploadEventGet value)? get,
    TResult? Function(CacheMediaUploadEventAdd value)? add,
    TResult? Function(CacheMediaUploadEventUpdate value)? update,
    TResult? Function(CacheMediaUploadEventDelete value)? delete,
    TResult? Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return delete?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheMediaUploadEventGet value)? get,
    TResult Function(CacheMediaUploadEventAdd value)? add,
    TResult Function(CacheMediaUploadEventUpdate value)? update,
    TResult Function(CacheMediaUploadEventDelete value)? delete,
    TResult Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (delete != null) {
      return delete(this);
    }
    return orElse();
  }
}

abstract class CacheMediaUploadEventDelete implements CacheMediaUploadEvent {
  const factory CacheMediaUploadEventDelete(final int id) =
      _$CacheMediaUploadEventDeleteImpl;

  int get id;
  @JsonKey(ignore: true)
  _$$CacheMediaUploadEventDeleteImplCopyWith<_$CacheMediaUploadEventDeleteImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheMediaUploadEventDeleteAllImplCopyWith<$Res> {
  factory _$$CacheMediaUploadEventDeleteAllImplCopyWith(
          _$CacheMediaUploadEventDeleteAllImpl value,
          $Res Function(_$CacheMediaUploadEventDeleteAllImpl) then) =
      __$$CacheMediaUploadEventDeleteAllImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId, String assetType});
}

/// @nodoc
class __$$CacheMediaUploadEventDeleteAllImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadEventCopyWithImpl<$Res,
        _$CacheMediaUploadEventDeleteAllImpl>
    implements _$$CacheMediaUploadEventDeleteAllImplCopyWith<$Res> {
  __$$CacheMediaUploadEventDeleteAllImplCopyWithImpl(
      _$CacheMediaUploadEventDeleteAllImpl _value,
      $Res Function(_$CacheMediaUploadEventDeleteAllImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? assetType = null,
  }) {
    return _then(_$CacheMediaUploadEventDeleteAllImpl(
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

class _$CacheMediaUploadEventDeleteAllImpl
    implements CacheMediaUploadEventDeleteAll {
  const _$CacheMediaUploadEventDeleteAllImpl(this.projectId, this.assetType);

  @override
  final String projectId;
  @override
  final String assetType;

  @override
  String toString() {
    return 'CacheMediaUploadEvent.deleteAll(projectId: $projectId, assetType: $assetType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheMediaUploadEventDeleteAllImpl &&
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
  _$$CacheMediaUploadEventDeleteAllImplCopyWith<
          _$CacheMediaUploadEventDeleteAllImpl>
      get copyWith => __$$CacheMediaUploadEventDeleteAllImplCopyWithImpl<
          _$CacheMediaUploadEventDeleteAllImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String assetType) get,
    required TResult Function(CacheMediaUpload entry) add,
    required TResult Function(CacheMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String projectId, String assetType) deleteAll,
  }) {
    return deleteAll(projectId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String assetType)? get,
    TResult? Function(CacheMediaUpload entry)? add,
    TResult? Function(CacheMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String projectId, String assetType)? deleteAll,
  }) {
    return deleteAll?.call(projectId, assetType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String assetType)? get,
    TResult Function(CacheMediaUpload entry)? add,
    TResult Function(CacheMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String projectId, String assetType)? deleteAll,
    required TResult orElse(),
  }) {
    if (deleteAll != null) {
      return deleteAll(projectId, assetType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheMediaUploadEventGet value) get,
    required TResult Function(CacheMediaUploadEventAdd value) add,
    required TResult Function(CacheMediaUploadEventUpdate value) update,
    required TResult Function(CacheMediaUploadEventDelete value) delete,
    required TResult Function(CacheMediaUploadEventDeleteAll value) deleteAll,
  }) {
    return deleteAll(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheMediaUploadEventGet value)? get,
    TResult? Function(CacheMediaUploadEventAdd value)? add,
    TResult? Function(CacheMediaUploadEventUpdate value)? update,
    TResult? Function(CacheMediaUploadEventDelete value)? delete,
    TResult? Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return deleteAll?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheMediaUploadEventGet value)? get,
    TResult Function(CacheMediaUploadEventAdd value)? add,
    TResult Function(CacheMediaUploadEventUpdate value)? update,
    TResult Function(CacheMediaUploadEventDelete value)? delete,
    TResult Function(CacheMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (deleteAll != null) {
      return deleteAll(this);
    }
    return orElse();
  }
}

abstract class CacheMediaUploadEventDeleteAll implements CacheMediaUploadEvent {
  const factory CacheMediaUploadEventDeleteAll(
          final String projectId, final String assetType) =
      _$CacheMediaUploadEventDeleteAllImpl;

  String get projectId;
  String get assetType;
  @JsonKey(ignore: true)
  _$$CacheMediaUploadEventDeleteAllImplCopyWith<
          _$CacheMediaUploadEventDeleteAllImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheMediaUploadState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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
abstract class $CacheMediaUploadStateCopyWith<$Res> {
  factory $CacheMediaUploadStateCopyWith(CacheMediaUploadState value,
          $Res Function(CacheMediaUploadState) then) =
      _$CacheMediaUploadStateCopyWithImpl<$Res, CacheMediaUploadState>;
}

/// @nodoc
class _$CacheMediaUploadStateCopyWithImpl<$Res,
        $Val extends CacheMediaUploadState>
    implements $CacheMediaUploadStateCopyWith<$Res> {
  _$CacheMediaUploadStateCopyWithImpl(this._value, this._then);

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
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'CacheMediaUploadState.initial()';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Initial implements CacheMediaUploadState {
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
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'CacheMediaUploadState.loading()';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Loading implements CacheMediaUploadState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$LoadedImplCopyWith<$Res> {
  factory _$$LoadedImplCopyWith(
          _$LoadedImpl value, $Res Function(_$LoadedImpl) then) =
      __$$LoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CacheMediaUpload> entries});
}

/// @nodoc
class __$$LoadedImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$LoadedImpl>
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
              as List<CacheMediaUpload>,
    ));
  }
}

/// @nodoc

class _$LoadedImpl implements _Loaded {
  const _$LoadedImpl(final List<CacheMediaUpload> entries) : _entries = entries;

  final List<CacheMediaUpload> _entries;
  @override
  List<CacheMediaUpload> get entries {
    if (_entries is EqualUnmodifiableListView) return _entries;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entries);
  }

  @override
  String toString() {
    return 'CacheMediaUploadState.loaded(entries: $entries)';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Loaded implements CacheMediaUploadState {
  const factory _Loaded(final List<CacheMediaUpload> entries) = _$LoadedImpl;

  List<CacheMediaUpload> get entries;
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
  $Res call({CacheMediaUpload entry});
}

/// @nodoc
class __$$AddedImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$AddedImpl>
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
              as CacheMediaUpload,
    ));
  }
}

/// @nodoc

class _$AddedImpl implements _Added {
  const _$AddedImpl(this.entry);

  @override
  final CacheMediaUpload entry;

  @override
  String toString() {
    return 'CacheMediaUploadState.added(entry: $entry)';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Added implements CacheMediaUploadState {
  const factory _Added(final CacheMediaUpload entry) = _$AddedImpl;

  CacheMediaUpload get entry;
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
  $Res call({CacheMediaUpload entry});
}

/// @nodoc
class __$$UpdatedImplCopyWithImpl<$Res>
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$UpdatedImpl>
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
              as CacheMediaUpload,
    ));
  }
}

/// @nodoc

class _$UpdatedImpl implements _Updated {
  const _$UpdatedImpl(this.entry);

  @override
  final CacheMediaUpload entry;

  @override
  String toString() {
    return 'CacheMediaUploadState.updated(entry: $entry)';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Updated implements CacheMediaUploadState {
  const factory _Updated(final CacheMediaUpload entry) = _$UpdatedImpl;

  CacheMediaUpload get entry;
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
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$DeletedImpl>
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
    return 'CacheMediaUploadState.deleted()';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Deleted implements CacheMediaUploadState {
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
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$NotFoundImpl>
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
    return 'CacheMediaUploadState.notFound()';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _NotFound implements CacheMediaUploadState {
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
    extends _$CacheMediaUploadStateCopyWithImpl<$Res, _$ErrorImpl>
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
    return 'CacheMediaUploadState.error(message: $message)';
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
    required TResult Function(List<CacheMediaUpload> entries) loaded,
    required TResult Function(CacheMediaUpload entry) added,
    required TResult Function(CacheMediaUpload entry) updated,
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
    TResult? Function(List<CacheMediaUpload> entries)? loaded,
    TResult? Function(CacheMediaUpload entry)? added,
    TResult? Function(CacheMediaUpload entry)? updated,
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
    TResult Function(List<CacheMediaUpload> entries)? loaded,
    TResult Function(CacheMediaUpload entry)? added,
    TResult Function(CacheMediaUpload entry)? updated,
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

abstract class _Error implements CacheMediaUploadState {
  const factory _Error(final String message) = _$ErrorImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
