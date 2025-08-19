import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:isar/isar.dart';
import 'package:selco/data/nosql/cache_unsubmitted_project.dart';

import '../data/nosql/cache_project_workflow.dart';
import '../data/remote_client.dart';
import '../model/document/document.dart';
import '../model/project_workflow/project_workflow.dart';
import '../model/projects/project.dart';
import '../utils/envConfig.dart';
import '../utils/utils.dart';

class ProjectRemoteRepository {
  ProjectRemoteRepository();

  final dio = DioClient().dio;

  FutureOr<List<ProjectWorkflow>> searchByWorkflow(
      {required ProjectSearchModel body,
      required List<String> workflowStatuses,
      int limit = 100,
      offset = 0,
      sortDirection = 'ASC'}) async {
    try {
      Response response;
      String searchPath = "project/v2/_search";

      print("envConfig.variables.envType ${envConfig.variables.envType}");
      print("EnvType.dev ${EnvType.dev}");
      if (envConfig.variables.envType == EnvType.dev) {
        print("EnvType.dev got here");
        // return _loadLocalProjects();
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
          'Project': body.toMap(),
          'workflowStatus': workflowStatuses,
        },
      );

      final responseMap = response.data['Project'];

      List<ProjectWorkflow> projectsList = [];
      for (final project in responseMap) {
        projectsList.add(ProjectWorkflow.fromJson(project));
      }
      return projectsList;
    } catch (err) {
      rethrow;
    }
  }

  FutureOr<int> searchByWorkflowCount({
    required ProjectSearchModel body,
    required List<String> workflowStatuses,
    int limit = 0,
    offset = 0,
  }) async {
    try {
      Response response;
      String searchPath = "project/v2/_search";

      if (envConfig.variables.envType == EnvType.dev) {
        // return _loadLocalProjects();
      }

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
          'Project': body.toMap(),
          'workflowStatus': workflowStatuses,
        },
      );

      final count = response.data['totalCount'];
      return count ?? 0;
    } catch (err) {
      rethrow;
    }
  }

  Future<void> updateProjectWorkflow({
    required String projectId,
    required String action,
    List<Document>? documents,
  }) async {
    final url = 'project/v1/project/workflow/update';

    final body = <String, dynamic>{
      'projectId': projectId,
      'workflow': {
        'action': action,
        if (documents != null) ...{
          'documents': documents.map((d) => d.toJsonForWorkflow()).toList()
        }
      }
    };

    print("body ${jsonEncode(body)}");

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

  Future<List<ProjectWorkflow>> _loadLocalProjects() async {
    try {
      final jsonString = await rootBundle.loadString(
          'assets/mocks/mockRejectedProject.json'); // Testing rejected Facilities
      final jsonResponse = json.decode(jsonString);
      final responseMap = jsonResponse['Project'];

      List<ProjectWorkflow> projectsList = [];
      for (final project in responseMap) {
        projectsList.add(ProjectWorkflow.fromJson(project));
      }

      return projectsList;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }
}

class ProjectRepository {
  final Isar _isar;
  final ProjectRemoteRepository _remote;

  ProjectRepository(this._isar) : _remote = ProjectRemoteRepository();

  /// Remote-first fetch with cache fallback
  Future<List<ProjectWorkflow>> fetchByWorkflow(
      {required ProjectSearchModel body,
      required List<String> workflowStatuses,
      sortDirection = 'ASC'}) async {
    try {
      print("workflowStatuses $workflowStatuses");
      final remoteList = await _remote.searchByWorkflow(
        body: body,
        workflowStatuses: workflowStatuses,
        sortDirection: sortDirection,
      );

      if (remoteList != null) {
        await _replaceCache(workflowStatuses, remoteList);
        return remoteList;
      }
    } catch (e) {
      // on any error, fall back to cache
      print("error in fetching remote project ${e.toString()}");
    }
    return readCache(workflowStatuses);
  }

  /// 1) Delete all cached entries matching any given status
  /// 2) Insert fresh entries
  Future<void> _replaceCache(
    List<String> statuses,
    List<ProjectWorkflow> newList,
  ) async {
    final col = _isar.cacheProjectWorkflows;
    await _isar.writeTxn(() async {
      // DELETE step
      for (final status in statuses) {
        final toDelete = await col.where().statusEqualTo(status).findAll();
        for (final entry in toDelete) {
          await col.delete(entry.id);
        }
      }
      // INSERT fresh
      for (final wf in newList) {
        await col.put(CacheProjectWorkflow(
          status: wf.status ?? '',
          project: wf.project,
          transactions: wf.transactions,
          workflow: wf.workflow,
        ));
      }
    });
  }

  /// Read cache entries matching any of the statuses
  Future<List<ProjectWorkflow>> readCache(
    List<String> statuses,
  ) async {
    final col = _isar.cacheProjectWorkflows;
    final List<CacheProjectWorkflow> all = [];
    for (final status in statuses) {
      final matches = await col.where().statusEqualTo(status).findAll();
      all.addAll(matches);
    }
    return all
        .map((c) => ProjectWorkflow(
              project: c.project,
              status: c.status,
              transactions: c.transactions,
              workflow: c.workflow,
            ))
        .toList();
  }
}

class UnsubmittedProjectRepository {
  final Isar _isar;
  final ProjectRemoteRepository _remote;

  UnsubmittedProjectRepository(this._isar)
      : _remote = ProjectRemoteRepository();

  Future<List<ProjectWorkflow>> fetchByWorkflowIncludeCache({
    required String userType,
    required List<String> workflowStatuses,
    required ProjectSearchModel body,
  }) async {
    // 1) remote fetch
    List<ProjectWorkflow> remoteList;
    try {
      remoteList = await _remote.searchByWorkflow(
        body: body,
        workflowStatuses: workflowStatuses,
      );
    } catch (_) {
      remoteList = <ProjectWorkflow>[];
    }

    // 2) load local cache for this userType
    final col = _isar.cacheUnsubmittedProjects;
    final localEntries =
        await col.where().filter().userTypeEqualTo(userType).findAll();

    // 3) convert to domain objects
    final localWorkflows = localEntries
        .map((e) => ProjectWorkflow(project: e.project, status: e.status))
        .toList();

    // 4) build a set of cached IDs for quick membership test
    final cachedIds = localEntries.map((e) => e.project.id).toSet();

    // 5) pick only those remote entries not in cache
    final remoteOnly =
        remoteList.where((r) => !cachedIds.contains(r.project.id));

    // 6) return all cached first, then any remote-only
    return [
      ...localWorkflows,
      ...remoteOnly,
    ];
  }

  Future<CacheUnsubmittedProject> addOrGet(
    ProjectWorkflow wf,
    String userType,
  ) async {
    final col = _isar.cacheUnsubmittedProjects;
    final projectId = wf.project.id;
    final existing = await col
        .where()
        .projectIdEqualTo(projectId)
        .filter()
        .userTypeEqualTo(userType)
        .findFirst();
    if (existing != null) return existing;

    final entry = CacheUnsubmittedProject(
      projectId: projectId,
      status: wf.status ?? '',
      project: wf.project,
      userType: userType,
    );
    await _isar.writeTxn(() => col.put(entry));
    return entry;
  }

  Future<void> delete(String projectId, String userType) async {
    final col = _isar.cacheUnsubmittedProjects;
    await _isar.writeTxn(() async {
      final toDelete = await col
          .where()
          .projectIdEqualTo(projectId)
          .filter()
          .userTypeEqualTo(userType)
          .findAll();
      for (final e in toDelete) {
        await col.delete(e.id);
      }
    });
  }
}
