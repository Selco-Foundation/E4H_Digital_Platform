import 'dart:convert';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/selected_amc_origin/selected_amc_origin.dart';
import '../blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../model/comment/comment.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../pages/submit_for_approval.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcRejctionReasonsPage extends StatefulWidget {
  const AmcRejctionReasonsPage({super.key});

  @override
  State<AmcRejctionReasonsPage> createState() => _AmcRejctionReasonsPageState();
}

class _AmcRejctionReasonsPageState extends State<AmcRejctionReasonsPage> {
  ScheduledVisit? scheduledVisit;

  List<Comment> _extractRejectionComments(ScheduledVisit? visit) {
    if (visit == null) return const <Comment>[];

    final candidates = <String>[];

    void addFromRawJson(String? rawJson) {
      if (rawJson == null || rawJson.trim().isEmpty) return;
      try {
        final decoded = jsonDecode(rawJson);

        if (decoded is Map<String, dynamic>) {
          final commentField = decoded['comment'];
          if (commentField is String && commentField.trim().isNotEmpty) {
            candidates.add(commentField);
          }
          if (commentField is List) {
            candidates.add(jsonEncode(commentField));
          }
        }
      } catch (_) {}
    }

    addFromRawJson(visit.processInstances.first.rawJson);
    if (candidates.isEmpty) return const <Comment>[];
    final rawCommentJson = candidates.last;
    try {
      final decodedList = jsonDecode(rawCommentJson);

      if (decodedList is List) {
        return decodedList.whereType<Map<String, dynamic>>().map((m) {
          final reason = m['reason']?.toString();
          final detail = m['comment']?.toString();
          final jsonForComment = <String, dynamic>{};
          if (reason != null) jsonForComment['reason'] = reason;
          if (detail != null) jsonForComment['comment'] = detail;

          return Comment(commentMessage: jsonEncode(jsonForComment));
        }).toList();
      }
    } catch (_) {}
    return const <Comment>[];
  }

  @override
  void initState() {
    super.initState();

    context.read<SelectedScheduledVisitBloc>().state.whenOrNull(
        selected: (visit) {
      scheduledVisit = visit;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return ScrollableContent(
      enableFixedDigitButton: true,
      backgroundColor: theme.colorTheme.generic.background,
      header: const BackNavigationHelpHeaderWidget(
        showBackNavigation: true,
        showHelp: false,
      ),
      footer: FooterButton(
        isDisabled: false,
        showSuffixIcon: false,
        text: context.translate(i18.common.coreCommonNext),
        onPress: () {
          context.read<SelectedAmcOriginBloc>().add(
              const SelectedAmcOriginEvent.select(
                  FormOrigin.submitForApproval));
          context.router.push(
            AmcDynamicFormRoute(
              pageName: "AMC_Report",
              uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
              schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
              scheduledVisit: scheduledVisit!,
              origin: FormOrigin.submitForApproval,
            ),
          );
        },
      ),
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(
              vertical: spacer2, horizontal: spacer2),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: spacer4),
              Padding(
                padding: const EdgeInsets.only(left: spacer1),
                child: Text(
                  'Rejection Reasons',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
              ),
              DigitCard(
                margin: const EdgeInsets.symmetric(vertical: spacer4),
                children: [
                  SizedBox(width: context.width),
                  RejectionReasonsList(
                    comments: _extractRejectionComments(scheduledVisit),
                    excludeStandardTypes: false,
                  ),
                ],
              ),
            ],
          ),
        )
      ],
    );
  }
}
