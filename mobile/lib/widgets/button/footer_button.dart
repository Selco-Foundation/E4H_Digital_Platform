import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

class FooterButton extends StatelessWidget {
  final String text;
  final Function() onPress;
  final bool showSuffixIcon;
  const FooterButton(
      {super.key,
      required this.text,
      required this.onPress,
      this.showSuffixIcon = true});

  @override
  Widget build(BuildContext context) {
    return DigitCard(margin: const EdgeInsets.only(top: spacer2), children: [
      DigitButton(
        suffixIcon: showSuffixIcon ? Icons.arrow_forward_outlined : null,
        mainAxisSize: MainAxisSize.max,
        label: text,
        type: DigitButtonType.primary,
        size: DigitButtonSize.large,
        onPressed: onPress,
      ),
    ]);
  }
}
