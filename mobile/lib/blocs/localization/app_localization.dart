import 'package:flutter/material.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/localization.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../model/localization/localizationModel.dart';
import 'app_localization_delegate.dart';

class AppLocalizations {
  final Locale locale;
  final Isar isar;

  AppLocalizations(this.locale, this.isar);

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static LocalizationModel? localizationModel;

  static final List<Localization> _localizedStrings = <Localization>[];

  static LocalizationsDelegate<AppLocalizations> getDelegate(
          AppConfig config, Isar isar) =>
      AppLocalizationsDelegate(config, isar);

  Future<bool> load() async {
    _localizedStrings.clear();
    final List<LocalizationWrapper> localizationList = await isar
        .localizationWrappers
        .filter()
        .localeEqualTo('${locale.languageCode}_${locale.countryCode}')
        .findAll();

    if (localizationList.isNotEmpty) {
      _localizedStrings.addAll(localizationList.first.localization!);
      return true;
    }

    return false;
  }

  String translate(String localizedValues) {
    if (_localizedStrings.isEmpty) {
      return localizedValues;
    } else {
      final index = _localizedStrings.indexWhere(
        (medium) => medium.code == localizedValues,
      );

      return index != -1 ? _localizedStrings[index].message : localizedValues;
    }
  }
}
