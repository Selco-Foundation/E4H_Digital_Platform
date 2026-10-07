// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_assessment_queue.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAssessmentQueueCollection on Isar {
  IsarCollection<CacheAssessmentQueue> get cacheAssessmentQueues =>
      this.collection();
}

const CacheAssessmentQueueSchema = CollectionSchema(
  name: r'CacheAssessmentQueue',
  id: 4419302095059376638,
  properties: {
    r'assessorId': PropertySchema(
      id: 0,
      name: r'assessorId',
      type: IsarType.string,
    ),
    r'cacheKey': PropertySchema(
      id: 1,
      name: r'cacheKey',
      type: IsarType.string,
    ),
    r'facilityJson': PropertySchema(
      id: 2,
      name: r'facilityJson',
      type: IsarType.string,
    ),
    r'facilityName': PropertySchema(
      id: 3,
      name: r'facilityName',
      type: IsarType.string,
    ),
    r'lastActionTime': PropertySchema(
      id: 4,
      name: r'lastActionTime',
      type: IsarType.long,
    ),
    r'phase': PropertySchema(
      id: 5,
      name: r'phase',
      type: IsarType.string,
    ),
    r'planFacilityId': PropertySchema(
      id: 6,
      name: r'planFacilityId',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 7,
      name: r'tenantId',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheAssessmentQueueEstimateSize,
  serialize: _cacheAssessmentQueueSerialize,
  deserialize: _cacheAssessmentQueueDeserialize,
  deserializeProp: _cacheAssessmentQueueDeserializeProp,
  idName: r'id',
  indexes: {
    r'cacheKey': IndexSchema(
      id: 5885332021012296610,
      name: r'cacheKey',
      unique: true,
      replace: true,
      properties: [
        IndexPropertySchema(
          name: r'cacheKey',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'tenantId': IndexSchema(
      id: -1042425927805315167,
      name: r'tenantId',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'tenantId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'assessorId': IndexSchema(
      id: -652810048622272529,
      name: r'assessorId',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'assessorId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'phase': IndexSchema(
      id: -467877781735009358,
      name: r'phase',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'phase',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheAssessmentQueueGetId,
  getLinks: _cacheAssessmentQueueGetLinks,
  attach: _cacheAssessmentQueueAttach,
  version: '3.1.0+1',
);

int _cacheAssessmentQueueEstimateSize(
  CacheAssessmentQueue object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.assessorId.length * 3;
  bytesCount += 3 + object.cacheKey.length * 3;
  bytesCount += 3 + object.facilityJson.length * 3;
  bytesCount += 3 + object.facilityName.length * 3;
  bytesCount += 3 + object.phase.length * 3;
  bytesCount += 3 + object.planFacilityId.length * 3;
  bytesCount += 3 + object.tenantId.length * 3;
  return bytesCount;
}

void _cacheAssessmentQueueSerialize(
  CacheAssessmentQueue object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.assessorId);
  writer.writeString(offsets[1], object.cacheKey);
  writer.writeString(offsets[2], object.facilityJson);
  writer.writeString(offsets[3], object.facilityName);
  writer.writeLong(offsets[4], object.lastActionTime);
  writer.writeString(offsets[5], object.phase);
  writer.writeString(offsets[6], object.planFacilityId);
  writer.writeString(offsets[7], object.tenantId);
}

CacheAssessmentQueue _cacheAssessmentQueueDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAssessmentQueue(
    assessorId: reader.readString(offsets[0]),
    cacheKey: reader.readString(offsets[1]),
    facilityJson: reader.readString(offsets[2]),
    facilityName: reader.readString(offsets[3]),
    lastActionTime: reader.readLongOrNull(offsets[4]),
    phase: reader.readString(offsets[5]),
    planFacilityId: reader.readString(offsets[6]),
    tenantId: reader.readString(offsets[7]),
  );
  object.id = id;
  return object;
}

P _cacheAssessmentQueueDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readString(offset)) as P;
    case 1:
      return (reader.readString(offset)) as P;
    case 2:
      return (reader.readString(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    case 4:
      return (reader.readLongOrNull(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    case 6:
      return (reader.readString(offset)) as P;
    case 7:
      return (reader.readString(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAssessmentQueueGetId(CacheAssessmentQueue object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAssessmentQueueGetLinks(
    CacheAssessmentQueue object) {
  return [];
}

void _cacheAssessmentQueueAttach(
    IsarCollection<dynamic> col, Id id, CacheAssessmentQueue object) {
  object.id = id;
}

extension CacheAssessmentQueueByIndex on IsarCollection<CacheAssessmentQueue> {
  Future<CacheAssessmentQueue?> getByCacheKey(String cacheKey) {
    return getByIndex(r'cacheKey', [cacheKey]);
  }

  CacheAssessmentQueue? getByCacheKeySync(String cacheKey) {
    return getByIndexSync(r'cacheKey', [cacheKey]);
  }

  Future<bool> deleteByCacheKey(String cacheKey) {
    return deleteByIndex(r'cacheKey', [cacheKey]);
  }

  bool deleteByCacheKeySync(String cacheKey) {
    return deleteByIndexSync(r'cacheKey', [cacheKey]);
  }

  Future<List<CacheAssessmentQueue?>> getAllByCacheKey(
      List<String> cacheKeyValues) {
    final values = cacheKeyValues.map((e) => [e]).toList();
    return getAllByIndex(r'cacheKey', values);
  }

  List<CacheAssessmentQueue?> getAllByCacheKeySync(
      List<String> cacheKeyValues) {
    final values = cacheKeyValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'cacheKey', values);
  }

  Future<int> deleteAllByCacheKey(List<String> cacheKeyValues) {
    final values = cacheKeyValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'cacheKey', values);
  }

  int deleteAllByCacheKeySync(List<String> cacheKeyValues) {
    final values = cacheKeyValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'cacheKey', values);
  }

  Future<Id> putByCacheKey(CacheAssessmentQueue object) {
    return putByIndex(r'cacheKey', object);
  }

  Id putByCacheKeySync(CacheAssessmentQueue object, {bool saveLinks = true}) {
    return putByIndexSync(r'cacheKey', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByCacheKey(List<CacheAssessmentQueue> objects) {
    return putAllByIndex(r'cacheKey', objects);
  }

  List<Id> putAllByCacheKeySync(List<CacheAssessmentQueue> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'cacheKey', objects, saveLinks: saveLinks);
  }
}

extension CacheAssessmentQueueQueryWhereSort
    on QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QWhere> {
  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhere>
      anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheAssessmentQueueQueryWhere
    on QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QWhereClause> {
  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      idNotEqualTo(Id id) {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      idBetween(
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      cacheKeyEqualTo(String cacheKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'cacheKey',
        value: [cacheKey],
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      cacheKeyNotEqualTo(String cacheKey) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'cacheKey',
              lower: [],
              upper: [cacheKey],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'cacheKey',
              lower: [cacheKey],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'cacheKey',
              lower: [cacheKey],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'cacheKey',
              lower: [],
              upper: [cacheKey],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      tenantIdEqualTo(String tenantId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'tenantId',
        value: [tenantId],
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      tenantIdNotEqualTo(String tenantId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'tenantId',
              lower: [],
              upper: [tenantId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'tenantId',
              lower: [tenantId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'tenantId',
              lower: [tenantId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'tenantId',
              lower: [],
              upper: [tenantId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      assessorIdEqualTo(String assessorId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'assessorId',
        value: [assessorId],
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      assessorIdNotEqualTo(String assessorId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assessorId',
              lower: [],
              upper: [assessorId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assessorId',
              lower: [assessorId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assessorId',
              lower: [assessorId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assessorId',
              lower: [],
              upper: [assessorId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      phaseEqualTo(String phase) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'phase',
        value: [phase],
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterWhereClause>
      phaseNotEqualTo(String phase) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'phase',
              lower: [],
              upper: [phase],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'phase',
              lower: [phase],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'phase',
              lower: [phase],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'phase',
              lower: [],
              upper: [phase],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CacheAssessmentQueueQueryFilter on QueryBuilder<CacheAssessmentQueue,
    CacheAssessmentQueue, QFilterCondition> {
  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assessorId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'assessorId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'assessorId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'assessorId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'assessorId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'assessorId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      assessorIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assessorId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      assessorIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assessorId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assessorId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> assessorIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assessorId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'cacheKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'cacheKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'cacheKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'cacheKey',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'cacheKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'cacheKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      cacheKeyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'cacheKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      cacheKeyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'cacheKey',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'cacheKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> cacheKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'cacheKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityJson',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      facilityJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      facilityJsonMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameEqualTo(
    String value, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameGreaterThan(
    String value, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameLessThan(
    String value, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameBetween(
    String lower,
    String upper, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameStartsWith(
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameEndsWith(
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      facilityNameContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      facilityNameMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> facilityNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> lastActionTimeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'lastActionTime',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> lastActionTimeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'lastActionTime',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> lastActionTimeEqualTo(int? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'lastActionTime',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> lastActionTimeGreaterThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'lastActionTime',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> lastActionTimeLessThan(
    int? value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'lastActionTime',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> lastActionTimeBetween(
    int? lower,
    int? upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'lastActionTime',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'phase',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'phase',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'phase',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'phase',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'phase',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'phase',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      phaseContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'phase',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      phaseMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'phase',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'phase',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> phaseIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'phase',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'planFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'planFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'planFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'planFacilityId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'planFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'planFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      planFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'planFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      planFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'planFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'planFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> planFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'planFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdEqualTo(
    String value, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdGreaterThan(
    String value, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdLessThan(
    String value, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdBetween(
    String lower,
    String upper, {
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdStartsWith(
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdEndsWith(
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

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      tenantIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
          QAfterFilterCondition>
      tenantIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'tenantId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue,
      QAfterFilterCondition> tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }
}

extension CacheAssessmentQueueQueryObject on QueryBuilder<CacheAssessmentQueue,
    CacheAssessmentQueue, QFilterCondition> {}

extension CacheAssessmentQueueQueryLinks on QueryBuilder<CacheAssessmentQueue,
    CacheAssessmentQueue, QFilterCondition> {}

extension CacheAssessmentQueueQuerySortBy
    on QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QSortBy> {
  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByAssessorId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByAssessorIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByCacheKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'cacheKey', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByCacheKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'cacheKey', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByFacilityJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByFacilityJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByFacilityName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByFacilityNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByLastActionTime() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastActionTime', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByLastActionTimeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastActionTime', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByPhase() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByPhaseDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByPlanFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByPlanFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      sortByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }
}

extension CacheAssessmentQueueQuerySortThenBy
    on QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QSortThenBy> {
  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByAssessorId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByAssessorIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByCacheKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'cacheKey', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByCacheKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'cacheKey', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByFacilityJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByFacilityJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByFacilityName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByFacilityNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByLastActionTime() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastActionTime', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByLastActionTimeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastActionTime', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByPhase() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByPhaseDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByPlanFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByPlanFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QAfterSortBy>
      thenByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }
}

extension CacheAssessmentQueueQueryWhereDistinct
    on QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct> {
  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByAssessorId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assessorId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByCacheKey({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'cacheKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByFacilityJson({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByFacilityName({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityName', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByLastActionTime() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'lastActionTime');
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByPhase({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'phase', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByPlanFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'planFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentQueue, CacheAssessmentQueue, QDistinct>
      distinctByTenantId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'tenantId', caseSensitive: caseSensitive);
    });
  }
}

extension CacheAssessmentQueueQueryProperty on QueryBuilder<
    CacheAssessmentQueue, CacheAssessmentQueue, QQueryProperty> {
  QueryBuilder<CacheAssessmentQueue, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations>
      assessorIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assessorId');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations>
      cacheKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'cacheKey');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations>
      facilityJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityJson');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations>
      facilityNameProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityName');
    });
  }

  QueryBuilder<CacheAssessmentQueue, int?, QQueryOperations>
      lastActionTimeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'lastActionTime');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations> phaseProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'phase');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations>
      planFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'planFacilityId');
    });
  }

  QueryBuilder<CacheAssessmentQueue, String, QQueryOperations>
      tenantIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'tenantId');
    });
  }
}
