import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/transaction/transaction.dart';
import '../../repositories/assetRepo.dart';

part 'asset_rejection.freezed.dart';

class RejectionBloc extends Bloc<RejectionEvent, RejectionState> {
  final AssetRepository _repo;

  RejectionBloc({AssetRepository? repo})
      : _repo = repo ?? AssetRepository(),
        super(const RejectionState.initial()) {
    on<_SubmitRejection>(_onSubmitRejection);
  }

  Future<void> _onSubmitRejection(
    _SubmitRejection event,
    Emitter<RejectionState> emit,
  ) async {
    emit(const RejectionState.loading());
    try {
      await _repo.submitRejection(
        projectId: event.projectId,
        action: event.action ?? '',
        transactions: event.transactions,
      );
      emit(const RejectionState.success());
    } catch (e) {
      emit(RejectionState.failure(e.toString()));
    }
  }
}

@freezed
class RejectionEvent with _$RejectionEvent {
  const factory RejectionEvent.submitRejection({
    required String projectId,
    String? action,
    required List<Transaction> transactions,
  }) = _SubmitRejection;
}

@freezed
class RejectionState with _$RejectionState {
  const factory RejectionState.initial() = _Initial;
  const factory RejectionState.loading() = _Loading;
  const factory RejectionState.success() = _Success;
  const factory RejectionState.failure(String message) = _Failure;
}
