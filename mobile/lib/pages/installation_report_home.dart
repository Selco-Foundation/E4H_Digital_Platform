import 'package:auto_route/annotations.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/navigation/navbar.dart';

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
      backgroundColor: DigitTheme.instance.colorScheme.surface,
      appBar: const Navbar(),
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: true,
        ),
        footer: const Padding(
          padding: EdgeInsets.only(bottom: spacer2),
          child: PoweredByDigit(
            version: '',
          ),
        ),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: spacer4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: spacer6),
                Text(
                  'Installation Report',
                  textAlign: TextAlign.start,
                  style: textTheme.headingXl.copyWith(
                    color: const DigitColors().light.primary2,
                  ),
                ),
                const SizedBox(height: spacer6),
                const ReportCard(
                  icon: Icons.note_add,
                  heading: 'New Report',
                  description:
                      'View list of assigned health facilities, search for health facility and create installation report',
                ),
                const ReportCard(
                  icon: Icons.menu,
                  heading: 'Inbox',
                  description: 'View reports that have been approved/rejected',
                ),
                const ReportCard(
                  icon: Icons.assignment_turned_in,
                  heading: 'Submitted Reports',
                  description: 'View reports that have been submitted',
                ),
                const SizedBox(height: spacer4),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class ReportCard extends StatelessWidget {
  final IconData icon;
  final String heading;
  final String description;

  const ReportCard({
    super.key,
    required this.icon,
    required this.heading,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return SizedBox(
      height: 3 * spacer11,
      child: DigitCard(
        onPressed: () {},
        margin: const EdgeInsets.only(bottom: spacer4),
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(icon, color: const DigitColors().light.alertError),
                  const SizedBox(width: 8),
                  Text(
                    heading,
                    style: textTheme.headingM.copyWith(
                      color: const DigitColors().light.primary2,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                description,
                style: textTheme.bodyS
                    .copyWith(color: const DigitColors().light.textSecondary),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
