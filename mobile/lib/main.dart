import 'package:digit_forms_engine/blocs/app_localization.dart'
    as forms_localization;
import 'package:digit_scanner/blocs/app_localization.dart'
    as scanner_localization;
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:isar/isar.dart';
import 'package:location/location.dart';

import 'blocs/activity_facility/activity_facility.dart';
import 'blocs/activity_facility_bom/activity_facility_bom.dart';
import 'blocs/app_init/app_init.dart';
import 'blocs/asset_rejection/asset_rejection.dart';
import 'blocs/asset_submission/asset_submission.dart';
import 'blocs/asset_summary/asset_summary.dart';
import 'blocs/auth/authbloc.dart';
import 'blocs/auth/user_otp.dart';
import 'blocs/cache_activity_facility_asset/cache_activity_facility_asset.dart';
import 'blocs/cache_add_new_asset/cache_add_new_asset.dart';
import 'blocs/cache_asset/cache_asset.dart';
import 'blocs/cache_asset_count/cache_asset_count.dart';
import 'blocs/cache_asset_detail/cache_asset_detail.dart';
import 'blocs/cache_completion_report/cache_completion_report.dart';
import 'blocs/cache_media_upload/cache_media_upload.dart';
import 'blocs/cache_specification/cache_specification.dart';
import 'blocs/cache_sync_record/cache_sync_record.dart';
import 'blocs/localization/app_localization.dart';
import 'blocs/localization/localization.dart';
import 'blocs/overall_asset_summary/overall_asset_summary.dart';
import 'blocs/user_type/user_type.dart';
import 'data/app_shared_preferences.dart';
import 'data/nosql/localization.dart';
import 'data/remote_client.dart';
import 'model/appconfig/mdmsResponse.dart';
import 'model/data_model.init.dart';
import 'repositories/app_init_repo.dart';
import 'router/app_router.dart';
import 'utils/background_service.dart';
import 'utils/constants.dart';

late Isar _isar;
late Dio _dio;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  initializeMappers();

  await envConfig.initialize();
  _dio = DioClient().dio;
  _isar = await Constants().isar;

  await setupBackgroundService();
  await ensureAndroidNotificationPermission();

  await AppSharedPreferences().init();

  if (AppSharedPreferences().isFirstLaunch) {
    AppLogger.instance.info('App Launched First Time', title: 'main');
    await AppSharedPreferences().appLaunchedFirstTime();
  }
  runApp(MainApp(
    isar: _isar,
  ));
}

class MainApp extends StatefulWidget {
  final Isar isar;
  const MainApp({super.key, required this.isar});

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
            BlocProvider(
              create: (context) =>
                  AppInitialization()..add(const InitEvent.onLaunch()),
            ),
            BlocProvider(
              create: (context) {
                return AuthBloc()..add(const AuthEvent.attemptLoad());
              },
            ),
            BlocProvider(create: (context) => UserOtpBloc()),
            BlocProvider<ActivityFacilityBloc>(
                create: (context) => ActivityFacilityBloc(widget.isar)),
            BlocProvider(create: (_) => LocationBloc(location: Location())),
            BlocProvider(create: (context) => UserTypeBloc()),
            BlocProvider(
                create: (context) =>
                    CacheActivityFacilityAssetBloc(widget.isar)),
            BlocProvider(create: (context) => CacheAssetCountBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheSpecificationBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheAssetDetailBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheAddNewAssetBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheMediaUploadBloc(widget.isar)),
            BlocProvider(create: (context) => AssetSummaryBloc(widget.isar)),
            BlocProvider(
                create: (context) => OverallAssetSummaryBloc(widget.isar)),
            BlocProvider(
                lazy: false,
                create: (context) => AssetSubmissionBloc(widget.isar)),
            BlocProvider(create: (context) => CacheSyncRecordBloc(widget.isar)),
            BlocProvider(create: (context) => CacheAssetBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheCompletionReportBloc(widget.isar)),
            BlocProvider(
                create: (context) => ActivityFacilityBomBloc(widget.isar)),
            BlocProvider(create: (context) => RejectionBloc(widget.isar)),
          ],
          child: BlocBuilder<AppInitialization, InitState>(
            builder: (context, state) {
              Widget buildShell(MdmsResponseModel appConfig) {
                final initialModuleList =
                    appConfig.appConfig!.appConfig?[0].backendInterface;
                final languages = appConfig.appConfig!.appConfig?[0].languages;
                var firstLanguage;
                firstLanguage = languages?.last.value;

                return BlocProvider(
                    create: (context) => LocalizationBloc(widget.isar)
                      ..add(LocalizationEvent.onSelect(
                          locale: firstLanguage,
                          moduleList: initialModuleList)),
                    child: BlocBuilder<LocalizationBloc, LocalizationState>(
                        builder: (context, state) {
                      final selectedLocale =
                          AppSharedPreferences().getSelectedLocale ??
                              firstLanguage;

                      return MaterialApp.router(
                        scaffoldMessengerKey: scaffoldMessengerKey,
                        theme: DigitTheme.instance.mobileTheme,
                        routerDelegate: _approuter.delegate(),
                        routeInformationParser: _approuter.defaultRouteParser(),
                        supportedLocales: languages != null
                            ? languages.map((e) {
                                final results = e.value.split('_');

                                return results.isNotEmpty
                                    ? Locale(results.first, results.last)
                                    : firstLanguage;
                              })
                            : [firstLanguage],
                        localizationsDelegates: [
                          AppLocalizations.getDelegate(
                              appConfig.appConfig!, widget.isar),
                          GlobalWidgetsLocalizations.delegate,
                          GlobalCupertinoLocalizations.delegate,
                          GlobalMaterialLocalizations.delegate,
                          scanner_localization.ScannerLocalization.getDelegate(
                              getLocalizationString(
                                  widget.isar, selectedLocale),
                              languages!),
                          forms_localization.FormLocalization.getDelegate(
                            getLocalizationString(widget.isar, selectedLocale),
                            languages!,
                          ),
                        ],
                        locale: languages != null
                            ? Locale(
                                selectedLocale!.split("_").first,
                                selectedLocale.split("_").last,
                              )
                            : firstLanguage,
                      );
                    }));
              }

              return state.maybeWhen(
                orElse: () =>
                    const Scaffold(body: Center(child: Text('loading...'))),
                loadingMdms: (appConfig) => buildShell(appConfig),
                defaulted: (appConfig) => buildShell(appConfig),
                initialized: (
                  appConfig,
                  assetCount,
                  assetType,
                  system,
                  warranty,
                  brand,
                  solutionDesign,
                  solutionDesignBom,
                ) =>
                    buildShell(appConfig),
              );
            },
          )),
    );
  }
}

Future<List<dynamic>> getLocalizationString(
    Isar isar, String selectedLocale) async {
  List<dynamic> localizationValues = [];

  final List<LocalizationWrapper> localizationList =
      await isar.localizationWrappers
          .filter()
          .localeEqualTo(
            selectedLocale.toString(),
          )
          .findAll();

  if (localizationList.isNotEmpty) {
    localizationValues.addAll(localizationList.first.localization!);
  }

  return localizationValues;
}
