import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_asset_detail.dart';

part 'cache_asset_detail.freezed.dart';

class CacheAssetDetailBloc
    extends Bloc<CacheAssetDetailEvent, CacheAssetDetailState> {
  final Isar isar;

  CacheAssetDetailBloc(this.isar)
      : super(const CacheAssetDetailState.initial()) {
    on<CacheAssetDetailEventGet>(_getCacheAssetDetail);
    on<CacheAssetDetailEventAdd>(_addCacheAssetDetail);
    on<CacheAssetDetailEventUpdate>(_updateCacheAssetDetail);
    on<CacheAssetDetailEventDelete>(_deleteCacheAssetDetail);
  }

  Future<void> _getCacheAssetDetail(
    CacheAssetDetailEventGet event,
    Emitter<CacheAssetDetailState> emit,
  ) async {
    emit(const CacheAssetDetailState.loading());
    try {
      final entries = await isar.cacheAssetDetails
          .where()
          .projectIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo(event.assetType)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheAssetDetailState.notFound());
      } else {
        emit(CacheAssetDetailState.loaded(entries));
      }
    } catch (e) {
      emit(CacheAssetDetailState.error(e.toString()));
    }
  }

  Future<void> _addCacheAssetDetail(
    CacheAssetDetailEventAdd event,
    Emitter<CacheAssetDetailState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAssetDetails
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .findFirst();

        if (existing != null) {
          existing.warranty = event.entry.warranty;
          existing.brand = event.entry.brand;
          existing.model = event.entry.model;
          existing.updatedAt = DateTime.now();
          await isar.cacheAssetDetails.put(existing);
        } else {
          await isar.cacheAssetDetails.put(event.entry);
        }
      });

      emit(CacheAssetDetailState.added(event.entry));
    } catch (e) {
      emit(CacheAssetDetailState.error(e.toString()));
    }
  }

  Future<void> _updateCacheAssetDetail(
    CacheAssetDetailEventUpdate event,
    Emitter<CacheAssetDetailState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAssetDetails
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .findFirst();

        if (existing != null) {
          existing.warranty = event.entry.warranty;
          existing.brand = event.entry.brand;
          existing.model = event.entry.model;
          existing.updatedAt = DateTime.now();
          await isar.cacheAssetDetails.put(existing);
        } else {
          final newEntry = CacheAssetDetail(
            projectId: event.entry.projectId,
            assetType: event.entry.assetType,
            warranty: event.entry.warranty,
            brand: event.entry.brand,
            model: event.entry.model,
          );
          await isar.cacheAssetDetails.put(newEntry);
        }
      });

      // Emit the newly updated/added entry
      final updatedEntry = await isar.cacheAssetDetails
          .where()
          .projectIdEqualTo(event.entry.projectId)
          .filter()
          .assetTypeEqualTo(event.entry.assetType)
          .findFirst();

      if (updatedEntry != null) {
        emit(CacheAssetDetailState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheAssetDetailState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheAssetDetail(
    CacheAssetDetailEventDelete event,
    Emitter<CacheAssetDetailState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheAssetDetails.delete(event.id);
      });
      emit(const CacheAssetDetailState.deleted());
    } catch (e) {
      emit(CacheAssetDetailState.error(e.toString()));
    }
  }
}

/// Events for CacheAssetDetailBloc
@freezed
class CacheAssetDetailEvent with _$CacheAssetDetailEvent {
  const factory CacheAssetDetailEvent.get(
    String projectId,
    String assetType,
  ) = CacheAssetDetailEventGet;

  const factory CacheAssetDetailEvent.add(CacheAssetDetail entry) =
      CacheAssetDetailEventAdd;

  const factory CacheAssetDetailEvent.update(CacheAssetDetail entry) =
      CacheAssetDetailEventUpdate;

  const factory CacheAssetDetailEvent.delete(int id) =
      CacheAssetDetailEventDelete;
}

/// States for CacheAssetDetailBloc
@freezed
class CacheAssetDetailState with _$CacheAssetDetailState {
  const factory CacheAssetDetailState.initial() = _Initial;
  const factory CacheAssetDetailState.loading() = _Loading;
  const factory CacheAssetDetailState.loaded(List<CacheAssetDetail> entries) =
      _Loaded;
  const factory CacheAssetDetailState.added(CacheAssetDetail entry) = _Added;
  const factory CacheAssetDetailState.updated(CacheAssetDetail entry) =
      _Updated;
  const factory CacheAssetDetailState.deleted() = _Deleted;
  const factory CacheAssetDetailState.notFound() = _NotFound;
  const factory CacheAssetDetailState.error(String message) = _Error;
}
