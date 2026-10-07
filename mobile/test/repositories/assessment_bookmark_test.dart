import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:isar/isar.dart';
import '../../lib/blocs/assessment_queue/assessment_queue.dart';
import '../../lib/model/assessment/assessment_mode.dart';
import '../../lib/repositories/assessment_queue_repo.dart';
import '../../lib/repositories/assessment_draft_repo.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../lib/data/secure_storage/secureStore.dart';
import '../../lib/model/assessment/assessment_form.dart';
import '../../lib/model/assessment/assessment_form_type.dart';
import '../../lib/model/assessment/assessment_queue.dart';
import '../../lib/model/response/responsemodel.dart';
import '../../lib/repositories/assessment_bookmark_repo.dart';
import '../../lib/repositories/assessment_form_repo.dart';

class BookmarkStore extends SecureStore {
  final values = <String, String>{};
  bool failWrites = false;
  String key(String tenant, String user, String phase) =>
      jsonEncode([tenant, user, phase]);
  @override
  Future<Map<String, dynamic>> getAssessmentBookmarks(
          String tenant, String user, String phase) async =>
      Map<String, dynamic>.from(
          jsonDecode(values[key(tenant, user, phase)] ?? '{}') as Map);
  @override
  Future<void> setAssessmentBookmarks(String tenant, String user, String phase,
      Map<String, dynamic> entries) async {
    if (failWrites) throw StateError('Storage unavailable');
    values[key(tenant, user, phase)] = jsonEncode(entries);
  }

  @override
  Future<ResponseModel?> getAccessInfo() async => const ResponseModel(
      access_token: 'token',
      token_type: null,
      refresh_token: null,
      scope: null,
      userRequest: UserRequest(
          id: 1,
          uuid: 'user',
          userName: 'user',
          name: null,
          mobileNumber: null,
          emailId: null,
          type: null,
          active: true,
          roles: [],
          tenantId: 'tenant'));
}

class UnusedIsar implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Bookmarks must not access draft storage');
}

class EmptyDrafts extends AssessmentDraftRepository {
  EmptyDrafts() : super(UnusedIsar());
  final excludedIds = <String>{};
  @override
  Future<Set<String>> draftedPlanFacilityIds(
          {required String assessorId, required AssessmentPhase phase}) async =>
      excludedIds;
}

const facility = AssessmentQueueFacility(
    planFacilityId: 'plan-facility',
    facilityId: 'facility',
    facilityName: 'Clinic',
    facilityCategory: 'HEALTH',
    district: 'District',
    facilityInCharge: AssessmentQueueContact(name: 'Contact', phone: '123'));

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test('secure storage bookmarks survive cache replacement and recreation',
      () async {
    FlutterSecureStorage.setMockInitialValues({});
    AssessmentBookmarkRepository repo() => AssessmentBookmarkRepository(
        storage: SecureStore(),
        tenantId: 'tenant',
        assessorId: 'user',
        phase: AssessmentPhase.PHONE);
    await repo().save(facility);
    final storage = SecureStore();
    await storage.setAssessmentResponse(
        ['queue', 'tenant', 'user', 'PHONE'], {'queue': []});
    await storage.setAssessmentResponse([
      'queue',
      'tenant',
      'user',
      'PHONE'
    ], {
      'queue': [
        {'planFacilityId': 'other'}
      ]
    });
    expect((await repo().list()).single.toJson(), facility.toJson());
  });
  test('bookmark-only queue loads locally with draft eligibility', () async {
    final store = BookmarkStore();
    final bookmarks = AssessmentBookmarkRepository(
        storage: store,
        tenantId: 'tenant',
        assessorId: 'user',
        phase: AssessmentPhase.PHONE);
    await bookmarks.save(facility);
    final dio = Dio();
    dio.interceptors.add(InterceptorsWrapper(
        onRequest: (_, __) => fail('Bookmarks must not call remote search')));
    final drafts = EmptyDrafts();
    final bloc = AssessmentQueueBloc(
      repository: AssessmentQueueRepository(
          dio: dio, storage: store, tenantId: 'tenant', assessorId: 'user'),
      draftRepository: drafts,
      assessmentMode: AssessmentMode.remote,
      assessorId: 'user',
      bookmarkRepository: bookmarks,
    );
    final loaded =
        bloc.stream.firstWhere((state) => state is AssessmentQueueLoaded);
    bloc.add(const AssessmentQueueLoadInitial(
        query: 'clinic', sortOrder: 'BOOKMARKED'));
    final state = await loaded as AssessmentQueueLoaded;
    expect(state.facilities.single.planFacilityId, facility.planFacilityId);
    expect(state.hasMore, isFalse);
    drafts.excludedIds.add('plan-facility');
    final hidden =
        bloc.stream.firstWhere((state) => state is AssessmentQueueLoaded);
    bloc.add(
        const AssessmentQueueRefresh(query: 'clinic', sortOrder: 'BOOKMARKED'));
    expect((await hidden as AssessmentQueueLoaded).facilities, isEmpty);
    expect(await bookmarks.ids(), {'plan-facility'});
    final empty =
        bloc.stream.firstWhere((state) => state is AssessmentQueueLoaded);
    bloc.add(const AssessmentQueueRefresh(
        query: 'missing', sortOrder: 'BOOKMARKED'));
    expect((await empty as AssessmentQueueLoaded).facilities, isEmpty);
    await bloc.close();
  });
  for (final mode in AssessmentMode.values) {
    test('$mode switches between bookmarked local results and normal queue',
        () async {
      FlutterSecureStorage.setMockInitialValues({});
      final store = BookmarkStore();
      final phase = mode == AssessmentMode.remote
          ? AssessmentPhase.PHONE
          : AssessmentPhase.FIELD;
      final bookmarks = AssessmentBookmarkRepository(
          storage: store, tenantId: 'tenant', assessorId: 'user', phase: phase);
      await bookmarks.save(facility);
      final requests = <RequestOptions>[];
      final dio = Dio();
      dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        requests.add(options);
        handler.resolve(Response(
            requestOptions: options,
            statusCode: 200,
            data: {'queue': [], 'count': 0}));
      }));
      final bloc = AssessmentQueueBloc(
          repository: AssessmentQueueRepository(
              dio: dio, storage: store, tenantId: 'tenant', assessorId: 'user'),
          draftRepository: EmptyDrafts(),
          assessmentMode: mode,
          assessorId: 'user',
          bookmarkRepository: bookmarks);
      Future<AssessmentQueueLoaded> load(String sort,
          {String query = ''}) async {
        final result =
            bloc.stream.firstWhere((state) => state is AssessmentQueueLoaded);
        bloc.add(AssessmentQueueLoadInitial(query: query, sortOrder: sort));
        return await result as AssessmentQueueLoaded;
      }

      expect(
          (await load('BOOKMARKED', query: 'c'))
              .facilities
              .single
              .planFacilityId,
          facility.planFacilityId);
      expect((await load('BOOKMARKED', query: 'missing')).facilities, isEmpty);
      expect(requests, isEmpty);
      final state = await load('BOOKMARKED');
      expect(state.hasMore, isFalse);
      bloc.add(const AssessmentQueueLoadMore(sortOrder: 'BOOKMARKED'));
      await Future<void>.delayed(Duration.zero);
      expect(requests, isEmpty);
      await load('ASC');
      expect(requests.length, 1);
      await load('BOOKMARKED');
      expect(requests.length, 1);
      await load('DESC');
      expect(requests.length, 2);
      await bookmarks.remove(facility.planFacilityId!);
      expect((await load('BOOKMARKED')).facilities, isEmpty);
      await bloc.close();
    });
  }

  test('failed bookmark saves and removals preserve persisted entries',
      () async {
    final store = BookmarkStore();
    final bookmarks = AssessmentBookmarkRepository(
        storage: store,
        tenantId: 'tenant',
        assessorId: 'user',
        phase: AssessmentPhase.PHONE);
    await bookmarks.save(facility);
    store.failWrites = true;
    await expectLater(bookmarks.remove('plan-facility'), throwsStateError);
    await expectLater(
        bookmarks.save(const AssessmentQueueFacility(planFacilityId: 'other')),
        throwsStateError);
    expect(await bookmarks.ids(), {'plan-facility'});
  });
  test('serialization retains facility and contact data', () {
    expect(AssessmentQueueFacility.fromJson(facility.toJson()).toJson(),
        facility.toJson());
  });
  test(
      'bookmarks persist, update one identity and survive unrelated cache writes',
      () async {
    final store = BookmarkStore();
    AssessmentBookmarkRepository repo() => AssessmentBookmarkRepository(
        storage: store,
        tenantId: 'tenant',
        assessorId: 'user',
        phase: AssessmentPhase.PHONE);
    await repo().save(facility);
    final savedTime = (await store.getAssessmentBookmarks(
        'tenant', 'user', 'PHONE'))['plan-facility']['bookmarkedAt'];
    await repo().save(const AssessmentQueueFacility(
        planFacilityId: 'plan-facility', facilityName: 'Updated Clinic'));
    expect(await repo().count(), 1);
    expect((await repo().list()).single.facilityName, 'Updated Clinic');
    expect(
        (await store.getAssessmentBookmarks(
            'tenant', 'user', 'PHONE'))['plan-facility']['bookmarkedAt'],
        savedTime);
    expect(await repo().list(query: ' updated '), hasLength(1));
    expect(await repo().list(query: 'missing'), isEmpty);
    for (final scope in [
      ('other', 'user', AssessmentPhase.PHONE),
      ('tenant', 'other', AssessmentPhase.PHONE),
      ('tenant', 'user', AssessmentPhase.FIELD)
    ]) {
      expect(
          await AssessmentBookmarkRepository(
                  storage: store,
                  tenantId: scope.$1,
                  assessorId: scope.$2,
                  phase: scope.$3)
              .count(),
          0);
    }
    await repo().remove('plan-facility');
    expect(await repo().count(), 0);
  });
  test('concurrent saves across repository instances do not lose entries',
      () async {
    final store = BookmarkStore();
    AssessmentBookmarkRepository repo() => AssessmentBookmarkRepository(
        storage: store,
        tenantId: 'tenant',
        assessorId: 'user',
        phase: AssessmentPhase.FIELD);
    await Future.wait([
      for (var i = 0; i < 10; i++)
        repo().save(
            AssessmentQueueFacility(planFacilityId: '$i', facilityName: '$i'))
    ]);
    expect(await repo().count(), 10);
    expect((await repo().list()).first.planFacilityId, '9');
    expect((await repo().list(sortOrder: 'ASC')).first.planFacilityId, '0');
  });
  for (final phase in AssessmentPhase.values) {
    test('${phase.name} invalidates snapshot only after confirmed submission',
        () async {
      final store = BookmarkStore();
      final bookmarks = AssessmentBookmarkRepository(
          storage: store, tenantId: 'tenant', assessorId: 'user', phase: phase);
      await bookmarks.save(facility);
      final savedAt = (await store.getAssessmentBookmarks(
          'tenant', 'user', phase.name))['plan-facility']['bookmarkedAt'];
      final otherPhase = phase == AssessmentPhase.PHONE
          ? AssessmentPhase.FIELD
          : AssessmentPhase.PHONE;
      final otherBookmarks = AssessmentBookmarkRepository(
          storage: store,
          tenantId: 'tenant',
          assessorId: 'user',
          phase: otherPhase);
      await otherBookmarks.save(facility);
      final dio = Dio();
      var resolveFails = true;
      var submissionFails = false;
      dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        if ((options.path.endsWith('_resolve') && resolveFails) ||
            (!options.path.endsWith('_resolve') && submissionFails)) {
          handler.reject(DioException(
              requestOptions: options, type: DioExceptionType.connectionError));
        } else {
          handler.resolve(Response(
              requestOptions: options,
              statusCode: 200,
              data: options.path.endsWith('_resolve')
                  ? {
                      'formType': phase == AssessmentPhase.PHONE
                          ? 'HF_PHONE'
                          : 'HF_FIELD'
                    }
                  : {
                      'submission': {'id': 'report'}
                    }));
        }
      }));
      final forms = AssessmentFormRepository(
          dio: dio, storage: store, tenantId: 'tenant');
      final request = AssessmentSubmissionRequest(
          planFacilityId: 'plan-facility',
          tenantId: 'tenant',
          facilityCategory: 'HEALTH',
          assessmentPhase: phase,
          submissionData: {},
          clientSubmissionTime: 123);
      await expectLater(forms.submitAssessment(request),
          throwsA(isA<AssessmentApiException>()));
      expect(await bookmarks.count(), 1);
      resolveFails = false;
      submissionFails = true;
      await expectLater(forms.submitAssessment(request),
          throwsA(isA<AssessmentApiException>()));
      expect(await bookmarks.count(), 1);
      submissionFails = false;
      expect((await forms.submitAssessment(request)).submissionId, 'report');
      expect(await bookmarks.count(), 1);
      expect(await bookmarks.ids(), {'plan-facility'});
      expect(await bookmarks.list(), isEmpty);
      await bookmarks.refreshSnapshots([facility]);
      expect((await bookmarks.list()).single.planFacilityId, 'plan-facility');
      expect(
          (await store.getAssessmentBookmarks(
              'tenant', 'user', phase.name))['plan-facility']['bookmarkedAt'],
          savedAt);
      expect(
          (await otherBookmarks.list()).single.planFacilityId, 'plan-facility');

      await bookmarks.save(facility);
      store.failWrites = true;
      expect((await forms.submitAssessment(request)).submissionId, 'report');
      expect(await bookmarks.count(), 1);
    });
  }
}
