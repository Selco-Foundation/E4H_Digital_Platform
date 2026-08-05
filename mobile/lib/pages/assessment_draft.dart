import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/auth/authbloc.dart';
import '../model/assessment/assessment_mode.dart';
import '../router/app_router.dart';
import '../utils/role_login_resolver.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentDraftPage extends StatefulWidget {
  const AssessmentDraftPage({super.key});

  @override
  State<AssessmentDraftPage> createState() => _AssessmentDraftPageState();
}

class _AssessmentDraftPageState extends State<AssessmentDraftPage> {
  int _selectedTabIndex = 0;

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
    final showModeTabs = hasRemoteAssessment && hasOnSiteAssessment;
    final selectedMode = showModeTabs
        ? AssessmentMode.values[_selectedTabIndex]
        : hasOnSiteAssessment
            ? AssessmentMode.onSite
            : AssessmentMode.remote;

    return Scaffold(
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer: FooterButton(
          text: 'Sync',
          showSuffixIcon: false,
          isDisabled: true,
          onPress: () {},
        ),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
              vertical: spacer4,
              horizontal: spacer4,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Drafts',
                  style: textTheme.headingXl.copyWith(
                    color: theme.colorTheme.primary.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                if (showModeTabs) ...[
                  SizedBox(
                    height: spacer12 + spacer1,
                    child: LayoutBuilder(
                      builder: (context, constraints) {
                        return DigitTabBar(
                          tabs: const ['Remote', 'On-site'],
                          initialIndex: _selectedTabIndex,
                          onTabSelected: (index) {
                            setState(() => _selectedTabIndex = index);
                          },
                          tabBarThemeData:
                              DigitTabBarThemeData.defaultTheme(context)
                                  .copyWith(
                            tabWidth: constraints.maxWidth / 2,
                            padding: EdgeInsets.zero,
                          ),
                        );
                      },
                    ),
                  ),
                  const SizedBox(height: spacer4),
                ],
                KeyedSubtree(
                  key: ValueKey('assessment-drafts-${selectedMode.name}'),
                  child: Center(
                    child: Text(
                      'No drafts to display',
                      style: textTheme.bodyL.copyWith(
                        color: theme.colorTheme.text.primary,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
