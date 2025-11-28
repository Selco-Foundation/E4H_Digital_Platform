import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:selco/utils/utils.dart';

import '../../model/document/document.dart';
import '../../model/scheduled_visit/scheduled_visit.dart';
import '../../repositories/scheduled_visit_repo.dart';

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

  Future<void> _onResend(
    AmcOtpEventResend event,
    Emitter<AmcOtpState> emit,
  ) async {
    emit(const AmcOtpState.resendLoading());

    try {
      await scheduledVisitRepo.resendVisitOtp();
      emit(const AmcOtpState.resendSuccess());
    } catch (e) {
      emit(
        const AmcOtpState.failure('Failed to resend OTP. Please try again.'),
      );
    }
  }

  Future<void> _onSubmit(
    AmcOtpEventSubmit event,
    Emitter<AmcOtpState> emit,
  ) async {
    emit(const AmcOtpState.submitLoading());

    try {
      // input workflowDocument from either local which is from
      final localScheduleVisitRepo = ScheduledVisitRepository(isar);
      final cachedForm =
          await localScheduleVisitRepo.getCacheAmcInstallationForm(
        scheduledVisitId: event.visitId,
        userType: USER_TYPES.AMC.name,
      );

      List<Document>? workflowDocuments;
      if (cachedForm != null) {
        final fileStoreId = await getFilestoreUrl(cachedForm.filePath);
        final doc = Document(
            documentType: 'AMC_INSTALLATION_FORM',
            fileStore: fileStoreId,
            documentUid:
                'AMC-FORM-${event.visitId}-${DateTime.now().millisecondsSinceEpoch}',
            geoLocation: GeoLocation(
                latitude: cachedForm.latitude,
                longitude: cachedForm.longitude));

        workflowDocuments = [doc];
      } else if (event.scheduledVisit?.workflow?.documents != null &&
          event.scheduledVisit!.workflow!.documents!.isNotEmpty) {
        workflowDocuments = event.scheduledVisit!.workflow!.documents!;
      }

      await scheduledVisitRepo.updateVisitWorkflow(
        visitId: event.visitId,
        schemaCode: event.schemaCode,
        version: event.version,
        otp: event.otp,
        status: 'SUBMIT_OTP',
        responses: null,
        workflowDocuments: workflowDocuments,
        visitDocuments: null,
      );
      await PrefilledScheduledVisitRepository(isar).delete(
          scheduledVisitId: event.visitId, userType: USER_TYPES.AMC.name);
      await localScheduleVisitRepo.deleteInstallationForm(
          scheduledVisitId: event.visitId);
      emit(const AmcOtpState.submitSuccess());
    } catch (e) {
      emit(const AmcOtpState.failure(
          'Invalid OTP or request failed. Please try again.'));
    }
  }
}

@freezed
class AmcOtpEvent with _$AmcOtpEvent {
  const factory AmcOtpEvent.resend() = AmcOtpEventResend;

  const factory AmcOtpEvent.submit({
    required String visitId,
    required String schemaCode,
    required int version,
    required String otp,
    ScheduledVisit? scheduledVisit,
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
