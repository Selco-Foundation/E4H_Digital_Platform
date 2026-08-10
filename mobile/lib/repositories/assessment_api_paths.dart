class AssessmentApiPaths {
  const AssessmentApiPaths._();

  static const serviceBase = 'field-planner/assessment/v1';
  static const queueSearch = '$serviceBase/submission/queue/_search';
  static const formResolve = '$serviceBase/submission/form/_resolve';
  static const phoneSubmission = '$serviceBase/submission/phone/_create';
  static const phoneUnableToContact =
      '$serviceBase/submission/phone/_unable-to-contact';
  static const fieldSubmission = '$serviceBase/submission/field/_create';
  static const facilitySearch = 'facility-service/v2/facility/search';
}
