import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/atoms/digit_search_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcInboxPage extends StatefulWidget {
  const AmcInboxPage({super.key});

  @override
  State<AmcInboxPage> createState() => _AmcInboxPageState();
}

class _AmcInboxPageState extends State<AmcInboxPage> {
  int _selectedTabIndex = 0;
  String _searchQuery = '';
  String? _sortDirection;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<InboxTypeBloc>().add(const InboxTypeEvent.typeSelected(1));
    });
  }

  void _fetchProjects(UserTypeState userState, int tabIndex) {
    context
        .read<ReportTypeBloc>()
        .add(const ReportTypeEvent.typeSelected("inbox"));
    List<String> workflowStatuses = [];
    if (tabIndex == 0) {
      workflowStatuses = [
        WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_FIELD_SUPERVISOR.name,
        WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_QC_SPOC.name
      ];
    } else if (tabIndex == 1) {
      workflowStatuses = [
        WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_SUPERVISOR.name,
        WORKFLOW_STATUS_FIELD_STAFF.APPROVED_BY_QC_SPOC.name,
        WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
        WORKFLOW_STATUS_FIELD_STAFF.PENDING_APPROVAL_FLAGGED_FOR_QC.name,
      ];
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

  void _onTabChanged(int index, UserTypeState userState) {
    setState(() {
      _selectedTabIndex = index;
      _searchQuery = '';
      _sortDirection = null;
    });

    context.read<InboxTypeBloc>().add(InboxTypeEvent.typeSelected(index + 1));
    _fetchProjects(userState, index);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
        final tabs = ['Rejected', 'Approved'];

        return Scaffold(
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
                          'Inbox',
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
                                        tabWidth:
                                            constraints.maxWidth / tabs.length,
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
                                innerLabel: "Search Health Facility",
                                suffixIcon: Icons.search,
                                onChange: (text) {
                                  setState(() {
                                    _searchQuery = text;
                                    _sortDirection = null;
                                  });
                                  _fetchProjects(userState, _selectedTabIndex);
                                },
                                iconColor: const Light().primary2,
                                enableBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(spacer1),
                                  borderSide: BorderSide(
                                      color: theme.colorTheme.text.secondary),
                                ),
                                focusBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(spacer1),
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
                                  Text("Sort",
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
                          fetched: (projectsList) =>
                              _buildList(projectsList, userState),
                          searchResults: (list) => _buildList(list, userState),
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
      List<ActivityFacilityWorkflow> projectsList, UserTypeState userState) {
    if (projectsList.isEmpty) {
      return const Center(
        child: Text('No Projects to display'),
      );
    }
    return Column(
      children: [
        for (final project in projectsList)
          Column(
            children: [
              BlocBuilder<InboxTypeBloc, InboxTypeState>(
                builder: (context, inboxState) {
                  return inboxState.when(
                    submitted: () => const SizedBox.shrink(),
                    rejected: () => InboxReportCard(
                      isAmc: true,
                      title: project.activityFacility.facility?.facilityName ??
                          '---',
                      status: project.status ?? '---',
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
                        isAmc: true,
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
                        status: project.status ?? '---'),
                  );
                },
              ),
              const SizedBox(height: spacer5),
            ],
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
          title: 'Sort by',
          type: PopUpType.simple,
          additionalWidgets: [
            RadioList(
              groupValue: _sortDirection ?? '',
              containerPadding: const EdgeInsets.symmetric(vertical: spacer2),
              radioDigitButtons: [
                RadioButtonModel(code: 'DESC', name: 'Newest first'),
                RadioButtonModel(code: 'ASC', name: 'Oldest first'),
              ],
              onChanged: (val) =>
                  popupSetState(() => _sortDirection = val.code),
            ),
            Row(
              children: [
                Expanded(
                  child: DigitButton(
                    label: 'Clear',
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
                    label: 'Sort',
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
