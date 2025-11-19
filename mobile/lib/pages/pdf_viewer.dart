import 'dart:io';

import 'package:auto_route/auto_route.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/utils/app_logger.dart';
import 'package:flutter/material.dart';
import 'package:flutter_pdfview/flutter_pdfview.dart';
import 'package:http/http.dart' as http;
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class PdfViewerPage extends StatefulWidget {
  final String path;
  const PdfViewerPage({@PathParam() required this.path, super.key});

  @override
  State<PdfViewerPage> createState() => _PdfViewerPageState();
}

class _PdfViewerPageState extends State<PdfViewerPage> {
  String? _localPath;
  bool _isLoading = true;
  int _pages = 0;
  int _currentPage = 0;

  @override
  void initState() {
    super.initState();
    _preparePdf();
  }

  Future<void> _preparePdf() async {
    String filePath = widget.path;

    if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
      final uri = Uri.parse(filePath);
      final response = await http.get(uri);
      if (response.statusCode == 200) {
        final dir = await getTemporaryDirectory();
        final tmp = File(p.join(dir.path, p.basename(uri.path)));
        await tmp.writeAsBytes(response.bodyBytes);
        filePath = tmp.path;
      } else {
        setState(() {
          _isLoading = false;
          _localPath = null;
        });
        return;
      }
    }

    setState(() {
      _localPath = filePath;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: theme.colorTheme.generic.transparent,
      body: Column(
        children: [
          const BackNavigationHelpHeaderWidget(
            showBackNavigation: true,
            showHelp: false,
          ),
          if (_isLoading) ...[
            const Expanded(child: Center(child: CircularProgressIndicator()))
          ] else if (_localPath == null) ...[
            const Expanded(
                child: Center(child: Text('Failed to load document')))
          ] else ...[
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text('Page ${_currentPage + 1} of $_pages'),
            ),
            Expanded(
              child: PDFView(
                filePath: _localPath,
                enableSwipe: true,
                swipeHorizontal: false,
                autoSpacing: true,
                pageFling: true,
                fitPolicy: FitPolicy.WIDTH,
                onRender: (pages) {
                  setState(() => _pages = pages ?? 0);
                },
                onViewCreated: (controller) {},
                onPageChanged: (page, _) {
                  setState(() => _currentPage = page ?? 0);
                },
                onError: (err) {
                  AppLogger.instance.info(err, title: "PDF error: ");
                },
                onPageError: (page, err) {
                  AppLogger.instance.info('PDF page $page error: $err');
                },
              ),
            ),
          ],
        ],
      ),
    );
  }
}
