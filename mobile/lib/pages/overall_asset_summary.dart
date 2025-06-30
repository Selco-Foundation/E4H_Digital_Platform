// lib/pages/overall_asset_summary.dart

import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/data/nosql/cache_completion_report.dart';
import 'package:selco/utils/utils.dart';

import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/asset_summary/asset_summary.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_completion_report/cache_completion_report.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
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
  String? filePath = "";
  String? _currentProjectId;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;

  @override
  void initState() {
    super.initState();
    final locBloc = context.read<LocationBloc>();
    locBloc.add(const LocationEvent.requestPermission());
    locBloc.add(const LocationEvent.requestService());
    // 2. Listen to updates so we keep _latitude/_longitude up to date:
    _locSub = locBloc.stream.listen((locationState) {
      if (locationState.latitude != null && locationState.longitude != null) {
        setState(() {
          _latitude = locationState.latitude;
          _longitude = locationState.longitude;
        });
      }
    });
    // As soon as this page appears, grab the selected project ID and tell OverallAssetSummaryBloc to load counts.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final selState = context.read<SelectedProjectBloc>().state;
      selState.whenOrNull(selected: (project) {
        _currentProjectId = project.project.id;
        context.read<OverallAssetSummaryBloc>().add(
              OverallAssetSummaryEvent.loadCounts(
                  projectId: project.project.id),
            );
      });
    });
  }

  void _handleUpload(PlatformFile platformFile) async {
    final file = File(platformFile.path!);
    final copiedPath = await copyFileToLocalDir(file);
    setState(() {
      filePath = copiedPath;
    });
    debugPrint("filePath: $filePath");
  }

  @override
  void dispose() {
    _locSub?.cancel();
    super.dispose();
  }

  Future<bool> _ensureLocationLoaded(
      {Duration timeout = const Duration(seconds: 10)}) async {
    final locBloc = context.read<LocationBloc>();
    // If already have coords, return immediately
    if (locBloc.state.latitude != null && locBloc.state.longitude != null) {
      return true;
    }
    try {
      final state = await locBloc.stream
          .firstWhere((s) => s.latitude != null && s.longitude != null)
          .timeout(timeout);
      // local vars already updated in listener above, but set again to be safe
      setState(() {
        _latitude = state.latitude;
        _longitude = state.longitude;
      });
      return true;
    } catch (_) {
      return false;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
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
                progress: (completed, total) {
                  // Optional: show overlay or log progress
                  debugPrint("Progress: $completed / $total");
                },
                initial: () {},
              );
            },
            builder: (BuildContext context,
                AssetSubmissionState assetSubmissionState) {
              return ScrollableContent(
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),

                // ── FOOTER BUTTON ───────────────────────────────────────────────────
                footer: BlocBuilder<OverallAssetSummaryBloc,
                    OverallAssetSummaryState>(
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

                    final String userType = userState.maybeWhen(
                      supervisor: () => USER_TYPES.SUPERVISOR.name,
                      orElse: () => USER_TYPES.FIELD_STAFF.name,
                    );

                    isDisabled = (batteryCount == 0 ||
                        inverterCount == 0 ||
                        panelCount == 0 ||
                        (userType == USER_TYPES.SUPERVISOR.name &&
                            filePath!.isEmpty));

                    return FooterButton(
                        showSuffixIcon: false,
                        text: assetSubmissionState.maybeWhen(
                            loading: () => 'Submitting...',
                            progress: (completed, total) =>
                                'Submitting... ($completed/$total)',
                            orElse: () => i18.common.coreCommonSubmit),
                        isDisabled: assetSubmissionState.maybeWhen(
                          loading: () => true,
                          progress: (_, __) => true,
                          orElse: () => isDisabled,
                        ),
                        onPress: assetSubmissionState.maybeWhen(
                            loading: () => () {},
                            progress: (_, __) => () {},
                            orElse: () => () {
                                  if (isDisabled) return;

                                  // Pull in the current projectId
                                  final selState =
                                      context.read<SelectedProjectBloc>().state;
                                  selState.whenOrNull(selected: (project) {
                                    final summaryState =
                                        context.read<AssetSummaryBloc>().state;

                                    summaryState.whenOrNull(
                                      loaded: (summary) {
                                        print("userType $userType");
                                        if (userType ==
                                            USER_TYPES.SUPERVISOR.name) {
                                          context
                                              .read<CacheCompletionReportBloc>()
                                              .add(
                                                CacheCompletionReportEvent
                                                    .addOrUpdate(
                                                        CacheCompletionReport(
                                                            projectId:
                                                                _currentProjectId!,
                                                            filePath: filePath!,
                                                            latitude:
                                                                _latitude
                                                                    .toString(),
                                                            longitude: _longitude
                                                                .toString())),
                                              );
                                        }
                                        context.read<AssetSubmissionBloc>().add(
                                            AssetSubmissionEvent.submitAll(
                                                projectId: project.project.id,
                                                userType: userType));
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
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
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
                                        count: 0, text: 'Batteries'),
                                    const ElementAssetSummary(
                                      count: 0,
                                      text: 'Inverters',
                                    ),
                                    const ElementAssetSummary(
                                        count: 0, text: 'Panels'),
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
                                            count: 0, text: 'Batteries'),
                                        ElementAssetSummary(
                                            count: 0, text: 'Inverters'),
                                        ElementAssetSummary(
                                            count: 0, text: 'Panels'),
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
                                      onPressed: () {},
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
                                            color:
                                                theme.colorTheme.alert.error),
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
                                        selState.whenOrNull(
                                            selected: (project) {
                                          context
                                              .read<OverallAssetSummaryBloc>()
                                              .add(
                                                OverallAssetSummaryEvent
                                                    .loadCounts(
                                                  projectId: project.project.id,
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
                                      count: batteryCount,
                                      text: 'Batteries',
                                      onPress: () {
                                        context.read<AssetTypeBloc>().add(
                                            const AssetTypeEvent.typeSelected(
                                                "BATTERY"));
                                        context.router
                                            .push(const AssetSummaryRoute());
                                      },
                                    ),
                                    ElementAssetSummary(
                                      count: inverterCount,
                                      text: 'Inverters',
                                      onPress: () {
                                        context.read<AssetTypeBloc>().add(
                                            const AssetTypeEvent.typeSelected(
                                                "INVERTER"));
                                        context.router
                                            .push(const AssetSummaryRoute());
                                      },
                                    ),
                                    ElementAssetSummary(
                                      count: panelCount,
                                      text: 'Panels',
                                      onPress: () {
                                        context.read<AssetTypeBloc>().add(
                                            const AssetTypeEvent.typeSelected(
                                                "PANEL"));
                                        context.router
                                            .push(const AssetSummaryRoute());
                                      },
                                    ),
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
                        userState.maybeWhen(
                            orElse: () => Container(),
                            supervisor: () => DigitCard(
                                  children: [
                                    Text(
                                      'Installation Completion Report',
                                      style: textTheme.headingM.copyWith(
                                          color: theme
                                              .colorTheme.primary.primary2),
                                    ),
                                    Text(
                                      'Please scan and upload the installation completion report',
                                      style: textTheme.bodyS.copyWith(
                                          color:
                                              theme.colorTheme.text.secondary),
                                    ),
                                    FileUploadWidget(
                                      showPreview: true,
                                      allowMultiples: false,
                                      label: 'Upload',
                                      onFilesSelected: (files) {
                                        if (files.isEmpty ||
                                            files.first.path == null)
                                          return <PlatformFile, String?>{};
                                        _ensureLocationLoaded();
                                        // if (!ok) {
                                        //   context.showSnackBar(
                                        //     const SnackBar(content: Text('Could not fetch location')),
                                        //   );
                                        // }
                                        // Handle file copying outside of the callback
                                        _handleUpload(files.first);
                                        return <PlatformFile, String?>{};
                                      },
                                    ),
                                  ],
                                ))
                      ],
                    ),
                  ),
                ],
              );
            },
          ),
        );
      },
    );
  }
}
