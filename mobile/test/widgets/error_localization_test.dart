import 'dart:io';

import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';

import '../../lib/blocs/localization/app_localization.dart';
import '../../lib/data/nosql/localization.dart';
import '../../lib/utils/error_localization.dart';
import '../../lib/utils/extensions.dart';
import '../../lib/utils/operation_progress.dart';
import '../../lib/widgets/progress_indicator/operation_progress_overlay.dart';
import '../support/offline_isar.dart';

class LoadedDelegate extends LocalizationsDelegate<AppLocalizations> {
  const LoadedDelegate(this.cached);
  final AppLocalizations cached;
  @override
  bool isSupported(Locale locale) => true;
  @override
  bool shouldReload(LoadedDelegate old) => true;
  @override
  Future<AppLocalizations> load(Locale locale) async => cached;
}

void main() {
  late Directory directory;
  late Isar isar;
  setUpAll(initializeOfflineIsar);
  setUp(() async {
    directory = await Directory.systemTemp.createTemp('error-localizations-');
    isar = await Isar.open([LocalizationWrapperSchema],
        directory: directory.path,
        name: 'error-localizations',
        inspector: false);
  });
  tearDown(() async {
    await isar.close(deleteFromDisk: true);
    await directory.delete(recursive: true);
  });

  Future<void> cacheMessage(String message) async {
    await isar.writeTxn(() async {
      await isar.localizationWrappers.clear();
      await isar.localizationWrappers.put(LocalizationWrapper()
        ..locale = 'en_IN'
        ..localization = [
          Localization()
            ..code = ErrorKeys.reportConnectionInterrupted
            ..message = message
            ..module = 'rainmaker-common'
            ..locale = 'en_IN'
        ]);
    });
  }

  test(
      'cache works after reopen; backend refresh changes copy; missing keys remain keys',
      () async {
    await cacheMessage('Cached backend copy');
    await isar.close();
    isar = await Isar.open([LocalizationWrapperSchema],
        directory: directory.path,
        name: 'error-localizations',
        inspector: false);
    final localizations = AppLocalizations(const Locale('en', 'IN'), isar);
    expect(await localizations.load(), isTrue);
    expect(localizations.translate(ErrorKeys.reportConnectionInterrupted),
        'Cached backend copy');
    expect(localizations.translate(ErrorKeys.reportSave), ErrorKeys.reportSave);
    await cacheMessage('Updated backend copy');
    await localizations.load();
    expect(localizations.translate(ErrorKeys.reportConnectionInterrupted),
        'Updated backend copy');
  });

  testWidgets(
      'progress overlay and notification translate the same persisted key',
      (tester) async {
    await tester.runAsync(() => cacheMessage('Backend connection message'));
    final cached = AppLocalizations(const Locale('en', 'IN'), isar);
    await tester.runAsync(cached.load);
    tester.view.physicalSize = const Size(500, 1000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(MaterialApp(
      locale: const Locale('en', 'IN'),
      supportedLocales: const [Locale('en', 'IN')],
      localizationsDelegates: [LoadedDelegate(cached)],
      theme: DigitTheme.instance.mobileTheme,
      home: Scaffold(
          body: Builder(
              builder: (context) => Stack(children: [
                    TextButton(
                        onPressed: () => ScaffoldMessenger.of(context)
                            .showSnackBar(SnackBar(
                                content: Text(context.localizeError(
                                    ErrorKeys.reportConnectionInterrupted)))),
                        child: const Text('Notify')),
                    const OperationProgressOverlay(
                        progress: OperationProgressModel(
                            activityFacilityId: 'facility',
                            operationType: OperationTypes.submit,
                            status: OperationStatuses.failed,
                            stageKey: 'preparing_submission',
                            stageLabel: 'Preparing',
                            completedSteps: 0,
                            totalSteps: 1,
                            progressPercent: 0,
                            retryCount: 0,
                            isBlocking: true,
                            errorMessage:
                                ErrorKeys.reportConnectionInterrupted)),
                  ]))),
    ));
    await tester.pumpAndSettle();
    expect(find.text('Backend connection message'), findsOneWidget);
    // Invoke the same button callback directly: the modal intentionally blocks taps.
    tester.widget<TextButton>(find.byType(TextButton).first).onPressed!();
    await tester.pump();
    expect(find.text('Backend connection message'), findsNWidgets(2));
    expect(find.text(ErrorKeys.reportConnectionInterrupted), findsNothing);
    expect(tester.takeException(), isNull);
  });
}
