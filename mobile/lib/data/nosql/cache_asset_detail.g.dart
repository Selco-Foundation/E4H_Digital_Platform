// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_asset_detail.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAssetDetailCollection on Isar {
  IsarCollection<CacheAssetDetail> get cacheAssetDetails => this.collection();
}

const CacheAssetDetailSchema = CollectionSchema(
  name: r'CacheAssetDetail',
  id: 5388707521120749043,
  properties: {
    r'activityFacilityId': PropertySchema(
      id: 0,
      name: r'activityFacilityId',
      type: IsarType.string,
    ),
    r'assetType': PropertySchema(
      id: 1,
      name: r'assetType',
      type: IsarType.string,
    ),
    r'brand': PropertySchema(
      id: 2,
      name: r'brand',
      type: IsarType.string,
    ),
    r'createdAt': PropertySchema(
      id: 3,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'model': PropertySchema(
      id: 4,
      name: r'model',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 5,
      name: r'updatedAt',
      type: IsarType.dateTime,
    ),
    r'warranty': PropertySchema(
      id: 6,
      name: r'warranty',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheAssetDetailEstimateSize,
  serialize: _cacheAssetDetailSerialize,
  deserialize: _cacheAssetDetailDeserialize,
  deserializeProp: _cacheAssetDetailDeserializeProp,
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
          caseSensitive: true,
        )
      ],
    ),
    r'assetType': IndexSchema(
      id: 2557228192997929194,
      name: r'assetType',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'assetType',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _cacheAssetDetailGetId,
  getLinks: _cacheAssetDetailGetLinks,
  attach: _cacheAssetDetailAttach,
  version: '3.1.0+1',
);

int _cacheAssetDetailEstimateSize(
  CacheAssetDetail object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  bytesCount += 3 + object.assetType.length * 3;
  bytesCount += 3 + object.brand.length * 3;
  {
    final value = object.model;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.warranty;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _cacheAssetDetailSerialize(
  CacheAssetDetail object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeString(offsets[1], object.assetType);
  writer.writeString(offsets[2], object.brand);
  writer.writeDateTime(offsets[3], object.createdAt);
  writer.writeString(offsets[4], object.model);
  writer.writeDateTime(offsets[5], object.updatedAt);
  writer.writeString(offsets[6], object.warranty);
}

CacheAssetDetail _cacheAssetDetailDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAssetDetail(
    activityFacilityId: reader.readString(offsets[0]),
    assetType: reader.readString(offsets[1]),
    brand: reader.readString(offsets[2]),
    model: reader.readStringOrNull(offsets[4]),
    warranty: reader.readStringOrNull(offsets[6]),
  );
  object.createdAt = reader.readDateTime(offsets[3]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[5]);
  return object;
}

P _cacheAssetDetailDeserializeProp<P>(
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
      return (reader.readDateTime(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAssetDetailGetId(CacheAssetDetail object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAssetDetailGetLinks(CacheAssetDetail object) {
  return [];
}

void _cacheAssetDetailAttach(
    IsarCollection<dynamic> col, Id id, CacheAssetDetail object) {
  object.id = id;
}

extension CacheAssetDetailQueryWhereSort
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QWhere> {
  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheAssetDetailQueryWhere
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QWhereClause> {
  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause> idEqualTo(
      Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause> idBetween(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
      activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
      assetTypeEqualTo(String assetType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'assetType',
        value: [assetType],
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterWhereClause>
      assetTypeNotEqualTo(String assetType) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assetType',
              lower: [],
              upper: [assetType],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assetType',
              lower: [assetType],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assetType',
              lower: [assetType],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'assetType',
              lower: [],
              upper: [assetType],
              includeUpper: false,
            ));
      }
    });
  }
}

extension CacheAssetDetailQueryFilter
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QFilterCondition> {
  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      activityFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      activityFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'assetType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assetType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assetType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      assetTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assetType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'brand',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'brand',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'brand',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'brand',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'brand',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'brand',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'brand',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'brand',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'brand',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      brandIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'brand',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      createdAtGreaterThan(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      createdAtLessThan(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      createdAtBetween(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'model',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'model',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'model',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'model',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'model',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'model',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'model',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'model',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'model',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'model',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'model',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      modelIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'model',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      updatedAtGreaterThan(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      updatedAtLessThan(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      updatedAtBetween(
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

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'warranty',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'warranty',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'warranty',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'warranty',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'warranty',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'warranty',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'warranty',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'warranty',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'warranty',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'warranty',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'warranty',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterFilterCondition>
      warrantyIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'warranty',
        value: '',
      ));
    });
  }
}

extension CacheAssetDetailQueryObject
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QFilterCondition> {}

extension CacheAssetDetailQueryLinks
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QFilterCondition> {}

extension CacheAssetDetailQuerySortBy
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QSortBy> {
  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByAssetType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByAssetTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy> sortByBrand() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'brand', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByBrandDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'brand', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy> sortByModel() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'model', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByModelDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'model', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByWarranty() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'warranty', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      sortByWarrantyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'warranty', Sort.desc);
    });
  }
}

extension CacheAssetDetailQuerySortThenBy
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QSortThenBy> {
  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByAssetType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByAssetTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy> thenByBrand() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'brand', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByBrandDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'brand', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy> thenByModel() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'model', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByModelDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'model', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByWarranty() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'warranty', Sort.asc);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QAfterSortBy>
      thenByWarrantyDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'warranty', Sort.desc);
    });
  }
}

extension CacheAssetDetailQueryWhereDistinct
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct> {
  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct>
      distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct>
      distinctByAssetType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assetType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct> distinctByBrand(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'brand', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct>
      distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct> distinctByModel(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'model', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct>
      distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<CacheAssetDetail, CacheAssetDetail, QDistinct>
      distinctByWarranty({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'warranty', caseSensitive: caseSensitive);
    });
  }
}

extension CacheAssetDetailQueryProperty
    on QueryBuilder<CacheAssetDetail, CacheAssetDetail, QQueryProperty> {
  QueryBuilder<CacheAssetDetail, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAssetDetail, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheAssetDetail, String, QQueryOperations> assetTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assetType');
    });
  }

  QueryBuilder<CacheAssetDetail, String, QQueryOperations> brandProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'brand');
    });
  }

  QueryBuilder<CacheAssetDetail, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheAssetDetail, String?, QQueryOperations> modelProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'model');
    });
  }

  QueryBuilder<CacheAssetDetail, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheAssetDetail, String?, QQueryOperations> warrantyProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'warranty');
    });
  }
}
