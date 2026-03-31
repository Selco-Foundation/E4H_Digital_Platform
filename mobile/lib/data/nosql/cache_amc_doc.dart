import 'dart:convert';

import 'package:isar/isar.dart';

import '../../utils/utils.dart';

part 'cache_amc_doc.g.dart';

@collection
class CacheAmcDoc {
  Id id = Isar.autoIncrement;

  @Index(
      composite: [CompositeIndex('schemaKey')],
      unique: true,
      caseSensitive: false)
  late String scheduleVisitId;
  late String schemaKey;
  late String dataJson;
  late String tenantId;
  String? facilityId;
  String? assignUserUuid;
  String? formName;
  @Index()
  late DateTime updatedAt;

  @Index()
  bool isDirty = true;

  @ignore
  Map<String, dynamic> get dataMap =>
      jsonDecode(dataJson) as Map<String, dynamic>;

  @ignore
  set dataMap(Map<String, dynamic> v) => dataJson = jsonEncode(jsonSafe(v));
}
