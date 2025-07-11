import 'package:isar/isar.dart';

import '../../model/comment/comment.dart';
import '../../model/entities/address.dart';
import '../../model/projects/project.dart';
import '../../model/transaction/transaction.dart';

part 'cache_unsubmitted_project.g.dart';

@Collection()
class CacheUnsubmittedProject {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;

  @Index()
  late String status;

  @Embedded()
  late ProjectModel project;

  late String userType;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheUnsubmittedProject({
    required this.projectId,
    required this.status,
    required this.project,
    required this.userType,
  });
}
