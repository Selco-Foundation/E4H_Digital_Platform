import 'package:dio/dio.dart';

bool isAuthenticationFailure(Object error) =>
    error is DioException &&
    (error.response?.statusCode == 401 ||
        error.response?.statusCode == 403 ||
        error.message == 'SESSION_EXPIRED');

bool matchesFacilityName(String? name, String? query) {
  final search = query?.trim().toLowerCase() ?? '';
  return search.isEmpty || (name ?? '').toLowerCase().contains(search);
}
