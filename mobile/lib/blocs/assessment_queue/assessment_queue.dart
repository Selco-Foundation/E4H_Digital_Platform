import 'package:flutter_bloc/flutter_bloc.dart';

import '../../model/assessment/assessment_mode.dart';
import '../../model/assessment/assessment_queue.dart';
import '../../repositories/assessment_queue_repo.dart';

class AssessmentQueueBloc
    extends Bloc<AssessmentQueueEvent, AssessmentQueueState> {
  final AssessmentQueueRepository repository;
  final AssessmentMode assessmentMode;
  int _requestGeneration = 0;

  AssessmentQueueBloc({
    required this.repository,
    required this.assessmentMode,
  }) : super(const AssessmentQueueInitial()) {
    on<AssessmentQueueLoadInitial>(_onLoadInitial);
    on<AssessmentQueueRefresh>(_onRefresh);
    on<AssessmentQueueLoadMore>(_onLoadMore);
  }

  Future<void> _onLoadInitial(
    AssessmentQueueLoadInitial event,
    Emitter<AssessmentQueueState> emit,
  ) async {
    await _loadFirstPage(
      query: event.query,
      sortOrder: event.sortOrder,
      emit: emit,
    );
  }

  Future<void> _onRefresh(
    AssessmentQueueRefresh event,
    Emitter<AssessmentQueueState> emit,
  ) async {
    await _loadFirstPage(
      query: event.query,
      sortOrder: event.sortOrder,
      emit: emit,
    );
  }

  Future<void> _loadFirstPage({
    required String query,
    required String sortOrder,
    required Emitter<AssessmentQueueState> emit,
  }) async {
    final generation = ++_requestGeneration;
    emit(const AssessmentQueueLoading());
    try {
      final response = await repository.search(
        assessmentMode: assessmentMode,
        searchText: query,
        sortOrder: sortOrder,
      );
      if (generation != _requestGeneration) return;
      emit(AssessmentQueueLoaded(
        facilities: response.facilities,
        total: response.pagination.total,
        nextOffset: response.pagination.offset + response.facilities.length,
        hasMore: response.pagination.offset + response.facilities.length <
            response.pagination.total,
      ));
    } catch (error) {
      if (generation != _requestGeneration) return;
      emit(AssessmentQueueFailure(error.toString()));
    }
  }

  Future<void> _onLoadMore(
    AssessmentQueueLoadMore event,
    Emitter<AssessmentQueueState> emit,
  ) async {
    final current = state;
    if (current is! AssessmentQueueLoaded ||
        !current.hasMore ||
        current.isLoadingMore) {
      return;
    }

    final generation = _requestGeneration;
    emit(current.copyWith(isLoadingMore: true, clearLoadMoreError: true));
    try {
      final response = await repository.search(
        assessmentMode: assessmentMode,
        searchText: event.query,
        sortOrder: event.sortOrder,
        offset: current.nextOffset,
      );
      if (generation != _requestGeneration) return;

      final byId = <String, AssessmentQueueFacility>{};
      final withoutId = <AssessmentQueueFacility>[];
      for (final facility in [...current.facilities, ...response.facilities]) {
        final id = facility.planFacilityId;
        if (id == null || id.isEmpty) {
          withoutId.add(facility);
        } else {
          byId[id] = facility;
        }
      }
      final facilities = [...byId.values, ...withoutId];
      emit(AssessmentQueueLoaded(
        facilities: facilities,
        total: response.pagination.total,
        nextOffset: response.pagination.offset + response.facilities.length,
        hasMore: response.pagination.offset + response.facilities.length <
                response.pagination.total &&
            response.facilities.isNotEmpty,
      ));
    } catch (error) {
      if (generation != _requestGeneration) return;
      emit(current.copyWith(
        isLoadingMore: false,
        loadMoreError: error.toString(),
      ));
    }
  }
}

abstract class AssessmentQueueEvent {
  const AssessmentQueueEvent();
}

class AssessmentQueueLoadInitial extends AssessmentQueueEvent {
  final String query;
  final String sortOrder;

  const AssessmentQueueLoadInitial({
    this.query = '',
    this.sortOrder = 'DESC',
  });
}

class AssessmentQueueRefresh extends AssessmentQueueEvent {
  final String query;
  final String sortOrder;

  const AssessmentQueueRefresh({
    this.query = '',
    this.sortOrder = 'DESC',
  });
}

class AssessmentQueueLoadMore extends AssessmentQueueEvent {
  final String query;
  final String sortOrder;

  const AssessmentQueueLoadMore({
    this.query = '',
    this.sortOrder = 'DESC',
  });
}

abstract class AssessmentQueueState {
  const AssessmentQueueState();
}

class AssessmentQueueInitial extends AssessmentQueueState {
  const AssessmentQueueInitial();
}

class AssessmentQueueLoading extends AssessmentQueueState {
  const AssessmentQueueLoading();
}

class AssessmentQueueLoaded extends AssessmentQueueState {
  final List<AssessmentQueueFacility> facilities;
  final int total;
  final int nextOffset;
  final bool hasMore;
  final bool isLoadingMore;
  final String? loadMoreError;

  const AssessmentQueueLoaded({
    required this.facilities,
    required this.total,
    required this.nextOffset,
    required this.hasMore,
    this.isLoadingMore = false,
    this.loadMoreError,
  });

  AssessmentQueueLoaded copyWith({
    List<AssessmentQueueFacility>? facilities,
    int? total,
    int? nextOffset,
    bool? hasMore,
    bool? isLoadingMore,
    String? loadMoreError,
    bool clearLoadMoreError = false,
  }) {
    return AssessmentQueueLoaded(
      facilities: facilities ?? this.facilities,
      total: total ?? this.total,
      nextOffset: nextOffset ?? this.nextOffset,
      hasMore: hasMore ?? this.hasMore,
      isLoadingMore: isLoadingMore ?? this.isLoadingMore,
      loadMoreError:
          clearLoadMoreError ? null : loadMoreError ?? this.loadMoreError,
    );
  }
}

class AssessmentQueueFailure extends AssessmentQueueState {
  final String message;

  const AssessmentQueueFailure(this.message);
}
