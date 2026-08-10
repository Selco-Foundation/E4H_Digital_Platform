class AssessmentQueueContact {
  final String? name;
  final String? phone;

  const AssessmentQueueContact({this.name, this.phone});

  factory AssessmentQueueContact.fromJson(
    Object? value, {
    String? fallbackName,
    String? fallbackPhone,
  }) {
    final json = value is Map
        ? Map<String, dynamic>.from(value)
        : const <String, dynamic>{};
    return AssessmentQueueContact(
      name: _asString(json['name']) ?? fallbackName,
      phone: _asString(json['phone']) ??
          _asString(json['phoneNumber']) ??
          _asString(json['mobileNumber']) ??
          fallbackPhone,
    );
  }
}

class AssessmentQueueFacility {
  final String? planFacilityId;
  final String? planId;
  final String? facilityId;
  final String? facilityName;
  final String? facilityCategory;
  final String? facilityType;
  final String? facilityCode;
  final String? address;
  final String? state;
  final String? district;
  final String? block;
  final AssessmentQueueContact facilityInCharge;
  final AssessmentQueueContact alternativeContact;
  final String? phoneStatus;
  final String? fieldStatus;
  final int? lastActionTime;

  const AssessmentQueueFacility({
    this.planFacilityId,
    this.planId,
    this.facilityId,
    this.facilityName,
    this.facilityCategory,
    this.facilityType,
    this.facilityCode,
    this.address,
    this.state,
    this.district,
    this.block,
    this.facilityInCharge = const AssessmentQueueContact(),
    this.alternativeContact = const AssessmentQueueContact(),
    this.phoneStatus,
    this.fieldStatus,
    this.lastActionTime,
  });

  factory AssessmentQueueFacility.fromJson(Map<String, dynamic> json) {
    return AssessmentQueueFacility(
      planFacilityId: _asString(json['planFacilityId']),
      planId: _asString(json['planId']),
      facilityId: _asString(json['facilityId']),
      facilityName: _asString(json['facilityName']),
      facilityCategory: _normalizeFacilityCategory(json['facilityCategory']),
      facilityType: _asString(json['facilityType']),
      facilityCode: _asString(json['facilityCode']),
      address: _asString(json['address']),
      state: _asString(json['state']),
      district: _asString(json['district']),
      block: _asString(json['block']),
      facilityInCharge: AssessmentQueueContact.fromJson(
        json['facilityInCharge'],
        fallbackName: _asString(json['facilityInChargeName']),
        fallbackPhone: _asString(json['facilityInChargePhone']),
      ),
      alternativeContact: AssessmentQueueContact.fromJson(
        json['alternativeContact'],
        fallbackName: _asString(json['alternativeContactName']),
        fallbackPhone: _asString(json['alternativeContactPhone']),
      ),
      phoneStatus: _asString(json['phoneStatus']),
      fieldStatus: _asString(json['fieldStatus']),
      lastActionTime: _asInt(json['lastActionTime']),
    );
  }
}

class AssessmentQueuePagination {
  final int offset;
  final int limit;
  final int total;

  const AssessmentQueuePagination({
    required this.offset,
    required this.limit,
    required this.total,
  });

  factory AssessmentQueuePagination.fromJson(
    Object? value, {
    required int fallbackOffset,
    required int fallbackLimit,
    required int fallbackTotal,
  }) {
    final json = value is Map
        ? Map<String, dynamic>.from(value)
        : const <String, dynamic>{};
    return AssessmentQueuePagination(
      offset: _asInt(json['offset']) ?? fallbackOffset,
      limit: _asInt(json['limit']) ?? fallbackLimit,
      total: _asInt(json['total']) ?? fallbackTotal,
    );
  }
}

class AssessmentQueueResponse {
  final List<AssessmentQueueFacility> facilities;
  final AssessmentQueuePagination pagination;

  const AssessmentQueueResponse({
    required this.facilities,
    required this.pagination,
  });

  factory AssessmentQueueResponse.fromJson(
    Map<String, dynamic> json, {
    required int requestedOffset,
    required int requestedLimit,
  }) {
    final rawQueue = json['queue'];
    final facilities = rawQueue is List
        ? rawQueue
            .whereType<Map>()
            .map((item) => AssessmentQueueFacility.fromJson(
                  Map<String, dynamic>.from(item),
                ))
            .toList(growable: false)
        : const <AssessmentQueueFacility>[];
    final legacyTotal = _asInt(json['total']) ?? facilities.length;

    return AssessmentQueueResponse(
      facilities: facilities,
      pagination: AssessmentQueuePagination.fromJson(
        json['pagination'],
        fallbackOffset: requestedOffset,
        fallbackLimit: requestedLimit,
        fallbackTotal: legacyTotal,
      ),
    );
  }
}

String? _asString(Object? value) {
  if (value == null) return null;
  final text = value.toString().trim();
  return text.isEmpty ? null : text;
}

int? _asInt(Object? value) {
  if (value is int) return value;
  if (value is num) return value.toInt();
  return int.tryParse(value?.toString() ?? '');
}

String? _normalizeFacilityCategory(Object? value) {
  final category = _asString(value);
  return category?.toUpperCase();
}
