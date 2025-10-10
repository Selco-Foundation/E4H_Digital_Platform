import 'dart:convert';

import 'package:freezed_annotation/freezed_annotation.dart';

import '../../model/workflow/workflow.dart';
import '../entities/address.dart';
import '../projects/project.dart';
import '../transaction/transaction.dart';

part 'project_workflow.freezed.dart';
part 'project_workflow.g.dart';

@freezed
class ProjectWorkflow with _$ProjectWorkflow {
  const factory ProjectWorkflow({
    @ProjectModelConverter() required ProjectModel project,
    String? status,
    List<Transaction>? transactions,
    @WorkflowFlexConverter() Workflow? workflow,
  }) = _ProjectWorkflow;

  factory ProjectWorkflow.fromJson(Map<String, dynamic> json) =>
      _$ProjectWorkflowFromJson(json);
}

class ProjectModelConverter
    implements JsonConverter<ProjectModel, Map<String, dynamic>> {
  const ProjectModelConverter();

  Map<String, dynamic> _asMap(Map<String, dynamic> v) =>
      jsonDecode(jsonEncode(v)) as Map<String, dynamic>;

  @override
  ProjectModel fromJson(Map<String, dynamic> json) {
    // Normalize the input so nested maps are Map<String, dynamic>
    final m = _asMap(json);

    // ---- Primitive fields on ProjectModel ----
    final model = ProjectModel(
      id: (m['id'] ?? '').toString(),
      projectType: m['projectType']?.toString(),
      projectTypeId: m['projectTypeId']?.toString(),
      projectNumber: m['projectNumber']?.toString(),
      subProjectTypeId:
          m['projectSubType']?.toString(), // API uses projectSubType
      isTaskEnabled:
          m['isTaskEnabled'] is bool ? m['isTaskEnabled'] as bool : null,
      parent: m['parent']?.toString(),
      name: m['name']?.toString(),
      department: m['department']?.toString(),
      description: m['description']?.toString(),
      referenceId: (m['referenceID'] ?? m['referenceId'])?.toString(),
      projectHierarchy: m['projectHierarchy']?.toString(),
      nonRecoverableError: m['nonRecoverableError'] is bool
          ? m['nonRecoverableError'] as bool
          : false,
      tenantId: m['tenantId']?.toString(),
      rowVersion: m['rowVersion'] is int ? m['rowVersion'] as int : null,
      startDate: m['startDate'] is int ? m['startDate'] as int : null,
      endDate: m['endDate'] is int ? m['endDate'] as int : null,
    );

    // ---- Address (if present) ----
    if (m['address'] is Map) {
      try {
        model.address = AddressModelMapper.fromMap(
          Map<String, dynamic>.from(m['address'] as Map),
        );
      } catch (_) {
        // ignore bad shapes
      }
    }

    // ---- additionalDetails (manual, exact shape per your payload) ----
    if (m['additionalDetails'] is Map) {
      final ad = Map<String, dynamic>.from(m['additionalDetails'] as Map);

      final additional = AdditionalDetails()
        ..status = ad['status']?.toString()
        ..systemCode = ad['systemCode']?.toString();

      // facility
      if (ad['facility'] is Map) {
        final f = Map<String, dynamic>.from(ad['facility'] as Map);
        final facility = Facility()
          ..isActive = f['isActive'] is bool ? f['isActive'] as bool : null
          ..wfStatus = f['wfStatus']?.toString()
          ..tenantId = f['tenant_id']?.toString()
          ..facilityId = f['facility_id']?.toString()
          ..boundaryCode = f['boundaryCode']?.toString()
          ..facilityName = f['facility_name']?.toString()
          ..facilityType = f['facility_type']?.toString()
          ..facilityRegion = f['facility_region']?.toString()
          ..facility_subtype = f['facility_subtype']?.toString()
          ..facility_category = f['facility_category']?.toString()
          ..facility_ownership = f['facility_ownership']?.toString();

        // facility.address
        if (f['address'] is Map) {
          final fa = Map<String, dynamic>.from(f['address'] as Map);
          facility.address = FacilityAddress()
            ..city = fa['city']?.toString()
            ..type = fa['type']?.toString()
            ..block = fa['block']?.toString()
            ..state = fa['state']?.toString()
            ..detail = fa['detail']?.toString()
            ..doorNo = fa['doorNo']?.toString()
            ..street = fa['street']?.toString()
            ..pincode = fa['pincode']?.toString()
            ..district = fa['district']?.toString()
            ..landmark = fa['landmark']?.toString()
            ..latitude = (fa['latitude'] is num)
                ? (fa['latitude'] as num).toDouble()
                : null
            ..tenantId = fa['tenantId']?.toString()
            ..addressId = fa['addressId']?.toString()
            ..longitude = (fa['longitude'] is num)
                ? (fa['longitude'] as num).toDouble()
                : null
            ..addressLine1 = fa['addressLine1']?.toString()
            ..addressLine2 = fa['addressLine2']?.toString()
            ..buildingName = fa['buildingName']?.toString()
            ..localityCode = fa['localityCode']?.toString()
            ..addressNumber = fa['addressNumber']?.toString()
            ..locationAccuracy = (fa['locationAccuracy'] is num)
                ? (fa['locationAccuracy'] as num).toDouble()
                : null;
        }

        // facility.facility_details
        if (f['facility_details'] is Map) {
          final fd = Map<String, dynamic>.from(f['facility_details'] as Map);
          facility.facilityDetails = FacilityDetails()
            ..hfr_id = fd['hfr_id']?.toString()
            ..nin_id = fd['nin_id']?.toString()
            ..pocName = fd['pocName']?.toString()
            ..pocContact = fd['pocContact']?.toString()
            ..pocDesignation = fd['pocDesignation']?.toString()
            ..solar_solution_design_type =
                fd['solar_solution_design_type']?.toString();
        }

        additional.facility = facility;
      }

      model.additionalDetails = additional;
    }

    return model;
  }

  @override
  Map<String, dynamic> toJson(ProjectModel model) {
    // If you need to send back to server later, mapper is fine:
    ProjectModelMapper.ensureInitialized();
    return model.toMap();
  }
}

class WorkflowFlexConverter implements JsonConverter<Workflow?, Object?> {
  const WorkflowFlexConverter();

  @override
  Workflow? fromJson(Object? json) {
    if (json == null) return null;

    // Case 1: {} (empty map) → treat as no workflow
    if (json is Map) {
      if (json.isEmpty) return null;
      return Workflow.fromJson(Map<String, dynamic>.from(json));
    }

    // Case 2: [ {…}, … ] → take the FIRST element
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
