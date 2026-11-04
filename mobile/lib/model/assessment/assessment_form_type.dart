// Backend wire values intentionally use upper snake case.
// ignore_for_file: constant_identifier_names

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
