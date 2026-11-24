import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../lib/model/assessment/assessment_form.dart';
import '../../lib/model/assessment/assessment_form_type.dart';
import '../../lib/model/assessment/assessment_mode.dart';
import '../../lib/model/assessment/assessment_queue.dart';
import '../../lib/repositories/app_init_repo.dart';
import '../../lib/repositories/assessment_form_repo.dart';
import '../../lib/utils/envConfig.dart' as env;
import '../support/assessment_mdms.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Dio mdmsDio;
  late AssessmentFormRepository repo;
  var offline = false;
  late List<String> requestedSchemas;
  late List<Map<String, dynamic>> documents;

  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await env.envConfig.initialize();
    offline = false;
    requestedSchemas = [];
    documents = [
      for (final code in [
        'AWC_NEW_PHONE',
        'AWC_PHONE',
        'AWC_FIELD',
        'HF_PHONE',
        'HF_FIELD'
      ])
        assessmentMobileSchema(code),
    ];
    mdmsDio = Dio()
      ..interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        requestedSchemas.add(options.data['MdmsCriteria'].schemaCode as String);
        if (offline) {
          handler.reject(DioException(
              requestOptions: options, type: DioExceptionType.connectionError));
        } else {
          handler.resolve(Response(
              requestOptions: options,
              statusCode: 200,
              data: {'mdms': documents}));
        }
      }));
    repo = AssessmentFormRepository(
        dio: Dio(), appInitRepo: AppInitRepo(dio: mdmsDio), tenantId: 'in');
  });
  tearDown(() => mdmsDio.close());

  test('queue questionnaire fields survive JSON serialization and old records',
      () {
    final facility = AssessmentQueueFacility.fromJson({
      'facilityCategory': 'Anganwadi',
      'onSiteQuestionnaire': 'AWC_FIELD',
      'onRemoteQuestionnaire': 'AWC_NEW_PHONE',
    });
    final restored = AssessmentQueueFacility.fromJson(facility.toJson());
    expect(restored.onRemoteQuestionnaire, 'AWC_NEW_PHONE');
    expect(restored.onSiteQuestionnaire, 'AWC_FIELD');
    final legacy =
        AssessmentQueueFacility.fromJson({'facilityCategory': 'Health'});
    expect(legacy.onRemoteQuestionnaire, isNull);
    expect(legacy.onSiteQuestionnaire, isNull);
  });

  test('assigned questionnaires take precedence without a category lookup',
      () async {
    expect(
        await repo.selectFormCode(
            facilityCategory: 'Anganwadi',
            mode: AssessmentMode.remote,
            questionnaire: ' AWC_NEW_PHONE '),
        'AWC_NEW_PHONE');
    expect(
        await repo.selectFormCode(
            facilityCategory: 'Anganwadi',
            mode: AssessmentMode.onSite,
            questionnaire: 'AWC_FIELD'),
        'AWC_FIELD');
    expect(
        await repo.selectFormCode(
            facilityCategory: 'School',
            mode: AssessmentMode.remote,
            questionnaire: 'SCH_PHONE'),
        'SCH_PHONE');
    expect(requestedSchemas, isEmpty);
  });

  test(
      'null and blank questionnaires use all four legacy category/phase mappings',
      () async {
    for (final category in {' health ': 'HF', 'ANGANWADI': 'AWC'}.entries) {
      for (final mode in AssessmentMode.values) {
        for (final questionnaire in [null, '', '   ']) {
          expect(
              await repo.selectFormCode(
                  facilityCategory: category.key,
                  mode: mode,
                  questionnaire: questionnaire),
              '${category.value}_${mode == AssessmentMode.remote ? 'PHONE' : 'FIELD'}');
        }
      }
    }
    await expectLater(
        repo.selectFormCode(
            facilityCategory: 'Unknown', mode: AssessmentMode.remote),
        throwsFormatException);
    expect(requestedSchemas, isEmpty);
  });

  test(
      'preload derives forms from mobile MDMS only and remains available offline',
      () async {
    await repo.preloadMobileSchemas();
    expect(requestedSchemas, [AssessmentFormRepository.mobileSchemaCode]);
    offline = true;
    expect(
        (await repo
            .loadCachedMobileSchema('AWC_NEW_PHONE'))['uniqueIdentifier'],
        'AWC_NEW_PHONE');
    expect((await repo.loadMobileSchema('HF_FIELD'))['uniqueIdentifier'],
        'HF_FIELD');
    await repo.preloadMobileSchemas();
    expect(
        (await repo
            .loadCachedMobileSchema('AWC_NEW_PHONE'))['uniqueIdentifier'],
        'AWC_NEW_PHONE');
    expect(
        requestedSchemas
            .every((s) => s == AssessmentFormRepository.mobileSchemaCode),
        isTrue);
  });

  test('explicit missing questionnaire fails instead of loading a legacy form',
      () async {
    final selected = await repo.selectFormCode(
        facilityCategory: 'Anganwadi',
        mode: AssessmentMode.remote,
        questionnaire: 'AWC_MISSING_PHONE');
    await expectLater(repo.loadMobileSchema(selected), throwsFormatException);
    expect((await repo.loadCachedMobileSchema('AWC_PHONE'))['uniqueIdentifier'],
        'AWC_PHONE');
  });

  test('existing mobile schema validation still rejects mismatched identifiers',
      () async {
    documents = [assessmentMobileSchema('AWC_NEW_PHONE')];
    documents.single['data']['name'] = 'Assessment.WRONG_PHONE';
    await expectLater(repo.preloadMobileSchemas(), throwsFormatException);
  });

  for (final matches in [true, false]) {
    test(
        'custom questionnaire resolver matches: $matches; payloads stay unchanged',
        () async {
      final requests = <RequestOptions>[];
      final dio = Dio()
        ..interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
          requests.add(options);
          handler.resolve(Response(
              requestOptions: options,
              statusCode: 200,
              data: options.path.endsWith('_resolve')
                  ? {'formType': matches ? 'AWC_NEW_PHONE' : 'AWC_PHONE'}
                  : {
                      'submission': {'id': 'report'}
                    }));
        }));
      final forms = AssessmentFormRepository(dio: dio, tenantId: 'in');
      const request = AssessmentSubmissionRequest(
          planFacilityId: 'facility',
          tenantId: 'in',
          facilityCategory: 'ANGANWADI',
          assessmentPhase: AssessmentPhase.PHONE,
          submissionData: {'remarks': 'test'},
          clientSubmissionTime: 123);
      final submission =
          forms.submitAssessment(request, formType: 'AWC_NEW_PHONE');
      if (matches) {
        expect((await submission).submissionId, 'report');
        expect(
            requests.last.path, AssessmentFormRepository.phoneSubmissionPath);
        expect(requests.last.data, request.toJson());
      } else {
        await expectLater(submission, throwsA(isA<AssessmentApiException>()));
        expect(requests, hasLength(1));
      }
      expect(requests.first.data, {
        'planFacilityId': 'facility',
        'facilityCategory': 'ANGANWADI',
        'assessmentPhase': 'PHONE',
        'tenantId': 'in',
      });
      dio.close();
    });
  }
}
