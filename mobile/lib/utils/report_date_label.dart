import 'i18_key_constants.dart' as i18;

/// Only the new workflow date labels need fallbacks before server translations
/// become available. Existing labels retain their localization behavior.
String reportDateLabel(String key, String Function(String) translate) {
  final translated = translate(key);
  if (translated != key) return translated;
  if (key == i18.common.approvedDate) return 'Approved Date';
  if (key == i18.common.rejectedDate) return 'Rejected Date';
  return translated;
}
