import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/panel_cards.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../router/app_router.dart';

@RoutePage()
class AssessmentSubmissionSuccessPage extends StatelessWidget {
  final String schemaName;

  const AssessmentSubmissionSuccessPage({
    super.key,
    required this.schemaName,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        children: [
          Padding(
            padding: const EdgeInsets.all(spacer2),
            child: PanelCard(
              animate: true,
              repeat: true,
              type: PanelType.success,
              title: 'Assessment Submitted Successfully',
              description:
                  'The assessment has been completed successfully on this device.',
              actions: [
                DigitButton(
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                  label: 'Home',
                  onPressed: () {
                    context
                        .read<FormsBloc>()
                        .add(FormsEvent.clearForm(schemaKey: schemaName));
                    context.router.replaceAll([const AssessmentHomeRoute()]);
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
