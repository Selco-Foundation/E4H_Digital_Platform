import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../lib/data/secure_storage/secureStore.dart';
import '../../lib/data/network_manager.dart';
import '../../lib/model/response/responsemodel.dart';
import '../../lib/model/assessment/assessment_form.dart';
import '../../lib/model/assessment/assessment_form_type.dart';
import '../../lib/model/assessment/assessment_mode.dart';
import '../../lib/repositories/assessment_form_repo.dart';
import '../../lib/repositories/assessment_draft_repo.dart';
import '../../lib/model/assessment/assessment_queue.dart';
import '../../lib/repositories/assessment_queue_repo.dart';
import '../../lib/pages/assessment_work_home.dart';
import '../../lib/repositories/assessment_queue_cache_repo.dart';
import '../support/offline_isar.dart';

class MemoryStore extends SecureStore {
  final responses = <String, Map<String, dynamic>>{};
  @override
  Future<List<Map<String, dynamic>>> getAssessmentQueueResponses(
          String tenantId, String assessorId, String phase) async =>
      responses.entries
          .where((entry) {
            final key = jsonDecode(entry.key) as List;
            return key.length == 8 &&
                key[0] == 'queue' &&
                key[1] == tenantId &&
                key[2] == assessorId &&
                key[3] == phase;
          })
          .map((entry) => entry.value)
          .toList();
  @override
  Future<ResponseModel?> getAccessInfo() async => null;
  @override
  Future<Map<String, dynamic>?> getAssessmentResponse(List<Object> key) async =>
      responses[jsonEncode(key)];
  @override
  Future<void> setAssessmentResponse(
      List<Object> key, Map<String, dynamic> response) async {
    responses[jsonEncode(key)] = response;
  }
}

void main() {
  setUpAll(initializeOfflineIsar);
  test('queue cache filters locally and isolates tenant, assessor and phase',
      () async {
    final store = MemoryStore();
    final directory =
        await Directory.systemTemp.createTemp('assessment-offline-');
    final isar = await openOfflineIsar(directory.path, 'assessment-offline');
    addTearDown(() async {
      await isar.close();
      await directory.delete(recursive: true);
    });
    final cache = IsarAssessmentQueueCache(isar);
    final dio = Dio();
    var offline = false;
    var emptyRemote = false;
    var status = 0;
    var offlinePrecheck = false;
    var sessionExpired = false;
    dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
      if (offline) {
        handler.reject(DioException(
            requestOptions: options,
            type: offlinePrecheck || sessionExpired
                ? DioExceptionType.unknown
                : status == 0
                    ? DioExceptionType.connectionError
                    : DioExceptionType.badResponse,
            error: offlinePrecheck
                ? const NetworkException('No internet access')
                : null,
            message: sessionExpired ? 'SESSION_EXPIRED' : null,
            response: status == 0
                ? null
                : Response(requestOptions: options, statusCode: status)));
      } else {
        handler
            .resolve(Response(requestOptions: options, statusCode: 200, data: {
          'queue': [
            if (!emptyRemote)
              {
                'planFacilityId': 'one',
                'facilityName': 'Clinic 7',
                'onRemoteQuestionnaire': 'AWC_NEW_PHONE',
                'onSiteQuestionnaire': 'AWC_FIELD',
                'lastActionTime': 1
              }
          ],
          'count': emptyRemote ? 0 : 12,
          'pagination': {
            'offset': 0,
            'limit': 10,
            'total': emptyRemote ? 0 : 12
          }
        }));
      }
    }));
    AssessmentQueueRepository repo(
            {String tenant = 'tenant', String user = 'user'}) =>
        AssessmentQueueRepository(
            dio: dio,
            storage: store,
            tenantId: tenant,
            assessorId: user,
            cache: cache);
    await repo()
        .search(assessmentMode: AssessmentMode.remote, searchText: ' clinic ');
    offline = true;
    final cached = await repo()
        .search(assessmentMode: AssessmentMode.remote, searchText: 'clinic');
    expect(cached.facilities.single.facilityName, 'Clinic 7');
    expect(cached.facilities.single.onRemoteQuestionnaire, 'AWC_NEW_PHONE');
    expect(cached.facilities.single.onSiteQuestionnaire, 'AWC_FIELD');
    expect(cached.count, 1);
    await expectLater(
      loadAssessmentWorkCounts(
        repository: repo(user: 'other'),
        hasRemoteAssessment: true,
        hasOnSiteAssessment: false,
      ),
      throwsA(isA<DioException>()),
    );

    for (final (repository, mode) in [
      (repo(), AssessmentMode.onSite),
      (repo(user: 'other'), AssessmentMode.remote),
      (repo(tenant: 'other'), AssessmentMode.remote),
    ]) {
      await expectLater(
        repository.search(
          assessmentMode: mode,
          searchText: 'clinic',
        ),
        throwsA(isA<DioException>()),
      );
    }
    expect(
        (await repo().search(
                assessmentMode: AssessmentMode.remote,
                searchText: 'clinic',
                offset: 10))
            .facilities,
        isEmpty);
    expect(
        (await repo()
                .search(assessmentMode: AssessmentMode.remote, searchText: '7'))
            .facilities,
        hasLength(1));
    status = 503;
    expect(
        (await repo().search(
                assessmentMode: AssessmentMode.remote, searchText: 'clinic'))
            .count,
        1);
    status = 0;
    offlinePrecheck = true;
    expect(
        (await repo().search(
                assessmentMode: AssessmentMode.remote, searchText: 'clinic'))
            .count,
        1);
    expect(
        (await repo().search(
                assessmentMode: AssessmentMode.remote, searchText: 'uncached'))
            .facilities,
        isEmpty);
    offlinePrecheck = false;
    for (final deniedStatus in [401, 403]) {
      status = deniedStatus;
      await expectLater(
          repo().search(
              assessmentMode: AssessmentMode.remote, searchText: 'clinic'),
          throwsA(isA<DioException>()));
    }
    status = 0;
    sessionExpired = true;
    await expectLater(
        repo().search(
            assessmentMode: AssessmentMode.remote, searchText: 'clinic'),
        throwsA(isA<DioException>()));
    offline = false;
    emptyRemote = true;
    final emptyPage = await repo(user: 'new-user')
        .search(assessmentMode: AssessmentMode.remote);
    expect(emptyPage.facilities, isEmpty);
    expect(emptyPage.count,
        0); // A successful empty response remains a valid empty result.
  });

  for (final type in ['HF_PHONE', 'HF_FIELD', 'AWC_PHONE', 'AWC_FIELD']) {
    test('$type resolves before submitting and stops on resolve failure',
        () async {
      final paths = <String>[];
      var fail = false;
      final dio = Dio();
      dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        paths.add(options.path);
        if (fail) {
          handler.reject(DioException(
              requestOptions: options, type: DioExceptionType.connectionError));
        } else {
          handler.resolve(Response(
              requestOptions: options,
              statusCode: 200,
              data: options.path.endsWith('_resolve')
                  ? {'formType': type}
                  : {
                      'submission': {'id': 'report'}
                    }));
        }
      }));
      final repo = AssessmentFormRepository(
          dio: dio, storage: MemoryStore(), tenantId: 'tenant');
      final request = AssessmentSubmissionRequest(
          planFacilityId: 'facility',
          tenantId: 'tenant',
          facilityCategory: (type.startsWith('HF_') ? 'HEALTH' : 'ANGANWADI'),
          assessmentPhase: (type.endsWith('_PHONE')
              ? AssessmentPhase.PHONE
              : AssessmentPhase.FIELD),
          submissionData: {'answer': 1},
          clientSubmissionTime: 123);
      expect((await repo.submitAssessment(request)).submissionId, 'report');
      expect(paths.first, endsWith('_resolve'));
      expect(paths.last, endsWith('_create'));
      paths.clear();
      fail = true;
      await expectLater(repo.submitAssessment(request),
          throwsA(isA<AssessmentApiException>()));
      expect(paths, hasLength(1));
      expect(request.submissionData, {'answer': 1});
    });
  }

  test(
      'drafts retain custom questionnaire metadata without changing submission payloads',
      () async {
    final directory =
        await Directory.systemTemp.createTemp('assessment-questionnaire-');
    final isar =
        await openOfflineIsar(directory.path, 'assessment-questionnaire');
    addTearDown(() async {
      await isar.close();
      await directory.delete(recursive: true);
    });
    const request = AssessmentSubmissionRequest(
        planFacilityId: 'custom',
        tenantId: 'tenant',
        facilityCategory: 'ANGANWADI',
        assessmentPhase: AssessmentPhase.PHONE,
        submissionData: {'remarks': 'saved answer'},
        clientSubmissionTime: 123);
    await AssessmentDraftRepository(isar).save(
      request: request,
      facility: const AssessmentQueueFacility(planFacilityId: 'custom'),
      assessorId: 'user',
      blocked: false,
      error: 'offline',
      facilityDefaults: {
        AssessmentDraftRepository.formTypeKey: 'AWC_NEW_PHONE'
      },
    );
    final drafts = AssessmentDraftRepository(isar);
    final draft = (await drafts.find(
        tenantId: 'tenant',
        assessorId: 'user',
        planFacilityId: 'custom',
        phase: AssessmentPhase.PHONE))!;
    final forms = AssessmentFormRepository(dio: Dio(), tenantId: 'tenant');
    expect(
        await forms.selectFormCode(
            facilityCategory: request.facilityCategory,
            mode: AssessmentMode.remote,
            questionnaire: drafts.formTypeOf(draft)),
        'AWC_NEW_PHONE');
    expect(drafts.requestOf(draft).toJson(), request.toJson());
    final legacyRequest = AssessmentSubmissionRequest.fromJson({
      ...request.toJson(),
      'planFacilityId': 'legacy',
    });
    final legacy = await drafts.save(
        request: legacyRequest,
        facility: const AssessmentQueueFacility(planFacilityId: 'legacy'),
        assessorId: 'legacy-user',
        blocked: false,
        error: 'offline');
    expect(drafts.formTypeOf(legacy), isNull);
    expect(
        await forms.selectFormCode(
            facilityCategory: request.facilityCategory,
            mode: AssessmentMode.remote,
            questionnaire: drafts.formTypeOf(legacy)),
        'AWC_PHONE');
  });

  test('cached facility details load without a network call', () async {
    final store = MemoryStore();
    await store.setAssessmentResponse(['facility', 'tenant', 'facility'],
        {'facility_id': 'facility', 'facility_name': 'Clinic'});
    final dio = Dio();
    dio.interceptors.add(InterceptorsWrapper(
        onRequest: (_, __) => fail('Unexpected network call')));
    final repo =
        AssessmentFormRepository(dio: dio, storage: store, tenantId: 'tenant');
    expect(
        (await repo.getFacilityDetails(facilityId: 'facility'))?.facilityName,
        'Clinic');
  });
}
