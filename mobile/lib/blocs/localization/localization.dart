import 'dart:async';
import 'dart:ui';

import 'package:selco/blocs/localization/app_localization.dart';
import 'package:selco/data/app_shared_preferences.dart';
import 'package:selco/data/local_store/nosql/localization.dart';
import 'package:selco/data/repositories/local/localization.dart';
import 'package:selco/models/app_config/mdms_response.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

part 'localization.freezed.dart';

class LocalizationBloc extends Bloc<LocalizationEvent, LocalizationState> {
  // late LocalizationModel localizationsList;
  final Isar isar;

  String? _locale;

  LocalizationBloc(this.isar) : super(const LocalizationState.initial()) {
    on<_LocaleSelectedEvent>(onLocaleSelected);
  }

  String? get locale => _locale;

  FutureOr<void> onLocaleSelected(
    _LocaleSelectedEvent event,
    Emitter<LocalizationState> emit,
  ) async {
    // Use "en_US" as a default if event.locale is null or empty.
    final localeStr =
        (event.locale != null && event.locale!.isNotEmpty)
            ? event.locale!
            : "en_US";
    _locale = localeStr;
    AppSharedPreferences().setSelectedLocale(_locale!);

    // Attempt to split the locale string into language and country.
    final splitLocale = localeStr.split('_');
    final languageCode = splitLocale.isNotEmpty ? splitLocale.first : "en";
    final countryCode = splitLocale.length > 1 ? splitLocale[1] : "US";

    // Initialize appLocalizations for searching ISAR or setting locmodel
    AppLocalizations appLocalizations = AppLocalizations(
      Locale(languageCode, countryCode),
      isar,
    );

    // Attempt to fetch from ISAR
    try {
      final localizationRepository = LocalizationRepository();

      // Define parameters in case we need to fetch localizations from online.
      // The module name list is essentially a list of all the use cases or modules we need our localizations for.
      List<String?> moduleNameList = [];
      if (event.moduleList != null) {
        for (var list in event.moduleList!.interfaces!) {
          if (!list.name!.contains(RegExp(r'[A-Z]'))) {
            moduleNameList.add(list.name);
          }
        }
      }
      final Map<String, String> queryParam = {
        'locale': localeStr,
        'module': moduleNameList.join(','),
        'tenantId': 'mz',
        // TODO: Need to read this from env file
      };

      // Attempt to fetch from ISAR.
      var localizationsListFetched = await appLocalizations.load();

      // If localizations couldn't be fetched from ISAR, fetch them online.
      if (localizationsListFetched == false) {
        final localizationsList = await localizationRepository
            .getLocalizationsList(queryParam);

        // Once we have the localizations from the server, we can save them in ISAR for future access.
        try {
          final localizationsListObject =
              LocalizationWrapper()
                ..locale = localeStr
                ..localization =
                    localizationsList.messages
                        .map(
                          (e) =>
                              Localization()
                                ..message = e.message
                                ..code = e.code
                                ..locale = e.locale
                                ..module = e.module,
                        )
                        .toList();

          await isar.writeTxn(() async {
            await isar.localizationWrappers.put(localizationsListObject);
          });
        } catch (err) {
          rethrow;
        }
      }

      // Emit the selected locale state.
      emit(LocalizationState.selected(locale: localeStr));
    } catch (err) {
      rethrow;
    }
  }
}

@freezed
class LocalizationEvent with _$LocalizationEvent {
  const factory LocalizationEvent.onSelect({
    String? locale,
    InterfacesList? moduleList,
  }) = _LocaleSelectedEvent;
}

@freezed
class LocalizationState with _$LocalizationState {
  const factory LocalizationState.initial() = _LocaleNotSelectedState;
  const factory LocalizationState.selected({required String? locale}) =
      _LocaleSelectedState;
}
