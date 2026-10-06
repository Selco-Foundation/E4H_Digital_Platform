// Backend wire values intentionally use upper snake case.
// ignore_for_file: constant_identifier_names

import 'assessment_mode.dart';

enum AssessmentPhase {
  PHONE,
  FIELD;

  static AssessmentPhase? fromCode(String? value) {
    final code = value?.trim().toUpperCase();
    if (code == null || code.isEmpty) return null;
    for (final phase in values) {
      if (phase.name == code) return phase;
    }
    return null;
  }
}

enum AssessmentFormType {
  HF_PHONE,
  AWC_PHONE,
  HF_FIELD,
  AWC_FIELD;

  static AssessmentFormType? fromCode(String? value) {
    final code = value?.trim().toUpperCase();
    if (code == null || code.isEmpty) return null;
    for (final type in values) {
      if (type.name == code) return type;
    }
    return null;
  }

  static AssessmentFormType? expectedFor({
    required String facilityCategory,
    required AssessmentMode mode,
  }) {
    final category = facilityCategory.trim().toUpperCase();
    return switch ((category, mode)) {
      ('HEALTH', AssessmentMode.remote) => HF_PHONE,
      ('ANGANWADI', AssessmentMode.remote) => AWC_PHONE,
      ('HEALTH', AssessmentMode.onSite) => HF_FIELD,
      ('ANGANWADI', AssessmentMode.onSite) => AWC_FIELD,
      _ => null,
    };
  }

  AssessmentPhase get phase => switch (this) {
        HF_PHONE || AWC_PHONE => AssessmentPhase.PHONE,
        HF_FIELD || AWC_FIELD => AssessmentPhase.FIELD,
      };

  AssessmentMode get mode => switch (phase) {
        AssessmentPhase.PHONE => AssessmentMode.remote,
        AssessmentPhase.FIELD => AssessmentMode.onSite,
      };

  String get facilityCategory => switch (this) {
        HF_PHONE || HF_FIELD => 'HEALTH',
        AWC_PHONE || AWC_FIELD => 'ANGANWADI',
      };

  String get schemaName => 'Assessment.$name';
}
