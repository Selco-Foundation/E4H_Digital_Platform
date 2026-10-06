import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_asset_count.dart';

part 'cache_asset_count.freezed.dart';

class CacheAssetCountBloc
    extends Bloc<CacheAssetCountEvent, CacheAssetCountState> {
  final Isar isar;

  CacheAssetCountBloc(this.isar) : super(const CacheAssetCountState.initial()) {
    on<CacheAssetCountEventGet>(_getCacheAssetCount);
    on<CacheAssetCountEventGetAll>(_getAllCacheAssetCounts);
    on<CacheAssetCountEventAdd>(_addCacheAssetCount);
    on<CacheAssetCountEventUpdate>(_updateCacheAssetCount);
    on<CacheAssetCountEventDelete>(_deleteCacheAssetCount);
  }

  String _normalizeAssetType(String value) => value.trim().toLowerCase();

  int _compareEntries(CacheAssetCount a, CacheAssetCount b) {
    final aTime = a.updatedAt ?? a.createdAt;
    final bTime = b.updatedAt ?? b.createdAt;
    final byTime = aTime.compareTo(bTime);
    if (byTime != 0) return byTime;
    return a.id.compareTo(b.id);
  }

  CacheAssetCount? _latestEntryForType(
    List<CacheAssetCount> entries,
    String normalizedAssetType,
  ) {
    CacheAssetCount? latest;
    for (final entry in entries) {
      if (_normalizeAssetType(entry.assetType) != normalizedAssetType) {
        continue;
      }
      if (latest == null || _compareEntries(latest, entry) < 0) {
        latest = entry;
      }
    }
    return latest;
  }

  Future<void> _getCacheAssetCount(
    CacheAssetCountEventGet event,
    Emitter<CacheAssetCountState> emit,
  ) async {
    emit(const CacheAssetCountState.loading());
    try {
      final normalizedAssetType = _normalizeAssetType(event.assetType);
      final entries = await isar.cacheAssetCounts
          .where()
          .activityFacilityIdEqualTo(event.projectId)
          .findAll();
      final filtered = entries
          .where((entry) =>
              _normalizeAssetType(entry.assetType) == normalizedAssetType)
          .toList();

      if (filtered.isEmpty) {
        emit(const CacheAssetCountState.notFound());
      } else {
        emit(CacheAssetCountState.loaded(filtered));
      }
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }

  Future<void> _getAllCacheAssetCounts(
    CacheAssetCountEventGetAll event,
    Emitter<CacheAssetCountState> emit,
  ) async {
    emit(const CacheAssetCountState.loading());
    try {
      final entries = await isar.cacheAssetCounts
          .where()
          .activityFacilityIdEqualTo(event.projectId)
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
    CacheAssetCountEventAdd event,
    Emitter<CacheAssetCountState> emit,
  ) async {
    try {
      final normalizedAssetType = _normalizeAssetType(event.entry.assetType);
      final normalizedProgress = event.entry.progress;

      await isar.writeTxn(() async {
        final entries = await isar.cacheAssetCounts
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .findAll();
        final existing = _latestEntryForType(entries, normalizedAssetType);

        if (existing != null) {
          existing.assetType = normalizedAssetType;
          existing.count = event.entry.count;
          if (normalizedProgress != null) {
            existing.progress = normalizedProgress;
          }
          existing.updatedAt = DateTime.now();
          await isar.cacheAssetCounts.put(existing);
        } else {
          await isar.cacheAssetCounts.put(
            CacheAssetCount(
              activityFacilityId: event.entry.activityFacilityId,
              assetType: normalizedAssetType,
              count: event.entry.count,
              progress: normalizedProgress ?? 0,
            ),
          );
        }
      });

      final updatedEntries = await isar.cacheAssetCounts
          .where()
          .activityFacilityIdEqualTo(event.entry.activityFacilityId)
          .findAll();
      final addedEntry =
          _latestEntryForType(updatedEntries, normalizedAssetType);
      if (addedEntry != null) {
        emit(CacheAssetCountState.added(addedEntry));
      }
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }

  Future<void> _updateCacheAssetCount(
    CacheAssetCountEventUpdate event,
    Emitter<CacheAssetCountState> emit,
  ) async {
    try {
      final normalizedAssetType = _normalizeAssetType(event.entry.assetType);

      await isar.writeTxn(() async {
        final entries = await isar.cacheAssetCounts
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .findAll();
        final existing = _latestEntryForType(entries, normalizedAssetType);

        if (existing != null) {
          existing.assetType = normalizedAssetType;
          existing.progress = event.entry.progress;
          existing.updatedAt = DateTime.now();
          await isar.cacheAssetCounts.put(existing);
        } else {
          final newEntry = CacheAssetCount(
            activityFacilityId: event.entry.activityFacilityId,
            assetType: normalizedAssetType,
            progress: event.entry.progress,
            count: event.entry.count,
          );
          await isar.cacheAssetCounts.put(newEntry);
        }
      });

      final updatedEntries = await isar.cacheAssetCounts
          .where()
          .activityFacilityIdEqualTo(event.entry.activityFacilityId)
          .findAll();
      final updatedEntry =
          _latestEntryForType(updatedEntries, normalizedAssetType);

      if (updatedEntry != null) {
        emit(CacheAssetCountState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheAssetCount(
    CacheAssetCountEventDelete event,
    Emitter<CacheAssetCountState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheAssetCounts.delete(event.id);
      });
      emit(const CacheAssetCountState.deleted());
    } catch (e) {
      emit(CacheAssetCountState.error(e.toString()));
    }
  }
}

@freezed
class CacheAssetCountEvent with _$CacheAssetCountEvent {
  const factory CacheAssetCountEvent.get(
    String projectId,
    String assetType,
  ) = CacheAssetCountEventGet;

  const factory CacheAssetCountEvent.getAll(String projectId) =
      CacheAssetCountEventGetAll;

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
