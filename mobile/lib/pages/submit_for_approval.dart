import 'dart:async';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path/path.dart' as p;
import 'package:recase/recase.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset/cache_asset.dart';
import '../blocs/cache_completion_report/cache_completion_report.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/project/project.dart';
import '../blocs/project_bom/project_bom.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../model/comment/comment.dart';
import '../model/project_workflow/project_workflow.dart';
import '../repositories/bom_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/summary/existing_or_loader.dart';
import '../widgets/summary/summary.dart';

@RoutePage()
class SubmitForApprovalPage extends StatefulWidget {
  const SubmitForApprovalPage({super.key});

  @override
  State<SubmitForApprovalPage> createState() => _SubmitForApprovalPageState();
}

class _SubmitForApprovalPageState extends State<SubmitForApprovalPage> {
  late String userType = "";
  late String projectId = "";
  ProjectWorkflow? project;
  double? _latitude;
  double? _longitude;
  bool rejection1 = false;
  bool rejection2 = false;
  bool rejection3 = false;
  String? _solutionDesignTypeCode;

  // for completion report upload
  List<PlatformFile> _pickedFiles = [];
  List<ExistingReport> _existingReports = [];

  StreamSubscription<LocationState>? _locSub;

  @override
  void initState() {
    super.initState();
    userType = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => USER_TYPES.SUPERVISOR.name,
          orElse: () => USER_TYPES.FIELD_STAFF.name,
        );
    // Kick off the cache sync
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final selState = context.read<SelectedProjectBloc>().state;
      selState.whenOrNull(selected: (selProject) {
        projectId = selProject.project.id;
        project = selProject;
        _solutionDesignTypeCode = "RMS_Single_Phase";
      });

      context
          .read<CacheAssetBloc>()
          .add(CacheAssetEvent.start(projectId, userType, project!));
      context.read<ProjectBomBloc>().add(
            ProjectBomEvent.syncIfNeeded(
              projectId: projectId,
              userType: userType,
            ),
          );
      _loadInitialCompletion();
    });
  }

  Future<void> _loadInitialCompletion() async {
    final isar = context.read<CacheAssetBloc>().isar;

    final combined = await loadInitialCompletion(
      isar: isar,
      projectId: projectId,
      projectWorkflow: project!,
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

    final allChecked = rejection1 && rejection2 && rejection3;
    final isSupervisor = userType == USER_TYPES.SUPERVISOR.name;

    return BlocListener<ProjectBomBloc, ProjectBomState>(
      listener: (context, state) {
        state.maybeWhen(
          loading: () {},
          success: (_) async {
            await _loadInitialCompletion();
          },
          failure: (msg) {
            context
                .showSnackBar(SnackBar(content: Text('BOM sync failed: $msg')));
          },
          orElse: () {},
        );
      },
      child: Scaffold(
        body: MultiBlocListener(
          listeners: [
            BlocListener<CacheAssetBloc, CacheAssetState>(
              listener: (context, cacheState) {
                cacheState.whenOrNull(
                  success: () {
                    context.read<OverallAssetSummaryBloc>().add(
                          OverallAssetSummaryEvent.loadCounts(
                              projectId: projectId),
                        );
                  },
                  failure: (error) {
                    context.read<OverallAssetSummaryBloc>().add(
                          OverallAssetSummaryEvent.loadCounts(
                              projectId: projectId),
                        );
                    context.showSnackBar(
                      SnackBar(content: Text("Sync failed: $error")),
                    );
                  },
                  // loading: (_) // we show loading in the summary widget itself
                );
              },
            ),
            BlocListener<AssetSubmissionBloc, AssetSubmissionState>(
              listener: (context, submissionState) {
                submissionState.maybeWhen(
                  loading: () {
                    showDialog(
                      context: context,
                      barrierDismissible: false,
                      builder: (_) => const Center(
                        child: CircularProgressIndicator(),
                      ),
                    );
                  },
                  success: () {
                    Navigator.of(context, rootNavigator: true).pop();
                    context.router.replace(const InboxRoute());
                  },
                  failure: (message) {
                    Navigator.of(context, rootNavigator: true).pop();
                    context.showSnackBar(SnackBar(content: Text(message)));
                  },
                  orElse: () {},
                );
              },
            ),
          ],
          child: ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            footer: BlocBuilder<SelectedProjectBloc, SelectedProjectState>(
              builder: (context, selProjectState) {
                return BlocBuilder<AssetSubmissionBloc, AssetSubmissionState>(
                  builder: (context, submissionState) {
                    final submitting = submissionState.maybeWhen(
                        loading: () => true, orElse: () => false);

                    final selProject =
                        selProjectState.whenOrNull(selected: (wf) => wf);
                    if (selProject == null) {
                      return const SizedBox.shrink();
                    }

                    final isRejectedByQc = selProject.status ==
                        WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_QC_SPOC.name;
                    bool isFieldStaff = userType == USER_TYPES.FIELD_STAFF.name;

                    if (isFieldStaff && isRejectedByQc) {
                      return const SizedBox.shrink();
                    }

                    // Determine if completion requirement is needed
                    final requireCompletion = isSupervisor;
                    final hasCompletion =
                        _existingReports.isNotEmpty || _pickedFiles.isNotEmpty;

                    final isDisabled = !allChecked ||
                        submitting ||
                        (requireCompletion && !hasCompletion);

                    return FooterButton(
                        showSuffixIcon: false,
                        isDisabled: isDisabled,
                        text: submitting
                            ? "loading..."
                            : "Re-Submit for Approval",
                        onPress: () async {
                          print("allChecked $allChecked");
                          print("submitting $submitting");
                          print("submitting ${!allChecked || submitting}");
                          if (isDisabled) return;
                          await _ensureLocationLoaded();

                          // Build file inputs (existing + picked)
                          final lat = _latitude?.toString() ?? '';
                          final lng = _longitude?.toString() ?? '';

                          final inputs = <CompletionFileInput>[];
                          for (final e in _existingReports) {
                            inputs.add(CompletionFileInput(
                              projectId: projectId,
                              filePath: e.filePath,
                              fileType: e.fileType,
                              fileName: e.fileName,
                              latitude: lat,
                              longitude: lng,
                              index: null,
                            ));
                          }
                          for (final pf in _pickedFiles) {
                            if (pf.path == null) continue;
                            inputs.add(CompletionFileInput(
                              projectId: projectId,
                              filePath: pf.path!,
                              fileType: inferFileType(pf.path!),
                              fileName: pf.name,
                              latitude: lat,
                              longitude: lng,
                              index: null,
                            ));
                          }
                          context.read<AssetSubmissionBloc>().add(
                                AssetSubmissionEvent.submitAll(
                                    projectId: projectId, userType: userType),
                              );
                        });
                  },
                );
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
                      'Summary',
                      style: textTheme.headingXl
                          .copyWith(color: theme.colorTheme.primary.primary2),
                    ),
                    const SizedBox(height: spacer4),
                    const RejectedEditAssetSummary(),
                    if (userType == USER_TYPES.SUPERVISOR.name) ...[
                      const SizedBox(height: spacer4),
                      DigitCard(
                        children: [
                          Text(
                            'Installation Completion Report',
                            style: textTheme.headingM.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                          ...[
                            BlocBuilder<AppInitialization, InitState>(
                              builder: (context, state) {
                                return state.maybeWhen(
                                  orElse: () => const SizedBox.shrink(),
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
                                      crossAxisAlignment:
                                          CrossAxisAlignment.stretch,
                                      children: [
                                        Builder(
                                          builder: (_) {
                                            final matches =
                                                solutionDesignBom.where(
                                              (e) =>
                                                  e.data
                                                      .solutionDesignTypeCode ==
                                                  _solutionDesignTypeCode,
                                            );

                                            final matching = matches.isNotEmpty
                                                ? matches.first
                                                : null;
                                            final entries =
                                                matching?.data.bomForms ??
                                                    const [];

                                            if (entries.isEmpty) {
                                              return const SizedBox.shrink();
                                            }

                                            return Column(
                                              crossAxisAlignment:
                                                  CrossAxisAlignment.stretch,
                                              children: [
                                                for (final entry
                                                    in entries) ...[
                                                  Builder(
                                                    builder: (_) {
                                                      final r =
                                                          bomRouteAndLabel(
                                                              entry.name);
                                                      final isar = context
                                                          .read<ProjectBloc>()
                                                          .isar;
                                                      return FutureBuilder<
                                                              String>(
                                                          future: BomRepository()
                                                              .resolveBomActionLabel(
                                                                  isar: isar,
                                                                  projectId:
                                                                      projectId,
                                                                  schemaKey: r
                                                                      .schemaName,
                                                                  isInboxView:
                                                                      false),
                                                          builder:
                                                              (context, snap) {
                                                            final labelWord =
                                                                snap.hasData
                                                                    ? snap.data!
                                                                    : '...';
                                                            return DigitButton(
                                                              mainAxisSize:
                                                                  MainAxisSize
                                                                      .max,
                                                              label:
                                                                  '$labelWord ${r.label}',
                                                              onPressed: () {
                                                                context.router.push(
                                                                    DynamicFormsRoute(
                                                                  pageName: r
                                                                      .pageName,
                                                                  schemaName: r
                                                                      .schemaName,
                                                                  projectId:
                                                                      projectId!,
                                                                ));
                                                              },
                                                              type:
                                                                  DigitButtonType
                                                                      .secondary,
                                                              size:
                                                                  DigitButtonSize
                                                                      .large,
                                                            );
                                                          });
                                                    },
                                                  ),
                                                  const SizedBox(
                                                      height: spacer4),
                                                ],
                                              ],
                                            );
                                          },
                                        ),
                                      ],
                                    );
                                  },
                                );
                              },
                            )
                          ],
                          Text(
                            'Please scan and upload the installation completion report',
                            style: textTheme.bodyS.copyWith(
                                color: theme.colorTheme.text.secondary),
                          ),
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
                                return <PlatformFile, String?>{};
                              }
                              _ensureLocationLoaded();
                              _handleUploads(files);
                              return <PlatformFile, String?>{};
                            },
                          ),
                          // existingFilesSection(
                          //   context: context,
                          //   existing: _existingReports,
                          //   showEditButton: true,
                          //   onTapImage: (path) => context.router
                          //       .push(ImageViewerRoute(path: path)),
                          //   onTapPdf: (path) =>
                          //       context.router.push(PdfViewerRoute(path: path)),
                          //   onRemove: (r) {
                          //     setState(() {
                          //       _existingReports.remove(r);
                          //     });
                          //   },
                          // ),
                          ExistingFilesOrLoader(
                            existingReports: _existingReports,
                            workflowDocuments:
                                project?.workflow?.documents ?? [],
                            readOnly: false,
                            onRemove: (r) {
                              setState(() {
                                _existingReports?.remove(r);
                              });
                            },
                          ),
                          RejectionReasonsList(
                            comments: context
                                    .read<SelectedProjectBloc>()
                                    .state
                                    .whenOrNull(
                                      selected: (wf) => wf.transactions
                                          ?.expand((tx) =>
                                              tx.comments ?? <Comment>[])
                                          .toList(),
                                    ) ??
                                <Comment>[],
                            excludeStandardTypes: true,
                          ),
                        ],
                      ),
                    ],
                    const SizedBox(height: spacer4),
                    Text(
                      "Rejection List",
                      style: textTheme.headingXl
                          .copyWith(color: theme.colorTheme.primary.primary2),
                    ),
                    const SizedBox(height: spacer1),
                    DigitCard(children: [
                      DigitCheckbox(
                          label: 'Inverter Rejection reason 1',
                          onChanged: (value) {
                            setState(() {
                              rejection1 = value;
                            });
                          }),
                      const SizedBox(height: spacer1),
                      DigitCheckbox(
                          label: 'Inverter Rejection reason 2',
                          onChanged: (value) {
                            setState(() {
                              rejection2 = value;
                            });
                          }),
                      const SizedBox(height: spacer1),
                      DigitCheckbox(
                          label: 'Panel  Rejection reason 1',
                          onChanged: (value) {
                            setState(() {
                              rejection3 = value;
                            });
                          }),
                    ])
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

class RejectedEditAssetSummary extends StatelessWidget {
  const RejectedEditAssetSummary({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    // 1) Grab the ProjectWorkflow to extract comments
    final workflow = context
        .watch<SelectedProjectBloc>()
        .state
        .whenOrNull(selected: (wf) => wf);

    // 2) Build a map: AssetType (title case) → List<Comment>
    final commentsByType = <String, List<Comment>>{};
    if (workflow?.transactions != null) {
      for (final tx in workflow!.transactions!) {
        for (final c in tx.comments ?? []) {
          final t =
              c.assetType != null ? ReCase(c.assetType!).titleCase : 'Unknown';
          commentsByType.putIfAbsent(t, () => []).add(c);
        }
      }
    }

    return BlocBuilder<OverallAssetSummaryBloc, OverallAssetSummaryState>(
      builder: (context, state) {
        final isLoading = state.maybeWhen(
          initial: () => true,
          loading: () => true,
          orElse: () => false,
        );
        final error = state.maybeWhen(error: (msg) => msg, orElse: () => null);

        int battery = 0, inverter = 0, panel = 0;
        state.maybeWhen(
            loaded: (b, i, p) {
              battery = b;
              inverter = i;
              panel = p;
            },
            orElse: () {});

        if (isLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (error != null) {
          return DigitCard(
            children: [
              Center(
                child: Text(
                  'Error loading counts:\n$error',
                  style: textTheme.bodyL
                      .copyWith(color: theme.colorTheme.alert.error),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: spacer6),
            ],
          );
        }
        return DigitCard(children: [
          _oneCard(context, 'Inverter', inverter, commentsByType['Inverter']),
          _oneCard(context, 'Battery', battery, commentsByType['Battery']),
          _oneCard(context, 'Panel', panel, commentsByType['Panel'],
              isLast: true),
        ]);
      },
    );
  }

  Widget _oneCard(
      BuildContext ctx, String assetType, int count, List<Comment>? comments,
      {bool isLast = false}) {
    final theme = Theme.of(ctx);
    final textTheme = theme.digitTextTheme(ctx);
    final has = comments != null && comments.isNotEmpty;

    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Stack(alignment: Alignment.center, children: [
        Align(
            alignment: Alignment.centerLeft,
            child: Text('${assetType}s', style: textTheme.headingS)),
        Center(child: Text('$count', style: textTheme.bodyL)),
      ]),
      if (has) RejectionReasonsList(comments: comments),
      if (count > 0) ...[
        const SizedBox(height: spacer5),
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          Expanded(
            child: DigitButton(
              label: 'Edit',
              onPressed: () {
                ctx
                    .read<AssetTypeBloc>()
                    .add(AssetTypeEvent.typeSelected(assetType.toUpperCase()));
                ctx.router.push(const AssetSummaryRoute());
              },
              type: DigitButtonType.secondary,
              size: DigitButtonSize.medium,
              prefixIcon: Icons.edit,
              mainAxisSize: MainAxisSize.min,
            ),
          ),
        ]),
      ],
      const SizedBox(height: spacer5),
      if (!isLast) const DigitDivider(dividerType: DividerType.small),
    ]);
  }
}

/// Extracted widget to render a card of rejection reasons.
class RejectionReasonsList extends StatelessWidget {
  final List<Comment>? comments;
  final bool excludeStandardTypes;

  const RejectionReasonsList({
    Key? key,
    required this.comments,
    this.excludeStandardTypes = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    if (comments == null || comments!.isEmpty) {
      return const SizedBox.shrink();
    }

    final filtered = excludeStandardTypes
        ? comments!.where((c) {
            final t = c.assetType?.toLowerCase();
            return t != 'inverter' && t != 'battery' && t != 'panel';
          }).toList()
        : comments!;

    if (filtered.isEmpty) return const SizedBox.shrink();

    return Column(
      children: [
        const SizedBox(height: spacer2),
        Container(
          width: double.infinity,
          decoration: BoxDecoration(
            color: theme.colorTheme.paper.secondary,
            border: Border.all(color: theme.colorTheme.generic.divider),
            borderRadius: BorderRadius.circular(spacer1),
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(
                horizontal: spacer3, vertical: spacer4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Rejection Reason(s)',
                  style: textTheme.headingS
                      .copyWith(color: theme.colorTheme.text.primary),
                ),
                const SizedBox(height: spacer5),
                for (var i = 0; i < filtered.length; i++) ...[
                  _oneReason(context, filtered[i], i + 1),
                  if (i < filtered.length - 1) const SizedBox(height: spacer4),
                ],
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _oneReason(BuildContext context, Comment comment, int index) {
    final theme = Theme.of(context);
    final labelStyle = theme
        .digitTextTheme(context)
        .label
        .copyWith(color: theme.colorTheme.primary.primary2);
    final valueStyle = theme
        .digitTextTheme(context)
        .label
        .copyWith(color: theme.colorTheme.text.primary);

    final reason = comment.reason; // may be null
    final details = comment.displayComment;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          decoration: BoxDecoration(
            border: Border.all(color: theme.colorTheme.primary.primary2),
            borderRadius: BorderRadius.circular(spacer2),
            color: theme.colorTheme.paper.primary,
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer1, horizontal: spacer3),
            child: Text(
              reason == null ? 'Reason $index' : '$reason',
              style: labelStyle,
            ),
          ),
        ),
        const SizedBox(height: spacer2),
        Text(details, style: valueStyle),
      ],
    );
  }
}
