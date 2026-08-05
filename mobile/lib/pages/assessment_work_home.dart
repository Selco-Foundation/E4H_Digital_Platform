import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/auth/authbloc.dart';
import '../model/assessment/assessment_mode.dart';
import '../router/app_router.dart';
import '../utils/role_login_resolver.dart';
import '../widgets/cards/report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentWorkHomePage extends StatelessWidget {
  const AssessmentWorkHomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final roleCodes = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, userRequest) =>
              userRequest?.roles.map((role) => role.code).toSet() ?? const {},
          orElse: () => const <String?>{},
        );
    final hasRemoteAssessment = roleCodes.contains(assessorRoleCode);
    final hasOnSiteAssessment = roleCodes.contains(fieldPocRoleCode);

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
              horizontal: spacer4,
              vertical: spacer1,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Assessment',
                  textAlign: TextAlign.start,
                  style: textTheme.headingXl.copyWith(
                    color: const DigitColors().light.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                if (hasRemoteAssessment)
                  ReportCard(
                    onPress: () => context.router.push(
                      AssessmentSelectFacilityRoute(
                        assessmentMode: AssessmentMode.remote,
                      ),
                    ),
                    icon: Icons.phone_in_talk_outlined,
                    heading: 'New Remote Assessment',
                    description:
                        'View assigned facilities and resume remote assessments in progress.',
                  ),
                if (hasOnSiteAssessment)
                  ReportCard(
                    onPress: () => context.router.push(
                      AssessmentSelectFacilityRoute(
                        assessmentMode: AssessmentMode.onSite,
                      ),
                    ),
                    icon: Icons.location_on_outlined,
                    heading: 'New On-site Assessment',
                    description:
                        'View assigned facilities and resume on-site assessments in progress.',
                  ),
                ReportCard(
                  onPress: () => context.router.push(
                    const AssessmentDraftRoute(),
                  ),
                  icon: Icons.pending_actions,
                  heading: 'Drafts',
                  description:
                      'View completed assessments waiting to sync or retry.',
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
