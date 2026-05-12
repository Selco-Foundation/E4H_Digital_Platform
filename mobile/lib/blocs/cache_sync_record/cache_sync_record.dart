import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_sync_record.dart';
import '../../data/nosql/cache_unsubmitted_activity_facility.dart';

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
      final colRecord = _isar.cacheSyncRecords;
      final record =
          await colRecord.where().userTypeEqualTo(event.userType).findFirst();

      final drafts = await _isar.cacheUnsubmittedActivityFacilitys
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
  const factory CacheSyncRecordEvent.fetch(String userType) = _Fetch;
}

@freezed
class CacheSyncRecordState with _$CacheSyncRecordState {
  const factory CacheSyncRecordState.initial() = _Initial;
  const factory CacheSyncRecordState.loading() = _Loading;
  const factory CacheSyncRecordState.loaded({
    required CacheSyncRecord record,
    required int pendingCount,
  }) = _Loaded;
  const factory CacheSyncRecordState.notFound({
    required int pendingCount,
  }) = _NotFound;
  const factory CacheSyncRecordState.failure(String error) = _Failure;
}
