import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../../repositories/assessment_draft_repo.dart';
import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;
import 'report_detail_row.dart';
import '../bookmarks/report_bookmarks.dart';

class AssessmentDraftCard extends StatelessWidget {
  final bool isBookmarked;
  final bool isSavingBookmark;
  final VoidCallback? onToggleBookmark;
  final String? facilityName;
  final String? facilityType;
  final String status;
  final String? state;
  final String? district;
  final String? block;
  final String? failureReason;
  final VoidCallback onPressed;

  const AssessmentDraftCard({
    super.key,
    this.isBookmarked = false,
    this.isSavingBookmark = false,
    this.onToggleBookmark,
    required this.facilityName,
    required this.facilityType,
    required this.status,
    required this.state,
    required this.district,
    required this.block,
    required this.failureReason,
    required this.onPressed,
  });

  String _displayValue(String? value) {
    final normalized = value?.trim();
    return normalized == null || normalized.isEmpty ? '---' : normalized;
  }

  Widget _detailValue(
    BuildContext context,
    String value, {
    Color? color,
  }) {
    final theme = Theme.of(context);
    return Text(
      value,
      style: theme.digitTextTheme(context).bodyL.copyWith(
            color: color ?? theme.colorTheme.text.secondary,
          ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final isBlocked = status == AssessmentDraftStatus.blocked;
    final normalizedFailure = failureReason?.trim();

    return DigitCard(
      onPressed: onPressed,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(width: double.infinity),
            Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Expanded(
                  child: Text(_displayValue(facilityName),
                      style: textTheme.headingL
                          .copyWith(color: theme.colorTheme.text.primary))),
              if (onToggleBookmark != null)
                ReportBookmarkButton(
                    selected: isBookmarked,
                    saving: isSavingBookmark,
                    onPressed: onToggleBookmark,
                    addLabelKey: i18.assessmentBookmarks.add,
                    removeLabelKey: i18.assessmentBookmarks.remove),
            ]),
            const SizedBox(height: spacer4),
            const DigitDivider(dividerType: DividerType.small),
            ReportDetailRow(
              label: context.translate(i18.assessmentDraft.facilityType),
              value: _detailValue(context, _displayValue(facilityType)),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.status),
              value: _detailValue(
                context,
                context.translate(
                  isBlocked
                      ? i18.assessmentDraft.blocked
                      : i18.assessmentDraft.pending,
                ),
                color: isBlocked ? theme.colorTheme.alert.error : null,
              ),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.state),
              value: _detailValue(context, _displayValue(state)),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.district),
              value: _detailValue(context, _displayValue(district)),
            ),
            ReportDetailRow(
              label: context.translate(i18.common.block),
              value: _detailValue(context, _displayValue(block)),
            ),
            if (normalizedFailure != null && normalizedFailure.isNotEmpty)
              ReportDetailRow(
                label: context.translate(i18.assessmentDraft.failureReason),
                value: _detailValue(
                  context,
                  context.translate(normalizedFailure),
                  color: theme.colorTheme.alert.error,
                ),
              ),
          ],
        ),
      ],
    );
  }
}
