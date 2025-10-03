// lib/blocs/project_bom/project_bom_bloc.dart
import 'dart:typed_data';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../repositories/bom_repo.dart';
import '../../repositories/project_repo.dart';

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

  const factory ProjectBomEvent.downloadPdf({
    required String projectId,
    required String userType,
  }) = _DownloadPdf;
}

@freezed
class ProjectBomState with _$ProjectBomState {
  const factory ProjectBomState.initial() = _Initial;
  const factory ProjectBomState.loading() = _Loading;
  const factory ProjectBomState.success({
    required int docCount,
    required bool savedBomValues,
  }) = _Success;
  const factory ProjectBomState.failure(String message) = _Failure;
  const factory ProjectBomState.downloading() = _Downloading;
  const factory ProjectBomState.pdfReady(Uint8List bytes) = _PdfReady;
}

class ProjectBomBloc extends Bloc<ProjectBomEvent, ProjectBomState> {
  final Isar _isar;
  final BomRepository _bomRepo = BomRepository();

  ProjectBomBloc(this._isar) : super(const ProjectBomState.initial()) {
    on<_SyncIfNeeded>(_onSyncIfNeeded);
    on<_ForceSync>(_onForceSync);
    on<_DownloadPdf>(_onDownloadPdf);
  }

  Future<void> _onSyncIfNeeded(
    _SyncIfNeeded event,
    Emitter<ProjectBomState> emit,
  ) async {
    emit(const ProjectBomState.loading());
    try {
      final isPrefilled =
          await _isPrefilledProject(_isar, event.projectId, event.userType);
      print("isPrefilled $isPrefilled");
      print("project ${event.projectId}");
      print("userType ${event.userType}");
      if (isPrefilled) {
        emit(const ProjectBomState.success(
          docCount: 0,
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
        docCount: r.docCount,
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
        docCount: r.docCount,
        savedBomValues: r.savedBomValues,
      ));
    } catch (e) {
      emit(ProjectBomState.failure('$e'));
    }
  }

  // Handler:
  Future<void> _onDownloadPdf(
    _DownloadPdf event,
    Emitter<ProjectBomState> emit,
  ) async {
    emit(const ProjectBomState.downloading());
    try {
      final bytes = await _bomRepo.generateBomPdf(
        isar: _isar,
        projectId: event.projectId,
        userType: event.userType,
      );
      emit(ProjectBomState.pdfReady(bytes));
    } catch (e) {
      emit(ProjectBomState.failure("PDF download failed: $e"));
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
