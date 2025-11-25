// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_prefilled_activity_facility.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCachePrefilledActivityFacilityCollection on Isar {
  IsarCollection<CachePrefilledActivityFacility>
      get cachePrefilledActivityFacilitys => this.collection();
}

const CachePrefilledActivityFacilitySchema = CollectionSchema(
  name: r'CachePrefilledActivityFacility',
  id: -8921921186662707809,
  properties: {
    r'activityFacilityId': PropertySchema(
      id: 0,
      name: r'activityFacilityId',
      type: IsarType.string,
    ),
    r'createdAt': PropertySchema(
      id: 1,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'updatedAt': PropertySchema(
      id: 2,
      name: r'updatedAt',
      type: IsarType.dateTime,
    ),
    r'userType': PropertySchema(
      id: 3,
      name: r'userType',
      type: IsarType.string,
    )
  },
  estimateSize: _cachePrefilledActivityFacilityEstimateSize,
  serialize: _cachePrefilledActivityFacilitySerialize,
  deserialize: _cachePrefilledActivityFacilityDeserialize,
  deserializeProp: _cachePrefilledActivityFacilityDeserializeProp,
  idName: r'id',
  indexes: {
    r'activityFacilityId_userType': IndexSchema(
      id: -7947145067659777558,
      name: r'activityFacilityId_userType',
      unique: true,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'activityFacilityId',
          type: IndexType.hash,
          caseSensitive: true,
        ),
        IndexPropertySchema(
          name: r'userType',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cachePrefilledActivityFacilityGetId,
  getLinks: _cachePrefilledActivityFacilityGetLinks,
  attach: _cachePrefilledActivityFacilityAttach,
  version: '3.1.0+1',
);

int _cachePrefilledActivityFacilityEstimateSize(
  CachePrefilledActivityFacility object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  bytesCount += 3 + object.userType.length * 3;
  return bytesCount;
}

void _cachePrefilledActivityFacilitySerialize(
  CachePrefilledActivityFacility object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeDateTime(offsets[1], object.createdAt);
  writer.writeDateTime(offsets[2], object.updatedAt);
  writer.writeString(offsets[3], object.userType);
}

CachePrefilledActivityFacility _cachePrefilledActivityFacilityDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CachePrefilledActivityFacility(
    activityFacilityId: reader.readString(offsets[0]),
    userType: reader.readString(offsets[3]),
  );
  object.createdAt = reader.readDateTime(offsets[1]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[2]);
  return object;
}

P _cachePrefilledActivityFacilityDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readString(offset)) as P;
    case 1:
      return (reader.readDateTime(offset)) as P;
    case 2:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cachePrefilledActivityFacilityGetId(CachePrefilledActivityFacility object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cachePrefilledActivityFacilityGetLinks(
    CachePrefilledActivityFacility object) {
  return [];
}

void _cachePrefilledActivityFacilityAttach(
    IsarCollection<dynamic> col, Id id, CachePrefilledActivityFacility object) {
  object.id = id;
}

extension CachePrefilledActivityFacilityByIndex
    on IsarCollection<CachePrefilledActivityFacility> {
  Future<CachePrefilledActivityFacility?> getByActivityFacilityIdUserType(
      String activityFacilityId, String userType) {
    return getByIndex(
        r'activityFacilityId_userType', [activityFacilityId, userType]);
  }

  CachePrefilledActivityFacility? getByActivityFacilityIdUserTypeSync(
      String activityFacilityId, String userType) {
    return getByIndexSync(
        r'activityFacilityId_userType', [activityFacilityId, userType]);
  }

  Future<bool> deleteByActivityFacilityIdUserType(
      String activityFacilityId, String userType) {
    return deleteByIndex(
        r'activityFacilityId_userType', [activityFacilityId, userType]);
  }

  bool deleteByActivityFacilityIdUserTypeSync(
      String activityFacilityId, String userType) {
    return deleteByIndexSync(
        r'activityFacilityId_userType', [activityFacilityId, userType]);
  }

  Future<List<CachePrefilledActivityFacility?>>
      getAllByActivityFacilityIdUserType(
          List<String> activityFacilityIdValues, List<String> userTypeValues) {
    final len = activityFacilityIdValues.length;
    assert(userTypeValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([activityFacilityIdValues[i], userTypeValues[i]]);
    }

    return getAllByIndex(r'activityFacilityId_userType', values);
  }

  List<CachePrefilledActivityFacility?> getAllByActivityFacilityIdUserTypeSync(
      List<String> activityFacilityIdValues, List<String> userTypeValues) {
    final len = activityFacilityIdValues.length;
    assert(userTypeValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([activityFacilityIdValues[i], userTypeValues[i]]);
    }

    return getAllByIndexSync(r'activityFacilityId_userType', values);
  }

  Future<int> deleteAllByActivityFacilityIdUserType(
      List<String> activityFacilityIdValues, List<String> userTypeValues) {
    final len = activityFacilityIdValues.length;
    assert(userTypeValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([activityFacilityIdValues[i], userTypeValues[i]]);
    }

    return deleteAllByIndex(r'activityFacilityId_userType', values);
  }

  int deleteAllByActivityFacilityIdUserTypeSync(
      List<String> activityFacilityIdValues, List<String> userTypeValues) {
    final len = activityFacilityIdValues.length;
    assert(userTypeValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([activityFacilityIdValues[i], userTypeValues[i]]);
    }

    return deleteAllByIndexSync(r'activityFacilityId_userType', values);
  }

  Future<Id> putByActivityFacilityIdUserType(
      CachePrefilledActivityFacility object) {
    return putByIndex(r'activityFacilityId_userType', object);
  }

  Id putByActivityFacilityIdUserTypeSync(CachePrefilledActivityFacility object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'activityFacilityId_userType', object,
        saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByActivityFacilityIdUserType(
      List<CachePrefilledActivityFacility> objects) {
    return putAllByIndex(r'activityFacilityId_userType', objects);
  }

  List<Id> putAllByActivityFacilityIdUserTypeSync(
      List<CachePrefilledActivityFacility> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'activityFacilityId_userType', objects,
        saveLinks: saveLinks);
  }
}

extension CachePrefilledActivityFacilityQueryWhereSort on QueryBuilder<
    CachePrefilledActivityFacility, CachePrefilledActivityFacility, QWhere> {
  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CachePrefilledActivityFacilityQueryWhere on QueryBuilder<
    CachePrefilledActivityFacility,
    CachePrefilledActivityFacility,
    QWhereClause> {
  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterWhereClause> idNotEqualTo(Id id) {
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterWhereClause> idBetween(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterWhereClause>
      activityFacilityIdEqualToAnyUserType(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId_userType',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterWhereClause>
      activityFacilityIdNotEqualToAnyUserType(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [],
              upper: [activityFacilityId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [activityFacilityId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [activityFacilityId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [],
              upper: [activityFacilityId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterWhereClause>
      activityFacilityIdUserTypeEqualTo(
          String activityFacilityId, String userType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId_userType',
        value: [activityFacilityId, userType],
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterWhereClause>
      activityFacilityIdEqualToUserTypeNotEqualTo(
          String activityFacilityId, String userType) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [activityFacilityId],
              upper: [activityFacilityId, userType],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [activityFacilityId, userType],
              includeLower: false,
              upper: [activityFacilityId],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [activityFacilityId, userType],
              includeLower: false,
              upper: [activityFacilityId],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'activityFacilityId_userType',
              lower: [activityFacilityId],
              upper: [activityFacilityId, userType],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CachePrefilledActivityFacilityQueryFilter on QueryBuilder<
    CachePrefilledActivityFacility,
    CachePrefilledActivityFacility,
    QFilterCondition> {
  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterFilterCondition>
      activityFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterFilterCondition>
      activityFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> createdAtBetween(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> idGreaterThan(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> idLessThan(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> idBetween(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> updatedAtBetween(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> userTypeEqualTo(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> userTypeLessThan(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> userTypeBetween(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> userTypeEndsWith(
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

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterFilterCondition>
      userTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
          QAfterFilterCondition>
      userTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'userType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> userTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: '',
      ));
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterFilterCondition> userTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'userType',
        value: '',
      ));
    });
  }
}

extension CachePrefilledActivityFacilityQueryObject on QueryBuilder<
    CachePrefilledActivityFacility,
    CachePrefilledActivityFacility,
    QFilterCondition> {}

extension CachePrefilledActivityFacilityQueryLinks on QueryBuilder<
    CachePrefilledActivityFacility,
    CachePrefilledActivityFacility,
    QFilterCondition> {}

extension CachePrefilledActivityFacilityQuerySortBy on QueryBuilder<
    CachePrefilledActivityFacility, CachePrefilledActivityFacility, QSortBy> {
  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> sortByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CachePrefilledActivityFacilityQuerySortThenBy on QueryBuilder<
    CachePrefilledActivityFacility,
    CachePrefilledActivityFacility,
    QSortThenBy> {
  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QAfterSortBy> thenByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CachePrefilledActivityFacilityQueryWhereDistinct on QueryBuilder<
    CachePrefilledActivityFacility, CachePrefilledActivityFacility, QDistinct> {
  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QDistinct> distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QDistinct> distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, CachePrefilledActivityFacility,
      QDistinct> distinctByUserType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'userType', caseSensitive: caseSensitive);
    });
  }
}

extension CachePrefilledActivityFacilityQueryProperty on QueryBuilder<
    CachePrefilledActivityFacility,
    CachePrefilledActivityFacility,
    QQueryProperty> {
  QueryBuilder<CachePrefilledActivityFacility, int, QQueryOperations>
      idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CachePrefilledActivityFacility, String, QQueryOperations>
      userTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'userType');
    });
  }
}
