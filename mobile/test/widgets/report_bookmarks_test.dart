import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';
import 'package:selco/blocs/activity_facility/activity_facility.dart';
import 'package:selco/blocs/app_init/app_init.dart';
import 'package:selco/blocs/auth/authbloc.dart';
import 'package:selco/blocs/cache_asset_count/cache_asset_count.dart';
import 'package:selco/blocs/scheduled_visit/scheduled_visit.dart';
import 'package:selco/blocs/user_type/user_type.dart';
import 'package:selco/model/activity_facility/activity_facility.dart';
import 'package:selco/data/nosql/cache_amc_doc.dart';
import 'package:selco/model/activity_facility_workflow/activity_facility_workflow.dart';
import 'package:selco/model/response/responsemodel.dart';
import 'package:selco/model/scheduled_visit/scheduled_visit.dart';
import 'package:selco/pages/amc_select_facility.dart';
import 'package:selco/pages/inbox.dart';
import 'package:selco/pages/installation_report_home.dart';
import 'package:selco/blocs/inbox_type/inbox_type.dart';
import 'package:selco/blocs/report_type/report_type.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:selco/pages/select_health_facility.dart';
import 'package:selco/repositories/report_bookmark_repo.dart';
import 'package:selco/repositories/dynamic_form_repo.dart';
import 'package:selco/utils/utils.dart';
import 'package:selco/utils/envConfig.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;
import 'package:selco/widgets/bookmarks/report_bookmarks.dart';
import 'package:selco/widgets/cards/report_card.dart';

const user = UserRequest(
    id: 1,
    uuid: 'user',
    userName: 'user',
    name: null,
    mobileNumber: null,
    emailId: null,
    type: null,
    active: true,
    roles: [],
    tenantId: 'tenant');

class FakeAuth extends Bloc<AuthEvent, AuthState> implements AuthBloc {
  FakeAuth()
      : super(const AuthState.authenticated(
            accesstoken: '', refreshtoken: null, userRequest: user));
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected auth access');
}

class UnusedIsar implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected database access');
}

// Exercise the real query-builder calls against an existing local AMC draft.
class DraftIsar implements Isar {
  @override
  IsarCollection<T> collection<T>() => DraftCollection<T>(T == CacheAmcDoc
      ? (CacheAmcDoc()
        ..scheduleVisitId = 'one'
        ..schemaKey = 'AssetForm.AMC_SCHEDULED_MAINTENANCE'
        ..dataJson = '{"answer":"saved"}') as T
      : null);
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected database mutation');
}

class DraftCollection<T> extends IsarCollection<T> {
  final T? record;
  DraftCollection(this.record);
  @override
  Query<R> buildQuery<R>(
          {List<WhereClause> whereClauses = const [],
          bool whereDistinct = false,
          Sort whereSort = Sort.asc,
          FilterOperation? filter,
          List<SortProperty> sortBy = const [],
          List<DistinctProperty> distinctBy = const [],
          int? offset,
          int? limit,
          String? property}) =>
      DraftQuery<R>(record as R?);
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected collection access');
}

class DraftQuery<T> implements Query<T> {
  final T? record;
  DraftQuery(this.record);
  @override
  Future<T?> findFirst() async => record;
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected query');
}

class NoSearchActivity
    extends Bloc<ActivityFacilityEvent, ActivityFacilityState>
    implements ActivityFacilityBloc {
  NoSearchActivity() : super(const ActivityFacilityState.initial());
  @override
  Isar get isar => UnusedIsar();
  @override
  void add(ActivityFacilityEvent event) =>
      throw StateError('Bookmark list must not search remotely');
}

class NoSearchVisits extends Bloc<ScheduledVisitEvent, ScheduledVisitState>
    implements ScheduledVisitBloc {
  NoSearchVisits() : super(const ScheduledVisitState.initial());
  @override
  Isar get isar => DraftIsar();
  @override
  void add(ScheduledVisitEvent event) =>
      throw StateError('Bookmark list must not search remotely');
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected visit access');
}

class RecordingActivity
    extends Bloc<ActivityFacilityEvent, ActivityFacilityState>
    implements ActivityFacilityBloc {
  final events = <ActivityFacilityEvent>[];
  RecordingActivity()
      : super(const ActivityFacilityState.paginatedLoaded(
            items: [], hasMore: false, totalCount: 0));
  @override
  Isar get isar => UnusedIsar();
  @override
  void add(ActivityFacilityEvent event) => events.add(event);
}

class FakeCounts extends Bloc<CacheAssetCountEvent, CacheAssetCountState>
    implements CacheAssetCountBloc {
  FakeCounts() : super(const CacheAssetCountState.initial());
  @override
  void add(CacheAssetCountEvent event) {} // local progress lookups only
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected count access');
}

class FakeInit extends Bloc<InitEvent, InitState> implements AppInitialization {
  FakeInit() : super(const InitState.uninitialized());
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected initialization');
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await envConfig.initialize();
  });
  InstallationBookmarkRepository installations() =>
      InstallationBookmarkRepository(
          tenantId: envConfig.variables.tenantId,
          userId: 'user',
          userType: 'FIELD_STAFF');
  AmcBookmarkRepository visits() => AmcBookmarkRepository(
      tenantId: envConfig.variables.tenantId, userId: 'user', userType: 'AMC');
  ActivityFacilityWorkflow facility(String id, String name) =>
      ActivityFacilityWorkflow(
          activityFacility: ActivityFacility(
              id: id, facility: Facility()..facilityName = name),
          status: 'ASSIGNED_TO_FIELD_STAFF');

  Widget app(Widget child,
      {ActivityFacilityBloc? activityBloc, bool supervisor = false}) {
    final router = AppRouter();
    addTearDown(router.dispose);
    final auth = FakeAuth();
    final type = UserTypeBloc();
    if (supervisor) type.add(const UserTypeEvent.typeSelected('supervisor'));
    final activity = activityBloc ?? NoSearchActivity();
    final visits = NoSearchVisits();
    final counts = FakeCounts();
    final init = FakeInit();
    final inboxType = InboxTypeBloc();
    final reportType = ReportTypeBloc();
    addTearDown(() async {
      await auth.close();
      await type.close();
      await activity.close();
      await visits.close();
      await counts.close();
      await init.close();
      await inboxType.close();
      await reportType.close();
    });
    return MultiBlocProvider(
        providers: [
          BlocProvider<InboxTypeBloc>.value(value: inboxType),
          BlocProvider<ReportTypeBloc>.value(value: reportType),
          BlocProvider<AuthBloc>.value(value: auth),
          BlocProvider<UserTypeBloc>.value(value: type),
          BlocProvider<ActivityFacilityBloc>.value(value: activity),
          BlocProvider<ScheduledVisitBloc>.value(value: visits),
          BlocProvider<CacheAssetCountBloc>.value(value: counts),
          BlocProvider<AppInitialization>.value(value: init),
        ],
        child: MaterialApp(
            theme: DigitTheme.instance.mobileTheme,
            home: StackRouterScope(
                controller: router,
                stateHash: 0,
                child: Scaffold(body: child))));
  }

  for (final labels in [
    (
      add: i18.installationBookmarks.add,
      remove: i18.installationBookmarks.remove
    ),
    (add: i18.amcBookmarks.add, remove: i18.amcBookmarks.remove),
  ]) {
    testWidgets('${labels.add} toggle is accessible and disabled while saving',
        (tester) async {
      var taps = 0;
      Widget button(bool selected, bool saving) => MaterialApp(
          theme: DigitTheme.instance.mobileTheme,
          home: Scaffold(
              body: ReportBookmarkButton(
                  selected: selected,
                  saving: saving,
                  onPressed: () => taps++,
                  addLabelKey: labels.add,
                  removeLabelKey: labels.remove)));
      await tester.pumpWidget(button(false, false));
      expect(find.byTooltip(labels.add), findsOneWidget);
      expect(find.byIcon(Icons.bookmark_border), findsOneWidget);
      final icon = tester.widget<IconButton>(find.byType(IconButton));
      expect(
          icon.color,
          Theme.of(tester.element(find.byType(IconButton)))
              .colorTheme
              .primary
              .primary1);
      expect(tester.getSize(find.byType(IconButton)).width,
          greaterThanOrEqualTo(48));
      await tester.tap(find.byType(IconButton));
      expect(taps, 1);
      await tester.pumpWidget(button(true, false));
      expect(find.byIcon(Icons.bookmark), findsOneWidget);
      expect(find.byTooltip(labels.remove), findsOneWidget);
      await tester.pumpWidget(button(true, true));
      expect(
          tester.widget<IconButton>(find.byType(IconButton)).onPressed, isNull);
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });
  }

  testWidgets(
      'installation bookmarks list and search locally; unbookmark removes immediately',
      (tester) async {
    await installations().save(facility('one', 'Clinic One'));
    await installations().save(facility('two', 'Clinic Two'));
    await tester
        .pumpWidget(app(const SelectHealthFacilityPage(bookmarksOnly: true)));
    await tester.pumpAndSettle();
    expect(find.text('Clinic One'), findsOneWidget);
    expect(find.text('Clinic Two'), findsOneWidget);
    expect(find.byIcon(Icons.bookmark), findsNWidgets(2));
    await tester.enterText(find.byType(TextField), 'One');
    await tester.pumpAndSettle();
    expect(find.text('Clinic One'), findsOneWidget);
    expect(find.text('Clinic Two'), findsNothing);
    tester.testTextInput.hide();
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.byIcon(Icons.bookmark));
    await tester.tap(find.byIcon(Icons.bookmark));
    await tester.pumpAndSettle();
    expect(find.text(i18.installationBookmarks.empty), findsOneWidget);
    expect(await installations().ids(), {'two'});
    expect(tester.takeException(), isNull);
  });

  testWidgets('AMC empty bookmark list does not search remotely',
      (tester) async {
    await tester
        .pumpWidget(app(const AmcSelectFacilityPage(bookmarksOnly: true)));
    await tester.pumpAndSettle();
    expect(find.text(i18.amcBookmarks.empty), findsOneWidget);
    await tester.enterText(find.byType(TextField), 'x');
    await tester.pumpAndSettle();
    expect(find.text(i18.amcBookmarks.empty), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets(
      'AMC bookmarked visit resumes its local draft and removes locally',
      (tester) async {
    await visits().save(ScheduledVisit(
        id: 'one', facility: Facility()..facilityName = 'AMC Clinic'));
    await tester
        .pumpWidget(app(const AmcSelectFacilityPage(bookmarksOnly: true)));
    await tester.pumpAndSettle();
    expect(find.text('AMC Clinic'), findsOneWidget);
    expect(
        await AmcDynamicFormRepository().resolveFormActionLabel(
            isar: DraftIsar(),
            scheduledVisitId: 'one',
            schemaKey: 'AssetForm.AMC_SCHEDULED_MAINTENANCE',
            origin: FormOrigin.overallSummary),
        'Resume');
    expect(
        tester
            .widget<AMCInstallationReportCard>(
                find.byType(AMCInstallationReportCard))
            .label,
        'Resume');
    await tester.ensureVisible(find.byIcon(Icons.bookmark));
    await tester.tap(find.byIcon(Icons.bookmark));
    await tester.pumpAndSettle();
    expect(find.text(i18.amcBookmarks.empty), findsOneWidget);
    expect(await visits().count(), 0);
    expect(tester.takeException(), isNull);
  });

  for (final amc in [false, true]) {
    testWidgets(
        '${amc ? 'AMC' : 'installation'} bookmark load failure has a working retry',
        (tester) async {
      final key =
          'reportBookmarks:["${amc ? 'amc' : 'installation'}","${envConfig.variables.tenantId}","user","${amc ? 'AMC' : 'FIELD_STAFF'}"]';
      FlutterSecureStorage.setMockInitialValues({key: 'invalid json'});
      await tester.pumpWidget(app(amc
          ? const AmcSelectFacilityPage(bookmarksOnly: true)
          : const SelectHealthFacilityPage(bookmarksOnly: true)));
      await tester.pumpAndSettle();
      expect(
          find.text(amc
              ? i18.amcBookmarks.saveFailed
              : i18.installationBookmarks.saveFailed),
          findsOneWidget);
      FlutterSecureStorage.setMockInitialValues({});
      await tester.tap(find.text(i18.common.retry));
      await tester.pumpAndSettle();
      expect(
          find.text(
              amc ? i18.amcBookmarks.empty : i18.installationBookmarks.empty),
          findsOneWidget);
      expect(tester.takeException(), isNull);
    });
  }

  for (final amc in [false, true]) {
    testWidgets(
        '${amc ? 'AMC' : 'installation'} home count refreshes after returning',
        (tester) async {
      if (amc) {
        await visits().save(const ScheduledVisit(id: 'one'));
      } else {
        await installations().save(facility('one', 'Clinic'));
      }
      await tester.pumpWidget(app(SingleChildScrollView(
          child: ReportBookmarksHomeCard<dynamic>(
              amc: amc,
              onOpen: () async {
                if (amc) {
                  await visits().remove('one');
                } else {
                  await installations().remove('one');
                }
              }))));
      await tester.pumpAndSettle();
      expect(tester.widget<ReportCard>(find.byType(ReportCard)).badgeCount, 1);
      tester.widget<ReportCard>(find.byType(ReportCard)).onPress();
      await tester.pumpAndSettle();
      expect(tester.widget<ReportCard>(find.byType(ReportCard)).badgeCount, 0);
      expect(tester.takeException(), isNull);
    });
  }
  for (final amc in [false, true]) {
    testWidgets(
        '${amc ? 'AMC' : 'installation'} home refresh token reloads the count',
        (tester) async {
      Widget home(int token) => ReportBookmarksHomeCard<dynamic>(
          key: const ValueKey('bookmarks'),
          amc: amc,
          refreshToken: token,
          onOpen: () async {});
      await tester.pumpWidget(app(SingleChildScrollView(child: home(0))));
      await tester.pumpAndSettle();
      expect(tester.widget<ReportCard>(find.byType(ReportCard)).badgeCount, 0);
      if (amc) {
        await visits().save(const ScheduledVisit(id: 'one'));
      } else {
        await installations().save(facility('one', 'Clinic'));
      }
      await tester.pumpWidget(app(SingleChildScrollView(child: home(1))));
      await tester.pumpAndSettle();
      expect(tester.widget<ReportCard>(find.byType(ReportCard)).badgeCount, 1);
      expect(tester.takeException(), isNull);
    });
  }
  Future<void> filter(WidgetTester tester, String code,
      {bool apply = true}) async {
    await tester.ensureVisible(find.byIcon(Icons.import_export));
    await tester.tap(find.byIcon(Icons.import_export));
    await tester.pumpAndSettle();
    final radio = tester.widget<RadioList>(find.byType(RadioList));
    expect(radio.radioDigitButtons.map((option) => option.code),
        ['DESC', 'ASC', 'BOOKMARKED']);
    radio.onChanged(RadioButtonModel(code: code, name: code));
    await tester.pump();
    if (apply) {
      tester
          .widgetList<DigitButton>(find.byType(DigitButton))
          .singleWhere((button) => button.label == i18.common.sort)
          .onPressed();
    } else {
      Navigator.of(tester.element(find.byType(RadioList))).pop();
    }
    await tester.pumpAndSettle();
  }

  testWidgets(
      'New Report filter switches locally, keeps search, and restores normal loading',
      (tester) async {
    await installations().save(facility('one', 'Clinic One'));
    final activity = RecordingActivity();
    await tester.pumpWidget(
        app(const SelectHealthFacilityPage(), activityBloc: activity));
    await tester.pumpAndSettle();
    final initialRequests = activity.events.length;
    await filter(tester, 'BOOKMARKED', apply: false);
    expect(find.text('Clinic One'), findsNothing);
    expect(activity.events.length, initialRequests);
    await filter(tester, 'BOOKMARKED');
    expect(find.text('Clinic One'), findsOneWidget);
    await tester.enterText(find.byType(TextField), 'C');
    await tester.pumpAndSettle();
    expect(find.text('Clinic One'), findsOneWidget);
    expect(activity.events.length, initialRequests);
    await filter(tester, 'DESC');
    expect(activity.events.length, greaterThan(initialRequests));
    expect(find.text('Clinic One'), findsNothing);
    await filter(tester, 'BOOKMARKED');
    final beforeClear = activity.events.length;
    await tester.tap(find.byIcon(Icons.import_export));
    await tester.pumpAndSettle();
    tester
        .widgetList<DigitButton>(find.byType(DigitButton))
        .singleWhere((button) => button.label == i18.common.clear)
        .onPressed();
    await tester.pumpAndSettle();
    expect(activity.events.length, greaterThan(beforeClear));
    expect(find.text('Clinic One'), findsNothing);
  });

  testWidgets(
      'Inbox bookmark filter respects tabs and unbookmarking without remote searches',
      (tester) async {
    await installations().save(facility('rejected', 'Rejected Clinic')
        .copyWith(status: 'REJECTED_BY_FIELD_SUPERVISOR'));
    await installations().save(facility('approved', 'Approved Clinic')
        .copyWith(status: 'APPROVED_BY_SUPERVISOR'));
    await installations().save(facility('assigned', 'Assigned Clinic'));
    final activity = RecordingActivity();
    await tester.pumpWidget(app(const InboxPage(), activityBloc: activity));
    await tester.pumpAndSettle();
    final initialRequests = activity.events.length;
    await filter(tester, 'BOOKMARKED');
    expect(find.text('Rejected Clinic'), findsOneWidget);
    expect(find.text('Approved Clinic'), findsNothing);
    expect(find.text('Assigned Clinic'), findsNothing);
    tester.widget<DigitTabBar>(find.byType(DigitTabBar)).onTabSelected(1);
    await tester.pumpAndSettle();
    expect(find.text('Approved Clinic'), findsOneWidget);
    expect(find.text('Rejected Clinic'), findsNothing);
    await tester.enterText(find.byType(TextField), 'Approved');
    tester.testTextInput.hide();
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.byIcon(Icons.bookmark));
    await tester.tap(find.byIcon(Icons.bookmark));
    await tester.pumpAndSettle();
    expect(find.text('Approved Clinic'), findsNothing);
    expect(activity.events.length, initialRequests);
    await filter(tester, 'ASC');
    expect(activity.events.length, greaterThan(initialRequests));
  });
  testWidgets(
      'supervisor Inbox bookmarks follow review, rejected and approved tabs',
      (tester) async {
    final repo = InstallationBookmarkRepository(
        tenantId: envConfig.variables.tenantId,
        userId: 'user',
        userType: 'SUPERVISOR');
    for (final entry in [
      ('review', 'SUBMITTED_BY_FIELD_STAFF'),
      ('rejected', 'REJECTED_BY_QC_SPOC'),
      ('approved', 'APPROVED_BY_QC_SPOC'),
    ]) {
      await repo.save(facility(entry.$1, entry.$1).copyWith(status: entry.$2));
    }
    final activity = RecordingActivity();
    await tester.pumpWidget(
        app(const InboxPage(), activityBloc: activity, supervisor: true));
    await tester.pumpAndSettle();
    final requests = activity.events.length;
    await filter(tester, 'BOOKMARKED');
    expect(find.text('review'), findsOneWidget);
    expect(find.text('rejected'), findsNothing);
    tester.widget<DigitTabBar>(find.byType(DigitTabBar)).onTabSelected(1);
    await tester.pumpAndSettle();
    expect(find.text('rejected'), findsOneWidget);
    expect(find.text('review'), findsNothing);
    tester.widget<DigitTabBar>(find.byType(DigitTabBar)).onTabSelected(2);
    await tester.pumpAndSettle();
    expect(find.text('approved'), findsOneWidget);
    expect(find.text('rejected'), findsNothing);
    expect(activity.events.length, requests);
  });

  testWidgets('installation home keeps its three existing sections',
      (tester) async {
    await tester.pumpWidget(
        app(const InstallationReportPage(), activityBloc: RecordingActivity()));
    await tester.pumpAndSettle();
    expect(find.byType(ReportCard), findsNWidgets(3));
    expect(find.text(i18.installationBookmarks.title), findsNothing);
    expect(find.byType(ReportBookmarksHomeCard<ActivityFacilityWorkflow>),
        findsNothing);
  });
}
