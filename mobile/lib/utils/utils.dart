import 'dart:io';

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
