import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import 'sync_loading.dart';

@RoutePage()
class DraftPage extends StatefulWidget {
  const DraftPage({super.key});

  @override
  State<DraftPage> createState() => _DraftPageState();
}

class _DraftPageState extends State<DraftPage> {
  static const _pageSize = 10;
  static const _scrollThreshold = 200.0;

  late String userType = "";
  Route? _syncRoute;
  int _visibleCount = _pageSize;
  bool _isLoadingMore = false;

  List<String> _draftStatusesForUserType() {
    return userType == USER_TYPES.FIELD_STAFF.name
        ? [WORKFLOW_STATUS_FIELD_STAFF.SUBMITTED_BY_FIELD_STAFF.name]
        : [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
            WORKFLOW_STATUS_FIELD_SUPERVISOR.PENDING_APPROVAL_FLAGGED_FOR_QC.name,
          ];
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.loadUnSubmitted(
              _draftStatusesForUserType(),
              userType,
            ),
          );
    });
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
        context.read<ActivityFacilityBloc>().add(
              ActivityFacilityEvent.loadUnSubmitted(
                _draftStatusesForUserType(),
                userType,
              ),
            );
      },
    );
  }

  void _handleDraftState(ActivityFacilityState state) {
    state.maybeWhen(
      unSubmittedLoaded: (drafts) {
        setState(() {
          _visibleCount = drafts.length < _pageSize ? drafts.length : _pageSize;
          _isLoadingMore = false;
        });
      },
      orElse: () {},
    );
  }

  void _loadMoreDrafts() {
    if (_isLoadingMore) return;

    final state = context.read<ActivityFacilityBloc>().state;
    state.maybeWhen(
      unSubmittedLoaded: (drafts) {
        if (_visibleCount >= drafts.length) return;
        setState(() => _isLoadingMore = true);
        Future<void>.delayed(const Duration(milliseconds: 300), () {
          if (!mounted) return;
          setState(() {
            _visibleCount =
                (_visibleCount + _pageSize).clamp(0, drafts.length).toInt();
            _isLoadingMore = false;
          });
        });
      },
      orElse: () {},
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return MultiBlocListener(
      listeners: [
        BlocListener<AssetSubmissionBloc, AssetSubmissionState>(
          listener: _handleAssetSubmissionState,
        ),
        BlocListener<ActivityFacilityBloc, ActivityFacilityState>(
          listener: (context, state) => _handleDraftState(state),
        ),
      ],
      child: NotificationListener<ScrollNotification>(
        onNotification: (notification) {
          if (notification is ScrollUpdateNotification) {
            final max = notification.metrics.maxScrollExtent;
            final current = notification.metrics.pixels;
            if (current > max - _scrollThreshold) {
              _loadMoreDrafts();
            }
          }
          return false;
        },
        child: Scaffold(
          body: ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            footer: FooterButton(
              showSuffixIcon: false,
              text: 'Sync',
              onPress: () {
                context.read<AssetSubmissionBloc>().add(
                      AssetSubmissionEvent.submitAllDrafts(userType: userType),
                    );
              },
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                  vertical: spacer4,
                  horizontal: spacer4,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Submitted Reports',
                      style: textTheme.headingXl.copyWith(
                        color: theme.colorTheme.primary.primary2,
                      ),
                    ),
                    const SizedBox(height: spacer4),
                    BlocBuilder<ActivityFacilityBloc, ActivityFacilityState>(
                      builder: (context, state) {
                        return state.maybeWhen(
                          unSubmittedLoaded: (drafts) {
                            if (drafts.isEmpty) {
                              return Center(
                                child: Text(
                                  'No unsynced reports found.',
                                  style: textTheme.bodyL.copyWith(
                                    color: theme.colorTheme.text.primary,
                                  ),
                                ),
                              );
                            }

                            final visibleDrafts = drafts.take(_visibleCount);
                            return Column(
                              children: [
                                ...visibleDrafts.map((project) {
                                  final locality = parseBoundaryCodeLocality(
                                    project.activityFacility.facility
                                        ?.boundaryCode,
                                  );
                                  return Column(
                                    children: [
                                      InboxReportCard(
                                        onPress: () {
                                          context
                                              .read<
                                                  SelectedActivityFacilityBloc>()
                                              .add(SelectedActivityFacilityEvent
                                                  .select(project));
                                          context.router.push(
                                              OverallAssetSummaryRoute(
                                                  refresh: DateTime.now()
                                                      .millisecondsSinceEpoch));
                                        },
                                        title: project.activityFacility.facility
                                                ?.facilityName ??
                                            "",
                                        dateAssigned: project
                                                .workflow
                                                ?.auditDetails
                                                ?.lastModifiedTime ??
                                            DateTime.now(),
                                        status: project.status ?? '---',
                                        state: locality.state,
                                        district: locality.district,
                                        block: locality.block,
                                      ),
                                      const SizedBox(height: spacer6),
                                    ],
                                  );
                                }),
                                if (_isLoadingMore)
                                  const Padding(
                                    padding: EdgeInsets.only(bottom: spacer4),
                                    child: CircularProgressIndicator(),
                                  ),
                              ],
                            );
                          },
                          initial: () => const Center(
                            child: Padding(
                              padding: EdgeInsets.only(top: spacer4),
                              child: CircularProgressIndicator(),
                            ),
                          ),
                          orElse: () => const Center(
                            child: Padding(
                              padding: EdgeInsets.only(top: spacer4),
                              child: CircularProgressIndicator(),
                            ),
                          ),
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
    );
  }
}
