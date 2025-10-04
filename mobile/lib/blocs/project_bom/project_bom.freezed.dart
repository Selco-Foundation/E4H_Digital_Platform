// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'project_bom.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$ProjectBomEvent {
  String get projectId => throw _privateConstructorUsedError;
  String get userType => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) syncIfNeeded,
    required TResult Function(String projectId, String userType) forceSync,
    required TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)
        downloadWorkflowDocument,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SyncIfNeeded value) syncIfNeeded,
    required TResult Function(_ForceSync value) forceSync,
    required TResult Function(_DownloadWorkflowDocument value)
        downloadWorkflowDocument,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadWorkflowDocument value)?
        downloadWorkflowDocument,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadWorkflowDocument value)? downloadWorkflowDocument,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ProjectBomEventCopyWith<ProjectBomEvent> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProjectBomEventCopyWith<$Res> {
  factory $ProjectBomEventCopyWith(
          ProjectBomEvent value, $Res Function(ProjectBomEvent) then) =
      _$ProjectBomEventCopyWithImpl<$Res, ProjectBomEvent>;
  @useResult
  $Res call({String projectId, String userType});
}

/// @nodoc
class _$ProjectBomEventCopyWithImpl<$Res, $Val extends ProjectBomEvent>
    implements $ProjectBomEventCopyWith<$Res> {
  _$ProjectBomEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? userType = null,
  }) {
    return _then(_value.copyWith(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SyncIfNeededImplCopyWith<$Res>
    implements $ProjectBomEventCopyWith<$Res> {
  factory _$$SyncIfNeededImplCopyWith(
          _$SyncIfNeededImpl value, $Res Function(_$SyncIfNeededImpl) then) =
      __$$SyncIfNeededImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId, String userType});
}

/// @nodoc
class __$$SyncIfNeededImplCopyWithImpl<$Res>
    extends _$ProjectBomEventCopyWithImpl<$Res, _$SyncIfNeededImpl>
    implements _$$SyncIfNeededImplCopyWith<$Res> {
  __$$SyncIfNeededImplCopyWithImpl(
      _$SyncIfNeededImpl _value, $Res Function(_$SyncIfNeededImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? userType = null,
  }) {
    return _then(_$SyncIfNeededImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SyncIfNeededImpl implements _SyncIfNeeded {
  const _$SyncIfNeededImpl({required this.projectId, required this.userType});

  @override
  final String projectId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ProjectBomEvent.syncIfNeeded(projectId: $projectId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SyncIfNeededImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SyncIfNeededImplCopyWith<_$SyncIfNeededImpl> get copyWith =>
      __$$SyncIfNeededImplCopyWithImpl<_$SyncIfNeededImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) syncIfNeeded,
    required TResult Function(String projectId, String userType) forceSync,
    required TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)
        downloadWorkflowDocument,
  }) {
    return syncIfNeeded(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
  }) {
    return syncIfNeeded?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
    required TResult orElse(),
  }) {
    if (syncIfNeeded != null) {
      return syncIfNeeded(projectId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SyncIfNeeded value) syncIfNeeded,
    required TResult Function(_ForceSync value) forceSync,
    required TResult Function(_DownloadWorkflowDocument value)
        downloadWorkflowDocument,
  }) {
    return syncIfNeeded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadWorkflowDocument value)?
        downloadWorkflowDocument,
  }) {
    return syncIfNeeded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadWorkflowDocument value)? downloadWorkflowDocument,
    required TResult orElse(),
  }) {
    if (syncIfNeeded != null) {
      return syncIfNeeded(this);
    }
    return orElse();
  }
}

abstract class _SyncIfNeeded implements ProjectBomEvent {
  const factory _SyncIfNeeded(
      {required final String projectId,
      required final String userType}) = _$SyncIfNeededImpl;

  @override
  String get projectId;
  @override
  String get userType;
  @override
  @JsonKey(ignore: true)
  _$$SyncIfNeededImplCopyWith<_$SyncIfNeededImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$ForceSyncImplCopyWith<$Res>
    implements $ProjectBomEventCopyWith<$Res> {
  factory _$$ForceSyncImplCopyWith(
          _$ForceSyncImpl value, $Res Function(_$ForceSyncImpl) then) =
      __$$ForceSyncImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId, String userType});
}

/// @nodoc
class __$$ForceSyncImplCopyWithImpl<$Res>
    extends _$ProjectBomEventCopyWithImpl<$Res, _$ForceSyncImpl>
    implements _$$ForceSyncImplCopyWith<$Res> {
  __$$ForceSyncImplCopyWithImpl(
      _$ForceSyncImpl _value, $Res Function(_$ForceSyncImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? userType = null,
  }) {
    return _then(_$ForceSyncImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$ForceSyncImpl implements _ForceSync {
  const _$ForceSyncImpl({required this.projectId, required this.userType});

  @override
  final String projectId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ProjectBomEvent.forceSync(projectId: $projectId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ForceSyncImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.userType, userType) ||
                other.userType == userType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, userType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ForceSyncImplCopyWith<_$ForceSyncImpl> get copyWith =>
      __$$ForceSyncImplCopyWithImpl<_$ForceSyncImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) syncIfNeeded,
    required TResult Function(String projectId, String userType) forceSync,
    required TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)
        downloadWorkflowDocument,
  }) {
    return forceSync(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
  }) {
    return forceSync?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
    required TResult orElse(),
  }) {
    if (forceSync != null) {
      return forceSync(projectId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SyncIfNeeded value) syncIfNeeded,
    required TResult Function(_ForceSync value) forceSync,
    required TResult Function(_DownloadWorkflowDocument value)
        downloadWorkflowDocument,
  }) {
    return forceSync(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadWorkflowDocument value)?
        downloadWorkflowDocument,
  }) {
    return forceSync?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadWorkflowDocument value)? downloadWorkflowDocument,
    required TResult orElse(),
  }) {
    if (forceSync != null) {
      return forceSync(this);
    }
    return orElse();
  }
}

abstract class _ForceSync implements ProjectBomEvent {
  const factory _ForceSync(
      {required final String projectId,
      required final String userType}) = _$ForceSyncImpl;

  @override
  String get projectId;
  @override
  String get userType;
  @override
  @JsonKey(ignore: true)
  _$$ForceSyncImplCopyWith<_$ForceSyncImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DownloadWorkflowDocumentImplCopyWith<$Res>
    implements $ProjectBomEventCopyWith<$Res> {
  factory _$$DownloadWorkflowDocumentImplCopyWith(
          _$DownloadWorkflowDocumentImpl value,
          $Res Function(_$DownloadWorkflowDocumentImpl) then) =
      __$$DownloadWorkflowDocumentImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String projectId,
      String userType,
      List<Document> workflowDocuments,
      String docType});
}

/// @nodoc
class __$$DownloadWorkflowDocumentImplCopyWithImpl<$Res>
    extends _$ProjectBomEventCopyWithImpl<$Res, _$DownloadWorkflowDocumentImpl>
    implements _$$DownloadWorkflowDocumentImplCopyWith<$Res> {
  __$$DownloadWorkflowDocumentImplCopyWithImpl(
      _$DownloadWorkflowDocumentImpl _value,
      $Res Function(_$DownloadWorkflowDocumentImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? userType = null,
    Object? workflowDocuments = null,
    Object? docType = null,
  }) {
    return _then(_$DownloadWorkflowDocumentImpl(
      projectId: null == projectId
          ? _value.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String,
      userType: null == userType
          ? _value.userType
          : userType // ignore: cast_nullable_to_non_nullable
              as String,
      workflowDocuments: null == workflowDocuments
          ? _value._workflowDocuments
          : workflowDocuments // ignore: cast_nullable_to_non_nullable
              as List<Document>,
      docType: null == docType
          ? _value.docType
          : docType // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$DownloadWorkflowDocumentImpl implements _DownloadWorkflowDocument {
  const _$DownloadWorkflowDocumentImpl(
      {required this.projectId,
      required this.userType,
      required final List<Document> workflowDocuments,
      required this.docType})
      : _workflowDocuments = workflowDocuments;

  @override
  final String projectId;
  @override
  final String userType;
  final List<Document> _workflowDocuments;
  @override
  List<Document> get workflowDocuments {
    if (_workflowDocuments is EqualUnmodifiableListView)
      return _workflowDocuments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_workflowDocuments);
  }

  @override
  final String docType;

  @override
  String toString() {
    return 'ProjectBomEvent.downloadWorkflowDocument(projectId: $projectId, userType: $userType, workflowDocuments: $workflowDocuments, docType: $docType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DownloadWorkflowDocumentImpl &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.userType, userType) ||
                other.userType == userType) &&
            const DeepCollectionEquality()
                .equals(other._workflowDocuments, _workflowDocuments) &&
            (identical(other.docType, docType) || other.docType == docType));
  }

  @override
  int get hashCode => Object.hash(runtimeType, projectId, userType,
      const DeepCollectionEquality().hash(_workflowDocuments), docType);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DownloadWorkflowDocumentImplCopyWith<_$DownloadWorkflowDocumentImpl>
      get copyWith => __$$DownloadWorkflowDocumentImplCopyWithImpl<
          _$DownloadWorkflowDocumentImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) syncIfNeeded,
    required TResult Function(String projectId, String userType) forceSync,
    required TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)
        downloadWorkflowDocument,
  }) {
    return downloadWorkflowDocument(
        projectId, userType, workflowDocuments, docType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
  }) {
    return downloadWorkflowDocument?.call(
        projectId, userType, workflowDocuments, docType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType,
            List<Document> workflowDocuments, String docType)?
        downloadWorkflowDocument,
    required TResult orElse(),
  }) {
    if (downloadWorkflowDocument != null) {
      return downloadWorkflowDocument(
          projectId, userType, workflowDocuments, docType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SyncIfNeeded value) syncIfNeeded,
    required TResult Function(_ForceSync value) forceSync,
    required TResult Function(_DownloadWorkflowDocument value)
        downloadWorkflowDocument,
  }) {
    return downloadWorkflowDocument(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadWorkflowDocument value)?
        downloadWorkflowDocument,
  }) {
    return downloadWorkflowDocument?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadWorkflowDocument value)? downloadWorkflowDocument,
    required TResult orElse(),
  }) {
    if (downloadWorkflowDocument != null) {
      return downloadWorkflowDocument(this);
    }
    return orElse();
  }
}

abstract class _DownloadWorkflowDocument implements ProjectBomEvent {
  const factory _DownloadWorkflowDocument(
      {required final String projectId,
      required final String userType,
      required final List<Document> workflowDocuments,
      required final String docType}) = _$DownloadWorkflowDocumentImpl;

  @override
  String get projectId;
  @override
  String get userType;
  List<Document> get workflowDocuments;
  String get docType;
  @override
  @JsonKey(ignore: true)
  _$$DownloadWorkflowDocumentImplCopyWith<_$DownloadWorkflowDocumentImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ProjectBomState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProjectBomStateCopyWith<$Res> {
  factory $ProjectBomStateCopyWith(
          ProjectBomState value, $Res Function(ProjectBomState) then) =
      _$ProjectBomStateCopyWithImpl<$Res, ProjectBomState>;
}

/// @nodoc
class _$ProjectBomStateCopyWithImpl<$Res, $Val extends ProjectBomState>
    implements $ProjectBomStateCopyWith<$Res> {
  _$ProjectBomStateCopyWithImpl(this._value, this._then);

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
    extends _$ProjectBomStateCopyWithImpl<$Res, _$InitialImpl>
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
    return 'ProjectBomState.initial()';
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
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
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
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return initial(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return initial?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (initial != null) {
      return initial(this);
    }
    return orElse();
  }
}

abstract class _Initial implements ProjectBomState {
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
    extends _$ProjectBomStateCopyWithImpl<$Res, _$LoadingImpl>
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
    return 'ProjectBomState.loading()';
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
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
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
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return loading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return loading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (loading != null) {
      return loading(this);
    }
    return orElse();
  }
}

abstract class _Loading implements ProjectBomState {
  const factory _Loading() = _$LoadingImpl;
}

/// @nodoc
abstract class _$$SuccessImplCopyWith<$Res> {
  factory _$$SuccessImplCopyWith(
          _$SuccessImpl value, $Res Function(_$SuccessImpl) then) =
      __$$SuccessImplCopyWithImpl<$Res>;
  @useResult
  $Res call({bool savedBomValues});
}

/// @nodoc
class __$$SuccessImplCopyWithImpl<$Res>
    extends _$ProjectBomStateCopyWithImpl<$Res, _$SuccessImpl>
    implements _$$SuccessImplCopyWith<$Res> {
  __$$SuccessImplCopyWithImpl(
      _$SuccessImpl _value, $Res Function(_$SuccessImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? savedBomValues = null,
  }) {
    return _then(_$SuccessImpl(
      savedBomValues: null == savedBomValues
          ? _value.savedBomValues
          : savedBomValues // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$SuccessImpl implements _Success {
  const _$SuccessImpl({required this.savedBomValues});

  @override
  final bool savedBomValues;

  @override
  String toString() {
    return 'ProjectBomState.success(savedBomValues: $savedBomValues)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SuccessImpl &&
            (identical(other.savedBomValues, savedBomValues) ||
                other.savedBomValues == savedBomValues));
  }

  @override
  int get hashCode => Object.hash(runtimeType, savedBomValues);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SuccessImplCopyWith<_$SuccessImpl> get copyWith =>
      __$$SuccessImplCopyWithImpl<_$SuccessImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return success(savedBomValues);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return success?.call(savedBomValues);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(savedBomValues);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return success(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return success?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(this);
    }
    return orElse();
  }
}

abstract class _Success implements ProjectBomState {
  const factory _Success({required final bool savedBomValues}) = _$SuccessImpl;

  bool get savedBomValues;
  @JsonKey(ignore: true)
  _$$SuccessImplCopyWith<_$SuccessImpl> get copyWith =>
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
    extends _$ProjectBomStateCopyWithImpl<$Res, _$FailureImpl>
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
    return 'ProjectBomState.failure(message: $message)';
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
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return failure(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return failure?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
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
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return failure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return failure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (failure != null) {
      return failure(this);
    }
    return orElse();
  }
}

abstract class _Failure implements ProjectBomState {
  const factory _Failure(final String message) = _$FailureImpl;

  String get message;
  @JsonKey(ignore: true)
  _$$FailureImplCopyWith<_$FailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DocDownloadInProgressImplCopyWith<$Res> {
  factory _$$DocDownloadInProgressImplCopyWith(
          _$DocDownloadInProgressImpl value,
          $Res Function(_$DocDownloadInProgressImpl) then) =
      __$$DocDownloadInProgressImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$DocDownloadInProgressImplCopyWithImpl<$Res>
    extends _$ProjectBomStateCopyWithImpl<$Res, _$DocDownloadInProgressImpl>
    implements _$$DocDownloadInProgressImplCopyWith<$Res> {
  __$$DocDownloadInProgressImplCopyWithImpl(_$DocDownloadInProgressImpl _value,
      $Res Function(_$DocDownloadInProgressImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$DocDownloadInProgressImpl implements _DocDownloadInProgress {
  const _$DocDownloadInProgressImpl();

  @override
  String toString() {
    return 'ProjectBomState.documentDownloadInProgress()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DocDownloadInProgressImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return documentDownloadInProgress();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return documentDownloadInProgress?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (documentDownloadInProgress != null) {
      return documentDownloadInProgress();
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return documentDownloadInProgress(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return documentDownloadInProgress?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (documentDownloadInProgress != null) {
      return documentDownloadInProgress(this);
    }
    return orElse();
  }
}

abstract class _DocDownloadInProgress implements ProjectBomState {
  const factory _DocDownloadInProgress() = _$DocDownloadInProgressImpl;
}

/// @nodoc
abstract class _$$DocDownloadSuccessImplCopyWith<$Res> {
  factory _$$DocDownloadSuccessImplCopyWith(_$DocDownloadSuccessImpl value,
          $Res Function(_$DocDownloadSuccessImpl) then) =
      __$$DocDownloadSuccessImplCopyWithImpl<$Res>;
  @useResult
  $Res call({File file});
}

/// @nodoc
class __$$DocDownloadSuccessImplCopyWithImpl<$Res>
    extends _$ProjectBomStateCopyWithImpl<$Res, _$DocDownloadSuccessImpl>
    implements _$$DocDownloadSuccessImplCopyWith<$Res> {
  __$$DocDownloadSuccessImplCopyWithImpl(_$DocDownloadSuccessImpl _value,
      $Res Function(_$DocDownloadSuccessImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? file = null,
  }) {
    return _then(_$DocDownloadSuccessImpl(
      null == file
          ? _value.file
          : file // ignore: cast_nullable_to_non_nullable
              as File,
    ));
  }
}

/// @nodoc

class _$DocDownloadSuccessImpl implements _DocDownloadSuccess {
  const _$DocDownloadSuccessImpl(this.file);

  @override
  final File file;

  @override
  String toString() {
    return 'ProjectBomState.documentDownloadSuccess(file: $file)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DocDownloadSuccessImpl &&
            (identical(other.file, file) || other.file == file));
  }

  @override
  int get hashCode => Object.hash(runtimeType, file);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DocDownloadSuccessImplCopyWith<_$DocDownloadSuccessImpl> get copyWith =>
      __$$DocDownloadSuccessImplCopyWithImpl<_$DocDownloadSuccessImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return documentDownloadSuccess(file);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return documentDownloadSuccess?.call(file);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (documentDownloadSuccess != null) {
      return documentDownloadSuccess(file);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return documentDownloadSuccess(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return documentDownloadSuccess?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (documentDownloadSuccess != null) {
      return documentDownloadSuccess(this);
    }
    return orElse();
  }
}

abstract class _DocDownloadSuccess implements ProjectBomState {
  const factory _DocDownloadSuccess(final File file) = _$DocDownloadSuccessImpl;

  File get file;
  @JsonKey(ignore: true)
  _$$DocDownloadSuccessImplCopyWith<_$DocDownloadSuccessImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$DocDownloadFailureImplCopyWith<$Res> {
  factory _$$DocDownloadFailureImplCopyWith(_$DocDownloadFailureImpl value,
          $Res Function(_$DocDownloadFailureImpl) then) =
      __$$DocDownloadFailureImplCopyWithImpl<$Res>;
  @useResult
  $Res call({String error});
}

/// @nodoc
class __$$DocDownloadFailureImplCopyWithImpl<$Res>
    extends _$ProjectBomStateCopyWithImpl<$Res, _$DocDownloadFailureImpl>
    implements _$$DocDownloadFailureImplCopyWith<$Res> {
  __$$DocDownloadFailureImplCopyWithImpl(_$DocDownloadFailureImpl _value,
      $Res Function(_$DocDownloadFailureImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? error = null,
  }) {
    return _then(_$DocDownloadFailureImpl(
      null == error
          ? _value.error
          : error // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$DocDownloadFailureImpl implements _DocDownloadFailure {
  const _$DocDownloadFailureImpl(this.error);

  @override
  final String error;

  @override
  String toString() {
    return 'ProjectBomState.documentDownloadFailure(error: $error)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DocDownloadFailureImpl &&
            (identical(other.error, error) || other.error == error));
  }

  @override
  int get hashCode => Object.hash(runtimeType, error);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DocDownloadFailureImplCopyWith<_$DocDownloadFailureImpl> get copyWith =>
      __$$DocDownloadFailureImplCopyWithImpl<_$DocDownloadFailureImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() documentDownloadInProgress,
    required TResult Function(File file) documentDownloadSuccess,
    required TResult Function(String error) documentDownloadFailure,
  }) {
    return documentDownloadFailure(error);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? documentDownloadInProgress,
    TResult? Function(File file)? documentDownloadSuccess,
    TResult? Function(String error)? documentDownloadFailure,
  }) {
    return documentDownloadFailure?.call(error);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? documentDownloadInProgress,
    TResult Function(File file)? documentDownloadSuccess,
    TResult Function(String error)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (documentDownloadFailure != null) {
      return documentDownloadFailure(error);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_DocDownloadInProgress value)
        documentDownloadInProgress,
    required TResult Function(_DocDownloadSuccess value)
        documentDownloadSuccess,
    required TResult Function(_DocDownloadFailure value)
        documentDownloadFailure,
  }) {
    return documentDownloadFailure(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult? Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult? Function(_DocDownloadFailure value)? documentDownloadFailure,
  }) {
    return documentDownloadFailure?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_DocDownloadInProgress value)? documentDownloadInProgress,
    TResult Function(_DocDownloadSuccess value)? documentDownloadSuccess,
    TResult Function(_DocDownloadFailure value)? documentDownloadFailure,
    required TResult orElse(),
  }) {
    if (documentDownloadFailure != null) {
      return documentDownloadFailure(this);
    }
    return orElse();
  }
}

abstract class _DocDownloadFailure implements ProjectBomState {
  const factory _DocDownloadFailure(final String error) =
      _$DocDownloadFailureImpl;

  String get error;
  @JsonKey(ignore: true)
  _$$DocDownloadFailureImplCopyWith<_$DocDownloadFailureImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
