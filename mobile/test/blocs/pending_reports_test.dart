import 'dart:async';
import 'dart:io';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';

import '../../lib/blocs/activity_facility/activity_facility.dart';
import '../../lib/repositories/activity_facility_repo.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/utils/envConfig.dart';
import '../repositories/offline_search_test.dart' as support;
import '../support/offline_isar.dart';

class DelayedPendingRepository extends UnsubmittedActivityFacilityRepository {
  final requests = <String, Completer<PendingReportsPage>>{};
  DelayedPendingRepository(super.isar);
  @override
  Future<PendingReportsPage> fetchPage(
      {required String userType,
      required List<String> workflowStatuses,
      required ActivityFacilitySearchModel body,
      int offset = 0,
      String sortDirection = 'DESC',
      bool? cacheOnly}) {
    return (requests[body.facilityName!] = Completer<PendingReportsPage>())
        .future;
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Directory directory;
  late Isar isar;
  late ActivityFacilityBloc bloc;
  late support.PagedActivityRemote remote;
  const statuses = ['SUBMITTED_BY_FIELD_STAFF'];

  setUpAll(initializeOfflineIsar);
  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await envConfig.initialize();
    directory = await Directory.systemTemp.createTemp('pending-pagination-');
    isar = await openOfflineIsar(directory.path, 'pending-pagination');
    remote = support.PagedActivityRemote()
      ..items = List.generate(
          23,
          (i) => support.activity('$i', 'Facility $i', i + 1,
              status: statuses.single));
    bloc = ActivityFacilityBloc(isar,
        pendingRepository:
            UnsubmittedActivityFacilityRepository(isar, remote: remote));
  });
  tearDown(() async {
    await bloc.close();
    await isar.close();
    await directory.delete(recursive: true);
  });

  Future<PendingReportsLoaded> load({bool more = false, String? query}) async {
    final result = bloc.stream.firstWhere(
        (state) => state is PendingReportsLoaded && !state.isLoadingMore);
    bloc.add(ActivityFacilityEvent.loadUnSubmitted(statuses, 'FIELD_STAFF',
        query: query, loadMore: more));
    return await result as PendingReportsLoaded;
  }

  test(
      'loads 10 at a time, preserves records on failure and retries same offset',
      () async {
    expect((await load()).unSubmitted.length, 10);
    expect(remote.requests.length, 1);
    expect((await load(more: true)).unSubmitted.length, 20);
    remote.error = support.failure();
    final failed = await load(more: true);
    expect(failed.loadFailed, isTrue);
    expect(failed.unSubmitted.length, 20);
    remote.error = null;
    final last = await load(more: true);
    expect(last.unSubmitted.length, 23);
    expect(
        last.unSubmitted.map((e) => e.activityFacility.id).toSet().length, 23);
    expect(last.hasMore, isFalse);
    expect(remote.requests.map((e) => e.offset), [0, 10, 20, 20]);
  });

  test(
      'search resets the server offset and finds a report outside the first page',
      () async {
    await load();
    await load(more: true);
    final searched = await load(query: 'Facility 0');
    expect(remote.requests.last.offset, 0);
    expect(searched.unSubmitted.single.activityFacility.id, '0');
    expect(searched.hasMore, isFalse);
    final refreshed = await load();
    expect(remote.requests.last.offset, 0);
    expect(refreshed.unSubmitted.length, 10);
  });
  test('an older search cannot replace a newer search result', () async {
    await bloc.close();
    final repository = DelayedPendingRepository(isar);
    bloc = ActivityFacilityBloc(isar, pendingRepository: repository);
    bloc.add(const ActivityFacilityEvent.loadUnSubmitted(
        statuses, 'FIELD_STAFF',
        query: 'old'));
    await Future<void>.delayed(Duration.zero);
    bloc.add(const ActivityFacilityEvent.loadUnSubmitted(
        statuses, 'FIELD_STAFF',
        query: 'new'));
    await Future<void>.delayed(Duration.zero);
    final loaded = bloc.stream.firstWhere((s) => s is PendingReportsLoaded);
    repository.requests['new']!.complete(PendingReportsPage(
        items: [support.activity('new', 'New', 1)],
        nextOffset: 1,
        hasMore: false,
        fromCache: false));
    await loaded;
    repository.requests['old']!.complete(PendingReportsPage(
        items: [support.activity('old', 'Old', 1)],
        nextOffset: 1,
        hasMore: false,
        fromCache: false));
    await Future<void>.delayed(Duration.zero);
    expect(
        (bloc.state as PendingReportsLoaded)
            .unSubmitted
            .single
            .activityFacility
            .id,
        'new');
  });
}
