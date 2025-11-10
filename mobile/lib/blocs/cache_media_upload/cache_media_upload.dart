import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_media_upload.dart';

part 'cache_media_upload.freezed.dart';

class CacheMediaUploadBloc
    extends Bloc<CacheMediaUploadEvent, CacheMediaUploadState> {
  final Isar isar;

  CacheMediaUploadBloc(this.isar)
      : super(const CacheMediaUploadState.initial()) {
    on<CacheMediaUploadEventGet>(_getCacheMediaUpload);
    on<CacheMediaUploadEventAdd>(_addCacheMediaUpload);
    on<CacheMediaUploadEventUpdate>(_updateCacheMediaUpload);
    on<CacheMediaUploadEventDelete>(_deleteCacheMediaUpload);
    on<CacheMediaUploadEventDeleteAll>(_deleteAllCacheMediaUpload);
  }

  Future<void> _getCacheMediaUpload(
    CacheMediaUploadEventGet event,
    Emitter<CacheMediaUploadState> emit,
  ) async {
    emit(const CacheMediaUploadState.loading());
    try {
      final entries = await isar.cacheMediaUploads
          .where()
          .activityFacilityIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo(event.assetType)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheMediaUploadState.notFound());
      } else {
        emit(CacheMediaUploadState.loaded(entries));
      }
    } catch (e) {
      emit(CacheMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _addCacheMediaUpload(
    CacheMediaUploadEventAdd event,
    Emitter<CacheMediaUploadState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheMediaUploads
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .and()
            .itemNumberEqualTo(event.entry.itemNumber)
            .and()
            .itemTypeEqualTo(event.entry.itemType)
            .and()
            .filePathEqualTo(event.entry.filePath)
            .findFirst();

        if (existing != null) {
          existing.updatedAt = DateTime.now();
          await isar.cacheMediaUploads.put(existing);
        } else {
          await isar.cacheMediaUploads.put(event.entry);
        }
      });

      emit(CacheMediaUploadState.added(event.entry));
    } catch (e) {
      emit(CacheMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _updateCacheMediaUpload(
    CacheMediaUploadEventUpdate event,
    Emitter<CacheMediaUploadState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheMediaUploads
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .and()
            .itemNumberEqualTo(event.entry.itemNumber)
            .and()
            .itemTypeEqualTo(event.entry.itemType)
            .findFirst();

        if (existing != null) {
          existing.filePath = event.entry.filePath;
          existing.latitude = event.entry.latitude;
          existing.longitude = event.entry.longitude;
          existing.updatedAt = DateTime.now();
          await isar.cacheMediaUploads.put(existing);
        } else {
          await isar.cacheMediaUploads.put(event.entry);
        }
      });

      final updatedEntry = await isar.cacheMediaUploads
          .where()
          .activityFacilityIdEqualTo(event.entry.activityFacilityId)
          .filter()
          .assetTypeEqualTo(event.entry.assetType)
          .and()
          .itemNumberEqualTo(event.entry.itemNumber)
          .and()
          .itemTypeEqualTo(event.entry.itemType)
          .findFirst();

      if (updatedEntry != null) {
        emit(CacheMediaUploadState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheMediaUpload(
    CacheMediaUploadEventDelete event,
    Emitter<CacheMediaUploadState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheMediaUploads.delete(event.id);
      });
      emit(const CacheMediaUploadState.deleted());
    } catch (e) {
      emit(CacheMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _deleteAllCacheMediaUpload(
    CacheMediaUploadEventDeleteAll event,
    Emitter<CacheMediaUploadState> emit,
  ) async {
    emit(const CacheMediaUploadState.loading());
    try {
      await isar.writeTxn(() async {
        final q = isar.cacheMediaUploads
            .where()
            .activityFacilityIdEqualTo(event.projectId)
            .filter()
            .assetTypeEqualTo(event.assetType);
        final all = await q.findAll();
        for (final e in all) {
          await isar.cacheMediaUploads.delete(e.id);
        }
      });
      emit(const CacheMediaUploadState.deleted());
    } catch (e) {
      emit(CacheMediaUploadState.error(e.toString()));
    }
  }
}

@freezed
class CacheMediaUploadEvent with _$CacheMediaUploadEvent {
  const factory CacheMediaUploadEvent.get(
    String projectId,
    String assetType,
  ) = CacheMediaUploadEventGet;

  const factory CacheMediaUploadEvent.add(CacheMediaUpload entry) =
      CacheMediaUploadEventAdd;
  const factory CacheMediaUploadEvent.update(CacheMediaUpload entry) =
      CacheMediaUploadEventUpdate;
  const factory CacheMediaUploadEvent.delete(int id) =
      CacheMediaUploadEventDelete;
  const factory CacheMediaUploadEvent.deleteAll(
    String projectId,
    String assetType,
  ) = CacheMediaUploadEventDeleteAll;
}

@freezed
class CacheMediaUploadState with _$CacheMediaUploadState {
  const factory CacheMediaUploadState.initial() = _Initial;
  const factory CacheMediaUploadState.loading() = _Loading;
  const factory CacheMediaUploadState.loaded(List<CacheMediaUpload> entries) =
      _Loaded;
  const factory CacheMediaUploadState.added(CacheMediaUpload entry) = _Added;
  const factory CacheMediaUploadState.updated(CacheMediaUpload entry) =
      _Updated;
  const factory CacheMediaUploadState.deleted() = _Deleted;
  const factory CacheMediaUploadState.notFound() = _NotFound;
  const factory CacheMediaUploadState.error(String message) = _Error;
}
