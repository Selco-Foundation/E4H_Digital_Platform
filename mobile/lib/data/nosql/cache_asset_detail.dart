import 'package:isar/isar.dart';

part 'cache_asset_detail.g.dart';

@Collection()
class CacheAssetDetail {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String assetType;

  late String? warranty;
  late String brand;
  late String? model;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAssetDetail({
    required this.activityFacilityId,
    required this.assetType,
    this.warranty,
    required this.brand,
    this.model,
  });
}
