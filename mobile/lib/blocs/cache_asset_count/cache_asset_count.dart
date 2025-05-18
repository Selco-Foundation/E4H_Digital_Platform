import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:selco/data/nosql/cache_asset_count.dart';

import '../../data/nosql/cache_project_asset.dart';

part 'cache_asset_count.freezed.dart';

class CacheAssetCountBloc
    extends Bloc<CacheAssetCountEvent, CacheAssetCountState> {
  final Isar isar;

  CacheAssetCountBloc(this.isar) : super(const CacheAssetCountState.initial()) {
    on<CacheAssetCountEventGet>(_getCacheAssetCount);
    on<CacheAssetCountEventAdd>(_addCacheAssetCount);
    on<CacheAssetCountEventUpdate>(_updateCacheAssetCount);
    on<CacheAssetCountEventDelete>(_deleteCacheAssetCount);
  }

  Future<void> _getCacheAssetCount(
      CacheAssetCountEventGet event, Emitter<CacheAssetCountState> emit) async {
    emit(const CacheAssetCountState.loading());
    try {
      final entries = await isar.cacheAssetCounts
          .where()
          .projectIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo(event.assetType)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheAssetCountState.notFound());
      } else {
        emit(CacheAssetCountState.loaded(entries));
      }
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }

  Future<void> _addCacheAssetCount(
      CacheAssetCountEventAdd event, Emitter<CacheAssetCountState> emit) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAssetCounts
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .findFirst();

        if (existing != null) {
          existing.count = event.entry.count ?? existing.count;
          existing.updatedAt = DateTime.now();
          await isar.cacheAssetCounts.put(existing);
        } else {
          await isar.cacheAssetCounts.put(event.entry);
        }
      });
      emit(CacheAssetCountState.added(event.entry));
    } catch (e) {
      print(e.toString());
      emit(CacheAssetCountState.error(e.toString()));
    }
  }

  Future<void> _updateCacheAssetCount(
    CacheAssetCountEventUpdate event,
    Emitter<CacheAssetCountState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAssetCounts
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .findFirst();

        if (existing != null) {
          existing.progress = event.entry.progress;
          existing.updatedAt = DateTime.now();
          await isar.cacheAssetCounts.put(existing);
        } else {
          final newEntry = CacheAssetCount(
            projectId: event.entry.projectId,
            assetType: event.entry.assetType,
            progress: event.entry.progress,
            count: 0,
          );
          await isar.cacheAssetCounts.put(newEntry);
        }
      });

      final updatedEntry = await isar.cacheAssetCounts
          .where()
          .projectIdEqualTo(event.entry.projectId)
          .filter()
          .assetTypeEqualTo(event.entry.assetType)
          .findFirst();

      if (updatedEntry != null) {
        emit(CacheAssetCountState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheAssetCount(CacheAssetCountEventDelete event,
      Emitter<CacheAssetCountState> emit) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheProjectAssets.delete(event.id);
      });
      emit(const CacheAssetCountState.deleted());
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }
}

@freezed
class CacheAssetCountEvent with _$CacheAssetCountEvent {
  const factory CacheAssetCountEvent.get(String projectId, String assetType) =
      CacheAssetCountEventGet;
  const factory CacheAssetCountEvent.add(CacheAssetCount entry) =
      CacheAssetCountEventAdd;
  const factory CacheAssetCountEvent.update(CacheAssetCount entry) =
      CacheAssetCountEventUpdate;
  const factory CacheAssetCountEvent.delete(int id) =
      CacheAssetCountEventDelete;
}

@freezed
class CacheAssetCountState with _$CacheAssetCountState {
  const factory CacheAssetCountState.initial() = _Initial;
  const factory CacheAssetCountState.loading() = _Loading;
  const factory CacheAssetCountState.loaded(List<CacheAssetCount> entries) =
      _Loaded;
  const factory CacheAssetCountState.added(CacheAssetCount entry) = _Added;
  const factory CacheAssetCountState.updated(CacheAssetCount entry) = _Updated;
  const factory CacheAssetCountState.deleted() = _Deleted;
  const factory CacheAssetCountState.notFound() = _NotFound;
  const factory CacheAssetCountState.error(String message) = _Error;
}
