// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_add_new_asset.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAddNewAssetCollection on Isar {
  IsarCollection<CacheAddNewAsset> get cacheAddNewAssets => this.collection();
}

const CacheAddNewAssetSchema = CollectionSchema(
  name: r'CacheAddNewAsset',
  id: -3761941355099299172,
  properties: {
    r'activityFacilityId': PropertySchema(
      id: 0,
      name: r'activityFacilityId',
      type: IsarType.string,
    ),
    r'assetId': PropertySchema(
      id: 1,
      name: r'assetId',
      type: IsarType.string,
    ),
    r'assetType': PropertySchema(
      id: 2,
      name: r'assetType',
      type: IsarType.string,
    ),
    r'batteryCapacity': PropertySchema(
      id: 3,
      name: r'batteryCapacity',
      type: IsarType.string,
    ),
    r'batteryType': PropertySchema(
      id: 4,
      name: r'batteryType',
      type: IsarType.string,
    ),
    r'batteryVoltage': PropertySchema(
      id: 5,
      name: r'batteryVoltage',
      type: IsarType.string,
    ),
    r'capacity': PropertySchema(
      id: 6,
      name: r'capacity',
      type: IsarType.string,
    ),
    r'capacityUnit': PropertySchema(
      id: 7,
      name: r'capacityUnit',
      type: IsarType.string,
    ),
    r'createdAt': PropertySchema(
      id: 8,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'currentUnit': PropertySchema(
      id: 9,
      name: r'currentUnit',
      type: IsarType.string,
    ),
    r'documentType': PropertySchema(
      id: 10,
      name: r'documentType',
      type: IsarType.string,
    ),
    r'inverterCapacity': PropertySchema(
      id: 11,
      name: r'inverterCapacity',
      type: IsarType.string,
    ),
    r'inverterCapacityUnit': PropertySchema(
      id: 12,
      name: r'inverterCapacityUnit',
      type: IsarType.string,
    ),
    r'itemNumber': PropertySchema(
      id: 13,
      name: r'itemNumber',
      type: IsarType.string,
    ),
    r'latitude': PropertySchema(
      id: 14,
      name: r'latitude',
      type: IsarType.string,
    ),
    r'longitude': PropertySchema(
      id: 15,
      name: r'longitude',
      type: IsarType.string,
    ),
    r'panelCapacity': PropertySchema(
      id: 16,
      name: r'panelCapacity',
      type: IsarType.string,
    ),
    r'photoPath': PropertySchema(
      id: 17,
      name: r'photoPath',
      type: IsarType.string,
    ),
    r'serialNumber': PropertySchema(
      id: 18,
      name: r'serialNumber',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 19,
      name: r'updatedAt',
      type: IsarType.dateTime,
    ),
    r'voltageUnit': PropertySchema(
      id: 20,
      name: r'voltageUnit',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheAddNewAssetEstimateSize,
  serialize: _cacheAddNewAssetSerialize,
  deserialize: _cacheAddNewAssetDeserialize,
  deserializeProp: _cacheAddNewAssetDeserializeProp,
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
  getId: _cacheAddNewAssetGetId,
  getLinks: _cacheAddNewAssetGetLinks,
  attach: _cacheAddNewAssetAttach,
  version: '3.1.0+1',
);

int _cacheAddNewAssetEstimateSize(
  CacheAddNewAsset object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.activityFacilityId.length * 3;
  {
    final value = object.assetId;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.assetType.length * 3;
  {
    final value = object.batteryCapacity;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.batteryType;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.batteryVoltage;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.capacity.length * 3;
  {
    final value = object.capacityUnit;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.currentUnit;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.documentType;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.inverterCapacity;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  {
    final value = object.inverterCapacityUnit;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.itemNumber.length * 3;
  bytesCount += 3 + object.latitude.length * 3;
  bytesCount += 3 + object.longitude.length * 3;
  {
    final value = object.panelCapacity;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.photoPath.length * 3;
  bytesCount += 3 + object.serialNumber.length * 3;
  {
    final value = object.voltageUnit;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _cacheAddNewAssetSerialize(
  CacheAddNewAsset object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.activityFacilityId);
  writer.writeString(offsets[1], object.assetId);
  writer.writeString(offsets[2], object.assetType);
  writer.writeString(offsets[3], object.batteryCapacity);
  writer.writeString(offsets[4], object.batteryType);
  writer.writeString(offsets[5], object.batteryVoltage);
  writer.writeString(offsets[6], object.capacity);
  writer.writeString(offsets[7], object.capacityUnit);
  writer.writeDateTime(offsets[8], object.createdAt);
  writer.writeString(offsets[9], object.currentUnit);
  writer.writeString(offsets[10], object.documentType);
  writer.writeString(offsets[11], object.inverterCapacity);
  writer.writeString(offsets[12], object.inverterCapacityUnit);
  writer.writeString(offsets[13], object.itemNumber);
  writer.writeString(offsets[14], object.latitude);
  writer.writeString(offsets[15], object.longitude);
  writer.writeString(offsets[16], object.panelCapacity);
  writer.writeString(offsets[17], object.photoPath);
  writer.writeString(offsets[18], object.serialNumber);
  writer.writeDateTime(offsets[19], object.updatedAt);
  writer.writeString(offsets[20], object.voltageUnit);
}

CacheAddNewAsset _cacheAddNewAssetDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAddNewAsset(
    activityFacilityId: reader.readString(offsets[0]),
    assetId: reader.readStringOrNull(offsets[1]),
    assetType: reader.readString(offsets[2]),
    batteryCapacity: reader.readStringOrNull(offsets[3]),
    batteryType: reader.readStringOrNull(offsets[4]),
    batteryVoltage: reader.readStringOrNull(offsets[5]),
    capacity: reader.readStringOrNull(offsets[6]) ?? '1',
    capacityUnit: reader.readStringOrNull(offsets[7]),
    currentUnit: reader.readStringOrNull(offsets[9]),
    documentType: reader.readStringOrNull(offsets[10]),
    inverterCapacity: reader.readStringOrNull(offsets[11]),
    inverterCapacityUnit: reader.readStringOrNull(offsets[12]),
    itemNumber: reader.readString(offsets[13]),
    latitude: reader.readString(offsets[14]),
    longitude: reader.readString(offsets[15]),
    panelCapacity: reader.readStringOrNull(offsets[16]),
    photoPath: reader.readString(offsets[17]),
    serialNumber: reader.readString(offsets[18]),
    voltageUnit: reader.readStringOrNull(offsets[20]),
  );
  object.createdAt = reader.readDateTime(offsets[8]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[19]);
  return object;
}

P _cacheAddNewAssetDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readString(offset)) as P;
    case 1:
      return (reader.readStringOrNull(offset)) as P;
    case 2:
      return (reader.readString(offset)) as P;
    case 3:
      return (reader.readStringOrNull(offset)) as P;
    case 4:
      return (reader.readStringOrNull(offset)) as P;
    case 5:
      return (reader.readStringOrNull(offset)) as P;
    case 6:
      return (reader.readStringOrNull(offset) ?? '1') as P;
    case 7:
      return (reader.readStringOrNull(offset)) as P;
    case 8:
      return (reader.readDateTime(offset)) as P;
    case 9:
      return (reader.readStringOrNull(offset)) as P;
    case 10:
      return (reader.readStringOrNull(offset)) as P;
    case 11:
      return (reader.readStringOrNull(offset)) as P;
    case 12:
      return (reader.readStringOrNull(offset)) as P;
    case 13:
      return (reader.readString(offset)) as P;
    case 14:
      return (reader.readString(offset)) as P;
    case 15:
      return (reader.readString(offset)) as P;
    case 16:
      return (reader.readStringOrNull(offset)) as P;
    case 17:
      return (reader.readString(offset)) as P;
    case 18:
      return (reader.readString(offset)) as P;
    case 19:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 20:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAddNewAssetGetId(CacheAddNewAsset object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAddNewAssetGetLinks(CacheAddNewAsset object) {
  return [];
}

void _cacheAddNewAssetAttach(
    IsarCollection<dynamic> col, Id id, CacheAddNewAsset object) {
  object.id = id;
}

extension CacheAddNewAssetQueryWhereSort
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QWhere> {
  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheAddNewAssetQueryWhere
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QWhereClause> {
  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause> idEqualTo(
      Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause> idBetween(
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
      activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
      assetTypeEqualTo(String assetType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'assetType',
        value: [assetType],
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterWhereClause>
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

extension CacheAddNewAssetQueryFilter
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QFilterCondition> {
  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      activityFacilityIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'activityFacilityId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      activityFacilityIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'activityFacilityId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'assetId',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'assetId',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assetId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'assetId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'assetId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'assetId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'assetId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'assetId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assetId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assetId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assetId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assetId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'assetType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'assetType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'assetType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      assetTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'assetType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'batteryCapacity',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'batteryCapacity',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'batteryCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'batteryCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'batteryCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'batteryCapacity',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'batteryCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'batteryCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'batteryCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'batteryCapacity',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'batteryCapacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryCapacityIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'batteryCapacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'batteryType',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'batteryType',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'batteryType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'batteryType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'batteryType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'batteryType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'batteryType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'batteryType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'batteryType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'batteryType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'batteryType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'batteryType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'batteryVoltage',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'batteryVoltage',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'batteryVoltage',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'batteryVoltage',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'batteryVoltage',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'batteryVoltage',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'batteryVoltage',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'batteryVoltage',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'batteryVoltage',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'batteryVoltage',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'batteryVoltage',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      batteryVoltageIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'batteryVoltage',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'capacity',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'capacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'capacity',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'capacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'capacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'capacityUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'capacityUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'capacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'capacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'capacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'capacityUnit',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'capacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'capacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'capacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'capacityUnit',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'capacityUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      capacityUnitIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'capacityUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'currentUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'currentUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'currentUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'currentUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'currentUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'currentUnit',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'currentUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'currentUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'currentUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'currentUnit',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'currentUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      currentUnitIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'currentUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'documentType',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'documentType',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'documentType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'documentType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'documentType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'documentType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'documentType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'documentType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'documentType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'documentType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'documentType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      documentTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'documentType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'inverterCapacity',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'inverterCapacity',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'inverterCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'inverterCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'inverterCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'inverterCapacity',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'inverterCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'inverterCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'inverterCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'inverterCapacity',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'inverterCapacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'inverterCapacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'inverterCapacityUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'inverterCapacityUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'inverterCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'inverterCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'inverterCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'inverterCapacityUnit',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'inverterCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'inverterCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'inverterCapacityUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'inverterCapacityUnit',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'inverterCapacityUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      inverterCapacityUnitIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'inverterCapacityUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'itemNumber',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'itemNumber',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'itemNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      itemNumberIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'itemNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'latitude',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'latitude',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'latitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      latitudeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'latitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'longitude',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'longitude',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'longitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      longitudeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'longitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'panelCapacity',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'panelCapacity',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'panelCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'panelCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'panelCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'panelCapacity',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'panelCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'panelCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'panelCapacity',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'panelCapacity',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'panelCapacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      panelCapacityIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'panelCapacity',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'photoPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'photoPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'photoPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'photoPath',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'photoPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'photoPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'photoPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'photoPath',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'photoPath',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      photoPathIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'photoPath',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'serialNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'serialNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'serialNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'serialNumber',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'serialNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'serialNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'serialNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'serialNumber',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'serialNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      serialNumberIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'serialNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
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

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'voltageUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'voltageUnit',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'voltageUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'voltageUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'voltageUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'voltageUnit',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'voltageUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'voltageUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'voltageUnit',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'voltageUnit',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'voltageUnit',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterFilterCondition>
      voltageUnitIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'voltageUnit',
        value: '',
      ));
    });
  }
}

extension CacheAddNewAssetQueryObject
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QFilterCondition> {}

extension CacheAddNewAssetQueryLinks
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QFilterCondition> {}

extension CacheAddNewAssetQuerySortBy
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QSortBy> {
  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByAssetId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetId', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByAssetIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetId', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByAssetType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByAssetTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByBatteryCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByBatteryCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByBatteryType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryType', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByBatteryTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryType', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByBatteryVoltage() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryVoltage', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByBatteryVoltageDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryVoltage', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCapacityUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacityUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCapacityUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacityUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCurrentUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'currentUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByCurrentUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'currentUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByDocumentType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'documentType', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByDocumentTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'documentType', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByInverterCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByInverterCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByInverterCapacityUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacityUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByInverterCapacityUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacityUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByItemNumber() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByItemNumberDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByLatitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByLatitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByLongitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByLongitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByPanelCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'panelCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByPanelCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'panelCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByPhotoPath() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'photoPath', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByPhotoPathDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'photoPath', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortBySerialNumber() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serialNumber', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortBySerialNumberDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serialNumber', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByVoltageUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'voltageUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      sortByVoltageUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'voltageUnit', Sort.desc);
    });
  }
}

extension CacheAddNewAssetQuerySortThenBy
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QSortThenBy> {
  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByAssetId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetId', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByAssetIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetId', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByAssetType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByAssetTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'assetType', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByBatteryCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByBatteryCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByBatteryType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryType', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByBatteryTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryType', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByBatteryVoltage() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryVoltage', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByBatteryVoltageDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'batteryVoltage', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCapacityUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacityUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCapacityUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'capacityUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCurrentUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'currentUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByCurrentUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'currentUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByDocumentType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'documentType', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByDocumentTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'documentType', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByInverterCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByInverterCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByInverterCapacityUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacityUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByInverterCapacityUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'inverterCapacityUnit', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByItemNumber() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByItemNumberDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByLatitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByLatitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByLongitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByLongitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByPanelCapacity() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'panelCapacity', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByPanelCapacityDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'panelCapacity', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByPhotoPath() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'photoPath', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByPhotoPathDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'photoPath', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenBySerialNumber() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serialNumber', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenBySerialNumberDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'serialNumber', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByVoltageUnit() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'voltageUnit', Sort.asc);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QAfterSortBy>
      thenByVoltageUnitDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'voltageUnit', Sort.desc);
    });
  }
}

extension CacheAddNewAssetQueryWhereDistinct
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct> {
  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct> distinctByAssetId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assetId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByAssetType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'assetType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByBatteryCapacity({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'batteryCapacity',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByBatteryType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'batteryType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByBatteryVoltage({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'batteryVoltage',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByCapacity({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'capacity', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByCapacityUnit({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'capacityUnit', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByCurrentUnit({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'currentUnit', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByDocumentType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'documentType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByInverterCapacity({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'inverterCapacity',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByInverterCapacityUnit({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'inverterCapacityUnit',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByItemNumber({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'itemNumber', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByLatitude({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'latitude', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByLongitude({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'longitude', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByPanelCapacity({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'panelCapacity',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByPhotoPath({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'photoPath', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctBySerialNumber({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'serialNumber', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QDistinct>
      distinctByVoltageUnit({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'voltageUnit', caseSensitive: caseSensitive);
    });
  }
}

extension CacheAddNewAssetQueryProperty
    on QueryBuilder<CacheAddNewAsset, CacheAddNewAsset, QQueryProperty> {
  QueryBuilder<CacheAddNewAsset, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations> assetIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assetId');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations> assetTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'assetType');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      batteryCapacityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'batteryCapacity');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      batteryTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'batteryType');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      batteryVoltageProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'batteryVoltage');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations> capacityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'capacity');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      capacityUnitProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'capacityUnit');
    });
  }

  QueryBuilder<CacheAddNewAsset, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      currentUnitProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'currentUnit');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      documentTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'documentType');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      inverterCapacityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'inverterCapacity');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      inverterCapacityUnitProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'inverterCapacityUnit');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations>
      itemNumberProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'itemNumber');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations> latitudeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'latitude');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations> longitudeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'longitude');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      panelCapacityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'panelCapacity');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations> photoPathProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'photoPath');
    });
  }

  QueryBuilder<CacheAddNewAsset, String, QQueryOperations>
      serialNumberProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'serialNumber');
    });
  }

  QueryBuilder<CacheAddNewAsset, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheAddNewAsset, String?, QQueryOperations>
      voltageUnitProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'voltageUnit');
    });
  }
}
