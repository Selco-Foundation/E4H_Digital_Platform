// Generated using mason. Do not modify by hand
import 'package:dart_mappable/dart_mappable.dart';
import 'package:isar/isar.dart';

import '../entities/address.dart';
import '../projects/project.dart';

part 'activity_facility.g.dart';
part 'activity_facility.mapper.dart';

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class ActivityFacility with ActivityFacilityMappable {
  static const schemaName = 'ActivityFacility';

  String id;
  String? tenantId;
  String? activityId;
  String? fieldPlanId;
  String? facilityId;

  String? status;
  int? scheduledAt;
  int? activatedAt;
  int? completedAt;

  String? assignedUser;
  String? assignedEmployeeUser;

  AddressModel? address;
  Facility? facility;
  String? description;
  int? rowVersion;

  ActivityFacility({
    this.id = '',
    this.tenantId,
    this.activityId,
    this.fieldPlanId,
    this.facilityId,
    this.status,
    this.scheduledAt,
    this.activatedAt,
    this.completedAt,
    this.assignedUser,
    this.assignedEmployeeUser,
    this.address,
    this.facility,
    this.description,
    this.rowVersion,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'tenantId': tenantId,
        'activityId': activityId,
        'fieldPlanId': fieldPlanId,
        'facilityId': facilityId,
        'status': status,
        'scheduledAt': scheduledAt,
        'activatedAt': activatedAt,
        'completedAt': completedAt,
        'assignedUser': assignedUser,
        'assignedEmployeeUser': assignedEmployeeUser,
        'address': address?.toMap(),
        'facility': facility?.toMap(),
        'description': description,
        'rowVersion': rowVersion,
      };
}
