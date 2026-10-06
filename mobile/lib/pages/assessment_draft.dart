import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/auth/authbloc.dart';
import '../data/nosql/cache_assessment_draft.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_mode.dart';
import '../model/assessment/assessment_queue.dart';
import '../repositories/assessment_draft_repo.dart';
import '../repositories/assessment_form_repo.dart';
import '../router/app_router.dart';
import '../utils/constants.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/role_login_resolver.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/assessment_draft_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentDraftPage extends StatefulWidget {
  const AssessmentDraftPage({super.key});

  @override
  State<AssessmentDraftPage> createState() => _AssessmentDraftPageState();
}

class _AssessmentDraftPageState extends State<AssessmentDraftPage> {
  int _selectedTabIndex = 0;
  bool _loading = true;
  bool _syncing = false;
  String? _error;
  List<CacheAssessmentDraft> _drafts = const [];

  String get _assessorId => context.read<AuthBloc>().state.maybeWhen(
        authenticated: (_, __, user) => user?.uuid ?? user?.userName ?? '',
        orElse: () => '',
      );

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<AssessmentDraftRepository> _draftRepository() async =>
      AssessmentDraftRepository(await Constants().isar);

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final repository = await _draftRepository();
      final drafts = await repository.listDrafts(_assessorId);
      if (mounted) {
        setState(() {
          _drafts = drafts;
          _loading = false;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _loading = false;
          _error = i18.assessmentDraft.loadFailed;
        });
      }
    }
  }

  Future<void> _syncAll() async {
    if (_syncing) return;
    final pending = _drafts
        .where((draft) => draft.status == AssessmentDraftStatus.pending)
        .toList(growable: false);
    if (pending.isEmpty) return;
    setState(() {
      _syncing = true;
      _error = null;
    });
    final drafts = await _draftRepository();
    final forms = AssessmentFormRepository();
    var failed = 0;
    for (final draft in pending) {
      try {
        final request = drafts.requestOf(draft);
        await forms.submitAssessment(request);
        await drafts.delete(
          request.tenantId,
          request.planFacilityId,
          request.assessmentPhase,
        );
      } on AssessmentApiException catch (error) {
        failed++;
        if (error.isConflict || error.isAuthorizationFailure) {
          await drafts.markBlocked(draft, error.message);
        } else {
          await drafts.markPendingFailure(draft, error.message);
        }
      } catch (error) {
        failed++;
        await drafts.markPendingFailure(draft, error.toString());
      }
    }
    await _load();
    if (!mounted) return;
    setState(() {
      _syncing = false;
      _error = failed == 0
          ? context.translate(i18.assessmentDraft.syncComplete)
          : context.translate(i18.assessmentDraft.syncPartial);
    });
  }

  Future<void> _openDraft(CacheAssessmentDraft draft) async {
    try {
      final repository = await _draftRepository();
      final request = repository.requestOf(draft);
      final facilityDefaults = repository.facilityDefaultsOf(draft);
      final mode = request.assessmentPhase == AssessmentPhase.PHONE
          ? AssessmentMode.remote
          : AssessmentMode.onSite;
      final facility = AssessmentQueueFacility(
        planFacilityId: request.planFacilityId,
        facilityName: draft.facilityName,
        facilityCategory: request.facilityCategory,
        facilityType: draft.facilityType,
        state: draft.state,
        district: draft.district,
        block: draft.block,
      );
      if (!mounted) return;
      await context.router.push<void>(
        AssessmentDynamicFormRoute(
          facility: facility,
          assessmentMode: mode,
          draftRequest: request,
          draftFacilityDefaults: facilityDefaults,
          onSubmissionSucceeded: () => _load(),
        ),
      );
      if (mounted) await _load();
    } catch (_) {
      if (mounted) {
        setState(() => _error = i18.assessmentDraft.loadFailed);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final roleCodes = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, userRequest) =>
              userRequest?.roles.map((role) => role.code).toSet() ?? const {},
          orElse: () => const <String?>{},
        );
    final hasRemote = roleCodes.contains(assessorRoleCode);
    final hasOnSite = roleCodes.contains(fieldPocRoleCode);
    final showTabs = hasRemote && hasOnSite;
    final selectedMode = showTabs
        ? AssessmentMode.values[_selectedTabIndex]
        : hasOnSite && !hasRemote
            ? AssessmentMode.onSite
            : AssessmentMode.remote;
    final selectedPhase = selectedMode == AssessmentMode.remote
        ? AssessmentPhase.PHONE
        : AssessmentPhase.FIELD;
    final visibleDrafts = _drafts
        .where(
            (draft) => AssessmentPhase.fromCode(draft.phase) == selectedPhase)
        .toList(growable: false);
    final hasPending = visibleDrafts.any(
      (draft) => draft.status == AssessmentDraftStatus.pending,
    );

    return Scaffold(
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer: FooterButton(
          text: _syncing
              ? context.translate(i18.assessmentDraft.syncing)
              : context.translate(i18.assessmentDraft.sync),
          showSuffixIcon: false,
          isDisabled: _syncing || !hasPending,
          onPress: _syncAll,
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
                const SizedBox(width: double.infinity),
                Text(
                  context.translate(i18.assessmentDraft.title),
                  style: textTheme.headingXl.copyWith(
                    color: theme.colorTheme.primary.primary2,
                  ),
                ),
                const SizedBox(height: spacer4),
                if (showTabs) ...[
                  SizedBox(
                    height: spacer12 + spacer1,
                    child: LayoutBuilder(
                      builder: (context, constraints) => DigitTabBar(
                        tabs: [
                          context.translate(i18.assessmentDraft.remote),
                          context.translate(i18.assessmentDraft.onSite),
                        ],
                        initialIndex: _selectedTabIndex,
                        onTabSelected: (index) =>
                            setState(() => _selectedTabIndex = index),
                        tabBarThemeData:
                            DigitTabBarThemeData.defaultTheme(context).copyWith(
                          tabWidth: constraints.maxWidth / 2,
                          padding: EdgeInsets.zero,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: spacer4),
                ],
                if (_loading)
                  const Center(child: CircularProgressIndicator())
                else if (visibleDrafts.isEmpty)
                  Center(
                    child: Text(
                      context.translate(i18.assessmentDraft.empty),
                      style: textTheme.bodyL.copyWith(
                        color: theme.colorTheme.text.primary,
                      ),
                    ),
                  )
                else
                  ...visibleDrafts.map(
                    (draft) => Padding(
                      padding: const EdgeInsets.only(bottom: spacer3),
                      child: AssessmentDraftCard(
                        onPressed: () => _openDraft(draft),
                        facilityName: draft.facilityName,
                        facilityType: draft.facilityType,
                        status: draft.status,
                        state: draft.state,
                        district: draft.district,
                        block: draft.block,
                        failureReason: draft.lastError,
                      ),
                    ),
                  ),
                if (_error != null) ...[
                  const SizedBox(height: spacer2),
                  SizedBox(
                    width: double.infinity,
                    child: Text(
                      context.translate(_error!),
                      textAlign: TextAlign.center,
                      style: textTheme.bodyS,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}
