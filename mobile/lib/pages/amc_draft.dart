import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/scheduled_visit/scheduled_visit.dart';
import '../blocs/selected_amc_origin/selected_amc_origin.dart';
import '../blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/progress_indicator/loading_indicator.dart';

@RoutePage()
class AmcDraftPage extends StatefulWidget {
  const AmcDraftPage({super.key});

  @override
  State<AmcDraftPage> createState() => _AmcDraftPageState();
}

class _AmcDraftPageState extends State<AmcDraftPage> {
  final tabs = ['Pending Otp Approval', 'Pending Approval'];
  int _selectedTabIndex = 0;

  List<String> _statusesForTab(int tabIndex) {
    if (tabIndex == 0) {
      return [WORKFLOW_STATUS_AMC_FIELD_STAFF.PENDING_OTP_APPROVAL.name];
    } else {
      return [WORKFLOW_STATUS_AMC_FIELD_STAFF.PENDING_APPROVAL.name];
    }
  }

  void _fetchVisits(int tabIndex) {
    final statuses = _statusesForTab(tabIndex);
    context.read<ScheduledVisitBloc>().add(
          ScheduledVisitEvent.loadInitial(statuses: statuses),
        );
  }

  void _onTabChanged(int index) {
    setState(() {
      _selectedTabIndex = index;
    });
    _fetchVisits(index);
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _fetchVisits(_selectedTabIndex);
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return NotificationListener<ScrollNotification>(
      onNotification: (notification) {
        if (notification is ScrollUpdateNotification) {
          final max = notification.metrics.maxScrollExtent;
          final current = notification.metrics.pixels;

          if (current > max - 200) {
            final bloc = context.read<ScheduledVisitBloc>();
            bloc.state.maybeWhen(
              loaded: (items, hasMore, totalCount, fromCache, isLoadingMore) {
                if (hasMore && !isLoadingMore) {
                  bloc.add(ScheduledVisitEvent.loadMore(
                      statuses: _statusesForTab(_selectedTabIndex)));
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
          enableFixedDigitButton: true,
          backgroundColor: theme.colorTheme.generic.background,
          header: const BackNavigationHelpHeaderWidget(
            showBackNavigation: true,
            showHelp: false,
          ),
          footer: const SizedBox.shrink(),
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(
                vertical: spacer4,
                horizontal: spacer4,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Pending Approval',
                    style: textTheme.headingXl.copyWith(
                      color: theme.colorTheme.primary.primary2,
                    ),
                  ),
                  const SizedBox(height: spacer4),
                  SizedBox(
                    height: spacer12 + spacer1,
                    child: LayoutBuilder(
                      builder: (context, constraints) {
                        return DigitTabBar(
                          tabs: tabs,
                          initialIndex: _selectedTabIndex,
                          onTabSelected: (index) => _onTabChanged(index),
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
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildVisitList(List<ScheduledVisit> items) {
    if (items.isEmpty) {
      return const Center(
        child: Text('No AMC drafts to display'),
      );
    }

    return Column(
      children: [
        for (final visit in items)
          Column(
            children: [
              if (_selectedTabIndex == 0)
                InboxReportCard(
                    onPress: () {
                      context
                          .read<SelectedScheduledVisitBloc>()
                          .add(SelectedScheduledVisitEvent.select(visit));
                      visit.status !=
                              WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name
                          ? context.router.push(const AmcOtpRoute())
                          : context.router.push(
                              AmcDynamicFormRoute(
                                  pageName: "AMC_Report",
                                  uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                                  schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                                  scheduledVisit: visit,
                                  origin: FormOrigin.overallSummary),
                            );
                    },
                    title: visit.facility?.facilityName ?? '',
                    dateAssigned: visit.scheduledDate ?? DateTime.now(),
                    status: visit.status ?? '---',
                    isAmc: true,
                    isOtp: true)
              else
                InboxReportCard(
                  onPress: () {
                    context
                        .read<SelectedScheduledVisitBloc>()
                        .add(SelectedScheduledVisitEvent.select(visit));
                    context.read<SelectedAmcOriginBloc>().add(
                        const SelectedAmcOriginEvent.select(
                            FormOrigin.submitted));
                    context.router.push(
                      AmcDynamicFormRoute(
                          pageName: "AMC_Report",
                          uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                          schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                          scheduledVisit: visit,
                          origin: FormOrigin.submitted),
                    );
                  },
                  title: visit.facility?.facilityName ?? '',
                  dateAssigned: visit.scheduledDate ?? DateTime.now(),
                  status: visit.status ?? '---',
                  isAmc: true,
                ),
              const SizedBox(height: spacer4),
            ],
          ),
      ],
    );
  }
}
