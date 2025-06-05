import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:flutter/material.dart';

class ElementAssetSummary extends StatelessWidget {
  final String type;
  final String text;
  final int count;
  final bool lastCard;
  final VoidCallback? onPress;

  const ElementAssetSummary(
      {super.key,
      required this.type,
      required this.text,
      required this.count,
      this.lastCard = false,
      this.onPress});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Stack(
          alignment: Alignment.center,
          children: [
            // Text on the left
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Total $text\ninstalled',
                style: textTheme.headingS,
              ),
            ),
            // Centered count
            Positioned.fill(
              child: Center(
                child: Text(
                  '$count',
                  style: textTheme.bodyL,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: spacer2),
        if (count > 0)
          Column(
            children: [
              DigitButton(
                mainAxisSize: MainAxisSize.max,
                label: 'View $type Summary',
                type: DigitButtonType.secondary,
                size: DigitButtonSize.medium,
                onPressed: onPress ?? () {},
              ),
              const SizedBox(height: spacer2),
            ],
          ),
        lastCard == true
            ? const SizedBox.shrink()
            : const Column(
                children: [
                  SizedBox(height: spacer2),
                  DigitDivider(dividerType: DividerType.small),
                ],
              ),
      ],
    );
  }
}
