import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _showPopup(context));
  }

  void _showPopup(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    showCustomPopup(
      context: context,
      builder: (ctx) => Popup(
        type: PopUpType.alert,
        onCrossTap: () => Navigator.of(ctx).pop(),
        title: "Data not synced!",
        // description:
        //     "Your data has not been synced since 28/01/2025. Sync now!",
        onOutsideTap: () => Navigator.of(ctx).pop(),
        actionAlignment: MainAxisAlignment.center,
        actions: [],
        additionalWidgets: [
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text("Your data has not been synced since 28/01/2025. Sync now!",
                  textAlign: TextAlign.center,
                  style: textTheme.bodyL.copyWith(
                      color: const Light().textPrimary,
                      fontWeight: FontWeight.w600)),
            ],
          ),
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Expanded(
                flex: 1,
                child: DigitButton(
                  label: "Skip",
                  onPressed: () => Navigator.of(ctx).pop(),
                  type: DigitButtonType.secondary,
                  size: DigitButtonSize.large,
                  mainAxisSize: MainAxisSize.min,
                ),
              ),
              const SizedBox(width: spacer5),
              Expanded(
                flex: 1,
                child: DigitButton(
                  label: "Sync Data",
                  onPressed: () {
                    Navigator.of(ctx).pop();

                    ///context.router.push(const AssetSummaryRoute());
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
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final screenWidth = context.width;

    final List<Map<String, dynamic>> _homeItems = [
      {
        'icon': Icons.text_snippet_outlined,
        'label': 'Installation Report',
        'onPressed': () => context.router.push(const InstallationReportRoute()),
      },
      {
        'icon': Icons.autorenew,
        'label': 'Data Sync',
        'onPressed': () => context.router.push(const DownloadStatusRoute()),
      },
    ];

    return Scaffold(
      backgroundColor: DigitTheme.instance.colorScheme.surface,
      drawer: const CustomDrawer(),
      appBar: const Navbar(),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: spacer2),
        child: ScrollableContent(
          backgroundColor: theme.colorTheme.generic.background,
          header: const BackNavigationHelpHeaderWidget(
            showBackNavigation: false,
            showHelp: true,
          ),
          footer: const PoweredByDigit(
            version: '',
          ),
          slivers: [
            SliverPadding(
              padding: const EdgeInsets.all(0).copyWith(top: spacer6),
              sliver: SliverGrid(
                delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    final item = _homeItems[index];
                    return HomeItemCard(
                      icon: item['icon'],
                      label: item['label'],
                      onPressed: item['onPressed'],
                    );
                  },
                  childCount: _homeItems.length,
                ),
                gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  mainAxisSpacing: spacer4,
                  childAspectRatio: _calculateAspectRatio(screenWidth),
                ),
              ),
            ),
          ],
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: spacer2)
                  .copyWith(top: spacer2),
              child: const Column(
                children: [
                  InfoCard(
                    title: "Data Sync Pending!",
                    type: InfoType.warning,
                    description: 'There are 90 records yet to be synced',
                  ),
                  SizedBox(height: spacer3),
                  InfoCard(
                    title: "Facilities assigned",
                    type: InfoType.info,
                    description:
                        '10 more facilities have been assigned to you.',
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  double _calculateAspectRatio(double screenWidth) {
    const baseWidth = 375; // Design reference width
    const baseHeight = 170; // Design reference height
    return (screenWidth / 2) / (baseHeight * (screenWidth / baseWidth));
  }
}
