import 'package:isar/isar.dart';

part 'cache_specification.g.dart';

@Collection()
class CacheSpecification {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String assetType;

  late String system;
  late double totalCapacity;
  late String totalCapacityUnit;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheSpecification({
    required this.activityFacilityId,
    required this.assetType,
    required this.system,
    required this.totalCapacity,
    required this.totalCapacityUnit,
  });
}
