import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';

import '../../lib/data/nosql/cache_activity_facility_workflow.dart';
import '../../lib/data/nosql/cache_assessment_queue.dart';
import '../../lib/data/nosql/cache_prefilled_scheduled_visit.dart';
import '../../lib/data/nosql/cache_scheduled_visit.dart';
import '../../lib/data/nosql/cache_unsubmitted_activity_facility.dart';
import '../../lib/data/secure_storage/secureStore.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../lib/model/assessment/assessment_mode.dart';
import '../../lib/model/assessment/assessment_queue.dart';
import '../../lib/model/scheduled_visit/scheduled_visit.dart';
import '../../lib/repositories/activity_facility_repo.dart';
import '../../lib/repositories/assessment_queue_cache_repo.dart';
import '../../lib/repositories/assessment_queue_repo.dart';
import '../../lib/repositories/scheduled_visit_repo.dart';
import '../../lib/utils/envConfig.dart';
import '../support/offline_isar.dart';
import 'report_bookmark_test.dart' as bookmarks_support;
import '../../lib/repositories/report_bookmark_repo.dart';
import '../../lib/repositories/assessment_bookmark_repo.dart';
import '../../lib/model/assessment/assessment_form_type.dart';

DioException failure([int? status]) => DioException(
    requestOptions: RequestOptions(),
    type: status == null
        ? DioExceptionType.connectionError
        : DioExceptionType.badResponse,
    response: status == null
        ? null
        : Response(requestOptions: RequestOptions(), statusCode: status));

class ActivityRemote extends ActivityFacilityRemoteRepository {
  List<ActivityFacilityWorkflow> items = [];
  Object? error;
  ActivityRemote() : super(dio: Dio());
  @override
  Future<List<ActivityFacilityWorkflow>> searchByWorkflow(
      {required ActivityFacilitySearchModel body,
      required List<String> workflowStatuses,
      int limit = 100,
      offset = 0,
      sortDirection = 'DESC'}) async {
    if (error != null) throw error!;
    return items;
  }

  @override
  Future<int> searchByWorkflowCount(
          {required ActivityFacilitySearchModel body,
          required List<String> workflowStatuses,
          int limit = 100,
          offset = 0}) async =>
      items.length;
}

class VisitRemote extends ScheduledVisitRemoteRepository {
  List<ScheduledVisit> items = [];
  Object? error;
  VisitRemote() : super(dio: Dio());
  @override
  Future<ScheduledVisitSearchResponse> search(
      {required ScheduledVisitSearchCriteria criteria,
      required int limit,
      required int offset}) async {
    if (error != null) throw error!;
    return ScheduledVisitSearchResponse(
        scheduledVisits: items, totalCount: items.length);
  }
}

ActivityFacilityWorkflow activity(String id, String name, int date,
        {String status = 'ASSIGNED_TO_FIELD_STAFF'}) =>
    ActivityFacilityWorkflow(
        status: status,
        activityFacility: ActivityFacility(
            id: id,
            facility: Facility()..facilityName = name,
            scheduledAt: DateTime(2026, 1, date)));

ScheduledVisit visit(String id, String name, int date,
        {String status = 'SCHEDULED'}) =>
    ScheduledVisit(
        id: id,
        facilityId: id,
        facility: Facility()..facilityName = name,
        status: status,
        scheduledDate: DateTime(2026, 1, date));

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Directory directory;
  late Isar isar;
  setUpAll(initializeOfflineIsar);
  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await envConfig.initialize();
    directory = await Directory.systemTemp.createTemp('offline-search-');
    isar = await openOfflineIsar(directory.path, 'offline-search');
  });
  tearDown(() async {
    if (isar.isOpen) await isar.close();
    await directory.delete(recursive: true);
  });

  test('downloaded pages restore invalidated snapshots without new bookmarks',
      () async {
    final store = SecureStore();
    await store.setAccessInfo(
        (await bookmarks_support.ReportStore().getAccessInfo())!);
    final tenant = envConfig.variables.tenantId;
    final installationBookmarks = InstallationBookmarkRepository(
        tenantId: tenant, userId: 'user', userType: 'FIELD_STAFF');
    final amcBookmarks = AmcBookmarkRepository(
        tenantId: tenant, userId: 'user', userType: 'AMC');
    final assessmentBookmarks = AssessmentBookmarkRepository(
        tenantId: 'tenant', assessorId: 'user', phase: AssessmentPhase.PHONE);
    await installationBookmarks.save(activity('one', 'Old', 1));
    await amcBookmarks.save(visit('one', 'Old', 1));
    await assessmentBookmarks.save(const AssessmentQueueFacility(
        planFacilityId: 'one', facilityName: 'Old'));
    await installationBookmarks.invalidate('one');
    await amcBookmarks.invalidate('one');
    await assessmentBookmarks.invalidate('one');
    final installations = ActivityFacilityRepository(isar,
        remote: ActivityRemote()
          ..items = [
            activity('one', 'Updated', 2),
            activity('other', 'Other', 2)
          ]);
    await installations.fetchByWorkflowPaginated(
        body: ActivityFacilitySearchModel(),
        workflowStatuses: ['ASSIGNED_TO_FIELD_STAFF']);
    final visits = ScheduledVisitRepository(isar,
        remote: VisitRemote()
          ..items = [visit('one', 'Updated', 2), visit('other', 'Other', 2)]);
    await visits.fetchByWorkflowStatus(statuses: ['SCHEDULED']);
    final dio = Dio();
    dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
      handler.resolve(Response(requestOptions: options, statusCode: 200, data: {
        'queue': [
          {'planFacilityId': 'one', 'facilityName': 'Updated'},
          {'planFacilityId': 'other', 'facilityName': 'Other'}
        ]
      }));
    }));
    await AssessmentQueueRepository(
            dio: dio,
            tenantId: 'tenant',
            assessorId: 'user',
            cache: IsarAssessmentQueueCache(isar))
        .search(assessmentMode: AssessmentMode.remote);
    expect(
        (await installationBookmarks.list())
            .single
            .activityFacility
            .facility!
            .facilityName,
        'Updated');
    expect(
        (await amcBookmarks.list()).single.facility!.facilityName, 'Updated');
    expect((await assessmentBookmarks.list()).single.facilityName, 'Updated');
    expect(await installationBookmarks.ids(), {'one'});
    expect(await amcBookmarks.ids(), {'one'});
    expect(await assessmentBookmarks.ids(), {'one'});
    dio.close();
  });

  test(
      'installation cache searches, excludes drafts, sorts and pages matching records',
      () async {
    final remote = ActivityRemote()
      ..items = [
        activity('a', 'Kanur 7', 1),
        activity('b', 'Maldare', 2),
        activity('c', 'KANUR North', 3),
        activity('draft', 'Kanur Draft', 4),
        activity('other', 'Kanur Approved', 5, status: 'APPROVED')
      ];
    final repo = ActivityFacilityRepository(isar, remote: remote);
    Future<PaginatedActivityFacilities> search(String query,
            {int offset = 0, int limit = 1, String sort = 'ASC'}) =>
        repo.fetchByWorkflowPaginated(
            body: ActivityFacilitySearchModel(facilityName: query),
            workflowStatuses: ['ASSIGNED_TO_FIELD_STAFF'],
            offset: offset,
            limit: limit,
            sortDirection: sort);
    await search('');
    await isar.writeTxn(() async {
      await isar.cacheUnsubmittedActivityFacilitys.put(
          CacheUnsubmittedActivityFacility(
              activityFacilityId: 'draft',
              status: 'ASSIGNED_TO_FIELD_STAFF',
              activityFacility:
                  activity('draft', 'Kanur Draft', 4).activityFacility,
              userType: 'STAFF'));
      await isar.cacheActivityFacilityWorkflows.put(
          CacheActivityFacilityWorkflow(
              activityFacilityId: 'b',
              status: 'ASSIGNED_TO_FIELD_STAFF',
              activityFacility: activity('b', 'Maldare', 2).activityFacility));
    });
    remote.error = failure();
    final first = await search(' kAnUr ');
    expect(first.fromCache, isTrue);
    expect(first.totalCount, 2);
    expect(first.items.single.activityFacility.id, 'a');
    expect((await search('kanur', offset: 1)).items.single.activityFacility.id,
        'c');
    expect((await search('k', sort: 'DESC')).items.single.activityFacility.id,
        'c');
    expect((await search('7')).totalCount, 1);
    expect((await search('missing')).items, isEmpty);
    expect((await search('', limit: 10)).totalCount, 3);
    final unpaged = await repo.fetchByWorkflow(
        body: ActivityFacilitySearchModel(facilityName: '7'),
        workflowStatuses: ['ASSIGNED_TO_FIELD_STAFF']);
    expect(unpaged.single.activityFacility.id, 'a');
    remote.error = null;
    remote.items = [activity('a', 'Kanur Updated', 1)];
    await search('updated');
    await search('updated');
    expect(
        await isar.cacheActivityFacilityWorkflows
            .where()
            .activityFacilityIdEqualTo('a')
            .count(),
        1);
    remote.error = failure();
    expect((await search('m')).items.single.activityFacility.id, 'b');
    remote.error = DioException(
        requestOptions: RequestOptions(), message: 'SESSION_EXPIRED');
    await expectLater(search('k'), throwsA(isA<DioException>()));
    for (final status in [401, 403]) {
      remote.error = failure(status);
      await expectLater(search('k'), throwsA(isA<DioException>()));
    }
  });

  test(
      'pending installation merges downloaded server reports with local drafts offline',
      () async {
    final remote = ActivityRemote()
      ..items = [
        activity('server', 'Kanur Server', 1,
            status: 'SUBMITTED_BY_FIELD_STAFF'),
        activity('local', 'Older Server Copy', 2,
            status: 'SUBMITTED_BY_FIELD_STAFF')
      ];
    final repo = UnsubmittedActivityFacilityRepository(isar, remote: remote);
    Future<List<ActivityFacilityWorkflow>> search(String query) =>
        repo.fetchByWorkflowIncludeCache(
            userType: 'FIELD_STAFF',
            workflowStatuses: ['SUBMITTED_BY_FIELD_STAFF'],
            body: ActivityFacilitySearchModel(facilityName: query));
    await search('');
    await isar.writeTxn(() async {
      await isar.cacheUnsubmittedActivityFacilitys.put(
          CacheUnsubmittedActivityFacility(
              activityFacilityId: 'local',
              status: 'SUBMITTED_BY_FIELD_STAFF',
              activityFacility:
                  activity('local', 'Kanur Unsynced', 2).activityFacility,
              userType: 'FIELD_STAFF'));
      await isar.cacheUnsubmittedActivityFacilitys.put(
          CacheUnsubmittedActivityFacility(
              activityFacilityId: 'other',
              status: 'SUBMITTED_BY_SUPERVISOR',
              activityFacility:
                  activity('other', 'Other User', 3).activityFacility,
              userType: 'SUPERVISOR'));
    });
    remote.error = failure();
    final results = await search(' kAnUr ');
    expect(results.map((record) => record.activityFacility.id),
        ['local', 'server']);
    expect(results.first.activityFacility.facility?.facilityName,
        'Kanur Unsynced');
    expect((await search('missing')), isEmpty);
    remote.error = failure(401);
    await expectLater(search('k'), throwsA(isA<DioException>()));
  });

  test(
      'AMC fallback filters before pagination and honors prefilled status rules',
      () async {
    final remote = VisitRemote()
      ..items = [
        visit('a', 'Kanur 7', 1),
        visit('b', 'Maldare', 2),
        visit('c', 'KANUR North', 3),
        visit('draft', 'Kanur Draft', 4),
        visit('pending', 'Other', 5, status: 'PENDING_OTP_APPROVAL')
      ];
    final repo = ScheduledVisitRepository(isar, remote: remote);
    Future<PaginatedScheduledVisits> search(String query,
            {int offset = 0,
            int limit = 1,
            String sort = 'ASC',
            List<String> statuses = const ['SCHEDULED']}) =>
        repo.fetchByWorkflowStatus(
            statuses: statuses,
            facilityName: query,
            sortDirection: sort,
            offset: offset,
            limit: limit);
    await search('');
    await isar.writeTxn(() async {
      await isar.cachePrefilledScheduledVisits.put(CachePrefilledScheduledVisit(
          scheduledVisitId: 'draft', userType: 'STAFF'));
      await isar.cacheScheduledVisits
          .put(CacheScheduledVisit.fromModel(visit('b', 'Maldare', 2)));
    });
    remote.error = failure();
    final first = await search(' kAnUr ');
    expect(first.fromCache, isTrue);
    expect(first.totalCount, 2);
    expect(first.items.single.id, 'a');
    expect((await search('kanur', offset: 1)).items.single.id, 'c');
    expect((await search('k', sort: 'DESC')).items.single.id, 'c');
    expect((await search('7')).totalCount, 1);
    expect((await search('missing')).items, isEmpty);
    expect((await search('', limit: 10)).totalCount, 3);
    expect(
        (await search('Kanur', statuses: ['PENDING_OTP_APPROVAL']))
            .items
            .single
            .id,
        'draft');
    expect((await search('missing', statuses: ['PENDING_OTP_APPROVAL'])).items,
        isEmpty);
    remote.error = null;
    remote.items = [visit('a', 'Kanur Updated', 1)];
    await search('updated');
    await search('updated');
    expect(
        await isar.cacheScheduledVisits
            .where()
            .scheduledVisitIdEqualTo('a')
            .count(),
        1);
    remote.error = failure();
    expect((await search('m')).items.single.id, 'b');
    remote.error = DioException(
        requestOptions: RequestOptions(), message: 'SESSION_EXPIRED');
    await expectLater(search('k'), throwsA(isA<DioException>()));
    for (final status in [401, 403]) {
      remote.error = failure(status);
      await expectLater(search('k'), throwsA(isA<DioException>()));
    }
  });

  test(
      'adding the assessment schema preserves an existing installation database',
      () async {
    await isar.close();
    isar = await openOfflineIsar(directory.path, 'legacy',
        includeAssessment: false);
    final row = activity('legacy', 'Downloaded Facility', 1);
    await isar.writeTxn(() async {
      await isar.cacheActivityFacilityWorkflows.put(
          CacheActivityFacilityWorkflow(
              activityFacilityId: 'legacy',
              status: row.status!,
              activityFacility: row.activityFacility));
    });
    await isar.close();
    isar = await openOfflineIsar(directory.path, 'legacy');
    expect(await isar.cacheActivityFacilityWorkflows.count(), 1);
    await IsarAssessmentQueueCache(isar).save(
        tenantId: 'tenant',
        assessorId: 'user',
        phase: 'FIELD',
        facilities: [
          const AssessmentQueueFacility(
              planFacilityId: 'assessment', facilityName: 'New Facility')
        ]);
    expect(await isar.cacheAssessmentQueues.count(), 1);
  });

  test(
      'online assessment pages and searches upsert without losing other downloaded records',
      () async {
    var offline = false;
    var page = <Map<String, dynamic>>[
      {'planFacilityId': 'a', 'facilityName': 'Kanur 7', 'lastActionTime': 1},
      {'planFacilityId': 'b', 'facilityName': 'Maldare', 'lastActionTime': 2}
    ];
    final dio = Dio()
      ..interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        if (offline) {
          handler.reject(failure());
          return;
        }
        handler.resolve(Response(
            requestOptions: options,
            statusCode: 200,
            data: {'queue': page, 'count': 3}));
      }));
    final repo = AssessmentQueueRepository(
        dio: dio,
        tenantId: 'tenant',
        assessorId: 'user',
        cache: IsarAssessmentQueueCache(isar));
    await repo.search(assessmentMode: AssessmentMode.remote);
    page = [
      {
        'planFacilityId': 'c',
        'facilityName': 'KANUR North',
        'lastActionTime': 3
      }
    ];
    await repo.search(assessmentMode: AssessmentMode.remote, offset: 2);
    page = [
      {'planFacilityId': 'a', 'facilityName': 'Updated 7', 'lastActionTime': 4}
    ];
    await repo.search(
        assessmentMode: AssessmentMode.remote, searchText: 'updated');
    await repo.search(
        assessmentMode: AssessmentMode.remote, searchText: 'updated');
    offline = true;
    final matching = await repo.search(
        assessmentMode: AssessmentMode.remote, searchText: 'm');
    expect(matching.facilities.single.planFacilityId, 'b');
    expect(
        (await repo.search(
                assessmentMode: AssessmentMode.remote, searchText: ''))
            .count,
        3);
    expect((await repo.count(assessmentMode: AssessmentMode.remote)), 3);
    expect(await isar.cacheAssessmentQueues.count(), 3);
  });

  test(
      'assessment imports legacy pages, retains fresh values and persists after reopening',
      () async {
    const scope = ['queue', 'tenant', 'user', 'PHONE'];
    FlutterSecureStorage.setMockInitialValues({
      'assessmentResponse:${jsonEncode([...scope, '', 'DESC', 0, 2])}':
          jsonEncode({
        'queue': [
          {
            'planFacilityId': 'a',
            'facilityName': 'Kanur 7',
            'lastActionTime': 1
          },
          {
            'planFacilityId': 'b',
            'facilityName': 'Maldare',
            'lastActionTime': 2
          }
        ]
      }),
      'assessmentResponse:${jsonEncode([...scope, '', 'DESC', 2, 2])}':
          jsonEncode({
        'queue': [
          {
            'planFacilityId': 'c',
            'facilityName': 'KANUR North',
            'lastActionTime': 3
          }
        ]
      }),
      'assessmentResponse:${jsonEncode([
            'queue',
            'tenant',
            'other',
            'PHONE',
            '',
            'DESC',
            0,
            2
          ])}': jsonEncode({
        'queue': [
          {'planFacilityId': 'private', 'facilityName': 'Kanur Private'}
        ]
      }),
      'assessmentResponse:${jsonEncode([...scope, 'older', 'DESC', 0, 2])}':
          jsonEncode({
        'queue': [
          {
            'planFacilityId': 'a',
            'facilityName': 'Old Name',
            'lastActionTime': 0
          }
        ]
      }),
      'assessmentResponse:broken': 'invalid',
    });
    final dio = Dio()
      ..interceptors.add(InterceptorsWrapper(
          onRequest: (options, handler) => handler.reject(failure())));
    AssessmentQueueRepository repo(
            {String tenant = 'tenant', String user = 'user'}) =>
        AssessmentQueueRepository(
            dio: dio,
            tenantId: tenant,
            assessorId: user,
            cache: IsarAssessmentQueueCache(isar));
    Future<AssessmentQueueResponse> search(String query,
            {int offset = 0, int limit = 1, String sort = 'ASC'}) =>
        repo().search(
            assessmentMode: AssessmentMode.remote,
            searchText: query,
            offset: offset,
            limit: limit,
            sortOrder: sort);
    final first = await search(' kAnUr ');
    expect(first.count, 2);
    expect(first.facilities.single.planFacilityId, 'a');
    expect((await search('kanur', offset: 1)).facilities.single.planFacilityId,
        'c');
    expect((await search('k', sort: 'DESC')).facilities.single.planFacilityId,
        'c');
    expect((await search('7')).count, 1);
    expect((await search('missing')).count, 0);
    expect((await search('', limit: 10)).count, 3);
    expect(
        (await repo(tenant: 'other')
                .search(assessmentMode: AssessmentMode.remote))
            .count,
        0);
    expect(
        (await repo().search(assessmentMode: AssessmentMode.onSite)).count, 0);
    expect(
        (await repo(user: 'other')
                .search(assessmentMode: AssessmentMode.remote))
            .count,
        1);
    await IsarAssessmentQueueCache(isar).save(
        tenantId: 'tenant',
        assessorId: 'user',
        phase: 'PHONE',
        facilities: [
          const AssessmentQueueFacility(
              planFacilityId: 'a', facilityName: 'Updated', lastActionTime: 4)
        ]);
    expect((await search('updated')).count, 1);
    expect(
        await isar.cacheAssessmentQueues
            .filter()
            .assessorIdEqualTo('user')
            .count(),
        3);
    await isar.close();
    isar = await openOfflineIsar(directory.path, 'offline-search');
    expect((await search('updated')).facilities.single.planFacilityId, 'a');
    expect((await search('kanur')).count, 1);
    expect(
        await SecureStore().storage.read(
                key: 'assessmentResponse:${jsonEncode([
                  ...scope,
                  '',
                  'DESC',
                  0,
                  2
                ])}'),
        isNotNull);
  });
}
