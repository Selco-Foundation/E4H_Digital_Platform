import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:digit_forms_engine/utils/utils.dart' as forms_utils;

bool isAssessmentPropertyVisible({
  required String pageKey,
  required dynamic property,
  required Map<String, dynamic> values,
}) {
  if (property.hidden == true) return false;
  final condition = property.visibilityCondition;
  if (condition == null || condition.expression.trim().isEmpty) return true;
  final flattened = <String, dynamic>{};
  values.forEach((page, pageValues) {
    if (pageValues is Map) {
      pageValues.forEach((field, value) {
        flattened['$page.$field'] = value ?? '';
      });
    }
  });
  return forms_utils.evaluateVisibilityExpression(
    condition.expression,
    flattened,
  );
}

Map<String, dynamic> buildAssessmentSubmissionData(SchemaObject schema) {
  final values = <String, dynamic>{
    for (final page in schema.pages.entries)
      page.key: <String, dynamic>{
        for (final property in page.value.properties?.entries ??
            <MapEntry<String, PropertySchema>>[])
          property.key: property.value.value,
      },
  };
  final output = <String, dynamic>{};
  for (final page in schema.pages.entries) {
    for (final propertyEntry in page.value.properties?.entries ??
        <MapEntry<String, PropertySchema>>[]) {
      final property = propertyEntry.value;
      if (property.readOnly == true || property.displayOnly == true) continue;
      if (!isAssessmentPropertyVisible(
        pageKey: page.key,
        property: property,
        values: values,
      )) {
        continue;
      }
      var value = property.value;
      if (value == null || (value is String && value.trim().isEmpty)) continue;
      if (property.isMultiSelect == true && value is String) {
        value = value
            .split('.')
            .map((entry) => entry.trim())
            .where((entry) => entry.isNotEmpty)
            .toList(growable: false);
      }
      output[propertyEntry.key] = value;
    }
  }
  return output;
}
