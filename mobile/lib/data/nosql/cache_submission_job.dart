import 'package:isar/isar.dart';

part 'cache_submission_job.g.dart';

@Collection()
class CacheSubmissionJob {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String activityFacilityId;

  /// 'queued' | 'running' | 'success' | 'failed'
  @Index()
  late String status;

  String? error;
  DateTime updatedAt = DateTime.now();

  CacheSubmissionJob({
    required this.activityFacilityId,
    required this.status,
    this.error,
  });
}
