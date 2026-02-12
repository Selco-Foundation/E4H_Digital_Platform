import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_submission_job.dart';
import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_activity_facility.dart';
import '../../repositories/activity_facility_repo.dart';
import '../../utils/background_service.dart';

part 'asset_submission.freezed.dart';

class AssetSubmissionBloc
    extends Bloc<AssetSubmissionEvent, AssetSubmissionState> {
  final Isar _isar;
  final UnsubmittedActivityFacilityRepository _draftRepo;

  StreamSubscription? _jobSub;
  StreamSubscription? _bulkJobsSub;
  StreamSubscription? _svcErrSub;
  StreamSubscription? _svcDoneSub;

  bool _isBatchMode = false;
  List<String> _batchIds = const [];

  String? _activeSingleActivityFacilityId;

  AssetSubmissionBloc(this._isar)
      : _draftRepo = UnsubmittedActivityFacilityRepository(_isar),
        super(const AssetSubmissionState.initial()) {
    on<_SubmitAll>(_onSubmitAll);
    on<_SubmitAllDrafts>(_onSubmitAllDrafts);

    on<AssetSubmissionEvent>((event, emit) async {
      await event.maybeMap(
        svcError: (e) async =>
            await _handleSvcError(e.activityFacilityId, e.message, emit),
        svcDone: (e) async => await _handleSvcDone(e.activityFacilityId, emit),
        orElse: () async {},
      );
    });

    final svc = FlutterBackgroundService();

    _svcErrSub?.cancel();
    _svcErrSub = svc.on(kEvtError).listen((data) {
      final pid = data?['activityFacilityId'] as String?;
      final msg = data?['message']?.toString();
      AppLogger.instance
          .info('[BLoC] kEvtError stream received pid=$pid msg=$msg');
      if (pid != null) {
        add(AssetSubmissionEvent.svcError(
            activityFacilityId: pid, message: msg));
      }
    });

    _svcDoneSub?.cancel();
    _svcDoneSub = svc.on(kEvtDone).listen((data) {
      final pid = data?['activityFacilityId'] as String?;
      AppLogger.instance.info('[BLoC] kEvtDone stream received pid=$pid');
      if (pid != null) {
        add(AssetSubmissionEvent.svcDone(activityFacilityId: pid));
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

  Future<void> _onSubmitAllDrafts(
    _SubmitAllDrafts event,
    Emitter<AssetSubmissionState> emit,
  ) async {
    emit(const AssetSubmissionState.loading());
    await upsertSyncRecord(event.userType);

    final localEntries = await _isar.cacheUnsubmittedActivityFacilitys
        .where()
        .filter()
        .userTypeEqualTo(event.userType)
        .findAll();

    final activityFacilityIds =
        localEntries.map((e) => e.activityFacility.id).toList();

    _isBatchMode = true;
    _batchIds = activityFacilityIds;
    _activeSingleActivityFacilityId = null;

    if (activityFacilityIds.isEmpty) {
      emit(const AssetSubmissionState.failure("No drafts to sync."));
      _isBatchMode = false;
      _batchIds = const [];
      return;
    }

    for (final entry in localEntries) {
      final pid = entry.activityFacility.id;
      final facilityId = entry.activityFacility.facility?.facilityId ?? "";

      await _writeJobStatusUI(activityFacilityId: pid, status: 'queued');
      await BackgroundServiceController.I.enqueueSubmission(
        activityFacilityId: pid,
        facilityId: facilityId,
        userType: event.userType,
        fromDraft: true,
      );
    }

    await _bulkJobsSub?.cancel();
    _bulkJobsSub = _isar.cacheSubmissionJobs.watchLazy().listen((_) async {
      await _emitBulkProgress(
          activityFacilityIds: activityFacilityIds, emit: emit);
    });

    if (!emit.isDone) {
      emit(AssetSubmissionState.progress(
          completed: 0, total: activityFacilityIds.length));
    }
  }

  Future<void> _emitBulkProgress({
    required List<String> activityFacilityIds,
    required Emitter<AssetSubmissionState> emit,
  }) async {
    final jobs = await _isar.cacheSubmissionJobs
        .where()
        .anyOf(
            activityFacilityIds, (q, pid) => q.activityFacilityIdEqualTo(pid))
        .findAll();

    final total = activityFacilityIds.length;
    final successes = jobs.where((j) => j.status == 'success').length;
    final anyFailed = jobs.any((j) => j.status == 'failed');
    final anySessionExpired = jobs
        .any((j) => j.status == 'failed' && _isSessionExpiredMessage(j.error));

    final anyRunningOrQueued =
        jobs.any((j) => j.status == 'running' || j.status == 'queued');

    if (!emit.isDone) {
      emit(AssetSubmissionState.progress(completed: successes, total: total));
    }

    if (!anyRunningOrQueued) {
      await BackgroundServiceController.I.stopNow();

      if (anyFailed) {
        if (!emit.isDone) {
          emit(AssetSubmissionState.failure(anySessionExpired
              ? 'SESSION_EXPIRED'
              : 'Some submissions failed.'));
        }
      } else {
        if (!emit.isDone) {
          emit(const AssetSubmissionState.success());
        }
      }

      await _bulkJobsSub?.cancel();
      _bulkJobsSub = null;

      _isBatchMode = false;
      _batchIds = const [];
    }
  }

  Future<void> _onSubmitAll(
    _SubmitAll event,
    Emitter<AssetSubmissionState> emit,
  ) =>
      _handleSubmit(
        activityFacilityId: event.activityFacilityId,
        facilityId: event.facilityId,
        userType: event.userType,
        emit: emit,
        fromDraft: false,
      );

  Future<bool> _handleSubmit({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
    required Emitter<AssetSubmissionState> emit,
    required bool fromDraft,
  }) async {
    _isBatchMode = false;
    _batchIds = const [];

    emit(const AssetSubmissionState.loading());
    _activeSingleActivityFacilityId = fromDraft ? null : activityFacilityId;

    await _writeJobStatusUI(
        activityFacilityId: activityFacilityId, status: 'queued');

    AppLogger.instance.info(
        '[BLoC] single submit firing for $activityFacilityId | _isBatchMode=$_isBatchMode | _activeSingleActivityFacilityId=$_activeSingleActivityFacilityId');

    await BackgroundServiceController.I.enqueueSubmission(
      activityFacilityId: activityFacilityId,
      facilityId: facilityId,
      userType: userType,
      fromDraft: fromDraft,
    );

    if (!fromDraft) {
      await _jobSub?.cancel();
      _jobSub = _isar.cacheSubmissionJobs
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .watch(fireImmediately: true)
          .listen((rows) async {
        if (rows.isEmpty) return;
        final job = rows.first;

        switch (job.status) {
          case 'running':
            break;

          case 'success':
            if (!emit.isDone) emit(const AssetSubmissionState.success());
            _activeSingleActivityFacilityId = null;
            await _jobSub?.cancel();
            _jobSub = null;
            await BackgroundServiceController.I.stopNow();
            break;

          case 'failed':
            if (!emit.isDone) {
              emit(AssetSubmissionState.failure(
                  _normalizeErrorMessage(job.error)));
            }
            _activeSingleActivityFacilityId = null;
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
    String activityFacilityId,
    String? message,
    Emitter<AssetSubmissionState> emit,
  ) async {
    final normalized = _normalizeErrorMessage(message);
    await _writeJobStatusUI(
      activityFacilityId: activityFacilityId,
      status: 'failed',
      error: normalized,
    );

    if (!_isBatchMode &&
        _activeSingleActivityFacilityId != null &&
        _activeSingleActivityFacilityId == activityFacilityId) {
      emit(AssetSubmissionState.failure(normalized));
      _activeSingleActivityFacilityId = null;
      await BackgroundServiceController.I.stopNow();
      return;
    }

    AppLogger.instance.info('[BLoC] _handleSvcError -> batch emit failure');
    if (!emit.isDone) {
      emit(AssetSubmissionState.failure(normalized));
    }
    await BackgroundServiceController.I.stopNow();
  }

  Future<void> _handleSvcDone(
    String activityFacilityId,
    Emitter<AssetSubmissionState> emit,
  ) async {
    await _writeJobStatusUI(
      activityFacilityId: activityFacilityId,
      status: 'success',
    );

    if (!_isBatchMode) {
      AppLogger.instance.info(
          '[BLoC] _handleSvcDone -> single emit success (force by !_isBatchMode)');
      if (!emit.isDone) {
        emit(const AssetSubmissionState.success());
      }
      _activeSingleActivityFacilityId = null;
      await BackgroundServiceController.I.stopNow();
      return;
    }
    AppLogger.instance
        .info('[BLoC] _handleSvcDone -> batch (no immediate emit)');
  }

  bool _isSessionExpiredMessage(String? message) {
    final msg = (message ?? '').toLowerCase();
    return msg.contains('session_expired');
  }

  String _normalizeErrorMessage(String? message) {
    if (_isSessionExpiredMessage(message)) return 'SESSION_EXPIRED';
    final m = (message ?? '').trim();
    return m.isEmpty ? 'Failed.' : m;
  }

  Future<void> _writeJobStatusUI({
    required String activityFacilityId,
    required String status,
    String? error,
  }) async {
    await _isar.writeTxn(() async {
      final existing = await _isar.cacheSubmissionJobs
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findFirst();

      if (existing == null) {
        await _isar.cacheSubmissionJobs.put(
          CacheSubmissionJob(
              activityFacilityId: activityFacilityId,
              status: status,
              error: error),
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

@freezed
class AssetSubmissionEvent with _$AssetSubmissionEvent {
  const factory AssetSubmissionEvent.submitAll({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
  }) = _SubmitAll;

  const factory AssetSubmissionEvent.submitAllDrafts({
    required String userType,
  }) = _SubmitAllDrafts;

  const factory AssetSubmissionEvent.svcError({
    required String activityFacilityId,
    String? message,
  }) = _SvcError;

  const factory AssetSubmissionEvent.svcDone({
    required String activityFacilityId,
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
