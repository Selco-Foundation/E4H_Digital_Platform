import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_add_new_asset.dart';

part 'cache_add_new_asset.freezed.dart';

class CacheAddNewAssetBloc
    extends Bloc<CacheAddNewAssetEvent, CacheAddNewAssetState> {
  final Isar isar;

  CacheAddNewAssetBloc(this.isar)
      : super(const CacheAddNewAssetState.initial()) {
    on<CacheAddNewAssetEventGet>(_getCacheAssetDetail);
    on<CacheAddNewAssetEventAdd>(_addCacheAssetDetail);
    on<CacheAddNewAssetEventUpdate>(_updateCacheAssetDetail);
    on<CacheAddNewAssetEventDelete>(_deleteCacheAssetDetail);
    on<CacheAddNewAssetEventDeleteAll>(_deleteAllCacheAssetDetail);
    on<CacheAddNewAssetEventReplaceAll>(_replaceAllCacheAssetDetail);
  }

  Future<void> _getCacheAssetDetail(
    CacheAddNewAssetEventGet event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    emit(const CacheAddNewAssetState.loading());
    try {
      final entries = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(event.activityFacilityId)
          .filter()
          .assetTypeEqualTo(event.assetType)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheAddNewAssetState.notFound());
      } else {
        emit(CacheAddNewAssetState.loaded(entries));
      }
    } catch (e) {
      emit(CacheAddNewAssetState.error(e.toString()));
    }
  }

  Future<void> _addCacheAssetDetail(
    CacheAddNewAssetEventAdd event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAddNewAssets
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .serialNumberEqualTo(event.entry.serialNumber)
            .findFirst();

        if (existing != null) {
          existing.documentId = event.entry.documentId;
          existing.assetId = event.entry.assetId;
          existing.itemNumber = event.entry.itemNumber;
          existing.photoPath = event.entry.photoPath;
          existing.longitude = event.entry.longitude;
          existing.latitude = event.entry.latitude;
          existing.documentType = "ASSET";
          existing.updatedAt = DateTime.now();
          await isar.cacheAddNewAssets.put(existing);
        } else {
          await isar.cacheAddNewAssets.put(event.entry);
        }
      });
      emit(CacheAddNewAssetState.added(event.entry));
    } catch (e) {
      emit(CacheAddNewAssetState.error(e.toString()));
    }
  }

  Future<void> _updateCacheAssetDetail(
    CacheAddNewAssetEventUpdate event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAddNewAssets
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .serialNumberEqualTo(event.entry.serialNumber)
            .findFirst();

        if (existing != null) {
          existing.itemNumber = event.entry.itemNumber;
          existing.documentId = event.entry.documentId;
          existing.documentType = "ASSET";
          existing.photoPath = event.entry.photoPath;
          existing.longitude = event.entry.longitude;
          existing.latitude = event.entry.latitude;
          existing.updatedAt = DateTime.now();
          await isar.cacheAddNewAssets.put(existing);
        } else {
          await isar.cacheAddNewAssets.put(event.entry);
        }
      });

      final updatedEntry = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(event.entry.activityFacilityId)
          .filter()
          .assetTypeEqualTo(event.entry.assetType)
          .serialNumberEqualTo(event.entry.serialNumber)
          .findFirst();

      if (updatedEntry != null) {
        emit(CacheAddNewAssetState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheAddNewAssetState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheAssetDetail(
    CacheAddNewAssetEventDelete event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheAddNewAssets.delete(event.id);
      });
      emit(const CacheAddNewAssetState.deleted());
    } catch (e) {
      emit(CacheAddNewAssetState.error(e.toString()));
    }
  }

  Future<void> _deleteAllCacheAssetDetail(
    CacheAddNewAssetEventDeleteAll event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    emit(const CacheAddNewAssetState.loading());
    try {
      await isar.writeTxn(() async {
        await isar.cacheAddNewAssets
            .where()
            .activityFacilityIdEqualTo(event.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.assetType)
            .deleteAll();
      });
      emit(const CacheAddNewAssetState.deleted());
    } catch (e) {
      emit(CacheAddNewAssetState.error(e.toString()));
    }
  }

  Future<void> _replaceAllCacheAssetDetail(
    CacheAddNewAssetEventReplaceAll event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    emit(const CacheAddNewAssetState.loading());
    try {
      await isar.writeTxn(() async {
        await isar.cacheAddNewAssets
            .where()
            .activityFacilityIdEqualTo(event.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.assetType)
            .deleteAll();

        await isar.cacheAddNewAssets.putAll(event.entries);
      });

      emit(CacheAddNewAssetState.loaded(event.entries));
    } catch (e) {
      emit(CacheAddNewAssetState.error(e.toString()));
    }
  }
}

@freezed
class CacheAddNewAssetEvent with _$CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEvent.get(
    String activityFacilityId,
    String assetType,
  ) = CacheAddNewAssetEventGet;

  const factory CacheAddNewAssetEvent.add(CacheAddNewAsset entry) =
      CacheAddNewAssetEventAdd;
  const factory CacheAddNewAssetEvent.update(CacheAddNewAsset entry) =
      CacheAddNewAssetEventUpdate;
  const factory CacheAddNewAssetEvent.delete(int id) =
      CacheAddNewAssetEventDelete;
  const factory CacheAddNewAssetEvent.deleteAll(
    String activityFacilityId,
    String assetType,
  ) = CacheAddNewAssetEventDeleteAll;
  const factory CacheAddNewAssetEvent.replaceAll(
    String activityFacilityId,
    String assetType,
    List<CacheAddNewAsset> entries,
  ) = CacheAddNewAssetEventReplaceAll;
}

@freezed
class CacheAddNewAssetState with _$CacheAddNewAssetState {
  const factory CacheAddNewAssetState.initial() = _Initial;
  const factory CacheAddNewAssetState.loading() = _Loading;
  const factory CacheAddNewAssetState.loaded(List<CacheAddNewAsset> entries) =
      _Loaded;
  const factory CacheAddNewAssetState.added(CacheAddNewAsset entry) = _Added;
  const factory CacheAddNewAssetState.updated(CacheAddNewAsset entry) =
      _Updated;
  const factory CacheAddNewAssetState.deleted() = _Deleted;
  const factory CacheAddNewAssetState.notFound() = _NotFound;
  const factory CacheAddNewAssetState.error(String message) = _Error;
}
