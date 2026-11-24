import 'package:isar/isar.dart';

part 'cache_assessment_draft.g.dart';

@Collection()
class CacheAssessmentDraft {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String draftKey;

  @Index(caseSensitive: false)
  late String tenantId;

  @Index(caseSensitive: false)
  late String assessorId;

  @Index(caseSensitive: false)
  late String phase;

  @Index(caseSensitive: false)
  late String status;

  late String planFacilityId;
  late String facilityName;
  late String facilityType;
  String? state;
  String? district;
  String? block;
  String? facilityDefaultsJson;
  late String requestJson;
  String? lastError;
  int attemptCount = 0;
  DateTime createdAt = DateTime.now();
  DateTime updatedAt = DateTime.now();

  CacheAssessmentDraft({
    required this.draftKey,
    required this.tenantId,
    required this.assessorId,
    required this.phase,
    required this.status,
    required this.planFacilityId,
    required this.facilityName,
    required this.facilityType,
    this.state,
    this.district,
    this.block,
    this.facilityDefaultsJson,
    required this.requestJson,
    this.lastError,
    this.attemptCount = 0,
  });
}
