import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_activity_facility_workflow.dart';
import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_completion_report.dart';
import '../data/nosql/cache_prefilled_activity_facility.dart';
import '../data/nosql/cache_unsubmitted_activity_facility.dart';
import '../data/remote_client.dart';
import '../model/activity_facility/activity_facility.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/document/document.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'bom_repo.dart';

class ActivityFacilityRemoteRepository {
  ActivityFacilityRemoteRepository();

  final dio = DioClient().dio;

  FutureOr<List<ActivityFacilityWorkflow>> searchByWorkflow(
      {required ActivityFacilitySearchModel body,
      required List<String> workflowStatuses,
      int limit = 100,
      offset = 0,
      sortDirection = 'ASC'}) async {
    try {
      Response response;
      String searchPath = "activity/v1/activities/_search";

      if (envConfig.variables.envType == EnvType.dev) {
        // return _loadLocalActivityFacility();
      }

      response = await dio.post(
        searchPath,
        queryParameters: {
          'tenantId': envConfig.variables.tenantId,
          'limit': limit,
          'offset': offset,
          'includeDescendants': false,
          'includeAncestors': false,
          'sort_direction': sortDirection
        },
        data: {
          'ActivityFacility': {
            'statuses': workflowStatuses,
            'tenantId': envConfig.variables.tenantId,
            ...body.toMap(),
          },
        },
      );

      print("response.data ${response.data}");

      final responseMap = response.data['facility'];

      List<ActivityFacilityWorkflow> activityFacilityList = [];
      for (final activityFacility in responseMap) {
        activityFacilityList
            .add(ActivityFacilityWorkflow.fromJson(activityFacility));
      }
      return activityFacilityList;
    } catch (err) {
      print("err ${err.toString()}");
      rethrow;
    }
  }

  FutureOr<int> searchByWorkflowCount({
    required ActivityFacilitySearchModel body,
    required List<String> workflowStatuses,
    int limit = 0,
    offset = 0,
  }) async {
    try {
      Response response;
      String searchPath = "activity/v1/activities/_search";

      response = await dio.post(
        searchPath,
        queryParameters: {
          'tenantId': envConfig.variables.tenantId,
          'limit': limit,
          'offset': offset,
          'includeDescendants': false,
          'includeAncestors': false
        },
        data: {
          'ActivityFacility': {
            'statuses': workflowStatuses,
            'tenantId': envConfig.variables.tenantId,
            ...body.toMap(),
          },
        },
      );

      final count = response.data['totalCount'];
      return count ?? 0;
    } catch (err) {
      rethrow;
    }
  }

  Future<void> updateActivityFacilityWorkflow({
    required String activityFacilityId,
    required String action,
    List<Document>? documents,
  }) async {
    const url = 'activity/v1/activities/workflow/update';

    final body = <String, dynamic>{
      'activityFacilityId': activityFacilityId,
      'workflow': {
        'action': action,
        if (documents != null) ...{
          'documents': documents.map((d) => d.toJsonForWorkflow()).toList()
        }
      }
    };

    try {
      final resp = await dio.post(url,
          data: body, options: Options(contentType: Headers.jsonContentType));
      if (resp.statusCode != 200 &&
          resp.statusCode != 201 &&
          resp.statusCode != 204) {
        throw Exception(
            'Workflow update failed (${resp.statusCode}): ${resp.data}');
      }
    } on DioError catch (dioErr) {
      // final msg = dioErr.response?.data?.toString() ?? dioErr.message;
      // throw Exception('Workflow update network error: $msg');
      throw DioErrorParser.parse(dioErr);
    }
  }

  Future<void> sendBackActivityFacilityWorkflow({
    required ActivityFacilityWorkflow activityFacilityWorkflow,
    required String userType,
    required Isar isar,
  }) async {
    try {
      final activityFacilityId = activityFacilityWorkflow.activityFacility.id;
      final workflowDocuments = activityFacilityWorkflow.workflow?.documents;

      final repo = ActivityFacilityRemoteRepository();
      await repo.updateActivityFacilityWorkflow(
        activityFacilityId: activityFacilityId,
        action: userType == USER_TYPES.SUPERVISOR.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name,
        documents: workflowDocuments,
      );

      await UnsubmittedActivityFacilityRepository(isar)
          .delete(activityFacilityId, userType);
      await PrefilledActivityFacilityRepository(isar)
          .delete(activityFacilityId: activityFacilityId, userType: userType);
      await CompletionReportRepository(isar)
          .delete(projectId: activityFacilityId);
      await BomRepository()
          .delete(isar: isar, activityFacilityId: activityFacilityId);
    } on DioError catch (dioErr) {
      throw DioErrorParser.parse(dioErr);
    }
  }
}

class ActivityFacilityRepository {
  final Isar _isar;
  final ActivityFacilityRemoteRepository _remote;

  ActivityFacilityRepository(this._isar)
      : _remote = ActivityFacilityRemoteRepository();

  Set<String> _resolveUserTypes(List<String> statuses) {
    final up = statuses.map((s) => s.toUpperCase()).toSet();
    final types = <String>{};
    if (up.contains('ASSIGNED_TO_FIELD_STAFF')) types.add('STAFF');
    if (up.contains('ASSIGNED_TO_FIELD_SUPERVISOR')) types.add('SUPERVISOR');
    return types;
  }

  Future<Set<String>> _excludedIdsFor(Set<String> userTypes) async {
    if (userTypes.isEmpty) return <String>{};

    final col = _isar.cacheUnsubmittedActivityFacilitys;
    final excluded = <String>{};
    for (final t in userTypes) {
      final matches = await col.where().filter().userTypeEqualTo(t).findAll();

      excluded.addAll(matches.map((e) => e.activityFacilityId));
    }
    return excluded;
  }

  List<ActivityFacilityWorkflow> _applyExclusion(
    List<ActivityFacilityWorkflow> list,
    Set<String> excludedIds,
  ) {
    if (excludedIds.isEmpty) return list;
    return list
        .where((wf) => !excludedIds.contains(wf.activityFacility.id))
        .toList();
  }

  Future<List<ActivityFacilityWorkflow>> fetchByWorkflow(
      {required ActivityFacilitySearchModel body,
      required List<String> workflowStatuses,
      sortDirection = 'ASC'}) async {
    final userTypes = _resolveUserTypes(workflowStatuses);
    try {
      print("workflowStatuses $workflowStatuses");
      final remoteList = await _remote.searchByWorkflow(
        body: body,
        workflowStatuses: workflowStatuses,
        sortDirection: sortDirection,
      );

      if (remoteList != null) {
        await _replaceCache(workflowStatuses, remoteList);
        final excludedIds = await _excludedIdsFor(userTypes);
        final filteredRemoteList = _applyExclusion(remoteList, excludedIds);
        return filteredRemoteList;
      }
    } catch (e) {
      debugPrint("error in fetching remote project ${e.toString()}");
    }
    final cachedList = await readCache(workflowStatuses);
    final excludedIds = await _excludedIdsFor(userTypes);
    return _applyExclusion(cachedList, excludedIds);
  }

  Future<void> _replaceCache(
    List<String> statuses,
    List<ActivityFacilityWorkflow> newList,
  ) async {
    final col = _isar.cacheActivityFacilityWorkflows;
    await _isar.writeTxn(() async {
      for (final status in statuses) {
        final toDelete = await col.where().statusEqualTo(status).findAll();
        for (final entry in toDelete) {
          await col.delete(entry.id);
        }
      }
      for (final wf in newList) {
        await col.put(CacheActivityFacilityWorkflow(
          activityFacilityId: wf.activityFacility.id,
          status: wf.status ?? '',
          activityFacility: wf.activityFacility,
          transactions: wf.transactions,
          workflow: wf.workflow,
        ));
      }
    });
  }

  Future<List<ActivityFacilityWorkflow>> readCache(
    List<String> statuses,
  ) async {
    final col = _isar.cacheActivityFacilityWorkflows;
    final List<CacheActivityFacilityWorkflow> all = [];
    for (final status in statuses) {
      final matches = await col.where().statusEqualTo(status).findAll();
      all.addAll(matches);
    }
    return all
        .map((c) => ActivityFacilityWorkflow(
              activityFacility: c.activityFacility,
              status: c.status,
              transactions: c.transactions,
              workflow: c.workflow,
            ))
        .toList();
  }

  Future<String?> getSolutionDesignTypeFromCache(
      Isar isar, String activityFacilityId) async {
    final row = await isar.cacheActivityFacilityWorkflows
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst();
    if (row == null) return null;

    try {
      final sys = row.activityFacility.facility?.facilityDetails
          ?.solar_solution_design_type
          ?.toString();
      if (sys != null && sys.isNotEmpty) return sys;
    } catch (_) {}
    return null;
  }
}

class UnsubmittedActivityFacilityRepository {
  final Isar _isar;
  final ActivityFacilityRemoteRepository _remote;

  UnsubmittedActivityFacilityRepository(this._isar)
      : _remote = ActivityFacilityRemoteRepository();

  Future<List<ActivityFacilityWorkflow>> fetchByWorkflowIncludeCache({
    required String userType,
    required List<String> workflowStatuses,
    required ActivityFacilitySearchModel body,
  }) async {
    List<ActivityFacilityWorkflow> remoteList;
    try {
      remoteList = await _remote.searchByWorkflow(
        body: body,
        workflowStatuses: workflowStatuses,
      );
    } catch (_) {
      remoteList = <ActivityFacilityWorkflow>[];
    }
    final col = _isar.cacheUnsubmittedActivityFacilitys;
    final localEntries =
        await col.where().filter().userTypeEqualTo(userType).findAll();
    final localWorkflows = localEntries
        .map((e) => ActivityFacilityWorkflow(
            activityFacility: e.activityFacility, status: e.status))
        .toList();
    final cachedIds = localEntries.map((e) => e.activityFacility.id).toSet();
    final remoteOnly =
        remoteList.where((r) => !cachedIds.contains(r.activityFacility.id));
    return [
      ...localWorkflows,
      ...remoteOnly,
    ];
  }

  Future<CacheUnsubmittedActivityFacility> addOrGet(
    ActivityFacilityWorkflow wf,
    String userType,
  ) async {
    final col = _isar.cacheUnsubmittedActivityFacilitys;
    final activityFacilityId = wf.activityFacility.id;
    final existing = await col
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .filter()
        .userTypeEqualTo(userType)
        .findFirst();
    if (existing != null) return existing;

    final entry = CacheUnsubmittedActivityFacility(
      activityFacilityId: activityFacilityId,
      status: wf.status ?? '',
      activityFacility: wf.activityFacility,
      userType: userType,
    );
    await _isar.writeTxn(() => col.put(entry));
    return entry;
  }

  Future<void> delete(String activityFacilityId, String userType) async {
    final col = _isar.cacheUnsubmittedActivityFacilitys;
    await _isar.writeTxn(() async {
      final toDelete = await col
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .userTypeEqualTo(userType)
          .findAll();
      for (final e in toDelete) {
        await col.delete(e.id);
      }
    });
  }

  Future<void> deleteAddNewAsset(String activityFacilityId) async {
    final col = _isar.cacheAddNewAssets;
    await _isar.writeTxn(() async {
      final toDelete = await col
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findAll();
      for (final e in toDelete) {
        await col.delete(e.id);
      }
    });
  }
}

class PrefilledActivityFacilityRepository {
  final Isar _isar;
  PrefilledActivityFacilityRepository(this._isar);

  Future<CachePrefilledActivityFacility> addOrTouch({
    required String activityFacilityId,
    required String userType,
  }) async {
    final col = _isar.cachePrefilledActivityFacilitys;
    final existing = await col
        .where()
        .activityFacilityIdUserTypeEqualTo(activityFacilityId, userType)
        .findFirst();

    final now = DateTime.now();
    return _isar.writeTxn(() async {
      if (existing != null) {
        existing.updatedAt = now;
        await col.put(existing);
        return existing;
      } else {
        final row = CachePrefilledActivityFacility(
            activityFacilityId: activityFacilityId, userType: userType)
          ..createdAt = now
          ..updatedAt = now;
        await col.put(row);
        return row;
      }
    });
  }

  Future<bool> exists({
    required String activityFacilityId,
    required String userType,
  }) async {
    final col = _isar.cachePrefilledActivityFacilitys;
    final row = await col
        .where()
        .activityFacilityIdUserTypeEqualTo(activityFacilityId, userType)
        .findFirst();
    return row != null;
  }

  Future<void> delete({
    required String activityFacilityId,
    required String userType,
  }) async {
    final col = _isar.cachePrefilledActivityFacilitys;
    final row = await col
        .where()
        .activityFacilityIdUserTypeEqualTo(activityFacilityId, userType)
        .findFirst();
    if (row != null) {
      await _isar.writeTxn(() async {
        await col.delete(row.id);
      });
    }
  }
}

class CompletionReportRepository {
  final Isar _isar;
  CompletionReportRepository(this._isar);

  Future<void> delete({required String projectId}) async {
    await _isar.writeTxn(() async {
      final col = _isar.cacheCompletionReports;
      final reports =
          await col.where().activityFacilityIdEqualTo(projectId).findAll();
      for (final report in reports) {
        await col.delete(report.id);
      }
    });
  }
}
