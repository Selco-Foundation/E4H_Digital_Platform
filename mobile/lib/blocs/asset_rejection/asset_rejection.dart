import 'dart:convert';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../model/document/document.dart';
import '../../model/transaction/transaction.dart';
import '../../repositories/assetRepo.dart';
import '../../repositories/project_repo.dart';
import '../../repositories/project_workflow.dart';

part 'asset_rejection.freezed.dart';

class RejectionBloc extends Bloc<RejectionEvent, RejectionState> {
  final Isar _isar;
  final AssetRepository _repo;

  RejectionBloc(this._isar)
      : _repo = AssetRepository(),
        super(const RejectionState.initial()) {
    on<_SubmitRejection>(_onSubmitRejection);
  }

  Future<void> _onSubmitRejection(
    _SubmitRejection event,
    // String projectId,
    // String userType,
    // List<String> types,
    Emitter<RejectionState> emit,
  ) async {
    emit(const RejectionState.loading());
    try {
      const types = ['inverter', 'battery', 'panel'];
      final workflowDocuments = <Document>[];
      final workflowDocumentFromCache =
          await ProjectWorkflowRepository().collectWorkflowMediaDocs(
        isar: _isar,
        projectId: event.projectId,
        types: types,
      );

      workflowDocuments.addAll(workflowDocumentFromCache);
      print("event.transaction ${jsonEncode(event.transactions)}");
      await _repo.submitRejection(
          projectId: event.projectId,
          transactions: event.transactions,
          documents: workflowDocuments);
      await UnsubmittedProjectRepository(_isar)
          .delete(event.projectId, event.userType);
      await PrefilledProjectRepository(_isar).delete(
        projectId: event.projectId,
        userType: event.userType,
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
    required String userType,
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
