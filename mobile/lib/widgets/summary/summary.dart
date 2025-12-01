import 'dart:io';

import 'package:path/path.dart' as p;

import '../../model/appconfig/mdmsRequest.dart';
import '../../repositories/app_init_repo.dart';

typedef FileTapCallback = void Function(String path);
typedef RemoveReportCallback = void Function(ExistingReport report);

class ExistingReport {
  final int? isarId;
  final String filePath;
  final String fileName;
  final String fileType; // 'pdf' | 'image' | 'unknown'

  ExistingReport({
    this.isarId,
    required this.filePath,
    required this.fileName,
    required this.fileType,
  });
}

String inferFileType(String path) {
  final ext = p.extension(path).toLowerCase();
  const imgExts = {'.jpg', '.jpeg', '.png', '.gif', '.webp'};
  if (ext == '.pdf') return 'pdf';
  if (imgExts.contains(ext)) return 'image';

  // Magic bytes (read a tiny header)
  try {
    final f = File(path);
    if (!f.existsSync()) return 'unknown';
    final raf = f.openSync(mode: FileMode.read);
    final header = raf.readSync(16);
    raf.closeSync();

    if (header.length >= 4) {
      // PDF: %PDF-
      if (header[0] == 0x25 &&
          header[1] == 0x50 &&
          header[2] == 0x44 &&
          header[3] == 0x46) {
        return 'pdf';
      }

      // JPEG: FF D8 (SOI)
      if (header[0] == 0xFF && header[1] == 0xD8) {
        return 'image';
      }

      // PNG: 89 50 4E 47 0D 0A 1A 0A
      if (header.length >= 8 &&
          header[0] == 0x89 &&
          header[1] == 0x50 &&
          header[2] == 0x4E &&
          header[3] == 0x47 &&
          header[4] == 0x0D &&
          header[5] == 0x0A &&
          header[6] == 0x1A &&
          header[7] == 0x0A) {
        return 'image';
      }

      // GIF: "GIF87a" or "GIF89a"
      if (header.length >= 6 &&
          header[0] == 0x47 &&
          header[1] == 0x49 &&
          header[2] == 0x46 &&
          header[3] == 0x38 &&
          (header[4] == 0x37 || header[4] == 0x39) &&
          header[5] == 0x61) {
        return 'image';
      }

      // WebP: "RIFF....WEBP"
      if (header.length >= 12 &&
          header[0] == 0x52 &&
          header[1] == 0x49 &&
          header[2] == 0x46 &&
          header[3] == 0x46 &&
          header[8] == 0x57 &&
          header[9] == 0x45 &&
          header[10] == 0x42 &&
          header[11] == 0x50) {
        return 'image';
      }
    }
  } catch (_) {
    // ignore and fall through
  }

  return 'unknown';
}

String fileSizeFor(String path) {
  try {
    final f = File(path);
    if (!f.existsSync()) return '0 KB';
    final kb = (f.lengthSync() / 1024).toStringAsFixed(1);
    return '$kb KB';
  } catch (_) {
    return '0 KB';
  }
}

Future<
    ({
      String label,
      String schemaName,
      String pageName,
      String schemaCode,
      String uniqueIdentifier,
    })> bomRouteAndLabel(String name) async {
  final r = AppInitRepo();
  final nameLower = name.toLowerCase();

  // 1) Load SELCO.FormConfig docs (raw)
  final rawDocs = await r.searchFormConfigsRaw(const MdmsRequestModel(
    mdmsCriteria: MdmsCriteriaModel(
      tenantId: 'in',
      moduleDetails: [
        MdmsModuleDetailsModel(
          moduleName: 'SELCO',
          masterDetails: [MdmsMasterDetailsModel('FormConfig')],
        ),
      ],
    ),
  ));
  if (rawDocs == null || rawDocs.isEmpty) {
    throw StateError('No SELCO.FormConfig documents available.');
  }

  // 2) Score candidates by how well they match `name` without assuming any prefix
  int scoreOf(Map e) {
    String s(Object? v) => (v ?? '').toString();
    final schemaCode = s(e['schemaCode']).toLowerCase();
    final dataName = s(e['data']?['name']).toLowerCase();

    // exact match
    if (schemaCode == nameLower || dataName == nameLower) return 100;

    // endsWith "<name>" (e.g., "...RMS_Single_Phase_BOM_Solar")
    if (schemaCode.endsWith(nameLower) || dataName.endsWith(nameLower))
      return 80;

    // contains "<name>"
    if (schemaCode.contains(nameLower) || dataName.contains(nameLower))
      return 60;

    return 0;
  }

  final candidates = rawDocs.whereType<Map>().toList();
  candidates.sort((a, b) => scoreOf(b).compareTo(scoreOf(a)));
  final best = candidates.isNotEmpty && scoreOf(candidates.first) > 0
      ? Map<String, dynamic>.from(candidates.first)
      : null;

  if (best == null) {
    throw StateError('No FormConfig matches for "$name".');
  }

  // 3) Extract schemaName & pageName strictly from the chosen doc
  String _str(Object? v) => (v ?? '').toString();

  final data = best['data'];
  if (data is! Map) {
    throw StateError('FormConfig[$name]: missing data block.');
  }

  final schemaName = _str(data['name']).trim();
  if (schemaName.isEmpty) {
    throw StateError('FormConfig[$name]: data.name is empty.');
  }

  final pages = data['pages'];
  if (pages is! List || pages.isEmpty || pages.first is! Map) {
    throw StateError('FormConfig[$name]: pages[0] is missing.');
  }
  final pageName = _str((pages.first as Map)['page']).trim();
  if (pageName.isEmpty) {
    throw StateError('FormConfig[$name]: pages[0].page is empty.');
  }

  final schemaCode = _str(best['schemaCode']).trim();
  final uniqueIdentifier = _str(best['uniqueIdentifier']).trim();

  // 4) Build a human label from the token (you can tweak this mapping freely)
  String labelFromName(String n) {
    final lower = n.toLowerCase();
    final afterBom = RegExp(r'_bom_([a-z0-9]+)$').firstMatch(lower)?.group(1);
    final alreadyPrefixed =
        RegExp(r'\bbom[_\-\s]([a-z0-9]+)\b').firstMatch(lower)?.group(1);
    final token =
        (afterBom ?? alreadyPrefixed ?? lower.split('_').last).toLowerCase();

    switch (token) {
      case 'solar':
      case 'solarsystem':
        return 'BOM Solar System';
      case 'rms':
        return 'BOM RMS';
      case 'wiring':
      case 'load':
      case 'loadwiring':
        return 'BOM Load Wiring';
      case 'luminaries':
      case 'luminary':
      case 'fan':
      case 'fans':
        return 'BOM Luminaries';
      case 'system':
      case 'parameters':
      case 'parameter':
        return 'System Parameters';
      default:
        // Title-case the original, purely cosmetic
        return n
            .replaceAll(RegExp(r'[_\-]+'), ' ')
            .replaceAllMapped(RegExp(r'\b[a-z]'), (m) => m[0]!.toUpperCase());
    }
  }

  final label = labelFromName(name);

  return (
    label: label,
    schemaName: schemaName,
    pageName: pageName,
    schemaCode: schemaCode,
    uniqueIdentifier: uniqueIdentifier,
  );
}
