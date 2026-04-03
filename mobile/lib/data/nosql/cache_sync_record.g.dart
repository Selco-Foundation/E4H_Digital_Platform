// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_sync_record.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheSyncRecordCollection on Isar {
  IsarCollection<CacheSyncRecord> get cacheSyncRecords => this.collection();
}

const CacheSyncRecordSchema = CollectionSchema(
  name: r'CacheSyncRecord',
  id: -5127239599341518281,
  properties: {
    r'syncedAt': PropertySchema(
      id: 0,
      name: r'syncedAt',
      type: IsarType.dateTime,
    ),
    r'userType': PropertySchema(
      id: 1,
      name: r'userType',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheSyncRecordEstimateSize,
  serialize: _cacheSyncRecordSerialize,
  deserialize: _cacheSyncRecordDeserialize,
  deserializeProp: _cacheSyncRecordDeserializeProp,
  idName: r'id',
  indexes: {
    r'userType': IndexSchema(
      id: -7871966206036222683,
      name: r'userType',
      unique: true,
      replace: true,
      properties: [
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
  getId: _cacheSyncRecordGetId,
  getLinks: _cacheSyncRecordGetLinks,
  attach: _cacheSyncRecordAttach,
  version: '3.1.0+1',
);

int _cacheSyncRecordEstimateSize(
  CacheSyncRecord object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.userType.length * 3;
  return bytesCount;
}

void _cacheSyncRecordSerialize(
  CacheSyncRecord object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeDateTime(offsets[0], object.syncedAt);
  writer.writeString(offsets[1], object.userType);
}

CacheSyncRecord _cacheSyncRecordDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheSyncRecord(
    syncedAt: reader.readDateTime(offsets[0]),
    userType: reader.readString(offsets[1]),
  );
  object.id = id;
  return object;
}

P _cacheSyncRecordDeserializeProp<P>(
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
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheSyncRecordGetId(CacheSyncRecord object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheSyncRecordGetLinks(CacheSyncRecord object) {
  return [];
}

void _cacheSyncRecordAttach(
    IsarCollection<dynamic> col, Id id, CacheSyncRecord object) {
  object.id = id;
}

extension CacheSyncRecordByIndex on IsarCollection<CacheSyncRecord> {
  Future<CacheSyncRecord?> getByUserType(String userType) {
    return getByIndex(r'userType', [userType]);
  }

  CacheSyncRecord? getByUserTypeSync(String userType) {
    return getByIndexSync(r'userType', [userType]);
  }

  Future<bool> deleteByUserType(String userType) {
    return deleteByIndex(r'userType', [userType]);
  }

  bool deleteByUserTypeSync(String userType) {
    return deleteByIndexSync(r'userType', [userType]);
  }

  Future<List<CacheSyncRecord?>> getAllByUserType(List<String> userTypeValues) {
    final values = userTypeValues.map((e) => [e]).toList();
    return getAllByIndex(r'userType', values);
  }

  List<CacheSyncRecord?> getAllByUserTypeSync(List<String> userTypeValues) {
    final values = userTypeValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'userType', values);
  }

  Future<int> deleteAllByUserType(List<String> userTypeValues) {
    final values = userTypeValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'userType', values);
  }

  int deleteAllByUserTypeSync(List<String> userTypeValues) {
    final values = userTypeValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'userType', values);
  }

  Future<Id> putByUserType(CacheSyncRecord object) {
    return putByIndex(r'userType', object);
  }

  Id putByUserTypeSync(CacheSyncRecord object, {bool saveLinks = true}) {
    return putByIndexSync(r'userType', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByUserType(List<CacheSyncRecord> objects) {
    return putAllByIndex(r'userType', objects);
  }

  List<Id> putAllByUserTypeSync(List<CacheSyncRecord> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'userType', objects, saveLinks: saveLinks);
  }
}

extension CacheSyncRecordQueryWhereSort
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QWhere> {
  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheSyncRecordQueryWhere
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QWhereClause> {
  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause> idEqualTo(
      Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause>
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause> idLessThan(
      Id id,
      {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause> idBetween(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause>
      userTypeEqualTo(String userType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'userType',
        value: [userType],
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterWhereClause>
      userTypeNotEqualTo(String userType) {
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
}

extension CacheSyncRecordQueryFilter
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QFilterCondition> {
  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      syncedAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'syncedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      syncedAtGreaterThan(
    DateTime value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'syncedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      syncedAtLessThan(
    DateTime value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'syncedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      syncedAtBetween(
    DateTime lower,
    DateTime upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'syncedAt',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeEqualTo(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeGreaterThan(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeLessThan(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeBetween(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeStartsWith(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeEndsWith(
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

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'userType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterFilterCondition>
      userTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'userType',
        value: '',
      ));
    });
  }
}

extension CacheSyncRecordQueryObject
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QFilterCondition> {}

extension CacheSyncRecordQueryLinks
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QFilterCondition> {}

extension CacheSyncRecordQuerySortBy
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QSortBy> {
  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      sortBySyncedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'syncedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      sortBySyncedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'syncedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      sortByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      sortByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheSyncRecordQuerySortThenBy
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QSortThenBy> {
  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      thenBySyncedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'syncedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      thenBySyncedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'syncedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      thenByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QAfterSortBy>
      thenByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheSyncRecordQueryWhereDistinct
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QDistinct> {
  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QDistinct>
      distinctBySyncedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'syncedAt');
    });
  }

  QueryBuilder<CacheSyncRecord, CacheSyncRecord, QDistinct> distinctByUserType(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'userType', caseSensitive: caseSensitive);
    });
  }
}

extension CacheSyncRecordQueryProperty
    on QueryBuilder<CacheSyncRecord, CacheSyncRecord, QQueryProperty> {
  QueryBuilder<CacheSyncRecord, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheSyncRecord, DateTime, QQueryOperations> syncedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'syncedAt');
    });
  }

  QueryBuilder<CacheSyncRecord, String, QQueryOperations> userTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'userType');
    });
  }
}
