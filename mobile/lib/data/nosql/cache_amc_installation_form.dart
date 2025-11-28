import 'package:isar/isar.dart';

part 'cache_amc_installation_form.g.dart';

@Collection()
class CacheAmcInstallationForm {
  Id id = Isar.autoIncrement;

  @Index()
  late String scheduledVisitId;

  @Index()
  late String userType;

  late String filePath;
  late String latitude;
  late String longitude;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAmcInstallationForm({
    required this.scheduledVisitId,
    required this.filePath,
    required this.latitude,
    required this.longitude,
    required this.userType,
  });
}
