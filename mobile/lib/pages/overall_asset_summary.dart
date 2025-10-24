import 'dart:async';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path/path.dart' as p;

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset/cache_asset.dart';
import '../blocs/cache_completion_report/cache_completion_report.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/project/project.dart';
import '../blocs/project_bom/project_bom.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../model/mdms/mdms.dart';
import '../model/project_workflow/project_workflow.dart';
import '../model/solution_design_type/solution_design_type.dart';
import '../repositories/project_workflow.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/bom_buttons.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/summary/existing_or_loader.dart';
import '../widgets/summary/summary.dart';

@RoutePage()
class OverallAssetSummaryPage extends StatefulWidget {
  const OverallAssetSummaryPage({super.key, this.refresh});
  final int? refresh;
  @override
  State<OverallAssetSummaryPage> createState() =>
      _OverallAssetSummaryPageState();
}

class _OverallAssetSummaryPageState extends State<OverallAssetSummaryPage> {
  String? _currentProjectId;
  ProjectWorkflow? projectWorkflow;
  double? _latitude;
  double? _longitude;
  String? _system;
  late String userType = "";
  bool _didNavigateAfterSubmit = false;

  List<ExistingReport> _existingReports = [];
  List<PlatformFile> _pickedFiles = [];

  StreamSubscription<LocationState>? _locSub;

  @override
  void initState() {
    super.initState();
    _reload();
  }

  @override
  void didUpdateWidget(covariant OverallAssetSummaryPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.refresh != oldWidget.refresh) {
      _reload();
    }
  }

  void _reload() {
    _locSub?.cancel();
    final locBloc = context.read<LocationBloc>();
    locBloc.add(const LocationEvent.requestPermission());
    locBloc.add(const LocationEvent.requestService());
    _locSub = locBloc.stream.listen((locationState) {
      if (locationState.latitude != null && locationState.longitude != null) {
        setState(() {
          _latitude = locationState.latitude;
          _longitude = locationState.longitude;
        });
      }
    });

    // WidgetsBinding.instance.addPostFrameCallback((_) {
    userType = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => USER_TYPES.SUPERVISOR.name,
          orElse: () => USER_TYPES.FIELD_STAFF.name,
        );
    final selState = context.read<SelectedProjectBloc>().state;
    selState.whenOrNull(selected: (project) {
      _currentProjectId = project.project.id;
      projectWorkflow = project;
      // _solutionDesignTypeCode = "DC";
      context
          .read<CacheAssetBloc>()
          .add(CacheAssetEvent.start(project.project.id, userType, project));

      context.read<OverallAssetSummaryBloc>().add(
            OverallAssetSummaryEvent.loadCounts(projectId: project.project.id),
          );
      context.read<ProjectBomBloc>().add(
            ProjectBomEvent.syncIfNeeded(
              projectId: _currentProjectId!,
              userType: userType,
            ),
          );
      _loadProjectSystem();
      _loadInitialCompletion();
    });
    //});
  }

  @override
  void dispose() {
    _didNavigateAfterSubmit = false;
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

    final combined = await loadInitialCompletion(
      isar: isar,
      projectId: _currentProjectId!,
      projectWorkflow: projectWorkflow!,
    );

    if (!mounted) return;
    setState(() {
      _existingReports = combined.map((pf) {
        final path = pf.path!;
        return ExistingReport(
          isarId: null,
          filePath: path,
          fileName: p.basename(path),
          fileType: inferFileType(path),
        );
      }).toList();
      _pickedFiles = [];
    });
  }

  Future<void> _handleUploads(List<PlatformFile> picked) async {
    final copied = await copyPickedFilesLocally(picked);
    if (!mounted) return;
    setState(() {
      _pickedFiles = copied;
    });
  }

  Future<void> _loadProjectSystem() async {
    if (_currentProjectId == null) return;

    final isar = context.read<ProjectBloc>().isar;

    final initState = context.read<AppInitialization>().state;
    final solutionDesignList =
        initState.maybeWhen<List<Mdms<SolutionDesignType>>>(
      initialized: (_, __, ___, ____, _____, ______, solutionDesign, _______) =>
          solutionDesign,
      orElse: () => const [],
    );

    final facilityCode = projectWorkflow?.project.additionalDetails?.facility
        ?.facilityDetails?.solar_solution_design_type;

    final sys = await ProjectWorkflowRepository().getProjectSystem(
        isar: isar,
        projectId: _currentProjectId!,
        solutionDesignList: solutionDesignList,
        facilitySolutionDesignCode: facilityCode);

    if (!mounted) return;
    setState(() => _system = sys);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<ProjectBomBloc, ProjectBomState>(
      listener: (context, state) {
        state.maybeWhen(
          loading: () {},
          success: (savedBomValues) async {
            context.read<OverallAssetSummaryBloc>().add(
                OverallAssetSummaryEvent.loadCounts(
                    projectId: _currentProjectId!));
            await _loadInitialCompletion();
          },
          failure: (msg) {
            context.showSnackBar(
              const SnackBar(content: Text('BOM sync failed')),
            );
          },
          orElse: () {},
        );
      },
      child: BlocBuilder<UserTypeBloc, UserTypeState>(
        builder: (context, userState) {
          return BlocListener<SelectedProjectBloc, SelectedProjectState>(
            listenWhen: (prev, curr) =>
                curr.maybeWhen(selected: (_) => true, orElse: () => false),
            listener: (context, state) {
              state.whenOrNull(
                selected: (_) {
                  if (mounted) _reload();
                },
              );
            },
            child: Scaffold(
              body: BlocBuilder<ReportTypeBloc, ReportTypeState>(
                builder: (context, reportState) {
                  final bool isNewReport = reportState.maybeWhen(
                      newReport: () => true, orElse: () => false);
                  final bool isInboxReport = reportState.maybeWhen(
                      inbox: () => true, orElse: () => false);
                  final bool isSubmittedReport = reportState.maybeWhen(
                      submitted: () => true, orElse: () => false);

                  return BlocConsumer<AssetSubmissionBloc,
                      AssetSubmissionState>(
                    listener: (context, assetSubmissionState) {
                      assetSubmissionState.whenOrNull(
                        success: () {
                          ScaffoldMessenger.of(context).clearSnackBars();
                          context.showSnackBar(
                            const SnackBar(
                                content:
                                    Text("All assets submitted successfully")),
                          );

                          final router = context.router.root;
                          WidgetsBinding.instance.addPostFrameCallback((_) {
                            if (!mounted) return;
                            if (router.canPop()) {
                              router.popAndPush(
                                  const SubmittedSaveSuccessRoute());
                            } else {
                              router.push(const SubmittedSaveSuccessRoute());
                            }
                          });
                        },
                        failure: (error) {
                          ScaffoldMessenger.of(context).clearSnackBars();
                          context
                              .showSnackBar(SnackBar(content: Text("$error")));
                          _didNavigateAfterSubmit = false;
                        },
                        loading: () {},
                        progress: (completed, total) {
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

                        // ── FOOTER BUTTON ────────────────────────────────────────────────
                        footer: BlocBuilder<OverallAssetSummaryBloc,
                            OverallAssetSummaryState>(
                          builder: (context, overallState) {
                            int batteryCount = 0,
                                inverterCount = 0,
                                panelCount = 0;

                            overallState.when(
                              initial: () {},
                              loading: () {},
                              error: (_) {},
                              loaded: (bCount, iCount, pCount) {
                                batteryCount = bCount;
                                inverterCount = iCount;
                                panelCount = pCount;
                              },
                            );

                            final String resolvedUserType = userState.maybeWhen(
                              supervisor: () => USER_TYPES.SUPERVISOR.name,
                              orElse: () => USER_TYPES.FIELD_STAFF.name,
                            );

                            final bool requireCompletionForSupervisor =
                                resolvedUserType == USER_TYPES.SUPERVISOR.name;

                            final bool hasAnyCompletion =
                                _existingReports.isNotEmpty ||
                                    _pickedFiles.isNotEmpty;

                            final bool isDisabled = (batteryCount == 0 ||
                                inverterCount == 0 ||
                                panelCount == 0 ||
                                (requireCompletionForSupervisor &&
                                    !hasAnyCompletion));

                            return reportState.maybeWhen(
                              submitted: () => const SizedBox.shrink(),
                              orElse: () => FooterButton(
                                showSuffixIcon: false,
                                text: assetSubmissionState.maybeWhen(
                                  loading: () => 'Submitting...',
                                  progress: (completed, total) =>
                                      'Submitting... ($completed/$total)',
                                  orElse: () => i18.common.coreCommonSubmit,
                                ),
                                isDisabled: assetSubmissionState.maybeWhen(
                                  loading: () => true,
                                  progress: (_, __) => true,
                                  orElse: () => isDisabled,
                                ),
                                onPress: assetSubmissionState.maybeWhen(
                                  loading: () => () {},
                                  progress: (_, __) => () {},
                                  orElse: () => () async {
                                    print("It got here actually!");
                                    if (isDisabled) return;
                                    print(
                                        "It got here actually _ensureLocationLoaded!");
                                    // ensure we have a lat/lng before saving completion reports
                                    await _ensureLocationLoaded();

                                    final selState = context
                                        .read<SelectedProjectBloc>()
                                        .state;
                                    selState.whenOrNull(selected: (project) {
                                      print("It got here actually selState");
                                      context.read<ProjectBloc>().add(
                                            ProjectEvent.addUnSubmitted(
                                                project, resolvedUserType),
                                          );

                                      // final summaryState = context
                                      //     .read<AssetSummaryBloc>()
                                      //     .state;

                                      // summaryState.whenOrNull(
                                      //  loaded: (summary) async {
                                      // Build inputs from existing (kept) + newly picked
                                      print("It got here actually loaded");
                                      final lat = _latitude?.toString() ?? '';
                                      final lng = _longitude?.toString() ?? '';

                                      final keptExisting = _existingReports
                                          .map((e) => CompletionFileInput(
                                                projectId: _currentProjectId!,
                                                filePath: e.filePath,
                                                fileType: e.fileType,
                                                fileName: e.fileName,
                                                latitude: lat,
                                                longitude: lng,
                                                index: null,
                                              ));

                                      print(
                                          "It got here actually loaded pickedInputs");

                                      final pickedInputs = _pickedFiles
                                          .where((pf) =>
                                              pf.path != null &&
                                              pf.path!.isNotEmpty)
                                          .map((pf) => CompletionFileInput(
                                                projectId: _currentProjectId!,
                                                filePath: pf.path!,
                                                fileType:
                                                    inferFileType(pf.path!),
                                                fileName: pf.name.isNotEmpty
                                                    ? pf.name
                                                    : p.basename(pf.path!),
                                                latitude: lat,
                                                longitude: lng,
                                                index: null,
                                              ));

                                      final inputs = [
                                        ...keptExisting,
                                        ...pickedInputs
                                      ].toList();

                                      print(
                                          "It got here actually loaded inputs");

                                      // Atomically clear then add
                                      context
                                          .read<CacheCompletionReportBloc>()
                                          .add(
                                            CacheCompletionReportEvent
                                                .replaceAllForProject(
                                              projectId: _currentProjectId!,
                                              files: inputs,
                                            ),
                                          );
                                      // Proceed with submission

                                      print(
                                          "It got here actually loaded before submisison");
                                      context.read<AssetSubmissionBloc>().add(
                                            AssetSubmissionEvent.submitAll(
                                              projectId: project.project.id,
                                              userType: resolvedUserType,
                                            ),
                                          );
                                      // },
                                      // );
                                    });
                                  },
                                ),
                              ),
                            );
                          },
                        ),

                        // ── MAIN CONTENT ────────────────────────────────────────────────
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

                                // ── COUNTS CARD ─────────────────────────────────────────
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
                                              onPressed: () {},
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
                                                    count: 0,
                                                    text: 'Batteries'),
                                                ElementAssetSummary(
                                                    count: 0,
                                                    text: 'Inverters'),
                                                ElementAssetSummary(
                                                    count: 0, text: 'Panels'),
                                              ],
                                            ),
                                            const SizedBox(height: spacer6),
                                            const Center(
                                                child:
                                                    CircularProgressIndicator()),
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
                                                    color: theme.colorTheme
                                                        .alert.error),
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
                                                          projectId: project
                                                              .project.id,
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
                                      loaded: (int batteryCount,
                                          int inverterCount, int panelCount) {
                                        return DigitCard(
                                          children: [
                                            ElementAssetSummary(
                                              count: batteryCount,
                                              text: 'Batteries',
                                              onPress: () {
                                                context
                                                    .read<AssetTypeBloc>()
                                                    .add(const AssetTypeEvent
                                                        .typeSelected(
                                                        "BATTERY"));
                                                context.router.push(
                                                    const AssetSummaryRoute());
                                              },
                                            ),
                                            ElementAssetSummary(
                                              count: inverterCount,
                                              text: 'Inverters',
                                              onPress: () {
                                                context
                                                    .read<AssetTypeBloc>()
                                                    .add(const AssetTypeEvent
                                                        .typeSelected(
                                                        "INVERTER"));
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
                                                      .add(const AssetTypeEvent
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
                                                          .read<AssetTypeBloc>()
                                                          .add(
                                                              const AssetTypeEvent
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
                                                    type:
                                                        DigitButtonType.primary,
                                                    size:
                                                        DigitButtonSize.medium,
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

                                const SizedBox(height: spacer4),

                                // ── COMPLETION REPORT (Supervisor only) ────────────────
                                userState.maybeWhen(
                                  orElse: () => Container(),
                                  supervisor: () => Column(
                                    children: [
                                      DigitCard(
                                        children: [
                                          Text(
                                            'Installation Completion Report',
                                            style: textTheme.headingM.copyWith(
                                              color: theme
                                                  .colorTheme.primary.primary2,
                                            ),
                                          ),
                                          ...(isNewReport || isInboxReport
                                              ? [
                                                  Text(
                                                    'Please fill out all sections of the report or upload relevant documents.',
                                                    style: textTheme.bodyS
                                                        .copyWith(
                                                            color: theme
                                                                .colorTheme
                                                                .primary
                                                                .primary2),
                                                  ),
                                                ]
                                              : []),
                                          ...[
                                            BlocBuilder<AppInitialization,
                                                InitState>(
                                              builder: (context, state) {
                                                return state.maybeWhen(
                                                  orElse: () =>
                                                      const SizedBox.shrink(),
                                                  initialized: (
                                                    appConfig,
                                                    assetCount,
                                                    assetType,
                                                    system,
                                                    warranty,
                                                    brand,
                                                    solutionDesign,
                                                    solutionDesignBom,
                                                  ) {
                                                    return Column(
                                                      children: [
                                                        if (_system != null)
                                                          BomButtonsSection(
                                                            key: PageStorageKey(
                                                                'bom-buttons-${_currentProjectId!}'),
                                                            solutionDesignBom:
                                                                solutionDesignBom,
                                                            systemCode:
                                                                _system!,
                                                            projectId:
                                                                _currentProjectId!,
                                                            origin: isSubmittedReport
                                                                ? FormOrigin
                                                                    .submitted
                                                                : FormOrigin
                                                                    .overallSummary,
                                                          ),
                                                      ],
                                                    );
                                                  },
                                                );
                                              },
                                            )
                                          ],
                                          ...(isNewReport || isInboxReport
                                              ? [
                                                  FileUploadWidget(
                                                      allowedExtensions: const [
                                                        "pdf",
                                                        "jpg",
                                                        "jpeg",
                                                        "png"
                                                      ],
                                                      showPreview: true,
                                                      allowMultiples: true,
                                                      label: 'Upload',
                                                      onFilesSelected: (files) {
                                                        if (files.isEmpty) {
                                                          return <PlatformFile,
                                                              String?>{};
                                                        }
                                                        _ensureLocationLoaded();
                                                        _handleUploads(files);

                                                        return <PlatformFile,
                                                            String?>{};
                                                      }),
                                                  ExistingFilesOrLoader(
                                                    existingReports:
                                                        _existingReports,
                                                    workflowDocuments:
                                                        projectWorkflow
                                                                ?.workflow
                                                                ?.documents ??
                                                            [],
                                                    readOnly: false,
                                                    onRemove: (r) {
                                                      setState(() {
                                                        _existingReports
                                                            ?.remove(r);
                                                      });
                                                    },
                                                  ),
                                                ]
                                              : [
                                                  ExistingFilesOrLoader(
                                                    existingReports:
                                                        _existingReports,
                                                    workflowDocuments:
                                                        projectWorkflow
                                                                ?.workflow
                                                                ?.documents ??
                                                            [],
                                                    readOnly: true,
                                                  ),
                                                ]),
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
            ),
          );
        },
      ),
    );
  }
}
