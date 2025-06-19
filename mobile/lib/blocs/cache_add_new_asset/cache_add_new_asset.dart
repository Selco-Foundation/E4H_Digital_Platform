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
  }

  Future<void> _getCacheAssetDetail(
    CacheAddNewAssetEventGet event,
    Emitter<CacheAddNewAssetState> emit,
  ) async {
    emit(const CacheAddNewAssetState.loading());
    try {
      final entries = await isar.cacheAddNewAssets
          .where()
          .projectIdEqualTo(event.projectId)
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
        // If you want to ensure only one record per projectId+assetType:
        final existing = await isar.cacheAddNewAssets
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .findFirst();

        if (existing != null) {
          // Overwrite fields if desired
          existing.itemNumber = event.entry.itemNumber;
          existing.serialNumber = event.entry.serialNumber;
          existing.photoPath = event.entry.photoPath;
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
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .findFirst();

        if (existing != null) {
          existing.itemNumber = event.entry.itemNumber;
          existing.serialNumber = event.entry.serialNumber;
          existing.photoPath = event.entry.photoPath;
          existing.longitude = event.entry.longitude;
          existing.latitude = event.entry.latitude;
          existing.updatedAt = DateTime.now();
          await isar.cacheAddNewAssets.put(existing);
        } else {
          final newEntry = CacheAddNewAsset(
            projectId: event.entry.projectId,
            assetType: event.entry.assetType,
            itemNumber: event.entry.itemNumber,
            serialNumber: event.entry.serialNumber,
            photoPath: event.entry.photoPath,
            latitude: event.entry.latitude,
            longitude: event.entry.longitude,
          );
          await isar.cacheAddNewAssets.put(newEntry);
        }
      });

      final updatedEntry = await isar.cacheAddNewAssets
          .where()
          .projectIdEqualTo(event.entry.projectId)
          .filter()
          .assetTypeEqualTo(event.entry.assetType)
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
}

/// Events for CacheAddNewAssetBloc
@freezed
class CacheAddNewAssetEvent with _$CacheAddNewAssetEvent {
  const factory CacheAddNewAssetEvent.get(
    String projectId,
    String assetType,
  ) = CacheAddNewAssetEventGet;

  const factory CacheAddNewAssetEvent.add(CacheAddNewAsset entry) =
      CacheAddNewAssetEventAdd;
  const factory CacheAddNewAssetEvent.update(CacheAddNewAsset entry) =
      CacheAddNewAssetEventUpdate;
  const factory CacheAddNewAssetEvent.delete(int id) =
      CacheAddNewAssetEventDelete;
}

/// States for CacheAddNewAssetBloc
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
