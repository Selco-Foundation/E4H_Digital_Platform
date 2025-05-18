import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';

import '../data/remote_client.dart';
import '../model/projects/project.dart';
import '../utils/envConfig.dart';

/// Repository handling remote operations related to projects.
class ProjectRemoteRepository {
  ProjectRemoteRepository();

  final dio = DioClient().dio;

  /// Searches for projects based on the provided [body]
  FutureOr<List<ProjectModel>> search(List<ProjectSearchModel> body) async {
    try {
      Response response;
      String searchPath = "";
      // actionMap![DataModelType.project]![ApiOperation.search]!;

      if (envConfig.variables.envType == EnvType.dev) {
        return _loadLocalProjects();
      }

      response = await dio.post(searchPath, queryParameters: {
        'tenantId': envConfig.variables.tenantId,
        'limit': 100,
        'offset': 0
      }, data: {
        'Projects': body.map((e) => e.toMap()).toList()
      });

      final responseMap = response.data['Project'];

      List<ProjectModel> projectsList = [];
      for (final project in responseMap) {
        projectsList.add(ProjectModelMapper.fromMap(project));
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
