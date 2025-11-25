import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';

import '../model/scheduled_visit/scheduled_visit.dart';
import '../pages/add_new_asset.dart';
import '../pages/amc_draft.dart';
import '../pages/amc_dynamic_form.dart';
import '../pages/amc_home.dart';
import '../pages/amc_inbox.dart';
import '../pages/amc_media_upload.dart';
import '../pages/amc_otp.dart';
import '../pages/amc_report_home.dart';
import '../pages/amc_select_facility.dart';
import '../pages/asset_count.dart';
import '../pages/asset_summary.dart';
import '../pages/asset_type_detail.dart';
import '../pages/authenticated.dart';
import '../pages/data_save_success.dart';
import '../pages/draft.dart';
import '../pages/dynamic_form.dart';
import '../pages/enter_otp.dart';
import '../pages/forgot_password.dart';
import '../pages/home.dart';
import '../pages/image_viewer.dart';
import '../pages/inbox.dart';
import '../pages/inbox_asset_summary.dart';
import '../pages/installation_report_home.dart';
import '../pages/login.dart';
import '../pages/media_upload.dart';
import '../pages/overall_asset_summary.dart';
import '../pages/pdf_viewer.dart';
import '../pages/select_asset_type.dart';
import '../pages/select_health_facility.dart';
import '../pages/setup_new_password.dart';
import '../pages/specification.dart';
import '../pages/submit_for_approval.dart';
import '../pages/submitted_save_success.dart';
import '../pages/sync_loading.dart';
import '../pages/unauthenticated.dart';
import '../pages/video_player.dart';
import '../pages/welcome.dart';
import '../router/routes.dart';
import '../utils/utils.dart';

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
          ],
        ),
        AutoRoute(page: AuthenticatedRouteWrapper.page, path: '/', children: [
          AutoRoute(page: HomeRoute.page, initial: true, path: Routes.home),
          AutoRoute(
              page: InstallationReportRoute.page,
              path: Routes.installationReport),
          AutoRoute(
              page: SelectHealthFacilityRoute.page,
              path: Routes.selectHealthFacility),
          AutoRoute(page: AssetCountRoute.page, path: Routes.assetCount),
          AutoRoute(
              page: SelectAssetTypeRoute.page, path: Routes.selectAssetType),
          AutoRoute(page: SpecificationRoute.page, path: Routes.specification),
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
          AutoRoute(page: SyncLoadingRoute.page, path: Routes.syncLoading),
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
          AutoRoute(page: DraftRoute.page, path: Routes.draft),
          AutoRoute(page: ImageViewerRoute.page, path: Routes.imageViewer),
          AutoRoute(page: VideoPlayerRoute.page, path: Routes.videoViewer),
          AutoRoute(page: PdfViewerRoute.page, path: Routes.pdfViewer),
          AutoRoute(page: DynamicFormsRoute.page, path: Routes.dynamicForm),
          AutoRoute(page: AmcHomeRoute.page, path: Routes.amcHome),
          AutoRoute(page: AmcReportHomeRoute.page, path: Routes.amcReportHome),
          AutoRoute(
              page: AmcSelectFacilityRoute.page,
              path: Routes.amcSelectFacility),
          AutoRoute(
              page: AmcDynamicFormRoute.page, path: Routes.amcDynamicForm),
          AutoRoute(page: AmcOtpRoute.page, path: Routes.amcOtp),
          AutoRoute(page: AmcDraftRoute.page, path: Routes.amcDraft),
          AutoRoute(page: AmcInboxRoute.page, path: Routes.amcInbox),
          AutoRoute(
              page: AmcMediaUploadRoute.page, path: Routes.amcMediaUpload),
        ])
      ];
}
