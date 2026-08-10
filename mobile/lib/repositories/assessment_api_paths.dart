class AssessmentApiPaths {
  const AssessmentApiPaths._();

  static const serviceBase = 'field-planner/assessment/v1';
  static const queueSearch = '$serviceBase/submission/queue/_search';
  static const formResolve = '$serviceBase/submission/form/_resolve';
  static const phoneSubmission = '$serviceBase/submission/phone/_create';
}
