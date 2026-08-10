import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';
import 'package:selco/blocs/localization/app_localization.dart';
import 'package:selco/widgets/cards/assessment_facility_card.dart';

class _UnusedIsar implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _TestLocalizationDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _TestLocalizationDelegate();

  @override
  bool isSupported(Locale locale) => true;

  @override
  Future<AppLocalizations> load(Locale locale) async =>
      AppLocalizations(locale, _UnusedIsar());

  @override
  bool shouldReload(_TestLocalizationDelegate old) => false;
}

void main() {
  const startAssessment = 'ASSESSMENT_SELECT_FACILITY_START_ASSESSMENT';
  const updateStatus = 'ASSESSMENT_SELECT_FACILITY_UPDATE_STATUS';

  Widget buildCard({required bool isRemoteAssessor}) {
    return MaterialApp(
      theme: DigitTheme.instance.mobileTheme,
      supportedLocales: const [Locale('en', 'IN')],
      localizationsDelegates: const [
        _TestLocalizationDelegate(),
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ],
      home: Scaffold(
        body: SingleChildScrollView(
          child: AssessmentFacilityCard(
            facilityName: 'Facility',
            status: 'Scheduled',
            state: 'State',
            district: 'District',
            block: 'Block',
            isRemoteAssessor: isRemoteAssessor,
            onStartAssessment: () {},
            onUpdateStatus: () {},
          ),
        ),
      ),
    );
  }

  bool hasButton(WidgetTester tester, String label) => tester
      .widgetList<DigitButton>(find.byType(DigitButton))
      .any((button) => button.label == label);

  testWidgets('selected unable-to-contact reason can be cleared',
      (tester) async {
    await tester.pumpWidget(buildCard(isRemoteAssessor: true));
    await tester.pumpAndSettle();

    expect(hasButton(tester, startAssessment), isTrue);

    var radioList = tester.widget<RadioList>(find.byType(RadioList));
    radioList.onChanged(radioList.radioDigitButtons.first);
    await tester.pump();

    expect(hasButton(tester, updateStatus), isTrue);
    radioList = tester.widget<RadioList>(find.byType(RadioList));
    expect(radioList.groupValue, 'NO_ANSWER');

    radioList.onChanged(radioList.radioDigitButtons.first);
    await tester.pump();

    expect(hasButton(tester, startAssessment), isTrue);
    radioList = tester.widget<RadioList>(find.byType(RadioList));
    expect(radioList.groupValue, isEmpty);
  });

  testWidgets('tapping another reason switches the selection', (tester) async {
    await tester.pumpWidget(buildCard(isRemoteAssessor: true));
    await tester.pumpAndSettle();

    var radioList = tester.widget<RadioList>(find.byType(RadioList));
    radioList.onChanged(radioList.radioDigitButtons.first);
    await tester.pump();

    radioList = tester.widget<RadioList>(find.byType(RadioList));
    radioList.onChanged(radioList.radioDigitButtons.last);
    await tester.pump();

    radioList = tester.widget<RadioList>(find.byType(RadioList));
    expect(radioList.groupValue, 'WRONG_NUMBER');
    expect(hasButton(tester, updateStatus), isTrue);
  });

  testWidgets('on-site card omits unable-to-contact controls', (tester) async {
    await tester.pumpWidget(buildCard(isRemoteAssessor: false));
    await tester.pumpAndSettle();

    expect(find.byType(RadioList), findsNothing);
    expect(
      find.text('ASSESSMENT_SELECT_FACILITY_COULD_NOT_REACH'),
      findsNothing,
    );
    expect(hasButton(tester, startAssessment), isTrue);
  });
}
