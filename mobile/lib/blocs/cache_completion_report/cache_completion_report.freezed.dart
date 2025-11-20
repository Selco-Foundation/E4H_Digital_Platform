// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'cache_completion_report.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$CacheCompletionReportEvent {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheCompletionReportEventCopyWith<$Res> {
  factory $CacheCompletionReportEventCopyWith(CacheCompletionReportEvent value,
          $Res Function(CacheCompletionReportEvent) then) =
      _$CacheCompletionReportEventCopyWithImpl<$Res,
          CacheCompletionReportEvent>;
}

/// @nodoc
class _$CacheCompletionReportEventCopyWithImpl<$Res,
        $Val extends CacheCompletionReportEvent>
    implements $CacheCompletionReportEventCopyWith<$Res> {
  _$CacheCompletionReportEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;
}

/// @nodoc
abstract class _$$LoadImplCopyWith<$Res> {
  factory _$$LoadImplCopyWith(
          _$LoadImpl value, $Res Function(_$LoadImpl) then) =
      __$$LoadImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId});
}

/// @nodoc
class __$$LoadImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res, _$LoadImpl>
    implements _$$LoadImplCopyWith<$Res> {
  __$$LoadImplCopyWithImpl(_$LoadImpl _value, $Res Function(_$LoadImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
  }) {
    return _then(_$LoadImpl(
      null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$LoadImpl implements _Load {
  const _$LoadImpl(this.projectId);

  @override
  final String projectId;

  @override
  String toString() {
    return 'CacheCompletionReportEvent.load(projectId: $projectId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$LoadImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$LoadImplCopyWith<_$LoadImpl> get copyWith =>
      __$$LoadImplCopyWithImpl<_$LoadImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return load(projectId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return load?.call(projectId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (load != null) {
      return load(projectId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return load(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return load?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (load != null) {
      return load(this);
    }
    return orElse();
  }
}

abstract class _Load implements CacheCompletionReportEvent {
  const factory _Load(final String projectId) = _$LoadImpl;

  String get projectId;
  @JsonKey(ignore: true)
  _$$LoadImplCopyWith<_$LoadImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$AddOrUpdateImplCopyWith<$Res> {
  factory _$$AddOrUpdateImplCopyWith(
          _$AddOrUpdateImpl value, $Res Function(_$AddOrUpdateImpl) then) =
      __$$AddOrUpdateImplCopyWithImpl<$Res>;
  @useResult
  $Res call(
      {String projectId,
      String filePath,
      String fileType,
      String fileName,
      String latitude,
      String longitude,
      int? index});
}

/// @nodoc
class __$$AddOrUpdateImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res, _$AddOrUpdateImpl>
    implements _$$AddOrUpdateImplCopyWith<$Res> {
  __$$AddOrUpdateImplCopyWithImpl(
      _$AddOrUpdateImpl _value, $Res Function(_$AddOrUpdateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? filePath = null,
    Object? fileType = null,
    Object? fileName = null,
    Object? latitude = null,
    Object? longitude = null,
    Object? index = freezed,
  }) {
    return _then(_$AddOrUpdateImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      filePath: null == filePath
          ? _value.filePath
          : filePath // ignore: cast_nullable_to_non_nullable
              as String,
      fileType: null == fileType
          ? _value.fileType
          : fileType // ignore: cast_nullable_to_non_nullable
              as String,
      fileName: null == fileName
          ? _value.fileName
          : fileName // ignore: cast_nullable_to_non_nullable
              as String,
      latitude: null == latitude
          ? _value.latitude
          : latitude // ignore: cast_nullable_to_non_nullable
              as String,
      longitude: null == longitude
          ? _value.longitude
          : longitude // ignore: cast_nullable_to_non_nullable
              as String,
      index: freezed == index
          ? _value.index
          : index // ignore: cast_nullable_to_non_nullable
              as int?,
    ));
  }
}

/// @nodoc

class _$AddOrUpdateImpl implements _AddOrUpdate {
  const _$AddOrUpdateImpl(
      {required this.projectId,
      required this.filePath,
      required this.fileType,
      required this.fileName,
      required this.latitude,
      required this.longitude,
      this.index});

  @override
  final String projectId;
  @override
  final String filePath;
  @override
  final String fileType;
  @override
  final String fileName;
  @override
  final String latitude;
  @override
  final String longitude;
  @override
  final int? index;

  @override
  String toString() {
    return 'CacheCompletionReportEvent.addOrUpdate(projectId: $projectId, filePath: $filePath, fileType: $fileType, fileName: $fileName, latitude: $latitude, longitude: $longitude, index: $index)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AddOrUpdateImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.filePath, filePath) ||
                other.filePath == filePath) &&
            (identical(other.fileType, fileType) ||
                other.fileType == fileType) &&
            (identical(other.fileName, fileName) ||
                other.fileName == fileName) &&
            (identical(other.latitude, latitude) ||
                other.latitude == latitude) &&
            (identical(other.longitude, longitude) ||
                other.longitude == longitude) &&
            (identical(other.index, index) || other.index == index));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, filePath, fileType,
      fileName, latitude, longitude, index);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AddOrUpdateImplCopyWith<_$AddOrUpdateImpl> get copyWith =>
      __$$AddOrUpdateImplCopyWithImpl<_$AddOrUpdateImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return addOrUpdate(
        projectId, filePath, fileType, fileName, latitude, longitude, index);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return addOrUpdate?.call(
        projectId, filePath, fileType, fileName, latitude, longitude, index);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (addOrUpdate != null) {
      return addOrUpdate(
          projectId, filePath, fileType, fileName, latitude, longitude, index);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return addOrUpdate(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return addOrUpdate?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (addOrUpdate != null) {
      return addOrUpdate(this);
    }
    return orElse();
  }
}

abstract class _AddOrUpdate implements CacheCompletionReportEvent {
  const factory _AddOrUpdate(
      {required final String projectId,
      required final String filePath,
      required final String fileType,
      required final String fileName,
      required final String latitude,
      required final String longitude,
      final int? index}) = _$AddOrUpdateImpl;

  String get projectId;
  String get filePath;
  String get fileType;
  String get fileName;
  String get latitude;
  String get longitude;
  int? get index;
  @JsonKey(ignore: true)
  _$$AddOrUpdateImplCopyWith<_$AddOrUpdateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$AddManyImplCopyWith<$Res> {
  factory _$$AddManyImplCopyWith(
          _$AddManyImpl value, $Res Function(_$AddManyImpl) then) =
      __$$AddManyImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<CompletionFileInput> files});
}

/// @nodoc
class __$$AddManyImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res, _$AddManyImpl>
    implements _$$AddManyImplCopyWith<$Res> {
  __$$AddManyImplCopyWithImpl(
      _$AddManyImpl _value, $Res Function(_$AddManyImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? files = null,
  }) {
    return _then(_$AddManyImpl(
      files: null == files
          ? _value._files
          : files // ignore: cast_nullable_to_non_nullable
              as List<CompletionFileInput>,
    ));
  }
}

/// @nodoc

class _$AddManyImpl implements _AddMany {
  const _$AddManyImpl({required final List<CompletionFileInput> files})
      : _files = files;

  final List<CompletionFileInput> _files;
  @override
  List<CompletionFileInput> get files {
    if (_files is EqualUnmodifiableListView) return _files;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_files);
  }

  @override
  String toString() {
    return 'CacheCompletionReportEvent.addMany(files: $files)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AddManyImpl &&
            const DeepCollectionEquality().equals(other._files, _files));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, const DeepCollectionEquality().hash(_files));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AddManyImplCopyWith<_$AddManyImpl> get copyWith =>
      __$$AddManyImplCopyWithImpl<_$AddManyImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return addMany(files);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return addMany?.call(files);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (addMany != null) {
      return addMany(files);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return addMany(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return addMany?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (addMany != null) {
      return addMany(this);
    }
    return orElse();
  }
}

abstract class _AddMany implements CacheCompletionReportEvent {
  const factory _AddMany({required final List<CompletionFileInput> files}) =
      _$AddManyImpl;

  List<CompletionFileInput> get files;
  @JsonKey(ignore: true)
  _$$AddManyImplCopyWith<_$AddManyImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$RemoveByIdImplCopyWith<$Res> {
  factory _$$RemoveByIdImplCopyWith(
          _$RemoveByIdImpl value, $Res Function(_$RemoveByIdImpl) then) =
      __$$RemoveByIdImplCopyWithImpl<$Res>;
  @useResult
  $Res call({int id});
}

/// @nodoc
class __$$RemoveByIdImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res, _$RemoveByIdImpl>
    implements _$$RemoveByIdImplCopyWith<$Res> {
  __$$RemoveByIdImplCopyWithImpl(
      _$RemoveByIdImpl _value, $Res Function(_$RemoveByIdImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
  }) {
    return _then(_$RemoveByIdImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$RemoveByIdImpl implements _RemoveById {
  const _$RemoveByIdImpl({required this.id});

  @override
  final int id;

  @override
  String toString() {
    return 'CacheCompletionReportEvent.removeById(id: $id)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RemoveByIdImpl &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$RemoveByIdImplCopyWith<_$RemoveByIdImpl> get copyWith =>
      __$$RemoveByIdImplCopyWithImpl<_$RemoveByIdImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return removeById(id);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return removeById?.call(id);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (removeById != null) {
      return removeById(id);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return removeById(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return removeById?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (removeById != null) {
      return removeById(this);
    }
    return orElse();
  }
}

abstract class _RemoveById implements CacheCompletionReportEvent {
  const factory _RemoveById({required final int id}) = _$RemoveByIdImpl;

  int get id;
  @JsonKey(ignore: true)
  _$$RemoveByIdImplCopyWith<_$RemoveByIdImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$RemoveByPathImplCopyWith<$Res> {
  factory _$$RemoveByPathImplCopyWith(
          _$RemoveByPathImpl value, $Res Function(_$RemoveByPathImpl) then) =
      __$$RemoveByPathImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId, String filePath});
}

/// @nodoc
class __$$RemoveByPathImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res, _$RemoveByPathImpl>
    implements _$$RemoveByPathImplCopyWith<$Res> {
  __$$RemoveByPathImplCopyWithImpl(
      _$RemoveByPathImpl _value, $Res Function(_$RemoveByPathImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? filePath = null,
  }) {
    return _then(_$RemoveByPathImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      filePath: null == filePath
          ? _value.filePath
          : filePath // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$RemoveByPathImpl implements _RemoveByPath {
  const _$RemoveByPathImpl({required this.projectId, required this.filePath});

  @override
  final String projectId;
  @override
  final String filePath;

  @override
  String toString() {
    return 'CacheCompletionReportEvent.removeByPath(projectId: $projectId, filePath: $filePath)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RemoveByPathImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.filePath, filePath) ||
                other.filePath == filePath));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, filePath);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$RemoveByPathImplCopyWith<_$RemoveByPathImpl> get copyWith =>
      __$$RemoveByPathImplCopyWithImpl<_$RemoveByPathImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return removeByPath(projectId, filePath);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return removeByPath?.call(projectId, filePath);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (removeByPath != null) {
      return removeByPath(projectId, filePath);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return removeByPath(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return removeByPath?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (removeByPath != null) {
      return removeByPath(this);
    }
    return orElse();
  }
}

abstract class _RemoveByPath implements CacheCompletionReportEvent {
  const factory _RemoveByPath(
      {required final String projectId,
      required final String filePath}) = _$RemoveByPathImpl;

  String get projectId;
  String get filePath;
  @JsonKey(ignore: true)
  _$$RemoveByPathImplCopyWith<_$RemoveByPathImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DeleteManyByEntryIdImplCopyWith<$Res> {
  factory _$$DeleteManyByEntryIdImplCopyWith(_$DeleteManyByEntryIdImpl value,
          $Res Function(_$DeleteManyByEntryIdImpl) then) =
      __$$DeleteManyByEntryIdImplCopyWithImpl<$Res>;
  @useResult
  $Res call({List<String> entryIds});
}

/// @nodoc
class __$$DeleteManyByEntryIdImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res,
        _$DeleteManyByEntryIdImpl>
    implements _$$DeleteManyByEntryIdImplCopyWith<$Res> {
  __$$DeleteManyByEntryIdImplCopyWithImpl(_$DeleteManyByEntryIdImpl _value,
      $Res Function(_$DeleteManyByEntryIdImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? entryIds = null,
  }) {
    return _then(_$DeleteManyByEntryIdImpl(
      entryIds: null == entryIds
          ? _value._entryIds
          : entryIds // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc

class _$DeleteManyByEntryIdImpl implements _DeleteManyByEntryId {
  const _$DeleteManyByEntryIdImpl({required final List<String> entryIds})
      : _entryIds = entryIds;

  final List<String> _entryIds;
  @override
  List<String> get entryIds {
    if (_entryIds is EqualUnmodifiableListView) return _entryIds;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_entryIds);
  }

  @override
  String toString() {
    return 'CacheCompletionReportEvent.deleteManyByEntryId(entryIds: $entryIds)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DeleteManyByEntryIdImpl &&
            const DeepCollectionEquality().equals(other._entryIds, _entryIds));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, const DeepCollectionEquality().hash(_entryIds));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DeleteManyByEntryIdImplCopyWith<_$DeleteManyByEntryIdImpl> get copyWith =>
      __$$DeleteManyByEntryIdImplCopyWithImpl<_$DeleteManyByEntryIdImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return deleteManyByEntryId(entryIds);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return deleteManyByEntryId?.call(entryIds);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (deleteManyByEntryId != null) {
      return deleteManyByEntryId(entryIds);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return deleteManyByEntryId(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return deleteManyByEntryId?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (deleteManyByEntryId != null) {
      return deleteManyByEntryId(this);
    }
    return orElse();
  }
}

abstract class _DeleteManyByEntryId implements CacheCompletionReportEvent {
  const factory _DeleteManyByEntryId({required final List<String> entryIds}) =
      _$DeleteManyByEntryIdImpl;

  List<String> get entryIds;
  @JsonKey(ignore: true)
  _$$DeleteManyByEntryIdImplCopyWith<_$DeleteManyByEntryIdImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ClearProjectImplCopyWith<$Res> {
  factory _$$ClearProjectImplCopyWith(
          _$ClearProjectImpl value, $Res Function(_$ClearProjectImpl) then) =
      __$$ClearProjectImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId});
}

/// @nodoc
class __$$ClearProjectImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res, _$ClearProjectImpl>
    implements _$$ClearProjectImplCopyWith<$Res> {
  __$$ClearProjectImplCopyWithImpl(
      _$ClearProjectImpl _value, $Res Function(_$ClearProjectImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
  }) {
    return _then(_$ClearProjectImpl(
      null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ClearProjectImpl implements _ClearProject {
  const _$ClearProjectImpl(this.projectId);

  @override
  final String projectId;

  @override
  String toString() {
    return 'CacheCompletionReportEvent.clearProject(projectId: $projectId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ClearProjectImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ClearProjectImplCopyWith<_$ClearProjectImpl> get copyWith =>
      __$$ClearProjectImplCopyWithImpl<_$ClearProjectImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return clearProject(projectId);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return clearProject?.call(projectId);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (clearProject != null) {
      return clearProject(projectId);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return clearProject(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return clearProject?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (clearProject != null) {
      return clearProject(this);
    }
    return orElse();
  }
}

abstract class _ClearProject implements CacheCompletionReportEvent {
  const factory _ClearProject(final String projectId) = _$ClearProjectImpl;

  String get projectId;
  @JsonKey(ignore: true)
  _$$ClearProjectImplCopyWith<_$ClearProjectImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ReplaceAllForProjectImplCopyWith<$Res> {
  factory _$$ReplaceAllForProjectImplCopyWith(_$ReplaceAllForProjectImpl value,
          $Res Function(_$ReplaceAllForProjectImpl) then) =
      __$$ReplaceAllForProjectImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId, List<CompletionFileInput> files});
}

/// @nodoc
class __$$ReplaceAllForProjectImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportEventCopyWithImpl<$Res,
        _$ReplaceAllForProjectImpl>
    implements _$$ReplaceAllForProjectImplCopyWith<$Res> {
  __$$ReplaceAllForProjectImplCopyWithImpl(_$ReplaceAllForProjectImpl _value,
      $Res Function(_$ReplaceAllForProjectImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? files = null,
  }) {
    return _then(_$ReplaceAllForProjectImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      files: null == files
          ? _value._files
          : files // ignore: cast_nullable_to_non_nullable
              as List<CompletionFileInput>,
    ));
  }
}

/// @nodoc

class _$ReplaceAllForProjectImpl implements _ReplaceAllForProject {
  const _$ReplaceAllForProjectImpl(
      {required this.projectId, required final List<CompletionFileInput> files})
      : _files = files;

  @override
  final String projectId;
  final List<CompletionFileInput> _files;
  @override
  List<CompletionFileInput> get files {
    if (_files is EqualUnmodifiableListView) return _files;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_files);
  }

  @override
  String toString() {
    return 'CacheCompletionReportEvent.replaceAllForProject(projectId: $projectId, files: $files)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ReplaceAllForProjectImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            const DeepCollectionEquality().equals(other._files, _files));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, projectId, const DeepCollectionEquality().hash(_files));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ReplaceAllForProjectImplCopyWith<_$ReplaceAllForProjectImpl>
      get copyWith =>
          __$$ReplaceAllForProjectImplCopyWithImpl<_$ReplaceAllForProjectImpl>(
              this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId) load,
    required TResult Function(
            String projectId,
            String filePath,
            String fileType,
            String fileName,
            String latitude,
            String longitude,
            int? index)
        addOrUpdate,
    required TResult Function(List<CompletionFileInput> files) addMany,
    required TResult Function(int id) removeById,
    required TResult Function(String projectId, String filePath) removeByPath,
    required TResult Function(List<String> entryIds) deleteManyByEntryId,
    required TResult Function(String projectId) clearProject,
    required TResult Function(String projectId, List<CompletionFileInput> files)
        replaceAllForProject,
  }) {
    return replaceAllForProject(projectId, files);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId)? load,
    TResult? Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult? Function(List<CompletionFileInput> files)? addMany,
    TResult? Function(int id)? removeById,
    TResult? Function(String projectId, String filePath)? removeByPath,
    TResult? Function(List<String> entryIds)? deleteManyByEntryId,
    TResult? Function(String projectId)? clearProject,
    TResult? Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
  }) {
    return replaceAllForProject?.call(projectId, files);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId)? load,
    TResult Function(String projectId, String filePath, String fileType,
            String fileName, String latitude, String longitude, int? index)?
        addOrUpdate,
    TResult Function(List<CompletionFileInput> files)? addMany,
    TResult Function(int id)? removeById,
    TResult Function(String projectId, String filePath)? removeByPath,
    TResult Function(List<String> entryIds)? deleteManyByEntryId,
    TResult Function(String projectId)? clearProject,
    TResult Function(String projectId, List<CompletionFileInput> files)?
        replaceAllForProject,
    required TResult orElse(),
  }) {
    if (replaceAllForProject != null) {
      return replaceAllForProject(projectId, files);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Load value) load,
    required TResult Function(_AddOrUpdate value) addOrUpdate,
    required TResult Function(_AddMany value) addMany,
    required TResult Function(_RemoveById value) removeById,
    required TResult Function(_RemoveByPath value) removeByPath,
    required TResult Function(_DeleteManyByEntryId value) deleteManyByEntryId,
    required TResult Function(_ClearProject value) clearProject,
    required TResult Function(_ReplaceAllForProject value) replaceAllForProject,
  }) {
    return replaceAllForProject(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Load value)? load,
    TResult? Function(_AddOrUpdate value)? addOrUpdate,
    TResult? Function(_AddMany value)? addMany,
    TResult? Function(_RemoveById value)? removeById,
    TResult? Function(_RemoveByPath value)? removeByPath,
    TResult? Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult? Function(_ClearProject value)? clearProject,
    TResult? Function(_ReplaceAllForProject value)? replaceAllForProject,
  }) {
    return replaceAllForProject?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Load value)? load,
    TResult Function(_AddOrUpdate value)? addOrUpdate,
    TResult Function(_AddMany value)? addMany,
    TResult Function(_RemoveById value)? removeById,
    TResult Function(_RemoveByPath value)? removeByPath,
    TResult Function(_DeleteManyByEntryId value)? deleteManyByEntryId,
    TResult Function(_ClearProject value)? clearProject,
    TResult Function(_ReplaceAllForProject value)? replaceAllForProject,
    required TResult orElse(),
  }) {
    if (replaceAllForProject != null) {
      return replaceAllForProject(this);
    }
    return orElse();
  }
}

abstract class _ReplaceAllForProject implements CacheCompletionReportEvent {
  const factory _ReplaceAllForProject(
          {required final String projectId,
          required final List<CompletionFileInput> files}) =
      _$ReplaceAllForProjectImpl;

  String get projectId;
  List<CompletionFileInput> get files;
  @JsonKey(ignore: true)
  _$$ReplaceAllForProjectImplCopyWith<_$ReplaceAllForProjectImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheCompletionReportState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(
            String projectId, List<CacheCompletionReport> files)
        loaded,
    required TResult Function(String message) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult? Function(String message)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Failure value) failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Failure value)? failure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheCompletionReportStateCopyWith<$Res> {
  factory $CacheCompletionReportStateCopyWith(CacheCompletionReportState value,
          $Res Function(CacheCompletionReportState) then) =
      _$CacheCompletionReportStateCopyWithImpl<$Res,
          CacheCompletionReportState>;
}

/// @nodoc
class _$CacheCompletionReportStateCopyWithImpl<$Res,
        $Val extends CacheCompletionReportState>
    implements $CacheCompletionReportStateCopyWith<$Res> {
  _$CacheCompletionReportStateCopyWithImpl(this._value, this._then);

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
    extends _$CacheCompletionReportStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'CacheCompletionReportState.initial()';
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
    required TResult Function(
            String projectId, List<CacheCompletionReport> files)
        loaded,
    required TResult Function(String message) failure,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult? Function(String message)? failure,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult Function(String message)? failure,
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
    required TResult Function(_Failure value) failure,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Failure value)? failure,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements CacheCompletionReportState {
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
    extends _$CacheCompletionReportStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'CacheCompletionReportState.loading()';
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
    required TResult Function(
            String projectId, List<CacheCompletionReport> files)
        loaded,
    required TResult Function(String message) failure,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult? Function(String message)? failure,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult Function(String message)? failure,
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
    required TResult Function(_Failure value) failure,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Failure value)? failure,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _Loading implements CacheCompletionReportState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$LoadedImplCopyWith<$Res> {
  factory _$$LoadedImplCopyWith(
          _$LoadedImpl value, $Res Function(_$LoadedImpl) then) =
      __$$LoadedImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String projectId, List<CacheCompletionReport> files});
}

/// @nodoc
class __$$LoadedImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportStateCopyWithImpl<$Res, _$LoadedImpl>
    implements _$$LoadedImplCopyWith<$Res> {
  __$$LoadedImplCopyWithImpl(
      _$LoadedImpl _value, $Res Function(_$LoadedImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? files = null,
  }) {
    return _then(_$LoadedImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      files: null == files
          ? _value._files
          : files // ignore: cast_nullable_to_non_nullable
              as List<CacheCompletionReport>,
    ));
  }
}

/// @nodoc

class _$LoadedImpl implements _Loaded {
  const _$LoadedImpl(
      {required this.projectId,
      required final List<CacheCompletionReport> files})
      : _files = files;

  @override
  final String projectId;
  final List<CacheCompletionReport> _files;
  @override
  List<CacheCompletionReport> get files {
    if (_files is EqualUnmodifiableListView) return _files;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_files);
  }

  @override
  String toString() {
    return 'CacheCompletionReportState.loaded(projectId: $projectId, files: $files)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$LoadedImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            const DeepCollectionEquality().equals(other._files, _files));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, projectId, const DeepCollectionEquality().hash(_files));

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
    required TResult Function(
            String projectId, List<CacheCompletionReport> files)
        loaded,
    required TResult Function(String message) failure,
  }) {
    return loaded(projectId, files);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult? Function(String message)? failure,
  }) {
    return loaded?.call(projectId, files);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (loaded != null) {
      return loaded(projectId, files);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Failure value) failure,
  }) {
    return loaded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Failure value)? failure,
  }) {
    return loaded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (loaded != null) {
      return loaded(this);
    }
    return orElse();
  }
}

abstract class _Loaded implements CacheCompletionReportState {
  const factory _Loaded(
      {required final String projectId,
      required final List<CacheCompletionReport> files}) = _$LoadedImpl;

  String get projectId;
  List<CacheCompletionReport> get files;
  @JsonKey(ignore: true)
  _$$LoadedImplCopyWith<_$LoadedImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FailureImplCopyWith<$Res> {
  factory _$$FailureImplCopyWith(
          _$FailureImpl value, $Res Function(_$FailureImpl) then) =
      __$$FailureImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String message});
}

/// @nodoc
class __$$FailureImplCopyWithImpl<$Res>
    extends _$CacheCompletionReportStateCopyWithImpl<$Res, _$FailureImpl>
    implements _$$FailureImplCopyWith<$Res> {
  __$$FailureImplCopyWithImpl(
      _$FailureImpl _value, $Res Function(_$FailureImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? message = null,
  }) {
    return _then(_$FailureImpl(
      null == message
          ? _value.message
          : message // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$FailureImpl implements _Failure {
  const _$FailureImpl(this.message);

  @override
  final String message;

  @override
  String toString() {
    return 'CacheCompletionReportState.failure(message: $message)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FailureImpl &&
            (identical(other.message, message) || other.message == message));
  }

  @override
  int get hashCode => Object.hash(runtimeType, message);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      __$$FailureImplCopyWithImpl<_$FailureImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(
            String projectId, List<CacheCompletionReport> files)
        loaded,
    required TResult Function(String message) failure,
  }) {
    return failure(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult? Function(String message)? failure,
  }) {
    return failure?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(String projectId, List<CacheCompletionReport> files)?
        loaded,
    TResult Function(String message)? failure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(message);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Loaded value) loaded,
    required TResult Function(_Failure value) failure,
  }) {
    return failure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Loaded value)? loaded,
    TResult? Function(_Failure value)? failure,
  }) {
    return failure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Loaded value)? loaded,
    TResult Function(_Failure value)? failure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements CacheCompletionReportState {
  const factory _Failure(final String message) = _$FailureImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
