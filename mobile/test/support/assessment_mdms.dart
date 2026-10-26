import '../../lib/model/appconfig/mdmsRequest.dart';
import '../../lib/repositories/app_init_repo.dart';

class AssessmentMdms extends AppInitRepo {
  List<Map<String, dynamic>> categories = [
    for (final entry
        in {'Health': 'HF', 'Anganwadi': 'AWC', 'School': 'SCH'}.entries)
      {
        'schemaCode': 'facility.CategoryAssessmentForm',
        'isActive': true,
        'data': {
          'active': true,
          'category': entry.key,
          'assessment_code_form': [
            '${entry.value}_PHONE',
            '${entry.value}_FIELD'
          ],
        },
      },
  ];
  bool? lastCacheOnly;

  @override
  Future<List<Map<String, dynamic>>> searchAssessmentCategoryFormsRaw(
    MdmsRequestModel request, {
    bool useCacheRead = false,
    bool cacheOnly = false,
    MdmsRawDocsValidator? validator,
  }) async {
    lastCacheOnly = cacheOnly;
    validator?.call(categories);
    return categories;
  }
}
