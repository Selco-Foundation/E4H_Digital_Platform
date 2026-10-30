import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/blocs/app_init/app_init.dart';
import '../../lib/widgets/mdms/mdms_gate.dart';
import '../../lib/data/api_interceptors.dart';
import '../../lib/model/appconfig/mdmsResponse.dart';
import '../../lib/repositories/app_init_repo.dart';
import '../../lib/utils/envConfig.dart' as env;

const types = ['INVERTER', 'BATTERY', 'PANEL'];
final payloads = <String, Map<String, dynamic>>{
  'asset-registry.AssetCountSchema': {
    'id': 1,
    'module': 'installation',
    'tenantId': 'in',
    'AssetCount': [
      for (final type in types)
        {'asset_type_code': type, 'active': true, 'min': 0, 'max': 10}
    ]
  },
  'asset-registry.AssetTypeSchema': {
    'id': 1,
    'module': 'installation',
    'tenantId': 'in',
    'AssetType': [
      for (final type in types)
        {'code': type, 'name': type, 'active': true, 'form_fields': []}
    ]
  },
  'asset-registry.SystemSchema': {
    'id': 1,
    'module': 'installation',
    'tenantId': 'in',
    'System': []
  },
  'asset-registry.WarrantyDurationSchema': {
    'id': 1,
    'module': 'installation',
    'tenantId': 'in',
    'WarrantyDuration': [
      {
        'asset_type_code': 'BATTERY',
        'active': true,
        'duration': '1',
        'format': 'year'
      }
    ]
  },
  'asset-registry.BrandSchema': {
    'id': 1,
    'module': 'installation',
    'tenantId': 'in',
    'Brand': []
  },
  'facility.SolarSolutionDesignType': {
    'code': 'test',
    'name': 'test',
    'active': true,
    'url': '',
    'system_code': 'test'
  },
  'common-masters.SolutionDesignTypeBOMForms': {
    'systemCode': 'test',
    'bomForms': [
      {'name': 'AssetForm.test'}
    ]
  },
  'common-masters.BOMFormSchema': {
    'name': 'AssetForm.test',
    'pages': [
      {
        'page': 'test',
        'properties': [
          {'fieldName': 'answer', 'type': 'string', 'format': 'text'}
        ]
      },
    ]
  },
  'common-masters.InstallationImages': {
    'id': 1,
    'tenantId': 'in',
    'InstallationImage': []
  },
};

class InitRepository extends AppInitRepo {
  InitRepository(Dio dio) : super(dio: dio);
  @override
  Future<MdmsResponseModel> searchAppConfiguration() async =>
      const MdmsResponseModel(appConfig: null);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Dio dio;
  late AppInitialization bloc;
  var offline = false;
  var serverFailure = false;
  var assetRequests = 0;
  Completer<void>? hold;

  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({});
    await env.envConfig.initialize();
    AuthTokenInterceptor.resetLogoutGuard();
    offline = false;
    serverFailure = false;
    assetRequests = 0;
    hold = null;
    dio = Dio()
      ..interceptors
          .add(InterceptorsWrapper(onRequest: (options, handler) async {
        final schema = options.data['MdmsCriteria'].schemaCode as String;
        if (schema == 'asset-registry.AssetCountSchema') {
          assetRequests++;
          final pending = hold;
          hold = null;
          await pending?.future;
        }
        if (offline) {
          return handler.reject(DioException(
              requestOptions: options, type: DioExceptionType.connectionError));
        }
        if (serverFailure) {
          return handler.reject(DioException(
            requestOptions: options,
            type: DioExceptionType.badResponse,
            response: Response(requestOptions: options, statusCode: 503),
          ));
        }
        final data = payloads[schema];
        handler
            .resolve(Response(requestOptions: options, statusCode: 200, data: {
          'mdms': data == null
              ? []
              : [
                  {
                    'id': schema,
                    'tenantId': 'in',
                    'schemaCode': schema,
                    'uniqueIdentifier': schema,
                    'isActive': true,
                    'auditDetails': {
                      'createdBy': 'test',
                      'lastModifiedBy': 'test',
                      'createdTime': 1,
                      'lastModifiedTime': 1
                    },
                    'data': data,
                  }
                ],
        }));
      }));
    bloc = AppInitialization(repository: InitRepository(dio));
    final launched = bloc.stream.firstWhere((s) => s is Defaulted);
    bloc.add(const InitEvent.onLaunch());
    await launched;
  });
  tearDown(() async {
    await bloc.close();
    dio.close();
  });

  Future<InitState> load() {
    final result =
        bloc.stream.firstWhere((s) => s is Initialized || s is Error);
    bloc.add(const InitEvent.fetchMdms());
    return result;
  }

  test('relogin refreshes already initialized MDMS without an app restart',
      () async {
    expect(await load(), isA<Initialized>());
    AuthTokenInterceptor.resetLogoutGuard();
    expect(await load(), isA<Initialized>());
    expect(assetRequests, 2);
  });
  for (final previousFailure in [false, true]) {
    testWidgets(
        'installation home refreshes after previous failure: $previousFailure',
        (tester) async {
      serverFailure = previousFailure;
      await tester.runAsync(load);
      serverFailure = false;
      final refreshed = bloc.stream.firstWhere((state) => state is Initialized);
      await tester.pumpWidget(BlocProvider.value(
        value: bloc,
        child: const MaterialApp(
            home: MdmsGate(
                module: MdmsModule.installation,
                child: Scaffold(body: Text('Report')))),
      ));
      await tester.runAsync(() => refreshed);
      await tester.pumpAndSettle();
      expect(assetRequests, 2);
      expect(bloc.state, isA<Initialized>());
      expect(find.byType(Dialog), findsNothing);
      await tester.pumpWidget(const SizedBox.shrink());
      await tester.pumpWidget(BlocProvider.value(
        value: bloc,
        child: const MaterialApp(
            home: MdmsGate(
                module: MdmsModule.installation,
                child: Scaffold(body: Text('Report')))),
      ));
      await tester.pumpAndSettle();
      expect(assetRequests, 2);
      expect(find.byType(Dialog), findsNothing);
      await tester.pumpWidget(const SizedBox.shrink());
    });
  }
  test('server failure without cache supports Retry without logout', () async {
    serverFailure = true;
    final session = AuthTokenInterceptor.sessionGeneration;
    expect(await load(), isA<Error>());
    expect(AuthTokenInterceptor.sessionGeneration, session);
    serverFailure = false;
    expect(await load(), isA<Initialized>());
  });
  test('complete cached configuration remains usable offline', () async {
    expect(await load(), isA<Initialized>());
    offline = true;
    expect(await load(), isA<Initialized>());
  });
  test('server failure falls back to cached configuration', () async {
    expect(await load(), isA<Initialized>());
    serverFailure = true;
    expect(await load(), isA<Initialized>());
  });
  test('duplicate loads share work and stale sessions cannot publish readiness',
      () async {
    final pending = hold = Completer<void>();
    bloc.add(const InitEvent.fetchMdms());
    while (assetRequests == 0) {
      await Future<void>.delayed(Duration.zero);
    }
    bloc.add(const InitEvent.fetchMdms());
    await Future<void>.delayed(Duration.zero);
    expect(assetRequests, 1);
    AuthTokenInterceptor.resetLogoutGuard();
    expect(await load(), isA<Initialized>());
    final emitted = <InitState>[];
    final sub = bloc.stream.listen(emitted.add);
    pending.complete();
    await Future<void>.delayed(const Duration(milliseconds: 20));
    expect(emitted, isEmpty);
    await sub.cancel();
  });
}
