import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentDraftPage extends StatelessWidget {
  const AssessmentDraftPage({super.key});

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
        footer: FooterButton(
          text: 'Sync',
          showSuffixIcon: false,
          isDisabled: true,
          onPress: () {},
        ),
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
                  'Drafts',
                  style: textTheme.headingXl.copyWith(
                    color: theme.colorTheme.primary.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                Center(
                  child: Text(
                    'No drafts to display',
                    style: textTheme.bodyL.copyWith(
                      color: theme.colorTheme.text.primary,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
