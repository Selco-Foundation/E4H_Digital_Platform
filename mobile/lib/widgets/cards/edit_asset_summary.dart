import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

class EditAssetSummary extends StatelessWidget {
  final String text;
  final int count;

  const EditAssetSummary({
    super.key,
    required this.text,
    required this.count,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Stack(
              alignment: Alignment.center,
              children: [
                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Total $text\ninstalled',
                    style: textTheme.headingS,
                  ),
                ),
                Positioned.fill(
                  child: Center(
                    child: Text(
                      '$count',
                      style: textTheme.bodyL,
                    ),
                  ),
                ),
                Align(
                  alignment: Alignment.centerRight,
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.edit,
                          color: theme.colorTheme.primary.primary1,
                          size: spacer4),
                      const SizedBox(width: spacer1),
                      Text(
                        'Edit',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.primary.primary1),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ],
    );
  }
}
