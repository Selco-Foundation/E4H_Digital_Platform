import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/widgets/mdms/mdms_gate.dart';

void main() {
  for (final module in ['Installation', 'AMC', 'Assessment']) {
    testWidgets('$module home blocks navigation and supports repeated Retry',
        (tester) async {
      var calls = 0;
      var entered = 0;
      var pending = Completer<void>();
      final home = MdmsGate(
        load: () {
          calls++;
          return pending.future;
        },
        child: Scaffold(
            body: TextButton(
          onPressed: () => entered++,
          child: Text('$module reports'),
        )),
      );
      await tester.pumpWidget(MaterialApp(home: home));
      await tester.pump();
      expect(find.byType(Dialog), findsOneWidget);
      pending.completeError(StateError('Server unavailable'));
      await tester.pumpAndSettle();
      expect(find.text('CORE_COMMON_RETRY'), findsOneWidget);
      expect(entered, 0);
      expect(calls, 1);

      pending = Completer<void>();
      await tester.tap(find.byType(TextButton).last);
      await tester.pump();
      pending.completeError(StateError('Still unavailable'));
      await tester.pumpAndSettle();
      expect(calls, 2);
      expect(find.byType(Dialog), findsNothing);

      pending = Completer<void>();
      await tester.tap(find.byType(TextButton).last);
      await tester.pump();
      pending.complete();
      await tester.pumpAndSettle();
      await tester.tap(find.text('$module reports'));
      expect(entered, 1);
      await tester.pumpWidget(MaterialApp(home: home));
      await tester.pumpAndSettle();
      expect(calls, 3); // A rebuild/return does not refresh a ready home.
    });
  }

  testWidgets('removing a loading home removes only its own dialog',
      (tester) async {
    final pending = Completer<void>();
    final navigator = GlobalKey<NavigatorState>();
    await tester.pumpWidget(MaterialApp(
      navigatorKey: navigator,
      home: MdmsGate(
          load: () => pending.future,
          child: const Scaffold(body: Text('Home'))),
    ));
    await tester.pump();
    navigator.currentState!.pushAndRemoveUntil(
      MaterialPageRoute<void>(
          builder: (_) => const Scaffold(body: Text('Login'))),
      (_) => false,
    );
    await tester.pumpAndSettle();
    pending.complete();
    await tester.pumpAndSettle();
    expect(find.text('Login'), findsOneWidget);
    expect(find.byType(Dialog), findsNothing);
    expect(tester.takeException(), isNull);
  });
}
