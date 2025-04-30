import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../router/app_router.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class InboxPage extends StatefulWidget {
  const InboxPage({super.key});

  @override
  State<InboxPage> createState() => _InboxPageState();
}

class _InboxPageState extends State<InboxPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
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
                Text(
                  'Inbox',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),
                SizedBox(
                  height: spacer12 + spacer1,
                  child: LayoutBuilder(
                    builder: (context, constraints) {
                      return DigitTabBar(
                        tabs: const ['Submitted', 'Rejected', 'Approved'],
                        initialIndex: 1,
                        onTabSelected: (index) {},
                        tabBarThemeData:
                            DigitTabBarThemeData.defaultTheme(context).copyWith(
                                tabWidth: constraints.maxWidth / 3,
                                padding: EdgeInsets.zero),
                      );
                    },
                  ),
                ),
                DigitCard(
                  children: [
                    Row(
                      children: [
                        const Expanded(
                          child: DigitSearchFormInput(
                            innerLabel: "Search Health Facility",
                            suffixIcon: Icons.search,
                          ),
                        ),
                        Icon(
                          Icons.import_export,
                          color: theme.colorTheme.primary.primary1,
                          size: spacer8,
                        ),
                        Text("Sort",
                            style: textTheme.headingS.copyWith(
                                color: theme.colorTheme.primary.primary1))
                      ],
                    )
                  ],
                ),
                const SizedBox(height: spacer4),
                InboxReportCard(
                    onPress: () =>
                        context.router.push(const SelectAssetTypeRoute()),
                    title: 'Alkod',
                    dateAssigned: DateTime(2024, 1, 25),
                    status: 'Pending Installation'),
                const SizedBox(height: spacer5),
              ],
            ),
          )
        ],
      ),
    );
  }
}

class InboxReportCard extends StatelessWidget {
  final String? title;
  final String? status;
  final DateTime dateAssigned;
  final Function() onPress;

  const InboxReportCard({
    super.key,
    this.title,
    this.status,
    required this.dateAssigned,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    String formattedDate = DateFormat('dd/MM/yy').format(dateAssigned);

    return DigitCard(children: [
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title!,
            style: textTheme.headingM
                .copyWith(color: theme.colorTheme.primary.primary2),
          ),
          const SizedBox(
            height: spacer4,
          ),
          const DigitDivider(
            dividerType: DividerType.small,
          ),
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
                    'Submission Date',
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
                    style: textTheme.bodyL
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                  const SizedBox(height: spacer4),
                  Text(
                    formattedDate,
                    style: textTheme.bodyL
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: spacer4),
          DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'View Summary',
              onPressed: onPress,
              type: DigitButtonType.secondary,
              size: DigitButtonSize.large),
        ],
      )
    ]);
  }
}
