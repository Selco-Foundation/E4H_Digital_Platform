import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:selco/widgets/cards/assessment_facility_card.dart';
import 'package:selco/pages/assessment_work_home.dart';

void main() {
  testWidgets(
      'bookmark is accessible, primary colored, and disabled during save',
      (tester) async {
    var taps = 0;
    Widget card({bool saved = false, bool busy = false}) => MaterialApp(
        theme: DigitTheme.instance.mobileTheme,
        home: Scaffold(
            body: SingleChildScrollView(
                child: AssessmentFacilityCard(
          facilityName:
              'A very long facility name that wraps onto several lines without hiding the bookmark',
          status: 'Pending',
          state: 'State',
          district: 'District',
          block: 'Block',
          isRemoteAssessor: false,
          onStartAssessment: () {},
          onUpdateStatus: (_) async => true,
          isBookmarked: saved,
          isSavingBookmark: busy,
          onToggleBookmark: () => taps++,
        ))));
    await tester.pumpWidget(card());
    expect(find.byTooltip('ASSESSMENT_BOOKMARKS_ADD'), findsOneWidget);
    final button = tester.widget<IconButton>(find.byType(IconButton));
    final context = tester.element(find.byType(IconButton));
    expect(button.color, Theme.of(context).colorTheme.primary.primary1);
    expect(tester.getSize(find.byType(IconButton)).width,
        greaterThanOrEqualTo(48));
    await tester.tap(find.byIcon(Icons.bookmark_border));
    expect(taps, 1);
    await tester.pumpWidget(card(saved: true, busy: true));
    expect(find.byTooltip('ASSESSMENT_BOOKMARKS_REMOVE'), findsOneWidget);
    expect(
        tester.widget<IconButton>(find.byType(IconButton)).onPressed, isNull);
    expect(tester.takeException(), isNull);
  });
  testWidgets('home shows separate counts and respects phase roles',
      (tester) async {
    Widget home(bool field) => MaterialApp(
        theme: DigitTheme.instance.mobileTheme,
        home: Scaffold(
            body: SingleChildScrollView(
                child: AssessmentWorkCards(
          hasRemoteAssessment: true,
          hasOnSiteAssessment: field,
          remoteCount: 0,
          onSiteCount: 0,
          draftCount: 0,
          remoteBookmarkCount: 2,
          onSiteBookmarkCount: 3,
          onRemoteBookmarksPressed: () {},
          onOnSiteBookmarksPressed: () {},
          onRemotePressed: () {},
          onOnSitePressed: () {},
          onDraftsPressed: () {},
        ))));
    await tester.pumpWidget(home(true));
    expect(find.text('ASSESSMENT_BOOKMARKS_REMOTE'), findsOneWidget);
    expect(find.text('ASSESSMENT_BOOKMARKS_ON_SITE'), findsOneWidget);
    expect(find.text('2'), findsOneWidget);
    expect(find.text('3'), findsOneWidget);
    await tester.pumpWidget(home(false));
    expect(find.text('ASSESSMENT_BOOKMARKS_REMOTE'), findsOneWidget);
    expect(find.text('ASSESSMENT_BOOKMARKS_ON_SITE'), findsNothing);
  });
}
