import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import '../../lib/data/secure_storage/secureStore.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../lib/model/response/responsemodel.dart';
import '../../lib/model/scheduled_visit/scheduled_visit.dart';
import '../../lib/repositories/activity_facility_repo.dart';
import '../../lib/repositories/report_bookmark_repo.dart';
import '../../lib/repositories/scheduled_visit_repo.dart';
import '../../lib/model/workflow/workflow.dart';
import '../../lib/data/nosql/workflow_audit_details.dart';

class ReportStore extends SecureStore {
  final values = <String, String>{};
  bool failWrites = false;
  @override
  Future<Map<String, dynamic>> getReportBookmarks(
          String kind, String tenant, String user, String type) async =>
      Map<String, dynamic>.from(
          jsonDecode(values[jsonEncode([kind, tenant, user, type])] ?? '{}')
              as Map);
  @override
  Future<void> setReportBookmarks(String kind, String tenant, String user,
      String type, Map<String, dynamic> entries) async {
    if (failWrites) throw StateError('Storage unavailable');
    values[jsonEncode([kind, tenant, user, type])] = jsonEncode(entries);
  }

  @override
  Future<ResponseModel?> getAccessInfo() async => const ResponseModel(
      access_token: 'token',
      token_type: null,
      refresh_token: null,
      scope: null,
      userRequest: UserRequest(
          id: 1,
          uuid: 'user',
          userName: 'user',
          name: null,
          mobileNumber: null,
          emailId: null,
          type: null,
          active: true,
          roles: [],
          tenantId: 'tenant'));
}

ActivityFacilityWorkflow installation(String id, [String name = 'Clinic']) =>
    ActivityFacilityWorkflow(
        activityFacility: ActivityFacility(
            id: id,
            tenantId: 'tenant',
            description: 'Report context',
            rowVersion: 7,
            scheduledAt: DateTime(2026, 1, 1),
            facility: Facility()
              ..facilityName = name
              ..facility_poc_phone = '123'));
ScheduledVisit visit(String id, [String name = 'Clinic']) => ScheduledVisit(
    id: id,
    tenantId: 'tenant',
    visitNumber: 2,
    status: 'SCHEDULED',
    scheduledDate: DateTime(2026, 1, 1),
    facility: Facility()
      ..facilityName = name
      ..facility_poc_phone = '123',
    visitReport: const ScheduledVisitReport(responses: {'answer': 'saved'}));

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test(
      'AMC snapshot refresh preserves full workflow and report details offline',
      () async {
    final store = ReportStore();
    AmcBookmarkRepository repo() => AmcBookmarkRepository(
        storage: store, tenantId: 'tenant', userId: 'user', userType: 'AMC');
    await repo().save(visit('one'));
    final timestamp =
        jsonDecode(store.values.values.single)['one']['bookmarkedAt'];
    final workflow = Workflow.fromJson({
      'id': 'process-one',
      'action': 'REJECT',
      'state': {'state': 'REJECTED'},
      'comment': [
        {'reason': 'IMAGE_UNCLEAR', 'comment': 'Retake selfie'}
      ],
      'auditDetails': {
        'createdBy': 'reviewer',
        'createdTime': 1234,
        'lastModifiedTime': 2345,
        'custom': 'metadata'
      },
      'documents': [
        {'fileStoreId': 'photo-one', 'documentType': 'selfie'}
      ],
      'assignes': ['reviewer'],
    });
    final updated = visit('one').copyWith(
        status: 'REJECTED',
        workflow: workflow,
        processInstances: [workflow],
        visitReport: const ScheduledVisitReport(
            responses: {'faults_observed': 'YES'},
            additionalDetails: {'remark': 'Saved report'}));
    await repo().refreshSnapshots([updated, visit('not-bookmarked')]);
    final restored = (await repo().list()).single;
    final process = restored.processInstances.single.raw!;
    expect(restored.status, 'REJECTED');
    expect(process['comment'], [
      {'reason': 'IMAGE_UNCLEAR', 'comment': 'Retake selfie'}
    ]);
    expect(jsonDecode(restored.processInstances.single.comment!),
        process['comment']);
    expect(process['id'], 'process-one');
    expect(process['state'], {'state': 'REJECTED'});
    expect(process['auditDetails']['createdBy'], 'reviewer');
    expect(process['auditDetails']['custom'], 'metadata');
    expect(restored.workflow!.documents!.single.fileStore, 'photo-one');
    expect(restored.visitReport!.responses, {'faults_observed': 'YES'});
    expect(restored.visitReport!.additionalDetails, {'remark': 'Saved report'});
    expect(await repo().ids(), {'one'});
    expect(jsonDecode(store.values.values.single)['one']['bookmarkedAt'],
        timestamp);
  });
  test(
      'installation workflow round trip preserves string comments and metadata',
      () async {
    final store = ReportStore();
    final repo = InstallationBookmarkRepository(
        storage: store,
        tenantId: 'tenant',
        userId: 'user',
        userType: 'FIELD_STAFF');
    final workflow = Workflow.fromJson({
      'comment': '[{"reason":"IMAGE_UNCLEAR"}]',
      'action': 'REJECT',
      'businessId': 'one'
    });
    await repo.save(installation('one').copyWith(workflow: workflow));
    final restored = (await repo.list()).single.workflow!;
    expect(restored.raw!['comment'], workflow.raw!['comment']);
    expect(restored.raw!['businessId'], 'one');
    expect(restored.comment, workflow.comment);
    final created = Workflow(
        comment: 'Typed comment',
        auditDetails: WorkflowAuditDetails(createdBy: 'user'));
    expect(Workflow.fromJson(created.toJson()).comment, 'Typed comment');
    expect(Workflow.fromJson(created.toJson()).auditDetails!.createdBy, 'user');
  });
  for (final amc in [false, true]) {
    ReportBookmarkRepository<dynamic> repo(SecureStore store,
        {String tenant = 'tenant',
        String user = 'user',
        String type = 'FIELD_STAFF'}) {
      if (amc) {
        return AmcBookmarkRepository(
            storage: store, tenantId: tenant, userId: user, userType: type);
      }
      return InstallationBookmarkRepository(
          storage: store, tenantId: tenant, userId: user, userType: type);
    }

    dynamic item(String id, [String name = 'Clinic']) =>
        amc ? visit(id, name) : installation(id, name);
    final label = amc ? 'AMC' : 'installation';
    test('$label persists full records across recreation and cache replacement',
        () async {
      FlutterSecureStorage.setMockInitialValues({});
      await repo(SecureStore()).save(item('one'));
      await SecureStore().setAssessmentResponse(['queue'], {'items': []});
      final saved = (await repo(SecureStore()).list()).single;
      expect(jsonDecode(jsonEncode(repo(SecureStore()).encode(saved))),
          jsonDecode(jsonEncode(repo(SecureStore()).encode(item('one')))));
      expect(await repo(SecureStore()).ids(), {'one'});
    });
    test(
        '$label deduplicates, preserves date, filters, sorts and isolates scopes',
        () async {
      final store = ReportStore();
      await repo(store).save(item('one'));
      final before =
          jsonDecode(store.values.values.single)['one']['bookmarkedAt'];
      await repo(store).save(item('two', 'Other'));
      await repo(store).save(item('one', 'Updated clinic'));
      expect(await repo(store).count(), 2);
      expect(jsonDecode(store.values.values.single)['one']['bookmarkedAt'],
          before);
      expect(
          (await repo(store).list()).map(repo(store).identity), ['two', 'one']);
      expect(
          (await repo(store).list(sortOrder: 'ASC')).map(repo(store).identity),
          ['one', 'two']);
      expect(
          (await repo(store).list(query: ' updated '))
              .map(repo(store).identity),
          ['one']);
      expect(await repo(store).list(query: 'missing'), isEmpty);
      expect(await repo(store, tenant: 'other').count(), 0);
      expect(await repo(store, user: 'other').count(), 0);
      expect(await repo(store, type: 'SUPERVISOR').count(), 0);
      expect(await repo(store, user: '').count(), 0);
      await repo(store).remove('one');
      expect(await repo(store).ids(), {'two'});
    });
    test('$label concurrent changes and failed writes preserve records',
        () async {
      final store = ReportStore();
      await Future.wait(
          [for (var i = 0; i < 10; i++) repo(store).save(item('$i'))]);
      expect(await repo(store).count(), 10);
      await Future.wait(
          [repo(store).remove('0'), repo(store).save(item('new'))]);
      expect(await repo(store).count(), 10);
      store.failWrites = true;
      await expectLater(repo(store).remove('1'), throwsStateError);
      await expectLater(repo(store).save(item('failed')), throwsStateError);
      expect(await repo(store).ids(), contains('1'));
      expect(await repo(store).ids(), isNot(contains('failed')));
    });
    test('$label retains IDs and save order across invalidation and refetch',
        () async {
      final store = ReportStore();
      final bookmarks = repo(store);
      await bookmarks.save(item('one'));
      await bookmarks.save(item('two'));
      await bookmarks.invalidate('one');
      expect(await repo(store).ids(), {'one', 'two'});
      expect((await repo(store).list()).map(bookmarks.identity), ['two']);
      await repo(store).refreshSnapshots(label == 'installation'
          ? <ActivityFacilityWorkflow>[
              installation('one'),
              installation('unbookmarked')
            ]
          : <ScheduledVisit>[visit('one'), visit('unbookmarked')]);
      expect(
          (await repo(store).list()).map(bookmarks.identity), ['two', 'one']);
      expect(await repo(store).count(), 2);
      await repo(store).remove('one');
      await repo(store).refreshSnapshots(label == 'installation'
          ? <ActivityFacilityWorkflow>[installation('one')]
          : <ScheduledVisit>[visit('one')]);
      expect(await repo(store).ids(), {'two'});
    });
    test('$label rejects missing identities', () async {
      final store = ReportStore();
      await expectLater(repo(store).save(item('')), throwsFormatException);
      await expectLater(
          repo(store, user: '').save(item('one')), throwsFormatException);
    });
  }
  for (final action in [
    'SUBMIT_REPORT_A',
    'SUBMIT_REPORT_B',
    'SUBMIT_VISIT_REPORT'
  ]) {
    test('$action invalidates only matching snapshot after successful workflow',
        () async {
      final store = ReportStore();
      final staff = InstallationBookmarkRepository(
          storage: store,
          tenantId: 'tenant',
          userId: 'user',
          userType: 'FIELD_STAFF');
      final supervisor = InstallationBookmarkRepository(
          storage: store,
          tenantId: 'tenant',
          userId: 'user',
          userType: 'SUPERVISOR');
      final amc = AmcBookmarkRepository(
          storage: store, tenantId: 'tenant', userId: 'user', userType: 'AMC');
      await staff.save(installation('one'));
      await supervisor.save(installation('one'));
      await amc.save(visit('one'));
      final dio = Dio();
      var fail = true;
      dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
        if (fail) {
          handler.reject(DioException(
              requestOptions: options, type: DioExceptionType.connectionError));
        } else {
          handler.resolve(
              Response(requestOptions: options, statusCode: 200, data: {}));
        }
      }));
      final installations = ActivityFacilityRemoteRepository(
          dio: dio, bookmarkStorage: store, bookmarkTenantId: 'tenant');
      final visits = ScheduledVisitRemoteRepository(
          dio: dio, bookmarkStorage: store, bookmarkTenantId: 'tenant');
      Future<void> submit() => action == 'SUBMIT_VISIT_REPORT'
          ? visits.updateVisitWorkflow(
              visitId: 'one', schemaCode: 'schema', version: 1)
          : installations.updateActivityFacilityWorkflow(
              activityFacilityId: 'one', action: action);
      await expectLater(submit(), throwsA(anything));
      expect(await staff.count(), 1);
      expect(await supervisor.count(), 1);
      expect(await amc.count(), 1);
      fail = false;
      await visits.updateVisitWorkflow(
          visitId: 'one',
          schemaCode: 'schema',
          version: 1,
          status: 'SUBMIT_OTP');
      await installations.updateActivityFacilityWorkflow(
          activityFacilityId: 'one', action: 'CREATE_AND_SAVE_DRAFT');
      expect(await amc.count(), 1);
      expect(await staff.count(), 1);
      await submit();
      expect(await staff.count(), 1);
      expect((await staff.list()).length, action == 'SUBMIT_REPORT_A' ? 0 : 1);
      expect(await supervisor.count(), 1);
      expect((await supervisor.list()).length,
          action == 'SUBMIT_REPORT_B' ? 0 : 1);
      expect(await amc.count(), 1);
      expect(
          (await amc.list()).length, action == 'SUBMIT_VISIT_REPORT' ? 0 : 1);
      await staff.save(installation('one'));
      await supervisor.save(installation('one'));
      await amc.save(visit('one'));
      store.failWrites = true;
      await submit();
      expect(await staff.count(), 1);
      expect(await supervisor.count(), 1);
      expect(await amc.count(), 1);
    });
  }
}
