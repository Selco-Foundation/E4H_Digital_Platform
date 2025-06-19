import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/model/project_workflow/project_workflow.dart';

// Import your generated mapper and model
import '../blocs/selected_project/selected_project.dart';
import '../data/secure_storage/secureStore.dart';
import '../router/app_router.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class DraftPage extends StatefulWidget {
  const DraftPage({super.key});

  @override
  State<DraftPage> createState() => _DraftPageState();
}

class _DraftPageState extends State<DraftPage> {
  final SecureStore storage = SecureStore();
  Future<List<ProjectWorkflow>>? _draftsFuture;

  @override
  void initState() {
    super.initState();
    _draftsFuture = storage.getDraftProjects();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer: FooterButton(
          showSuffixIcon: false,
          text: 'Sync',
          onPress: () {
            // Implement your “sync” logic here
            context.router.replace(const SubmittedSaveSuccessRoute());
          },
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
                  'Unsynced Reports',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),

                // ── FUTURE BUILDER TO DISPLAY ONE CARD PER DRAFT ─────────────────
                FutureBuilder<List<ProjectWorkflow>>(
                  future: _draftsFuture,
                  builder: (context, snapshot) {
                    if (snapshot.connectionState == ConnectionState.waiting) {
                      // While loading from secure storage, show a spinner
                      return const Center(child: CircularProgressIndicator());
                    }

                    if (snapshot.hasError) {
                      // If something went wrong, show an error message
                      return Center(
                        child: Text(
                          'Failed to load drafts.',
                          style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.alert.error,
                          ),
                        ),
                      );
                    }

                    final drafts = snapshot.data ?? <ProjectWorkflow>[];

                    if (drafts.isEmpty) {
                      // No drafts found
                      return Center(
                        child: Text(
                          'No unsynced reports found.',
                          style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                        ),
                      );
                    }

                    return Column(
                      children: drafts.map((project) {
                        return Column(
                          children: [
                            InboxReportCard(
                              onPress: () {
                                context
                                    .read<SelectedProjectBloc>()
                                    .add(SelectedProjectEvent.select(project));
                                //context.router.push(const AssetSummaryRoute());
                                context.router
                                    .push(const OverallAssetSummaryRoute());
                              },
                              title: project.project.name ?? "",
                              // Use project's startDateTime if available; otherwise default
                              dateAssigned: project.project.startDateTime ??
                                  DateTime.now(),
                              status: project.state ?? '---',
                            ),
                            const SizedBox(height: spacer6),
                          ],
                        );
                      }).toList(),
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
