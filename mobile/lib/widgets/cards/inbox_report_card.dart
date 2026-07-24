import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;
import '../../utils/utils.dart';
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
  final int? visitNumber;
  final int? durationMonths;
  final int? visitFrequencyMonths;

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
    this.visitNumber,
    this.durationMonths,
    this.visitFrequencyMonths,
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
            label: context.translate(i18.common.status),
            value: _detailText(
              context.translate(status ?? ''),
              textTheme,
              theme,
            ),
          ),
          ReportDetailRow(
            label: context.translate(i18.common.submissionDate),
            value: _detailText(formattedDate, textTheme, theme),
          ),
          ReportDetailRow(
            label: context.translate(i18.common.state),
            value: _detailText(_displayValue(state), textTheme, theme),
          ),
          ReportDetailRow(
            label: context.translate(i18.common.district),
            value: _detailText(_displayValue(district), textTheme, theme),
          ),
          ReportDetailRow(
            label: context.translate(i18.common.block),
            value: _detailText(_displayValue(block), textTheme, theme),
          ),
          if (isAmc == true)
            ReportDetailRow(
              label: context.translate(i18.amcSelectFacility.amcNumber),
              value: _detailText(
                formatAmcNumber(
                  visitNumber,
                  durationMonths,
                  visitFrequencyMonths,
                ),
                textTheme,
                theme,
              ),
            ),
          const SizedBox(height: spacer4),
          DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: !(isAmc!)
                  ? context.translate(i18.sharedCards.viewSummary)
                  : isOtp!
                      ? context.translate(i18.sharedCards.submitForApproval)
                      : context.translate(i18.sharedCards.viewReport),
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
