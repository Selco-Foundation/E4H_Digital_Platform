// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_bom_doc.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheBomDocCollection on Isar {
  IsarCollection<CacheBomDoc> get cacheBomDocs => this.collection();
}

const CacheBomDocSchema = CollectionSchema(
  name: r'CacheBomDoc',
  id: -3643283558535085398,
  properties: {
    r'assignUserUuid': PropertySchema(
      id: 0,
      name: r'assignUserUuid',
      type: IsarType.string,
    ),
    r'bomName': PropertySchema(
      id: 1,
      name: r'bomName',
      type: IsarType.string,
    ),
    r'dataJson': PropertySchema(
      id: 2,
      name: r'dataJson',
      type: IsarType.string,
    ),
    r'facilityId': PropertySchema(
      id: 3,
      name: r'facilityId',
      type: IsarType.string,
    ),
    r'isDirty': PropertySchema(
      id: 4,
      name: r'isDirty',
      type: IsarType.bool,
    ),
    r'projectId': PropertySchema(
      id: 5,
      name: r'projectId',
      type: IsarType.string,
    ),
    r'schemaKey': PropertySchema(
      id: 6,
      name: r'schemaKey',
      type: IsarType.string,
    ),
    r'serverBomId': PropertySchema(
      id: 7,
      name: r'serverBomId',
      type: IsarType.string,
    ),
    r'tenantId': PropertySchema(
      id: 8,
      name: r'tenantId',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 9,
      name: r'updatedAt',
      type: IsarType.dateTime,
    )
  },
  estimateSize: _cacheBomDocEstimateSize,
  serialize: _cacheBomDocSerialize,
  deserialize: _cacheBomDocDeserialize,
  deserializeProp: _cacheBomDocDeserializeProp,
  idName: r'id',
  indexes: {
    r'projectId_schemaKey': IndexSchema(
      id: -8384682765972658953,
      name: r'projectId_schemaKey',
      unique: true,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'projectId',
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
  getId: _cacheBomDocGetId,
  getLinks: _cacheBomDocGetLinks,
  attach: _cacheBomDocAttach,
  version: '3.1.0+1',
);

int _cacheBomDocEstimateSize(
  CacheBomDoc object,
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
  {
    final value = object.bomName;
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
  bytesCount += 3 + object.projectId.length * 3;
  bytesCount += 3 + object.schemaKey.length * 3;
  {
    final value = object.serverBomId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.tenantId.length * 3;
  return bytesCount;
}

void _cacheBomDocSerialize(
  CacheBomDoc object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.assignUserUuid);
  writer.writeString(offsets[1], object.bomName);
  writer.writeString(offsets[2], object.dataJson);
  writer.writeString(offsets[3], object.facilityId);
  writer.writeBool(offsets[4], object.isDirty);
  writer.writeString(offsets[5], object.projectId);
  writer.writeString(offsets[6], object.schemaKey);
  writer.writeString(offsets[7], object.serverBomId);
  writer.writeString(offsets[8], object.tenantId);
  writer.writeDateTime(offsets[9], object.updatedAt);
}

CacheBomDoc _cacheBomDocDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheBomDoc();
  object.assignUserUuid = reader.readStringOrNull(offsets[0]);
  object.bomName = reader.readStringOrNull(offsets[1]);
  object.dataJson = reader.readString(offsets[2]);
  object.facilityId = reader.readStringOrNull(offsets[3]);
  object.id = id;
  object.isDirty = reader.readBool(offsets[4]);
  object.projectId = reader.readString(offsets[5]);
  object.schemaKey = reader.readString(offsets[6]);
  object.serverBomId = reader.readStringOrNull(offsets[7]);
  object.tenantId = reader.readString(offsets[8]);
  object.updatedAt = reader.readDateTime(offsets[9]);
  return object;
}

P _cacheBomDocDeserializeProp<P>(
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
      return (reader.readString(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readBool(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    case 6:
      return (reader.readString(offset)) as P;
    case 7:
      return (reader.readStringOrNull(offset)) as P;
    case 8:
      return (reader.readString(offset)) as P;
    case 9:
      return (reader.readDateTime(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheBomDocGetId(CacheBomDoc object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheBomDocGetLinks(CacheBomDoc object) {
  return [];
}

void _cacheBomDocAttach(
    IsarCollection<dynamic> col, Id id, CacheBomDoc object) {
  object.id = id;
}

extension CacheBomDocByIndex on IsarCollection<CacheBomDoc> {
  Future<CacheBomDoc?> getByProjectIdSchemaKey(
      String projectId, String schemaKey) {
    return getByIndex(r'projectId_schemaKey', [projectId, schemaKey]);
  }

  CacheBomDoc? getByProjectIdSchemaKeySync(String projectId, String schemaKey) {
    return getByIndexSync(r'projectId_schemaKey', [projectId, schemaKey]);
  }

  Future<bool> deleteByProjectIdSchemaKey(String projectId, String schemaKey) {
    return deleteByIndex(r'projectId_schemaKey', [projectId, schemaKey]);
  }

  bool deleteByProjectIdSchemaKeySync(String projectId, String schemaKey) {
    return deleteByIndexSync(r'projectId_schemaKey', [projectId, schemaKey]);
  }

  Future<List<CacheBomDoc?>> getAllByProjectIdSchemaKey(
      List<String> projectIdValues, List<String> schemaKeyValues) {
    final len = projectIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([projectIdValues[i], schemaKeyValues[i]]);
    }

    return getAllByIndex(r'projectId_schemaKey', values);
  }

  List<CacheBomDoc?> getAllByProjectIdSchemaKeySync(
      List<String> projectIdValues, List<String> schemaKeyValues) {
    final len = projectIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([projectIdValues[i], schemaKeyValues[i]]);
    }

    return getAllByIndexSync(r'projectId_schemaKey', values);
  }

  Future<int> deleteAllByProjectIdSchemaKey(
      List<String> projectIdValues, List<String> schemaKeyValues) {
    final len = projectIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([projectIdValues[i], schemaKeyValues[i]]);
    }

    return deleteAllByIndex(r'projectId_schemaKey', values);
  }

  int deleteAllByProjectIdSchemaKeySync(
      List<String> projectIdValues, List<String> schemaKeyValues) {
    final len = projectIdValues.length;
    assert(schemaKeyValues.length == len,
        'All index values must have the same length');
    final values = <List<dynamic>>[];
    for (var i = 0; i < len; i++) {
      values.add([projectIdValues[i], schemaKeyValues[i]]);
    }

    return deleteAllByIndexSync(r'projectId_schemaKey', values);
  }

  Future<Id> putByProjectIdSchemaKey(CacheBomDoc object) {
    return putByIndex(r'projectId_schemaKey', object);
  }

  Id putByProjectIdSchemaKeySync(CacheBomDoc object, {bool saveLinks = true}) {
    return putByIndexSync(r'projectId_schemaKey', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByProjectIdSchemaKey(List<CacheBomDoc> objects) {
    return putAllByIndex(r'projectId_schemaKey', objects);
  }

  List<Id> putAllByProjectIdSchemaKeySync(List<CacheBomDoc> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'projectId_schemaKey', objects,
        saveLinks: saveLinks);
  }
}

extension CacheBomDocQueryWhereSort
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QWhere> {
  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhere> anyUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        const IndexWhereClause.any(indexName: r'updatedAt'),
      );
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhere> anyIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        const IndexWhereClause.any(indexName: r'isDirty'),
      );
    });
  }
}

extension CacheBomDocQueryWhere
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QWhereClause> {
  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> idNotEqualTo(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> idGreaterThan(Id id,
      {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> idLessThan(Id id,
      {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> idBetween(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause>
      projectIdEqualToAnySchemaKey(String projectId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'projectId_schemaKey',
        value: [projectId],
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause>
      projectIdNotEqualToAnySchemaKey(String projectId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [],
              upper: [projectId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [projectId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [projectId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [],
              upper: [projectId],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause>
      projectIdSchemaKeyEqualTo(String projectId, String schemaKey) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'projectId_schemaKey',
        value: [projectId, schemaKey],
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause>
      projectIdEqualToSchemaKeyNotEqualTo(String projectId, String schemaKey) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [projectId],
              upper: [projectId, schemaKey],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [projectId, schemaKey],
              includeLower: false,
              upper: [projectId],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [projectId, schemaKey],
              includeLower: false,
              upper: [projectId],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'projectId_schemaKey',
              lower: [projectId],
              upper: [projectId, schemaKey],
              includeUpper: false,
            ));
      }
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> updatedAtEqualTo(
      DateTime updatedAt) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'updatedAt',
        value: [updatedAt],
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> updatedAtNotEqualTo(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> updatedAtLessThan(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> updatedAtBetween(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> isDirtyEqualTo(
      bool isDirty) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'isDirty',
        value: [isDirty],
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterWhereClause> isDirtyNotEqualTo(
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

extension CacheBomDocQueryFilter
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QFilterCondition> {
  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      assignUserUuidIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'assignUserUuid',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      assignUserUuidIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'assignUserUuid',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      assignUserUuidContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assignUserUuid',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      assignUserUuidMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assignUserUuid',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      assignUserUuidIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assignUserUuid',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      assignUserUuidIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assignUserUuid',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      bomNameIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'bomName',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      bomNameIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'bomName',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> bomNameEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'bomName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      bomNameGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'bomName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> bomNameLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'bomName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> bomNameBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'bomName',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      bomNameStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'bomName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> bomNameEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'bomName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> bomNameContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'bomName',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> bomNameMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'bomName',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      bomNameIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'bomName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      bomNameIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'bomName',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> dataJsonEqualTo(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> dataJsonBetween(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      dataJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'dataJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> dataJsonMatches(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      dataJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      dataJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'dataJson',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      facilityIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      facilityIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'facilityId',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      facilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'facilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      facilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'facilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      facilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      facilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'facilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> idEqualTo(
      Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> idGreaterThan(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> idLessThan(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> idBetween(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> isDirtyEqualTo(
      bool value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'isDirty',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'projectId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'projectId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'projectId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'projectId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'projectId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'projectId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'projectId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'projectId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      projectIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'projectId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      schemaKeyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'schemaKey',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      schemaKeyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'schemaKey',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      schemaKeyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'schemaKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      schemaKeyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'schemaKey',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'serverBomId',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'serverBomId',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'serverBomId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'serverBomId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'serverBomId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'serverBomId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'serverBomId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'serverBomId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'serverBomId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'serverBomId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'serverBomId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      serverBomIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'serverBomId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> tenantIdEqualTo(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> tenantIdBetween(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      tenantIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'tenantId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition> tenantIdMatches(
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      tenantIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      tenantIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'tenantId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
      updatedAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterFilterCondition>
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

extension CacheBomDocQueryObject
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QFilterCondition> {}

extension CacheBomDocQueryLinks
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QFilterCondition> {}

extension CacheBomDocQuerySortBy
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QSortBy> {
  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByAssignUserUuid() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy>
      sortByAssignUserUuidDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByBomName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'bomName', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByBomNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'bomName', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByIsDirtyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByProjectId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'projectId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByProjectIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'projectId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortBySchemaKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortBySchemaKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByServerBomId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serverBomId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByServerBomIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serverBomId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheBomDocQuerySortThenBy
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QSortThenBy> {
  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByAssignUserUuid() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy>
      thenByAssignUserUuidDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assignUserUuid', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByBomName() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'bomName', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByBomNameDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'bomName', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByDataJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByDataJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'dataJson', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'facilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByIsDirtyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isDirty', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByProjectId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'projectId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByProjectIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'projectId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenBySchemaKey() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenBySchemaKeyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'schemaKey', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByServerBomId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serverBomId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByServerBomIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serverBomId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByTenantId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByTenantIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'tenantId', Sort.desc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheBomDocQueryWhereDistinct
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> {
  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByAssignUserUuid(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assignUserUuid',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByBomName(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'bomName', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByDataJson(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'dataJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByFacilityId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'facilityId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByIsDirty() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'isDirty');
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByProjectId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'projectId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctBySchemaKey(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'schemaKey', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByServerBomId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'serverBomId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByTenantId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'tenantId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheBomDoc, CacheBomDoc, QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheBomDocQueryProperty
    on QueryBuilder<CacheBomDoc, CacheBomDoc, QQueryProperty> {
  QueryBuilder<CacheBomDoc, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheBomDoc, String?, QQueryOperations>
      assignUserUuidProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assignUserUuid');
    });
  }

  QueryBuilder<CacheBomDoc, String?, QQueryOperations> bomNameProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'bomName');
    });
  }

  QueryBuilder<CacheBomDoc, String, QQueryOperations> dataJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'dataJson');
    });
  }

  QueryBuilder<CacheBomDoc, String?, QQueryOperations> facilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'facilityId');
    });
  }

  QueryBuilder<CacheBomDoc, bool, QQueryOperations> isDirtyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'isDirty');
    });
  }

  QueryBuilder<CacheBomDoc, String, QQueryOperations> projectIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'projectId');
    });
  }

  QueryBuilder<CacheBomDoc, String, QQueryOperations> schemaKeyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'schemaKey');
    });
  }

  QueryBuilder<CacheBomDoc, String?, QQueryOperations> serverBomIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'serverBomId');
    });
  }

  QueryBuilder<CacheBomDoc, String, QQueryOperations> tenantIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'tenantId');
    });
  }

  QueryBuilder<CacheBomDoc, DateTime, QQueryOperations> updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }
}
