import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';

@RoutePage()
class AmcHomePage extends StatefulWidget {
  const AmcHomePage({super.key});

  @override
  State<AmcHomePage> createState() => _AmcHomePageState();
}

class _AmcHomePageState extends State<AmcHomePage> {
  late String _userType;
  late String pendingRecords = "0";
  late String assignedFacility = "0";

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final screenWidth = context.width;

    final List<Map<String, dynamic>> _homeItems = [
      {
        'icon': Icons.text_snippet_outlined,
        'label': 'AMC Report',
        'onPressed': () => context.router.push(const AmcReportHomeRoute())
      },
      {
        'icon': Icons.autorenew,
        'label': 'Data Sync',
        'onPressed': () => context.router.push(const AmcDraftRoute()),
      },
    ];

    return Scaffold(
      backgroundColor: DigitTheme.instance.colorScheme.surface,
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: spacer2),
        child: ScrollableContent(
          backgroundColor: theme.colorTheme.generic.background,
          header: const BackNavigationHelpHeaderWidget(
            showBackNavigation: false,
            showHelp: true,
          ),
          footer: const PoweredByDigit(version: ''),
          slivers: [
            SliverPadding(
              padding: const EdgeInsets.only(top: spacer6),
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
                  childAspectRatio:
                      (screenWidth / 2) / (170 * (screenWidth / 375)),
                ),
              ),
            ),
          ],
          children: [],
        ),
      ),
    );
  }
}
