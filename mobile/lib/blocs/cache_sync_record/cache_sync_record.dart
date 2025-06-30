import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_project.dart';

part 'cache_sync_record.freezed.dart';

class CacheSyncRecordBloc
    extends Bloc<CacheSyncRecordEvent, CacheSyncRecordState> {
  final Isar _isar;

  CacheSyncRecordBloc(this._isar)
      : super(const CacheSyncRecordState.initial()) {
    on<_Fetch>(_onFetch);
  }

  Future<void> _onFetch(
    _Fetch event,
    Emitter<CacheSyncRecordState> emit,
  ) async {
    emit(const CacheSyncRecordState.loading());
    try {
      // 1) load or not‐found
      final colRecord = _isar.cacheSyncRecords;
      final record =
          await colRecord.where().userTypeEqualTo(event.userType).findFirst();

      // 2) count drafts for this userType
      final drafts = await _isar.cacheUnsubmittedProjects
          .where()
          .filter()
          .userTypeEqualTo(event.userType)
          .findAll();
      final pendingCount = drafts.length;

      if (record != null) {
        emit(CacheSyncRecordState.loaded(
          record: record,
          pendingCount: pendingCount,
        ));
      } else {
        emit(CacheSyncRecordState.notFound(
          pendingCount: pendingCount,
        ));
      }
    } catch (e) {
      emit(CacheSyncRecordState.failure(e.toString()));
    }
  }
}

@freezed
class CacheSyncRecordEvent with _$CacheSyncRecordEvent {
  /// Fetch both last‐sync record *and* draft‐count for this userType
  const factory CacheSyncRecordEvent.fetch(String userType) = _Fetch;
}

@freezed
class CacheSyncRecordState with _$CacheSyncRecordState {
  /// before anything happens
  const factory CacheSyncRecordState.initial() = _Initial;

  /// while loading
  const factory CacheSyncRecordState.loading() = _Loading;

  /// we found a sync record (with date) *and* have a pending draft count
  const factory CacheSyncRecordState.loaded({
    required CacheSyncRecord record,
    required int pendingCount,
  }) = _Loaded;

  /// no sync record exists yet (first‐time), but we still emit the pending count
  const factory CacheSyncRecordState.notFound({
    required int pendingCount,
  }) = _NotFound;

  /// something went wrong
  const factory CacheSyncRecordState.failure(String error) = _Failure;
}
