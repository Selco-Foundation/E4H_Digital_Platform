import 'package:dio/dio.dart';

import '../data/api_interceptors.dart';
import '../data/remote_client.dart';
import '../model/user/userModel.dart';
import '../utils/envConfig.dart';

class UserRepository {
  UserRepository();

  Future<Response> searchUser(String url, String uuid) async {
    final client = DioClient().dio;
    client.interceptors.add(AuthTokenInterceptor());

    try {
      final response = await client.post(url,
          queryParameters: {
            'offset': 0,
            'limit': 100,
            'tenantId': envConfig.variables.tenantId,
          },
          data: UserSearchModel(uuid: [uuid]).toMap());

      return response;
    } catch (err) {
      rethrow;
    }
  }

  Future<Response> updateUser(String url, UserModel user) async {
    final client = DioClient().dio;

    try {
      final response = await client.post(url, queryParameters: {
        'offset': 0,
        'limit': 100,
        'tenantId': envConfig.variables.tenantId,
      }, data: {
        "user": user.toMap()
      });
      return response;
    } catch (err) {
      rethrow;
    }
  }
}
