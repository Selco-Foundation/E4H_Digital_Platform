import 'package:isar/isar.dart';

part 'cache_project_asset.g.dart';

@Collection()
class CacheProjectAsset {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String projectId;

  int progress = 0;
  DateTime createdAt = DateTime.now();
  DateTime updatedAt = DateTime.now();

  CacheProjectAsset({
    required this.projectId,
    this.progress = 0,
  });
}
