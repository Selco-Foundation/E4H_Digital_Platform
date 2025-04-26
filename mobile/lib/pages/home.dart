import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/extensions.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/home/home_item_card.dart';
import 'package:selco/widgets/navigation/navbar.dart';

@RoutePage()
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
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
        'onPressed': () {},
      },
    ];

    return Scaffold(
      backgroundColor: DigitTheme.instance.colorScheme.surface,
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
              child: Column(
                children: [
                  InfoCard(
                    title: context.translate(i18.dashboard.dataSyncPending),
                    type: InfoType.warning,
                    description: 'There are 90 records yet to be synced',
                  ),
                  const SizedBox(height: spacer3),
                  InfoCard(
                    title: context.translate(i18.dashboard.facilityAssigned),
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
