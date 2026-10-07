import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/scheduled_visit/scheduled_visit.dart';
import '../blocs/selected_amc_origin/selected_amc_origin.dart';
import '../blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../router/app_router.dart';
import '../widgets/bookmarks/report_bookmarks.dart';
import '../widgets/facility_list_controls.dart';
import '../utils/facility_list_filter.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/progress_indicator/loading_indicator.dart';

@RoutePage()
class AmcDraftPage extends StatefulWidget {
  const AmcDraftPage({super.key});

  @override
  State<AmcDraftPage> createState() => _AmcDraftPageState();
}

class _AmcDraftPageState extends State<AmcDraftPage>
    with ReportBookmarksState<AmcDraftPage, ScheduledVisit> {
  String _searchQuery = '';
  String? _filter;
  int _visibleCount = 10;
  int _bookmarkCriteriaGeneration = 0;
  bool _loadingEligibleBookmarks = false;
  List<ScheduledVisit> _eligibleBookmarks = [];

  List<ScheduledVisit> _matchingBookmarks() =>
      filterFacilityList(_eligibleBookmarks,
          query: _searchQuery,
          filter: 'BOOKMARKED',
          id: (record) => record.id ?? '',
          name: (record) => record.facility?.facilityName,
          date: (record) => record.scheduledDate ?? DateTime(1970),
          bookmarkedIds: bookmarkedItems.map(bookmarks.identity).toList());

  Future<void> _reloadBookmarks() async {
    final generation = ++_bookmarkCriteriaGeneration;
    final statuses = _statusesForTab(_selectedTabIndex);
    await loadBookmarks(only: true);
    if (!mounted || generation != _bookmarkCriteriaGeneration) return;
    if (_filter != 'BOOKMARKED') return;
    setState(() => _loadingEligibleBookmarks = true);
    try {
      final records = await context
          .read<ScheduledVisitBloc>()
          .repository
          .readEligibleCache(statuses);
      if (mounted && generation == _bookmarkCriteriaGeneration) {
        setState(() {
          _eligibleBookmarks = records;
          _loadingEligibleBookmarks = false;
          _visibleCount = 10;
        });
      }
    } catch (_) {
      if (mounted && generation == _bookmarkCriteriaGeneration) {
        setState(() {
          bookmarksFailed = true;
          _loadingEligibleBookmarks = false;
        });
      }
    }
  }

  Future<void> _refreshVisits() async {
    if (_filter == 'BOOKMARKED') {
      await _reloadBookmarks();
      return;
    }
    final bloc = context.read<ScheduledVisitBloc>();
    final loaded = bloc.stream.firstWhere((state) => state.maybeWhen(
        loaded: (_, __, ___, ____, _____) => true,
        failure: (_) => true,
        orElse: () => false));
    _fetchVisits(_selectedTabIndex);
    await loaded;
    if (mounted) await _reloadBookmarks();
  }

  Future<void> _openVisit(ScheduledVisit visit) async {
    context
        .read<SelectedScheduledVisitBloc>()
        .add(SelectedScheduledVisitEvent.select(visit));
    if (_selectedTabIndex == 0 &&
        visit.status != WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name) {
      await context.router.push(const AmcOtpRoute());
    } else {
      final origin = _selectedTabIndex == 0
          ? FormOrigin.overallSummary
          : FormOrigin.submitted;
      context
          .read<SelectedAmcOriginBloc>()
          .add(SelectedAmcOriginEvent.select(origin));
      await context.router.push(AmcDynamicFormRoute(
          pageName: 'AMC_Report',
          uniqueIdentifier: 'AssetForm.AMC_SCHEDULED_MAINTENANCE',
          schemaName: 'AssetForm.AMC_SCHEDULED_MAINTENANCE',
          scheduledVisit: visit,
          origin: origin));
    }
    if (mounted) {
      await _reloadBookmarks();
      if (mounted) _fetchVisits(_selectedTabIndex);
    }
  }

  int _selectedTabIndex = 0;
  String otpText = "otp";

  List<String> _statusesForTab(int tabIndex) {
    if (tabIndex == 0) {
      return [WORKFLOW_STATUS_AMC_FIELD_STAFF.PENDING_OTP_APPROVAL.name];
    } else {
      return [WORKFLOW_STATUS_AMC_FIELD_STAFF.PENDING_APPROVAL.name];
    }
  }

  void _fetchVisits(int tabIndex) {
    _visibleCount = 10;
    if (_filter == 'BOOKMARKED') {
      _reloadBookmarks();
      return;
    }
    final statuses = _statusesForTab(tabIndex);
    context.read<ScheduledVisitBloc>().add(
          ScheduledVisitEvent.loadInitial(
              statuses: statuses,
              query: _searchQuery.trim(),
              sortDirection: _filter),
        );
  }

  void _onTabChanged(int index) {
    setState(() {
      _selectedTabIndex = index;
    });
    _fetchVisits(index);
  }

  @override
  void initState() {
    super.initState();
    bookmarks = reportBookmarksFor<ScheduledVisit>(context, amc: true);
    bookmarkSaveFailedKey = i18.amcBookmarks.saveFailed;
    _reloadBookmarks();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _fetchVisits(_selectedTabIndex);
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final tabs = [
      context.translate(i18.amcDraft.pendingOtpApproval),
      context.translate(i18.amcDraft.pendingApproval),
    ];

    return NotificationListener<ScrollNotification>(
      onNotification: (notification) {
        if (notification is ScrollUpdateNotification &&
            notification.depth == 0) {
          final max = notification.metrics.maxScrollExtent;
          final current = notification.metrics.pixels;

          if (current > max - 200) {
            if (_filter == 'BOOKMARKED') {
              final total = _matchingBookmarks().length;
              if (_visibleCount < total) {
                setState(
                    () => _visibleCount = (_visibleCount + 10).clamp(0, total));
              }
              return false;
            }
            final bloc = context.read<ScheduledVisitBloc>();
            bloc.state.maybeWhen(
              loaded: (items, hasMore, totalCount, fromCache, isLoadingMore) {
                if (hasMore && !isLoadingMore) {
                  bloc.add(ScheduledVisitEvent.loadMore(
                      statuses: _statusesForTab(_selectedTabIndex),
                      query: _searchQuery.trim(),
                      sortDirection: _filter));
                }
              },
              orElse: () {},
            );
          }
        }
        return false;
      },
      child: Scaffold(
        body: RefreshableFacilityList(
            onRefresh: _refreshVisits,
            child: ScrollableContent(
              enableFixedDigitButton: true,
              backgroundColor: theme.colorTheme.generic.background,
              header: const BackNavigationHelpHeaderWidget(
                showBackNavigation: true,
                showHelp: false,
              ),
              footer: const SizedBox.shrink(),
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
                        context.translate(i18.amcDraft.pendingApproval),
                        style: textTheme.headingXl.copyWith(
                          color: theme.colorTheme.primary.primary2,
                        ),
                      ),
                      const SizedBox(height: spacer4),
                      SizedBox(
                        height: spacer12 + spacer1,
                        child: LayoutBuilder(
                          builder: (context, constraints) {
                            return DigitTabBar(
                              tabs: tabs,
                              initialIndex: _selectedTabIndex,
                              onTabSelected: (index) => _onTabChanged(index),
                              tabBarThemeData:
                                  DigitTabBarThemeData.defaultTheme(context)
                                      .copyWith(
                                          tabWidth: constraints.maxWidth /
                                              tabs.length,
                                          padding: EdgeInsets.zero),
                            );
                          },
                        ),
                      ),
                      const SizedBox(height: spacer4),
                      FacilityListControls(
                          titleKey: i18.amcSelectFacility.title,
                          filter: _filter,
                          bookmarkLabelKey: i18.amcBookmarks.filter,
                          onSearch: (query) {
                            setState(() => _searchQuery = query);
                            _fetchVisits(_selectedTabIndex);
                          },
                          onFilter: (filter) {
                            setState(() => _filter = filter);
                            _fetchVisits(_selectedTabIndex);
                          }),
                      const SizedBox(height: spacer4),
                      if (bookmarksFailed) bookmarkLoadError(_reloadBookmarks),
                      if (_filter == 'BOOKMARKED')
                        (bookmarksLoading || _loadingEligibleBookmarks)
                            ? loadingIndicator()
                            : _buildVisitList(_matchingBookmarks()
                                .take(_visibleCount)
                                .toList())
                      else
                        BlocBuilder<ScheduledVisitBloc, ScheduledVisitState>(
                          builder: (context, visitState) {
                            return visitState.maybeWhen(
                              initial: () => loadingIndicator(),
                              loading: () => loadingIndicator(),
                              failure: (message) => Center(
                                child: Padding(
                                  padding: const EdgeInsets.only(top: spacer4),
                                  child: Text(message),
                                ),
                              ),
                              loaded: (items, hasMore, totalCount, fromCache,
                                  isLoadingMore) {
                                return _buildVisitList(
                                  items,
                                  isLoadingMore: isLoadingMore,
                                );
                              },
                              orElse: () => const SizedBox.shrink(),
                            );
                          },
                        ),
                    ],
                  ),
                ),
              ],
            )),
      ),
    );
  }

  Widget _buildVisitList(
    List<ScheduledVisit> items, {
    bool isLoadingMore = false,
  }) {
    if (items.isEmpty) {
      return Center(
        child: Text(context.translate(_searchQuery.trim().isNotEmpty
            ? i18.common.noMatchingFacilitiesFound
            : i18.amcDraft.noDraftsToDisplay)),
      );
    }

    return Column(
      children: [
        for (final visit in items)
          Column(
            children: [
              Builder(builder: (context) {
                final locality =
                    parseBoundaryCodeLocality(visit.facility?.boundaryCode);
                if (_selectedTabIndex == 0) {
                  return InboxReportCard(
                      isBookmarked: bookmarkIds.contains(visit.id),
                      isSavingBookmark: !bookmarksLoaded ||
                          savingBookmarks.contains(visit.id),
                      onToggleBookmark: (visit.id ?? '').isEmpty
                          ? null
                          : () =>
                              toggleBookmark(visit, reload: _reloadBookmarks),
                      onPress: () => _openVisit(visit),
                      title: visit.facility?.facilityName ?? '',
                      visitNumber: visit.visitNumber,
                      durationMonths: visit.amcConfiguration?.durationMonths,
                      visitFrequencyMonths:
                          visit.amcConfiguration?.visitFrequencyMonths,
                      dateAssigned: visit.scheduledDate ?? DateTime.now(),
                      status: visit.status!.isNotEmpty
                          ? (visit.status!.toLowerCase().contains(otpText)
                              ? context.translate(i18
                                  .amcDraft.amcDraftPendingCompletionApproval)
                              : visit.status)
                          : '---',
                      state: locality.state,
                      district: locality.district,
                      block: locality.block,
                      isAmc: true,
                      isOtp: true);
                }

                return InboxReportCard(
                  isBookmarked: bookmarkIds.contains(visit.id),
                  isSavingBookmark:
                      !bookmarksLoaded || savingBookmarks.contains(visit.id),
                  onToggleBookmark: (visit.id ?? '').isEmpty
                      ? null
                      : () => toggleBookmark(visit, reload: _reloadBookmarks),
                  onPress: () => _openVisit(visit),
                  title: visit.facility?.facilityName ?? '',
                  visitNumber: visit.visitNumber,
                  durationMonths: visit.amcConfiguration?.durationMonths,
                  visitFrequencyMonths:
                      visit.amcConfiguration?.visitFrequencyMonths,
                  dateAssigned: visit.scheduledDate ?? DateTime.now(),
                  status: visit.status ?? '---',
                  state: locality.state,
                  district: locality.district,
                  block: locality.block,
                  isAmc: true,
                );
              }),
              const SizedBox(height: spacer4),
            ],
          ),
        if (isLoadingMore)
          const Padding(
            padding: EdgeInsets.only(bottom: spacer4),
            child: Center(
              child: CircularProgressIndicator(),
            ),
          ),
      ],
    );
  }
}
