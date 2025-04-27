import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/panel_cards.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class DataSaveSuccessPage extends StatelessWidget {
  const DataSaveSuccessPage({super.key});
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
              title: 'Data Saved Successfully',
              description:
                  'The data has been saved successfully on your device. Please click submit to submit the report for approval on the health facility summary page.',
              actions: [
                DigitButton(
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                  label: 'Next',
                  onPressed: () {},
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}
