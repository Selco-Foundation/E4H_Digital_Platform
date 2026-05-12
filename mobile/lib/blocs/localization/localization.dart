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
    on<_LocaleSelectedEvent>(onLocaleSelected);
  }

  String? get locale => _locale;

  FutureOr<void> onLocaleSelected(
      _LocaleSelectedEvent event, Emitter<LocalizationState> emit) async {
    _locale = event.locale;
    AppSharedPreferences().setSelectedLocale(_locale!);

    //Search for localizations
    try {
      final localizationRepository = LocalizationRepository();
      List<String?> moduleNameList = [
        'rainmaker-common',
      ];
      final Map<String, String> queryParam = {
        'locale': 'en_IN',
        'module': moduleNameList.join(','),
        'tenantId': envConfig.variables.tenantId
      };

      //initialize appLocalizations for searching ISAR or setting locmodel
      final splitLocale = event.locale!.split('_');
      AppLocalizations appLocalizations =
          AppLocalizations(Locale(splitLocale[0], splitLocale[1]), isar);

      var localizationsListFetched = await appLocalizations.load();

      //fetch localizationList online if localizations could not be fetched from ISAR
      if (localizationsListFetched == false) {
        final localizationsList =
            await localizationRepository.getLocalizationsList(queryParam);

        //once we have the localizations from the server, we can save them in ISAR
        //for future access
        try {
          final localizationsListObject = LocalizationWrapper()
            ..locale = event.locale!
            ..localization = localizationsList.messages
                .map((e) => Localization()
                  ..message = e.message
                  ..code = e.code
                  ..locale = e.locale
                  ..module = e.module)
                .toList();

          await isar.writeTxn(() async {
            await isar.localizationWrappers
                .put(localizationsListObject); // insert & update
          });
        } catch (err) {
          rethrow;
        }
      }

      emit(LocalizationState.selected(locale: event.locale));
    } catch (err) {
      rethrow;
    }
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
