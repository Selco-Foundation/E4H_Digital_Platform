import 'package:isar/isar.dart';

import '../../model/comment/comment.dart';
import '../../model/entities/address.dart';
import '../../model/projects/project.dart';
import '../../model/transaction/transaction.dart';

part 'cache_project_workflow.g.dart';

@Collection()
class CacheProjectWorkflow {
  Id id = Isar.autoIncrement;

  @Index()
  late String status;

  @Embedded()
  late ProjectModel project;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheProjectWorkflow({
    required this.status,
    required this.project,
  });
}
