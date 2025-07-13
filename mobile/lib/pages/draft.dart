import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/project/project.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class DraftPage extends StatefulWidget {
  const DraftPage({super.key});

  @override
  State<DraftPage> createState() => _DraftPageState();
}

class _DraftPageState extends State<DraftPage> {
  late String userType = "";

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
      context.read<ProjectBloc>().add(
            ProjectEvent.loadUnSubmitted(
              [
                userType == USER_TYPES.FIELD_STAFF.name
                    ? WORKFLOW_STATUS_FIELD_STAFF.SUBMITTED_BY_FIELD_STAFF.name
                    : WORKFLOW_STATUS_FIELD_SUPERVISOR
                        .ASSIGNED_TO_FIELD_SUPERVISOR.name
              ],
              userType,
            ),
          );
    });
  }

  void _showSyncDialog(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    showCustomPopup(
      context: context,
      builder: (ctx) => BlocBuilder<AssetSubmissionBloc, AssetSubmissionState>(
        builder: (context, state) {
          return Popup(
            type: PopUpType.alert,
            onCrossTap: () => Navigator.of(ctx).pop(),
            onOutsideTap: () => Navigator.of(ctx).pop(),
            titleIcon: null,
            title: state.maybeWhen(
              progress: (_, __) => "Syncing...",
              failure: (_) => "Sync Failed",
              success: () => "Sync Complete",
              orElse: () => "Sync",
            ),
            actionAlignment: MainAxisAlignment.center,
            actions: [],
            additionalWidgets: [
              if (state.maybeWhen(
                progress: (_, __) => true,
                orElse: () => false,
              ))
                Builder(
                  builder: (_) {
                    final (completed, total) = state.maybeMap(
                      progress: (p) => (p.completed, p.total),
                      orElse: () => (0, 1),
                    );
                    final percent =
                        ((completed / total) * 100).clamp(0, 100).toInt();

                    return Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        LinearProgressIndicator(
                          value: completed / total,
                          minHeight: 8,
                          backgroundColor: theme.colorTheme.text.primary,
                          color: theme.colorTheme.primary.primary1,
                        ),
                        const SizedBox(height: spacer2),
                        Text(
                          '$percent% completed',
                          style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.primary.primary1,
                          ),
                        ),
                      ],
                    );
                  },
                ),
              if (state.maybeWhen(
                failure: (_) => true,
                orElse: () => false,
              ))
                Text(
                  state.maybeMap(
                    failure: (f) => f.errorMessage,
                    orElse: () => '',
                  ),
                  textAlign: TextAlign.center,
                  style: textTheme.bodyL.copyWith(
                    color: theme.colorTheme.text.primary,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              if (state.maybeWhen(success: () => true, orElse: () => false))
                Text(
                  "All drafts successfully synced.",
                  textAlign: TextAlign.center,
                  style: textTheme.bodyL.copyWith(
                    color: theme.colorTheme.text.primary,
                    fontWeight: FontWeight.w600,
                  ),
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
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
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
            _showSyncDialog(context);
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
                BlocBuilder<ProjectBloc, ProjectState>(
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
                                    context.read<SelectedProjectBloc>().add(
                                        SelectedProjectEvent.select(project));
                                    context.router
                                        .push(const OverallAssetSummaryRoute());
                                  },
                                  title: project.project.name ?? "",
                                  dateAssigned: project.project.startDateTime ??
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
    );
  }
}
