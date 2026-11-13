import 'package:digit_ui_components/digit_components.dart';
import 'package:flutter/material.dart';

import '../blocs/localization/app_localization.dart';
import 'error_localization.dart';

extension on ThemeData {
  get transparent => DigitTheme.instance.colors.transparent;
}

extension ContextExt on BuildContext {
  ThemeData get theme => Theme.of(this);

  MediaQueryData get mediaQuery => MediaQuery.of(this);

  Size get size => mediaQuery.size;

  double get width => size.width;

  double get height => size.height;

  NavigatorState get navigator => Navigator.of(this);

  ScaffoldMessengerState get scaffoldMessenger => ScaffoldMessenger.of(this);

  get showSnackBar => scaffoldMessenger.showSnackBar;
}

extension LocalizationExtension on BuildContext {
  String localizeError(Object? error) {
    final key = errorMessageKey(error);
    return translate(
      key == 'SESSION_EXPIRED' ? 'CORE_COMMON_TOKEN_EXPIRED_LOGIN_AGAIN' : key,
    );
  }

  String translate(String key) {
    final localization = Localizations.of<AppLocalizations>(
      this,
      AppLocalizations,
    );
    return localization?.translate(key) ?? key;
  }
}
