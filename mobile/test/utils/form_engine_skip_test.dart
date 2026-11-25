import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/utils/utils.dart';

void main() {
  const page = PropertySchema(
    type: PropertySchemaType.object,
    properties: {
      'visible': PropertySchema(type: PropertySchemaType.string),
      'hidden_not_in_form': PropertySchema(
        type: PropertySchemaType.string,
        hidden: true,
        includeInForm: false,
      ),
      'hidden_null_in_form': PropertySchema(
        type: PropertySchemaType.string,
        hidden: true,
      ),
      'hidden_in_form': PropertySchema(
        type: PropertySchemaType.string,
        hidden: true,
        includeInForm: true,
      ),
    },
  );

  test('skips hidden fields the form engine builds no control for', () {
    expect(isSkippedByFormEngine(page, 'hidden_not_in_form'), isTrue);
    expect(isSkippedByFormEngine(page, 'hidden_null_in_form'), isTrue);
  });

  test('keeps visible fields and hidden fields included in the form', () {
    expect(isSkippedByFormEngine(page, 'visible'), isFalse);
    expect(isSkippedByFormEngine(page, 'hidden_in_form'), isFalse);
  });

  test('unknown key is not skipped, so it is still reported as missing', () {
    expect(isSkippedByFormEngine(page, 'nope'), isFalse);
  });
}
