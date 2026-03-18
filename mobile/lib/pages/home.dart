import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/cache_sync_record/cache_sync_record.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/sync_popup_guard.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';
import '../widgets/mdms/mdms_gate.dart';
import 'sync_loading.dart';

@RoutePage()
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage>
    with AutoRouteAwareStateMixin<HomePage> {
  late String _userType;
  late String pendingRecords = "0";
  late String assignedFacility = "0";
  Route? _syncRoute;
  bool _popupShown = false;
  bool _routeActive = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.getNewlyAssigned(userType: _userType),
          );
      _refreshPendingSyncState();
    });
  }

  @override
  void didPush() {
    _routeActive = true;
    _refreshPendingSyncState();
  }

  @override
  void didPopNext() {
    _routeActive = true;
    _refreshPendingSyncState();
  }

  @override
  void didPushNext() {
    _routeActive = false;
  }

  @override
  void didPop() {
    _routeActive = false;
  }

  void _refreshPendingSyncState() {
    if (!mounted) return;
    context.read<CacheSyncRecordBloc>().add(CacheSyncRecordEvent.fetch(_userType));
  }

  void _maybeShowPendingSyncPopup(CacheSyncRecordState state) {
    if (!_routeActive || _popupShown || !mounted) return;

    final pendingCount = state.maybeWhen(
      loaded: (_, pendingCount) => pendingCount,
      notFound: (pendingCount) => pendingCount,
      orElse: () => 0,
    );

    if (pendingCount <= 0) return;
    if (SyncPopupGuard.consumeSuppression()) return;

    _popupShown = true;
    _showPopup(context);
  }

  void _showSyncDialog(BuildContext context, {String? error}) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    showCustomPopup(
      context: context,
      builder: (ctx) => Popup(
        type: PopUpType.alert,
        onCrossTap: () => Navigator.of(ctx).pop(),
        onOutsideTap: () => Navigator.of(ctx).pop(),
        title: "Sync Failed",
        actionAlignment: MainAxisAlignment.center,
        actions: const [],
        additionalWidgets: [
          Text(
            error ?? "Something went wrong.",
            textAlign: TextAlign.center,
            style: textTheme.bodyL.copyWith(
              color: theme.colorTheme.text.primary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  void _handleAssetSubmissionState(
      BuildContext context, AssetSubmissionState state) {
    state.whenOrNull(
      bulkProgress: (_) {
        if (_syncRoute == null) {
          _syncRoute = MaterialPageRoute(
            fullscreenDialog: true,
            builder: (_) => const SyncLoadingPage(),
          );
          Navigator.of(context).push(_syncRoute!);
        }
      },
      bulkFailure: (errorMessage) {
        if (_syncRoute != null) {
          Navigator.of(context).pop();
          _syncRoute = null;
        }

        if (isSessionExpiredMessage(errorMessage)) {
          handleSessionExpired(context);
          return;
        }
        _showSyncDialog(context, error: errorMessage);
      },
      success: () {
        if (_syncRoute != null) {
          Navigator.of(context).pop();
          _syncRoute = null;
        }
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('All drafts successfully synced!')),
        );
        _popupShown = false;
        _refreshPendingSyncState();
      },
    );
  }

  void _showPopup(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    showCustomPopup(
      context: context,
      builder: (ctx) => BlocBuilder<CacheSyncRecordBloc, CacheSyncRecordState>(
        builder: (context, state) {
          String description = state.maybeWhen(
            loaded: (record, pending) {
              final dt = record.syncedAt;
              final formatted = "${dt.day.toString().padLeft(2, '0')}/"
                  "${dt.month.toString().padLeft(2, '0')}/"
                  "${dt.year}";
              return "Your data was last synced on $formatted.";
            },
            loading: () => "---",
            orElse: () => "Your data has not been synced. Sync now!",
          );
          return Popup(
            type: PopUpType.alert,
            onCrossTap: () => Navigator.of(ctx).pop(),
            onOutsideTap: () => Navigator.of(ctx).pop(),
            title: "Data not synced!",
            actionAlignment: MainAxisAlignment.center,
            actions: const [],
            additionalWidgets: [
              Column(
                children: [
                  Text(
                    description,
                    textAlign: TextAlign.center,
                    style: textTheme.bodyL.copyWith(
                      color: const Light().textPrimary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: spacer4),
                  Row(
                    children: [
                      Expanded(
                        child: DigitButton(
                          label: "Skip",
                          onPressed: () {
                            _popupShown = false;
                            Navigator.of(ctx).pop();
                          },
                          type: DigitButtonType.secondary,
                          size: DigitButtonSize.large,
                        ),
                      ),
                      const SizedBox(width: spacer5),
                      Expanded(
                        child: DigitButton(
                          label: "Sync Data",
                          onPressed: () {
                            _popupShown = false;
                            Navigator.of(ctx).pop();
                            context.read<AssetSubmissionBloc>().add(
                                AssetSubmissionEvent.submitAllDrafts(
                                    userType: _userType));
                          },
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.large,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final screenWidth = context.width;

    final List<Map<String, dynamic>> homeItems = [
      {
        'icon': Icons.text_snippet_outlined,
        'label': 'Installation Report',
        'onPressed': () => context.router.push(const InstallationReportRoute()),
      },
      {
        'icon': Icons.autorenew,
        'label': 'Data Sync',
        'onPressed': () {
          context
              .read<AssetSubmissionBloc>()
              .add(AssetSubmissionEvent.submitAllDrafts(userType: _userType));
        },
      },
    ];

    return BlocListener<AssetSubmissionBloc, AssetSubmissionState>(
      listener: _handleAssetSubmissionState,
      child: BlocListener<CacheSyncRecordBloc, CacheSyncRecordState>(
        listener: (context, state) => _maybeShowPendingSyncPopup(state),
        child: Stack(
          children: [
            const MdmsGate(),
            Scaffold(
              backgroundColor: DigitTheme.instance.colorScheme.surface,
              body: Padding(
                padding: const EdgeInsets.symmetric(horizontal: spacer2),
                child: ScrollableContent(
                  backgroundColor: theme.colorTheme.generic.background,
                  header: const BackNavigationHelpHeaderWidget(
                    showBackNavigation: false,
                    showHelp: true,
                  ),
                  footer: const PoweredByDigit(version: ''),
                  slivers: [
                    SliverPadding(
                      padding: const EdgeInsets.only(top: spacer6),
                      sliver: SliverGrid(
                        delegate: SliverChildBuilderDelegate(
                          (context, index) {
                            final item = homeItems[index];
                            return HomeItemCard(
                              icon: item['icon'],
                              label: item['label'],
                              onPressed: item['onPressed'],
                            );
                          },
                          childCount: homeItems.length,
                        ),
                        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          mainAxisSpacing: spacer4,
                          childAspectRatio:
                              (screenWidth / 2) / (170 * (screenWidth / 375)),
                        ),
                      ),
                    ),
                  ],
                  children: [
                    Padding(
                      padding: const EdgeInsets.only(
                          top: spacer2, left: spacer2, right: spacer2),
                      child: Column(
                        children: [
                          BlocBuilder<CacheSyncRecordBloc, CacheSyncRecordState>(
                            builder: (context, state) {
                              pendingRecords = state.maybeWhen(
                                loaded: (record, pending) => pending.toString(),
                                loading: () => "---",
                                notFound: (val) => "$val",
                                orElse: () => "---",
                              );
                              return InfoCard(
                                title: "Data Sync Pending!",
                                type: InfoType.warning,
                                description:
                                    'There are $pendingRecords record${pendingRecords == '1' ? '' : 's'} yet to be synced',
                              );
                            },
                          ),
                          const SizedBox(height: spacer3),
                          BlocBuilder<ActivityFacilityBloc,
                              ActivityFacilityState>(
                            builder: (context, state) {
                              assignedFacility = state.maybeWhen(
                                newlyAssignedLoaded: (count) => "$count",
                                orElse: () => "0",
                              );
                              return InfoCard(
                                title: "Facilities assigned",
                                type: InfoType.info,
                                description:
                                    '$assignedFacility more facilit${assignedFacility == '1' ? 'y' : 'ies'} have been assigned to you.',
                              );
                            },
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
