import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_submission_job.dart';
import '../../repositories/operation_progress_repo.dart';
import '../../utils/background_service.dart';
import '../../utils/operation_progress.dart';

part 'send_back.freezed.dart';

class SendBackBloc extends Bloc<SendBackEvent, SendBackState> {
  final OperationProgressRepository _progressRepo;
  StreamSubscription<CacheSubmissionJob?>? _jobSub;
  String? _activeWatchId;

  SendBackBloc(Isar isar)
      : _progressRepo = OperationProgressRepository(isar),
        super(const SendBackState.initial()) {
    on<_Submit>(_onSubmit);
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

  Future<void> _onSubmit(
    _Submit event,
    Emitter<SendBackState> emit,
  ) async {
    add(SendBackEvent.watch(event.activityFacilityId));
    await _progressRepo.upsertJob(
      activityFacilityId: event.activityFacilityId,
      operationType: OperationTypes.sendBack,
      status: OperationStatuses.queued,
      stageKey: 'preparing_send_back',
      completedSteps: 1,
      totalSteps: sendBackStages.length,
      incrementRetry: event.isRetry,
    );

    await BackgroundServiceController.I.enqueueSendBack(
      activityFacilityId: event.activityFacilityId,
      userType: event.userType,
    );
  }

  Future<void> _onRetry(
    _Retry event,
    Emitter<SendBackState> emit,
  ) async {
    add(SendBackEvent.submit(
      activityFacilityId: event.activityFacilityId,
      userType: event.userType,
      isRetry: true,
    ));
  }

  Future<void> _onDismiss(
    _Dismiss event,
    Emitter<SendBackState> emit,
  ) async {
    emit(const SendBackState.initial());
  }

  Future<void> _onWatch(
    _Watch event,
    Emitter<SendBackState> emit,
  ) async {
    _activeWatchId = event.activityFacilityId;
    await _jobSub?.cancel();
    _jobSub = _progressRepo.watchJob(event.activityFacilityId).listen((job) {
      add(SendBackEvent.jobChanged(job));
    });
  }

  Future<void> _onJobChanged(
    _JobChanged event,
    Emitter<SendBackState> emit,
  ) async {
    final job = event.job;
    if (job == null ||
        job.activityFacilityId != _activeWatchId ||
        job.operationType != OperationTypes.sendBack) {
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
        emit(SendBackState.inProgress(progress));
        break;
      case OperationStatuses.failed:
        emit(SendBackState.failure(progress));
        break;
      case OperationStatuses.success:
        if (state.maybeWhen(
          inProgress: (_) => true,
          failure: (_) => true,
          orElse: () => false,
        )) {
          emit(const SendBackState.success());
        }
        break;
      default:
        emit(const SendBackState.initial());
        break;
    }
  }
}

@freezed
class SendBackEvent with _$SendBackEvent {
  const factory SendBackEvent.submit({
    required String activityFacilityId,
    required String userType,
    @Default(false) bool isRetry,
  }) = _Submit;

  const factory SendBackEvent.retry({
    required String activityFacilityId,
    required String userType,
  }) = _Retry;

  const factory SendBackEvent.watch(String activityFacilityId) = _Watch;

  const factory SendBackEvent.jobChanged(CacheSubmissionJob? job) = _JobChanged;

  const factory SendBackEvent.dismiss() = _Dismiss;
}

@freezed
class SendBackState with _$SendBackState {
  const factory SendBackState.initial() = _Initial;
  const factory SendBackState.inProgress(OperationProgressModel progress) =
      _InProgress;
  const factory SendBackState.failure(OperationProgressModel progress) =
      _Failure;
  const factory SendBackState.success() = _Success;
}
