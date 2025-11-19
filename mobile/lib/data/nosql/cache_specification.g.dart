// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_specification.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheSpecificationCollection on Isar {
  IsarCollection<CacheSpecification> get cacheSpecifications =>
      this.collection();
}

const CacheSpecificationSchema = CollectionSchema(
  name: r'CacheSpecification',
  id: 2458590090407296274,
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
    r'createdAt': PropertySchema(
      id: 2,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'system': PropertySchema(
      id: 3,
      name: r'system',
      type: IsarType.string,
    ),
    r'totalCapacity': PropertySchema(
      id: 4,
      name: r'totalCapacity',
      type: IsarType.double,
    ),
    r'totalCapacityUnit': PropertySchema(
      id: 5,
      name: r'totalCapacityUnit',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 6,
      name: r'updatedAt',
      type: IsarType.dateTime,
    )
  },
  estimateSize: _cacheSpecificationEstimateSize,
  serialize: _cacheSpecificationSerialize,
  deserialize: _cacheSpecificationDeserialize,
  deserializeProp: _cacheSpecificationDeserializeProp,
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
  getId: _cacheSpecificationGetId,
  getLinks: _cacheSpecificationGetLinks,
  attach: _cacheSpecificationAttach,
  version: '3.1.0+1',
);

int _cacheSpecificationEstimateSize(
  CacheSpecification object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  bytesCount += 3 + object.assetType.length * 3;
  bytesCount += 3 + object.system.length * 3;
  bytesCount += 3 + object.totalCapacityUnit.length * 3;
  return bytesCount;
}

void _cacheSpecificationSerialize(
  CacheSpecification object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeString(offsets[1], object.assetType);
  writer.writeDateTime(offsets[2], object.createdAt);
  writer.writeString(offsets[3], object.system);
  writer.writeDouble(offsets[4], object.totalCapacity);
  writer.writeString(offsets[5], object.totalCapacityUnit);
  writer.writeDateTime(offsets[6], object.updatedAt);
}

CacheSpecification _cacheSpecificationDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheSpecification(
    activityFacilityId: reader.readString(offsets[0]),
    assetType: reader.readString(offsets[1]),
    system: reader.readString(offsets[3]),
    totalCapacity: reader.readDouble(offsets[4]),
    totalCapacityUnit: reader.readString(offsets[5]),
  );
  object.createdAt = reader.readDateTime(offsets[2]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[6]);
  return object;
}

P _cacheSpecificationDeserializeProp<P>(
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
      return (reader.readDateTime(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    case 4:
      return (reader.readDouble(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    case 6:
      return (reader.readDateTimeOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheSpecificationGetId(CacheSpecification object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheSpecificationGetLinks(
    CacheSpecification object) {
  return [];
}

void _cacheSpecificationAttach(
    IsarCollection<dynamic> col, Id id, CacheSpecification object) {
  object.id = id;
}

extension CacheSpecificationQueryWhereSort
    on QueryBuilder<CacheSpecification, CacheSpecification, QWhere> {
  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheSpecificationQueryWhere
    on QueryBuilder<CacheSpecification, CacheSpecification, QWhereClause> {
  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
      idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
      activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
      assetTypeEqualTo(String assetType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'assetType',
        value: [assetType],
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterWhereClause>
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

extension CacheSpecificationQueryFilter
    on QueryBuilder<CacheSpecification, CacheSpecification, QFilterCondition> {
  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      activityFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      activityFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      assetTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      assetTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assetType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      assetTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assetType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      assetTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assetType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'system',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'system',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'system',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'system',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'system',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'system',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'system',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'system',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'system',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      systemIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'system',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityEqualTo(
    double value, {
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'totalCapacity',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityGreaterThan(
    double value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'totalCapacity',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityLessThan(
    double value, {
    bool include = false,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'totalCapacity',
        value: value,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityBetween(
    double lower,
    double upper, {
    bool includeLower = true,
    bool includeUpper = true,
    double epsilon = Query.epsilon,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'totalCapacity',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        epsilon: epsilon,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'totalCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'totalCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'totalCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'totalCapacityUnit',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'totalCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'totalCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'totalCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'totalCapacityUnit',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'totalCapacityUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      totalCapacityUnitIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'totalCapacityUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
      updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterFilterCondition>
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
}

extension CacheSpecificationQueryObject
    on QueryBuilder<CacheSpecification, CacheSpecification, QFilterCondition> {}

extension CacheSpecificationQueryLinks
    on QueryBuilder<CacheSpecification, CacheSpecification, QFilterCondition> {}

extension CacheSpecificationQuerySortBy
    on QueryBuilder<CacheSpecification, CacheSpecification, QSortBy> {
  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByAssetType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByAssetTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortBySystem() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'system', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortBySystemDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'system', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByTotalCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByTotalCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByTotalCapacityUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacityUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByTotalCapacityUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacityUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheSpecificationQuerySortThenBy
    on QueryBuilder<CacheSpecification, CacheSpecification, QSortThenBy> {
  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByAssetType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByAssetTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenBySystem() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'system', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenBySystemDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'system', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByTotalCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByTotalCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByTotalCapacityUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacityUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByTotalCapacityUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'totalCapacityUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QAfterSortBy>
      thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheSpecificationQueryWhereDistinct
    on QueryBuilder<CacheSpecification, CacheSpecification, QDistinct> {
  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctByAssetType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assetType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctBySystem({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'system', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctByTotalCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'totalCapacity');
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctByTotalCapacityUnit({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'totalCapacityUnit',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheSpecification, CacheSpecification, QDistinct>
      distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheSpecificationQueryProperty
    on QueryBuilder<CacheSpecification, CacheSpecification, QQueryProperty> {
  QueryBuilder<CacheSpecification, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheSpecification, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheSpecification, String, QQueryOperations>
      assetTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assetType');
    });
  }

  QueryBuilder<CacheSpecification, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheSpecification, String, QQueryOperations> systemProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'system');
    });
  }

  QueryBuilder<CacheSpecification, double, QQueryOperations>
      totalCapacityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'totalCapacity');
    });
  }

  QueryBuilder<CacheSpecification, String, QQueryOperations>
      totalCapacityUnitProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'totalCapacityUnit');
    });
  }

  QueryBuilder<CacheSpecification, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }
}
