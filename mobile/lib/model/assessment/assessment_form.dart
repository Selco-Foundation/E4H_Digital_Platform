class AssessmentFormResolution {
  final String formType;

  const AssessmentFormResolution({required this.formType});

  factory AssessmentFormResolution.fromJson(Map<String, dynamic> json) {
    final formType = json['formType']?.toString().trim();
    if (formType == null || formType.isEmpty) {
      throw const FormatException('Assessment form type is missing');
    }
    return AssessmentFormResolution(formType: formType);
  }
}

class AssessmentSubmissionRequest {
  final String planFacilityId;
  final String tenantId;
  final String facilityCategory;
  final Map<String, dynamic> submissionData;
  final String submittedByName;
  final int clientSubmissionTime;

  const AssessmentSubmissionRequest({
    required this.planFacilityId,
    required this.tenantId,
    required this.facilityCategory,
    required this.submissionData,
    required this.submittedByName,
    required this.clientSubmissionTime,
  });

  Map<String, dynamic> toJson() => {
        'planFacilityId': planFacilityId,
        'tenantId': tenantId,
        'facilityCategory': facilityCategory,
        'assessmentPhase': 'PHONE',
        'submissionData': submissionData,
        'submittedByName': submittedByName,
        'clientSubmissionTime': clientSubmissionTime,
      };

  factory AssessmentSubmissionRequest.fromJson(Map<String, dynamic> json) {
    return AssessmentSubmissionRequest(
      planFacilityId: json['planFacilityId'].toString(),
      tenantId: json['tenantId'].toString(),
      facilityCategory: json['facilityCategory'].toString(),
      submissionData: Map<String, dynamic>.from(
        json['submissionData'] as Map? ?? const {},
      ),
      submittedByName: json['submittedByName']?.toString() ?? '',
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
