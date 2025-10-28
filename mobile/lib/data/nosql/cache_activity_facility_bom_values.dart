import 'package:isar/isar.dart';

part 'cache_activity_facility_bom_values.g.dart';

@collection
class CacheActivityFacilityBomValues {
  Id id = Isar.autoIncrement;

  @Index(caseSensitive: false)
  late String activityFacilityId;

  @Index(caseSensitive: false)
  late String userType;

  @Index(unique: true, replace: true, caseSensitive: false)
  late String entryKey; // "$projectId::$userType"

  late String dataJson;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;
}
