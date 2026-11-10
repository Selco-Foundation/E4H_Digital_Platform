import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/utils/form_date_value.dart';
import '../../lib/utils/utils.dart';

void main() {
  final date = PropertySchema.fromJson({'type': 'string', 'format': 'date'});

  test('selected calendar date survives storage without a timezone shift', () {
    final selected = DateTime(2026, 11, 4);
    final stored = formValueForStorage(date, selected);
    expect(stored, '2026-11-04');
    expect(jsonSafe({'dc_date_of_recording': stored}),
        {'dc_date_of_recording': '2026-11-04'});
    expect(DateTime.parse(stored), selected);
  });

  test('date-only values and legacy displayed dates retain their components',
      () {
    expect(formValueForStorage(date, '2026-11-04'), '2026-11-04');
    expect(formValueForStorage(date, '2026-11-04T18:30:00.000Z'), '2026-11-04');
    expect(
        formValueForStorage(date, DateTime.utc(2026, 11, 4, 23)), '2026-11-04');
  });

  test('empty values and non-date timestamps are unchanged', () {
    expect(formValueForStorage(date, null), isNull);
    expect(formValueForStorage(date, ''), '');
    final text = PropertySchema.fromJson({'type': 'string', 'format': 'text'});
    final timestamp = DateTime.utc(2026, 11, 4, 18, 30);
    expect(formValueForStorage(text, timestamp), same(timestamp));
    expect(jsonSafe(formValueForStorage(text, timestamp)),
        '2026-11-04T18:30:00.000Z');
  });

  test('nested date fields are normalized without changing other values', () {
    final object = PropertySchema.fromJson({
      'type': 'object',
      'properties': {
        'date': {'type': 'string', 'format': 'date'},
        'weather': {'type': 'string', 'format': 'text'},
      },
    });
    expect(
        formValueForStorage(object, {
          'date': DateTime(2026, 11, 4),
          'weather': 'PARTLY',
        }),
        {'date': '2026-11-04', 'weather': 'PARTLY'});
  });
}
