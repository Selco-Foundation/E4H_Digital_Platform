import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/panel_cards.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;

@RoutePage()
class SubmittedSaveSuccessPage extends StatelessWidget {
  const SubmittedSaveSuccessPage({super.key});
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        children: [
          Padding(
            padding: const EdgeInsets.all(spacer2),
            child: PanelCard(
              animate: true,
              repeat: true,
              type: PanelType.success,
              title: context.translate(i18.submittedSaveSuccess.title),
              description:
                  context.translate(i18.submittedSaveSuccess.description),
              actions: [
                DigitButton(
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                  label: context.translate(i18.common.coreCommonHome),
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
