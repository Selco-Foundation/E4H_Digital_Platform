import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../blocs/app_init/app_init.dart';
import '../repositories/assessment_form_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';
import '../widgets/mdms/mdms_gate.dart';

@RoutePage()
class AssessmentHomePage extends StatefulWidget {
  const AssessmentHomePage({super.key});

  @override
  State<AssessmentHomePage> createState() => _AssessmentHomePageState();
}

class _AssessmentHomePageState extends State<AssessmentHomePage> {
  final AssessmentFormRepository _repository = AssessmentFormRepository();
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final screenWidth = context.width;

    final homeItems = [
      (
        icon: Icons.business_center_outlined,
        label: context.translate(i18.assessmentHome.assessment),
        onPressed: () => context.router.push(
              const AssessmentWorkHomeRoute(),
            ),
      ),
      (
        icon: Icons.autorenew,
        label: context.translate(i18.home.dataSync),
        onPressed: () => context.router.push(
              AssessmentDraftRoute(),
            ),
      ),
    ];

    return MdmsGate(
      module: MdmsModule.assessment,
      load: () => _repository.preloadMobileSchemas(forceRefresh: true),
      child: Scaffold(
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
                      final item = homeItems[index];
                      return HomeItemCard(
                        icon: item.icon,
                        label: item.label,
                        onPressed: item.onPressed,
                        labelPadding: const EdgeInsets.symmetric(
                          horizontal: spacer2,
                        ),
                        fitLabelOnOneLine: true,
                      );
                    },
                    childCount: homeItems.length,
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
            children: const [],
          ),
        ),
      ),
    );
  }
}
