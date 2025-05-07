import 'dart:async';

import 'package:dio/dio.dart';

import '../data/remote_client.dart';
import '../model/dataModel.dart';
import '../model/entities/project_facility.dart';
import '../utils/envConfig.dart';

class ProjectFacilityRemoteRepository {
  ProjectFacilityRemoteRepository();

  final dio = DioClient().dio;

  FutureOr<List<ProjectFacilityModel>> search(ProjectFacilitySearchModel body,
      Map<DataModelType, Map<ApiOperation, String>>? actionMap) async {
    try {
      Response response;
      String searchPath =
          actionMap![DataModelType.projectFacility]![ApiOperation.search]!;

      response = await dio.post(
        searchPath,
        queryParameters: {
          'tenantId': envConfig.variables.tenantId,
          'limit': 100,
          'offset': 0
        },
        data: {"ProjectFacility": body.toMap()},
      );

      final responseMap = response.data['ProjectFacilities'];

      List<ProjectFacilityModel> projectFacilityList = [];
      for (final facility in responseMap) {
        projectFacilityList.add(ProjectFacilityModelMapper.fromMap(facility));
      }

      return projectFacilityList;
    } catch (err) {
      rethrow;
    }
  }
}
