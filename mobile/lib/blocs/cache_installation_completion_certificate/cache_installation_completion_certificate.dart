import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_installation_completion_certificate.dart';
import '../../repositories/installation_completion_certificate_repo.dart';

part 'cache_installation_completion_certificate.freezed.dart';

@freezed
class CacheInstallationCompletionCertificateEvent
    with _$CacheInstallationCompletionCertificateEvent {
  const factory CacheInstallationCompletionCertificateEvent.load(
    String activityFacilityId,
  ) = _Load;

  const factory CacheInstallationCompletionCertificateEvent.replaceAllForProject({
    required String activityFacilityId,
    required List<InstallationCompletionCertificateInput> files,
  }) = _ReplaceAllForProject;

  const factory CacheInstallationCompletionCertificateEvent.clearProject(
    String activityFacilityId,
  ) = _ClearProject;
}

@freezed
class CacheInstallationCompletionCertificateState
    with _$CacheInstallationCompletionCertificateState {
  const factory CacheInstallationCompletionCertificateState.initial() =
      _Initial;
  const factory CacheInstallationCompletionCertificateState.loading() =
      _Loading;
  const factory CacheInstallationCompletionCertificateState.loaded(
    List<CacheInstallationCompletionCertificate> files,
  ) = _Loaded;
  const factory CacheInstallationCompletionCertificateState.saved() = _Saved;
  const factory CacheInstallationCompletionCertificateState.notFound() =
      _NotFound;
  const factory CacheInstallationCompletionCertificateState.error(
    String message,
  ) = _Error;
}

class CacheInstallationCompletionCertificateBloc extends Bloc<
    CacheInstallationCompletionCertificateEvent,
    CacheInstallationCompletionCertificateState> {
  CacheInstallationCompletionCertificateBloc(Isar isar)
      : _repository = InstallationCompletionCertificateRepository(isar),
        super(const CacheInstallationCompletionCertificateState.initial()) {
    on<_Load>(_onLoad);
    on<_ReplaceAllForProject>(_onReplaceAllForProject);
    on<_ClearProject>(_onClearProject);
  }

  final InstallationCompletionCertificateRepository _repository;

  Future<void> _onLoad(
    _Load event,
    Emitter<CacheInstallationCompletionCertificateState> emit,
  ) async {
    emit(const CacheInstallationCompletionCertificateState.loading());
    try {
      final files = await _repository.getCachedFiles(
        activityFacilityId: event.activityFacilityId,
      );
      if (files.isEmpty) {
        emit(const CacheInstallationCompletionCertificateState.notFound());
      } else {
        emit(CacheInstallationCompletionCertificateState.loaded(files));
      }
    } catch (e) {
      emit(CacheInstallationCompletionCertificateState.error(e.toString()));
    }
  }

  Future<void> _onReplaceAllForProject(
    _ReplaceAllForProject event,
    Emitter<CacheInstallationCompletionCertificateState> emit,
  ) async {
    emit(const CacheInstallationCompletionCertificateState.loading());
    try {
      await _repository.replaceAllForProject(
        activityFacilityId: event.activityFacilityId,
        files: event.files,
      );
      emit(const CacheInstallationCompletionCertificateState.saved());
    } catch (e) {
      emit(CacheInstallationCompletionCertificateState.error(e.toString()));
    }
  }

  Future<void> _onClearProject(
    _ClearProject event,
    Emitter<CacheInstallationCompletionCertificateState> emit,
  ) async {
    emit(const CacheInstallationCompletionCertificateState.loading());
    try {
      await _repository.clearProject(
        activityFacilityId: event.activityFacilityId,
      );
      emit(const CacheInstallationCompletionCertificateState.notFound());
    } catch (e) {
      emit(CacheInstallationCompletionCertificateState.error(e.toString()));
    }
  }
}
