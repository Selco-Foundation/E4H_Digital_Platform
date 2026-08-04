import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/atoms/digit_search_form_input.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/blocs/user_type/user_type.dart';
import 'package:selco/model/login/loginModel.dart';
import 'package:selco/pages/assessment_draft.dart';
import 'package:selco/pages/assessment_home.dart';
import 'package:selco/pages/assessment_select_facility.dart';
import 'package:selco/pages/assessment_work_home.dart';
import 'package:selco/repositories/auth_repo.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/envConfig.dart';
import 'package:selco/utils/role_login_resolver.dart';
import 'package:selco/utils/utils.dart';
import 'package:selco/widgets/cards/report_card.dart';
import 'package:selco/widgets/cards/assessment_facility_card.dart';
import 'package:selco/widgets/button/footer_button.dart';
import 'package:selco/widgets/home/home_item_card.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUpAll(() async {
    await envConfig.initialize();
  });

  test('development assessor credentials load the local login fixture',
      () async {
    final repository = AuthRepository();
    final response = await repository.validateLogin(
      const LoginModel(
        username: '1234567895',
        password: 'Beehyv@123',
        tenantId: 'in',
        userType: 'EMPLOYEE',
        grant_type: 'password',
        scope: 'read',
      ),
    );

    expect(response.userRequest?.userName, '1234567895');
    expect(
      response.userRequest?.roles.map((role) => role.code),
      containsAll(<String>['EMPLOYEE', 'ENUMERATOR']),
    );

    final roleActions = await repository.searchRoleActions(const {});
    expect(roleActions.actions, isEmpty);
  });

  test('ENUMERATOR resolves directly to assessor', () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const ['EMPLOYEE', 'ENUMERATOR'],
    );

    expect(resolution.requiresSelection, isFalse);
    expect(resolution.directUserType, USER_TYPES.ASSESSOR);
  });

  test('assessor role takes precedence over field and AMC role combinations',
      () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const [
        'ENUMERATOR',
        'AMC_FIELD_STAFF',
        'INSTALLATION_REPORT_PART_B_EDITOR',
      ],
    );

    expect(resolution.requiresSelection, isFalse);
    expect(resolution.directUserType, USER_TYPES.ASSESSOR);
  });

  test('assessor user type event emits assessor state', () async {
    final bloc = UserTypeBloc();
    addTearDown(bloc.close);

    final nextState = bloc.stream.first;
    bloc.add(const UserTypeEvent.typeSelected('assessor'));

    expect(await nextState, const UserTypeState.assessor());
  });

  testWidgets('assessment home renders assessment and data sync tiles',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(375, 812));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    final router = AppRouter();
    await tester.pumpWidget(
      StackRouterScope(
        controller: router,
        stateHash: 0,
        child: const MaterialApp(home: AssessmentHomePage()),
      ),
    );

    expect(find.text('Assessment'), findsOneWidget);
    expect(find.text('Data Sync'), findsOneWidget);
    expect(find.byType(HomeItemCard), findsNWidgets(2));
    expect(find.byIcon(Icons.business_center_outlined), findsOneWidget);
    expect(find.byIcon(Icons.autorenew), findsOneWidget);

    final tiles = tester.widgetList<HomeItemCard>(find.byType(HomeItemCard));
    expect(
      tiles.every(
        (tile) =>
            tile.labelPadding ==
            const EdgeInsets.symmetric(horizontal: spacer2),
      ),
      isTrue,
    );
    expect(tiles.every((tile) => tile.fitLabelOnOneLine), isTrue);
    expect(find.byType(FittedBox), findsNWidgets(2));

    final assessmentText = tester.widget<Text>(find.text('Assessment'));
    expect(assessmentText.maxLines, 1);
    expect(assessmentText.softWrap, isFalse);

    tiles.last.onPressed?.call();
    await tester.pump();
    expect(router.isRouteActive(AssessmentDraftRoute.name), isTrue);

    tiles.first.onPressed?.call();
    await tester.pump();
    expect(router.isRouteActive(AssessmentWorkHomeRoute.name), isTrue);
  });

  testWidgets('assessment work home renders only its two work cards',
      (tester) async {
    final router = AppRouter();
    await tester.pumpWidget(
      StackRouterScope(
        controller: router,
        stateHash: 0,
        child: const MaterialApp(home: AssessmentWorkHomePage()),
      ),
    );

    expect(find.text('New Assessments'), findsOneWidget);
    expect(find.text('Drafts'), findsOneWidget);
    expect(
      find.text(
        'View assigned facilities and resume assessments in progress.',
      ),
      findsOneWidget,
    );
    expect(
      find.text('View completed assessments waiting to sync or retry.'),
      findsOneWidget,
    );

    expect(find.byType(ReportCard), findsNWidgets(2));
  });

  testWidgets('New Assessments card opens the select facility route',
      (tester) async {
    final router = AppRouter();
    await tester.pumpWidget(
      StackRouterScope(
        controller: router,
        stateHash: 0,
        child: const MaterialApp(home: AssessmentWorkHomePage()),
      ),
    );

    final newAssessmentsCard = tester
        .widgetList<ReportCard>(find.byType(ReportCard))
        .firstWhere((card) => card.heading == 'New Assessments');
    newAssessmentsCard.onPress();
    await tester.pump();

    expect(router.isRouteActive(AssessmentSelectFacilityRoute.name), isTrue);
  });

  testWidgets('assessment select facility renders remote dummy facility card',
      (tester) async {
    final router = AppRouter();
    final userTypeBloc = UserTypeBloc()
      ..add(const UserTypeEvent.typeSelected('assessor'));
    addTearDown(userTypeBloc.close);
    await tester.pumpWidget(
      BlocProvider.value(
        value: userTypeBloc,
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: AssessmentSelectFacilityPage()),
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(Scaffold), findsWidgets);
    expect(find.byType(DigitSearchFormInput), findsOneWidget);
    expect(find.byIcon(Icons.search), findsOneWidget);
    expect(find.byIcon(Icons.import_export), findsOneWidget);
    expect(find.byIcon(Icons.filter_alt_outlined), findsNothing);
    expect(find.byType(AssessmentFacilityCard), findsOneWidget);
    expect(find.text('Digar Kashipur'), findsOneWidget);
    expect(find.text('Scheduled'), findsOneWidget);
    expect(find.text('Assam'), findsOneWidget);
    expect(find.text('Cachar'), findsOneWidget);
    expect(find.text('Cedharban'), findsOneWidget);
    expect(find.text('No assessments to display'), findsNothing);
    expect(find.text('Could not reach?'), findsOneWidget);
    expect(find.text('Select a reason'), findsOneWidget);
    expect(find.text("Ring but didn't pick"), findsOneWidget);
    expect(find.text('Wrong number'), findsOneWidget);
    expect(find.byType(RadioList), findsOneWidget);
    expect(find.text('Start Assessment'), findsOneWidget);
    expect(find.text('Update Status'), findsNothing);
    expect(find.byType(PoweredByDigit), findsNothing);

    final radioControls = find.descendant(
      of: find.byType(RadioList),
      matching: find.byType(InkWell),
    );
    await tester.tap(radioControls.first);
    await tester.pump();

    expect(find.text('Start Assessment'), findsNothing);
    expect(find.text('Update Status'), findsOneWidget);

    await tester.tap(find.text('Update Status'));
    await tester.enterText(find.byType(TextField), 'Test facility');
    await tester.tap(find.byIcon(Icons.import_export));
    await tester.pump();

    expect(find.byType(AssessmentSelectFacilityPage), findsOneWidget);
    expect(router.isRouteActive(AssessmentDraftRoute.name), isFalse);
  });

  testWidgets('field assessment card hides unable-to-contact reasons',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AssessmentFacilityCard(
            facilityName: 'Digar Kashipur',
            status: 'Scheduled',
            state: 'Assam',
            district: 'Cachar',
            block: 'Cedharban',
            isRemoteAssessor: false,
            onStartAssessment: () {},
            onUpdateStatus: () {},
          ),
        ),
      ),
    );

    expect(find.text('Could not reach?'), findsNothing);
    expect(find.byType(RadioList), findsNothing);
    expect(find.text('Start Assessment'), findsOneWidget);

    await tester.tap(find.text('Start Assessment'));
    await tester.pump();
    expect(find.text('Start Assessment'), findsOneWidget);
  });

  testWidgets('assessment Drafts card opens the draft route', (tester) async {
    final router = AppRouter();
    await tester.pumpWidget(
      StackRouterScope(
        controller: router,
        stateHash: 0,
        child: const MaterialApp(home: AssessmentWorkHomePage()),
      ),
    );

    final draftsCard = tester
        .widgetList<ReportCard>(find.byType(ReportCard))
        .firstWhere((card) => card.heading == 'Drafts');
    draftsCard.onPress();
    await tester.pump();

    expect(router.isRouteActive(AssessmentDraftRoute.name), isTrue);
  });

  testWidgets('assessment draft page renders an empty disabled state',
      (tester) async {
    final router = AppRouter();
    await tester.pumpWidget(
      StackRouterScope(
        controller: router,
        stateHash: 0,
        child: const MaterialApp(home: AssessmentDraftPage()),
      ),
    );

    expect(find.text('Drafts'), findsOneWidget);
    expect(find.text('No drafts to display'), findsOneWidget);
    expect(find.text('Sync'), findsOneWidget);

    final footer = tester.widget<FooterButton>(find.byType(FooterButton));
    expect(footer.isDisabled, isTrue);
    expect(footer.showSuffixIcon, isFalse);
  });
}
