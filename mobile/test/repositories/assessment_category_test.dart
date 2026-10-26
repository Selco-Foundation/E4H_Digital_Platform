import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../lib/repositories/app_init_repo.dart';
import '../../lib/model/assessment/assessment_form.dart';
import '../../lib/model/assessment/assessment_mode.dart';
import '../../lib/repositories/assessment_form_repo.dart';
import '../support/assessment_mdms.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('category MDMS is cached separately and available offline', () async {
    FlutterSecureStorage.setMockInitialValues({});
    var offline = false;
    var requests = 0;
    final dio = Dio()
      ..interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        requests++;
        if (offline) {
          handler.reject(DioException(
              requestOptions: options, type: DioExceptionType.connectionError));
        } else {
          handler.resolve(
              Response(requestOptions: options, statusCode: 200, data: {
            'mdms': options.data
                    .toString()
                    .contains(AssessmentFormRepository.categorySchemaCode)
                ? AssessmentMdms().categories
                : [
                    for (final code in [
                      'HF_PHONE',
                      'HF_FIELD',
                      'AWC_PHONE',
                      'AWC_FIELD',
                      'SCH_PHONE',
                      'SCH_FIELD'
                    ])
                      {
                        'schemaCode': AssessmentFormRepository.mobileSchemaCode,
                        'uniqueIdentifier': code,
                        'isActive': true,
                        'data': {
                          'name': 'Assessment.$code',
                          'formType': code,
                          'pages': [
                            {
                              'page': 'details',
                              'properties': [
                                {
                                  'fieldName': 'remarks',
                                  'type': 'string',
                                  'format': 'text'
                                }
                              ]
                            }
                          ]
                        },
                      }
                  ]
          }));
        }
      }));
    final repo = AssessmentFormRepository(
        dio: Dio(), appInitRepo: AppInitRepo(dio: dio), tenantId: 'in');
    await repo.preloadMobileSchemas();
    expect(
        await repo.selectFormCode(
            facilityCategory: 'School', mode: AssessmentMode.remote),
        'SCH_PHONE');
    offline = true;
    expect(
        await repo.selectFormCode(
            facilityCategory: 'School',
            mode: AssessmentMode.onSite,
            cacheOnly: true),
        'SCH_FIELD');
    expect((await repo.loadCachedMobileSchema('SCH_FIELD'))['uniqueIdentifier'],
        'SCH_FIELD');
    expect(requests, 2);
    await repo.preloadMobileSchemas();
    expect((await repo.loadCachedMobileSchema('SCH_PHONE'))['uniqueIdentifier'],
        'SCH_PHONE');
  });

  test(
      'selects configured codes by category and phase, including new categories',
      () async {
    final mdms = AssessmentMdms();
    final repo =
        AssessmentFormRepository(dio: Dio(), appInitRepo: mdms, tenantId: 'in');
    for (final category
        in {' health ': 'HF', 'ANGANWADI': 'AWC', 'School': 'SCH'}.entries) {
      expect(
          await repo.selectFormCode(
              facilityCategory: category.key, mode: AssessmentMode.remote),
          '${category.value}_PHONE');
      expect(
          await repo.selectFormCode(
              facilityCategory: category.key,
              mode: AssessmentMode.onSite,
              cacheOnly: true),
          '${category.value}_FIELD');
      expect(mdms.lastCacheOnly, isTrue);
    }
  });

  test('rejects missing, inactive and ambiguous category forms', () async {
    final mdms = AssessmentMdms();
    final repo =
        AssessmentFormRepository(dio: Dio(), appInitRepo: mdms, tenantId: 'in');
    Future<String> select(String category) => repo.selectFormCode(
        facilityCategory: category, mode: AssessmentMode.remote);
    await expectLater(select('Unknown'), throwsFormatException);
    mdms.categories.last['isActive'] = false;
    await expectLater(select('School'), throwsFormatException);
    (mdms.categories.first['data']['assessment_code_form'] as List)
        .add('SECOND_PHONE');
    await expectLater(select('Health'), throwsFormatException);
  });

  test('backend resolve mismatch blocks submission for a dynamic form',
      () async {
    final paths = <String>[];
    final dio = Dio()
      ..interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        paths.add(options.path);
        handler.resolve(Response(
            requestOptions: options,
            statusCode: 200,
            data: {'formType': 'OTHER_PHONE'}));
      }));
    final repo = AssessmentFormRepository(
        dio: dio, appInitRepo: AssessmentMdms(), tenantId: 'in');
    await expectLater(
        repo.resolveForm(
            planFacilityId: 'school',
            facilityCategory: 'School',
            assessmentMode: AssessmentMode.remote),
        throwsA(isA<AssessmentApiException>()));
    expect(paths, [AssessmentFormRepository.resolvePath]);
  });
}
