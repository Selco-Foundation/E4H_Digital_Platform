import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../../model/assessment/assessment_form.dart';
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
  final Future<bool> Function(AssessmentUnableToContactReason reason)
      onUpdateStatus;

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
  AssessmentUnableToContactReason? _unableToContactReason;
  bool _isUpdatingStatus = false;

  bool get _hasUnableToContactReason => _unableToContactReason != null;

  void _clearUnableToContactReason() {
    if (_isUpdatingStatus || !_hasUnableToContactReason) return;
    setState(() => _unableToContactReason = null);
  }

  Future<void> _updateStatus() async {
    final reason = _unableToContactReason;
    if (reason == null || _isUpdatingStatus) return;
    setState(() => _isUpdatingStatus = true);
    var succeeded = false;
    try {
      succeeded = await widget.onUpdateStatus(reason);
    } catch (_) {
      succeeded = false;
    }
    if (!mounted) return;
    setState(() {
      _isUpdatingStatus = false;
      if (succeeded) _unableToContactReason = null;
    });
  }

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
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Expanded(
                            child: RadioList(
                              groupValue: _unableToContactReason?.name ?? '',
                              isDisabled: _isUpdatingStatus,
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
                                    i18.assessmentSelectFacility
                                        .wrongNumberReason,
                                  ),
                                ),
                              ],
                              onChanged: (reason) {
                                if (_isUpdatingStatus) return;
                                final selectedReason =
                                    AssessmentUnableToContactReason.fromCode(
                                  reason.code,
                                );
                                if (selectedReason == null) return;
                                setState(() {
                                  _unableToContactReason =
                                      _unableToContactReason == selectedReason
                                          ? null
                                          : selectedReason;
                                });
                              },
                            ),
                          ),
                          if (_hasUnableToContactReason)
                            Padding(
                              padding: const EdgeInsets.only(bottom: spacer2),
                              child: Semantics(
                                button: true,
                                child: InkWell(
                                  key: const ValueKey(
                                    'assessment-unable-to-contact-clear',
                                  ),
                                  onTap: _isUpdatingStatus
                                      ? null
                                      : _clearUnableToContactReason,
                                  child: Text(
                                    context.translate(i18.common.clear),
                                    style: textTheme.bodyS.copyWith(
                                      color: theme.colorTheme.primary.primary1,
                                    ),
                                  ),
                                ),
                              ),
                            ),
                        ],
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
              label: _isUpdatingStatus
                  ? context.translate(
                      i18.assessmentSelectFacility.updatingStatus,
                    )
                  : _hasUnableToContactReason
                      ? context.translate(
                          i18.assessmentSelectFacility.updateStatus,
                        )
                      : context.translate(
                          i18.assessmentSelectFacility.startAssessment,
                        ),
              onPressed: _hasUnableToContactReason
                  ? _updateStatus
                  : widget.onStartAssessment,
              isDisabled: _isUpdatingStatus,
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
