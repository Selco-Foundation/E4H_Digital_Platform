// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_activity_facility_asset.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheActivityFacilityAssetCollection on Isar {
  IsarCollection<CacheActivityFacilityAsset> get cacheActivityFacilityAssets =>
      this.collection();
}

const CacheActivityFacilityAssetSchema = CollectionSchema(
  name: r'CacheActivityFacilityAsset',
  id: 6066041660537022987,
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
    r'progress': PropertySchema(
      id: 2,
      name: r'progress',
      type: IsarType.long,
    ),
    r'updatedAt': PropertySchema(
      id: 3,
      name: r'updatedAt',
      type: IsarType.dateTime,
    )
  },
  estimateSize: _cacheActivityFacilityAssetEstimateSize,
  serialize: _cacheActivityFacilityAssetSerialize,
  deserialize: _cacheActivityFacilityAssetDeserialize,
  deserializeProp: _cacheActivityFacilityAssetDeserializeProp,
  idName: r'id',
  indexes: {
    r'activityFacilityId': IndexSchema(
      id: -3740981522167357561,
      name: r'activityFacilityId',
      unique: true,
      replace: true,
      properties: [
        IndexPropertySchema(
          name: r'activityFacilityId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheActivityFacilityAssetGetId,
  getLinks: _cacheActivityFacilityAssetGetLinks,
  attach: _cacheActivityFacilityAssetAttach,
  version: '3.1.0+1',
);

int _cacheActivityFacilityAssetEstimateSize(
  CacheActivityFacilityAsset object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  return bytesCount;
}

void _cacheActivityFacilityAssetSerialize(
  CacheActivityFacilityAsset object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeDateTime(offsets[1], object.createdAt);
  writer.writeLong(offsets[2], object.progress);
  writer.writeDateTime(offsets[3], object.updatedAt);
}

CacheActivityFacilityAsset _cacheActivityFacilityAssetDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheActivityFacilityAsset(
    activityFacilityId: reader.readString(offsets[0]),
    progress: reader.readLongOrNull(offsets[2]) ?? 0,
  );
  object.createdAt = reader.readDateTime(offsets[1]);
  object.id = id;
  object.updatedAt = reader.readDateTime(offsets[3]);
  return object;
}

P _cacheActivityFacilityAssetDeserializeProp<P>(
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
      return (reader.readLongOrNull(offset) ?? 0) as P;
    case 3:
      return (reader.readDateTime(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheActivityFacilityAssetGetId(CacheActivityFacilityAsset object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheActivityFacilityAssetGetLinks(
    CacheActivityFacilityAsset object) {
  return [];
}

void _cacheActivityFacilityAssetAttach(
    IsarCollection<dynamic> col, Id id, CacheActivityFacilityAsset object) {
  object.id = id;
}

extension CacheActivityFacilityAssetByIndex
    on IsarCollection<CacheActivityFacilityAsset> {
  Future<CacheActivityFacilityAsset?> getByActivityFacilityId(
      String activityFacilityId) {
    return getByIndex(r'activityFacilityId', [activityFacilityId]);
  }

  CacheActivityFacilityAsset? getByActivityFacilityIdSync(
      String activityFacilityId) {
    return getByIndexSync(r'activityFacilityId', [activityFacilityId]);
  }

  Future<bool> deleteByActivityFacilityId(String activityFacilityId) {
    return deleteByIndex(r'activityFacilityId', [activityFacilityId]);
  }

  bool deleteByActivityFacilityIdSync(String activityFacilityId) {
    return deleteByIndexSync(r'activityFacilityId', [activityFacilityId]);
  }

  Future<List<CacheActivityFacilityAsset?>> getAllByActivityFacilityId(
      List<String> activityFacilityIdValues) {
    final values = activityFacilityIdValues.map((e) => [e]).toList();
    return getAllByIndex(r'activityFacilityId', values);
  }

  List<CacheActivityFacilityAsset?> getAllByActivityFacilityIdSync(
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

  Future<Id> putByActivityFacilityId(CacheActivityFacilityAsset object) {
    return putByIndex(r'activityFacilityId', object);
  }

  Id putByActivityFacilityIdSync(CacheActivityFacilityAsset object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'activityFacilityId', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByActivityFacilityId(
      List<CacheActivityFacilityAsset> objects) {
    return putAllByIndex(r'activityFacilityId', objects);
  }

  List<Id> putAllByActivityFacilityIdSync(
      List<CacheActivityFacilityAsset> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'activityFacilityId', objects,
        saveLinks: saveLinks);
  }
}

extension CacheActivityFacilityAssetQueryWhereSort on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QWhere> {
  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheActivityFacilityAssetQueryWhere on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QWhereClause> {
  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterWhereClause> activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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
}

extension CacheActivityFacilityAssetQueryFilter on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QFilterCondition> {
  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> progressEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'progress',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> progressGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'progress',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> progressLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'progress',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> progressBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'progress',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterFilterCondition> updatedAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
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

extension CacheActivityFacilityAssetQueryObject on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QFilterCondition> {}

extension CacheActivityFacilityAssetQueryLinks on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QFilterCondition> {}

extension CacheActivityFacilityAssetQuerySortBy on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QSortBy> {
  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByProgress() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progress', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByProgressDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progress', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheActivityFacilityAssetQuerySortThenBy on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QSortThenBy> {
  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByProgress() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progress', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByProgressDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'progress', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheActivityFacilityAssetQueryWhereDistinct on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QDistinct> {
  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QDistinct> distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QDistinct> distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QDistinct> distinctByProgress() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'progress');
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, CacheActivityFacilityAsset,
      QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheActivityFacilityAssetQueryProperty on QueryBuilder<
    CacheActivityFacilityAsset, CacheActivityFacilityAsset, QQueryProperty> {
  QueryBuilder<CacheActivityFacilityAsset, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, int, QQueryOperations>
      progressProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'progress');
    });
  }

  QueryBuilder<CacheActivityFacilityAsset, DateTime, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }
}
