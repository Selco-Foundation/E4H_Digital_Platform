import 'package:isar/isar.dart';

part 'cache_submission_job.g.dart';

@Collection()
class CacheSubmissionJob {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String activityFacilityId;

  /// 'submit' | 'reject' | 'send_back' | 'submit_visit'
  @Index(caseSensitive: false)
  late String operationType;

  /// 'queued' | 'running' | 'partial' | 'success' | 'failed'
  @Index()
  late String status;

  @Index(caseSensitive: false)
  late String stageKey;

  late String stageLabel;
  int completedSteps = 0;
  int totalSteps = 1;
  int progressPercent = 0;
  int retryCount = 0;
  bool isBlocking = true;
  String? lastError;
  DateTime updatedAt = DateTime.now();

  CacheSubmissionJob({
    required this.activityFacilityId,
    required this.operationType,
    required this.status,
    required this.stageKey,
    required this.stageLabel,
    this.completedSteps = 0,
    this.totalSteps = 1,
    this.progressPercent = 0,
    this.retryCount = 0,
    this.isBlocking = true,
    this.lastError,
  });
}
