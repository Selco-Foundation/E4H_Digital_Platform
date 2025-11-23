// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// AutoRouterGenerator
// **************************************************************************

// ignore_for_file: type=lint
// coverage:ignore-file

part of 'app_router.dart';

abstract class _$AppRouter extends RootStackRouter {
  // ignore: unused_element
  _$AppRouter({super.navigatorKey});

  @override
  final Map<String, PageFactory> pagesMap = {
    AddNewAssetRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AddNewAssetPage(),
      );
    },
    AmcDraftRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcDraftPage(),
      );
    },
    AmcDynamicFormRoute.name: (routeData) {
      final args = routeData.argsAs<AmcDynamicFormRouteArgs>();
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: AmcDynamicFormPage(
          key: args.key,
          pageName: args.pageName,
          schemaName: args.schemaName,
          uniqueIdentifier: args.uniqueIdentifier,
          projectId: args.projectId,
          origin: args.origin,
        ),
      );
    },
    AmcHomeRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcHomePage(),
      );
    },
    AmcInboxRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcInboxPage(),
      );
    },
    AmcMediaUploadRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcMediaUploadPage(),
      );
    },
    AmcOtpRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcOtpPage(),
      );
    },
    AmcReportHomeRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcReportHomePage(),
      );
    },
    AmcSelectFacilityRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AmcSelectFacilityPage(),
      );
    },
    AssetCountRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AssetCountPage(),
      );
    },
    AssetSummaryRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AssetSummaryPage(),
      );
    },
    AssetTypeDetailRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AssetTypeDetailPage(),
      );
    },
    AuthenticatedRouteWrapper.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const AuthenticatedScreenWrapper(),
      );
    },
    DataSaveSuccessRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const DataSaveSuccessPage(),
      );
    },
    DraftRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const DraftPage(),
      );
    },
    DynamicFormsRoute.name: (routeData) {
      final args = routeData.argsAs<DynamicFormsRouteArgs>();
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: DynamicFormsPage(
          key: args.key,
          pageName: args.pageName,
          schemaName: args.schemaName,
          uniqueIdentifier: args.uniqueIdentifier,
          projectId: args.projectId,
          origin: args.origin,
        ),
      );
    },
    EnterOtpRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const EnterOtpPage(),
      );
    },
    ForgotPasswordRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const ForgotPasswordPage(),
      );
    },
    HomeRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const HomePage(),
      );
    },
    ImageViewerRoute.name: (routeData) {
      final args = routeData.argsAs<ImageViewerRouteArgs>();
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: ImageViewerPage(
          path: args.path,
          key: args.key,
        ),
      );
    },
    InboxAssetSummaryRoute.name: (routeData) {
      final args = routeData.argsAs<InboxAssetSummaryRouteArgs>(
          orElse: () => const InboxAssetSummaryRouteArgs());
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: InboxAssetSummaryPage(
          key: args.key,
          refresh: args.refresh,
        ),
      );
    },
    InboxRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const InboxPage(),
      );
    },
    InstallationReportRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const InstallationReportPage(),
      );
    },
    LoginRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const LoginPage(),
      );
    },
    MediaUploadRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const MediaUploadPage(),
      );
    },
    OverallAssetSummaryRoute.name: (routeData) {
      final args = routeData.argsAs<OverallAssetSummaryRouteArgs>(
          orElse: () => const OverallAssetSummaryRouteArgs());
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: OverallAssetSummaryPage(
          key: args.key,
          refresh: args.refresh,
        ),
      );
    },
    PdfViewerRoute.name: (routeData) {
      final pathParams = routeData.inheritedPathParams;
      final args = routeData.argsAs<PdfViewerRouteArgs>(
          orElse: () => PdfViewerRouteArgs(path: pathParams.getString('path')));
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: PdfViewerPage(
          path: args.path,
          key: args.key,
        ),
      );
    },
    SelectAssetTypeRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SelectAssetTypePage(),
      );
    },
    SelectHealthFacilityRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SelectHealthFacilityPage(),
      );
    },
    SetupNewPasswordRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SetupNewPasswordPage(),
      );
    },
    SpecificationRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SpecificationPage(),
      );
    },
    SubmitForApprovalRoute.name: (routeData) {
      final args = routeData.argsAs<SubmitForApprovalRouteArgs>(
          orElse: () => const SubmitForApprovalRouteArgs());
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: SubmitForApprovalPage(
          key: args.key,
          refresh: args.refresh,
        ),
      );
    },
    SubmittedSaveSuccessRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SubmittedSaveSuccessPage(),
      );
    },
    SyncLoadingRoute.name: (routeData) {
      final args = routeData.argsAs<SyncLoadingRouteArgs>();
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: SyncLoadingPage(
          key: args.key,
          completed: args.completed,
          total: args.total,
        ),
      );
    },
    UnauthenticatedRouteWrapper.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const UnauthenticatedScreenWrapper(),
      );
    },
    VideoPlayerRoute.name: (routeData) {
      final args = routeData.argsAs<VideoPlayerRouteArgs>();
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: VideoPlayerPage(
          path: args.path,
          key: args.key,
        ),
      );
    },
    WelcomeRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const WelcomePage(),
      );
    },
  };
}

/// generated route for
/// [AddNewAssetPage]
class AddNewAssetRoute extends PageRouteInfo<void> {
  const AddNewAssetRoute({List<PageRouteInfo>? children})
      : super(
          AddNewAssetRoute.name,
          initialChildren: children,
        );

  static const String name = 'AddNewAssetRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcDraftPage]
class AmcDraftRoute extends PageRouteInfo<void> {
  const AmcDraftRoute({List<PageRouteInfo>? children})
      : super(
          AmcDraftRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcDraftRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcDynamicFormPage]
class AmcDynamicFormRoute extends PageRouteInfo<AmcDynamicFormRouteArgs> {
  AmcDynamicFormRoute({
    Key? key,
    required String pageName,
    String? schemaName,
    String? uniqueIdentifier,
    required String projectId,
    required FormOrigin origin,
    List<PageRouteInfo>? children,
  }) : super(
          AmcDynamicFormRoute.name,
          args: AmcDynamicFormRouteArgs(
            key: key,
            pageName: pageName,
            schemaName: schemaName,
            uniqueIdentifier: uniqueIdentifier,
            projectId: projectId,
            origin: origin,
          ),
          rawPathParams: {'pageName': pageName},
          initialChildren: children,
        );

  static const String name = 'AmcDynamicFormRoute';

  static const PageInfo<AmcDynamicFormRouteArgs> page =
      PageInfo<AmcDynamicFormRouteArgs>(name);
}

class AmcDynamicFormRouteArgs {
  const AmcDynamicFormRouteArgs({
    this.key,
    required this.pageName,
    this.schemaName,
    this.uniqueIdentifier,
    required this.projectId,
    required this.origin,
  });

  final Key? key;

  final String pageName;

  final String? schemaName;

  final String? uniqueIdentifier;

  final String projectId;

  final FormOrigin origin;

  @override
  String toString() {
    return 'AmcDynamicFormRouteArgs{key: $key, pageName: $pageName, schemaName: $schemaName, uniqueIdentifier: $uniqueIdentifier, projectId: $projectId, origin: $origin}';
  }
}

/// generated route for
/// [AmcHomePage]
class AmcHomeRoute extends PageRouteInfo<void> {
  const AmcHomeRoute({List<PageRouteInfo>? children})
      : super(
          AmcHomeRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcHomeRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcInboxPage]
class AmcInboxRoute extends PageRouteInfo<void> {
  const AmcInboxRoute({List<PageRouteInfo>? children})
      : super(
          AmcInboxRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcInboxRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcMediaUploadPage]
class AmcMediaUploadRoute extends PageRouteInfo<void> {
  const AmcMediaUploadRoute({List<PageRouteInfo>? children})
      : super(
          AmcMediaUploadRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcMediaUploadRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcOtpPage]
class AmcOtpRoute extends PageRouteInfo<void> {
  const AmcOtpRoute({List<PageRouteInfo>? children})
      : super(
          AmcOtpRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcOtpRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcReportHomePage]
class AmcReportHomeRoute extends PageRouteInfo<void> {
  const AmcReportHomeRoute({List<PageRouteInfo>? children})
      : super(
          AmcReportHomeRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcReportHomeRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AmcSelectFacilityPage]
class AmcSelectFacilityRoute extends PageRouteInfo<void> {
  const AmcSelectFacilityRoute({List<PageRouteInfo>? children})
      : super(
          AmcSelectFacilityRoute.name,
          initialChildren: children,
        );

  static const String name = 'AmcSelectFacilityRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AssetCountPage]
class AssetCountRoute extends PageRouteInfo<void> {
  const AssetCountRoute({List<PageRouteInfo>? children})
      : super(
          AssetCountRoute.name,
          initialChildren: children,
        );

  static const String name = 'AssetCountRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AssetSummaryPage]
class AssetSummaryRoute extends PageRouteInfo<void> {
  const AssetSummaryRoute({List<PageRouteInfo>? children})
      : super(
          AssetSummaryRoute.name,
          initialChildren: children,
        );

  static const String name = 'AssetSummaryRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AssetTypeDetailPage]
class AssetTypeDetailRoute extends PageRouteInfo<void> {
  const AssetTypeDetailRoute({List<PageRouteInfo>? children})
      : super(
          AssetTypeDetailRoute.name,
          initialChildren: children,
        );

  static const String name = 'AssetTypeDetailRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [AuthenticatedScreenWrapper]
class AuthenticatedRouteWrapper extends PageRouteInfo<void> {
  const AuthenticatedRouteWrapper({List<PageRouteInfo>? children})
      : super(
          AuthenticatedRouteWrapper.name,
          initialChildren: children,
        );

  static const String name = 'AuthenticatedRouteWrapper';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [DataSaveSuccessPage]
class DataSaveSuccessRoute extends PageRouteInfo<void> {
  const DataSaveSuccessRoute({List<PageRouteInfo>? children})
      : super(
          DataSaveSuccessRoute.name,
          initialChildren: children,
        );

  static const String name = 'DataSaveSuccessRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [DraftPage]
class DraftRoute extends PageRouteInfo<void> {
  const DraftRoute({List<PageRouteInfo>? children})
      : super(
          DraftRoute.name,
          initialChildren: children,
        );

  static const String name = 'DraftRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [DynamicFormsPage]
class DynamicFormsRoute extends PageRouteInfo<DynamicFormsRouteArgs> {
  DynamicFormsRoute({
    Key? key,
    required String pageName,
    String? schemaName,
    String? uniqueIdentifier,
    required String projectId,
    required FormOrigin origin,
    List<PageRouteInfo>? children,
  }) : super(
          DynamicFormsRoute.name,
          args: DynamicFormsRouteArgs(
            key: key,
            pageName: pageName,
            schemaName: schemaName,
            uniqueIdentifier: uniqueIdentifier,
            projectId: projectId,
            origin: origin,
          ),
          rawPathParams: {'pageName': pageName},
          initialChildren: children,
        );

  static const String name = 'DynamicFormsRoute';

  static const PageInfo<DynamicFormsRouteArgs> page =
      PageInfo<DynamicFormsRouteArgs>(name);
}

class DynamicFormsRouteArgs {
  const DynamicFormsRouteArgs({
    this.key,
    required this.pageName,
    this.schemaName,
    this.uniqueIdentifier,
    required this.projectId,
    required this.origin,
  });

  final Key? key;

  final String pageName;

  final String? schemaName;

  final String? uniqueIdentifier;

  final String projectId;

  final FormOrigin origin;

  @override
  String toString() {
    return 'DynamicFormsRouteArgs{key: $key, pageName: $pageName, schemaName: $schemaName, uniqueIdentifier: $uniqueIdentifier, projectId: $projectId, origin: $origin}';
  }
}

/// generated route for
/// [EnterOtpPage]
class EnterOtpRoute extends PageRouteInfo<void> {
  const EnterOtpRoute({List<PageRouteInfo>? children})
      : super(
          EnterOtpRoute.name,
          initialChildren: children,
        );

  static const String name = 'EnterOtpRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [ForgotPasswordPage]
class ForgotPasswordRoute extends PageRouteInfo<void> {
  const ForgotPasswordRoute({List<PageRouteInfo>? children})
      : super(
          ForgotPasswordRoute.name,
          initialChildren: children,
        );

  static const String name = 'ForgotPasswordRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [HomePage]
class HomeRoute extends PageRouteInfo<void> {
  const HomeRoute({List<PageRouteInfo>? children})
      : super(
          HomeRoute.name,
          initialChildren: children,
        );

  static const String name = 'HomeRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [ImageViewerPage]
class ImageViewerRoute extends PageRouteInfo<ImageViewerRouteArgs> {
  ImageViewerRoute({
    required String path,
    Key? key,
    List<PageRouteInfo>? children,
  }) : super(
          ImageViewerRoute.name,
          args: ImageViewerRouteArgs(
            path: path,
            key: key,
          ),
          initialChildren: children,
        );

  static const String name = 'ImageViewerRoute';

  static const PageInfo<ImageViewerRouteArgs> page =
      PageInfo<ImageViewerRouteArgs>(name);
}

class ImageViewerRouteArgs {
  const ImageViewerRouteArgs({
    required this.path,
    this.key,
  });

  final String path;

  final Key? key;

  @override
  String toString() {
    return 'ImageViewerRouteArgs{path: $path, key: $key}';
  }
}

/// generated route for
/// [InboxAssetSummaryPage]
class InboxAssetSummaryRoute extends PageRouteInfo<InboxAssetSummaryRouteArgs> {
  InboxAssetSummaryRoute({
    Key? key,
    int? refresh,
    List<PageRouteInfo>? children,
  }) : super(
          InboxAssetSummaryRoute.name,
          args: InboxAssetSummaryRouteArgs(
            key: key,
            refresh: refresh,
          ),
          initialChildren: children,
        );

  static const String name = 'InboxAssetSummaryRoute';

  static const PageInfo<InboxAssetSummaryRouteArgs> page =
      PageInfo<InboxAssetSummaryRouteArgs>(name);
}

class InboxAssetSummaryRouteArgs {
  const InboxAssetSummaryRouteArgs({
    this.key,
    this.refresh,
  });

  final Key? key;

  final int? refresh;

  @override
  String toString() {
    return 'InboxAssetSummaryRouteArgs{key: $key, refresh: $refresh}';
  }
}

/// generated route for
/// [InboxPage]
class InboxRoute extends PageRouteInfo<void> {
  const InboxRoute({List<PageRouteInfo>? children})
      : super(
          InboxRoute.name,
          initialChildren: children,
        );

  static const String name = 'InboxRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [InstallationReportPage]
class InstallationReportRoute extends PageRouteInfo<void> {
  const InstallationReportRoute({List<PageRouteInfo>? children})
      : super(
          InstallationReportRoute.name,
          initialChildren: children,
        );

  static const String name = 'InstallationReportRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [LoginPage]
class LoginRoute extends PageRouteInfo<void> {
  const LoginRoute({List<PageRouteInfo>? children})
      : super(
          LoginRoute.name,
          initialChildren: children,
        );

  static const String name = 'LoginRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [MediaUploadPage]
class MediaUploadRoute extends PageRouteInfo<void> {
  const MediaUploadRoute({List<PageRouteInfo>? children})
      : super(
          MediaUploadRoute.name,
          initialChildren: children,
        );

  static const String name = 'MediaUploadRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [OverallAssetSummaryPage]
class OverallAssetSummaryRoute
    extends PageRouteInfo<OverallAssetSummaryRouteArgs> {
  OverallAssetSummaryRoute({
    Key? key,
    int? refresh,
    List<PageRouteInfo>? children,
  }) : super(
          OverallAssetSummaryRoute.name,
          args: OverallAssetSummaryRouteArgs(
            key: key,
            refresh: refresh,
          ),
          initialChildren: children,
        );

  static const String name = 'OverallAssetSummaryRoute';

  static const PageInfo<OverallAssetSummaryRouteArgs> page =
      PageInfo<OverallAssetSummaryRouteArgs>(name);
}

class OverallAssetSummaryRouteArgs {
  const OverallAssetSummaryRouteArgs({
    this.key,
    this.refresh,
  });

  final Key? key;

  final int? refresh;

  @override
  String toString() {
    return 'OverallAssetSummaryRouteArgs{key: $key, refresh: $refresh}';
  }
}

/// generated route for
/// [PdfViewerPage]
class PdfViewerRoute extends PageRouteInfo<PdfViewerRouteArgs> {
  PdfViewerRoute({
    required String path,
    Key? key,
    List<PageRouteInfo>? children,
  }) : super(
          PdfViewerRoute.name,
          args: PdfViewerRouteArgs(
            path: path,
            key: key,
          ),
          rawPathParams: {'path': path},
          initialChildren: children,
        );

  static const String name = 'PdfViewerRoute';

  static const PageInfo<PdfViewerRouteArgs> page =
      PageInfo<PdfViewerRouteArgs>(name);
}

class PdfViewerRouteArgs {
  const PdfViewerRouteArgs({
    required this.path,
    this.key,
  });

  final String path;

  final Key? key;

  @override
  String toString() {
    return 'PdfViewerRouteArgs{path: $path, key: $key}';
  }
}

/// generated route for
/// [SelectAssetTypePage]
class SelectAssetTypeRoute extends PageRouteInfo<void> {
  const SelectAssetTypeRoute({List<PageRouteInfo>? children})
      : super(
          SelectAssetTypeRoute.name,
          initialChildren: children,
        );

  static const String name = 'SelectAssetTypeRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SelectHealthFacilityPage]
class SelectHealthFacilityRoute extends PageRouteInfo<void> {
  const SelectHealthFacilityRoute({List<PageRouteInfo>? children})
      : super(
          SelectHealthFacilityRoute.name,
          initialChildren: children,
        );

  static const String name = 'SelectHealthFacilityRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SetupNewPasswordPage]
class SetupNewPasswordRoute extends PageRouteInfo<void> {
  const SetupNewPasswordRoute({List<PageRouteInfo>? children})
      : super(
          SetupNewPasswordRoute.name,
          initialChildren: children,
        );

  static const String name = 'SetupNewPasswordRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SpecificationPage]
class SpecificationRoute extends PageRouteInfo<void> {
  const SpecificationRoute({List<PageRouteInfo>? children})
      : super(
          SpecificationRoute.name,
          initialChildren: children,
        );

  static const String name = 'SpecificationRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SubmitForApprovalPage]
class SubmitForApprovalRoute extends PageRouteInfo<SubmitForApprovalRouteArgs> {
  SubmitForApprovalRoute({
    Key? key,
    int? refresh,
    List<PageRouteInfo>? children,
  }) : super(
          SubmitForApprovalRoute.name,
          args: SubmitForApprovalRouteArgs(
            key: key,
            refresh: refresh,
          ),
          initialChildren: children,
        );

  static const String name = 'SubmitForApprovalRoute';

  static const PageInfo<SubmitForApprovalRouteArgs> page =
      PageInfo<SubmitForApprovalRouteArgs>(name);
}

class SubmitForApprovalRouteArgs {
  const SubmitForApprovalRouteArgs({
    this.key,
    this.refresh,
  });

  final Key? key;

  final int? refresh;

  @override
  String toString() {
    return 'SubmitForApprovalRouteArgs{key: $key, refresh: $refresh}';
  }
}

/// generated route for
/// [SubmittedSaveSuccessPage]
class SubmittedSaveSuccessRoute extends PageRouteInfo<void> {
  const SubmittedSaveSuccessRoute({List<PageRouteInfo>? children})
      : super(
          SubmittedSaveSuccessRoute.name,
          initialChildren: children,
        );

  static const String name = 'SubmittedSaveSuccessRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SyncLoadingPage]
class SyncLoadingRoute extends PageRouteInfo<SyncLoadingRouteArgs> {
  SyncLoadingRoute({
    Key? key,
    required int completed,
    required int total,
    List<PageRouteInfo>? children,
  }) : super(
          SyncLoadingRoute.name,
          args: SyncLoadingRouteArgs(
            key: key,
            completed: completed,
            total: total,
          ),
          initialChildren: children,
        );

  static const String name = 'SyncLoadingRoute';

  static const PageInfo<SyncLoadingRouteArgs> page =
      PageInfo<SyncLoadingRouteArgs>(name);
}

class SyncLoadingRouteArgs {
  const SyncLoadingRouteArgs({
    this.key,
    required this.completed,
    required this.total,
  });

  final Key? key;

  final int completed;

  final int total;

  @override
  String toString() {
    return 'SyncLoadingRouteArgs{key: $key, completed: $completed, total: $total}';
  }
}

/// generated route for
/// [UnauthenticatedScreenWrapper]
class UnauthenticatedRouteWrapper extends PageRouteInfo<void> {
  const UnauthenticatedRouteWrapper({List<PageRouteInfo>? children})
      : super(
          UnauthenticatedRouteWrapper.name,
          initialChildren: children,
        );

  static const String name = 'UnauthenticatedRouteWrapper';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [VideoPlayerPage]
class VideoPlayerRoute extends PageRouteInfo<VideoPlayerRouteArgs> {
  VideoPlayerRoute({
    required String path,
    Key? key,
    List<PageRouteInfo>? children,
  }) : super(
          VideoPlayerRoute.name,
          args: VideoPlayerRouteArgs(
            path: path,
            key: key,
          ),
          initialChildren: children,
        );

  static const String name = 'VideoPlayerRoute';

  static const PageInfo<VideoPlayerRouteArgs> page =
      PageInfo<VideoPlayerRouteArgs>(name);
}

class VideoPlayerRouteArgs {
  const VideoPlayerRouteArgs({
    required this.path,
    this.key,
  });

  final String path;

  final Key? key;

  @override
  String toString() {
    return 'VideoPlayerRouteArgs{path: $path, key: $key}';
  }
}

/// generated route for
/// [WelcomePage]
class WelcomeRoute extends PageRouteInfo<void> {
  const WelcomeRoute({List<PageRouteInfo>? children})
      : super(
          WelcomeRoute.name,
          initialChildren: children,
        );

  static const String name = 'WelcomeRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}
