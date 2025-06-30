import 'package:isar/isar.dart';

part 'cache_completion_report.g.dart';

@Collection()
class CacheCompletionReport {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;
  late String filePath;
  late String latitude;
  late String longitude;
  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheCompletionReport({
    required this.projectId,
    required this.filePath,
    required this.latitude,
    required this.longitude,
  });
}
