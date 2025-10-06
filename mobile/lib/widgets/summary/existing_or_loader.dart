import 'dart:io';

import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';
import 'package:path/path.dart' as p;

import '../../model/document/document.dart';
import '../../router/app_router.dart';
import '../../widgets/summary/summary.dart';
import '../files/pdf_card.dart';

/// Rename PDFs using workflow documentType **only when** the filePath
/// contains that document's fileStoreId (so we don't mix names by order).
List<ExistingReport> _applyPdfNamesByFileStoreId(
  List<ExistingReport> existing,
  List<Document> docs,
) {
  if (existing.isEmpty || docs.isEmpty) return existing;

  // Map fileStoreId -> documentType (both lowercased for easy matching)
  final byFsId = <String, String>{};
  for (final d in docs) {
    final fsid = (d.fileStore ?? '').toLowerCase();
    final dtype = (d.documentType ?? '').toLowerCase();
    if (fsid.isNotEmpty && dtype.isNotEmpty) {
      byFsId[fsid] = dtype; // e.g. 58a28418-... -> installation_report
    }
  }

  return existing.map((e) {
    final isPdf = (e.fileType.toLowerCase() == 'pdf');
    if (!isPdf) return e; // images/unknown untouched

    final pathLower = e.filePath.toLowerCase();
    String? matchedDocType;

    // Match by fileStoreId substring in the saved file name/path
    for (final entry in byFsId.entries) {
      if (pathLower.contains(entry.key)) {
        matchedDocType = entry.value; // already lowercased
        break;
      }
    }

    // Only rename when we have a positive match
    if (matchedDocType != null && matchedDocType.isNotEmpty) {
      final clean =
          '${matchedDocType.replaceAll('_', ' ')}.pdf'; // installation_report.pdf or installation_report_bom.pdf
      return ExistingReport(
        isarId: e.isarId,
        filePath: e.filePath,
        fileName: clean,
        fileType: e.fileType,
      );
    }

    // No match -> keep as-is (prevents wrong swaps)
    return e;
  }).toList();
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
  final pdfs = existing
      .where((e) => e.fileType == 'pdf' || e.fileType == 'unknown')
      .toList();

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

            final _display = (pdf.fileName.trim().isNotEmpty
                ? pdf.fileName.trim()
                : p.basename(pdf.filePath));

            return Padding(
              padding: const EdgeInsets.only(bottom: spacer2),
              child: Stack(
                clipBehavior: Clip.none,
                children: [
                  GestureDetector(
                    onTap: () => onTapPdf(pdf.filePath),
                    child: pdfCard(
                        context: context,
                        // filePath: pdf.filePath,
                        fileSize: fileSizeFor(pdf.filePath),
                        filePath: _display.replaceAll('_', ' ')),
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

class ExistingFilesOrLoader extends StatelessWidget {
  final List<ExistingReport>? existingReports;
  final List<Document>? workflowDocuments;
  final bool readOnly;
  final void Function(ExistingReport)? onRemove;

  const ExistingFilesOrLoader({
    Key? key,
    required this.existingReports,
    required this.workflowDocuments,
    this.readOnly = false,
    this.onRemove,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final docs = workflowDocuments ?? [];
    final hasInstallReportDoc = docs.any((d) {
      final t = (d.documentType ?? "");
      return t.toUpperCase().contains("INSTALLATION_REPORT");
    });

    if (existingReports!.isEmpty && hasInstallReportDoc) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 24.0),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    final displayable = _applyPdfNamesByFileStoreId(
        existingReports ?? [], workflowDocuments ?? []);
    return existingFilesSection(
      context: context,
      existing: displayable,
      showEditButton: !readOnly,
      onTapImage: (path) => context.router.push(ImageViewerRoute(path: path)),
      onTapPdf: (path) => context.router.push(PdfViewerRoute(path: path)),
      onRemove: (r) {
        if (!readOnly && onRemove != null) {
          onRemove!(r);
        }
      },
    );
  }
}
