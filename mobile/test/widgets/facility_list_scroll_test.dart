import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/widgets/facility_list_controls.dart';

void main() {
  testWidgets('dragging cards scrolls the page without nested scroll updates',
      (tester) async {
    final page = ScrollController();
    final depths = <int>[];
    await tester.pumpWidget(MaterialApp(
      theme: DigitTheme.instance.mobileTheme,
      home: NotificationListener<ScrollUpdateNotification>(
        onNotification: (notification) {
          depths.add(notification.depth);
          return false;
        },
        child: RefreshableFacilityList(
          onRefresh: () async {},
          child: ScrollableContent(controller: page, children: const [
            DigitCard(children: [Text('Search controls')]),
            DigitCard(children: [SizedBox(height: 200, child: Text('Report'))]),
            SizedBox(height: 1200),
          ]),
        ),
      ),
    ));
    for (final label in ['Search controls', 'Report']) {
      page.jumpTo(0);
      await tester.pump();
      await tester.drag(find.text(label), const Offset(0, -150));
      await tester.pumpAndSettle();
      expect(page.offset, greaterThan(0));
      final cards = tester.stateList<ScrollableState>(find.descendant(
          of: find.byType(DigitCard), matching: find.byType(Scrollable)));
      expect(cards.every((card) => card.position.pixels == 0), isTrue);
    }
    expect(depths, isNotEmpty);
    expect(depths.every((depth) => depth == 0), isTrue);
    await tester.pumpWidget(const SizedBox.shrink());
    page.dispose();
  });

  for (final height in [0.0, 100.0, 1200.0]) {
    testWidgets('pull to refresh works with content height $height',
        (tester) async {
      var refreshes = 0;
      await tester.pumpWidget(MaterialApp(
        home: RefreshableFacilityList(
          onRefresh: () async => refreshes++,
          child: ScrollableContent(children: [SizedBox(height: height)]),
        ),
      ));
      await tester.drag(find.byType(CustomScrollView), const Offset(0, 400));
      await tester.pumpAndSettle();
      expect(refreshes, 1);
    });
  }
}
