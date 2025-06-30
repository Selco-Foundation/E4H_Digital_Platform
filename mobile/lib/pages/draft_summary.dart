import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/view_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage() //todo to delete the page, no longer using it
class DraftSummaryPage extends StatefulWidget {
  const DraftSummaryPage({super.key});

  @override
  State<DraftSummaryPage> createState() => _DraftSummaryPageState();
}

class _DraftSummaryPageState extends State<DraftSummaryPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer: FooterButton(
          showSuffixIcon: false,
          text: "Submit",
          onPress: () {
            context.router.replace(const AssetSummaryRoute());
          },
        ),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer3, horizontal: spacer4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Summary',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),
                const ViewAssetSummary(text: 'Battery', count: 2),
                const SizedBox(height: spacer4),
                const ViewAssetSummary(text: 'Panel', count: 2),
                const SizedBox(height: spacer4),
                const ViewAssetSummary(text: 'Inverter', count: 2),
                const SizedBox(height: spacer4),
                DigitCard(children: [
                  Text(
                    'Installation Completion Report',
                    style: textTheme.headingM
                        .copyWith(color: theme.colorTheme.primary.primary2),
                  ),
                  Text(
                    'Please scan and upload the installation completion report',
                    style: textTheme.bodyS
                        .copyWith(color: theme.colorTheme.text.secondary),
                  ),
                  FileUploadWidget(
                    showPreview: true,
                    allowMultiples: false,
                    label: 'Upload',
                    onFilesSelected: (files) {
                      return {};
                    },
                  ),
                ]),
                const SizedBox(height: spacer4),
              ],
            ),
          )
        ],
      ),
    );
  }
}
