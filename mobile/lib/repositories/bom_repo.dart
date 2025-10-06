import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:http_parser/http_parser.dart';
import 'package:isar/isar.dart';
import 'package:mime/mime.dart';

import '../data/nosql/cache_bom_doc.dart';
import '../data/nosql/cache_project_bom_values.dart';
import '../data/remote_client.dart';
import '../model/entities/project_facility.dart';
import '../repositories/project_facility_repo.dart';
import '../utils/envConfig.dart' as env;
import '../utils/utils.dart';
import 'assetRepo.dart';
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
          d.isDirty = true;
          await isar.cacheBomDocs.put(d);
        }
      }
    });
  }

  // -------- Deep extractor (NEW) ----------
  /// Deep, shape-agnostic flattener:
  /// collects every `{ fieldName: <string>, value: <any> }` anywhere in the tree.
  Map<String, dynamic> extractKVFromRawDoc(Map<String, dynamic> raw) {
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
      final kv = extractKVFromRawDoc(d.dataMap);
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

    final path =
        isUpdate ? 'activity/v1/bom/_update' : 'activity/v1/bom/_create';
    final response = await _dio.post(path, data: payload);

    print("response.data ${response.data}");

    // 8) Mark clean (persist id for all if update)
    await isar.writeTxn(() async {
      for (final d in dirty) {
        if (isUpdate) d.serverBomId = firstId;
        d.isDirty = false;
        await isar.cacheBomDocs.put(d);
      }
    });
  }

  Future<Map<String, dynamic>> searchBom({
    required List<String> facilityIds,
    int offset = 0,
    int limit = 100,
  }) async {
    final tenantId = envConfigs.variables.tenantId;
    final path =
        "/activity/v1/bom/_search?tenantId=${tenantId}&offset=$offset&limit=$limit";

    Response response;
    final body = {
      "bom": {
        "facilityIds": facilityIds,
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

  // ------------------------ New: BOM Sync -------------------------------

  /// Fetch BOM for [projectId] (resolving facilityId internally),
  /// cache the BOM `data` per (projectId, userType), and
  /// replace completion reports with BOM `documents`.
  ///
  /// Returns (savedBomValues).
  Future<({bool savedBomValues})> syncBomForProject(
      {required String projectId,
      required String userType,
      required Isar isar}) async {
    try {
      final facilityId = (await ProjectFacilityRepository().search(
        ProjectFacilitySearchModel(projectId: [projectId]),
        isar,
      ))
          .facilityId;
      if (facilityId == null || facilityId.isEmpty) {
        return (savedBomValues: false);
      }

      // 2) Fetch from server
      final res = await searchBom(facilityIds: [facilityId]);
      final boms = (res['bom'] as List?) ?? const [];
      print("bom $boms");
      if (boms.isEmpty) {
        return (savedBomValues: false);
      }

      // If multiple BOMs, pick the first. Adjust to merge if you prefer.
      final bom = (boms.first as Map<String, dynamic>);

      // 3) Save BOM `data` for (projectId, userType)
      bool savedValues = false;
      print("bom $bom");
      final data = (bom['data'] as Map<String, dynamic>?);
      if (data != null) {
        final entryKey = '$projectId::$userType';
        await isar.writeTxn(() async {
          await isar.cacheProjectBomValues.put(
            CacheProjectBomValues()
              ..projectId = projectId
              ..userType = userType
              ..entryKey = entryKey
              ..dataJson = jsonEncode(data)
              ..updatedAt = DateTime.now(),
          );
        });
        savedValues = true;
      }

      // 5) Replace all completion reports for this project atomically

      return (savedBomValues: savedValues);
    } catch (e, stack) {
      // Print full error & stacktrace
      print("syncBomForProject ERROR: $e");
      print(stack);
      throw Exception("Error syncing bom");
    }
  }

  // inside class BomRepository

  /// Calls /activity/v1/bom/_generate_pdf, passing the bom data stored in Isar for (projectId, userType),
  /// and returns the bytes (Uint8List). Throws on error.
  Future<Uint8List?> generateBomPdf({
    required Isar isar,
    required String projectId,
    required String userType,
  }) async {
    try {
      final entryKey = '$projectId::$userType';
      print("entryKey $entryKey");
      final rec = await isar.cacheProjectBomValues
          .where()
          .entryKeyEqualTo(entryKey)
          .findFirst();
      print("entryKey $entryKey");
      if (rec == null) {
        throw Exception("No BOM values found for project");
      }
      final Map<String, dynamic> bomData =
          jsonDecode(rec.dataJson) as Map<String, dynamic>;

      // 2. Build request body
      final tenantId = env.envConfig.variables.tenantId;
      final body = {
        "bom": bomData,
      };

      final path = "activity/v1/bom/_generate_pdf?tenantId=$tenantId";

      final response = await _dio.post<List<int>>(
        path,
        data: body,
        options: Options(
          responseType: ResponseType.bytes,
          headers: {"Content-Type": "application/json"},
        ),
      );

      print("response.type ${response.headers}");
      print("response.status ${response.statusCode}");

      final data = response.data;
      if (data == null) {
        throw Exception("Empty PDF response");
      }

      if (data is Uint8List) {
        return data;
      }
      if (data is List<int>) {
        return Uint8List.fromList(data);
      }
      throw Exception("Unexpected PDF response type: ${data.runtimeType}");
    } catch (e) {
      print("error $e");
    }
  }

  Future<String> uploadPdfToFileStore(Uint8List bytes, String filename) async {
    // 1. Write bytes to a temp file
    final tempDir = Directory.systemTemp;
    final tempPath = '${tempDir.path}/$filename';
    final file = File(tempPath);
    await file.writeAsBytes(bytes);

    // 2. Use the AssetRepository (or its logic) to upload file
    // If you want to reuse uploadFile, you could inject or call that method,
    // or just replicate the same multipart logic here.

    // Determine mimeType
    String? mimeType = lookupMimeType(filename);
    if (mimeType == null) {
      final bd = await file.readAsBytes();
      mimeType = lookupMimeType('', headerBytes: bd);
    }

    // Ensure extension if missing
    String fileNameWithExt = filename;
    if (!filename.contains('.')) {
      final ext = getExtensionFromMime(mimeType ?? 'application/pdf');
      fileNameWithExt = '$filename.$ext';
    }

    final mf = await MultipartFile.fromFile(
      file.path,
      filename: fileNameWithExt,
      contentType: mimeType != null
          ? MediaType.parse(mimeType)
          : MediaType('application', 'pdf'),
    );

    final form = FormData.fromMap({
      "file": mf,
      "tenantId": env.envConfig.variables.tenantId,
      "module": "Incident",
    });

    final response = await _dio.post(
      "/filestore/v1/files",
      data: form,
      options: Options(contentType: "multipart/form-data"),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return FileStoreResponse.fromJson(response.data).fileStoreId;
    } else {
      throw Exception("File store upload failed: ${response.statusCode}");
    }
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
      final existing = await isar.cacheProjectBomValues
          .where()
          .entryKeyEqualTo(entryKey)
          .findFirst();

      final existingMap = (existing?.dataJson?.isNotEmpty ?? false)
          ? (json.decode(existing!.dataJson!) as Map<String, dynamic>)
          : <String, dynamic>{};

      final merged = deepMerge(existingMap, safeIncoming);
      final jsonString = json.encode(jsonSafe(merged));

      final rec = existing ?? CacheProjectBomValues()
        ..projectId = projectId
        ..userType = userType
        ..entryKey = entryKey;

      rec
        ..dataJson = jsonString
        ..updatedAt = DateTime.now();

      await isar.cacheProjectBomValues.put(rec);
    });
  }

  Future<bool> hasBomForSchema({
    required Isar isar,
    required String projectId,
    required String schemaKey,
  }) async {
    final existing = await isar.cacheBomDocs
        .where()
        .projectIdEqualToAnySchemaKey(projectId)
        .filter()
        .schemaKeyEqualTo(schemaKey)
        .findFirst();
    return existing != null;
  }

  Future<String> resolveBomActionLabel({
    required Isar isar,
    required String projectId,
    required String schemaKey,
    required bool isInboxView,
    bool isOverall = false,
  }) async {
    if (isInboxView) return 'View';
    final exists = await hasBomForSchema(
      isar: isar,
      projectId: projectId,
      schemaKey: schemaKey,
    );
    return exists
        ? 'Edit'
        : isOverall
            ? 'Fill'
            : 'View';
  }

  Future<Map<String, dynamic>?> getProjectBomKV({
    required Isar isar,
    required String projectId,
    required String userType,
  }) async {
    final entryKey = '$projectId::$userType';
    final rec = await isar.cacheProjectBomValues
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
}
