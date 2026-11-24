import 'package:isar/isar.dart';

import '../data/nosql/cache_activity_facility_workflow.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/mdms/mdms.dart';
import '../model/required_bom_form_keys/required_bom_form_keys.dart';
import '../repositories/app_init_repo.dart';
import '../repositories/dynamic_form_repo.dart';
import 'app_logger.dart';
import 'envConfig.dart';

class MissingRequiredBomFormKeysMessage {
  const MissingRequiredBomFormKeysMessage({
    required this.title,
    required this.message,
    required this.missingMessages,
  });

  final String title;
  final String message;
  final List<String> missingMessages;
}

Future<MissingRequiredBomFormKeysMessage?> missingRequiredBomFormKeysMessage({
  required Isar isar,
  required String activityFacilityId,
  required String userType,
  required String? systemCode,
}) async {
  final normalizedSystemCode = systemCode?.trim().toUpperCase();
  if (normalizedSystemCode == null || normalizedSystemCode.isEmpty) {
    return null;
  }

  final config = await _requiredBomFormKeysForSystem(normalizedSystemCode);
  if (config == null || !config.active) return null;

  final activeRules = config.rules
      .where((rule) => rule.active && rule.fieldName.trim().isNotEmpty)
      .toList();
  if (activeRules.isEmpty) return null;

  final values = await _bomValuesForProject(
    isar: isar,
    activityFacilityId: activityFacilityId,
    userType: userType,
  );

  final missingMessages = <String>[];
  for (final rule in activeRules) {
    final value = values[rule.fieldName];
    if (_isMissingBomValue(value)) {
      missingMessages.add(rule.message.trim().isNotEmpty
          ? rule.message.trim()
          : 'Please fill ${rule.label}.');
    }
  }

  if (missingMessages.isEmpty) return null;

  return MissingRequiredBomFormKeysMessage(
    title: config.dialogTitle,
    message: config.dialogMessage,
    missingMessages: missingMessages,
  );
}

Future<RequiredBomFormKeysData?> _requiredBomFormKeysForSystem(
  String systemCode,
) async {
  try {
    final tenantId = EnvironmentConfiguration.instance.variables.tenantId;
    final docs = await AppInitRepo().searchRequiredBomFormKeys(
      MdmsRequestModel(
        mdmsCriteria: MdmsCriteriaModel(
          tenantId: tenantId,
          schemaCode: 'common-masters.RequiredBomFormKeys',
          moduleDetails: [],
        ),
      ),
      useCacheRead: true,
    );

    for (final Mdms<RequiredBomFormKeysData> doc in docs) {
      if (!doc.isActive) continue;
      final data = doc.data;
      if (data.systemCode.trim().toUpperCase() == systemCode) {
        return data;
      }
    }
  } catch (e) {
    AppLogger.instance.info('Required BOM form key validation skipped: $e');
  }
  return null;
}

Future<Map<String, dynamic>> _bomValuesForProject({
  required Isar isar,
  required String activityFacilityId,
  required String userType,
}) async {
  final cached = await BomRepository().getProjectBomKV(
    isar: isar,
    activityFacilityId: activityFacilityId,
    userType: userType,
  );
  if (cached != null && cached.isNotEmpty) {
    return Map<String, dynamic>.from(cached);
  }

  try {
    final row = await isar.cacheActivityFacilityWorkflows
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst();
    final modelBom = row?.activityFacility.additionalDetails?.bom;
    if (modelBom != null && modelBom.isNotEmpty) {
      return Map<String, dynamic>.from(modelBom);
    }
  } catch (_) {}

  return <String, dynamic>{};
}

bool _isMissingBomValue(Object? value) {
  if (value == null) return true;
  if (value is String) return value.trim().isEmpty;
  if (value is Iterable) return value.isEmpty;
  if (value is Map) return value.isEmpty;
  return false;
}
