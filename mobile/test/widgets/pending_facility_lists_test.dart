import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';

import '../../lib/blocs/activity_facility/activity_facility.dart';
import '../../lib/blocs/app_init/app_init.dart';
import '../../lib/blocs/asset_submission/asset_submission.dart';
import '../../lib/blocs/auth/authbloc.dart';
import '../../lib/blocs/scheduled_visit/scheduled_visit.dart';
import '../../lib/blocs/user_type/user_type.dart';
import '../../lib/data/nosql/cache_activity_facility_workflow.dart';
import '../../lib/data/nosql/cache_assessment_draft.dart';
import '../../lib/data/nosql/cache_scheduled_visit.dart';
import '../../lib/data/remote_client.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../lib/model/assessment/assessment_form.dart';
import '../../lib/model/assessment/assessment_queue.dart';
import '../../lib/model/assessment/assessment_form_type.dart';
import '../../lib/model/response/responsemodel.dart';
import '../../lib/model/scheduled_visit/scheduled_visit.dart';
import '../../lib/pages/amc_draft.dart';
import '../../lib/pages/assessment_draft.dart';
import '../../lib/pages/draft.dart';
import '../../lib/repositories/assessment_bookmark_repo.dart';
import '../../lib/repositories/assessment_draft_repo.dart';
import '../../lib/repositories/assessment_form_repo.dart';
import '../../lib/repositories/scheduled_visit_repo.dart';
import '../../lib/utils/facility_list_filter.dart';
import '../../lib/repositories/report_bookmark_repo.dart';
import '../../lib/router/app_router.dart';
import '../../lib/utils/envConfig.dart';
import '../../lib/utils/i18_key_constants.dart' as i18;
import '../../lib/widgets/button/footer_button.dart';
import '../../lib/widgets/cards/assessment_draft_card.dart';
import '../../lib/widgets/facility_list_controls.dart';
import '../support/offline_isar.dart';
import 'report_bookmarks_test.dart' as support;

class DraftAuth extends Bloc<AuthEvent, AuthState> implements AuthBloc {
  DraftAuth()
      : super(AuthState.authenticated(
            accesstoken: '',
            refreshtoken: null,
            userRequest: support.user.copyWith(roles: const [
              Roles(name: 'Assessor', code: 'ENUMERATOR', tenantId: 'tenant'),
              Roles(name: 'Field', code: 'FIELD_POC', tenantId: 'tenant'),
            ])));
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected auth access');
}

class RecordingSubmission
    extends Bloc<AssetSubmissionEvent, AssetSubmissionState>
    implements AssetSubmissionBloc {
  final events = <AssetSubmissionEvent>[];
  RecordingSubmission() : super(const AssetSubmissionState.initial());
  @override
  void add(AssetSubmissionEvent event) => events.add(event);
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected submission access');
}

class RecordingAssessmentForms extends AssessmentFormRepository {
  final submitted = <String>[];
  RecordingAssessmentForms() : super(dio: Dio());
  @override
  Future<AssessmentSubmissionResponse> submitAssessment(
      AssessmentSubmissionRequest request) async {
    submitted.add(request.planFacilityId);
    return const AssessmentSubmissionResponse();
  }
}

class CachedActivity extends Bloc<ActivityFacilityEvent, ActivityFacilityState>
    implements ActivityFacilityBloc {
  @override
  final Isar isar;
  final List<ActivityFacilityWorkflow> records;
  CachedActivity(this.isar, this.records)
      : super(ActivityFacilityState.unSubmittedLoaded(records));
  @override
  void add(ActivityFacilityEvent event) {}
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected activity access');
}

class CachedVisitRepository extends ScheduledVisitRepository {
  final List<ScheduledVisit> records;
  CachedVisitRepository(super.isar, this.records);
  @override
  Future<List<ScheduledVisit>> readEligibleCache(List<String> statuses) async =>
      records.where((record) => statuses.contains(record.status)).toList();
}

class CachedVisits extends Bloc<ScheduledVisitEvent, ScheduledVisitState>
    implements ScheduledVisitBloc {
  @override
  final Isar isar;
  @override
  final ScheduledVisitRepository repository;
  final List<ScheduledVisit> records;
  CachedVisits(this.isar, this.records)
      : repository = CachedVisitRepository(isar, records),
        super(const ScheduledVisitState.initial());
  @override
  void add(ScheduledVisitEvent event) {
    event.when(loadInitial: _load, loadMore: _load, refresh: _load);
  }

  void _load(List<String> statuses, String? query, String? sort) {
    final matching = filterFacilityList(
        records.where((record) => statuses.contains(record.status)),
        query: query ?? '',
        filter: sort,
        id: (record) => record.id ?? '',
        name: (record) => record.facility?.facilityName,
        date: (record) => record.scheduledDate ?? DateTime(1970));
    emit(ScheduledVisitState.loaded(
        items: matching,
        hasMore: false,
        totalCount: matching.length,
        fromCache: true));
  }

  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected visit access');
}

class MemoryDraftRepository extends AssessmentDraftRepository {
  final List<CacheAssessmentDraft> records;
  MemoryDraftRepository(super.isar, this.records);
  @override
  Future<List<CacheAssessmentDraft>> listDrafts(String assessorId) async =>
      records.where((record) => record.assessorId == assessorId).toList();
  @override
  Future<void> delete(
      String tenantId, String planFacilityId, AssessmentPhase phase) async {
    records.removeWhere((record) =>
        record.tenantId == tenantId &&
        record.planFacilityId == planFacilityId &&
        record.phase == phase.name);
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Directory directory;
  late Isar isar;
  setUpAll(initializeOfflineIsar);
  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await envConfig.initialize();
    directory = await Directory.systemTemp.createTemp('pending-lists-');
    isar = await openOfflineIsar(directory.path, 'pending-lists');
    final interceptor = InterceptorsWrapper(
        onRequest: (options, handler) => handler.reject(DioException(
            requestOptions: options, type: DioExceptionType.connectionError)));
    DioClient().dio.interceptors.insert(0, interceptor);
    addTearDown(() => DioClient().dio.interceptors.remove(interceptor));
  });
  tearDown(() async {
    await isar.close();
    await directory.delete(recursive: true);
  });

  Widget app(Widget page,
      {RecordingSubmission? submission,
      List<ActivityFacilityWorkflow> installations = const [],
      List<ScheduledVisit> amc = const []}) {
    final auth = DraftAuth();
    final activity = CachedActivity(isar, installations);
    final visits = CachedVisits(isar, amc);
    final type = UserTypeBloc();
    final init = support.FakeInit();
    final sync = submission ?? RecordingSubmission();
    final router = AppRouter();
    addTearDown(() async {
      await auth.close();
      await activity.close();
      await visits.close();
      await type.close();
      await init.close();
      await sync.close();
      router.dispose();
    });
    return MultiBlocProvider(
        providers: [
          BlocProvider<AuthBloc>.value(value: auth),
          BlocProvider<UserTypeBloc>.value(value: type),
          BlocProvider<ActivityFacilityBloc>.value(value: activity),
          BlocProvider<ScheduledVisitBloc>.value(value: visits),
          BlocProvider<AppInitialization>.value(value: init),
          BlocProvider<AssetSubmissionBloc>.value(value: sync),
        ],
        child: MaterialApp(
            theme: DigitTheme.instance.mobileTheme,
            home: StackRouterScope(
                controller: router, stateHash: 0, child: page)));
  }

  Future<void> settle(WidgetTester tester) async {
    for (var i = 0; i < 5; i++) {
      await tester.runAsync(
          () => Future<void>.delayed(const Duration(milliseconds: 20)));
      await tester.pump();
    }
    await tester.pumpAndSettle();
  }

  Future<void> filter(WidgetTester tester, String code) async {
    await tester.tap(find.byIcon(Icons.import_export));
    await settle(tester);
    tester
        .widget<RadioList>(find.byType(RadioList))
        .onChanged(RadioButtonModel(code: code, name: code));
    await settle(tester);
    tester
        .widgetList<DigitButton>(find.byType(DigitButton))
        .singleWhere((button) => button.label == i18.common.sort)
        .onPressed();
    await settle(tester);
  }

  testWidgets(
      'installation pending searches beyond the first page, shares bookmarks and keeps sync scope',
      (tester) async {
    await tester.runAsync(() async {
      final records = List.generate(
          12,
          (i) => ActivityFacilityWorkflow(
              status: 'SUBMITTED_BY_FIELD_STAFF',
              activityFacility: ActivityFacility(
                  id: '$i',
                  facility: Facility()
                    ..facilityName = i == 11 ? 'Kanur 7' : 'Other $i',
                  scheduledAt: DateTime(2026, 1, i + 1))));
      await isar.writeTxn(() async {
        for (final record in records) {
          await isar.cacheActivityFacilityWorkflows.put(
              CacheActivityFacilityWorkflow(
                  activityFacilityId: record.activityFacility.id,
                  status: record.status!,
                  activityFacility: record.activityFacility));
        }
      });
    });
    final sync = RecordingSubmission();
    final installations = await tester.runAsync(() async =>
        (await isar.cacheActivityFacilityWorkflows.where().findAll())
            .map((row) => ActivityFacilityWorkflow(
                activityFacility: row.activityFacility, status: row.status))
            .toList());
    await tester.pumpWidget(app(const DraftPage(),
        submission: sync, installations: installations!));
    await settle(tester);
    expect(find.byType(FacilityListControls), findsOneWidget);
    final pageScroll = find.byType(CustomScrollView).first;
    final scrollContext = tester.element(pageScroll);
    final metrics = FixedScrollMetrics(
        minScrollExtent: 0,
        maxScrollExtent: 100,
        pixels: 100,
        viewportDimension: 600,
        axisDirection: AxisDirection.down,
        devicePixelRatio: 1);
    ScrollUpdateNotification(
            metrics: metrics, context: scrollContext, depth: 1, scrollDelta: 1)
        .dispatch(scrollContext);
    await settle(tester);
    expect(find.text('Other 10'), findsNothing);
    ScrollUpdateNotification(
            metrics: metrics, context: scrollContext, depth: 0, scrollDelta: 1)
        .dispatch(scrollContext);
    await settle(tester);
    expect(find.text('Other 10'), findsOneWidget);
    await tester.enterText(find.byType(TextField), ' kAnUr ');
    await settle(tester);
    expect(find.text('Kanur 7'), findsOneWidget);
    expect(find.text('Other 0'), findsNothing);
    await tester.tap(find.byTooltip(i18.installationBookmarks.add));
    await settle(tester);
    final bookmarks = InstallationBookmarkRepository(
        tenantId: envConfig.variables.tenantId,
        userId: 'user',
        userType: 'FIELD_STAFF');
    expect(await bookmarks.ids(), {'11'});
    await filter(tester, 'BOOKMARKED');
    await tester.tap(find.byTooltip(i18.installationBookmarks.remove));
    await settle(tester);
    expect(find.text(i18.common.noMatchingFacilitiesFound), findsOneWidget);
    tester.widget<FooterButton>(find.byType(FooterButton)).onPress();
    expect(
        sync.events.single.maybeWhen(
            submitAllDrafts: (userType) => userType, orElse: () => ''),
        'FIELD_STAFF');
    expect(tester.takeException(), isNull);
  });

  testWidgets(
      'AMC pending bookmarks intersect current statuses and retain search across tabs offline',
      (tester) async {
    final bookmarks = AmcBookmarkRepository(
        tenantId: envConfig.variables.tenantId,
        userId: 'user',
        userType: 'AMC');
    await tester.runAsync(() async {
      final a = ScheduledVisit(
          id: 'otp',
          facilityId: 'a',
          status: 'PENDING_OTP_APPROVAL',
          scheduledDate: DateTime(2026, 1, 1),
          facility: Facility()..facilityName = 'Kanur OTP');
      final b = ScheduledVisit(
          id: 'approval',
          facilityId: 'b',
          status: 'PENDING_APPROVAL',
          scheduledDate: DateTime(2026, 1, 2),
          facility: Facility()..facilityName = 'Kanur Approval');
      final unrelated = ScheduledVisit(
          id: 'scheduled',
          facilityId: 'c',
          status: 'SCHEDULED',
          scheduledDate: DateTime(2026, 1, 3),
          facility: Facility()..facilityName = 'Kanur Scheduled');
      await isar.writeTxn(() async {
        for (final record in [a, b, unrelated]) {
          await isar.cacheScheduledVisits
              .put(CacheScheduledVisit.fromModel(record));
        }
      });

      await bookmarks.save(b);
      await bookmarks.save(unrelated);
    });
    final amc = await tester.runAsync(() async =>
        (await isar.cacheScheduledVisits.where().findAll())
            .map((row) => row.toModel())
            .toList());
    await tester.pumpWidget(app(const AmcDraftPage(), amc: amc!));
    await settle(tester);
    await tester.enterText(find.byType(TextField), 'k');
    await settle(tester);
    await tester.tap(find.byTooltip(i18.amcBookmarks.add));
    await settle(tester);
    await filter(tester, 'BOOKMARKED');
    expect(find.text('Kanur OTP'), findsOneWidget);
    expect(find.text('Kanur Scheduled'), findsNothing);
    tester.widget<DigitTabBar>(find.byType(DigitTabBar)).onTabSelected(1);
    await settle(tester);
    expect(find.text('Kanur Approval'), findsOneWidget);
    expect(find.text('Kanur OTP'), findsNothing);
    expect(
        tester.widget<TextField>(find.byType(TextField)).controller!.text, 'k');
    await tester.tap(find.byTooltip(i18.amcBookmarks.remove));
    await settle(tester);
    expect(find.text(i18.common.noMatchingFacilitiesFound), findsOneWidget);
    expect(await bookmarks.ids(), {'otp', 'scheduled'});
    expect(tester.takeException(), isNull);
  });

  Future<void> seedDraft(String id, AssessmentPhase phase, String name) async {
    final tenant = envConfig.variables.tenantId;
    final request = AssessmentSubmissionRequest(
        planFacilityId: id,
        tenantId: tenant,
        facilityCategory: 'HF',
        assessmentPhase: phase,
        submissionData: {},
        clientSubmissionTime: 1);
    await isar.writeTxn(() async {
      await isar.cacheAssessmentDrafts.put(CacheAssessmentDraft(
          draftKey: AssessmentDraftRepository.key(tenant, id, phase),
          tenantId: tenant,
          assessorId: 'user',
          phase: phase.name,
          status: AssessmentDraftStatus.pending,
          planFacilityId: id,
          facilityName: name,
          facilityType: 'HF',
          requestJson: jsonEncode(request.toJson())));
    });
  }

  testWidgets(
      'assessment draft bookmarks preserve phase boundaries and search across tabs',
      (tester) async {
    final fieldBookmarks = AssessmentBookmarkRepository(
        tenantId: envConfig.variables.tenantId,
        assessorId: 'user',
        phase: AssessmentPhase.FIELD);
    await tester.runAsync(() async {
      await seedDraft('phone', AssessmentPhase.PHONE, 'Kanur Phone');
      await seedDraft('field', AssessmentPhase.FIELD, 'Kanur Field');

      await fieldBookmarks.save(const AssessmentQueueFacility(
          planFacilityId: 'field', facilityName: 'Kanur Field'));
    });
    final drafts = MemoryDraftRepository(
        isar,
        (await tester
            .runAsync(() => isar.cacheAssessmentDrafts.where().findAll()))!);
    await tester.pumpWidget(app(AssessmentDraftPage(repository: drafts)));
    await settle(tester);
    expect(find.byType(AssessmentDraftCard), findsOneWidget);
    await tester.enterText(find.byType(TextField), 'kanur');
    await settle(tester);
    await tester.tap(find.byTooltip(i18.assessmentBookmarks.add));
    await settle(tester);
    final phoneBookmarks = AssessmentBookmarkRepository(
        tenantId: envConfig.variables.tenantId,
        assessorId: 'user',
        phase: AssessmentPhase.PHONE);
    expect((await phoneBookmarks.list()).single.planFacilityId, 'phone');
    await filter(tester, 'BOOKMARKED');
    tester.widget<DigitTabBar>(find.byType(DigitTabBar)).onTabSelected(1);
    await settle(tester);
    expect(find.text('Kanur Field'), findsOneWidget);
    expect(find.text('Kanur Phone'), findsNothing);
    await tester.tap(find.byTooltip(i18.assessmentBookmarks.remove));
    await settle(tester);
    expect(find.text(i18.common.noMatchingFacilitiesFound), findsOneWidget);
    expect(await phoneBookmarks.ids(), {'phone'});
    expect(await fieldBookmarks.ids(), isEmpty);
    expect(tester.takeException(), isNull);
  });

  testWidgets(
      'assessment Sync All still processes drafts hidden by search and phase',
      (tester) async {
    await tester.runAsync(() async {
      await seedDraft('phone', AssessmentPhase.PHONE, 'Phone');
      await seedDraft('field', AssessmentPhase.FIELD, 'Field');
    });
    final forms = RecordingAssessmentForms();
    final drafts = MemoryDraftRepository(
        isar,
        (await tester
            .runAsync(() => isar.cacheAssessmentDrafts.where().findAll()))!);
    await tester
        .pumpWidget(app(AssessmentDraftPage(repository: drafts, forms: forms)));
    await settle(tester);
    await tester.enterText(find.byType(TextField), 'missing');
    await settle(tester);
    expect(find.byType(AssessmentDraftCard), findsNothing);
    final footer = tester.widget<FooterButton>(find.byType(FooterButton));
    expect(footer.isDisabled, isFalse);
    footer.onPress();
    await settle(tester);
    expect(forms.submitted, unorderedEquals(['phone', 'field']));
    expect(drafts.records, isEmpty);
    expect(tester.takeException(), isNull);
  });
}
