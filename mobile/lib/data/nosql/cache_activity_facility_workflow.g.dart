// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cache_activity_facility_workflow.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetCacheActivityFacilityWorkflowCollection on Isar {
  IsarCollection<CacheActivityFacilityWorkflow>
      get cacheActivityFacilityWorkflows => this.collection();
}

const CacheActivityFacilityWorkflowSchema = CollectionSchema(
  name: r'CacheActivityFacilityWorkflow',
  id: 7629554972142841863,
  properties: {
    r'activityFacility': PropertySchema(
      id: 0,
      name: r'activityFacility',
      type: IsarType.object,
      target: r'ActivityFacility',
    ),
    r'activityFacilityId': PropertySchema(
      id: 1,
      name: r'activityFacilityId',
      type: IsarType.string,
    ),
    r'createdAt': PropertySchema(
      id: 2,
      name: r'createdAt',
      type: IsarType.dateTime,
    ),
    r'status': PropertySchema(
      id: 3,
      name: r'status',
      type: IsarType.string,
    ),
    r'transactions': PropertySchema(
      id: 4,
      name: r'transactions',
      type: IsarType.objectList,
      target: r'Transaction',
    ),
    r'updatedAt': PropertySchema(
      id: 5,
      name: r'updatedAt',
      type: IsarType.dateTime,
    ),
    r'workflow': PropertySchema(
      id: 6,
      name: r'workflow',
      type: IsarType.object,
      target: r'Workflow',
    )
  },
  estimateSize: _cacheActivityFacilityWorkflowEstimateSize,
  serialize: _cacheActivityFacilityWorkflowSerialize,
  deserialize: _cacheActivityFacilityWorkflowDeserialize,
  deserializeProp: _cacheActivityFacilityWorkflowDeserializeProp,
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
    r'status': IndexSchema(
      id: -107785170620420283,
      name: r'status',
      unique: false,
      replace: false,
      properties: [
        IndexPropertySchema(
          name: r'status',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {
    r'ActivityFacility': ActivityFacilitySchema,
    r'FieldPlan': FieldPlanSchema,
    r'Project': ProjectSchema,
    r'ProjectAdditionalDetails': ProjectAdditionalDetailsSchema,
    r'Facility': FacilitySchema,
    r'FacilityAddress': FacilityAddressSchema,
    r'FacilityDetails': FacilityDetailsSchema,
    r'GeographyDetails': GeographyDetailsSchema,
    r'StateRef': StateRefSchema,
    r'BlockRef': BlockRefSchema,
    r'AddressModel': AddressModelSchema,
    r'AdditionalDetails': AdditionalDetailsSchema,
    r'AssetTypeAdditionalDetails': AssetTypeAdditionalDetailsSchema,
    r'Workflow': WorkflowSchema,
    r'Document': DocumentSchema,
    r'GeoLocation': GeoLocationSchema,
    r'WorkflowAuditDetails': WorkflowAuditDetailsSchema,
    r'Transaction': TransactionSchema,
    r'Comment': CommentSchema
  },
  getId: _cacheActivityFacilityWorkflowGetId,
  getLinks: _cacheActivityFacilityWorkflowGetLinks,
  attach: _cacheActivityFacilityWorkflowAttach,
  version: '3.1.0+1',
);

int _cacheActivityFacilityWorkflowEstimateSize(
  CacheActivityFacilityWorkflow object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  bytesCount += 3 +
      ActivityFacilitySchema.estimateSize(
          object.activityFacility, allOffsets[ActivityFacility]!, allOffsets);
  bytesCount += 3 + object.activityFacilityId.length * 3;
  bytesCount += 3 + object.status.length * 3;
  {
    final list = object.transactions;
    if (list != null) {
      bytesCount += 3 + list.length * 3;
      {
        final offsets = allOffsets[Transaction]!;
        for (var i = 0; i < list.length; i++) {
          final value = list[i];
          bytesCount +=
              TransactionSchema.estimateSize(value, offsets, allOffsets);
        }
      }
    }
  }
  {
    final value = object.workflow;
    if (value != null) {
      bytesCount += 3 +
          WorkflowSchema.estimateSize(value, allOffsets[Workflow]!, allOffsets);
    }
  }
  return bytesCount;
}

void _cacheActivityFacilityWorkflowSerialize(
  CacheActivityFacilityWorkflow object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<ActivityFacility>(
    offsets[0],
    allOffsets,
    ActivityFacilitySchema.serialize,
    object.activityFacility,
  );
  writer.writeString(offsets[1], object.activityFacilityId);
  writer.writeDateTime(offsets[2], object.createdAt);
  writer.writeString(offsets[3], object.status);
  writer.writeObjectList<Transaction>(
    offsets[4],
    allOffsets,
    TransactionSchema.serialize,
    object.transactions,
  );
  writer.writeDateTime(offsets[5], object.updatedAt);
  writer.writeObject<Workflow>(
    offsets[6],
    allOffsets,
    WorkflowSchema.serialize,
    object.workflow,
  );
}

CacheActivityFacilityWorkflow _cacheActivityFacilityWorkflowDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = CacheActivityFacilityWorkflow(
    activityFacility: reader.readObjectOrNull<ActivityFacility>(
          offsets[0],
          ActivityFacilitySchema.deserialize,
          allOffsets,
        ) ??
        ActivityFacility(),
    activityFacilityId: reader.readString(offsets[1]),
    status: reader.readString(offsets[3]),
    transactions: reader.readObjectList<Transaction>(
      offsets[4],
      TransactionSchema.deserialize,
      allOffsets,
      Transaction(),
    ),
    workflow: reader.readObjectOrNull<Workflow>(
      offsets[6],
      WorkflowSchema.deserialize,
      allOffsets,
    ),
  );
  object.createdAt = reader.readDateTime(offsets[2]);
  object.id = id;
  object.updatedAt = reader.readDateTimeOrNull(offsets[5]);
  return object;
}

P _cacheActivityFacilityWorkflowDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<ActivityFacility>(
            offset,
            ActivityFacilitySchema.deserialize,
            allOffsets,
          ) ??
          ActivityFacility()) as P;
    case 1:
      return (reader.readString(offset)) as P;
    case 2:
      return (reader.readDateTime(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    case 4:
      return (reader.readObjectList<Transaction>(
        offset,
        TransactionSchema.deserialize,
        allOffsets,
        Transaction(),
      )) as P;
    case 5:
      return (reader.readDateTimeOrNull(offset)) as P;
    case 6:
      return (reader.readObjectOrNull<Workflow>(
        offset,
        WorkflowSchema.deserialize,
        allOffsets,
      )) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _cacheActivityFacilityWorkflowGetId(CacheActivityFacilityWorkflow object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _cacheActivityFacilityWorkflowGetLinks(
    CacheActivityFacilityWorkflow object) {
  return [];
}

void _cacheActivityFacilityWorkflowAttach(
    IsarCollection<dynamic> col, Id id, CacheActivityFacilityWorkflow object) {
  object.id = id;
}

extension CacheActivityFacilityWorkflowQueryWhereSort on QueryBuilder<
    CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow, QWhere> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension CacheActivityFacilityWorkflowQueryWhere on QueryBuilder<
    CacheActivityFacilityWorkflow,
    CacheActivityFacilityWorkflow,
    QWhereClause> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhereClause> idEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhereClause> idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhereClause> idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhereClause> activityFacilityIdEqualTo(String activityFacilityId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'activityFacilityId',
        value: [activityFacilityId],
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhereClause> statusEqualTo(String status) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'status',
        value: [status],
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterWhereClause> statusNotEqualTo(String status) {
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

extension CacheActivityFacilityWorkflowQueryFilter on QueryBuilder<
    CacheActivityFacilityWorkflow,
    CacheActivityFacilityWorkflow,
    QFilterCondition> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> activityFacilityIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> activityFacilityIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'activityFacilityId',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> createdAtEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'createdAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> statusIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> statusIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'status',
        value: '',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'transactions',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'transactions',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsLengthEqualTo(int length) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'transactions',
        length,
        true,
        length,
        true,
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'transactions',
        0,
        true,
        0,
        true,
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'transactions',
        0,
        false,
        999999,
        true,
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsLengthLessThan(
    int length, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'transactions',
        0,
        true,
        length,
        include,
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsLengthGreaterThan(
    int length, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'transactions',
        length,
        include,
        999999,
        true,
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsLengthBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'transactions',
        lower,
        includeLower,
        upper,
        includeUpper,
      );
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> updatedAtIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> updatedAtIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'updatedAt',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> updatedAtEqualTo(DateTime? value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'updatedAt',
        value: value,
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> updatedAtGreaterThan(
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> updatedAtLessThan(
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> updatedAtBetween(
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

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> workflowIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'workflow',
      ));
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> workflowIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'workflow',
      ));
    });
  }
}

extension CacheActivityFacilityWorkflowQueryObject on QueryBuilder<
    CacheActivityFacilityWorkflow,
    CacheActivityFacilityWorkflow,
    QFilterCondition> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> activityFacility(FilterQuery<ActivityFacility> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'activityFacility');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> transactionsElement(FilterQuery<Transaction> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'transactions');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterFilterCondition> workflow(FilterQuery<Workflow> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'workflow');
    });
  }
}

extension CacheActivityFacilityWorkflowQueryLinks on QueryBuilder<
    CacheActivityFacilityWorkflow,
    CacheActivityFacilityWorkflow,
    QFilterCondition> {}

extension CacheActivityFacilityWorkflowQuerySortBy on QueryBuilder<
    CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow, QSortBy> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> sortByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheActivityFacilityWorkflowQuerySortThenBy on QueryBuilder<
    CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow, QSortThenBy> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByActivityFacilityId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByActivityFacilityIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'activityFacilityId', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByCreatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'createdAt', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByStatus() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByStatusDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'status', Sort.desc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.asc);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QAfterSortBy> thenByUpdatedAtDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'updatedAt', Sort.desc);
    });
  }
}

extension CacheActivityFacilityWorkflowQueryWhereDistinct on QueryBuilder<
    CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow, QDistinct> {
  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QDistinct> distinctByActivityFacilityId({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'activityFacilityId',
          caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QDistinct> distinctByCreatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'createdAt');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QDistinct> distinctByStatus({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'status', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, CacheActivityFacilityWorkflow,
      QDistinct> distinctByUpdatedAt() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'updatedAt');
    });
  }
}

extension CacheActivityFacilityWorkflowQueryProperty on QueryBuilder<
    CacheActivityFacilityWorkflow,
    CacheActivityFacilityWorkflow,
    QQueryProperty> {
  QueryBuilder<CacheActivityFacilityWorkflow, int, QQueryOperations>
      idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, ActivityFacility,
      QQueryOperations> activityFacilityProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacility');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, String, QQueryOperations>
      activityFacilityIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'activityFacilityId');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, DateTime, QQueryOperations>
      createdAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'createdAt');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, String, QQueryOperations>
      statusProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'status');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, List<Transaction>?,
      QQueryOperations> transactionsProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'transactions');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, DateTime?, QQueryOperations>
      updatedAtProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'updatedAt');
    });
  }

  QueryBuilder<CacheActivityFacilityWorkflow, Workflow?, QQueryOperations>
      workflowProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'workflow');
    });
  }
}
