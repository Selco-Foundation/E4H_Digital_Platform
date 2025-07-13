import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../repositories/assetRepo.dart';

part 'cache_asset.freezed.dart';

/// BLOC
class CacheAssetBloc extends Bloc<CacheAssetEvent, CacheAssetState> {
  final AssetRepository _repo;
  final Isar _isar;

  CacheAssetBloc(this._isar)
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
          projectId: event.projectId, isar: _isar, userType: event.userType);
      emit(const CacheAssetState.success());
    } catch (e) {
      print(e.toString());
      emit(CacheAssetState.failure(e.toString()));
    }
  }
}

/// EVENTS
@freezed
class CacheAssetEvent with _$CacheAssetEvent {
  /// Start a full “remote → local” sync for [projectId]
  const factory CacheAssetEvent.start(String projectId, String userType) =
      _StartSync;
}

/// STATES
@freezed
class CacheAssetState with _$CacheAssetState {
  const factory CacheAssetState.initial() = _Initial;
  const factory CacheAssetState.loading() = _Loading;
  const factory CacheAssetState.success() = _Success;
  const factory CacheAssetState.failure(String error) = _Failure;
}
