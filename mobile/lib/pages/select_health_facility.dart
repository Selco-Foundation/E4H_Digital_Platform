import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/app_init/app_init.dart';
import '../blocs/cache_activity_facility_asset/cache_activity_facility_asset.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_activity_facility_asset.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/mdms/mdms.dart';
import '../model/solution_design_type/solution_design_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class SelectHealthFacilityPage extends StatefulWidget {
  const SelectHealthFacilityPage({super.key});

  @override
  State<SelectHealthFacilityPage> createState() =>
      _SelectHealthFacilityPageState();
}

class _SelectHealthFacilityPageState extends State<SelectHealthFacilityPage> {
  String? _sortDirection;
  String _searchQuery = '';

  final Map<String, Map<String, int>> _progress = {};

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _fetchProject();
    });
  }

  void _fetchProject() {
    final userType = context.read<UserTypeBloc>().state;
    final statuses = [
      userType.maybeWhen(
        supervisor: () =>
            WORKFLOW_STATUS_FIELD_SUPERVISOR.ASSIGNED_TO_FIELD_SUPERVISOR.name,
        orElse: () => WORKFLOW_STATUS_FIELD_STAFF.ASSIGNED_TO_FIELD_STAFF.name,
      ),
    ];

    if (_searchQuery.isNotEmpty) {
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchActivityFacilityBySearch(
              query: _searchQuery,
              workflowStatuses: statuses,
            ),
          );
    } else if (_sortDirection != null) {
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchActivityFacilitySorted(
              workflowStatuses: statuses,
              sortDirection: _sortDirection!,
            ),
          );
    } else {
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchActivityFacilityByWorkflow(
                workflowStatuses: statuses),
          );
    }
  }

  void _handleProjectTap(ActivityFacilityWorkflow project) {
    context.read<CacheActivityFacilityAssetBloc>().add(
          CacheActivityFacilityAssetEvent.add(CacheActivityFacilityAsset(
              activityFacilityId: project.activityFacility.id)),
        );
    context
        .read<SelectedActivityFacilityBloc>()
        .add(SelectedActivityFacilityEvent.select(project));
    context.router.push(const AssetCountRoute());
  }

  double _fractionForProject(String projectId) {
    final isSupervisor = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => true,
          orElse: () => false,
        );
    final maxStepsPerType = isSupervisor ? 6.0 : 5.0;

    const types = ['inverter', 'battery', 'panel'];
    final map = _progress[projectId] ?? const {};

    double sum = 0.0;
    for (final t in types) {
      final steps = (map[t] ?? 0).clamp(0, maxStepsPerType.toInt()).toDouble();
      sum += steps / maxStepsPerType;
    }
    return (sum / types.length).clamp(0.0, 1.0);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: BlocListener<CacheAssetCountBloc, CacheAssetCountState>(
        listener: (context, st) {
          st.maybeWhen(
            loaded: (list) {
              bool changed = false;
              for (final e in list) {
                final pid = e.activityFacilityId;
                final type = (e.assetType ?? '').toLowerCase().trim();
                final p = (e.progress ?? 0);
                if (pid.isEmpty || type.isEmpty) continue;

                final byType = _progress.putIfAbsent(pid, () => {});
                final prev = byType[type] ?? 0;
                if (p > prev) {
                  byType[type] = p; // keep best we’ve seen
                  changed = true;
                }
              }
              if (changed) setState(() {});
            },
            orElse: () {},
          );
        },
        child: ScrollableContent(
          backgroundColor: theme.colorTheme.generic.background,
          children: [
            const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            Column(
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(
                      horizontal: spacer4, vertical: spacer2),
                  child: _buildSearchAndSortControls(textTheme, theme),
                ),
                const SizedBox(height: spacer2),
                BlocBuilder<ActivityFacilityBloc, ActivityFacilityState>(
                  builder: (context, state) {
                    return state.maybeWhen(
                      initial: () => _loadingIndicator(),
                      loading: () => _loadingIndicator(),
                      fetched: (projectList) {
                        for (final p in projectList) {
                          for (final t in const [
                            'inverter',
                            'battery',
                            'panel'
                          ]) {
                            context.read<CacheAssetCountBloc>().add(
                                CacheAssetCountEvent.get(
                                    p.activityFacility.id, t));
                          }
                        }
                        return Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            _buildProjectList(projectList),
                          ],
                        );
                      },
                      searchLoading: () => _loadingIndicator(),
                      searchResults: (searchList) {
                        for (final p in searchList) {
                          for (final t in const [
                            'inverter',
                            'battery',
                            'panel'
                          ]) {
                            context.read<CacheAssetCountBloc>().add(
                                CacheAssetCountEvent.get(
                                    p.activityFacility.id, t));
                          }
                        }
                        return _buildProjectList(searchList);
                      },
                      orElse: () => const SizedBox.shrink(),
                    );
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _loadingIndicator() => const Center(
        child: Center(
          child: Padding(
            padding: EdgeInsets.only(top: spacer8),
            child: CircularProgressIndicator(),
          ),
        ),
      );

  Widget _buildSearchAndSortControls(
      DigitTextTheme textTheme, ThemeData theme) {
    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Select Health Facility',
              style: textTheme.bodyL
                  .copyWith(color: theme.colorTheme.text.primary),
            ),
            const SizedBox(height: spacer1),
            Row(
              children: [
                Expanded(
                  child: DigitSearchFormInput(
                    suffixIcon: Icons.search,
                    onChange: (text) {
                      setState(() {
                        _searchQuery = text;
                        _sortDirection = null;
                      });
                      _fetchProject();
                    },
                  ),
                ),
                const SizedBox(width: spacer2),
                GestureDetector(
                  onTap: () => _showSortPopup(textTheme, theme),
                  child: Icon(
                    Icons.import_export,
                    color: theme.colorTheme.primary.primary1,
                    size: spacer8,
                  ),
                ),
              ],
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildProjectList(List<ActivityFacilityWorkflow> projects) {
    if (projects.isEmpty) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: spacer4),
        child: Center(child: Text('No projects found')),
      );
    }
    return Padding(
      padding:
          const EdgeInsets.symmetric(horizontal: spacer4, vertical: spacer2),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          for (final project in projects) ...[
            InstallationReportCard(
              onPress: () => _handleProjectTap(project),
              activityFacility: project,
              projectId: project.activityFacility.id,
              title: project.activityFacility.facility?.facilityName ?? '—',
              dateAssigned:
                  project.activityFacility.scheduledAt ?? DateTime.now(),
              status: project.status ?? '—',
              systemDesignCode: project.activityFacility.facility
                      ?.facilityDetails?.solar_solution_design_type ??
                  '',
              fraction: _fractionForProject(project.activityFacility.id),
            ),
            const SizedBox(height: spacer5),
          ],
        ],
      ),
    );
  }

  void _showSortPopup(DigitTextTheme textTheme, ThemeData theme) {
    showCustomPopup(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, popupSetState) => Popup(
          onCrossTap: () => Navigator.of(ctx).pop(),
          title: 'Sort by',
          type: PopUpType.simple,
          actionAlignment: MainAxisAlignment.center,
          additionalWidgets: [
            Text('Submission Date',
                style: textTheme.headingS
                    .copyWith(color: theme.colorTheme.text.primary)),
            RadioList(
              groupValue: _sortDirection ?? '',
              containerPadding:
                  const EdgeInsets.symmetric(horizontal: 0, vertical: spacer2),
              onChanged: (value) =>
                  popupSetState(() => _sortDirection = value.code),
              radioDigitButtons: [
                RadioButtonModel(code: 'DESC', name: 'Newest first'),
                RadioButtonModel(code: 'ASC', name: 'Oldest first'),
              ],
            ),
            Row(
              children: [
                Expanded(
                  child: DigitButton(
                    label: 'Clear',
                    onPressed: () => Navigator.of(ctx).pop(),
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                    mainAxisSize: MainAxisSize.min,
                  ),
                ),
                const SizedBox(width: spacer5),
                Expanded(
                  child: DigitButton(
                    label: 'Sort',
                    isDisabled: _sortDirection == null,
                    onPressed: () {
                      final userType = context.read<UserTypeBloc>().state;
                      final statuses = [
                        userType.maybeWhen(
                          supervisor: () => WORKFLOW_STATUS_FIELD_SUPERVISOR
                              .ASSIGNED_TO_FIELD_SUPERVISOR.name,
                          orElse: () => WORKFLOW_STATUS_FIELD_STAFF
                              .ASSIGNED_TO_FIELD_STAFF.name,
                        ),
                      ];
                      context.read<ActivityFacilityBloc>().add(
                            ActivityFacilityEvent.fetchActivityFacilitySorted(
                              workflowStatuses: statuses,
                              sortDirection: _sortDirection!,
                            ),
                          );
                      Navigator.of(ctx).pop();
                    },
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
    );
  }
}

class InstallationReportCard extends StatelessWidget {
  final ActivityFacilityWorkflow? activityFacility;
  final String? projectId;
  final String? title;
  final String? status;
  final DateTime dateAssigned;
  final String? systemDesignCode;
  final Function() onPress;
  final double fraction;

  const InstallationReportCard({
    super.key,
    this.activityFacility,
    this.projectId,
    this.title,
    this.status,
    required this.dateAssigned,
    this.systemDesignCode,
    required this.onPress,
    required this.fraction,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    String formattedDate = DateFormat('dd/MM/yy').format(dateAssigned);

    return BlocBuilder<AppInitialization, InitState>(
      builder: (context, initState) {
        final List<Mdms<SolutionDesignType>> solutionDesignList =
            initState.maybeWhen(
                orElse: () => <Mdms<SolutionDesignType>>[],
                initialized: (appConfig, assetCount, assetType, system,
                        warranty, brand, solutionDesign, _) =>
                    solutionDesign);

        final code = systemDesignCode ?? '';

        final matchedSystemDesign =
            solutionDesignList.firstWhereOrNull((e) => e.data.code == code);

        final solutionDocsUrl = matchedSystemDesign?.data.url ?? '';

        return DigitCard(
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "$title",
                  style: textTheme.headingL.copyWith(
                    color: theme.colorTheme.text.primary,
                  ),
                ),
                const SizedBox(height: spacer4),
                const DigitDivider(dividerType: DividerType.small),
                Row(
                  children: [
                    Expanded(
                      flex: 2,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: spacer4),
                          Text(
                            'Status',
                            style: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                          ),
                          const SizedBox(height: spacer4),
                          Text(
                            'Date Assigned',
                            style: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                          ),
                          const SizedBox(height: spacer4),
                          Text(
                            'Solution Doc',
                            style: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                          )
                        ],
                      ),
                    ),
                    const SizedBox(width: spacer12),
                    Expanded(
                      flex: 3,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: spacer4),
                          Text(
                            context.translate('$status'),
                            style: textTheme.bodyL.copyWith(
                              color: theme.colorTheme.text.primary,
                            ),
                            softWrap: true,
                            overflow: TextOverflow.visible,
                          ),
                          const SizedBox(height: spacer4),
                          Text(
                            formattedDate,
                            style: textTheme.bodyL.copyWith(
                              color: theme.colorTheme.text.primary,
                            ),
                          ),
                          const SizedBox(height: spacer4),
                          Row(
                            children: [
                              Icon(
                                Icons.picture_as_pdf,
                                color: theme.colorTheme.primary.primary1,
                              ),
                              const SizedBox(width: spacer1),
                              Expanded(
                                child: GestureDetector(
                                  onTap: () {
                                    if (solutionDocsUrl.isNotEmpty) {
                                      context.router.push(PdfViewerRoute(
                                          path:
                                              "$fileStoreFileUrl$solutionDocsUrl"));
                                    }
                                  },
                                  child: Text(
                                    "Solution Doc",
                                    style: textTheme.bodyL.copyWith(
                                      color: theme.colorTheme.text.disabled,
                                      fontSize: spacer3,
                                    ),
                                    softWrap: true,
                                    overflow: TextOverflow.visible,
                                  ),
                                ),
                              )
                            ],
                          )
                        ],
                      ),
                    ),
                  ],
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: spacer4),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Expanded(
                        child: LinearProgressIndicator(
                          borderRadius: BorderRadius.circular(spacer1),
                          backgroundColor: theme.colorTheme.generic.background,
                          valueColor: AlwaysStoppedAnimation<Color>(
                            theme.colorTheme.alert.success,
                          ),
                          value: fraction,
                          minHeight: spacer3,
                        ),
                      ),
                      const SizedBox(width: spacer3),
                      Text(
                        '${(fraction * 100).round()}%',
                        style: textTheme.bodyS.copyWith(
                          color: theme.colorTheme.text.secondary,
                        ),
                      )
                    ],
                  ),
                ),
                DigitButton(
                  mainAxisSize: MainAxisSize.max,
                  label: (fraction * 100).round() > 0
                      ? 'Resume Installation Report'
                      : 'Start Installation Report',
                  onPressed: onPress,
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                ),
                const SizedBox(height: spacer4),
                DigitButton(
                  mainAxisSize: MainAxisSize.max,
                  label: 'Submit For Approval',
                  onPressed: () {
                    context.read<SelectedActivityFacilityBloc>().add(
                        SelectedActivityFacilityEvent.select(
                            activityFacility!));
                    context.router.push(OverallAssetSummaryRoute(
                        refresh: DateTime.now().millisecondsSinceEpoch));
                  },
                  isDisabled: (fraction * 100).round() >= 98 ? false : true,
                  type: DigitButtonType.secondary,
                  size: DigitButtonSize.large,
                ),
              ],
            )
          ],
        );
      },
    );
  }
}
