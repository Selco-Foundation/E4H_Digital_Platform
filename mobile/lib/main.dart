import 'package:digit_data_model/data/local_store/sql_store/sql_store.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/AppLocalization.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:isar/isar.dart';
import 'package:package_info_plus/package_info_plus.dart';

import 'blocs/app_bloc_observer.dart';
import 'blocs/asset_type/asset_type.dart';
import 'blocs/inbox_type/inbox_type.dart';
import 'blocs/report_type/report_type.dart';
import 'router/app_router.dart';
import 'utils/constants.dart';

final LocalSqlDataStore _sql = LocalSqlDataStore();
late Dio _dio;
late Isar _isar;
int i = 0;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final info = await PackageInfo.fromPlatform();
  Bloc.observer = AppBlocObserver();

  DigitUi.instance.initThemeComponents();
  //_isar = await Constants().isar;
  //await initializeService(_dio, _isar);

  runApp(const MainApp());
}

/// A minimal delegate that never crashes and provides an empty localization.
class AppComponentLocalizationDelegate
    extends LocalizationsDelegate<ComponentLocalization> {
  const AppComponentLocalizationDelegate();

  @override
  bool isSupported(Locale locale) => true; // support every locale

  @override
  Future<ComponentLocalization> load(Locale locale) async {
    // Create an “empty” ComponentLocalization so .of(context)! and .translate() work.
    final instance = ComponentLocalization(
      locale,
      Future.value(<dynamic>[]),
      <dynamic>[],
    );
    await instance.load();
    return instance;
  }

  @override
  bool shouldReload(
          covariant LocalizationsDelegate<ComponentLocalization> old) =>
      false;
}

class MainApp extends StatefulWidget {
  const MainApp({super.key});

  @override
  State<MainApp> createState() => _MainAppState();
}

class _MainAppState extends State<MainApp> {
  final _approuter = AppRouter();

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.ltr,
      child: MultiBlocProvider(
        providers: [
          BlocProvider(create: (context) => AssetTypeBloc()),
          BlocProvider(create: (context) => ReportTypeBloc()),
          BlocProvider(create: (context) => InboxTypeBloc()),
        ],
        child: Builder(
          builder: (BuildContext context) {
            return MaterialApp.router(
              scaffoldMessengerKey: scaffoldMessengerKey,
              theme: DigitTheme.instance.mobileTheme.copyWith(),
              routerDelegate: _approuter.delegate(),
              routeInformationParser: _approuter.defaultRouteParser(),
              localizationsDelegates: const [
                AppComponentLocalizationDelegate(),
                GlobalWidgetsLocalizations.delegate,
                GlobalCupertinoLocalizations.delegate,
                GlobalMaterialLocalizations.delegate,
              ],
              supportedLocales: const [
                Locale('en', 'US'),
              ],
              locale: const Locale('en', 'US'),
            );
          },
        ),
      ),
    );
  }
}
