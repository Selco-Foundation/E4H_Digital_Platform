import 'dart:io';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_installation_image.dart';
import '../../repositories/installation_images_repo.dart';
import '../../utils/utils.dart';

part 'cache_installation_image.freezed.dart';

class CacheInstallationImageBloc
    extends Bloc<CacheInstallationImageEvent, CacheInstallationImageState> {
  CacheInstallationImageBloc(Isar isar)
      : _repository = InstallationImagesRepository(isar),
        super(const CacheInstallationImageState.initial()) {
    on<CacheInstallationImageEventGet>(_onGet);
    on<CacheInstallationImageEventSaveAll>(_onSaveAll);
  }

  final InstallationImagesRepository _repository;

  Future<void> _onGet(
    CacheInstallationImageEventGet event,
    Emitter<CacheInstallationImageState> emit,
  ) async {
    emit(const CacheInstallationImageState.loading());
    try {
      final entries = await _repository.getCachedImages(
        activityFacilityId: event.activityFacilityId,
      );

      if (entries.isEmpty) {
        emit(const CacheInstallationImageState.notFound());
      } else {
        emit(CacheInstallationImageState.loaded(entries));
      }
    } catch (e) {
      emit(CacheInstallationImageState.error(e.toString()));
    }
  }

  Future<void> _onSaveAll(
    CacheInstallationImageEventSaveAll event,
    Emitter<CacheInstallationImageState> emit,
  ) async {
    emit(const CacheInstallationImageState.loading());
    try {
      await _repository.deleteAllCachedImages(
        activityFacilityId: event.activityFacilityId,
      );

      for (final entry in event.selectedImages.entries) {
        for (final file in entry.value) {
          final copiedPath = await copyFileToLocalDir(file);
          await _repository.addCachedImage(
            CacheInstallationImage(
              activityFacilityId: event.activityFacilityId,
              userType: event.userType,
              code: entry.key,
              order: event.orderByCode[entry.key] ?? '',
              photoPath: copiedPath,
              latitude: event.latitude,
              longitude: event.longitude,
            ),
          );
        }
      }

      emit(const CacheInstallationImageState.saved());
    } catch (e) {
      emit(CacheInstallationImageState.error(e.toString()));
    }
  }
}

@freezed
class CacheInstallationImageEvent with _$CacheInstallationImageEvent {
  const factory CacheInstallationImageEvent.get(
    String activityFacilityId,
    String userType,
  ) = CacheInstallationImageEventGet;

  const factory CacheInstallationImageEvent.saveAll({
    required String activityFacilityId,
    required String userType,
    required Map<String, List<File>> selectedImages,
    required Map<String, String> orderByCode,
    required String latitude,
    required String longitude,
  }) = CacheInstallationImageEventSaveAll;
}

@freezed
class CacheInstallationImageState with _$CacheInstallationImageState {
  const factory CacheInstallationImageState.initial() = _Initial;
  const factory CacheInstallationImageState.loading() = _Loading;
  const factory CacheInstallationImageState.loaded(
    List<CacheInstallationImage> entries,
  ) = _Loaded;
  const factory CacheInstallationImageState.saved() = _Saved;
  const factory CacheInstallationImageState.deleted() = _Deleted;
  const factory CacheInstallationImageState.notFound() = _NotFound;
  const factory CacheInstallationImageState.error(String message) = _Error;
}
