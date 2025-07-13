import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:selco/model/workflow/workflow.dart';

import '../projects/project.dart';
import '../transaction/transaction.dart';

part 'project_workflow.freezed.dart';
part 'project_workflow.g.dart';

@freezed
class ProjectWorkflow with _$ProjectWorkflow {
  const factory ProjectWorkflow({
    @ProjectModelConverter() required ProjectModel project,
    String? status,
    List<Transaction>? transactions,
    Workflow? workflow,
  }) = _ProjectWorkflow;

  factory ProjectWorkflow.fromJson(Map<String, dynamic> json) =>
      _$ProjectWorkflowFromJson(json);
}

class ProjectModelConverter
    implements JsonConverter<ProjectModel, Map<String, dynamic>> {
  const ProjectModelConverter();

  @override
  ProjectModel fromJson(Map<String, dynamic> json) {
    return ProjectModelMapper.fromMap(json);
  }

  @override
  Map<String, dynamic> toJson(ProjectModel model) {
    return model.toMap();
  }
}
