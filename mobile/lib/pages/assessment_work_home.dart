import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/cards/report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentWorkHomePage extends StatelessWidget {
  const AssessmentWorkHomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

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
              horizontal: spacer4,
              vertical: spacer1,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Assessment',
                  textAlign: TextAlign.start,
                  style: textTheme.headingXl.copyWith(
                    color: const DigitColors().light.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                ReportCard(
                  onPress: () => context.router.push(
                    const AssessmentSelectFacilityRoute(),
                  ),
                  icon: Icons.add_box_outlined,
                  heading: 'New Assessments',
                  description:
                      'View assigned facilities and resume assessments in progress.',
                ),
                ReportCard(
                  onPress: () => context.router.push(
                    const AssessmentDraftRoute(),
                  ),
                  icon: Icons.pending_actions,
                  heading: 'Drafts',
                  description:
                      'View completed assessments waiting to sync or retry.',
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
