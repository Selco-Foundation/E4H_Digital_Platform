import 'dart:io';

import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';
import 'package:path/path.dart' as p;

import '../../model/document/document.dart';
import '../../router/app_router.dart';
import '../../utils/utils.dart';
import '../../widgets/summary/summary.dart';
import '../images/cached_image.dart';
import '../files/pdf_card.dart';

Widget _imagePlaceholder(BuildContext context) {
  final theme = Theme.of(context);
  return Container(
    color: theme.colorTheme.generic.background,
    child: const Center(
      child: SizedBox(
        width: spacer5,
        height: spacer5,
        child: CircularProgressIndicator(strokeWidth: 2),
      ),
    ),
  );
}

Widget _imageErrorFallback(BuildContext context) {
  final theme = Theme.of(context);
  return Container(
    color: theme.colorTheme.generic.background,
    child: Icon(
      Icons.broken_image_outlined,
      color: theme.colorTheme.text.secondary,
      size: spacer6,
    ),
  );
}

List<ExistingReport> _applyPdfNamesByFileStoreId(
  List<ExistingReport> existing,
  List<Document> docs,
) {
  if (existing.isEmpty || docs.isEmpty) return existing;

  final byFsId = <String, String>{};
  for (final d in docs) {
    final fsid = (d.fileStore ?? '').toLowerCase();
    final dtype = (d.documentType ?? '').toLowerCase();
    if (fsid.isNotEmpty && dtype.isNotEmpty) {
      byFsId[fsid] = dtype;
    }
  }

  return existing.map((e) {
    final isPdf = (e.fileType.toLowerCase() == 'pdf');
    if (!isPdf) return e;

    final pathLower = e.filePath.toLowerCase();
    String? matchedDocType;

    for (final entry in byFsId.entries) {
      if (pathLower.contains(entry.key)) {
        matchedDocType = entry.value;
        break;
      }
    }

    if (matchedDocType != null && matchedDocType.isNotEmpty) {
      final clean = '${matchedDocType.replaceAll('_', ' ')}.pdf';
      return ExistingReport(
        isarId: e.isarId,
        source: e.source,
        fileName: clean,
        fileType: e.fileType,
        isRemote: e.isRemote,
      );
    }

    return e;
  }).toList();
}

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
              final previewSize = spacer12 * 2;
              final decodeSize =
                  (previewSize * MediaQuery.of(context).devicePixelRatio)
                      .round();

              return Padding(
                padding: const EdgeInsets.only(right: spacer2),
                child: Stack(
                  clipBehavior: Clip.none,
                  children: [
                    GestureDetector(
                      onTap: () => onTapImage(img.source),
                      child: img.isRemote
                          ? CachedImage(
                              '$fileStoreFileUrl${img.source}',
                              width: previewSize,
                              height: previewSize,
                            )
                          : SizedBox(
                              width: previewSize,
                              height: previewSize,
                              child: Image.file(
                                File(img.source),
                                width: previewSize,
                                height: previewSize,
                                fit: BoxFit.cover,
                                cacheWidth: decodeSize,
                                cacheHeight: decodeSize,
                                frameBuilder: (context, child, frame, _) {
                                  if (frame == null) {
                                    return _imagePlaceholder(context);
                                  }
                                  return child;
                                },
                                errorBuilder: (_, __, ___) =>
                                    _imageErrorFallback(context),
                              ),
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

            final display = (pdf.fileName.trim().isNotEmpty
                ? pdf.fileName.trim()
                : p.basename(pdf.source));

            return Padding(
              padding: const EdgeInsets.only(bottom: spacer2),
              child: Stack(
                clipBehavior: Clip.none,
                children: [
                  GestureDetector(
                    onTap: pdf.isRemote ? null : () => onTapPdf(pdf.source),
                    child: pdfCard(
                        context: context,
                        fileSize: pdf.isRemote ? '' : fileSizeFor(pdf.source),
                        filePath: display.replaceAll('_', ' ')),
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
  final bool isLoading;
  final void Function(ExistingReport)? onRemove;

  const ExistingFilesOrLoader({
    Key? key,
    required this.existingReports,
    required this.workflowDocuments,
    this.readOnly = false,
    this.isLoading = false,
    this.onRemove,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 24.0),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    final displayable =
        _applyPdfNamesByFileStoreId(existingReports ?? [], workflowDocuments ?? []);
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
