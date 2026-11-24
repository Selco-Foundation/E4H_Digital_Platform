import 'dart:async';
import 'dart:ui';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/app_shared_preferences.dart';
import '../../data/nosql/localization.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../repositories/app_init_repo.dart';
import '../../repositories/localization_repo.dart';
import 'app_localization.dart';

part 'localization.freezed.dart';

class LocalizationBloc extends Bloc<LocalizationEvent, LocalizationState> {
  final Isar isar;

  String? _locale;

  LocalizationBloc(this.isar) : super(const LocalizationState.initial()) {
    on<_LocaleSelectedEvent>(_onLocaleSelected);
  }

  String? get locale => _locale;

  FutureOr<void> _onLocaleSelected(
      _LocaleSelectedEvent event, Emitter<LocalizationState> emit) async {
    final selectedLocale = event.locale;
    if (selectedLocale == null || selectedLocale.isEmpty) {
      emit(LocalizationState.selected(locale: selectedLocale));
      return;
    }

    _locale = selectedLocale;
    AppSharedPreferences().setSelectedLocale(selectedLocale);

    final appLocalizations = _appLocalizationsFor(selectedLocale);
    await appLocalizations.load();

    try {
      final localizationsList = await LocalizationRepository()
          .getLocalizationsList(_queryParamsFor(selectedLocale));

      await _replaceCachedLocalizations(
        locale: selectedLocale,
        localizations: localizationsList.messages
            .map(
              (e) => Localization()
                ..message = e.message
                ..code = e.code
                ..locale = e.locale
                ..module = e.module,
            )
            .toList(),
      );

      await appLocalizations.load();
    } catch (_) {
      // Keep startup/offline behavior non-blocking. Cached translations remain
      // loaded when present; otherwise UI falls back to localization keys.
    }

    emit(LocalizationState.selected(locale: selectedLocale));
  }

  AppLocalizations _appLocalizationsFor(String locale) {
    final splitLocale = locale.split('_');
    final languageCode = splitLocale.first;
    final countryCode = splitLocale.length > 1 ? splitLocale[1] : null;
    return AppLocalizations(Locale(languageCode, countryCode), isar);
  }

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
  }) async {
    await isar.writeTxn(() async {
      final existing = await isar.localizationWrappers
          .filter()
          .localeEqualTo(locale)
          .findAll();

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
  const factory LocalizationState.selected({
    required String? locale,
  }) = _LocaleSelectedState;
}
