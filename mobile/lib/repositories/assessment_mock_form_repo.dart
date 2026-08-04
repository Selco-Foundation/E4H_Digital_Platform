import 'dart:convert';

import 'package:flutter/services.dart';

import '../utils/utils.dart';

class AssessmentMockFormRepository {
  static const assetPath = 'assets/mocks/mockAssessmentHFPhoneForm.json';

  Future<Map<String, dynamic>> loadFormSchema({
    String formType = 'HF_PHONE',
  }) async {
    final source = await rootBundle.loadString(assetPath);
    final decoded = jsonDecode(source) as Map<String, dynamic>;
    final records = (decoded['mdms'] as List? ?? const [])
        .whereType<Map>()
        .map((record) => Map<String, dynamic>.from(record))
        .toList();

    final record = records.cast<Map<String, dynamic>?>().firstWhere(
          (candidate) => candidate?['data']?['formType'] == formType,
          orElse: () => null,
        );

    if (record == null) {
      throw StateError('Assessment form $formType was not found in $assetPath');
    }

    final transformed = transformSelcoFormMdmsDocToSchema(record);
    transformed['uniqueIdentifier'] = record['uniqueIdentifier'];
    return transformed;
  }
}
