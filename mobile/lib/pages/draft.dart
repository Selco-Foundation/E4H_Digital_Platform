import 'dart:async';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/asset_submission/asset_submission.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../widgets/bookmarks/report_bookmarks.dart';
import '../widgets/facility_list_controls.dart';
import '../utils/facility_list_filter.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import 'sync_loading.dart';

@RoutePage()
class DraftPage extends StatefulWidget {
  const DraftPage({super.key});

  @override
  State<DraftPage> createState() => _DraftPageState();
}

class _DraftPageState extends State<DraftPage>
    with ReportBookmarksState<DraftPage, ActivityFacilityWorkflow> {
  String _searchQuery = '';
  String? _filter;
  Timer? _searchDebounce;
  bool _fetchingMore = false;

  List<ActivityFacilityWorkflow> _matchingDrafts(
          List<ActivityFacilityWorkflow> drafts) =>
      filterFacilityList([
        ...drafts,
        if (_filter == 'BOOKMARKED')
          ...bookmarkedItems.where(
            (item) =>
                _draftStatusesForUserType().contains(item.status) &&
                !drafts.any(
                  (draft) =>
                      draft.activityFacility.id == item.activityFacility.id,
                ),
          ),
      ],
          query: _searchQuery,
          filter: _filter,
          id: (record) => record.activityFacility.id,
          name: (record) => record.activityFacility.facility?.facilityName,
          date: (record) =>
              record.activityFacility.scheduledAt ?? DateTime(1970),
          bookmarkedIds: bookmarkIds.toList());

  Future<void> _reloadBookmarks() async {
    await loadBookmarks(only: true);
    if (mounted) {
      setState(() {
        _visibleCount = _pageSize;
        _isLoadingMore = false;
      });
    }
  }

  void _fetchDrafts({bool loadMore = false}) {
    _searchDebounce?.cancel();
    context.read<ActivityFacilityBloc>().add(
          ActivityFacilityEvent.loadUnSubmitted(
            _draftStatusesForUserType(),
            userType,
            query: _searchQuery.trim(),
            sortDirection: _filter == 'ASC' ? 'ASC' : 'DESC',
            loadMore: loadMore,
          ),
        );
  }

  Future<void> _refreshDrafts() async {
    final bloc = context.read<ActivityFacilityBloc>();
    final loaded = bloc.stream.firstWhere((state) =>
        state is ActivityFacilityListFailure ||
        state.maybeWhen(
            unSubmittedLoaded: (_, __, ___, ____) => true,
            orElse: () => false));
    _fetchDrafts();
    await loaded;
    if (mounted) await _reloadBookmarks();
  }

  void _changeCriteria(
      {String? query, String? filter, bool changeFilter = false}) {
    _searchDebounce?.cancel();
    setState(() {
      if (query != null) _searchQuery = query;
      if (changeFilter) _filter = filter;
      _visibleCount = _pageSize;
      _isLoadingMore = false;
      _fetchingMore = false;
    });
    if (query != null) {
      _searchDebounce = Timer(const Duration(milliseconds: 350), () {
        if (mounted) _fetchDrafts();
      });
    } else {
      _fetchDrafts();
    }
  }

  @override
  void dispose() {
    _searchDebounce?.cancel();
    super.dispose();
  }

  static const _pageSize = 10;
  static const _scrollThreshold = 200.0;

  late String userType = "";
  Route? _syncRoute;
  int _visibleCount = _pageSize;
  bool _isLoadingMore = false;

  List<String> _draftStatusesForUserType() {
    return userType == USER_TYPES.FIELD_STAFF.name
        ? [WORKFLOW_STATUS_FIELD_STAFF.SUBMITTED_BY_FIELD_STAFF.name]
        : [
            WORKFLOW_STATUS_FIELD_SUPERVISOR.SUBMITTED_BY_SUPERVISOR.name,
            WORKFLOW_STATUS_FIELD_SUPERVISOR
                .PENDING_APPROVAL_FLAGGED_FOR_QC.name,
          ];
  }

  @override
  void initState() {
    super.initState();
    bookmarks =
        reportBookmarksFor<ActivityFacilityWorkflow>(context, amc: false);
    bookmarkSaveFailedKey = i18.installationBookmarks.saveFailed;
    _reloadBookmarks();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
      _fetchDrafts();
    });
  }

  void _showSyncDialog(BuildContext context, {String? error}) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    showCustomPopup(
      context: context,
      builder: (ctx) => Popup(
        type: PopUpType.alert,
        onCrossTap: () => Navigator.of(ctx).pop(),
        onOutsideTap: () => Navigator.of(ctx).pop(),
        title: context.translate(i18.draft.syncFailed),
        actionAlignment: MainAxisAlignment.center,
        actions: const [],
        additionalWidgets: [
          Text(
            error == null
                ? context.translate(i18.draft.somethingWentWrong)
                : context.localizeError(error),
            textAlign: TextAlign.center,
            style: textTheme.bodyL.copyWith(
              color: theme.colorTheme.text.primary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  void _handleAssetSubmissionState(
      BuildContext context, AssetSubmissionState state) {
    state.whenOrNull(
      bulkProgress: (_) {
        if (_syncRoute == null) {
          _syncRoute = MaterialPageRoute(
            fullscreenDialog: true,
            builder: (_) => const SyncLoadingPage(),
          );
          Navigator.of(context).push(_syncRoute!);
        }
      },
      bulkFailure: (errorMessage) {
        if (_syncRoute != null) {
          Navigator.of(context).pop();
          _syncRoute = null;
        }
        if (isSessionExpiredMessage(errorMessage)) {
          handleSessionExpired(context);
          return;
        }
        _showSyncDialog(context, error: errorMessage);
      },
      success: () {
        if (_syncRoute != null) {
          Navigator.of(context).pop();
          _syncRoute = null;
        }
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(context.translate(i18.draft.allDraftsSynced))),
        );
        _reloadBookmarks();
        _fetchDrafts();
      },
    );
  }

  void _handleDraftState(ActivityFacilityState state) {
    state.maybeWhen(
      unSubmittedLoaded: (drafts, hasMore, isLoadingMore, loadFailed) {
        setState(() {
          if (!isLoadingMore && !_fetchingMore) _visibleCount = _pageSize;
          if (!isLoadingMore) _fetchingMore = false;
          _isLoadingMore = isLoadingMore;
        });
      },
      listFailure: () => setState(() {
        _fetchingMore = false;
        _isLoadingMore = false;
      }),
      orElse: () {},
    );
  }

  void _loadMoreDrafts() {
    if (_isLoadingMore || (_searchDebounce?.isActive ?? false)) return;
    final bloc = context.read<ActivityFacilityBloc>();
    bloc.state.maybeWhen(
      unSubmittedLoaded: (drafts, hasMore, isLoadingMore, loadFailed) {
        final matches = _matchingDrafts(drafts);
        if (_visibleCount < matches.length) {
          setState(
            () => _visibleCount =
                (_visibleCount + _pageSize).clamp(0, matches.length).toInt(),
          );
        } else if (hasMore) {
          setState(() {
            _isLoadingMore = true;
            _fetchingMore = true;
            _visibleCount += _pageSize;
          });
          _fetchDrafts(loadMore: true);
        }
      },
      orElse: () {},
    );
  }

  Widget _retryDrafts({bool loadMore = false}) => TextButton(
        onPressed: loadMore ? _loadMoreDrafts : _fetchDrafts,
        child: Text(context.translate(i18.common.retry)),
      );

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return MultiBlocListener(
      listeners: [
        BlocListener<AssetSubmissionBloc, AssetSubmissionState>(
          listener: _handleAssetSubmissionState,
        ),
        BlocListener<ActivityFacilityBloc, ActivityFacilityState>(
          listener: (context, state) => _handleDraftState(state),
        ),
      ],
      child: NotificationListener<ScrollNotification>(
        onNotification: (notification) {
          if (notification is ScrollUpdateNotification &&
              notification.depth == 0) {
            final max = notification.metrics.maxScrollExtent;
            final current = notification.metrics.pixels;
            if (current > max - _scrollThreshold) {
              _loadMoreDrafts();
            }
          }
          return false;
        },
        child: Scaffold(
          body: RefreshableFacilityList(
              onRefresh: _refreshDrafts,
              child: ScrollableContent(
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                footer: FooterButton(
                  showSuffixIcon: false,
                  text: context.translate(i18.draft.sync),
                  onPress: () {
                    context.read<AssetSubmissionBloc>().add(
                          AssetSubmissionEvent.submitAllDrafts(
                              userType: userType),
                        );
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
                          context.translate(i18.draft.submittedReports),
                          style: textTheme.headingXl.copyWith(
                            color: theme.colorTheme.primary.primary2,
                          ),
                        ),
                        const SizedBox(height: spacer4),
                        FacilityListControls(
                            titleKey: i18.selectHealthFacility.title,
                            filter: _filter,
                            bookmarkLabelKey: i18.installationBookmarks.filter,
                            onSearch: (query) => _changeCriteria(query: query),
                            onFilter: (filter) => _changeCriteria(
                                filter: filter, changeFilter: true)),
                        const SizedBox(height: spacer4),
                        if (bookmarksFailed)
                          bookmarkLoadError(_reloadBookmarks),
                        BlocBuilder<ActivityFacilityBloc,
                            ActivityFacilityState>(
                          builder: (context, state) {
                            return state.maybeWhen(
                              unSubmittedLoaded: (
                                drafts,
                                hasMore,
                                isLoadingMore,
                                loadFailed,
                              ) {
                                final matches = _matchingDrafts(drafts);
                                if (matches.isEmpty) {
                                  return Column(
                                    children: [
                                      Center(
                                        child: Text(
                                          context.translate(
                                              _searchQuery.trim().isNotEmpty
                                                  ? i18.common
                                                      .noMatchingFacilitiesFound
                                                  : i18.draft
                                                      .noUnsyncedReportsFound),
                                          style: textTheme.bodyL.copyWith(
                                            color:
                                                theme.colorTheme.text.primary,
                                          ),
                                        ),
                                      ),
                                      if (hasMore)
                                        TextButton(
                                          onPressed: _isLoadingMore
                                              ? null
                                              : _loadMoreDrafts,
                                          child: _isLoadingMore
                                              ? const CircularProgressIndicator()
                                              : Text(
                                                  context.translate(
                                                    i18.common.retry,
                                                  ),
                                                ),
                                        ),
                                    ],
                                  );
                                }

                                final visibleDrafts =
                                    matches.take(_visibleCount);
                                return Column(
                                  children: [
                                    ...visibleDrafts.map((project) {
                                      final locality =
                                          parseBoundaryCodeLocality(
                                        project.activityFacility.facility
                                            ?.boundaryCode,
                                      );
                                      return Column(
                                        children: [
                                          InboxReportCard(
                                            isRectification:
                                                isRectificationSite(project),
                                            isBookmarked: bookmarkIds.contains(
                                                project.activityFacility.id),
                                            isSavingBookmark:
                                                !bookmarksLoaded ||
                                                    savingBookmarks.contains(
                                                        project.activityFacility
                                                            .id),
                                            onToggleBookmark: project
                                                    .activityFacility.id.isEmpty
                                                ? null
                                                : () => toggleBookmark(project,
                                                    reload: _reloadBookmarks),
                                            onPress: () async {
                                              context
                                                  .read<
                                                      SelectedActivityFacilityBloc>()
                                                  .add(
                                                      SelectedActivityFacilityEvent
                                                          .select(project));
                                              await context.router.push(
                                                  OverallAssetSummaryRoute(
                                                      refresh: DateTime.now()
                                                          .millisecondsSinceEpoch));
                                              if (mounted) {
                                                await _reloadBookmarks();
                                                if (!context.mounted) return;
                                                _fetchDrafts();
                                              }
                                            },
                                            title: project.activityFacility
                                                    .facility?.facilityName ??
                                                "",
                                            dateAssigned: project
                                                    .workflow
                                                    ?.auditDetails
                                                    ?.lastModifiedTime ??
                                                DateTime.now(),
                                            status: project.status ?? '---',
                                            state: locality.state,
                                            district: locality.district,
                                            block: locality.block,
                                          ),
                                          const SizedBox(height: spacer6),
                                        ],
                                      );
                                    }),
                                    if (loadFailed)
                                      _retryDrafts(loadMore: true),
                                    if (_isLoadingMore)
                                      const Padding(
                                        padding:
                                            EdgeInsets.only(bottom: spacer4),
                                        child: CircularProgressIndicator(),
                                      ),
                                  ],
                                );
                              },
                              listFailure: () => Center(child: _retryDrafts()),
                              loading: () => const Center(
                                  child: CircularProgressIndicator()),
                              initial: () => const Center(
                                child: Padding(
                                  padding: EdgeInsets.only(top: spacer4),
                                  child: CircularProgressIndicator(),
                                ),
                              ),
                              orElse: () => const Center(
                                child: Padding(
                                  padding: EdgeInsets.only(top: spacer4),
                                  child: CircularProgressIndicator(),
                                ),
                              ),
                            );
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              )),
        ),
      ),
    );
  }
}
