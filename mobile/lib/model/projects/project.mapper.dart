// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, unnecessary_cast, override_on_non_overriding_member
// ignore_for_file: strict_raw_type, inference_failure_on_untyped_parameter

part of 'project.dart';

class ProjectSearchModelWrapperMapper
    extends ClassMapperBase<ProjectSearchModelWrapper> {
  ProjectSearchModelWrapperMapper._();

  static ProjectSearchModelWrapperMapper? _instance;
  static ProjectSearchModelWrapperMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals
          .use(_instance = ProjectSearchModelWrapperMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ProjectSearchModelWrapper';

  static List<ProjectSearchModel>? _$projects(ProjectSearchModelWrapper v) =>
      v.projects;
  static const Field<ProjectSearchModelWrapper, List<ProjectSearchModel>>
      _f$projects = Field('projects', _$projects, opt: true);

  @override
  final MappableFields<ProjectSearchModelWrapper> fields = const {
    #projects: _f$projects,
  };
  @override
  final bool ignoreNull = true;

  static ProjectSearchModelWrapper _instantiate(DecodingData data) {
    return ProjectSearchModelWrapper(projects: data.dec(_f$projects));
  }

  @override
  final Function instantiate = _instantiate;

  static ProjectSearchModelWrapper fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<ProjectSearchModelWrapper>(map);
  }

  static ProjectSearchModelWrapper fromJson(String json) {
    return ensureInitialized().decodeJson<ProjectSearchModelWrapper>(json);
  }
}

mixin ProjectSearchModelWrapperMappable {
  String toJson() {
    return ProjectSearchModelWrapperMapper.ensureInitialized()
        .encodeJson<ProjectSearchModelWrapper>(
            this as ProjectSearchModelWrapper);
  }

  Map<String, dynamic> toMap() {
    return ProjectSearchModelWrapperMapper.ensureInitialized()
        .encodeMap<ProjectSearchModelWrapper>(
            this as ProjectSearchModelWrapper);
  }

  ProjectSearchModelWrapperCopyWith<ProjectSearchModelWrapper,
          ProjectSearchModelWrapper, ProjectSearchModelWrapper>
      get copyWith => _ProjectSearchModelWrapperCopyWithImpl(
          this as ProjectSearchModelWrapper, $identity, $identity);
  @override
  String toString() {
    return ProjectSearchModelWrapperMapper.ensureInitialized()
        .stringifyValue(this as ProjectSearchModelWrapper);
  }

  @override
  bool operator ==(Object other) {
    return ProjectSearchModelWrapperMapper.ensureInitialized()
        .equalsValue(this as ProjectSearchModelWrapper, other);
  }

  @override
  int get hashCode {
    return ProjectSearchModelWrapperMapper.ensureInitialized()
        .hashValue(this as ProjectSearchModelWrapper);
  }
}

extension ProjectSearchModelWrapperValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ProjectSearchModelWrapper, $Out> {
  ProjectSearchModelWrapperCopyWith<$R, ProjectSearchModelWrapper, $Out>
      get $asProjectSearchModelWrapper => $base
          .as((v, t, t2) => _ProjectSearchModelWrapperCopyWithImpl(v, t, t2));
}

abstract class ProjectSearchModelWrapperCopyWith<
    $R,
    $In extends ProjectSearchModelWrapper,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  ListCopyWith<
      $R,
      ProjectSearchModel,
      ProjectSearchModelCopyWith<$R, ProjectSearchModel,
          ProjectSearchModel>>? get projects;
  $R call({List<ProjectSearchModel>? projects});
  ProjectSearchModelWrapperCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _ProjectSearchModelWrapperCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ProjectSearchModelWrapper, $Out>
    implements
        ProjectSearchModelWrapperCopyWith<$R, ProjectSearchModelWrapper, $Out> {
  _ProjectSearchModelWrapperCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ProjectSearchModelWrapper> $mapper =
      ProjectSearchModelWrapperMapper.ensureInitialized();
  @override
  ListCopyWith<
      $R,
      ProjectSearchModel,
      ProjectSearchModelCopyWith<$R, ProjectSearchModel,
          ProjectSearchModel>>? get projects => $value.projects != null
      ? ListCopyWith($value.projects!, (v, t) => v.copyWith.$chain(t),
          (v) => call(projects: v))
      : null;
  @override
  $R call({Object? projects = $none}) =>
      $apply(FieldCopyWithData({if (projects != $none) #projects: projects}));
  @override
  ProjectSearchModelWrapper $make(CopyWithData data) =>
      ProjectSearchModelWrapper(
          projects: data.get(#projects, or: $value.projects));

  @override
  ProjectSearchModelWrapperCopyWith<$R2, ProjectSearchModelWrapper, $Out2>
      $chain<$R2, $Out2>(Then<$Out2, $R2> t) =>
          _ProjectSearchModelWrapperCopyWithImpl($value, $cast, t);
}

class ProjectSearchModelMapper extends ClassMapperBase<ProjectSearchModel> {
  ProjectSearchModelMapper._();

  static ProjectSearchModelMapper? _instance;
  static ProjectSearchModelMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = ProjectSearchModelMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ProjectSearchModel';

  static String? _$id(ProjectSearchModel v) => v.id;
  static const Field<ProjectSearchModel, String> _f$id =
      Field('id', _$id, opt: true);
  static String? _$name(ProjectSearchModel v) => v.name;
  static const Field<ProjectSearchModel, String> _f$name =
      Field('name', _$name, opt: true);
  static String? _$projectTypeId(ProjectSearchModel v) => v.projectTypeId;
  static const Field<ProjectSearchModel, String> _f$projectTypeId =
      Field('projectTypeId', _$projectTypeId, opt: true);
  static String? _$projectNumber(ProjectSearchModel v) => v.projectNumber;
  static const Field<ProjectSearchModel, String> _f$projectNumber =
      Field('projectNumber', _$projectNumber, opt: true);
  static String? _$subProjectTypeId(ProjectSearchModel v) => v.subProjectTypeId;
  static const Field<ProjectSearchModel, String> _f$subProjectTypeId =
      Field('subProjectTypeId', _$subProjectTypeId, opt: true);
  static bool? _$isTaskEnabled(ProjectSearchModel v) => v.isTaskEnabled;
  static const Field<ProjectSearchModel, bool> _f$isTaskEnabled =
      Field('isTaskEnabled', _$isTaskEnabled, opt: true);
  static String? _$parent(ProjectSearchModel v) => v.parent;
  static const Field<ProjectSearchModel, String> _f$parent =
      Field('parent', _$parent, opt: true);
  static String? _$department(ProjectSearchModel v) => v.department;
  static const Field<ProjectSearchModel, String> _f$department =
      Field('department', _$department, opt: true);
  static String? _$referenceId(ProjectSearchModel v) => v.referenceId;
  static const Field<ProjectSearchModel, String> _f$referenceId =
      Field('referenceId', _$referenceId, opt: true);
  static String? _$tenantId(ProjectSearchModel v) => v.tenantId;
  static const Field<ProjectSearchModel, String> _f$tenantId =
      Field('tenantId', _$tenantId, opt: true);
  static int? _$startDate(ProjectSearchModel v) => v.startDate;
  static const Field<ProjectSearchModel, int> _f$startDate =
      Field('startDate', _$startDate, opt: true);
  static int? _$endDate(ProjectSearchModel v) => v.endDate;
  static const Field<ProjectSearchModel, int> _f$endDate =
      Field('endDate', _$endDate, opt: true);
  static DateTime? _$startDateTime(ProjectSearchModel v) => v.startDateTime;
  static const Field<ProjectSearchModel, DateTime> _f$startDateTime =
      Field('startDateTime', _$startDateTime, mode: FieldMode.member);
  static DateTime? _$endDateTime(ProjectSearchModel v) => v.endDateTime;
  static const Field<ProjectSearchModel, DateTime> _f$endDateTime =
      Field('endDateTime', _$endDateTime, mode: FieldMode.member);

  @override
  final MappableFields<ProjectSearchModel> fields = const {
    #id: _f$id,
    #name: _f$name,
    #projectTypeId: _f$projectTypeId,
    #projectNumber: _f$projectNumber,
    #subProjectTypeId: _f$subProjectTypeId,
    #isTaskEnabled: _f$isTaskEnabled,
    #parent: _f$parent,
    #department: _f$department,
    #referenceId: _f$referenceId,
    #tenantId: _f$tenantId,
    #startDate: _f$startDate,
    #endDate: _f$endDate,
    #startDateTime: _f$startDateTime,
    #endDateTime: _f$endDateTime,
  };
  @override
  final bool ignoreNull = true;

  static ProjectSearchModel _instantiate(DecodingData data) {
    return ProjectSearchModel.ignoreDeleted(
        id: data.dec(_f$id),
        name: data.dec(_f$name),
        projectTypeId: data.dec(_f$projectTypeId),
        projectNumber: data.dec(_f$projectNumber),
        subProjectTypeId: data.dec(_f$subProjectTypeId),
        isTaskEnabled: data.dec(_f$isTaskEnabled),
        parent: data.dec(_f$parent),
        department: data.dec(_f$department),
        referenceId: data.dec(_f$referenceId),
        tenantId: data.dec(_f$tenantId),
        startDate: data.dec(_f$startDate),
        endDate: data.dec(_f$endDate));
  }

  @override
  final Function instantiate = _instantiate;

  static ProjectSearchModel fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<ProjectSearchModel>(map);
  }

  static ProjectSearchModel fromJson(String json) {
    return ensureInitialized().decodeJson<ProjectSearchModel>(json);
  }
}

mixin ProjectSearchModelMappable {
  String toJson() {
    return ProjectSearchModelMapper.ensureInitialized()
        .encodeJson<ProjectSearchModel>(this as ProjectSearchModel);
  }

  Map<String, dynamic> toMap() {
    return ProjectSearchModelMapper.ensureInitialized()
        .encodeMap<ProjectSearchModel>(this as ProjectSearchModel);
  }

  ProjectSearchModelCopyWith<ProjectSearchModel, ProjectSearchModel,
          ProjectSearchModel>
      get copyWith => _ProjectSearchModelCopyWithImpl(
          this as ProjectSearchModel, $identity, $identity);
  @override
  String toString() {
    return ProjectSearchModelMapper.ensureInitialized()
        .stringifyValue(this as ProjectSearchModel);
  }

  @override
  bool operator ==(Object other) {
    return ProjectSearchModelMapper.ensureInitialized()
        .equalsValue(this as ProjectSearchModel, other);
  }

  @override
  int get hashCode {
    return ProjectSearchModelMapper.ensureInitialized()
        .hashValue(this as ProjectSearchModel);
  }
}

extension ProjectSearchModelValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ProjectSearchModel, $Out> {
  ProjectSearchModelCopyWith<$R, ProjectSearchModel, $Out>
      get $asProjectSearchModel =>
          $base.as((v, t, t2) => _ProjectSearchModelCopyWithImpl(v, t, t2));
}

abstract class ProjectSearchModelCopyWith<$R, $In extends ProjectSearchModel,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  $R call(
      {String? id,
      String? name,
      String? projectTypeId,
      String? projectNumber,
      String? subProjectTypeId,
      bool? isTaskEnabled,
      String? parent,
      String? department,
      String? referenceId,
      String? tenantId,
      int? startDate,
      int? endDate});
  ProjectSearchModelCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _ProjectSearchModelCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ProjectSearchModel, $Out>
    implements ProjectSearchModelCopyWith<$R, ProjectSearchModel, $Out> {
  _ProjectSearchModelCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ProjectSearchModel> $mapper =
      ProjectSearchModelMapper.ensureInitialized();
  @override
  $R call(
          {Object? id = $none,
          Object? name = $none,
          Object? projectTypeId = $none,
          Object? projectNumber = $none,
          Object? subProjectTypeId = $none,
          Object? isTaskEnabled = $none,
          Object? parent = $none,
          Object? department = $none,
          Object? referenceId = $none,
          Object? tenantId = $none,
          Object? startDate = $none,
          Object? endDate = $none}) =>
      $apply(FieldCopyWithData({
        if (id != $none) #id: id,
        if (name != $none) #name: name,
        if (projectTypeId != $none) #projectTypeId: projectTypeId,
        if (projectNumber != $none) #projectNumber: projectNumber,
        if (subProjectTypeId != $none) #subProjectTypeId: subProjectTypeId,
        if (isTaskEnabled != $none) #isTaskEnabled: isTaskEnabled,
        if (parent != $none) #parent: parent,
        if (department != $none) #department: department,
        if (referenceId != $none) #referenceId: referenceId,
        if (tenantId != $none) #tenantId: tenantId,
        if (startDate != $none) #startDate: startDate,
        if (endDate != $none) #endDate: endDate
      }));
  @override
  ProjectSearchModel $make(CopyWithData data) =>
      ProjectSearchModel.ignoreDeleted(
          id: data.get(#id, or: $value.id),
          name: data.get(#name, or: $value.name),
          projectTypeId: data.get(#projectTypeId, or: $value.projectTypeId),
          projectNumber: data.get(#projectNumber, or: $value.projectNumber),
          subProjectTypeId:
              data.get(#subProjectTypeId, or: $value.subProjectTypeId),
          isTaskEnabled: data.get(#isTaskEnabled, or: $value.isTaskEnabled),
          parent: data.get(#parent, or: $value.parent),
          department: data.get(#department, or: $value.department),
          referenceId: data.get(#referenceId, or: $value.referenceId),
          tenantId: data.get(#tenantId, or: $value.tenantId),
          startDate: data.get(#startDate, or: $value.startDate),
          endDate: data.get(#endDate, or: $value.endDate));

  @override
  ProjectSearchModelCopyWith<$R2, ProjectSearchModel, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _ProjectSearchModelCopyWithImpl($value, $cast, t);
}

class ProjectModelMapper extends ClassMapperBase<ProjectModel> {
  ProjectModelMapper._();

  static ProjectModelMapper? _instance;
  static ProjectModelMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = ProjectModelMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ProjectModel';

  static String _$id(ProjectModel v) => v.id;
  static const Field<ProjectModel, String> _f$id =
      Field('id', _$id, opt: true, def: '');
  static String? _$projectType(ProjectModel v) => v.projectType;
  static const Field<ProjectModel, String> _f$projectType =
      Field('projectType', _$projectType, opt: true);
  static String? _$projectTypeId(ProjectModel v) => v.projectTypeId;
  static const Field<ProjectModel, String> _f$projectTypeId =
      Field('projectTypeId', _$projectTypeId, opt: true);
  static String? _$projectNumber(ProjectModel v) => v.projectNumber;
  static const Field<ProjectModel, String> _f$projectNumber =
      Field('projectNumber', _$projectNumber, opt: true);
  static String? _$subProjectTypeId(ProjectModel v) => v.subProjectTypeId;
  static const Field<ProjectModel, String> _f$subProjectTypeId =
      Field('subProjectTypeId', _$subProjectTypeId, opt: true);
  static bool? _$isTaskEnabled(ProjectModel v) => v.isTaskEnabled;
  static const Field<ProjectModel, bool> _f$isTaskEnabled =
      Field('isTaskEnabled', _$isTaskEnabled, opt: true);
  static String? _$parent(ProjectModel v) => v.parent;
  static const Field<ProjectModel, String> _f$parent =
      Field('parent', _$parent, opt: true);
  static String? _$name(ProjectModel v) => v.name;
  static const Field<ProjectModel, String> _f$name =
      Field('name', _$name, opt: true);
  static String? _$department(ProjectModel v) => v.department;
  static const Field<ProjectModel, String> _f$department =
      Field('department', _$department, opt: true);
  static String? _$description(ProjectModel v) => v.description;
  static const Field<ProjectModel, String> _f$description =
      Field('description', _$description, opt: true);
  static String? _$referenceId(ProjectModel v) => v.referenceId;
  static const Field<ProjectModel, String> _f$referenceId =
      Field('referenceId', _$referenceId, key: 'referenceID', opt: true);
  static String? _$projectHierarchy(ProjectModel v) => v.projectHierarchy;
  static const Field<ProjectModel, String> _f$projectHierarchy =
      Field('projectHierarchy', _$projectHierarchy, opt: true);
  static bool? _$nonRecoverableError(ProjectModel v) => v.nonRecoverableError;
  static const Field<ProjectModel, bool> _f$nonRecoverableError = Field(
      'nonRecoverableError', _$nonRecoverableError,
      opt: true, def: false);
  static String? _$tenantId(ProjectModel v) => v.tenantId;
  static const Field<ProjectModel, String> _f$tenantId =
      Field('tenantId', _$tenantId, opt: true);
  static int? _$rowVersion(ProjectModel v) => v.rowVersion;
  static const Field<ProjectModel, int> _f$rowVersion =
      Field('rowVersion', _$rowVersion, opt: true);
  static AddressModel? _$address(ProjectModel v) => v.address;
  static const Field<ProjectModel, AddressModel> _f$address =
      Field('address', _$address, opt: true);
  static AdditionalDetails? _$additionalDetails(ProjectModel v) =>
      v.additionalDetails;
  static const Field<ProjectModel, AdditionalDetails> _f$additionalDetails =
      Field('additionalDetails', _$additionalDetails, opt: true);
  static int? _$startDate(ProjectModel v) => v.startDate;
  static const Field<ProjectModel, int> _f$startDate =
      Field('startDate', _$startDate, opt: true);
  static int? _$endDate(ProjectModel v) => v.endDate;
  static const Field<ProjectModel, int> _f$endDate =
      Field('endDate', _$endDate, opt: true);
  static DateTime? _$startDateTime(ProjectModel v) => v.startDateTime;
  static const Field<ProjectModel, DateTime> _f$startDateTime =
      Field('startDateTime', _$startDateTime, mode: FieldMode.member);
  static DateTime? _$endDateTime(ProjectModel v) => v.endDateTime;
  static const Field<ProjectModel, DateTime> _f$endDateTime =
      Field('endDateTime', _$endDateTime, mode: FieldMode.member);

  @override
  final MappableFields<ProjectModel> fields = const {
    #id: _f$id,
    #projectType: _f$projectType,
    #projectTypeId: _f$projectTypeId,
    #projectNumber: _f$projectNumber,
    #subProjectTypeId: _f$subProjectTypeId,
    #isTaskEnabled: _f$isTaskEnabled,
    #parent: _f$parent,
    #name: _f$name,
    #department: _f$department,
    #description: _f$description,
    #referenceId: _f$referenceId,
    #projectHierarchy: _f$projectHierarchy,
    #nonRecoverableError: _f$nonRecoverableError,
    #tenantId: _f$tenantId,
    #rowVersion: _f$rowVersion,
    #address: _f$address,
    #additionalDetails: _f$additionalDetails,
    #startDate: _f$startDate,
    #endDate: _f$endDate,
    #startDateTime: _f$startDateTime,
    #endDateTime: _f$endDateTime,
  };
  @override
  final bool ignoreNull = true;

  static ProjectModel _instantiate(DecodingData data) {
    return ProjectModel(
        id: data.dec(_f$id),
        projectType: data.dec(_f$projectType),
        projectTypeId: data.dec(_f$projectTypeId),
        projectNumber: data.dec(_f$projectNumber),
        subProjectTypeId: data.dec(_f$subProjectTypeId),
        isTaskEnabled: data.dec(_f$isTaskEnabled),
        parent: data.dec(_f$parent),
        name: data.dec(_f$name),
        department: data.dec(_f$department),
        description: data.dec(_f$description),
        referenceId: data.dec(_f$referenceId),
        projectHierarchy: data.dec(_f$projectHierarchy),
        nonRecoverableError: data.dec(_f$nonRecoverableError),
        tenantId: data.dec(_f$tenantId),
        rowVersion: data.dec(_f$rowVersion),
        address: data.dec(_f$address),
        additionalDetails: data.dec(_f$additionalDetails),
        startDate: data.dec(_f$startDate),
        endDate: data.dec(_f$endDate));
  }

  @override
  final Function instantiate = _instantiate;

  static ProjectModel fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<ProjectModel>(map);
  }

  static ProjectModel fromJson(String json) {
    return ensureInitialized().decodeJson<ProjectModel>(json);
  }
}

mixin ProjectModelMappable {
  String toJson() {
    return ProjectModelMapper.ensureInitialized()
        .encodeJson<ProjectModel>(this as ProjectModel);
  }

  Map<String, dynamic> toMap() {
    return ProjectModelMapper.ensureInitialized()
        .encodeMap<ProjectModel>(this as ProjectModel);
  }

  ProjectModelCopyWith<ProjectModel, ProjectModel, ProjectModel> get copyWith =>
      _ProjectModelCopyWithImpl(this as ProjectModel, $identity, $identity);
  @override
  String toString() {
    return ProjectModelMapper.ensureInitialized()
        .stringifyValue(this as ProjectModel);
  }

  @override
  bool operator ==(Object other) {
    return ProjectModelMapper.ensureInitialized()
        .equalsValue(this as ProjectModel, other);
  }

  @override
  int get hashCode {
    return ProjectModelMapper.ensureInitialized()
        .hashValue(this as ProjectModel);
  }
}

extension ProjectModelValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ProjectModel, $Out> {
  ProjectModelCopyWith<$R, ProjectModel, $Out> get $asProjectModel =>
      $base.as((v, t, t2) => _ProjectModelCopyWithImpl(v, t, t2));
}

abstract class ProjectModelCopyWith<$R, $In extends ProjectModel, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  AddressModelCopyWith<$R, AddressModel, AddressModel>? get address;
  AdditionalDetailsCopyWith<$R, AdditionalDetails, AdditionalDetails>?
      get additionalDetails;
  $R call(
      {String? id,
      String? projectType,
      String? projectTypeId,
      String? projectNumber,
      String? subProjectTypeId,
      bool? isTaskEnabled,
      String? parent,
      String? name,
      String? department,
      String? description,
      String? referenceId,
      String? projectHierarchy,
      bool? nonRecoverableError,
      String? tenantId,
      int? rowVersion,
      AddressModel? address,
      AdditionalDetails? additionalDetails,
      int? startDate,
      int? endDate});
  ProjectModelCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _ProjectModelCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ProjectModel, $Out>
    implements ProjectModelCopyWith<$R, ProjectModel, $Out> {
  _ProjectModelCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ProjectModel> $mapper =
      ProjectModelMapper.ensureInitialized();
  @override
  AddressModelCopyWith<$R, AddressModel, AddressModel>? get address =>
      $value.address?.copyWith.$chain((v) => call(address: v));
  @override
  AdditionalDetailsCopyWith<$R, AdditionalDetails, AdditionalDetails>?
      get additionalDetails => $value.additionalDetails?.copyWith
          .$chain((v) => call(additionalDetails: v));
  @override
  $R call(
          {String? id,
          Object? projectType = $none,
          Object? projectTypeId = $none,
          Object? projectNumber = $none,
          Object? subProjectTypeId = $none,
          Object? isTaskEnabled = $none,
          Object? parent = $none,
          Object? name = $none,
          Object? department = $none,
          Object? description = $none,
          Object? referenceId = $none,
          Object? projectHierarchy = $none,
          Object? nonRecoverableError = $none,
          Object? tenantId = $none,
          Object? rowVersion = $none,
          Object? address = $none,
          Object? additionalDetails = $none,
          Object? startDate = $none,
          Object? endDate = $none}) =>
      $apply(FieldCopyWithData({
        if (id != null) #id: id,
        if (projectType != $none) #projectType: projectType,
        if (projectTypeId != $none) #projectTypeId: projectTypeId,
        if (projectNumber != $none) #projectNumber: projectNumber,
        if (subProjectTypeId != $none) #subProjectTypeId: subProjectTypeId,
        if (isTaskEnabled != $none) #isTaskEnabled: isTaskEnabled,
        if (parent != $none) #parent: parent,
        if (name != $none) #name: name,
        if (department != $none) #department: department,
        if (description != $none) #description: description,
        if (referenceId != $none) #referenceId: referenceId,
        if (projectHierarchy != $none) #projectHierarchy: projectHierarchy,
        if (nonRecoverableError != $none)
          #nonRecoverableError: nonRecoverableError,
        if (tenantId != $none) #tenantId: tenantId,
        if (rowVersion != $none) #rowVersion: rowVersion,
        if (address != $none) #address: address,
        if (additionalDetails != $none) #additionalDetails: additionalDetails,
        if (startDate != $none) #startDate: startDate,
        if (endDate != $none) #endDate: endDate
      }));
  @override
  ProjectModel $make(CopyWithData data) => ProjectModel(
      id: data.get(#id, or: $value.id),
      projectType: data.get(#projectType, or: $value.projectType),
      projectTypeId: data.get(#projectTypeId, or: $value.projectTypeId),
      projectNumber: data.get(#projectNumber, or: $value.projectNumber),
      subProjectTypeId:
          data.get(#subProjectTypeId, or: $value.subProjectTypeId),
      isTaskEnabled: data.get(#isTaskEnabled, or: $value.isTaskEnabled),
      parent: data.get(#parent, or: $value.parent),
      name: data.get(#name, or: $value.name),
      department: data.get(#department, or: $value.department),
      description: data.get(#description, or: $value.description),
      referenceId: data.get(#referenceId, or: $value.referenceId),
      projectHierarchy:
          data.get(#projectHierarchy, or: $value.projectHierarchy),
      nonRecoverableError:
          data.get(#nonRecoverableError, or: $value.nonRecoverableError),
      tenantId: data.get(#tenantId, or: $value.tenantId),
      rowVersion: data.get(#rowVersion, or: $value.rowVersion),
      address: data.get(#address, or: $value.address),
      additionalDetails:
          data.get(#additionalDetails, or: $value.additionalDetails),
      startDate: data.get(#startDate, or: $value.startDate),
      endDate: data.get(#endDate, or: $value.endDate));

  @override
  ProjectModelCopyWith<$R2, ProjectModel, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _ProjectModelCopyWithImpl($value, $cast, t);
}

class AdditionalDetailsMapper extends ClassMapperBase<AdditionalDetails> {
  AdditionalDetailsMapper._();

  static AdditionalDetailsMapper? _instance;
  static AdditionalDetailsMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = AdditionalDetailsMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'AdditionalDetails';

  static String? _$status(AdditionalDetails v) => v.status;
  static const Field<AdditionalDetails, String> _f$status =
      Field('status', _$status, mode: FieldMode.member);
  static Facility? _$facility(AdditionalDetails v) => v.facility;
  static const Field<AdditionalDetails, Facility> _f$facility =
      Field('facility', _$facility, mode: FieldMode.member);
  static String? _$systemCode(AdditionalDetails v) => v.systemCode;
  static const Field<AdditionalDetails, String> _f$systemCode =
      Field('systemCode', _$systemCode, mode: FieldMode.member);

  @override
  final MappableFields<AdditionalDetails> fields = const {
    #status: _f$status,
    #facility: _f$facility,
    #systemCode: _f$systemCode,
  };
  @override
  final bool ignoreNull = true;

  static AdditionalDetails _instantiate(DecodingData data) {
    return AdditionalDetails();
  }

  @override
  final Function instantiate = _instantiate;

  static AdditionalDetails fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<AdditionalDetails>(map);
  }

  static AdditionalDetails fromJson(String json) {
    return ensureInitialized().decodeJson<AdditionalDetails>(json);
  }
}

mixin AdditionalDetailsMappable {
  String toJson() {
    return AdditionalDetailsMapper.ensureInitialized()
        .encodeJson<AdditionalDetails>(this as AdditionalDetails);
  }

  Map<String, dynamic> toMap() {
    return AdditionalDetailsMapper.ensureInitialized()
        .encodeMap<AdditionalDetails>(this as AdditionalDetails);
  }

  AdditionalDetailsCopyWith<AdditionalDetails, AdditionalDetails,
          AdditionalDetails>
      get copyWith => _AdditionalDetailsCopyWithImpl(
          this as AdditionalDetails, $identity, $identity);
  @override
  String toString() {
    return AdditionalDetailsMapper.ensureInitialized()
        .stringifyValue(this as AdditionalDetails);
  }

  @override
  bool operator ==(Object other) {
    return AdditionalDetailsMapper.ensureInitialized()
        .equalsValue(this as AdditionalDetails, other);
  }

  @override
  int get hashCode {
    return AdditionalDetailsMapper.ensureInitialized()
        .hashValue(this as AdditionalDetails);
  }
}

extension AdditionalDetailsValueCopy<$R, $Out>
    on ObjectCopyWith<$R, AdditionalDetails, $Out> {
  AdditionalDetailsCopyWith<$R, AdditionalDetails, $Out>
      get $asAdditionalDetails =>
          $base.as((v, t, t2) => _AdditionalDetailsCopyWithImpl(v, t, t2));
}

abstract class AdditionalDetailsCopyWith<$R, $In extends AdditionalDetails,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  AdditionalDetailsCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _AdditionalDetailsCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, AdditionalDetails, $Out>
    implements AdditionalDetailsCopyWith<$R, AdditionalDetails, $Out> {
  _AdditionalDetailsCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<AdditionalDetails> $mapper =
      AdditionalDetailsMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  AdditionalDetails $make(CopyWithData data) => AdditionalDetails();

  @override
  AdditionalDetailsCopyWith<$R2, AdditionalDetails, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _AdditionalDetailsCopyWithImpl($value, $cast, t);
}

class FacilityAddressMapper extends ClassMapperBase<FacilityAddress> {
  FacilityAddressMapper._();

  static FacilityAddressMapper? _instance;
  static FacilityAddressMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = FacilityAddressMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'FacilityAddress';

  static String? _$city(FacilityAddress v) => v.city;
  static const Field<FacilityAddress, String> _f$city =
      Field('city', _$city, mode: FieldMode.member);
  static String? _$type(FacilityAddress v) => v.type;
  static const Field<FacilityAddress, String> _f$type =
      Field('type', _$type, mode: FieldMode.member);
  static String? _$block(FacilityAddress v) => v.block;
  static const Field<FacilityAddress, String> _f$block =
      Field('block', _$block, mode: FieldMode.member);
  static String? _$state(FacilityAddress v) => v.state;
  static const Field<FacilityAddress, String> _f$state =
      Field('state', _$state, mode: FieldMode.member);
  static String? _$detail(FacilityAddress v) => v.detail;
  static const Field<FacilityAddress, String> _f$detail =
      Field('detail', _$detail, mode: FieldMode.member);
  static String? _$doorNo(FacilityAddress v) => v.doorNo;
  static const Field<FacilityAddress, String> _f$doorNo =
      Field('doorNo', _$doorNo);
  static String? _$street(FacilityAddress v) => v.street;
  static const Field<FacilityAddress, String> _f$street =
      Field('street', _$street, mode: FieldMode.member);
  static String? _$pincode(FacilityAddress v) => v.pincode;
  static const Field<FacilityAddress, String> _f$pincode =
      Field('pincode', _$pincode, mode: FieldMode.member);
  static String? _$district(FacilityAddress v) => v.district;
  static const Field<FacilityAddress, String> _f$district =
      Field('district', _$district, mode: FieldMode.member);
  static String? _$landmark(FacilityAddress v) => v.landmark;
  static const Field<FacilityAddress, String> _f$landmark =
      Field('landmark', _$landmark, mode: FieldMode.member);
  static double? _$latitude(FacilityAddress v) => v.latitude;
  static const Field<FacilityAddress, double> _f$latitude =
      Field('latitude', _$latitude, mode: FieldMode.member);
  static String? _$tenantId(FacilityAddress v) => v.tenantId;
  static const Field<FacilityAddress, String> _f$tenantId =
      Field('tenantId', _$tenantId);
  static String? _$addressId(FacilityAddress v) => v.addressId;
  static const Field<FacilityAddress, String> _f$addressId =
      Field('addressId', _$addressId);
  static double? _$longitude(FacilityAddress v) => v.longitude;
  static const Field<FacilityAddress, double> _f$longitude =
      Field('longitude', _$longitude, mode: FieldMode.member);
  static String? _$addressLine1(FacilityAddress v) => v.addressLine1;
  static const Field<FacilityAddress, String> _f$addressLine1 =
      Field('addressLine1', _$addressLine1, mode: FieldMode.member);
  static String? _$addressLine2(FacilityAddress v) => v.addressLine2;
  static const Field<FacilityAddress, String> _f$addressLine2 =
      Field('addressLine2', _$addressLine2, mode: FieldMode.member);
  static String? _$buildingName(FacilityAddress v) => v.buildingName;
  static const Field<FacilityAddress, String> _f$buildingName =
      Field('buildingName', _$buildingName, mode: FieldMode.member);
  static String? _$localityCode(FacilityAddress v) => v.localityCode;
  static const Field<FacilityAddress, String> _f$localityCode =
      Field('localityCode', _$localityCode, mode: FieldMode.member);
  static String? _$addressNumber(FacilityAddress v) => v.addressNumber;
  static const Field<FacilityAddress, String> _f$addressNumber =
      Field('addressNumber', _$addressNumber, mode: FieldMode.member);
  static double? _$locationAccuracy(FacilityAddress v) => v.locationAccuracy;
  static const Field<FacilityAddress, double> _f$locationAccuracy =
      Field('locationAccuracy', _$locationAccuracy, mode: FieldMode.member);

  @override
  final MappableFields<FacilityAddress> fields = const {
    #city: _f$city,
    #type: _f$type,
    #block: _f$block,
    #state: _f$state,
    #detail: _f$detail,
    #doorNo: _f$doorNo,
    #street: _f$street,
    #pincode: _f$pincode,
    #district: _f$district,
    #landmark: _f$landmark,
    #latitude: _f$latitude,
    #tenantId: _f$tenantId,
    #addressId: _f$addressId,
    #longitude: _f$longitude,
    #addressLine1: _f$addressLine1,
    #addressLine2: _f$addressLine2,
    #buildingName: _f$buildingName,
    #localityCode: _f$localityCode,
    #addressNumber: _f$addressNumber,
    #locationAccuracy: _f$locationAccuracy,
  };
  @override
  final bool ignoreNull = true;

  static FacilityAddress _instantiate(DecodingData data) {
    return FacilityAddress();
  }

  @override
  final Function instantiate = _instantiate;

  static FacilityAddress fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<FacilityAddress>(map);
  }

  static FacilityAddress fromJson(String json) {
    return ensureInitialized().decodeJson<FacilityAddress>(json);
  }
}

mixin FacilityAddressMappable {
  String toJson() {
    return FacilityAddressMapper.ensureInitialized()
        .encodeJson<FacilityAddress>(this as FacilityAddress);
  }

  Map<String, dynamic> toMap() {
    return FacilityAddressMapper.ensureInitialized()
        .encodeMap<FacilityAddress>(this as FacilityAddress);
  }

  FacilityAddressCopyWith<FacilityAddress, FacilityAddress, FacilityAddress>
      get copyWith => _FacilityAddressCopyWithImpl(
          this as FacilityAddress, $identity, $identity);
  @override
  String toString() {
    return FacilityAddressMapper.ensureInitialized()
        .stringifyValue(this as FacilityAddress);
  }

  @override
  bool operator ==(Object other) {
    return FacilityAddressMapper.ensureInitialized()
        .equalsValue(this as FacilityAddress, other);
  }

  @override
  int get hashCode {
    return FacilityAddressMapper.ensureInitialized()
        .hashValue(this as FacilityAddress);
  }
}

extension FacilityAddressValueCopy<$R, $Out>
    on ObjectCopyWith<$R, FacilityAddress, $Out> {
  FacilityAddressCopyWith<$R, FacilityAddress, $Out> get $asFacilityAddress =>
      $base.as((v, t, t2) => _FacilityAddressCopyWithImpl(v, t, t2));
}

abstract class FacilityAddressCopyWith<$R, $In extends FacilityAddress, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  FacilityAddressCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _FacilityAddressCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, FacilityAddress, $Out>
    implements FacilityAddressCopyWith<$R, FacilityAddress, $Out> {
  _FacilityAddressCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<FacilityAddress> $mapper =
      FacilityAddressMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  FacilityAddress $make(CopyWithData data) => FacilityAddress();

  @override
  FacilityAddressCopyWith<$R2, FacilityAddress, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _FacilityAddressCopyWithImpl($value, $cast, t);
}

class FacilityDetailsMapper extends ClassMapperBase<FacilityDetails> {
  FacilityDetailsMapper._();

  static FacilityDetailsMapper? _instance;
  static FacilityDetailsMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = FacilityDetailsMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'FacilityDetails';

  static String? _$hfr_id(FacilityDetails v) => v.hfr_id;
  static const Field<FacilityDetails, String> _f$hfr_id =
      Field('hfr_id', _$hfr_id);
  static String? _$nin_id(FacilityDetails v) => v.nin_id;
  static const Field<FacilityDetails, String> _f$nin_id =
      Field('nin_id', _$nin_id);
  static String? _$pocName(FacilityDetails v) => v.pocName;
  static const Field<FacilityDetails, String> _f$pocName =
      Field('pocName', _$pocName, mode: FieldMode.member);
  static String? _$pocContact(FacilityDetails v) => v.pocContact;
  static const Field<FacilityDetails, String> _f$pocContact =
      Field('pocContact', _$pocContact, mode: FieldMode.member);
  static String? _$pocDesignation(FacilityDetails v) => v.pocDesignation;
  static const Field<FacilityDetails, String> _f$pocDesignation =
      Field('pocDesignation', _$pocDesignation, mode: FieldMode.member);
  static String? _$solar_solution_design_type(FacilityDetails v) =>
      v.solar_solution_design_type;
  static const Field<FacilityDetails, String> _f$solar_solution_design_type =
      Field('solar_solution_design_type', _$solar_solution_design_type);

  @override
  final MappableFields<FacilityDetails> fields = const {
    #hfr_id: _f$hfr_id,
    #nin_id: _f$nin_id,
    #pocName: _f$pocName,
    #pocContact: _f$pocContact,
    #pocDesignation: _f$pocDesignation,
    #solar_solution_design_type: _f$solar_solution_design_type,
  };
  @override
  final bool ignoreNull = true;

  static FacilityDetails _instantiate(DecodingData data) {
    return FacilityDetails();
  }

  @override
  final Function instantiate = _instantiate;

  static FacilityDetails fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<FacilityDetails>(map);
  }

  static FacilityDetails fromJson(String json) {
    return ensureInitialized().decodeJson<FacilityDetails>(json);
  }
}

mixin FacilityDetailsMappable {
  String toJson() {
    return FacilityDetailsMapper.ensureInitialized()
        .encodeJson<FacilityDetails>(this as FacilityDetails);
  }

  Map<String, dynamic> toMap() {
    return FacilityDetailsMapper.ensureInitialized()
        .encodeMap<FacilityDetails>(this as FacilityDetails);
  }

  FacilityDetailsCopyWith<FacilityDetails, FacilityDetails, FacilityDetails>
      get copyWith => _FacilityDetailsCopyWithImpl(
          this as FacilityDetails, $identity, $identity);
  @override
  String toString() {
    return FacilityDetailsMapper.ensureInitialized()
        .stringifyValue(this as FacilityDetails);
  }

  @override
  bool operator ==(Object other) {
    return FacilityDetailsMapper.ensureInitialized()
        .equalsValue(this as FacilityDetails, other);
  }

  @override
  int get hashCode {
    return FacilityDetailsMapper.ensureInitialized()
        .hashValue(this as FacilityDetails);
  }
}

extension FacilityDetailsValueCopy<$R, $Out>
    on ObjectCopyWith<$R, FacilityDetails, $Out> {
  FacilityDetailsCopyWith<$R, FacilityDetails, $Out> get $asFacilityDetails =>
      $base.as((v, t, t2) => _FacilityDetailsCopyWithImpl(v, t, t2));
}

abstract class FacilityDetailsCopyWith<$R, $In extends FacilityDetails, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  FacilityDetailsCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _FacilityDetailsCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, FacilityDetails, $Out>
    implements FacilityDetailsCopyWith<$R, FacilityDetails, $Out> {
  _FacilityDetailsCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<FacilityDetails> $mapper =
      FacilityDetailsMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  FacilityDetails $make(CopyWithData data) => FacilityDetails();

  @override
  FacilityDetailsCopyWith<$R2, FacilityDetails, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _FacilityDetailsCopyWithImpl($value, $cast, t);
}

class FacilityMapper extends ClassMapperBase<Facility> {
  FacilityMapper._();

  static FacilityMapper? _instance;
  static FacilityMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = FacilityMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'Facility';

  static FacilityAddress? _$address(Facility v) => v.address;
  static const Field<Facility, FacilityAddress> _f$address =
      Field('address', _$address, mode: FieldMode.member);
  static bool? _$isActive(Facility v) => v.isActive;
  static const Field<Facility, bool> _f$isActive =
      Field('isActive', _$isActive, mode: FieldMode.member);
  static String? _$wfStatus(Facility v) => v.wfStatus;
  static const Field<Facility, String> _f$wfStatus =
      Field('wfStatus', _$wfStatus, mode: FieldMode.member);
  static String? _$tenantId(Facility v) => v.tenantId;
  static const Field<Facility, String> _f$tenantId =
      Field('tenantId', _$tenantId, key: 'tenant_id');
  static String? _$facilityId(Facility v) => v.facilityId;
  static const Field<Facility, String> _f$facilityId =
      Field('facilityId', _$facilityId, key: 'facility_id');
  static String? _$boundaryCode(Facility v) => v.boundaryCode;
  static const Field<Facility, String> _f$boundaryCode =
      Field('boundaryCode', _$boundaryCode, mode: FieldMode.member);
  static String? _$facilityName(Facility v) => v.facilityName;
  static const Field<Facility, String> _f$facilityName =
      Field('facilityName', _$facilityName, key: 'facility_name');
  static String? _$facilityType(Facility v) => v.facilityType;
  static const Field<Facility, String> _f$facilityType =
      Field('facilityType', _$facilityType, key: 'facility_type');
  static String? _$facilityRegion(Facility v) => v.facilityRegion;
  static const Field<Facility, String> _f$facilityRegion =
      Field('facilityRegion', _$facilityRegion, key: 'facility_region');
  static FacilityDetails? _$facilityDetails(Facility v) => v.facilityDetails;
  static const Field<Facility, FacilityDetails> _f$facilityDetails =
      Field('facilityDetails', _$facilityDetails, key: 'facility_details');
  static String? _$facility_subtype(Facility v) => v.facility_subtype;
  static const Field<Facility, String> _f$facility_subtype =
      Field('facility_subtype', _$facility_subtype);
  static String? _$facility_category(Facility v) => v.facility_category;
  static const Field<Facility, String> _f$facility_category =
      Field('facility_category', _$facility_category);
  static String? _$facility_ownership(Facility v) => v.facility_ownership;
  static const Field<Facility, String> _f$facility_ownership =
      Field('facility_ownership', _$facility_ownership);

  @override
  final MappableFields<Facility> fields = const {
    #address: _f$address,
    #isActive: _f$isActive,
    #wfStatus: _f$wfStatus,
    #tenantId: _f$tenantId,
    #facilityId: _f$facilityId,
    #boundaryCode: _f$boundaryCode,
    #facilityName: _f$facilityName,
    #facilityType: _f$facilityType,
    #facilityRegion: _f$facilityRegion,
    #facilityDetails: _f$facilityDetails,
    #facility_subtype: _f$facility_subtype,
    #facility_category: _f$facility_category,
    #facility_ownership: _f$facility_ownership,
  };
  @override
  final bool ignoreNull = true;

  static Facility _instantiate(DecodingData data) {
    return Facility();
  }

  @override
  final Function instantiate = _instantiate;

  static Facility fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<Facility>(map);
  }

  static Facility fromJson(String json) {
    return ensureInitialized().decodeJson<Facility>(json);
  }
}

mixin FacilityMappable {
  String toJson() {
    return FacilityMapper.ensureInitialized()
        .encodeJson<Facility>(this as Facility);
  }

  Map<String, dynamic> toMap() {
    return FacilityMapper.ensureInitialized()
        .encodeMap<Facility>(this as Facility);
  }

  FacilityCopyWith<Facility, Facility, Facility> get copyWith =>
      _FacilityCopyWithImpl(this as Facility, $identity, $identity);
  @override
  String toString() {
    return FacilityMapper.ensureInitialized().stringifyValue(this as Facility);
  }

  @override
  bool operator ==(Object other) {
    return FacilityMapper.ensureInitialized()
        .equalsValue(this as Facility, other);
  }

  @override
  int get hashCode {
    return FacilityMapper.ensureInitialized().hashValue(this as Facility);
  }
}

extension FacilityValueCopy<$R, $Out> on ObjectCopyWith<$R, Facility, $Out> {
  FacilityCopyWith<$R, Facility, $Out> get $asFacility =>
      $base.as((v, t, t2) => _FacilityCopyWithImpl(v, t, t2));
}

abstract class FacilityCopyWith<$R, $In extends Facility, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  FacilityCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _FacilityCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, Facility, $Out>
    implements FacilityCopyWith<$R, Facility, $Out> {
  _FacilityCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<Facility> $mapper =
      FacilityMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  Facility $make(CopyWithData data) => Facility();

  @override
  FacilityCopyWith<$R2, Facility, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _FacilityCopyWithImpl($value, $cast, t);
}
