import 'dart:convert';
import 'dart:io';
import 'dart:ffi';

import 'package:isar/isar.dart';

import '../../lib/data/nosql/cache_activity_facility_workflow.dart';
import '../../lib/data/nosql/cache_assessment_queue.dart';
import '../../lib/data/nosql/cache_assessment_draft.dart';
import '../../lib/data/nosql/cache_prefilled_scheduled_visit.dart';
import '../../lib/data/nosql/cache_scheduled_visit.dart';
import '../../lib/data/nosql/cache_unsubmitted_activity_facility.dart';

Future<void> initializeOfflineIsar() async {
  final configFile = File('.dart_tool/package_config.json');
  final config = jsonDecode(await configFile.readAsString()) as Map;
  final package = (config['packages'] as List)
      .cast<Map>()
      .firstWhere((package) => package['name'] == 'isar_flutter_libs');
  final root = Directory.fromUri(
          configFile.absolute.uri.resolve(package['rootUri'] as String))
      .uri;
  final libraries = <Abi, String>{
    Abi.macosArm64: root.resolve('macos/libisar.dylib').toFilePath(),
    Abi.macosX64: root.resolve('macos/libisar.dylib').toFilePath(),
    Abi.linuxX64: root.resolve('linux/libisar.so').toFilePath(),
    Abi.windowsX64: root.resolve('windows/isar.dll').toFilePath(),
  };
  await Isar.initializeIsarCore(libraries: libraries);
}

Future<Isar> openOfflineIsar(String directory, String name,
        {bool includeAssessment = true}) =>
    Isar.open(
      [
        CacheActivityFacilityWorkflowSchema,
        CacheAssessmentDraftSchema,
        CacheUnsubmittedActivityFacilitySchema,
        CacheScheduledVisitSchema,
        CachePrefilledScheduledVisitSchema,
        if (includeAssessment) CacheAssessmentQueueSchema,
      ],
      directory: directory,
      name: name,
      inspector: false,
    );
