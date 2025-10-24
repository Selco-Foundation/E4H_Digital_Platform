// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, unnecessary_cast, override_on_non_overriding_member
// ignore_for_file: strict_raw_type, inference_failure_on_untyped_parameter

part of 'activity_facility.dart';

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
  static int? _$scheduledAt(ActivityFacility v) => v.scheduledAt;
  static const Field<ActivityFacility, int> _f$scheduledAt =
      Field('scheduledAt', _$scheduledAt, opt: true);
  static int? _$activatedAt(ActivityFacility v) => v.activatedAt;
  static const Field<ActivityFacility, int> _f$activatedAt =
      Field('activatedAt', _$activatedAt, opt: true);
  static int? _$completedAt(ActivityFacility v) => v.completedAt;
  static const Field<ActivityFacility, int> _f$completedAt =
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
      int? scheduledAt,
      int? activatedAt,
      int? completedAt,
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
