import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/utils/extensions.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset/cache_asset.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../router/app_router.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class InboxAssetSummaryPage extends StatefulWidget {
  const InboxAssetSummaryPage({super.key});

  @override
  State<InboxAssetSummaryPage> createState() => _InboxAssetSummaryPageState();
}

class _InboxAssetSummaryPageState extends State<InboxAssetSummaryPage> {
  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final selState = context.read<SelectedProjectBloc>().state;
      selState.whenOrNull(selected: (project) {
        context.read<CacheAssetBloc>().add(
              CacheAssetEvent.start(project.project.id),
            );
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    final selectedProjectState = context.watch<SelectedProjectBloc>().state;
    final projectId = selectedProjectState.whenOrNull(
      selected: (project) => project.project.id,
    );

    return Scaffold(
      body: BlocConsumer<CacheAssetBloc, CacheAssetState>(
        listener: (context, cacheState) {
          cacheState.whenOrNull(
            success: () {
              if (projectId != null) {
                context.read<OverallAssetSummaryBloc>().add(
                      OverallAssetSummaryEvent.loadCounts(projectId: projectId),
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
              // Show loader while syncing or summary is loading
              final isSummaryLoading = summaryState.maybeWhen(
                loading: () => true,
                orElse: () => false,
              );

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

              return ScrollableContent(
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                footer: BlocBuilder<InboxTypeBloc, InboxTypeState>(
                  builder: (context, state) {
                    return state.maybeWhen(
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
                                          const ReportTypeEvent.typeSelected(
                                              "inbox"));
                                      context.router
                                          .push(const AssetCountRoute());
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
                                        onCrossTap: () {
                                          Navigator.of(ctx).pop();
                                        },
                                        title:
                                            "Are you sure you want send to back the report?",
                                        description:
                                            "If you send back the report now, you cannot add any more rejection reasons or add more details to the report until it is sent back from the field",
                                        onOutsideTap: () {
                                          Navigator.of(ctx).pop();
                                        },
                                        type: PopUpType.simple,
                                        actionAlignment:
                                            MainAxisAlignment.center,
                                        actions: [],
                                        additionalWidgets: [
                                          Row(
                                            crossAxisAlignment:
                                                CrossAxisAlignment.center,
                                            children: [
                                              Expanded(
                                                flex: 1,
                                                child: DigitButton(
                                                  label: "Close",
                                                  onPressed: () {
                                                    Navigator.of(ctx).pop();
                                                  },
                                                  type:
                                                      DigitButtonType.secondary,
                                                  size: DigitButtonSize.large,
                                                  mainAxisSize:
                                                      MainAxisSize.min,
                                                ),
                                              ),
                                              const SizedBox(width: spacer5),
                                              Expanded(
                                                flex: 1,
                                                child: DigitButton(
                                                  label: "Send back",
                                                  onPressed: () {
                                                    Navigator.of(ctx).pop();
                                                    context
                                                        .read<AssetTypeBloc>()
                                                        .add(
                                                            const AssetTypeEvent
                                                                .typeSelected(
                                                                "inverter"));
                                                    context
                                                        .read<ReportTypeBloc>()
                                                        .add(
                                                            const ReportTypeEvent
                                                                .typeSelected(
                                                                "send-back"));
                                                    context.router.push(
                                                        const AssetSummaryRoute());
                                                  },
                                                  type: DigitButtonType.primary,
                                                  size: DigitButtonSize.large,
                                                  mainAxisSize:
                                                      MainAxisSize.min,
                                                ),
                                              ),
                                            ],
                                          ),
                                        ],
                                      ),
                                    ),
                                  ),
                                ]));
                  },
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
                                  context.read<ReportTypeBloc>().add(
                                      const ReportTypeEvent.typeSelected(
                                          "send-back"));
                                  context.router
                                      .push(const AssetSummaryRoute());
                                }),
                            ElementAssetSummary(
                              count: inverter,
                              text: 'Inverters',
                              onPress: () {
                                context.read<AssetTypeBloc>().add(
                                    const AssetTypeEvent.typeSelected(
                                        "INVERTER"));
                                context.read<ReportTypeBloc>().add(
                                    const ReportTypeEvent.typeSelected(
                                        "send-back"));
                                context.router.push(const AssetSummaryRoute());
                              },
                            ),
                            ElementAssetSummary(
                              count: panel,
                              text: 'Panels',
                              lastCard: true,
                              onPress: () {
                                context.read<AssetTypeBloc>().add(
                                    const AssetTypeEvent.typeSelected("PANEL"));
                                context.read<ReportTypeBloc>().add(
                                    const ReportTypeEvent.typeSelected(
                                        "send-back"));
                                context.router.push(const AssetSummaryRoute());
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
      ),
    );
  }
}
