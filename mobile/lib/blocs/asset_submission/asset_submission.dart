import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_submission_job.dart';
import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_activity_facility.dart';
import '../../repositories/operation_progress_repo.dart';
import '../../utils/background_service.dart';
import '../../utils/i18_key_constants.dart' as i18;
import '../../utils/operation_progress.dart';

part 'asset_submission.freezed.dart';

class AssetSubmissionBloc
    extends Bloc<AssetSubmissionEvent, AssetSubmissionState> {
  final Isar _isar;
  late final OperationProgressRepository _progressRepo;

  StreamSubscription<CacheSubmissionJob?>? _jobSub;
  StreamSubscription? _bulkJobsSub;

  String? _activeWatchId;
  Set<String> _activeBulkSyncIds = const {};
  int _bulkWatchToken = 0;
  int? _bulkFirstSnapshotToken;

  AssetSubmissionBloc(this._isar)
      : super(const AssetSubmissionState.initial()) {
    _progressRepo = OperationProgressRepository(_isar);

    on<_SubmitAll>(_onSubmitAll);
    on<_Retry>(_onRetry);
    on<_Watch>(_onWatch);
    on<_JobChanged>(_onJobChanged);
    on<_SubmitAllDrafts>(_onSubmitAllDrafts);
    on<_BulkJobsChanged>(_onBulkJobsChanged);
    on<_Dismiss>(_onDismiss);
  }

  @override
  Future<void> close() async {
    await _jobSub?.cancel();
    await _bulkJobsSub?.cancel();
    return super.close();
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

  Future<void> _onWatch(
    _Watch event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    _activeWatchId = event.activityFacilityId;
    await _jobSub?.cancel();
    _jobSub = _progressRepo.watchJob(event.activityFacilityId).listen((job) {
      add(AssetSubmissionEvent.jobChanged(job));
    });
  }

  Future<void> _onSubmitAll(
    _SubmitAll event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    add(AssetSubmissionEvent.watch(event.activityFacilityId));
    if (!event.isRetry) {
      await _progressRepo.clearOperationCheckpoints(
        activityFacilityId: event.activityFacilityId,
        operationType: OperationTypes.submit,
      );
    }
    await _progressRepo.upsertJob(
      activityFacilityId: event.activityFacilityId,
      operationType: OperationTypes.submit,
      status: OperationStatuses.queued,
      stageKey: 'preparing_submission',
      completedSteps: 1,
      totalSteps: submitStages.length,
      incrementRetry: event.isRetry,
    );

    await BackgroundServiceController.I.enqueueSubmission(
      activityFacilityId: event.activityFacilityId,
      facilityId: event.facilityId,
      userType: event.userType,
      fromDraft: false,
    );
  }

  Future<void> _onRetry(
    _Retry event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    add(AssetSubmissionEvent.submitAll(
      activityFacilityId: event.activityFacilityId,
      facilityId: event.facilityId,
      userType: event.userType,
      isRetry: true,
    ));
  }

  Future<void> _onDismiss(
    _Dismiss event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    emit(const AssetSubmissionState.initial());
  }

  Future<void> _onJobChanged(
    _JobChanged event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    final job = event.job;
    if (job == null ||
        job.activityFacilityId != _activeWatchId ||
        job.operationType != OperationTypes.submit) {
      return;
    }

    final model = _toProgressModel(job);
    switch (job.status) {
      case OperationStatuses.queued:
      case OperationStatuses.running:
      case OperationStatuses.partial:
        emit(AssetSubmissionState.inProgress(model));
        break;
      case OperationStatuses.failed:
        emit(AssetSubmissionState.failure(model));
        break;
      case OperationStatuses.success:
        if (state.maybeWhen(
          inProgress: (_) => true,
          failure: (_) => true,
          orElse: () => false,
        )) {
          emit(const AssetSubmissionState.success());
        }
        break;
      default:
        emit(const AssetSubmissionState.initial());
        break;
    }
  }

  Future<void> _onSubmitAllDrafts(
    _SubmitAllDrafts event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    await upsertSyncRecord(event.userType);

    final localEntries = await _isar.cacheUnsubmittedActivityFacilitys
        .where()
        .filter()
        .userTypeEqualTo(event.userType)
        .findAll();

    final activityFacilityIds =
        localEntries.map((e) => e.activityFacility.id).toList(growable: false);
    _activeBulkSyncIds = activityFacilityIds.toSet();

    if (activityFacilityIds.isEmpty) {
      emit(const AssetSubmissionState.bulkFailure('No drafts to sync.'));
      return;
    }

    for (final entry in localEntries) {
      final pid = entry.activityFacility.id;
      final facilityId = entry.activityFacility.facility?.facilityId ?? '';

      await _progressRepo.clearOperationCheckpoints(
        activityFacilityId: pid,
        operationType: OperationTypes.submit,
      );
      await _progressRepo.upsertJob(
        activityFacilityId: pid,
        operationType: OperationTypes.submit,
        status: OperationStatuses.queued,
        stageKey: 'preparing_submission',
        completedSteps: 1,
        totalSteps: submitStages.length,
      );
      await BackgroundServiceController.I.enqueueSubmission(
        activityFacilityId: pid,
        facilityId: facilityId,
        userType: event.userType,
        fromDraft: true,
      );
    }

    await _bulkJobsSub?.cancel();
    final watchToken = ++_bulkWatchToken;
    _bulkFirstSnapshotToken = watchToken;
    _bulkJobsSub = _isar.cacheSubmissionJobs.watchLazy().listen((_) async {
      final jobs = await _isar.cacheSubmissionJobs
          .where()
          .anyOf(activityFacilityIds,
              (q, pid) => q.activityFacilityIdEqualTo(pid.toString()))
          .findAll();
      if (isClosed || watchToken != _bulkWatchToken) return;
      add(AssetSubmissionEvent.bulkJobsChanged(
        jobs: jobs,
        watchToken: watchToken,
      ));
    });

    emit(
      AssetSubmissionState.bulkProgress(
        BulkOperationProgressModel(
          completed: 0,
          total: activityFacilityIds.length,
          progressPercent: 0,
          activeCount: activityFacilityIds.length,
          label: i18.syncLoading.preparingSync,
        ),
      ),
    );
  }

  Future<void> _onBulkJobsChanged(
    _BulkJobsChanged event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    if (event.watchToken != _bulkWatchToken || _activeBulkSyncIds.isEmpty) {
      return;
    }

    final isFirstSnapshotForWatch = _bulkFirstSnapshotToken == event.watchToken;
    if (isFirstSnapshotForWatch) {
      _bulkFirstSnapshotToken = null;
    }

    final total = _activeBulkSyncIds.length;
    final jobsById = <String, CacheSubmissionJob>{};
    for (final job in event.jobs) {
      if (job.operationType != OperationTypes.submit) continue;
      if (!_activeBulkSyncIds.contains(job.activityFacilityId)) continue;
      jobsById[job.activityFacilityId] = job;
    }

    var completed = 0;
    var activeCount = 0;
    var progressSum = 0;
    CacheSubmissionJob? failedJob;

    for (final activityFacilityId in _activeBulkSyncIds) {
      final job = jobsById[activityFacilityId];
      if (job == null) continue;

      progressSum += job.progressPercent;
      if (job.status == OperationStatuses.success) {
        completed += 1;
      }
      if (job.status == OperationStatuses.queued ||
          job.status == OperationStatuses.running ||
          job.status == OperationStatuses.partial) {
        activeCount += 1;
      }
      if (failedJob == null && job.status == OperationStatuses.failed) {
        failedJob = job;
      }
    }

    final syncingCount = (completed + activeCount).clamp(0, total);
    final progress = BulkOperationProgressModel(
      completed: completed,
      total: total,
      progressPercent: total == 0 ? 0 : (progressSum / total).round(),
      activeCount: activeCount,
      label: completed >= total
          ? 'Sync completed'
          : 'Syncing $syncingCount of $total reports',
    );

    emit(AssetSubmissionState.bulkProgress(progress));

    if (isFirstSnapshotForWatch && event.jobs.isEmpty) {
      return;
    }

    if (activeCount > 0) return;

    _activeBulkSyncIds = const {};
    await _bulkJobsSub?.cancel();
    _bulkJobsSub = null;

    if (failedJob != null) {
      emit(AssetSubmissionState.bulkFailure(
        failedJob.lastError ?? 'Some submissions failed.',
      ));
      return;
    }

    emit(const AssetSubmissionState.success());
  }

  OperationProgressModel _toProgressModel(CacheSubmissionJob job) {
    return OperationProgressModel(
      activityFacilityId: job.activityFacilityId,
      operationType: job.operationType,
      status: job.status,
      stageKey: job.stageKey,
      stageLabel: job.stageLabel,
      completedSteps: job.completedSteps,
      totalSteps: job.totalSteps,
      progressPercent: job.progressPercent,
      retryCount: job.retryCount,
      isBlocking: job.isBlocking,
      errorMessage: job.lastError,
    );
  }
}

@freezed
class AssetSubmissionEvent with _$AssetSubmissionEvent {
  const factory AssetSubmissionEvent.submitAll({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
    @Default(false) bool isRetry,
  }) = _SubmitAll;

  const factory AssetSubmissionEvent.retry({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
  }) = _Retry;

  const factory AssetSubmissionEvent.watch(String activityFacilityId) = _Watch;

  const factory AssetSubmissionEvent.jobChanged(CacheSubmissionJob? job) =
      _JobChanged;

  const factory AssetSubmissionEvent.submitAllDrafts({
    required String userType,
  }) = _SubmitAllDrafts;

  const factory AssetSubmissionEvent.bulkJobsChanged({
    required List<CacheSubmissionJob> jobs,
    required int watchToken,
  }) = _BulkJobsChanged;

  const factory AssetSubmissionEvent.dismiss() = _Dismiss;
}

@freezed
class AssetSubmissionState with _$AssetSubmissionState {
  const factory AssetSubmissionState.initial() = _Initial;
  const factory AssetSubmissionState.inProgress(
      OperationProgressModel progress) = _InProgress;
  const factory AssetSubmissionState.failure(OperationProgressModel progress) =
      _Failure;
  const factory AssetSubmissionState.success() = _Success;
  const factory AssetSubmissionState.bulkProgress(
    BulkOperationProgressModel progress,
  ) = _BulkProgress;
  const factory AssetSubmissionState.bulkFailure(String errorMessage) =
      _BulkFailure;
}
