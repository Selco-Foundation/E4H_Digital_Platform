import 'package:isar/isar.dart';

part 'cache_completion_report.g.dart';

@Collection()
class CacheCompletionReport {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;

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

  CacheCompletionReport({
    required this.projectId,
    required this.filePath,
    required this.entryId,
    required this.latitude,
    required this.longitude,
    this.fileName,
    this.fileType = 'unknown',
    this.index,
  });
}
