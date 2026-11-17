import 'package:digit_forms_engine/blocs/app_localization.dart';
import 'package:digit_scanner/blocs/app_localization.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import 'app_localization.dart';

// The package loaders clear shared strings before awaiting their data. Use the
// completed app snapshot instead, keeping old screens translated during refresh.
class ScannerTranslationsDelegate
    extends LocalizationsDelegate<ScannerLocalization> {
  final AppLocalizations translations;
  const ScannerTranslationsDelegate(this.translations);

  @override
  bool isSupported(Locale locale) => locale == translations.locale;
  @override
  bool shouldReload(ScannerTranslationsDelegate old) =>
      old.translations != translations;
  @override
  Future<ScannerLocalization> load(Locale locale) =>
      SynchronousFuture(_ScannerTranslations(translations));
}

class _ScannerTranslations extends ScannerLocalization {
  final AppLocalizations translations;
  _ScannerTranslations(this.translations)
      : super(translations.locale, SynchronousFuture(const []), const []);
  @override
  String translate(String key) => translations.translate(key);
}

class FormTranslationsDelegate extends LocalizationsDelegate<FormLocalization> {
  final AppLocalizations translations;
  const FormTranslationsDelegate(this.translations);

  @override
  bool isSupported(Locale locale) => locale == translations.locale;
  @override
  bool shouldReload(FormTranslationsDelegate old) =>
      old.translations != translations;
  @override
  Future<FormLocalization> load(Locale locale) =>
      SynchronousFuture(_FormTranslations(translations));
}

class _FormTranslations extends FormLocalization {
  final AppLocalizations translations;
  _FormTranslations(this.translations)
      : super(translations.locale, SynchronousFuture(const []), const []);
  @override
  String translate(String key) => translations.translate(key);
}
