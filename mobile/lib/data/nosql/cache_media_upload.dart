import 'package:isar/isar.dart';

part 'cache_media_upload.g.dart';

@Collection()
class CacheMediaUpload {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;

  @Index()
  late String assetType;

  late String itemNumber;
  late String itemType;
  late String photoPath;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheMediaUpload({
    required this.projectId,
    required this.assetType,
    required this.itemNumber,
    required this.itemType,
    required this.photoPath,
  });
}
