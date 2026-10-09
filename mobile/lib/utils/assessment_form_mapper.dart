import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:digit_forms_engine/utils/utils.dart' as forms_utils;

import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_queue.dart';

Map<String, dynamic> buildAssessmentDraftDefaults({
  required AssessmentSubmissionRequest request,
  required AssessmentQueueFacility facility,
  Map<String, dynamic>? facilityDefaults,
}) {
  String display(String? value) {
    final normalized = value?.trim();
    return normalized == null || normalized.isEmpty ? '---' : normalized;
  }

  final location = [facility.state, facility.district, facility.block]
      .map((value) => value?.trim())
      .whereType<String>()
      .where((value) => value.isNotEmpty)
      .join(', ');
  return {
    'facilityName': display(facility.facilityName),
    'facilityType': display(facility.facilityType),
    'facilityAddress': display(
      facility.address ?? (location.isEmpty ? null : location),
    ),
    'facilityCode': display(facility.facilityCode),
    ...?facilityDefaults,
    ...request.submissionData,
  };
}

Map<String, dynamic> normalizeAssessmentDraftDefaultsForPage({
  required PropertySchema page,
  required Map<String, dynamic> defaults,
}) {
  final normalized = Map<String, dynamic>.from(defaults);
  for (final entry
      in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[]) {
    final value = normalized[entry.key];
    if (entry.value.isMultiSelect == true && value is List) {
      normalized[entry.key] = value
          .map((item) => item.toString().trim())
          .where((item) => item.isNotEmpty)
          .join('.');
    }
  }
  return normalized;
}

Map<String, dynamic> buildAssessmentFacilityDefaults({
  required AssessmentFacilityDetails? facility,
  required AssessmentFormType formType,
  AssessmentQueueFacility? fallbackFacility,
}) {
  String display(String? value) {
    final normalized = value?.trim();
    return normalized == null || normalized.isEmpty ? '---' : normalized;
  }

  String editable(String? value) => value?.trim() ?? '';
  String? firstValue(Iterable<String?> values) {
    for (final value in values) {
      final normalized = value?.trim();
      if (normalized != null && normalized.isNotEmpty) return normalized;
    }
    return null;
  }

  final fallbackLocation = [
    fallbackFacility?.state,
    fallbackFacility?.district,
    fallbackFacility?.block,
  ]
      .map((value) => value?.trim())
      .whereType<String>()
      .where((value) => value.isNotEmpty)
      .join(', ');
  final isAwc = formType == AssessmentFormType.AWC_PHONE ||
      formType == AssessmentFormType.AWC_FIELD;
  final inChargeName = firstValue([
    facility?.facilityPocName,
    fallbackFacility?.facilityInCharge.name,
  ]);
  final inChargeContact = firstValue([
    facility?.facilityPocPhone,
    fallbackFacility?.facilityInCharge.phone,
  ]);
  final alternateName = fallbackFacility?.alternativeContact.name;
  final alternateContact = fallbackFacility?.alternativeContact.phone;
  return {
    'facilityName': display(
      firstValue([facility?.facilityName, fallbackFacility?.facilityName]),
    ),
    'facilityType': display(
      firstValue([facility?.facilityType, fallbackFacility?.facilityType]),
    ),
    'facilityAddress': display(
      firstValue([
        facility?.formattedAddress,
        fallbackFacility?.address,
        fallbackLocation,
      ]),
    ),
    'facilityCode': display(
      firstValue([
        facility?.facilityId,
        fallbackFacility?.facilityCode,
        fallbackFacility?.facilityId,
      ]),
    ),
    'facilityInChargeName':
        isAwc ? editable(inChargeName) : display(inChargeName),
    'facilityInChargeContact':
        isAwc ? editable(inChargeContact) : display(inChargeContact),
    'facilityInChargeDesignation': '',
    'alternateContactName':
        isAwc ? editable(alternateName) : display(alternateName),
    'alternateContactDesignation': '',
    'alternateContactNumber':
        isAwc ? editable(alternateContact) : display(alternateContact),
    'ninId': display(facility?.ninId),
    'hfrId': display(facility?.hfrId),
  };
}

bool isAssessmentPropertyVisible({
  required String pageKey,
  required dynamic property,
  required Map<String, dynamic> values,
}) {
  if (property.hidden == true) return false;
  final otherSource = property.schemaCode?.toString();
  if (otherSource != null && otherSource.startsWith('ASSESSMENT_OTHER_FOR:')) {
    final sourceField = otherSource.substring('ASSESSMENT_OTHER_FOR:'.length);
    final pageValues = values[pageKey];
    final raw = pageValues is Map ? pageValues[sourceField] : null;
    return raw
        .toString()
        .split('.')
        .map((value) => value.trim().toUpperCase())
        .contains('OTHER');
  }
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
