import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/cards/report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class InstallationReportPage extends StatefulWidget {
  const InstallationReportPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return _InstallationReportPageState();
  }
}

class _InstallationReportPageState extends State<InstallationReportPage> {
  late var userType = "";

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );

      context.read<ActivityFacilityBloc>().add(
            ActivityFacilityEvent.fetchAllReportCounts(userType: userType),
          );
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<ActivityFacilityBloc, ActivityFacilityState>(
      builder: (context, state) {
        return Scaffold(
          body: ScrollableContent(
            footer: const PoweredByDigit(version: ''),
            backgroundColor: theme.colorTheme.generic.background,
            children: [
              const BackNavigationHelpHeaderWidget(
                showHelp: true,
                showBackNavigation: true,
              ),
              const SizedBox(height: spacer3),
              Padding(
                padding: const EdgeInsets.symmetric(
                    horizontal: spacer4, vertical: spacer1),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Installation Report',
                      textAlign: TextAlign.start,
                      style: textTheme.headingXl.copyWith(
                        color: const DigitColors().light.primary2,
                      ),
                    ),
                    const SizedBox(height: spacer4),
                    ReportCard(
                      badgeCount: state.maybeWhen(
                          orElse: () => 0,
                          reportCountsLoaded:
                              (newCount, inboxCount, submittedCount) =>
                                  newCount),
                      onPress: () {
                        context.read<ReportTypeBloc>().add(
                            const ReportTypeEvent.typeSelected("new-report"));
                        context.router.push(const SelectHealthFacilityRoute());
                      },
                      icon: Icons.add_box_outlined,
                      heading: 'New Report',
                      description:
                          'View list of assigned health facilities, search for health facility and create installation report',
                    ),
                    ReportCard(
                      onPress: () {
                        context
                            .read<ReportTypeBloc>()
                            .add(const ReportTypeEvent.typeSelected("inbox"));
                        context.router.push(const InboxRoute());
                      },
                      badgeCount: state.maybeWhen(
                          orElse: () => 0,
                          reportCountsLoaded:
                              (newCount, inboxCount, submittedCount) =>
                                  inboxCount),
                      icon: Icons.toc,
                      heading: 'Inbox',
                      description: userType == USER_TYPES.SUPERVISOR.name
                          ? 'Review reports from field and view reports that have been approved/rejected'
                          : 'View reports that have been approved/rejected',
                    ),
                    ReportCard(
                        onPress: () {
                          context.read<ReportTypeBloc>().add(
                              const ReportTypeEvent.typeSelected("submitted"));
                          context.router.push(const DraftRoute());
                        },
                        icon: Icons.assignment_late,
                        badgeCount: state.maybeWhen(
                            orElse: () => 0,
                            reportCountsLoaded:
                                (newCount, inboxCount, submittedCount) =>
                                    submittedCount),
                        heading: 'Pending Approval',
                        description:
                            'View all reports (both synced and unsynced) that have been submitted but are pending approval. '),
                  ],
                ),
              )
            ],
          ),
        );
      },
    );
  }
}
