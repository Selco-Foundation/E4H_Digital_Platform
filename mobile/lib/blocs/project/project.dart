// import 'dart:async';
//
// import 'package:flutter_bloc/flutter_bloc.dart';
// import 'package:freezed_annotation/freezed_annotation.dart';
// import 'package:isar/isar.dart';
// import 'package:selco/model/project_workflow/project_workflow.dart';
//
// import '../../data/nosql/cache_unsubmitted_project.dart';
// import '../../model/projects/project.dart';
// import '../../repositories/app_init_Repo.dart';
// import '../../repositories/project_repo.dart';
//
// part 'project.freezed.dart';
//
// class ProjectBloc extends Bloc<ProjectEvent, ProjectState> {
//   final Isar isar;
//   ProjectBloc(this.isar) : super(const ProjectState.initial()) {
// //    on<ProjectsFetchEvent>(_handleFetchProjects);
//     on<ProjectSelectEvent>(_selectProject);
//     on<FetchProjectsByWorkflowEvent>(_handleFetchProjectsByWorkflow);
//
//     on<AddUnSubmittedEvent>(_onAddUnSubmitted);
//     on<LoadUnSubmittedEvent>(_onLoadUnSubmitted);
//     on<DeleteUnSubmittedEvent>(_onDeleteUnSubmitted);
//   }
//
//   // FutureOr<void> _handleFetchProjects(
//   //     ProjectsFetchEvent event, Emitter<ProjectState> emit) async {
//   //   // final projectStaffList = await ProjectStaffRemoteRepository()
//   //   //     .searchStaff(ProjectStaffSearchModel(staffId: [event.uuid.toString()]));
//   //
//   //   ProjectSearchModel searchBody = ProjectSearchModel(
//   //     tenantId: envConfig.variables.tenantId,
//   //     projectTypeId: "Facility",
//   //   );
//   //
//   //   final projectRemoteRepository = ProjectRemoteRepository();
//   //   List<ProjectWorkflow> projectsList =
//   //       await projectRemoteRepository.search(searchBody);
//   //
//   //   emit(ProjectState.fetched(projectsList));
//   // }
//
//   FutureOr<void> _selectProject(
//       ProjectSelectEvent event, Emitter<ProjectState> emit) {
//     final projectId = event.projectId;
//
//     emit(ProjectState.selected(projectId));
//   }
//
//   FutureOr<void> _handleFetchProjectsByWorkflow(
//       FetchProjectsByWorkflowEvent event, Emitter<ProjectState> emit) async {
//     final projectRepository = ProjectRepository(isar);
//     ProjectSearchModel searchBody = ProjectSearchModel(
//       tenantId: envConfig.variables.tenantId,
//       // projectTypeId: "Facility",
//     );
//     List<ProjectWorkflow> projectsList =
//         await projectRepository.fetchByWorkflow(
//             workflowStatuses: event.workflowStatuses, body: searchBody);
//
//     emit(ProjectState.fetched(projectsList));
//   }
//
//   Future<void> _onAddUnSubmitted(
//     AddUnSubmittedEvent event,
//     Emitter<ProjectState> emit,
//   ) async {
//     final _unsubRepo = UnsubmittedProjectRepository(isar);
//     final entry = await _unsubRepo.addOrGet(event.workflow, event.userType);
//     emit(ProjectState.unSubmittedAdded(entry));
//   }
//
//   Future<void> _onLoadUnSubmitted(
//     LoadUnSubmittedEvent event,
//     Emitter<ProjectState> emit,
//   ) async {
//     final _unsubRepo = UnsubmittedProjectRepository(isar);
//     ProjectSearchModel body = ProjectSearchModel(
//       tenantId: envConfig.variables.tenantId,
//       // projectTypeId: "Facility",
//     );
//     final unSubmitted = await _unsubRepo.fetchByWorkflowIncludeCache(
//       workflowStatuses: event.statuses,
//       userType: event.userType,
//       body: body,
//     );
//     emit(ProjectState.unSubmittedLoaded(unSubmitted));
//   }
//
//   Future<void> _onDeleteUnSubmitted(
//     DeleteUnSubmittedEvent event,
//     Emitter<ProjectState> emit,
//   ) async {
//     final unsubRepo = UnsubmittedProjectRepository(isar);
//     await unsubRepo.delete(event.projectId, event.userType);
//     emit(const ProjectState.unSubmittedDeleted());
//   }
// }
//
// @freezed
// class ProjectEvent with _$ProjectEvent {
//   // const factory ProjectEvent.fetchProjects({required String uuid}) =
//   //     ProjectsFetchEvent;
//
//   const factory ProjectEvent.selectProject(String projectId) =
//       ProjectSelectEvent;
//
//   const factory ProjectEvent.fetchProjectsByWorkflow({
//     required List<String> workflowStatuses,
//   }) = FetchProjectsByWorkflowEvent;
//
//   const factory ProjectEvent.addUnSubmitted(
//       ProjectWorkflow workflow, String userType) = AddUnSubmittedEvent;
//   const factory ProjectEvent.loadUnSubmitted(
//       List<String> statuses, String userType) = LoadUnSubmittedEvent;
//   const factory ProjectEvent.deleteUnSubmitted(
//       String projectId, String userType) = DeleteUnSubmittedEvent;
// }
//
// @freezed
// class ProjectState with _$ProjectState {
//   const factory ProjectState.initial() = _ProjectInitialState;
//   const factory ProjectState.fetched(List<ProjectWorkflow> projectsList) =
//       ProjectFetchedState;
//   const factory ProjectState.selected(projectId) = ProjectSelectedState;
//
//   const factory ProjectState.unSubmittedLoaded(
//       List<ProjectWorkflow> unSubmitted) = _UnSubmittedLoaded;
//   const factory ProjectState.unSubmittedAdded(CacheUnsubmittedProject entry) =
//       _UnSubmittedAdded;
//   const factory ProjectState.unSubmittedDeleted() = _UnSubmittedDeleted;
// }

// lib/blocs/project/project.dart

import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import 'package:selco/model/project_workflow/project_workflow.dart';

import '../../data/nosql/cache_unsubmitted_project.dart';
import '../../model/projects/project.dart';
import '../../repositories/app_init_Repo.dart';
import '../../repositories/project_repo.dart';

part 'project.freezed.dart';

class ProjectBloc extends Bloc<ProjectEvent, ProjectState> {
  final Isar isar;

  ProjectBloc(this.isar) : super(const ProjectState.initial()) {
    on<ProjectSelectEvent>(_selectProject);

    // 1) Emit loading before fetching
    on<FetchProjectsByWorkflowEvent>(_handleFetchProjectsByWorkflow);

    on<AddUnSubmittedEvent>(_onAddUnSubmitted);

    // 2) Emit loading before loading un‐submitted
    on<LoadUnSubmittedEvent>(_onLoadUnSubmitted);

    on<DeleteUnSubmittedEvent>(_onDeleteUnSubmitted);
  }

  FutureOr<void> _selectProject(
      ProjectSelectEvent event, Emitter<ProjectState> emit) {
    emit(ProjectState.selected(event.projectId));
  }

  FutureOr<void> _handleFetchProjectsByWorkflow(
      FetchProjectsByWorkflowEvent event, Emitter<ProjectState> emit) async {
    // new: immediately show loading
    emit(const ProjectState.loading());

    final projectRepository = ProjectRepository(isar);
    final searchBody = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    try {
      final projectsList = await projectRepository.fetchByWorkflow(
        workflowStatuses: event.workflowStatuses,
        body: searchBody,
      );
      emit(ProjectState.fetched(projectsList));
    } catch (_) {
      // on error, you may choose to emit an error or empty
      emit(const ProjectState.fetched([]));
    }
  }

  Future<void> _onAddUnSubmitted(
    AddUnSubmittedEvent event,
    Emitter<ProjectState> emit,
  ) async {
    final repo = UnsubmittedProjectRepository(isar);
    final entry = await repo.addOrGet(event.workflow, event.userType);
    emit(ProjectState.unSubmittedAdded(entry));
  }

  Future<void> _onLoadUnSubmitted(
    LoadUnSubmittedEvent event,
    Emitter<ProjectState> emit,
  ) async {
    // new: loading state
    emit(const ProjectState.loading());

    final repo = UnsubmittedProjectRepository(isar);
    final searchBody = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    try {
      final unSubmitted = await repo.fetchByWorkflowIncludeCache(
        workflowStatuses: event.statuses,
        userType: event.userType,
        body: searchBody,
      );
      emit(ProjectState.unSubmittedLoaded(unSubmitted));
    } catch (_) {
      emit(const ProjectState.unSubmittedLoaded([]));
    }
  }

  Future<void> _onDeleteUnSubmitted(
    DeleteUnSubmittedEvent event,
    Emitter<ProjectState> emit,
  ) async {
    final repo = UnsubmittedProjectRepository(isar);
    await repo.delete(event.projectId, event.userType);
    emit(const ProjectState.unSubmittedDeleted());
  }
}

@freezed
class ProjectEvent with _$ProjectEvent {
  const factory ProjectEvent.selectProject(String projectId) =
      ProjectSelectEvent;

  const factory ProjectEvent.fetchProjectsByWorkflow({
    required List<String> workflowStatuses,
  }) = FetchProjectsByWorkflowEvent;

  const factory ProjectEvent.addUnSubmitted(
      ProjectWorkflow workflow, String userType) = AddUnSubmittedEvent;

  const factory ProjectEvent.loadUnSubmitted(
      List<String> statuses, String userType) = LoadUnSubmittedEvent;

  const factory ProjectEvent.deleteUnSubmitted(
      String projectId, String userType) = DeleteUnSubmittedEvent;
}

@freezed
class ProjectState with _$ProjectState {
  const factory ProjectState.initial() = _ProjectInitialState;

  /// new loading state
  const factory ProjectState.loading() = _ProjectLoadingState;

  const factory ProjectState.fetched(List<ProjectWorkflow> projectsList) =
      ProjectFetchedState;

  const factory ProjectState.selected(String projectId) = ProjectSelectedState;

  const factory ProjectState.unSubmittedLoaded(
      List<ProjectWorkflow> unSubmitted) = _UnSubmittedLoaded;

  const factory ProjectState.unSubmittedAdded(CacheUnsubmittedProject entry) =
      _UnSubmittedAdded;

  const factory ProjectState.unSubmittedDeleted() = _UnSubmittedDeleted;
}
