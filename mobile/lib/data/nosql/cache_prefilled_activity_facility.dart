import 'package:isar/isar.dart';

part 'cache_prefilled_activity_facility.g.dart';

@Collection()
class CachePrefilledActivityFacility {
  Id id = Isar.autoIncrement;

  @Index(
      composite: [CompositeIndex('userType')],
      unique: true,
      caseSensitive: true)
  late String activityFacilityId;

  late String userType;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CachePrefilledActivityFacility({
    required this.activityFacilityId,
    required this.userType,
  });
}
