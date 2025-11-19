import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_specification.dart';

part 'cache_specification.freezed.dart';

class CacheSpecificationBloc
    extends Bloc<CacheSpecificationEvent, CacheSpecificationState> {
  final Isar isar;

  CacheSpecificationBloc(this.isar)
      : super(const CacheSpecificationState.initial()) {
    on<CacheSpecificationEventGet>(_getCacheSpecification);
    on<CacheSpecificationEventAdd>(_addCacheSpecification);
    on<CacheSpecificationEventUpdate>(_updateCacheSpecification);
    on<CacheSpecificationEventDelete>(_deleteCacheSpecification);
  }

  Future<void> _getCacheSpecification(
    CacheSpecificationEventGet event,
    Emitter<CacheSpecificationState> emit,
  ) async {
    emit(const CacheSpecificationState.loading());
    try {
      final entries = await isar.cacheSpecifications
          .where()
          .activityFacilityIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo(event.assetType)
          .findAll();

      if (entries.isEmpty) {
        emit(const CacheSpecificationState.notFound());
      } else {
        emit(CacheSpecificationState.loaded(entries));
      }
    } catch (e) {
      emit(CacheSpecificationState.error(e.toString()));
    }
  }

  Future<void> _addCacheSpecification(
    CacheSpecificationEventAdd event,
    Emitter<CacheSpecificationState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheSpecifications
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .and()
            .systemEqualTo(event.entry.system)
            .findFirst();

        if (existing != null) {
          existing.totalCapacity = event.entry.totalCapacity;
          existing.totalCapacityUnit = event.entry.totalCapacityUnit;
          existing.updatedAt = DateTime.now();
          await isar.cacheSpecifications.put(existing);
        } else {
          await isar.cacheSpecifications.put(event.entry);
        }
      });

      emit(CacheSpecificationState.added(event.entry));
    } catch (e) {
      emit(CacheSpecificationState.error(e.toString()));
    }
  }

  Future<void> _updateCacheSpecification(
    CacheSpecificationEventUpdate event,
    Emitter<CacheSpecificationState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        final existing = await isar.cacheSpecifications
            .where()
            .activityFacilityIdEqualTo(event.entry.activityFacilityId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .and()
            .systemEqualTo(event.entry.system)
            .findFirst();

        if (existing != null) {
          existing.totalCapacity = event.entry.totalCapacity;
          existing.totalCapacityUnit = event.entry.totalCapacityUnit;
          existing.updatedAt = DateTime.now();
          await isar.cacheSpecifications.put(existing);
        } else {
          await isar.cacheSpecifications.put(event.entry);
        }
      });

      final updatedEntry = await isar.cacheSpecifications
          .where()
          .activityFacilityIdEqualTo(event.entry.activityFacilityId)
          .filter()
          .assetTypeEqualTo(event.entry.assetType)
          .and()
          .systemEqualTo(event.entry.system)
          .findFirst();

      if (updatedEntry != null) {
        emit(CacheSpecificationState.updated(updatedEntry));
      }
    } catch (e) {
      emit(CacheSpecificationState.error(e.toString()));
    }
  }

  Future<void> _deleteCacheSpecification(
    CacheSpecificationEventDelete event,
    Emitter<CacheSpecificationState> emit,
  ) async {
    try {
      await isar.writeTxn(() async {
        await isar.cacheSpecifications.delete(event.id);
      });
      emit(const CacheSpecificationState.deleted());
    } catch (e) {
      emit(CacheSpecificationState.error(e.toString()));
    }
  }
}

@freezed
class CacheSpecificationEvent with _$CacheSpecificationEvent {
  const factory CacheSpecificationEvent.get(
    String projectId,
    String assetType,
  ) = CacheSpecificationEventGet;

  const factory CacheSpecificationEvent.add(CacheSpecification entry) =
      CacheSpecificationEventAdd;

  const factory CacheSpecificationEvent.update(CacheSpecification entry) =
      CacheSpecificationEventUpdate;

  const factory CacheSpecificationEvent.delete(int id) =
      CacheSpecificationEventDelete;
}

@freezed
class CacheSpecificationState with _$CacheSpecificationState {
  const factory CacheSpecificationState.initial() = _Initial;
  const factory CacheSpecificationState.loading() = _Loading;
  const factory CacheSpecificationState.loaded(
      List<CacheSpecification> entries) = _Loaded;
  const factory CacheSpecificationState.added(CacheSpecification entry) =
      _Added;
  const factory CacheSpecificationState.updated(CacheSpecification entry) =
      _Updated;
  const factory CacheSpecificationState.deleted() = _Deleted;
  const factory CacheSpecificationState.notFound() = _NotFound;
  const factory CacheSpecificationState.error(String message) = _Error;
}
