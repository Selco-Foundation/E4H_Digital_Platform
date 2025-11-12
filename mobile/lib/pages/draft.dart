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
  late String userType = "";
  Route? _syncRoute;

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
              userType == USER_TYPES.FIELD_STAFF.name
                  ? [WORKFLOW_STATUS_FIELD_STAFF.SUBMITTED_BY_FIELD_STAFF.name]
                  : [
                      WORKFLOW_STATUS_FIELD_SUPERVISOR
                          .SUBMITTED_BY_SUPERVISOR.name,
                      WORKFLOW_STATUS_FIELD_SUPERVISOR
                          .PENDING_APPROVAL_FLAGGED_FOR_QC.name
                    ],
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
        context.read<ActivityFacilityBloc>().add(
              ActivityFacilityEvent.loadUnSubmitted(
                [
                  userType == USER_TYPES.FIELD_STAFF.name
                      ? WORKFLOW_STATUS_FIELD_STAFF
                          .SUBMITTED_BY_FIELD_STAFF.name
                      : WORKFLOW_STATUS_FIELD_SUPERVISOR
                          .SUBMITTED_BY_SUPERVISOR.name,
                ],
                userType,
              ),
            );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<AssetSubmissionBloc, AssetSubmissionState>(
      listener: _handleAssetSubmissionState,
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
                          return Column(
                            children: drafts.map((project) {
                              return Column(
                                children: [
                                  InboxReportCard(
                                    onPress: () {
                                      context
                                          .read<SelectedActivityFacilityBloc>()
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
                                    dateAssigned: project.workflow?.auditDetails
                                            ?.lastModifiedTime ??
                                        DateTime.now(),
                                    status: project.status ?? '---',
                                  ),
                                  const SizedBox(height: spacer6),
                                ],
                              );
                            }).toList(),
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
    );
  }
}
