import 'package:freezed_annotation/freezed_annotation.dart';

import '../activity_facility/activity_facility.dart';
import '../audit_details/audit_details.dart';
import '../document/document.dart';
import '../transaction/transaction.dart';
import '../workflow/workflow.dart';

part 'scheduled_visit.freezed.dart';
part 'scheduled_visit.g.dart';

@freezed
class ScheduledVisitSearchResponse with _$ScheduledVisitSearchResponse {
  const factory ScheduledVisitSearchResponse({
    @JsonKey(name: 'ScheduledVisits')
    @Default(<ScheduledVisit>[])
    List<ScheduledVisit> scheduledVisits,
    @JsonKey(name: 'TotalCount') @Default(0) int totalCount,
  }) = _ScheduledVisitSearchResponse;

  factory ScheduledVisitSearchResponse.fromJson(Map<String, dynamic> json) =>
      _$ScheduledVisitSearchResponseFromJson(json);
}

@freezed
class AmcConfiguration with _$AmcConfiguration {
  const factory AmcConfiguration({
    String? id,
    String? tenantId,
    String? vendorId,
    String? facilityId,
    @FacilityConverter() Facility? facility,
    String? projectId,
    Map<String, dynamic>? project,
    @Default(<AmcAssetType>[]) List<AmcAssetType> assetTypes,
    @Default(<AmcAssignment>[]) List<AmcAssignment> assignments,
    int? durationMonths,
    int? visitFrequencyMonths,
    @EpochDateTimeConverter() DateTime? configurationStartDate,
    @EpochDateTimeConverter() DateTime? configurationEndDate,
    String? status,
    Map<String, dynamic>? additionalDetails,
    AuditDetails? auditDetails,
  }) = _AmcConfiguration;

  factory AmcConfiguration.fromJson(Map<String, dynamic> json) =>
      _$AmcConfigurationFromJson(json);
}

@freezed
class AmcAssetType with _$AmcAssetType {
  const factory AmcAssetType({
    String? code,
    String? name,
  }) = _AmcAssetType;

  factory AmcAssetType.fromJson(Map<String, dynamic> json) =>
      _$AmcAssetTypeFromJson(json);
}

@freezed
class AmcAssignment with _$AmcAssignment {
  const factory AmcAssignment({
    String? id,
    String? tenantId,
    String? amcConfigurationId,
    String? assignedUser,
    Map<String, dynamic>? additionalDetails,
    AuditDetails? auditDetails,
    bool? isActive,
  }) = _AmcAssignment;

  factory AmcAssignment.fromJson(Map<String, dynamic> json) =>
      _$AmcAssignmentFromJson(json);
}

@freezed
class ScheduledVisit with _$ScheduledVisit {
  const factory ScheduledVisit({
    String? id,
    String? tenantId,
    String? amcConfigurationId,
    AmcConfiguration? amcConfiguration,
    String? facilityId,
    @FacilityConverter() Facility? facility,
    int? visitNumber,
    @EpochDateTimeConverter() DateTime? scheduledDate,
    @EpochDateTimeConverter() DateTime? actualVisitDate,
    String? status,
    ScheduledVisitReport? visitReport,
    @WorkflowFlexConverter() Workflow? workflow,
    @Default(<Workflow>[]) List<Workflow> processInstances,
    List<Transaction>? transactions,
    @Default(<ScheduledVisitAssignment>[])
    List<ScheduledVisitAssignment> assignments,
    Map<String, dynamic>? additionalDetails,
    AuditDetails? auditDetails,
  }) = _ScheduledVisit;

  factory ScheduledVisit.fromJson(Map<String, dynamic> json) =>
      _$ScheduledVisitFromJson(json);
}

@freezed
class ScheduledVisitReport with _$ScheduledVisitReport {
  const factory ScheduledVisitReport({
    String? schemaCode,
    String? version,
    String? submittedBy,
    @EpochDateTimeConverter() DateTime? submittedAt,
    String? otpReference,
    @EpochDateTimeConverter() DateTime? otpVerifiedAt,
    Map<String, dynamic>? responses,
    List<Document>? documents,
    Map<String, dynamic>? additionalDetails,
  }) = _ScheduledVisitReport;

  factory ScheduledVisitReport.fromJson(Map<String, dynamic> json) =>
      _$ScheduledVisitReportFromJson(json);
}

@freezed
class ScheduledVisitAssignment with _$ScheduledVisitAssignment {
  const factory ScheduledVisitAssignment({
    String? id,
    String? tenantId,
    String? scheduledVisitId,
    String? assignedUser,
    Map<String, dynamic>? additionalDetails,
    AuditDetails? auditDetails,
    bool? isActive,
  }) = _ScheduledVisitAssignment;

  factory ScheduledVisitAssignment.fromJson(Map<String, dynamic> json) =>
      _$ScheduledVisitAssignmentFromJson(json);
}

@freezed
class ScheduledVisitSearchCriteria with _$ScheduledVisitSearchCriteria {
  const factory ScheduledVisitSearchCriteria({
    String? tenantId,
    String? facilityId,
    String? amcConfigurationId,
    @Default(<String>[]) List<String> statuses,
    int? visitNumber,
    @EpochDateTimeConverter() DateTime? scheduledFrom,
    @EpochDateTimeConverter() DateTime? scheduledTo,
  }) = _ScheduledVisitSearchCriteria;

  const ScheduledVisitSearchCriteria._();

  static const String TENANT_ID = 'tenantId';
  static const String FACILITY_ID = 'facilityId';
  static const String AMC_CONFIGURATION_ID = 'amcConfigurationId';
  static const String STATUSES = 'statuses';
  static const String VISIT_NUMBER = 'visitNumber';
  static const String SCHEDULED_FROM = 'scheduledFrom';
  static const String SCHEDULED_TO = 'scheduledTo';

  Map<String, dynamic> toApiMap() {
    final out = <String, dynamic>{};
    if (tenantId != null) out[TENANT_ID] = tenantId;
    if (facilityId != null) out[FACILITY_ID] = facilityId;
    if (amcConfigurationId != null) {
      out[AMC_CONFIGURATION_ID] = amcConfigurationId;
    }
    if (statuses.isNotEmpty) out[STATUSES] = statuses;
    if (visitNumber != null) out[VISIT_NUMBER] = visitNumber;
    if (scheduledFrom != null) {
      out[SCHEDULED_FROM] = scheduledFrom!.millisecondsSinceEpoch;
    }
    if (scheduledTo != null) {
      out[SCHEDULED_TO] = scheduledTo!.millisecondsSinceEpoch;
    }
    return out;
  }
}

class EpochDateTimeConverter implements JsonConverter<DateTime?, Object?> {
  const EpochDateTimeConverter();

  @override
  DateTime? fromJson(Object? json) {
    if (json == null) return null;

    if (json is int) {
      return DateTime.fromMillisecondsSinceEpoch(json);
    }

    if (json is String && json.isNotEmpty) {
      final asInt = int.tryParse(json);
      if (asInt != null) {
        return DateTime.fromMillisecondsSinceEpoch(asInt);
      }
      return DateTime.tryParse(json);
    }

    return null;
  }

  @override
  Object? toJson(DateTime? date) => date?.millisecondsSinceEpoch;
}

class WorkflowFlexConverter implements JsonConverter<Workflow?, Object?> {
  const WorkflowFlexConverter();

  @override
  Workflow? fromJson(Object? json) {
    if (json == null) return null;

    if (json is Map) {
      if (json.isEmpty) return null;
      return Workflow.fromJson(Map<String, dynamic>.from(json));
    }

    if (json is List && json.isNotEmpty) {
      final first = json.first;
      if (first is Map<String, dynamic>) {
        return Workflow.fromJson(first);
      }
      if (first is Map) {
        return Workflow.fromJson(Map<String, dynamic>.from(first));
      }
    }

    return null;
  }

  @override
  Object? toJson(Workflow? value) => value?.toJson();
}

class FacilityConverter
    implements JsonConverter<Facility, Map<String, dynamic>> {
  const FacilityConverter();

  @override
  Facility fromJson(Map<String, dynamic> json) {
    final m = Map<String, dynamic>.from(json);
    return Facility.fromMap(m);
  }

  @override
  Map<String, dynamic> toJson(Facility value) => value.toMap();
}
