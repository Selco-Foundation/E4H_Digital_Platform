// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'cache_amc_media_upload.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$CacheAmcMediaUploadEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) get,
    required TResult Function(CacheAmcMediaUpload entry) add,
    required TResult Function(CacheAmcMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String scheduledVisitId, String userType)
        deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? get,
    TResult? Function(CacheAmcMediaUpload entry)? add,
    TResult? Function(CacheAmcMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String scheduledVisitId, String userType)? deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? get,
    TResult Function(CacheAmcMediaUpload entry)? add,
    TResult Function(CacheAmcMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String scheduledVisitId, String userType)? deleteAll,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAmcMediaUploadEventGet value) get,
    required TResult Function(CacheAmcMediaUploadEventAdd value) add,
    required TResult Function(CacheAmcMediaUploadEventUpdate value) update,
    required TResult Function(CacheAmcMediaUploadEventDelete value) delete,
    required TResult Function(CacheAmcMediaUploadEventDeleteAll value)
        deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAmcMediaUploadEventGet value)? get,
    TResult? Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult? Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult? Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult? Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAmcMediaUploadEventGet value)? get,
    TResult Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheAmcMediaUploadEventCopyWith<$Res> {
  factory $CacheAmcMediaUploadEventCopyWith(CacheAmcMediaUploadEvent value,
          $Res Function(CacheAmcMediaUploadEvent) then) =
      _$CacheAmcMediaUploadEventCopyWithImpl<$Res, CacheAmcMediaUploadEvent>;
}

/// @nodoc
class _$CacheAmcMediaUploadEventCopyWithImpl<$Res,
        $Val extends CacheAmcMediaUploadEvent>
    implements $CacheAmcMediaUploadEventCopyWith<$Res> {
  _$CacheAmcMediaUploadEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$CacheAmcMediaUploadEventGetImplCopyWith<$Res> {
  factory _$$CacheAmcMediaUploadEventGetImplCopyWith(
          _$CacheAmcMediaUploadEventGetImpl value,
          $Res Function(_$CacheAmcMediaUploadEventGetImpl) then) =
      __$$CacheAmcMediaUploadEventGetImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String scheduledVisitId, String userType});
}

/// @nodoc
class __$$CacheAmcMediaUploadEventGetImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadEventCopyWithImpl<$Res,
        _$CacheAmcMediaUploadEventGetImpl>
    implements _$$CacheAmcMediaUploadEventGetImplCopyWith<$Res> {
  __$$CacheAmcMediaUploadEventGetImplCopyWithImpl(
      _$CacheAmcMediaUploadEventGetImpl _value,
      $Res Function(_$CacheAmcMediaUploadEventGetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisitId = null,
    Object? userType = null,
  }) {
    return _then(_$CacheAmcMediaUploadEventGetImpl(
      null == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String,
      null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$CacheAmcMediaUploadEventGetImpl implements CacheAmcMediaUploadEventGet {
  const _$CacheAmcMediaUploadEventGetImpl(this.scheduledVisitId, this.userType);

  @override
  final String scheduledVisitId;
  @override
  final String userType;

  @override
  String toString() {
    return 'CacheAmcMediaUploadEvent.get(scheduledVisitId: $scheduledVisitId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAmcMediaUploadEventGetImpl &&
            (identical(other.scheduledVisitId, scheduledVisitId) ||
                other.scheduledVisitId == scheduledVisitId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisitId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAmcMediaUploadEventGetImplCopyWith<_$CacheAmcMediaUploadEventGetImpl>
      get copyWith => __$$CacheAmcMediaUploadEventGetImplCopyWithImpl<
          _$CacheAmcMediaUploadEventGetImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) get,
    required TResult Function(CacheAmcMediaUpload entry) add,
    required TResult Function(CacheAmcMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String scheduledVisitId, String userType)
        deleteAll,
  }) {
    return get(scheduledVisitId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? get,
    TResult? Function(CacheAmcMediaUpload entry)? add,
    TResult? Function(CacheAmcMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String scheduledVisitId, String userType)? deleteAll,
  }) {
    return get?.call(scheduledVisitId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? get,
    TResult Function(CacheAmcMediaUpload entry)? add,
    TResult Function(CacheAmcMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String scheduledVisitId, String userType)? deleteAll,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(scheduledVisitId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAmcMediaUploadEventGet value) get,
    required TResult Function(CacheAmcMediaUploadEventAdd value) add,
    required TResult Function(CacheAmcMediaUploadEventUpdate value) update,
    required TResult Function(CacheAmcMediaUploadEventDelete value) delete,
    required TResult Function(CacheAmcMediaUploadEventDeleteAll value)
        deleteAll,
  }) {
    return get(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAmcMediaUploadEventGet value)? get,
    TResult? Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult? Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult? Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult? Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return get?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAmcMediaUploadEventGet value)? get,
    TResult Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (get != null) {
      return get(this);
    }
    return orElse();
  }
}

abstract class CacheAmcMediaUploadEventGet implements CacheAmcMediaUploadEvent {
  const factory CacheAmcMediaUploadEventGet(
          final String scheduledVisitId, final String userType) =
      _$CacheAmcMediaUploadEventGetImpl;

  String get scheduledVisitId;
  String get userType;
  @JsonKey(ignore: true)
  _$$CacheAmcMediaUploadEventGetImplCopyWith<_$CacheAmcMediaUploadEventGetImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAmcMediaUploadEventAddImplCopyWith<$Res> {
  factory _$$CacheAmcMediaUploadEventAddImplCopyWith(
          _$CacheAmcMediaUploadEventAddImpl value,
          $Res Function(_$CacheAmcMediaUploadEventAddImpl) then) =
      __$$CacheAmcMediaUploadEventAddImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAmcMediaUpload entry});
}

/// @nodoc
class __$$CacheAmcMediaUploadEventAddImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadEventCopyWithImpl<$Res,
        _$CacheAmcMediaUploadEventAddImpl>
    implements _$$CacheAmcMediaUploadEventAddImplCopyWith<$Res> {
  __$$CacheAmcMediaUploadEventAddImplCopyWithImpl(
      _$CacheAmcMediaUploadEventAddImpl _value,
      $Res Function(_$CacheAmcMediaUploadEventAddImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheAmcMediaUploadEventAddImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAmcMediaUpload,
    ));
  }
}

/// @nodoc

class _$CacheAmcMediaUploadEventAddImpl implements CacheAmcMediaUploadEventAdd {
  const _$CacheAmcMediaUploadEventAddImpl(this.entry);

  @override
  final CacheAmcMediaUpload entry;

  @override
  String toString() {
    return 'CacheAmcMediaUploadEvent.add(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAmcMediaUploadEventAddImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAmcMediaUploadEventAddImplCopyWith<_$CacheAmcMediaUploadEventAddImpl>
      get copyWith => __$$CacheAmcMediaUploadEventAddImplCopyWithImpl<
          _$CacheAmcMediaUploadEventAddImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) get,
    required TResult Function(CacheAmcMediaUpload entry) add,
    required TResult Function(CacheAmcMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String scheduledVisitId, String userType)
        deleteAll,
  }) {
    return add(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? get,
    TResult? Function(CacheAmcMediaUpload entry)? add,
    TResult? Function(CacheAmcMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String scheduledVisitId, String userType)? deleteAll,
  }) {
    return add?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? get,
    TResult Function(CacheAmcMediaUpload entry)? add,
    TResult Function(CacheAmcMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String scheduledVisitId, String userType)? deleteAll,
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
    required TResult Function(CacheAmcMediaUploadEventGet value) get,
    required TResult Function(CacheAmcMediaUploadEventAdd value) add,
    required TResult Function(CacheAmcMediaUploadEventUpdate value) update,
    required TResult Function(CacheAmcMediaUploadEventDelete value) delete,
    required TResult Function(CacheAmcMediaUploadEventDeleteAll value)
        deleteAll,
  }) {
    return add(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAmcMediaUploadEventGet value)? get,
    TResult? Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult? Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult? Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult? Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return add?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAmcMediaUploadEventGet value)? get,
    TResult Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (add != null) {
      return add(this);
    }
    return orElse();
  }
}

abstract class CacheAmcMediaUploadEventAdd implements CacheAmcMediaUploadEvent {
  const factory CacheAmcMediaUploadEventAdd(final CacheAmcMediaUpload entry) =
      _$CacheAmcMediaUploadEventAddImpl;

  CacheAmcMediaUpload get entry;
  @JsonKey(ignore: true)
  _$$CacheAmcMediaUploadEventAddImplCopyWith<_$CacheAmcMediaUploadEventAddImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAmcMediaUploadEventUpdateImplCopyWith<$Res> {
  factory _$$CacheAmcMediaUploadEventUpdateImplCopyWith(
          _$CacheAmcMediaUploadEventUpdateImpl value,
          $Res Function(_$CacheAmcMediaUploadEventUpdateImpl) then) =
      __$$CacheAmcMediaUploadEventUpdateImplCopyWithImpl<$Res>;
  @useResult
  $Res call({CacheAmcMediaUpload entry});
}

/// @nodoc
class __$$CacheAmcMediaUploadEventUpdateImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadEventCopyWithImpl<$Res,
        _$CacheAmcMediaUploadEventUpdateImpl>
    implements _$$CacheAmcMediaUploadEventUpdateImplCopyWith<$Res> {
  __$$CacheAmcMediaUploadEventUpdateImplCopyWithImpl(
      _$CacheAmcMediaUploadEventUpdateImpl _value,
      $Res Function(_$CacheAmcMediaUploadEventUpdateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entry = null,
  }) {
    return _then(_$CacheAmcMediaUploadEventUpdateImpl(
      null == entry
          ? _value.entry
          : entry // ignore: cast_nullable_to_non_nullable
              as CacheAmcMediaUpload,
    ));
  }
}

/// @nodoc

class _$CacheAmcMediaUploadEventUpdateImpl
    implements CacheAmcMediaUploadEventUpdate {
  const _$CacheAmcMediaUploadEventUpdateImpl(this.entry);

  @override
  final CacheAmcMediaUpload entry;

  @override
  String toString() {
    return 'CacheAmcMediaUploadEvent.update(entry: $entry)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAmcMediaUploadEventUpdateImpl &&
            (identical(other.entry, entry) || other.entry == entry));
  }

  @override
  int get hashCode => Object.hash(runtimeType, entry);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAmcMediaUploadEventUpdateImplCopyWith<
          _$CacheAmcMediaUploadEventUpdateImpl>
      get copyWith => __$$CacheAmcMediaUploadEventUpdateImplCopyWithImpl<
          _$CacheAmcMediaUploadEventUpdateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) get,
    required TResult Function(CacheAmcMediaUpload entry) add,
    required TResult Function(CacheAmcMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String scheduledVisitId, String userType)
        deleteAll,
  }) {
    return update(entry);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? get,
    TResult? Function(CacheAmcMediaUpload entry)? add,
    TResult? Function(CacheAmcMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String scheduledVisitId, String userType)? deleteAll,
  }) {
    return update?.call(entry);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? get,
    TResult Function(CacheAmcMediaUpload entry)? add,
    TResult Function(CacheAmcMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String scheduledVisitId, String userType)? deleteAll,
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
    required TResult Function(CacheAmcMediaUploadEventGet value) get,
    required TResult Function(CacheAmcMediaUploadEventAdd value) add,
    required TResult Function(CacheAmcMediaUploadEventUpdate value) update,
    required TResult Function(CacheAmcMediaUploadEventDelete value) delete,
    required TResult Function(CacheAmcMediaUploadEventDeleteAll value)
        deleteAll,
  }) {
    return update(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAmcMediaUploadEventGet value)? get,
    TResult? Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult? Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult? Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult? Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return update?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAmcMediaUploadEventGet value)? get,
    TResult Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (update != null) {
      return update(this);
    }
    return orElse();
  }
}

abstract class CacheAmcMediaUploadEventUpdate
    implements CacheAmcMediaUploadEvent {
  const factory CacheAmcMediaUploadEventUpdate(
      final CacheAmcMediaUpload entry) = _$CacheAmcMediaUploadEventUpdateImpl;

  CacheAmcMediaUpload get entry;
  @JsonKey(ignore: true)
  _$$CacheAmcMediaUploadEventUpdateImplCopyWith<
          _$CacheAmcMediaUploadEventUpdateImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAmcMediaUploadEventDeleteImplCopyWith<$Res> {
  factory _$$CacheAmcMediaUploadEventDeleteImplCopyWith(
          _$CacheAmcMediaUploadEventDeleteImpl value,
          $Res Function(_$CacheAmcMediaUploadEventDeleteImpl) then) =
      __$$CacheAmcMediaUploadEventDeleteImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int id});
}

/// @nodoc
class __$$CacheAmcMediaUploadEventDeleteImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadEventCopyWithImpl<$Res,
        _$CacheAmcMediaUploadEventDeleteImpl>
    implements _$$CacheAmcMediaUploadEventDeleteImplCopyWith<$Res> {
  __$$CacheAmcMediaUploadEventDeleteImplCopyWithImpl(
      _$CacheAmcMediaUploadEventDeleteImpl _value,
      $Res Function(_$CacheAmcMediaUploadEventDeleteImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
  }) {
    return _then(_$CacheAmcMediaUploadEventDeleteImpl(
      null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$CacheAmcMediaUploadEventDeleteImpl
    implements CacheAmcMediaUploadEventDelete {
  const _$CacheAmcMediaUploadEventDeleteImpl(this.id);

  @override
  final int id;

  @override
  String toString() {
    return 'CacheAmcMediaUploadEvent.delete(id: $id)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAmcMediaUploadEventDeleteImpl &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAmcMediaUploadEventDeleteImplCopyWith<
          _$CacheAmcMediaUploadEventDeleteImpl>
      get copyWith => __$$CacheAmcMediaUploadEventDeleteImplCopyWithImpl<
          _$CacheAmcMediaUploadEventDeleteImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) get,
    required TResult Function(CacheAmcMediaUpload entry) add,
    required TResult Function(CacheAmcMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String scheduledVisitId, String userType)
        deleteAll,
  }) {
    return delete(id);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? get,
    TResult? Function(CacheAmcMediaUpload entry)? add,
    TResult? Function(CacheAmcMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String scheduledVisitId, String userType)? deleteAll,
  }) {
    return delete?.call(id);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? get,
    TResult Function(CacheAmcMediaUpload entry)? add,
    TResult Function(CacheAmcMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String scheduledVisitId, String userType)? deleteAll,
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
    required TResult Function(CacheAmcMediaUploadEventGet value) get,
    required TResult Function(CacheAmcMediaUploadEventAdd value) add,
    required TResult Function(CacheAmcMediaUploadEventUpdate value) update,
    required TResult Function(CacheAmcMediaUploadEventDelete value) delete,
    required TResult Function(CacheAmcMediaUploadEventDeleteAll value)
        deleteAll,
  }) {
    return delete(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAmcMediaUploadEventGet value)? get,
    TResult? Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult? Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult? Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult? Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return delete?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAmcMediaUploadEventGet value)? get,
    TResult Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (delete != null) {
      return delete(this);
    }
    return orElse();
  }
}

abstract class CacheAmcMediaUploadEventDelete
    implements CacheAmcMediaUploadEvent {
  const factory CacheAmcMediaUploadEventDelete(final int id) =
      _$CacheAmcMediaUploadEventDeleteImpl;

  int get id;
  @JsonKey(ignore: true)
  _$$CacheAmcMediaUploadEventDeleteImplCopyWith<
          _$CacheAmcMediaUploadEventDeleteImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$CacheAmcMediaUploadEventDeleteAllImplCopyWith<$Res> {
  factory _$$CacheAmcMediaUploadEventDeleteAllImplCopyWith(
          _$CacheAmcMediaUploadEventDeleteAllImpl value,
          $Res Function(_$CacheAmcMediaUploadEventDeleteAllImpl) then) =
      __$$CacheAmcMediaUploadEventDeleteAllImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String scheduledVisitId, String userType});
}

/// @nodoc
class __$$CacheAmcMediaUploadEventDeleteAllImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadEventCopyWithImpl<$Res,
        _$CacheAmcMediaUploadEventDeleteAllImpl>
    implements _$$CacheAmcMediaUploadEventDeleteAllImplCopyWith<$Res> {
  __$$CacheAmcMediaUploadEventDeleteAllImplCopyWithImpl(
      _$CacheAmcMediaUploadEventDeleteAllImpl _value,
      $Res Function(_$CacheAmcMediaUploadEventDeleteAllImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? scheduledVisitId = null,
    Object? userType = null,
  }) {
    return _then(_$CacheAmcMediaUploadEventDeleteAllImpl(
      null == scheduledVisitId
          ? _value.scheduledVisitId
          : scheduledVisitId // ignore: cast_nullable_to_non_nullable
              as String,
      null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$CacheAmcMediaUploadEventDeleteAllImpl
    implements CacheAmcMediaUploadEventDeleteAll {
  const _$CacheAmcMediaUploadEventDeleteAllImpl(
      this.scheduledVisitId, this.userType);

  @override
  final String scheduledVisitId;
  @override
  final String userType;

  @override
  String toString() {
    return 'CacheAmcMediaUploadEvent.deleteAll(scheduledVisitId: $scheduledVisitId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheAmcMediaUploadEventDeleteAllImpl &&
            (identical(other.scheduledVisitId, scheduledVisitId) ||
                other.scheduledVisitId == scheduledVisitId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, scheduledVisitId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheAmcMediaUploadEventDeleteAllImplCopyWith<
          _$CacheAmcMediaUploadEventDeleteAllImpl>
      get copyWith => __$$CacheAmcMediaUploadEventDeleteAllImplCopyWithImpl<
          _$CacheAmcMediaUploadEventDeleteAllImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String scheduledVisitId, String userType) get,
    required TResult Function(CacheAmcMediaUpload entry) add,
    required TResult Function(CacheAmcMediaUpload entry) update,
    required TResult Function(int id) delete,
    required TResult Function(String scheduledVisitId, String userType)
        deleteAll,
  }) {
    return deleteAll(scheduledVisitId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String scheduledVisitId, String userType)? get,
    TResult? Function(CacheAmcMediaUpload entry)? add,
    TResult? Function(CacheAmcMediaUpload entry)? update,
    TResult? Function(int id)? delete,
    TResult? Function(String scheduledVisitId, String userType)? deleteAll,
  }) {
    return deleteAll?.call(scheduledVisitId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String scheduledVisitId, String userType)? get,
    TResult Function(CacheAmcMediaUpload entry)? add,
    TResult Function(CacheAmcMediaUpload entry)? update,
    TResult Function(int id)? delete,
    TResult Function(String scheduledVisitId, String userType)? deleteAll,
    required TResult orElse(),
  }) {
    if (deleteAll != null) {
      return deleteAll(scheduledVisitId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(CacheAmcMediaUploadEventGet value) get,
    required TResult Function(CacheAmcMediaUploadEventAdd value) add,
    required TResult Function(CacheAmcMediaUploadEventUpdate value) update,
    required TResult Function(CacheAmcMediaUploadEventDelete value) delete,
    required TResult Function(CacheAmcMediaUploadEventDeleteAll value)
        deleteAll,
  }) {
    return deleteAll(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(CacheAmcMediaUploadEventGet value)? get,
    TResult? Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult? Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult? Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult? Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
  }) {
    return deleteAll?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(CacheAmcMediaUploadEventGet value)? get,
    TResult Function(CacheAmcMediaUploadEventAdd value)? add,
    TResult Function(CacheAmcMediaUploadEventUpdate value)? update,
    TResult Function(CacheAmcMediaUploadEventDelete value)? delete,
    TResult Function(CacheAmcMediaUploadEventDeleteAll value)? deleteAll,
    required TResult orElse(),
  }) {
    if (deleteAll != null) {
      return deleteAll(this);
    }
    return orElse();
  }
}

abstract class CacheAmcMediaUploadEventDeleteAll
    implements CacheAmcMediaUploadEvent {
  const factory CacheAmcMediaUploadEventDeleteAll(
          final String scheduledVisitId, final String userType) =
      _$CacheAmcMediaUploadEventDeleteAllImpl;

  String get scheduledVisitId;
  String get userType;
  @JsonKey(ignore: true)
  _$$CacheAmcMediaUploadEventDeleteAllImplCopyWith<
          _$CacheAmcMediaUploadEventDeleteAllImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheAmcMediaUploadState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
    required TResult Function() deleted,
    required TResult Function() notFound,
    required TResult Function(String message) error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
    TResult? Function()? deleted,
    TResult? Function()? notFound,
    TResult? Function(String message)? error,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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
abstract class $CacheAmcMediaUploadStateCopyWith<$Res> {
  factory $CacheAmcMediaUploadStateCopyWith(CacheAmcMediaUploadState value,
          $Res Function(CacheAmcMediaUploadState) then) =
      _$CacheAmcMediaUploadStateCopyWithImpl<$Res, CacheAmcMediaUploadState>;
}

/// @nodoc
class _$CacheAmcMediaUploadStateCopyWithImpl<$Res,
        $Val extends CacheAmcMediaUploadState>
    implements $CacheAmcMediaUploadStateCopyWith<$Res> {
  _$CacheAmcMediaUploadStateCopyWithImpl(this._value, this._then);

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
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'CacheAmcMediaUploadState.initial()';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Initial implements CacheAmcMediaUploadState {
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
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'CacheAmcMediaUploadState.loading()';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Loading implements CacheAmcMediaUploadState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$LoadedImplCopyWith<$Res> {
  factory _$$LoadedImplCopyWith(
          _$LoadedImpl value, $Res Function(_$LoadedImpl) then) =
      __$$LoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CacheAmcMediaUpload> entries});
}

/// @nodoc
class __$$LoadedImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$LoadedImpl>
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
              as List<CacheAmcMediaUpload>,
    ));
  }
}

/// @nodoc

class _$LoadedImpl implements _Loaded {
  const _$LoadedImpl(final List<CacheAmcMediaUpload> entries)
      : _entries = entries;

  final List<CacheAmcMediaUpload> _entries;
  @override
  List<CacheAmcMediaUpload> get entries {
    if (_entries is EqualUnmodifiableListView) return _entries;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entries);
  }

  @override
  String toString() {
    return 'CacheAmcMediaUploadState.loaded(entries: $entries)';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Loaded implements CacheAmcMediaUploadState {
  const factory _Loaded(final List<CacheAmcMediaUpload> entries) = _$LoadedImpl;

  List<CacheAmcMediaUpload> get entries;
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
  $Res call({CacheAmcMediaUpload entry});
}

/// @nodoc
class __$$AddedImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$AddedImpl>
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
              as CacheAmcMediaUpload,
    ));
  }
}

/// @nodoc

class _$AddedImpl implements _Added {
  const _$AddedImpl(this.entry);

  @override
  final CacheAmcMediaUpload entry;

  @override
  String toString() {
    return 'CacheAmcMediaUploadState.added(entry: $entry)';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Added implements CacheAmcMediaUploadState {
  const factory _Added(final CacheAmcMediaUpload entry) = _$AddedImpl;

  CacheAmcMediaUpload get entry;
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
  $Res call({CacheAmcMediaUpload entry});
}

/// @nodoc
class __$$UpdatedImplCopyWithImpl<$Res>
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$UpdatedImpl>
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
              as CacheAmcMediaUpload,
    ));
  }
}

/// @nodoc

class _$UpdatedImpl implements _Updated {
  const _$UpdatedImpl(this.entry);

  @override
  final CacheAmcMediaUpload entry;

  @override
  String toString() {
    return 'CacheAmcMediaUploadState.updated(entry: $entry)';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Updated implements CacheAmcMediaUploadState {
  const factory _Updated(final CacheAmcMediaUpload entry) = _$UpdatedImpl;

  CacheAmcMediaUpload get entry;
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
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$DeletedImpl>
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
    return 'CacheAmcMediaUploadState.deleted()';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Deleted implements CacheAmcMediaUploadState {
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
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$NotFoundImpl>
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
    return 'CacheAmcMediaUploadState.notFound()';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _NotFound implements CacheAmcMediaUploadState {
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
    extends _$CacheAmcMediaUploadStateCopyWithImpl<$Res, _$ErrorImpl>
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
    return 'CacheAmcMediaUploadState.error(message: $message)';
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
    required TResult Function(List<CacheAmcMediaUpload> entries) loaded,
    required TResult Function(CacheAmcMediaUpload entry) added,
    required TResult Function(CacheAmcMediaUpload entry) updated,
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
    TResult? Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult? Function(CacheAmcMediaUpload entry)? added,
    TResult? Function(CacheAmcMediaUpload entry)? updated,
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
    TResult Function(List<CacheAmcMediaUpload> entries)? loaded,
    TResult Function(CacheAmcMediaUpload entry)? added,
    TResult Function(CacheAmcMediaUpload entry)? updated,
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

abstract class _Error implements CacheAmcMediaUploadState {
  const factory _Error(final String message) = _$ErrorImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$ErrorImplCopyWith<_$ErrorImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
