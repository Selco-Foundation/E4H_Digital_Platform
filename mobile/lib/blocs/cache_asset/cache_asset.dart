import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../repositories/asset_repo.dart';

part 'cache_asset.freezed.dart';

class CacheAssetBloc extends Bloc<CacheAssetEvent, CacheAssetState> {
  final AssetRepository _repo;
  final Isar isar;

  CacheAssetBloc(this.isar)
      : _repo = AssetRepository(),
        super(const CacheAssetState.initial()) {
    on<_StartSync>(_onStartSync);
  }

  Future<void> _onStartSync(
    _StartSync event,
    Emitter<CacheAssetState> emit,
  ) async {
    emit(const CacheAssetState.loading());
    try {
      await _repo.syncRemoteToLocal(
          activityFacility: event.project,
          activityFacilityId: event.projectId,
          isar: isar,
          userType: event.userType);
      emit(const CacheAssetState.success());
    } catch (e) {
      AppLogger.instance.info(e.toString());
      emit(CacheAssetState.failure(e.toString()));
    }
  }
}

@freezed
class CacheAssetEvent with _$CacheAssetEvent {
  const factory CacheAssetEvent.start(
          String projectId, String userType, ActivityFacilityWorkflow project) =
      _StartSync;
}

@freezed
class CacheAssetState with _$CacheAssetState {
  const factory CacheAssetState.initial() = _Initial;
  const factory CacheAssetState.loading() = _Loading;
  const factory CacheAssetState.success() = _Success;
  const factory CacheAssetState.failure(String error) = _Failure;
}
