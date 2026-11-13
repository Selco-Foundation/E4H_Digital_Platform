import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar_community/isar.dart';

import '../../model/installation_images/installation_images.dart';
import '../../repositories/app_init_repo.dart';
import '../../repositories/installation_images_repo.dart';
import '../../utils/error_localization.dart';

part 'installation_images.freezed.dart';

class InstallationImagesBloc
    extends Bloc<InstallationImagesEvent, InstallationImagesState> {
  InstallationImagesBloc(
    Isar isar, {
    AppInitRepo? appInitRepo,
  })  : _repository = InstallationImagesRepository(
          isar,
          appInitRepo: appInitRepo,
        ),
        super(const InstallationImagesState.initial()) {
    on<_Fetch>(_onFetchInstallationImages);
  }

  final InstallationImagesRepository _repository;

  Future<void> _onFetchInstallationImages(
    _Fetch event,
    Emitter<InstallationImagesState> emit,
  ) async {
    emit(const InstallationImagesState.loading());

    try {
      final activeItems =
          await _repository.fetchMdms(systemType: event.systemType);
      if (activeItems.isNotEmpty) {
        emit(InstallationImagesState.loaded(activeItems));
        return;
      } else {
        emit(const InstallationImagesState.error(
          'INSTALLATION_IMAGES_NO_CONFIGURATION_FOUND',
        ));
        return;
      }
    } catch (_) {
      emit(const InstallationImagesState.error(
        ErrorKeys.installationImagesLoad,
      ));
    }
  }
}

@freezed
class InstallationImagesEvent with _$InstallationImagesEvent {
  const factory InstallationImagesEvent.fetch({
    required String systemType,
  }) = _Fetch;
}

@freezed
class InstallationImagesState with _$InstallationImagesState {
  const factory InstallationImagesState.initial() = _Initial;
  const factory InstallationImagesState.loading() = _Loading;
  const factory InstallationImagesState.loaded(
    List<InstallationImageItem> items,
  ) = _Loaded;
  const factory InstallationImagesState.error(String message) = _Error;
}
