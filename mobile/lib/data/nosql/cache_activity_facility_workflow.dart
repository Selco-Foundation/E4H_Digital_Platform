import 'package:isar/isar.dart';
import 'package:selco/data/nosql/workflow_audit_details.dart';

import '../../model/activity_facility/activity_facility.dart';
import '../../model/comment/comment.dart';
import '../../model/document/document.dart';
import '../../model/entities/address.dart';
import '../../model/transaction/transaction.dart';
import '../../model/workflow/workflow.dart';

part 'cache_activity_facility_workflow.g.dart';

@Collection()
class CacheActivityFacilityWorkflow {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String status;

  @Embedded()
  late ActivityFacility activityFacility;

  @Embedded()
  Workflow? workflow;

  @Embedded()
  List<Transaction>? transactions;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheActivityFacilityWorkflow({
    required this.status,
    required this.activityFacility,
    required this.activityFacilityId,
    this.workflow,
    this.transactions,
  });
}
