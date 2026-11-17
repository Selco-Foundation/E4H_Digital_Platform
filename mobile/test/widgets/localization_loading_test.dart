import 'package:digit_forms_engine/blocs/app_localization.dart';
import 'package:digit_scanner/blocs/app_localization.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';

import '../../lib/blocs/localization/app_localization.dart';
import '../../lib/blocs/localization/localization.dart';
import '../../lib/blocs/localization/package_localization_delegates.dart';
import '../../lib/data/nosql/localization.dart';
import '../../lib/model/appconfig/mdmsResponse.dart';
import '../../lib/utils/extensions.dart';
import '../../lib/widgets/localization_loading_screen.dart';

class NoDatabase implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected database read');
}

const config = AppConfig(appConfig: [
  AppConfigListItems(
      tenantId: 'in',
      languages: [
        Language(label: 'English', value: 'en_IN'),
        Language(label: 'French', value: 'fr_FR')
      ],
      backendInterface: null,
      genderOptions: [])
]);

List<Localization> messages(String text, {String locale = 'en_IN'}) => [
      Localization()
        ..code = 'TEST_LABEL'
        ..message = text
        ..module = 'rainmaker-common'
        ..locale = locale
    ];

class TranslatedScreen extends StatefulWidget {
  final VoidCallback onInit;
  const TranslatedScreen({super.key, required this.onInit});
  @override
  State<TranslatedScreen> createState() => _TranslatedScreenState();
}

class _TranslatedScreenState extends State<TranslatedScreen> {
  final input = TextEditingController();
  @override
  void initState() {
    super.initState();
    widget.onInit();
  }

  @override
  void dispose() {
    input.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
          body: Column(children: [
        Text(context.translate('TEST_LABEL')),
        Text(ScannerLocalization.of(context).translate('TEST_LABEL')),
        Text(FormLocalization.of(context).translate('TEST_LABEL')),
        TextField(controller: input),
        TextButton(
            onPressed: () => Navigator.of(context).push(MaterialPageRoute<void>(
                builder: (_) =>
                    Scaffold(appBar: AppBar(title: const Text('Next route'))))),
            child: const Text('Next')),
      ]));
}

void main() {
  late ValueNotifier<LocalizationState> state;
  late Isar isar;
  var initializations = 0;
  var retries = 0;
  setUp(() {
    state = ValueNotifier(const LocalizationState.initial());
    isar = NoDatabase();
    initializations = 0;
    retries = 0;
  });
  tearDown(() => state.dispose());

  Widget app() => ValueListenableBuilder<LocalizationState>(
      valueListenable: state,
      builder: (context, value, _) {
        final rows = value.maybeWhen(
            selected: (_, rows) => rows, orElse: () => const <Localization>[]);
        final locale = value.maybeWhen(
            selected: (locale, _) => locale,
            loading: (locale) => locale,
            failure: (locale) => locale,
            orElse: () => 'en_IN');
        final parts = locale.split('_');
        final selected = Locale(parts.first, parts.last);
        final snapshot = AppLocalizations(selected, isar, messages: rows);
        return MaterialApp(
          theme: DigitTheme.instance.mobileTheme
              .copyWith(splashFactory: NoSplash.splashFactory),
          locale: selected,
          supportedLocales: const [Locale('en', 'IN'), Locale('fr', 'FR')],
          localizationsDelegates: [
            AppLocalizations.getDelegate(config, isar, messages: rows),
            ScannerTranslationsDelegate(snapshot),
            FormTranslationsDelegate(snapshot),
          ],
          home: LocalizationGate(
              state: value,
              onRetry: () {
                retries++;
                state.value = const LocalizationState.loading(locale: 'en_IN');
              },
              child: TranslatedScreen(onInit: () => initializations++)),
        );
      });

  testWidgets(
      'startup only mounts translated screens once a completed snapshot is ready',
      (tester) async {
    await tester.pumpWidget(app());
    await tester.pump();
    expect(find.text('Loading…'), findsOneWidget);
    expect(find.text('TEST_LABEL'), findsNothing);
    expect(initializations, 0);
    state.value = LocalizationState.selected(
        locale: 'en_IN', messages: messages('Ready'));
    await tester.pump();
    expect(find.text('Ready'), findsNWidgets(3));
    expect(find.text('Loading…'), findsNothing);
    expect(initializations, 1);
  });

  testWidgets('failure shows one plain snackbar and Retry returns to loading',
      (tester) async {
    await tester.pumpWidget(app());
    await tester.pump();
    state.value = const LocalizationState.failure(locale: 'en_IN');
    await tester.pump();
    await tester.pump();
    expect(find.text('Failed to load localization'), findsOneWidget);
    expect(find.text('Retry'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);
    await tester.pump();
    expect(find.text('Failed to load localization'), findsOneWidget);
    await tester.tap(find.text('Retry'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));
    expect(retries, 1);
    expect(find.text('Loading…'), findsOneWidget);
    expect(find.text('Failed to load localization'), findsNothing);
    state.value = LocalizationState.selected(
        locale: 'en_IN', messages: messages('Retried'));
    await tester.pump();
    expect(find.text('Retried'), findsNWidgets(3));
    expect(initializations, 1);
  });

  testWidgets(
      'same-locale refresh changes all three translations without remounting or losing input',
      (tester) async {
    state.value = LocalizationState.selected(
        locale: 'en_IN', messages: messages('Cached'));
    await tester.pumpWidget(app());
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), 'Unsent draft');
    state.value = LocalizationState.selected(
        locale: 'en_IN', messages: messages('Refreshed'));
    await tester.pump();
    expect(find.text('Refreshed'), findsNWidgets(3));
    expect(find.text('TEST_LABEL'), findsNothing);
    expect(find.text('Loading…'), findsNothing);
    expect(find.text('Unsent draft'), findsOneWidget);
    expect(initializations, 1);
    await tester.tap(find.text('Next'));
    await tester.pumpAndSettle();
    state.value = LocalizationState.selected(
        locale: 'en_IN', messages: messages('Updated again'));
    await tester.pump();
    expect(find.text('Next route'), findsOneWidget);
    await tester.tap(find.byType(BackButton));
    await tester.pumpAndSettle();
    expect(find.text('Updated again'), findsNWidgets(3));
    expect(find.text('Unsent draft'), findsOneWidget);
    expect(initializations, 1);
  });

  test(
      'loading another locale does not mutate existing app/scanner/form snapshots',
      () async {
    final english = AppLocalizations(const Locale('en', 'IN'), isar,
        messages: messages('Cached'));
    final scanner =
        await ScannerTranslationsDelegate(english).load(english.locale);
    final forms = await FormTranslationsDelegate(english).load(english.locale);
    final french = AppLocalizations(const Locale('fr', 'FR'), isar,
        messages: messages('Français', locale: 'fr_FR'));
    await ScannerTranslationsDelegate(french).load(french.locale);
    await FormTranslationsDelegate(french).load(french.locale);
    expect(english.translate('TEST_LABEL'), 'Cached');
    expect(scanner.translate('TEST_LABEL'), 'Cached');
    expect(forms.translate('TEST_LABEL'), 'Cached');
    expect(french.translate('MISSING_KEY'), 'MISSING_KEY');
  });
}
