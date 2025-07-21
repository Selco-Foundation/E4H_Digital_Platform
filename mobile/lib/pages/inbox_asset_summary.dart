import 'dart:async';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset/cache_asset.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../repositories/project_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/utils.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class InboxAssetSummaryPage extends StatefulWidget {
  const InboxAssetSummaryPage({super.key});

  @override
  State<InboxAssetSummaryPage> createState() => _InboxAssetSummaryPageState();
}

class _InboxAssetSummaryPageState extends State<InboxAssetSummaryPage> {
  late String userType = "";

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
      context.read<SelectedProjectBloc>().state.whenOrNull(selected: (proj) {
        context
            .read<CacheAssetBloc>()
            .add(CacheAssetEvent.start(proj.project.id, userType, proj));
      });
    });
  }

  Future<void> _sendBackReport(BuildContext popupCtx) async {
    // 1. Dismiss the confirmation popup
    Navigator.of(popupCtx).pop();

    // 2. Grab projectId
    final projectId = context
        .read<SelectedProjectBloc>()
        .state
        .whenOrNull(selected: (p) => p.project.id);
    if (projectId == null) {
      context.showSnackBar(
        const SnackBar(content: Text("No project selected")),
      );
      return;
    }

    // 3. Show a full‐screen loading spinner and capture its BuildContext
    BuildContext? dialogCtx;
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) {
        dialogCtx = ctx;
        return const Center(child: CircularProgressIndicator());
      },
    );

    try {
      final repo = ProjectRemoteRepository();
      await repo.updateProjectWorkflow(
        projectId: projectId,
        action: userType == USER_TYPES.SUPERVISOR.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name,
      );

      // 5. On success: pop the spinner only
      if (dialogCtx != null && mounted) {
        Navigator.of(dialogCtx!).pop();
      }

      context.showSnackBar(
        const SnackBar(content: Text("Report sent back successfully")),
      );
      context.router.popAndPush(const InboxRoute());
    } catch (e) {
      if (dialogCtx != null && mounted) {
        Navigator.of(dialogCtx!).pop();
      }
      context.showSnackBar(
        SnackBar(content: Text("Failed to send back: $e")),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: BlocConsumer<CacheAssetBloc, CacheAssetState>(
        listener: (context, cacheState) {
          cacheState.whenOrNull(
            success: () {
              final pid = context
                  .read<SelectedProjectBloc>()
                  .state
                  .whenOrNull(selected: (p) => p.project.id);
              if (pid != null) {
                context.read<OverallAssetSummaryBloc>().add(
                      OverallAssetSummaryEvent.loadCounts(projectId: pid),
                    );
              }
            },
            failure: (error) {
              context.showSnackBar(
                SnackBar(content: Text("Sync failed: $error")),
              );
            },
          );
        },
        builder: (context, cacheState) {
          final isSyncing =
              cacheState.maybeWhen(loading: () => true, orElse: () => false);

          return BlocBuilder<OverallAssetSummaryBloc, OverallAssetSummaryState>(
            builder: (context, summaryState) {
              final isSummaryLoading = summaryState.maybeWhen(
                  loading: () => true, orElse: () => false);

              if (isSyncing || isSummaryLoading) {
                return const Center(child: CircularProgressIndicator());
              }

              final errorMessage = summaryState.maybeWhen(
                error: (msg) => msg,
                orElse: () => null,
              );
              if (errorMessage != null) {
                return Center(child: Text("Error: $errorMessage"));
              }

              int battery = 0, inverter = 0, panel = 0;
              summaryState.maybeWhen(
                loaded: (b, i, p) {
                  battery = b;
                  inverter = i;
                  panel = p;
                },
                orElse: () {},
              );

              return BlocBuilder<InboxTypeBloc, InboxTypeState>(
                builder: (context, inboxState) {
                  return ScrollableContent(
                    enableFixedDigitButton: true,
                    backgroundColor: theme.colorTheme.generic.background,
                    header: const BackNavigationHelpHeaderWidget(
                      showBackNavigation: true,
                      showHelp: false,
                    ),
                    footer: inboxState.maybeWhen(
                      approved: () => const SizedBox.shrink(),
                      orElse: () => DigitCard(
                        margin: const EdgeInsets.only(top: spacer2),
                        children: [
                          DigitButton(
                            mainAxisSize: MainAxisSize.max,
                            label: "Add more details",
                            type: DigitButtonType.primary,
                            size: DigitButtonSize.large,
                            onPressed: () {
                              context.read<ReportTypeBloc>().add(
                                  const ReportTypeEvent.typeSelected("inbox"));
                              context.router.push(const AssetCountRoute());
                            },
                          ),
                          DigitButton(
                            mainAxisSize: MainAxisSize.max,
                            label: "Send Back",
                            type: DigitButtonType.secondary,
                            size: DigitButtonSize.large,
                            onPressed: () => showCustomPopup(
                              context: context,
                              builder: (ctx) => Popup(
                                onCrossTap: () => Navigator.of(ctx).pop(),
                                title:
                                    "Are you sure you want to send back the report?",
                                description:
                                    "If you send back the report now, you cannot add any more rejection reasons or add more details until it is sent back from the field",
                                type: PopUpType.simple,
                                actionAlignment: MainAxisAlignment.center,
                                actions: [],
                                additionalWidgets: [
                                  Row(
                                    children: [
                                      Expanded(
                                        flex: 1,
                                        child: DigitButton(
                                          label: "Close",
                                          onPressed: () {
                                            Navigator.of(ctx).pop();
                                          },
                                          type: DigitButtonType.secondary,
                                          size: DigitButtonSize.large,
                                          mainAxisSize: MainAxisSize.min,
                                        ),
                                      ),
                                      const SizedBox(width: spacer5),
                                      Expanded(
                                        flex: 1,
                                        child: DigitButton(
                                          label: "Send back",
                                          onPressed: () => _sendBackReport(ctx),
                                          type: DigitButtonType.primary,
                                          size: DigitButtonSize.large,
                                          mainAxisSize: MainAxisSize.min,
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    children: [
                      Padding(
                        padding: const EdgeInsets.symmetric(
                            vertical: spacer2, horizontal: spacer4),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Summary Overview',
                              style: textTheme.headingXl.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                            const SizedBox(height: spacer4),
                            DigitCard(
                              children: [
                                ElementAssetSummary(
                                  count: battery,
                                  text: 'Batteries',
                                  onPress: () {
                                    context.read<AssetTypeBloc>().add(
                                        const AssetTypeEvent.typeSelected(
                                            "BATTERY"));

                                    inboxState.maybeWhen(
                                        rejected: () => {
                                              context
                                                  .read<ReportTypeBloc>()
                                                  .add(const ReportTypeEvent
                                                      .typeSelected(
                                                      "send-back"))
                                            },
                                        orElse: () {});
                                    context.router
                                        .push(const AssetSummaryRoute());
                                  },
                                ),
                                ElementAssetSummary(
                                  count: inverter,
                                  text: 'Inverters',
                                  onPress: () {
                                    context.read<AssetTypeBloc>().add(
                                        const AssetTypeEvent.typeSelected(
                                            "INVERTER"));
                                    inboxState.maybeWhen(
                                        rejected: () => {
                                              context
                                                  .read<ReportTypeBloc>()
                                                  .add(const ReportTypeEvent
                                                      .typeSelected(
                                                      "send-back"))
                                            },
                                        orElse: () {});
                                    context.router
                                        .push(const AssetSummaryRoute());
                                  },
                                ),
                                ElementAssetSummary(
                                  count: panel,
                                  text: 'Panels',
                                  lastCard: true,
                                  onPress: () {
                                    context.read<AssetTypeBloc>().add(
                                        const AssetTypeEvent.typeSelected(
                                            "PANEL"));
                                    inboxState.maybeWhen(
                                        rejected: () => {
                                              context
                                                  .read<ReportTypeBloc>()
                                                  .add(const ReportTypeEvent
                                                      .typeSelected(
                                                      "send-back"))
                                            },
                                        orElse: () {});
                                    context.router
                                        .push(const AssetSummaryRoute());
                                  },
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  );
                },
              );
            },
          );
        },
      ),
    );
  }
}
