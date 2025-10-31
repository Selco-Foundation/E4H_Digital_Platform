import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_submission_job.dart';
import '../../utils/background_service.dart'
    show BackgroundServiceController, kEvtRejectDone, kEvtRejectError;

part 'asset_rejection.freezed.dart';

StreamSubscription? _rejDoneSub;
StreamSubscription? _rejErrSub;

class RejectionBloc extends Bloc<RejectionEvent, RejectionState> {
  final Isar _isar;

  RejectionBloc(this._isar) : super(const RejectionState.initial()) {
    on<_SubmitRejection>(_onSubmitRejection);
    on<_BgRejectDone>(_onBgDone);
    on<_BgRejectError>(_onBgError);

    final svc = FlutterBackgroundService();

    _rejDoneSub?.cancel();
    _rejDoneSub = svc.on(kEvtRejectDone).listen((data) {
      final aFid = data?['activityFacilityId'] as String?;
      if (aFid != null) {
        add(RejectionEvent.bgRejectDone(activityFacilityId: aFid));
      }
    });

    _rejErrSub?.cancel();
    _rejErrSub = svc.on(kEvtRejectError).listen((data) {
      final aFid = data?['activityFacilityId'] as String?;
      final msg = data?['message']?.toString();
      if (aFid != null) {
        add(RejectionEvent.bgRejectError(
            activityFacilityId: aFid, message: msg));
      }
    });
  }

  @override
  Future<void> close() async {
    await _rejDoneSub?.cancel();
    await _rejErrSub?.cancel();
    return super.close();
  }

  // ------------------------------------------------------------
  // Submit rejection: enqueue a background job and wait for BG events
  // ------------------------------------------------------------
  Future<void> _onSubmitRejection(
    _SubmitRejection event,
    Emitter<RejectionState> emit,
  ) async {
    emit(const RejectionState.loading());

    try {
      // Serialize transactions for the background isolate:
      // Accepts List<Map<String,dynamic>> or objects with toJson()/toMap()
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

      // (Optional) reflect a 'queued' status locally so any watchers can show progress
      await _writeJobStatusUI(
        activityFacilityId: event.activityFacilityId,
        status: 'queued',
      );

      await BackgroundServiceController.I.enqueueRejection(
        activityFacilityId: event.activityFacilityId,
        userType: event.userType,
        transactions: txMaps,
      );

      // Do NOT emit success here; we wait for kEvtRejectDone / kEvtRejectError
    } catch (e) {
      emit(RejectionState.failure(e.toString()));
    }
  }

  // ------------------------------------------------------------
  // BG -> success
  // ------------------------------------------------------------
  Future<void> _onBgDone(
    _BgRejectDone event,
    Emitter<RejectionState> emit,
  ) async {
    // Mirror to Isar so any UI watchers (if you have) will advance
    await _writeJobStatusUI(
        activityFacilityId: event.activityFacilityId, status: 'success');
    emit(const RejectionState.success());
  }

  // ------------------------------------------------------------
  // BG -> error
  // ------------------------------------------------------------
  Future<void> _onBgError(
    _BgRejectError event,
    Emitter<RejectionState> emit,
  ) async {
    await _writeJobStatusUI(
      activityFacilityId: event.activityFacilityId,
      status: 'failed',
      error: event.message,
    );
    emit(RejectionState.failure(event.message ?? 'Failed to reject.'));
  }

  // ---- Small helper to touch the same CacheSubmissionJob Isar used elsewhere ----
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
        await _isar.cacheSubmissionJobs.put(CacheSubmissionJob(
            activityFacilityId: activityFacilityId,
            status: status,
            error: error));
      } else {
        existing
          ..status = status
          ..error = error;
        await _isar.cacheSubmissionJobs.put(existing);
      }
    });
  }
}

// ======================= Freezed =========================
@freezed
class RejectionEvent with _$RejectionEvent {
  /// Kick off a background rejection job.
  /// `transactions` can be List<Map<String,dynamic>> or objects with toJson()/toMap().
  const factory RejectionEvent.submitRejection({
    required String activityFacilityId,
    required String userType,
    required List<dynamic> transactions,
  }) = _SubmitRejection;

  /// Emitted internally when BG signals success
  const factory RejectionEvent.bgRejectDone({
    required String activityFacilityId,
  }) = _BgRejectDone;

  /// Emitted internally when BG signals error
  const factory RejectionEvent.bgRejectError({
    required String activityFacilityId,
    String? message,
  }) = _BgRejectError;
}

@freezed
class RejectionState with _$RejectionState {
  const factory RejectionState.initial() = _Initial;
  const factory RejectionState.loading() = _Loading;
  const factory RejectionState.success() = _Success;
  const factory RejectionState.failure(String errorMessage) = _Failure;
}
