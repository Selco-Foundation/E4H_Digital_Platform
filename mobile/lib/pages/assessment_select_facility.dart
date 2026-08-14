import 'dart:async';

import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/atoms/digit_search_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/auth/authbloc.dart';
import '../blocs/assessment_queue/assessment_queue.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_mode.dart';
import '../model/assessment/assessment_queue.dart';
import '../repositories/assessment_draft_repo.dart';
import '../repositories/assessment_form_repo.dart';
import '../repositories/assessment_queue_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/cards/assessment_facility_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentSelectFacilityPage extends StatelessWidget {
  final AssessmentMode assessmentMode;

  const AssessmentSelectFacilityPage({
    super.key,
    required this.assessmentMode,
  });

  @override
  Widget build(BuildContext context) {
    final assessorId = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, user) => user?.uuid ?? user?.userName ?? '',
          orElse: () => '',
        );
    final isar = context.read<ActivityFacilityBloc>().isar;
    return BlocProvider(
      create: (_) => AssessmentQueueBloc(
        repository: AssessmentQueueRepository(),
        draftRepository: AssessmentDraftRepository(isar),
        assessmentMode: assessmentMode,
        assessorId: assessorId,
      )..add(const AssessmentQueueLoadInitial()),
      child: AssessmentSelectFacilityView(assessmentMode: assessmentMode),
    );
  }
}

class AssessmentSelectFacilityView extends StatefulWidget {
  final AssessmentMode assessmentMode;

  const AssessmentSelectFacilityView({
    super.key,
    required this.assessmentMode,
  });

  @override
  State<AssessmentSelectFacilityView> createState() =>
      _AssessmentSelectFacilityViewState();
}

class _AssessmentSelectFacilityViewState
    extends State<AssessmentSelectFacilityView> {
  static const _searchDebounce = Duration(milliseconds: 300);
  static const _scrollThreshold = 200.0;

  Timer? _searchTimer;
  final AssessmentFormRepository _assessmentFormRepository =
      AssessmentFormRepository();
  String _searchQuery = '';
  String _sortOrder = 'DESC';

  @override
  void dispose() {
    _searchTimer?.cancel();
    super.dispose();
  }

  void _onSearchChanged(String value) {
    _searchTimer?.cancel();
    _searchQuery = value.trim();
    if (_searchQuery.isNotEmpty &&
        _searchQuery.length < minFacilitySearchQueryLength) {
      return;
    }
    _searchTimer = Timer(_searchDebounce, _loadInitial);
  }

  void _loadInitial() {
    if (!mounted) return;
    context.read<AssessmentQueueBloc>().add(
          AssessmentQueueLoadInitial(
            query: _searchQuery,
            sortOrder: _sortOrder,
          ),
        );
  }

  Future<void> _refresh() async {
    final bloc = context.read<AssessmentQueueBloc>();
    _requestRefresh(bloc);
    await bloc.stream.firstWhere(
      (state) =>
          state is AssessmentQueueLoaded || state is AssessmentQueueFailure,
    );
  }

  void _requestRefresh([AssessmentQueueBloc? bloc]) {
    (bloc ?? context.read<AssessmentQueueBloc>()).add(
      AssessmentQueueRefresh(
        query: _searchQuery,
        sortOrder: _sortOrder,
      ),
    );
  }

  void _loadMore() {
    context.read<AssessmentQueueBloc>().add(
          AssessmentQueueLoadMore(
            query: _searchQuery,
            sortOrder: _sortOrder,
          ),
        );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      backgroundColor: theme.colorTheme.generic.background,
      body: Column(
        children: [
          const BackNavigationHelpHeaderWidget(
            showBackNavigation: true,
            showHelp: false,
          ),
          Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: spacer4,
              vertical: spacer2,
            ),
            child: _buildSearchAndSort(textTheme, theme),
          ),
          const SizedBox(height: spacer2),
          Expanded(
            child: NotificationListener<ScrollNotification>(
              onNotification: (notification) {
                if (notification is ScrollUpdateNotification &&
                    notification.metrics.maxScrollExtent > 0 &&
                    notification.metrics.pixels >= 0 &&
                    notification.metrics.pixels >
                        notification.metrics.maxScrollExtent -
                            _scrollThreshold) {
                  _loadMore();
                }
                return false;
              },
              child: RefreshIndicator(
                onRefresh: _refresh,
                child: BlocBuilder<AssessmentQueueBloc, AssessmentQueueState>(
                  builder: (context, state) =>
                      _buildQueueState(state, textTheme, theme),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchAndSort(DigitTextTheme textTheme, ThemeData theme) {
    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              context.translate(i18.assessmentSelectFacility.title),
              style: textTheme.bodyL.copyWith(
                color: theme.colorTheme.text.primary,
              ),
            ),
            const SizedBox(height: spacer1),
            Row(
              children: [
                Expanded(
                  child: DigitSearchFormInput(
                    suffixIcon: Icons.search,
                    onChange: _onSearchChanged,
                  ),
                ),
                const SizedBox(width: spacer2),
                GestureDetector(
                  key: const ValueKey('assessment-queue-sort'),
                  onTap: () => _showSortPopup(textTheme, theme),
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
    );
  }

  Widget _buildQueueState(
    AssessmentQueueState state,
    DigitTextTheme textTheme,
    ThemeData theme,
  ) {
    if (state is AssessmentQueueInitial || state is AssessmentQueueLoading) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: const [
          Padding(
            padding: EdgeInsets.only(top: spacer8),
            child: Center(child: CircularProgressIndicator()),
          ),
        ],
      );
    }

    if (state is AssessmentQueueFailure) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.symmetric(horizontal: spacer4),
        children: [
          const SizedBox(height: spacer4),
          Center(
            child: Column(
              children: [
                Text(
                  context.translate(i18.assessmentSelectFacility.failedToLoad),
                  style: textTheme.bodyS.copyWith(
                    color: theme.colorTheme.alert.error,
                  ),
                ),
                const SizedBox(height: spacer2),
                DigitButton(
                  label: context.translate(i18.common.retry),
                  onPressed: _loadInitial,
                  type: DigitButtonType.secondary,
                  size: DigitButtonSize.medium,
                ),
              ],
            ),
          ),
        ],
      );
    }

    final loaded = state as AssessmentQueueLoaded;
    if (loaded.facilities.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.symmetric(horizontal: spacer4),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(vertical: spacer4),
            child: Center(
              child: Text(
                context.translate(
                  i18.assessmentSelectFacility.noFacilitiesFound,
                ),
                style: textTheme.bodyS.copyWith(
                  color: theme.colorTheme.text.secondary,
                ),
              ),
            ),
          ),
        ],
      );
    }

    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(
        horizontal: spacer4,
        vertical: spacer2,
      ),
      children: [
        for (final facility in loaded.facilities) ...[
          AssessmentFacilityCard(
            key: ValueKey(
              facility.planFacilityId ??
                  facility.facilityId ??
                  facility.hashCode,
            ),
            facilityName: _displayValue(facility.facilityName),
            status: _statusLabel(facility),
            state: _displayValue(facility.state),
            district: _displayValue(facility.district),
            block: _displayValue(facility.block),
            isRemoteAssessor: widget.assessmentMode == AssessmentMode.remote,
            onStartAssessment: () => _startAssessment(facility),
            onUpdateStatus: (reason) =>
                _updateUnableToContactStatus(facility, reason),
          ),
          const SizedBox(height: spacer5),
        ],
        if (loaded.isLoadingMore)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: spacer2),
            child: Center(
              child: CircularProgressIndicator(),
            ),
          ),
        if (loaded.loadMoreError != null)
          Padding(
            padding: const EdgeInsets.only(bottom: spacer2),
            child: TextButton(
              onPressed: _loadMore,
              child: Text(
                context.translate(
                  i18.assessmentSelectFacility.loadMoreFailedRetry,
                ),
              ),
            ),
          ),
      ],
    );
  }

  Future<void> _startAssessment(AssessmentQueueFacility facility) async {
    await context.router.push<void>(
      AssessmentDynamicFormRoute(
        facility: facility,
        assessmentMode: widget.assessmentMode,
        onSubmissionSucceeded: _requestRefresh,
      ),
    );
    if (mounted) _requestRefresh();
  }

  Future<bool> _updateUnableToContactStatus(
    AssessmentQueueFacility facility,
    AssessmentUnableToContactReason reason,
  ) async {
    final planFacilityId = facility.planFacilityId?.trim();
    if (planFacilityId == null || planFacilityId.isEmpty) {
      _showStatusUpdateMessage(
        i18.assessmentSelectFacility.statusUpdateFailed,
      );
      return false;
    }
    try {
      await _assessmentFormRepository.markPhoneUnableToContact(
        planFacilityId: planFacilityId,
        reason: reason,
      );
      if (!mounted) return true;
      _showStatusUpdateMessage(
        i18.assessmentSelectFacility.statusUpdateSuccess,
      );
      _requestRefresh();
      return true;
    } catch (_) {
      if (mounted) {
        _showStatusUpdateMessage(
          i18.assessmentSelectFacility.statusUpdateFailed,
        );
      }
      return false;
    }
  }

  void _showStatusUpdateMessage(String localizationKey) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(context.translate(localizationKey))),
    );
  }

  void _showSortPopup(DigitTextTheme textTheme, ThemeData theme) {
    var pendingSortOrder = _sortOrder;
    showCustomPopup(
      context: context,
      builder: (popupContext) => StatefulBuilder(
        builder: (popupContext, popupSetState) => Popup(
          onCrossTap: () => Navigator.of(popupContext).pop(),
          title: context.translate(i18.common.sortBy),
          type: PopUpType.simple,
          actionAlignment: MainAxisAlignment.center,
          additionalWidgets: [
            Text(
              context.translate(i18.assessmentSelectFacility.lastActionTime),
              style: textTheme.headingS.copyWith(
                color: theme.colorTheme.text.primary,
              ),
            ),
            RadioList(
              groupValue: pendingSortOrder,
              containerPadding: const EdgeInsets.symmetric(
                horizontal: 0,
                vertical: spacer2,
              ),
              onChanged: (value) => popupSetState(
                () => pendingSortOrder = value.code,
              ),
              radioDigitButtons: [
                RadioButtonModel(
                  code: 'DESC',
                  name: context.translate(i18.common.newestFirst),
                ),
                RadioButtonModel(
                  code: 'ASC',
                  name: context.translate(i18.common.oldestFirst),
                ),
              ],
            ),
            Row(
              children: [
                Expanded(
                  child: DigitButton(
                    label: context.translate(i18.common.clear),
                    onPressed: () {
                      setState(() => _sortOrder = 'DESC');
                      Navigator.of(popupContext).pop();
                      _loadInitial();
                    },
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                    mainAxisSize: MainAxisSize.min,
                  ),
                ),
                const SizedBox(width: spacer5),
                Expanded(
                  child: DigitButton(
                    label: context.translate(i18.common.sort),
                    onPressed: () {
                      setState(() => _sortOrder = pendingSortOrder);
                      Navigator.of(popupContext).pop();
                      _loadInitial();
                    },
                    type: DigitButtonType.primary,
                    size: DigitButtonSize.large,
                    mainAxisSize: MainAxisSize.min,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _statusLabel(AssessmentQueueFacility facility) {
    final raw = widget.assessmentMode == AssessmentMode.remote
        ? facility.phoneStatus
        : facility.fieldStatus;
    final status = raw?.trim();
    if (status == null || status.isEmpty) return '---';
    switch (ASSESSMENT_STATUS.fromCode(status)) {
      case ASSESSMENT_STATUS.PENDING:
        return context.translate(
          i18.assessmentSelectFacility.statusScheduled,
        );
      case ASSESSMENT_STATUS.PENDING_NO_ANSWER:
        return context.translate(
          i18.assessmentSelectFacility.statusNoAnswer,
        );
      case ASSESSMENT_STATUS.PENDING_WRONG_NUMBER:
        return context.translate(
          i18.assessmentSelectFacility.statusWrongNumber,
        );
      case ASSESSMENT_STATUS.QUALIFIED:
        return context.translate(
          i18.assessmentSelectFacility.statusQualified,
        );
      case ASSESSMENT_STATUS.NOT_QUALIFIED:
        return context.translate(
          i18.assessmentSelectFacility.statusNotQualified,
        );
      case null:
        return context.translate(status);
    }
  }

  String _displayValue(String? value) {
    final normalized = value?.trim();
    return normalized == null || normalized.isEmpty ? '---' : normalized;
  }
}
