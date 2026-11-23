import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/report_type/report_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/cards/report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcReportHomePage extends StatefulWidget {
  const AmcReportHomePage({super.key});

  @override
  State<AmcReportHomePage> createState() => _AmcReportHomePageState();
}

class _AmcReportHomePageState extends State<AmcReportHomePage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
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
                  context.translate(i18.amcReportHome.installationReport),
                  textAlign: TextAlign.start,
                  style: textTheme.headingXl.copyWith(
                    color: const DigitColors().light.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                ReportCard(
                  onPress: () {
                    context
                        .read<ReportTypeBloc>()
                        .add(const ReportTypeEvent.typeSelected("new-report"));
                    context.router.push(const AmcSelectFacilityRoute());
                  },
                  icon: Icons.add_box_outlined,
                  heading: context.translate(i18.amcReportHome.newAmcReport),
                  description: context
                      .translate(i18.amcReportHome.newAmcReportDescription),
                ),
                ReportCard(
                  onPress: () {
                    context
                        .read<ReportTypeBloc>()
                        .add(const ReportTypeEvent.typeSelected("inbox"));
                    context.router.push(const AmcInboxRoute());
                  },
                  icon: Icons.toc,
                  heading: context.translate(i18.inbox.title),
                  description:
                      context.translate(i18.amcReportHome.inboxDescription),
                ),
                ReportCard(
                    onPress: () {
                      context
                          .read<ReportTypeBloc>()
                          .add(const ReportTypeEvent.typeSelected("submitted"));
                      context.router.push(const AmcDraftRoute());
                    },
                    icon: Icons.assignment_late,
                    heading: context.translate(i18.amcDraft.pendingApproval),
                    description: context.translate(
                        i18.amcReportHome.pendingApprovalDescription)),
              ],
            ),
          )
        ],
      ),
    );
  }
}
