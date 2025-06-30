import 'dart:async';

import 'package:isar/isar.dart';

import '../data/nosql/cache_project_facility.dart';
import '../data/remote_client.dart';
import '../model/entities/project_facility.dart';
import '../utils/envConfig.dart';

class FacilityNotFoundException implements Exception {
  final String projectId;
  FacilityNotFoundException(this.projectId);

  @override
  String toString() => 'No facility found for project "$projectId".';
}

class ProjectFacilityRepository {
  ProjectFacilityRepository();

  final dio = DioClient().dio;

  FutureOr<CacheProjectFacility> search(
    ProjectFacilitySearchModel body,
    Isar isar,
  ) async {
    final projectId = body.projectId!.first;

    final existing = await isar.cacheProjectFacilitys
        .where()
        .projectIdEqualTo(projectId)
        .findFirst();
    if (existing != null) {
      return existing;
    }

    const String searchPath = "project/facility/v1/_search";
    final response = await dio.post(
      searchPath,
      queryParameters: {
        'tenantId': envConfig.variables.tenantId,
        'limit': 1,
        'offset': 0,
      },
      data: {"ProjectFacility": body.toMap()},
    );

    final List<dynamic>? responseMap = response.data['ProjectFacilities'];
    if (responseMap == null || responseMap.isEmpty) {
      throw FacilityNotFoundException(projectId);
    }

    final facilityModel = ProjectFacilityModelMapper.fromMap(responseMap.first);
    final newEntry = CacheProjectFacility(
      projectId: projectId,
      facilityId: facilityModel.facilityId,
    );

    await isar.writeTxn(() async {
      await isar.cacheProjectFacilitys.put(newEntry);
    });
    return newEntry;
  }
}
