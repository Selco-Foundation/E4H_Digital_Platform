import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../repositories/dynamic_form_repo.dart';
import '../../repositories/scheduled_visit_repo.dart';
import '../../utils/app_logger.dart';
import '../../utils/utils.dart';

part 'amc_otp.freezed.dart';

class AmcOtpBloc extends Bloc<AmcOtpEvent, AmcOtpState> {
  final ScheduledVisitRemoteRepository scheduledVisitRepo;
  final Isar isar;

  AmcOtpBloc(this.isar)
      : scheduledVisitRepo = ScheduledVisitRemoteRepository(),
        super(const AmcOtpState.initial()) {
    on<AmcOtpEventResend>(_onResend);
    on<AmcOtpEventSubmit>(_onSubmit);
  }

  static const String CLEANUP_ERROR = "Cleanup Error";
  static const String ERROR_CLEARING_CACHE = "Error clearing cache for";

  Future<void> _onResend(
    AmcOtpEventResend event,
    Emitter<AmcOtpState> emit,
  ) async {
    emit(const AmcOtpState.resendLoading());

    try {
      await scheduledVisitRepo.resendVisitOtp(visitId: event.visitId);
      emit(const AmcOtpState.resendSuccess());
    } catch (e) {
      AppLogger.instance.error(
          title: "Resend AMC Service completion code", message: e.toString());
      emit(
        const AmcOtpState.failure(
            'Failed to resend AMC Service completion code. Please try again.'),
      );
    }
  }

  Future<void> _onSubmit(
    AmcOtpEventSubmit event,
    Emitter<AmcOtpState> emit,
  ) async {
    emit(const AmcOtpState.submitLoading());

    try {
      await scheduledVisitRepo.updateVisitWorkflow(
        visitId: event.visitId,
        schemaCode: event.schemaCode,
        version: event.version,
        otp: event.otp,
        status: 'SUBMIT_OTP',
        responses: null,
        visitDocuments: null,
      );
      try {
        await PrefilledScheduledVisitRepository(isar).delete(
            scheduledVisitId: event.visitId, userType: USER_TYPES.AMC.name);
      } catch (e) {
        AppLogger.instance.error(
            title: CLEANUP_ERROR,
            message: '$ERROR_CLEARING_CACHE prefilled scheduled visit.');
      }
      try {
        await AmcDynamicFormRepository()
            .delete(isar: isar, scheduledVisitId: event.visitId);
      } catch (e) {
        AppLogger.instance.error(
            title: CLEANUP_ERROR, message: '$ERROR_CLEARING_CACHE filled form');
      }
      try {
        await AmcDynamicFormRepository()
            .deleteAllLocal(isar: isar, scheduledVisitId: event.visitId);
      } catch (e) {
        AppLogger.instance.error(
            title: CLEANUP_ERROR, message: '$ERROR_CLEARING_CACHE form schema');
      }
      try {
        await ScheduledVisitRepository(isar)
            .deleteAmcMediaUploads(scheduledVisitId: event.visitId);
      } catch (e) {
        AppLogger.instance.error(
            title: CLEANUP_ERROR,
            message: '$ERROR_CLEARING_CACHE media uploads');
      }
      emit(const AmcOtpState.submitSuccess());
    } catch (e) {
      AppLogger.instance.error(
          title: "AMC AMC Service completion code Submit",
          message: e.toString());
      emit(const AmcOtpState.failure(
          'Invalid AMC Service completion code or request failed. Please try again.'));
    }
  }
}

@freezed
class AmcOtpEvent with _$AmcOtpEvent {
  const factory AmcOtpEvent.resend({
    required String visitId,
  }) = AmcOtpEventResend;

  const factory AmcOtpEvent.submit({
    required String visitId,
    required String schemaCode,
    required int version,
    required String otp,
  }) = AmcOtpEventSubmit;
}

@freezed
class AmcOtpState with _$AmcOtpState {
  const factory AmcOtpState.initial() = _Initial;
  const factory AmcOtpState.resendLoading() = _ResendLoading;
  const factory AmcOtpState.resendSuccess() = _ResendSuccess;
  const factory AmcOtpState.submitLoading() = _SubmitLoading;
  const factory AmcOtpState.submitSuccess() = _SubmitSuccess;
  const factory AmcOtpState.failure(String message) = _Failure;
}
