import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar_community/isar.dart';

import '../../lib/blocs/localization/app_localization.dart';
import '../../lib/data/nosql/localization.dart';
import '../../lib/utils/error_localization.dart';
import '../../lib/utils/scanner_localization.dart';
import '../support/offline_isar.dart';

void main() {
  final rows = (jsonDecode(File('docs/localization/scanner_messages.en_IN.json')
          .readAsStringSync()) as List)
      .cast<Map>();
  final english = {
    for (final row in rows) row['code'] as String: row['message'] as String
  };
  String translate(String key) => english[key] ?? key;

  test('legacy mixed-case validation aliases cannot leak into scanner toasts',
      () {
    for (final alias in [
      'Common_error_scan_duplicate',
      'COMMON_ERROR_SCAN_DUPLICATE',
      'Resource_already_scanned',
      'RESOURCES_ALREADY_SCANNED'
    ]) {
      expect(scannerValidationKey('duplicate', alias, translate),
          ErrorKeys.scanDuplicate);
      expect(scannerErrorKey(alias, translate), ErrorKeys.scanDuplicate);
    }
    expect(
        scannerValidationKey('pattern', 'Common_error_scan_pattern', translate),
        ErrorKeys.scanPattern);
    expect(
        scannerValidationKey(
            'scanLimit', 'SCANNED_QTY_EXCEED_THAN_REQUIRED', translate),
        ErrorKeys.scanLimit);
    expect(scannerValidationKey('invalidGS1', 'INVALID_BARCODE', translate),
        ErrorKeys.invalidGs1);
  });

  test(
      'custom overrides must resolve for the selected locale; missing defaults stay keys',
      () {
    String localized(String key) =>
        key == 'CUSTOM_DUPLICATE' ? 'Backend override' : key;
    expect(scannerValidationKey('duplicate', 'CUSTOM_DUPLICATE', localized),
        'CUSTOM_DUPLICATE');
    expect(scannerValidationKey('duplicate', 'MISSING_CUSTOM_KEY', localized),
        ErrorKeys.scanDuplicate);
    expect(
        scannerValidationKey(
            'duplicate', 'Hardcoded English override', localized),
        ErrorKeys.scanDuplicate);
    expect(scannerValidationKey('duplicate', null, localized),
        ErrorKeys.scanDuplicate);
    expect(scannerErrorKey('Private plugin exception', localized),
        ErrorKeys.scannerFailed);
    expect(scannerErrorKey('FAILED_TO_READ_PHOTO', localized),
        'FAILED_TO_READ_PHOTO');
  });

  test('zero, one, and multiple counts use complete backend templates', () {
    expect(scannedCountLabel(0, translate), '0 codes scanned');
    expect(scannedCountLabel(1, translate), '1 code scanned');
    expect(scannedCountLabel(2, translate), '2 codes scanned');
    String reordered(String key) => key == ScannerKeys.codeScanned
        ? 'Sélection : {count} code'
        : 'Sélection : {count} codes';
    expect(scannedCountLabel(2, reordered), 'Sélection : 2 codes');
  });

  test(
      'scanner deployment JSON has the exact upload shape and covers scanner strings',
      () {
    expect(english.length, rows.length);
    for (final row in rows) {
      expect(row.keys.toSet(), {'code', 'message', 'module', 'locale'});
      expect(row['module'], 'rainmaker-common');
      expect(row['locale'], 'en_IN');
      expect(row['message'], isNotEmpty);
    }
    final source = File('lib/widgets/customized_digit_widget/qr_scanner.dart')
            .readAsStringSync() +
        File('lib/widgets/customized_digit_widget/scanner_utils.dart')
            .readAsStringSync();
    final packageConfig =
        jsonDecode(File('.dart_tool/package_config.json').readAsStringSync())
            as Map;
    final package = (packageConfig['packages'] as List)
        .cast<Map>()
        .singleWhere((row) => row['name'] == 'digit_scanner');
    final constants = File.fromUri(Uri.parse('${package['rootUri']}/')
            .resolve('lib/utils/i18_key_constants.dart'))
        .readAsStringSync();
    for (final match
        in RegExp(r'i18\.(?:common|scanner)\.(\w+)').allMatches(source)) {
      final getter =
          RegExp('String get ${match[1]}\\s*=>\\s*\x27([^\x27]+)\x27')
              .firstMatch(constants);
      // App-only gallery keys are checked separately below.
      if (getter != null) {
        expect(english, contains(getter[1]), reason: match[0]);
      }
    }
    for (final key in [
      ...scannerErrorDefaults.values,
      'CHANGING_CAMERA_LENS',
      'SCAN_FROM_PHOTO',
      'NO_CODE_FOUND_IN_PHOTO',
      'FAILED_TO_READ_PHOTO',
      ScannerKeys.codeScanned,
      ScannerKeys.codesScanned,
      ScannerKeys.codeRequired,
      ScannerKeys.codesRequired
    ]) {
      expect(english, contains(key));
    }
  });

  test(
      'scanner messages survive offline reopen and use the selected locale cache',
      () async {
    await initializeOfflineIsar();
    final directory = await Directory.systemTemp.createTemp('scanner-cache-');
    var isar = await Isar.open([LocalizationWrapperSchema],
        directory: directory.path, name: 'scanner-cache', inspector: false);
    try {
      await isar.writeTxn(() async {
        await isar.localizationWrappers.put(LocalizationWrapper()
          ..locale = 'en_IN'
          ..localization = [
            for (final row in rows)
              Localization()
                ..code = row['code'] as String
                ..message = row['message'] as String
                ..module = 'rainmaker-common'
                ..locale = 'en_IN'
          ]);
        await isar.localizationWrappers.put(LocalizationWrapper()
          ..locale = 'fr_FR'
          ..localization = [
            Localization()
              ..code = ScannerKeys.codesScanned
              ..message = 'Sélection : {count} codes'
              ..module = 'rainmaker-common'
              ..locale = 'fr_FR'
          ]);
      });
      await isar.close();
      isar = await Isar.open([LocalizationWrapperSchema],
          directory: directory.path, name: 'scanner-cache', inspector: false);
      final cached = AppLocalizations(const Locale('en', 'IN'), isar);
      expect(await cached.load(), isTrue);
      expect(scannedCountLabel(2, cached.translate), '2 codes scanned');
      final french = AppLocalizations(const Locale('fr', 'FR'), isar);
      await french.load();
      expect(scannedCountLabel(2, french.translate), 'Sélection : 2 codes');
      expect(
          french.translate(ScannerKeys.codeScanned), ScannerKeys.codeScanned);
    } finally {
      await isar.close(deleteFromDisk: true);
      await directory.delete(recursive: true);
    }
  });
}
