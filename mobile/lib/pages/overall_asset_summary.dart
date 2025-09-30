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
import 'package:isar/isar.dart';
import 'package:path/path.dart' show basename;

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/asset_summary/asset_summary.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset/cache_asset.dart';
import '../blocs/cache_completion_report/cache_completion_report.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/project/project.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_completion_report.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/files/pdf_card.dart';
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
  String? _solutionDesignTypeCode;
  late String userType = "";
  List<PlatformFile> _initialCompletion = [];
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
      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
      final selState = context.read<SelectedProjectBloc>().state;
      selState.whenOrNull(selected: (project) {
        _currentProjectId = project.project.id;
        _solutionDesignTypeCode = "RMS_Single_Phase";
        // project?.project?.additionalDetails?.facility //todo remove when all solutionTypes are found
        //         ?.facilityDetails?.solar_solution_design_type ??
        //     "RMS_Single_Phase";
        context
            .read<CacheAssetBloc>()
            .add(CacheAssetEvent.start(project.project.id, userType, project));
        _loadInitialCompletion();
        context.read<OverallAssetSummaryBloc>().add(
              OverallAssetSummaryEvent.loadCounts(
                  projectId: project.project.id),
            );
      });
    });
  }

  @override
  void dispose() {
    _locSub?.cancel();
    super.dispose();
  }

  Future<bool> _ensureLocationLoaded(
      {Duration timeout = const Duration(seconds: 10)}) async {
    final locBloc = context.read<LocationBloc>();
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

  Future<void> _loadInitialCompletion() async {
    final isar = context.read<CacheAssetBloc>().isar;

    // 1) Try local cache
    final cached = await isar.cacheCompletionReports
        .where()
        .projectIdEqualTo(_currentProjectId!)
        .findFirst();

    if (cached?.filePath.isNotEmpty == true) {
      final f = await getCachedFile(cached!.filePath);
      if (f != null) {
        // persist into app's data directory too
        final p = await copyFileToLocalDir(f);
        if (!mounted) return;
        setState(() {
          filePath = p;
          _initialCompletion = [
            PlatformFile(
              name: basename(p),
              path: p,
              size: File(p).lengthSync(),
            )
          ];
        });
        return;
      }
    }

    // 2) Fallback: any server‐side docs on the workflow
    final wf = context
        .read<SelectedProjectBloc>()
        .state
        .whenOrNull(selected: (wf) => wf);
    final docs = wf?.workflow?.documents ?? [];

    final files = <PlatformFile>[];
    for (final doc in docs) {
      if (doc.documentType == 'INSTALLATION_REPORT' && doc.fileStore != null) {
        final f = await getCachedFile(doc.fileStore!);
        if (f != null) {
          final p = await copyFileToLocalDir(f);
          files.add(PlatformFile(
            name: basename(p),
            path: p,
            size: File(p).lengthSync(),
          ));
          filePath = p;
        }
      }
    }

    if (files.isNotEmpty && mounted) {
      setState(() => _initialCompletion = files);
    }
  }

  void _handleUpload(PlatformFile pf) async {
    final f = File(pf.path!);
    final dest = await copyFileToLocalDir(f);
    setState(() {
      filePath = dest;
      _initialCompletion = [
        PlatformFile(
          name: basename(dest),
          path: dest,
          size: File(dest).lengthSync(),
        )
      ];
    });
  }

  /// Decides BOTH the label shown on the button and the destination route.
  /// We look at the BOM form's `name` and infer a friendly label + (schemaName, pageName).
  ({String label, String schemaName, String pageName}) _bomRouteAndLabel(
      String name) {
    final n = name.toLowerCase();

    // Try to extract the token after "..._bom_"
    final bomMatch = RegExp(r'_bom_([a-z0-9]+)$').firstMatch(n);
    final token =
        bomMatch?.group(1) ?? n.split('_').last; // fallback: last segment

    switch (token) {
      case 'system':
      case 'parameters':
      case 'parameter':
        return (
          label: 'Fill System Parameters',
          schemaName: 'AssetForm.SystemParameters',
          pageName: 'SystemFunctionalityParameters_1',
        );
      case 'solar':
      case 'solarsystem':
        return (
          label: 'Fill BOM Solar System',
          schemaName: 'AssetForm',
          pageName: 'ModuleMountingstructure',
        );
      case 'luminaries':
      case 'luminary':
      case 'fan':
      case 'fans':
        return (
          label: 'Fill BOM Luminaries',
          schemaName: 'AssetForm.LuminariesFan',
          pageName: 'Luminaires_Fans_Page1',
        );
      case 'wiring':
      case 'load':
      case 'loadwiring':
        return (
          label: 'Fill BOM Load Wiring',
          schemaName: 'AssetForm.LoadWiring',
          pageName: 'BOM.LoadWiring',
        );
      case 'rms':
        return (
          label: 'Fill BOM RMS',
          schemaName: 'AssetForm.RMS',
          pageName: 'BOM.RMS',
        );
      default:
        // Fallback: humanize raw name and open Solar page
        final pretty = name
            .replaceAll('_', ' ')
            .replaceAllMapped(RegExp(r'\b([a-z])'), (m) => m[1]!.toUpperCase());
        return (
          label: 'Fill $pretty',
          schemaName: 'AssetForm',
          pageName: 'ModuleMountingstructure',
        );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    String _displaySize() {
      if (filePath == null || filePath!.isEmpty) return '0 KB';
      final f = File(filePath!);
      if (!f.existsSync()) return '0 KB';
      return '${(f.lengthSync() / 1024).toStringAsFixed(1)} KB';
    }

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
        return Scaffold(
          body: BlocBuilder<ReportTypeBloc, ReportTypeState>(
            builder: (context, reportState) {
              bool isNewReport = reportState.maybeWhen(
                  newReport: () => true, orElse: () => false);
              bool isInboxReport =
                  reportState.maybeWhen(inbox: () => true, orElse: () => false);
              return BlocConsumer<AssetSubmissionBloc, AssetSubmissionState>(
                listener: (context, assetSubmissionState) {
                  assetSubmissionState.whenOrNull(
                    success: () {
                      // Show a snack bar and navigate to the success page
                      context.showSnackBar(
                        const SnackBar(
                            content: Text("All assets submitted successfully")),
                      );
                      context.router
                          .popAndPush(const SubmittedSaveSuccessRoute());
                    },
                    failure: (error) {
                      context.showSnackBar(
                        SnackBar(content: Text("$error")),
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

                        return reportState.maybeWhen(
                          submitted: () => const SizedBox.shrink(),
                          orElse: () => FooterButton(
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
                                        final selState = context
                                            .read<SelectedProjectBloc>()
                                            .state;
                                        selState.whenOrNull(
                                            selected: (project) {
                                          context.read<ProjectBloc>().add(
                                                ProjectEvent.addUnSubmitted(
                                                    project, userType),
                                              );

                                          final summaryState = context
                                              .read<AssetSummaryBloc>()
                                              .state;

                                          summaryState.whenOrNull(
                                            loaded: (summary) {
                                              if (userType ==
                                                  USER_TYPES.SUPERVISOR.name) {
                                                context
                                                    .read<
                                                        CacheCompletionReportBloc>()
                                                    .add(
                                                      CacheCompletionReportEvent.addOrUpdate(
                                                          CacheCompletionReport(
                                                              projectId:
                                                                  _currentProjectId!,
                                                              filePath:
                                                                  filePath!,
                                                              latitude: _latitude
                                                                  .toString(),
                                                              longitude: _longitude
                                                                  .toString())),
                                                    );
                                              }
                                              context
                                                  .read<AssetSubmissionBloc>()
                                                  .add(AssetSubmissionEvent
                                                      .submitAll(
                                                          projectId: project
                                                              .project.id,
                                                          userType: userType));
                                            },
                                          );
                                        });
                                      })),
                        );
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
                                                color: theme
                                                    .colorTheme.alert.error),
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
                                                  .read<
                                                      OverallAssetSummaryBloc>()
                                                  .add(
                                                    OverallAssetSummaryEvent
                                                        .loadCounts(
                                                      projectId:
                                                          project.project.id,
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
                                                const AssetTypeEvent
                                                    .typeSelected("BATTERY"));
                                            context.router.push(
                                                const AssetSummaryRoute());
                                          },
                                        ),
                                        ElementAssetSummary(
                                          count: inverterCount,
                                          text: 'Inverters',
                                          onPress: () {
                                            context.read<AssetTypeBloc>().add(
                                                const AssetTypeEvent
                                                    .typeSelected("INVERTER"));
                                            context.router.push(
                                                const AssetSummaryRoute());
                                          },
                                        ),
                                        reportState.maybeWhen(
                                            submitted: () =>
                                                ElementAssetSummary(
                                                  lastCard: true,
                                                  count: panelCount,
                                                  text: 'Panels',
                                                  onPress: () {
                                                    context
                                                        .read<AssetTypeBloc>()
                                                        .add(
                                                            const AssetTypeEvent
                                                                .typeSelected(
                                                                "PANEL"));
                                                    context.router.push(
                                                        const AssetSummaryRoute());
                                                  },
                                                ),
                                            orElse: () => Column(
                                                  children: [
                                                    ElementAssetSummary(
                                                      count: panelCount,
                                                      text: 'Panels',
                                                      onPress: () {
                                                        context
                                                            .read<
                                                                AssetTypeBloc>()
                                                            .add(const AssetTypeEvent
                                                                .typeSelected(
                                                                "PANEL"));
                                                        context.router.push(
                                                            const AssetSummaryRoute());
                                                      },
                                                    ),
                                                    DigitButton(
                                                      mainAxisSize:
                                                          MainAxisSize.max,
                                                      label: 'Add More Assets',
                                                      prefixIcon: Icons.add_box,
                                                      onPressed: () {
                                                        context.router.push(
                                                            const SelectAssetTypeRoute());
                                                      },
                                                      type: DigitButtonType
                                                          .primary,
                                                      size: DigitButtonSize
                                                          .medium,
                                                    )
                                                  ],
                                                ))
                                      ],
                                    );
                                  },
                                );
                              },
                            ),

                            const SizedBox(height: spacer4),
                            userState.maybeWhen(
                              orElse: () => Container(),
                              supervisor: () => Column(
                                children: [
                                  DigitCard(
                                    children: [
                                      ...(isNewReport || isInboxReport
                                          ? [
                                              Text(
                                                'Installation Completion Report',
                                                style: textTheme.headingM
                                                    .copyWith(
                                                        color: theme.colorTheme
                                                            .primary.primary2),
                                              ),
                                              Text(
                                                'Please scan and upload the installation completion report',
                                                style: textTheme.bodyS.copyWith(
                                                    color: theme.colorTheme.text
                                                        .secondary),
                                              ),
                                              FileUploadWidget(
                                                initialFiles:
                                                    _initialCompletion,
                                                allowedExtensions: ["pdf"],
                                                showPreview: true,
                                                allowMultiples: false,
                                                label: 'Upload',
                                                onFilesSelected: (files) {
                                                  if (files.isEmpty ||
                                                      files.first.path ==
                                                          null) {
                                                    return <PlatformFile,
                                                        String?>{};
                                                  }
                                                  _ensureLocationLoaded();
                                                  _handleUpload(files.first);
                                                  return <PlatformFile,
                                                      String?>{};
                                                },
                                              ),
                                            ]
                                          : [
                                              GestureDetector(
                                                onTap: () {
                                                  print("filePath $filePath");
                                                  if (filePath != null &&
                                                      filePath!.isNotEmpty) {
                                                    context.router.push(
                                                        PdfViewerRoute(
                                                            path: filePath!));
                                                  }
                                                },
                                                child: pdfCard(
                                                  context: context,
                                                  filePath: filePath ??
                                                      'No report yet',
                                                  fileSize: _displaySize(),
                                                ),
                                              )
                                            ]),
                                      ...[
                                        BlocBuilder<AppInitialization,
                                            InitState>(
                                          builder: (context, state) {
                                            return state.maybeWhen(
                                                orElse: () =>
                                                    const SizedBox.shrink(),
                                                initialized: (appConfig,
                                                    assetCount,
                                                    assetType,
                                                    system,
                                                    warranty,
                                                    brand,
                                                    solutionDesign,
                                                    solutionDesignBom) {
                                                  return Column(
                                                    crossAxisAlignment:
                                                        CrossAxisAlignment
                                                            .stretch,
                                                    children: [
                                                      Builder(
                                                        builder: (_) {
                                                          final matches =
                                                              solutionDesignBom
                                                                  .where(
                                                            (e) =>
                                                                e.data
                                                                    .solutionDesignTypeCode ==
                                                                _solutionDesignTypeCode,
                                                          );

                                                          final matching = matches
                                                                  .isNotEmpty
                                                              ? matches.first
                                                              : null; // <-- nullable
                                                          final entries = matching
                                                                  ?.data
                                                                  .bomForms ??
                                                              const [];

                                                          if (entries.isEmpty) {
                                                            return const SizedBox
                                                                .shrink();
                                                          }

                                                          return Builder(
                                                              builder:
                                                                  (context) {
                                                            return Column(
                                                              crossAxisAlignment:
                                                                  CrossAxisAlignment
                                                                      .stretch,
                                                              children: [
                                                                for (final entry
                                                                    in entries) ...[
                                                                  Builder(
                                                                      builder:
                                                                          (_) {
                                                                    final r =
                                                                        _bomRouteAndLabel(
                                                                            entry.name);
                                                                    return DigitButton(
                                                                        mainAxisSize:
                                                                            MainAxisSize
                                                                                .max,
                                                                        label: r
                                                                            .label,
                                                                        onPressed:
                                                                            () {
                                                                          final r =
                                                                              _bomRouteAndLabel(entry.name);
                                                                          context.router.push(DynamicFormsRoute(
                                                                              pageName: r.pageName,
                                                                              schemaName: r.schemaName,
                                                                              projectId: _currentProjectId!));
                                                                        },
                                                                        type: DigitButtonType
                                                                            .secondary,
                                                                        size: DigitButtonSize
                                                                            .large);
                                                                  }),
                                                                  const SizedBox(
                                                                      height:
                                                                          spacer4),
                                                                ],
                                                              ],
                                                            );
                                                          });
                                                        },
                                                      ),
                                                    ],
                                                  );
                                                });
                                          },
                                        )
                                      ]
                                    ],
                                  ),
                                ],
                              ),
                            )
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
      },
    );
  }
}
