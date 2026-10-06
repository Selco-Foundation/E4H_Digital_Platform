// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_activity_facility_bom_values.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheActivityFacilityBomValuesCollection on Isar {
  IsarCollection<CacheActivityFacilityBomValues>
      get cacheActivityFacilityBomValues => this.collection();
}

const CacheActivityFacilityBomValuesSchema = CollectionSchema(
  name: r'CacheActivityFacilityBomValues',
  id: -3550432093938915292,
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
    r'dataJson': PropertySchema(
      id: 2,
      name: r'dataJson',
      type: IsarType.string,
    ),
    r'entryKey': PropertySchema(
      id: 3,
      name: r'entryKey',
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
  estimateSize: _cacheActivityFacilityBomValuesEstimateSize,
  serialize: _cacheActivityFacilityBomValuesSerialize,
  deserialize: _cacheActivityFacilityBomValuesDeserialize,
  deserializeProp: _cacheActivityFacilityBomValuesDeserializeProp,
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
          caseSensitive: false,
        )
      ],
    ),
    r'userType': IndexSchema(
      id: -7871966206036222683,
      name: r'userType',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'userType',
          type: IndexType.hash,
          caseSensitive: false,
        )
      ],
    ),
    r'entryKey': IndexSchema(
      id: 7468454376934395055,
      name: r'entryKey',
      unique: true,
      replace: true,
      properties: [
        IndexPropertySchema(
          name: r'entryKey',
          type: IndexType.hash,
          caseSensitive: false,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheActivityFacilityBomValuesGetId,
  getLinks: _cacheActivityFacilityBomValuesGetLinks,
  attach: _cacheActivityFacilityBomValuesAttach,
  version: '3.1.0+1',
);

int _cacheActivityFacilityBomValuesEstimateSize(
  CacheActivityFacilityBomValues object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  bytesCount += 3 + object.dataJson.length * 3;
  bytesCount += 3 + object.entryKey.length * 3;
  bytesCount += 3 + object.userType.length * 3;
  return bytesCount;
}

void _cacheActivityFacilityBomValuesSerialize(
  CacheActivityFacilityBomValues object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeDateTime(offsets[1], object.createdAt);
  writer.writeString(offsets[2], object.dataJson);
  writer.writeString(offsets[3], object.entryKey);
  writer.writeDateTime(offsets[4], object.updatedAt);
  writer.writeString(offsets[5], object.userType);
}

CacheActivityFacilityBomValues _cacheActivityFacilityBomValuesDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheActivityFacilityBomValues();
  object.activityFacilityId = reader.readString(offsets[0]);
  object.createdAt = reader.readDateTime(offsets[1]);
  object.dataJson = reader.readString(offsets[2]);
  object.entryKey = reader.readString(offsets[3]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[4]);
  object.userType = reader.readString(offsets[5]);
  return object;
}

P _cacheActivityFacilityBomValuesDeserializeProp<P>(
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
      return (reader.readString(offset)) as P;
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

Id _cacheActivityFacilityBomValuesGetId(CacheActivityFacilityBomValues object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheActivityFacilityBomValuesGetLinks(
    CacheActivityFacilityBomValues object) {
  return [];
}

void _cacheActivityFacilityBomValuesAttach(
    IsarCollection<dynamic> col, Id id, CacheActivityFacilityBomValues object) {
  object.id = id;
}

extension CacheActivityFacilityBomValuesByIndex
    on IsarCollection<CacheActivityFacilityBomValues> {
  Future<CacheActivityFacilityBomValues?> getByEntryKey(String entryKey) {
    return getByIndex(r'entryKey', [entryKey]);
  }

  CacheActivityFacilityBomValues? getByEntryKeySync(String entryKey) {
    return getByIndexSync(r'entryKey', [entryKey]);
  }

  Future<bool> deleteByEntryKey(String entryKey) {
    return deleteByIndex(r'entryKey', [entryKey]);
  }

  bool deleteByEntryKeySync(String entryKey) {
    return deleteByIndexSync(r'entryKey', [entryKey]);
  }

  Future<List<CacheActivityFacilityBomValues?>> getAllByEntryKey(
      List<String> entryKeyValues) {
    final values = entryKeyValues.map((e) => [e]).toList();
    return getAllByIndex(r'entryKey', values);
  }

  List<CacheActivityFacilityBomValues?> getAllByEntryKeySync(
      List<String> entryKeyValues) {
    final values = entryKeyValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'entryKey', values);
  }

  Future<int> deleteAllByEntryKey(List<String> entryKeyValues) {
    final values = entryKeyValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'entryKey', values);
  }

  int deleteAllByEntryKeySync(List<String> entryKeyValues) {
    final values = entryKeyValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'entryKey', values);
  }

  Future<Id> putByEntryKey(CacheActivityFacilityBomValues object) {
    return putByIndex(r'entryKey', object);
  }

  Id putByEntryKeySync(CacheActivityFacilityBomValues object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'entryKey', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByEntryKey(
      List<CacheActivityFacilityBomValues> objects) {
    return putAllByIndex(r'entryKey', objects);
  }

  List<Id> putAllByEntryKeySync(List<CacheActivityFacilityBomValues> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'entryKey', objects, saveLinks: saveLinks);
  }
}

extension CacheActivityFacilityBomValuesQueryWhereSort on QueryBuilder<
    CacheActivityFacilityBomValues, CacheActivityFacilityBomValues, QWhere> {
  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheActivityFacilityBomValuesQueryWhere on QueryBuilder<
    CacheActivityFacilityBomValues,
    CacheActivityFacilityBomValues,
    QWhereClause> {
  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
          QAfterWhereClause>
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> userTypeEqualTo(String userType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'userType',
        value: [userType],
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> userTypeNotEqualTo(String userType) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'userType',
              lower: [],
              upper: [userType],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'userType',
              lower: [userType],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'userType',
              lower: [userType],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'userType',
              lower: [],
              upper: [userType],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> entryKeyEqualTo(String entryKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'entryKey',
        value: [entryKey],
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterWhereClause> entryKeyNotEqualTo(String entryKey) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'entryKey',
              lower: [],
              upper: [entryKey],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'entryKey',
              lower: [entryKey],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'entryKey',
              lower: [entryKey],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'entryKey',
              lower: [],
              upper: [entryKey],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CacheActivityFacilityBomValuesQueryFilter on QueryBuilder<
    CacheActivityFacilityBomValues,
    CacheActivityFacilityBomValues,
    QFilterCondition> {
  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'dataJson',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
          QAfterFilterCondition>
      dataJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
          QAfterFilterCondition>
      dataJsonMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'dataJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> dataJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'entryKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'entryKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'entryKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'entryKey',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'entryKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'entryKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
          QAfterFilterCondition>
      entryKeyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'entryKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
          QAfterFilterCondition>
      entryKeyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'entryKey',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'entryKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> entryKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'entryKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
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

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> userTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterFilterCondition> userTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'userType',
        value: '',
      ));
    });
  }
}

extension CacheActivityFacilityBomValuesQueryObject on QueryBuilder<
    CacheActivityFacilityBomValues,
    CacheActivityFacilityBomValues,
    QFilterCondition> {}

extension CacheActivityFacilityBomValuesQueryLinks on QueryBuilder<
    CacheActivityFacilityBomValues,
    CacheActivityFacilityBomValues,
    QFilterCondition> {}

extension CacheActivityFacilityBomValuesQuerySortBy on QueryBuilder<
    CacheActivityFacilityBomValues, CacheActivityFacilityBomValues, QSortBy> {
  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByEntryKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByEntryKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> sortByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheActivityFacilityBomValuesQuerySortThenBy on QueryBuilder<
    CacheActivityFacilityBomValues,
    CacheActivityFacilityBomValues,
    QSortThenBy> {
  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByEntryKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByEntryKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QAfterSortBy> thenByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheActivityFacilityBomValuesQueryWhereDistinct on QueryBuilder<
    CacheActivityFacilityBomValues, CacheActivityFacilityBomValues, QDistinct> {
  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QDistinct> distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QDistinct> distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QDistinct> distinctByDataJson({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'dataJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QDistinct> distinctByEntryKey({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'entryKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, CacheActivityFacilityBomValues,
      QDistinct> distinctByUserType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'userType', caseSensitive: caseSensitive);
    });
  }
}

extension CacheActivityFacilityBomValuesQueryProperty on QueryBuilder<
    CacheActivityFacilityBomValues,
    CacheActivityFacilityBomValues,
    QQueryProperty> {
  QueryBuilder<CacheActivityFacilityBomValues, int, QQueryOperations>
      idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, String, QQueryOperations>
      dataJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'dataJson');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, String, QQueryOperations>
      entryKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'entryKey');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheActivityFacilityBomValues, String, QQueryOperations>
      userTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'userType');
    });
  }
}
