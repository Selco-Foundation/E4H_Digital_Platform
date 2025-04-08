import 'package:selco/blocs/localization/app_localization.dart';
import 'package:selco/models/app_config/mdms_response.dart';
import 'package:flutter/material.dart';
import 'package:isar/isar.dart';

//the aim here is to override methods that are defined by default in the localizations delegate file
class AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  final AppConfig? appConfig;
  final Isar isar;

  const AppLocalizationsDelegate(this.appConfig, this.isar);

  //check from configuration if the language is supported in the app
  @override
  bool isSupported(Locale locale) {
    return (appConfig!.appConfig?[0].languages)!
        .map((e) {
          final results = e.value.split('_');
          if (results.isNotEmpty) return results.first;
          return null;
        })
        .contains(locale.languageCode);
  }

  @override
  bool shouldReload(covariant LocalizationsDelegate<AppLocalizations> old) =>
      true;

  //load localizations from storage
  @override
  Future<AppLocalizations> load(Locale locale) async {
    AppLocalizations appLocalizations = AppLocalizations(locale, isar);
    await appLocalizations.load();
    return appLocalizations;
  }
}
