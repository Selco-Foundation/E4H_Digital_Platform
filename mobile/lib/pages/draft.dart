import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class DraftPage extends StatefulWidget {
  const DraftPage({super.key});

  @override
  State<DraftPage> createState() => _DraftPageState();
}

class _DraftPageState extends State<DraftPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer: FooterButton(
          showSuffixIcon: false,
          text: "Sync",
          onPress: () {
            context.router.replace(const SubmittedSaveSuccessRoute());
          },
        ),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer4, horizontal: spacer4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Unsynced Reports',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),
                InboxReportCard(
                    onPress: () =>
                        context.router.push(const DraftSummaryRoute()),
                    title: 'Alkod',
                    dateAssigned: DateTime(2024, 1, 25),
                    status: 'Pending Installation'),
                const SizedBox(height: spacer6),
                InboxReportCard(
                    onPress: () =>
                        context.router.push(const DraftSummaryRoute()),
                    title: 'Alkod',
                    dateAssigned: DateTime(2024, 1, 25),
                    status: 'Pending Installation')
              ],
            ),
          )
        ],
      ),
    );
  }
}
