import 'package:isar/isar.dart';

part 'cache_operation_checkpoint.g.dart';

@Collection()
class CacheOperationCheckpoint {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String entryKey;

  late String activityFacilityId;

  late String operationType;
  late String checkpointKey;
  late String itemKey;

  /// 'pending' | 'success' | 'failed'
  @Index(caseSensitive: false)
  late String status;

  String? payloadJson;
  String? remoteId;
  String? error;
  DateTime updatedAt = DateTime.now();

  CacheOperationCheckpoint({
    required this.entryKey,
    required this.activityFacilityId,
    required this.operationType,
    required this.checkpointKey,
    required this.itemKey,
    required this.status,
    this.payloadJson,
    this.remoteId,
    this.error,
  });
}
