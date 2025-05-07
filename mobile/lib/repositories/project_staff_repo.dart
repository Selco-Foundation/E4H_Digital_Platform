import 'dart:async';

import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../model/dataModel.dart';
import '../model/project_staff/project_staff.dart';
import '../utils/envConfig.dart';

/// Repository handling remote operations related to project staff.
class ProjectStaffRemoteRepository {
  ProjectStaffRemoteRepository();

  final dio = DioClient().dio;

  /// Searches for project staff based on the provided [body] and [actionMap].
  FutureOr<List<ProjectStaffModel>> searchStaff(ProjectStaffSearchModel body,
      Map<DataModelType, Map<ApiOperation, String>>? actionMap) async {
    try {
      Response response;
      String searchPath =
          actionMap![DataModelType.projectStaff]![ApiOperation.search]!;

      response = await dio.post(
        searchPath,
        queryParameters: {
          'tenantId': envConfig.variables.tenantId,
          'limit': 100,
          'offset': 0
        },
        data: {"ProjectStaff": body.toMap()},
      );

      final responseMap = response.data['ProjectStaff'];

      List<ProjectStaffModel> projectStaffList = [];
      for (final staff in responseMap) {
        projectStaffList.add(ProjectStaffModelMapper.fromMap(staff));
      }

      return projectStaffList;
    } catch (err) {
      rethrow;
    }
  }
}
