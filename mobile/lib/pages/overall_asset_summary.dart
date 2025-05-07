import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_drag.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class OverallAssetSummaryPage extends StatefulWidget {
  const OverallAssetSummaryPage({super.key});

  @override
  State<OverallAssetSummaryPage> createState() =>
      _OverallAssetSummaryPageState();
}

class _OverallAssetSummaryPageState extends State<OverallAssetSummaryPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
        appBar: const Navbar(),
        drawer: const CustomDrawer(),
        body: ScrollableContent(
          enableFixedDigitButton: true,
          backgroundColor: theme.colorTheme.generic.background,
          header: const BackNavigationHelpHeaderWidget(
            showBackNavigation: true,
            showHelp: false,
          ),
          footer: FooterButton(
            // isDisabled: true,
            showSuffixIcon: false,
            text: i18.common.coreCommonSubmit,
            onPress: () {
              context.router.replace(const SubmittedSaveSuccessRoute());
            },
          ),
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(
                  vertical: spacer2, horizontal: spacer4),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Summary',
                    style: textTheme.headingXl
                        .copyWith(color: theme.colorTheme.primary.primary2),
                  ),
                  const SizedBox(height: spacer4),
                  DigitCard(
                    children: [
                      const ElementAssetSummary(
                          type: 'Battery', count: 2, text: 'batteries'),
                      const ElementAssetSummary(
                          type: 'Inverter', count: 0, text: 'inverters'),
                      const ElementAssetSummary(
                          type: 'Panel', count: 0, text: 'panels'),
                      DigitButton(
                          mainAxisSize: MainAxisSize.max,
                          label: 'Add More Assets',
                          prefixIcon: Icons.add_box,
                          onPressed: () {},
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.medium),
                    ],
                  ),
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
                    FileUploadWidget2(
                      // showPreview: true,
                      // allowMultiples: false,
                      label: 'Upload',
                      // downloadText: "Hello",
                      onFilesSelected: (files) {
                        return {};
                      },
                    ),
                    // FileUploadWidget(
                    //   showPreview: true,
                    //   allowMultiples: false,
                    //   label: 'Upload',
                    //   onFilesSelected: (files) {
                    //     return {};
                    //   },
                    // ),
                  ])
                ],
              ),
            )
          ],
        ));
  }
}
