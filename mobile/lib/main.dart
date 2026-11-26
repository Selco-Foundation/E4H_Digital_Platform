import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:dio/dio.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:image_picker_android/image_picker_android.dart';
import 'package:image_picker_platform_interface/image_picker_platform_interface.dart';
import 'package:isar_community/isar.dart';
import 'package:location/location.dart';

import '../blocs/cache_amc_media_upload/cache_amc_media_upload.dart';
import '../blocs/cache_installation_image/cache_installation_image.dart';
import '../blocs/installation_images/installation_images.dart';
import '../blocs/scheduled_visit/scheduled_visit.dart';
import 'blocs/send_back/send_back.dart';
import 'blocs/activity_facility/activity_facility.dart';
import 'blocs/activity_facility_bom/activity_facility_bom.dart';
import 'blocs/amc_otp/amc_otp.dart';
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
import 'blocs/cache_asset_handover_document/cache_asset_handover_document.dart';
import 'blocs/cache_warranty_card/cache_warranty_card.dart';
import 'blocs/cache_completion_report/cache_completion_report.dart';
import 'blocs/cache_installation_completion_certificate/cache_installation_completion_certificate.dart';
import 'blocs/cache_media_upload/cache_media_upload.dart';
import 'blocs/cache_specification/cache_specification.dart';
import 'blocs/cache_sync_record/cache_sync_record.dart';
import 'blocs/localization/app_localization.dart';
import 'blocs/localization/localization.dart';
import 'blocs/localization/package_localization_delegates.dart';
import 'blocs/overall_asset_summary/overall_asset_summary.dart';
import 'blocs/scheduled_visit_submission/scheduled_visit_submission.dart';
import 'blocs/user_type/user_type.dart';
import 'data/app_shared_preferences.dart';
import 'data/nosql/localization.dart';
import 'data/remote_client.dart';
import 'data/api_interceptors.dart';
import 'utils/extensions.dart';
import 'utils/i18_key_constants.dart' as i18;
import 'model/appconfig/mdmsResponse.dart';
import 'model/data_model.init.dart';
import 'repositories/app_init_repo.dart';
import 'router/app_router.dart';
import 'widgets/app_safe_area.dart';
import 'widgets/localization_loading_screen.dart';
import 'utils/background_service.dart';
import 'utils/constants.dart';
import 'utils/app_logger.dart';
import 'utils/intl_locale.dart';

late Isar _isar;
late Dio _dio;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final imagePickerImplementation = ImagePickerPlatform.instance;
  if (imagePickerImplementation is ImagePickerAndroid) {
    imagePickerImplementation.useAndroidPhotoPicker = true;
  }

  initializeMappers();

  await envConfig.initialize();
  _dio = DioClient().dio;
  _isar = await Constants().isar;

  await setupBackgroundService();
  await ensureAndroidNotificationPermission();

  await AppSharedPreferences().init();

  if (!kIsWeb && Platform.isAndroid) {
    await Firebase.initializeApp();
    await AppLogger.initCrashlytics();
    FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterFatalError;
    PlatformDispatcher.instance.onError = (error, stackTrace) {
      unawaited(
        FirebaseCrashlytics.instance.recordError(
          error,
          stackTrace,
          fatal: true,
        ),
      );
      return true;
    };
  }

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
  late final AuthBloc _authBloc;
  late final SessionExpiredCallback _sessionExpiredCallback;

  @override
  void initState() {
    super.initState();
    _authBloc = AuthBloc()..add(const AuthEvent.attemptLoad());
    _sessionExpiredCallback = () async {
      if (!mounted) return;
      _authBloc.add(const AuthEvent.logout());
      await _approuter.replaceAll(const [
        UnauthenticatedRouteWrapper(children: [WelcomeRoute()])
      ]);
      if (!mounted) return;
      final messenger = scaffoldMessengerKey.currentState;
      if (messenger != null) {
        messenger.hideCurrentSnackBar();
        messenger.showSnackBar(SnackBar(
          content: Text(messenger.context.translate(i18.common.sessionExpired)),
        ));
      }
    };
    AuthTokenInterceptor.onSessionExpired = _sessionExpiredCallback;
  }

  @override
  void dispose() {
    if (AuthTokenInterceptor.onSessionExpired == _sessionExpiredCallback) {
      AuthTokenInterceptor.onSessionExpired = null;
    }
    _authBloc.close();
    _approuter.dispose();
    super.dispose();
  }

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
            BlocProvider<AuthBloc>.value(value: _authBloc),
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
            BlocProvider(create: (context) => SendBackBloc(widget.isar)),
            BlocProvider(create: (context) => ScheduledVisitBloc(widget.isar)),
            BlocProvider(
                lazy: true,
                create: (context) => ScheduleVisitSubmitBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheAmcMediaUploadBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheInstallationImageBloc(widget.isar)),
            BlocProvider(
                create: (context) =>
                    CacheInstallationCompletionCertificateBloc(widget.isar)),
            BlocProvider(
                create: (context) =>
                    CacheAssetHandoverDocumentBloc(widget.isar)),
            BlocProvider(
                create: (context) => CacheWarrantyCardBloc(widget.isar)),
            BlocProvider(
                create: (context) => InstallationImagesBloc(widget.isar)),
            BlocProvider(create: (context) => AmcOtpBloc(widget.isar)),
          ],
          child: BlocBuilder<AppInitialization, InitState>(
            builder: (context, state) {
              Widget buildShell(MdmsResponseModel appConfig) {
                final initialModuleList =
                    appConfig.appConfig!.appConfig?[0].backendInterface;
                final languages = appConfig.appConfig!.appConfig?[0].languages;
                final defaultLocale = languages?.isNotEmpty == true
                    ? languages!.last.value
                    : 'en_IN';
                final savedLocale = AppSharedPreferences().getSelectedLocale;
                final initialLocale = languages?.any(
                            (language) => language.value == savedLocale) ==
                        true
                    ? savedLocale!
                    : defaultLocale;

                return BlocProvider(
                    create: (context) => LocalizationBloc(widget.isar)
                      ..add(LocalizationEvent.onSelect(
                          locale: initialLocale,
                          moduleList: initialModuleList)),
                    child: BlocBuilder<LocalizationBloc, LocalizationState>(
                        builder: (context, state) {
                      final selectedLocale =
                          context.read<LocalizationBloc>().locale ??
                              initialLocale;
                      final parts = selectedLocale.split('_');
                      final locale = Locale(
                          parts.first, parts.length > 1 ? parts[1] : null);
                      final messages = state.maybeWhen(
                        selected: (_, messages) => messages,
                        orElse: () => const <Localization>[],
                      );
                      final translations = AppLocalizations(locale, widget.isar,
                          messages: messages);
                      syncIntlDefaultLocale(selectedLocale);

                      return MaterialApp.router(
                        builder: (context, child) => AppSafeArea(
                          child: LocalizationGate(
                            state: state,
                            onRetry: () => context.read<LocalizationBloc>().add(
                                LocalizationEvent.onSelect(
                                    locale: selectedLocale,
                                    moduleList: initialModuleList)),
                            child: child ?? const SizedBox.shrink(),
                          ),
                        ),
                        scaffoldMessengerKey: scaffoldMessengerKey,
                        theme: DigitTheme.instance.mobileTheme,
                        routerDelegate: _approuter.delegate(),
                        routeInformationParser: _approuter.defaultRouteParser(),
                        supportedLocales: languages?.isNotEmpty == true
                            ? languages!.map((language) {
                                final parts = language.value.split('_');
                                return Locale(parts.first,
                                    parts.length > 1 ? parts[1] : null);
                              })
                            : [locale],
                        localizationsDelegates: [
                          AppLocalizations.getDelegate(
                              appConfig.appConfig!, widget.isar,
                              messages: messages),
                          GlobalWidgetsLocalizations.delegate,
                          GlobalCupertinoLocalizations.delegate,
                          GlobalMaterialLocalizations.delegate,
                          ScannerTranslationsDelegate(translations),
                          FormTranslationsDelegate(translations),
                        ],
                        locale: locale,
                      );
                    }));
              }

              final cachedAppConfig =
                  context.read<AppInitialization>().cachedAppConfig;

              return state.maybeWhen(
                orElse: () => MaterialApp(
                    theme: DigitTheme.instance.mobileTheme,
                    home: const LocalizationLoadingScreen()),
                loadingMdms: (appConfig) => buildShell(appConfig),
                defaulted: (appConfig) => buildShell(appConfig),
                error: (_) => cachedAppConfig != null
                    ? buildShell(cachedAppConfig)
                    : MaterialApp(
                        theme: DigitTheme.instance.mobileTheme,
                        home: const LocalizationLoadingScreen()),
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
