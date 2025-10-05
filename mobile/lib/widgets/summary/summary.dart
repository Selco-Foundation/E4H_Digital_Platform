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
  const img = {'.jpg', '.jpeg', '.png'};
  if (ext == '.pdf') return 'pdf';
  if (img.contains(ext)) return 'image';
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
