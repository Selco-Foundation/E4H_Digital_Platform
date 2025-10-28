// Generated using mason. Do not modify by hand
import 'package:dart_mappable/dart_mappable.dart';
import 'package:isar/isar.dart';

import '../entities/address.dart';

part 'activity_facility.g.dart';
part 'activity_facility.mapper.dart';

@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class ActivityFacilitySearchModelWrapper
    with ActivityFacilitySearchModelWrapperMappable {
  final List<ActivityFacilitySearchModel>? items;

  ActivityFacilitySearchModelWrapper({this.items});
}

@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class ActivityFacilitySearchModel with ActivityFacilitySearchModelMappable {
  final String? id;
  final String? name;
  final String? activityId;
  final String? facilityId;
  final bool? isTaskEnabled;
  final String? parent;
  final String? department;
  final String? referenceId;
  final String? tenantId;
  final DateTime? startDateTime;
  final DateTime? endDateTime;

  ActivityFacilitySearchModel({
    this.id,
    this.name,
    this.activityId,
    this.facilityId,
    this.isTaskEnabled,
    this.parent,
    this.department,
    this.referenceId,
    this.tenantId,
    int? startDate,
    int? endDate,
  })  : startDateTime = startDate == null
            ? null
            : DateTime.fromMillisecondsSinceEpoch(startDate),
        endDateTime = endDate == null
            ? null
            : DateTime.fromMillisecondsSinceEpoch(endDate),
        super();

  int? get startDate => startDateTime?.millisecondsSinceEpoch;
  int? get endDate => endDateTime?.millisecondsSinceEpoch;
}

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
  DateTime? scheduledAt;
  DateTime? activatedAt;
  DateTime? completedAt;

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
        'scheduledAt': scheduledAt?.millisecondsSinceEpoch,
        'activatedAt': activatedAt?.millisecondsSinceEpoch,
        'completedAt': completedAt?.millisecondsSinceEpoch,
        'assignedUser': assignedUser,
        'assignedEmployeeUser': assignedEmployeeUser,
        'address': address?.toMap(),
        'facility': facility?.toMap(),
        'description': description,
        'rowVersion': rowVersion,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class FacilityAddress with FacilityAddressMappable {
  String? city;
  String? type;
  String? block;
  String? state;
  String? detail;
  @MappableField(key: 'doorNo')
  String? doorNo;
  String? street;
  String? pincode;
  String? district;
  String? landmark;
  double? latitude;
  @MappableField(key: 'tenantId')
  String? tenantId;
  @MappableField(key: 'addressId')
  String? addressId;
  double? longitude;
  String? addressLine1;
  String? addressLine2;
  String? buildingName;
  String? localityCode;
  String? addressNumber;
  double? locationAccuracy;

  FacilityAddress();

  factory FacilityAddress.fromMap(Map<String, dynamic> m) {
    return FacilityAddress()
      ..city = m['city']?.toString()
      ..type = m['type']?.toString()
      ..block = m['block']?.toString()
      ..state = m['state']?.toString()
      ..detail = m['detail']?.toString()
      ..doorNo = m['doorNo']?.toString()
      ..street = m['street']?.toString()
      ..pincode = m['pincode']?.toString()
      ..district = m['district']?.toString()
      ..landmark = m['landmark']?.toString()
      ..latitude =
          m['latitude'] is num ? (m['latitude'] as num).toDouble() : null
      ..tenantId = m['tenantId']?.toString()
      ..addressId = m['addressId']?.toString()
      ..longitude =
          m['longitude'] is num ? (m['longitude'] as num).toDouble() : null
      ..addressLine1 = m['addressLine1']?.toString()
      ..addressLine2 = m['addressLine2']?.toString()
      ..buildingName = m['buildingName']?.toString()
      ..localityCode = m['localityCode']?.toString()
      ..addressNumber = m['addressNumber']?.toString()
      ..locationAccuracy = m['locationAccuracy'] is num
          ? (m['locationAccuracy'] as num).toDouble()
          : null;
  }

  Map<String, dynamic> toMap() => {
        'city': city,
        'type': type,
        'block': block,
        'state': state,
        'detail': detail,
        'doorNo': doorNo,
        'street': street,
        'pincode': pincode,
        'district': district,
        'landmark': landmark,
        'latitude': latitude,
        'tenantId': tenantId,
        'addressId': addressId,
        'longitude': longitude,
        'addressLine1': addressLine1,
        'addressLine2': addressLine2,
        'buildingName': buildingName,
        'localityCode': localityCode,
        'addressNumber': addressNumber,
        'locationAccuracy': locationAccuracy,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class FacilityDetails with FacilityDetailsMappable {
  @MappableField(key: 'hfr_id')
  String? hfr_id;
  @MappableField(key: 'nin_id')
  String? nin_id;
  String? pocName;
  String? pocContact;
  String? pocDesignation;
  @MappableField(key: 'solar_solution_design_type')
  String? solar_solution_design_type;

  FacilityDetails();

  factory FacilityDetails.fromMap(Map<String, dynamic> m) {
    return FacilityDetails()
      ..hfr_id = m['hfr_id']?.toString()
      ..nin_id = m['nin_id']?.toString()
      ..pocName = m['pocName']?.toString()
      ..pocContact = m['pocContact']?.toString()
      ..pocDesignation = m['pocDesignation']?.toString()
      ..solar_solution_design_type =
          m['solar_solution_design_type']?.toString();
  }

  Map<String, dynamic> toMap() => {
        'hfr_id': hfr_id,
        'nin_id': nin_id,
        'pocName': pocName,
        'pocContact': pocContact,
        'pocDesignation': pocDesignation,
        'solar_solution_design_type': solar_solution_design_type,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class Facility with FacilityMappable {
  FacilityAddress? address;

  bool? isActive;
  String? wfStatus;

  @MappableField(key: 'tenant_id')
  String? tenantId;

  @MappableField(key: 'facility_id')
  String? facilityId;

  String? boundaryCode;
  @MappableField(key: 'facility_name')
  String? facilityName;
  @MappableField(key: 'facility_type')
  String? facilityType;
  @MappableField(key: 'facility_region')
  String? facilityRegion;

  @MappableField(key: 'facility_details')
  FacilityDetails? facilityDetails;

  @MappableField(key: 'facility_subtype')
  String? facility_subtype;
  @MappableField(key: 'facility_category')
  String? facility_category;
  @MappableField(key: 'facility_ownership')
  String? facility_ownership;

  Facility();

  factory Facility.fromMap(Map<String, dynamic> m) {
    final f = Facility();
    f.address = m['address'] != null
        ? FacilityAddress.fromMap(Map<String, dynamic>.from(m['address']))
        : null;
    f.isActive = m['isActive'] as bool?;
    f.wfStatus = m['wfStatus']?.toString();
    f.tenantId = m['tenant_id']?.toString();
    f.facilityId = m['facility_id']?.toString();
    f.boundaryCode = m['boundaryCode']?.toString();
    f.facilityName = m['facility_name']?.toString();
    f.facilityType = m['facility_type']?.toString();
    f.facilityRegion = m['facility_region']?.toString();
    if (m['facility_details'] != null) {
      f.facilityDetails = FacilityDetails.fromMap(
          Map<String, dynamic>.from(m['facility_details']));
    }
    f.facility_subtype = m['facility_subtype']?.toString();
    f.facility_category = m['facility_category']?.toString();
    f.facility_ownership = m['facility_ownership']?.toString();
    return f;
  }

  Map<String, dynamic> toMap() => {
        'address': address?.toMap(),
        'isActive': isActive,
        'wfStatus': wfStatus,
        'tenant_id': tenantId,
        'facility_id': facilityId,
        'boundaryCode': boundaryCode,
        'facility_name': facilityName,
        'facility_type': facilityType,
        'facility_region': facilityRegion,
        'facility_details': facilityDetails?.toMap(),
        'facility_subtype': facility_subtype,
        'facility_category': facility_category,
        'facility_ownership': facility_ownership,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class AdditionalDetails with AdditionalDetailsMappable {
  String? status;

  Facility? facility;

  String? systemCode;

  AdditionalDetails();

  factory AdditionalDetails.fromMap(Map<String, dynamic> m) {
    final a = AdditionalDetails();
    a.status = m['status']?.toString();
    a.facility = m['facility'] != null
        ? Facility.fromMap(Map<String, dynamic>.from(m['facility']))
        : null;
    a.systemCode = m['systemCode']?.toString();
    return a;
  }

  Map<String, dynamic> toMap() => {
        'status': status,
        'facility': facility?.toMap(),
        'systemCode': systemCode,
      };
}
