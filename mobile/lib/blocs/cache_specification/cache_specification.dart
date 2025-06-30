import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_specification.dart';

part 'cache_specification.freezed.dart';

/// Bloc responsible for CRUD on CacheSpecification
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
          .projectIdEqualTo(event.projectId)
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
        // Check if an entry already exists for this projectId + assetType + system
        final existing = await isar.cacheSpecifications
            .where()
            .projectIdEqualTo(event.entry.projectId)
            .filter()
            .assetTypeEqualTo(event.entry.assetType)
            .and()
            .systemEqualTo(event.entry.system)
            .findFirst();

        if (existing != null) {
          // Overwrite totalCapacity / totalCapacityUnit if desired:
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
            .projectIdEqualTo(event.entry.projectId)
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
          // final newEntry = CacheSpecification(
          //   projectId: event.entry.projectId,
          //   assetType: event.entry.assetType,
          //   system: event.entry.system,
          //   totalCapacity: event.entry.totalCapacity,
          //   totalCapacityUnit: event.entry.totalCapacityUnit,
          // );
          await isar.cacheSpecifications.put(event.entry);
        }
      });

      // Emit the newly updated/added entry
      final updatedEntry = await isar.cacheSpecifications
          .where()
          .projectIdEqualTo(event.entry.projectId)
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

/// Events for CacheSpecificationBloc
@freezed
class CacheSpecificationEvent with _$CacheSpecificationEvent {
  /// Load all specs for a given projectId + assetType
  const factory CacheSpecificationEvent.get(
    String projectId,
    String assetType,
  ) = CacheSpecificationEventGet;

  /// Add a new specification (or overwrite existing totalCapacity)
  const factory CacheSpecificationEvent.add(CacheSpecification entry) =
      CacheSpecificationEventAdd;

  /// Update an existing specification (or insert if missing)
  const factory CacheSpecificationEvent.update(CacheSpecification entry) =
      CacheSpecificationEventUpdate;

  /// Delete by Isar Id
  const factory CacheSpecificationEvent.delete(int id) =
      CacheSpecificationEventDelete;
}

/// States for CacheSpecificationBloc
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
