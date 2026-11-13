import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';

import '../../lib/blocs/activity_facility/activity_facility.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../lib/repositories/activity_facility_repo.dart';
import '../../lib/utils/envConfig.dart';

class UnusedIsar implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('List tests must not access the database');
}

class ListRepository extends ActivityFacilityRepository {
  Completer<PaginatedActivityFacilities> pending = Completer();
  final offsets = <int>[];
  ListRepository(super.isar);

  @override
  Future<PaginatedActivityFacilities> fetchByWorkflowPaginated({
    required ActivityFacilitySearchModel body,
    required List<String> workflowStatuses,
    int limit = ActivityFacilityRepository.defaultPageSize,
    int offset = 0,
    String sortDirection = 'DESC',
  }) {
    offsets.add(offset);
    return pending.future;
  }
}

void main() {
  late ActivityFacilityBloc bloc;
  late ListRepository repository;
  const fetch = ActivityFacilityEvent.fetchActivityFacilityByWorkflow(
      workflowStatuses: ['ASSIGNED_TO_FIELD_STAFF']);
  const more = ActivityFacilityEvent.loadMoreActivityFacility(
      workflowStatuses: ['ASSIGNED_TO_FIELD_STAFF']);
  final first = ActivityFacilityWorkflow(
      activityFacility: ActivityFacility(id: 'first'),
      status: 'ASSIGNED_TO_FIELD_STAFF');
  final second = ActivityFacilityWorkflow(
      activityFacility: ActivityFacility(id: 'second'),
      status: 'ASSIGNED_TO_FIELD_STAFF');

  setUp(() async {
    await envConfig.initialize();
    final isar = UnusedIsar();
    repository = ListRepository(isar);
    bloc = ActivityFacilityBloc(isar, repository: repository);
  });
  tearDown(() => bloc.close());

  Future<void> start() async {
    final loading = bloc.stream
        .firstWhere((state) => state is ActivityFacilitySearchLoading);
    bloc.add(fetch);
    await loading;
  }

  test('delayed request stays loading; failure is retryable rather than empty',
      () async {
    await start();
    expect(bloc.state, isA<ActivityFacilitySearchLoading>());
    final failed =
        bloc.stream.firstWhere((state) => state is ActivityFacilityListFailure);
    repository.pending.completeError(StateError('Server unavailable'));
    await failed;
    repository.pending = Completer();
    await start();
    final ready = bloc.stream
        .firstWhere((state) => state is ActivityFacilityPaginatedLoaded);
    repository.pending
        .complete(PaginatedActivityFacilities(items: [first], totalCount: 1));
    expect((await ready as ActivityFacilityPaginatedLoaded).items, [first]);
    expect(repository.offsets, [0, 0]);
  });

  for (final cached in [false, true]) {
    test(
        '${cached ? 'failed cache fallback' : 'successful response'} with no rows',
        () async {
      await start();
      final done = bloc.stream
          .firstWhere((state) => state is! ActivityFacilitySearchLoading);
      repository.pending.complete(PaginatedActivityFacilities(
          items: [], totalCount: 0, fromCache: cached));
      expect(
          await done,
          cached
              ? isA<ActivityFacilityListFailure>()
              : isA<ActivityFacilityPaginatedLoaded>());
    });
  }

  test('usable cache remains visible', () async {
    await start();
    final ready = bloc.stream
        .firstWhere((state) => state is ActivityFacilityPaginatedLoaded);
    repository.pending.complete(PaginatedActivityFacilities(
        items: [first], totalCount: 1, fromCache: true));
    final state = await ready as ActivityFacilityPaginatedLoaded;
    expect(state.items, [first]);
    expect(state.fromCache, isTrue);
  });

  test('pagination retains rows and continues after an unrelated state',
      () async {
    await start();
    var ready = bloc.stream
        .firstWhere((state) => state is ActivityFacilityPaginatedLoaded);
    repository.pending
        .complete(PaginatedActivityFacilities(items: [first], totalCount: 2));
    await ready;
    final unrelated = bloc.stream
        .firstWhere((state) => state is ActivityFacilitySelectedState);
    bloc.add(const ActivityFacilityEvent.selectActivityFacility('other'));
    await unrelated;
    repository.pending = Completer();
    final loading = bloc.stream.firstWhere((state) =>
        state is ActivityFacilityPaginatedLoaded && state.isLoadingMore);
    bloc.add(more);
    expect((await loading as ActivityFacilityPaginatedLoaded).items, [first]);
    ready = bloc.stream.firstWhere((state) =>
        state is ActivityFacilityPaginatedLoaded && !state.isLoadingMore);
    repository.pending
        .complete(PaginatedActivityFacilities(items: [second], totalCount: 2));
    expect((await ready as ActivityFacilityPaginatedLoaded).items,
        [first, second]);
    expect(repository.offsets, [0, ActivityFacilityRepository.defaultPageSize]);
  });
}
