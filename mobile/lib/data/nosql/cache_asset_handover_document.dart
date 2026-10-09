import 'package:isar/isar.dart';

part 'cache_asset_handover_document.g.dart';

@Collection()
class CacheAssetHandoverDocument {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String userType;

  @Index(unique: true, replace: true)
  late String entryId;

  @Index(caseSensitive: false)
  late String filePath;

  String? fileName;

  @Index(caseSensitive: false)
  String fileType = 'unknown';

  int? index;

  late String latitude;
  late String longitude;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAssetHandoverDocument({
    required this.activityFacilityId,
    required this.userType,
    required this.entryId,
    required this.filePath,
    required this.latitude,
    required this.longitude,
    this.fileName,
    this.fileType = 'unknown',
    this.index,
  });
}
