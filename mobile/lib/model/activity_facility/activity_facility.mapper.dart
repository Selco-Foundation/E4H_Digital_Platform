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
  static FieldPlan? _$fieldPlan(ActivityFacility v) => v.fieldPlan;
  static const Field<ActivityFacility, FieldPlan> _f$fieldPlan =
      Field('fieldPlan', _$fieldPlan, opt: true);
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
  static AdditionalDetails? _$additionalDetails(ActivityFacility v) =>
      v.additionalDetails;
  static const Field<ActivityFacility, AdditionalDetails> _f$additionalDetails =
      Field('additionalDetails', _$additionalDetails, opt: true);

  @override
  final MappableFields<ActivityFacility> fields = const {
    #id: _f$id,
    #tenantId: _f$tenantId,
    #activityId: _f$activityId,
    #fieldPlanId: _f$fieldPlanId,
    #facilityId: _f$facilityId,
    #fieldPlan: _f$fieldPlan,
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
    #additionalDetails: _f$additionalDetails,
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
        fieldPlan: data.dec(_f$fieldPlan),
        status: data.dec(_f$status),
        scheduledAt: data.dec(_f$scheduledAt),
        activatedAt: data.dec(_f$activatedAt),
        completedAt: data.dec(_f$completedAt),
        assignedUser: data.dec(_f$assignedUser),
        assignedEmployeeUser: data.dec(_f$assignedEmployeeUser),
        address: data.dec(_f$address),
        facility: data.dec(_f$facility),
        description: data.dec(_f$description),
        rowVersion: data.dec(_f$rowVersion),
        additionalDetails: data.dec(_f$additionalDetails));
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
  FieldPlanCopyWith<$R, FieldPlan, FieldPlan>? get fieldPlan;
  AddressModelCopyWith<$R, AddressModel, AddressModel>? get address;
  FacilityCopyWith<$R, Facility, Facility>? get facility;
  AdditionalDetailsCopyWith<$R, AdditionalDetails, AdditionalDetails>?
      get additionalDetails;
  $R call(
      {String? id,
      String? tenantId,
      String? activityId,
      String? fieldPlanId,
      String? facilityId,
      FieldPlan? fieldPlan,
      String? status,
      DateTime? scheduledAt,
      DateTime? activatedAt,
      DateTime? completedAt,
      String? assignedUser,
      String? assignedEmployeeUser,
      AddressModel? address,
      Facility? facility,
      String? description,
      int? rowVersion,
      AdditionalDetails? additionalDetails});
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
  FieldPlanCopyWith<$R, FieldPlan, FieldPlan>? get fieldPlan =>
      $value.fieldPlan?.copyWith.$chain((v) => call(fieldPlan: v));
  @override
  AddressModelCopyWith<$R, AddressModel, AddressModel>? get address =>
      $value.address?.copyWith.$chain((v) => call(address: v));
  @override
  FacilityCopyWith<$R, Facility, Facility>? get facility =>
      $value.facility?.copyWith.$chain((v) => call(facility: v));
  @override
  AdditionalDetailsCopyWith<$R, AdditionalDetails, AdditionalDetails>?
      get additionalDetails => $value.additionalDetails?.copyWith
          .$chain((v) => call(additionalDetails: v));
  @override
  $R call(
          {String? id,
          Object? tenantId = $none,
          Object? activityId = $none,
          Object? fieldPlanId = $none,
          Object? facilityId = $none,
          Object? fieldPlan = $none,
          Object? status = $none,
          Object? scheduledAt = $none,
          Object? activatedAt = $none,
          Object? completedAt = $none,
          Object? assignedUser = $none,
          Object? assignedEmployeeUser = $none,
          Object? address = $none,
          Object? facility = $none,
          Object? description = $none,
          Object? rowVersion = $none,
          Object? additionalDetails = $none}) =>
      $apply(FieldCopyWithData({
        if (id != null) #id: id,
        if (tenantId != $none) #tenantId: tenantId,
        if (activityId != $none) #activityId: activityId,
        if (fieldPlanId != $none) #fieldPlanId: fieldPlanId,
        if (facilityId != $none) #facilityId: facilityId,
        if (fieldPlan != $none) #fieldPlan: fieldPlan,
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
        if (rowVersion != $none) #rowVersion: rowVersion,
        if (additionalDetails != $none) #additionalDetails: additionalDetails
      }));
  @override
  ActivityFacility $make(CopyWithData data) => ActivityFacility(
      id: data.get(#id, or: $value.id),
      tenantId: data.get(#tenantId, or: $value.tenantId),
      activityId: data.get(#activityId, or: $value.activityId),
      fieldPlanId: data.get(#fieldPlanId, or: $value.fieldPlanId),
      facilityId: data.get(#facilityId, or: $value.facilityId),
      fieldPlan: data.get(#fieldPlan, or: $value.fieldPlan),
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
      rowVersion: data.get(#rowVersion, or: $value.rowVersion),
      additionalDetails:
          data.get(#additionalDetails, or: $value.additionalDetails));

  @override
  ActivityFacilityCopyWith<$R2, ActivityFacility, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _ActivityFacilityCopyWithImpl($value, $cast, t);
}

class FieldPlanMapper extends ClassMapperBase<FieldPlan> {
  FieldPlanMapper._();

  static FieldPlanMapper? _instance;
  static FieldPlanMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = FieldPlanMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'FieldPlan';

  static String? _$id(FieldPlan v) => v.id;
  static const Field<FieldPlan, String> _f$id =
      Field('id', _$id, mode: FieldMode.member);
  static String? _$tenantId(FieldPlan v) => v.tenantId;
  static const Field<FieldPlan, String> _f$tenantId =
      Field('tenantId', _$tenantId, mode: FieldMode.member);
  static String? _$name(FieldPlan v) => v.name;
  static const Field<FieldPlan, String> _f$name =
      Field('name', _$name, mode: FieldMode.member);
  static String? _$status(FieldPlan v) => v.status;
  static const Field<FieldPlan, String> _f$status =
      Field('status', _$status, mode: FieldMode.member);
  static int? _$healthFacilityNumber(FieldPlan v) => v.healthFacilityNumber;
  static const Field<FieldPlan, int> _f$healthFacilityNumber = Field(
      'healthFacilityNumber', _$healthFacilityNumber,
      mode: FieldMode.member);
  static DateTime? _$startDateTime(FieldPlan v) => v.startDateTime;
  static const Field<FieldPlan, DateTime> _f$startDateTime =
      Field('startDateTime', _$startDateTime, mode: FieldMode.member);
  static DateTime? _$endDateTime(FieldPlan v) => v.endDateTime;
  static const Field<FieldPlan, DateTime> _f$endDateTime =
      Field('endDateTime', _$endDateTime, mode: FieldMode.member);
  static Project? _$project(FieldPlan v) => v.project;
  static const Field<FieldPlan, Project> _f$project =
      Field('project', _$project, mode: FieldMode.member);

  @override
  final MappableFields<FieldPlan> fields = const {
    #id: _f$id,
    #tenantId: _f$tenantId,
    #name: _f$name,
    #status: _f$status,
    #healthFacilityNumber: _f$healthFacilityNumber,
    #startDateTime: _f$startDateTime,
    #endDateTime: _f$endDateTime,
    #project: _f$project,
  };
  @override
  final bool ignoreNull = true;

  static FieldPlan _instantiate(DecodingData data) {
    return FieldPlan();
  }

  @override
  final Function instantiate = _instantiate;

  static FieldPlan fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<FieldPlan>(map);
  }

  static FieldPlan fromJson(String json) {
    return ensureInitialized().decodeJson<FieldPlan>(json);
  }
}

mixin FieldPlanMappable {
  String toJson() {
    return FieldPlanMapper.ensureInitialized()
        .encodeJson<FieldPlan>(this as FieldPlan);
  }

  Map<String, dynamic> toMap() {
    return FieldPlanMapper.ensureInitialized()
        .encodeMap<FieldPlan>(this as FieldPlan);
  }

  FieldPlanCopyWith<FieldPlan, FieldPlan, FieldPlan> get copyWith =>
      _FieldPlanCopyWithImpl(this as FieldPlan, $identity, $identity);
  @override
  String toString() {
    return FieldPlanMapper.ensureInitialized()
        .stringifyValue(this as FieldPlan);
  }

  @override
  bool operator ==(Object other) {
    return FieldPlanMapper.ensureInitialized()
        .equalsValue(this as FieldPlan, other);
  }

  @override
  int get hashCode {
    return FieldPlanMapper.ensureInitialized().hashValue(this as FieldPlan);
  }
}

extension FieldPlanValueCopy<$R, $Out> on ObjectCopyWith<$R, FieldPlan, $Out> {
  FieldPlanCopyWith<$R, FieldPlan, $Out> get $asFieldPlan =>
      $base.as((v, t, t2) => _FieldPlanCopyWithImpl(v, t, t2));
}

abstract class FieldPlanCopyWith<$R, $In extends FieldPlan, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  FieldPlanCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _FieldPlanCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, FieldPlan, $Out>
    implements FieldPlanCopyWith<$R, FieldPlan, $Out> {
  _FieldPlanCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<FieldPlan> $mapper =
      FieldPlanMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  FieldPlan $make(CopyWithData data) => FieldPlan();

  @override
  FieldPlanCopyWith<$R2, FieldPlan, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _FieldPlanCopyWithImpl($value, $cast, t);
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
  static AssetTypeAdditionalDetails? _$battery(AdditionalDetails v) =>
      v.battery;
  static const Field<AdditionalDetails, AssetTypeAdditionalDetails> _f$battery =
      Field('battery', _$battery, mode: FieldMode.member);
  static AssetTypeAdditionalDetails? _$inverter(AdditionalDetails v) =>
      v.inverter;
  static const Field<AdditionalDetails, AssetTypeAdditionalDetails>
      _f$inverter = Field('inverter', _$inverter, mode: FieldMode.member);
  static AssetTypeAdditionalDetails? _$panel(AdditionalDetails v) => v.panel;
  static const Field<AdditionalDetails, AssetTypeAdditionalDetails> _f$panel =
      Field('panel', _$panel, mode: FieldMode.member);

  @override
  final MappableFields<AdditionalDetails> fields = const {
    #status: _f$status,
    #facility: _f$facility,
    #systemCode: _f$systemCode,
    #battery: _f$battery,
    #inverter: _f$inverter,
    #panel: _f$panel,
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

class ProjectMapper extends ClassMapperBase<Project> {
  ProjectMapper._();

  static ProjectMapper? _instance;
  static ProjectMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = ProjectMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'Project';

  static String? _$id(Project v) => v.id;
  static const Field<Project, String> _f$id =
      Field('id', _$id, mode: FieldMode.member);
  static String? _$tenantId(Project v) => v.tenantId;
  static const Field<Project, String> _f$tenantId =
      Field('tenantId', _$tenantId, mode: FieldMode.member);
  static String? _$projectNumber(Project v) => v.projectNumber;
  static const Field<Project, String> _f$projectNumber =
      Field('projectNumber', _$projectNumber, mode: FieldMode.member);
  static String? _$name(Project v) => v.name;
  static const Field<Project, String> _f$name =
      Field('name', _$name, mode: FieldMode.member);
  static String? _$projectType(Project v) => v.projectType;
  static const Field<Project, String> _f$projectType =
      Field('projectType', _$projectType, mode: FieldMode.member);
  static String? _$projectSubType(Project v) => v.projectSubType;
  static const Field<Project, String> _f$projectSubType =
      Field('projectSubType', _$projectSubType, mode: FieldMode.member);
  static String? _$referenceID(Project v) => v.referenceID;
  static const Field<Project, String> _f$referenceID =
      Field('referenceID', _$referenceID, mode: FieldMode.member);
  static DateTime? _$startDateTime(Project v) => v.startDateTime;
  static const Field<Project, DateTime> _f$startDateTime =
      Field('startDateTime', _$startDateTime, mode: FieldMode.member);
  static DateTime? _$endDateTime(Project v) => v.endDateTime;
  static const Field<Project, DateTime> _f$endDateTime =
      Field('endDateTime', _$endDateTime, mode: FieldMode.member);
  static ProjectAdditionalDetails? _$additionalDetails(Project v) =>
      v.additionalDetails;
  static const Field<Project, ProjectAdditionalDetails> _f$additionalDetails =
      Field('additionalDetails', _$additionalDetails, mode: FieldMode.member);

  @override
  final MappableFields<Project> fields = const {
    #id: _f$id,
    #tenantId: _f$tenantId,
    #projectNumber: _f$projectNumber,
    #name: _f$name,
    #projectType: _f$projectType,
    #projectSubType: _f$projectSubType,
    #referenceID: _f$referenceID,
    #startDateTime: _f$startDateTime,
    #endDateTime: _f$endDateTime,
    #additionalDetails: _f$additionalDetails,
  };
  @override
  final bool ignoreNull = true;

  static Project _instantiate(DecodingData data) {
    return Project();
  }

  @override
  final Function instantiate = _instantiate;

  static Project fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<Project>(map);
  }

  static Project fromJson(String json) {
    return ensureInitialized().decodeJson<Project>(json);
  }
}

mixin ProjectMappable {
  String toJson() {
    return ProjectMapper.ensureInitialized()
        .encodeJson<Project>(this as Project);
  }

  Map<String, dynamic> toMap() {
    return ProjectMapper.ensureInitialized()
        .encodeMap<Project>(this as Project);
  }

  ProjectCopyWith<Project, Project, Project> get copyWith =>
      _ProjectCopyWithImpl(this as Project, $identity, $identity);
  @override
  String toString() {
    return ProjectMapper.ensureInitialized().stringifyValue(this as Project);
  }

  @override
  bool operator ==(Object other) {
    return ProjectMapper.ensureInitialized()
        .equalsValue(this as Project, other);
  }

  @override
  int get hashCode {
    return ProjectMapper.ensureInitialized().hashValue(this as Project);
  }
}

extension ProjectValueCopy<$R, $Out> on ObjectCopyWith<$R, Project, $Out> {
  ProjectCopyWith<$R, Project, $Out> get $asProject =>
      $base.as((v, t, t2) => _ProjectCopyWithImpl(v, t, t2));
}

abstract class ProjectCopyWith<$R, $In extends Project, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  ProjectCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _ProjectCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, Project, $Out>
    implements ProjectCopyWith<$R, Project, $Out> {
  _ProjectCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<Project> $mapper =
      ProjectMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  Project $make(CopyWithData data) => Project();

  @override
  ProjectCopyWith<$R2, Project, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t) =>
      _ProjectCopyWithImpl($value, $cast, t);
}

class ProjectAdditionalDetailsMapper
    extends ClassMapperBase<ProjectAdditionalDetails> {
  ProjectAdditionalDetailsMapper._();

  static ProjectAdditionalDetailsMapper? _instance;
  static ProjectAdditionalDetailsMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals
          .use(_instance = ProjectAdditionalDetailsMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'ProjectAdditionalDetails';

  static String? _$status(ProjectAdditionalDetails v) => v.status;
  static const Field<ProjectAdditionalDetails, String> _f$status =
      Field('status', _$status, mode: FieldMode.member);
  static Facility? _$facility(ProjectAdditionalDetails v) => v.facility;
  static const Field<ProjectAdditionalDetails, Facility> _f$facility =
      Field('facility', _$facility, mode: FieldMode.member);
  static GeographyDetails? _$geographyDetails(ProjectAdditionalDetails v) =>
      v.geographyDetails;
  static const Field<ProjectAdditionalDetails, GeographyDetails>
      _f$geographyDetails =
      Field('geographyDetails', _$geographyDetails, mode: FieldMode.member);

  @override
  final MappableFields<ProjectAdditionalDetails> fields = const {
    #status: _f$status,
    #facility: _f$facility,
    #geographyDetails: _f$geographyDetails,
  };
  @override
  final bool ignoreNull = true;

  static ProjectAdditionalDetails _instantiate(DecodingData data) {
    return ProjectAdditionalDetails();
  }

  @override
  final Function instantiate = _instantiate;

  static ProjectAdditionalDetails fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<ProjectAdditionalDetails>(map);
  }

  static ProjectAdditionalDetails fromJson(String json) {
    return ensureInitialized().decodeJson<ProjectAdditionalDetails>(json);
  }
}

mixin ProjectAdditionalDetailsMappable {
  String toJson() {
    return ProjectAdditionalDetailsMapper.ensureInitialized()
        .encodeJson<ProjectAdditionalDetails>(this as ProjectAdditionalDetails);
  }

  Map<String, dynamic> toMap() {
    return ProjectAdditionalDetailsMapper.ensureInitialized()
        .encodeMap<ProjectAdditionalDetails>(this as ProjectAdditionalDetails);
  }

  ProjectAdditionalDetailsCopyWith<ProjectAdditionalDetails,
          ProjectAdditionalDetails, ProjectAdditionalDetails>
      get copyWith => _ProjectAdditionalDetailsCopyWithImpl(
          this as ProjectAdditionalDetails, $identity, $identity);
  @override
  String toString() {
    return ProjectAdditionalDetailsMapper.ensureInitialized()
        .stringifyValue(this as ProjectAdditionalDetails);
  }

  @override
  bool operator ==(Object other) {
    return ProjectAdditionalDetailsMapper.ensureInitialized()
        .equalsValue(this as ProjectAdditionalDetails, other);
  }

  @override
  int get hashCode {
    return ProjectAdditionalDetailsMapper.ensureInitialized()
        .hashValue(this as ProjectAdditionalDetails);
  }
}

extension ProjectAdditionalDetailsValueCopy<$R, $Out>
    on ObjectCopyWith<$R, ProjectAdditionalDetails, $Out> {
  ProjectAdditionalDetailsCopyWith<$R, ProjectAdditionalDetails, $Out>
      get $asProjectAdditionalDetails => $base
          .as((v, t, t2) => _ProjectAdditionalDetailsCopyWithImpl(v, t, t2));
}

abstract class ProjectAdditionalDetailsCopyWith<
    $R,
    $In extends ProjectAdditionalDetails,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  ProjectAdditionalDetailsCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _ProjectAdditionalDetailsCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, ProjectAdditionalDetails, $Out>
    implements
        ProjectAdditionalDetailsCopyWith<$R, ProjectAdditionalDetails, $Out> {
  _ProjectAdditionalDetailsCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<ProjectAdditionalDetails> $mapper =
      ProjectAdditionalDetailsMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  ProjectAdditionalDetails $make(CopyWithData data) =>
      ProjectAdditionalDetails();

  @override
  ProjectAdditionalDetailsCopyWith<$R2, ProjectAdditionalDetails, $Out2>
      $chain<$R2, $Out2>(Then<$Out2, $R2> t) =>
          _ProjectAdditionalDetailsCopyWithImpl($value, $cast, t);
}

class GeographyDetailsMapper extends ClassMapperBase<GeographyDetails> {
  GeographyDetailsMapper._();

  static GeographyDetailsMapper? _instance;
  static GeographyDetailsMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = GeographyDetailsMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'GeographyDetails';

  static StateRef? _$state(GeographyDetails v) => v.state;
  static const Field<GeographyDetails, StateRef> _f$state =
      Field('state', _$state, mode: FieldMode.member);
  static List<BlockRef>? _$blocks(GeographyDetails v) => v.blocks;
  static const Field<GeographyDetails, List<BlockRef>> _f$blocks =
      Field('blocks', _$blocks, mode: FieldMode.member);

  @override
  final MappableFields<GeographyDetails> fields = const {
    #state: _f$state,
    #blocks: _f$blocks,
  };
  @override
  final bool ignoreNull = true;

  static GeographyDetails _instantiate(DecodingData data) {
    return GeographyDetails();
  }

  @override
  final Function instantiate = _instantiate;

  static GeographyDetails fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<GeographyDetails>(map);
  }

  static GeographyDetails fromJson(String json) {
    return ensureInitialized().decodeJson<GeographyDetails>(json);
  }
}

mixin GeographyDetailsMappable {
  String toJson() {
    return GeographyDetailsMapper.ensureInitialized()
        .encodeJson<GeographyDetails>(this as GeographyDetails);
  }

  Map<String, dynamic> toMap() {
    return GeographyDetailsMapper.ensureInitialized()
        .encodeMap<GeographyDetails>(this as GeographyDetails);
  }

  GeographyDetailsCopyWith<GeographyDetails, GeographyDetails, GeographyDetails>
      get copyWith => _GeographyDetailsCopyWithImpl(
          this as GeographyDetails, $identity, $identity);
  @override
  String toString() {
    return GeographyDetailsMapper.ensureInitialized()
        .stringifyValue(this as GeographyDetails);
  }

  @override
  bool operator ==(Object other) {
    return GeographyDetailsMapper.ensureInitialized()
        .equalsValue(this as GeographyDetails, other);
  }

  @override
  int get hashCode {
    return GeographyDetailsMapper.ensureInitialized()
        .hashValue(this as GeographyDetails);
  }
}

extension GeographyDetailsValueCopy<$R, $Out>
    on ObjectCopyWith<$R, GeographyDetails, $Out> {
  GeographyDetailsCopyWith<$R, GeographyDetails, $Out>
      get $asGeographyDetails =>
          $base.as((v, t, t2) => _GeographyDetailsCopyWithImpl(v, t, t2));
}

abstract class GeographyDetailsCopyWith<$R, $In extends GeographyDetails, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  GeographyDetailsCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _GeographyDetailsCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, GeographyDetails, $Out>
    implements GeographyDetailsCopyWith<$R, GeographyDetails, $Out> {
  _GeographyDetailsCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<GeographyDetails> $mapper =
      GeographyDetailsMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  GeographyDetails $make(CopyWithData data) => GeographyDetails();

  @override
  GeographyDetailsCopyWith<$R2, GeographyDetails, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _GeographyDetailsCopyWithImpl($value, $cast, t);
}

class StateRefMapper extends ClassMapperBase<StateRef> {
  StateRefMapper._();

  static StateRefMapper? _instance;
  static StateRefMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = StateRefMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'StateRef';

  static String? _$code(StateRef v) => v.code;
  static const Field<StateRef, String> _f$code =
      Field('code', _$code, mode: FieldMode.member);
  static String? _$name(StateRef v) => v.name;
  static const Field<StateRef, String> _f$name =
      Field('name', _$name, mode: FieldMode.member);

  @override
  final MappableFields<StateRef> fields = const {
    #code: _f$code,
    #name: _f$name,
  };
  @override
  final bool ignoreNull = true;

  static StateRef _instantiate(DecodingData data) {
    return StateRef();
  }

  @override
  final Function instantiate = _instantiate;

  static StateRef fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<StateRef>(map);
  }

  static StateRef fromJson(String json) {
    return ensureInitialized().decodeJson<StateRef>(json);
  }
}

mixin StateRefMappable {
  String toJson() {
    return StateRefMapper.ensureInitialized()
        .encodeJson<StateRef>(this as StateRef);
  }

  Map<String, dynamic> toMap() {
    return StateRefMapper.ensureInitialized()
        .encodeMap<StateRef>(this as StateRef);
  }

  StateRefCopyWith<StateRef, StateRef, StateRef> get copyWith =>
      _StateRefCopyWithImpl(this as StateRef, $identity, $identity);
  @override
  String toString() {
    return StateRefMapper.ensureInitialized().stringifyValue(this as StateRef);
  }

  @override
  bool operator ==(Object other) {
    return StateRefMapper.ensureInitialized()
        .equalsValue(this as StateRef, other);
  }

  @override
  int get hashCode {
    return StateRefMapper.ensureInitialized().hashValue(this as StateRef);
  }
}

extension StateRefValueCopy<$R, $Out> on ObjectCopyWith<$R, StateRef, $Out> {
  StateRefCopyWith<$R, StateRef, $Out> get $asStateRef =>
      $base.as((v, t, t2) => _StateRefCopyWithImpl(v, t, t2));
}

abstract class StateRefCopyWith<$R, $In extends StateRef, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  StateRefCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _StateRefCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, StateRef, $Out>
    implements StateRefCopyWith<$R, StateRef, $Out> {
  _StateRefCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<StateRef> $mapper =
      StateRefMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  StateRef $make(CopyWithData data) => StateRef();

  @override
  StateRefCopyWith<$R2, StateRef, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _StateRefCopyWithImpl($value, $cast, t);
}

class BlockRefMapper extends ClassMapperBase<BlockRef> {
  BlockRefMapper._();

  static BlockRefMapper? _instance;
  static BlockRefMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals.use(_instance = BlockRefMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'BlockRef';

  static String? _$code(BlockRef v) => v.code;
  static const Field<BlockRef, String> _f$code =
      Field('code', _$code, mode: FieldMode.member);
  static String? _$name(BlockRef v) => v.name;
  static const Field<BlockRef, String> _f$name =
      Field('name', _$name, mode: FieldMode.member);
  static String? _$stateCode(BlockRef v) => v.stateCode;
  static const Field<BlockRef, String> _f$stateCode =
      Field('stateCode', _$stateCode, mode: FieldMode.member);
  static String? _$districtCode(BlockRef v) => v.districtCode;
  static const Field<BlockRef, String> _f$districtCode =
      Field('districtCode', _$districtCode, mode: FieldMode.member);

  @override
  final MappableFields<BlockRef> fields = const {
    #code: _f$code,
    #name: _f$name,
    #stateCode: _f$stateCode,
    #districtCode: _f$districtCode,
  };
  @override
  final bool ignoreNull = true;

  static BlockRef _instantiate(DecodingData data) {
    return BlockRef();
  }

  @override
  final Function instantiate = _instantiate;

  static BlockRef fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<BlockRef>(map);
  }

  static BlockRef fromJson(String json) {
    return ensureInitialized().decodeJson<BlockRef>(json);
  }
}

mixin BlockRefMappable {
  String toJson() {
    return BlockRefMapper.ensureInitialized()
        .encodeJson<BlockRef>(this as BlockRef);
  }

  Map<String, dynamic> toMap() {
    return BlockRefMapper.ensureInitialized()
        .encodeMap<BlockRef>(this as BlockRef);
  }

  BlockRefCopyWith<BlockRef, BlockRef, BlockRef> get copyWith =>
      _BlockRefCopyWithImpl(this as BlockRef, $identity, $identity);
  @override
  String toString() {
    return BlockRefMapper.ensureInitialized().stringifyValue(this as BlockRef);
  }

  @override
  bool operator ==(Object other) {
    return BlockRefMapper.ensureInitialized()
        .equalsValue(this as BlockRef, other);
  }

  @override
  int get hashCode {
    return BlockRefMapper.ensureInitialized().hashValue(this as BlockRef);
  }
}

extension BlockRefValueCopy<$R, $Out> on ObjectCopyWith<$R, BlockRef, $Out> {
  BlockRefCopyWith<$R, BlockRef, $Out> get $asBlockRef =>
      $base.as((v, t, t2) => _BlockRefCopyWithImpl(v, t, t2));
}

abstract class BlockRefCopyWith<$R, $In extends BlockRef, $Out>
    implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  BlockRefCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(Then<$Out2, $R2> t);
}

class _BlockRefCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, BlockRef, $Out>
    implements BlockRefCopyWith<$R, BlockRef, $Out> {
  _BlockRefCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<BlockRef> $mapper =
      BlockRefMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  BlockRef $make(CopyWithData data) => BlockRef();

  @override
  BlockRefCopyWith<$R2, BlockRef, $Out2> $chain<$R2, $Out2>(
          Then<$Out2, $R2> t) =>
      _BlockRefCopyWithImpl($value, $cast, t);
}

class AssetTypeAdditionalDetailsMapper
    extends ClassMapperBase<AssetTypeAdditionalDetails> {
  AssetTypeAdditionalDetailsMapper._();

  static AssetTypeAdditionalDetailsMapper? _instance;
  static AssetTypeAdditionalDetailsMapper ensureInitialized() {
    if (_instance == null) {
      MapperContainer.globals
          .use(_instance = AssetTypeAdditionalDetailsMapper._());
    }
    return _instance!;
  }

  @override
  final String id = 'AssetTypeAdditionalDetails';

  static String? _$brandName(AssetTypeAdditionalDetails v) => v.brandName;
  static const Field<AssetTypeAdditionalDetails, String> _f$brandName =
      Field('brandName', _$brandName, mode: FieldMode.member);
  static String? _$brandCode(AssetTypeAdditionalDetails v) => v.brandCode;
  static const Field<AssetTypeAdditionalDetails, String> _f$brandCode =
      Field('brandCode', _$brandCode, mode: FieldMode.member);
  static String? _$capacity(AssetTypeAdditionalDetails v) => v.capacity;
  static const Field<AssetTypeAdditionalDetails, String> _f$capacity =
      Field('capacity', _$capacity, mode: FieldMode.member);

  @override
  final MappableFields<AssetTypeAdditionalDetails> fields = const {
    #brandName: _f$brandName,
    #brandCode: _f$brandCode,
    #capacity: _f$capacity,
  };
  @override
  final bool ignoreNull = true;

  static AssetTypeAdditionalDetails _instantiate(DecodingData data) {
    return AssetTypeAdditionalDetails();
  }

  @override
  final Function instantiate = _instantiate;

  static AssetTypeAdditionalDetails fromMap(Map<String, dynamic> map) {
    return ensureInitialized().decodeMap<AssetTypeAdditionalDetails>(map);
  }

  static AssetTypeAdditionalDetails fromJson(String json) {
    return ensureInitialized().decodeJson<AssetTypeAdditionalDetails>(json);
  }
}

mixin AssetTypeAdditionalDetailsMappable {
  String toJson() {
    return AssetTypeAdditionalDetailsMapper.ensureInitialized()
        .encodeJson<AssetTypeAdditionalDetails>(
            this as AssetTypeAdditionalDetails);
  }

  Map<String, dynamic> toMap() {
    return AssetTypeAdditionalDetailsMapper.ensureInitialized()
        .encodeMap<AssetTypeAdditionalDetails>(
            this as AssetTypeAdditionalDetails);
  }

  AssetTypeAdditionalDetailsCopyWith<AssetTypeAdditionalDetails,
          AssetTypeAdditionalDetails, AssetTypeAdditionalDetails>
      get copyWith => _AssetTypeAdditionalDetailsCopyWithImpl(
          this as AssetTypeAdditionalDetails, $identity, $identity);
  @override
  String toString() {
    return AssetTypeAdditionalDetailsMapper.ensureInitialized()
        .stringifyValue(this as AssetTypeAdditionalDetails);
  }

  @override
  bool operator ==(Object other) {
    return AssetTypeAdditionalDetailsMapper.ensureInitialized()
        .equalsValue(this as AssetTypeAdditionalDetails, other);
  }

  @override
  int get hashCode {
    return AssetTypeAdditionalDetailsMapper.ensureInitialized()
        .hashValue(this as AssetTypeAdditionalDetails);
  }
}

extension AssetTypeAdditionalDetailsValueCopy<$R, $Out>
    on ObjectCopyWith<$R, AssetTypeAdditionalDetails, $Out> {
  AssetTypeAdditionalDetailsCopyWith<$R, AssetTypeAdditionalDetails, $Out>
      get $asAssetTypeAdditionalDetails => $base
          .as((v, t, t2) => _AssetTypeAdditionalDetailsCopyWithImpl(v, t, t2));
}

abstract class AssetTypeAdditionalDetailsCopyWith<
    $R,
    $In extends AssetTypeAdditionalDetails,
    $Out> implements ClassCopyWith<$R, $In, $Out> {
  $R call();
  AssetTypeAdditionalDetailsCopyWith<$R2, $In, $Out2> $chain<$R2, $Out2>(
      Then<$Out2, $R2> t);
}

class _AssetTypeAdditionalDetailsCopyWithImpl<$R, $Out>
    extends ClassCopyWithBase<$R, AssetTypeAdditionalDetails, $Out>
    implements
        AssetTypeAdditionalDetailsCopyWith<$R, AssetTypeAdditionalDetails,
            $Out> {
  _AssetTypeAdditionalDetailsCopyWithImpl(super.value, super.then, super.then2);

  @override
  late final ClassMapperBase<AssetTypeAdditionalDetails> $mapper =
      AssetTypeAdditionalDetailsMapper.ensureInitialized();
  @override
  $R call() => $apply(FieldCopyWithData({}));
  @override
  AssetTypeAdditionalDetails $make(CopyWithData data) =>
      AssetTypeAdditionalDetails();

  @override
  AssetTypeAdditionalDetailsCopyWith<$R2, AssetTypeAdditionalDetails, $Out2>
      $chain<$R2, $Out2>(Then<$Out2, $R2> t) =>
          _AssetTypeAdditionalDetailsCopyWithImpl($value, $cast, t);
}
