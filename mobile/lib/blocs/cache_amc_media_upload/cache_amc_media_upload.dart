import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_amc_media_upload.dart';

part 'cache_amc_media_upload.freezed.dart';

class CacheAmcMediaUploadBloc
    extends Bloc<CacheAmcMediaUploadEvent, CacheAmcMediaUploadState> {
  final Isar isar;

  CacheAmcMediaUploadBloc(this.isar)
      : super(const CacheAmcMediaUploadState.initial()) {
    on<CacheAmcMediaUploadEventGet>(_getCacheAmcMediaUpload);
    on<CacheAmcMediaUploadEventAdd>(_addCacheAmcMediaUpload);
    on<CacheAmcMediaUploadEventUpdate>(_updateCacheAmcMediaUpload);
    on<CacheAmcMediaUploadEventDelete>(_deleteCacheAmcMediaUpload);
    on<CacheAmcMediaUploadEventDeleteAll>(_deleteAllCacheAmcMediaUpload);
  }

  Future<void> _getCacheAmcMediaUpload(
    CacheAmcMediaUploadEventGet event,
    Emitter<CacheAmcMediaUploadState> emit,
  ) async {
    emit(const CacheAmcMediaUploadState.loading());
    try {
      final entries = await isar.cacheAmcMediaUploads
          .where()
          .scheduledVisitIdEqualTo(event.scheduledVisitId)
          .filter()
          .userTypeEqualTo(event.userType)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheAmcMediaUploadState.notFound());
      } else {
        emit(CacheAmcMediaUploadState.loaded(entries));
      }
    } catch (e) {
      emit(CacheAmcMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _addCacheAmcMediaUpload(
    CacheAmcMediaUploadEventAdd event,
    Emitter<CacheAmcMediaUploadState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAmcMediaUploads
            .where()
            .scheduledVisitIdEqualTo(event.entry.scheduledVisitId)
            .filter()
            .userTypeEqualTo(event.entry.userType)
            .and()
            .itemNumberEqualTo(event.entry.itemNumber)
            .and()
            .itemTypeEqualTo(event.entry.itemType)
            .and()
            .filePathEqualTo(event.entry.filePath)
            .findFirst();

        if (existing != null) {
          existing.updatedAt = DateTime.now();
          await isar.cacheAmcMediaUploads.put(existing);
        } else {
          await isar.cacheAmcMediaUploads.put(event.entry);
        }
      });

      emit(CacheAmcMediaUploadState.added(event.entry));
    } catch (e) {
      emit(CacheAmcMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _updateCacheAmcMediaUpload(
    CacheAmcMediaUploadEventUpdate event,
    Emitter<CacheAmcMediaUploadState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheAmcMediaUploads
            .where()
            .scheduledVisitIdEqualTo(event.entry.scheduledVisitId)
            .filter()
            .userTypeEqualTo(event.entry.userType)
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
          await isar.cacheAmcMediaUploads.put(existing);
        } else {
          await isar.cacheAmcMediaUploads.put(event.entry);
        }
      });

      final updatedEntry = await isar.cacheAmcMediaUploads
          .where()
          .scheduledVisitIdEqualTo(event.entry.scheduledVisitId)
          .filter()
          .userTypeEqualTo(event.entry.userType)
          .and()
          .itemNumberEqualTo(event.entry.itemNumber)
          .and()
          .itemTypeEqualTo(event.entry.itemType)
          .findFirst();

      if (updatedEntry != null) {
        emit(CacheAmcMediaUploadState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheAmcMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheAmcMediaUpload(
    CacheAmcMediaUploadEventDelete event,
    Emitter<CacheAmcMediaUploadState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheAmcMediaUploads.delete(event.id);
      });
      emit(const CacheAmcMediaUploadState.deleted());
    } catch (e) {
      emit(CacheAmcMediaUploadState.error(e.toString()));
    }
  }

  Future<void> _deleteAllCacheAmcMediaUpload(
    CacheAmcMediaUploadEventDeleteAll event,
    Emitter<CacheAmcMediaUploadState> emit,
  ) async {
    emit(const CacheAmcMediaUploadState.loading());
    try {
      await isar.writeTxn(() async {
        final q = isar.cacheAmcMediaUploads
            .where()
            .scheduledVisitIdEqualTo(event.scheduledVisitId)
            .filter()
            .userTypeEqualTo(event.userType);
        final all = await q.findAll();
        for (final e in all) {
          await isar.cacheAmcMediaUploads.delete(e.id);
        }
      });
      emit(const CacheAmcMediaUploadState.deleted());
    } catch (e) {
      emit(CacheAmcMediaUploadState.error(e.toString()));
    }
  }
}

@freezed
class CacheAmcMediaUploadEvent with _$CacheAmcMediaUploadEvent {
  const factory CacheAmcMediaUploadEvent.get(
    String scheduledVisitId,
    String userType,
  ) = CacheAmcMediaUploadEventGet;

  const factory CacheAmcMediaUploadEvent.add(CacheAmcMediaUpload entry) =
      CacheAmcMediaUploadEventAdd;

  const factory CacheAmcMediaUploadEvent.update(CacheAmcMediaUpload entry) =
      CacheAmcMediaUploadEventUpdate;

  const factory CacheAmcMediaUploadEvent.delete(int id) =
      CacheAmcMediaUploadEventDelete;

  const factory CacheAmcMediaUploadEvent.deleteAll(
    String scheduledVisitId,
    String userType,
  ) = CacheAmcMediaUploadEventDeleteAll;
}

@freezed
class CacheAmcMediaUploadState with _$CacheAmcMediaUploadState {
  const factory CacheAmcMediaUploadState.initial() = _Initial;
  const factory CacheAmcMediaUploadState.loading() = _Loading;
  const factory CacheAmcMediaUploadState.loaded(
    List<CacheAmcMediaUpload> entries,
  ) = _Loaded;
  const factory CacheAmcMediaUploadState.added(
    CacheAmcMediaUpload entry,
  ) = _Added;
  const factory CacheAmcMediaUploadState.updated(
    CacheAmcMediaUpload entry,
  ) = _Updated;
  const factory CacheAmcMediaUploadState.deleted() = _Deleted;
  const factory CacheAmcMediaUploadState.notFound() = _NotFound;
  const factory CacheAmcMediaUploadState.error(String message) = _Error;
}
