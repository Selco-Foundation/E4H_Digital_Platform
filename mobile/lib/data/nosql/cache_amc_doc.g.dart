// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_amc_doc.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAmcDocCollection on Isar {
  IsarCollection<CacheAmcDoc> get cacheAmcDocs => this.collection();
}

const CacheAmcDocSchema = CollectionSchema(
  name: r'CacheAmcDoc',
  id: 2998285831148170078,
  properties: {
    r'assignUserUuid': PropertySchema(
      id: 0,
      name: r'assignUserUuid',
      type: IsarType.string,
    ),
    r'dataJson': PropertySchema(
      id: 1,
      name: r'dataJson',
      type: IsarType.string,
    ),
    r'facilityId': PropertySchema(
      id: 2,
      name: r'facilityId',
      type: IsarType.string,
    ),
    r'formName': PropertySchema(
      id: 3,
      name: r'formName',
      type: IsarType.string,
    ),
    r'isDirty': PropertySchema(
      id: 4,
      name: r'isDirty',
      type: IsarType.bool,
    ),
    r'scheduleVisitId': PropertySchema(
      id: 5,
      name: r'scheduleVisitId',
      type: IsarType.string,
    ),
    r'schemaKey': PropertySchema(
      id: 6,
      name: r'schemaKey',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 7,
      name: r'tenantId',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 8,
      name: r'updatedAt',
      type: IsarType.dateTime,
    )
  },
  estimateSize: _cacheAmcDocEstimateSize,
  serialize: _cacheAmcDocSerialize,
  deserialize: _cacheAmcDocDeserialize,
  deserializeProp: _cacheAmcDocDeserializeProp,
  idName: r'id',
  indexes: {
    r'scheduleVisitId_schemaKey': IndexSchema(
      id: 2698259246245121292,
      name: r'scheduleVisitId_schemaKey',
      unique: true,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'scheduleVisitId',
          type: IndexType.hash,
          caseSensitive: false,
        ),
        IndexPropertySchema(
          name: r'schemaKey',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'updatedAt': IndexSchema(
      id: -6238191080293565125,
      name: r'updatedAt',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'updatedAt',
          type: IndexType.value,
          caseSensitive: false,
        )
      ],
    ),
    r'isDirty': IndexSchema(
      id: 5701622868881901852,
      name: r'isDirty',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'isDirty',
          type: IndexType.value,
          caseSensitive: false,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheAmcDocGetId,
  getLinks: _cacheAmcDocGetLinks,
  attach: _cacheAmcDocAttach,
  version: '3.1.0+1',
);

int _cacheAmcDocEstimateSize(
  CacheAmcDoc object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.assignUserUuid;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.dataJson.length * 3;
  {
    final value = object.facilityId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.formName;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.scheduleVisitId.length * 3;
  bytesCount += 3 + object.schemaKey.length * 3;
  bytesCount += 3 + object.tenantId.length * 3;
  return bytesCount;
}

void _cacheAmcDocSerialize(
  CacheAmcDoc object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.assignUserUuid);
  writer.writeString(offsets[1], object.dataJson);
  writer.writeString(offsets[2], object.facilityId);
  writer.writeString(offsets[3], object.formName);
  writer.writeBool(offsets[4], object.isDirty);
  writer.writeString(offsets[5], object.scheduleVisitId);
  writer.writeString(offsets[6], object.schemaKey);
  writer.writeString(offsets[7], object.tenantId);
  writer.writeDateTime(offsets[8], object.updatedAt);
}

CacheAmcDoc _cacheAmcDocDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAmcDoc();
  object.assignUserUuid = reader.readStringOrNull(offsets[0]);
  object.dataJson = reader.readString(offsets[1]);
  object.facilityId = reader.readStringOrNull(offsets[2]);
  object.formName = reader.readStringOrNull(offsets[3]);
  object.id = id;
  object.isDirty = reader.readBool(offsets[4]);
  object.scheduleVisitId = reader.readString(offsets[5]);
  object.schemaKey = reader.readString(offsets[6]);
  object.tenantId = reader.readString(offsets[7]);
  object.updatedAt = reader.readDateTime(offsets[8]);
  return object;
}

P _cacheAmcDocDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readString(offset)) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readBool(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    case 6:
      return (reader.readString(offset)) as P;
    case 7:
      return (reader.readString(offset)) as P;
    case 8:
      return (reader.readDateTime(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAmcDocGetId(CacheAmcDoc object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAmcDocGetLinks(CacheAmcDoc object) {
  return [];
}

void _cacheAmcDocAttach(
    IsarCollection<dynamic> col, Id id, CacheAmcDoc object) {
  object.id = id;
}

extension CacheAmcDocByIndex on IsarCollection<CacheAmcDoc> {
  Future<CacheAmcDoc?> getByScheduleVisitIdSchemaKey(
      String scheduleVisitId, String schemaKey) {
    return getByIndex(
        r'scheduleVisitId_schemaKey', [scheduleVisitId, schemaKey]);
  }

  CacheAmcDoc? getByScheduleVisitIdSchemaKeySync(
      String scheduleVisitId, String schemaKey) {
    return getByIndexSync(
        r'scheduleVisitId_schemaKey', [scheduleVisitId, schemaKey]);
  }

  Future<bool> deleteByScheduleVisitIdSchemaKey(
      String scheduleVisitId, String schemaKey) {
    return deleteByIndex(
        r'scheduleVisitId_schemaKey', [scheduleVisitId, schemaKey]);
  }

  bool deleteByScheduleVisitIdSchemaKeySync(
      String scheduleVisitId, String schemaKey) {
    return deleteByIndexSync(
        r'scheduleVisitId_schemaKey', [scheduleVisitId, schemaKey]);
  }

  Future<List<CacheAmcDoc?>> getAllByScheduleVisitIdSchemaKey(
      List<String> scheduleVisitIdValues, List<String> schemaKeyValues) {
    final len = scheduleVisitIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([scheduleVisitIdValues[i], schemaKeyValues[i]]);
    }

    return getAllByIndex(r'scheduleVisitId_schemaKey', values);
  }

  List<CacheAmcDoc?> getAllByScheduleVisitIdSchemaKeySync(
      List<String> scheduleVisitIdValues, List<String> schemaKeyValues) {
    final len = scheduleVisitIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([scheduleVisitIdValues[i], schemaKeyValues[i]]);
    }

    return getAllByIndexSync(r'scheduleVisitId_schemaKey', values);
  }

  Future<int> deleteAllByScheduleVisitIdSchemaKey(
      List<String> scheduleVisitIdValues, List<String> schemaKeyValues) {
    final len = scheduleVisitIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([scheduleVisitIdValues[i], schemaKeyValues[i]]);
    }

    return deleteAllByIndex(r'scheduleVisitId_schemaKey', values);
  }

  int deleteAllByScheduleVisitIdSchemaKeySync(
      List<String> scheduleVisitIdValues, List<String> schemaKeyValues) {
    final len = scheduleVisitIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([scheduleVisitIdValues[i], schemaKeyValues[i]]);
    }

    return deleteAllByIndexSync(r'scheduleVisitId_schemaKey', values);
  }

  Future<Id> putByScheduleVisitIdSchemaKey(CacheAmcDoc object) {
    return putByIndex(r'scheduleVisitId_schemaKey', object);
  }

  Id putByScheduleVisitIdSchemaKeySync(CacheAmcDoc object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'scheduleVisitId_schemaKey', object,
        saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByScheduleVisitIdSchemaKey(List<CacheAmcDoc> objects) {
    return putAllByIndex(r'scheduleVisitId_schemaKey', objects);
  }

  List<Id> putAllByScheduleVisitIdSchemaKeySync(List<CacheAmcDoc> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'scheduleVisitId_schemaKey', objects,
        saveLinks: saveLinks);
  }
}

extension CacheAmcDocQueryWhereSort
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QWhere> {
  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhere> anyUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        const IndexWhereClause.any(indexName: r'updatedAt'),
      );
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhere> anyIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        const IndexWhereClause.any(indexName: r'isDirty'),
      );
    });
  }
}

extension CacheAmcDocQueryWhere
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QWhereClause> {
  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> idNotEqualTo(
      Id id) {
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> idGreaterThan(Id id,
      {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> idLessThan(Id id,
      {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> idBetween(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause>
      scheduleVisitIdEqualToAnySchemaKey(String scheduleVisitId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'scheduleVisitId_schemaKey',
        value: [scheduleVisitId],
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause>
      scheduleVisitIdNotEqualToAnySchemaKey(String scheduleVisitId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [],
              upper: [scheduleVisitId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [scheduleVisitId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [scheduleVisitId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [],
              upper: [scheduleVisitId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause>
      scheduleVisitIdSchemaKeyEqualTo(
          String scheduleVisitId, String schemaKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'scheduleVisitId_schemaKey',
        value: [scheduleVisitId, schemaKey],
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause>
      scheduleVisitIdEqualToSchemaKeyNotEqualTo(
          String scheduleVisitId, String schemaKey) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [scheduleVisitId],
              upper: [scheduleVisitId, schemaKey],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [scheduleVisitId, schemaKey],
              includeLower: false,
              upper: [scheduleVisitId],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [scheduleVisitId, schemaKey],
              includeLower: false,
              upper: [scheduleVisitId],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'scheduleVisitId_schemaKey',
              lower: [scheduleVisitId],
              upper: [scheduleVisitId, schemaKey],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> updatedAtEqualTo(
      DateTime updatedAt) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'updatedAt',
        value: [updatedAt],
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> updatedAtNotEqualTo(
      DateTime updatedAt) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'updatedAt',
              lower: [],
              upper: [updatedAt],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'updatedAt',
              lower: [updatedAt],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'updatedAt',
              lower: [updatedAt],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'updatedAt',
              lower: [],
              upper: [updatedAt],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause>
      updatedAtGreaterThan(
    DateTime updatedAt, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.between(
        indexName: r'updatedAt',
        lower: [updatedAt],
        includeLower: include,
        upper: [],
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> updatedAtLessThan(
    DateTime updatedAt, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.between(
        indexName: r'updatedAt',
        lower: [],
        upper: [updatedAt],
        includeUpper: include,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> updatedAtBetween(
    DateTime lowerUpdatedAt,
    DateTime upperUpdatedAt, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.between(
        indexName: r'updatedAt',
        lower: [lowerUpdatedAt],
        includeLower: includeLower,
        upper: [upperUpdatedAt],
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> isDirtyEqualTo(
      bool isDirty) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'isDirty',
        value: [isDirty],
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterWhereClause> isDirtyNotEqualTo(
      bool isDirty) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'isDirty',
              lower: [],
              upper: [isDirty],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'isDirty',
              lower: [isDirty],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'isDirty',
              lower: [isDirty],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'isDirty',
              lower: [],
              upper: [isDirty],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CacheAmcDocQueryFilter
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QFilterCondition> {
  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'assignUserUuid',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'assignUserUuid',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'assignUserUuid',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assignUserUuid',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignUserUuid',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      assignUserUuidIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assignUserUuid',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> dataJsonEqualTo(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonGreaterThan(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonLessThan(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> dataJsonBetween(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonStartsWith(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonEndsWith(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> dataJsonMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'dataJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      dataJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      facilityIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      facilityIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      facilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      facilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      facilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      facilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'formName',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'formName',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> formNameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'formName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'formName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'formName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> formNameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'formName',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'formName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'formName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'formName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> formNameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'formName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'formName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      formNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'formName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> idEqualTo(
      Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> idGreaterThan(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> idLessThan(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> idBetween(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> isDirtyEqualTo(
      bool value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'isDirty',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduleVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'scheduleVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'scheduleVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'scheduleVisitId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'scheduleVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'scheduleVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'scheduleVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'scheduleVisitId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduleVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      scheduleVisitIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'scheduleVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'schemaKey',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'schemaKey',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'schemaKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      schemaKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'schemaKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> tenantIdEqualTo(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      tenantIdGreaterThan(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      tenantIdLessThan(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> tenantIdBetween(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      tenantIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition> tenantIdMatches(
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
      updatedAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterFilterCondition>
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

extension CacheAmcDocQueryObject
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QFilterCondition> {}

extension CacheAmcDocQueryLinks
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QFilterCondition> {}

extension CacheAmcDocQuerySortBy
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QSortBy> {
  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByAssignUserUuid() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy>
      sortByAssignUserUuidDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByFormName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'formName', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByFormNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'formName', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByIsDirtyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByScheduleVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduleVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy>
      sortByScheduleVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduleVisitId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortBySchemaKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortBySchemaKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheAmcDocQuerySortThenBy
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QSortThenBy> {
  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByAssignUserUuid() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy>
      thenByAssignUserUuidDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByFormName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'formName', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByFormNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'formName', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByIsDirtyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByScheduleVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduleVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy>
      thenByScheduleVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduleVisitId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenBySchemaKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenBySchemaKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheAmcDocQueryWhereDistinct
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> {
  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByAssignUserUuid(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assignUserUuid',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByDataJson(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'dataJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByFacilityId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByFormName(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'formName', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'isDirty');
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByScheduleVisitId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'scheduleVisitId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctBySchemaKey(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'schemaKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByTenantId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'tenantId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcDoc, CacheAmcDoc, QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheAmcDocQueryProperty
    on QueryBuilder<CacheAmcDoc, CacheAmcDoc, QQueryProperty> {
  QueryBuilder<CacheAmcDoc, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAmcDoc, String?, QQueryOperations>
      assignUserUuidProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assignUserUuid');
    });
  }

  QueryBuilder<CacheAmcDoc, String, QQueryOperations> dataJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'dataJson');
    });
  }

  QueryBuilder<CacheAmcDoc, String?, QQueryOperations> facilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityId');
    });
  }

  QueryBuilder<CacheAmcDoc, String?, QQueryOperations> formNameProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'formName');
    });
  }

  QueryBuilder<CacheAmcDoc, bool, QQueryOperations> isDirtyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'isDirty');
    });
  }

  QueryBuilder<CacheAmcDoc, String, QQueryOperations>
      scheduleVisitIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'scheduleVisitId');
    });
  }

  QueryBuilder<CacheAmcDoc, String, QQueryOperations> schemaKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'schemaKey');
    });
  }

  QueryBuilder<CacheAmcDoc, String, QQueryOperations> tenantIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'tenantId');
    });
  }

  QueryBuilder<CacheAmcDoc, DateTime, QQueryOperations> updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }
}
