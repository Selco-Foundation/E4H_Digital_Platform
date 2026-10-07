import 'dart:async';

import 'package:dio/dio.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_activity_facility_workflow.dart';
import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_completion_report.dart';
import '../data/nosql/cache_prefilled_activity_facility.dart';
import '../data/nosql/cache_unsubmitted_activity_facility.dart';
import '../data/remote_client.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/activity_facility/activity_facility.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/document/document.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';
import 'dynamic_form_repo.dart';

import 'report_bookmark_repo.dart';
import 'cache_fallback.dart';

class PaginatedActivityFacilities {
  final List<ActivityFacilityWorkflow> items;
  final int totalCount;
  final bool fromCache;

  PaginatedActivityFacilities({
    required this.items,
    required this.totalCount,
    this.fromCache = false,
  });
}

class ActivityFacilityRemoteRepository {
  final Dio dio;
  final SecureStore bookmarkStorage;
  final String? bookmarkTenantId;
  ActivityFacilityRemoteRepository(
      {Dio? dio, SecureStore? bookmarkStorage, this.bookmarkTenantId})
      : dio = dio ?? DioClient().dio,
        bookmarkStorage = bookmarkStorage ?? SecureStore();

  FutureOr<List<ActivityFacilityWorkflow>> searchByWorkflow(
      {required ActivityFacilitySearchModel body,
      required List<String> workflowStatuses,
      int limit = 100,
      offset = 0,
      sortDirection = DEFAULT_SORT_DIRECTION}) async {
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
          'includeAncestors': false,
        },
        data: {
          'ActivityFacility': {
            'sort_direction': sortDirection,
            'statuses': workflowStatuses,
            'tenantId': envConfig.variables.tenantId,
            ...body.toMap(),
          },
        },
      );

      final responseMap = response.data['facility'];

      List<ActivityFacilityWorkflow> activityFacilityList = [];
      for (final activityFacility in responseMap) {
        activityFacilityList
            .add(ActivityFacilityWorkflow.fromJson(activityFacility));
      }
      return activityFacilityList;
    } catch (err) {
      AppLogger.instance.error(
        title: "Activity Search",
        message: err.toString(),
      );
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
      final resp = await dio.post(
        url,
        data: body,
        options: Options(
          contentType: Headers.jsonContentType,
          responseType: ResponseType.bytes,
        ),
      );
      if (resp.statusCode != 200 &&
          resp.statusCode != 201 &&
          resp.statusCode != 204) {
        throw Exception('Workflow update failed (${resp.statusCode})');
      }
      await invalidateSubmittedReportBookmark(
          id: activityFacilityId,
          action: action,
          storage: bookmarkStorage,
          tenantId: bookmarkTenantId);
    } on DioError catch (dioErr) {
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
  static const int defaultPageSize = 10;

  final Isar _isar;
  final ActivityFacilityRemoteRepository _remote;

  ActivityFacilityRepository(this._isar,
      {ActivityFacilityRemoteRepository? remote})
      : _remote = remote ?? ActivityFacilityRemoteRepository();

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
      final matches = await col.where().userTypeEqualTo(t).findAll();

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
      sortDirection = DEFAULT_SORT_DIRECTION}) async {
    final userTypes = _resolveUserTypes(workflowStatuses);
    try {
      final remoteList = await _remote.searchByWorkflow(
        body: body,
        workflowStatuses: workflowStatuses,
        sortDirection: sortDirection,
      );

      await _upsertCache(remoteList);
      final excludedIds = await _excludedIdsFor(userTypes);
      final filteredRemoteList = _applyExclusion(remoteList, excludedIds);
      return filteredRemoteList;
    } catch (e) {
      if (isAuthenticationFailure(e)) rethrow;
      AppLogger.instance.info("error in fetching remote project $e");
    }
    final cachedList = (await _readCacheSorted(
            statuses: workflowStatuses, sortDirection: sortDirection))
        .where((wf) => matchesFacilityName(
            wf.activityFacility.facility?.facilityName, body.facilityName))
        .toList();
    final excludedIds = await _excludedIdsFor(userTypes);
    return _applyExclusion(cachedList, excludedIds);
  }

  Future<PaginatedActivityFacilities> fetchByWorkflowPaginated({
    required ActivityFacilitySearchModel body,
    required List<String> workflowStatuses,
    int limit = defaultPageSize,
    int offset = 0,
    String sortDirection = DEFAULT_SORT_DIRECTION,
  }) async {
    final userTypes = _resolveUserTypes(workflowStatuses);
    try {
      final remoteList = await _remote.searchByWorkflow(
        body: body,
        workflowStatuses: workflowStatuses,
        limit: limit,
        offset: offset,
        sortDirection: sortDirection,
      );

      await _upsertCache(remoteList);

      final excludedIds = await _excludedIdsFor(userTypes);
      final filteredRemoteList = _applyExclusion(remoteList, excludedIds);
      final pageSize = limit;

      int totalCount;
      if (offset == 0) {
        try {
          final remoteCount = await _remote.searchByWorkflowCount(
            body: body,
            workflowStatuses: workflowStatuses,
          );
          totalCount = (remoteCount - excludedIds.length).clamp(0, remoteCount);
        } catch (error) {
          if (isAuthenticationFailure(error)) rethrow;
          totalCount = offset +
              filteredRemoteList.length +
              (remoteList.length == pageSize ? 1 : 0);
        }
      } else {
        totalCount = offset +
            filteredRemoteList.length +
            (remoteList.length == pageSize ? 1 : 0);
      }

      return PaginatedActivityFacilities(
        items: filteredRemoteList,
        totalCount: totalCount,
        fromCache: false,
      );
    } catch (e) {
      if (isAuthenticationFailure(e)) rethrow;
      AppLogger.instance.info("error in paginated fetch $e");
    }

    final excludedIds = await _excludedIdsFor(userTypes);
    final cachedSorted = await _readCacheSorted(
      statuses: workflowStatuses,
      sortDirection: sortDirection,
    );
    final filteredCache = _applyExclusion(cachedSorted, excludedIds)
        .where((wf) => matchesFacilityName(
            wf.activityFacility.facility?.facilityName, body.facilityName))
        .toList();
    final paged = filteredCache.skip(offset).take(limit).toList();

    return PaginatedActivityFacilities(
      items: paged,
      totalCount: filteredCache.length,
      fromCache: true,
    );
  }

  Future<void> _upsertCache(
    List<ActivityFacilityWorkflow> items,
  ) async {
    if (items.isEmpty) return;
    final col = _isar.cacheActivityFacilityWorkflows;
    await _isar.writeTxn(() async {
      for (final wf in items) {
        if (wf.activityFacility.id.isEmpty) continue;
        final existing = await col
            .where()
            .activityFacilityIdEqualTo(wf.activityFacility.id)
            .findAll();
        for (final row in existing) {
          await col.delete(row.id);
        }
        await col.put(CacheActivityFacilityWorkflow(
          activityFacilityId: wf.activityFacility.id,
          status: wf.status ?? '',
          activityFacility: wf.activityFacility,
          transactions: wf.transactions,
          workflow: wf.workflow,
        ));
      }
    });
    await refreshReportBookmarkSnapshots(items,
        amc: false,
        storage: _remote.bookmarkStorage,
        tenantId: _remote.bookmarkTenantId);
  }

  Future<List<ActivityFacilityWorkflow>> readCache(
    List<String> statuses,
  ) async {
    final all = await _isar.cacheActivityFacilityWorkflows.where().findAll();
    final byId = <String, CacheActivityFacilityWorkflow>{};
    for (final row in all) {
      final previous = byId[row.activityFacilityId];
      if (previous == null || row.id > previous.id) {
        byId[row.activityFacilityId] = row;
      }
    }
    return byId.values
        .where((row) => statuses.contains(row.status))
        .map((c) => ActivityFacilityWorkflow(
              activityFacility: c.activityFacility,
              status: c.status,
              transactions: c.transactions,
              workflow: c.workflow,
            ))
        .toList();
  }

  Future<List<ActivityFacilityWorkflow>> _readCacheSorted({
    required List<String> statuses,
    required String sortDirection,
  }) async {
    final list = await readCache(statuses);
    list.sort((a, b) {
      final aDate = a.activityFacility.scheduledAt ?? DateTime(1970);
      final bDate = b.activityFacility.scheduledAt ?? DateTime(1970);
      return sortDirection == 'DESC'
          ? bDate.compareTo(aDate)
          : aDate.compareTo(bDate);
    });
    return list;
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

  UnsubmittedActivityFacilityRepository(this._isar,
      {ActivityFacilityRemoteRepository? remote})
      : _remote = remote ?? ActivityFacilityRemoteRepository();

  Future<List<ActivityFacilityWorkflow>> fetchByWorkflowIncludeCache({
    required String userType,
    required List<String> workflowStatuses,
    required ActivityFacilitySearchModel body,
  }) async {
    final serverCache = ActivityFacilityRepository(_isar, remote: _remote);
    final remoteList = <ActivityFacilityWorkflow>[];
    try {
      var offset = 0;
      const pageSize = 100;
      while (true) {
        final page = await _remote.searchByWorkflow(
            body: body,
            workflowStatuses: workflowStatuses,
            limit: pageSize,
            offset: offset);
        await serverCache._upsertCache(page);
        remoteList.addAll(page);
        if (page.length < pageSize) break;
        offset += page.length;
      }
    } catch (error) {
      if (isAuthenticationFailure(error)) rethrow;
      remoteList.addAll(await serverCache.readCache(workflowStatuses));
    }
    final col = _isar.cacheUnsubmittedActivityFacilitys;
    final localEntries = await col.where().userTypeEqualTo(userType).findAll();
    final localWorkflows = localEntries
        .map((e) => ActivityFacilityWorkflow(
            activityFacility: e.activityFacility, status: e.status))
        .toList();
    final byId = <String, ActivityFacilityWorkflow>{};
    for (final record in remoteList) {
      byId[record.activityFacility.id] = record;
    }
    for (final record in localWorkflows) {
      byId[record.activityFacility.id] = record;
    }
    // Keep the existing local-first ordering and let unsynced data take precedence.
    final localIds =
        localWorkflows.map((record) => record.activityFacility.id).toSet();
    return [
      ...localIds.map((id) => byId[id]!),
      ...byId.entries
          .where((entry) => !localIds.contains(entry.key))
          .map((entry) => entry.value),
    ]
        .where((record) => matchesFacilityName(
            record.activityFacility.facility?.facilityName, body.facilityName))
        .toList();
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
        .activityFacilityIdEqualToAnyUserType(activityFacilityId)
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
