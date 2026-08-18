import 'package:isar/isar.dart';

part 'cache_installation_image.g.dart';

@Collection()
class CacheInstallationImage {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String userType;

  @Index()
  late String code;

  String order = '';

  late String photoPath;
  late String latitude;
  late String longitude;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheInstallationImage({
    required this.activityFacilityId,
    required this.userType,
    required this.code,
    this.order = '',
    required this.photoPath,
    required this.latitude,
    required this.longitude,
  });
}
