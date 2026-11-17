import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:isar_community/isar.dart';

import '../../model/appconfig/mdmsResponse.dart';
import 'app_localization.dart';
import '../../data/nosql/localization.dart';

//the aim here is to override methods that are defined by default in the localizations delegate file
class AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  final AppConfig? appConfig;
  final Isar isar;
  final List<Localization>? messages;

  const AppLocalizationsDelegate(this.appConfig, this.isar, {this.messages});

  //check from configuration if the language is supported in the app
  @override
  bool isSupported(Locale locale) {
    return (appConfig!.appConfig?[0].languages)!.map((e) {
      final results = e.value.split('_');
      if (results.isNotEmpty) return results.first;
      return null;
    }).contains(locale.languageCode);
  }

  @override
  bool shouldReload(covariant LocalizationsDelegate<AppLocalizations> old) =>
      true;

  //load localizations from storage
  @override
  Future<AppLocalizations> load(Locale locale) {
    final localization = AppLocalizations(locale, isar, messages: messages);
    if (messages != null) return SynchronousFuture(localization);
    return localization.load().then((_) => localization);
  }
}
