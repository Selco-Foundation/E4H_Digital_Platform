import 'package:badges/badges.dart' as badges;
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class InstallationReportPage extends StatefulWidget {
  const InstallationReportPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return _InstallationReportPageState();
  }
}

class _InstallationReportPageState extends State<InstallationReportPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
      body: ScrollableContent(
        footer: const PoweredByDigit(version: ''),
        backgroundColor: theme.colorTheme.generic.background,
        children: [
          const BackNavigationHelpHeaderWidget(
            showHelp: true,
            showBackNavigation: true,
          ),
          const SizedBox(height: spacer3),
          Padding(
            padding: const EdgeInsets.symmetric(
                horizontal: spacer4, vertical: spacer1),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Installation Report',
                  textAlign: TextAlign.start,
                  style: textTheme.headingXl.copyWith(
                    color: const DigitColors().light.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                ReportCard(
                  onPress: () =>
                      context.router.push(const SelectHealthFacilityRoute()),
                  icon: Icons.add_box_outlined,
                  heading: 'New Report',
                  description:
                      'View list of assigned health facilities, search for health facility and create installation report',
                ),
                ReportCard(
                  onPress: () {},
                  icon: Icons.toc,
                  heading: 'Inbox',
                  description: 'View reports that have been approved/rejected',
                ),
                ReportCard(
                  onPress: () {},
                  icon: Icons.assignment_late,
                  heading: 'Submitted Reports',
                  description: 'View reports that have been submitted',
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}

class ReportCard extends StatelessWidget {
  final IconData icon;
  final String heading;
  final String description;
  final Function() onPress;

  const ReportCard({
    super.key,
    required this.icon,
    required this.heading,
    required this.description,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return SizedBox(
      height: 3 * spacer11,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: spacer2),
        child: DigitCard(
          onPressed: onPress,
          margin: const EdgeInsets.only(bottom: spacer1),
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      icon,
                      color: const DigitColors().light.alertError,
                      size: spacer8,
                    ),
                    const SizedBox(width: spacer2),
                    Text(
                      heading,
                      style: textTheme.headingL.copyWith(
                        color: const DigitColors().light.primary2,
                      ),
                    ),
                    const Spacer(),
                    badges.Badge(
                      badgeStyle: badges.BadgeStyle(
                        shape: badges.BadgeShape.square,
                        badgeColor: theme.colorTheme.alert.error,
                        padding: const EdgeInsets.symmetric(
                            horizontal: spacer3, vertical: spacer1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      badgeContent: Text('05',
                          style: textTheme.bodyS
                              .copyWith(color: theme.colorTheme.paper.primary)),
                    ),
                  ],
                ),
                const SizedBox(height: spacer3),
                Text(
                  description,
                  style: textTheme.bodyS
                      .copyWith(color: const DigitColors().light.textPrimary),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
