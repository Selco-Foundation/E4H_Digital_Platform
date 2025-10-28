import 'dart:async';

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

class _HomePageState extends State<HomePage> {
  late String _userType;
  late String pendingRecords = "0";
  late String assignedFacility = "0";
  Route? _syncRoute;
  StreamSubscription<CacheSyncRecordState>? _syncSub;
  bool _popupShown = false;

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
      context
          .read<CacheSyncRecordBloc>()
          .add(CacheSyncRecordEvent.fetch(_userType));
      //_showPopup(context);

      // REMOVE this line:
      // _showPopup(context);

      // ADD this listener (one-shot)
      _syncSub = context.read<CacheSyncRecordBloc>().stream.listen((state) {
        if (_popupShown) return; // guard against repeats

        state.maybeWhen(
          loaded: (_, pending) {
            if (pending != null && pending > 0) {
              _popupShown = true;
              _showPopup(context);
            }
          },
          notFound: (val) {
            // If your bloc uses notFound(0) when nothing to sync, do nothing.
            // If val > 0 you can also choose to show the popup:
            if (val != null && val > 0) {
              _popupShown = true;
              _showPopup(context);
            }
          },
          orElse: () {},
        );
      });
    });
  }

  @override
  void dispose() {
    _syncSub?.cancel();
    super.dispose();
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
        actions: [],
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
      progress: (completed, total) {
        if (_syncRoute == null) {
          _syncRoute = MaterialPageRoute(
            fullscreenDialog: true,
            builder: (_) => SyncLoadingPage(completed: completed, total: total),
          );
          Navigator.of(context).push(_syncRoute!);
        }
      },
      failure: (errorMessage) {
        if (_syncRoute != null) {
          Navigator.of(context).pop();
          _syncRoute = null;
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
        // Optionally refresh your cache sync record or project list here
        context
            .read<CacheSyncRecordBloc>()
            .add(CacheSyncRecordEvent.fetch(_userType));
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
            actions: [],
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
                          onPressed: () => Navigator.of(ctx).pop(),
                          type: DigitButtonType.secondary,
                          size: DigitButtonSize.large,
                        ),
                      ),
                      const SizedBox(width: spacer5),
                      Expanded(
                        child: DigitButton(
                          label: "Sync Data",
                          onPressed: () {
                            Navigator.of(ctx).pop();
                            // Kick off the same sync flow
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

    final List<Map<String, dynamic>> _homeItems = [
      {
        'icon': Icons.text_snippet_outlined,
        'label': 'Installation Report',
        'onPressed': () => context.router.push(const InstallationReportRoute()),
      },
      {
        'icon': Icons.autorenew,
        'label': 'Data Sync',
        'onPressed': () {
          // Directly start sync
          context
              .read<AssetSubmissionBloc>()
              .add(AssetSubmissionEvent.submitAllDrafts(userType: _userType));
        },
      },
    ];

    return BlocListener<AssetSubmissionBloc, AssetSubmissionState>(
      listener: _handleAssetSubmissionState,
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
                          final item = _homeItems[index];
                          return HomeItemCard(
                            icon: item['icon'],
                            label: item['label'],
                            onPressed: item['onPressed'],
                          );
                        },
                        childCount: _homeItems.length,
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
    );
  }
}
