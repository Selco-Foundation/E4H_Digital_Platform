// Backend wire values intentionally use upper snake case.
// ignore_for_file: constant_identifier_names

import 'assessment_form_type.dart';

class AssessmentFacilityAddress {
  final String? addressNumber;
  final String? addressLine1;
  final String? addressLine2;
  final String? landmark;
  final String? city;
  final String? pincode;
  final String? detail;
  final String? state;
  final String? district;
  final String? block;
  final String? doorNo;
  final String? buildingName;
  final String? street;

  const AssessmentFacilityAddress({
    this.addressNumber,
    this.addressLine1,
    this.addressLine2,
    this.landmark,
    this.city,
    this.pincode,
    this.detail,
    this.state,
    this.district,
    this.block,
    this.doorNo,
    this.buildingName,
    this.street,
  });

  factory AssessmentFacilityAddress.fromJson(Object? value) {
    final json = value is Map
        ? Map<String, dynamic>.from(value)
        : const <String, dynamic>{};
    return AssessmentFacilityAddress(
      addressNumber: _assessmentString(json['addressNumber']),
      addressLine1: _assessmentString(json['addressLine1']),
      addressLine2: _assessmentString(json['addressLine2']),
      landmark: _assessmentString(json['landmark']),
      city: _assessmentString(json['city']),
      pincode: _assessmentString(json['pincode']),
      detail: _assessmentString(json['detail']),
      state: _assessmentString(json['state']),
      district: _assessmentString(json['district']),
      block: _assessmentString(json['block']),
      doorNo: _assessmentString(json['doorNo']),
      buildingName: _assessmentString(json['buildingName']),
      street: _assessmentString(json['street']),
    );
  }
}

class AssessmentFacilityBoundary {
  final String? state;
  final String? district;
  final String? block;

  const AssessmentFacilityBoundary({this.state, this.district, this.block});

  factory AssessmentFacilityBoundary.fromJson(Object? value) {
    final json = value is Map
        ? Map<String, dynamic>.from(value)
        : const <String, dynamic>{};
    return AssessmentFacilityBoundary(
      state: _assessmentString(json['state']),
      district: _assessmentString(json['district']),
      block: _assessmentString(json['block']),
    );
  }
}

class AssessmentFacilityDetails {
  final String? facilityId;
  final String? facilityName;
  final String? facilityCategory;
  final String? facilityType;
  final AssessmentFacilityAddress address;
  final String? boundaryCode;
  final AssessmentFacilityBoundary boundary;
  final String? facilityPocName;
  final String? facilityPocPhone;
  final String? ninId;
  final String? hfrId;

  const AssessmentFacilityDetails({
    this.facilityId,
    this.facilityName,
    this.facilityCategory,
    this.facilityType,
    this.address = const AssessmentFacilityAddress(),
    this.boundaryCode,
    this.boundary = const AssessmentFacilityBoundary(),
    this.facilityPocName,
    this.facilityPocPhone,
    this.ninId,
    this.hfrId,
  });

  factory AssessmentFacilityDetails.fromJson(Map<String, dynamic> json) {
    return AssessmentFacilityDetails(
      facilityId: _assessmentString(json['facility_id']),
      facilityName: _assessmentString(json['facility_name']),
      facilityCategory: _assessmentString(json['facility_category']),
      facilityType: _assessmentString(json['facility_type']),
      address: AssessmentFacilityAddress.fromJson(json['address']),
      boundaryCode: _assessmentString(json['boundaryCode']),
      boundary: AssessmentFacilityBoundary.fromJson(json['boundary']),
      facilityPocName: _assessmentString(json['facility_poc_name']),
      facilityPocPhone: _assessmentString(json['facility_poc_phone']),
      ninId: _assessmentString(json['nin_id']),
      hfrId: _assessmentString(json['hfr_id']),
    );
  }

  String? get formattedAddress {
    final boundaryParts = boundaryCode
            ?.split('_')
            .map((value) => value.trim())
            .where((value) => value.isNotEmpty)
            .toList() ??
        const <String>[];
    String? boundaryCodePart(int index) {
      if (index >= boundaryParts.length) return null;
      final value = boundaryParts[index];
      return value.contains('/') ? null : value;
    }

    String? boundaryLabel(String? value) {
      final normalized = _assessmentString(value);
      if (normalized == null) return null;
      final parts = normalized.split('_');
      return _assessmentString(parts.last);
    }

    final block =
        address.block ?? boundaryLabel(boundary.block) ?? boundaryCodePart(3);
    final district = address.district ??
        boundaryLabel(boundary.district) ??
        boundaryCodePart(2);
    final state =
        address.state ?? boundaryLabel(boundary.state) ?? boundaryCodePart(1);
    final values = <String?>[state, district, block];
    final normalized = values
        .map(_assessmentString)
        .whereType<String>()
        .toList(growable: false);
    return normalized.isEmpty ? null : normalized.join(', ');
  }
}

String? _assessmentString(Object? value) {
  if (value == null) return null;
  final normalized = value.toString().trim();
  return normalized.isEmpty ? null : normalized;
}

enum AssessmentUnableToContactReason {
  NO_ANSWER,
  WRONG_NUMBER;

  static AssessmentUnableToContactReason? fromCode(String? value) {
    final code = value?.trim().toUpperCase();
    if (code == null || code.isEmpty) return null;
    for (final reason in values) {
      if (reason.name == code) return reason;
    }
    return null;
  }
}

class AssessmentFormResolution {
  final AssessmentFormType formType;

  const AssessmentFormResolution({required this.formType});

  factory AssessmentFormResolution.fromJson(Map<String, dynamic> json) {
    final formType = AssessmentFormType.fromCode(json['formType']?.toString());
    if (formType == null) {
      throw const FormatException('Assessment form type is missing');
    }
    return AssessmentFormResolution(formType: formType);
  }
}

class AssessmentSubmissionRequest {
  final String planFacilityId;
  final String tenantId;
  final String facilityCategory;
  final AssessmentPhase assessmentPhase;
  final Map<String, dynamic> submissionData;
  final String? submittedByName;
  final int clientSubmissionTime;

  const AssessmentSubmissionRequest({
    required this.planFacilityId,
    required this.tenantId,
    required this.facilityCategory,
    required this.assessmentPhase,
    required this.submissionData,
    this.submittedByName,
    required this.clientSubmissionTime,
  });

  Map<String, dynamic> toJson() => {
        'planFacilityId': planFacilityId,
        'tenantId': tenantId,
        'facilityCategory': facilityCategory,
        'assessmentPhase': assessmentPhase.name,
        'submissionData': submissionData,
        if (assessmentPhase == AssessmentPhase.PHONE &&
            submittedByName?.trim().isNotEmpty == true)
          'submittedByName': submittedByName!.trim(),
        'clientSubmissionTime': clientSubmissionTime,
      };

  factory AssessmentSubmissionRequest.fromJson(Map<String, dynamic> json) {
    return AssessmentSubmissionRequest(
      planFacilityId: json['planFacilityId'].toString(),
      tenantId: json['tenantId'].toString(),
      facilityCategory: json['facilityCategory'].toString(),
      assessmentPhase:
          AssessmentPhase.fromCode(json['assessmentPhase']?.toString()) ??
              AssessmentPhase.PHONE,
      submissionData: Map<String, dynamic>.from(
        json['submissionData'] as Map? ?? const {},
      ),
      submittedByName: json['submittedByName']?.toString(),
      clientSubmissionTime:
          (json['clientSubmissionTime'] as num?)?.toInt() ?? 0,
    );
  }
}

class AssessmentSubmissionResponse {
  final String? submissionId;
  final String? outcome;
  final bool idempotentReplay;

  const AssessmentSubmissionResponse({
    this.submissionId,
    this.outcome,
    this.idempotentReplay = false,
  });

  factory AssessmentSubmissionResponse.fromJson(Map<String, dynamic> json) {
    final submission = json['submission'] is Map
        ? Map<String, dynamic>.from(json['submission'] as Map)
        : const <String, dynamic>{};
    return AssessmentSubmissionResponse(
      submissionId: submission['id']?.toString(),
      outcome: submission['outcome']?.toString(),
      idempotentReplay: json['idempotentReplay'] == true,
    );
  }
}

class AssessmentApiException implements Exception {
  final int? statusCode;
  final String code;
  final String message;
  final List<String> fields;

  const AssessmentApiException({
    this.statusCode,
    required this.code,
    required this.message,
    this.fields = const [],
  });

  bool get isSessionExpired => statusCode == 401 || code == 'SESSION_EXPIRED';

  bool get isAuthorizationFailure => statusCode == 403;

  bool get isConflict => statusCode == 409;

  bool get isRetryable =>
      statusCode == null ||
      statusCode == 408 ||
      statusCode == 429 ||
      (statusCode != null && statusCode! >= 500) ||
      isSessionExpired;

  @override
  String toString() => message;
}
