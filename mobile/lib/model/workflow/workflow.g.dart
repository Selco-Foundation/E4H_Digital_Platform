// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workflow.dart';

// **************************************************************************
// IsarEmbeddedGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

const WorkflowSchema = Schema(
  name: r'Workflow',
  id: 1996891966521180322,
  properties: {
    r'auditDetails': PropertySchema(
      id: 0,
      name: r'auditDetails',
      type: IsarType.object,
      target: r'WorkflowAuditDetails',
    ),
    r'documents': PropertySchema(
      id: 1,
      name: r'documents',
      type: IsarType.objectList,
      target: r'Document',
    ),
    r'rawJson': PropertySchema(
      id: 2,
      name: r'rawJson',
      type: IsarType.string,
    )
  },
  estimateSize: _workflowEstimateSize,
  serialize: _workflowSerialize,
  deserialize: _workflowDeserialize,
  deserializeProp: _workflowDeserializeProp,
);

int _workflowEstimateSize(
  Workflow object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.auditDetails;
    if (value != null) {
      bytesCount += 3 +
          WorkflowAuditDetailsSchema.estimateSize(
              value, allOffsets[WorkflowAuditDetails]!, allOffsets);
    }
  }
  {
    final list = object.documents;
    if (list != null) {
      bytesCount += 3 + list.length * 3;
      {
        final offsets = allOffsets[Document]!;
        for (var i = 0; i < list.length; i++) {
          final value = list[i];
          bytesCount += DocumentSchema.estimateSize(value, offsets, allOffsets);
        }
      }
    }
  }
  {
    final value = object.rawJson;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  return bytesCount;
}

void _workflowSerialize(
  Workflow object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeObject<WorkflowAuditDetails>(
    offsets[0],
    allOffsets,
    WorkflowAuditDetailsSchema.serialize,
    object.auditDetails,
  );
  writer.writeObjectList<Document>(
    offsets[1],
    allOffsets,
    DocumentSchema.serialize,
    object.documents,
  );
  writer.writeString(offsets[2], object.rawJson);
}

Workflow _workflowDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = Workflow(
    auditDetails: reader.readObjectOrNull<WorkflowAuditDetails>(
      offsets[0],
      WorkflowAuditDetailsSchema.deserialize,
      allOffsets,
    ),
    documents: reader.readObjectList<Document>(
      offsets[1],
      DocumentSchema.deserialize,
      allOffsets,
      Document(),
    ),
    rawJson: reader.readStringOrNull(offsets[2]),
  );
  return object;
}

P _workflowDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readObjectOrNull<WorkflowAuditDetails>(
        offset,
        WorkflowAuditDetailsSchema.deserialize,
        allOffsets,
      )) as P;
    case 1:
      return (reader.readObjectList<Document>(
        offset,
        DocumentSchema.deserialize,
        allOffsets,
        Document(),
      )) as P;
    case 2:
      return (reader.readStringOrNull(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

extension WorkflowQueryFilter
    on QueryBuilder<Workflow, Workflow, QFilterCondition> {
  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> auditDetailsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'auditDetails',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition>
      auditDetailsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'auditDetails',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> documentsIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'documents',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> documentsIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'documents',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition>
      documentsLengthEqualTo(int length) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'documents',
        length,
        true,
        length,
        true,
      );
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> documentsIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'documents',
        0,
        true,
        0,
        true,
      );
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition>
      documentsIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'documents',
        0,
        false,
        999999,
        true,
      );
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition>
      documentsLengthLessThan(
    int length, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'documents',
        0,
        true,
        length,
        include,
      );
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition>
      documentsLengthGreaterThan(
    int length, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'documents',
        length,
        include,
        999999,
        true,
      );
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition>
      documentsLengthBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.listLength(
        r'documents',
        lower,
        includeLower,
        upper,
        includeUpper,
      );
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'rawJson',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'rawJson',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'rawJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'rawJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'rawJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'rawJson',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'rawJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'rawJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonContains(
      String value,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'rawJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonMatches(
      String pattern,
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'rawJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'rawJson',
        value: '',
      ));
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> rawJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'rawJson',
        value: '',
      ));
    });
  }
}

extension WorkflowQueryObject
    on QueryBuilder<Workflow, Workflow, QFilterCondition> {
  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> auditDetails(
      FilterQuery<WorkflowAuditDetails> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'auditDetails');
    });
  }

  QueryBuilder<Workflow, Workflow, QAfterFilterCondition> documentsElement(
      FilterQuery<Document> q) {
    return QueryBuilder.apply(this, (query) {
      return query.object(q, r'documents');
    });
  }
}
