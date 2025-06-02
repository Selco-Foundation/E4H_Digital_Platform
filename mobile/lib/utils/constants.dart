import 'package:flutter/material.dart';
import 'package:isar/isar.dart';
import 'package:path_provider/path_provider.dart';
import 'package:selco/data/nosql/cache_add_new_asset.dart';
import 'package:selco/data/nosql/cache_asset_count.dart';
import 'package:selco/data/nosql/cache_asset_detail.dart';
import 'package:selco/data/nosql/cache_media_upload.dart';
import 'package:selco/data/nosql/cache_specification.dart';

import '../data/nosql/cache_project_asset.dart';
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
          CacheProjectAssetSchema,
          CacheAssetCountSchema,
          CacheSpecificationSchema,
          CacheAssetDetailSchema,
          CacheAddNewAssetSchema,
          CacheMediaUploadSchema
        ],
        name: 'HCM',
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
  static const String apiId = 'hcm';
  static const String ver = '.01';
  static num ts = DateTime.now().millisecondsSinceEpoch;
  static const did = "1";
  static const key = "1";
  static String? authToken;
}
