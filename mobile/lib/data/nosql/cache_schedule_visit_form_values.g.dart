// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_schedule_visit_form_values.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheScheduleVisitFormValuesCollection on Isar {
  IsarCollection<CacheScheduleVisitFormValues>
      get cacheScheduleVisitFormValues => this.collection();
}

const CacheScheduleVisitFormValuesSchema = CollectionSchema(
  name: r'CacheScheduleVisitFormValues',
  id: -4373339768396284375,
  properties: {
    r'createdAt': PropertySchema(
      id: 0,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'dataJson': PropertySchema(
      id: 1,
      name: r'dataJson',
      type: IsarType.string,
    ),
    r'entryKey': PropertySchema(
      id: 2,
      name: r'entryKey',
      type: IsarType.string,
    ),
    r'scheduledVisitId': PropertySchema(
      id: 3,
      name: r'scheduledVisitId',
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
  estimateSize: _cacheScheduleVisitFormValuesEstimateSize,
  serialize: _cacheScheduleVisitFormValuesSerialize,
  deserialize: _cacheScheduleVisitFormValuesDeserialize,
  deserializeProp: _cacheScheduleVisitFormValuesDeserializeProp,
  idName: r'id',
  indexes: {
    r'scheduledVisitId': IndexSchema(
      id: -3342719759858217623,
      name: r'scheduledVisitId',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'scheduledVisitId',
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
  getId: _cacheScheduleVisitFormValuesGetId,
  getLinks: _cacheScheduleVisitFormValuesGetLinks,
  attach: _cacheScheduleVisitFormValuesAttach,
  version: '3.1.0+1',
);

int _cacheScheduleVisitFormValuesEstimateSize(
  CacheScheduleVisitFormValues object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.dataJson.length * 3;
  bytesCount += 3 + object.entryKey.length * 3;
  bytesCount += 3 + object.scheduledVisitId.length * 3;
  bytesCount += 3 + object.userType.length * 3;
  return bytesCount;
}

void _cacheScheduleVisitFormValuesSerialize(
  CacheScheduleVisitFormValues object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeDateTime(offsets[0], object.createdAt);
  writer.writeString(offsets[1], object.dataJson);
  writer.writeString(offsets[2], object.entryKey);
  writer.writeString(offsets[3], object.scheduledVisitId);
  writer.writeDateTime(offsets[4], object.updatedAt);
  writer.writeString(offsets[5], object.userType);
}

CacheScheduleVisitFormValues _cacheScheduleVisitFormValuesDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheScheduleVisitFormValues();
  object.createdAt = reader.readDateTime(offsets[0]);
  object.dataJson = reader.readString(offsets[1]);
  object.entryKey = reader.readString(offsets[2]);
  object.id = id;
  object.scheduledVisitId = reader.readString(offsets[3]);
  object.updatedAt = reader.readDateTimeOrNull(offsets[4]);
  object.userType = reader.readString(offsets[5]);
  return object;
}

P _cacheScheduleVisitFormValuesDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readDateTime(offset)) as P;
    case 1:
      return (reader.readString(offset)) as P;
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

Id _cacheScheduleVisitFormValuesGetId(CacheScheduleVisitFormValues object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheScheduleVisitFormValuesGetLinks(
    CacheScheduleVisitFormValues object) {
  return [];
}

void _cacheScheduleVisitFormValuesAttach(
    IsarCollection<dynamic> col, Id id, CacheScheduleVisitFormValues object) {
  object.id = id;
}

extension CacheScheduleVisitFormValuesByIndex
    on IsarCollection<CacheScheduleVisitFormValues> {
  Future<CacheScheduleVisitFormValues?> getByEntryKey(String entryKey) {
    return getByIndex(r'entryKey', [entryKey]);
  }

  CacheScheduleVisitFormValues? getByEntryKeySync(String entryKey) {
    return getByIndexSync(r'entryKey', [entryKey]);
  }

  Future<bool> deleteByEntryKey(String entryKey) {
    return deleteByIndex(r'entryKey', [entryKey]);
  }

  bool deleteByEntryKeySync(String entryKey) {
    return deleteByIndexSync(r'entryKey', [entryKey]);
  }

  Future<List<CacheScheduleVisitFormValues?>> getAllByEntryKey(
      List<String> entryKeyValues) {
    final values = entryKeyValues.map((e) => [e]).toList();
    return getAllByIndex(r'entryKey', values);
  }

  List<CacheScheduleVisitFormValues?> getAllByEntryKeySync(
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

  Future<Id> putByEntryKey(CacheScheduleVisitFormValues object) {
    return putByIndex(r'entryKey', object);
  }

  Id putByEntryKeySync(CacheScheduleVisitFormValues object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'entryKey', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByEntryKey(
      List<CacheScheduleVisitFormValues> objects) {
    return putAllByIndex(r'entryKey', objects);
  }

  List<Id> putAllByEntryKeySync(List<CacheScheduleVisitFormValues> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'entryKey', objects, saveLinks: saveLinks);
  }
}

extension CacheScheduleVisitFormValuesQueryWhereSort on QueryBuilder<
    CacheScheduleVisitFormValues, CacheScheduleVisitFormValues, QWhere> {
  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheScheduleVisitFormValuesQueryWhere on QueryBuilder<
    CacheScheduleVisitFormValues, CacheScheduleVisitFormValues, QWhereClause> {
  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> scheduledVisitIdEqualTo(String scheduledVisitId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'scheduledVisitId',
        value: [scheduledVisitId],
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> scheduledVisitIdNotEqualTo(String scheduledVisitId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduledVisitId',
              lower: [],
              upper: [scheduledVisitId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduledVisitId',
              lower: [scheduledVisitId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduledVisitId',
              lower: [scheduledVisitId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduledVisitId',
              lower: [],
              upper: [scheduledVisitId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> userTypeEqualTo(String userType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'userType',
        value: [userType],
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterWhereClause> entryKeyEqualTo(String entryKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'entryKey',
        value: [entryKey],
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

extension CacheScheduleVisitFormValuesQueryFilter on QueryBuilder<
    CacheScheduleVisitFormValues,
    CacheScheduleVisitFormValues,
    QFilterCondition> {
  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> dataJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> dataJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> entryKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'entryKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> entryKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'entryKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'scheduledVisitId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
          QAfterFilterCondition>
      scheduledVisitIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
          QAfterFilterCondition>
      scheduledVisitIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'scheduledVisitId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduledVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> scheduledVisitIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'scheduledVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
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

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> userTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterFilterCondition> userTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'userType',
        value: '',
      ));
    });
  }
}

extension CacheScheduleVisitFormValuesQueryObject on QueryBuilder<
    CacheScheduleVisitFormValues,
    CacheScheduleVisitFormValues,
    QFilterCondition> {}

extension CacheScheduleVisitFormValuesQueryLinks on QueryBuilder<
    CacheScheduleVisitFormValues,
    CacheScheduleVisitFormValues,
    QFilterCondition> {}

extension CacheScheduleVisitFormValuesQuerySortBy on QueryBuilder<
    CacheScheduleVisitFormValues, CacheScheduleVisitFormValues, QSortBy> {
  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByEntryKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByEntryKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByScheduledVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByScheduledVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> sortByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheScheduleVisitFormValuesQuerySortThenBy on QueryBuilder<
    CacheScheduleVisitFormValues, CacheScheduleVisitFormValues, QSortThenBy> {
  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByEntryKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByEntryKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'entryKey', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByScheduledVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByScheduledVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QAfterSortBy> thenByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheScheduleVisitFormValuesQueryWhereDistinct on QueryBuilder<
    CacheScheduleVisitFormValues, CacheScheduleVisitFormValues, QDistinct> {
  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QDistinct> distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QDistinct> distinctByDataJson({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'dataJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QDistinct> distinctByEntryKey({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'entryKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QDistinct> distinctByScheduledVisitId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'scheduledVisitId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, CacheScheduleVisitFormValues,
      QDistinct> distinctByUserType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'userType', caseSensitive: caseSensitive);
    });
  }
}

extension CacheScheduleVisitFormValuesQueryProperty on QueryBuilder<
    CacheScheduleVisitFormValues,
    CacheScheduleVisitFormValues,
    QQueryProperty> {
  QueryBuilder<CacheScheduleVisitFormValues, int, QQueryOperations>
      idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, String, QQueryOperations>
      dataJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'dataJson');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, String, QQueryOperations>
      entryKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'entryKey');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, String, QQueryOperations>
      scheduledVisitIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'scheduledVisitId');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheScheduleVisitFormValues, String, QQueryOperations>
      userTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'userType');
    });
  }
}
