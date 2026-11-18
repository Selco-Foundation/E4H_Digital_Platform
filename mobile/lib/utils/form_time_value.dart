import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:reactive_forms/reactive_forms.dart';

// This validation also preserves the time semantic through engine copyWith/toJson.
const timeValuePattern = r'^(?:[01]\d|2[0-3]):[0-5]\d$';

bool isTimeProperty(PropertySchema property) =>
    property.format == PropertySchemaFormat.custom &&
    (property.validations?.any(
          (rule) => rule.type == 'pattern' && rule.value == timeValuePattern,
        ) ??
        false);

/// Adapt a rendering copy; the MDMS/cache document retains format: time.
Map<String, dynamic> adaptTimeSchema(Map<String, dynamic> schema) {
  dynamic adapt(dynamic value) {
    if (value is List) return value.map(adapt).toList();
    if (value is! Map) return value;
    final result = <String, dynamic>{
      for (final entry in value.entries)
        entry.key.toString(): adapt(entry.value),
    };
    if (result['format'] == 'time') {
      result['format'] = 'custom';
      result['validations'] = [
        ...?result['validations'] as List?,
        {
          'type': 'pattern',
          'value': timeValuePattern,
          'message': 'Select a valid time',
        },
      ];
      if (result.containsKey('value')) {
        result['value'] = normalizeTimeValue(result['value']);
      }
    }
    return result;
  }

  return adapt(schema) as Map<String, dynamic>;
}

/// Return HH:mm without timezone conversion, or null for blank/invalid values.
String? normalizeTimeValue(dynamic value) {
  if (value is DateTime) {
    return '${value.hour.toString().padLeft(2, '0')}:'
        '${value.minute.toString().padLeft(2, '0')}';
  }
  if (value is! String || value.trim().isEmpty) return null;
  final text = value.trim();
  final clock = RegExp(
    r'^(\d{1,2}):(\d{2})(?::[0-5]\d(?:\.\d+)?)?$',
  ).firstMatch(text);
  final timestamp = RegExp(
    r'^\d{4}-\d{2}-\d{2}[T ](\d{2}):(\d{2})(?::[0-5]\d(?:\.\d+)?)?(?:Z|[+-]\d{2}:?\d{2})?$',
  ).firstMatch(text);
  final match = clock ?? timestamp;
  if (match == null) return null;
  final hour = int.parse(match[1]!);
  final minute = int.parse(match[2]!);
  if (hour > 23 || minute > 59) return null;
  return '${hour.toString().padLeft(2, '0')}:${minute.toString().padLeft(2, '0')}';
}

FormGroup normalizeTimeControls(FormGroup form, PropertySchema page) {
  for (final entry
      in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[]) {
    if (!form.contains(entry.key)) continue;
    final control = form.control(entry.key);
    if (isTimeProperty(entry.value)) {
      control.updateValue(normalizeTimeValue(control.value), emitEvent: false);
    } else if (control is FormGroup) {
      normalizeTimeControls(control, entry.value);
    }
  }
  return form;
}
