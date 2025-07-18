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

import '../blocs/cache_project_asset/cache_project_asset.dart';
import '../blocs/project/project.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_project_asset.dart';
import '../model/project_workflow/project_workflow.dart';
import '../router/app_router.dart';
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

    // Choose search vs sort vs basic fetch && Dispatch fetch + loading state
    if (_searchQuery.isNotEmpty) {
      context.read<ProjectBloc>().add(
            ProjectEvent.fetchProjectsBySearch(
              query: _searchQuery,
              workflowStatuses: statuses,
            ),
          );
    } else if (_sortDirection != null) {
      context.read<ProjectBloc>().add(
            ProjectEvent.fetchProjectsSorted(
              workflowStatuses: statuses,
              sortDirection: _sortDirection!,
            ),
          );
    } else {
      context.read<ProjectBloc>().add(
            ProjectEvent.fetchProjectsByWorkflow(workflowStatuses: statuses),
          );
    }
  }

  void _handleProjectTap(ProjectWorkflow project) {
    context.read<CacheProjectAssetBloc>().add(
          CacheProjectAssetEvent.add(
              CacheProjectAsset(projectId: project.project.id)),
        );
    context
        .read<SelectedProjectBloc>()
        .add(SelectedProjectEvent.select(project));
    context.router.push(const AssetCountRoute());
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: ScrollableContent(
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
              BlocBuilder<ProjectBloc, ProjectState>(
                builder: (context, state) {
                  return state.maybeWhen(
                    initial: () => _loadingIndicator(),
                    loading: () => _loadingIndicator(),
                    fetched: (projectList) {
                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          _buildProjectList(projectList),
                        ],
                      );
                    },
                    searchLoading: () => _loadingIndicator(),
                    searchResults: (searchList) =>
                        _buildProjectList(searchList),
                    orElse: () => const SizedBox.shrink(),
                  );
                },
              ),
            ],
          ),
        ],
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
                        _sortDirection = null; // clear sort
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

  /// Extracted helper to render a vertical list of cards
  Widget _buildProjectList(List<ProjectWorkflow> projects) {
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
              title: project.project.name ?? '—',
              dateAssigned: project.project.startDateTime ?? DateTime.now(),
              status: truncateText(project.status ?? '—', maxLength: 18),
              solutionDocPath: project.project.projectNumber,
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
                      context.read<ProjectBloc>().add(
                            ProjectEvent.fetchProjectsSorted(
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
  final String? title;
  final String? status;
  final DateTime dateAssigned;
  final String? solutionDocPath;
  final Function() onPress;

  const InstallationReportCard({
    super.key,
    this.title,
    this.status,
    required this.dateAssigned,
    this.solutionDocPath,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    String formattedDate = DateFormat('dd/MM/yy').format(dateAssigned);

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
                Column(
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
                const SizedBox(width: spacer12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const SizedBox(height: spacer4),
                    Text(
                      '$status',
                      style: textTheme.bodyL.copyWith(
                        color: theme.colorTheme.text.primary,
                      ),
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
                        Text(
                          '$solutionDocPath',
                          style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.text.disabled,
                            fontSize: spacer3,
                          ),
                        ),
                      ],
                    )
                  ],
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
                      value: 0.4,
                      minHeight: spacer3,
                    ),
                  ),
                  const SizedBox(width: spacer3),
                  Text(
                    '40%',
                    style: textTheme.bodyS.copyWith(
                      color: theme.colorTheme.text.secondary,
                    ),
                  )
                ],
              ),
            ),
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'Start Installation Report',
              onPressed: onPress,
              type: DigitButtonType.primary,
              size: DigitButtonSize.large,
            ),
            const SizedBox(height: spacer4),
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'Submit For Approval',
              onPressed: () {},
              isDisabled: true,
              type: DigitButtonType.secondary,
              size: DigitButtonSize.large,
            ),
          ],
        )
      ],
    );
  }
}
