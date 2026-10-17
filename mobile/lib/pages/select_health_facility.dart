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
import '../repositories/asset_submission_eligibility_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/cards/report_detail_row.dart';
import '../widgets/bookmarks/report_bookmarks.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class SelectHealthFacilityPage extends StatefulWidget {
  final bool bookmarksOnly;
  const SelectHealthFacilityPage({super.key, this.bookmarksOnly = false});

  @override
  State<SelectHealthFacilityPage> createState() =>
      _SelectHealthFacilityPageState();
}

class _SelectHealthFacilityPageState extends State<SelectHealthFacilityPage>
    with
        ReportBookmarksState<SelectHealthFacilityPage,
            ActivityFacilityWorkflow> {
  static const _scrollThreshold = 200.0;

  String? _sortDirection;
  bool get _bookmarksOnly => _sortDirection == 'BOOKMARKED';
  String _searchQuery = '';

  final Map<String, Map<String, int>> _progress = {};

  @override
  void initState() {
    super.initState();
    if (widget.bookmarksOnly) _sortDirection = 'BOOKMARKED';
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      bookmarks =
          reportBookmarksFor<ActivityFacilityWorkflow>(context, amc: false);
      bookmarkSaveFailedKey = i18.installationBookmarks.saveFailed;
      _reloadBookmarks();
      if (!_bookmarksOnly) _fetchProject();
    });
  }

  Future<void> _reloadBookmarks() async {
    await loadBookmarks(
        only: _bookmarksOnly, query: _searchQuery, sortOrder: 'DESC');
    if (!mounted || !_bookmarksOnly) return;
    for (final project in bookmarkedItems) {
      for (final type in const ['inverter', 'battery', 'panel']) {
        context
            .read<CacheAssetCountBloc>()
            .add(CacheAssetCountEvent.get(project.activityFacility.id, type));
      }
    }
  }

  void _fetchProject() {
    if (_bookmarksOnly) {
      _reloadBookmarks();
      return;
    }
    final statuses = _workflowStatuses();

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

  List<String> _workflowStatuses() {
    return [
      WORKFLOW_STATUS_FIELD_SUPERVISOR.ASSIGNED_TO_FIELD_SUPERVISOR.name,
      WORKFLOW_STATUS_FIELD_STAFF.ASSIGNED_TO_FIELD_STAFF.name,
    ];
  }

  void _tryLoadMore() {
    if (_bookmarksOnly) return;
    context.read<ActivityFacilityBloc>().add(
          ActivityFacilityEvent.loadMoreActivityFacility(
            workflowStatuses: _workflowStatuses(),
            query: _searchQuery.isNotEmpty ? _searchQuery : null,
            sortDirection: _sortDirection,
          ),
        );
  }

  Future<void> _handleProjectTap(ActivityFacilityWorkflow project) async {
    context.read<CacheActivityFacilityAssetBloc>().add(
          CacheActivityFacilityAssetEvent.add(CacheActivityFacilityAsset(
              activityFacilityId: project.activityFacility.id)),
        );
    context
        .read<SelectedActivityFacilityBloc>()
        .add(SelectedActivityFacilityEvent.select(project));
    // context.router.push(const AssetCountRoute());
    await context.router.push(OverallAssetSummaryRoute(
        refresh: DateTime.now().millisecondsSinceEpoch));
    if (mounted) await _reloadBookmarks();
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
                final type = e.assetType.toLowerCase().trim();
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
        child: NotificationListener<ScrollNotification>(
          onNotification: (notification) {
            if (notification is ScrollUpdateNotification) {
              final max = notification.metrics.maxScrollExtent;
              final current = notification.metrics.pixels;
              if (current > max - _scrollThreshold) {
                _tryLoadMore();
              }
            }
            return false;
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
                  BlocListener<ActivityFacilityBloc, ActivityFacilityState>(
                    listenWhen: (prev, curr) => prev != curr,
                    listener: (context, state) {
                      state.maybeWhen(
                        paginatedLoaded: (items, hasMore, totalCount, fromCache,
                            isLoadingMore, rawFetchedCount) {
                          for (final p in items) {
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
                        },
                        orElse: () {},
                      );
                    },
                    child: BlocBuilder<ActivityFacilityBloc,
                        ActivityFacilityState>(
                      builder: (context, state) {
                        if (_bookmarksOnly) {
                          if (bookmarksLoading) return _loadingIndicator();
                          if (bookmarksFailed) {
                            return bookmarkLoadError(_reloadBookmarks);
                          }
                          return _buildProjectList(bookmarkedItems
                              .where((project) =>
                                  _workflowStatuses().contains(project.status))
                              .toList());
                        }
                        return Column(children: [
                          if (bookmarksFailed)
                            bookmarkLoadError(_reloadBookmarks),
                          state.maybeWhen(
                            initial: () => _loadingIndicator(),
                            loading: () => _loadingIndicator(),
                            paginatedLoaded: (items, hasMore, totalCount,
                                fromCache, isLoadingMore, rawFetchedCount) {
                              return Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  _buildProjectList(items),
                                  if (isLoadingMore)
                                    const Padding(
                                      padding: EdgeInsets.only(bottom: spacer4),
                                      child: Center(
                                        child: CircularProgressIndicator(),
                                      ),
                                    ),
                                ],
                              );
                            },
                            searchLoading: () => _loadingIndicator(),
                            orElse: () => const SizedBox.shrink(),
                          ),
                        ]);
                      },
                    ),
                  ),
                ],
              ),
            ],
          ),
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
              context.translate(i18.selectHealthFacility.title),
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
                        if (!_bookmarksOnly) _sortDirection = null;
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
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: spacer4),
        child: Center(
            child: Text(context.translate(_searchQuery.trim().isNotEmpty
                ? i18.common.noMatchingFacilitiesFound
                : _bookmarksOnly
                    ? i18.installationBookmarks.empty
                    : i18.selectHealthFacility.noProjectsFound))),
      );
    }
    return Padding(
      padding:
          const EdgeInsets.symmetric(horizontal: spacer4, vertical: spacer2),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          for (final project in projects) ...[
            Builder(builder: (context) {
              final locality = parseBoundaryCodeLocality(
                project.activityFacility.facility?.boundaryCode,
              );
              return InstallationReportCard(
                onPress: () => _handleProjectTap(project),
                isBookmarked:
                    bookmarkIds.contains(project.activityFacility.id.trim()),
                isSavingBookmark: !bookmarksLoaded ||
                    savingBookmarks
                        .contains(project.activityFacility.id.trim()),
                onToggleBookmark: project.activityFacility.id.trim().isEmpty
                    ? null
                    : () => toggleBookmark(project, reload: _reloadBookmarks),
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
                state: locality.state,
                district: locality.district,
                block: locality.block,
              );
            }),
            const SizedBox(height: spacer5),
          ],
        ],
      ),
    );
  }

  void _showSortPopup(DigitTextTheme textTheme, ThemeData theme) {
    var selectedFilter = _sortDirection;
    showCustomPopup(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, popupSetState) => Popup(
          onCrossTap: () => Navigator.of(ctx).pop(),
          title: context.translate(i18.common.sortBy),
          type: PopUpType.simple,
          actionAlignment: MainAxisAlignment.center,
          additionalWidgets: [
            RadioList(
              groupValue: selectedFilter ?? '',
              containerPadding:
                  const EdgeInsets.symmetric(horizontal: 0, vertical: spacer2),
              onChanged: (value) =>
                  popupSetState(() => selectedFilter = value.code),
              radioDigitButtons: [
                RadioButtonModel(
                    code: 'DESC',
                    name: context.translate(i18.common.newestFirst)),
                RadioButtonModel(
                    code: 'ASC',
                    name: context.translate(i18.common.oldestFirst)),
                RadioButtonModel(
                    code: 'BOOKMARKED',
                    name: context.translate(i18.installationBookmarks.filter)),
              ],
            ),
            Row(
              children: [
                Expanded(
                  child: DigitButton(
                    label: context.translate(i18.common.clear),
                    onPressed: () {
                      setState(() => _sortDirection = null);
                      Navigator.of(ctx).pop();
                      _fetchProject();
                    },
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                    mainAxisSize: MainAxisSize.min,
                  ),
                ),
                const SizedBox(width: spacer5),
                Expanded(
                  child: DigitButton(
                    label: context.translate(i18.common.sort),
                    isDisabled: selectedFilter == null,
                    onPressed: () {
                      setState(() => _sortDirection = selectedFilter);
                      _fetchProject();
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
  final bool isBookmarked;
  final bool isSavingBookmark;
  final VoidCallback? onToggleBookmark;
  final ActivityFacilityWorkflow? activityFacility;
  final String? projectId;
  final String? title;
  final String? status;
  final String? state;
  final String? district;
  final String? block;
  final DateTime dateAssigned;
  final String? systemDesignCode;
  final Function() onPress;
  final double fraction;

  const InstallationReportCard({
    super.key,
    this.isBookmarked = false,
    this.isSavingBookmark = false,
    this.onToggleBookmark,
    this.activityFacility,
    this.projectId,
    this.title,
    this.status,
    this.state,
    this.district,
    this.block,
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
                Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Expanded(
                      child: Text("$title",
                          style: textTheme.headingL
                              .copyWith(color: theme.colorTheme.text.primary))),
                  if (onToggleBookmark != null)
                    ReportBookmarkButton(
                        selected: isBookmarked,
                        saving: isSavingBookmark,
                        onPressed: onToggleBookmark,
                        addLabelKey: i18.installationBookmarks.add,
                        removeLabelKey: i18.installationBookmarks.remove),
                ]),
                const SizedBox(height: spacer4),
                const DigitDivider(dividerType: DividerType.small),
                ReportDetailRow(
                  label: context.translate(i18.common.status),
                  value: _detailText(
                    context.translate('$status'),
                    textTheme,
                    theme,
                  ),
                ),
                ReportDetailRow(
                  label: context.translate(i18.common.dateAssigned),
                  value: _detailText(formattedDate, textTheme, theme),
                ),
                ReportDetailRow(
                  label: context.translate(i18.common.solutionDoc),
                  value: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
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
                                  path: "$fileStoreFileUrl$solutionDocsUrl"));
                            }
                          },
                          child: Text(
                            context.translate(i18.common.solutionDoc),
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
                  ),
                ),
                ReportDetailRow(
                  label: context.translate(i18.common.state),
                  value: _detailText(_displayValue(state), textTheme, theme),
                ),
                ReportDetailRow(
                  label: context.translate(i18.common.district),
                  value: _detailText(_displayValue(district), textTheme, theme),
                ),
                ReportDetailRow(
                  label: context.translate(i18.common.block),
                  value: _detailText(_displayValue(block), textTheme, theme),
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
                      ? context.translate(
                          i18.selectHealthFacility.resumeInstallationReport)
                      : context.translate(
                          i18.selectHealthFacility.startInstallationReport),
                  onPressed: onPress,
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                ),
                const SizedBox(height: spacer4),
                FutureBuilder<bool>(
                  key: ValueKey(projectId),
                  future: AssetSubmissionEligibilityRepository(
                    context.read<ActivityFacilityBloc>().isar,
                  ).hasReadyAssets(projectId ?? ''),
                  builder: (context, readiness) => DigitButton(
                    mainAxisSize: MainAxisSize.max,
                    label: context
                        .translate(i18.selectHealthFacility.submitForApproval),
                    onPressed: onPress,
                    isDisabled: readiness.data != true,
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                  ),
                ),
              ],
            )
          ],
        );
      },
    );
  }

  String _displayValue(String? value) {
    final normalized = value?.trim() ?? '';
    return normalized.isEmpty ? '---' : normalized;
  }

  Widget _detailText(String value, dynamic textTheme, ThemeData theme) {
    return Text(
      value,
      style: textTheme.bodyL.copyWith(color: theme.colorTheme.text.primary),
      softWrap: true,
      overflow: TextOverflow.visible,
    );
  }
}
