import 'package:isar/isar.dart';

import '../../model/comment/comment.dart';
import '../../model/document/document.dart';
import '../../model/entities/address.dart';
import '../../model/projects/project.dart';
import '../../model/transaction/transaction.dart';
import '../../model/workflow/workflow.dart';

part 'cache_project_workflow.g.dart';

@Collection()
class CacheProjectWorkflow {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;

  @Index()
  late String status;

  @Embedded()
  late ProjectModel project;

  @Embedded()
  Workflow? workflow;

  @Embedded()
  List<Transaction>? transactions;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheProjectWorkflow({
    required this.status,
    required this.project,
    required this.projectId,
    this.workflow,
    this.transactions,
  });
}
