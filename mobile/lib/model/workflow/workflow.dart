import 'dart:convert';

import 'package:isar/isar.dart';

import '../../data/nosql/workflow_audit_details.dart';
import '../document/document.dart';

part 'workflow.g.dart';

@Embedded()
class Workflow {
  @Embedded()
  List<Document>? documents;

  @Embedded()
  WorkflowAuditDetails? auditDetails;

  String? rawJson;

  Workflow({
    this.documents,
    this.auditDetails,
    this.rawJson,
  });

  @ignore
  Map<String, dynamic>? get raw =>
      rawJson == null ? null : (jsonDecode(rawJson!) as Map<String, dynamic>);

  factory Workflow.fromJson(Map<String, dynamic> json) {
    final docs = json['documents'] is List
        ? (json['documents'] as List)
            .map((d) => Document.fromJson(Map<String, dynamic>.from(d)))
            .toList()
        : null;
    final details = json['auditDetails'] is Map<String, dynamic>
        ? WorkflowAuditDetails.fromJson(
            json['auditDetails'] as Map<String, dynamic>,
          )
        : null;
    final rawJson = json.isNotEmpty ? jsonEncode(json) : null;
    return Workflow(documents: docs, auditDetails: details, rawJson: rawJson);
  }

  Map<String, dynamic> toJson() => {
        'documents': documents?.map((d) => d.toJson()).toList(),
      };
}
