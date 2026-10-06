import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/auth/authbloc.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_mode.dart';
import '../repositories/assessment_draft_repo.dart';
import '../repositories/assessment_queue_repo.dart';
import '../router/app_router.dart';
import '../utils/constants.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/role_login_resolver.dart';
import '../widgets/cards/report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

typedef AssessmentWorkCounts = ({int remote, int onSite});

Future<AssessmentWorkCounts> loadAssessmentWorkCounts({
  required AssessmentQueueRepository repository,
  required bool hasRemoteAssessment,
  required bool hasOnSiteAssessment,
}) async {
  final counts = await Future.wait<int>([
    if (hasRemoteAssessment)
      repository
          .count(assessmentMode: AssessmentMode.remote)
          .onError((_, __) => 0)
    else
      Future<int>.value(0),
    if (hasOnSiteAssessment)
      repository
          .count(assessmentMode: AssessmentMode.onSite)
          .onError((_, __) => 0)
    else
      Future<int>.value(0),
  ]);
  return (remote: counts[0], onSite: counts[1]);
}

@RoutePage()
class AssessmentWorkHomePage extends StatefulWidget {
  const AssessmentWorkHomePage({super.key});

  @override
  State<AssessmentWorkHomePage> createState() => _AssessmentWorkHomePageState();
}

class _AssessmentWorkHomePageState extends State<AssessmentWorkHomePage> {
  final AssessmentQueueRepository _repository = AssessmentQueueRepository();
  int _remoteCount = 0;
  int _onSiteCount = 0;
  int _draftCount = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _loadCounts());
  }

  Future<void> _loadCounts() async {
    if (!mounted) return;

    final roleCodes = _roleCodes(context);
    final hasRemoteAssessment = roleCodes.contains(assessorRoleCode);
    final hasOnSiteAssessment = roleCodes.contains(fieldPocRoleCode);
    final user = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, userRequest) => userRequest,
          orElse: () => null,
        );
    final assessorId = user?.uuid ?? user?.userName ?? '';
    final allowedPhases = <AssessmentPhase>{
      if (hasRemoteAssessment) AssessmentPhase.PHONE,
      if (hasOnSiteAssessment) AssessmentPhase.FIELD,
    };

    final queueCountsFuture = loadAssessmentWorkCounts(
      repository: _repository,
      hasRemoteAssessment: hasRemoteAssessment,
      hasOnSiteAssessment: hasOnSiteAssessment,
    );
    final draftCountFuture = _loadDraftCount(
      assessorId: assessorId,
      allowedPhases: allowedPhases,
    );
    final counts = await queueCountsFuture;
    final draftCount = await draftCountFuture;

    if (!mounted) return;
    setState(() {
      _remoteCount = counts.remote;
      _onSiteCount = counts.onSite;
      _draftCount = draftCount;
    });
  }

  Future<int> _loadDraftCount({
    required String assessorId,
    required Set<AssessmentPhase> allowedPhases,
  }) async {
    try {
      final repository = AssessmentDraftRepository(await Constants().isar);
      return await repository.countDrafts(
        assessorId: assessorId,
        phases: allowedPhases,
      );
    } catch (_) {
      return 0;
    }
  }

  Future<void> _openRemoteAssessments() async {
    await context.router.push(
      AssessmentSelectFacilityRoute(assessmentMode: AssessmentMode.remote),
    );
    await _loadCounts();
  }

  Future<void> _openOnSiteAssessments() async {
    await context.router.push(
      AssessmentSelectFacilityRoute(assessmentMode: AssessmentMode.onSite),
    );
    await _loadCounts();
  }

  Future<void> _openDrafts() async {
    await context.router.push(const AssessmentDraftRoute());
    await _loadCounts();
  }

  Set<String?> _roleCodes(BuildContext context) {
    return context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, userRequest) =>
              userRequest?.roles.map((role) => role.code).toSet() ?? const {},
          orElse: () => const <String?>{},
        );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final roleCodes = _roleCodes(context);
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
                  context.translate(i18.assessmentHome.assessment),
                  textAlign: TextAlign.start,
                  style: textTheme.headingXl.copyWith(
                    color: const DigitColors().light.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                AssessmentWorkCards(
                  hasRemoteAssessment: hasRemoteAssessment,
                  hasOnSiteAssessment: hasOnSiteAssessment,
                  remoteCount: _remoteCount,
                  onSiteCount: _onSiteCount,
                  draftCount: _draftCount,
                  onRemotePressed: _openRemoteAssessments,
                  onOnSitePressed: _openOnSiteAssessments,
                  onDraftsPressed: _openDrafts,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class AssessmentWorkCards extends StatelessWidget {
  final bool hasRemoteAssessment;
  final bool hasOnSiteAssessment;
  final int remoteCount;
  final int onSiteCount;
  final int draftCount;
  final VoidCallback onRemotePressed;
  final VoidCallback onOnSitePressed;
  final VoidCallback onDraftsPressed;

  const AssessmentWorkCards({
    super.key,
    required this.hasRemoteAssessment,
    required this.hasOnSiteAssessment,
    required this.remoteCount,
    required this.onSiteCount,
    required this.draftCount,
    required this.onRemotePressed,
    required this.onOnSitePressed,
    required this.onDraftsPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        if (hasRemoteAssessment)
          ReportCard(
            badgeCount: remoteCount,
            onPress: onRemotePressed,
            icon: Icons.phone_in_talk_outlined,
            heading: context.translate(i18.assessmentWorkHome.newRemote),
            description: context.translate(
              i18.assessmentWorkHome.newRemoteDescription,
            ),
          ),
        if (hasOnSiteAssessment)
          ReportCard(
            badgeCount: onSiteCount,
            onPress: onOnSitePressed,
            icon: Icons.location_on_outlined,
            heading: context.translate(i18.assessmentWorkHome.newOnSite),
            description: context.translate(
              i18.assessmentWorkHome.newOnSiteDescription,
            ),
          ),
        ReportCard(
          badgeCount: draftCount,
          onPress: onDraftsPressed,
          icon: Icons.pending_actions,
          heading: context.translate(i18.assessmentDraft.title),
          description: context.translate(
            i18.assessmentWorkHome.draftsDescription,
          ),
        ),
      ],
    );
  }
}
