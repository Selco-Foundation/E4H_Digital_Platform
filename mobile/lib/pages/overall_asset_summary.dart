// lib/pages/overall_asset_summary.dart

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/asset_summary/asset_summary.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/selected_project/selected_project.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class OverallAssetSummaryPage extends StatefulWidget {
  const OverallAssetSummaryPage({Key? key}) : super(key: key);

  @override
  State<OverallAssetSummaryPage> createState() =>
      _OverallAssetSummaryPageState();
}

class _OverallAssetSummaryPageState extends State<OverallAssetSummaryPage> {
  @override
  void initState() {
    super.initState();

    // As soon as this page appears, grab the selected project ID and tell OverallAssetSummaryBloc to load counts.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final selState = context.read<SelectedProjectBloc>().state;
      selState.whenOrNull(selected: (project) {
        context.read<OverallAssetSummaryBloc>().add(
              OverallAssetSummaryEvent.loadCounts(projectId: project.id),
            );
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      // We want to listen for success/failure from AssetSubmissionBloc
      body: BlocConsumer<AssetSubmissionBloc, AssetSubmissionState>(
        listener: (context, assetSubmissionState) {
          assetSubmissionState.whenOrNull(
            success: () {
              // Show a snack bar and navigate to the success page
              context.showSnackBar(
                const SnackBar(
                    content: Text("All assets submitted successfully")),
              );
              context.router.replace(const SubmittedSaveSuccessRoute());
            },
            failure: (error) {
              context.showSnackBar(
                SnackBar(content: Text("Submission failed: ${error}")),
              );
            },
            loading: () {
              // You could show a fullscreen overlay, but here we simply do nothing
            },
            initial: () {},
          );
        },
        builder:
            (BuildContext context, AssetSubmissionState assetSubmissionState) {
          return ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),

            // ── FOOTER BUTTON ───────────────────────────────────────────────────
            footer:
                BlocBuilder<OverallAssetSummaryBloc, OverallAssetSummaryState>(
              builder: (context, overallState) {
                // Determine if any count is zero (or not loaded yet)
                bool isDisabled = true;
                int batteryCount = 0, inverterCount = 0, panelCount = 0;

                overallState.when(
                  initial: () {
                    batteryCount = 0;
                    inverterCount = 0;
                    panelCount = 0;
                  },
                  loading: () {
                    batteryCount = 0;
                    inverterCount = 0;
                    panelCount = 0;
                  },
                  error: (_) {
                    batteryCount = 0;
                    inverterCount = 0;
                    panelCount = 0;
                  },
                  loaded: (bCount, iCount, pCount) {
                    batteryCount = bCount;
                    inverterCount = iCount;
                    panelCount = pCount;
                  },
                );

                isDisabled = (batteryCount == 0 ||
                    inverterCount == 0 ||
                    panelCount == 0);

                // assetSubmissionState.whenOrNull(loading: () {
                //   return DigitButton(
                //     isDisabled: true,
                //     label: 'Submitting...',
                //     type: DigitButtonType.primary,
                //     onPressed: () {},
                //     size: DigitButtonSize.large,
                //     mainAxisSize: MainAxisSize.max,
                //   );
                // });

                return FooterButton(
                    showSuffixIcon: false,
                    text: assetSubmissionState.maybeWhen(
                        loading: () => 'Submitting...',
                        orElse: () => i18.common.coreCommonSubmit),
                    isDisabled: assetSubmissionState.maybeWhen(
                        loading: () => true, orElse: () => isDisabled),
                    onPress: assetSubmissionState.maybeWhen(
                        loading: () => () {},
                        orElse: () => () {
                              if (isDisabled) return;

                              // Pull in the current projectId
                              final selState =
                                  context.read<SelectedProjectBloc>().state;
                              selState.whenOrNull(selected: (project) {
                                // Now pull in whatever fields we have in AssetSummaryBloc
                                final summaryState =
                                    context.read<AssetSummaryBloc>().state;

                                summaryState.whenOrNull(
                                  loaded: (summary) {
                                    context.read<AssetSubmissionBloc>().add(
                                          AssetSubmissionEvent.submitAll(
                                            projectId: project.id,
                                            authToken: "<YOUR_AUTH_TOKEN_HERE>",
                                            tenantId: "pg",
                                            facilityId: "FAC/2025/000050",
                                            systemCode:
                                                summary.specEntry!.system,
                                            modelNumber:
                                                summary.detailEntry!.model,
                                            brandId: summary.detailEntry!.brand,
                                            totalCapacity: summary
                                                .specEntry!.totalCapacity,
                                            totalCapacityUnit: summary
                                                .specEntry!.totalCapacityUnit,
                                            panelCapacity: 34.1,
                                            capacityUnit: "",
                                            warrantyStartDate:
                                                DateTime.timestamp().toString(),
                                            warrantyDuration: 1,
                                            warrantyEndDate:
                                                "2027-01-20T00:00:00.000Z",
                                            additionalDetails: {},
                                          ),
                                        );
                                  },
                                );
                              });
                            }));
              },
            ),

            // ── MAIN CONTENT ────────────────────────────────────────────────────
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    vertical: spacer2, horizontal: spacer4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Summary',
                      style: textTheme.headingXl
                          .copyWith(color: theme.colorTheme.primary.primary2),
                    ),
                    const SizedBox(height: spacer4),

                    // ── BLOC BUILDER FOR THE COUNTS ─────────────────────────────────────
                    BlocBuilder<OverallAssetSummaryBloc,
                        OverallAssetSummaryState>(
                      builder: (context, state) {
                        return state.when(
                          initial: () {
                            return DigitCard(
                              children: [
                                const ElementAssetSummary(
                                  type: 'Battery',
                                  count: 0,
                                  text: 'batteries',
                                ),
                                const ElementAssetSummary(
                                  type: 'Inverter',
                                  count: 0,
                                  text: 'inverters',
                                ),
                                const ElementAssetSummary(
                                  type: 'Panel',
                                  count: 0,
                                  text: 'panels',
                                ),
                                const SizedBox(height: spacer6),
                                DigitButton(
                                  mainAxisSize: MainAxisSize.max,
                                  label: 'Add More Assets',
                                  prefixIcon: Icons.add_box,
                                  onPressed: () {}, // disabled
                                  type: DigitButtonType.primary,
                                  size: DigitButtonSize.medium,
                                ),
                              ],
                            );
                          },
                          loading: () {
                            return DigitCard(
                              children: [
                                const Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceEvenly,
                                  children: [
                                    ElementAssetSummary(
                                      type: 'Battery',
                                      count: 0,
                                      text: 'batteries',
                                    ),
                                    ElementAssetSummary(
                                      type: 'Inverter',
                                      count: 0,
                                      text: 'inverters',
                                    ),
                                    ElementAssetSummary(
                                      type: 'Panel',
                                      count: 0,
                                      text: 'panels',
                                    ),
                                  ],
                                ),
                                const SizedBox(height: spacer6),
                                const Center(
                                    child: CircularProgressIndicator()),
                                const SizedBox(height: spacer6),
                                DigitButton(
                                  mainAxisSize: MainAxisSize.max,
                                  label: 'Add More Assets',
                                  prefixIcon: Icons.add_box,
                                  onPressed: () {}, // still disabled
                                  type: DigitButtonType.primary,
                                  size: DigitButtonSize.medium,
                                ),
                              ],
                            );
                          },
                          error: (message) {
                            return DigitCard(
                              children: [
                                Center(
                                  child: Text(
                                    'Error loading counts:\n$message',
                                    style: textTheme.bodyL.copyWith(
                                        color: theme.colorTheme.alert.error),
                                    textAlign: TextAlign.center,
                                  ),
                                ),
                                const SizedBox(height: spacer6),
                                DigitButton(
                                  mainAxisSize: MainAxisSize.max,
                                  label: 'Retry',
                                  prefixIcon: Icons.refresh,
                                  onPressed: () {
                                    final selState = context
                                        .read<SelectedProjectBloc>()
                                        .state;
                                    selState.whenOrNull(selected: (project) {
                                      context
                                          .read<OverallAssetSummaryBloc>()
                                          .add(
                                            OverallAssetSummaryEvent.loadCounts(
                                              projectId: project.id,
                                            ),
                                          );
                                    });
                                  },
                                  type: DigitButtonType.primary,
                                  size: DigitButtonSize.medium,
                                ),
                              ],
                            );
                          },
                          loaded: (int batteryCount, int inverterCount,
                              int panelCount) {
                            return DigitCard(
                              children: [
                                ElementAssetSummary(
                                  type: 'Battery',
                                  count: batteryCount,
                                  text: 'batteries',
                                  onPress: () {
                                    context.read<AssetTypeBloc>().add(
                                        const AssetTypeEvent.typeSelected(
                                            "BATTERY"));
                                    context.router
                                        .push(const AssetSummaryRoute());
                                  },
                                ),
                                ElementAssetSummary(
                                  type: 'Inverter',
                                  count: inverterCount,
                                  text: 'inverters',
                                  onPress: () {
                                    context.read<AssetTypeBloc>().add(
                                        const AssetTypeEvent.typeSelected(
                                            "INVERTER"));
                                    context.router
                                        .push(const AssetSummaryRoute());
                                  },
                                ),
                                ElementAssetSummary(
                                  type: 'Panel',
                                  count: panelCount,
                                  text: 'panels',
                                  onPress: () {
                                    context.read<AssetTypeBloc>().add(
                                        const AssetTypeEvent.typeSelected(
                                            "PANEL"));
                                    context.router
                                        .push(const AssetSummaryRoute());
                                  },
                                ),
                                const SizedBox(height: spacer6),
                                DigitButton(
                                  mainAxisSize: MainAxisSize.max,
                                  label: 'Add More Assets',
                                  prefixIcon: Icons.add_box,
                                  onPressed: () {
                                    context.router
                                        .push(const SelectAssetTypeRoute());
                                  },
                                  type: DigitButtonType.primary,
                                  size: DigitButtonSize.medium,
                                ),
                              ],
                            );
                          },
                        );
                      },
                    ),

                    const SizedBox(height: spacer4),
                    DigitCard(
                      children: [
                        Text(
                          'Installation Completion Report',
                          style: textTheme.headingM.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const SizedBox(height: spacer8),
                        Text(
                          'Please scan and upload the installation completion report',
                          style: textTheme.bodyS
                              .copyWith(color: theme.colorTheme.text.secondary),
                        ),
                        const SizedBox(height: spacer8),
                        FileUploadWidget(
                          showPreview: true,
                          allowMultiples: false,
                          label: 'Upload',
                          onFilesSelected: (files) {
                            return <PlatformFile, String?>{};
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
      ),
    );
  }
}
