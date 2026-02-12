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

  FieldPlan? fieldPlan;

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
  AdditionalDetails? additionalDetails;

  ActivityFacility({
    this.id = '',
    this.tenantId,
    this.activityId,
    this.fieldPlanId,
    this.facilityId,
    this.fieldPlan,
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
    this.additionalDetails,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'tenantId': tenantId,
        'activityId': activityId,
        'fieldPlanId': fieldPlanId,
        'facilityId': facilityId,
        'fieldPlan': fieldPlan?.toMap(),
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
        'additionalDetails': additionalDetails?.toMap(),
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
class FieldPlan with FieldPlanMappable {
  String? id;
  String? tenantId;
  String? name;
  String? status;
  int? healthFacilityNumber;

  DateTime? startDateTime;
  DateTime? endDateTime;

  Project? project;

  FieldPlan();

  int? get startDate => startDateTime?.millisecondsSinceEpoch;
  int? get endDate => endDateTime?.millisecondsSinceEpoch;

  factory FieldPlan.fromMap(Map<String, dynamic> m) {
    final fp = FieldPlan();
    fp.id = m['id']?.toString();
    fp.tenantId = m['tenantId']?.toString();
    fp.name = m['name']?.toString();
    fp.status = m['status']?.toString();
    fp.healthFacilityNumber = m['healthFacilityNumber'] is int
        ? (m['healthFacilityNumber'] as int)
        : int.tryParse(m['healthFacilityNumber']?.toString() ?? '');
    fp.startDateTime = m['startDate'] is int
        ? DateTime.fromMillisecondsSinceEpoch(m['startDate'] as int)
        : null;
    fp.endDateTime = m['endDate'] is int
        ? DateTime.fromMillisecondsSinceEpoch(m['endDate'] as int)
        : null;

    final pj = m['project'];
    if (pj is Map) {
      fp.project = Project.fromMap(Map<String, dynamic>.from(pj));
    }
    return fp;
  }

  Map<String, dynamic> toMap() => {
        'id': id,
        'tenantId': tenantId,
        'name': name,
        'status': status,
        'healthFacilityNumber': healthFacilityNumber,
        'startDate': startDate,
        'endDate': endDate,
        'project': project?.toMap(),
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class Project with ProjectMappable {
  String? id;
  String? tenantId;
  String? projectNumber;
  String? name;
  String? projectType;
  String? projectSubType;
  String? referenceID;

  DateTime? startDateTime;
  DateTime? endDateTime;

  ProjectAdditionalDetails? additionalDetails;

  Project();

  int? get startDate => startDateTime?.millisecondsSinceEpoch;
  int? get endDate => endDateTime?.millisecondsSinceEpoch;

  factory Project.fromMap(Map<String, dynamic> m) {
    final p = Project();
    p.id = m['id']?.toString();
    p.tenantId = m['tenantId']?.toString();
    p.projectNumber = m['projectNumber']?.toString();
    p.name = m['name']?.toString();
    p.projectType = m['projectType']?.toString();
    p.projectSubType = m['projectSubType']?.toString();
    p.referenceID = m['referenceID']?.toString();
    p.startDateTime = m['startDate'] is int
        ? DateTime.fromMillisecondsSinceEpoch(m['startDate'] as int)
        : null;
    p.endDateTime = m['endDate'] is int
        ? DateTime.fromMillisecondsSinceEpoch(m['endDate'] as int)
        : null;

    final ad = m['additionalDetails'];
    if (ad is Map) {
      p.additionalDetails =
          ProjectAdditionalDetails.fromMap(Map<String, dynamic>.from(ad));
    }
    return p;
  }

  Map<String, dynamic> toMap() => {
        'id': id,
        'tenantId': tenantId,
        'projectNumber': projectNumber,
        'name': name,
        'projectType': projectType,
        'projectSubType': projectSubType,
        'referenceID': referenceID,
        'startDate': startDate,
        'endDate': endDate,
        'additionalDetails': additionalDetails?.toMap(),
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class ProjectAdditionalDetails with ProjectAdditionalDetailsMappable {
  String? status;
  Facility? facility;
  GeographyDetails? geographyDetails;

  ProjectAdditionalDetails();

  factory ProjectAdditionalDetails.fromMap(Map<String, dynamic> m) {
    final a = ProjectAdditionalDetails();
    a.status = m['status']?.toString();

    final f = m['facility'];
    if (f is Map) {
      a.facility = Facility.fromMap(Map<String, dynamic>.from(f));
    }

    final g = m['geographyDetails'];
    if (g is Map) {
      a.geographyDetails =
          GeographyDetails.fromMap(Map<String, dynamic>.from(g));
    }

    return a;
  }

  Map<String, dynamic> toMap() => {
        'status': status,
        'facility': facility?.toMap(),
        'geographyDetails': geographyDetails?.toMap(),
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class GeographyDetails with GeographyDetailsMappable {
  StateRef? state;
  List<BlockRef>? blocks;

  GeographyDetails();

  factory GeographyDetails.fromMap(Map<String, dynamic> m) {
    final g = GeographyDetails();

    final s = m['state'];
    if (s is Map) {
      g.state = StateRef.fromMap(Map<String, dynamic>.from(s));
    }

    final bl = m['blocks'];
    if (bl is List) {
      g.blocks = bl
          .whereType<Map>()
          .map((e) => BlockRef.fromMap(Map<String, dynamic>.from(e)))
          .toList();
    }

    return g;
  }

  Map<String, dynamic> toMap() => {
        'state': state?.toMap(),
        'blocks': blocks?.map((e) => e.toMap()).toList(),
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class StateRef with StateRefMappable {
  String? code;
  String? name;

  StateRef();

  factory StateRef.fromMap(Map<String, dynamic> m) {
    final s = StateRef();
    s.code = m['code']?.toString();
    s.name = m['name']?.toString();
    return s;
  }

  Map<String, dynamic> toMap() => {
        'code': code,
        'name': name,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class BlockRef with BlockRefMappable {
  String? code;
  String? name;
  String? stateCode;
  String? districtCode;

  BlockRef();

  factory BlockRef.fromMap(Map<String, dynamic> m) {
    final b = BlockRef();
    b.code = m['code']?.toString();
    b.name = m['name']?.toString();
    b.stateCode = m['stateCode']?.toString();
    b.districtCode = m['districtCode']?.toString();
    return b;
  }

  Map<String, dynamic> toMap() => {
        'code': code,
        'name': name,
        'stateCode': stateCode,
        'districtCode': districtCode,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class AssetTypeAdditionalDetails with AssetTypeAdditionalDetailsMappable {
  String? brandName;
  String? brandCode;
  String? capacity;

  AssetTypeAdditionalDetails();

  factory AssetTypeAdditionalDetails.fromMap(Map<String, dynamic> m) {
    final a = AssetTypeAdditionalDetails();
    a.brandName = m['brandName']?.toString();
    a.brandCode = m['brandCode']?.toString();
    a.capacity = m['capacity']?.toString();
    return a;
  }

  Map<String, dynamic> toMap() => {
        'brandName': brandName,
        'brandCode': brandCode,
        'capacity': capacity,
      };
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class AdditionalDetails with AdditionalDetailsMappable {
  String? status;

  Facility? facility;

  String? systemCode;
  AssetTypeAdditionalDetails? battery;
  AssetTypeAdditionalDetails? inverter;
  AssetTypeAdditionalDetails? panel;

  AdditionalDetails();

  factory AdditionalDetails.fromMap(Map<String, dynamic> m) {
    final a = AdditionalDetails();
    a.status = m['status']?.toString();
    a.facility = m['facility'] != null
        ? Facility.fromMap(Map<String, dynamic>.from(m['facility']))
        : null;
    a.systemCode = m['systemCode']?.toString();
    final b = m['battery'];
    if (b is Map) {
      a.battery =
          AssetTypeAdditionalDetails.fromMap(Map<String, dynamic>.from(b));
    }

    final i = m['inverter'];
    if (i is Map) {
      a.inverter =
          AssetTypeAdditionalDetails.fromMap(Map<String, dynamic>.from(i));
    }

    final p = m['panel'];
    if (p is Map) {
      a.panel =
          AssetTypeAdditionalDetails.fromMap(Map<String, dynamic>.from(p));
    }

    return a;
  }

  Map<String, dynamic> toMap() => {
        'status': status,
        'facility': facility?.toMap(),
        'systemCode': systemCode,
        'battery': battery?.toMap(),
        'inverter': inverter?.toMap(),
        'panel': panel?.toMap(),
      };
}
