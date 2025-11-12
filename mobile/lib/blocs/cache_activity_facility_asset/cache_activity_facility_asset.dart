import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_activity_facility_asset.dart';

part 'cache_activity_facility_asset.freezed.dart';

class CacheActivityFacilityAssetBloc extends Bloc<
    CacheActivityFacilityAssetEvent, CacheActivityFacilityAssetState> {
  final Isar isar;

  CacheActivityFacilityAssetBloc(this.isar)
      : super(const CacheActivityFacilityAssetState.initial()) {
    on<CacheActivityFacilityAssetEventGet>(_getCacheActivityFacilityAsset);
    on<CacheActivityFacilityAssetEventAdd>(_addCacheActivityFacilityAsset);
    on<CacheActivityFacilityAssetEventUpdate>(
        _updateCacheActivityFacilityAsset);
    on<CacheActivityFacilityAssetEventDelete>(
        _deleteCacheActivityFacilityAsset);
  }

  Future<void> _getCacheActivityFacilityAsset(
      CacheActivityFacilityAssetEventGet event,
      Emitter<CacheActivityFacilityAssetState> emit) async {
    emit(const CacheActivityFacilityAssetState.loading());
    try {
      final entries = await isar.cacheActivityFacilityAssets
          .where()
          .activityFacilityIdEqualTo(event.projectId)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheActivityFacilityAssetState.notFound());
      } else {
        emit(CacheActivityFacilityAssetState.loaded(entries));
      }
    } catch (e) {
      emit(CacheActivityFacilityAssetState.error(e.toString()));
    }
  }

  Future<void> _addCacheActivityFacilityAsset(
      CacheActivityFacilityAssetEventAdd event,
      Emitter<CacheActivityFacilityAssetState> emit) async {
    try {
      var inserted = false;
      var persisted = event.entry;
      await isar.writeTxn(() async {
        final existing = await isar.cacheActivityFacilityAssets
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .findFirst();

        if (existing != null) {
          existing
            ..progress = event.entry.progress
            ..updatedAt = DateTime.now();
          await isar.cacheActivityFacilityAssets.put(existing);
          persisted = existing;
          return;
        }
        await isar.cacheActivityFacilityAssets.put(event.entry);
        inserted = true;
      });
      emit(
        inserted
            ? CacheActivityFacilityAssetState.added(persisted)
            : CacheActivityFacilityAssetState.updated(persisted),
      );
    } catch (e) {
      emit(CacheActivityFacilityAssetState.error(e.toString()));
    }
  }

  Future<void> _updateCacheActivityFacilityAsset(
    CacheActivityFacilityAssetEventUpdate event,
    Emitter<CacheActivityFacilityAssetState> emit,
  ) async {
    try {
      CacheActivityFacilityAsset? resultEntry;
      bool isUpdate = false;
      await isar.writeTxn(() async {
        final existing = await isar.cacheActivityFacilityAssets
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .findFirst();

        if (existing != null) {
          existing.progress = event.entry.progress;
          existing.updatedAt = DateTime.now();
          await isar.cacheActivityFacilityAssets.put(existing);
          resultEntry = existing;
          isUpdate = true;
        } else {
          final newEntry = CacheActivityFacilityAsset(
            activityFacilityId: event.entry.activityFacilityId,
            progress: event.entry.progress,
          );
          await isar.cacheActivityFacilityAssets.put(newEntry);
          resultEntry = newEntry;
        }
      });

      if (resultEntry != null) {
        emit(
          isUpdate
              ? CacheActivityFacilityAssetState.updated(resultEntry!)
              : CacheActivityFacilityAssetState.added(resultEntry!),
        );
      }
      add(CacheActivityFacilityAssetEvent.get(event.entry.activityFacilityId));
    } catch (e) {
      emit(CacheActivityFacilityAssetState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheActivityFacilityAsset(
      CacheActivityFacilityAssetEventDelete event,
      Emitter<CacheActivityFacilityAssetState> emit) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheActivityFacilityAssets.delete(event.id);
      });
      emit(const CacheActivityFacilityAssetState.deleted());
    } catch (e) {
      emit(CacheActivityFacilityAssetState.error(e.toString()));
    }
  }
}

@freezed
class CacheActivityFacilityAssetEvent with _$CacheActivityFacilityAssetEvent {
  const factory CacheActivityFacilityAssetEvent.get(String projectId) =
      CacheActivityFacilityAssetEventGet;
  const factory CacheActivityFacilityAssetEvent.add(
      CacheActivityFacilityAsset entry) = CacheActivityFacilityAssetEventAdd;
  const factory CacheActivityFacilityAssetEvent.update(
      CacheActivityFacilityAsset entry) = CacheActivityFacilityAssetEventUpdate;
  const factory CacheActivityFacilityAssetEvent.delete(int id) =
      CacheActivityFacilityAssetEventDelete;
}

@freezed
class CacheActivityFacilityAssetState with _$CacheActivityFacilityAssetState {
  const factory CacheActivityFacilityAssetState.initial() = _Initial;
  const factory CacheActivityFacilityAssetState.loading() = _Loading;
  const factory CacheActivityFacilityAssetState.loaded(
      List<CacheActivityFacilityAsset> entries) = _Loaded;
  const factory CacheActivityFacilityAssetState.added(
      CacheActivityFacilityAsset entry) = _Added;
  const factory CacheActivityFacilityAssetState.updated(
      CacheActivityFacilityAsset entry) = _Updated;
  const factory CacheActivityFacilityAssetState.deleted() = _Deleted;
  const factory CacheActivityFacilityAssetState.notFound() = _NotFound;
  const factory CacheActivityFacilityAssetState.error(String message) = _Error;
}
