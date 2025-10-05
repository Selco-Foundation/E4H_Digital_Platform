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

import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/project/project.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../model/project_workflow/project_workflow.dart';
import '../router/app_router.dart';
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
  int _selectedTabIndex = 0;
  String _searchQuery = '';
  String? _sortDirection;

  @override
  void initState() {
    super.initState();
    // Trigger initial fetch after first frame
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final userState = context.read<UserTypeBloc>().state;
      // Initialize InboxTypeBloc for the first tab
      if (userState.maybeWhen(supervisor: () => true, orElse: () => false)) {
        // Supervisor first tab index 0
        context.read<InboxTypeBloc>().add(const InboxTypeEvent.typeSelected(0));
      } else {
        // User first tab maps to typeSelected(1)
        context.read<InboxTypeBloc>().add(const InboxTypeEvent.typeSelected(1));
      }
      _fetchProjects(userState, _selectedTabIndex);
    });
  }

  void _fetchProjects(UserTypeState userState, int tabIndex) {
    // Compute workflowStatuses based on role & tabIndex
    context
        .read<ReportTypeBloc>()
        .add(const ReportTypeEvent.typeSelected("inbox"));
    List<String> workflowStatuses = [];
    userState.maybeWhen(
      supervisor: () {
        if (tabIndex == 0) {
          workflowStatuses = [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_FIELD_STAFF.name,
          ];
          context
              .read<ReportTypeBloc>()
              .add(const ReportTypeEvent.typeSelected("send-back"));
        } else if (tabIndex == 1) {
          workflowStatuses = [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.REJECTED_BY_QC_SPOC.name
          ];
        } else if (tabIndex == 2) {
          workflowStatuses = [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.APPROVED_BY_QC_SPOC.name
          ];
        }
      },
      orElse: () {
        // User role
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
          ];
        }
      },
    );

    // Choose search vs sort vs basic fetch && Dispatch fetch + loading state
    if (_searchQuery.isNotEmpty) {
      context.read<ProjectBloc>().add(
            ProjectEvent.fetchProjectsBySearch(
              query: _searchQuery,
              workflowStatuses: workflowStatuses,
            ),
          );
    } else if (_sortDirection != null) {
      context.read<ProjectBloc>().add(
            ProjectEvent.fetchProjectsSorted(
              workflowStatuses: workflowStatuses,
              sortDirection: _sortDirection!,
            ),
          );
    } else {
      context.read<ProjectBloc>().add(
            ProjectEvent.fetchProjectsByWorkflow(
                workflowStatuses: workflowStatuses),
          );
    }
  }

  void _onTabChanged(int index, UserTypeState userState) {
    setState(() {
      _selectedTabIndex = index;
      // reset search & sort when tab changes
      _searchQuery = '';
      _sortDirection = null;
    });

    context.read<InboxTypeBloc>().add(
          userState.maybeWhen(
            supervisor: () => InboxTypeEvent.typeSelected(index),
            orElse: () => InboxTypeEvent.typeSelected(
                index + 1), // user tabs are shifted by +1 as it's just 2
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
        // Determine tab labels based on user type
        final tabs = userState.maybeWhen(
          supervisor: () => ['For Review', 'Rejected', 'Approved'],
          orElse: () => ['Rejected', 'Approved'],
        );

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
                    // Header Row: Title and toggle User/Supervisor
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
                                    _sortDirection = null; // clear sort
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

                    // ── PROJECT LIST ─────────────────────────────────────────────────
                    BlocBuilder<ProjectBloc, ProjectState>(
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
      List<ProjectWorkflow> projectsList, UserTypeState userState) {
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
                    submitted: () => InboxReportCard(
                        onPress: () {
                          context.read<SelectedProjectBloc>().add(
                                SelectedProjectEvent.select(project),
                              );
                          context.router.push(const InboxAssetSummaryRoute());
                        },
                        title: project.project.name ?? '---',
                        dateAssigned:
                            project.project.startDateTime ?? DateTime.now(),
                        status: project.status ?? '---'),
                    rejected: () => InboxReportRejectedCard(
                      title: project.project.name ?? '---',
                      status: project.status ?? '---',
                      dateAssigned:
                          project.project.startDateTime ?? DateTime.now(),
                      onPress: () {
                        context.read<SelectedProjectBloc>().add(
                              SelectedProjectEvent.select(project),
                            );
                        context.router.push(const SubmitForApprovalRoute());
                      },
                    ),
                    approved: () => InboxReportCard(
                        onPress: () {
                          context.read<SelectedProjectBloc>().add(
                                SelectedProjectEvent.select(project),
                              );
                          context.router.push(const InboxAssetSummaryRoute());
                        },
                        title: project.project.name ?? '---',
                        dateAssigned:
                            project.project.startDateTime ?? DateTime.now(),
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
