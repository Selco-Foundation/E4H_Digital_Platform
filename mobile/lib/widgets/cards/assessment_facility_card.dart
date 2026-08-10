import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;
import 'report_detail_row.dart';

class AssessmentFacilityCard extends StatefulWidget {
  final String facilityName;
  final String status;
  final String state;
  final String district;
  final String block;
  final bool isRemoteAssessor;
  final VoidCallback onStartAssessment;
  final VoidCallback onUpdateStatus;

  const AssessmentFacilityCard({
    super.key,
    required this.facilityName,
    required this.status,
    required this.state,
    required this.district,
    required this.block,
    required this.isRemoteAssessor,
    required this.onStartAssessment,
    required this.onUpdateStatus,
  });

  @override
  State<AssessmentFacilityCard> createState() => _AssessmentFacilityCardState();
}

class _AssessmentFacilityCardState extends State<AssessmentFacilityCard> {
  String? _unableToContactReason;

  bool get _hasUnableToContactReason => _unableToContactReason != null;

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
              widget.facilityName,
              style: textTheme.headingL.copyWith(
                color: theme.colorTheme.text.primary,
              ),
            ),
            const SizedBox(height: spacer4),
            const DigitDivider(dividerType: DividerType.small),
            ReportDetailRow(
              label: context.translate(i18.common.status),
              value: _detailText(widget.status, textTheme, theme),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.state),
              value: _detailText(widget.state, textTheme, theme),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.district),
              value: _detailText(widget.district, textTheme, theme),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.block),
              value: _detailText(widget.block, textTheme, theme),
            ),
            if (widget.isRemoteAssessor) ...[
              const SizedBox(height: spacer4),
              DigitCard(
                cardType: CardType.secondary,
                padding: const EdgeInsets.all(spacer3),
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(width: double.infinity),
                      const SizedBox(height: spacer1),
                      Text(
                        context.translate(
                          i18.assessmentSelectFacility.couldNotReach,
                        ),
                        style: textTheme.headingS.copyWith(
                          color: theme.colorTheme.text.primary,
                        ),
                      ),
                      const SizedBox(height: spacer2),
                      Text(
                        context.translate(
                          i18.assessmentSelectFacility.selectReason,
                        ),
                        style: textTheme.bodyS.copyWith(
                          color: theme.colorTheme.text.secondary,
                        ),
                      ),
                      const SizedBox(height: spacer2),
                      RadioList(
                        groupValue: _unableToContactReason ?? '',
                        containerPadding: const EdgeInsets.symmetric(
                          vertical: spacer2,
                        ),
                        radioDigitButtons: [
                          RadioButtonModel(
                            code: 'NO_ANSWER',
                            name: context.translate(
                              i18.assessmentSelectFacility.noAnswerReason,
                            ),
                          ),
                          RadioButtonModel(
                            code: 'WRONG_NUMBER',
                            name: context.translate(
                              i18.assessmentSelectFacility.wrongNumberReason,
                            ),
                          ),
                        ],
                        onChanged: (reason) {
                          setState(() {
                            _unableToContactReason =
                                _unableToContactReason == reason.code
                                    ? null
                                    : reason.code;
                          });
                        },
                      ),
                      const SizedBox(height: spacer1),
                    ],
                  ),
                ],
              ),
            ],
            const SizedBox(height: spacer4),
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: _hasUnableToContactReason
                  ? context.translate(
                      i18.assessmentSelectFacility.updateStatus,
                    )
                  : context.translate(
                      i18.assessmentSelectFacility.startAssessment,
                    ),
              onPressed: _hasUnableToContactReason
                  ? widget.onUpdateStatus
                  : widget.onStartAssessment,
              type: DigitButtonType.primary,
              size: DigitButtonSize.large,
            ),
          ],
        ),
      ],
    );
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
