import 'dart:io';

import 'package:path/path.dart' as p;

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

({String label, String schemaName, String pageName}) bomRouteAndLabel(
    String name) {
  final n = name.toLowerCase();

  // Try to extract the token after "..._bom_"
  final bomMatch = RegExp(r'_bom_([a-z0-9]+)$').firstMatch(n);
  final token =
      bomMatch?.group(1) ?? n.split('_').last; // fallback: last segment

  switch (token) {
    case 'system':
    case 'parameters':
    case 'parameter':
      return (
        label: 'System Parameters',
        schemaName: 'AssetForm.SystemParameters',
        pageName: 'SystemFunctionalityParameters_1',
      );
    case 'solar':
    case 'solarsystem':
      return (
        label: 'BOM Solar System',
        schemaName: 'AssetForm',
        pageName: 'ModuleMountingstructure',
      );
    case 'luminaries':
    case 'luminary':
    case 'fan':
    case 'fans':
      return (
        label: 'BOM Luminaries',
        schemaName: 'AssetForm.LuminariesFan',
        pageName: 'Luminaires_Fans_Page1',
      );
    case 'wiring':
    case 'load':
    case 'loadwiring':
      return (
        label: 'BOM Load Wiring',
        schemaName: 'AssetForm.LoadWiring',
        pageName: 'BOM.LoadWiring',
      );
    case 'rms':
      return (
        label: 'BOM RMS',
        schemaName: 'AssetForm.RMS',
        pageName: 'BOM.RMS',
      );
    default:
      final pretty = name
          .replaceAll('_', ' ')
          .replaceAllMapped(RegExp(r'\b([a-z])'), (m) => m[1]!.toUpperCase());
      return (
        label: '$pretty',
        schemaName: 'AssetForm',
        pageName: 'ModuleMountingstructure',
      );
  }
}
