import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_completion_report.dart';

part 'cache_completion_report.freezed.dart';

@freezed
class CacheCompletionReportEvent with _$CacheCompletionReportEvent {
  const factory CacheCompletionReportEvent.get(String projectId) = _Get;
  const factory CacheCompletionReportEvent.addOrUpdate(
      CacheCompletionReport report) = _AddOrUpdate;
  const factory CacheCompletionReportEvent.delete(int id) = _Delete;
}

@freezed
class CacheCompletionReportState with _$CacheCompletionReportState {
  const factory CacheCompletionReportState.initial() = _Initial;
  const factory CacheCompletionReportState.loading() = _Loading;
  const factory CacheCompletionReportState.loaded(
      CacheCompletionReport report) = _Loaded;
  const factory CacheCompletionReportState.addedOrUpdated(
      CacheCompletionReport report) = _AddedOrUpdated;
  const factory CacheCompletionReportState.deleted() = _Deleted;
  const factory CacheCompletionReportState.notFound() = _NotFound;
  const factory CacheCompletionReportState.error(String message) = _Error;
}

class CacheCompletionReportBloc
    extends Bloc<CacheCompletionReportEvent, CacheCompletionReportState> {
  final Isar isar;

  CacheCompletionReportBloc(this.isar)
      : super(const CacheCompletionReportState.initial()) {
    on<_Get>(_getReport);
    on<_AddOrUpdate>(_addOrUpdateReport);
    on<_Delete>(_deleteReport);
  }

  Future<void> _getReport(
    _Get event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    emit(const CacheCompletionReportState.loading());
    try {
      final report = await isar.cacheCompletionReports
          .where()
          .projectIdEqualTo(event.projectId)
          .findFirst();

      if (report == null) {
        emit(const CacheCompletionReportState.notFound());
      } else {
        emit(CacheCompletionReportState.loaded(report));
      }
    } catch (e) {
      emit(CacheCompletionReportState.error(e.toString()));
    }
  }

  Future<void> _addOrUpdateReport(
    _AddOrUpdate event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheCompletionReports
            .where()
            .projectIdEqualTo(event.report.projectId)
            .findFirst();

        if (existing != null) {
          existing.filePath = event.report.filePath;
          existing.latitude = event.report.latitude;
          existing.longitude = event.report.longitude;
          existing.updatedAt = DateTime.now();
          await isar.cacheCompletionReports.put(existing);
          emit(CacheCompletionReportState.addedOrUpdated(existing));
        } else {
          await isar.cacheCompletionReports.put(event.report);
          emit(CacheCompletionReportState.addedOrUpdated(event.report));
        }
      });
    } catch (e) {
      emit(CacheCompletionReportState.error(e.toString()));
    }
  }

  Future<void> _deleteReport(
    _Delete event,
    Emitter<CacheCompletionReportState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheCompletionReports.delete(event.id);
      });
      emit(const CacheCompletionReportState.deleted());
    } catch (e) {
      emit(CacheCompletionReportState.error(e.toString()));
    }
  }
}
