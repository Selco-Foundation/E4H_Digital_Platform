import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_submission_job.dart';
import '../../repositories/operation_progress_repo.dart';
import '../../utils/background_service.dart';
import '../../utils/operation_progress.dart';

part 'asset_rejection.freezed.dart';

class RejectionBloc extends Bloc<RejectionEvent, RejectionState> {
  final OperationProgressRepository _progressRepo;
  StreamSubscription<CacheSubmissionJob?>? _jobSub;
  String? _activeWatchId;

  RejectionBloc(Isar isar)
      : _progressRepo = OperationProgressRepository(isar),
        super(const RejectionState.initial()) {
    on<_SubmitRejection>(_onSubmitRejection);
    on<_Retry>(_onRetry);
    on<_Watch>(_onWatch);
    on<_JobChanged>(_onJobChanged);
    on<_Dismiss>(_onDismiss);
  }

  @override
  Future<void> close() async {
    await _jobSub?.cancel();
    return super.close();
  }

  Future<void> _onSubmitRejection(
    _SubmitRejection event,
    Emitter<RejectionState> emit,
  ) async {
    add(RejectionEvent.watch(event.activityFacilityId));
    if (!event.isRetry) {
      await _progressRepo.clearOperationCheckpoints(
        activityFacilityId: event.activityFacilityId,
        operationType: OperationTypes.reject,
      );
    }

    final txMaps = event.transactions.map<Map<String, dynamic>>((t) {
      if (t is Map<String, dynamic>) return t;
      try {
        final m = (t as dynamic).toJson();
        if (m is Map<String, dynamic>) return m;
      } catch (_) {}
      try {
        final m = (t as dynamic).toMap();
        if (m is Map<String, dynamic>) return m;
      } catch (_) {}
      throw Exception('Transaction must be Map or have toJson/toMap');
    }).toList();

    await _progressRepo.upsertJob(
      activityFacilityId: event.activityFacilityId,
      operationType: OperationTypes.reject,
      status: OperationStatuses.queued,
      stageKey: 'preparing_rejection',
      completedSteps: 1,
      totalSteps: rejectionStages.length,
      incrementRetry: event.isRetry,
    );

    emit(
      RejectionState.inProgress(
        OperationProgressModel(
          activityFacilityId: event.activityFacilityId,
          operationType: OperationTypes.reject,
          status: OperationStatuses.queued,
          stageKey: 'preparing_rejection',
          stageLabel:
              stageForKey(OperationTypes.reject, 'preparing_rejection').label,
          completedSteps: 1,
          totalSteps: rejectionStages.length,
          progressPercent: progressPercent(
            completedSteps: 1,
            totalSteps: rejectionStages.length,
          ),
          retryCount: state.maybeWhen(
            inProgress: (progress) => progress.retryCount,
            failure: (progress) => progress.retryCount,
            orElse: () => 0,
          ),
          isBlocking: true,
        ),
      ),
    );

    await BackgroundServiceController.I.enqueueRejection(
      activityFacilityId: event.activityFacilityId,
      userType: event.userType,
      transactions: txMaps,
    );
  }

  Future<void> _onRetry(
    _Retry event,
    Emitter<RejectionState> emit,
  ) async {
    add(RejectionEvent.submitRejection(
      activityFacilityId: event.activityFacilityId,
      userType: event.userType,
      transactions: event.transactions,
      isRetry: true,
    ));
  }

  Future<void> _onDismiss(
    _Dismiss event,
    Emitter<RejectionState> emit,
  ) async {
    emit(const RejectionState.initial());
  }

  Future<void> _onWatch(
    _Watch event,
    Emitter<RejectionState> emit,
  ) async {
    _activeWatchId = event.activityFacilityId;
    await _jobSub?.cancel();
    _jobSub = _progressRepo.watchJob(event.activityFacilityId).listen((job) {
      add(RejectionEvent.jobChanged(job));
    });
  }

  Future<void> _onJobChanged(
    _JobChanged event,
    Emitter<RejectionState> emit,
  ) async {
    final job = event.job;
    if (job == null ||
        job.activityFacilityId != _activeWatchId ||
        job.operationType != OperationTypes.reject) {
      return;
    }

    final progress = OperationProgressModel(
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

    switch (job.status) {
      case OperationStatuses.queued:
      case OperationStatuses.running:
      case OperationStatuses.partial:
        emit(RejectionState.inProgress(progress));
        break;
      case OperationStatuses.failed:
        emit(RejectionState.failure(progress));
        break;
      case OperationStatuses.success:
        if (state.maybeWhen(
          inProgress: (_) => true,
          failure: (_) => true,
          orElse: () => false,
        )) {
          emit(const RejectionState.success());
        }
        break;
      default:
        emit(const RejectionState.initial());
        break;
    }
  }
}

@freezed
class RejectionEvent with _$RejectionEvent {
  const factory RejectionEvent.submitRejection({
    required String activityFacilityId,
    required String userType,
    required List<dynamic> transactions,
    @Default(false) bool isRetry,
  }) = _SubmitRejection;

  const factory RejectionEvent.retry({
    required String activityFacilityId,
    required String userType,
    required List<dynamic> transactions,
  }) = _Retry;

  const factory RejectionEvent.watch(String activityFacilityId) = _Watch;

  const factory RejectionEvent.jobChanged(CacheSubmissionJob? job) =
      _JobChanged;

  const factory RejectionEvent.dismiss() = _Dismiss;
}

@freezed
class RejectionState with _$RejectionState {
  const factory RejectionState.initial() = _Initial;
  const factory RejectionState.inProgress(OperationProgressModel progress) =
      _InProgress;
  const factory RejectionState.failure(OperationProgressModel progress) =
      _Failure;
  const factory RejectionState.success() = _Success;
}
