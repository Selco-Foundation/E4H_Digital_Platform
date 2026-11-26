import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar_community/isar.dart';

import '../../data/nosql/cache_warranty_card.dart';
import '../../repositories/warranty_card_repo.dart';
import '../../utils/error_localization.dart';

part 'cache_warranty_card.freezed.dart';

@freezed
class CacheWarrantyCardEvent with _$CacheWarrantyCardEvent {
  const factory CacheWarrantyCardEvent.load(
    String activityFacilityId,
  ) = _Load;

  const factory CacheWarrantyCardEvent.replaceAllForProject({
    required String activityFacilityId,
    required List<WarrantyCardInput> files,
  }) = _ReplaceAllForProject;

  const factory CacheWarrantyCardEvent.clearProject(
    String activityFacilityId,
  ) = _ClearProject;
}

@freezed
class CacheWarrantyCardState with _$CacheWarrantyCardState {
  const factory CacheWarrantyCardState.initial() = _Initial;
  const factory CacheWarrantyCardState.loading() = _Loading;
  const factory CacheWarrantyCardState.loaded(
    List<CacheWarrantyCard> files,
  ) = _Loaded;
  const factory CacheWarrantyCardState.saved() = _Saved;
  const factory CacheWarrantyCardState.notFound() = _NotFound;
  const factory CacheWarrantyCardState.error(
    String message,
  ) = _Error;
}

class CacheWarrantyCardBloc
    extends Bloc<CacheWarrantyCardEvent, CacheWarrantyCardState> {
  CacheWarrantyCardBloc(Isar isar)
      : _repository = WarrantyCardRepository(isar),
        super(const CacheWarrantyCardState.initial()) {
    on<_Load>(_onLoad);
    on<_ReplaceAllForProject>(_onReplaceAllForProject);
    on<_ClearProject>(_onClearProject);
  }

  final WarrantyCardRepository _repository;

  Future<void> _onLoad(
    _Load event,
    Emitter<CacheWarrantyCardState> emit,
  ) async {
    emit(const CacheWarrantyCardState.loading());
    try {
      final files = await _repository.getCachedFiles(
        activityFacilityId: event.activityFacilityId,
      );
      if (files.isEmpty) {
        emit(const CacheWarrantyCardState.notFound());
      } else {
        emit(CacheWarrantyCardState.loaded(files));
      }
    } catch (e) {
      emit(CacheWarrantyCardState.error(errorMessageKey(e)));
    }
  }

  Future<void> _onReplaceAllForProject(
    _ReplaceAllForProject event,
    Emitter<CacheWarrantyCardState> emit,
  ) async {
    emit(const CacheWarrantyCardState.loading());
    try {
      await _repository.replaceAllForProject(
        activityFacilityId: event.activityFacilityId,
        files: event.files,
      );
      emit(const CacheWarrantyCardState.saved());
    } catch (e) {
      emit(CacheWarrantyCardState.error(errorMessageKey(e)));
    }
  }

  Future<void> _onClearProject(
    _ClearProject event,
    Emitter<CacheWarrantyCardState> emit,
  ) async {
    emit(const CacheWarrantyCardState.loading());
    try {
      await _repository.clearProject(
        activityFacilityId: event.activityFacilityId,
      );
      emit(const CacheWarrantyCardState.notFound());
    } catch (e) {
      emit(CacheWarrantyCardState.error(errorMessageKey(e)));
    }
  }
}
