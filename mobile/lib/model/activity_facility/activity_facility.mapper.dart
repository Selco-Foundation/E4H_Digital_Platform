// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, unnecessary_cast, override_on_non_overriding_member
// ignore_for_file: strict_raw_type, inference_failure_on_untyped_parameter

part of 'activity_facility.dart';

class ActivityFacilitySearchModelWrapperMapper
    extends ClassMapperBase<ActivityFacilitySearchModelWrapper> {
  ActivityFacilitySearchModelWrapperMapper._();

  static ActivityFacilitySearchModelWrapperMapper? _instance;
  static ActivityFacilitySearchModelWrapperMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals
          .use(_instance = ActivityFacilitySearchModelWrapperMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ActivityFacilitySearchModelWrapper';

  static List<ActivityFacilitySearchModel>? _$items(
          ActivityFacilitySearchModelWrapper v) =>
      v.items;
  static const Field<ActivityFacilitySearchModelWrapper,
          List<ActivityFacilitySearchModel>> _f$items =
      Field('items', _$items, opt: true);

  @override
  final MappableFields<ActivityFacilitySearchModelWrapper> fields = const {
    #items: _f$items,
  };
  @override
  final bool ignoreNull = true;

  static ActivityFacilitySearchModelWrapper _instantiate(DecodingData data) {
    return ActivityFacilitySearchModelWrapper(items: data.dec(_f$items));
  }

  @override
  final Function instantiate = _instantiate;

  static ActivityFacilitySearchModelWrapper fromMap(Map<String, dynamic> map) {
    return ensureInitialized()
        .decodeMap<ActivityFacilitySearchModelWrapper>(map);
  }

  static ActivityFacilitySearchModelWrapper fromJson(String json) {
    return ensureInitialized()
        .decodeJson<ActivityFacilitySearchModelWrapper>(json);
  }
}

mixin ActivityFacilitySearchModelWrapperMappable {
  String toJson() {
    return ActivityFacilitySearchModelWrapperMapper.ensureInitialized()
        .encodeJson<ActivityFacilitySearchModelWrapper>(
            this as ActivityFacilitySearchModelWrapper);
  }

  Map<String, dynamic> toMap() {
    return ActivityFacilitySearchModelWrapperMapper.ensureInitialized()
        .encodeMap<ActivityFacilitySearchModelWrapper>(
            this as ActivityFacilitySearchModelWrapper);
  }

  ActivityFacilitySearchModelWrapperCopyWith<
          ActivityFacilitySearchModelWrapper,
          ActivityFacilitySearchModelWrapper,
          ActivityFacilitySearchModelWrapper>
      get copyWith => _ActivityFacilitySearchModelWrapperCopyWithImpl(
          this as ActivityFacilitySearchModelWrapper, $identity, $identity);
  @override
  String toString() {
    return ActivityFacilitySearchModelWrapperMapper.ensureInitialized()
        .stringifyValue(this as ActivityFacilitySearchModelWrapper);
  }

  @override
  bool operator ==(Object other) {
    return ActivityFacilitySearchModelWrapperMapper.ensureInitialized()
        .equalsValue(this as ActivityFacilitySearchModelWrapper, other);
  }

  @override
  int get hashCode {
    return ActivityFacilitySearchModelWrapperMapper.ensureInitialized()
        .hashValue(this as ActivityFacilitySearchModelWrapper);
  }
}

extension ActivityFacilitySearchModelWrapperValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ActivityFacilitySearchModelWrapper, $Out> {
  ActivityFacilitySearchModelWrapperCopyWith<$R,
          ActivityFacilitySearchModelWrapper, $Out>
      get $asActivityFacilitySearchModelWrapper => $base.as((v, t, t2) =>
          _ActivityFacilitySearchModelWrapperCopyWithImpl(v, t, t2));
}

abstract class ActivityFacilitySearchModelWrapperCopyWith<
    $R,
    $In extends ActivityFacilitySearchModelWrapper,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  ListCopyWith<
      $R,
      ActivityFacilitySearchModel,
      ActivityFacilitySearchModelCopyWith<$R, ActivityFacilitySearchModel,
          ActivityFacilitySearchModel>>? get items;
  $R call({List<ActivityFacilitySearchModel>? items});
  ActivityFacilitySearchModelWrapperCopyWith<$R2, $In, $Out2>
      $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _ActivityFacilitySearchModelWrapperCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ActivityFacilitySearchModelWrapper, $Out>
    implements
        ActivityFacilitySearchModelWrapperCopyWith<$R,
            ActivityFacilitySearchModelWrapper, $Out> {
  _ActivityFacilitySearchModelWrapperCopyWithImpl(
      super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ActivityFacilitySearchModelWrapper> $mapper =
      ActivityFacilitySearchModelWrapperMapper.ensureInitialized();
  @override
  ListCopyWith<
      $R,
      ActivityFacilitySearchModel,
      ActivityFacilitySearchModelCopyWith<$R, ActivityFacilitySearchModel,
          ActivityFacilitySearchModel>>? get items => $value.items != null
      ? ListCopyWith(
          $value.items!, (v, t) => v.copyWith.$chain(t), (v) => call(items: v))
      : null;
  @override
  $R call({Object? items = $none}) =>
      $apply(FieldCopyWithData({if (items != $none) #items: items}));
  @override
  ActivityFacilitySearchModelWrapper $make(CopyWithData data) =>
      ActivityFacilitySearchModelWrapper(
          items: data.get(#items, or: $value.items));

  @override
  ActivityFacilitySearchModelWrapperCopyWith<$R2,
      ActivityFacilitySearchModelWrapper, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _ActivityFacilitySearchModelWrapperCopyWithImpl($value, $cast, t);
}

class ActivityFacilitySearchModelMapper
    extends ClassMapperBase<ActivityFacilitySearchModel> {
  ActivityFacilitySearchModelMapper._();

  static ActivityFacilitySearchModelMapper? _instance;
  static ActivityFacilitySearchModelMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals
          .use(_instance = ActivityFacilitySearchModelMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ActivityFacilitySearchModel';

  static String? _$id(ActivityFacilitySearchModel v) => v.id;
  static const Field<ActivityFacilitySearchModel, String> _f$id =
      Field('id', _$id, opt: true);
  static String? _$name(ActivityFacilitySearchModel v) => v.name;
  static const Field<ActivityFacilitySearchModel, String> _f$name =
      Field('name', _$name, opt: true);
  static String? _$activityId(ActivityFacilitySearchModel v) => v.activityId;
  static const Field<ActivityFacilitySearchModel, String> _f$activityId =
      Field('activityId', _$activityId, opt: true);
  static String? _$facilityId(ActivityFacilitySearchModel v) => v.facilityId;
  static const Field<ActivityFacilitySearchModel, String> _f$facilityId =
      Field('facilityId', _$facilityId, opt: true);
  static bool? _$isTaskEnabled(ActivityFacilitySearchModel v) =>
      v.isTaskEnabled;
  static const Field<ActivityFacilitySearchModel, bool> _f$isTaskEnabled =
      Field('isTaskEnabled', _$isTaskEnabled, opt: true);
  static String? _$parent(ActivityFacilitySearchModel v) => v.parent;
  static const Field<ActivityFacilitySearchModel, String> _f$parent =
      Field('parent', _$parent, opt: true);
  static String? _$department(ActivityFacilitySearchModel v) => v.department;
  static const Field<ActivityFacilitySearchModel, String> _f$department =
      Field('department', _$department, opt: true);
  static String? _$referenceId(ActivityFacilitySearchModel v) => v.referenceId;
  static const Field<ActivityFacilitySearchModel, String> _f$referenceId =
      Field('referenceId', _$referenceId, opt: true);
  static String? _$tenantId(ActivityFacilitySearchModel v) => v.tenantId;
  static const Field<ActivityFacilitySearchModel, String> _f$tenantId =
      Field('tenantId', _$tenantId, opt: true);
  static int? _$startDate(ActivityFacilitySearchModel v) => v.startDate;
  static const Field<ActivityFacilitySearchModel, int> _f$startDate =
      Field('startDate', _$startDate, opt: true);
  static int? _$endDate(ActivityFacilitySearchModel v) => v.endDate;
  static const Field<ActivityFacilitySearchModel, int> _f$endDate =
      Field('endDate', _$endDate, opt: true);
  static DateTime? _$startDateTime(ActivityFacilitySearchModel v) =>
      v.startDateTime;
  static const Field<ActivityFacilitySearchModel, DateTime> _f$startDateTime =
      Field('startDateTime', _$startDateTime, mode: FieldMode.member);
  static DateTime? _$endDateTime(ActivityFacilitySearchModel v) =>
      v.endDateTime;
  static const Field<ActivityFacilitySearchModel, DateTime> _f$endDateTime =
      Field('endDateTime', _$endDateTime, mode: FieldMode.member);

  @override
  final MappableFields<ActivityFacilitySearchModel> fields = const {
    #id: _f$id,
    #name: _f$name,
    #activityId: _f$activityId,
    #facilityId: _f$facilityId,
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

  static ActivityFacilitySearchModel _instantiate(DecodingData data) {
    return ActivityFacilitySearchModel(
        id: data.dec(_f$id),
        name: data.dec(_f$name),
        activityId: data.dec(_f$activityId),
        facilityId: data.dec(_f$facilityId),
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

  static ActivityFacilitySearchModel fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<ActivityFacilitySearchModel>(map);
  }

  static ActivityFacilitySearchModel fromJson(String json) {
    return ensureInitialized().decodeJson<ActivityFacilitySearchModel>(json);
  }
}

mixin ActivityFacilitySearchModelMappable {
  String toJson() {
    return ActivityFacilitySearchModelMapper.ensureInitialized()
        .encodeJson<ActivityFacilitySearchModel>(
            this as ActivityFacilitySearchModel);
  }

  Map<String, dynamic> toMap() {
    return ActivityFacilitySearchModelMapper.ensureInitialized()
        .encodeMap<ActivityFacilitySearchModel>(
            this as ActivityFacilitySearchModel);
  }

  ActivityFacilitySearchModelCopyWith<ActivityFacilitySearchModel,
          ActivityFacilitySearchModel, ActivityFacilitySearchModel>
      get copyWith => _ActivityFacilitySearchModelCopyWithImpl(
          this as ActivityFacilitySearchModel, $identity, $identity);
  @override
  String toString() {
    return ActivityFacilitySearchModelMapper.ensureInitialized()
        .stringifyValue(this as ActivityFacilitySearchModel);
  }

  @override
  bool operator ==(Object other) {
    return ActivityFacilitySearchModelMapper.ensureInitialized()
        .equalsValue(this as ActivityFacilitySearchModel, other);
  }

  @override
  int get hashCode {
    return ActivityFacilitySearchModelMapper.ensureInitialized()
        .hashValue(this as ActivityFacilitySearchModel);
  }
}

extension ActivityFacilitySearchModelValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ActivityFacilitySearchModel, $Out> {
  ActivityFacilitySearchModelCopyWith<$R, ActivityFacilitySearchModel, $Out>
      get $asActivityFacilitySearchModel => $base
          .as((v, t, t2) => _ActivityFacilitySearchModelCopyWithImpl(v, t, t2));
}

abstract class ActivityFacilitySearchModelCopyWith<
    $R,
    $In extends ActivityFacilitySearchModel,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  $R call(
      {String? id,
      String? name,
      String? activityId,
      String? facilityId,
      bool? isTaskEnabled,
      String? parent,
      String? department,
      String? referenceId,
      String? tenantId,
      int? startDate,
      int? endDate});
  ActivityFacilitySearchModelCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _ActivityFacilitySearchModelCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ActivityFacilitySearchModel, $Out>
    implements
        ActivityFacilitySearchModelCopyWith<$R, ActivityFacilitySearchModel,
            $Out> {
  _ActivityFacilitySearchModelCopyWithImpl(
      super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ActivityFacilitySearchModel> $mapper =
      ActivityFacilitySearchModelMapper.ensureInitialized();
  @override
  $R call(
          {Object? id = $none,
          Object? name = $none,
          Object? activityId = $none,
          Object? facilityId = $none,
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
        if (activityId != $none) #activityId: activityId,
        if (facilityId != $none) #facilityId: facilityId,
        if (isTaskEnabled != $none) #isTaskEnabled: isTaskEnabled,
        if (parent != $none) #parent: parent,
        if (department != $none) #department: department,
        if (referenceId != $none) #referenceId: referenceId,
        if (tenantId != $none) #tenantId: tenantId,
        if (startDate != $none) #startDate: startDate,
        if (endDate != $none) #endDate: endDate
      }));
  @override
  ActivityFacilitySearchModel $make(CopyWithData data) =>
      ActivityFacilitySearchModel(
          id: data.get(#id, or: $value.id),
          name: data.get(#name, or: $value.name),
          activityId: data.get(#activityId, or: $value.activityId),
          facilityId: data.get(#facilityId, or: $value.facilityId),
          isTaskEnabled: data.get(#isTaskEnabled, or: $value.isTaskEnabled),
          parent: data.get(#parent, or: $value.parent),
          department: data.get(#department, or: $value.department),
          referenceId: data.get(#referenceId, or: $value.referenceId),
          tenantId: data.get(#tenantId, or: $value.tenantId),
          startDate: data.get(#startDate, or: $value.startDate),
          endDate: data.get(#endDate, or: $value.endDate));

  @override
  ActivityFacilitySearchModelCopyWith<$R2, ActivityFacilitySearchModel, $Out2>
      $chain<$R2, $Out2>(Then<$Out2, $R2> t) =>
          _ActivityFacilitySearchModelCopyWithImpl($value, $cast, t);
}

class ActivityFacilityMapper extends ClassMapperBase<ActivityFacility> {
  ActivityFacilityMapper._();

  static ActivityFacilityMapper? _instance;
  static ActivityFacilityMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = ActivityFacilityMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ActivityFacility';

  static String _$id(ActivityFacility v) => v.id;
  static const Field<ActivityFacility, String> _f$id =
      Field('id', _$id, opt: true, def: '');
  static String? _$tenantId(ActivityFacility v) => v.tenantId;
  static const Field<ActivityFacility, String> _f$tenantId =
      Field('tenantId', _$tenantId, opt: true);
  static String? _$activityId(ActivityFacility v) => v.activityId;
  static const Field<ActivityFacility, String> _f$activityId =
      Field('activityId', _$activityId, opt: true);
  static String? _$fieldPlanId(ActivityFacility v) => v.fieldPlanId;
  static const Field<ActivityFacility, String> _f$fieldPlanId =
      Field('fieldPlanId', _$fieldPlanId, opt: true);
  static String? _$facilityId(ActivityFacility v) => v.facilityId;
  static const Field<ActivityFacility, String> _f$facilityId =
      Field('facilityId', _$facilityId, opt: true);
  static String? _$status(ActivityFacility v) => v.status;
  static const Field<ActivityFacility, String> _f$status =
      Field('status', _$status, opt: true);
  static DateTime? _$scheduledAt(ActivityFacility v) => v.scheduledAt;
  static const Field<ActivityFacility, DateTime> _f$scheduledAt =
      Field('scheduledAt', _$scheduledAt, opt: true);
  static DateTime? _$activatedAt(ActivityFacility v) => v.activatedAt;
  static const Field<ActivityFacility, DateTime> _f$activatedAt =
      Field('activatedAt', _$activatedAt, opt: true);
  static DateTime? _$completedAt(ActivityFacility v) => v.completedAt;
  static const Field<ActivityFacility, DateTime> _f$completedAt =
      Field('completedAt', _$completedAt, opt: true);
  static String? _$assignedUser(ActivityFacility v) => v.assignedUser;
  static const Field<ActivityFacility, String> _f$assignedUser =
      Field('assignedUser', _$assignedUser, opt: true);
  static String? _$assignedEmployeeUser(ActivityFacility v) =>
      v.assignedEmployeeUser;
  static const Field<ActivityFacility, String> _f$assignedEmployeeUser =
      Field('assignedEmployeeUser', _$assignedEmployeeUser, opt: true);
  static AddressModel? _$address(ActivityFacility v) => v.address;
  static const Field<ActivityFacility, AddressModel> _f$address =
      Field('address', _$address, opt: true);
  static Facility? _$facility(ActivityFacility v) => v.facility;
  static const Field<ActivityFacility, Facility> _f$facility =
      Field('facility', _$facility, opt: true);
  static String? _$description(ActivityFacility v) => v.description;
  static const Field<ActivityFacility, String> _f$description =
      Field('description', _$description, opt: true);
  static int? _$rowVersion(ActivityFacility v) => v.rowVersion;
  static const Field<ActivityFacility, int> _f$rowVersion =
      Field('rowVersion', _$rowVersion, opt: true);

  @override
  final MappableFields<ActivityFacility> fields = const {
    #id: _f$id,
    #tenantId: _f$tenantId,
    #activityId: _f$activityId,
    #fieldPlanId: _f$fieldPlanId,
    #facilityId: _f$facilityId,
    #status: _f$status,
    #scheduledAt: _f$scheduledAt,
    #activatedAt: _f$activatedAt,
    #completedAt: _f$completedAt,
    #assignedUser: _f$assignedUser,
    #assignedEmployeeUser: _f$assignedEmployeeUser,
    #address: _f$address,
    #facility: _f$facility,
    #description: _f$description,
    #rowVersion: _f$rowVersion,
  };
  @override
  final bool ignoreNull = true;

  static ActivityFacility _instantiate(DecodingData data) {
    return ActivityFacility(
        id: data.dec(_f$id),
        tenantId: data.dec(_f$tenantId),
        activityId: data.dec(_f$activityId),
        fieldPlanId: data.dec(_f$fieldPlanId),
        facilityId: data.dec(_f$facilityId),
        status: data.dec(_f$status),
        scheduledAt: data.dec(_f$scheduledAt),
        activatedAt: data.dec(_f$activatedAt),
        completedAt: data.dec(_f$completedAt),
        assignedUser: data.dec(_f$assignedUser),
        assignedEmployeeUser: data.dec(_f$assignedEmployeeUser),
        address: data.dec(_f$address),
        facility: data.dec(_f$facility),
        description: data.dec(_f$description),
        rowVersion: data.dec(_f$rowVersion));
  }

  @override
  final Function instantiate = _instantiate;

  static ActivityFacility fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<ActivityFacility>(map);
  }

  static ActivityFacility fromJson(String json) {
    return ensureInitialized().decodeJson<ActivityFacility>(json);
  }
}

mixin ActivityFacilityMappable {
  String toJson() {
    return ActivityFacilityMapper.ensureInitialized()
        .encodeJson<ActivityFacility>(this as ActivityFacility);
  }

  Map<String, dynamic> toMap() {
    return ActivityFacilityMapper.ensureInitialized()
        .encodeMap<ActivityFacility>(this as ActivityFacility);
  }

  ActivityFacilityCopyWith<ActivityFacility, ActivityFacility, ActivityFacility>
      get copyWith => _ActivityFacilityCopyWithImpl(
          this as ActivityFacility, $identity, $identity);
  @override
  String toString() {
    return ActivityFacilityMapper.ensureInitialized()
        .stringifyValue(this as ActivityFacility);
  }

  @override
  bool operator ==(Object other) {
    return ActivityFacilityMapper.ensureInitialized()
        .equalsValue(this as ActivityFacility, other);
  }

  @override
  int get hashCode {
    return ActivityFacilityMapper.ensureInitialized()
        .hashValue(this as ActivityFacility);
  }
}

extension ActivityFacilityValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ActivityFacility, $Out> {
  ActivityFacilityCopyWith<$R, ActivityFacility, $Out>
      get $asActivityFacility =>
          $base.as((v, t, t2) => _ActivityFacilityCopyWithImpl(v, t, t2));
}

abstract class ActivityFacilityCopyWith<$R, $In extends ActivityFacility, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  AddressModelCopyWith<$R, AddressModel, AddressModel>? get address;
  FacilityCopyWith<$R, Facility, Facility>? get facility;
  $R call(
      {String? id,
      String? tenantId,
      String? activityId,
      String? fieldPlanId,
      String? facilityId,
      String? status,
      DateTime? scheduledAt,
      DateTime? activatedAt,
      DateTime? completedAt,
      String? assignedUser,
      String? assignedEmployeeUser,
      AddressModel? address,
      Facility? facility,
      String? description,
      int? rowVersion});
  ActivityFacilityCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _ActivityFacilityCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ActivityFacility, $Out>
    implements ActivityFacilityCopyWith<$R, ActivityFacility, $Out> {
  _ActivityFacilityCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ActivityFacility> $mapper =
      ActivityFacilityMapper.ensureInitialized();
  @override
  AddressModelCopyWith<$R, AddressModel, AddressModel>? get address =>
      $value.address?.copyWith.$chain((v) => call(address: v));
  @override
  FacilityCopyWith<$R, Facility, Facility>? get facility =>
      $value.facility?.copyWith.$chain((v) => call(facility: v));
  @override
  $R call(
          {String? id,
          Object? tenantId = $none,
          Object? activityId = $none,
          Object? fieldPlanId = $none,
          Object? facilityId = $none,
          Object? status = $none,
          Object? scheduledAt = $none,
          Object? activatedAt = $none,
          Object? completedAt = $none,
          Object? assignedUser = $none,
          Object? assignedEmployeeUser = $none,
          Object? address = $none,
          Object? facility = $none,
          Object? description = $none,
          Object? rowVersion = $none}) =>
      $apply(FieldCopyWithData({
        if (id != null) #id: id,
        if (tenantId != $none) #tenantId: tenantId,
        if (activityId != $none) #activityId: activityId,
        if (fieldPlanId != $none) #fieldPlanId: fieldPlanId,
        if (facilityId != $none) #facilityId: facilityId,
        if (status != $none) #status: status,
        if (scheduledAt != $none) #scheduledAt: scheduledAt,
        if (activatedAt != $none) #activatedAt: activatedAt,
        if (completedAt != $none) #completedAt: completedAt,
        if (assignedUser != $none) #assignedUser: assignedUser,
        if (assignedEmployeeUser != $none)
          #assignedEmployeeUser: assignedEmployeeUser,
        if (address != $none) #address: address,
        if (facility != $none) #facility: facility,
        if (description != $none) #description: description,
        if (rowVersion != $none) #rowVersion: rowVersion
      }));
  @override
  ActivityFacility $make(CopyWithData data) => ActivityFacility(
      id: data.get(#id, or: $value.id),
      tenantId: data.get(#tenantId, or: $value.tenantId),
      activityId: data.get(#activityId, or: $value.activityId),
      fieldPlanId: data.get(#fieldPlanId, or: $value.fieldPlanId),
      facilityId: data.get(#facilityId, or: $value.facilityId),
      status: data.get(#status, or: $value.status),
      scheduledAt: data.get(#scheduledAt, or: $value.scheduledAt),
      activatedAt: data.get(#activatedAt, or: $value.activatedAt),
      completedAt: data.get(#completedAt, or: $value.completedAt),
      assignedUser: data.get(#assignedUser, or: $value.assignedUser),
      assignedEmployeeUser:
          data.get(#assignedEmployeeUser, or: $value.assignedEmployeeUser),
      address: data.get(#address, or: $value.address),
      facility: data.get(#facility, or: $value.facility),
      description: data.get(#description, or: $value.description),
      rowVersion: data.get(#rowVersion, or: $value.rowVersion));

  @override
  ActivityFacilityCopyWith<$R2, ActivityFacility, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _ActivityFacilityCopyWithImpl($value, $cast, t);
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
