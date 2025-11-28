import 'dart:async';

import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../utils/background_service.dart'
    show
        BackgroundServiceController,
        kEvtScheduleVisitDone,
        kEvtScheduleVisitError;

part 'scheduled_visit_submission.freezed.dart';

class ScheduleVisitSubmitBloc
    extends Bloc<ScheduleVisitSubmitEvent, ScheduleVisitSubmitState> {
  StreamSubscription? _doneSub;
  StreamSubscription? _errSub;

  ScheduleVisitSubmitBloc() : super(const ScheduleVisitSubmitState.initial()) {
    on<_Submit>(_onSubmit);
    on<_BgDone>(_onBgDone);
    on<_BgError>(_onBgError);

    final svc = FlutterBackgroundService();

    _doneSub?.cancel();
    _doneSub = svc.on(kEvtScheduleVisitDone).listen((data) {
      final visitId = data?['scheduledVisitId'] as String?;
      AppLogger.instance.info(
        '[ScheduleVisitSubmitBloc] kEvtScheduleVisitDone visitId=$visitId',
      );
      if (visitId != null) {
        add(ScheduleVisitSubmitEvent.bgDone(scheduledVisitId: visitId));
      }
    });

    _errSub?.cancel();
    _errSub = svc.on(kEvtScheduleVisitError).listen((data) {
      final visitId = data?['scheduledVisitId'] as String?;
      final msg = data?['message'] as String?;
      AppLogger.instance.info(
        '[ScheduleVisitSubmitBloc] kEvtScheduleVisitError visitId=$visitId msg=$msg',
      );
      if (visitId != null) {
        add(
          ScheduleVisitSubmitEvent.bgError(
            scheduledVisitId: visitId,
            message: msg,
          ),
        );
      }
    });
  }

  @override
  Future<void> close() async {
    await _doneSub?.cancel();
    await _errSub?.cancel();
    return super.close();
  }

  Future<void> _onSubmit(
    _Submit event,
    Emitter<ScheduleVisitSubmitState> emit,
  ) async {
    emit(const ScheduleVisitSubmitState.loading());

    try {
      await BackgroundServiceController.I.enqueueScheduleVisitSubmission(
        scheduledVisitId: event.scheduledVisitId,
        userType: event.userType,
      );
    } catch (e, st) {
      AppLogger.instance.error(
          title:
              '[ScheduleVisitSubmitBloc] enqueueScheduleVisitSubmission failed',
          message: e.toString(),
          stackTrace: st);
      await BackgroundServiceController.I.stopNow();
      emit(
        const ScheduleVisitSubmitState.failure(
          'Failed to submit visit. Please try again.',
        ),
      );
    }
  }

  Future<void> _onBgDone(
    _BgDone event,
    Emitter<ScheduleVisitSubmitState> emit,
  ) async {
    await BackgroundServiceController.I.stopNow();
    emit(const ScheduleVisitSubmitState.success());
  }

  Future<void> _onBgError(
    _BgError event,
    Emitter<ScheduleVisitSubmitState> emit,
  ) async {
    await BackgroundServiceController.I.stopNow();
    emit(
      ScheduleVisitSubmitState.failure(
          event.message ?? 'Failed to submit visit.'),
    );
  }
}

@freezed
class ScheduleVisitSubmitEvent with _$ScheduleVisitSubmitEvent {
  const factory ScheduleVisitSubmitEvent.submit({
    required String scheduledVisitId,
    required String userType,
  }) = _Submit;

  const factory ScheduleVisitSubmitEvent.bgDone({
    required String scheduledVisitId,
  }) = _BgDone;

  const factory ScheduleVisitSubmitEvent.bgError({
    required String scheduledVisitId,
    String? message,
  }) = _BgError;
}

@freezed
class ScheduleVisitSubmitState with _$ScheduleVisitSubmitState {
  const factory ScheduleVisitSubmitState.initial() = _Initial;
  const factory ScheduleVisitSubmitState.loading() = _Loading;
  const factory ScheduleVisitSubmitState.success() = _Success;
  const factory ScheduleVisitSubmitState.failure(String message) = _Failure;
}
