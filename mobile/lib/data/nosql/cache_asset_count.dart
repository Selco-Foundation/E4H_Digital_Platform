import 'package:isar/isar.dart';

part 'cache_asset_count.g.dart';

@Collection()
class CacheAssetCount {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;
  @Index()
  late String assetType;

  late int count;
  int? progress = 0;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAssetCount({
    required this.activityFacilityId,
    required this.assetType,
    this.count = 0,
    this.progress = 0,
  });
}
