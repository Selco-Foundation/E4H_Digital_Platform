import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/blocs/auth/authbloc.dart';
import 'package:selco/blocs/user_type/user_type.dart';
import 'package:selco/model/response/responsemodel.dart';
import 'package:selco/model/assessment/assessment_mode.dart';
import 'package:selco/pages/assessment_draft.dart';
import 'package:selco/pages/assessment_select_facility.dart';
import 'package:selco/pages/assessment_work_home.dart';
import 'package:selco/pages/role_selection.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/role_login_resolver.dart';
import 'package:selco/utils/utils.dart';
import 'package:selco/widgets/home/home_item_card.dart';
import 'package:selco/widgets/cards/assessment_facility_card.dart';
import 'package:selco/widgets/cards/report_card.dart';

class _TestAuthBloc extends AuthBloc {
  _TestAuthBloc(AuthState initialState) : super() {
    emit(initialState);
  }
}

void main() {
  test('both assessment role codes resolve to one Assessment module', () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const [assessorRoleCode, fieldPocRoleCode],
    );

    expect(resolution.requiresSelection, isFalse);
    expect(resolution.directUserType, USER_TYPES.ASSESSOR);
  });

  test('FIELD_POC alone opens Assessment directly', () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const [fieldPocRoleCode],
    );

    expect(resolution.requiresSelection, isFalse);
    expect(resolution.directUserType, USER_TYPES.ASSESSOR);
  });

  test('Assessment and Installation produce two module options', () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const [assessorRoleCode, installationReportPartAEditorRoleCode],
    );

    expect(resolution.requiresSelection, isTrue);
    expect(
      resolution.selectionOptions,
      const [RoleSelectionOption.staff, RoleSelectionOption.assessment],
    );
  });

  test('Assessment and AMC produce two module options', () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const [fieldPocRoleCode, amcFieldStaffRoleCode],
    );

    expect(resolution.requiresSelection, isTrue);
    expect(
      resolution.selectionOptions,
      const [RoleSelectionOption.amc, RoleSelectionOption.assessment],
    );
  });

  test('all capabilities produce Installation, AMC, and Assessment', () {
    final resolution = RoleLoginResolver.resolveRoleCodes(
      const [
        installationReportPartBEditorRoleCode,
        amcFieldStaffRoleCode,
        assessorRoleCode,
        fieldPocRoleCode,
      ],
    );

    expect(resolution.requiresSelection, isTrue);
    expect(
      resolution.selectionOptions,
      const [
        RoleSelectionOption.supervisor,
        RoleSelectionOption.amc,
        RoleSelectionOption.assessment,
      ],
    );
  });

  test('existing AMC and Installation resolution remains unchanged', () {
    final mixed = RoleLoginResolver.resolveRoleCodes(
      const [amcFieldStaffRoleCode, installationReportPartAEditorRoleCode],
    );
    final amcOnly = RoleLoginResolver.resolveRoleCodes(
      const [amcFieldStaffRoleCode],
    );

    expect(
      mixed.selectionOptions,
      const [RoleSelectionOption.staff, RoleSelectionOption.amc],
    );
    expect(amcOnly.directUserType, USER_TYPES.AMC);
  });

  testWidgets('mixed-role page shows a one-line Assessment module card',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(375, 812));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    final roles = [
      const Roles(
        name: 'Installation Editor',
        code: installationReportPartAEditorRoleCode,
        tenantId: 'in',
      ),
      const Roles(
        name: 'AMC Field Staff',
        code: amcFieldStaffRoleCode,
        tenantId: 'in',
      ),
      const Roles(
        name: 'Enumerator',
        code: assessorRoleCode,
        tenantId: 'in',
      ),
      const Roles(
        name: 'Field POC',
        code: fieldPocRoleCode,
        tenantId: 'in',
      ),
    ];
    final authBloc = _TestAuthBloc(
      AuthState.authenticated(
        accesstoken: 'test-token',
        refreshtoken: 'test-refresh-token',
        userRequest: UserRequest(
          id: 1,
          uuid: 'test-user',
          userName: 'assessment-user',
          name: 'Assessment User',
          mobileNumber: '9876500000',
          emailId: null,
          type: 'EMPLOYEE',
          active: true,
          roles: roles,
          tenantId: 'in',
        ),
      ),
    );
    final userTypeBloc = UserTypeBloc();
    final router = AppRouter();
    addTearDown(authBloc.close);
    addTearDown(userTypeBloc.close);

    await tester.pumpWidget(
      MultiBlocProvider(
        providers: [
          BlocProvider<AuthBloc>.value(value: authBloc),
          BlocProvider<UserTypeBloc>.value(value: userTypeBloc),
        ],
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: RoleSelectionPage()),
        ),
      ),
    );
    await tester.pump();

    expect(find.text('Installation'), findsOneWidget);
    expect(find.text('AMC'), findsOneWidget);
    expect(find.text('Assessment'), findsOneWidget);
    expect(find.byType(HomeItemCard), findsNWidgets(3));

    final assessmentCard = tester
        .widgetList<HomeItemCard>(find.byType(HomeItemCard))
        .singleWhere((card) => card.label == 'Assessment');
    expect(
      assessmentCard.labelPadding,
      const EdgeInsets.symmetric(horizontal: spacer2),
    );
    expect(assessmentCard.fitLabelOnOneLine, isTrue);

    assessmentCard.onPressed?.call();
    await tester.pump();

    expect(router.isRouteActive(AssessmentHomeRoute.name), isTrue);
    expect(userTypeBloc.state, const UserTypeState.assessor());
  });

  testWidgets('ENUMERATOR sees Remote Assessment and shared Drafts',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(375, 812));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    final authBloc = _authBlocForRoles(const [
      Roles(
        name: 'Enumerator',
        code: assessorRoleCode,
        tenantId: 'in',
      ),
    ]);
    final router = AppRouter();
    addTearDown(authBloc.close);

    await tester.pumpWidget(
      BlocProvider<AuthBloc>.value(
        value: authBloc,
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: AssessmentWorkHomePage()),
        ),
      ),
    );

    expect(find.text('New Remote Assessment'), findsOneWidget);
    expect(find.text('New On-site Assessment'), findsNothing);
    expect(find.text('Drafts'), findsOneWidget);
    expect(find.byType(ReportCard), findsNWidgets(2));
  });

  testWidgets('FIELD_POC sees On-site Assessment and shared Drafts',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(375, 812));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    final authBloc = _authBlocForRoles(const [
      Roles(
        name: 'Field POC',
        code: fieldPocRoleCode,
        tenantId: 'in',
      ),
    ]);
    final router = AppRouter();
    addTearDown(authBloc.close);

    await tester.pumpWidget(
      BlocProvider<AuthBloc>.value(
        value: authBloc,
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: AssessmentWorkHomePage()),
        ),
      ),
    );

    expect(find.text('New Remote Assessment'), findsNothing);
    expect(find.text('New On-site Assessment'), findsOneWidget);
    expect(find.text('Drafts'), findsOneWidget);
    expect(find.byType(ReportCard), findsNWidgets(2));
  });

  testWidgets('dual assessment roles see both work cards and Drafts',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(375, 812));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    final authBloc = _authBlocForRoles(const [
      Roles(
        name: 'Enumerator',
        code: assessorRoleCode,
        tenantId: 'in',
      ),
      Roles(
        name: 'Field POC',
        code: fieldPocRoleCode,
        tenantId: 'in',
      ),
    ]);
    final router = AppRouter();
    addTearDown(authBloc.close);

    await tester.pumpWidget(
      BlocProvider<AuthBloc>.value(
        value: authBloc,
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: AssessmentWorkHomePage()),
        ),
      ),
    );

    expect(find.text('New Remote Assessment'), findsOneWidget);
    expect(find.text('New On-site Assessment'), findsOneWidget);
    expect(find.text('Drafts'), findsOneWidget);
    expect(find.byType(ReportCard), findsNWidgets(3));
  });

  testWidgets('single assessment roles see draft content without mode tabs',
      (tester) async {
    Future<void> pumpDraftsFor(List<Roles> roles) async {
      final authBloc = _authBlocForRoles(roles);
      final router = AppRouter();
      addTearDown(authBloc.close);

      await tester.pumpWidget(
        BlocProvider<AuthBloc>.value(
          value: authBloc,
          child: StackRouterScope(
            controller: router,
            stateHash: 0,
            child: MaterialApp(
              home: AssessmentDraftPage(
                key: ValueKey(roles.first.code),
              ),
            ),
          ),
        ),
      );
      await tester.pump();
    }

    await pumpDraftsFor(const [
      Roles(
        name: 'Enumerator',
        code: assessorRoleCode,
        tenantId: 'in',
      ),
    ]);
    expect(find.byType(DigitTabBar), findsNothing);
    expect(
      find.byKey(const ValueKey('assessment-drafts-remote')),
      findsOneWidget,
    );
    expect(find.text('No drafts to display'), findsOneWidget);

    await pumpDraftsFor(const [
      Roles(
        name: 'Field POC',
        code: fieldPocRoleCode,
        tenantId: 'in',
      ),
    ]);
    expect(find.byType(DigitTabBar), findsNothing);
    expect(
      find.byKey(const ValueKey('assessment-drafts-onSite')),
      findsOneWidget,
    );
    expect(find.text('No drafts to display'), findsOneWidget);
  });

  testWidgets('dual assessment roles can switch Remote and On-site drafts',
      (tester) async {
    final authBloc = _authBlocForRoles(const [
      Roles(
        name: 'Enumerator',
        code: assessorRoleCode,
        tenantId: 'in',
      ),
      Roles(
        name: 'Field POC',
        code: fieldPocRoleCode,
        tenantId: 'in',
      ),
    ]);
    final router = AppRouter();
    addTearDown(authBloc.close);

    await tester.pumpWidget(
      BlocProvider<AuthBloc>.value(
        value: authBloc,
        child: StackRouterScope(
          controller: router,
          stateHash: 0,
          child: const MaterialApp(home: AssessmentDraftPage()),
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(DigitTabBar), findsOneWidget);
    expect(find.text('Remote'), findsOneWidget);
    expect(find.text('On-site'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('assessment-drafts-remote')),
      findsOneWidget,
    );

    await tester.tap(find.text('On-site'));
    await tester.pump();

    expect(
      find.byKey(const ValueKey('assessment-drafts-onSite')),
      findsOneWidget,
    );
    expect(find.text('No drafts to display'), findsOneWidget);
  });

  testWidgets('remote and on-site modes control unable-to-contact UI',
      (tester) async {
    final formsBloc = FormsBloc();
    var router = AppRouter();
    addTearDown(formsBloc.close);

    Future<void> pumpMode(AssessmentMode mode) async {
      await tester.pumpWidget(
        BlocProvider<FormsBloc>.value(
          value: formsBloc,
          child: StackRouterScope(
            controller: router,
            stateHash: 0,
            child: MaterialApp(
              home: AssessmentSelectFacilityPage(assessmentMode: mode),
            ),
          ),
        ),
      );
      await tester.pump();
    }

    await pumpMode(AssessmentMode.remote);
    expect(find.text('Could not reach?'), findsOneWidget);
    expect(find.byType(RadioList), findsOneWidget);

    final remoteCard = tester.widget<AssessmentFacilityCard>(
      find.byType(AssessmentFacilityCard),
    );
    remoteCard.onStartAssessment();
    await tester.pump();
    expect(router.isRouteActive(AssessmentDynamicFormRoute.name), isTrue);

    router = AppRouter();
    await pumpMode(AssessmentMode.onSite);
    expect(find.text('Could not reach?'), findsNothing);
    expect(find.byType(RadioList), findsNothing);

    final onSiteCard = tester.widget<AssessmentFacilityCard>(
      find.byType(AssessmentFacilityCard),
    );
    onSiteCard.onStartAssessment();
    await tester.pump();
    expect(router.isRouteActive(AssessmentDynamicFormRoute.name), isTrue);
  });
}

_TestAuthBloc _authBlocForRoles(List<Roles> roles) {
  return _TestAuthBloc(
    AuthState.authenticated(
      accesstoken: 'test-token',
      refreshtoken: 'test-refresh-token',
      userRequest: UserRequest(
        id: 1,
        uuid: 'test-user',
        userName: 'assessment-user',
        name: 'Assessment User',
        mobileNumber: '9876500000',
        emailId: null,
        type: 'EMPLOYEE',
        active: true,
        roles: roles,
        tenantId: 'in',
      ),
    ),
  );
}
