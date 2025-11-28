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

import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/scheduled_visit/scheduled_visit.dart';
import '../blocs/user_type/user_type.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/progress_indicator/loading_indicator.dart';
import 'amc_select_facility.dart';

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
      _fetchProjects(_selectedTabIndex);
    });
  }

  List<String> _statusesForTab(int tabIndex) {
    if (tabIndex == 0) {
      return [WORKFLOW_STATUS_AMC_FIELD_STAFF.REJECTED.name];
    } else {
      return [WORKFLOW_STATUS_AMC_FIELD_STAFF.APPROVED.name];
    }
  }

  void _fetchProjects(int tabIndex) {
    context
        .read<ReportTypeBloc>()
        .add(const ReportTypeEvent.typeSelected("inbox"));

    final statuses = _statusesForTab(tabIndex);

    context
        .read<ScheduledVisitBloc>()
        .add(ScheduledVisitEvent.loadInitial(statuses: statuses));
  }

  void _onTabChanged(int index, UserTypeState userState) {
    setState(() {
      _selectedTabIndex = index;
      _searchQuery = '';
      _sortDirection = null;
    });

    context.read<InboxTypeBloc>().add(InboxTypeEvent.typeSelected(index + 1));
    _fetchProjects(index);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
        final tabs = ['Rejected', 'Approved'];

        return NotificationListener<ScrollNotification>(
          onNotification: (notification) {
            if (notification is ScrollUpdateNotification) {
              final max = notification.metrics.maxScrollExtent;
              final current = notification.metrics.pixels;

              if (current > max - 200) {
                final bloc = context.read<ScheduledVisitBloc>();
                bloc.state.maybeWhen(
                  loaded:
                      (items, hasMore, totalCount, fromCache, isLoadingMore) {
                    if (hasMore && !isLoadingMore) {
                      bloc.add(
                        ScheduledVisitEvent.loadMore(
                          statuses: _statusesForTab(_selectedTabIndex),
                        ),
                      );
                    }
                  },
                  orElse: () {},
                );
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
                                  innerLabel: "Search Health Facility",
                                  suffixIcon: Icons.search,
                                  onChange: (text) {
                                    setState(() {
                                      _searchQuery = text;
                                      _sortDirection = null;
                                    });
                                    _fetchProjects(_selectedTabIndex);
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
                      // if (_selectedTabIndex == 0)
                      //   AMCInstallationReportCard(
                      //     label: "View",
                      //     title: 'Dharnal PHC',
                      //     status: 'Rejected',
                      //     dateAssigned: DateTime.now(),
                      //     onPress: () {
                      //       context.router.push(AmcDynamicFormRoute(
                      //           pageName: "AMC_Report",
                      //           uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                      //           schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                      //           scheduledVisitId: '12345678',
                      //           origin: FormOrigin.submitForApproval));
                      //     },
                      //   ),
                      // const SizedBox(height: spacer4),
                      // if (_selectedTabIndex == 1)
                      //   InboxReportCard(
                      //     onPress: () {
                      //       context.router.push(AmcDynamicFormRoute(
                      //           pageName: "AMC_Report",
                      //           uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                      //           schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                      //           scheduledVisitId: "123456789",
                      //           origin: FormOrigin.submitted));
                      //     },
                      //     title: "Sirsa PHC",
                      //     dateAssigned: DateTime.now(),
                      //     status: 'Approved',
                      //     isAmc: true,
                      //   ),
                      BlocBuilder<ScheduledVisitBloc, ScheduledVisitState>(
                        builder: (context, visitState) {
                          return visitState.maybeWhen(
                            initial: () => loadingIndicator(),
                            loading: () => loadingIndicator(),
                            failure: (message) => Center(
                              child: Padding(
                                padding: const EdgeInsets.only(top: spacer4),
                                child: Text(message),
                              ),
                            ),
                            loaded: (items, hasMore, totalCount, fromCache,
                                isLoadingMore) {
                              return _buildVisitList(items);
                            },
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

  Widget _buildVisitList(List<ScheduledVisit> items) {
    if (items.isEmpty) {
      return const Center(
        child: Text('No AMC visits to display'),
      );
    }

    return Column(
      children: [
        for (final visit in items)
          Column(
            children: [
              if (_selectedTabIndex == 0)
                AMCInstallationReportCard(
                  label: "View",
                  title: visit.facility?.facilityName ?? '',
                  status: visit.status ?? '---',
                  dateAssigned: visit.scheduledDate ?? DateTime.now(),
                  onPress: () {
                    context.router.push(
                      AmcDynamicFormRoute(
                        pageName: "AMC_Report",
                        uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                        schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                        // NOTE: assuming ScheduledVisit has an `id` field.
                        scheduledVisitId: visit.id ?? '',
                        origin: FormOrigin.submitForApproval,
                      ),
                    );
                  },
                )
              else
                InboxReportCard(
                  onPress: () {
                    context.router.push(
                      AmcDynamicFormRoute(
                        pageName: "AMC_Report",
                        uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                        schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                        // Same assumption as above.
                        scheduledVisitId: visit.id ?? '',
                        origin: FormOrigin.submitted,
                      ),
                    );
                  },
                  title: visit.facility?.facilityName ?? '',
                  dateAssigned: visit.scheduledDate ?? DateTime.now(),
                  status: visit.status ?? '---',
                  isAmc: true,
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
                      _fetchProjects(_selectedTabIndex);
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
                      _fetchProjects(_selectedTabIndex);
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
