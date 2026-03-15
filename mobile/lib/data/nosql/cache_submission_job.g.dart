// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_submission_job.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheSubmissionJobCollection on Isar {
  IsarCollection<CacheSubmissionJob> get cacheSubmissionJobs =>
      this.collection();
}

const CacheSubmissionJobSchema = CollectionSchema(
  name: r'CacheSubmissionJob',
  id: 6232009367466659378,
  properties: {
    r'activityFacilityId': PropertySchema(
      id: 0,
      name: r'activityFacilityId',
      type: IsarType.string,
    ),
    r'completedSteps': PropertySchema(
      id: 1,
      name: r'completedSteps',
      type: IsarType.long,
    ),
    r'isBlocking': PropertySchema(
      id: 2,
      name: r'isBlocking',
      type: IsarType.bool,
    ),
    r'lastError': PropertySchema(
      id: 3,
      name: r'lastError',
      type: IsarType.string,
    ),
    r'operationType': PropertySchema(
      id: 4,
      name: r'operationType',
      type: IsarType.string,
    ),
    r'progressPercent': PropertySchema(
      id: 5,
      name: r'progressPercent',
      type: IsarType.long,
    ),
    r'retryCount': PropertySchema(
      id: 6,
      name: r'retryCount',
      type: IsarType.long,
    ),
    r'stageKey': PropertySchema(
      id: 7,
      name: r'stageKey',
      type: IsarType.string,
    ),
    r'stageLabel': PropertySchema(
      id: 8,
      name: r'stageLabel',
      type: IsarType.string,
    ),
    r'status': PropertySchema(
      id: 9,
      name: r'status',
      type: IsarType.string,
    ),
    r'totalSteps': PropertySchema(
      id: 10,
      name: r'totalSteps',
      type: IsarType.long,
    ),
    r'updatedAt': PropertySchema(
      id: 11,
      name: r'updatedAt',
      type: IsarType.dateTime,
    )
  },
  estimateSize: _cacheSubmissionJobEstimateSize,
  serialize: _cacheSubmissionJobSerialize,
  deserialize: _cacheSubmissionJobDeserialize,
  deserializeProp: _cacheSubmissionJobDeserializeProp,
  idName: r'id',
  indexes: {
    r'activityFacilityId': IndexSchema(
      id: -3740981522167357561,
      name: r'activityFacilityId',
      unique: true,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'activityFacilityId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'operationType': IndexSchema(
      id: 7940488376024458150,
      name: r'operationType',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'operationType',
          type: IndexType.hash,
          caseSensitive: false,
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
    ),
    r'stageKey': IndexSchema(
      id: 907655490056677013,
      name: r'stageKey',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'stageKey',
          type: IndexType.hash,
          caseSensitive: false,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheSubmissionJobGetId,
  getLinks: _cacheSubmissionJobGetLinks,
  attach: _cacheSubmissionJobAttach,
  version: '3.1.0+1',
);

int _cacheSubmissionJobEstimateSize(
  CacheSubmissionJob object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  {
    final value = object.lastError;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.operationType.length * 3;
  bytesCount += 3 + object.stageKey.length * 3;
  bytesCount += 3 + object.stageLabel.length * 3;
  bytesCount += 3 + object.status.length * 3;
  return bytesCount;
}

void _cacheSubmissionJobSerialize(
  CacheSubmissionJob object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeLong(offsets[1], object.completedSteps);
  writer.writeBool(offsets[2], object.isBlocking);
  writer.writeString(offsets[3], object.lastError);
  writer.writeString(offsets[4], object.operationType);
  writer.writeLong(offsets[5], object.progressPercent);
  writer.writeLong(offsets[6], object.retryCount);
  writer.writeString(offsets[7], object.stageKey);
  writer.writeString(offsets[8], object.stageLabel);
  writer.writeString(offsets[9], object.status);
  writer.writeLong(offsets[10], object.totalSteps);
  writer.writeDateTime(offsets[11], object.updatedAt);
}

CacheSubmissionJob _cacheSubmissionJobDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheSubmissionJob(
    activityFacilityId: reader.readString(offsets[0]),
    completedSteps: reader.readLongOrNull(offsets[1]) ?? 0,
    isBlocking: reader.readBoolOrNull(offsets[2]) ?? true,
    lastError: reader.readStringOrNull(offsets[3]),
    operationType: reader.readString(offsets[4]),
    progressPercent: reader.readLongOrNull(offsets[5]) ?? 0,
    retryCount: reader.readLongOrNull(offsets[6]) ?? 0,
    stageKey: reader.readString(offsets[7]),
    stageLabel: reader.readString(offsets[8]),
    status: reader.readString(offsets[9]),
    totalSteps: reader.readLongOrNull(offsets[10]) ?? 1,
  );
  object.id = id;
  object.updatedAt = reader.readDateTime(offsets[11]);
  return object;
}

P _cacheSubmissionJobDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readString(offset)) as P;
    case 1:
      return (reader.readLongOrNull(offset) ?? 0) as P;
    case 2:
      return (reader.readBoolOrNull(offset) ?? true) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readString(offset)) as P;
    case 5:
      return (reader.readLongOrNull(offset) ?? 0) as P;
    case 6:
      return (reader.readLongOrNull(offset) ?? 0) as P;
    case 7:
      return (reader.readString(offset)) as P;
    case 8:
      return (reader.readString(offset)) as P;
    case 9:
      return (reader.readString(offset)) as P;
    case 10:
      return (reader.readLongOrNull(offset) ?? 1) as P;
    case 11:
      return (reader.readDateTime(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheSubmissionJobGetId(CacheSubmissionJob object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheSubmissionJobGetLinks(
    CacheSubmissionJob object) {
  return [];
}

void _cacheSubmissionJobAttach(
    IsarCollection<dynamic> col, Id id, CacheSubmissionJob object) {
  object.id = id;
}

extension CacheSubmissionJobByIndex on IsarCollection<CacheSubmissionJob> {
  Future<CacheSubmissionJob?> getByActivityFacilityId(
      String activityFacilityId) {
    return getByIndex(r'activityFacilityId', [activityFacilityId]);
  }

  CacheSubmissionJob? getByActivityFacilityIdSync(String activityFacilityId) {
    return getByIndexSync(r'activityFacilityId', [activityFacilityId]);
  }

  Future<bool> deleteByActivityFacilityId(String activityFacilityId) {
    return deleteByIndex(r'activityFacilityId', [activityFacilityId]);
  }

  bool deleteByActivityFacilityIdSync(String activityFacilityId) {
    return deleteByIndexSync(r'activityFacilityId', [activityFacilityId]);
  }

  Future<List<CacheSubmissionJob?>> getAllByActivityFacilityId(
      List<String> activityFacilityIdValues) {
    final values = activityFacilityIdValues.map((e) => [e]).toList();
    return getAllByIndex(r'activityFacilityId', values);
  }

  List<CacheSubmissionJob?> getAllByActivityFacilityIdSync(
      List<String> activityFacilityIdValues) {
    final values = activityFacilityIdValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'activityFacilityId', values);
  }

  Future<int> deleteAllByActivityFacilityId(
      List<String> activityFacilityIdValues) {
    final values = activityFacilityIdValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'activityFacilityId', values);
  }

  int deleteAllByActivityFacilityIdSync(List<String> activityFacilityIdValues) {
    final values = activityFacilityIdValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'activityFacilityId', values);
  }

  Future<Id> putByActivityFacilityId(CacheSubmissionJob object) {
    return putByIndex(r'activityFacilityId', object);
  }

  Id putByActivityFacilityIdSync(CacheSubmissionJob object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'activityFacilityId', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByActivityFacilityId(
      List<CacheSubmissionJob> objects) {
    return putAllByIndex(r'activityFacilityId', objects);
  }

  List<Id> putAllByActivityFacilityIdSync(List<CacheSubmissionJob> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'activityFacilityId', objects,
        saveLinks: saveLinks);
  }
}

extension CacheSubmissionJobQueryWhereSort
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QWhere> {
  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheSubmissionJobQueryWhere
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QWhereClause> {
  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      operationTypeEqualTo(String operationType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'operationType',
        value: [operationType],
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      operationTypeNotEqualTo(String operationType) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'operationType',
              lower: [],
              upper: [operationType],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'operationType',
              lower: [operationType],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'operationType',
              lower: [operationType],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'operationType',
              lower: [],
              upper: [operationType],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      statusEqualTo(String status) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'status',
        value: [status],
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      statusNotEqualTo(String status) {
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      stageKeyEqualTo(String stageKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'stageKey',
        value: [stageKey],
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterWhereClause>
      stageKeyNotEqualTo(String stageKey) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'stageKey',
              lower: [],
              upper: [stageKey],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'stageKey',
              lower: [stageKey],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'stageKey',
              lower: [stageKey],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'stageKey',
              lower: [],
              upper: [stageKey],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CacheSubmissionJobQueryFilter
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QFilterCondition> {
  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdEqualTo(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdGreaterThan(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdLessThan(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdBetween(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdStartsWith(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdEndsWith(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      completedStepsEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'completedSteps',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      completedStepsGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'completedSteps',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      completedStepsLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'completedSteps',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      completedStepsBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'completedSteps',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      idGreaterThan(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      idLessThan(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      idBetween(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      isBlockingEqualTo(bool value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'isBlocking',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'lastError',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'lastError',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'lastError',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'lastError',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'lastError',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      lastErrorIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'lastError',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'operationType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'operationType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'operationType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'operationType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'operationType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'operationType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'operationType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'operationType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'operationType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      operationTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'operationType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      progressPercentEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'progressPercent',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      progressPercentGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'progressPercent',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      progressPercentLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'progressPercent',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      progressPercentBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'progressPercent',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      retryCountEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'retryCount',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      retryCountGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'retryCount',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      retryCountLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'retryCount',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      retryCountBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'retryCount',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'stageKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'stageKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'stageKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'stageKey',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'stageKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'stageKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'stageKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'stageKey',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'stageKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'stageKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'stageLabel',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'stageLabel',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'stageLabel',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'stageLabel',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'stageLabel',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'stageLabel',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'stageLabel',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'stageLabel',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'stageLabel',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      stageLabelIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'stageLabel',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusEqualTo(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusGreaterThan(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusLessThan(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusBetween(
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'status',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'status',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      totalStepsEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'totalSteps',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      totalStepsGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'totalSteps',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      totalStepsLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'totalSteps',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      totalStepsBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'totalSteps',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      updatedAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      updatedAtGreaterThan(
    DateTime value, {
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      updatedAtLessThan(
    DateTime value, {
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

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterFilterCondition>
      updatedAtBetween(
    DateTime lower,
    DateTime upper, {
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
}

extension CacheSubmissionJobQueryObject
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QFilterCondition> {}

extension CacheSubmissionJobQueryLinks
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QFilterCondition> {}

extension CacheSubmissionJobQuerySortBy
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QSortBy> {
  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByCompletedSteps() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'completedSteps', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByCompletedStepsDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'completedSteps', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByIsBlocking() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isBlocking', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByIsBlockingDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isBlocking', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByLastError() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByLastErrorDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByOperationType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'operationType', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByOperationTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'operationType', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByProgressPercent() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progressPercent', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByProgressPercentDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progressPercent', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByRetryCount() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'retryCount', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByRetryCountDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'retryCount', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByStageKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageKey', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByStageKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageKey', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByStageLabel() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageLabel', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByStageLabelDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageLabel', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByTotalSteps() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalSteps', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByTotalStepsDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalSteps', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheSubmissionJobQuerySortThenBy
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QSortThenBy> {
  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByCompletedSteps() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'completedSteps', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByCompletedStepsDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'completedSteps', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByIsBlocking() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isBlocking', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByIsBlockingDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isBlocking', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByLastError() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByLastErrorDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByOperationType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'operationType', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByOperationTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'operationType', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByProgressPercent() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progressPercent', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByProgressPercentDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progressPercent', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByRetryCount() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'retryCount', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByRetryCountDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'retryCount', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByStageKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageKey', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByStageKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageKey', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByStageLabel() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageLabel', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByStageLabelDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'stageLabel', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByTotalSteps() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalSteps', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByTotalStepsDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalSteps', Sort.desc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QAfterSortBy>
      thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheSubmissionJobQueryWhereDistinct
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct> {
  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByCompletedSteps() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'completedSteps');
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByIsBlocking() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'isBlocking');
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByLastError({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'lastError', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByOperationType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'operationType',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByProgressPercent() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'progressPercent');
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByRetryCount() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'retryCount');
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByStageKey({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'stageKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByStageLabel({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'stageLabel', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByStatus({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'status', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByTotalSteps() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'totalSteps');
    });
  }

  QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QDistinct>
      distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheSubmissionJobQueryProperty
    on QueryBuilder<CacheSubmissionJob, CacheSubmissionJob, QQueryProperty> {
  QueryBuilder<CacheSubmissionJob, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheSubmissionJob, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheSubmissionJob, int, QQueryOperations>
      completedStepsProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'completedSteps');
    });
  }

  QueryBuilder<CacheSubmissionJob, bool, QQueryOperations>
      isBlockingProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'isBlocking');
    });
  }

  QueryBuilder<CacheSubmissionJob, String?, QQueryOperations>
      lastErrorProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'lastError');
    });
  }

  QueryBuilder<CacheSubmissionJob, String, QQueryOperations>
      operationTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'operationType');
    });
  }

  QueryBuilder<CacheSubmissionJob, int, QQueryOperations>
      progressPercentProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'progressPercent');
    });
  }

  QueryBuilder<CacheSubmissionJob, int, QQueryOperations> retryCountProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'retryCount');
    });
  }

  QueryBuilder<CacheSubmissionJob, String, QQueryOperations>
      stageKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'stageKey');
    });
  }

  QueryBuilder<CacheSubmissionJob, String, QQueryOperations>
      stageLabelProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'stageLabel');
    });
  }

  QueryBuilder<CacheSubmissionJob, String, QQueryOperations> statusProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'status');
    });
  }

  QueryBuilder<CacheSubmissionJob, int, QQueryOperations> totalStepsProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'totalSteps');
    });
  }

  QueryBuilder<CacheSubmissionJob, DateTime, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }
}
