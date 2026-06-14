import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_asset_handover_document.dart';
import '../../repositories/asset_handover_document_repo.dart';

part 'cache_asset_handover_document.freezed.dart';

@freezed
class CacheAssetHandoverDocumentEvent with _$CacheAssetHandoverDocumentEvent {
  const factory CacheAssetHandoverDocumentEvent.load(
    String activityFacilityId,
  ) = _Load;

  const factory CacheAssetHandoverDocumentEvent.replaceAllForProject({
    required String activityFacilityId,
    required List<AssetHandoverDocumentInput> files,
  }) = _ReplaceAllForProject;

  const factory CacheAssetHandoverDocumentEvent.clearProject(
    String activityFacilityId,
  ) = _ClearProject;
}

@freezed
class CacheAssetHandoverDocumentState with _$CacheAssetHandoverDocumentState {
  const factory CacheAssetHandoverDocumentState.initial() = _Initial;
  const factory CacheAssetHandoverDocumentState.loading() = _Loading;
  const factory CacheAssetHandoverDocumentState.loaded(
    List<CacheAssetHandoverDocument> files,
  ) = _Loaded;
  const factory CacheAssetHandoverDocumentState.saved() = _Saved;
  const factory CacheAssetHandoverDocumentState.notFound() = _NotFound;
  const factory CacheAssetHandoverDocumentState.error(
    String message,
  ) = _Error;
}

class CacheAssetHandoverDocumentBloc extends Bloc<
    CacheAssetHandoverDocumentEvent, CacheAssetHandoverDocumentState> {
  CacheAssetHandoverDocumentBloc(Isar isar)
      : _repository = AssetHandoverDocumentRepository(isar),
        super(const CacheAssetHandoverDocumentState.initial()) {
    on<_Load>(_onLoad);
    on<_ReplaceAllForProject>(_onReplaceAllForProject);
    on<_ClearProject>(_onClearProject);
  }

  final AssetHandoverDocumentRepository _repository;

  Future<void> _onLoad(
    _Load event,
    Emitter<CacheAssetHandoverDocumentState> emit,
  ) async {
    emit(const CacheAssetHandoverDocumentState.loading());
    try {
      final files = await _repository.getCachedFiles(
        activityFacilityId: event.activityFacilityId,
      );
      if (files.isEmpty) {
        emit(const CacheAssetHandoverDocumentState.notFound());
      } else {
        emit(CacheAssetHandoverDocumentState.loaded(files));
      }
    } catch (e) {
      emit(CacheAssetHandoverDocumentState.error(e.toString()));
    }
  }

  Future<void> _onReplaceAllForProject(
    _ReplaceAllForProject event,
    Emitter<CacheAssetHandoverDocumentState> emit,
  ) async {
    emit(const CacheAssetHandoverDocumentState.loading());
    try {
      await _repository.replaceAllForProject(
        activityFacilityId: event.activityFacilityId,
        files: event.files,
      );
      emit(const CacheAssetHandoverDocumentState.saved());
    } catch (e) {
      emit(CacheAssetHandoverDocumentState.error(e.toString()));
    }
  }

  Future<void> _onClearProject(
    _ClearProject event,
    Emitter<CacheAssetHandoverDocumentState> emit,
  ) async {
    emit(const CacheAssetHandoverDocumentState.loading());
    try {
      await _repository.clearProject(
        activityFacilityId: event.activityFacilityId,
      );
      emit(const CacheAssetHandoverDocumentState.notFound());
    } catch (e) {
      emit(CacheAssetHandoverDocumentState.error(e.toString()));
    }
  }
}
