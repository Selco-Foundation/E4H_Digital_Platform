import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../../utils/extensions.dart';

class RejectedEditAssetSummary extends StatelessWidget {
  final String text;
  final int count;

  const RejectedEditAssetSummary({
    super.key,
    required this.text,
    required this.count,
  });

  @override
  Widget build(BuildContext context) {
    return DigitCard(
      children: [
        _rejectCard(context: context, assetType: 'Inverters', count: 2),
        _rejectCard(context: context, assetType: 'Batteries'),
        _rejectCard(
            context: context, assetType: "Panels", isLast: true, count: 2)
      ],
    );
  }

  Widget _rejectCard({
    required BuildContext context,
    String? assetType,
    int count = 0,
    bool isLast = false,
  }) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Stack(
          alignment: Alignment.center,
          children: [
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                '$assetType',
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
          ],
        ),
        if (count > 0)
          Column(
            children: [
              const SizedBox(height: spacer4),
              Container(
                  decoration: BoxDecoration(
                      color: theme.colorTheme.generic.background,
                      border:
                          Border.all(color: theme.colorTheme.generic.divider),
                      borderRadius: BorderRadius.circular(spacer1)),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: spacer3, vertical: spacer4),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        SizedBox(width: context.width),
                        Text(
                          "Rejection Reason(s)",
                          style: textTheme.headingM
                              .copyWith(color: theme.colorTheme.text.primary),
                        ),
                        const SizedBox(height: spacer5),
                        _rejectionReason(
                            context: context,
                            reason: "Serial Number incorrect",
                            index: 1),
                        const SizedBox(height: spacer4),
                        _rejectionReason(
                            context: context,
                            reason: "Additional Reason 2",
                            index: 2),
                      ],
                    ),
                  )),
              const SizedBox(height: spacer5),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Expanded(
                    flex: 1,
                    child: DigitButton(
                      label: "Edit",
                      onPressed: () {},
                      type: DigitButtonType.secondary,
                      size: DigitButtonSize.medium,
                      prefixIcon: Icons.edit,
                      mainAxisSize: MainAxisSize.min,
                    ),
                  ),
                ],
              ),
            ],
          ),
        const SizedBox(height: spacer5),
        if (!isLast) const DigitDivider(dividerType: DividerType.small),
      ],
    );
  }

  Widget _rejectionReason(
      {required BuildContext context, String? reason, int? index}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          decoration: BoxDecoration(
              border: Border.all(color: const Light().primary2),
              borderRadius: BorderRadius.circular(spacer2),
              color: Light().paperSecondary),
          child: Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer1, horizontal: spacer3),
            child: Text(
              "Reason Reason $index",
              style: Theme.of(context)
                  .digitTextTheme(context)
                  .label
                  .copyWith(color: const Light().primary2),
            ),
          ),
        ),
        const SizedBox(height: spacer2),
        Text("$reason",
            style: Theme.of(context).digitTextTheme(context).label.copyWith(
                  color: const Light().textPrimary,
                )),
      ],
    );
  }
}
