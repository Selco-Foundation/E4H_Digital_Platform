import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:selco/model/project_workflow/project_workflow.dart';

import '../data/remote_client.dart';
import '../model/projects/project.dart';
import '../utils/envConfig.dart';

class ProjectRemoteRepository {
  ProjectRemoteRepository();

  final dio = DioClient().dio;

  FutureOr<List<ProjectWorkflow>> search(ProjectSearchModel body) async {
    try {
      Response response;
      String searchPath = "project/v2/_search";
      // actionMap![DataModelType.project]![ApiOperation.search]!;

      if (envConfig.variables.envType == EnvType.dev) {
        // return _loadLocalProjects();
      }

      response = await dio.post(searchPath, queryParameters: {
        'tenantId': envConfig.variables.tenantId,
        'limit': 100,
        'offset': 0,
        'includeDescendants': false,
        'includeAncestors': false
      }, data: {
        'Project': body.toMap()
      });

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

  FutureOr<List<ProjectWorkflow>> searchByWorkflow({
    required ProjectSearchModel body,
    required List<String> workflowStatuses,
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
          'limit': 100,
          'offset': 0,
          'includeDescendants': false,
          'includeAncestors': false
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

  Future<List<ProjectModel>> _loadLocalProjects() async {
    try {
      final jsonString =
          await rootBundle.loadString('assets/mocks/mockProjects.json');
      final jsonResponse = json.decode(jsonString);
      final responseMap = jsonResponse['Project'];

      List<ProjectModel> projectsList = [];
      for (final project in responseMap) {
        projectsList.add(ProjectModelMapper.fromMap(project));
      }

      return projectsList;
    } catch (e) {
      throw Exception('Failed to load mock projects: $e');
    }
  }
}
