import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';
import '../../model/project_workflow/project_workflow.dart';

import '../../data/nosql/cache_unsubmitted_project.dart';
import '../../model/projects/project.dart';
import '../../repositories/app_init_Repo.dart';
import '../../repositories/project_repo.dart';
import '../../utils/utils.dart';

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

    on<FetchAllReportCountsEvent>(_onFetchAllReportCounts);

    on<GetNewlyAssignedEvent>(_onGetNewlyAssigned);

    on<FetchProjectsSortedEvent>(_handleFetchProjectsSorted);

    on<FetchProjectsBySearchEvent>(_handleFetchProjectsBySearch);

    on<ProjectCheckIfInCache>(_checkIfInCache);
  }

  FutureOr<void> _selectProject(
      ProjectSelectEvent event, Emitter<ProjectState> emit) {
    emit(ProjectState.selected(event.projectId));
  }

  FutureOr<void> _handleFetchProjectsByWorkflow(
      FetchProjectsByWorkflowEvent event, Emitter<ProjectState> emit) async {
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
      print(projectsList[0].project.name);
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

  Future<void> _onFetchAllReportCounts(
    FetchAllReportCountsEvent event,
    Emitter<ProjectState> emit,
  ) async {
    emit(const ProjectState.loading());

    final repo = ProjectRepository(isar);
    final remote = ProjectRemoteRepository();
    final body = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    // 1) Build each status‐list based on userType
    final isSupervisor = event.userType == USER_TYPES.SUPERVISOR.name;

    final newStatuses = [
      if (isSupervisor)
        WORKFLOW_STATUS_FIELD_SUPERVISOR.ASSIGNED_TO_FIELD_SUPERVISOR.name
      else
        WORKFLOW_STATUS_FIELD_STAFF.ASSIGNED_TO_FIELD_STAFF.name,
    ];

    final inboxStatuses = isSupervisor
        ? [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_FIELD_STAFF.name,
            WORKFLOW_STATUS_FIELD_SUPERVISOR.REJECTED_BY_QC_SPOC.name,
            WORKFLOW_STATUS_FIELD_SUPERVISOR.APPROVED_BY_QC_SPOC.name,
          ]
        : [
            WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_FIELD_SUPERVISOR.name,
            WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_SUPERVISOR.name,
            WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_QC_SPOC.name,
          ];

    final submittedStatuses = isSupervisor
        ? [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
          ]
        : [
            WORKFLOW_STATUS_FIELD_STAFF.SUBMITTED_BY_FIELD_STAFF.name,
          ];

    // 2) Helper to try remote.count → fallback to cache
    Future<int> _fetchCount(List<String> statuses) async {
      try {
        return await remote.searchByWorkflowCount(
          body: body,
          workflowStatuses: statuses,
        );
      } catch (_) {
        // readCache is your _readCache renamed to public
        final cachedList = await repo.readCache(statuses);
        return cachedList.length;
      }
    }

    // 3) Fire them in parallel
    final results = await Future.wait([
      _fetchCount(newStatuses),
      _fetchCount(inboxStatuses),
      _fetchCount(submittedStatuses),
    ]);

    emit(ProjectState.reportCountsLoaded(
      newReportCount: results[0],
      inboxCount: results[1],
      submittedCount: results[2],
    ));
  }

  Future<void> _onGetNewlyAssigned(
    GetNewlyAssignedEvent event,
    Emitter<ProjectState> emit,
  ) async {
    emit(const ProjectState.loading());

    final remote = ProjectRemoteRepository();
    final repo = ProjectRepository(isar);
    final body = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    final isSupervisor = event.userType == USER_TYPES.SUPERVISOR.name;
    final newStatuses = [
      if (isSupervisor)
        WORKFLOW_STATUS_FIELD_SUPERVISOR.ASSIGNED_TO_FIELD_SUPERVISOR.name
      else
        WORKFLOW_STATUS_FIELD_STAFF.ASSIGNED_TO_FIELD_STAFF.name,
    ];

    try {
      final count = await remote.searchByWorkflowCount(
        body: body,
        workflowStatuses: newStatuses,
      );

      if (count > 0) {
        final cachedList = await repo.readCache(newStatuses);
        final newlyAssigned = count - cachedList.length;
        emit(ProjectState.newlyAssignedLoaded(newlyAssigned));
      } else {
        emit(const ProjectState.newlyAssignedLoaded(0));
      }
    } catch (_) {
      emit(const ProjectState.newlyAssignedLoaded(0));
    }
  }

  Future<void> _handleFetchProjectsSorted(
    FetchProjectsSortedEvent event,
    Emitter<ProjectState> emit,
  ) async {
    emit(const ProjectState.loading());

    final repo = ProjectRepository(isar);
    final body = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    try {
      final remoteList = await repo.fetchByWorkflow(
        body: body,
        workflowStatuses: event.workflowStatuses,
        sortDirection: event.sortDirection,
      );
      emit(ProjectState.fetched(remoteList));
    } catch (_) {
      emit(const ProjectState.fetched([]));
    }
  }

  Future<void> _handleFetchProjectsBySearch(
    FetchProjectsBySearchEvent event,
    Emitter<ProjectState> emit,
  ) async {
    // only search when you have at least 3 characters
    if (event.query.length < 3) {
      emit(const ProjectState.initial());
    }

    emit(const ProjectState.searchLoading());
    final remote = ProjectRemoteRepository();
    final body = ProjectSearchModel(
      tenantId: envConfig.variables.tenantId,
      name: event.query,
    );

    try {
      final results = await remote.searchByWorkflow(
        body: body,
        workflowStatuses: event.workflowStatuses,
      );
      emit(ProjectState.searchResults(results));
    } catch (_) {
      emit(const ProjectState.searchResults([]));
    }
  }

  Future<void> _checkIfInCache(
    ProjectCheckIfInCache event,
    Emitter<ProjectState> emit,
  ) async {
    emit(const ProjectState.loading());

    final col = isar.cacheUnsubmittedProjects;
    final cached = await col
        .where()
        .projectIdEqualTo(event.projectId)
        .filter()
        .userTypeEqualTo(event.userType)
        .findAll();

    final isInCache = cached.isNotEmpty;

    emit(ProjectState.inCache(isInCache));
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

  const factory ProjectEvent.fetchAllReportCounts({
    required String userType,
  }) = FetchAllReportCountsEvent;

  const factory ProjectEvent.getNewlyAssigned({
    required String userType,
  }) = GetNewlyAssignedEvent;

  const factory ProjectEvent.fetchProjectsSorted({
    required List<String> workflowStatuses,
    required String sortDirection, // ASC or DESC
  }) = FetchProjectsSortedEvent;

  const factory ProjectEvent.fetchProjectsBySearch({
    required String query,
    required List<String> workflowStatuses,
  }) = FetchProjectsBySearchEvent;

  const factory ProjectEvent.checkIfInCache({
    required String projectId,
    required String userType,
  }) = ProjectCheckIfInCache;
}

@freezed
class ProjectState with _$ProjectState {
  const factory ProjectState.initial() = _ProjectInitialState;

  /// new loading state
  const factory ProjectState.loading() = _ProjectLoadingState;

  const factory ProjectState.inCache(bool isInCache) = ProjectInCache;

  const factory ProjectState.fetched(List<ProjectWorkflow> projectsList) =
      ProjectFetchedState;

  const factory ProjectState.selected(String projectId) = ProjectSelectedState;

  const factory ProjectState.unSubmittedLoaded(
      List<ProjectWorkflow> unSubmitted) = _UnSubmittedLoaded;

  const factory ProjectState.unSubmittedAdded(CacheUnsubmittedProject entry) =
      _UnSubmittedAdded;

  const factory ProjectState.unSubmittedDeleted() = _UnSubmittedDeleted;

  const factory ProjectState.reportCountsLoaded({
    required int newReportCount,
    required int inboxCount,
    required int submittedCount,
  }) = ReportCountsLoaded;

  const factory ProjectState.newlyAssignedLoaded(int count) =
      NewlyAssignedLoaded;

  const factory ProjectState.sorted({
    required List<ProjectWorkflow> projectsList,
    required String sortDirection,
  }) = ProjectSortedState;

  const factory ProjectState.searchLoading() = ProjectSearchLoading;
  const factory ProjectState.searchResults(List<ProjectWorkflow> results) =
      ProjectSearchResults;
}
