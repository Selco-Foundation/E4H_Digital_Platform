import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';

import '../../lib/data/nosql/cache_activity_facility_bom_values.dart';
import '../../lib/data/nosql/cache_activity_facility_workflow.dart';
import '../../lib/data/nosql/cache_bom_doc.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/repositories/dynamic_form_repo.dart';
import '../../lib/utils/envConfig.dart';
import '../support/offline_isar.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Directory directory;
  late Isar isar;
  late BomRepository repository;
  const template = {
    'led_bulb_1_make': null,
    'switch_modular_make': null,
    'dc_array_size_kwp': null,
    'solar_charge_controller_make': 'Template brand',
  };
  const savedValues = {
    'led_bulb_1_make': 'KEI',
    'switch_modular_make': 'ANCHOR',
    'dc_array_size_kwp': '1.98',
  };

  setUpAll(() async {
    await envConfig.initialize();
    await initializeOfflineIsar();
  });
  setUp(() async {
    directory = await Directory.systemTemp.createTemp('bom-initial-values');
    isar = await Isar.open([
      CacheActivityFacilityBomValuesSchema,
      CacheActivityFacilityWorkflowSchema,
      CacheBomDocSchema,
    ], directory: directory.path, name: 'bom-values', inspector: false);
    repository = BomRepository();
    await isar.writeTxn(() async {
      await isar.cacheActivityFacilityWorkflows
          .put(CacheActivityFacilityWorkflow(
        status: 'SUBMITTED_BY_SUPERVISOR',
        activityFacilityId: 'activity',
        activityFacility: ActivityFacility(
          id: 'activity',
          tenantId: 'in',
          additionalDetails: AdditionalDetails()..bom = Map.from(template),
        ),
      ));
    });
    // A different form's local document must not hide submitted values.
    await repository.saveLocal(
      isar: isar,
      activityFacilityId: 'activity',
      schemaKey: 'AssetForm.DC_BOM_Solar',
      rawDocWithValues: {},
    );
  });
  tearDown(() async {
    await isar.close(deleteFromDisk: true);
    await directory.delete(recursive: true);
  });

  Future<void> cacheValues(Map<String, dynamic> values) =>
      isar.writeTxn(() async {
        await isar.cacheActivityFacilityBomValues.put(
          CacheActivityFacilityBomValues()
            ..activityFacilityId = 'activity'
            ..userType = 'SUPERVISOR'
            ..entryKey = 'activity::SUPERVISOR'
            ..dataJson = jsonEncode(values),
        );
      });

  Future<Map<String, dynamic>> read(String schemaKey) =>
      repository.getInitialFormValuesForSchema(
        isar: isar,
        activityFacilityId: 'activity',
        userType: 'SUPERVISOR',
        schemaKey: schemaKey,
      );

  for (final entry in {
    'AssetForm.DC_BOM_luminaries': 'led_bulb_1_make',
    'AssetForm.DC_BOM_wiring': 'switch_modular_make',
    'AssetForm.DC_BOM_system': 'dc_array_size_kwp',
  }.entries) {
    test('${entry.key} displays saved values with an unrelated local form',
        () async {
      await cacheValues(savedValues);
      final values = await read(entry.key);
      expect(values[entry.value], savedValues[entry.value]);
      expect(values, savedValues);
    });
  }

  for (final emptyCache in [false, true]) {
    test(
        'template fallback when cache ${emptyCache ? 'is empty' : 'is missing'}',
        () async {
      if (emptyCache) await cacheValues({});
      expect(await read('AssetForm.DC_BOM_system'), template);
    });
  }
}
