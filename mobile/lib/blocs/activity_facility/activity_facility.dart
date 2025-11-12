import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_unsubmitted_activity_facility.dart';
import '../../model/activity_facility/activity_facility.dart';
import '../../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../repositories/activity_facility_repo.dart';
import '../../repositories/app_init_repo.dart';
import '../../utils/utils.dart';

part 'activity_facility.freezed.dart';

class ActivityFacilityBloc
    extends Bloc<ActivityFacilityEvent, ActivityFacilityState> {
  final Isar isar;

  ActivityFacilityBloc(this.isar)
      : super(const ActivityFacilityState.initial()) {
    on<ActivityFacilitySelectEvent>(_selectActivityFacility);
    on<FetchActivityFacilityByWorkflowEvent>(
        _handleFetchActivityFacilityByWorkflow);
    on<AddUnSubmittedEvent>(_onAddUnSubmitted);
    on<LoadUnSubmittedEvent>(_onLoadUnSubmitted);
    on<DeleteUnSubmittedEvent>(_onDeleteUnSubmitted);
    on<FetchAllReportCountsEvent>(_onFetchAllReportCounts);
    on<GetNewlyAssignedEvent>(_onGetNewlyAssigned);
    on<FetchActivityFacilitySortedEvent>(_handleFetchActivityFacilitySorted);
    on<FetchActivityFacilityBySearchEvent>(
        _handleFetchActivityFacilityBySearch);
    on<ActivityFacilityCheckIfInCache>(_checkIfInCache);
  }

  FutureOr<void> _selectActivityFacility(
      ActivityFacilitySelectEvent event, Emitter<ActivityFacilityState> emit) {
    emit(ActivityFacilityState.selected(event.activityFacilityId));
  }

  FutureOr<void> _handleFetchActivityFacilityByWorkflow(
      FetchActivityFacilityByWorkflowEvent event,
      Emitter<ActivityFacilityState> emit) async {
    emit(const ActivityFacilityState.loading());

    final activityFacilityRepository = ActivityFacilityRepository(isar);
    final searchBody = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    try {
      final activityFacilityList =
          await activityFacilityRepository.fetchByWorkflow(
        workflowStatuses: event.workflowStatuses,
        body: searchBody,
      );
      emit(ActivityFacilityState.fetched(activityFacilityList));
    } catch (_) {
      emit(const ActivityFacilityState.fetched([]));
    }
  }

  Future<void> _onAddUnSubmitted(
    AddUnSubmittedEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    final repo = UnsubmittedActivityFacilityRepository(isar);
    final entry = await repo.addOrGet(event.workflow, event.userType);
    emit(ActivityFacilityState.unSubmittedAdded(entry));
  }

  Future<void> _onLoadUnSubmitted(
    LoadUnSubmittedEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    emit(const ActivityFacilityState.loading());

    final repo = UnsubmittedActivityFacilityRepository(isar);
    final searchBody = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    try {
      final unSubmitted = await repo.fetchByWorkflowIncludeCache(
        workflowStatuses: event.statuses,
        userType: event.userType,
        body: searchBody,
      );
      emit(ActivityFacilityState.unSubmittedLoaded(unSubmitted));
    } catch (_) {
      emit(const ActivityFacilityState.unSubmittedLoaded([]));
    }
  }

  Future<void> _onDeleteUnSubmitted(
    DeleteUnSubmittedEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    final repo = UnsubmittedActivityFacilityRepository(isar);
    await repo.delete(event.activityFacilityId, event.userType);
    emit(const ActivityFacilityState.unSubmittedDeleted());
  }

  Future<void> _onFetchAllReportCounts(
    FetchAllReportCountsEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    emit(const ActivityFacilityState.loading());

    final repo = ActivityFacilityRepository(isar);
    final remote = ActivityFacilityRemoteRepository();
    final body = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
    );

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
            WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_QC_SPOC.name,
            WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_SUPERVISOR.name,
            WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_QC_SPOC.name,
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
          ];

    final submittedStatuses = isSupervisor
        ? [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
          ]
        : [
            WORKFLOW_STATUS_FIELD_STAFF.SUBMITTED_BY_FIELD_STAFF.name,
          ];

    Future<int> _fetchCount(List<String> statuses) async {
      try {
        return await remote.searchByWorkflowCount(
          body: body,
          workflowStatuses: statuses,
        );
      } catch (_) {
        final cachedList = await repo.readCache(statuses);
        return cachedList.length;
      }
    }

    final results = await Future.wait([
      _fetchCount(newStatuses),
      _fetchCount(inboxStatuses),
      _fetchCount(submittedStatuses),
    ]);

    emit(ActivityFacilityState.reportCountsLoaded(
      newReportCount: results[0],
      inboxCount: results[1],
      submittedCount: results[2],
    ));
  }

  Future<void> _onGetNewlyAssigned(
    GetNewlyAssignedEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    emit(const ActivityFacilityState.loading());

    final remote = ActivityFacilityRemoteRepository();
    final repo = ActivityFacilityRepository(isar);
    final body = ActivityFacilitySearchModel(
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
        final newlyAssigned = (count - cachedList.length).clamp(0, count);
        emit(ActivityFacilityState.newlyAssignedLoaded(newlyAssigned));
      } else {
        emit(const ActivityFacilityState.newlyAssignedLoaded(0));
      }
    } catch (_) {
      emit(const ActivityFacilityState.newlyAssignedLoaded(0));
    }
  }

  Future<void> _handleFetchActivityFacilitySorted(
    FetchActivityFacilitySortedEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    emit(const ActivityFacilityState.loading());

    final repo = ActivityFacilityRepository(isar);
    final body = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
    );

    try {
      final remoteList = await repo.fetchByWorkflow(
        body: body,
        workflowStatuses: event.workflowStatuses,
        sortDirection: event.sortDirection,
      );
      emit(ActivityFacilityState.fetched(remoteList));
    } catch (_) {
      emit(const ActivityFacilityState.fetched([]));
    }
  }

  Future<void> _handleFetchActivityFacilityBySearch(
    FetchActivityFacilityBySearchEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    if (event.query.length < 3) {
      emit(const ActivityFacilityState.initial());
    }

    emit(const ActivityFacilityState.searchLoading());
    final remote = ActivityFacilityRemoteRepository();
    final body = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
      name: event.query,
    );

    try {
      final results = await remote.searchByWorkflow(
        body: body,
        workflowStatuses: event.workflowStatuses,
      );
      emit(ActivityFacilityState.searchResults(results));
    } catch (_) {
      emit(const ActivityFacilityState.searchResults([]));
    }
  }

  Future<void> _checkIfInCache(
    ActivityFacilityCheckIfInCache event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    emit(const ActivityFacilityState.loading());

    final col = isar.cacheUnsubmittedActivityFacilitys;
    final cached = await col
        .where()
        .activityFacilityIdEqualTo(event.activityFacilityId)
        .filter()
        .userTypeEqualTo(event.userType)
        .findAll();

    final isInCache = cached.isNotEmpty;

    emit(ActivityFacilityState.inCache(isInCache));
  }
}

@freezed
class ActivityFacilityEvent with _$ActivityFacilityEvent {
  const factory ActivityFacilityEvent.selectActivityFacility(
      String activityFacilityId) = ActivityFacilitySelectEvent;

  const factory ActivityFacilityEvent.fetchActivityFacilityByWorkflow({
    required List<String> workflowStatuses,
  }) = FetchActivityFacilityByWorkflowEvent;

  const factory ActivityFacilityEvent.addUnSubmitted(
      ActivityFacilityWorkflow workflow, String userType) = AddUnSubmittedEvent;

  const factory ActivityFacilityEvent.loadUnSubmitted(
      List<String> statuses, String userType) = LoadUnSubmittedEvent;

  const factory ActivityFacilityEvent.deleteUnSubmitted(
      String activityFacilityId, String userType) = DeleteUnSubmittedEvent;

  const factory ActivityFacilityEvent.fetchAllReportCounts({
    required String userType,
  }) = FetchAllReportCountsEvent;

  const factory ActivityFacilityEvent.getNewlyAssigned({
    required String userType,
  }) = GetNewlyAssignedEvent;

  const factory ActivityFacilityEvent.fetchActivityFacilitySorted({
    required List<String> workflowStatuses,
    required String sortDirection,
  }) = FetchActivityFacilitySortedEvent;

  const factory ActivityFacilityEvent.fetchActivityFacilityBySearch({
    required String query,
    required List<String> workflowStatuses,
  }) = FetchActivityFacilityBySearchEvent;

  const factory ActivityFacilityEvent.checkIfInCache({
    required String activityFacilityId,
    required String userType,
  }) = ActivityFacilityCheckIfInCache;
}

@freezed
class ActivityFacilityState with _$ActivityFacilityState {
  const factory ActivityFacilityState.initial() = _ActivityFacilityInitialState;

  const factory ActivityFacilityState.loading() = _ActivityFacilityLoadingState;

  const factory ActivityFacilityState.inCache(bool isInCache) =
      ActivityFacilityInCache;

  const factory ActivityFacilityState.fetched(
          List<ActivityFacilityWorkflow> activityFacilityList) =
      ActivityFacilityFetchedState;

  const factory ActivityFacilityState.selected(String activityFacilityId) =
      ActivityFacilitySelectedState;

  const factory ActivityFacilityState.unSubmittedLoaded(
      List<ActivityFacilityWorkflow> unSubmitted) = _UnSubmittedLoaded;

  const factory ActivityFacilityState.unSubmittedAdded(
      CacheUnsubmittedActivityFacility entry) = _UnSubmittedAdded;

  const factory ActivityFacilityState.unSubmittedDeleted() =
      _UnSubmittedDeleted;

  const factory ActivityFacilityState.reportCountsLoaded({
    required int newReportCount,
    required int inboxCount,
    required int submittedCount,
  }) = ReportCountsLoaded;

  const factory ActivityFacilityState.newlyAssignedLoaded(int count) =
      NewlyAssignedLoaded;

  const factory ActivityFacilityState.sorted({
    required List<ActivityFacilityWorkflow> activityFacilityList,
    required String sortDirection,
  }) = ActivityFacilitySortedState;

  const factory ActivityFacilityState.searchLoading() =
      ActivityFacilitySearchLoading;
  const factory ActivityFacilityState.searchResults(
      List<ActivityFacilityWorkflow> results) = ProjectSearchResults;
}
