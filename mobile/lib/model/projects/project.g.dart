// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'project.dart';

// **************************************************************************
// IsarEmbeddedGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const ProjectModelSchema = Schema(
  name: r'ProjectModel',
  id: 1822059982794199752,
  properties: {
    r'additionalDetails': PropertySchema(
      id: 0,
      name: r'additionalDetails',
      type: IsarType.object,
      target: r'AdditionalDetails',
    ),
    r'address': PropertySchema(
      id: 1,
      name: r'address',
      type: IsarType.object,
      target: r'AddressModel',
    ),
    r'department': PropertySchema(
      id: 2,
      name: r'department',
      type: IsarType.string,
    ),
    r'description': PropertySchema(
      id: 3,
      name: r'description',
      type: IsarType.string,
    ),
    r'endDate': PropertySchema(
      id: 4,
      name: r'endDate',
      type: IsarType.long,
    ),
    r'endDateTime': PropertySchema(
      id: 5,
      name: r'endDateTime',
      type: IsarType.dateTime,
    ),
    r'id': PropertySchema(
      id: 6,
      name: r'id',
      type: IsarType.string,
    ),
    r'isTaskEnabled': PropertySchema(
      id: 7,
      name: r'isTaskEnabled',
      type: IsarType.bool,
    ),
    r'name': PropertySchema(
      id: 8,
      name: r'name',
      type: IsarType.string,
    ),
    r'nonRecoverableError': PropertySchema(
      id: 9,
      name: r'nonRecoverableError',
      type: IsarType.bool,
    ),
    r'parent': PropertySchema(
      id: 10,
      name: r'parent',
      type: IsarType.string,
    ),
    r'projectHierarchy': PropertySchema(
      id: 11,
      name: r'projectHierarchy',
      type: IsarType.string,
    ),
    r'projectNumber': PropertySchema(
      id: 12,
      name: r'projectNumber',
      type: IsarType.string,
    ),
    r'projectType': PropertySchema(
      id: 13,
      name: r'projectType',
      type: IsarType.string,
    ),
    r'projectTypeId': PropertySchema(
      id: 14,
      name: r'projectTypeId',
      type: IsarType.string,
    ),
    r'referenceId': PropertySchema(
      id: 15,
      name: r'referenceId',
      type: IsarType.string,
    ),
    r'rowVersion': PropertySchema(
      id: 16,
      name: r'rowVersion',
      type: IsarType.long,
    ),
    r'startDate': PropertySchema(
      id: 17,
      name: r'startDate',
      type: IsarType.long,
    ),
    r'startDateTime': PropertySchema(
      id: 18,
      name: r'startDateTime',
      type: IsarType.dateTime,
    ),
    r'subProjectTypeId': PropertySchema(
      id: 19,
      name: r'subProjectTypeId',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 20,
      name: r'tenantId',
      type: IsarType.string,
    )
  },
  estimateSize: _projectModelEstimateSize,
  serialize: _projectModelSerialize,
  deserialize: _projectModelDeserialize,
  deserializeProp: _projectModelDeserializeProp,
);

int _projectModelEstimateSize(
  ProjectModel object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
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
    final value = object.department;
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
  bytesCount += 3 + object.id.length * 3;
  {
    final value = object.name;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.parent;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.projectHierarchy;
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
    final value = object.projectType;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.projectTypeId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.referenceId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.subProjectTypeId;
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

void _projectModelSerialize(
  ProjectModel object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<AdditionalDetails>(
    offsets[0],
    allOffsets,
    AdditionalDetailsSchema.serialize,
    object.additionalDetails,
  );
  writer.writeObject<AddressModel>(
    offsets[1],
    allOffsets,
    AddressModelSchema.serialize,
    object.address,
  );
  writer.writeString(offsets[2], object.department);
  writer.writeString(offsets[3], object.description);
  writer.writeLong(offsets[4], object.endDate);
  writer.writeDateTime(offsets[5], object.endDateTime);
  writer.writeString(offsets[6], object.id);
  writer.writeBool(offsets[7], object.isTaskEnabled);
  writer.writeString(offsets[8], object.name);
  writer.writeBool(offsets[9], object.nonRecoverableError);
  writer.writeString(offsets[10], object.parent);
  writer.writeString(offsets[11], object.projectHierarchy);
  writer.writeString(offsets[12], object.projectNumber);
  writer.writeString(offsets[13], object.projectType);
  writer.writeString(offsets[14], object.projectTypeId);
  writer.writeString(offsets[15], object.referenceId);
  writer.writeLong(offsets[16], object.rowVersion);
  writer.writeLong(offsets[17], object.startDate);
  writer.writeDateTime(offsets[18], object.startDateTime);
  writer.writeString(offsets[19], object.subProjectTypeId);
  writer.writeString(offsets[20], object.tenantId);
}

ProjectModel _projectModelDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = ProjectModel(
    additionalDetails: reader.readObjectOrNull<AdditionalDetails>(
      offsets[0],
      AdditionalDetailsSchema.deserialize,
      allOffsets,
    ),
    address: reader.readObjectOrNull<AddressModel>(
      offsets[1],
      AddressModelSchema.deserialize,
      allOffsets,
    ),
    department: reader.readStringOrNull(offsets[2]),
    description: reader.readStringOrNull(offsets[3]),
    endDate: reader.readLongOrNull(offsets[4]),
    id: reader.readStringOrNull(offsets[6]) ?? '',
    isTaskEnabled: reader.readBoolOrNull(offsets[7]),
    name: reader.readStringOrNull(offsets[8]),
    nonRecoverableError: reader.readBoolOrNull(offsets[9]),
    parent: reader.readStringOrNull(offsets[10]),
    projectHierarchy: reader.readStringOrNull(offsets[11]),
    projectNumber: reader.readStringOrNull(offsets[12]),
    projectType: reader.readStringOrNull(offsets[13]),
    projectTypeId: reader.readStringOrNull(offsets[14]),
    referenceId: reader.readStringOrNull(offsets[15]),
    rowVersion: reader.readLongOrNull(offsets[16]),
    startDate: reader.readLongOrNull(offsets[17]),
    subProjectTypeId: reader.readStringOrNull(offsets[19]),
    tenantId: reader.readStringOrNull(offsets[20]),
  );
  object.endDateTime = reader.readDateTimeOrNull(offsets[5]);
  object.startDateTime = reader.readDateTimeOrNull(offsets[18]);
  return object;
}

P _projectModelDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<AdditionalDetails>(
        offset,
        AdditionalDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readObjectOrNull<AddressModel>(
        offset,
        AddressModelSchema.deserialize,
        allOffsets,
      )) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readLongOrNull(offset)) as P;
    case 5:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset) ?? '') as P;
    case 7:
      return (reader.readBoolOrNull(offset)) as P;
    case 8:
      return (reader.readStringOrNull(offset)) as P;
    case 9:
      return (reader.readBoolOrNull(offset)) as P;
    case 10:
      return (reader.readStringOrNull(offset)) as P;
    case 11:
      return (reader.readStringOrNull(offset)) as P;
    case 12:
      return (reader.readStringOrNull(offset)) as P;
    case 13:
      return (reader.readStringOrNull(offset)) as P;
    case 14:
      return (reader.readStringOrNull(offset)) as P;
    case 15:
      return (reader.readStringOrNull(offset)) as P;
    case 16:
      return (reader.readLongOrNull(offset)) as P;
    case 17:
      return (reader.readLongOrNull(offset)) as P;
    case 18:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 19:
      return (reader.readStringOrNull(offset)) as P;
    case 20:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension ProjectModelQueryFilter
    on QueryBuilder<ProjectModel, ProjectModel, QFilterCondition> {
  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      additionalDetailsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'additionalDetails',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      additionalDetailsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'additionalDetails',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      addressIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'address',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      addressIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'address',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'department',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'department',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'department',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'department',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'department',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'department',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'department',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'department',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'department',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'department',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'department',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      departmentIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'department',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      descriptionIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'description',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      descriptionIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'description',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      descriptionContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'description',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      descriptionMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'description',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      descriptionIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'description',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      descriptionIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'description',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'endDate',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'endDate',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateEqualTo(int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'endDate',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateGreaterThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'endDateTime',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'endDateTime',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateTimeEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'endDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateTimeLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      endDateTimeBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idEqualTo(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idGreaterThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idStartsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idEndsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idContains(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idMatches(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> idIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      idIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'id',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      isTaskEnabledIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'isTaskEnabled',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      isTaskEnabledIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'isTaskEnabled',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      isTaskEnabledEqualTo(bool? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'isTaskEnabled',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'name',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameEqualTo(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nameGreaterThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nameStartsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameEndsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameContains(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> nameMatches(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'name',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nonRecoverableErrorIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'nonRecoverableError',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nonRecoverableErrorIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'nonRecoverableError',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      nonRecoverableErrorEqualTo(bool? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'nonRecoverableError',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'parent',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'parent',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> parentEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'parent',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'parent',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'parent',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> parentBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'parent',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'parent',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'parent',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'parent',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> parentMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'parent',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'parent',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      parentIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'parent',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectHierarchy',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectHierarchy',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectHierarchy',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'projectHierarchy',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'projectHierarchy',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'projectHierarchy',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'projectHierarchy',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'projectHierarchy',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectHierarchy',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectHierarchy',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectHierarchy',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectHierarchyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectHierarchy',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectNumber',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectNumber',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberEqualTo(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberStartsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberEndsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectNumber',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectNumberIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectType',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectType',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeEqualTo(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeGreaterThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeStartsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeEndsWith(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectType',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectType',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'projectTypeId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'projectTypeId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'projectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'projectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'projectTypeId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'projectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'projectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectTypeId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectTypeId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      projectTypeIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectTypeId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'referenceId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'referenceId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'referenceId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'referenceId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'referenceId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'referenceId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'referenceId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'referenceId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'referenceId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'referenceId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'referenceId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      referenceIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'referenceId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      rowVersionIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'rowVersion',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      rowVersionIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'rowVersion',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      rowVersionEqualTo(int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'rowVersion',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'startDate',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'startDate',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateEqualTo(int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'startDate',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateLessThan(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateBetween(
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'startDateTime',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'startDateTime',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      startDateTimeEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'startDateTime',
        value: value,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'subProjectTypeId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'subProjectTypeId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'subProjectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'subProjectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'subProjectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'subProjectTypeId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'subProjectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'subProjectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'subProjectTypeId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'subProjectTypeId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'subProjectTypeId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      subProjectTypeIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'subProjectTypeId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      tenantIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      tenantIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'tenantId',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
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

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      tenantIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      tenantIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }
}

extension ProjectModelQueryObject
    on QueryBuilder<ProjectModel, ProjectModel, QFilterCondition> {
  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition>
      additionalDetails(FilterQuery<AdditionalDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'additionalDetails');
    });
  }

  QueryBuilder<ProjectModel, ProjectModel, QAfterFilterCondition> address(
      FilterQuery<AddressModel> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'address');
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

const AdditionalDetailsSchema = Schema(
  name: r'AdditionalDetails',
  id: -7408414191232501776,
  properties: {
    r'facility': PropertySchema(
      id: 0,
      name: r'facility',
      type: IsarType.object,
      target: r'Facility',
    ),
    r'status': PropertySchema(
      id: 1,
      name: r'status',
      type: IsarType.string,
    ),
    r'systemCode': PropertySchema(
      id: 2,
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
    final value = object.facility;
    if (value != null) {
      bytesCount += 3 +
          FacilitySchema.estimateSize(value, allOffsets[Facility]!, allOffsets);
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
  writer.writeObject<Facility>(
    offsets[0],
    allOffsets,
    FacilitySchema.serialize,
    object.facility,
  );
  writer.writeString(offsets[1], object.status);
  writer.writeString(offsets[2], object.systemCode);
}

AdditionalDetails _additionalDetailsDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = AdditionalDetails();
  object.facility = reader.readObjectOrNull<Facility>(
    offsets[0],
    FacilitySchema.deserialize,
    allOffsets,
  );
  object.status = reader.readStringOrNull(offsets[1]);
  object.systemCode = reader.readStringOrNull(offsets[2]);
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
      return (reader.readObjectOrNull<Facility>(
        offset,
        FacilitySchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension AdditionalDetailsQueryFilter
    on QueryBuilder<AdditionalDetails, AdditionalDetails, QFilterCondition> {
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
      facility(FilterQuery<Facility> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'facility');
    });
  }
}
