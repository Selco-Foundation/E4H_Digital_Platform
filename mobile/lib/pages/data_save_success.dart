import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/panel_cards.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/utils/utils.dart';

import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/project/project.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../repositories/project_repo.dart';
import '../router/app_router.dart';

@RoutePage()
class DataSaveSuccessPage extends StatefulWidget {
  const DataSaveSuccessPage({super.key});

  @override
  State<DataSaveSuccessPage> createState() => _DataSaveSuccessPageState();
}

class _DataSaveSuccessPageState extends State<DataSaveSuccessPage> {
  bool rejectedReport = false;

  @override
  void initState() {
    super.initState();

    // Delay until after the first frame so that context.read<T>() is safe:
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final reportState = context.read<ReportTypeBloc>().state;
      final inboxState = context.read<InboxTypeBloc>().state;

      final isInboxReport =
          reportState.maybeWhen(inbox: () => true, orElse: () => false);

      final isRejectedReport =
          inboxState.maybeWhen(rejected: () => true, orElse: () => false);

      setState(() {
        rejectedReport = isInboxReport && isRejectedReport;
      });
    });
  }

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
              title: rejectedReport
                  ? 'Submitted for Approval'
                  : 'Data Saved Successfully',
              description: rejectedReport
                  ? 'The data has been recorded successfully. '
                  : 'The data has been saved successfully on your device. Please click submit to submit the report for approval on the health facility summary page.',
              actions: [
                DigitButton(
                    type: DigitButtonType.primary,
                    size: DigitButtonSize.large,
                    label: rejectedReport ? 'Back to Landing Page' : 'Next',
                    onPressed: () async {
                      final selected = context
                          .read<SelectedProjectBloc>()
                          .state
                          .whenOrNull(selected: (s) => s);
                      final projectId = selected?.project.id;
                      final userType =
                          context.read<UserTypeBloc>().state.maybeWhen(
                                supervisor: () => USER_TYPES.SUPERVISOR.name,
                                orElse: () => USER_TYPES.FIELD_STAFF.name,
                              );
                      if (projectId != null) {
                        final isar = context.read<ProjectBloc>().isar;
                        await PrefilledProjectRepository(isar).addOrTouch(
                          projectId: projectId,
                          userType: userType,
                        );
                      }
                      if (rejectedReport) {
                        context.router.replaceAll([const HomeRoute()]);
                      } else {
                        context.router.push(OverallAssetSummaryRoute(
                            refresh: DateTime.now().millisecondsSinceEpoch));
                      }
                    }),
              ],
            ),
          )
        ],
      ),
    );
  }
}
