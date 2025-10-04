import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../model/document/document.dart';
import '../../repositories/bom_repo.dart';
import '../../repositories/project_repo.dart';
import '../../utils/utils.dart';

part 'project_bom.freezed.dart';

@freezed
class ProjectBomEvent with _$ProjectBomEvent {
  const factory ProjectBomEvent.syncIfNeeded({
    required String projectId,
    required String userType,
  }) = _SyncIfNeeded;

  const factory ProjectBomEvent.forceSync({
    required String projectId,
    required String userType,
  }) = _ForceSync;
  const factory ProjectBomEvent.downloadWorkflowDocument({
    required String projectId,
    required String userType,
    required List<Document> workflowDocuments,
    required String docType,
  }) = _DownloadWorkflowDocument;
}

@freezed
class ProjectBomState with _$ProjectBomState {
  const factory ProjectBomState.initial() = _Initial;
  const factory ProjectBomState.loading() = _Loading;
  const factory ProjectBomState.success({
    required bool savedBomValues,
  }) = _Success;
  const factory ProjectBomState.failure(String message) = _Failure;
  const factory ProjectBomState.documentDownloadInProgress() =
      _DocDownloadInProgress;
  const factory ProjectBomState.documentDownloadSuccess(File file) =
      _DocDownloadSuccess;
  const factory ProjectBomState.documentDownloadFailure(String error) =
      _DocDownloadFailure;
}

class ProjectBomBloc extends Bloc<ProjectBomEvent, ProjectBomState> {
  final Isar _isar;
  final BomRepository _bomRepo = BomRepository();

  ProjectBomBloc(this._isar) : super(const ProjectBomState.initial()) {
    on<_SyncIfNeeded>(_onSyncIfNeeded);
    on<_ForceSync>(_onForceSync);
    on<_DownloadWorkflowDocument>(_onDownloadWorkflowDocument);
  }

  Future<void> _onSyncIfNeeded(
    _SyncIfNeeded event,
    Emitter<ProjectBomState> emit,
  ) async {
    emit(const ProjectBomState.loading());
    try {
      final isPrefilled =
          await _isPrefilledProject(_isar, event.projectId, event.userType);
      if (isPrefilled) {
        emit(const ProjectBomState.success(
          savedBomValues: false,
        ));
        return;
      }

      final r = await _bomRepo.syncBomForProject(
        isar: _isar,
        projectId: event.projectId,
        userType: event.userType,
      );

      emit(ProjectBomState.success(
        savedBomValues: r.savedBomValues,
      ));
    } catch (e) {
      emit(ProjectBomState.failure('$e'));
    }
  }

  Future<void> _onForceSync(
    _ForceSync event,
    Emitter<ProjectBomState> emit,
  ) async {
    emit(const ProjectBomState.loading());
    try {
      final r = await _bomRepo.syncBomForProject(
        isar: _isar,
        projectId: event.projectId,
        userType: event.userType,
      );
      emit(ProjectBomState.success(
        savedBomValues: r.savedBomValues,
      ));
    } catch (e) {
      emit(ProjectBomState.failure('$e'));
    }
  }

  Future<void> _onDownloadWorkflowDocument(
    _DownloadWorkflowDocument event,
    Emitter<ProjectBomState> emit,
  ) async {
    emit(const ProjectBomState.documentDownloadInProgress());
    try {
      final docs = event.workflowDocuments;
      final match = docs.firstWhere(
        (d) =>
            d.documentType == event.docType &&
            d.fileStore != null &&
            d.fileStore!.isNotEmpty,
        orElse: () =>
            throw Exception("No document of type ${event.docType} in workflow"),
      );

      final fileStoreId = match.fileStore!;
      final file = await getCachedFile(fileStoreId);
      if (file == null) {
        throw Exception(
            "Could not retrieve file from fileStoreId $fileStoreId");
      }
      emit(ProjectBomState.documentDownloadSuccess(file));
    } catch (e, st) {
      emit(ProjectBomState.documentDownloadFailure(e.toString()));
    }
  }

  Future<bool> _isPrefilledProject(
      Isar isar, String projectId, String userType) async {
    final draft = await PrefilledProjectRepository(isar).exists(
      projectId: projectId,
      userType: userType,
    );
    if (draft) {
      return true;
    } else {
      return false;
    }
  }
}
