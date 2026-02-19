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
  static const _pageSize = ActivityFacilityRepository.defaultPageSize;
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
    on<LoadMoreActivityFacilityEvent>(_onLoadMoreActivityFacility);
    on<ActivityFacilityCheckIfInCache>(_checkIfInCache);
  }

  FutureOr<void> _selectActivityFacility(
      ActivityFacilitySelectEvent event, Emitter<ActivityFacilityState> emit) {
    emit(ActivityFacilityState.selected(event.activityFacilityId));
  }

  FutureOr<void> _handleFetchActivityFacilityByWorkflow(
      FetchActivityFacilityByWorkflowEvent event,
      Emitter<ActivityFacilityState> emit) async {
    await _fetchPaginatedInitial(
      emit,
      workflowStatuses: event.workflowStatuses,
    );
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

    Future<int> fetchCount(List<String> statuses) async {
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
      fetchCount(newStatuses),
      fetchCount(inboxStatuses),
      fetchCount(submittedStatuses),
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
    await _fetchPaginatedInitial(
      emit,
      workflowStatuses: event.workflowStatuses,
      sortDirection: event.sortDirection,
    );
  }

  Future<void> _handleFetchActivityFacilityBySearch(
    FetchActivityFacilityBySearchEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    if (event.query.length < 3) {
      emit(const ActivityFacilityState.initial());
      return;
    }
    await _fetchPaginatedInitial(
      emit,
      workflowStatuses: event.workflowStatuses,
      query: event.query,
    );
  }

  Future<void> _onLoadMoreActivityFacility(
    LoadMoreActivityFacilityEvent event,
    Emitter<ActivityFacilityState> emit,
  ) async {
    final currentState = state;
    if (currentState is! ActivityFacilityPaginatedLoaded) return;
    if (!currentState.hasMore || currentState.isLoadingMore) return;

    emit(currentState.copyWith(isLoadingMore: true));

    final repo = ActivityFacilityRepository(isar);
    final body = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
      facilityName: event.query,
    );

    try {
      final result = await repo.fetchByWorkflowPaginated(
        body: body,
        workflowStatuses: event.workflowStatuses,
        limit: _pageSize,
        offset: currentState.rawFetchedCount,
        sortDirection: event.sortDirection ?? 'ASC',
      );

      final ids = currentState.items
          .map((e) => e.activityFacility.id)
          .where((e) => e.isNotEmpty)
          .toSet();
      final merged = <ActivityFacilityWorkflow>[...currentState.items];
      for (final item in result.items) {
        final id = item.activityFacility.id;
        if (id.isEmpty || ids.contains(id)) continue;
        ids.add(id);
        merged.add(item);
      }

      emit(currentState.copyWith(
        items: merged,
        hasMore: result.items.isNotEmpty && merged.length < result.totalCount,
        totalCount: result.totalCount,
        fromCache: result.fromCache,
        rawFetchedCount: currentState.rawFetchedCount + _pageSize,
        isLoadingMore: false,
      ));
    } catch (_) {
      emit(currentState.copyWith(isLoadingMore: false));
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

  Future<void> _fetchPaginatedInitial(
    Emitter<ActivityFacilityState> emit, {
    required List<String> workflowStatuses,
    String? query,
    String? sortDirection,
  }) async {
    emit(const ActivityFacilityState.loading());

    final repo = ActivityFacilityRepository(isar);
    final body = ActivityFacilitySearchModel(
      tenantId: envConfig.variables.tenantId,
      facilityName: query,
    );

    try {
      final result = await repo.fetchByWorkflowPaginated(
        body: body,
        workflowStatuses: workflowStatuses,
        limit: _pageSize,
        offset: 0,
        sortDirection: sortDirection ?? 'ASC',
      );
      emit(ActivityFacilityState.paginatedLoaded(
        items: result.items,
        hasMore: result.items.length < result.totalCount,
        totalCount: result.totalCount,
        fromCache: result.fromCache,
        rawFetchedCount: _pageSize,
      ));
    } catch (_) {
      emit(const ActivityFacilityState.paginatedLoaded(
        items: [],
        hasMore: false,
        totalCount: 0,
      ));
    }
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

  const factory ActivityFacilityEvent.loadMoreActivityFacility({
    required List<String> workflowStatuses,
    String? query,
    String? sortDirection,
  }) = LoadMoreActivityFacilityEvent;

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

  const factory ActivityFacilityState.paginatedLoaded({
    required List<ActivityFacilityWorkflow> items,
    required bool hasMore,
    required int totalCount,
    @Default(false) bool fromCache,
    @Default(false) bool isLoadingMore,
    @Default(0) int rawFetchedCount,
  }) = ActivityFacilityPaginatedLoaded;
}
