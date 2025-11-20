import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcDraftPage extends StatefulWidget {
  const AmcDraftPage({super.key});

  @override
  State<AmcDraftPage> createState() => _AmcDraftPageState();
}

class _AmcDraftPageState extends State<AmcDraftPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
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
                  'Submitted Reports',
                  style: textTheme.headingXl.copyWith(
                    color: theme.colorTheme.primary.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                Column(
                  children: [
                    InboxReportCard(
                      onPress: () => context.router.push(const AmcOtpRoute()),
                      title: "Nakodar PHC",
                      dateAssigned: DateTime.now(),
                      status: 'Pending Otp Approval',
                      isAmc: true,
                    ),
                    const SizedBox(height: spacer4),
                    InboxReportCard(
                      onPress: () {
                        context.router.push(AmcDynamicFormRoute(
                            pageName: "AMC_Report",
                            uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                            schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                            projectId: "123456789",
                            origin: FormOrigin.submitted));
                      },
                      title: "Sirsa PHC",
                      dateAssigned: DateTime.now(),
                      status: 'Pending Approval',
                      isAmc: true,
                    ),
                  ],
                )
              ],
            ),
          ),
        ],
      ),
    );
  }
}
