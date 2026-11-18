import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:digit_forms_engine/json_forms.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../../lib/utils/form_time_value.dart';
import '../../lib/utils/form_date_value.dart';

void main() {
  Map<String, dynamic> schema() => {
        'name': 'AnyForm',
        'version': 1,
        'pages': {
          'page': {
            'type': 'object',
            'properties': {
              'arbitrary_new_key': {'type': 'string', 'format': 'time'},
              'another_key': {
                'type': 'string',
                'format': 'time',
                'readOnly': true
              },
              'date': {'type': 'string', 'format': 'date'},
              'text': {'type': 'string', 'format': 'text'},
            },
          },
        },
      };

  test('adapts arbitrary time keys without mutating cached JSON', () {
    final raw = schema();
    final parsed = SchemaObject.fromJson(adaptTimeSchema(raw));
    final page = parsed.pages['page']!;
    expect(
      raw['pages']['page']['properties']['arbitrary_new_key']['format'],
      'time',
    );
    expect(isTimeProperty(page.properties!['arbitrary_new_key']!), isTrue);
    expect(isTimeProperty(page.properties!['another_key']!), isTrue);
    expect(page.properties!['another_key']!.readOnly, isTrue);
    expect(isTimeProperty(page.properties!['text']!), isFalse);
    final copied = PropertySchema.fromJson(
      page.properties!['arbitrary_new_key']!.copyWith(value: '09:05').toJson(),
    );
    expect(isTimeProperty(copied), isTrue);
    expect(adaptTimeSchema(adaptTimeSchema(raw)), adaptTimeSchema(raw));
  });

  test('clock values and timestamps retain written hour and minute', () {
    for (final entry in {
      '00:00': '00:00',
      '12:00': '12:00',
      '14:30': '14:30',
      '9:05': '09:05',
      '09:05:45': '09:05',
      '2026-11-18T09:05:59.123Z': '09:05',
      '2026-11-18T09:05:00+05:30': '09:05',
      '2026-11-18 09:05:00-0800': '09:05',
    }.entries) {
      expect(normalizeTimeValue(entry.key), entry.value);
    }
    expect(normalizeTimeValue(DateTime.utc(2026, 11, 18, 9, 5)), '09:05');
    for (final value in [null, '', 'bad', '24:00', '12:60', '2026-11-18']) {
      expect(normalizeTimeValue(value), value);
    }
  });

  test('controls normalize defaults and reject invalid nonempty times', () {
    final page =
        SchemaObject.fromJson(adaptTimeSchema(schema())).pages['page']!;
    final form = FormGroup(JsonForms.getFormControls(page));
    form.control('arbitrary_new_key').value = '2026-11-18T14:30:00Z';
    normalizeTimeControls(form, page);
    expect(form.control('arbitrary_new_key').value, '14:30');
    expect(form.control('arbitrary_new_key').valid, isTrue);
    form.control('arbitrary_new_key').value = 'not a time';
    expect(form.control('arbitrary_new_key').invalid, isTrue);
    form.control('arbitrary_new_key').value = '';
    expect(form.control('arbitrary_new_key').valid, isTrue);
    expect(
      formValueForStorage(
        page.properties!['arbitrary_new_key']!,
        '2026-11-18T14:30:00+05:30',
      ),
      '14:30',
    );
    expect(
      formValueForStorage(page.properties!['date']!, DateTime(2026, 11, 18)),
      '2026-11-18',
    );
    expect(
      formValueForStorage(page.properties!['text']!, '2026-11-18T14:30:00Z'),
      '2026-11-18T14:30:00Z',
    );
  });
}
