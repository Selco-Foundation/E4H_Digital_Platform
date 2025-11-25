// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scheduled_visit.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ScheduledVisitSearchResponseImpl _$$ScheduledVisitSearchResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$ScheduledVisitSearchResponseImpl(
      scheduledVisits: (json['ScheduledVisits'] as List<dynamic>?)
              ?.map((e) => ScheduledVisit.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <ScheduledVisit>[],
      totalCount: (json['TotalCount'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$ScheduledVisitSearchResponseImplToJson(
        _$ScheduledVisitSearchResponseImpl instance) =>
    <String, dynamic>{
      'ScheduledVisits': instance.scheduledVisits,
      'TotalCount': instance.totalCount,
    };

_$AmcConfigurationImpl _$$AmcConfigurationImplFromJson(
        Map<String, dynamic> json) =>
    _$AmcConfigurationImpl(
      id: json['id'] as String?,
      tenantId: json['tenantId'] as String?,
      vendorId: json['vendorId'] as String?,
      facilityId: json['facilityId'] as String?,
      facility: _$JsonConverterFromJson<Map<String, dynamic>, Facility>(
          json['facility'], const FacilityConverter().fromJson),
      projectId: json['projectId'] as String?,
      project: json['project'] as Map<String, dynamic>?,
      assetTypes: (json['assetTypes'] as List<dynamic>?)
              ?.map((e) => AmcAssetType.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <AmcAssetType>[],
      assignments: (json['assignments'] as List<dynamic>?)
              ?.map((e) => AmcAssignment.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <AmcAssignment>[],
      durationMonths: (json['durationMonths'] as num?)?.toInt(),
      visitFrequencyMonths: (json['visitFrequencyMonths'] as num?)?.toInt(),
      configurationStartDate: const EpochDateTimeConverter()
          .fromJson(json['configurationStartDate']),
      configurationEndDate:
          const EpochDateTimeConverter().fromJson(json['configurationEndDate']),
      status: json['status'] as String?,
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
      auditDetails: json['auditDetails'] == null
          ? null
          : AuditDetails.fromJson(json['auditDetails'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$AmcConfigurationImplToJson(
        _$AmcConfigurationImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'tenantId': instance.tenantId,
      'vendorId': instance.vendorId,
      'facilityId': instance.facilityId,
      'facility': _$JsonConverterToJson<Map<String, dynamic>, Facility>(
          instance.facility, const FacilityConverter().toJson),
      'projectId': instance.projectId,
      'project': instance.project,
      'assetTypes': instance.assetTypes,
      'assignments': instance.assignments,
      'durationMonths': instance.durationMonths,
      'visitFrequencyMonths': instance.visitFrequencyMonths,
      'configurationStartDate': const EpochDateTimeConverter()
          .toJson(instance.configurationStartDate),
      'configurationEndDate':
          const EpochDateTimeConverter().toJson(instance.configurationEndDate),
      'status': instance.status,
      'additionalDetails': instance.additionalDetails,
      'auditDetails': instance.auditDetails,
    };

Value? _$JsonConverterFromJson<Json, Value>(
  Object? json,
  Value? Function(Json json) fromJson,
) =>
    json == null ? null : fromJson(json as Json);

Json? _$JsonConverterToJson<Json, Value>(
  Value? value,
  Json? Function(Value value) toJson,
) =>
    value == null ? null : toJson(value);

_$AmcAssetTypeImpl _$$AmcAssetTypeImplFromJson(Map<String, dynamic> json) =>
    _$AmcAssetTypeImpl(
      code: json['code'] as String?,
      name: json['name'] as String?,
    );

Map<String, dynamic> _$$AmcAssetTypeImplToJson(_$AmcAssetTypeImpl instance) =>
    <String, dynamic>{
      'code': instance.code,
      'name': instance.name,
    };

_$AmcAssignmentImpl _$$AmcAssignmentImplFromJson(Map<String, dynamic> json) =>
    _$AmcAssignmentImpl(
      id: json['id'] as String?,
      tenantId: json['tenantId'] as String?,
      amcConfigurationId: json['amcConfigurationId'] as String?,
      assignedUser: json['assignedUser'] as String?,
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
      auditDetails: json['auditDetails'] == null
          ? null
          : AuditDetails.fromJson(json['auditDetails'] as Map<String, dynamic>),
      isActive: json['isActive'] as bool?,
    );

Map<String, dynamic> _$$AmcAssignmentImplToJson(_$AmcAssignmentImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'tenantId': instance.tenantId,
      'amcConfigurationId': instance.amcConfigurationId,
      'assignedUser': instance.assignedUser,
      'additionalDetails': instance.additionalDetails,
      'auditDetails': instance.auditDetails,
      'isActive': instance.isActive,
    };

_$ScheduledVisitImpl _$$ScheduledVisitImplFromJson(Map<String, dynamic> json) =>
    _$ScheduledVisitImpl(
      id: json['id'] as String?,
      tenantId: json['tenantId'] as String?,
      amcConfigurationId: json['amcConfigurationId'] as String?,
      amcConfiguration: json['amcConfiguration'] == null
          ? null
          : AmcConfiguration.fromJson(
              json['amcConfiguration'] as Map<String, dynamic>),
      facilityId: json['facilityId'] as String?,
      facility: _$JsonConverterFromJson<Map<String, dynamic>, Facility>(
          json['facility'], const FacilityConverter().fromJson),
      visitNumber: (json['visitNumber'] as num?)?.toInt(),
      scheduledDate:
          const EpochDateTimeConverter().fromJson(json['scheduledDate']),
      actualVisitDate:
          const EpochDateTimeConverter().fromJson(json['actualVisitDate']),
      status: json['status'] as String?,
      visitReport: json['visitReport'] == null
          ? null
          : ScheduledVisitReport.fromJson(
              json['visitReport'] as Map<String, dynamic>),
      workflow: const WorkflowFlexConverter().fromJson(json['workflow']),
      processInstances: (json['processInstances'] as List<dynamic>?)
              ?.map((e) => e as Map<String, dynamic>)
              .toList() ??
          const <Map<String, dynamic>>[],
      transactions: (json['transactions'] as List<dynamic>?)
          ?.map((e) => Transaction.fromJson(e as Map<String, dynamic>))
          .toList(),
      assignments: (json['assignments'] as List<dynamic>?)
              ?.map((e) =>
                  ScheduledVisitAssignment.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <ScheduledVisitAssignment>[],
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
      auditDetails: json['auditDetails'] == null
          ? null
          : AuditDetails.fromJson(json['auditDetails'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$ScheduledVisitImplToJson(
        _$ScheduledVisitImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'tenantId': instance.tenantId,
      'amcConfigurationId': instance.amcConfigurationId,
      'amcConfiguration': instance.amcConfiguration,
      'facilityId': instance.facilityId,
      'facility': _$JsonConverterToJson<Map<String, dynamic>, Facility>(
          instance.facility, const FacilityConverter().toJson),
      'visitNumber': instance.visitNumber,
      'scheduledDate':
          const EpochDateTimeConverter().toJson(instance.scheduledDate),
      'actualVisitDate':
          const EpochDateTimeConverter().toJson(instance.actualVisitDate),
      'status': instance.status,
      'visitReport': instance.visitReport,
      'workflow': const WorkflowFlexConverter().toJson(instance.workflow),
      'processInstances': instance.processInstances,
      'transactions': instance.transactions,
      'assignments': instance.assignments,
      'additionalDetails': instance.additionalDetails,
      'auditDetails': instance.auditDetails,
    };

_$ScheduledVisitReportImpl _$$ScheduledVisitReportImplFromJson(
        Map<String, dynamic> json) =>
    _$ScheduledVisitReportImpl(
      schemaCode: json['schemaCode'] as String?,
      version: json['version'] as String?,
      submittedBy: json['submittedBy'] as String?,
      submittedAt: const EpochDateTimeConverter().fromJson(json['submittedAt']),
      otpReference: json['otpReference'] as String?,
      otpVerifiedAt:
          const EpochDateTimeConverter().fromJson(json['otpVerifiedAt']),
      responses: json['responses'] as Map<String, dynamic>?,
      documents: (json['documents'] as List<dynamic>?)
          ?.map((e) => Document.fromJson(e as Map<String, dynamic>))
          .toList(),
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
    );

Map<String, dynamic> _$$ScheduledVisitReportImplToJson(
        _$ScheduledVisitReportImpl instance) =>
    <String, dynamic>{
      'schemaCode': instance.schemaCode,
      'version': instance.version,
      'submittedBy': instance.submittedBy,
      'submittedAt':
          const EpochDateTimeConverter().toJson(instance.submittedAt),
      'otpReference': instance.otpReference,
      'otpVerifiedAt':
          const EpochDateTimeConverter().toJson(instance.otpVerifiedAt),
      'responses': instance.responses,
      'documents': instance.documents,
      'additionalDetails': instance.additionalDetails,
    };

_$ScheduledVisitAssignmentImpl _$$ScheduledVisitAssignmentImplFromJson(
        Map<String, dynamic> json) =>
    _$ScheduledVisitAssignmentImpl(
      id: json['id'] as String?,
      tenantId: json['tenantId'] as String?,
      scheduledVisitId: json['scheduledVisitId'] as String?,
      assignedUser: json['assignedUser'] as String?,
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
      auditDetails: json['auditDetails'] == null
          ? null
          : AuditDetails.fromJson(json['auditDetails'] as Map<String, dynamic>),
      isActive: json['isActive'] as bool?,
    );

Map<String, dynamic> _$$ScheduledVisitAssignmentImplToJson(
        _$ScheduledVisitAssignmentImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'tenantId': instance.tenantId,
      'scheduledVisitId': instance.scheduledVisitId,
      'assignedUser': instance.assignedUser,
      'additionalDetails': instance.additionalDetails,
      'auditDetails': instance.auditDetails,
      'isActive': instance.isActive,
    };
