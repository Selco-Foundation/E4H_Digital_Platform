// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'activity_facility.dart';

// **************************************************************************
// IsarEmbeddedGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const ActivityFacilitySchema = Schema(
  name: r'ActivityFacility',
  id: -8816070600442527583,
  properties: {
    r'activatedAt': PropertySchema(
      id: 0,
      name: r'activatedAt',
      type: IsarType.dateTime,
    ),
    r'activityId': PropertySchema(
      id: 1,
      name: r'activityId',
      type: IsarType.string,
    ),
    r'additionalDetails': PropertySchema(
      id: 2,
      name: r'additionalDetails',
      type: IsarType.object,
      target: r'AdditionalDetails',
    ),
    r'address': PropertySchema(
      id: 3,
      name: r'address',
      type: IsarType.object,
      target: r'AddressModel',
    ),
    r'assignedEmployeeUser': PropertySchema(
      id: 4,
      name: r'assignedEmployeeUser',
      type: IsarType.string,
    ),
    r'assignedUser': PropertySchema(
      id: 5,
      name: r'assignedUser',
      type: IsarType.string,
    ),
    r'completedAt': PropertySchema(
      id: 6,
      name: r'completedAt',
      type: IsarType.dateTime,
    ),
    r'description': PropertySchema(
      id: 7,
      name: r'description',
      type: IsarType.string,
    ),
    r'facility': PropertySchema(
      id: 8,
      name: r'facility',
      type: IsarType.object,
      target: r'Facility',
    ),
    r'facilityId': PropertySchema(
      id: 9,
      name: r'facilityId',
      type: IsarType.string,
    ),
    r'fieldPlan': PropertySchema(
      id: 10,
      name: r'fieldPlan',
      type: IsarType.object,
      target: r'FieldPlan',
    ),
    r'fieldPlanId': PropertySchema(
      id: 11,
      name: r'fieldPlanId',
      type: IsarType.string,
    ),
    r'id': PropertySchema(
      id: 12,
      name: r'id',
      type: IsarType.string,
    ),
    r'rowVersion': PropertySchema(
      id: 13,
      name: r'rowVersion',
      type: IsarType.long,
    ),
    r'scheduledAt': PropertySchema(
      id: 14,
      name: r'scheduledAt',
      type: IsarType.dateTime,
    ),
    r'status': PropertySchema(
      id: 15,
      name: r'status',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 16,
      name: r'tenantId',
      type: IsarType.string,
    )
  },
  estimateSize: _activityFacilityEstimateSize,
  serialize: _activityFacilitySerialize,
  deserialize: _activityFacilityDeserialize,
  deserializeProp: _activityFacilityDeserializeProp,
);

int _activityFacilityEstimateSize(
  ActivityFacility object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.activityId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.additionalDetails;
    if (value != null) {
      bytesCount += 3 +
          AdditionalDetailsSchema.estimateSize(
              value, allOffsets[AdditionalDetails]!, allOffsets);
    }
  }
  {
    final value = object.address;
    if (value != null) {
      bytesCount += 3 +
          AddressModelSchema.estimateSize(
              value, allOffsets[AddressModel]!, allOffsets);
    }
  }
  {
    final value = object.assignedEmployeeUser;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.assignedUser;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.description;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facility;
    if (value != null) {
      bytesCount += 3 +
          FacilitySchema.estimateSize(value, allOffsets[Facility]!, allOffsets);
    }
  }
  {
    final value = object.facilityId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.fieldPlan;
    if (value != null) {
      bytesCount += 3 +
          FieldPlanSchema.estimateSize(
              value, allOffsets[FieldPlan]!, allOffsets);
    }
  }
  {
    final value = object.fieldPlanId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.id.length * 3;
  {
    final value = object.status;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.tenantId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _activityFacilitySerialize(
  ActivityFacility object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeDateTime(offsets[0], object.activatedAt);
  writer.writeString(offsets[1], object.activityId);
  writer.writeObject<AdditionalDetails>(
    offsets[2],
    allOffsets,
    AdditionalDetailsSchema.serialize,
    object.additionalDetails,
  );
  writer.writeObject<AddressModel>(
    offsets[3],
    allOffsets,
    AddressModelSchema.serialize,
    object.address,
  );
  writer.writeString(offsets[4], object.assignedEmployeeUser);
  writer.writeString(offsets[5], object.assignedUser);
  writer.writeDateTime(offsets[6], object.completedAt);
  writer.writeString(offsets[7], object.description);
  writer.writeObject<Facility>(
    offsets[8],
    allOffsets,
    FacilitySchema.serialize,
    object.facility,
  );
  writer.writeString(offsets[9], object.facilityId);
  writer.writeObject<FieldPlan>(
    offsets[10],
    allOffsets,
    FieldPlanSchema.serialize,
    object.fieldPlan,
  );
  writer.writeString(offsets[11], object.fieldPlanId);
  writer.writeString(offsets[12], object.id);
  writer.writeLong(offsets[13], object.rowVersion);
  writer.writeDateTime(offsets[14], object.scheduledAt);
  writer.writeString(offsets[15], object.status);
  writer.writeString(offsets[16], object.tenantId);
}

ActivityFacility _activityFacilityDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = ActivityFacility(
    activatedAt: reader.readDateTimeOrNull(offsets[0]),
    activityId: reader.readStringOrNull(offsets[1]),
    additionalDetails: reader.readObjectOrNull<AdditionalDetails>(
      offsets[2],
      AdditionalDetailsSchema.deserialize,
      allOffsets,
    ),
    address: reader.readObjectOrNull<AddressModel>(
      offsets[3],
      AddressModelSchema.deserialize,
      allOffsets,
    ),
    assignedEmployeeUser: reader.readStringOrNull(offsets[4]),
    assignedUser: reader.readStringOrNull(offsets[5]),
    completedAt: reader.readDateTimeOrNull(offsets[6]),
    description: reader.readStringOrNull(offsets[7]),
    facility: reader.readObjectOrNull<Facility>(
      offsets[8],
      FacilitySchema.deserialize,
      allOffsets,
    ),
    facilityId: reader.readStringOrNull(offsets[9]),
    fieldPlan: reader.readObjectOrNull<FieldPlan>(
      offsets[10],
      FieldPlanSchema.deserialize,
      allOffsets,
    ),
    fieldPlanId: reader.readStringOrNull(offsets[11]),
    id: reader.readStringOrNull(offsets[12]) ?? '',
    rowVersion: reader.readLongOrNull(offsets[13]),
    scheduledAt: reader.readDateTimeOrNull(offsets[14]),
    status: reader.readStringOrNull(offsets[15]),
    tenantId: reader.readStringOrNull(offsets[16]),
  );
  return object;
}

P _activityFacilityDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readObjectOrNull<AdditionalDetails>(
        offset,
        AdditionalDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 3:
      return (reader.readObjectOrNull<AddressModel>(
        offset,
        AddressModelSchema.deserialize,
        allOffsets,
      )) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    case 6:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 7:
      return (reader.readStringOrNull(offset)) as P;
    case 8:
      return (reader.readObjectOrNull<Facility>(
        offset,
        FacilitySchema.deserialize,
        allOffsets,
      )) as P;
    case 9:
      return (reader.readStringOrNull(offset)) as P;
    case 10:
      return (reader.readObjectOrNull<FieldPlan>(
        offset,
        FieldPlanSchema.deserialize,
        allOffsets,
      )) as P;
    case 11:
      return (reader.readStringOrNull(offset)) as P;
    case 12:
      return (reader.readStringOrNull(offset) ?? '') as P;
    case 13:
      return (reader.readLongOrNull(offset)) as P;
    case 14:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 15:
      return (reader.readStringOrNull(offset)) as P;
    case 16:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension ActivityFacilityQueryFilter
    on QueryBuilder<ActivityFacility, ActivityFacility, QFilterCondition> {
  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'activatedAt',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'activatedAt',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activatedAtGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'activatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activatedAtLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'activatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activatedAtBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'activatedAt',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'activityId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'activityId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'activityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'activityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'activityId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'activityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'activityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      activityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      additionalDetailsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'additionalDetails',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      additionalDetailsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'additionalDetails',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      addressIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'address',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      addressIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'address',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'assignedEmployeeUser',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'assignedEmployeeUser',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignedEmployeeUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'assignedEmployeeUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'assignedEmployeeUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'assignedEmployeeUser',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'assignedEmployeeUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'assignedEmployeeUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assignedEmployeeUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assignedEmployeeUser',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignedEmployeeUser',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedEmployeeUserIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assignedEmployeeUser',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'assignedUser',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'assignedUser',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignedUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'assignedUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'assignedUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'assignedUser',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'assignedUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'assignedUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assignedUser',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assignedUser',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignedUser',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      assignedUserIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assignedUser',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      completedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'completedAt',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      completedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'completedAt',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      completedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'completedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      completedAtGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'completedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      completedAtLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'completedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      completedAtBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'completedAt',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'description',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'description',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'description',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'description',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'description',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      descriptionIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'description',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facility',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facility',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'fieldPlan',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'fieldPlan',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'fieldPlanId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'fieldPlanId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'fieldPlanId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'fieldPlanId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'fieldPlanId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'fieldPlanId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'fieldPlanId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'fieldPlanId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'fieldPlanId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'fieldPlanId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'fieldPlanId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlanIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'fieldPlanId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'id',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      idIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      rowVersionIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'rowVersion',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      rowVersionIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'rowVersion',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      rowVersionEqualTo(int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'rowVersion',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      rowVersionGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'rowVersion',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      rowVersionLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'rowVersion',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      rowVersionBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'rowVersion',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      scheduledAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'scheduledAt',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      scheduledAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'scheduledAt',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      scheduledAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduledAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      scheduledAtGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'scheduledAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      scheduledAtLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'scheduledAt',
        value: value,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      scheduledAtBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'scheduledAt',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'status',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'status',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'tenantId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }
}

extension ActivityFacilityQueryObject
    on QueryBuilder<ActivityFacility, ActivityFacility, QFilterCondition> {
  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      additionalDetails(FilterQuery<AdditionalDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'additionalDetails');
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      address(FilterQuery<AddressModel> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'address');
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      facility(FilterQuery<Facility> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'facility');
    });
  }

  QueryBuilder<ActivityFacility, ActivityFacility, QAfterFilterCondition>
      fieldPlan(FilterQuery<FieldPlan> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'fieldPlan');
    });
  }
}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const FacilityAddressSchema = Schema(
  name: r'FacilityAddress',
  id: 2013743256626840514,
  properties: {
    r'addressId': PropertySchema(
      id: 0,
      name: r'addressId',
      type: IsarType.string,
    ),
    r'addressLine1': PropertySchema(
      id: 1,
      name: r'addressLine1',
      type: IsarType.string,
    ),
    r'addressLine2': PropertySchema(
      id: 2,
      name: r'addressLine2',
      type: IsarType.string,
    ),
    r'addressNumber': PropertySchema(
      id: 3,
      name: r'addressNumber',
      type: IsarType.string,
    ),
    r'block': PropertySchema(
      id: 4,
      name: r'block',
      type: IsarType.string,
    ),
    r'buildingName': PropertySchema(
      id: 5,
      name: r'buildingName',
      type: IsarType.string,
    ),
    r'city': PropertySchema(
      id: 6,
      name: r'city',
      type: IsarType.string,
    ),
    r'detail': PropertySchema(
      id: 7,
      name: r'detail',
      type: IsarType.string,
    ),
    r'district': PropertySchema(
      id: 8,
      name: r'district',
      type: IsarType.string,
    ),
    r'doorNo': PropertySchema(
      id: 9,
      name: r'doorNo',
      type: IsarType.string,
    ),
    r'landmark': PropertySchema(
      id: 10,
      name: r'landmark',
      type: IsarType.string,
    ),
    r'latitude': PropertySchema(
      id: 11,
      name: r'latitude',
      type: IsarType.double,
    ),
    r'localityCode': PropertySchema(
      id: 12,
      name: r'localityCode',
      type: IsarType.string,
    ),
    r'locationAccuracy': PropertySchema(
      id: 13,
      name: r'locationAccuracy',
      type: IsarType.double,
    ),
    r'longitude': PropertySchema(
      id: 14,
      name: r'longitude',
      type: IsarType.double,
    ),
    r'pincode': PropertySchema(
      id: 15,
      name: r'pincode',
      type: IsarType.string,
    ),
    r'state': PropertySchema(
      id: 16,
      name: r'state',
      type: IsarType.string,
    ),
    r'street': PropertySchema(
      id: 17,
      name: r'street',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 18,
      name: r'tenantId',
      type: IsarType.string,
    ),
    r'type': PropertySchema(
      id: 19,
      name: r'type',
      type: IsarType.string,
    )
  },
  estimateSize: _facilityAddressEstimateSize,
  serialize: _facilityAddressSerialize,
  deserialize: _facilityAddressDeserialize,
  deserializeProp: _facilityAddressDeserializeProp,
);

int _facilityAddressEstimateSize(
  FacilityAddress object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.addressId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.addressLine1;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.addressLine2;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.addressNumber;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.block;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.buildingName;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.city;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.detail;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.district;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.doorNo;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.landmark;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.localityCode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.pincode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.state;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.street;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.tenantId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.type;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _facilityAddressSerialize(
  FacilityAddress object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.addressId);
  writer.writeString(offsets[1], object.addressLine1);
  writer.writeString(offsets[2], object.addressLine2);
  writer.writeString(offsets[3], object.addressNumber);
  writer.writeString(offsets[4], object.block);
  writer.writeString(offsets[5], object.buildingName);
  writer.writeString(offsets[6], object.city);
  writer.writeString(offsets[7], object.detail);
  writer.writeString(offsets[8], object.district);
  writer.writeString(offsets[9], object.doorNo);
  writer.writeString(offsets[10], object.landmark);
  writer.writeDouble(offsets[11], object.latitude);
  writer.writeString(offsets[12], object.localityCode);
  writer.writeDouble(offsets[13], object.locationAccuracy);
  writer.writeDouble(offsets[14], object.longitude);
  writer.writeString(offsets[15], object.pincode);
  writer.writeString(offsets[16], object.state);
  writer.writeString(offsets[17], object.street);
  writer.writeString(offsets[18], object.tenantId);
  writer.writeString(offsets[19], object.type);
}

FacilityAddress _facilityAddressDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = FacilityAddress();
  object.addressId = reader.readStringOrNull(offsets[0]);
  object.addressLine1 = reader.readStringOrNull(offsets[1]);
  object.addressLine2 = reader.readStringOrNull(offsets[2]);
  object.addressNumber = reader.readStringOrNull(offsets[3]);
  object.block = reader.readStringOrNull(offsets[4]);
  object.buildingName = reader.readStringOrNull(offsets[5]);
  object.city = reader.readStringOrNull(offsets[6]);
  object.detail = reader.readStringOrNull(offsets[7]);
  object.district = reader.readStringOrNull(offsets[8]);
  object.doorNo = reader.readStringOrNull(offsets[9]);
  object.landmark = reader.readStringOrNull(offsets[10]);
  object.latitude = reader.readDoubleOrNull(offsets[11]);
  object.localityCode = reader.readStringOrNull(offsets[12]);
  object.locationAccuracy = reader.readDoubleOrNull(offsets[13]);
  object.longitude = reader.readDoubleOrNull(offsets[14]);
  object.pincode = reader.readStringOrNull(offsets[15]);
  object.state = reader.readStringOrNull(offsets[16]);
  object.street = reader.readStringOrNull(offsets[17]);
  object.tenantId = reader.readStringOrNull(offsets[18]);
  object.type = reader.readStringOrNull(offsets[19]);
  return object;
}

P _facilityAddressDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset)) as P;
    case 7:
      return (reader.readStringOrNull(offset)) as P;
    case 8:
      return (reader.readStringOrNull(offset)) as P;
    case 9:
      return (reader.readStringOrNull(offset)) as P;
    case 10:
      return (reader.readStringOrNull(offset)) as P;
    case 11:
      return (reader.readDoubleOrNull(offset)) as P;
    case 12:
      return (reader.readStringOrNull(offset)) as P;
    case 13:
      return (reader.readDoubleOrNull(offset)) as P;
    case 14:
      return (reader.readDoubleOrNull(offset)) as P;
    case 15:
      return (reader.readStringOrNull(offset)) as P;
    case 16:
      return (reader.readStringOrNull(offset)) as P;
    case 17:
      return (reader.readStringOrNull(offset)) as P;
    case 18:
      return (reader.readStringOrNull(offset)) as P;
    case 19:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension FacilityAddressQueryFilter
    on QueryBuilder<FacilityAddress, FacilityAddress, QFilterCondition> {
  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'addressId',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'addressId',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'addressId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'addressId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'addressId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'addressId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'addressId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'addressId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'addressId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressId',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'addressId',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1IsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'addressLine1',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1IsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'addressLine1',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1EqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressLine1',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1GreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'addressLine1',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1LessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'addressLine1',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1Between(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'addressLine1',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1StartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'addressLine1',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1EndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'addressLine1',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1Contains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'addressLine1',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1Matches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'addressLine1',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1IsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressLine1',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine1IsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'addressLine1',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2IsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'addressLine2',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2IsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'addressLine2',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2EqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressLine2',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2GreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'addressLine2',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2LessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'addressLine2',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2Between(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'addressLine2',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2StartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'addressLine2',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2EndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'addressLine2',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2Contains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'addressLine2',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2Matches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'addressLine2',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2IsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressLine2',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressLine2IsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'addressLine2',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'addressNumber',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'addressNumber',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'addressNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'addressNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'addressNumber',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'addressNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'addressNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'addressNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'addressNumber',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'addressNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      addressNumberIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'addressNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'block',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'block',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'block',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'block',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'block',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      blockIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'block',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'buildingName',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'buildingName',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'buildingName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'buildingName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'buildingName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'buildingName',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'buildingName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'buildingName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'buildingName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'buildingName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'buildingName',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      buildingNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'buildingName',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'city',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'city',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'city',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'city',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'city',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'city',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'city',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'city',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'city',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'city',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'city',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      cityIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'city',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'detail',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'detail',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'detail',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'detail',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'detail',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'detail',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'detail',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'detail',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'detail',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'detail',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'detail',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      detailIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'detail',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'district',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'district',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'district',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'district',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'district',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      districtIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'district',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'doorNo',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'doorNo',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'doorNo',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'doorNo',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'doorNo',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'doorNo',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'doorNo',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'doorNo',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'doorNo',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'doorNo',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'doorNo',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      doorNoIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'doorNo',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'landmark',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'landmark',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'landmark',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'landmark',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'landmark',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'landmark',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'landmark',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'landmark',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'landmark',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'landmark',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'landmark',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      landmarkIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'landmark',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      latitudeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'latitude',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      latitudeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'latitude',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      latitudeEqualTo(
    double? value, {
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'latitude',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      latitudeGreaterThan(
    double? value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'latitude',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      latitudeLessThan(
    double? value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'latitude',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      latitudeBetween(
    double? lower,
    double? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'latitude',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'localityCode',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'localityCode',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'localityCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'localityCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'localityCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'localityCode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'localityCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'localityCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'localityCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'localityCode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'localityCode',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      localityCodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'localityCode',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      locationAccuracyIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'locationAccuracy',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      locationAccuracyIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'locationAccuracy',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      locationAccuracyEqualTo(
    double? value, {
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'locationAccuracy',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      locationAccuracyGreaterThan(
    double? value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'locationAccuracy',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      locationAccuracyLessThan(
    double? value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'locationAccuracy',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      locationAccuracyBetween(
    double? lower,
    double? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'locationAccuracy',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      longitudeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'longitude',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      longitudeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'longitude',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      longitudeEqualTo(
    double? value, {
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'longitude',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      longitudeGreaterThan(
    double? value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'longitude',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      longitudeLessThan(
    double? value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'longitude',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      longitudeBetween(
    double? lower,
    double? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'longitude',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'pincode',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'pincode',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pincode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'pincode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'pincode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'pincode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'pincode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'pincode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'pincode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'pincode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pincode',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      pincodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'pincode',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'state',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'state',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'state',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'state',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'state',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      stateIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'state',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'street',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'street',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'street',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'street',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'street',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'street',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'street',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'street',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'street',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'street',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'street',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      streetIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'street',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'tenantId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'type',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'type',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'type',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'type',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'type',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityAddress, FacilityAddress, QAfterFilterCondition>
      typeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'type',
        value: '',
      ));
    });
  }
}

extension FacilityAddressQueryObject
    on QueryBuilder<FacilityAddress, FacilityAddress, QFilterCondition> {}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const FacilityDetailsSchema = Schema(
  name: r'FacilityDetails',
  id: 6715944927992802061,
  properties: {
    r'hfr_id': PropertySchema(
      id: 0,
      name: r'hfr_id',
      type: IsarType.string,
    ),
    r'nin_id': PropertySchema(
      id: 1,
      name: r'nin_id',
      type: IsarType.string,
    ),
    r'pocContact': PropertySchema(
      id: 2,
      name: r'pocContact',
      type: IsarType.string,
    ),
    r'pocDesignation': PropertySchema(
      id: 3,
      name: r'pocDesignation',
      type: IsarType.string,
    ),
    r'pocName': PropertySchema(
      id: 4,
      name: r'pocName',
      type: IsarType.string,
    ),
    r'solar_solution_design_type': PropertySchema(
      id: 5,
      name: r'solar_solution_design_type',
      type: IsarType.string,
    )
  },
  estimateSize: _facilityDetailsEstimateSize,
  serialize: _facilityDetailsSerialize,
  deserialize: _facilityDetailsDeserialize,
  deserializeProp: _facilityDetailsDeserializeProp,
);

int _facilityDetailsEstimateSize(
  FacilityDetails object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.hfr_id;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.nin_id;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.pocContact;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.pocDesignation;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.pocName;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.solar_solution_design_type;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _facilityDetailsSerialize(
  FacilityDetails object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.hfr_id);
  writer.writeString(offsets[1], object.nin_id);
  writer.writeString(offsets[2], object.pocContact);
  writer.writeString(offsets[3], object.pocDesignation);
  writer.writeString(offsets[4], object.pocName);
  writer.writeString(offsets[5], object.solar_solution_design_type);
}

FacilityDetails _facilityDetailsDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = FacilityDetails();
  object.hfr_id = reader.readStringOrNull(offsets[0]);
  object.nin_id = reader.readStringOrNull(offsets[1]);
  object.pocContact = reader.readStringOrNull(offsets[2]);
  object.pocDesignation = reader.readStringOrNull(offsets[3]);
  object.pocName = reader.readStringOrNull(offsets[4]);
  object.solar_solution_design_type = reader.readStringOrNull(offsets[5]);
  return object;
}

P _facilityDetailsDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension FacilityDetailsQueryFilter
    on QueryBuilder<FacilityDetails, FacilityDetails, QFilterCondition> {
  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'hfr_id',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'hfr_id',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'hfr_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'hfr_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'hfr_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'hfr_id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'hfr_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'hfr_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'hfr_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'hfr_id',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'hfr_id',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      hfr_idIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'hfr_id',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'nin_id',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'nin_id',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'nin_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'nin_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'nin_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'nin_id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'nin_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'nin_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'nin_id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'nin_id',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'nin_id',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      nin_idIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'nin_id',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'pocContact',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'pocContact',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pocContact',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'pocContact',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'pocContact',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'pocContact',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'pocContact',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'pocContact',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'pocContact',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'pocContact',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pocContact',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocContactIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'pocContact',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'pocDesignation',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'pocDesignation',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pocDesignation',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'pocDesignation',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'pocDesignation',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'pocDesignation',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'pocDesignation',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'pocDesignation',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'pocDesignation',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'pocDesignation',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pocDesignation',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocDesignationIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'pocDesignation',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'pocName',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'pocName',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pocName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'pocName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'pocName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'pocName',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'pocName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'pocName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'pocName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'pocName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'pocName',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      pocNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'pocName',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'solar_solution_design_type',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'solar_solution_design_type',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'solar_solution_design_type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'solar_solution_design_type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'solar_solution_design_type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'solar_solution_design_type',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'solar_solution_design_type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'solar_solution_design_type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeContains(String value,
          {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'solar_solution_design_type',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeMatches(String pattern,
          {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'solar_solution_design_type',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'solar_solution_design_type',
        value: '',
      ));
    });
  }

  QueryBuilder<FacilityDetails, FacilityDetails, QAfterFilterCondition>
      solar_solution_design_typeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'solar_solution_design_type',
        value: '',
      ));
    });
  }
}

extension FacilityDetailsQueryObject
    on QueryBuilder<FacilityDetails, FacilityDetails, QFilterCondition> {}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const FacilitySchema = Schema(
  name: r'Facility',
  id: -1716346399248053847,
  properties: {
    r'address': PropertySchema(
      id: 0,
      name: r'address',
      type: IsarType.object,
      target: r'FacilityAddress',
    ),
    r'boundaryCode': PropertySchema(
      id: 1,
      name: r'boundaryCode',
      type: IsarType.string,
    ),
    r'facilityDetails': PropertySchema(
      id: 2,
      name: r'facilityDetails',
      type: IsarType.object,
      target: r'FacilityDetails',
    ),
    r'facilityId': PropertySchema(
      id: 3,
      name: r'facilityId',
      type: IsarType.string,
    ),
    r'facilityName': PropertySchema(
      id: 4,
      name: r'facilityName',
      type: IsarType.string,
    ),
    r'facilityRegion': PropertySchema(
      id: 5,
      name: r'facilityRegion',
      type: IsarType.string,
    ),
    r'facilityType': PropertySchema(
      id: 6,
      name: r'facilityType',
      type: IsarType.string,
    ),
    r'facility_category': PropertySchema(
      id: 7,
      name: r'facility_category',
      type: IsarType.string,
    ),
    r'facility_ownership': PropertySchema(
      id: 8,
      name: r'facility_ownership',
      type: IsarType.string,
    ),
    r'facility_subtype': PropertySchema(
      id: 9,
      name: r'facility_subtype',
      type: IsarType.string,
    ),
    r'isActive': PropertySchema(
      id: 10,
      name: r'isActive',
      type: IsarType.bool,
    ),
    r'tenantId': PropertySchema(
      id: 11,
      name: r'tenantId',
      type: IsarType.string,
    ),
    r'wfStatus': PropertySchema(
      id: 12,
      name: r'wfStatus',
      type: IsarType.string,
    )
  },
  estimateSize: _facilityEstimateSize,
  serialize: _facilitySerialize,
  deserialize: _facilityDeserialize,
  deserializeProp: _facilityDeserializeProp,
);

int _facilityEstimateSize(
  Facility object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.address;
    if (value != null) {
      bytesCount += 3 +
          FacilityAddressSchema.estimateSize(
              value, allOffsets[FacilityAddress]!, allOffsets);
    }
  }
  {
    final value = object.boundaryCode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facilityDetails;
    if (value != null) {
      bytesCount += 3 +
          FacilityDetailsSchema.estimateSize(
              value, allOffsets[FacilityDetails]!, allOffsets);
    }
  }
  {
    final value = object.facilityId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facilityName;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facilityRegion;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facilityType;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facility_category;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facility_ownership;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.facility_subtype;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.tenantId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.wfStatus;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _facilitySerialize(
  Facility object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<FacilityAddress>(
    offsets[0],
    allOffsets,
    FacilityAddressSchema.serialize,
    object.address,
  );
  writer.writeString(offsets[1], object.boundaryCode);
  writer.writeObject<FacilityDetails>(
    offsets[2],
    allOffsets,
    FacilityDetailsSchema.serialize,
    object.facilityDetails,
  );
  writer.writeString(offsets[3], object.facilityId);
  writer.writeString(offsets[4], object.facilityName);
  writer.writeString(offsets[5], object.facilityRegion);
  writer.writeString(offsets[6], object.facilityType);
  writer.writeString(offsets[7], object.facility_category);
  writer.writeString(offsets[8], object.facility_ownership);
  writer.writeString(offsets[9], object.facility_subtype);
  writer.writeBool(offsets[10], object.isActive);
  writer.writeString(offsets[11], object.tenantId);
  writer.writeString(offsets[12], object.wfStatus);
}

Facility _facilityDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = Facility();
  object.address = reader.readObjectOrNull<FacilityAddress>(
    offsets[0],
    FacilityAddressSchema.deserialize,
    allOffsets,
  );
  object.boundaryCode = reader.readStringOrNull(offsets[1]);
  object.facilityDetails = reader.readObjectOrNull<FacilityDetails>(
    offsets[2],
    FacilityDetailsSchema.deserialize,
    allOffsets,
  );
  object.facilityId = reader.readStringOrNull(offsets[3]);
  object.facilityName = reader.readStringOrNull(offsets[4]);
  object.facilityRegion = reader.readStringOrNull(offsets[5]);
  object.facilityType = reader.readStringOrNull(offsets[6]);
  object.facility_category = reader.readStringOrNull(offsets[7]);
  object.facility_ownership = reader.readStringOrNull(offsets[8]);
  object.facility_subtype = reader.readStringOrNull(offsets[9]);
  object.isActive = reader.readBoolOrNull(offsets[10]);
  object.tenantId = reader.readStringOrNull(offsets[11]);
  object.wfStatus = reader.readStringOrNull(offsets[12]);
  return object;
}

P _facilityDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<FacilityAddress>(
        offset,
        FacilityAddressSchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readObjectOrNull<FacilityDetails>(
        offset,
        FacilityDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset)) as P;
    case 7:
      return (reader.readStringOrNull(offset)) as P;
    case 8:
      return (reader.readStringOrNull(offset)) as P;
    case 9:
      return (reader.readStringOrNull(offset)) as P;
    case 10:
      return (reader.readBoolOrNull(offset)) as P;
    case 11:
      return (reader.readStringOrNull(offset)) as P;
    case 12:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension FacilityQueryFilter
    on QueryBuilder<Facility, Facility, QFilterCondition> {
  QueryBuilder<Facility, Facility, QAfterFilterCondition> addressIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'address',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> addressIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'address',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'boundaryCode',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      boundaryCodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'boundaryCode',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'boundaryCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      boundaryCodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'boundaryCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'boundaryCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'boundaryCode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      boundaryCodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'boundaryCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'boundaryCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'boundaryCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> boundaryCodeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'boundaryCode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      boundaryCodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'boundaryCode',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      boundaryCodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'boundaryCode',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityDetailsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityDetails',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityDetailsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityDetails',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityName',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityNameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityName',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityNameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityName',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityNameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityNameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityName',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityName',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityRegion',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityRegion',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityRegionEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityRegion',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityRegion',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityRegion',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityRegionBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityRegion',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityRegion',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityRegion',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityRegion',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityRegionMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityRegion',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityRegion',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityRegionIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityRegion',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityType',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityTypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityType',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityTypeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityTypeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityType',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facilityTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityType',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facility_category',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facility_category',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facility_category',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facility_category',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facility_category',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facility_category',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facility_category',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facility_category',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facility_category',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facility_category',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facility_category',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_categoryIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facility_category',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facility_ownership',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facility_ownership',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facility_ownership',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facility_ownership',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facility_ownership',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facility_ownership',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facility_ownership',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facility_ownership',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facility_ownership',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facility_ownership',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facility_ownership',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_ownershipIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facility_ownership',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facility_subtype',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facility_subtype',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facility_subtype',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facility_subtype',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facility_subtype',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facility_subtype',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facility_subtype',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facility_subtype',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facility_subtype',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facility_subtype',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facility_subtype',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition>
      facility_subtypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facility_subtype',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> isActiveIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'isActive',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> isActiveIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'isActive',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> isActiveEqualTo(
      bool? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'isActive',
        value: value,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'tenantId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'wfStatus',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'wfStatus',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'wfStatus',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'wfStatus',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'wfStatus',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'wfStatus',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'wfStatus',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'wfStatus',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'wfStatus',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'wfStatus',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'wfStatus',
        value: '',
      ));
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> wfStatusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'wfStatus',
        value: '',
      ));
    });
  }
}

extension FacilityQueryObject
    on QueryBuilder<Facility, Facility, QFilterCondition> {
  QueryBuilder<Facility, Facility, QAfterFilterCondition> address(
      FilterQuery<FacilityAddress> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'address');
    });
  }

  QueryBuilder<Facility, Facility, QAfterFilterCondition> facilityDetails(
      FilterQuery<FacilityDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'facilityDetails');
    });
  }
}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const FieldPlanSchema = Schema(
  name: r'FieldPlan',
  id: -6521223832976236710,
  properties: {
    r'endDate': PropertySchema(
      id: 0,
      name: r'endDate',
      type: IsarType.long,
    ),
    r'endDateTime': PropertySchema(
      id: 1,
      name: r'endDateTime',
      type: IsarType.dateTime,
    ),
    r'healthFacilityNumber': PropertySchema(
      id: 2,
      name: r'healthFacilityNumber',
      type: IsarType.long,
    ),
    r'id': PropertySchema(
      id: 3,
      name: r'id',
      type: IsarType.string,
    ),
    r'name': PropertySchema(
      id: 4,
      name: r'name',
      type: IsarType.string,
    ),
    r'project': PropertySchema(
      id: 5,
      name: r'project',
      type: IsarType.object,
      target: r'Project',
    ),
    r'startDate': PropertySchema(
      id: 6,
      name: r'startDate',
      type: IsarType.long,
    ),
    r'startDateTime': PropertySchema(
      id: 7,
      name: r'startDateTime',
      type: IsarType.dateTime,
    ),
    r'status': PropertySchema(
      id: 8,
      name: r'status',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 9,
      name: r'tenantId',
      type: IsarType.string,
    )
  },
  estimateSize: _fieldPlanEstimateSize,
  serialize: _fieldPlanSerialize,
  deserialize: _fieldPlanDeserialize,
  deserializeProp: _fieldPlanDeserializeProp,
);

int _fieldPlanEstimateSize(
  FieldPlan object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.id;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.name;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.project;
    if (value != null) {
      bytesCount += 3 +
          ProjectSchema.estimateSize(value, allOffsets[Project]!, allOffsets);
    }
  }
  {
    final value = object.status;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.tenantId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _fieldPlanSerialize(
  FieldPlan object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeLong(offsets[0], object.endDate);
  writer.writeDateTime(offsets[1], object.endDateTime);
  writer.writeLong(offsets[2], object.healthFacilityNumber);
  writer.writeString(offsets[3], object.id);
  writer.writeString(offsets[4], object.name);
  writer.writeObject<Project>(
    offsets[5],
    allOffsets,
    ProjectSchema.serialize,
    object.project,
  );
  writer.writeLong(offsets[6], object.startDate);
  writer.writeDateTime(offsets[7], object.startDateTime);
  writer.writeString(offsets[8], object.status);
  writer.writeString(offsets[9], object.tenantId);
}

FieldPlan _fieldPlanDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = FieldPlan();
  object.endDateTime = reader.readDateTimeOrNull(offsets[1]);
  object.healthFacilityNumber = reader.readLongOrNull(offsets[2]);
  object.id = reader.readStringOrNull(offsets[3]);
  object.name = reader.readStringOrNull(offsets[4]);
  object.project = reader.readObjectOrNull<Project>(
    offsets[5],
    ProjectSchema.deserialize,
    allOffsets,
  );
  object.startDateTime = reader.readDateTimeOrNull(offsets[7]);
  object.status = reader.readStringOrNull(offsets[8]);
  object.tenantId = reader.readStringOrNull(offsets[9]);
  return object;
}

P _fieldPlanDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readLongOrNull(offset)) as P;
    case 1:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 2:
      return (reader.readLongOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readObjectOrNull<Project>(
        offset,
        ProjectSchema.deserialize,
        allOffsets,
      )) as P;
    case 6:
      return (reader.readLongOrNull(offset)) as P;
    case 7:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 8:
      return (reader.readStringOrNull(offset)) as P;
    case 9:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension FieldPlanQueryFilter
    on QueryBuilder<FieldPlan, FieldPlan, QFilterCondition> {
  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'endDate',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'endDate',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateEqualTo(
      int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'endDate',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      endDateTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'endDateTime',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      endDateTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'endDateTime',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateTimeEqualTo(
      DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      endDateTimeGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateTimeLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> endDateTimeBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'endDateTime',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      healthFacilityNumberIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'healthFacilityNumber',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      healthFacilityNumberIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'healthFacilityNumber',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      healthFacilityNumberEqualTo(int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'healthFacilityNumber',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      healthFacilityNumberGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'healthFacilityNumber',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      healthFacilityNumberLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'healthFacilityNumber',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      healthFacilityNumberBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'healthFacilityNumber',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'id',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'id',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'id',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> idIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'name',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'name',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> nameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> projectIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'project',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> projectIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'project',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> startDateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'startDate',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'startDate',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> startDateEqualTo(
      int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> startDateLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> startDateBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'startDate',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'startDateTime',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'startDateTime',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateTimeEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateTimeGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateTimeLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      startDateTimeBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'startDateTime',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'status',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'status',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      tenantIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'tenantId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition>
      tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }
}

extension FieldPlanQueryObject
    on QueryBuilder<FieldPlan, FieldPlan, QFilterCondition> {
  QueryBuilder<FieldPlan, FieldPlan, QAfterFilterCondition> project(
      FilterQuery<Project> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'project');
    });
  }
}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const ProjectSchema = Schema(
  name: r'Project',
  id: 3302999628838485849,
  properties: {
    r'additionalDetails': PropertySchema(
      id: 0,
      name: r'additionalDetails',
      type: IsarType.object,
      target: r'ProjectAdditionalDetails',
    ),
    r'endDate': PropertySchema(
      id: 1,
      name: r'endDate',
      type: IsarType.long,
    ),
    r'endDateTime': PropertySchema(
      id: 2,
      name: r'endDateTime',
      type: IsarType.dateTime,
    ),
    r'id': PropertySchema(
      id: 3,
      name: r'id',
      type: IsarType.string,
    ),
    r'name': PropertySchema(
      id: 4,
      name: r'name',
      type: IsarType.string,
    ),
    r'projectNumber': PropertySchema(
      id: 5,
      name: r'projectNumber',
      type: IsarType.string,
    ),
    r'projectSubType': PropertySchema(
      id: 6,
      name: r'projectSubType',
      type: IsarType.string,
    ),
    r'projectType': PropertySchema(
      id: 7,
      name: r'projectType',
      type: IsarType.string,
    ),
    r'referenceID': PropertySchema(
      id: 8,
      name: r'referenceID',
      type: IsarType.string,
    ),
    r'startDate': PropertySchema(
      id: 9,
      name: r'startDate',
      type: IsarType.long,
    ),
    r'startDateTime': PropertySchema(
      id: 10,
      name: r'startDateTime',
      type: IsarType.dateTime,
    ),
    r'tenantId': PropertySchema(
      id: 11,
      name: r'tenantId',
      type: IsarType.string,
    )
  },
  estimateSize: _projectEstimateSize,
  serialize: _projectSerialize,
  deserialize: _projectDeserialize,
  deserializeProp: _projectDeserializeProp,
);

int _projectEstimateSize(
  Project object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.additionalDetails;
    if (value != null) {
      bytesCount += 3 +
          ProjectAdditionalDetailsSchema.estimateSize(
              value, allOffsets[ProjectAdditionalDetails]!, allOffsets);
    }
  }
  {
    final value = object.id;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.name;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.projectNumber;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.projectSubType;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.projectType;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.referenceID;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.tenantId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _projectSerialize(
  Project object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<ProjectAdditionalDetails>(
    offsets[0],
    allOffsets,
    ProjectAdditionalDetailsSchema.serialize,
    object.additionalDetails,
  );
  writer.writeLong(offsets[1], object.endDate);
  writer.writeDateTime(offsets[2], object.endDateTime);
  writer.writeString(offsets[3], object.id);
  writer.writeString(offsets[4], object.name);
  writer.writeString(offsets[5], object.projectNumber);
  writer.writeString(offsets[6], object.projectSubType);
  writer.writeString(offsets[7], object.projectType);
  writer.writeString(offsets[8], object.referenceID);
  writer.writeLong(offsets[9], object.startDate);
  writer.writeDateTime(offsets[10], object.startDateTime);
  writer.writeString(offsets[11], object.tenantId);
}

Project _projectDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = Project();
  object.additionalDetails = reader.readObjectOrNull<ProjectAdditionalDetails>(
    offsets[0],
    ProjectAdditionalDetailsSchema.deserialize,
    allOffsets,
  );
  object.endDateTime = reader.readDateTimeOrNull(offsets[2]);
  object.id = reader.readStringOrNull(offsets[3]);
  object.name = reader.readStringOrNull(offsets[4]);
  object.projectNumber = reader.readStringOrNull(offsets[5]);
  object.projectSubType = reader.readStringOrNull(offsets[6]);
  object.projectType = reader.readStringOrNull(offsets[7]);
  object.referenceID = reader.readStringOrNull(offsets[8]);
  object.startDateTime = reader.readDateTimeOrNull(offsets[10]);
  object.tenantId = reader.readStringOrNull(offsets[11]);
  return object;
}

P _projectDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<ProjectAdditionalDetails>(
        offset,
        ProjectAdditionalDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readLongOrNull(offset)) as P;
    case 2:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset)) as P;
    case 7:
      return (reader.readStringOrNull(offset)) as P;
    case 8:
      return (reader.readStringOrNull(offset)) as P;
    case 9:
      return (reader.readLongOrNull(offset)) as P;
    case 10:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 11:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension ProjectQueryFilter
    on QueryBuilder<Project, Project, QFilterCondition> {
  QueryBuilder<Project, Project, QAfterFilterCondition>
      additionalDetailsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'additionalDetails',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      additionalDetailsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'additionalDetails',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'endDate',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'endDate',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateEqualTo(
      int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'endDate',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'endDateTime',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'endDateTime',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateTimeEqualTo(
      DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateTimeGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateTimeLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> endDateTimeBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'endDateTime',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'id',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'id',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idContains(String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'id',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'id',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> idIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'name',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'name',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> nameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectNumber',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectNumberIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectNumber',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectNumberGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'projectNumber',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectNumber',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectNumberIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectNumberIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectSubType',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectSubTypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectSubType',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectSubType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectSubTypeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'projectSubType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'projectSubType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'projectSubType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectSubTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'projectSubType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'projectSubType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectSubType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectSubTypeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectSubType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectSubTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectSubType',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectSubTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectSubType',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectType',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectType',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'projectType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> projectTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectType',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      projectTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectType',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'referenceID',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'referenceID',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'referenceID',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'referenceID',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'referenceID',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'referenceID',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'referenceID',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'referenceID',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'referenceID',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'referenceID',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> referenceIDIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'referenceID',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      referenceIDIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'referenceID',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'startDate',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'startDate',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateEqualTo(
      int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'startDate',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'startDateTime',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      startDateTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'startDateTime',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateTimeEqualTo(
      DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition>
      startDateTimeGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateTimeLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> startDateTimeBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'startDateTime',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'tenantId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<Project, Project, QAfterFilterCondition> tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }
}

extension ProjectQueryObject
    on QueryBuilder<Project, Project, QFilterCondition> {
  QueryBuilder<Project, Project, QAfterFilterCondition> additionalDetails(
      FilterQuery<ProjectAdditionalDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'additionalDetails');
    });
  }
}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const ProjectAdditionalDetailsSchema = Schema(
  name: r'ProjectAdditionalDetails',
  id: 2801978142905411967,
  properties: {
    r'facility': PropertySchema(
      id: 0,
      name: r'facility',
      type: IsarType.object,
      target: r'Facility',
    ),
    r'geographyDetails': PropertySchema(
      id: 1,
      name: r'geographyDetails',
      type: IsarType.object,
      target: r'GeographyDetails',
    ),
    r'status': PropertySchema(
      id: 2,
      name: r'status',
      type: IsarType.string,
    )
  },
  estimateSize: _projectAdditionalDetailsEstimateSize,
  serialize: _projectAdditionalDetailsSerialize,
  deserialize: _projectAdditionalDetailsDeserialize,
  deserializeProp: _projectAdditionalDetailsDeserializeProp,
);

int _projectAdditionalDetailsEstimateSize(
  ProjectAdditionalDetails object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.facility;
    if (value != null) {
      bytesCount += 3 +
          FacilitySchema.estimateSize(value, allOffsets[Facility]!, allOffsets);
    }
  }
  {
    final value = object.geographyDetails;
    if (value != null) {
      bytesCount += 3 +
          GeographyDetailsSchema.estimateSize(
              value, allOffsets[GeographyDetails]!, allOffsets);
    }
  }
  {
    final value = object.status;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _projectAdditionalDetailsSerialize(
  ProjectAdditionalDetails object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<Facility>(
    offsets[0],
    allOffsets,
    FacilitySchema.serialize,
    object.facility,
  );
  writer.writeObject<GeographyDetails>(
    offsets[1],
    allOffsets,
    GeographyDetailsSchema.serialize,
    object.geographyDetails,
  );
  writer.writeString(offsets[2], object.status);
}

ProjectAdditionalDetails _projectAdditionalDetailsDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = ProjectAdditionalDetails();
  object.facility = reader.readObjectOrNull<Facility>(
    offsets[0],
    FacilitySchema.deserialize,
    allOffsets,
  );
  object.geographyDetails = reader.readObjectOrNull<GeographyDetails>(
    offsets[1],
    GeographyDetailsSchema.deserialize,
    allOffsets,
  );
  object.status = reader.readStringOrNull(offsets[2]);
  return object;
}

P _projectAdditionalDetailsDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<Facility>(
        offset,
        FacilitySchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readObjectOrNull<GeographyDetails>(
        offset,
        GeographyDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension ProjectAdditionalDetailsQueryFilter on QueryBuilder<
    ProjectAdditionalDetails, ProjectAdditionalDetails, QFilterCondition> {
  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> facilityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facility',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> facilityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facility',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> geographyDetailsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'geographyDetails',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> geographyDetailsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'geographyDetails',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'status',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
          QAfterFilterCondition>
      statusContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
          QAfterFilterCondition>
      statusMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'status',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }
}

extension ProjectAdditionalDetailsQueryObject on QueryBuilder<
    ProjectAdditionalDetails, ProjectAdditionalDetails, QFilterCondition> {
  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> facility(FilterQuery<Facility> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'facility');
    });
  }

  QueryBuilder<ProjectAdditionalDetails, ProjectAdditionalDetails,
      QAfterFilterCondition> geographyDetails(FilterQuery<GeographyDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'geographyDetails');
    });
  }
}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const GeographyDetailsSchema = Schema(
  name: r'GeographyDetails',
  id: 5055433399552421457,
  properties: {
    r'blocks': PropertySchema(
      id: 0,
      name: r'blocks',
      type: IsarType.objectList,
      target: r'BlockRef',
    ),
    r'state': PropertySchema(
      id: 1,
      name: r'state',
      type: IsarType.object,
      target: r'StateRef',
    )
  },
  estimateSize: _geographyDetailsEstimateSize,
  serialize: _geographyDetailsSerialize,
  deserialize: _geographyDetailsDeserialize,
  deserializeProp: _geographyDetailsDeserializeProp,
);

int _geographyDetailsEstimateSize(
  GeographyDetails object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final list = object.blocks;
    if (list != null) {
      bytesCount += 3 + list.length * 3;
      {
        final offsets = allOffsets[BlockRef]!;
        for (var i = 0; i < list.length; i++) {
          final value = list[i];
          bytesCount += BlockRefSchema.estimateSize(value, offsets, allOffsets);
        }
      }
    }
  }
  {
    final value = object.state;
    if (value != null) {
      bytesCount += 3 +
          StateRefSchema.estimateSize(value, allOffsets[StateRef]!, allOffsets);
    }
  }
  return bytesCount;
}

void _geographyDetailsSerialize(
  GeographyDetails object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObjectList<BlockRef>(
    offsets[0],
    allOffsets,
    BlockRefSchema.serialize,
    object.blocks,
  );
  writer.writeObject<StateRef>(
    offsets[1],
    allOffsets,
    StateRefSchema.serialize,
    object.state,
  );
}

GeographyDetails _geographyDetailsDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = GeographyDetails();
  object.blocks = reader.readObjectList<BlockRef>(
    offsets[0],
    BlockRefSchema.deserialize,
    allOffsets,
    BlockRef(),
  );
  object.state = reader.readObjectOrNull<StateRef>(
    offsets[1],
    StateRefSchema.deserialize,
    allOffsets,
  );
  return object;
}

P _geographyDetailsDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectList<BlockRef>(
        offset,
        BlockRefSchema.deserialize,
        allOffsets,
        BlockRef(),
      )) as P;
    case 1:
      return (reader.readObjectOrNull<StateRef>(
        offset,
        StateRefSchema.deserialize,
        allOffsets,
      )) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension GeographyDetailsQueryFilter
    on QueryBuilder<GeographyDetails, GeographyDetails, QFilterCondition> {
  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'blocks',
      ));
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'blocks',
      ));
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksLengthEqualTo(int length) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'blocks',
        length,
        true,
        length,
        true,
      );
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'blocks',
        0,
        true,
        0,
        true,
      );
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'blocks',
        0,
        false,
        999999,
        true,
      );
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksLengthLessThan(
    int length, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'blocks',
        0,
        true,
        length,
        include,
      );
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksLengthGreaterThan(
    int length, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'blocks',
        length,
        include,
        999999,
        true,
      );
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksLengthBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'blocks',
        lower,
        includeLower,
        upper,
        includeUpper,
      );
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      stateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'state',
      ));
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      stateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'state',
      ));
    });
  }
}

extension GeographyDetailsQueryObject
    on QueryBuilder<GeographyDetails, GeographyDetails, QFilterCondition> {
  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition>
      blocksElement(FilterQuery<BlockRef> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'blocks');
    });
  }

  QueryBuilder<GeographyDetails, GeographyDetails, QAfterFilterCondition> state(
      FilterQuery<StateRef> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'state');
    });
  }
}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const StateRefSchema = Schema(
  name: r'StateRef',
  id: -5573519050344956570,
  properties: {
    r'code': PropertySchema(
      id: 0,
      name: r'code',
      type: IsarType.string,
    ),
    r'name': PropertySchema(
      id: 1,
      name: r'name',
      type: IsarType.string,
    )
  },
  estimateSize: _stateRefEstimateSize,
  serialize: _stateRefSerialize,
  deserialize: _stateRefDeserialize,
  deserializeProp: _stateRefDeserializeProp,
);

int _stateRefEstimateSize(
  StateRef object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.code;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.name;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _stateRefSerialize(
  StateRef object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.code);
  writer.writeString(offsets[1], object.name);
}

StateRef _stateRefDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = StateRef();
  object.code = reader.readStringOrNull(offsets[0]);
  object.name = reader.readStringOrNull(offsets[1]);
  return object;
}

P _stateRefDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension StateRefQueryFilter
    on QueryBuilder<StateRef, StateRef, QFilterCondition> {
  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'code',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'code',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'code',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'code',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'code',
        value: '',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> codeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'code',
        value: '',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'name',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'name',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<StateRef, StateRef, QAfterFilterCondition> nameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'name',
        value: '',
      ));
    });
  }
}

extension StateRefQueryObject
    on QueryBuilder<StateRef, StateRef, QFilterCondition> {}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const BlockRefSchema = Schema(
  name: r'BlockRef',
  id: -3624656680300054164,
  properties: {
    r'code': PropertySchema(
      id: 0,
      name: r'code',
      type: IsarType.string,
    ),
    r'districtCode': PropertySchema(
      id: 1,
      name: r'districtCode',
      type: IsarType.string,
    ),
    r'name': PropertySchema(
      id: 2,
      name: r'name',
      type: IsarType.string,
    ),
    r'stateCode': PropertySchema(
      id: 3,
      name: r'stateCode',
      type: IsarType.string,
    )
  },
  estimateSize: _blockRefEstimateSize,
  serialize: _blockRefSerialize,
  deserialize: _blockRefDeserialize,
  deserializeProp: _blockRefDeserializeProp,
);

int _blockRefEstimateSize(
  BlockRef object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.code;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.districtCode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.name;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.stateCode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _blockRefSerialize(
  BlockRef object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.code);
  writer.writeString(offsets[1], object.districtCode);
  writer.writeString(offsets[2], object.name);
  writer.writeString(offsets[3], object.stateCode);
}

BlockRef _blockRefDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = BlockRef();
  object.code = reader.readStringOrNull(offsets[0]);
  object.districtCode = reader.readStringOrNull(offsets[1]);
  object.name = reader.readStringOrNull(offsets[2]);
  object.stateCode = reader.readStringOrNull(offsets[3]);
  return object;
}

P _blockRefDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension BlockRefQueryFilter
    on QueryBuilder<BlockRef, BlockRef, QFilterCondition> {
  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'code',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'code',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'code',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'code',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'code',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'code',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> codeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'code',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'districtCode',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition>
      districtCodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'districtCode',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'districtCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition>
      districtCodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'districtCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'districtCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'districtCode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition>
      districtCodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'districtCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'districtCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'districtCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> districtCodeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'districtCode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition>
      districtCodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'districtCode',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition>
      districtCodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'districtCode',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'name',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'name',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'name',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> nameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'stateCode',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'stateCode',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'stateCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'stateCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'stateCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'stateCode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'stateCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'stateCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'stateCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'stateCode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition> stateCodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'stateCode',
        value: '',
      ));
    });
  }

  QueryBuilder<BlockRef, BlockRef, QAfterFilterCondition>
      stateCodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'stateCode',
        value: '',
      ));
    });
  }
}

extension BlockRefQueryObject
    on QueryBuilder<BlockRef, BlockRef, QFilterCondition> {}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const AssetTypeAdditionalDetailsSchema = Schema(
  name: r'AssetTypeAdditionalDetails',
  id: -5638672588894969050,
  properties: {
    r'brandCode': PropertySchema(
      id: 0,
      name: r'brandCode',
      type: IsarType.string,
    ),
    r'brandName': PropertySchema(
      id: 1,
      name: r'brandName',
      type: IsarType.string,
    ),
    r'capacity': PropertySchema(
      id: 2,
      name: r'capacity',
      type: IsarType.string,
    )
  },
  estimateSize: _assetTypeAdditionalDetailsEstimateSize,
  serialize: _assetTypeAdditionalDetailsSerialize,
  deserialize: _assetTypeAdditionalDetailsDeserialize,
  deserializeProp: _assetTypeAdditionalDetailsDeserializeProp,
);

int _assetTypeAdditionalDetailsEstimateSize(
  AssetTypeAdditionalDetails object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.brandCode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.brandName;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.capacity;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _assetTypeAdditionalDetailsSerialize(
  AssetTypeAdditionalDetails object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.brandCode);
  writer.writeString(offsets[1], object.brandName);
  writer.writeString(offsets[2], object.capacity);
}

AssetTypeAdditionalDetails _assetTypeAdditionalDetailsDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = AssetTypeAdditionalDetails();
  object.brandCode = reader.readStringOrNull(offsets[0]);
  object.brandName = reader.readStringOrNull(offsets[1]);
  object.capacity = reader.readStringOrNull(offsets[2]);
  return object;
}

P _assetTypeAdditionalDetailsDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension AssetTypeAdditionalDetailsQueryFilter on QueryBuilder<
    AssetTypeAdditionalDetails, AssetTypeAdditionalDetails, QFilterCondition> {
  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'brandCode',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'brandCode',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'brandCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'brandCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'brandCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'brandCode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'brandCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'brandCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
          QAfterFilterCondition>
      brandCodeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'brandCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
          QAfterFilterCondition>
      brandCodeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'brandCode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'brandCode',
        value: '',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandCodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'brandCode',
        value: '',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'brandName',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'brandName',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'brandName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'brandName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'brandName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'brandName',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'brandName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'brandName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
          QAfterFilterCondition>
      brandNameContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'brandName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
          QAfterFilterCondition>
      brandNameMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'brandName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'brandName',
        value: '',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> brandNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'brandName',
        value: '',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'capacity',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'capacity',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'capacity',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
          QAfterFilterCondition>
      capacityContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
          QAfterFilterCondition>
      capacityMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'capacity',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'capacity',
        value: '',
      ));
    });
  }

  QueryBuilder<AssetTypeAdditionalDetails, AssetTypeAdditionalDetails,
      QAfterFilterCondition> capacityIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'capacity',
        value: '',
      ));
    });
  }
}

extension AssetTypeAdditionalDetailsQueryObject on QueryBuilder<
    AssetTypeAdditionalDetails, AssetTypeAdditionalDetails, QFilterCondition> {}

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const AdditionalDetailsSchema = Schema(
  name: r'AdditionalDetails',
  id: -7408414191232501776,
  properties: {
    r'battery': PropertySchema(
      id: 0,
      name: r'battery',
      type: IsarType.object,
      target: r'AssetTypeAdditionalDetails',
    ),
    r'facility': PropertySchema(
      id: 1,
      name: r'facility',
      type: IsarType.object,
      target: r'Facility',
    ),
    r'inverter': PropertySchema(
      id: 2,
      name: r'inverter',
      type: IsarType.object,
      target: r'AssetTypeAdditionalDetails',
    ),
    r'panel': PropertySchema(
      id: 3,
      name: r'panel',
      type: IsarType.object,
      target: r'AssetTypeAdditionalDetails',
    ),
    r'status': PropertySchema(
      id: 4,
      name: r'status',
      type: IsarType.string,
    ),
    r'systemCode': PropertySchema(
      id: 5,
      name: r'systemCode',
      type: IsarType.string,
    )
  },
  estimateSize: _additionalDetailsEstimateSize,
  serialize: _additionalDetailsSerialize,
  deserialize: _additionalDetailsDeserialize,
  deserializeProp: _additionalDetailsDeserializeProp,
);

int _additionalDetailsEstimateSize(
  AdditionalDetails object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.battery;
    if (value != null) {
      bytesCount += 3 +
          AssetTypeAdditionalDetailsSchema.estimateSize(
              value, allOffsets[AssetTypeAdditionalDetails]!, allOffsets);
    }
  }
  {
    final value = object.facility;
    if (value != null) {
      bytesCount += 3 +
          FacilitySchema.estimateSize(value, allOffsets[Facility]!, allOffsets);
    }
  }
  {
    final value = object.inverter;
    if (value != null) {
      bytesCount += 3 +
          AssetTypeAdditionalDetailsSchema.estimateSize(
              value, allOffsets[AssetTypeAdditionalDetails]!, allOffsets);
    }
  }
  {
    final value = object.panel;
    if (value != null) {
      bytesCount += 3 +
          AssetTypeAdditionalDetailsSchema.estimateSize(
              value, allOffsets[AssetTypeAdditionalDetails]!, allOffsets);
    }
  }
  {
    final value = object.status;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.systemCode;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _additionalDetailsSerialize(
  AdditionalDetails object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<AssetTypeAdditionalDetails>(
    offsets[0],
    allOffsets,
    AssetTypeAdditionalDetailsSchema.serialize,
    object.battery,
  );
  writer.writeObject<Facility>(
    offsets[1],
    allOffsets,
    FacilitySchema.serialize,
    object.facility,
  );
  writer.writeObject<AssetTypeAdditionalDetails>(
    offsets[2],
    allOffsets,
    AssetTypeAdditionalDetailsSchema.serialize,
    object.inverter,
  );
  writer.writeObject<AssetTypeAdditionalDetails>(
    offsets[3],
    allOffsets,
    AssetTypeAdditionalDetailsSchema.serialize,
    object.panel,
  );
  writer.writeString(offsets[4], object.status);
  writer.writeString(offsets[5], object.systemCode);
}

AdditionalDetails _additionalDetailsDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = AdditionalDetails();
  object.battery = reader.readObjectOrNull<AssetTypeAdditionalDetails>(
    offsets[0],
    AssetTypeAdditionalDetailsSchema.deserialize,
    allOffsets,
  );
  object.facility = reader.readObjectOrNull<Facility>(
    offsets[1],
    FacilitySchema.deserialize,
    allOffsets,
  );
  object.inverter = reader.readObjectOrNull<AssetTypeAdditionalDetails>(
    offsets[2],
    AssetTypeAdditionalDetailsSchema.deserialize,
    allOffsets,
  );
  object.panel = reader.readObjectOrNull<AssetTypeAdditionalDetails>(
    offsets[3],
    AssetTypeAdditionalDetailsSchema.deserialize,
    allOffsets,
  );
  object.status = reader.readStringOrNull(offsets[4]);
  object.systemCode = reader.readStringOrNull(offsets[5]);
  return object;
}

P _additionalDetailsDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<AssetTypeAdditionalDetails>(
        offset,
        AssetTypeAdditionalDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readObjectOrNull<Facility>(
        offset,
        FacilitySchema.deserialize,
        allOffsets,
      )) as P;
    case 2:
      return (reader.readObjectOrNull<AssetTypeAdditionalDetails>(
        offset,
        AssetTypeAdditionalDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 3:
      return (reader.readObjectOrNull<AssetTypeAdditionalDetails>(
        offset,
        AssetTypeAdditionalDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension AdditionalDetailsQueryFilter
    on QueryBuilder<AdditionalDetails, AdditionalDetails, QFilterCondition> {
  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      batteryIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'battery',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      batteryIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'battery',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      facilityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facility',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      facilityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facility',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      inverterIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'inverter',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      inverterIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'inverter',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      panelIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'panel',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      panelIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'panel',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'status',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'status',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'status',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'systemCode',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'systemCode',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'systemCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'systemCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'systemCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'systemCode',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'systemCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'systemCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'systemCode',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'systemCode',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'systemCode',
        value: '',
      ));
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      systemCodeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'systemCode',
        value: '',
      ));
    });
  }
}

extension AdditionalDetailsQueryObject
    on QueryBuilder<AdditionalDetails, AdditionalDetails, QFilterCondition> {
  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      battery(FilterQuery<AssetTypeAdditionalDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'battery');
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      facility(FilterQuery<Facility> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'facility');
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      inverter(FilterQuery<AssetTypeAdditionalDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'inverter');
    });
  }

  QueryBuilder<AdditionalDetails, AdditionalDetails, QAfterFilterCondition>
      panel(FilterQuery<AssetTypeAdditionalDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'panel');
    });
  }
}
