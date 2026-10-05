import 'package:digit_ui_components/digit_components.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';
import 'package:selco/blocs/activity_facility/activity_facility.dart';
import 'package:selco/blocs/app_init/app_init.dart';
import 'package:selco/pages/select_health_facility.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;
import '../support/asset_store.dart';

class LocalActivityBloc
    extends Bloc<ActivityFacilityEvent, ActivityFacilityState>
    implements ActivityFacilityBloc {
  @override
  final Isar isar;
  LocalActivityBloc(this.isar) : super(const ActivityFacilityState.initial());
}

class LocalInit extends Bloc<InitEvent, InitState>
    implements AppInitialization {
  LocalInit() : super(const InitState.uninitialized());
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected initialization');
}

void main() {
  for (final type in ['battery', 'inverter', 'panel']) {
    testWidgets(
        '$type Summary readiness enables facility submission even at low overall progress',
        (tester) async {
      final store = AssetStore();
      final bloc = LocalActivityBloc(store);
      final init = LocalInit();
      addTearDown(bloc.close);
      addTearDown(init.close);
      var taps = 0;
      Widget card() => MultiBlocProvider(
              providers: [
                BlocProvider<ActivityFacilityBloc>.value(value: bloc),
                BlocProvider<AppInitialization>.value(value: init),
              ],
              child: MaterialApp(
                  theme: DigitTheme.instance.mobileTheme,
                  home: Scaffold(
                      body: SingleChildScrollView(
                          child: InstallationReportCard(
                    projectId: 'facility',
                    title: 'Clinic',
                    dateAssigned: DateTime(2026),
                    fraction: 0.1,
                    onPress: () => taps++,
                  )))));
      DigitButton submit() => tester
          .widgetList<DigitButton>(find.byType(DigitButton))
          .singleWhere((button) =>
              button.label == i18.selectHealthFacility.submitForApproval);
      await tester.pumpWidget(card());
      await tester.pumpAndSettle();
      expect(submit().isDisabled, isTrue);
      store.counts.add(count(type));
      await tester.pumpWidget(card());
      await tester.pumpAndSettle();
      expect(submit().isDisabled, isTrue);
      store.assets.add(asset(type));
      await tester.pumpWidget(card());
      await tester.pumpAndSettle();
      expect(submit().isDisabled, isFalse);
      submit().onPressed();
      expect(taps, 1);
      expect(tester.takeException(), isNull);
    });
  }
}
