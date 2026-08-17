import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_activity_facility_bom_values.dart';
import '../data/nosql/cache_activity_facility_workflow.dart';
import '../data/nosql/cache_amc_doc.dart';
import '../data/nosql/cache_bom_doc.dart';
import '../data/nosql/cache_schedule_visit_form_values.dart';
import '../data/remote_client.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart' as env;
import '../utils/utils.dart';
import 'activity_facility_repo.dart';

final envConfigs = env.EnvironmentConfiguration.instance;

class BomRepository {
  final Dio _dio = DioClient().dio;

  BomRepository();

  Future<void> saveLocal({
    required Isar isar,
    required String activityFacilityId,
    required String schemaKey,
    required Map<String, dynamic> rawDocWithValues,
    String? facilityId,
    String? assignUserUuid,
    String? bomName,
  }) async {
    await isar.writeTxn(() async {
      final existing = await isar.cacheBomDocs
          .where()
          .filter()
          .activityFacilityIdEqualTo(activityFacilityId)
          .schemaKeyEqualTo(schemaKey, caseSensitive: false)
          .findFirst();

      final now = DateTime.now().toUtc();

      if (existing == null) {
        final doc = CacheBomDoc()
          ..activityFacilityId = activityFacilityId
          ..schemaKey = schemaKey
          ..tenantId = envConfigs.variables.tenantId
          ..facilityId = facilityId
          ..assignUserUuid = assignUserUuid
          ..bomName = bomName ?? schemaKey
          ..dataMap = rawDocWithValues
          ..updatedAt = now
          ..isDirty = true;
        await isar.cacheBomDocs.put(doc);
      } else {
        existing.tenantId = envConfigs.variables.tenantId;
        existing.facilityId = facilityId ?? existing.facilityId;
        existing.assignUserUuid = assignUserUuid ?? existing.assignUserUuid;
        existing.bomName = (bomName ?? existing.bomName ?? schemaKey);
        existing.dataMap = rawDocWithValues;
        existing.updatedAt = now;
        existing.isDirty = true;
        await isar.cacheBomDocs.put(existing);
      }
    });
  }

  Future<CacheBomDoc?> getLocal({
    required Isar isar,
    required String activityFacilityId,
    required String schemaKey,
  }) {
    return isar.cacheBomDocs
        .where()
        .filter()
        .activityFacilityIdEqualTo(activityFacilityId)
        .schemaKeyEqualTo(schemaKey, caseSensitive: false)
        .findFirst();
  }

  Future<List<CacheBomDoc>> getAllForProject(
      Isar isar, String activityFacilityId) {
    return isar.cacheBomDocs
        .where()
        .filter()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();
  }

  Future<void> deleteLocal({
    required Isar isar,
    required String activityFacilityId,
    required String schemaKey,
  }) async {
    final rec = await getLocal(
        isar: isar,
        activityFacilityId: activityFacilityId,
        schemaKey: schemaKey);
    if (rec != null) {
      await isar.writeTxn(() async => isar.cacheBomDocs.delete(rec.id));
    }
  }

  Future<void> deleteAllBomDocs(
      {required Isar isar, required String activityFacilityId}) async {
    await isar.writeTxn(() async {
      final col = isar.cacheBomDocs;
      final rec = await col
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findAll();
      for (final r in rec) {
        await col.delete(r.id);
      }
    });
  }

  Future<void> delete(
      {required Isar isar, required String activityFacilityId}) async {
    await isar.writeTxn(() async {
      final col = isar.cacheActivityFacilityBomValues;
      final rec = await col
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findAll();
      for (final r in rec) {
        await col.delete(r.id);
      }
    });
  }

  Future<void> enrichProjectDocs({
    required Isar isar,
    required String activityFacilityId,
    String? tenantId,
    String? facilityId,
    String? assignUserUuid,
  }) async {
    final docs = await getAllForProject(isar, activityFacilityId);
    if (docs.isEmpty) return;

    await isar.writeTxn(() async {
      for (final d in docs) {
        bool changed = false;
        if (tenantId != null && (d.tenantId.isEmpty)) {
          d.tenantId = tenantId;
          changed = true;
        }
        if (facilityId != null &&
            (d.facilityId == null || d.facilityId!.isEmpty)) {
          d.facilityId = facilityId;
          changed = true;
        }
        if (assignUserUuid != null &&
            (d.assignUserUuid == null || d.assignUserUuid!.isEmpty)) {
          d.assignUserUuid = assignUserUuid;
          changed = true;
        }
        if (changed) {
          d.isDirty = true;
          await isar.cacheBomDocs.put(d);
        }
      }
    });
  }

  Future<void> submitMergedForProject({
    required Isar isar,
    required String activityFacilityId,
    required String tenantId,
    required String facilityId,
    required String assignUserUuid,
    required String userType,
    getSolutionName,
  }) async {
    await enrichProjectDocs(
      isar: isar,
      activityFacilityId: activityFacilityId,
      tenantId: tenantId,
      facilityId: facilityId,
      assignUserUuid: assignUserUuid,
    );

    final allDocs = await getAllForProject(isar, activityFacilityId);
    final dirty = allDocs.where((d) => d.isDirty).toList();
    if (dirty.isEmpty) return;

    var mergedKV = await getProjectBomKV(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );

    if (mergedKV == null) {
      final fallback = <String, dynamic>{};
      for (final d in allDocs) {
        final kv = extractKVFromRawDoc(d.dataMap);
        fallback.addAll(kv);
      }
      mergedKV = fallback;
    }

    final existingId = allDocs
        .firstWhere(
          (d) => (d.serverBomId != null && d.serverBomId!.trim().isNotEmpty),
          orElse: () => CacheBomDoc()..serverBomId = null,
        )
        .serverBomId;

    final isUpdate = existingId != null && existingId.trim().isNotEmpty;

    final solutionName = await ActivityFacilityRepository(isar)
        .getSolutionDesignTypeFromCache(isar, activityFacilityId);

    final apiName = (solutionName != null && solutionName.trim().isNotEmpty)
        ? solutionName.trim()
        : 'BOM.SolarSystem';

    final payload = {
      "bom": [
        {
          if (isUpdate) "id": existingId,
          "tenantId": tenantId,
          "name": apiName,
          "facilityId": facilityId,
          "activityFacilityId": activityFacilityId,
          "assignUser": assignUserUuid,
          "data": mergedKV,
          "isActive": true,
        }
      ],
      "isCascadingProjectDateUpdate": false,
      "apiOperation": isUpdate ? "UPDATE" : "CREATE",
    };

    final path =
        isUpdate ? 'activity/v1/bom/_update' : 'activity/v1/bom/_create';
    final response = await _dio.post(path, data: payload);
    final returnedId = _extractBomId(response.data);
    final finalId = isUpdate ? existingId : (returnedId ?? '').trim();

    await isar.writeTxn(() async {
      for (final d in allDocs) {
        if (finalId.isNotEmpty) {
          d.serverBomId = finalId;
        }
        if (d.isDirty) d.isDirty = false;
        await isar.cacheBomDocs.put(d);
      }
    });
  }

  String? _extractBomId(dynamic data) {
    if (data is Map) {
      final bom = data["bom"] ?? data["BOM"] ?? data["Bom"];
      if (bom is List && bom.isNotEmpty) {
        final first = bom.first;
        if (first is Map) {
          final id = first["id"] ?? first["Id"] ?? first["ID"];
          if (id is String && id.trim().isNotEmpty) return id.trim();
        }
      }

      final direct = data["id"] ?? data["Id"] ?? data["ID"];
      if (direct is String && direct.trim().isNotEmpty) return direct.trim();

      for (final v in data.values) {
        final got = _extractBomId(v);
        if (got != null && got.trim().isNotEmpty) return got.trim();
      }
      return null;
    }

    if (data is List && data.isNotEmpty) {
      return _extractBomId(data.first);
    }

    if (data is String && data.trim().isNotEmpty) {
      try {
        return _extractBomId(jsonDecode(data));
      } catch (_) {
        return null;
      }
    }

    return null;
  }

  Future<Map<String, dynamic>> searchBom({
    required List<String> activityFacilityIds,
    int offset = 0,
    int limit = 100,
  }) async {
    final tenantId = envConfigs.variables.tenantId;
    final path =
        "/activity/v1/bom/_search?tenantId=$tenantId&offset=$offset&limit=$limit";

    Response response;
    final body = {
      "bom": {
        "activityFacilityIds": activityFacilityIds,
        "tenantId": tenantId,
      }
    };

    try {
      response = await _dio.post(path, data: body);
      return response.data as Map<String, dynamic>;
    } catch (err) {
      rethrow;
    }
  }

  Future<({bool savedBomValues})> syncBomForActivityFacility(
      {required String activityFacilityId,
      required String facilityId,
      required String userType,
      required Isar isar}) async {
    try {
      if (facilityId.isEmpty) {
        return (savedBomValues: false);
      }

      final res = await searchBom(activityFacilityIds: [activityFacilityId]);
      final boms = (res['bom'] as List?) ?? const [];
      if (boms.isEmpty) {
        return (savedBomValues: false);
      }

      final bom = (boms.first as Map<String, dynamic>);

      bool savedValues = false;
      final data = (bom['data'] as Map<String, dynamic>?);
      if (data != null) {
        final entryKey = '$activityFacilityId::$userType';
        await isar.writeTxn(() async {
          await isar.cacheActivityFacilityBomValues.put(
            CacheActivityFacilityBomValues()
              ..activityFacilityId = activityFacilityId
              ..userType = userType
              ..entryKey = entryKey
              ..dataJson = jsonEncode(data)
              ..updatedAt = DateTime.now(),
          );
        });
        savedValues = true;
      }
      return (savedBomValues: savedValues);
    } catch (e, stack) {
      AppLogger.instance.error(
          title: "syncBomForProject ERROR:",
          message: e.toString(),
          stackTrace: stack);
      throw Exception("Error syncing bom");
    }
  }

  Future<String?> resolveBomUserType({
    required Isar isar,
    required String activityFacilityId,
    required userType,
  }) async {
    final entryKey = '$activityFacilityId::$userType';
    final rec = await isar.cacheActivityFacilityBomValues
        .where()
        .entryKeyEqualTo(entryKey)
        .findFirst();

    if (rec != null) return userType;
    final fallbackBomData = await _getModelBomValues(
      isar: isar,
      activityFacilityId: activityFacilityId,
    );
    if (fallbackBomData.isNotEmpty) return userType;
    return null;
  }

  Future<void> mergeKvForEntryKey({
    required Isar isar,
    required String projectId,
    required String userType,
    required Map<String, dynamic> kvUpdate,
  }) async {
    final entryKey = '$projectId::$userType';
    final safeIncoming = jsonSafe(kvUpdate) as Map<String, dynamic>;

    await isar.writeTxn(() async {
      final existing = await isar.cacheActivityFacilityBomValues
          .where()
          .entryKeyEqualTo(entryKey)
          .findFirst();

      final existingMap = (existing?.dataJson.isNotEmpty ?? false)
          ? (json.decode(existing!.dataJson) as Map<String, dynamic>)
          : <String, dynamic>{};

      final merged = deepMerge(existingMap, safeIncoming);
      final jsonString = json.encode(jsonSafe(merged));

      final rec = existing ?? CacheActivityFacilityBomValues()
        ..activityFacilityId = projectId
        ..userType = userType
        ..entryKey = entryKey;

      rec
        ..dataJson = jsonString
        ..updatedAt = DateTime.now();

      await isar.cacheActivityFacilityBomValues.put(rec);
    });
  }

  Future<bool> hasBomForSchema({
    required Isar isar,
    required String projectId,
    required String schemaKey,
  }) async {
    final existing = await isar.cacheBomDocs
        .where()
        .activityFacilityIdEqualToAnySchemaKey(projectId)
        .filter()
        .schemaKeyEqualTo(schemaKey)
        .findFirst();
    return existing != null;
  }

  Future<String> resolveBomActionLabel({
    required Isar isar,
    required String projectId,
    required String schemaKey,
    required FormOrigin origin,
    required bool isSystemParameters,
  }) async {
    if (origin == FormOrigin.inboxSummary || origin == FormOrigin.submitted) {
      return 'View';
    }
    final exists = await hasBomForSchema(
      isar: isar,
      projectId: projectId,
      schemaKey: schemaKey,
    );

    return exists
        ? 'Edit'
        : origin == FormOrigin.overallSummary && isSystemParameters
            ? 'Fill'
            : 'View/Edit';
  }

  Future<Map<String, dynamic>?> getProjectBomKV({
    required Isar isar,
    required String activityFacilityId,
    required String userType,
  }) async {
    final entryKey = '$activityFacilityId::$userType';
    final rec = await isar.cacheActivityFacilityBomValues
        .where()
        .entryKeyEqualTo(entryKey)
        .findFirst();
    if (rec == null || rec.dataJson.isEmpty) return null;
    try {
      return jsonDecode(rec.dataJson) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  Future<bool> hasLocalBomForSchema({
    required Isar isar,
    required String activityFacilityId,
    required String schemaKey,
  }) async {
    final rec = await getLocal(
      isar: isar,
      activityFacilityId: activityFacilityId,
      schemaKey: schemaKey,
    );
    return rec != null;
  }

  Future<bool> _hasAnyLocalBomDocs({
    required Isar isar,
    required String activityFacilityId,
  }) async {
    final docs = await getAllForProject(isar, activityFacilityId);
    return docs.isNotEmpty;
  }

  Future<bool> _hasAnyServerBackedBomDoc({
    required Isar isar,
    required String activityFacilityId,
  }) async {
    final docs = await getAllForProject(isar, activityFacilityId);
    return docs.any(
      (d) => (d.serverBomId ?? '').trim().isNotEmpty,
    );
  }

  Future<Map<String, dynamic>> _getModelBomValues({
    required Isar isar,
    required String activityFacilityId,
  }) async {
    Map<String, dynamic> modelBom = <String, dynamic>{};
    try {
      final row = await isar.cacheActivityFacilityWorkflows
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .findFirst();
      final fromModel = row?.activityFacility.additionalDetails?.bom;
      if (fromModel != null) {
        modelBom = Map<String, dynamic>.from(fromModel);
      }
    } catch (_) {}

    return modelBom;
  }

  bool _sameKvMap(
    Map<String, dynamic> left,
    Map<String, dynamic> right,
  ) {
    return json.encode(jsonSafe(left)) == json.encode(jsonSafe(right));
  }

  Future<Map<String, dynamic>> getOrCreateInitialFormValuesForSchema({
    required Isar isar,
    required String activityFacilityId,
    required String userType,
  }) async {
    final modelBom = await _getModelBomValues(
      isar: isar,
      activityFacilityId: activityFacilityId,
    );

    final globalKv = await getProjectBomKV(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );

    final resolved = globalKv == null || globalKv.isEmpty
        ? Map<String, dynamic>.from(modelBom)
        : deepMerge(
            Map<String, dynamic>.from(modelBom),
            Map<String, dynamic>.from(globalKv),
          );

    if (resolved.isNotEmpty &&
        (globalKv == null || !_sameKvMap(globalKv, resolved))) {
      await mergeKvForEntryKey(
        isar: isar,
        projectId: activityFacilityId,
        userType: userType,
        kvUpdate: resolved,
      );
    }

    return resolved;
  }

  Future<Map<String, dynamic>> getInitialFormValuesForSchema({
    required Isar isar,
    required String activityFacilityId,
    required String userType,
    required String schemaKey,
  }) async {
    final globalKv = await getProjectBomKV(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );

    if (globalKv != null && globalKv.isNotEmpty) {
      final hasLocalForSchema = await hasLocalBomForSchema(
        isar: isar,
        activityFacilityId: activityFacilityId,
        schemaKey: schemaKey,
      );

      if (hasLocalForSchema) {
        return Map<String, dynamic>.from(globalKv);
      }

      final hasServerBackedBom = await _hasAnyServerBackedBomDoc(
        isar: isar,
        activityFacilityId: activityFacilityId,
      );
      if (hasServerBackedBom) {
        return Map<String, dynamic>.from(globalKv);
      }

      final hasAnyLocalDocs = await _hasAnyLocalBomDocs(
        isar: isar,
        activityFacilityId: activityFacilityId,
      );
      if (!hasAnyLocalDocs) {
        return Map<String, dynamic>.from(globalKv);
      }
    }

    return _getModelBomValues(
      isar: isar,
      activityFacilityId: activityFacilityId,
    );
  }

  Future<Map<String, dynamic>> getInitialFormValues({
    required Isar isar,
    required String activityFacilityId,
    required String userType,
  }) async {
    final backendOrLocal = await getProjectBomKV(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );
    if (backendOrLocal != null && backendOrLocal.isNotEmpty) {
      return Map<String, dynamic>.from(backendOrLocal);
    }

    return _getModelBomValues(
      isar: isar,
      activityFacilityId: activityFacilityId,
    );
  }

  Stream<void> watchBomForSchema({
    required Isar isar,
    required String projectId,
    required String schemaKey,
  }) {
    final q = isar.cacheBomDocs
        .where()
        .activityFacilityIdEqualToAnySchemaKey(projectId)
        .filter()
        .schemaKeyEqualTo(schemaKey);
    return q.watchLazy(fireImmediately: false);
  }

  Stream<void> watchBomForSchemas({
    required Isar isar,
    required String projectId,
    required List<String> schemaKeys,
  }) {
    final controller = StreamController<void>.broadcast();
    final subs = <StreamSubscription<void>>[];

    void addSub(String key) {
      final sub = watchBomForSchema(
        isar: isar,
        projectId: projectId,
        schemaKey: key,
      ).listen((_) {
        if (!controller.isClosed) controller.add(null);
      });
      subs.add(sub);
    }

    for (final key in schemaKeys.toSet()) {
      addSub(key);
    }

    controller.onCancel = () {
      for (final s in subs) {
        s.cancel();
      }
    };
    return controller.stream;
  }
}

class AmcDynamicFormRepository {
  AmcDynamicFormRepository();

  Future<void> saveLocal({
    required Isar isar,
    required String scheduledVisitId,
    required String schemaKey,
    String? facilityId,
    String? assignUserUuid,
    required Map<String, dynamic> rawDocWithValues,
    String? formName,
  }) async {
    await isar.writeTxn(() async {
      final existing = await isar.cacheAmcDocs
          .where()
          .filter()
          .scheduleVisitIdEqualTo(scheduledVisitId)
          .schemaKeyEqualTo(schemaKey, caseSensitive: false)
          .findFirst();

      final now = DateTime.now().toUtc();

      if (existing == null) {
        final doc = CacheAmcDoc()
          ..scheduleVisitId = scheduledVisitId
          ..schemaKey = schemaKey
          ..tenantId = envConfigs.variables.tenantId
          ..facilityId = facilityId
          ..assignUserUuid = assignUserUuid
          ..formName = formName ?? schemaKey
          ..dataMap = rawDocWithValues
          ..updatedAt = now
          ..isDirty = true;
        await isar.cacheAmcDocs.put(doc);
      } else {
        existing.tenantId = envConfigs.variables.tenantId;
        existing.facilityId = facilityId ?? existing.facilityId;
        existing.assignUserUuid = assignUserUuid ?? existing.assignUserUuid;
        existing.formName = (formName ?? existing.formName ?? schemaKey);
        existing.dataMap = rawDocWithValues;
        existing.updatedAt = now;
        existing.isDirty = true;
        await isar.cacheAmcDocs.put(existing);
      }
    });
  }

  Future<CacheAmcDoc?> getLocal({
    required Isar isar,
    required String scheduledVisitId,
    required String schemaKey,
  }) {
    return isar.cacheAmcDocs
        .where()
        .filter()
        .scheduleVisitIdEqualTo(scheduledVisitId)
        .schemaKeyEqualTo(schemaKey, caseSensitive: false)
        .findFirst();
  }

  Future<List<CacheAmcDoc>> getAllForScheduledVisit(
      Isar isar, String scheduledVisitId) {
    return isar.cacheAmcDocs
        .where()
        .filter()
        .scheduleVisitIdEqualTo(scheduledVisitId)
        .findAll();
  }

  Future<void> deleteAllLocal(
      {required Isar isar, required String scheduledVisitId}) async {
    await isar.writeTxn(() async {
      final col = isar.cacheAmcDocs;
      final rec = await col
          .where()
          .scheduleVisitIdEqualToAnySchemaKey(scheduledVisitId)
          .findAll();
      for (final r in rec) {
        await col.delete(r.id);
      }
    });
  }

  Future<void> delete(
      {required Isar isar, required String scheduledVisitId}) async {
    await isar.writeTxn(() async {
      final col = isar.cacheScheduleVisitFormValues;
      final rec =
          await col.where().scheduledVisitIdEqualTo(scheduledVisitId).findAll();
      for (final r in rec) {
        await col.delete(r.id);
      }
    });
  }

  Future<void> enrichScheduledVisitDocs({
    required Isar isar,
    required String scheduledVisitId,
    String? tenantId,
    String? facilityId,
    String? assignUserUuid,
  }) async {
    final docs = await getAllForScheduledVisit(isar, scheduledVisitId);
    if (docs.isEmpty) return;

    await isar.writeTxn(() async {
      for (final d in docs) {
        bool changed = false;
        if (tenantId != null && (d.tenantId.isEmpty)) {
          d.tenantId = tenantId;
          changed = true;
        }
        if (facilityId != null &&
            (d.facilityId == null || d.facilityId!.isEmpty)) {
          d.facilityId = facilityId;
          changed = true;
        }
        if (assignUserUuid != null &&
            (d.assignUserUuid == null || d.assignUserUuid!.isEmpty)) {
          d.assignUserUuid = assignUserUuid;
          changed = true;
        }
        if (changed) {
          d.isDirty = true;
          await isar.cacheAmcDocs.put(d);
        }
      }
    });
  }

  Future<void> mergeKvForEntryKey({
    required Isar isar,
    required String scheduledVisitId,
    required String userType,
    required Map<String, dynamic> kvUpdate,
  }) async {
    final entryKey = '$scheduledVisitId::$userType';
    final safeIncoming = jsonSafe(kvUpdate) as Map<String, dynamic>;

    await isar.writeTxn(() async {
      final existing = await isar.cacheScheduleVisitFormValues
          .where()
          .entryKeyEqualTo(entryKey)
          .findFirst();

      final existingMap = (existing?.dataJson.isNotEmpty ?? false)
          ? (json.decode(existing!.dataJson) as Map<String, dynamic>)
          : <String, dynamic>{};

      final merged = deepMerge(existingMap, safeIncoming);
      final jsonString = json.encode(jsonSafe(merged));

      final rec = existing ?? CacheScheduleVisitFormValues()
        ..scheduledVisitId = scheduledVisitId
        ..userType = userType
        ..entryKey = entryKey;

      rec
        ..dataJson = jsonString
        ..updatedAt = DateTime.now();

      await isar.cacheScheduleVisitFormValues.put(rec);
    });
  }

  Future<bool> hasFormForSchema({
    required Isar isar,
    required String scheduledVisitId,
    required String schemaKey,
  }) async {
    final existing = await isar.cacheAmcDocs
        .where()
        .scheduleVisitIdEqualToAnySchemaKey(scheduledVisitId)
        .filter()
        .schemaKeyEqualTo(schemaKey)
        .findFirst();
    return existing != null;
  }

  Future<String> resolveFormActionLabel({
    required Isar isar,
    required String scheduledVisitId,
    required String schemaKey,
    required FormOrigin origin,
  }) async {
    if (origin == FormOrigin.inboxSummary || origin == FormOrigin.submitted) {
      return 'Start';
    }
    final exists = await hasFormForSchema(
      isar: isar,
      scheduledVisitId: scheduledVisitId,
      schemaKey: schemaKey,
    );

    return exists ? 'Resume' : 'Start';
  }

  Future<Map<String, dynamic>?> getScheduledVisitFormKV({
    required Isar isar,
    required String scheduledVisitId,
    required String userType,
  }) async {
    final entryKey = '$scheduledVisitId::$userType';
    final rec = await isar.cacheScheduleVisitFormValues
        .where()
        .entryKeyEqualTo(entryKey)
        .findFirst();
    if (rec == null || rec.dataJson.isEmpty) return null;
    try {
      return jsonDecode(rec.dataJson) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  Stream<void> watchFormForSchema({
    required Isar isar,
    required String scheduledVisitId,
    required String schemaKey,
  }) {
    final q = isar.cacheAmcDocs
        .where()
        .scheduleVisitIdEqualToAnySchemaKey(scheduledVisitId)
        .filter()
        .schemaKeyEqualTo(schemaKey);
    return q.watchLazy(fireImmediately: false);
  }

  Stream<void> watchFormForSchemas({
    required Isar isar,
    required String scheduledVisitId,
    required List<String> schemaKeys,
  }) {
    final controller = StreamController<void>.broadcast();
    final subs = <StreamSubscription<void>>[];

    void addSub(String key) {
      final sub = watchFormForSchema(
        isar: isar,
        scheduledVisitId: scheduledVisitId,
        schemaKey: key,
      ).listen((_) {
        if (!controller.isClosed) controller.add(null);
      });
      subs.add(sub);
    }

    for (final key in schemaKeys.toSet()) {
      addSub(key);
    }

    controller.onCancel = () {
      for (final s in subs) {
        s.cancel();
      }
    };
    return controller.stream;
  }

  Future<Map<String, dynamic>?> getInitialFormValues({
    required Isar isar,
    required String scheduledVisitId,
    required Map<String, dynamic>? responsesFromModel,
    required String userType,
  }) async {
    final local = await getScheduledVisitFormKV(
        isar: isar, scheduledVisitId: scheduledVisitId, userType: userType);
    if (local != null) return local;
    return responsesFromModel;
  }
}
