import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_submission_job.dart';
import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_project.dart';
import '../../repositories/project_repo.dart';
import '../../utils/background_service.dart';

part 'asset_submission.freezed.dart';

StreamSubscription? _jobSub;
StreamSubscription? _bulkJobsSub;
StreamSubscription? _svcErrSub;
StreamSubscription? _svcDoneSub;

class AssetSubmissionBloc
    extends Bloc<AssetSubmissionEvent, AssetSubmissionState> {
  final Isar _isar;
  final UnsubmittedProjectRepository _draftRepo;

  // Track currently active single submit
  String? _activeSingleProjectId;

  AssetSubmissionBloc(this._isar)
      : _draftRepo = UnsubmittedProjectRepository(_isar),
        super(const AssetSubmissionState.initial()) {
    on<_SubmitAll>(_onSubmitAll);
    on<_SubmitAllDrafts>(_onSubmitAllDrafts);

    on<AssetSubmissionEvent>((event, emit) async {
      await event.maybeMap(
        svcError: (e) async =>
            await _handleSvcError(e.projectId, e.message, emit),
        svcDone: (e) async => await _handleSvcDone(e.projectId, emit),
        orElse: () async {},
      );
    });

    final svc = FlutterBackgroundService();

    _svcErrSub?.cancel();
    _svcErrSub = svc.on(kEvtError).listen((data) {
      final pid = data?['projectId'] as String?;
      final msg = data?['message']?.toString();
      // DEBUG
      // ignore: avoid_print
      print('[BLoC] kEvtError stream received pid=$pid msg=$msg');
      if (pid != null) {
        add(AssetSubmissionEvent.svcError(projectId: pid, message: msg));
      }
    });

    _svcDoneSub?.cancel();
    _svcDoneSub = svc.on(kEvtDone).listen((data) {
      final pid = data?['projectId'] as String?;
      // DEBUG
      // ignore: avoid_print
      print('[BLoC] kEvtDone stream received pid=$pid');
      if (pid != null) {
        add(AssetSubmissionEvent.svcDone(projectId: pid));
      }
    });
  }

  Future<void> upsertSyncRecord(String userType) async {
    final now = DateTime.now().toUtc();

    await _isar.writeTxn(() async {
      final existing = await _isar.cacheSyncRecords
          .where()
          .userTypeEqualTo(userType)
          .findFirst();

      if (existing != null) {
        existing.syncedAt = now;
        await _isar.cacheSyncRecords.put(existing);
      } else {
        final record = CacheSyncRecord(userType: userType, syncedAt: now);
        await _isar.cacheSyncRecords.put(record);
      }
    });
  }

  @override
  Future<void> close() {
    _bulkJobsSub?.cancel();
    _jobSub?.cancel();
    _svcErrSub?.cancel();
    _svcDoneSub?.cancel();
    return super.close();
  }

  // ================================================
  // Submit all drafts (batch)
  // ================================================
  Future<void> _onSubmitAllDrafts(
    _SubmitAllDrafts event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    emit(const AssetSubmissionState.loading());
    await upsertSyncRecord(event.userType);

    // Load all local, unsubmitted projects for this userType
    final localEntries = await _isar.cacheUnsubmittedProjects
        .where()
        .filter()
        .userTypeEqualTo(event.userType)
        .findAll();

    final projectIds = localEntries.map((e) => e.project.id).toList();
    if (projectIds.isEmpty) {
      emit(const AssetSubmissionState.failure("No drafts to sync."));
      return;
    }

    // Mark all as queued and enqueue immediately
    for (final pid in projectIds) {
      await _writeJobStatusUI(projectId: pid, status: 'queued');
      await BackgroundServiceController.I.enqueueSubmission(
        projectId: pid,
        userType: event.userType,
        fromDraft: true,
      );
    }

    // Batch watcher: compute progress + final state
    await _bulkJobsSub?.cancel();
    _bulkJobsSub = _isar.cacheSubmissionJobs.watchLazy().listen((_) async {
      await _emitBulkProgress(projectIds: projectIds, emit: emit);
    });

    // Initial “0 of N”
    if (!emit.isDone) {
      emit(AssetSubmissionState.progress(
          completed: 0, total: projectIds.length));
    }
  }

  Future<void> _emitBulkProgress({
    required List<String> projectIds,
    required Emitter<AssetSubmissionState> emit,
  }) async {
    final jobs = await _isar.cacheSubmissionJobs
        .where()
        .anyOf(projectIds, (q, pid) => q.projectIdEqualTo(pid))
        .findAll();

    final total = projectIds.length;
    final successes = jobs.where((j) => j.status == 'success').length;
    final anyFailed = jobs.any((j) => j.status == 'failed');
    final anyRunningOrQueued =
        jobs.any((j) => j.status == 'running' || j.status == 'queued');

    if (!emit.isDone) {
      emit(AssetSubmissionState.progress(completed: successes, total: total));
    }

    if (!anyRunningOrQueued) {
      await BackgroundServiceController.I.stopNow(); // clear notif

      if (anyFailed) {
        if (!emit.isDone) {
          emit(const AssetSubmissionState.failure('Some submissions failed.'));
        }
      } else {
        if (!emit.isDone) {
          emit(const AssetSubmissionState.success());
        }
      }

      await _bulkJobsSub?.cancel();
      _bulkJobsSub = null;
    }
  }

  // ================================================
  // Single submit
  // ================================================
  Future<void> _onSubmitAll(
    _SubmitAll event,
    Emitter<AssetSubmissionState> emit,
  ) =>
      _handleSubmit(
        projectId: event.projectId,
        userType: event.userType,
        emit: emit,
        fromDraft: false,
      );

  Future<bool> _handleSubmit({
    required String projectId,
    required String userType,
    required Emitter<AssetSubmissionState> emit,
    required bool fromDraft,
  }) async {
    print("loading now now");
    emit(const AssetSubmissionState.loading());
    _activeSingleProjectId = fromDraft ? null : projectId;

    await _writeJobStatusUI(projectId: projectId, status: 'queued');

    await BackgroundServiceController.I.enqueueSubmission(
      projectId: projectId,
      userType: userType,
      fromDraft: fromDraft,
    );

    // Keep the per-project watcher as before (for redundancy)
    if (!fromDraft) {
      await _jobSub?.cancel();
      _jobSub = _isar.cacheSubmissionJobs
          .where()
          .projectIdEqualTo(projectId)
          .watch(fireImmediately: true)
          .listen((rows) async {
        if (rows.isEmpty) return;
        final job = rows.first;

        switch (job.status) {
          case 'running':
            // already in loading
            break;

          case 'success':
            if (!emit.isDone) emit(const AssetSubmissionState.success());
            _activeSingleProjectId = null;
            await _jobSub?.cancel();
            _jobSub = null;
            await BackgroundServiceController.I.stopNow();
            break;

          case 'failed':
            if (!emit.isDone) {
              emit(AssetSubmissionState.failure(job.error ?? 'Failed.'));
            }
            _activeSingleProjectId = null;
            await _jobSub?.cancel();
            _jobSub = null;
            await BackgroundServiceController.I.stopNow();
            break;

          case 'queued':
          default:
            break;
        }
      });
    }

    return true;
  }

  Future<void> _handleSvcError(
    String projectId,
    String? message,
    Emitter<AssetSubmissionState> emit,
  ) async {
    // Mirror to Isar so watchers fire
    await _writeJobStatusUI(
      projectId: projectId,
      status: 'failed',
      error: message,
    );

    // If the active single matches, emit immediately (single submit)
    if (_activeSingleProjectId != null && _activeSingleProjectId == projectId) {
      // ignore: avoid_print
      print('[BLoC] _handleSvcError -> single emit failure');
      emit(AssetSubmissionState.failure(message ?? 'Failed.'));
      _activeSingleProjectId = null;
      await BackgroundServiceController.I.stopNow();
      return;
    }

    // NEW: also emit failure for batch runs so the dialog closes immediately
    // ignore: avoid_print
    print('[BLoC] _handleSvcError -> batch emit failure');
    if (!emit.isDone) {
      emit(AssetSubmissionState.failure(message ?? 'Failed.'));
    }
    await BackgroundServiceController.I.stopNow();
    // Batch watcher will still run and settle things afterward; this just updates UI promptly.
  }

  Future<void> _handleSvcDone(
    String projectId,
    Emitter<AssetSubmissionState> emit,
  ) async {
    // Mirror to Isar so watchers fire
    await _writeJobStatusUI(
      projectId: projectId,
      status: 'success',
    );

    if (_activeSingleProjectId != null && _activeSingleProjectId == projectId) {
      // ignore: avoid_print
      print('[BLoC] _handleSvcDone -> single emit success');
      emit(const AssetSubmissionState.success());
      _activeSingleProjectId = null;
      await BackgroundServiceController.I.stopNow();
      return;
    }

    // For batch, let the watcher compute progress/finish; no immediate success emit here.
    // ignore: avoid_print
    print('[BLoC] _handleSvcDone -> batch (no immediate emit)');
  }

  // ================================================
  // helpers
  // ================================================
  Future<void> _writeJobStatusUI({
    required String projectId,
    required String status,
    String? error,
  }) async {
    await _isar.writeTxn(() async {
      final existing = await _isar.cacheSubmissionJobs
          .where()
          .projectIdEqualTo(projectId)
          .findFirst();

      if (existing == null) {
        await _isar.cacheSubmissionJobs.put(
          CacheSubmissionJob(
              projectId: projectId, status: status, error: error),
        );
      } else {
        existing
          ..status = status
          ..error = error;
        await _isar.cacheSubmissionJobs.put(existing);
      }
    });
  }
}

// ======================= Freezed unions =========================

@freezed
class AssetSubmissionEvent with _$AssetSubmissionEvent {
  const factory AssetSubmissionEvent.submitAll({
    required String projectId,
    required String userType,
  }) = _SubmitAll;

  const factory AssetSubmissionEvent.submitAllDrafts({
    required String userType,
  }) = _SubmitAllDrafts;

  // Bridge events from background service
  const factory AssetSubmissionEvent.svcError({
    required String projectId,
    String? message,
  }) = _SvcError;

  const factory AssetSubmissionEvent.svcDone({
    required String projectId,
  }) = _SvcDone;
}

@freezed
class AssetSubmissionState with _$AssetSubmissionState {
  const factory AssetSubmissionState.initial() = _Initial;
  const factory AssetSubmissionState.loading() = _Loading;
  const factory AssetSubmissionState.success() = _Success;
  const factory AssetSubmissionState.failure(String errorMessage) = _Failure;

  const factory AssetSubmissionState.progress({
    required int completed,
    required int total,
  }) = _Progress;
}
