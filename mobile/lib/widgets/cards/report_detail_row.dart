import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';

class ReportDetailRow extends StatelessWidget {
  final String label;
  final Widget value;
  final int labelFlex;
  final int valueFlex;

  const ReportDetailRow({
    super.key,
    required this.label,
    required this.value,
    this.labelFlex = 2,
    this.valueFlex = 3,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Padding(
      padding: const EdgeInsets.only(top: spacer4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            flex: labelFlex,
            child: Text(
              label,
              style: textTheme.headingS
                  .copyWith(color: theme.colorTheme.text.primary),
            ),
          ),
          const SizedBox(width: spacer12),
          Expanded(
            flex: valueFlex,
            child: value,
          ),
        ],
      ),
    );
  }
}
