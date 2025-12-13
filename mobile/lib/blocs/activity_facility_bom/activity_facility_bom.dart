import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../model/document/document.dart';
import '../../repositories/activity_facility_repo.dart';
import '../../repositories/dynamic_form_repo.dart';
import '../../utils/utils.dart';

part 'activity_facility_bom.freezed.dart';

@freezed
class ActivityFacilityBomEvent with _$ActivityFacilityBomEvent {
  const factory ActivityFacilityBomEvent.syncIfNeeded({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
  }) = _SyncIfNeeded;

  const factory ActivityFacilityBomEvent.forceSync({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
  }) = _ForceSync;
  const factory ActivityFacilityBomEvent.downloadWorkflowDocument({
    required String activityFacilityId,
    required String userType,
    required List<Document> workflowDocuments,
    required String docType,
  }) = _DownloadWorkflowDocument;
}

@freezed
class ActivityFacilityBomState with _$ActivityFacilityBomState {
  const factory ActivityFacilityBomState.initial() = _Initial;
  const factory ActivityFacilityBomState.loading() = _Loading;
  const factory ActivityFacilityBomState.success({
    required bool savedBomValues,
  }) = _Success;
  const factory ActivityFacilityBomState.failure(String message) = _Failure;
  const factory ActivityFacilityBomState.documentDownloadInProgress() =
      _DocDownloadInProgress;
  const factory ActivityFacilityBomState.documentDownloadSuccess(File file) =
      _DocDownloadSuccess;
  const factory ActivityFacilityBomState.documentDownloadFailure(String error) =
      _DocDownloadFailure;
}

class ActivityFacilityBomBloc
    extends Bloc<ActivityFacilityBomEvent, ActivityFacilityBomState> {
  final Isar _isar;
  final BomRepository _bomRepo = BomRepository();

  ActivityFacilityBomBloc(this._isar)
      : super(const ActivityFacilityBomState.initial()) {
    on<_SyncIfNeeded>(_onSyncIfNeeded);
    on<_ForceSync>(_onForceSync);
    on<_DownloadWorkflowDocument>(_onDownloadWorkflowDocument);
  }

  Future<void> _onSyncIfNeeded(
    _SyncIfNeeded event,
    Emitter<ActivityFacilityBomState> emit,
  ) async {
    emit(const ActivityFacilityBomState.loading());
    try {
      final isPrefilled = await _isPrefilledActivityFacility(
          _isar, event.activityFacilityId, event.userType);
      print("Got here so what? ");
      if (isPrefilled) {
        emit(const ActivityFacilityBomState.success(
          savedBomValues: false,
        ));
        return;
      }

      final r = await _bomRepo.syncBomForActivityFacility(
        isar: _isar,
        activityFacilityId: event.activityFacilityId,
        facilityId: event.facilityId,
        userType: event.userType,
      );

      emit(ActivityFacilityBomState.success(
        savedBomValues: r.savedBomValues,
      ));
    } catch (e) {
      emit(ActivityFacilityBomState.failure('$e'));
    }
  }

  Future<void> _onForceSync(
    _ForceSync event,
    Emitter<ActivityFacilityBomState> emit,
  ) async {
    emit(const ActivityFacilityBomState.loading());
    try {
      final r = await _bomRepo.syncBomForActivityFacility(
        isar: _isar,
        activityFacilityId: event.activityFacilityId,
        facilityId: event.facilityId,
        userType: event.userType,
      );
      emit(ActivityFacilityBomState.success(
        savedBomValues: r.savedBomValues,
      ));
    } catch (e) {
      emit(ActivityFacilityBomState.failure('$e'));
    }
  }

  Future<void> _onDownloadWorkflowDocument(
    _DownloadWorkflowDocument event,
    Emitter<ActivityFacilityBomState> emit,
  ) async {
    emit(const ActivityFacilityBomState.documentDownloadInProgress());
    try {
      final docs = event.workflowDocuments;
      final match = docs.firstWhere(
        (d) =>
            d.documentType == event.docType &&
            d.fileStore != null &&
            d.fileStore!.isNotEmpty,
        orElse: () =>
            throw Exception("No document of type ${event.docType} in workflow"),
      );

      final fileStoreId = match.fileStore!;
      final file = await getCachedFile(fileStoreId);
      if (file == null) {
        throw Exception(
            "Could not retrieve file from fileStoreId $fileStoreId");
      }
      emit(ActivityFacilityBomState.documentDownloadSuccess(file));
    } catch (e, st) {
      emit(ActivityFacilityBomState.documentDownloadFailure(e.toString()));
    }
  }

  Future<bool> _isPrefilledActivityFacility(
      Isar isar, String activityFacilityId, String userType) async {
    final draft = await PrefilledActivityFacilityRepository(isar).exists(
      activityFacilityId: activityFacilityId,
      userType: userType,
    );
    if (draft) {
      return true;
    } else {
      return false;
    }
  }
}
