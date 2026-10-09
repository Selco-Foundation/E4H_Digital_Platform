import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/data/api_interceptors.dart';
import '../../lib/data/remote_client.dart';
import '../../lib/model/appconfig/mdmsRequest.dart';
import '../../lib/repositories/app_init_repo.dart' hide envConfig;
import '../../lib/utils/envConfig.dart';

const request = MdmsRequestModel(
    mdmsCriteria: MdmsCriteriaModel(
        tenantId: 'in',
        schemaCode: 'asset-registry.AssetCountSchema',
        moduleDetails: []));
final record = {
  'id': 'one',
  'tenantId': 'in',
  'schemaCode': 'asset-registry.AssetCountSchema',
  'uniqueIdentifier': 'one',
  'isActive': true,
  'auditDetails': {
    'createdBy': 'user',
    'lastModifiedBy': 'user',
    'createdTime': 1,
    'lastModifiedTime': 1
  },
  'data': {
    'id': 1,
    'module': 'installation',
    'tenantId': 'in',
    'AssetCount': []
  },
};

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(() async {
    await envConfig.initialize();
    FlutterSecureStorage.setMockInitialValues({});
  });

  test('authenticated MDMS reaches API without connectivity plugins', () async {
    final dio = Dio(configuredApiOptions());
    dio.interceptors.add(AuthTokenInterceptor());
    dio.interceptors.add(InterceptorsWrapper(onRequest: (options, handler) {
      expect(options.data['RequestInfo'], isNotNull);
      handler.resolve(Response(requestOptions: options, statusCode: 200, data: {
        'mdms': [record]
      }));
    }));
    final result = await AppInitRepo(dio: dio).searchAssetCount(request);
    expect(result.single.id, 'one');
    expect(dio.options.connectTimeout,
        Duration(milliseconds: envConfig.variables.connectTimeout));
    expect(dio.options.sendTimeout,
        Duration(milliseconds: envConfig.variables.sendTimeout));
    expect(dio.options.receiveTimeout,
        Duration(milliseconds: envConfig.variables.receiveTimeout));
    dio.close();
  });

  for (final type in [
    DioExceptionType.connectionError,
    DioExceptionType.connectionTimeout,
    DioExceptionType.sendTimeout,
    DioExceptionType.receiveTimeout
  ]) {
    for (final raw in [
      jsonEncode([record]),
      '[]',
      null,
      '{}',
      'broken'
    ]) {
      test('$type with cache $raw', () async {
        FlutterSecureStorage.setMockInitialValues({
          if (raw != null) 'assetCount': raw,
          if (raw != null) 'formConfigsRaw': raw,
        });
        final dio = Dio();
        dio.interceptors.add(InterceptorsWrapper(
            onRequest: (options, handler) => handler
                .reject(DioException(requestOptions: options, type: type))));
        final repo = AppInitRepo(dio: dio);
        if (raw == null || raw == '{}' || raw == 'broken') {
          for (final load in [
            () => repo.searchAssetCount(request),
            () => repo.searchFormConfigsRaw(request)
          ]) {
            await expectLater(
                load(),
                throwsA(predicate((e) => e
                    .toString()
                    .contains(raw == null ? 'missing' : 'invalid'))));
          }
        } else {
          expect((await repo.searchAssetCount(request)).length,
              raw == '[]' ? 0 : 1);
          expect((await repo.searchFormConfigsRaw(request)).length,
              raw == '[]' ? 0 : 1);
        }
        dio.close();
      });
    }
  }
  for (final status in [401, 403]) {
    test('$status never falls back to cached MDMS', () async {
      FlutterSecureStorage.setMockInitialValues({
        'assetCount': jsonEncode([record])
      });
      final dio = Dio();
      dio.interceptors.add(InterceptorsWrapper(
          onRequest: (options, handler) => handler.reject(DioException(
              requestOptions: options,
              type: DioExceptionType.badResponse,
              response:
                  Response(requestOptions: options, statusCode: status)))));
      await expectLater(AppInitRepo(dio: dio).searchAssetCount(request),
          throwsA(isA<DioException>()));
      dio.close();
    });
  }
}
