// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_assessment_draft.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAssessmentDraftCollection on Isar {
  IsarCollection<CacheAssessmentDraft> get cacheAssessmentDrafts =>
      this.collection();
}

const CacheAssessmentDraftSchema = CollectionSchema(
  name: r'CacheAssessmentDraft',
  id: -8401842128441499391,
  properties: {
    r'assessorId': PropertySchema(
      id: 0,
      name: r'assessorId',
      type: IsarType.string,
    ),
    r'attemptCount': PropertySchema(
      id: 1,
      name: r'attemptCount',
      type: IsarType.long,
    ),
    r'block': PropertySchema(
      id: 2,
      name: r'block',
      type: IsarType.string,
    ),
    r'createdAt': PropertySchema(
      id: 3,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'district': PropertySchema(
      id: 4,
      name: r'district',
      type: IsarType.string,
    ),
    r'draftKey': PropertySchema(
      id: 5,
      name: r'draftKey',
      type: IsarType.string,
    ),
    r'facilityDefaultsJson': PropertySchema(
      id: 6,
      name: r'facilityDefaultsJson',
      type: IsarType.string,
    ),
    r'facilityName': PropertySchema(
      id: 7,
      name: r'facilityName',
      type: IsarType.string,
    ),
    r'facilityType': PropertySchema(
      id: 8,
      name: r'facilityType',
      type: IsarType.string,
    ),
    r'lastError': PropertySchema(
      id: 9,
      name: r'lastError',
      type: IsarType.string,
    ),
    r'phase': PropertySchema(
      id: 10,
      name: r'phase',
      type: IsarType.string,
    ),
    r'planFacilityId': PropertySchema(
      id: 11,
      name: r'planFacilityId',
      type: IsarType.string,
    ),
    r'requestJson': PropertySchema(
      id: 12,
      name: r'requestJson',
      type: IsarType.string,
    ),
    r'state': PropertySchema(
      id: 13,
      name: r'state',
      type: IsarType.string,
    ),
    r'status': PropertySchema(
      id: 14,
      name: r'status',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 15,
      name: r'tenantId',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 16,
      name: r'updatedAt',
      type: IsarType.dateTime,
    )
  },
  estimateSize: _cacheAssessmentDraftEstimateSize,
  serialize: _cacheAssessmentDraftSerialize,
  deserialize: _cacheAssessmentDraftDeserialize,
  deserializeProp: _cacheAssessmentDraftDeserializeProp,
  idName: r'id',
  indexes: {
    r'draftKey': IndexSchema(
      id: -6531847789214907499,
      name: r'draftKey',
      unique: true,
      replace: true,
      properties: [
        IndexPropertySchema(
          name: r'draftKey',
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
          caseSensitive: false,
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
          caseSensitive: false,
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
          caseSensitive: false,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheAssessmentDraftGetId,
  getLinks: _cacheAssessmentDraftGetLinks,
  attach: _cacheAssessmentDraftAttach,
  version: '3.1.0+1',
);

int _cacheAssessmentDraftEstimateSize(
  CacheAssessmentDraft object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.assessorId.length * 3;
  {
    final value = object.block;
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
  bytesCount += 3 + object.draftKey.length * 3;
  {
    final value = object.facilityDefaultsJson;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.facilityName.length * 3;
  bytesCount += 3 + object.facilityType.length * 3;
  {
    final value = object.lastError;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.phase.length * 3;
  bytesCount += 3 + object.planFacilityId.length * 3;
  bytesCount += 3 + object.requestJson.length * 3;
  {
    final value = object.state;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.status.length * 3;
  bytesCount += 3 + object.tenantId.length * 3;
  return bytesCount;
}

void _cacheAssessmentDraftSerialize(
  CacheAssessmentDraft object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.assessorId);
  writer.writeLong(offsets[1], object.attemptCount);
  writer.writeString(offsets[2], object.block);
  writer.writeDateTime(offsets[3], object.createdAt);
  writer.writeString(offsets[4], object.district);
  writer.writeString(offsets[5], object.draftKey);
  writer.writeString(offsets[6], object.facilityDefaultsJson);
  writer.writeString(offsets[7], object.facilityName);
  writer.writeString(offsets[8], object.facilityType);
  writer.writeString(offsets[9], object.lastError);
  writer.writeString(offsets[10], object.phase);
  writer.writeString(offsets[11], object.planFacilityId);
  writer.writeString(offsets[12], object.requestJson);
  writer.writeString(offsets[13], object.state);
  writer.writeString(offsets[14], object.status);
  writer.writeString(offsets[15], object.tenantId);
  writer.writeDateTime(offsets[16], object.updatedAt);
}

CacheAssessmentDraft _cacheAssessmentDraftDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAssessmentDraft(
    assessorId: reader.readString(offsets[0]),
    attemptCount: reader.readLongOrNull(offsets[1]) ?? 0,
    block: reader.readStringOrNull(offsets[2]),
    district: reader.readStringOrNull(offsets[4]),
    draftKey: reader.readString(offsets[5]),
    facilityDefaultsJson: reader.readStringOrNull(offsets[6]),
    facilityName: reader.readString(offsets[7]),
    facilityType: reader.readString(offsets[8]),
    lastError: reader.readStringOrNull(offsets[9]),
    phase: reader.readString(offsets[10]),
    planFacilityId: reader.readString(offsets[11]),
    requestJson: reader.readString(offsets[12]),
    state: reader.readStringOrNull(offsets[13]),
    status: reader.readString(offsets[14]),
    tenantId: reader.readString(offsets[15]),
  );
  object.createdAt = reader.readDateTime(offsets[3]);
  object.id = id;
  object.updatedAt = reader.readDateTime(offsets[16]);
  return object;
}

P _cacheAssessmentDraftDeserializeProp<P>(
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
      return (reader.readStringOrNull(offset)) as P;
    case 3:
      return (reader.readDateTime(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset)) as P;
    case 7:
      return (reader.readString(offset)) as P;
    case 8:
      return (reader.readString(offset)) as P;
    case 9:
      return (reader.readStringOrNull(offset)) as P;
    case 10:
      return (reader.readString(offset)) as P;
    case 11:
      return (reader.readString(offset)) as P;
    case 12:
      return (reader.readString(offset)) as P;
    case 13:
      return (reader.readStringOrNull(offset)) as P;
    case 14:
      return (reader.readString(offset)) as P;
    case 15:
      return (reader.readString(offset)) as P;
    case 16:
      return (reader.readDateTime(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAssessmentDraftGetId(CacheAssessmentDraft object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAssessmentDraftGetLinks(
    CacheAssessmentDraft object) {
  return [];
}

void _cacheAssessmentDraftAttach(
    IsarCollection<dynamic> col, Id id, CacheAssessmentDraft object) {
  object.id = id;
}

extension CacheAssessmentDraftByIndex on IsarCollection<CacheAssessmentDraft> {
  Future<CacheAssessmentDraft?> getByDraftKey(String draftKey) {
    return getByIndex(r'draftKey', [draftKey]);
  }

  CacheAssessmentDraft? getByDraftKeySync(String draftKey) {
    return getByIndexSync(r'draftKey', [draftKey]);
  }

  Future<bool> deleteByDraftKey(String draftKey) {
    return deleteByIndex(r'draftKey', [draftKey]);
  }

  bool deleteByDraftKeySync(String draftKey) {
    return deleteByIndexSync(r'draftKey', [draftKey]);
  }

  Future<List<CacheAssessmentDraft?>> getAllByDraftKey(
      List<String> draftKeyValues) {
    final values = draftKeyValues.map((e) => [e]).toList();
    return getAllByIndex(r'draftKey', values);
  }

  List<CacheAssessmentDraft?> getAllByDraftKeySync(
      List<String> draftKeyValues) {
    final values = draftKeyValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'draftKey', values);
  }

  Future<int> deleteAllByDraftKey(List<String> draftKeyValues) {
    final values = draftKeyValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'draftKey', values);
  }

  int deleteAllByDraftKeySync(List<String> draftKeyValues) {
    final values = draftKeyValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'draftKey', values);
  }

  Future<Id> putByDraftKey(CacheAssessmentDraft object) {
    return putByIndex(r'draftKey', object);
  }

  Id putByDraftKeySync(CacheAssessmentDraft object, {bool saveLinks = true}) {
    return putByIndexSync(r'draftKey', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByDraftKey(List<CacheAssessmentDraft> objects) {
    return putAllByIndex(r'draftKey', objects);
  }

  List<Id> putAllByDraftKeySync(List<CacheAssessmentDraft> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'draftKey', objects, saveLinks: saveLinks);
  }
}

extension CacheAssessmentDraftQueryWhereSort
    on QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QWhere> {
  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhere>
      anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheAssessmentDraftQueryWhere
    on QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QWhereClause> {
  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      draftKeyEqualTo(String draftKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'draftKey',
        value: [draftKey],
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      draftKeyNotEqualTo(String draftKey) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'draftKey',
              lower: [],
              upper: [draftKey],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'draftKey',
              lower: [draftKey],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'draftKey',
              lower: [draftKey],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'draftKey',
              lower: [],
              upper: [draftKey],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      tenantIdEqualTo(String tenantId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'tenantId',
        value: [tenantId],
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      assessorIdEqualTo(String assessorId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'assessorId',
        value: [assessorId],
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      phaseEqualTo(String phase) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'phase',
        value: [phase],
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
      statusEqualTo(String status) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'status',
        value: [status],
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterWhereClause>
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
}

extension CacheAssessmentDraftQueryFilter on QueryBuilder<CacheAssessmentDraft,
    CacheAssessmentDraft, QFilterCondition> {
  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> assessorIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assessorId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> assessorIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assessorId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> attemptCountEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'attemptCount',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> attemptCountGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'attemptCount',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> attemptCountLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'attemptCount',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> attemptCountBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'attemptCount',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'block',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'block',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockEqualTo(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockGreaterThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockLessThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockBetween(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockStartsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockEndsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      blockContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'block',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      blockMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'block',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'block',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> blockIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'block',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'district',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'district',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtEqualTo(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtGreaterThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtLessThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtBetween(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtStartsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtEndsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      districtContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'district',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      districtMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'district',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'district',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> districtIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'district',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'draftKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'draftKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'draftKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'draftKey',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'draftKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'draftKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      draftKeyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'draftKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      draftKeyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'draftKey',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'draftKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> draftKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'draftKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityDefaultsJson',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityDefaultsJson',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityDefaultsJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'facilityDefaultsJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'facilityDefaultsJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'facilityDefaultsJson',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'facilityDefaultsJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'facilityDefaultsJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      facilityDefaultsJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityDefaultsJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      facilityDefaultsJsonMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityDefaultsJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityDefaultsJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityDefaultsJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityDefaultsJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeEqualTo(
    String value, {
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeGreaterThan(
    String value, {
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeLessThan(
    String value, {
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeBetween(
    String lower,
    String upper, {
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeStartsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeEndsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      facilityTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      facilityTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> facilityTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'lastError',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'lastError',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorEqualTo(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorGreaterThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorLessThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorBetween(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorStartsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorEndsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      lastErrorContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'lastError',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      lastErrorMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'lastError',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'lastError',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> lastErrorIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'lastError',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> phaseIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'phase',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> phaseIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'phase',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> planFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'planFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> planFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'planFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'requestJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'requestJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'requestJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'requestJson',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'requestJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'requestJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      requestJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'requestJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      requestJsonMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'requestJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'requestJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> requestJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'requestJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'state',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'state',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateEqualTo(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateGreaterThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateLessThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateBetween(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateStartsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateEndsWith(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      stateContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'state',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
          QAfterFilterCondition>
      stateMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'state',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'state',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> stateIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'state',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> statusEqualTo(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> statusLessThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> statusBetween(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> updatedAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> updatedAtGreaterThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> updatedAtLessThan(
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

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft,
      QAfterFilterCondition> updatedAtBetween(
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

extension CacheAssessmentDraftQueryObject on QueryBuilder<CacheAssessmentDraft,
    CacheAssessmentDraft, QFilterCondition> {}

extension CacheAssessmentDraftQueryLinks on QueryBuilder<CacheAssessmentDraft,
    CacheAssessmentDraft, QFilterCondition> {}

extension CacheAssessmentDraftQuerySortBy
    on QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QSortBy> {
  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByAssessorId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByAssessorIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByAttemptCount() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'attemptCount', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByAttemptCountDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'attemptCount', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByBlock() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'block', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByBlockDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'block', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByDistrict() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'district', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByDistrictDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'district', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByDraftKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'draftKey', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByDraftKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'draftKey', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByFacilityDefaultsJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityDefaultsJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByFacilityDefaultsJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityDefaultsJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByFacilityName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByFacilityNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByFacilityType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityType', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByFacilityTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityType', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByLastError() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByLastErrorDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByPhase() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByPhaseDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByPlanFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByPlanFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByRequestJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'requestJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByRequestJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'requestJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByState() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'state', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByStateDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'state', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheAssessmentDraftQuerySortThenBy
    on QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QSortThenBy> {
  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByAssessorId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByAssessorIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assessorId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByAttemptCount() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'attemptCount', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByAttemptCountDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'attemptCount', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByBlock() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'block', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByBlockDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'block', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByDistrict() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'district', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByDistrictDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'district', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByDraftKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'draftKey', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByDraftKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'draftKey', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByFacilityDefaultsJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityDefaultsJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByFacilityDefaultsJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityDefaultsJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByFacilityName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByFacilityNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityName', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByFacilityType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityType', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByFacilityTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityType', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByLastError() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByLastErrorDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastError', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByPhase() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByPhaseDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'phase', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByPlanFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByPlanFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'planFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByRequestJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'requestJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByRequestJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'requestJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByState() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'state', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByStateDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'state', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QAfterSortBy>
      thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheAssessmentDraftQueryWhereDistinct
    on QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct> {
  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByAssessorId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assessorId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByAttemptCount() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'attemptCount');
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByBlock({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'block', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByDistrict({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'district', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByDraftKey({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'draftKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByFacilityDefaultsJson({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityDefaultsJson',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByFacilityName({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityName', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByFacilityType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByLastError({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'lastError', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByPhase({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'phase', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByPlanFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'planFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByRequestJson({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'requestJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByState({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'state', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByStatus({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'status', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByTenantId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'tenantId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssessmentDraft, CacheAssessmentDraft, QDistinct>
      distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheAssessmentDraftQueryProperty on QueryBuilder<
    CacheAssessmentDraft, CacheAssessmentDraft, QQueryProperty> {
  QueryBuilder<CacheAssessmentDraft, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      assessorIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assessorId');
    });
  }

  QueryBuilder<CacheAssessmentDraft, int, QQueryOperations>
      attemptCountProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'attemptCount');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String?, QQueryOperations>
      blockProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'block');
    });
  }

  QueryBuilder<CacheAssessmentDraft, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String?, QQueryOperations>
      districtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'district');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      draftKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'draftKey');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String?, QQueryOperations>
      facilityDefaultsJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityDefaultsJson');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      facilityNameProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityName');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      facilityTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityType');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String?, QQueryOperations>
      lastErrorProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'lastError');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations> phaseProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'phase');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      planFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'planFacilityId');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      requestJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'requestJson');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String?, QQueryOperations>
      stateProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'state');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      statusProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'status');
    });
  }

  QueryBuilder<CacheAssessmentDraft, String, QQueryOperations>
      tenantIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'tenantId');
    });
  }

  QueryBuilder<CacheAssessmentDraft, DateTime, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }
}
