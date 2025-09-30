import 'dart:convert';

import 'package:isar/isar.dart';

import '../document/document.dart';

part 'workflow.g.dart'; // optional

@Embedded()
class Workflow {
  /// Embedded documents
  @Embedded()
  List<Document>? documents;

  /// We also keep rawJson for any additional top-level fields the server may return.
  /// This is persisted as JSON string to avoid Map types in embedded classes.
  String? rawJson;

  Workflow({this.documents, this.rawJson});

  @ignore
  Map<String, dynamic>? get raw =>
      rawJson == null ? null : (jsonDecode(rawJson!) as Map<String, dynamic>);

  factory Workflow.fromJson(Map<String, dynamic> json) {
    final docs = json['documents'] is List
        ? (json['documents'] as List)
            .map((d) => Document.fromJson(Map<String, dynamic>.from(d)))
            .toList()
        : null;
    final rawJson = json.isNotEmpty ? jsonEncode(json) : null;
    return Workflow(documents: docs, rawJson: rawJson);
  }

  Map<String, dynamic> toJson() => {
        'documents': documents?.map((d) => d.toJson()).toList(),
      };
}
