import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/panel_cards.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class SubmittedSaveSuccessPage extends StatelessWidget {
  const SubmittedSaveSuccessPage({super.key});
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: const Navbar(),
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        children: [
          Padding(
            padding: const EdgeInsets.all(spacer2),
            child: PanelCard(
              animate: true,
              repeat: true,
              type: PanelType.success,
              title: 'Submitted for Approval',
              description: 'The data has been recorded successfully.',
              actions: [
                DigitButton(
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                  label: 'Back to Landing Page',
                  onPressed: () => context.router.push(const HomeRoute()),
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}
