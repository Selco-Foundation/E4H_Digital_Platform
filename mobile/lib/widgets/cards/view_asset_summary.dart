import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

class ViewAssetSummary extends StatelessWidget {
  final String text;
  final int count;

  const ViewAssetSummary({
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
                  child: Container(
                    decoration: BoxDecoration(
                        border: Border.all(
                            color: theme.colorTheme.primary.primary1)),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                          vertical: spacer1, horizontal: spacer2),
                      child: Text(
                        'View',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.primary.primary1),
                      ),
                    ),
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
