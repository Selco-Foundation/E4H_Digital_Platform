import 'package:flutter/material.dart';
import 'package:isar/isar.dart';
import 'package:selco/utils/utils.dart';

final scaffoldMessengerKey = GlobalKey<ScaffoldMessengerState>();

class Constants {
  late Future<Isar> _isar;
  late String _version;
  static final Constants _instance = Constants._();

  Constants._() {
    // _isar = openIsar();
  }
  factory Constants() {
    return _instance;
  }

  Future initialize(version) async {
    await initializeAllMappers();
    //setInitialDataOfPackages();
    // await _initializeIsar(version);
  }

  Future<Isar> get isar {
    return _isar;
  }

  String get version {
    return _version;
  }

  Future openIsar() async {
    if (Isar.instanceNames.isEmpty) {
      return await Future.value(Isar.getInstance());
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
