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
    required TResult Function(String projectId, String userType) downloadPdf,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType)? downloadPdf,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType)? downloadPdf,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SyncIfNeeded value) syncIfNeeded,
    required TResult Function(_ForceSync value) forceSync,
    required TResult Function(_DownloadPdf value) downloadPdf,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadPdf value)? downloadPdf,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadPdf value)? downloadPdf,
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
    required TResult Function(String projectId, String userType) downloadPdf,
  }) {
    return syncIfNeeded(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType)? downloadPdf,
  }) {
    return syncIfNeeded?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType)? downloadPdf,
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
    required TResult Function(_DownloadPdf value) downloadPdf,
  }) {
    return syncIfNeeded(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadPdf value)? downloadPdf,
  }) {
    return syncIfNeeded?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadPdf value)? downloadPdf,
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
    required TResult Function(String projectId, String userType) downloadPdf,
  }) {
    return forceSync(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType)? downloadPdf,
  }) {
    return forceSync?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType)? downloadPdf,
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
    required TResult Function(_DownloadPdf value) downloadPdf,
  }) {
    return forceSync(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadPdf value)? downloadPdf,
  }) {
    return forceSync?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadPdf value)? downloadPdf,
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
abstract class _$$DownloadPdfImplCopyWith<$Res>
    implements $ProjectBomEventCopyWith<$Res> {
  factory _$$DownloadPdfImplCopyWith(
          _$DownloadPdfImpl value, $Res Function(_$DownloadPdfImpl) then) =
      __$$DownloadPdfImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String projectId, String userType});
}

/// @nodoc
class __$$DownloadPdfImplCopyWithImpl<$Res>
    extends _$ProjectBomEventCopyWithImpl<$Res, _$DownloadPdfImpl>
    implements _$$DownloadPdfImplCopyWith<$Res> {
  __$$DownloadPdfImplCopyWithImpl(
      _$DownloadPdfImpl _value, $Res Function(_$DownloadPdfImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? projectId = null,
    Object? userType = null,
  }) {
    return _then(_$DownloadPdfImpl(
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

class _$DownloadPdfImpl implements _DownloadPdf {
  const _$DownloadPdfImpl({required this.projectId, required this.userType});

  @override
  final String projectId;
  @override
  final String userType;

  @override
  String toString() {
    return 'ProjectBomEvent.downloadPdf(projectId: $projectId, userType: $userType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DownloadPdfImpl &&
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
  _$$DownloadPdfImplCopyWith<_$DownloadPdfImpl> get copyWith =>
      __$$DownloadPdfImplCopyWithImpl<_$DownloadPdfImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String projectId, String userType) syncIfNeeded,
    required TResult Function(String projectId, String userType) forceSync,
    required TResult Function(String projectId, String userType) downloadPdf,
  }) {
    return downloadPdf(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String projectId, String userType)? syncIfNeeded,
    TResult? Function(String projectId, String userType)? forceSync,
    TResult? Function(String projectId, String userType)? downloadPdf,
  }) {
    return downloadPdf?.call(projectId, userType);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String projectId, String userType)? syncIfNeeded,
    TResult Function(String projectId, String userType)? forceSync,
    TResult Function(String projectId, String userType)? downloadPdf,
    required TResult orElse(),
  }) {
    if (downloadPdf != null) {
      return downloadPdf(projectId, userType);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_SyncIfNeeded value) syncIfNeeded,
    required TResult Function(_ForceSync value) forceSync,
    required TResult Function(_DownloadPdf value) downloadPdf,
  }) {
    return downloadPdf(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult? Function(_ForceSync value)? forceSync,
    TResult? Function(_DownloadPdf value)? downloadPdf,
  }) {
    return downloadPdf?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_SyncIfNeeded value)? syncIfNeeded,
    TResult Function(_ForceSync value)? forceSync,
    TResult Function(_DownloadPdf value)? downloadPdf,
    required TResult orElse(),
  }) {
    if (downloadPdf != null) {
      return downloadPdf(this);
    }
    return orElse();
  }
}

abstract class _DownloadPdf implements ProjectBomEvent {
  const factory _DownloadPdf(
      {required final String projectId,
      required final String userType}) = _$DownloadPdfImpl;

  @override
  String get projectId;
  @override
  String get userType;
  @override
  @JsonKey(ignore: true)
  _$$DownloadPdfImplCopyWith<_$DownloadPdfImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ProjectBomState {
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_Initial value) initial,
    required TResult Function(_Loading value) loading,
    required TResult Function(_Success value) success,
    required TResult Function(_Failure value) failure,
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
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
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) {
    return initial();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) {
    return initial?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
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
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
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
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
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
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
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
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) {
    return loading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) {
    return loading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
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
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
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
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
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
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
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
  $Res call({int docCount, bool savedBomValues});
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
    Object? docCount = null,
    Object? savedBomValues = null,
  }) {
    return _then(_$SuccessImpl(
      docCount: null == docCount
          ? _value.docCount
          : docCount // ignore: cast_nullable_to_non_nullable
              as int,
      savedBomValues: null == savedBomValues
          ? _value.savedBomValues
          : savedBomValues // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$SuccessImpl implements _Success {
  const _$SuccessImpl({required this.docCount, required this.savedBomValues});

  @override
  final int docCount;
  @override
  final bool savedBomValues;

  @override
  String toString() {
    return 'ProjectBomState.success(docCount: $docCount, savedBomValues: $savedBomValues)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SuccessImpl &&
            (identical(other.docCount, docCount) ||
                other.docCount == docCount) &&
            (identical(other.savedBomValues, savedBomValues) ||
                other.savedBomValues == savedBomValues));
  }

  @override
  int get hashCode => Object.hash(runtimeType, docCount, savedBomValues);

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
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) {
    return success(docCount, savedBomValues);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) {
    return success?.call(docCount, savedBomValues);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(docCount, savedBomValues);
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
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
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
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
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
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
    required TResult orElse(),
  }) {
    if (success != null) {
      return success(this);
    }
    return orElse();
  }
}

abstract class _Success implements ProjectBomState {
  const factory _Success(
      {required final int docCount,
      required final bool savedBomValues}) = _$SuccessImpl;

  int get docCount;
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
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) {
    return failure(message);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) {
    return failure?.call(message);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
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
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
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
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
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
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
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
abstract class _$$DownloadingImplCopyWith<$Res> {
  factory _$$DownloadingImplCopyWith(
          _$DownloadingImpl value, $Res Function(_$DownloadingImpl) then) =
      __$$DownloadingImplCopyWithImpl<$Res>;
}

/// @nodoc
class __$$DownloadingImplCopyWithImpl<$Res>
    extends _$ProjectBomStateCopyWithImpl<$Res, _$DownloadingImpl>
    implements _$$DownloadingImplCopyWith<$Res> {
  __$$DownloadingImplCopyWithImpl(
      _$DownloadingImpl _value, $Res Function(_$DownloadingImpl) _then)
      : super(_value, _then);
}

/// @nodoc

class _$DownloadingImpl implements _Downloading {
  const _$DownloadingImpl();

  @override
  String toString() {
    return 'ProjectBomState.downloading()';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType && other is _$DownloadingImpl);
  }

  @override
  int get hashCode => runtimeType.hashCode;

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) {
    return downloading();
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) {
    return downloading?.call();
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
    required TResult orElse(),
  }) {
    if (downloading != null) {
      return downloading();
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
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
  }) {
    return downloading(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
  }) {
    return downloading?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
    required TResult orElse(),
  }) {
    if (downloading != null) {
      return downloading(this);
    }
    return orElse();
  }
}

abstract class _Downloading implements ProjectBomState {
  const factory _Downloading() = _$DownloadingImpl;
}

/// @nodoc
abstract class _$$PdfReadyImplCopyWith<$Res> {
  factory _$$PdfReadyImplCopyWith(
          _$PdfReadyImpl value, $Res Function(_$PdfReadyImpl) then) =
      __$$PdfReadyImplCopyWithImpl<$Res>;
  @useResult
  $Res call({Uint8List bytes});
}

/// @nodoc
class __$$PdfReadyImplCopyWithImpl<$Res>
    extends _$ProjectBomStateCopyWithImpl<$Res, _$PdfReadyImpl>
    implements _$$PdfReadyImplCopyWith<$Res> {
  __$$PdfReadyImplCopyWithImpl(
      _$PdfReadyImpl _value, $Res Function(_$PdfReadyImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? bytes = freezed,
  }) {
    return _then(_$PdfReadyImpl(
      freezed == bytes
          ? _value.bytes
          : bytes // ignore: cast_nullable_to_non_nullable
              as Uint8List,
    ));
  }
}

/// @nodoc

class _$PdfReadyImpl implements _PdfReady {
  const _$PdfReadyImpl(this.bytes);

  @override
  final Uint8List bytes;

  @override
  String toString() {
    return 'ProjectBomState.pdfReady(bytes: $bytes)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$PdfReadyImpl &&
            const DeepCollectionEquality().equals(other.bytes, bytes));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, const DeepCollectionEquality().hash(bytes));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$PdfReadyImplCopyWith<_$PdfReadyImpl> get copyWith =>
      __$$PdfReadyImplCopyWithImpl<_$PdfReadyImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function() initial,
    required TResult Function() loading,
    required TResult Function(int docCount, bool savedBomValues) success,
    required TResult Function(String message) failure,
    required TResult Function() downloading,
    required TResult Function(Uint8List bytes) pdfReady,
  }) {
    return pdfReady(bytes);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function()? initial,
    TResult? Function()? loading,
    TResult? Function(int docCount, bool savedBomValues)? success,
    TResult? Function(String message)? failure,
    TResult? Function()? downloading,
    TResult? Function(Uint8List bytes)? pdfReady,
  }) {
    return pdfReady?.call(bytes);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function()? initial,
    TResult Function()? loading,
    TResult Function(int docCount, bool savedBomValues)? success,
    TResult Function(String message)? failure,
    TResult Function()? downloading,
    TResult Function(Uint8List bytes)? pdfReady,
    required TResult orElse(),
  }) {
    if (pdfReady != null) {
      return pdfReady(bytes);
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
    required TResult Function(_Downloading value) downloading,
    required TResult Function(_PdfReady value) pdfReady,
  }) {
    return pdfReady(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_Initial value)? initial,
    TResult? Function(_Loading value)? loading,
    TResult? Function(_Success value)? success,
    TResult? Function(_Failure value)? failure,
    TResult? Function(_Downloading value)? downloading,
    TResult? Function(_PdfReady value)? pdfReady,
  }) {
    return pdfReady?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_Initial value)? initial,
    TResult Function(_Loading value)? loading,
    TResult Function(_Success value)? success,
    TResult Function(_Failure value)? failure,
    TResult Function(_Downloading value)? downloading,
    TResult Function(_PdfReady value)? pdfReady,
    required TResult orElse(),
  }) {
    if (pdfReady != null) {
      return pdfReady(this);
    }
    return orElse();
  }
}

abstract class _PdfReady implements ProjectBomState {
  const factory _PdfReady(final Uint8List bytes) = _$PdfReadyImpl;

  Uint8List get bytes;
  @JsonKey(ignore: true)
  _$$PdfReadyImplCopyWith<_$PdfReadyImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
