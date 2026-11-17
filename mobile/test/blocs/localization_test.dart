import 'dart:async';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../lib/blocs/localization/localization.dart';
import '../../lib/data/app_shared_preferences.dart';
import '../../lib/data/nosql/localization.dart';
import '../../lib/model/localization/localizationModel.dart';
import '../../lib/repositories/localization_repo.dart';
import '../../lib/utils/envConfig.dart';
import '../support/offline_isar.dart';

class TestLocalizationRepository extends LocalizationRepository {
  final Future<LocalizationModel> Function(Map<String, String>) respond;
  final called = Completer<void>();
  TestLocalizationRepository(this.respond);

  @override
  Future<LocalizationModel> getLocalizationsList(Map<String, String> query) {
    if (!called.isCompleted) called.complete();
    return respond(query);
  }
}

LocalizationModel translations(String text, {String locale = 'en_IN'}) =>
    LocalizationModel(messages: [
      LocalizationMessageModel(
          code: 'TEST_LABEL',
          message: text,
          module: 'rainmaker-common',
          locale: locale)
    ]);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Directory directory;
  late Isar isar;
  late LocalizationBloc bloc;

  setUpAll(() async {
    await initializeOfflineIsar();
    await envConfig.initialize();
  });
  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    await AppSharedPreferences().init();
    directory = await Directory.systemTemp.createTemp('localization-bloc-');
    isar = await Isar.open([LocalizationWrapperSchema],
        directory: directory.path, name: 'localization-bloc', inspector: false);
  });
  tearDown(() async {
    await bloc.close();
    await isar.close(deleteFromDisk: true);
    await directory.delete(recursive: true);
  });

  Future<void> seed(String message, {String locale = 'en_IN'}) async {
    await isar
        .writeTxn(() => isar.localizationWrappers.put(LocalizationWrapper()
          ..locale = locale
          ..localization = [
            Localization()
              ..code = 'TEST_LABEL'
              ..message = message
              ..module = 'rainmaker-common'
              ..locale = locale
          ]));
  }

  bool ready(LocalizationState state) =>
      state.maybeWhen(selected: (_, __) => true, orElse: () => false);
  bool failed(LocalizationState state) =>
      state.maybeWhen(failure: (_) => true, orElse: () => false);
  String? message(LocalizationState state) =>
      state.whenOrNull(selected: (_, rows) => rows.single.message);

  test('cold startup stays loading until server translations arrive', () async {
    final server = Completer<LocalizationModel>();
    final repository = TestLocalizationRepository((_) => server.future);
    bloc = LocalizationBloc(isar, repository: repository);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    await repository.called.future;
    expect(bloc.state.maybeWhen(loading: (_) => true, orElse: () => false),
        isTrue);
    final loaded = bloc.stream.firstWhere(ready);
    server.complete(translations('Loaded'));
    expect(message(await loaded), 'Loaded');
    expect(
        (await isar.localizationWrappers.where().findFirst())
            ?.localization
            ?.single
            .message,
        'Loaded');
  });

  test(
      'cache is ready before a pending server refresh; failed refresh keeps it',
      () async {
    await seed('Cached');
    final server = Completer<LocalizationModel>();
    final repository = TestLocalizationRepository((_) => server.future);
    bloc = LocalizationBloc(isar, repository: repository);
    final cached = bloc.stream.firstWhere(ready);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    expect(message(await cached), 'Cached');
    await repository.called.future;
    server.completeError(StateError('offline'));
    await Future<void>.delayed(const Duration(milliseconds: 30));
    expect(message(bloc.state), 'Cached');
    expect(AppSharedPreferences().getSelectedLocale, 'en_IN');
  });

  test('no-cache failure can retry successfully', () async {
    var attempt = 0;
    bloc = LocalizationBloc(isar,
        repository: TestLocalizationRepository((_) async {
      if (attempt++ == 0) throw StateError('offline');
      return translations('Retried');
    }));
    final error = bloc.stream.firstWhere(failed);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    await error;
    final loaded = bloc.stream.firstWhere(ready);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    expect(message(await loaded), 'Retried');
  });

  test(
      'timeout ends loading; late response does not overwrite a successful retry',
      () async {
    final server = Completer<LocalizationModel>();
    var attempt = 0;
    bloc = LocalizationBloc(isar,
        requestTimeout: const Duration(milliseconds: 20),
        repository: TestLocalizationRepository((_) => attempt++ == 0
            ? server.future
            : Future.value(translations('Retry'))));
    final error = bloc.stream.firstWhere(failed);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    await error;
    final loaded = bloc.stream.firstWhere(ready);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    expect(message(await loaded), 'Retry');
    server.complete(translations('Late'));
    await Future<void>.delayed(const Duration(milliseconds: 30));
    expect(message(bloc.state), 'Retry');
  });

  test('empty and wrong-locale server results do not replace a valid cache',
      () async {
    await seed('Cached');
    var attempt = 0;
    bloc = LocalizationBloc(isar,
        repository: TestLocalizationRepository((_) async => attempt++ == 0
            ? const LocalizationModel()
            : translations('Other', locale: 'fr_FR')));
    for (var i = 0; i < 2; i++) {
      final cached = bloc.stream.firstWhere(ready);
      bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
      await cached;
      await Future<void>.delayed(const Duration(milliseconds: 30));
      expect(message(bloc.state), 'Cached');
      expect(
          (await isar.localizationWrappers.where().findFirst())
              ?.localization
              ?.single
              .message,
          'Cached');
    }
  });

  test('empty server response without cache is a retryable failure', () async {
    bloc = LocalizationBloc(isar,
        repository:
            TestLocalizationRepository((_) async => const LocalizationModel()));
    final error = bloc.stream.firstWhere(failed);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    await error;
    expect(await isar.localizationWrappers.count(), 0);
  });

  test(
      'older locale response cannot replace the newer locale or its saved preference',
      () async {
    final english = Completer<LocalizationModel>();
    final repository = TestLocalizationRepository((query) =>
        query['locale'] == 'en_IN'
            ? english.future
            : Future.value(translations('Français', locale: 'fr_FR')));
    bloc = LocalizationBloc(isar, repository: repository);
    bloc.add(const LocalizationEvent.onSelect(locale: 'en_IN'));
    await repository.called.future;
    final french = bloc.stream.firstWhere(ready);
    bloc.add(const LocalizationEvent.onSelect(locale: 'fr_FR'));
    expect(message(await french), 'Français');
    english.complete(translations('Old English'));
    await Future<void>.delayed(const Duration(milliseconds: 30));
    expect(message(bloc.state), 'Français');
    expect(bloc.locale, 'fr_FR');
    expect(AppSharedPreferences().getSelectedLocale, 'fr_FR');
  });
}
