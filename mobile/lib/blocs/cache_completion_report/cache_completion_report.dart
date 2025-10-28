import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_completion_report.dart';

part 'cache_completion_report.freezed.dart';

/// Small input model for bulk adds
class CompletionFileInput {
  final String projectId;
  final String filePath; // local path or filestore id
  final String fileType; // "pdf" | "image" | "unknown"
  final String fileName; // display name
  final String latitude; // required
  final String longitude; // required
  final int? index;

  CompletionFileInput({
    required this.projectId,
    required this.filePath,
    required this.fileType,
    required this.fileName,
    required this.latitude,
    required this.longitude,
    this.index,
  });
}

@freezed
class CacheCompletionReportEvent with _$CacheCompletionReportEvent {
  /// Load all completion files for a project
  const factory CacheCompletionReportEvent.load(String projectId) = _Load;

  /// Add or update a single file (upsert using unique entryId).
  /// entryId is computed internally from (projectId + filePath).
  const factory CacheCompletionReportEvent.addOrUpdate({
    required String projectId,
    required String filePath,
    required String fileType, // "pdf" | "image" | "unknown"
    required String fileName,
    required String latitude,
    required String longitude,
    int? index,
  }) = _AddOrUpdate;

  /// Bulk add/update many files in one transaction
  const factory CacheCompletionReportEvent.addMany({
    required List<CompletionFileInput> files,
  }) = _AddMany;

  /// Remove one by Isar id
  const factory CacheCompletionReportEvent.removeById({
    required int id,
  }) = _RemoveById;

  /// Remove by (projectId + filePath)
  const factory CacheCompletionReportEvent.removeByPath({
    required String projectId,
    required String filePath,
  }) = _RemoveByPath;

  /// Bulk delete by entryIds (projectId::filePath)
  const factory CacheCompletionReportEvent.deleteManyByEntryId({
    required List<String> entryIds,
  }) = _DeleteManyByEntryId;

  /// Clear all files for a project
  const factory CacheCompletionReportEvent.clearProject(String projectId) =
      _ClearProject;

  const factory CacheCompletionReportEvent.replaceAllForProject({
    required String projectId,
    required List<CompletionFileInput> files,
  }) = _ReplaceAllForProject;
}

@freezed
class CacheCompletionReportState with _$CacheCompletionReportState {
  const factory CacheCompletionReportState.initial() = _Initial;
  const factory CacheCompletionReportState.loading() = _Loading;

  /// Loaded list for the current project (if any)
  const factory CacheCompletionReportState.loaded({
    required String projectId,
    required List<CacheCompletionReport> files,
  }) = _Loaded;

  const factory CacheCompletionReportState.failure(String message) = _Failure;
}

class CacheCompletionReportBloc
    extends Bloc<CacheCompletionReportEvent, CacheCompletionReportState> {
  final Isar isar;

  CacheCompletionReportBloc(this.isar)
      : super(const CacheCompletionReportState.initial()) {
    on<_Load>(_onLoad);
    on<_AddOrUpdate>(_onAddOrUpdate);
    on<_AddMany>(_onAddMany);
    on<_RemoveById>(_onRemoveById);
    on<_RemoveByPath>(_onRemoveByPath);
    on<_DeleteManyByEntryId>(_onDeleteManyByEntryId);
    on<_ClearProject>(_onClearProject);
    on<_ReplaceAllForProject>(_onReplaceAllForProject);
  }

  // ----------------- Helpers -----------------

  String _entryIdOf(String projectId, String filePath) =>
      '$projectId::$filePath';

  String _basename(String pathOrId) {
    final norm = pathOrId.replaceAll('\\', '/');
    final idx = norm.lastIndexOf('/');
    return idx == -1 ? norm : norm.substring(idx + 1);
  }

  Future<void> _emitLoadedForProject(String projectId, Emitter emit) async {
    final list = await isar.cacheCompletionReports
        .where()
        .activityFacilityIdEqualTo(projectId)
        .sortByCreatedAt()
        .findAll();

    emit(CacheCompletionReportState.loaded(
      projectId: projectId,
      files: list,
    ));
  }

  // ----------------- Handlers -----------------

  Future<void> _onLoad(
    _Load event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    emit(const CacheCompletionReportState.loading());
    try {
      await _emitLoadedForProject(event.projectId, emit);
    } catch (e) {
      emit(CacheCompletionReportState.failure('Load failed: $e'));
    }
  }

  Future<void> _onAddOrUpdate(
    _AddOrUpdate event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      final entryId = _entryIdOf(event.projectId, event.filePath);

      await isar.writeTxn(() async {
        // Because entryId has a unique index (replace: true),
        // we can upsert by putting a row with the same entryId.
        // First check if it exists to preserve createdAt.
        final existing = await isar.cacheCompletionReports
            .where()
            .entryIdEqualTo(entryId)
            .findFirst();

        if (existing != null) {
          existing
            ..filePath = event.filePath
            ..fileName = event.fileName
            ..fileType = event.fileType
            ..latitude = event.latitude!
            ..longitude = event.longitude!
            ..index = event.index
            ..updatedAt = DateTime.now();
          await isar.cacheCompletionReports.put(existing);
        } else {
          await isar.cacheCompletionReports.put(
            CacheCompletionReport(
              activityFacilityId: event.projectId,
              filePath: event.filePath,
              entryId: entryId,
              fileName: event.fileName!.isEmpty
                  ? _basename(event.filePath)
                  : event.fileName,
              fileType: event.fileType,
              latitude: event.latitude!,
              longitude: event.longitude!,
              index: event.index,
            )..createdAt = DateTime.now(),
          );
        }
      });

      await _emitLoadedForProject(event.projectId, emit);
    } catch (e) {
      emit(CacheCompletionReportState.failure('Save failed: $e'));
    }
  }

  Future<void> _onAddMany(
    _AddMany event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    if (event.files.isEmpty) return;

    try {
      // Group by project to emit correct loaded states
      final groups = <String, List<CompletionFileInput>>{};
      for (final f in event.files) {
        groups.putIfAbsent(f.projectId, () => []).add(f);
      }

      await isar.writeTxn(() async {
        for (final entry in groups.entries) {
          for (final f in entry.value) {
            final entryId = _entryIdOf(f.projectId, f.filePath);

            final existing = await isar.cacheCompletionReports
                .where()
                .entryIdEqualTo(entryId)
                .findFirst();

            if (existing != null) {
              existing
                ..filePath = f.filePath
                ..fileName =
                    f.fileName.isEmpty ? _basename(f.filePath) : f.fileName
                ..fileType = f.fileType
                ..latitude = f.latitude
                ..longitude = f.longitude
                ..index = f.index
                ..updatedAt = DateTime.now();
              await isar.cacheCompletionReports.put(existing);
            } else {
              await isar.cacheCompletionReports.put(
                CacheCompletionReport(
                  activityFacilityId: f.projectId,
                  filePath: f.filePath,
                  entryId: entryId,
                  fileName:
                      f.fileName.isEmpty ? _basename(f.filePath) : f.fileName,
                  fileType: f.fileType,
                  latitude: f.latitude,
                  longitude: f.longitude,
                  index: f.index,
                )..createdAt = DateTime.now(),
              );
            }
          }
        }
      });

      // Emit loaded for each affected project
      for (final pid in groups.keys) {
        await _emitLoadedForProject(pid, emit);
      }
    } catch (e) {
      emit(CacheCompletionReportState.failure('Bulk save failed: $e'));
    }
  }

  Future<void> _onRemoveById(
    _RemoveById event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      final rec = await isar.cacheCompletionReports.get(event.id);
      final projectId = rec?.activityFacilityId;

      await isar.writeTxn(() async {
        await isar.cacheCompletionReports.delete(event.id);
      });

      if (projectId != null && projectId.isNotEmpty) {
        await _emitLoadedForProject(projectId, emit);
      }
    } catch (e) {
      emit(CacheCompletionReportState.failure('Delete failed: $e'));
    }
  }

  Future<void> _onRemoveByPath(
    _RemoveByPath event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      final entryId = _entryIdOf(event.projectId, event.filePath);

      final toDelete = await isar.cacheCompletionReports
          .where()
          .entryIdEqualTo(entryId)
          .findFirst();

      if (toDelete != null) {
        await isar.writeTxn(() async {
          await isar.cacheCompletionReports.delete(toDelete.id);
        });
      }

      await _emitLoadedForProject(event.projectId, emit);
    } catch (e) {
      emit(CacheCompletionReportState.failure('Delete failed: $e'));
    }
  }

  Future<void> _onDeleteManyByEntryId(
    _DeleteManyByEntryId event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    if (event.entryIds.isEmpty) return;

    try {
      // capture projectIds to refresh later
      final affected = <String>{};
      for (final eid in event.entryIds) {
        final rec = await isar.cacheCompletionReports
            .where()
            .entryIdEqualTo(eid)
            .findFirst();
        if (rec != null) affected.add(rec.activityFacilityId);
      }

      await isar.writeTxn(() async {
        for (final eid in event.entryIds) {
          final rec = await isar.cacheCompletionReports
              .where()
              .entryIdEqualTo(eid)
              .findFirst();
          if (rec != null) {
            await isar.cacheCompletionReports.delete(rec.id);
          }
        }
      });

      for (final pid in affected) {
        await _emitLoadedForProject(pid, emit);
      }
    } catch (e) {
      emit(CacheCompletionReportState.failure('Bulk delete failed: $e'));
    }
  }

  Future<void> _onClearProject(
    _ClearProject event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      final all = await isar.cacheCompletionReports
          .where()
          .activityFacilityIdEqualTo(event.projectId)
          .findAll();

      await isar.writeTxn(() async {
        for (final r in all) {
          await isar.cacheCompletionReports.delete(r.id);
        }
      });

      await _emitLoadedForProject(event.projectId, emit);
    } catch (e) {
      emit(CacheCompletionReportState.failure('Clear failed: $e'));
    }
  }

  Future<void> _onReplaceAllForProject(
    _ReplaceAllForProject event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      final pid = event.projectId;

      await isar.writeTxn(() async {
        // 1) delete everything for this project
        final existing = await isar.cacheCompletionReports
            .where()
            .activityFacilityIdEqualTo(pid)
            .findAll();
        for (final r in existing) {
          await isar.cacheCompletionReports.delete(r.id);
        }

        // 2) add the new ones
        for (final f in event.files) {
          final entryId = _entryIdOf(f.projectId, f.filePath);
          await isar.cacheCompletionReports.put(
            CacheCompletionReport(
              activityFacilityId: f.projectId,
              filePath: f.filePath,
              entryId: entryId,
              fileName:
                  (f.fileName.isEmpty) ? _basename(f.filePath) : f.fileName,
              fileType: f.fileType,
              latitude: f.latitude,
              longitude: f.longitude,
              index: f.index,
            )..createdAt = DateTime.now(),
          );
        }
      });

      await _emitLoadedForProject(event.projectId, emit);
    } catch (e) {
      emit(CacheCompletionReportState.failure('Replace-all failed: $e'));
    }
  }
}
