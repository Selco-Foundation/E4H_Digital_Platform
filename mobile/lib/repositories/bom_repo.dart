// import 'package:dio/dio.dart';
// import 'package:isar/isar.dart';
//
// import '../data/nosql/cache_bom_doc.dart';
// import '../data/remote_client.dart';
// import '../utils/envConfig.dart' as env;
// import 'project_repo.dart';
//
// final envConfigs = env.EnvironmentConfiguration.instance;
//
// class BomRepository {
//   final Dio _dio = DioClient().dio;
//
//   BomRepository();
//
//   // -------- Local (Isar) ----------
//   Future<void> saveLocal({
//     required Isar isar,
//     required String projectId,
//     required String schemaKey,
//     required Map<String, dynamic> rawDocWithValues,
//     String? facilityId,
//     String? assignUserUuid,
//     String? bomName,
//   }) async {
//     await isar.writeTxn(() async {
//       final existing = await isar.cacheBomDocs
//           .where()
//           .filter()
//           .projectIdEqualTo(projectId)
//           .schemaKeyEqualTo(schemaKey, caseSensitive: false)
//           .findFirst();
//
//       final now = DateTime.now().toUtc();
//
//       if (existing == null) {
//         final doc = CacheBomDoc()
//           ..projectId = projectId
//           ..schemaKey = schemaKey
//           ..tenantId = envConfigs.variables.tenantId
//           ..facilityId = facilityId
//           ..assignUserUuid = assignUserUuid
//           ..bomName = bomName ?? schemaKey
//           ..dataMap = rawDocWithValues
//           ..updatedAt = now
//           ..isDirty = true;
//         await isar.cacheBomDocs.put(doc);
//       } else {
//         existing.tenantId = envConfigs.variables.tenantId;
//         existing.facilityId = facilityId ?? existing.facilityId;
//         existing.assignUserUuid = assignUserUuid ?? existing.assignUserUuid;
//         existing.bomName = (bomName ?? existing.bomName ?? schemaKey);
//         existing.dataMap = rawDocWithValues;
//         existing.updatedAt = now;
//         existing.isDirty = true;
//         await isar.cacheBomDocs.put(existing);
//       }
//     });
//   }
//
//   Future<CacheBomDoc?> getLocal({
//     required Isar isar,
//     required String projectId,
//     required String schemaKey,
//   }) {
//     return isar.cacheBomDocs
//         .where()
//         .filter()
//         .projectIdEqualTo(projectId)
//         .schemaKeyEqualTo(schemaKey, caseSensitive: false)
//         .findFirst();
//   }
//
//   Future<List<CacheBomDoc>> getAllForProject(Isar isar, String projectId) {
//     return isar.cacheBomDocs
//         .where()
//         .filter()
//         .projectIdEqualTo(projectId)
//         .findAll();
//   }
//
//   Future<void> deleteLocal({
//     required Isar isar,
//     required String projectId,
//     required String schemaKey,
//   }) async {
//     final rec =
//         await getLocal(isar: isar, projectId: projectId, schemaKey: schemaKey);
//     if (rec != null) {
//       await isar.writeTxn(() async => isar.cacheBomDocs.delete(rec.id));
//     }
//   }
//
//   Future<void> enrichProjectDocs({
//     required Isar isar,
//     required String projectId,
//     String? tenantId,
//     String? facilityId,
//     String? assignUserUuid,
//   }) async {
//     final docs = await getAllForProject(isar, projectId);
//     print("projectId from docs $projectId");
//     print("docs $docs");
//     if (docs.isEmpty) return;
//
//     await isar.writeTxn(() async {
//       for (final d in docs) {
//         bool changed = false;
//         if (tenantId != null && (d.tenantId.isEmpty)) {
//           d.tenantId = tenantId;
//           changed = true;
//         }
//         if (facilityId != null &&
//             (d.facilityId == null || d.facilityId!.isEmpty)) {
//           d.facilityId = facilityId;
//           changed = true;
//         }
//         if (assignUserUuid != null &&
//             (d.assignUserUuid == null || d.assignUserUuid!.isEmpty)) {
//           d.assignUserUuid = assignUserUuid;
//           changed = true;
//         }
//         if (changed) {
//           d.isDirty = true; // ensure latest enrichment goes out
//           await isar.cacheBomDocs.put(d);
//         }
//       }
//     });
//   }
//
//   // -------- Remote (API) ----------
//   /// Splits dirty records into create vs update and pushes both in bulk
//   // Future<void> submitAllDirtyForProject({
//   //   required Isar isar,
//   //   required String projectId,
//   //   required String tenantId,
//   //   required String facilityId,
//   //   required String assignUserUuid,
//   // }) async {
//   //   print(
//   //       '[BOM:submit 2] isarInstance=${identityHashCode(isar)} project=$projectId');
//   //   // 1) Enrich once here
//   //   await enrichProjectDocs(
//   //     isar: isar,
//   //     projectId: projectId,
//   //     tenantId: tenantId,
//   //     facilityId: facilityId,
//   //     assignUserUuid: assignUserUuid,
//   //   );
//   //
//   //   // 2) Proceed with current dirty split & bulk submit
//   //   final dirty = (await getAllForProject(isar, projectId))
//   //       .where((d) => d.isDirty)
//   //       .toList();
//   //   if (dirty.isEmpty) return;
//   //
//   //   final creates = <CacheBomDoc>[];
//   //   final updates = <CacheBomDoc>[];
//   //
//   //   for (final d in dirty) {
//   //     if (d.serverBomId == null || d.serverBomId!.isEmpty) {
//   //       creates.add(d);
//   //     } else {
//   //       updates.add(d);
//   //     }
//   //   }
//   //
//   //   if (creates.isNotEmpty) {
//   //     final body = await _buildRequestBody(creates);
//   //     print('Request payload create: ${jsonEncode(body)}');
//   //     await _dio.post('activity/v1/bom/_create', data: body);
//   //     await _markClean(isar, creates);
//   //   }
//   //
//   //   if (updates.isNotEmpty) {
//   //     final body = await _buildRequestBody(updates, isUpdate: true);
//   //     print('Request payload update: ${jsonEncode(body)}');
//   //     await _dio.post('activity/v1/bom/_update', data: body);
//   //     await _markClean(isar, updates);
//   //   }
//   // }
//
//   Future<Map<String, dynamic>> _buildRequestBody(List<CacheBomDoc> docs,
//       {bool isUpdate = false}) async {
//     final bomArray = docs.map<Map<String, dynamic>>((d) {
//       final payload = <String, dynamic>{
//         "tenantId": d.tenantId,
//         "name": d.bomName ?? d.schemaKey,
//         if (d.facilityId?.isNotEmpty == true) "facilityId": d.facilityId,
//         if (d.assignUserUuid?.isNotEmpty == true)
//           "assignUser": d.assignUserUuid,
//         "data": d.dataMap, // RAW MDMS doc exactly as in Postman
//         "isActive": true,
//       };
//       if (isUpdate && d.serverBomId != null && d.serverBomId!.isNotEmpty) {
//         payload["id"] = d.serverBomId;
//       }
//       return payload;
//     }).toList();
//
//     return {
//       "bom": bomArray,
//       "isCascadingProjectDateUpdate": false,
//       "apiOperation": isUpdate ? "UPDATE" : "CREATE"
//     };
//   }
//
//   Future<void> _markClean(Isar isar, List<CacheBomDoc> docs) async {
//     await isar.writeTxn(() async {
//       for (final d in docs) {
//         d.isDirty = false;
//         await isar.cacheBomDocs.put(d);
//       }
//     });
//   }
//
//   // -------- Remote (API) ----------
//   Future<void> submitMergedForProject({
//     required Isar isar,
//     required String projectId,
//     required String tenantId,
//     required String facilityId,
//     required String assignUserUuid,
//   }) async {
//     // Enrich once
//     await enrichProjectDocs(
//       isar: isar,
//       projectId: projectId,
//       tenantId: tenantId,
//       facilityId: facilityId,
//       assignUserUuid: assignUserUuid,
//     );
//
//     // Dirty docs
//     final dirty = (await getAllForProject(isar, projectId))
//         .where((d) => d.isDirty)
//         .toList();
//
//     print("dirty $dirty");
//
//     if (dirty.isEmpty) return;
//
//     // Merge all fieldName/value pairs
//     final mergedKV = <String, dynamic>{};
//     for (final d in dirty) {
//       mergedKV.addAll(_extractKVFromRawDoc(d.dataMap));
//     }
//
//     print(" mergedKV $mergedKV");
//
//     if (mergedKV.isEmpty) {
//       // Fallback: try to send the inner data node as-is (server now wants merged;
//       // but empty is worse — this gives you something useful immediately).
//       // We pick the first dirty doc as the source.
//       final Map dataNode = (() {
//         final dd = dirty.first.dataMap;
//         if (dd['data'] is Map) {
//           final Map inner = dd['data'];
//           if (inner['data'] is Map) return inner['data'];
//           return inner;
//         }
//         return dd;
//       })();
//       // only copy flat keys (avoid re-sending 'pages' bulk)
//       dataNode.forEach((k, v) {
//         if (v is! Map && v is! List) mergedKV[k.toString()] = v;
//       });
//
//       // still empty? as last resort, keep entire first doc's 'data' block
//       if (mergedKV.isEmpty && dataNode.isNotEmpty) {
//         // This line intentionally sends the whole dataNode;
//         // remove it if your server must strictly have flattened KV.
//         mergedKV['__raw_fallback__'] = dataNode;
//       }
//     }
//
//     // Decide UPDATE vs CREATE (first id wins)
//     final firstId = dirty
//         .firstWhere(
//           (d) => (d.serverBomId != null && d.serverBomId!.isNotEmpty),
//           orElse: () => CacheBomDoc()..serverBomId = null,
//         )
//         .serverBomId;
//
//     print("firstId $firstId");
//     final isUpdate = firstId != null && firstId.isNotEmpty;
//     print("isUpdate $isUpdate");
//     final solutionName = await ProjectRepository(isar)
//         .getSolutionDesignTypeFromCache(isar, projectId);
//     print("solutionName $solutionName");
//
//     final apiName = solutionName?.trim().isNotEmpty == true
//         ? solutionName!.trim()
//         : 'BOM.SolarSystem';
//
//     print("apiName $apiName");
//
//     final payload = {
//       "bom": [
//         {
//           if (isUpdate) "id": firstId,
//           "tenantId": tenantId,
//           "name": apiName, // <-- use systemCode here
//           "facilityId": facilityId,
//           "assignUser": assignUserUuid,
//           "data": mergedKV,
//           "isActive": true,
//         }
//       ],
//       "isCascadingProjectDateUpdate": false,
//       "apiOperation": isUpdate ? "UPDATE" : "CREATE",
//     };
//
//     print("payload $payload");
//
//     final path =
//         isUpdate ? 'activity/v1/bom/_update' : 'activity/v1/bom/_create';
//     await _dio.post(path, data: payload);
//
//     await isar.writeTxn(() async {
//       for (final d in dirty) {
//         if (isUpdate) d.serverBomId = firstId;
//         d.isDirty = false;
//         await isar.cacheBomDocs.put(d);
//       }
//     });
//   }
//
//   /// Flatten RAW BOM -> { fieldName: value } no matter how it was saved.
//   /// Handles all of these:
//   ///  A) { data: { pages: [ { properties: [ { fieldName, value } ] } ] } }    // classic MDMS
//   ///  B) { pages: [ ... ] }                                                    // inner MDMS saved
//   ///  C) { page: "...", properties: [...] }                                    // single page
//   ///  D) properties as Map keyed by fieldName                                  // transformed
//   ///  E) double 'data' nesting: { data: { data: { pages:[...] } } }            // seen in some flows
//   ///  F) already-merged KV: { data: { "field": "...", ... } }                  // just use it
//   Map<String, dynamic> _extractKVFromRawDoc(Map<String, dynamic> raw) {
//     final kv = <String, dynamic>{};
//
//     // --- helpers --------------------------------------------------------------
//
//     // add a single property object
//     void addProp(dynamic prop) {
//       if (prop is! Map) return;
//       final field = (prop['fieldName'] ?? prop['key'] ?? prop['name']);
//       if (field is! String || field.isEmpty) return;
//
//       // prefer 'value' if present; otherwise try a few common fallbacks
//       dynamic v;
//       if (prop.containsKey('value')) {
//         v = prop['value'];
//       } else if (prop.containsKey('defaultValue')) {
//         v = prop['defaultValue'];
//       } else if (prop.containsKey('initialValue')) {
//         v = prop['initialValue'];
//       } else {
//         // last resort: null (keeps the key so server receives the field)
//         v = null;
//       }
//       kv[field] = v;
//     }
//
//     // walk a "properties" node which might be List or Map
//     void walkProps(dynamic props) {
//       if (props is List) {
//         for (final it in props) addProp(it);
//       } else if (props is Map) {
//         for (final it in props.values) addProp(it);
//       }
//     }
//
//     // Given a node, try to harvest pages/properties
//     void harvestFromNode(Map node) {
//       // pages as list
//       final pages = node['pages'];
//       if (pages is List) {
//         for (final p in pages) {
//           if (p is! Map) continue;
//           walkProps(p['properties']);
//         }
//         return;
//       }
//
//       // single page with properties or transformed
//       if (node.containsKey('properties')) {
//         walkProps(node['properties']);
//         return;
//       }
//
//       // already-merged KV? Take all non-collection leaves
//       // heuristic: if there is no 'pages' or 'properties' but there ARE flat scalars,
//       // then treat it as KV.
//       bool hasFlat = false;
//       node.forEach((k, v) {
//         if (v is! Map && v is! List) hasFlat = true;
//       });
//       if (hasFlat) {
//         node.forEach((k, v) {
//           if (v is! Map && v is! List) kv[k.toString()] = v;
//         });
//       }
//     }
//
//     // normalize root:
//     // some flows saved the outer envelope: { id, tenantId, ..., data: {...} }
//     // and some have double 'data': { data: { data: {...} } }
//     Map top = raw;
//     if (top['data'] is Map) {
//       top = top['data'] as Map;
//       if (top['data'] is Map) {
//         top = top['data'] as Map; // handle double data nesting
//       }
//     }
//
//     harvestFromNode(top);
//
//     return kv;
//   }
// }

import 'package:dio/dio.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_bom_doc.dart';
import '../data/remote_client.dart';
import '../utils/envConfig.dart' as env;
import 'project_repo.dart';

final envConfigs = env.EnvironmentConfiguration.instance;

class BomRepository {
  final Dio _dio = DioClient().dio;

  BomRepository();

  // -------- Local (Isar) ----------
  Future<void> saveLocal({
    required Isar isar,
    required String projectId,
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
          .projectIdEqualTo(projectId)
          .schemaKeyEqualTo(schemaKey, caseSensitive: false)
          .findFirst();

      final now = DateTime.now().toUtc();

      if (existing == null) {
        final doc = CacheBomDoc()
          ..projectId = projectId
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
    required String projectId,
    required String schemaKey,
  }) {
    return isar.cacheBomDocs
        .where()
        .filter()
        .projectIdEqualTo(projectId)
        .schemaKeyEqualTo(schemaKey, caseSensitive: false)
        .findFirst();
  }

  Future<List<CacheBomDoc>> getAllForProject(Isar isar, String projectId) {
    return isar.cacheBomDocs
        .where()
        .filter()
        .projectIdEqualTo(projectId)
        .findAll();
  }

  Future<void> deleteLocal({
    required Isar isar,
    required String projectId,
    required String schemaKey,
  }) async {
    final rec =
        await getLocal(isar: isar, projectId: projectId, schemaKey: schemaKey);
    if (rec != null) {
      await isar.writeTxn(() async => isar.cacheBomDocs.delete(rec.id));
    }
  }

  Future<void> enrichProjectDocs({
    required Isar isar,
    required String projectId,
    String? tenantId,
    String? facilityId,
    String? assignUserUuid,
  }) async {
    final docs = await getAllForProject(isar, projectId);
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
          d.isDirty = true; // ensure latest enrichment goes out
          await isar.cacheBomDocs.put(d);
        }
      }
    });
  }

  // -------- Deep extractor (NEW) ----------
  /// Deep, shape-agnostic flattener:
  /// collects every `{ fieldName: <string>, value: <any> }` anywhere in the tree.
  Map<String, dynamic> _extractKVFromRawDoc(Map<String, dynamic> raw) {
    final acc = <String, dynamic>{};

    // Handle {data:{...}} and {data:{data:{...}}}
    dynamic root = raw;
    if (root is Map && root['data'] is Map) {
      root = root['data'];
      if (root is Map && root['data'] is Map) {
        root = root['data'];
      }
    }

    void visit(dynamic node) {
      if (node is Map) {
        final fn = node['fieldName'];
        if (fn is String && fn.isNotEmpty && node.containsKey('value')) {
          acc[fn] = node['value'];
        }
        for (final v in node.values) {
          visit(v);
        }
      } else if (node is List) {
        for (final v in node) visit(v);
      }
    }

    visit(root);
    return acc;
  }

  // -------- Remote (API): merged create/update (NEW) ----------
  /// Merge all dirty docs into one payload; use systemCode as "name".
  Future<void> submitMergedForProject({
    required Isar isar,
    required String projectId,
    required String tenantId,
    required String facilityId,
    required String assignUserUuid,
    getSolutionName, // inject resolver
  }) async {
    // 1) Enrich once
    await enrichProjectDocs(
      isar: isar,
      projectId: projectId,
      tenantId: tenantId,
      facilityId: facilityId,
      assignUserUuid: assignUserUuid,
    );

    // 2) Dirty docs
    final dirty = (await getAllForProject(isar, projectId))
        .where((d) => d.isDirty)
        .toList();

    if (dirty.isEmpty) return;

    // 3) Merge all fieldName/value pairs
    final mergedKV = <String, dynamic>{};
    for (final d in dirty) {
      final kv = _extractKVFromRawDoc(d.dataMap);
      mergedKV.addAll(kv);
    }

    // 4) Decide UPDATE vs CREATE
    final firstId = dirty
        .firstWhere(
          (d) => (d.serverBomId != null && d.serverBomId!.isNotEmpty),
          orElse: () => CacheBomDoc()..serverBomId = null,
        )
        .serverBomId;
    final isUpdate = firstId != null && firstId.isNotEmpty;

    // 5) Resolve API "name" from project.systemCode (solutionDesignType)
    final solutionName = await ProjectRepository(isar)
        .getSolutionDesignTypeFromCache(isar, projectId);
    print("solutionName $solutionName");
    final apiName = (solutionName != null && solutionName.trim().isNotEmpty)
        ? solutionName.trim()
        : 'BOM.SolarSystem';

    // 6) Build payload (merged)
    final payload = {
      "bom": [
        {
          if (isUpdate) "id": firstId,
          "tenantId": tenantId,
          "name": apiName,
          "facilityId": facilityId,
          "assignUser": assignUserUuid,
          "data": mergedKV,
          "isActive": true,
        }
      ],
      "isCascadingProjectDateUpdate": false,
      "apiOperation": isUpdate ? "UPDATE" : "CREATE",
    };

    print("payload $payload");

    // 7) POST
    final path =
        isUpdate ? 'activity/v1/bom/_update' : 'activity/v1/bom/_create';
    await _dio.post(path, data: payload);

    // 8) Mark clean (persist id for all if update)
    await isar.writeTxn(() async {
      for (final d in dirty) {
        if (isUpdate) d.serverBomId = firstId;
        d.isDirty = false;
        await isar.cacheBomDocs.put(d);
      }
    });
  }

  // -------- Old split flow kept (optional) ----------
  Future<void> submitAllDirtyForProject({
    required Isar isar,
    required String projectId,
    required String tenantId,
    required String facilityId,
    required String assignUserUuid,
  }) async {
    // Keep for backward compatibility if you still call this somewhere.
    await enrichProjectDocs(
      isar: isar,
      projectId: projectId,
      tenantId: tenantId,
      facilityId: facilityId,
      assignUserUuid: assignUserUuid,
    );

    final dirty = (await getAllForProject(isar, projectId))
        .where((d) => d.isDirty)
        .toList();
    if (dirty.isEmpty) return;

    final creates = <CacheBomDoc>[];
    final updates = <CacheBomDoc>[];

    for (final d in dirty) {
      if (d.serverBomId == null || d.serverBomId!.isEmpty) {
        creates.add(d);
      } else {
        updates.add(d);
      }
    }

    if (creates.isNotEmpty) {
      final body = await _buildRequestBody(creates);
      await _dio.post('activity/v1/bom/_create', data: body);
      await _markClean(isar, creates);
    }

    if (updates.isNotEmpty) {
      final body = await _buildRequestBody(updates, isUpdate: true);
      await _dio.post('activity/v1/bom/_update', data: body);
      await _markClean(isar, updates);
    }
  }

  Future<Map<String, dynamic>> _buildRequestBody(List<CacheBomDoc> docs,
      {bool isUpdate = false}) async {
    final bomArray = docs.map<Map<String, dynamic>>((d) {
      final payload = <String, dynamic>{
        "tenantId": d.tenantId,
        "name": d.bomName ?? d.schemaKey,
        if (d.facilityId?.isNotEmpty == true) "facilityId": d.facilityId,
        if (d.assignUserUuid?.isNotEmpty == true)
          "assignUser": d.assignUserUuid,
        // NOTE: this is raw, not merged; kept only for the old split flow.
        "data": d.dataMap,
        "isActive": true,
      };
      if (isUpdate && d.serverBomId != null && d.serverBomId!.isNotEmpty) {
        payload["id"] = d.serverBomId;
      }
      return payload;
    }).toList();

    return {
      "bom": bomArray,
      "isCascadingProjectDateUpdate": false,
      "apiOperation": isUpdate ? "UPDATE" : "CREATE"
    };
  }

  Future<void> _markClean(Isar isar, List<CacheBomDoc> docs) async {
    await isar.writeTxn(() async {
      for (final d in docs) {
        d.isDirty = false;
        await isar.cacheBomDocs.put(d);
      }
    });
  }
}
