import '../data/remote_client.dart';
import '../model/localization/localizationModel.dart';
import '../utils/envConfig.dart';

class LocalizationRepository {
  final client = DioClient().dio;

  Future<LocalizationModel> getLocalizationsList(
      Map<String, String> queryParameters) async {
    try {
      final response = await client.post(
          '${envConfig.variables.baseUrl}localization/messages/v1/_search',
          queryParameters: queryParameters,
          data: {});

      final responseBody = LocalizationModel.fromJson(response.data);

      return responseBody;
    } catch (err) {
      rethrow;
    }
  }
}
