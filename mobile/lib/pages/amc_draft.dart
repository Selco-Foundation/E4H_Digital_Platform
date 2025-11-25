import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
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
  final tabs = ['Pending Approval', 'Pending Otp Approval'];
  int _selectedTabIndex = 0;

  void _onTabChanged(int index) {
    setState(() {
      _selectedTabIndex = index;
    });
  }

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
                            DigitTabBarThemeData.defaultTheme(context).copyWith(
                                tabWidth: constraints.maxWidth / tabs.length,
                                padding: EdgeInsets.zero),
                      );
                    },
                  ),
                ),
                const SizedBox(height: spacer4),
                Column(
                  children: [
                    if (_selectedTabIndex == 1)
                      InboxReportCard(
                        onPress: () => context.router.push(const AmcOtpRoute()),
                        title: "Nakodar PHC",
                        dateAssigned: DateTime.now(),
                        status: 'Pending Otp Approval',
                        isAmc: true,
                        isOtp: true,
                      ),
                    const SizedBox(height: spacer4),
                    if (_selectedTabIndex == 0)
                      InboxReportCard(
                        onPress: () {
                          context.router.push(AmcDynamicFormRoute(
                              pageName: "AMC_Report",
                              uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                              schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                              scheduledVisitId: "123456789",
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
