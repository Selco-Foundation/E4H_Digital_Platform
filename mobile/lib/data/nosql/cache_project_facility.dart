import 'package:isar/isar.dart';

part 'cache_project_facility.g.dart';

@Collection()
class CacheProjectFacility {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;
  @Index()
  late String facilityId;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheProjectFacility({
    required this.projectId,
    required this.facilityId,
  });
}
