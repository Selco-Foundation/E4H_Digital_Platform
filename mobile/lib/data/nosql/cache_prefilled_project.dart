import 'package:isar/isar.dart';

part 'cache_prefilled_project.g.dart';

@Collection()
class CachePrefilledProject {
  Id id = Isar.autoIncrement;

  @Index(
      composite: [CompositeIndex('userType')],
      unique: true,
      caseSensitive: true)
  late String projectId;

  late String userType;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CachePrefilledProject({
    required this.projectId,
    required this.userType,
  });
}
