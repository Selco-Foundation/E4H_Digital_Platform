import 'package:intl/intl.dart';

/// Keeps locale-aware formatting and parsing in `intl` aligned with the
/// locale selected by the Flutter application.
void syncIntlDefaultLocale(String? localeName) {
  final locale = localeName?.trim();
  if (locale == null || locale.isEmpty) return;

  Intl.defaultLocale = Intl.canonicalizedLocale(locale);
}
