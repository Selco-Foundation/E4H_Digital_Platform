import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_project_asset.dart';

part 'cache_project_asset.freezed.dart';

class CacheProjectAssetBloc
    extends Bloc<CacheProjectAssetEvent, CacheProjectAssetState> {
  final Isar isar;

  CacheProjectAssetBloc(this.isar)
      : super(const CacheProjectAssetState.initial()) {
    on<CacheProjectAssetEventGet>(_getCacheProjectAsset);
    on<CacheProjectAssetEventAdd>(_addCacheProjectAsset);
    on<CacheProjectAssetEventUpdate>(_updateCacheProjectAsset);
    on<CacheProjectAssetEventDelete>(_deleteCacheProjectAsset);
  }

  Future<void> _getCacheProjectAsset(CacheProjectAssetEventGet event,
      Emitter<CacheProjectAssetState> emit) async {
    emit(const CacheProjectAssetState.loading());
    try {
      final entries = await isar.cacheProjectAssets
          .where()
          .projectIdEqualTo(event.projectId)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheProjectAssetState.notFound());
      } else {
        emit(CacheProjectAssetState.loaded(entries));
      }
    } catch (e) {
      emit(CacheProjectAssetState.error(e.toString()));
    }
  }

  Future<void> _addCacheProjectAsset(CacheProjectAssetEventAdd event,
      Emitter<CacheProjectAssetState> emit) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheProjectAssets
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .findFirst();

        if (existing == null) {
          await isar.cacheProjectAssets.put(event.entry);
        }
      });
      emit(CacheProjectAssetState.added(event.entry));
    } catch (e) {
      emit(CacheProjectAssetState.error(e.toString()));
    }
  }

  // Future<void> _updateCacheProjectAsset(CacheProjectAssetEventUpdate event,
  //     Emitter<CacheProjectAssetState> emit) async {
  //   try {
  //     await isar.writeTxn(() async {
  //       final existing = await isar.cacheProjectAssets
  //           .where()
  //           .projectIdEqualTo(event.entry.projectId)
  //           .findFirst();
  //
  //       if (existing != null) {
  //         existing.progress = event.entry.progress;
  //         existing.updatedAt = DateTime.now();
  //         await isar.cacheProjectAssets.put(existing);
  //         emit(CacheProjectAssetState.updated(existing));
  //       }
  //     });
  //   } catch (e) {
  //     emit(CacheProjectAssetState.error(e.toString()));
  //   }
  // }

  Future<void> _updateCacheProjectAsset(
    CacheProjectAssetEventUpdate event,
    Emitter<CacheProjectAssetState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheProjectAssets
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .findFirst();

        if (existing != null) {
          existing.progress = event.entry.progress;
          existing.updatedAt = DateTime.now();
          await isar.cacheProjectAssets.put(existing);
          emit(CacheProjectAssetState.updated(existing));
        } else {
          final newEntry = CacheProjectAsset(
            projectId: event.entry.projectId,
            progress: event.entry.progress,
          );
          await isar.cacheProjectAssets.put(newEntry);
          emit(CacheProjectAssetState.added(newEntry));
        }
      });

      // Always refresh data after update
      add(CacheProjectAssetEvent.get(event.entry.projectId));
    } catch (e) {
      emit(CacheProjectAssetState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheProjectAsset(CacheProjectAssetEventDelete event,
      Emitter<CacheProjectAssetState> emit) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheProjectAssets.delete(event.id);
      });
      emit(const CacheProjectAssetState.deleted());
    } catch (e) {
      emit(CacheProjectAssetState.error(e.toString()));
    }
  }
}

@freezed
class CacheProjectAssetEvent with _$CacheProjectAssetEvent {
  const factory CacheProjectAssetEvent.get(String projectId) =
      CacheProjectAssetEventGet;
  const factory CacheProjectAssetEvent.add(CacheProjectAsset entry) =
      CacheProjectAssetEventAdd;
  const factory CacheProjectAssetEvent.update(CacheProjectAsset entry) =
      CacheProjectAssetEventUpdate;
  const factory CacheProjectAssetEvent.delete(int id) =
      CacheProjectAssetEventDelete;
}

@freezed
class CacheProjectAssetState with _$CacheProjectAssetState {
  const factory CacheProjectAssetState.initial() = _Initial;
  const factory CacheProjectAssetState.loading() = _Loading;
  const factory CacheProjectAssetState.loaded(List<CacheProjectAsset> entries) =
      _Loaded;
  const factory CacheProjectAssetState.added(CacheProjectAsset entry) = _Added;
  const factory CacheProjectAssetState.updated(CacheProjectAsset entry) =
      _Updated;
  const factory CacheProjectAssetState.deleted() = _Deleted;
  const factory CacheProjectAssetState.notFound() = _NotFound;
  const factory CacheProjectAssetState.error(String message) = _Error;
}
