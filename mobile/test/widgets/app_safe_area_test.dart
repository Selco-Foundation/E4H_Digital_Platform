import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/widgets/app_safe_area.dart';
import '../../lib/widgets/button/footer_button.dart';

void main() {
  Future<void> pumpScreen(
      WidgetTester tester, String label, VoidCallback onPressed) async {
    await tester.pumpWidget(MaterialApp(
      theme: DigitTheme.instance.mobileTheme,
      builder: (context, child) => AppSafeArea(child: child!),
      home: Scaffold(
        appBar: AppBar(title: const Text('Header')),
        body: ScrollableContent(
          enableFixedDigitButton: true,
          footer: FooterButton(text: label, onPress: onPressed),
          children: const [
            TextField(decoration: InputDecoration(labelText: 'Login input'))
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();
  }

  void configureView(WidgetTester tester, double bottom) {
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(400, 800);
    tester.view.viewPadding = FakeViewPadding(top: 24, bottom: bottom);
    tester.view.padding = FakeViewPadding(top: 24, bottom: bottom);
    addTearDown(tester.view.reset);
  }

  for (final bottom in [48.0, 24.0, 0.0]) {
    for (final label in ['Proceed', 'Login', 'Save', 'Next']) {
      testWidgets('$label stays tappable with bottom system inset $bottom',
          (tester) async {
        configureView(tester, bottom);
        var taps = 0;
        await pumpScreen(tester, label, () => taps++);
        final footer = find.byType(FooterButton);
        expect(tester.getBottomLeft(footer).dy, 800 - bottom);
        expect(tester.getTopLeft(find.byType(AppBar)).dy, 0);
        await tester.tap(find.text(label));
        expect(taps, 1);
        expect(tester.takeException(), isNull);
      });
    }
  }

  testWidgets('side insets apply once and preserve the header top inset',
      (tester) async {
    configureView(tester, 24);
    tester.view.viewPadding =
        const FakeViewPadding(top: 24, left: 20, right: 30, bottom: 24);
    tester.view.padding =
        const FakeViewPadding(top: 24, left: 20, right: 30, bottom: 24);
    await pumpScreen(tester, 'Next', () {});
    final footer = find.byType(FooterButton);
    expect(tester.getTopLeft(footer).dx, 20);
    expect(tester.getSize(footer).width, 350);
    final context = tester.element(footer);
    expect(MediaQuery.paddingOf(context), EdgeInsets.zero);
    expect(tester.getSize(find.byType(AppBar)).height, kToolbarHeight + 24);
  });

  testWidgets('keyboard opens and closes without extra bottom safe-area space',
      (tester) async {
    configureView(tester, 48);
    var taps = 0;
    await pumpScreen(tester, 'Login', () => taps++);
    await tester.tap(find.byType(TextField));
    await tester.enterText(find.byType(TextField), 'assessor');
    tester.view.viewInsets = const FakeViewPadding(bottom: 300);
    tester.view.padding = const FakeViewPadding(top: 24);
    await tester.pumpAndSettle();
    expect(tester.getBottomLeft(find.byType(FooterButton)).dy, 500);
    await tester.tap(find.text('Login'));
    expect(taps, 1);
    tester.view.viewInsets = const FakeViewPadding();
    tester.view.padding = const FakeViewPadding(top: 24, bottom: 48);
    await tester.pumpAndSettle();
    expect(tester.getBottomLeft(find.byType(FooterButton)).dy, 752);
    expect(find.text('assessor'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
