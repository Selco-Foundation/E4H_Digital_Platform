// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_unsubmitted_activity_facility.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheUnsubmittedActivityFacilityCollection on Isar {
  IsarCollection<CacheUnsubmittedActivityFacility>
      get cacheUnsubmittedActivityFacilitys => this.collection();
}

const CacheUnsubmittedActivityFacilitySchema = CollectionSchema(
  name: r'CacheUnsubmittedActivityFacility',
  id: -7528482081408684407,
  properties: {
    r'activityFacility': PropertySchema(
      id: 0,
      name: r'activityFacility',
      type: IsarType.object,
      target: r'ActivityFacility',
    ),
    r'activityFacilityId': PropertySchema(
      id: 1,
      name: r'activityFacilityId',
      type: IsarType.string,
    ),
    r'createdAt': PropertySchema(
      id: 2,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'status': PropertySchema(
      id: 3,
      name: r'status',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 4,
      name: r'updatedAt',
      type: IsarType.dateTime,
    ),
    r'userType': PropertySchema(
      id: 5,
      name: r'userType',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheUnsubmittedActivityFacilityEstimateSize,
  serialize: _cacheUnsubmittedActivityFacilitySerialize,
  deserialize: _cacheUnsubmittedActivityFacilityDeserialize,
  deserializeProp: _cacheUnsubmittedActivityFacilityDeserializeProp,
  idName: r'id',
  indexes: {
    r'activityFacilityId': IndexSchema(
      id: -3740981522167357561,
      name: r'activityFacilityId',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'activityFacilityId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'status': IndexSchema(
      id: -107785170620420283,
      name: r'status',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'status',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {
    r'ActivityFacility': ActivityFacilitySchema,
    r'AddressModel': AddressModelSchema,
    r'Facility': FacilitySchema,
    r'FacilityAddress': FacilityAddressSchema,
    r'FacilityDetails': FacilityDetailsSchema
  },
  getId: _cacheUnsubmittedActivityFacilityGetId,
  getLinks: _cacheUnsubmittedActivityFacilityGetLinks,
  attach: _cacheUnsubmittedActivityFacilityAttach,
  version: '3.1.0+1',
);

int _cacheUnsubmittedActivityFacilityEstimateSize(
  CacheUnsubmittedActivityFacility object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 +
      ActivityFacilitySchema.estimateSize(
          object.activityFacility, allOffsets[ActivityFacility]!, allOffsets);
  bytesCount += 3 + object.activityFacilityId.length * 3;
  bytesCount += 3 + object.status.length * 3;
  bytesCount += 3 + object.userType.length * 3;
  return bytesCount;
}

void _cacheUnsubmittedActivityFacilitySerialize(
  CacheUnsubmittedActivityFacility object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<ActivityFacility>(
    offsets[0],
    allOffsets,
    ActivityFacilitySchema.serialize,
    object.activityFacility,
  );
  writer.writeString(offsets[1], object.activityFacilityId);
  writer.writeDateTime(offsets[2], object.createdAt);
  writer.writeString(offsets[3], object.status);
  writer.writeDateTime(offsets[4], object.updatedAt);
  writer.writeString(offsets[5], object.userType);
}

CacheUnsubmittedActivityFacility _cacheUnsubmittedActivityFacilityDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheUnsubmittedActivityFacility(
    activityFacility: reader.readObjectOrNull<ActivityFacility>(
          offsets[0],
          ActivityFacilitySchema.deserialize,
          allOffsets,
        ) ??
        ActivityFacility(),
    activityFacilityId: reader.readString(offsets[1]),
    status: reader.readString(offsets[3]),
    userType: reader.readString(offsets[5]),
  );
  object.createdAt = reader.readDateTime(offsets[2]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[4]);
  return object;
}

P _cacheUnsubmittedActivityFacilityDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<ActivityFacility>(
            offset,
            ActivityFacilitySchema.deserialize,
            allOffsets,
          ) ??
          ActivityFacility()) as P;
    case 1:
      return (reader.readString(offset)) as P;
    case 2:
      return (reader.readDateTime(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    case 4:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheUnsubmittedActivityFacilityGetId(
    CacheUnsubmittedActivityFacility object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheUnsubmittedActivityFacilityGetLinks(
    CacheUnsubmittedActivityFacility object) {
  return [];
}

void _cacheUnsubmittedActivityFacilityAttach(IsarCollection<dynamic> col, Id id,
    CacheUnsubmittedActivityFacility object) {
  object.id = id;
}

extension CacheUnsubmittedActivityFacilityQueryWhereSort on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QWhere> {
  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheUnsubmittedActivityFacilityQueryWhere on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QWhereClause> {
  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterWhereClause> idNotEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(
              IdWhereClause.lessThan(upper: id, includeUpper: false),
            )
            .addWhereClause(
              IdWhereClause.greaterThan(lower: id, includeLower: false),
            );
      } else {
        return query
            .addWhereClause(
              IdWhereClause.greaterThan(lower: id, includeLower: false),
            )
            .addWhereClause(
              IdWhereClause.lessThan(upper: id, includeUpper: false),
            );
      }
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterWhereClause> idBetween(
    Id lowerId,
    Id upperId, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: lowerId,
        includeLower: includeLower,
        upper: upperId,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterWhereClause> activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterWhereClause>
      activityFacilityIdNotEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId',
              lower: [],
              upper: [activityFacilityId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId',
              lower: [activityFacilityId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId',
              lower: [activityFacilityId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId',
              lower: [],
              upper: [activityFacilityId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterWhereClause> statusEqualTo(String status) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'status',
        value: [status],
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterWhereClause> statusNotEqualTo(String status) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'status',
              lower: [],
              upper: [status],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'status',
              lower: [status],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'status',
              lower: [status],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'status',
              lower: [],
              upper: [status],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CacheUnsubmittedActivityFacilityQueryFilter on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QFilterCondition> {
  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'activityFacilityId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterFilterCondition>
      activityFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterFilterCondition>
      activityFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> createdAtGreaterThan(
    DateTime value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> createdAtLessThan(
    DateTime value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> createdAtBetween(
    DateTime lower,
    DateTime upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'createdAt',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> idGreaterThan(
    Id value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> idLessThan(
    Id value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> idBetween(
    Id lower,
    Id upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> statusEqualTo(
    String value, {
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

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> statusGreaterThan(
    String value, {
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

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> statusLessThan(
    String value, {
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

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> statusBetween(
    String lower,
    String upper, {
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

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> statusStartsWith(
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

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> statusEndsWith(
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

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterFilterCondition>
      statusContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterFilterCondition>
      statusMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'status',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> updatedAtGreaterThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> updatedAtLessThan(
    DateTime? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> updatedAtBetween(
    DateTime? lower,
    DateTime? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'updatedAt',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> userTypeEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> userTypeGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> userTypeLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> userTypeBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'userType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> userTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterFilterCondition> userTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterFilterCondition>
      userTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
          CacheUnsubmittedActivityFacility, QAfterFilterCondition>
      userTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'userType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> userTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: '',
      ));
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> userTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'userType',
        value: '',
      ));
    });
  }
}

extension CacheUnsubmittedActivityFacilityQueryObject on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QFilterCondition> {
  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterFilterCondition> activityFacility(FilterQuery<ActivityFacility> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'activityFacility');
    });
  }
}

extension CacheUnsubmittedActivityFacilityQueryLinks on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QFilterCondition> {}

extension CacheUnsubmittedActivityFacilityQuerySortBy on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QSortBy> {
  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterSortBy> sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterSortBy> sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> sortByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheUnsubmittedActivityFacilityQuerySortThenBy on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QSortThenBy> {
  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterSortBy> thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QAfterSortBy> thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QAfterSortBy> thenByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheUnsubmittedActivityFacilityQueryWhereDistinct on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QDistinct> {
  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QDistinct> distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QDistinct> distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QDistinct> distinctByStatus({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'status', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility, QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<
      CacheUnsubmittedActivityFacility,
      CacheUnsubmittedActivityFacility,
      QDistinct> distinctByUserType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'userType', caseSensitive: caseSensitive);
    });
  }
}

extension CacheUnsubmittedActivityFacilityQueryProperty on QueryBuilder<
    CacheUnsubmittedActivityFacility,
    CacheUnsubmittedActivityFacility,
    QQueryProperty> {
  QueryBuilder<CacheUnsubmittedActivityFacility, int, QQueryOperations>
      idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility, ActivityFacility,
      QQueryOperations> activityFacilityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacility');
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility, String, QQueryOperations>
      statusProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'status');
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheUnsubmittedActivityFacility, String, QQueryOperations>
      userTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'userType');
    });
  }
}
