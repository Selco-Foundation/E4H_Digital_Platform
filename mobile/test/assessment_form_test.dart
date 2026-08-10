import 'dart:convert';

import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/model/assessment/assessment_form.dart';
import 'package:selco/model/assessment/assessment_mode.dart';
import 'package:selco/model/assessment/assessment_queue.dart';
import 'package:selco/repositories/assessment_api_paths.dart';
import 'package:selco/repositories/assessment_form_repo.dart';
import 'package:selco/repositories/assessment_queue_repo.dart';
import 'package:selco/utils/assessment_form_mapper.dart';

class _RecordingAdapter implements HttpClientAdapter {
  final int submitStatus;
  final List<RequestOptions> requests = [];

  _RecordingAdapter({this.submitStatus = 200});

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requests.add(options);
    if (options.path.endsWith('/form/_resolve')) {
      return ResponseBody.fromString(
        jsonEncode({
          'formType': 'HF_PHONE',
          'schema': {'fields': []}
        }),
        200,
        headers: {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      );
    }
    return ResponseBody.fromString(
      jsonEncode({
        'idempotentReplay': true,
        'submission': {'id': 'submission-1', 'outcome': 'QUALIFIED'},
      }),
      submitStatus,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

class _QueueAdapter implements HttpClientAdapter {
  final List<RequestOptions> requests = [];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requests.add(options);
    return ResponseBody.fromString(
      jsonEncode({
        'ResponseInfo': {'status': 'successful'},
        'queue': [
          {
            'planFacilityId': 'pf-health',
            'facilityId': 'FAC/1',
            'facilityName': 'Health Facility',
            'facilityCategory': ' Health ',
            'phoneStatus': 'PENDING',
          },
          {
            'planFacilityId': 'pf-awc',
            'facilityId': 'FAC/2',
            'facilityName': 'Anganwadi Facility',
            'facilityCategory': 'aNgAnWaDi',
            'phoneStatus': 'PENDING',
          },
        ],
      }),
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const schemaDocument = '''
  {
    "mdms": [{
      "schemaCode": "assessment.AssessmentMobileFormSchema",
      "data": {
        "formType": "HF_PHONE",
        "name": "Assessment.HF_PHONE",
        "version": 1,
        "pages": [{
          "page": "details",
          "type": "object",
          "properties": [
            {"fieldName":"facilityName","type":"string","readOnly":true,"value":"Facility"},
            {"fieldName":"govtOwned","type":"string","value":"YES"},
            {"fieldName":"existingSolar","type":"string","value":"NO"},
            {"fieldName":"equipment","type":"string","isMultiSelect":true,"value":"LIGHTS.FANS"},
            {"fieldName":"hiddenAnswer","type":"string","value":"OLD","visibilityCondition":{"expression":"details.existingSolar == \\"YES\\""}}
          ]
        }]
      }
    }]
  }
  ''';

  group('AssessmentFormRepository', () {
    late Dio dio;
    late _RecordingAdapter adapter;
    late AssessmentFormRepository repository;

    setUp(() {
      adapter = _RecordingAdapter();
      dio = Dio(BaseOptions(baseUrl: 'https://example.test/'))
        ..httpClientAdapter = adapter;
      repository = AssessmentFormRepository(
        dio: dio,
        tenantId: 'in',
        schemaLoader: () async => schemaDocument,
      );
    });

    test('resolves PHONE before loading the private mobile schema', () async {
      final facility = AssessmentQueueFacility.fromJson({
        'facilityCategory': 'Health',
      });
      final resolution = await repository.resolvePhoneForm(
        planFacilityId: 'pf-1',
        facilityCategory: facility.facilityCategory!,
      );
      final schema = await repository.loadPhoneMobileSchema();

      expect(resolution.formType, 'HF_PHONE');
      expect(schema['name'], 'Assessment.HF_PHONE');
      expect(adapter.requests, hasLength(1));
      expect(
          adapter.requests.single.path, AssessmentFormRepository.resolvePath);
      expect(adapter.requests.single.data,
          containsPair('assessmentPhase', 'PHONE'));
      expect(
          adapter.requests.single.data, containsPair('planFacilityId', 'pf-1'));
      expect(adapter.requests.single.data,
          containsPair('facilityCategory', 'HEALTH'));
    });

    test('submits only the production PHONE request contract', () async {
      final response = await repository.submitPhoneAssessment(
        const AssessmentSubmissionRequest(
          planFacilityId: 'pf-1',
          tenantId: 'in',
          facilityCategory: 'HEALTH',
          submissionData: {'govtOwned': 'YES'},
          submittedByName: 'Assessor',
          clientSubmissionTime: 123,
        ),
      );

      expect(response.idempotentReplay, isTrue);
      final data = adapter.requests.single.data as Map<String, dynamic>;
      expect(data['assessmentPhase'], 'PHONE');
      expect(data['submissionData'], {'govtOwned': 'YES'});
      expect(data, isNot(contains('formType')));
      expect(data, isNot(contains('facilityName')));
      expect(data, isNot(contains('outcome')));
    });

    test('accepts a 201 assessment creation response', () async {
      final createdAdapter = _RecordingAdapter(submitStatus: 201);
      final createdDio = Dio(BaseOptions(baseUrl: 'https://example.test/'))
        ..httpClientAdapter = createdAdapter;
      final createdRepository = AssessmentFormRepository(
        dio: createdDio,
        tenantId: 'in',
        schemaLoader: () async => schemaDocument,
      );

      final logs = <String>[];
      final originalDebugPrint = debugPrint;
      late AssessmentSubmissionResponse response;
      debugPrint = (message, {wrapWidth}) => logs.add(message ?? '');
      try {
        response = await createdRepository.submitPhoneAssessment(
          const AssessmentSubmissionRequest(
            planFacilityId: 'pf-1',
            tenantId: 'in',
            facilityCategory: 'HEALTH',
            submissionData: {'govtOwned': 'YES'},
            submittedByName: 'Assessor',
            clientSubmissionTime: 123,
          ),
        );
      } finally {
        debugPrint = originalDebugPrint;
      }

      expect(response.submissionId, 'submission-1');
      expect(createdAdapter.requests, hasLength(1));
      final logged = logs.join('\n');
      expect(logged, contains('statusCode: 201'));
      expect(logged, contains('submissionId: submission-1'));
      expect(logged, isNot(contains('submissionData')));
      expect(logged, isNot(contains('govtOwned')));
    });
  });

  group('live assessment queue compatibility', () {
    test('all assessment endpoints use the field-planner gateway', () {
      expect(
        AssessmentQueueRepository.queueSearchPath,
        'field-planner/assessment/v1/submission/queue/_search',
      );
      expect(
        AssessmentFormRepository.resolvePath,
        'field-planner/assessment/v1/submission/form/_resolve',
      );
      expect(
        AssessmentFormRepository.phoneSubmissionPath,
        'field-planner/assessment/v1/submission/phone/_create',
      );
      expect(AssessmentApiPaths.serviceBase, 'field-planner/assessment/v1');
    });

    test('parses title-case categories and missing pagination', () async {
      final adapter = _QueueAdapter();
      final dio = Dio(BaseOptions(baseUrl: 'https://example.test/'))
        ..httpClientAdapter = adapter;
      final repository = AssessmentQueueRepository(
        dio: dio,
        tenantId: 'in',
      );

      final response = await repository.search(
        assessmentMode: AssessmentMode.remote,
        searchText: 'health',
        sortOrder: 'ASC',
        offset: 0,
        limit: 10,
      );

      expect(response.facilities, hasLength(2));
      expect(response.facilities.first.facilityCategory, 'HEALTH');
      expect(response.facilities.last.facilityCategory, 'ANGANWADI');
      expect(response.pagination.offset, 0);
      expect(response.pagination.limit, 10);
      expect(response.pagination.total, 2);
      expect(
        response.pagination.offset + response.facilities.length <
            response.pagination.total,
        isFalse,
      );

      final request = adapter.requests.single;
      expect(request.path, AssessmentApiPaths.queueSearch);
      expect(request.data, containsPair('assessmentPhase', 'PHONE'));
      expect(request.data, containsPair('tenantId', 'in'));
      expect(request.data, containsPair('searchText', 'health'));
      expect(request.data, containsPair('sortBy', 'lastActionTime'));
      expect(request.data, containsPair('sortOrder', 'ASC'));
      expect(request.data, containsPair('offset', 0));
      expect(request.data, containsPair('limit', 10));
    });

    test('normalizes unknown non-empty category without fabricating it', () {
      final facility = AssessmentQueueFacility.fromJson({
        'facilityCategory': ' custom_category ',
      });
      expect(facility.facilityCategory, 'CUSTOM_CATEGORY');
      expect(
        AssessmentQueueFacility.fromJson({}).facilityCategory,
        isNull,
      );
    });
  });

  test('payload excludes read-only and invisible values and converts arrays',
      () async {
    final repository = AssessmentFormRepository(
      dio: Dio(),
      schemaLoader: () async => schemaDocument,
    );
    final raw = await repository.loadPhoneMobileSchema();
    final schema = SchemaObject.fromJson(raw);

    expect(buildAssessmentSubmissionData(schema), {
      'govtOwned': 'YES',
      'existingSolar': 'NO',
      'equipment': ['LIGHTS', 'FANS'],
    });
  });

  test('bundled production schema has 34 fields and corrected backend codes',
      () async {
    final repository = AssessmentFormRepository(dio: Dio());
    final raw = await repository.loadPhoneMobileSchema();
    final schema = SchemaObject.fromJson(raw);
    final properties = schema.pages.values
        .expand((page) =>
            page.properties?.entries ?? <MapEntry<String, PropertySchema>>[])
        .toList();

    expect(schema.pages, hasLength(4));
    expect(properties, hasLength(34));
    expect(properties.where((entry) => entry.value.readOnly == true),
        hasLength(10));
    expect(properties.map((entry) => entry.key), contains('govtOwned'));
    expect(properties.map((entry) => entry.key), contains('existingSolar'));
    expect(properties.map((entry) => entry.key),
        isNot(contains('governmentOwnedFacility')));
    expect(properties.map((entry) => entry.key),
        isNot(contains('existingSolarSystem')));
  });
}
