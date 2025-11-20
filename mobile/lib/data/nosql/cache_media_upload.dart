import 'package:isar/isar.dart';

part 'cache_media_upload.g.dart';

@Collection()
class CacheMediaUpload {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String assetType;

  @Index()
  late String userType;

  late String itemNumber;
  late String itemType;
  late String filePath;
  late String latitude;
  late String longitude;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheMediaUpload({
    required this.activityFacilityId,
    required this.assetType,
    required this.itemNumber,
    required this.itemType,
    required this.filePath,
    required this.latitude,
    required this.longitude,
    required this.userType,
  });
}
