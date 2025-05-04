import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

class InboxReportRejectedCard extends StatelessWidget {
  final String? title;
  final String? status;
  final String? reason;
  final Function() onPress;

  const InboxReportRejectedCard({
    super.key,
    this.title,
    this.status,
    this.reason,
    required this.onPress,
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
            Text(
              title!,
              style: textTheme.headingM
                  .copyWith(color: theme.colorTheme.primary.primary2),
            ),
            const SizedBox(height: spacer4),
            const DigitDivider(dividerType: DividerType.small),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  flex: 1,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: spacer4),
                      Text(
                        'Status',
                        style: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                      const SizedBox(height: spacer4),
                      Text(
                        'Rejection Reason',
                        style: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: spacer12),
                Expanded(
                  flex: 2,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: spacer4),
                      Text(
                        status ?? '',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                      const SizedBox(height: spacer4),
                      Text(
                        reason ?? '',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.text.primary),
                        softWrap: true,
                        overflow: TextOverflow.visible,
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: spacer8),
            DigitButton(
                mainAxisSize: MainAxisSize.max,
                label: 'Edit Asset Data',
                onPressed: onPress,
                type: DigitButtonType.primary,
                size: DigitButtonSize.large),
            const SizedBox(height: spacer4),
            DigitButton(
                mainAxisSize: MainAxisSize.max,
                label: 'Re-Submit for Approval',
                onPressed: onPress,
                type: DigitButtonType.secondary,
                size: DigitButtonSize.large),
          ],
        ),
      ],
    );
  }
}
