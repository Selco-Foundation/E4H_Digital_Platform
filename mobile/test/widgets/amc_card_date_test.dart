import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/utils/i18_key_constants.dart' as i18;
import '../../lib/utils/report_date_label.dart';
import '../../lib/widgets/cards/inbox_report_card.dart';
import '../../lib/widgets/cards/inbox_report_rejected_card.dart';

void main() {
  test('workflow date labels use translations and limited English fallbacks',
      () {
    for (final entry in {
      i18.common.approvedDate: 'Approved Date',
      i18.common.rejectedDate: 'Rejected Date',
    }.entries) {
      expect(reportDateLabel(entry.key, (key) => key), entry.value);
      expect(reportDateLabel(entry.key, (_) => 'Translated date'),
          'Translated date');
    }
    expect(reportDateLabel(i18.common.submissionDate, (key) => key),
        i18.common.submissionDate);
    expect(reportDateLabel(i18.amcSelectFacility.amcDate, (key) => key),
        i18.amcSelectFacility.amcDate);
  });

  for (final amc in [false, true]) {
    for (final rejected in [false, true]) {
      testWidgets(
          '${amc ? "AMC" : "Installation"} workflow date label, bookmarked',
          (tester) async {
        final label =
            rejected ? i18.common.rejectedDate : i18.common.approvedDate;
        final card = rejected
            ? InboxReportRejectedCard(
                title: 'Facility',
                isBookmarked: true,
                dateLabelKey: label,
                dateAssigned: DateTime(2026, 8, 26),
                onPress: () {})
            : InboxReportCard(
                title: 'Facility',
                isAmc: amc,
                isBookmarked: true,
                dateLabelKey: label,
                dateAssigned: DateTime(2026, 8, 26),
                onPress: () {});
        await tester.pumpWidget(MaterialApp(
          theme: DigitTheme.instance.mobileTheme,
          home: Scaffold(body: SingleChildScrollView(child: card)),
        ));
        expect(find.text(rejected ? 'Rejected Date' : 'Approved Date'),
            findsOneWidget);
        expect(find.text(i18.common.submissionDate), findsNothing);
        expect(find.text('26/08/26'), findsOneWidget);
        expect(tester.takeException(), isNull);
      });
    }
  }

  for (final rejected in [false, true]) {
    testWidgets('AMC ${rejected ? 'rejected' : 'report'} date and missing date',
        (tester) async {
      for (final date in [DateTime(2026, 8, 26), null]) {
        final card = rejected
            ? InboxReportRejectedCard(
                title: 'Kasibahal', dateAssigned: date, onPress: () {})
            : InboxReportCard(
                title: 'Kasibahal',
                isAmc: true,
                dateAssigned: date,
                onPress: () {});
        await tester.pumpWidget(MaterialApp(
          theme: DigitTheme.instance.mobileTheme,
          home: Scaffold(body: SingleChildScrollView(child: card)),
        ));
        expect(find.text(date == null ? '—' : '26/08/26'), findsOneWidget);
        expect(tester.takeException(), isNull);
      }
    });
  }

  testWidgets('installation card retains the supplied date', (tester) async {
    await tester.pumpWidget(MaterialApp(
      theme: DigitTheme.instance.mobileTheme,
      home: Scaffold(
        body: SingleChildScrollView(
          child: InboxReportCard(
            title: 'Installation',
            dateAssigned: DateTime(2025, 11, 17),
            onPress: () {},
          ),
        ),
      ),
    ));
    expect(find.text('17/11/25'), findsOneWidget);
    expect(find.text(i18.common.submissionDate), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
