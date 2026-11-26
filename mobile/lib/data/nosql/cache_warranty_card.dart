import 'package:isar_community/isar.dart';

part 'cache_warranty_card.g.dart';

@Collection()
class CacheWarrantyCard {
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

  CacheWarrantyCard({
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
