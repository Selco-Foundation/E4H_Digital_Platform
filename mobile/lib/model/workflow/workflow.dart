import 'package:freezed_annotation/freezed_annotation.dart';
import '../../model/document/document.dart';

part 'workflow.g.dart';

@JsonSerializable(explicitToJson: true)
class Workflow {
  final List<Document>? documents;

  Workflow({
    this.documents,
  });

  factory Workflow.fromJson(Map<String, dynamic> json) =>
      _$WorkflowFromJson(json);
  Map<String, dynamic> toJson() => _$WorkflowToJson(this);
}
