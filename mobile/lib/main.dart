import 'package:digit_scanner/blocs/app_localization.dart'
    as scanner_localization;
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:forms_engine/blocs/app_localization.dart' as forms_localization;
import 'package:isar/isar.dart';
import 'package:location/location.dart';

import 'blocs/app_init/app_init.dart';
import 'blocs/asset_rejection/asset_rejection.dart';
import 'blocs/asset_submission/asset_submission.dart';
import 'blocs/asset_summary/asset_summary.dart';
import 'blocs/auth/authbloc.dart';
import 'blocs/auth/user_otp.dart';
import 'blocs/cache_add_new_asset/cache_add_new_asset.dart';
import 'blocs/cache_asset/cache_asset.dart';
import 'blocs/cache_asset_count/cache_asset_count.dart';
import 'blocs/cache_asset_detail/cache_asset_detail.dart';
import 'blocs/cache_completion_report/cache_completion_report.dart';
import 'blocs/cache_media_upload/cache_media_upload.dart';
import 'blocs/cache_project_asset/cache_project_asset.dart';
import 'blocs/cache_specification/cache_specification.dart';
import 'blocs/cache_sync_record/cache_sync_record.dart';
import 'blocs/localization/app_localization.dart';
import 'blocs/localization/localization.dart';
import 'blocs/overall_asset_summary/overall_asset_summary.dart';
import 'blocs/project/project.dart';
import 'blocs/user_type/user_type.dart';
import 'data/app_shared_preferences.dart';
import 'data/nosql/localization.dart';
import 'data/remote_client.dart';
import 'model/data_model.init.dart';
import 'repositories/app_init_Repo.dart';
import 'router/app_router.dart';
import 'utils/constants.dart';

late Isar _isar;
late Dio _dio;

void main() async {
  // Ensure Flutter widgets are initialized
  WidgetsFlutterBinding.ensureInitialized();
  initializeMappers();

  // Initialize environment configurations, ISAR, dio
  await envConfig.initialize();
  _dio = DioClient().dio;
  _isar = await Constants().isar;

  // Initialize shared preferences
  await AppSharedPreferences().init();

  // Check if it's the first launch of the app
  if (AppSharedPreferences().isFirstLaunch) {
    // Log first launch
    AppLogger.instance.info('App Launched First Time', title: 'main');

    // Mark app as launched for the first time
    await AppSharedPreferences().appLaunchedFirstTime();
  }

  // Run the main app widget
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
                //try to load credentials locally first to skip login page
                return AuthBloc()..add(const AuthEvent.attemptLoad());
              },
            ),
            BlocProvider(create: (context) => UserOtpBloc()),
            BlocProvider<ProjectBloc>(
                create: (context) => ProjectBloc(widget.isar)),
            BlocProvider(create: (_) => LocationBloc(location: Location())),
            BlocProvider(create: (context) => UserTypeBloc()),
            BlocProvider(
                create: (context) => CacheProjectAssetBloc(widget.isar)),
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
            BlocProvider(create: (context) => AssetSubmissionBloc(widget.isar)),
            BlocProvider(create: (context) => CacheSyncRecordBloc(widget.isar)),
            BlocProvider(create: (context) => CacheAssetBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheCompletionReportBloc(widget.isar)),
            BlocProvider(create: (context) => RejectionBloc())
          ],
          child: BlocBuilder<AppInitialization, InitState>(
            builder: (context, state) => state.maybeWhen(
                orElse: () => const Center(child: Text('error Initializing')),
                initialized: (appConfig, assetCount, assetType, system,
                    warranty, brand, solutionDesign) {
                  final initialModuleList =
                      appConfig.appConfig!.appConfig?[0].backendInterface;
                  final languages =
                      appConfig.appConfig!.appConfig?[0].languages;
                  var firstLanguage;
                  firstLanguage = languages?.last.value;

                  // Get the selected locale from shared preferences, or fallback to the default firstLanguage
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
                          routeInformationParser:
                              _approuter.defaultRouteParser(),
                          // Define supported locales based on available languages
                          supportedLocales: languages != null
                              ? languages.map((e) {
                                  final results = e.value.split('_');

                                  return results.isNotEmpty
                                      ? Locale(results.first, results.last)
                                      : firstLanguage;
                                })
                              : [firstLanguage],
                          // Define localizations delegates
                          localizationsDelegates: [
                            AppLocalizations.getDelegate(
                                appConfig.appConfig!, widget.isar),
                            GlobalWidgetsLocalizations.delegate,
                            GlobalCupertinoLocalizations.delegate,
                            GlobalMaterialLocalizations.delegate,
                            scanner_localization.ScannerLocalization
                                .getDelegate(
                                    getLocalizationString(
                                        widget.isar, selectedLocale),
                                    languages!),
                            // 🔴 REQUIRED for forms_engine / JsonForms
                            forms_localization.FormLocalization.getDelegate(
                              getLocalizationString(
                                  widget.isar, selectedLocale),
                              languages!, // the same languages list you already have
                            ),
                          ],
                          // Set the locale for the app
                          locale: languages != null
                              ? Locale(
                                  selectedLocale!.split("_").first,
                                  selectedLocale.split("_").last,
                                )
                              : firstLanguage,
                        );
                      }));
                }),
          )),
    );
  }
}

// Function to fetch localization values for the selected locale from Isar database
Future<List<dynamic>> getLocalizationString(
    Isar isar, String selectedLocale) async {
  // Initialize an empty list to store localization values
  List<dynamic> localizationValues = [];

  // Query Isar database to fetch localization wrappers for the selected locale
  final List<LocalizationWrapper> localizationList =
      await isar.localizationWrappers
          .filter()
          .localeEqualTo(
            selectedLocale.toString(),
          )
          .findAll();

  // Check if localization wrappers are found for the selected locale
  if (localizationList.isNotEmpty) {
    // Add localization values to the list if found
    localizationValues.addAll(localizationList.first.localization!);
  }

  // Return the fetched localization values
  return localizationValues;
}
