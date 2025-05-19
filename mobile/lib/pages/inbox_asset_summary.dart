import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../router/app_router.dart';
import '../widgets/cards/element_asset_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/drawer.dart';
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
      drawer: const CustomDrawer(),
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer:
            DigitCard(margin: const EdgeInsets.only(top: spacer2), children: [
          DigitButton(
            mainAxisSize: MainAxisSize.max,
            label: "Add more details",
            type: DigitButtonType.primary,
            size: DigitButtonSize.large,
            onPressed: () {
              context
                  .read<AssetTypeBloc>()
                  .add(const AssetTypeEvent.typeSelected("inverter"));
              context.router.push(const AssetSummaryRoute());
            },
          ),
          DigitButton(
            mainAxisSize: MainAxisSize.max,
            label: "Send Back",
            type: DigitButtonType.secondary,
            size: DigitButtonSize.large,
            onPressed: () => showCustomPopup(
              context: context,
              builder: (ctx) => Popup(
                onCrossTap: () {
                  Navigator.of(ctx).pop();
                },
                title: "Are you sure you want send to back the report?",
                description:
                    "If you send back the report now, you cannot add any more rejection reasons or add more details to the report until it is sent back from the field",
                onOutsideTap: () {
                  Navigator.of(ctx).pop();
                },
                type: PopUpType.simple,
                actionAlignment: MainAxisAlignment.center,
                actions: [],
                additionalWidgets: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Expanded(
                        flex: 1,
                        child: DigitButton(
                          label: "Close",
                          onPressed: () {
                            Navigator.of(ctx).pop();
                          },
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.large,
                          mainAxisSize: MainAxisSize.min,
                        ),
                      ),
                      const SizedBox(width: spacer5),
                      Expanded(
                        flex: 1,
                        child: DigitButton(
                          label: "Send back",
                          onPressed: () {
                            Navigator.of(ctx).pop();
                            context.read<AssetTypeBloc>().add(
                                const AssetTypeEvent.typeSelected("inverter"));
                            context.router.push(const AssetSummaryRoute());
                          },
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.large,
                          mainAxisSize: MainAxisSize.min,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ]),
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
                // DigitCard(children: [
                //   Text(
                //     'Installation Completion Report',
                //     style: textTheme.headingM
                //         .copyWith(color: theme.colorTheme.primary.primary2),
                //   ),
                //   Text(
                //     'Please scan and upload the installation completion report',
                //     style: textTheme.bodyS
                //         .copyWith(color: theme.colorTheme.text.secondary),
                //   ),
                //   FileUploadWidget(
                //     showPreview: true,
                //     allowMultiples: false,
                //     label: 'Upload',
                //     onFilesSelected: (files) {
                //       return {};
                //     },
                //   ),
                // ])
              ],
            ),
          )
        ],
      ),
    );
  }
}
