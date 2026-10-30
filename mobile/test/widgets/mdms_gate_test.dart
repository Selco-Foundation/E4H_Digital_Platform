import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../lib/blocs/app_init/app_init.dart';
import '../../lib/data/api_interceptors.dart';
import '../../lib/widgets/mdms/mdms_gate.dart';

void main() {
  late AppInitialization initialization;
  setUp(() {
    initialization = AppInitialization();
    AuthTokenInterceptor.resetLogoutGuard();
  });
  tearDown(() => initialization.close());

  Widget app(Widget home, {GlobalKey<NavigatorState>? navigatorKey}) =>
      BlocProvider.value(
        value: initialization,
        child: MaterialApp(navigatorKey: navigatorKey, home: home),
      );

  for (final module in MdmsModule.values) {
    testWidgets('$module home blocks navigation and supports repeated Retry',
        (tester) async {
      var calls = 0;
      var entered = 0;
      var pending = Completer<void>();
      final home = MdmsGate(
        module: module,
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
      await tester.pumpWidget(app(home));
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
      await tester.pumpWidget(app(home));
      await tester.pumpAndSettle();
      expect(calls, 3); // A rebuild/return does not refresh a ready home.
    });
  }

  for (final module in MdmsModule.values) {
    testWidgets(
        '$module recreated home reuses readiness until reentry or login',
        (tester) async {
      var calls = 0;
      var entered = 0;
      final navigator = GlobalKey<NavigatorState>();
      Widget home() => MdmsGate(
            module: module,
            load: () async {
              calls++;
            },
            child: Scaffold(
                body: TextButton(
                    onPressed: () => entered++, child: const Text('Reports'))),
          );
      await tester.pumpWidget(app(home(), navigatorKey: navigator));
      await tester.pumpAndSettle();
      expect(calls, 1);
      expect(initialization.isModuleReady(module), isTrue);

      // Submission replaces the stack, destroying the original home gate.
      navigator.currentState!.pushAndRemoveUntil(
        MaterialPageRoute<void>(
            builder: (_) => const Scaffold(body: Text('Submitted'))),
        (_) => false,
      );
      await tester.pumpAndSettle();
      navigator.currentState!.pushReplacement(
        MaterialPageRoute<void>(builder: (_) => home()),
      );
      await tester.pump();
      expect(find.byType(Dialog), findsNothing);
      await tester.pumpAndSettle();
      expect(calls, 1);
      await tester.tap(find.text('Reports'));
      expect(entered, 1);

      // Explicit role selection refreshes only the selected module.
      initialization.invalidateModule(module);
      navigator.currentState!.pushReplacement(
        MaterialPageRoute<void>(builder: (_) => home()),
      );
      await tester.pumpAndSettle();
      expect(calls, 2);

      // Login/session restoration changes the generation, invalidating readiness.
      AuthTokenInterceptor.resetLogoutGuard();
      navigator.currentState!.pushReplacement(
        MaterialPageRoute<void>(builder: (_) => home()),
      );
      await tester.pumpAndSettle();
      expect(calls, 3);
    });
  }

  test('role reentry clears only the selected module readiness', () {
    final session = AuthTokenInterceptor.sessionGeneration;
    for (final module in MdmsModule.values) {
      initialization.markModuleReady(module, session);
    }
    initialization.invalidateModule(MdmsModule.amc);
    expect(initialization.isModuleReady(MdmsModule.amc), isFalse);
    expect(initialization.isModuleReady(MdmsModule.installation), isTrue);
    expect(initialization.isModuleReady(MdmsModule.assessment), isTrue);
  });

  testWidgets('old session completion cannot mark a module ready',
      (tester) async {
    final pending = Completer<void>();
    await tester.pumpWidget(app(MdmsGate(
      module: MdmsModule.assessment,
      load: () => pending.future,
      child: const Scaffold(),
    )));
    await tester.pump();
    AuthTokenInterceptor.resetLogoutGuard();
    pending.complete();
    await tester.pumpAndSettle();
    expect(initialization.isModuleReady(MdmsModule.assessment), isFalse);
  });

  testWidgets('removing a loading home removes only its own dialog',
      (tester) async {
    final pending = Completer<void>();
    final navigator = GlobalKey<NavigatorState>();
    await tester.pumpWidget(app(
      MdmsGate(
          module: MdmsModule.installation,
          load: () => pending.future,
          child: const Scaffold(body: Text('Home'))),
      navigatorKey: navigator,
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
