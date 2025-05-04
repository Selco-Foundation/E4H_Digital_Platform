import 'package:auto_route/auto_route.dart';

import '../pages/add_new_asset.dart';
import '../pages/asset_summary.dart';
import '../pages/asset_type_detail.dart';
import '../pages/data_save_success.dart';
import '../pages/download_status.dart';
import '../pages/draft.dart';
import '../pages/enter_otp.dart';
import '../pages/forgot_password.dart';
import '../pages/home.dart';
import '../pages/inbox.dart';
import '../pages/inbox_asset_summary.dart';
import '../pages/installation_report_home.dart';
import '../pages/login.dart';
import '../pages/media_upload.dart';
import '../pages/overall_asset_summary.dart';
import '../pages/select_asset_type.dart';
import '../pages/select_health_facility.dart';
import '../pages/setup_new_password.dart';
import '../pages/specification.dart';
import '../pages/submit_for_approval.dart';
import '../pages/submitted_save_success.dart';
import '../pages/unauthenticated.dart';
import '../pages/welcome.dart';
import '../router/routes.dart';

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
            AutoRoute(page: MediaUploadRoute.page, path: Routes.mediaUpload),
            AutoRoute(page: AssetSummaryRoute.page, path: Routes.assetSummary),
            AutoRoute(
                page: DataSaveSuccessRoute.page, path: Routes.dataSaveSuccess),
            AutoRoute(
                page: OverallAssetSummaryRoute.page,
                path: Routes.overallAssetSummary),
            AutoRoute(
                page: DownloadStatusRoute.page, path: Routes.downloadStatus),
            AutoRoute(page: InboxRoute.page, path: Routes.inbox),
            AutoRoute(
                page: InboxAssetSummaryRoute.page,
                path: Routes.inboxAssetSummary),
            AutoRoute(
                page: SubmitForApprovalRoute.page,
                path: Routes.submitForApproval),
            AutoRoute(
                page: SubmittedSaveSuccessRoute.page,
                path: Routes.submittedSaveSuccess),
            AutoRoute(page: DraftRoute.page, path: Routes.draft)
          ],
        ),
      ];
}
