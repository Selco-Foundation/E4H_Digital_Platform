import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_drag.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class InboxAssetSummaryPage extends StatefulWidget {
  const InboxAssetSummaryPage({super.key});

  @override
  State<InboxAssetSummaryPage> createState() => _InboxAssetSummaryPageState();
}

class _InboxAssetSummaryPageState extends State<InboxAssetSummaryPage> {
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
          showSuffixIcon: false,
          text: i18.common.coreCommonSubmit,
          onPress: () {
            context
                .read<AssetTypeBloc>()
                .add(const AssetTypeEvent.typeSelected("inverter"));
            context.router.push(const AssetSummaryRoute());
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
                  'Summary Overview',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),
                const DigitCard(
                  children: [
                    ElementAssetSummary(
                        type: 'Battery', count: 2, text: 'batteries'),
                    ElementAssetSummary(
                        type: 'Inverter', count: 2, text: 'inverters'),
                    ElementAssetSummary(
                        type: 'Panel',
                        count: 2,
                        text: 'panels',
                        lastCard: true),
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
      ),
    );
  }
}
