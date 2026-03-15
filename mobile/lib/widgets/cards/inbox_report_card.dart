import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../utils/extensions.dart';
import 'report_detail_row.dart';

class InboxReportCard extends StatelessWidget {
  final String? title;
  final String? status;
  final String? state;
  final String? district;
  final String? block;
  final DateTime dateAssigned;
  final Function() onPress;
  final bool? isAmc;
  final bool? isOtp;

  const InboxReportCard({
    super.key,
    this.title,
    this.status,
    this.state,
    this.district,
    this.block,
    required this.dateAssigned,
    required this.onPress,
    this.isAmc = false,
    this.isOtp = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    String formattedDate = DateFormat('dd/MM/yy').format(dateAssigned);

    return DigitCard(children: [
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            "${title}",
            style: textTheme.headingM
                .copyWith(color: theme.colorTheme.primary.primary2),
          ),
          const SizedBox(height: spacer4),
          const DigitDivider(dividerType: DividerType.small),
          ReportDetailRow(
            label: 'Status',
            value: _detailText(
              context.translate(status ?? ''),
              textTheme,
              theme,
            ),
          ),
          ReportDetailRow(
            label: 'Submission Date',
            value: _detailText(formattedDate, textTheme, theme),
          ),
          ReportDetailRow(
            label: 'State',
            value: _detailText(_displayValue(state), textTheme, theme),
          ),
          ReportDetailRow(
            label: 'District',
            value: _detailText(_displayValue(district), textTheme, theme),
          ),
          ReportDetailRow(
            label: 'Block',
            value: _detailText(_displayValue(block), textTheme, theme),
          ),
          const SizedBox(height: spacer4),
          DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: !(isAmc!)
                  ? 'View Summary'
                  : isOtp!
                      ? 'Submit For Approval'
                      : 'View Report',
              onPressed: onPress,
              type: DigitButtonType.secondary,
              size: DigitButtonSize.large),
        ],
      )
    ]);
  }

  String _displayValue(String? value) {
    final normalized = value?.trim() ?? '';
    return normalized.isEmpty ? '---' : normalized;
  }

  Widget _detailText(String value, dynamic textTheme, ThemeData theme) {
    return Text(
      value,
      style: textTheme.bodyL.copyWith(color: theme.colorTheme.text.primary),
      softWrap: true,
      overflow: TextOverflow.visible,
    );
  }
}
