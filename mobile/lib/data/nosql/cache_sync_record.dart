import 'package:isar/isar.dart';

part 'cache_sync_record.g.dart';

@Collection()
class CacheSyncRecord {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String userType;

  late DateTime syncedAt;

  CacheSyncRecord({
    required this.userType,
    required this.syncedAt,
  });
}
