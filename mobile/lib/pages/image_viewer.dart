import 'dart:io';

import 'package:auto_route/auto_route.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';

import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/images/cached_image.dart';

@RoutePage()
class ImageViewerPage extends StatelessWidget {
  final String path;
  const ImageViewerPage({required this.path, super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return Scaffold(
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        children: [
          const SizedBox(height: spacer10),
          Center(
            child: InteractiveViewer(
              maxScale: 5,
              child: isValidUuid(path)
                  ? CachedImage('$fileStoreFileUrl$path')
                  : Image.file(File(path)),
            ),
          )
        ],
      ),
    );
  }
}
