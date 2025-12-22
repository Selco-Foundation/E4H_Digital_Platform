import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/button_theme.dart';
import 'package:flutter/material.dart';

import '../../router/app_router.dart';

class BackNavigationHelpHeaderWidget extends StatelessWidget {
  final bool showHelp;
  final bool showBackNavigation;
  final VoidCallback? helpClicked;
  final VoidCallback? handleback;
  final bool defaultPopRoute;

  const BackNavigationHelpHeaderWidget({
    super.key,
    this.showHelp = true,
    this.showBackNavigation = true,
    this.helpClicked,
    this.handleback,
    this.defaultPopRoute = true,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(8, 8, 8, 0),
      child: Row(
        children: [
          Expanded(
            child: Row(
              children: [
                if (context.router.canPop() && showBackNavigation)
                  DigitButton(
                    prefixIcon: Icons.arrow_left,
                    textColor: const DigitColors().light.textPrimary,
                    iconColor: const DigitColors().light.textPrimary,
                    label: "Back",
                    type: DigitButtonType.tertiary,
                    size: DigitButtonSize.medium,
                    onPressed: () {
                      if (defaultPopRoute) {
                        context.router.maybePop();
                      }
                      handleback != null ? handleback!() : null;
                    },
                    digitButtonThemeData: const DigitButtonThemeData().copyWith(
                      smallIconSize: spacer6,
                    ),
                  ),
              ],
            ),
          ),
          SizedBox(width: showHelp ? spacer4 : 0),
          if (showHelp)
            DigitButton(
              textColor: const Light().primary1,
              iconColor: const Light().primary1,
              isDisabled: helpClicked == null,
              label: "Help",
              type: DigitButtonType.tertiary,
              size: DigitButtonSize.medium,
              suffixIcon: Icons.help_outline_outlined,
              onPressed: () => helpClicked,
            ),
        ],
      ),
    );
  }
}
