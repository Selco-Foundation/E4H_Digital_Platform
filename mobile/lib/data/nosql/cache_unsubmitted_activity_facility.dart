import 'package:isar/isar.dart';

import '../../model/activity_facility/activity_facility.dart';
import '../../model/entities/address.dart';

part 'cache_unsubmitted_activity_facility.g.dart';

@Collection()
class CacheUnsubmittedActivityFacility {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String status;

  @Embedded()
  late ActivityFacility activityFacility;

  late String userType;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheUnsubmittedActivityFacility({
    required this.activityFacilityId,
    required this.status,
    required this.activityFacility,
    required this.userType,
  });
}
