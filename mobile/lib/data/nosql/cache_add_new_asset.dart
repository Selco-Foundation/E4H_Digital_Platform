import 'package:isar/isar.dart';

part 'cache_add_new_asset.g.dart';

@Collection()
class CacheAddNewAsset {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;

  @Index()
  late String assetType;

  late String itemNumber;
  late String serialNumber;
  late String photoPath;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAddNewAsset({
    required this.projectId,
    required this.assetType,
    required this.itemNumber,
    required this.serialNumber,
    required this.photoPath,
  });
}
