import 'package:isar/isar.dart';

part 'cache_activity_facility_asset.g.dart';

@Collection()
class CacheActivityFacilityAsset {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String activityFacilityId;

  int progress = 0;
  DateTime createdAt = DateTime.now();
  DateTime updatedAt = DateTime.now();

  CacheActivityFacilityAsset({
    required this.activityFacilityId,
    this.progress = 0,
  });
}
