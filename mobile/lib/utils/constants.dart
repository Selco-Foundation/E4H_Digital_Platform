import 'package:flutter/material.dart';
import 'package:isar/isar.dart';
import 'package:path_provider/path_provider.dart';
import 'package:selco/data/nosql/cache_activity_facility_bom_values.dart';
import 'package:selco/data/nosql/cache_submission_job.dart';

import '../data/nosql/cache_activity_facility_asset.dart';
import '../data/nosql/cache_activity_facility_workflow.dart';
import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../data/nosql/cache_bom_doc.dart';
import '../data/nosql/cache_completion_report.dart';
import '../data/nosql/cache_media_upload.dart';
import '../data/nosql/cache_prefilled_activity_facility.dart';
import '../data/nosql/cache_specification.dart';
import '../data/nosql/cache_sync_record.dart';
import '../data/nosql/cache_unsubmitted_activity_facility.dart';
import '../data/nosql/localization.dart';

final scaffoldMessengerKey = GlobalKey<ScaffoldMessengerState>();

class Constants {
  late Future<Isar> _isar;
  late String _version;
  static final Constants _instance = Constants._();

  Constants._() {
    _isar = openIsar();
  }
  factory Constants() {
    return _instance;
  }

  Future<Isar> get isar {
    return _isar;
  }

  String get version {
    return _version;
  }

  Future<Isar> openIsar() async {
    if (Isar.instanceNames.isEmpty) {
      final directory = await getApplicationDocumentsDirectory();

      return await Isar.open(
        [
          LocalizationWrapperSchema,
          CacheActivityFacilityAssetSchema, // todo to be removed, no longer used, as there's now cache for project per assets
          CacheAssetCountSchema,
          CacheSpecificationSchema,
          CacheAssetDetailSchema,
          CacheAddNewAssetSchema,
          CacheMediaUploadSchema,
          CacheActivityFacilityWorkflowSchema,
          CacheUnsubmittedActivityFacilitySchema,
          CacheSyncRecordSchema,
          CacheCompletionReportSchema,
          CacheBomDocSchema,
          CachePrefilledActivityFacilitySchema,
          CacheActivityFacilityBomValuesSchema,
          CacheSubmissionJobSchema,
        ],
        name: 'E4H',
        inspector: true,
        directory: directory.path,
      );
    } else {
      return await Future.value(Isar.getInstance());
    }
  }

  static const String localizationApiPath = 'localization/messages/v1/_search';
}

class RequestInfoData {
  static const String apiId = 'project-api';
  static const String ver = '.01';
  static num ts = DateTime.now().millisecondsSinceEpoch;
  static const did = "1";
  static const key = "1";
  static String? authToken;
  static String msgId = "${DateTime.now().millisecondsSinceEpoch}|en_IN";
}
