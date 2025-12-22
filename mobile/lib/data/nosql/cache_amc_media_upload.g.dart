// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_amc_media_upload.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheAmcMediaUploadCollection on Isar {
  IsarCollection<CacheAmcMediaUpload> get cacheAmcMediaUploads =>
      this.collection();
}

const CacheAmcMediaUploadSchema = CollectionSchema(
  name: r'CacheAmcMediaUpload',
  id: 3679139202516112680,
  properties: {
    r'createdAt': PropertySchema(
      id: 0,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'filePath': PropertySchema(
      id: 1,
      name: r'filePath',
      type: IsarType.string,
    ),
    r'itemNumber': PropertySchema(
      id: 2,
      name: r'itemNumber',
      type: IsarType.string,
    ),
    r'itemType': PropertySchema(
      id: 3,
      name: r'itemType',
      type: IsarType.string,
    ),
    r'latitude': PropertySchema(
      id: 4,
      name: r'latitude',
      type: IsarType.string,
    ),
    r'longitude': PropertySchema(
      id: 5,
      name: r'longitude',
      type: IsarType.string,
    ),
    r'scheduledVisitId': PropertySchema(
      id: 6,
      name: r'scheduledVisitId',
      type: IsarType.string,
    ),
    r'updatedAt': PropertySchema(
      id: 7,
      name: r'updatedAt',
      type: IsarType.dateTime,
    ),
    r'userType': PropertySchema(
      id: 8,
      name: r'userType',
      type: IsarType.string,
    )
  },
  estimateSize: _cacheAmcMediaUploadEstimateSize,
  serialize: _cacheAmcMediaUploadSerialize,
  deserialize: _cacheAmcMediaUploadDeserialize,
  deserializeProp: _cacheAmcMediaUploadDeserializeProp,
  idName: r'id',
  indexes: {
    r'scheduledVisitId': IndexSchema(
      id: -3342719759858217623,
      name: r'scheduledVisitId',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'scheduledVisitId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    ),
    r'userType': IndexSchema(
      id: -7871966206036222683,
      name: r'userType',
      unique: false,
      replace: false,
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
  getId: _cacheAmcMediaUploadGetId,
  getLinks: _cacheAmcMediaUploadGetLinks,
  attach: _cacheAmcMediaUploadAttach,
  version: '3.1.0+1',
);

int _cacheAmcMediaUploadEstimateSize(
  CacheAmcMediaUpload object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 + object.filePath.length * 3;
  bytesCount += 3 + object.itemNumber.length * 3;
  bytesCount += 3 + object.itemType.length * 3;
  bytesCount += 3 + object.latitude.length * 3;
  bytesCount += 3 + object.longitude.length * 3;
  bytesCount += 3 + object.scheduledVisitId.length * 3;
  bytesCount += 3 + object.userType.length * 3;
  return bytesCount;
}

void _cacheAmcMediaUploadSerialize(
  CacheAmcMediaUpload object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeDateTime(offsets[0], object.createdAt);
  writer.writeString(offsets[1], object.filePath);
  writer.writeString(offsets[2], object.itemNumber);
  writer.writeString(offsets[3], object.itemType);
  writer.writeString(offsets[4], object.latitude);
  writer.writeString(offsets[5], object.longitude);
  writer.writeString(offsets[6], object.scheduledVisitId);
  writer.writeDateTime(offsets[7], object.updatedAt);
  writer.writeString(offsets[8], object.userType);
}

CacheAmcMediaUpload _cacheAmcMediaUploadDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheAmcMediaUpload(
    filePath: reader.readString(offsets[1]),
    itemNumber: reader.readString(offsets[2]),
    itemType: reader.readString(offsets[3]),
    latitude: reader.readString(offsets[4]),
    longitude: reader.readString(offsets[5]),
    scheduledVisitId: reader.readString(offsets[6]),
    userType: reader.readString(offsets[8]),
  );
  object.createdAt = reader.readDateTime(offsets[0]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[7]);
  return object;
}

P _cacheAmcMediaUploadDeserializeProp<P>(
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
    case 2:
      return (reader.readString(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    case 4:
      return (reader.readString(offset)) as P;
    case 5:
      return (reader.readString(offset)) as P;
    case 6:
      return (reader.readString(offset)) as P;
    case 7:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 8:
      return (reader.readString(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheAmcMediaUploadGetId(CacheAmcMediaUpload object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheAmcMediaUploadGetLinks(
    CacheAmcMediaUpload object) {
  return [];
}

void _cacheAmcMediaUploadAttach(
    IsarCollection<dynamic> col, Id id, CacheAmcMediaUpload object) {
  object.id = id;
}

extension CacheAmcMediaUploadQueryWhereSort
    on QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QWhere> {
  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheAmcMediaUploadQueryWhere
    on QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QWhereClause> {
  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
      idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
      scheduledVisitIdEqualTo(String scheduledVisitId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'scheduledVisitId',
        value: [scheduledVisitId],
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
      scheduledVisitIdNotEqualTo(String scheduledVisitId) {
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
      userTypeEqualTo(String userType) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'userType',
        value: [userType],
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterWhereClause>
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

extension CacheAmcMediaUploadQueryFilter on QueryBuilder<CacheAmcMediaUpload,
    CacheAmcMediaUpload, QFilterCondition> {
  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'filePath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'filePath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'filePath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'filePath',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'filePath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'filePath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'filePath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'filePath',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'filePath',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      filePathIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'filePath',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemNumberContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'itemNumber',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemNumberMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'itemNumber',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemNumberIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'itemNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemNumberIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'itemNumber',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'itemType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'itemType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'itemType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'itemType',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'itemType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'itemType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'itemType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'itemType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'itemType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      itemTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'itemType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      latitudeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'latitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      latitudeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'latitude',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      latitudeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'latitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      latitudeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'latitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      longitudeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'longitude',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      longitudeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'longitude',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      longitudeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'longitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      longitudeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'longitude',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdEqualTo(
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdGreaterThan(
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdLessThan(
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdBetween(
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdStartsWith(
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdEndsWith(
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'scheduledVisitId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'scheduledVisitId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'scheduledVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      scheduledVisitIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'scheduledVisitId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
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

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      userTypeContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'userType',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      userTypeMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'userType',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      userTypeIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'userType',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterFilterCondition>
      userTypeIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'userType',
        value: '',
      ));
    });
  }
}

extension CacheAmcMediaUploadQueryObject on QueryBuilder<CacheAmcMediaUpload,
    CacheAmcMediaUpload, QFilterCondition> {}

extension CacheAmcMediaUploadQueryLinks on QueryBuilder<CacheAmcMediaUpload,
    CacheAmcMediaUpload, QFilterCondition> {}

extension CacheAmcMediaUploadQuerySortBy
    on QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QSortBy> {
  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByFilePath() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'filePath', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByFilePathDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'filePath', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByItemNumber() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByItemNumberDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByItemType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemType', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByItemTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemType', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByLatitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByLatitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByLongitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByLongitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByScheduledVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByScheduledVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      sortByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheAmcMediaUploadQuerySortThenBy
    on QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QSortThenBy> {
  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByFilePath() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'filePath', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByFilePathDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'filePath', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByItemNumber() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByItemNumberDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemNumber', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByItemType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemType', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByItemTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'itemType', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByLatitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByLatitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'latitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByLongitude() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByLongitudeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'longitude', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByScheduledVisitId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByScheduledVisitIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'scheduledVisitId', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByUserType() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.asc);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QAfterSortBy>
      thenByUserTypeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'userType', Sort.desc);
    });
  }
}

extension CacheAmcMediaUploadQueryWhereDistinct
    on QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct> {
  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByFilePath({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'filePath', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByItemNumber({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'itemNumber', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByItemType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'itemType', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByLatitude({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'latitude', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByLongitude({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'longitude', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByScheduledVisitId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'scheduledVisitId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QDistinct>
      distinctByUserType({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'userType', caseSensitive: caseSensitive);
    });
  }
}

extension CacheAmcMediaUploadQueryProperty
    on QueryBuilder<CacheAmcMediaUpload, CacheAmcMediaUpload, QQueryProperty> {
  QueryBuilder<CacheAmcMediaUpload, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      filePathProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'filePath');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      itemNumberProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'itemNumber');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      itemTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'itemType');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      latitudeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'latitude');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      longitudeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'longitude');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      scheduledVisitIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'scheduledVisitId');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheAmcMediaUpload, String, QQueryOperations>
      userTypeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'userType');
    });
  }
}
