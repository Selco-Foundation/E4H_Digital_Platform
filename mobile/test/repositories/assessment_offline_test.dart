import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../lib/data/secure_storage/secureStore.dart';
import '../../lib/data/network_manager.dart';
import '../../lib/model/response/responsemodel.dart';
import '../../lib/model/assessment/assessment_form.dart';
import '../../lib/model/assessment/assessment_form_type.dart';
import '../../lib/model/assessment/assessment_mode.dart';
import '../../lib/repositories/assessment_form_repo.dart';
import '../../lib/repositories/assessment_queue_repo.dart';

class MemoryStore extends SecureStore {
  final responses = <String, Map<String, dynamic>>{};
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
  test(
      'queue cache persists between repositories and isolates request dimensions',
      () async {
    final store = MemoryStore();
    final dio = Dio();
    var offline = false;
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
          'queue': [],
          'count': 12,
          'pagination': {'offset': 0, 'limit': 10, 'total': 12}
        }));
      }
    }));
    AssessmentQueueRepository repo(
            {String tenant = 'tenant', String user = 'user'}) =>
        AssessmentQueueRepository(
            dio: dio, storage: store, tenantId: tenant, assessorId: user);
    await repo()
        .search(assessmentMode: AssessmentMode.remote, searchText: ' clinic ');
    offline = true;
    final cached = await repo()
        .search(assessmentMode: AssessmentMode.remote, searchText: 'clinic');
    expect(cached.facilities, isEmpty);
    expect(cached.count, 12);
    for (final mode in [AssessmentMode.onSite]) {
      await expectLater(
          repo().search(assessmentMode: mode, searchText: 'clinic'),
          throwsA(isA<DioException>()));
    }
    await expectLater(
        repo(user: 'other').search(
            assessmentMode: AssessmentMode.remote, searchText: 'clinic'),
        throwsA(isA<DioException>()));
    await expectLater(
        repo(tenant: 'other').search(
            assessmentMode: AssessmentMode.remote, searchText: 'clinic'),
        throwsA(isA<DioException>()));
    await expectLater(
        repo().search(
            assessmentMode: AssessmentMode.remote,
            searchText: 'clinic',
            offset: 10),
        throwsA(isA<DioException>()));
    status = 503;
    expect(
        (await repo().search(
                assessmentMode: AssessmentMode.remote, searchText: 'clinic'))
            .count,
        12);
    status = 0;
    offlinePrecheck = true;
    expect(
        (await repo().search(
                assessmentMode: AssessmentMode.remote, searchText: 'clinic'))
            .count,
        12);
    await expectLater(
        repo().search(
            assessmentMode: AssessmentMode.remote, searchText: 'uncached'),
        throwsA(isA<DioException>()));
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
  });

  for (final type in AssessmentFormType.values) {
    test('${type.name} resolves before submitting and stops on resolve failure',
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
                  ? {'formType': type.name}
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
          facilityCategory: type.facilityCategory,
          assessmentPhase: type.phase,
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
