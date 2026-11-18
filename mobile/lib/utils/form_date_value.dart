import 'package:digit_forms_engine/models/property_schema/property_schema.dart';

import 'form_time_value.dart';

/// Calendar dates use the selected components, without a timezone conversion.
/// Time fields use HH:mm; other values retain their existing wire format.
dynamic formValueForStorage(PropertySchema property, dynamic value) {
  if (isTimeProperty(property)) return normalizeTimeValue(value);
  if (property.format == PropertySchemaFormat.date) {
    final date = value is DateTime
        ? value
        : value is String
            ? DateTime.tryParse(value)
            : null;
    if (date == null) return value;
    return '${date.year.toString().padLeft(4, '0')}-'
        '${date.month.toString().padLeft(2, '0')}-'
        '${date.day.toString().padLeft(2, '0')}';
  }
  if (value is Map && property.properties != null) {
    return {
      for (final entry in value.entries)
        entry.key: property.properties!.containsKey(entry.key)
            ? formValueForStorage(property.properties![entry.key]!, entry.value)
            : entry.value,
    };
  }
  return value;
}
