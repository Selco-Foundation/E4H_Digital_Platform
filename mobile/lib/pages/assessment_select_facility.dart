import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_search_form_input.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../widgets/cards/assessment_facility_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentSelectFacilityPage extends StatelessWidget {
  const AssessmentSelectFacilityPage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: spacer4,
              vertical: spacer2,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: spacer2),
                DigitCard(
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: spacer1),
                        Row(
                          children: [
                            Expanded(
                              child: DigitSearchFormInput(
                                suffixIcon: Icons.search,
                                onChange: (_) {},
                              ),
                            ),
                            const SizedBox(width: spacer2),
                            GestureDetector(
                              onTap: () {},
                              child: Icon(
                                Icons.import_export,
                                color: theme.colorTheme.primary.primary1,
                                size: spacer8,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ],
                ),
                const SizedBox(height: spacer4),
                BlocBuilder<UserTypeBloc, UserTypeState>(
                  builder: (context, userTypeState) {
                    final isRemoteAssessor = userTypeState.maybeWhen(
                      assessor: () => true,
                      orElse: () => false,
                    );

                    return AssessmentFacilityCard(
                      facilityName: 'Digar Kashipur',
                      status: 'Scheduled',
                      state: 'Assam',
                      district: 'Cachar',
                      block: 'Cedharban',
                      isRemoteAssessor: isRemoteAssessor,
                      onStartAssessment: () {
                        const schemaName = 'Assessment.HF_PHONE';
                        context.read<FormsBloc>().add(
                              const FormsEvent.clearForm(
                                schemaKey: schemaName,
                              ),
                            );
                        context.router.push(
                          AssessmentDynamicFormRoute(
                            pageName: 'assessorFacilityDetails',
                            schemaName: schemaName,
                          ),
                        );
                      },
                      onUpdateStatus: () {},
                    );
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
