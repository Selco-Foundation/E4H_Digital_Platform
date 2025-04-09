import 'package:auto_route/auto_route.dart';
import 'package:selco/pages/add_new_asset.dart';
import 'package:selco/pages/asset_type_detail.dart';
import 'package:selco/pages/enter_otp.dart';
import 'package:selco/pages/forgot_password.dart';
import 'package:selco/pages/home.dart';
import 'package:selco/pages/installation_report_home.dart';
import 'package:selco/pages/login.dart';
import 'package:selco/pages/select_asset_type.dart';
import 'package:selco/pages/select_health_facility.dart';
import 'package:selco/pages/setup_new_password.dart';
import 'package:selco/pages/specification.dart';
import 'package:selco/pages/unauthenticated.dart';
import 'package:selco/pages/welcome.dart';
import 'package:selco/router/routes.dart';

export 'package:auto_route/auto_route.dart';

part 'app_router.gr.dart';

@AutoRouterConfig(modules: [])
class AppRouter extends _$AppRouter {
  @override
  RouteType get defaultRouteType => const RouteType.material();

  @override
  List<AutoRoute> get routes => [
        AutoRoute(
          page: UnauthenticatedRouteWrapper.page,
          path: '/',
          children: [
            AutoRoute(
                page: WelcomeRoute.page, initial: true, path: Routes.welcome),
            AutoRoute(page: LoginRoute.page, path: Routes.login),
            AutoRoute(
                page: ForgotPasswordRoute.page, path: Routes.forgotPassword),
            AutoRoute(page: EnterOtpRoute.page, path: Routes.enterOtp),
            AutoRoute(
                page: SetupNewPasswordRoute.page,
                path: Routes.setUpNewPassword),
            AutoRoute(page: HomeRoute.page, path: Routes.home),
            AutoRoute(
                page: InstallationReportRoute.page,
                path: Routes.installationReport),
            AutoRoute(
                page: SelectHealthFacilityRoute.page,
                path: Routes.selectHealthFacility),
            AutoRoute(
                page: SelectAssetTypeRoute.page, path: Routes.selectAssetType),
            AutoRoute(
                page: SpecificationRoute.page, path: Routes.specification),
            AutoRoute(
                page: AssetTypeDetailRoute.page, path: Routes.assetTypeDetail),
            AutoRoute(page: AddNewAssetRoute.page, path: Routes.addNewAsset),
          ],
        ),
      ];
}
