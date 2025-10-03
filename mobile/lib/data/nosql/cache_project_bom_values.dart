import 'package:isar/isar.dart';

part 'cache_project_bom_values.g.dart';

@collection
class CacheProjectBomValues {
  Id id = Isar.autoIncrement;

  @Index(caseSensitive: false)
  late String projectId;

  @Index(caseSensitive: false)
  late String userType;

  @Index(unique: true, replace: true, caseSensitive: false)
  late String entryKey; // "$projectId::$userType"

  late String dataJson;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;
}
