import 'dart:collection';
import 'dart:convert';
import 'dart:io';

import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart'
    as DigitPropertySchema;
import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:dio/dio.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:intl/intl.dart';
import 'package:isar/isar.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:uuid/uuid.dart';

import '../blocs/app_init/app_init.dart';
import '../data/app_shared_preferences.dart';
import '../data/nosql/cache_completion_report.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/document/document.dart';
import '../repositories/app_init_repo.dart';
import '../repositories/asset_repo.dart';

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
  static IdGen get i => instance;
  final Uuid uuid;
  const IdGen._internal() : uuid = const Uuid();
  String get identifier => uuid.v1();
}

Future<String> copyFileToLocalDir(File sourceFile) async {
  try {
    final appDocDir = await getApplicationDocumentsDirectory();
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    final fileName = '${timestamp}_${sourceFile.uri.pathSegments.last}';
    final dest = File('${appDocDir.path}/$fileName');
    final copied = await sourceFile.copy(dest.path);
    return copied.path;
  } on MissingPluginException catch (e) {
    AppLogger.instance.info('Storage permission check failed: $e');
    return sourceFile.path;
  }
}

Future<File?> resolveFilePath(String path) async {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    try {
      final resp = await http.get(Uri.parse(path));
      if (resp.statusCode != 200) return null;
      final dir = await getTemporaryDirectory();
      final filename = path.split('/').last;
      final f = File('${dir.path}/$filename');
      await f.writeAsBytes(resp.bodyBytes);
      return f;
    } catch (_) {
      return null;
    }
  } else {
    final f = File(path);
    return await f.exists() ? f : null;
  }
}

String truncateText(String text, {int maxLength = 16}) {
  if (text.length > maxLength) {
    return '${text.substring(0, maxLength)}...';
  }
  return text;
}

String truncateTextFromStart(String text, {int maxLength = 16}) {
  if (text.length <= maxLength) return text;
  final keep = maxLength - 3;
  if (keep <= 0) return '...';
  return '...${text.substring(text.length - keep)}';
}

int parseWarrantyMonths(String s) {
  final regex = RegExp(r'^P(?:(\d+)Y)?(?:(\d+)M)?');
  final match = regex.firstMatch(s);
  if (match != null) {
    final years = int.tryParse(match.group(1) ?? '') ?? 0;
    final months = int.tryParse(match.group(2) ?? '') ?? 0;
    return years * 12 + months;
  }
  return 1;
}

int parseWarrantyYears(String? s) {
  if (s == null || s.isEmpty || s.length < 1) return 0;
  final numeric = int.tryParse(s);
  if (numeric != null) {
    return numeric;
  }
  final regex = RegExp(r'^P(?:(\d+)Y)?(?:(\d+)M)?');
  final match = regex.firstMatch(s);
  if (match != null) {
    final years = int.tryParse(match.group(1) ?? '') ?? 0;
    final months = int.tryParse(match.group(2) ?? '') ?? 0;
    final totalMonths = years * 12 + months;
    return (totalMonths + 11) ~/ 12;
  }
  return 0;
}

final _displayFmt = DateFormat('yyyy-MM-dd HH:mm');

String buildWarrantyStart(String? raw) {
  try {
    if (raw == null || raw.isEmpty) {
      return _displayFmt.format(DateTime.now());
    }

    final dt = DateTime.parse(raw);
    return _displayFmt.format(dt);
  } catch (_) {
    return _displayFmt.format(DateTime.now());
  }
}

enum WORKFLOW_STATUS_FIELD_STAFF {
  ASSIGNED_TO_FIELD_STAFF,
  REJECTED_BY_FIELD_SUPERVISOR,
  REJECTED_BY_QC_SPOC,
  APPROVED_BY_QC_SPOC,
  APPROVED_BY_SUPERVISOR,
  SUBMITTED_BY_FIELD_STAFF,
  PENDING_APPROVAL_FLAGGED_FOR_QC
}

enum WORKFLOW_STATUS_FIELD_SUPERVISOR {
  ASSIGNED_TO_FIELD_SUPERVISOR,
  SUBMITTED_BY_FIELD_STAFF,
  SUBMITTED_BY_SUPERVISOR,
  REJECTED_BY_QC_SPOC,
  APPROVED_BY_QC_SPOC,
  PENDING_APPROVAL_FLAGGED_FOR_QC
}

enum WORKFLOW_STATUS_AMC_FIELD_STAFF {
  SCHEDULED,
  APPROVED,
  REJECTED,
  PENDING_OTP_APPROVAL,
  PENDING_APPROVAL,
}

enum WORKFLOW_ACTIONS {
  CREATE_AND_SAVE_DRAFT,
  SUBMIT_REPORT_A,
  SUBMIT_REPORT_B
}

enum REPORT_TYPES {
  NEW_REPORT,
  INBOX,
  SUBMITTED,
  REVIEW,
  REJECTED,
  APPROVED,
  ADD_MORE
}

enum USER_TYPES { SUPERVISOR, FIELD_STAFF, AMC }

enum ASSET_TYPES { BATTERY, INVERTER, PANEL }

enum FormOrigin { overallSummary, inboxSummary, submitForApproval, submitted }

enum SYSTEM_TYPE { DC }

bool isValidUuid(String value) {
  try {
    return Uuid.parse(value).isNotEmpty;
  } catch (_) {
    return false;
  }
}

String? normalizedInstallPdfNameFromPath(
  String path,
  List<Document> docs,
) {
  String normalizeType(String t) {
    final s = (t).toLowerCase();
    if (s.contains('installation_report_bom')) return 'installation_report_bom';
    if (s.contains('installation_report')) return 'installation_report';
    return s;
  }

  final pathLower = path.toLowerCase();
  final byFsId = <String, String>{};
  for (final d in docs) {
    final fsid = (d.fileStore ?? '').toLowerCase();
    final dtype = (d.documentType ?? '').toLowerCase();
    if (fsid.isNotEmpty && dtype.isNotEmpty) {
      byFsId[fsid] = normalizeType(dtype);
    }
  }

  for (final entry in byFsId.entries) {
    if (pathLower.contains(entry.key)) {
      return '${entry.value}.pdf';
    }
  }

  final base = p.basename(path).toLowerCase();
  if (base.contains('bom')) return 'installation_report_bom.pdf';

  return null;
}

final Map<String, File> _fileCache = {};

Future<File?> getCachedFile(String idOrPath) async {
  if (_fileCache.containsKey(idOrPath)) return _fileCache[idOrPath];

  if (isValidUuid(idOrPath)) {
    try {
      final uri = Uri.parse('$fileStoreFileUrl$idOrPath');
      final resp = await http.get(uri);
      if (resp.statusCode == 200) {
        final dir = await getTemporaryDirectory();
        final safeName = idOrPath.replaceAll(RegExp(r'[^a-zA-Z0-9._-]'), '_');
        final file = File(
            '${dir.path}/${DateTime.now().millisecondsSinceEpoch}_$safeName');
        await file.writeAsBytes(resp.bodyBytes);
        _fileCache[idOrPath] = file;
        return file;
      }
    } catch (_) {}
  }

  final file = File(idOrPath);
  if (await file.exists()) {
    _fileCache[idOrPath] = file;
    return file;
  }

  return null;
}

Future<String> getFilestoreUrl(String idOrPath) async {
  String photoId = idOrPath;
  if (!isValidUuid(photoId)) {
    final file = await getCachedFile(idOrPath);
    if (file != null) {
      final repo = AssetRepository();
      photoId = await repo.uploadFile(file);
    }
  }
  AppLogger.instance.info("photoId from return is $photoId");
  return photoId;
}

String fileStoreFileUrl =
    "${envConfig.variables.baseUrl}filestore/v1/files/file?tenantId=${envConfig.variables.tenantId}&fileStoreId=";

Map<String, dynamic> transformSelcoFormMdmsDocToSchema(
    Map<String, dynamic> mdmsDoc) {
  final data = (mdmsDoc['data'] as Map?) ?? const <String, dynamic>{};

  final name = (data['name'] ??
          mdmsDoc['schemaCode'] ??
          mdmsDoc['uniqueIdentifier'] ??
          'Form')
      .toString();
  final version = (data['version'] ?? 1);
  final order = data['order'] ?? 0;
  final summary = data['summary'] ?? false;

  final pagesArr = (data['pages'] as List?) ?? const <dynamic>[];
  final pagesMap = LinkedHashMap<String, dynamic>();
  for (final p in pagesArr) {
    if (p is! Map) continue;
    final page = Map<String, dynamic>.from(p as Map);
    final pageKey =
        (page['page'] ?? page['name'] ?? 'page_${pagesMap.length + 1}')
            .toString();

    final propsArr = (page['properties'] as List?) ?? const <dynamic>[];
    final propsMap = LinkedHashMap<String, dynamic>();
    for (var i = 0; i < propsArr.length; i++) {
      final raw = propsArr[i];
      if (raw is! Map) continue;
      final prop = Map<String, dynamic>.from(raw as Map);
      final key = (prop['fieldName'] ?? 'field_${i + 1}').toString();

      propsMap[key] = {
        'type': prop['type'],
        'label': prop['label'],
        'order': prop['order'],
        'value': prop['value'],
        'format': prop['format'],
        'section': prop['section'],
        'sectionDescription': prop['sectionDescription'],
        'hidden': prop['hidden'],
        'readOnly': prop['readOnly'],
        'deleteFlag': prop['deleteFlag'],
        'isMultiSelect': prop['isMultiSelect'],
        'includeInForm': prop['includeInForm'],
        'includeInSummary': prop['includeInSummary'],
        'systemDate': prop['systemDate'],
        'tooltip': prop['tooltip'],
        'helpText': prop['helpText'],
        'infoText': prop['infoText'],
        'innerLabel': prop['innerLabel'],
        'suffixText': prop['suffixText'],
        'errorMessage': prop['errorMessage'],
        'enums': prop['enums'],
        'validations': prop['validations'],
        'fieldName': key,
      };
    }

    pagesMap[pageKey] = {
      'page': pageKey,
      'type': page['type'] ?? 'object',
      'label': page['label'],
      'order': page['order'],
      'actionLabel': page['actionLabel'],
      'description': page['description'],
      'properties': propsMap,
    };
  }

  return {
    'name': name,
    'version': version,
    'order': order,
    'summary': summary,
    'pages': pagesMap,
  };
}

Map<String, dynamic> injectValuesIntoRawDoc({
  required Map<String, dynamic> rawDoc,
  required Map<String, dynamic> flatValues,
}) {
  final doc = jsonDecode(jsonEncode(rawDoc)) as Map<String, dynamic>;

  final pagesField = doc['pages'];
  if (pagesField is List) {
    for (final p in pagesField) {
      if (p is! Map) continue;
      final propsList = p['properties'];
      if (propsList is List) {
        for (final item in propsList) {
          if (item is Map) {
            final fieldName = item['fieldName']?.toString();
            if (fieldName != null && flatValues.containsKey(fieldName)) {
              item['value'] = flatValues[fieldName];
            }
          }
        }
      }
    }
    doc['pages'] = pagesField;
  } else if (pagesField is Map) {
    final pagesMap = <String, dynamic>{};
    pagesField.forEach((key, pObj) {
      if (pObj is Map) {
        final propsField = pObj['properties'];
        if (propsField is Map) {
          final propsMap = <String, dynamic>{};
          propsField.forEach((fieldKey, propRaw) {
            if (propRaw is Map) {
              final fieldName = propRaw['fieldName']?.toString() ?? fieldKey;
              if (flatValues.containsKey(fieldName)) {
                propRaw['value'] = flatValues[fieldName];
              }
              propsMap[fieldKey] = propRaw;
            } else {
              propsMap[fieldKey] = propRaw;
            }
          });
          pObj['properties'] = propsMap;
        }
      }
      pagesMap[key] = pObj;
    });
    doc['pages'] = pagesMap;
  } else {}

  return doc;
}

dynamic jsonSafe(dynamic v) {
  if (v is DateTime) return v.toUtc().toIso8601String();
  if (v is Map) {
    return v.map((k, val) => MapEntry(k.toString(), jsonSafe(val)));
  }
  if (v is List) return v.map(jsonSafe).toList();
  return v;
}

Map<String, dynamic> deepMerge(
  Map<String, dynamic> base,
  Map<String, dynamic> update,
) {
  final result = Map<String, dynamic>.from(base);
  update.forEach((k, v) {
    if (v is Map && result[k] is Map) {
      result[k] = deepMerge(
        Map<String, dynamic>.from(result[k] as Map),
        Map<String, dynamic>.from(v as Map),
      );
    } else {
      result[k] = v;
    }
  });
  return result;
}

Map<String, dynamic> subsetForPage(
  SchemaObject schema,
  String pageName,
  Map<String, dynamic> kv,
) {
  final page = schema.pages[pageName];
  if (page == null || page.properties == null) return const {};
  final allowed = page.properties!.keys.toSet();
  final out = <String, dynamic>{};
  for (final entry in kv.entries) {
    if (allowed.contains(entry.key)) {
      out[entry.key] = entry.value;
    }
  }
  return out;
}

Map<String, dynamic> extractKVFromRawDoc(Map<String, dynamic> raw) {
  final acc = <String, dynamic>{};
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

String prettyLabel(String s) {
  if (s.trim().isEmpty) return s;
  final spaced = s.replaceAll(RegExp(r'[_\-]+'), ' ').trim();
  return spaced.replaceAllMapped(
      RegExp(r'\b[a-z]'), (m) => m.group(0)!.toUpperCase());
}

String labelForKey(DigitPropertySchema.PropertySchema pageSchema, String key) {
  final raw = pageSchema?.properties?[key]?.label ?? key;
  return prettyLabel(raw);
}

dynamic coerceForControl(AbstractControl<Object?> control, dynamic v) {
  if (v == null) return null;

  if (control is FormControl<DateTime?>) {
    if (v is DateTime) return v;
    if (v is String) return DateTime.tryParse(v);
    return null;
  }

  if (control is FormControl<int?>) {
    if (v is int) return v;
    if (v is double) return v.toInt();
    if (v is String) return int.tryParse(v);
    return null;
  }

  if (control is FormControl<double?>) {
    if (v is double) return v;
    if (v is int) return v.toDouble();
    if (v is String) return double.tryParse(v);
    return null;
  }

  if (control is FormControl<bool?>) {
    if (v is bool) return v;
    if (v is String) {
      final s = v.toLowerCase().trim();
      if (s == 'true' || s == '1' || s == 'yes') return true;
      if (s == 'false' || s == '0' || s == 'no') return false;
    }
    if (v is num) return v != 0;
    return null;
  }

  return v;
}

String? currentSchemaKey({
  required FormsState state,
  required String pageName,
  String? schemaName,
  String? uniqueIdentifier,
}) {
  final requested = schemaName ?? uniqueIdentifier;
  if (requested != null && state.cachedSchemas.containsKey(requested)) {
    return requested;
  }
  final active = state.activeSchemaKey;
  if (active != null && state.cachedSchemas.containsKey(active)) {
    return active;
  }
  for (final e in state.cachedSchemas.entries) {
    if (e.value.pages.containsKey(pageName)) return e.key;
  }
  return state.cachedSchemas.isEmpty ? null : state.cachedSchemas.keys.first;
}

bool isLastPage({required SchemaObject schema, required String pageName}) {
  final lastKey = schema.pages.keys.isEmpty ? null : schema.pages.keys.last;
  return lastKey == pageName;
}

String basenameUtil(String path) {
  final norm = path.replaceAll('\\', '/');
  final idx = norm.lastIndexOf('/');
  return idx == -1 ? norm : norm.substring(idx + 1);
}

String inferFileTypeFromName(String name) {
  final lower = name.toLowerCase();
  if (lower.endsWith('.pdf')) return 'pdf';
  return 'image';
}

String getExtensionFromMime(String mimeType) {
  const map = {
    'image/jpeg': 'jpg',
    'image/png': 'png',
    'application/pdf': 'pdf',
    'video/mp4': 'mp4',
    'video/quicktime': 'mov',
    'text/plain': 'txt',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
        'docx',
    'application/msword': 'doc',
    'application/vnd.ms-excel': 'xls',
    'text/csv': 'csv',
    'audio/mpeg': 'mp3',
    'video/x-msvideo': 'avi',
    'video/x-ms-wmv': 'wmv',
    'application/x-mpegurl': 'm3u8',
    'video/mp2t': 'ts',
  };
  return map[mimeType] ?? 'dat';
}

Future<List<PlatformFile>> loadInitialCompletion({
  required Isar isar,
  required String projectId,
  required ActivityFacilityWorkflow activityFacilityWorkflow,
}) async {
  final cachedList = await isar.cacheCompletionReports
      .where()
      .activityFacilityIdEqualTo(projectId)
      .findAll();

  final localFiles = <PlatformFile>[];
  for (final cached in cachedList) {
    if (cached.filePath.isNotEmpty) {
      final f = await getCachedFile(cached.filePath);
      if (f != null) {
        final pth = await copyFileToLocalDir(f);
        localFiles.add(PlatformFile(
          name: p.basename(pth),
          path: pth,
          size: File(pth).lengthSync(),
        ));
      }
    }
  }

  final docs = activityFacilityWorkflow.workflow?.documents ?? [];

  final serverFiles = <PlatformFile>[];
  for (final doc in docs) {
    if (doc.documentType!.contains('INSTALLATION_REPORT') &&
        (doc.fileStore != null && doc.fileStore!.isNotEmpty)) {
      final idOrUrl = doc.fileStore ?? '';
      if (idOrUrl.isEmpty) continue;
      final f = await getCachedFile(idOrUrl);
      if (f != null) {
        final pth = await copyFileToLocalDir(f);
        serverFiles.add(PlatformFile(
          name: p.basename(pth),
          path: pth,
          size: File(pth).lengthSync(),
        ));
      }
    }
  }

  return [...localFiles, ...serverFiles];
}

Future<List<PlatformFile>> copyPickedFilesLocally(
    List<PlatformFile> picked) async {
  final copied = <PlatformFile>[];
  for (final pf in picked) {
    if (pf.path == null) continue;
    final f = File(pf.path!);
    final dest = await copyFileToLocalDir(f);
    copied.add(
      PlatformFile(
        name: p.basename(dest),
        path: dest,
        size: File(dest).lengthSync(),
      ),
    );
  }
  return copied;
}

class DioErrorParser {
  static Exception parse(DioError dioErr) {
    AppLogger.instance.info("Dio error: ${dioErr}");
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
