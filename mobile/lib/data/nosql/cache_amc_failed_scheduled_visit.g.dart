// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_amc_failed_scheduled_visit.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAmcFailedScheduledVisitCollection on Isar {
  IsarCollection<CacheAmcFailedScheduledVisit>
      get cacheAmcFailedScheduledVisits => this.collection();
}

const CacheAmcFailedScheduledVisitSchema = CollectionSchema(
  name: r'CacheAmcFailedScheduledVisit',
  id: 8204134071920357736,
  properties: {
    r'scheduledVisitId': PropertySchema(
      id: 0,
      name: r'scheduledVisitId',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheAmcFailedScheduledVisitEstimateSize,
  serialize: _cacheAmcFailedScheduledVisitSerialize,
  deserialize: _cacheAmcFailedScheduledVisitDeserialize,
  deserializeProp: _cacheAmcFailedScheduledVisitDeserializeProp,
  idName: r'id',
  indexes: {
    r'scheduledVisitId': IndexSchema(
      id: -3342719759858217623,
      name: r'scheduledVisitId',
      unique: true,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'scheduledVisitId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheAmcFailedScheduledVisitGetId,
  getLinks: _cacheAmcFailedScheduledVisitGetLinks,
  attach: _cacheAmcFailedScheduledVisitAttach,
  version: '3.1.0+1',
);

int _cacheAmcFailedScheduledVisitEstimateSize(
  CacheAmcFailedScheduledVisit object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.scheduledVisitId.length * 3;
  return bytesCount;
}

void _cacheAmcFailedScheduledVisitSerialize(
  CacheAmcFailedScheduledVisit object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.scheduledVisitId);
}

CacheAmcFailedScheduledVisit _cacheAmcFailedScheduledVisitDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAmcFailedScheduledVisit(
    scheduledVisitId: reader.readString(offsets[0]),
  );
  object.id = id;
  return object;
}

P _cacheAmcFailedScheduledVisitDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readString(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAmcFailedScheduledVisitGetId(CacheAmcFailedScheduledVisit object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAmcFailedScheduledVisitGetLinks(
    CacheAmcFailedScheduledVisit object) {
  return [];
}

void _cacheAmcFailedScheduledVisitAttach(
    IsarCollection<dynamic> col, Id id, CacheAmcFailedScheduledVisit object) {
  object.id = id;
}

extension CacheAmcFailedScheduledVisitByIndex
    on IsarCollection<CacheAmcFailedScheduledVisit> {
  Future<CacheAmcFailedScheduledVisit?> getByScheduledVisitId(
      String scheduledVisitId) {
    return getByIndex(r'scheduledVisitId', [scheduledVisitId]);
  }

  CacheAmcFailedScheduledVisit? getByScheduledVisitIdSync(
      String scheduledVisitId) {
    return getByIndexSync(r'scheduledVisitId', [scheduledVisitId]);
  }

  Future<bool> deleteByScheduledVisitId(String scheduledVisitId) {
    return deleteByIndex(r'scheduledVisitId', [scheduledVisitId]);
  }

  bool deleteByScheduledVisitIdSync(String scheduledVisitId) {
    return deleteByIndexSync(r'scheduledVisitId', [scheduledVisitId]);
  }

  Future<List<CacheAmcFailedScheduledVisit?>> getAllByScheduledVisitId(
      List<String> scheduledVisitIdValues) {
    final values = scheduledVisitIdValues.map((e) => [e]).toList();
    return getAllByIndex(r'scheduledVisitId', values);
  }

  List<CacheAmcFailedScheduledVisit?> getAllByScheduledVisitIdSync(
      List<String> scheduledVisitIdValues) {
    final values = scheduledVisitIdValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'scheduledVisitId', values);
  }

  Future<int> deleteAllByScheduledVisitId(List<String> scheduledVisitIdValues) {
    final values = scheduledVisitIdValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'scheduledVisitId', values);
  }

  int deleteAllByScheduledVisitIdSync(List<String> scheduledVisitIdValues) {
    final values = scheduledVisitIdValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'scheduledVisitId', values);
  }

  Future<Id> putByScheduledVisitId(CacheAmcFailedScheduledVisit object) {
    return putByIndex(r'scheduledVisitId', object);
  }

  Id putByScheduledVisitIdSync(CacheAmcFailedScheduledVisit object,
      {bool saveLinks = true}) {
    return putByIndexSync(r'scheduledVisitId', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByScheduledVisitId(
      List<CacheAmcFailedScheduledVisit> objects) {
    return putAllByIndex(r'scheduledVisitId', objects);
  }

  List<Id> putAllByScheduledVisitIdSync(
      List<CacheAmcFailedScheduledVisit> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'scheduledVisitId', objects,
        saveLinks: saveLinks);
  }
}

extension CacheAmcFailedScheduledVisitQueryWhereSort on QueryBuilder<
    CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit, QWhere> {
  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheAmcFailedScheduledVisitQueryWhere on QueryBuilder<
    CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit, QWhereClause> {
  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterWhereClause> scheduledVisitIdEqualTo(String scheduledVisitId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'scheduledVisitId',
        value: [scheduledVisitId],
      ));
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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
}

extension CacheAmcFailedScheduledVisitQueryFilter on QueryBuilder<
    CacheAmcFailedScheduledVisit,
    CacheAmcFailedScheduledVisit,
    QFilterCondition> {
  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
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

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterFilterCondition> scheduledVisitIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduledVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterFilterCondition> scheduledVisitIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'scheduledVisitId',
        value: '',
      ));
    });
  }
}

extension CacheAmcFailedScheduledVisitQueryObject on QueryBuilder<
    CacheAmcFailedScheduledVisit,
    CacheAmcFailedScheduledVisit,
    QFilterCondition> {}

extension CacheAmcFailedScheduledVisitQueryLinks on QueryBuilder<
    CacheAmcFailedScheduledVisit,
    CacheAmcFailedScheduledVisit,
    QFilterCondition> {}

extension CacheAmcFailedScheduledVisitQuerySortBy on QueryBuilder<
    CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit, QSortBy> {
  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterSortBy> sortByScheduledVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterSortBy> sortByScheduledVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.desc);
    });
  }
}

extension CacheAmcFailedScheduledVisitQuerySortThenBy on QueryBuilder<
    CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit, QSortThenBy> {
  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterSortBy> thenByScheduledVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QAfterSortBy> thenByScheduledVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.desc);
    });
  }
}

extension CacheAmcFailedScheduledVisitQueryWhereDistinct on QueryBuilder<
    CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit, QDistinct> {
  QueryBuilder<CacheAmcFailedScheduledVisit, CacheAmcFailedScheduledVisit,
      QDistinct> distinctByScheduledVisitId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'scheduledVisitId',
          caseSensitive: caseSensitive);
    });
  }
}

extension CacheAmcFailedScheduledVisitQueryProperty on QueryBuilder<
    CacheAmcFailedScheduledVisit,
    CacheAmcFailedScheduledVisit,
    QQueryProperty> {
  QueryBuilder<CacheAmcFailedScheduledVisit, int, QQueryOperations>
      idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAmcFailedScheduledVisit, String, QQueryOperations>
      scheduledVisitIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'scheduledVisitId');
    });
  }
}
