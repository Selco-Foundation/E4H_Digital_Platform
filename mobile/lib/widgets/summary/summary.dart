import 'dart:io';

import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';
import 'package:path/path.dart' as p;

import '../files/pdf_card.dart';

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

/// Widget to display a list of existing files (images & pdfs).
/// showEditButton toggles whether remove icons are shown.
Widget existingFilesSection({
  required BuildContext context,
  required List<ExistingReport> existing,
  required bool showEditButton,
  required FileTapCallback onTapImage,
  required FileTapCallback onTapPdf,
  required RemoveReportCallback onRemove,
}) {
  if (existing.isEmpty) return const SizedBox.shrink();

  final images = existing.where((e) => e.fileType == 'image').toList();
  final pdfs = existing.where((e) => e.fileType == 'pdf').toList();

  return Column(
    crossAxisAlignment: CrossAxisAlignment.stretch,
    children: [
      if (images.isNotEmpty) ...[
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: images.asMap().entries.map((entry) {
              final img = entry.value;

              return Padding(
                padding: const EdgeInsets.only(right: spacer2),
                child: Stack(
                  clipBehavior: Clip.none,
                  children: [
                    GestureDetector(
                      onTap: () => onTapImage(img.filePath),
                      child: Image.file(
                        File(img.filePath),
                        width: spacer12 * 2,
                        height: spacer12 * 2,
                        fit: BoxFit.cover,
                      ),
                    ),
                    if (showEditButton == true)
                      cancelIcon(
                        context: context,
                        onPress: () => onRemove(img),
                      ),
                  ],
                ),
              );
            }).toList(),
          ),
        ),
        const SizedBox(height: spacer3),
      ],
      if (pdfs.isNotEmpty) ...[
        const SizedBox(height: spacer2),
        Column(
          children: pdfs.asMap().entries.map((entry) {
            final pdf = entry.value;

            return Padding(
              padding: const EdgeInsets.only(bottom: spacer2),
              child: Stack(
                clipBehavior: Clip.none,
                children: [
                  GestureDetector(
                    onTap: () => onTapPdf(pdf.filePath),
                    child: pdfCard(
                      context: context,
                      filePath: pdf.filePath,
                      fileSize: fileSizeFor(pdf.filePath),
                    ),
                  ),
                  if (showEditButton == true)
                    cancelIcon(context: context, onPress: () => onRemove(pdf))
                ],
              ),
            );
          }).toList(),
        ),
      ],
    ],
  );
  return Column(
    crossAxisAlignment: CrossAxisAlignment.stretch,
    children: [
      if (images.isNotEmpty) ...[
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: images.map((img) {
              return Padding(
                padding: const EdgeInsets.only(right: 8.0),
                child: Stack(
                  children: [
                    GestureDetector(
                      onTap: () => onTapImage(img.filePath),
                      child: Image.file(
                        File(img.filePath),
                        width: 100,
                        height: 100,
                        fit: BoxFit.cover,
                      ),
                    ),
                    if (showEditButton)
                      Positioned(
                        right: 0,
                        top: 0,
                        child: IconButton(
                          icon: const Icon(Icons.close, size: 20),
                          onPressed: () => onRemove(img),
                        ),
                      ),
                  ],
                ),
              );
            }).toList(),
          ),
        ),
        const SizedBox(height: 8),
      ],
      if (pdfs.isNotEmpty) ...[
        Column(
          children: pdfs.map((pdf) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 8.0),
              child: Stack(
                children: [
                  GestureDetector(
                    onTap: () => onTapPdf(pdf.filePath),
                    child: pdfCard(
                      context: context,
                      filePath: pdf.filePath,
                      fileSize: File(pdf.filePath).lengthSync().toString(),
                    ),
                  ),
                  if (showEditButton)
                    cancelIcon(
                      context: context,
                      onPress: () => onRemove(pdf),
                    )
                ],
              ),
            );
          }).toList(),
        ),
      ],
    ],
  );
}

/// A small cancel icon (used to remove) overlaid widget
Widget cancelIcon({required BuildContext context, Function()? onPress}) {
  final theme = Theme.of(context);
  return Positioned(
    right: 0,
    top: 0,
    child: Container(
      width: spacer6,
      height: spacer6,
      color: theme.colorTheme.primary.primary2,
      child: IconButton(
          iconSize: spacer4,
          padding: EdgeInsets.zero,
          icon: Icon(Icons.close, color: theme.colorTheme.paper.secondary),
          onPressed: onPress),
    ),
  );
}

/// Decides BOTH the label shown on the button and the destination route.
/// We look at the BOM form's `name` and infer a friendly label + (schemaName, pageName).
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
        label: 'Fill System Parameters',
        schemaName: 'AssetForm.SystemParameters',
        pageName: 'SystemFunctionalityParameters_1',
      );
    case 'solar':
    case 'solarsystem':
      return (
        label: 'Fill BOM Solar System',
        schemaName: 'AssetForm',
        pageName: 'ModuleMountingstructure',
      );
    case 'luminaries':
    case 'luminary':
    case 'fan':
    case 'fans':
      return (
        label: 'Fill BOM Luminaries',
        schemaName: 'AssetForm.LuminariesFan',
        pageName: 'Luminaires_Fans_Page1',
      );
    case 'wiring':
    case 'load':
    case 'loadwiring':
      return (
        label: 'Fill BOM Load Wiring',
        schemaName: 'AssetForm.LoadWiring',
        pageName: 'BOM.LoadWiring',
      );
    case 'rms':
      return (
        label: 'Fill BOM RMS',
        schemaName: 'AssetForm.RMS',
        pageName: 'BOM.RMS',
      );
    default:
      final pretty = name
          .replaceAll('_', ' ')
          .replaceAllMapped(RegExp(r'\b([a-z])'), (m) => m[1]!.toUpperCase());
      return (
        label: 'Fill $pretty',
        schemaName: 'AssetForm',
        pageName: 'ModuleMountingstructure',
      );
  }
}
