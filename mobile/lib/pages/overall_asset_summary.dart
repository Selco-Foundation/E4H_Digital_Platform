import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/upload_drag.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';
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
        body: ScrollableContent(
          enableFixedDigitButton: true,
          backgroundColor: theme.colorTheme.generic.background,
          header: const BackNavigationHelpHeaderWidget(
            showBackNavigation: true,
            showHelp: false,
          ),
          footer: FooterButton(
            isDisabled: true,
            showSuffixIcon: false,
            text: i18.common.coreCommonSubmit,
            onPress: () {
              // context.router.push(const AssetSummaryRoute());
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
                      const ElementAssetSummary(type: 'Battery', count: 2),
                      const ElementAssetSummary(type: 'Inverter', count: 0),
                      const ElementAssetSummary(type: 'Panel', count: 0),
                      DigitButton(
                          mainAxisSize: MainAxisSize.max,
                          label: 'Add More Assets',
                          prefixIcon: Icons.add_box,
                          onPressed: () {},
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.medium),
                    ],
                  ),
                  const SizedBox(
                    height: spacer4,
                  ),
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

class ElementAssetSummary extends StatelessWidget {
  final String type;
  final int count;

  const ElementAssetSummary({
    super.key,
    required this.type,
    required this.count,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.start,
          children: [
            Flexible(
              child: Text(
                'Total batteries\ninstalled',
                style: textTheme.headingS,
              ),
            ),
            const SizedBox(
              width: spacer6 * 3,
            ),
            Text(
              '$count',
              style: textTheme.bodyL,
            ),
          ],
        ),
        const SizedBox(height: spacer4),
        if (count > 0)
          DigitButton(
            mainAxisSize: MainAxisSize.max,
            label: 'View $type Summary',
            type: DigitButtonType.secondary,
            size: DigitButtonSize.medium,
            onPressed: () {
              // context.router.replace(const EnterOtpRoute());
            },
          ),
        const SizedBox(height: spacer2),
        const DigitDivider(dividerType: DividerType.small),
      ],
    );
  }
}
