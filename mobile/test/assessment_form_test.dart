import 'dart:convert';

import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/blocs/user_type/user_type.dart';
import 'package:selco/pages/assessment_select_facility.dart';
import 'package:selco/pages/assessment_submission_success.dart';
import 'package:selco/repositories/assessment_mock_form_repo.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/widgets/cards/assessment_facility_card.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('HF phone fixture has one record, four pages, and 34 field codes',
      () async {
    final source = await rootBundle.loadString(
      AssessmentMockFormRepository.assetPath,
    );
    final fixture = jsonDecode(source) as Map<String, dynamic>;
    final records = fixture['mdms'] as List<dynamic>;
    final data = records.single['data'] as Map<String, dynamic>;
    final pages = data['pages'] as List<dynamic>;
    final fieldCodes = pages
        .expand((page) => page['properties'] as List<dynamic>)
        .map((property) => property['fieldName'] as String)
        .toList();

    expect(data['formType'], 'HF_PHONE');
    expect(data['name'], 'Assessment.HF_PHONE');
    expect(records, hasLength(1));
    expect(pages, hasLength(4));
    expect(pages.map((page) => page['order']), [1, 2, 3, 4]);
    expect(fieldCodes, hasLength(34));
    expect(fieldCodes.toSet(), hasLength(34));
  });

  test('assessment repository transforms validation and visibility metadata',
      () async {
    final transformed = await AssessmentMockFormRepository().loadFormSchema();
    final schema = SchemaObject.fromJson(transformed);
    final services = schema.pages['facilityServices']!;
    final solar = services.properties!['existingSolarEquipment']!;
    final deliveries = services.properties!['averageMonthlyDeliveries']!;
    final hfr = schema.pages['energyAndIdentifiers']!.properties!['hfrId']!;

    expect(schema.name, 'Assessment.HF_PHONE');
    expect(schema.pages.keys, [
      'assessorFacilityDetails',
      'facilityServices',
      'infrastructure',
      'energyAndIdentifiers',
    ]);
    expect(solar.isMultiSelect, isTrue);
    expect(
      solar.visibilityCondition?.expression,
      'facilityServices.existingSolarSystem == "YES"',
    );
    expect(deliveries.minValue, 0);
    expect(
      deliveries.validations?.map((rule) => rule.type),
      containsAll(['required', 'minValue']),
    );
    expect(
      hfr.validations?.firstWhere((rule) => rule.type == 'pattern').value,
      '^(IN[0-9]{10}|0)\$',
    );
  });

  test('FormsBloc submits and clears the assessment only in memory', () async {
    final bloc = FormsBloc();
    addTearDown(bloc.close);
    final transformed = await AssessmentMockFormRepository().loadFormSchema();

    final loaded = bloc.stream.firstWhere(
      (state) => state.cachedSchemas.containsKey('Assessment.HF_PHONE'),
    );
    bloc.add(FormsEvent.load(schemas: [jsonEncode(transformed)]));
    await loaded;

    final submitted = bloc.stream.firstWhere(
      (state) => state is FormsSubmittedState,
    );
    bloc.add(
      const FormsEvent.submit(schemaKey: 'Assessment.HF_PHONE'),
    );
    final state = await submitted as FormsSubmittedState;

    expect(state.formData.keys, hasLength(4));
    expect(state.activeSchemaKey, 'Assessment.HF_PHONE');

    final cleared = bloc.stream.firstWhere(
      (next) => next is! FormsSubmittedState,
    );
    bloc.add(
      const FormsEvent.clearForm(schemaKey: 'Assessment.HF_PHONE'),
    );
    expect((await cleared).cachedSchemas, contains('Assessment.HF_PHONE'));
  });

  testWidgets('Start Assessment opens the first local form page',
      (tester) async {
    final router = AppRouter();
    final formsBloc = FormsBloc();
    final userTypeBloc = UserTypeBloc()
      ..add(const UserTypeEvent.typeSelected('assessor'));
    addTearDown(formsBloc.close);
    addTearDown(userTypeBloc.close);

    await tester.pumpWidget(
      MultiBlocProvider(
        providers: [
          BlocProvider.value(value: formsBloc),
          BlocProvider.value(value: userTypeBloc),
        ],
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: AssessmentSelectFacilityPage()),
        ),
      ),
    );
    await tester.pump();
    tester
        .widget<AssessmentFacilityCard>(find.byType(AssessmentFacilityCard))
        .onStartAssessment();
    await tester.pump();

    expect(router.isRouteActive(AssessmentDynamicFormRoute.name), isTrue);
  });

  testWidgets('success Home clears the form and returns to assessment home',
      (tester) async {
    final router = AppRouter();
    final formsBloc = FormsBloc();
    addTearDown(formsBloc.close);

    await tester.pumpWidget(
      BlocProvider.value(
        value: formsBloc,
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(
            home: AssessmentSubmissionSuccessPage(
              schemaName: 'Assessment.HF_PHONE',
            ),
          ),
        ),
      ),
    );

    expect(find.text('Assessment Submitted Successfully'), findsOneWidget);
    expect(find.text('Home'), findsOneWidget);
    await tester.tap(find.text('Home'));
    await tester.pump();

    expect(router.isRouteActive(AssessmentHomeRoute.name), isTrue);
  });
}
