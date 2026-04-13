import 'dart:convert';

import 'package:isar/isar.dart';

import '../../utils/utils.dart';

part 'cache_bom_doc.g.dart';

@collection
class CacheBomDoc {
  Id id = Isar.autoIncrement;

  @Index(
      composite: [CompositeIndex('schemaKey')],
      unique: true,
      caseSensitive: false)
  @Index()
  late String activityFacilityId;
  late String schemaKey;
  String? serverBomId;
  late String dataJson;
  late String tenantId;
  String? facilityId;
  String? assignUserUuid;
  String? bomName;
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
