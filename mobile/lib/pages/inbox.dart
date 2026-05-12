import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/cards/inbox_report_rejected_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class InboxPage extends StatefulWidget {
  const InboxPage({super.key});

  @override
  State<InboxPage> createState() => _InboxPageState();
}

class _InboxPageState extends State<InboxPage> {
  static const _scrollThreshold = 200.0;

  int _selectedTabIndex = 0;
  String _searchQuery = '';
  String? _sortDirection;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final userState = context.read<UserTypeBloc>().state;
      if (userState.maybeWhen(supervisor: () => true, orElse: () => false)) {
        context.read<InboxTypeBloc>().add(const InboxTypeEvent.typeSelected(0));
      } else {
        context.read<InboxTypeBloc>().add(const InboxTypeEvent.typeSelected(1));
      }
      _fetchProjects(userState, _selectedTabIndex);
    });
  }

  void _fetchProjects(UserTypeState userState, int tabIndex) {
    context
        .read<ReportTypeBloc>()
        .add(const ReportTypeEvent.typeSelected("inbox"));
    final workflowStatuses = _workflowStatusesForTab(userState, tabIndex);
    final isSupervisor =
        userState.maybeWhen(supervisor: () => true, orElse: () => false);
    if (isSupervisor && tabIndex == 0) {
      context
          .read<ReportTypeBloc>()
          .add(const ReportTypeEvent.typeSelected("send-back"));
    }

    if (_searchQuery.isNotEmpty) {
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchActivityFacilityBySearch(
              query: _searchQuery,
              workflowStatuses: workflowStatuses,
            ),
          );
    } else if (_sortDirection != null) {
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchActivityFacilitySorted(
              workflowStatuses: workflowStatuses,
              sortDirection: _sortDirection!,
            ),
          );
    } else {
      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchActivityFacilityByWorkflow(
                workflowStatuses: workflowStatuses),
          );
    }
  }

  List<String> _workflowStatusesForTab(UserTypeState userState, int tabIndex) {
    return userState.maybeWhen(
      supervisor: () {
        if (tabIndex == 0) {
          return [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_FIELD_STAFF.name,
          ];
        } else if (tabIndex == 1) {
          return [WORKFLOW_STATUS_FIELD_SUPERVISOR.REJECTED_BY_QC_SPOC.name];
        }
        return [WORKFLOW_STATUS_FIELD_SUPERVISOR.APPROVED_BY_QC_SPOC.name];
      },
      orElse: () {
        if (tabIndex == 0) {
          return [
            WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_FIELD_SUPERVISOR.name,
            WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_QC_SPOC.name,
          ];
        }
        return [
          WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_SUPERVISOR.name,
          WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_QC_SPOC.name,
          WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
          WORKFLOW_STATUS_FIELD_STAFF.PENDING_APPROVAL_FLAGGED_FOR_QC.name,
        ];
      },
    );
  }

  void _tryLoadMore(UserTypeState userState) {
    context.read<ActivityFacilityBloc>().add(
          ActivityFacilityEvent.loadMoreActivityFacility(
            workflowStatuses:
                _workflowStatusesForTab(userState, _selectedTabIndex),
            query: _searchQuery.isNotEmpty ? _searchQuery : null,
            sortDirection: _sortDirection,
          ),
        );
  }

  void _onTabChanged(int index, UserTypeState userState) {
    setState(() {
      _selectedTabIndex = index;
      _searchQuery = '';
      _sortDirection = null;
    });

    context.read<InboxTypeBloc>().add(
          userState.maybeWhen(
            supervisor: () => InboxTypeEvent.typeSelected(index),
            orElse: () => InboxTypeEvent.typeSelected(index + 1),
          ),
        );

    _fetchProjects(userState, index);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
        final tabs = userState.maybeWhen(
          supervisor: () => [
            context.translate(i18.inbox.forReview),
            context.translate(i18.inbox.rejected),
            context.translate(i18.inbox.approved),
          ],
          orElse: () => [
            context.translate(i18.inbox.rejected),
            context.translate(i18.inbox.approved),
          ],
        );

        return NotificationListener<ScrollNotification>(
          onNotification: (notification) {
            if (notification is ScrollUpdateNotification) {
              final max = notification.metrics.maxScrollExtent;
              final current = notification.metrics.pixels;
              if (current > max - _scrollThreshold) {
                _tryLoadMore(userState);
              }
            }
            return false;
          },
          child: Scaffold(
            body: ScrollableContent(
              backgroundColor: theme.colorTheme.generic.background,
              header: const BackNavigationHelpHeaderWidget(
                showBackNavigation: true,
                showHelp: false,
              ),
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(
                      vertical: spacer2, horizontal: spacer4),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(
                            context.translate(i18.inbox.title),
                            style: textTheme.headingXl.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                        ],
                      ),
                      const SizedBox(height: spacer4),
                      SizedBox(
                        height: spacer12 + spacer1,
                        child: LayoutBuilder(
                          builder: (context, constraints) {
                            return DigitTabBar(
                              tabs: tabs,
                              initialIndex: _selectedTabIndex,
                              onTabSelected: (index) =>
                                  _onTabChanged(index, userState),
                              tabBarThemeData:
                                  DigitTabBarThemeData.defaultTheme(context)
                                      .copyWith(
                                          tabWidth: constraints.maxWidth /
                                              tabs.length,
                                          padding: EdgeInsets.zero),
                            );
                          },
                        ),
                      ),
                      const SizedBox(height: spacer4),
                      DigitCard(
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: DigitSearchFormInput(
                                  innerLabel: context
                                      .translate(i18.inbox.searchHealthFacility),
                                  suffixIcon: Icons.search,
                                  onChange: (text) {
                                    setState(() {
                                      _searchQuery = text;
                                      _sortDirection = null;
                                    });
                                    _fetchProjects(
                                        userState, _selectedTabIndex);
                                  },
                                  iconColor: const Light().primary2,
                                  enableBorder: OutlineInputBorder(
                                    borderRadius:
                                        BorderRadius.circular(spacer1),
                                    borderSide: BorderSide(
                                        color: theme.colorTheme.text.secondary),
                                  ),
                                  focusBorder: OutlineInputBorder(
                                    borderRadius:
                                        BorderRadius.circular(spacer1),
                                    borderSide: BorderSide(
                                        color: theme.colorTheme.text.secondary),
                                  ),
                                ),
                              ),
                              GestureDetector(
                                onTap: () =>
                                    _showSortPopup(textTheme, theme, userState),
                                child: Row(
                                  children: [
                                    Icon(
                                      Icons.import_export,
                                      color: theme.colorTheme.primary.primary1,
                                      size: spacer8,
                                    ),
                                    Text(context.translate(i18.common.sort),
                                        style: textTheme.headingS.copyWith(
                                            color: theme
                                                .colorTheme.primary.primary1))
                                  ],
                                ),
                              ),
                            ],
                          )
                        ],
                      ),
                      const SizedBox(height: spacer4),
                      BlocBuilder<ActivityFacilityBloc, ActivityFacilityState>(
                        builder: (context, projectState) {
                          return projectState.maybeWhen(
                            initial: () => _loadingIndicator(),
                            loading: () => _loadingIndicator(),
                            paginatedLoaded: (items, hasMore, totalCount,
                                    fromCache, isLoadingMore,
                                    rawFetchedCount) =>
                                _buildList(
                              items,
                              userState,
                              isLoadingMore: isLoadingMore,
                            ),
                            searchLoading: () => _loadingIndicator(),
                            orElse: () => const SizedBox.shrink(),
                          );
                        },
                      ),
                      const SizedBox(height: spacer5),
                    ],
                  ),
                ),
              ],
            ),
          ),
        );
      },
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

  Widget _buildList(
    List<ActivityFacilityWorkflow> projectsList,
    UserTypeState userState, {
    bool isLoadingMore = false,
  }) {
    if (projectsList.isEmpty) {
      return Center(
        child: Text(context.translate(i18.inbox.noProjectsToDisplay)),
      );
    }
    return Column(
      children: [
        for (final project in projectsList)
          Column(
            children: [
              BlocBuilder<InboxTypeBloc, InboxTypeState>(
                builder: (context, inboxState) {
                  final locality = parseBoundaryCodeLocality(
                    project.activityFacility.facility?.boundaryCode,
                  );
                  return inboxState.when(
                    submitted: () => InboxReportCard(
                        onPress: () {
                          context.read<SelectedActivityFacilityBloc>().add(
                                SelectedActivityFacilityEvent.select(project),
                              );
                          context.router.push(InboxAssetSummaryRoute(
                              refresh: DateTime.now().millisecondsSinceEpoch));
                        },
                        title:
                            project.activityFacility.facility?.facilityName ??
                                '---',
                        dateAssigned:
                            project.workflow?.auditDetails?.lastModifiedTime ??
                                DateTime.now(),
                        status: project.status ?? '---',
                        state: locality.state,
                        district: locality.district,
                        block: locality.block),
                    rejected: () => InboxReportRejectedCard(
                      title: project.activityFacility.facility?.facilityName ??
                          '---',
                      status: project.status ?? '---',
                      state: locality.state,
                      district: locality.district,
                      block: locality.block,
                      dateAssigned:
                          project.workflow?.auditDetails?.lastModifiedTime ??
                              DateTime.now(),
                      onPress: () {
                        context.read<SelectedActivityFacilityBloc>().add(
                              SelectedActivityFacilityEvent.select(project),
                            );
                        context.router.push(SubmitForApprovalRoute(
                            refresh: DateTime.now().millisecondsSinceEpoch));
                      },
                    ),
                    approved: () => InboxReportCard(
                        onPress: () {
                          context.read<SelectedActivityFacilityBloc>().add(
                                SelectedActivityFacilityEvent.select(project),
                              );
                          context.router.push(InboxAssetSummaryRoute(
                              refresh: DateTime.now().millisecondsSinceEpoch));
                        },
                        title:
                            project.activityFacility.facility?.facilityName ??
                                '---',
                        dateAssigned:
                            project.workflow?.auditDetails?.lastModifiedTime ??
                                DateTime.now(),
                        status: project.status ?? '---',
                        state: locality.state,
                        district: locality.district,
                        block: locality.block),
                  );
                },
              ),
              const SizedBox(height: spacer5),
            ],
          ),
        if (isLoadingMore)
          const Padding(
            padding: EdgeInsets.only(bottom: spacer4),
            child: Center(
              child: CircularProgressIndicator(),
            ),
          ),
      ],
    );
  }

  void _showSortPopup(
      DigitTextTheme textTheme, ThemeData theme, UserTypeState userState) {
    showCustomPopup(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, popupSetState) => Popup(
          onCrossTap: () => Navigator.of(ctx).pop(),
          title: context.translate(i18.common.sortBy),
          type: PopUpType.simple,
          additionalWidgets: [
            RadioList(
              groupValue: _sortDirection ?? '',
              containerPadding: const EdgeInsets.symmetric(vertical: spacer2),
              radioDigitButtons: [
                RadioButtonModel(
                    code: 'DESC',
                    name: context.translate(i18.common.newestFirst)),
                RadioButtonModel(
                    code: 'ASC',
                    name: context.translate(i18.common.oldestFirst)),
              ],
              onChanged: (val) =>
                  popupSetState(() => _sortDirection = val.code),
            ),
            Row(
              children: [
                Expanded(
                  child: DigitButton(
                    label: context.translate(i18.common.clear),
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                    onPressed: () {
                      setState(() {
                        _sortDirection = null;
                        _searchQuery = '';
                      });
                      Navigator.of(ctx).pop();
                      _fetchProjects(userState, _selectedTabIndex);
                    },
                  ),
                ),
                const SizedBox(width: spacer5),
                Expanded(
                  child: DigitButton(
                    type: DigitButtonType.primary,
                    size: DigitButtonSize.large,
                    label: context.translate(i18.common.sort),
                    isDisabled: _sortDirection == null,
                    onPressed: () {
                      Navigator.of(ctx).pop();
                      _fetchProjects(userState, _selectedTabIndex);
                    },
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
