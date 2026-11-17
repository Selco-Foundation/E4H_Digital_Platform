import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar_community/isar.dart';

import '../../data/app_shared_preferences.dart';
import '../../data/nosql/localization.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../repositories/app_init_repo.dart';
import '../../repositories/localization_repo.dart';
import '../../utils/app_logger.dart';

part 'localization.freezed.dart';

class LocalizationBloc extends Bloc<LocalizationEvent, LocalizationState> {
  final Isar isar;

  final LocalizationRepository _repository;
  final Duration _requestTimeout;
  String? _locale;
  int _requestId = 0;

  LocalizationBloc(
    this.isar, {
    LocalizationRepository? repository,
    Duration requestTimeout = const Duration(seconds: 20),
  })  : _repository = repository ?? LocalizationRepository(),
        _requestTimeout = requestTimeout,
        super(const LocalizationState.initial()) {
    on<_LocaleSelectedEvent>(_onLocaleSelected);
  }

  String? get locale => _locale;

  Future<void> _onLocaleSelected(
    _LocaleSelectedEvent event,
    Emitter<LocalizationState> emit,
  ) async {
    final requestId = ++_requestId;
    final selectedLocale = event.locale?.trim() ?? '';
    _locale = selectedLocale;
    var hasTranslations = state.maybeWhen(
      selected: (locale, messages) =>
          locale == selectedLocale && messages.isNotEmpty,
      orElse: () => false,
    );
    if (!hasTranslations) {
      emit(LocalizationState.loading(locale: selectedLocale));
    }
    if (selectedLocale.isEmpty) {
      emit(LocalizationState.failure(locale: selectedLocale));
      return;
    }

    try {
      final cached = await isar.localizationWrappers
          .filter()
          .localeEqualTo(selectedLocale)
          .findFirst();
      if (requestId != _requestId || emit.isDone) return;
      final messages =
          _validMessages(cached?.localization ?? [], selectedLocale);
      if (messages.isNotEmpty) {
        hasTranslations = true;
        emit(LocalizationState.selected(
            locale: selectedLocale, messages: messages));
        await _saveLocale(selectedLocale);
      }
    } catch (error) {
      AppLogger.instance.info(error, title: 'Loading cached localization');
    }
    if (requestId != _requestId || emit.isDone) return;

    try {
      final response = await _repository
          .getLocalizationsList(_queryParamsFor(selectedLocale))
          .timeout(_requestTimeout);
      if (requestId != _requestId || emit.isDone) return;
      final messages = _validMessages(
          response.messages
              .map((message) => Localization()
                ..code = message.code
                ..message = message.message
                ..module = message.module
                ..locale = message.locale)
              .toList(),
          selectedLocale);
      if (messages.isEmpty) {
        throw const FormatException('No localization messages');
      }

      try {
        await _replaceCachedLocalizations(
            locale: selectedLocale,
            localizations: messages,
            requestId: requestId);
      } catch (error) {
        // Fresh server messages remain usable if persisting the cache fails.
        AppLogger.instance.info(error, title: 'Saving localization cache');
      }
      if (requestId != _requestId || emit.isDone) return;
      emit(LocalizationState.selected(
          locale: selectedLocale, messages: messages));
      await _saveLocale(selectedLocale);
    } catch (error) {
      AppLogger.instance.info(error, title: 'Loading server localization');
      if (requestId == _requestId && !emit.isDone && !hasTranslations) {
        emit(LocalizationState.failure(locale: selectedLocale));
      }
    }
  }

  Future<void> _saveLocale(String locale) async {
    try {
      await AppSharedPreferences().setSelectedLocale(locale);
    } catch (error) {
      AppLogger.instance.info(error, title: 'Saving selected locale');
    }
  }

  List<Localization> _validMessages(
          List<Localization> messages, String locale) =>
      List.unmodifiable(messages.where((message) =>
          message.locale == locale &&
          message.code.trim().isNotEmpty &&
          message.message.trim().isNotEmpty));

  Map<String, String> _queryParamsFor(String locale) {
    const moduleNameList = ['rainmaker-common'];
    return {
      'locale': locale,
      'module': moduleNameList.join(','),
      'tenantId': envConfig.variables.tenantId,
    };
  }

  Future<void> _replaceCachedLocalizations({
    required String locale,
    required List<Localization> localizations,
    required int requestId,
  }) async {
    await isar.writeTxn(() async {
      if (requestId != _requestId || isClosed) return;
      final existing = await isar.localizationWrappers
          .filter()
          .localeEqualTo(locale)
          .findAll();

      if (requestId != _requestId || isClosed) return;
      for (final wrapper in existing) {
        await isar.localizationWrappers.delete(wrapper.id);
      }

      await isar.localizationWrappers.put(
        LocalizationWrapper()
          ..locale = locale
          ..localization = localizations,
      );
    });
  }
}

@freezed
class LocalizationEvent with _$LocalizationEvent {
  const factory LocalizationEvent.onSelect(
      {String? locale, InterfacesList? moduleList}) = _LocaleSelectedEvent;
}

@freezed
class LocalizationState with _$LocalizationState {
  const factory LocalizationState.initial() = _LocaleNotSelectedState;
  const factory LocalizationState.loading({required String locale}) =
      _LocaleLoadingState;
  const factory LocalizationState.selected({
    required String locale,
    required List<Localization> messages,
  }) = _LocaleSelectedState;
  const factory LocalizationState.failure({required String locale}) =
      _LocaleFailureState;
}
