import 'package:flutter/material.dart';
import 'package:isar_community/isar.dart';

import '../../data/nosql/localization.dart';
import '../../model/appconfig/mdmsResponse.dart';
import '../../model/localization/localizationModel.dart';
import 'app_localization_delegate.dart';

class AppLocalizations {
  final Locale locale;
  final Isar isar;

  AppLocalizations(this.locale, this.isar, {List<Localization>? messages}) {
    if (messages != null) _setMessages(messages);
  }

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static LocalizationModel? localizationModel;

  Map<String, String> _localizedStrings = const {};

  bool get hasTranslations => _localizedStrings.isNotEmpty;

  bool _setMessages(List<Localization> messages) {
    final translated = <String, String>{
      for (final message in messages)
        if (message.locale == locale.toString() &&
            message.code.trim().isNotEmpty &&
            message.message.trim().isNotEmpty)
          message.code: message.message,
    };
    if (translated.isEmpty) return false;
    _localizedStrings = Map.unmodifiable(translated);
    return true;
  }

  static LocalizationsDelegate<AppLocalizations> getDelegate(
          AppConfig config, Isar isar,
          {List<Localization>? messages}) =>
      AppLocalizationsDelegate(config, isar, messages: messages);

  Future<bool> load() async {
    final List<LocalizationWrapper> localizationList = await isar
        .localizationWrappers
        .filter()
        .localeEqualTo('${locale.languageCode}_${locale.countryCode}')
        .findAll();

    if (localizationList.isNotEmpty) {
      final messages = localizationList.first.localization ?? [];
      return _setMessages(messages);
    }

    return false;
  }

  String translate(String localizedValues) =>
      _localizedStrings[localizedValues] ?? localizedValues;
}
