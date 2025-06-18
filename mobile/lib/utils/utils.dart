import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:path_provider/path_provider.dart';
import 'package:uuid/uuid.dart';

import '../blocs/app_init/app_init.dart';
import '../data/app_shared_preferences.dart';

getSelectedLanguage(Initialized state, int index) {
  if (AppSharedPreferences().getSelectedLocale == null) {
    AppSharedPreferences().setSelectedLocale(
        state.appConfig.appConfig!.appConfig![0].languages.last.value);
  }
  final selectedLanguage = AppSharedPreferences().getSelectedLocale;
  final isSelected =
      state.appConfig.appConfig!.appConfig![0].languages[index].value ==
          selectedLanguage;

  return isSelected;
}

class IdGen {
  static const IdGen _instance = IdGen._internal();

  static IdGen get instance => _instance;

  /// Shorthand for [instance]
  static IdGen get i => instance;

  final Uuid uuid;

  const IdGen._internal() : uuid = const Uuid();

  String get identifier => uuid.v1();
}

Future<String> copyFileToLocalDir(File sourceFile) async {
  final appDocDir = await getApplicationDocumentsDirectory();
  final timestamp = DateTime.now().millisecondsSinceEpoch;
  final fileName = '${timestamp}_${sourceFile.uri.pathSegments.last}';
  final dest = File('${appDocDir.path}/$fileName');
  final copied = await sourceFile.copy(dest.path);
  return copied.path;
}

String truncateText(String text, {int maxLength = 16}) {
  if (text.length > maxLength) {
    return '${text.substring(0, maxLength)}...';
  }
  return text;
}

class DioErrorParser {
  static Exception parse(DioError dioErr) {
    debugPrint("Dio error: ${dioErr.response?.data ?? dioErr}");

    final serverData = dioErr.response?.data;
    if (serverData is Map<String, dynamic> &&
        serverData.containsKey('Errors')) {
      final errors = serverData['Errors'] as List<dynamic>;
      if (errors.isNotEmpty) {
        final firstErr = errors.first as Map<String, dynamic>;
        final msg = firstErr['message'] as String? ?? dioErr.message;
        return Exception(msg);
      }
    }

    return Exception(dioErr.message);
  }
}
