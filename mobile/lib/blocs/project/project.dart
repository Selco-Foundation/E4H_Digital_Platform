import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:selco/model/project_workflow/project_workflow.dart';

import '../../model/projects/project.dart';
import '../../repositories/app_init_Repo.dart';
import '../../repositories/project_repo.dart';

part 'project.freezed.dart';

class ProjectBloc extends Bloc<ProjectEvent, ProjectState> {
  ProjectBloc() : super(const ProjectState.initial()) {
    on<ProjectsFetchEvent>(_handleFetchProjects);
    on<ProjectSelectEvent>(_selectProject);
    on<FetchProjectsByWorkflowEvent>(_handleFetchProjectsByWorkflow);
  }

  FutureOr<void> _handleFetchProjects(
      ProjectsFetchEvent event, Emitter<ProjectState> emit) async {
    // final projectStaffList = await ProjectStaffRemoteRepository()
    //     .searchStaff(ProjectStaffSearchModel(staffId: [event.uuid.toString()]));

    ProjectSearchModel searchBody = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
      projectTypeId: "Facility",
    );

    final projectRemoteRepository = ProjectRemoteRepository();
    List<ProjectWorkflow> projectsList =
        await projectRemoteRepository.search(searchBody);

    emit(ProjectState.fetched(projectsList));
  }

  FutureOr<void> _selectProject(
      ProjectSelectEvent event, Emitter<ProjectState> emit) {
    final projectId = event.projectId;

    emit(ProjectState.selected(projectId));
  }

  FutureOr<void> _handleFetchProjectsByWorkflow(
      FetchProjectsByWorkflowEvent event, Emitter<ProjectState> emit) async {
    final searchBody = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    final projectRemoteRepository = ProjectRemoteRepository();
    List<ProjectWorkflow> projectsList =
        await projectRemoteRepository.searchByWorkflow(
      body: searchBody,
      workflowStatuses: event.workflowStatuses,
    );

    emit(ProjectState.fetched(projectsList));
  }
}

@freezed
class ProjectEvent with _$ProjectEvent {
  const factory ProjectEvent.fetchProjects({required String uuid}) =
      ProjectsFetchEvent;

  const factory ProjectEvent.selectProject(String projectId) =
      ProjectSelectEvent;

  const factory ProjectEvent.fetchProjectsByWorkflow({
    required List<String> workflowStatuses,
  }) = FetchProjectsByWorkflowEvent;
}

@freezed
class ProjectState with _$ProjectState {
  const factory ProjectState.initial() = _ProjectInitialState;
  const factory ProjectState.fetched(List<ProjectWorkflow> projectsList) =
      ProjectFetchedState;
  const factory ProjectState.selected(projectId) = ProjectSelectedState;
}
