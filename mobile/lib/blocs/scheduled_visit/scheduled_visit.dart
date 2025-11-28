import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../model/scheduled_visit/scheduled_visit.dart';
import '../../repositories/scheduled_visit_repo.dart';

part 'scheduled_visit.freezed.dart';

class ScheduledVisitBloc
    extends Bloc<ScheduledVisitEvent, ScheduledVisitState> {
  final ScheduledVisitRepository repository;
  final Isar isar;
  static const _pageSize = ScheduledVisitRepository.defaultPageSize;

  /// 🔹 Bloc receives Isar and builds the repository itself.
  ScheduledVisitBloc(this.isar)
      : repository = ScheduledVisitRepository(isar),
        super(const ScheduledVisitState.initial()) {
    on<_LoadInitial>(_onLoadInitial);
    on<_LoadMore>(_onLoadMore);
    on<_Refresh>(_onRefresh);
  }

  Future<void> _onLoadInitial(
    _LoadInitial event,
    Emitter<ScheduledVisitState> emit,
  ) async {
    emit(const ScheduledVisitState.loading());
    try {
      final result = await repository.fetchByWorkflowStatus(
        statuses: event.statuses,
        limit: _pageSize,
        offset: 0,
      );

      emit(
        ScheduledVisitState.loaded(
          items: result.items,
          hasMore: result.items.length < result.totalCount,
          totalCount: result.totalCount,
          fromCache: result.fromCache,
        ),
      );
    } catch (e) {
      emit(ScheduledVisitState.failure(e.toString()));
    }
  }

  Future<void> _onLoadMore(
    _LoadMore event,
    Emitter<ScheduledVisitState> emit,
  ) async {
    final current = state;
    if (current is! _Loaded) return;
    if (!current.hasMore || current.isLoadingMore) return;

    final offset = current.items.length;
    emit(current.copyWith(isLoadingMore: true));

    try {
      final result = await repository.fetchByWorkflowStatus(
        statuses: event.statuses,
        limit: _pageSize,
        offset: offset,
      );

      final newItems = [...current.items, ...result.items];
      final hasMore = newItems.length < result.totalCount;

      emit(
        current.copyWith(
          items: newItems,
          hasMore: hasMore,
          totalCount: result.totalCount,
          fromCache: result.fromCache,
          isLoadingMore: false,
        ),
      );
    } catch (_) {
      // Keep old list, just stop the loading-more spinner
      emit(current.copyWith(isLoadingMore: false));
    }
  }

  Future<void> _onRefresh(
    _Refresh event,
    Emitter<ScheduledVisitState> emit,
  ) async {
    add(ScheduledVisitEvent.loadInitial(
      statuses: event.statuses,
    ));
  }
}

@freezed
class ScheduledVisitEvent with _$ScheduledVisitEvent {
  const factory ScheduledVisitEvent.loadInitial({
    required List<String> statuses,
  }) = _LoadInitial;

  const factory ScheduledVisitEvent.loadMore({
    required List<String> statuses,
  }) = _LoadMore;

  const factory ScheduledVisitEvent.refresh({
    required List<String> statuses,
  }) = _Refresh;
}

@freezed
class ScheduledVisitState with _$ScheduledVisitState {
  const factory ScheduledVisitState.initial() = _Initial;

  const factory ScheduledVisitState.loading() = _Loading;

  const factory ScheduledVisitState.loaded({
    required List<ScheduledVisit> items,
    required bool hasMore,
    required int totalCount,
    @Default(false) bool fromCache,
    @Default(false) bool isLoadingMore,
  }) = _Loaded;

  const factory ScheduledVisitState.failure(String message) = _Failure;
}
