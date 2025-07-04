// Generated using mason. Do not modify by hand
import 'package:dart_mappable/dart_mappable.dart';
import 'package:isar/isar.dart';

import '../entities/address.dart';

part 'project.g.dart';
part 'project.mapper.dart';

@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class ProjectSearchModelWrapper with ProjectSearchModelWrapperMappable {
  final List<ProjectSearchModel>? projects;

  ProjectSearchModelWrapper({this.projects});
}

@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class ProjectSearchModel with ProjectSearchModelMappable {
  final String? id;
  final String? name;
  final String? projectTypeId;
  final String? projectNumber;
  final String? subProjectTypeId;
  final bool? isTaskEnabled;
  final String? parent;
  final String? department;
  final String? referenceId;
  final String? tenantId;
  final DateTime? startDateTime;
  final DateTime? endDateTime;

  ProjectSearchModel({
    this.id,
    this.name,
    this.projectTypeId,
    this.projectNumber,
    this.subProjectTypeId,
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

  @MappableConstructor()
  ProjectSearchModel.ignoreDeleted({
    this.id,
    this.name,
    this.projectTypeId,
    this.projectNumber,
    this.subProjectTypeId,
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
class ProjectModel with ProjectModelMappable {
  static const schemaName = 'Project';
  String id;
  String? projectType;
  String? projectTypeId;
  String? projectNumber;
  String? subProjectTypeId;
  bool? isTaskEnabled;
  String? parent;
  String? name;
  String? department;
  String? description;
  String? referenceId;
  String? projectHierarchy;
  bool? nonRecoverableError;
  String? tenantId;
  int? rowVersion;
  AddressModel? address;

  DateTime? startDateTime;
  DateTime? endDateTime;

  ProjectModel({
    this.id = '',
    this.projectType,
    this.projectTypeId,
    this.projectNumber,
    this.subProjectTypeId,
    this.isTaskEnabled,
    this.parent,
    this.name,
    this.department,
    this.description,
    this.referenceId,
    this.projectHierarchy,
    this.nonRecoverableError = false,
    this.tenantId,
    this.rowVersion,
    this.address,
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
