import 'package:isar/isar.dart';

part 'cache_assessment_queue.g.dart';

@Collection()
class CacheAssessmentQueue {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String cacheKey;

  @Index()
  late String tenantId;
  @Index()
  late String assessorId;
  @Index()
  late String phase;

  late String planFacilityId;
  late String facilityName;
  int? lastActionTime;
  late String facilityJson;

  CacheAssessmentQueue({
    required this.cacheKey,
    required this.tenantId,
    required this.assessorId,
    required this.phase,
    required this.planFacilityId,
    required this.facilityName,
    required this.facilityJson,
    this.lastActionTime,
  });
}
