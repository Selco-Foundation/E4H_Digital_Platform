import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/digit_radio_list.dart';
import 'package:digit_ui_components/widgets/atoms/digit_search_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';

import '../blocs/scheduled_visit/scheduled_visit.dart';
import '../blocs/selected_amc_origin/selected_amc_origin.dart';
import '../blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../repositories/dynamic_form_repo.dart';
import '../repositories/scheduled_visit_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/cards/report_detail_row.dart';

@RoutePage()
class AmcSelectFacilityPage extends StatefulWidget {
  const AmcSelectFacilityPage({super.key});

  @override
  State<AmcSelectFacilityPage> createState() => _AmcSelectFacilityPageState();
}

class _AmcSelectFacilityPageState extends State<AmcSelectFacilityPage> {
  String? _sortDirection;
  String _searchQuery = '';

  List<String> _statuses() {
    return [WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name];
  }

  @override
  void initState() {
    super.initState();
    _fetchVisits();
  }

  void _fetchVisits() {
    final statuses = _statuses();
    if (_searchQuery.isNotEmpty) {
      context.read<ScheduledVisitBloc>().add(
            ScheduledVisitEvent.loadInitial(
              statuses: statuses,
              query: _searchQuery,
            ),
          );
    } else if (_sortDirection != null) {
      context.read<ScheduledVisitBloc>().add(
            ScheduledVisitEvent.loadInitial(
              statuses: statuses,
              sortDirection: _sortDirection,
            ),
          );
    } else {
      context.read<ScheduledVisitBloc>().add(
            ScheduledVisitEvent.loadInitial(statuses: statuses),
          );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    final isar = context.read<ScheduledVisitBloc>().isar;
    const schemaKey = 'AssetForm.AMC_SCHEDULED_MAINTENANCE';

    return BlocBuilder<ScheduledVisitBloc, ScheduledVisitState>(
      builder: (context, state) {
        return NotificationListener<ScrollNotification>(
          onNotification: (notification) {
            if (notification is ScrollUpdateNotification) {
              final max = notification.metrics.maxScrollExtent;
              final current = notification.metrics.pixels;

              if (current > max - 200) {
                final bloc = context.read<ScheduledVisitBloc>();
                bloc.state.maybeWhen(
                  loaded:
                      (items, hasMore, totalCount, fromCache, isLoadingMore) {
                    if (hasMore && !isLoadingMore) {
                      bloc.add(
                        ScheduledVisitEvent.loadMore(
                          statuses: _statuses(),
                          query: _searchQuery.isNotEmpty ? _searchQuery : null,
                          sortDirection: _sortDirection,
                        ),
                      );
                    }
                  },
                  orElse: () {},
                );
              }
            }
            return false;
          },
          child: Scaffold(
            body: ScrollableContent(
              backgroundColor: theme.colorTheme.generic.background,
              children: [
                const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: spacer4,
                        vertical: spacer2,
                      ),
                      child: _buildSearchAndSortControls(textTheme, theme),
                    ),
                    const SizedBox(height: spacer2),
                    state.maybeWhen(
                      loading: () => const Center(
                        child: Padding(
                          padding: EdgeInsets.only(top: spacer8),
                          child: CircularProgressIndicator(),
                        ),
                      ),
                      failure: (msg) => Padding(
                        padding: const EdgeInsets.all(spacer2),
                        child: Text(
                          'Failed to load scheduled visits',
                          style: textTheme.bodyS
                              .copyWith(color: theme.colorTheme.alert.error),
                        ),
                      ),
                      loaded: (items, hasMore, totalCount, fromCache,
                          isLoadingMore) {
                        if (items.isEmpty) {
                          return Padding(
                            padding: const EdgeInsets.all(spacer4),
                            child: Text(
                              'No scheduled visits found.',
                              style: textTheme.bodyS.copyWith(
                                color: theme.colorTheme.text.secondary,
                              ),
                            ),
                          );
                        }

                        return Padding(
                          padding: const EdgeInsets.symmetric(
                            horizontal: spacer4,
                            vertical: spacer2,
                          ),
                          child: Column(
                            children: [
                              for (int index = 0;
                                  index < items.length;
                                  index++) ...[
                                FutureBuilder(
                                    future: AmcDynamicFormRepository()
                                        .resolveFormActionLabel(
                                      isar: isar,
                                      scheduledVisitId: items[index].id ?? '',
                                      schemaKey: schemaKey,
                                      origin: FormOrigin.overallSummary,
                                    ),
                                    builder: (context, snapshot) {
                                      final label = snapshot.data ?? 'Start';
                                      final locality =
                                          parseBoundaryCodeLocality(
                                        items[index].facility?.boundaryCode,
                                      );
                                      return AMCInstallationReportCard(
                                          scheduledVisitId: items[index].id,
                                          label: label,
                                          title: items[index]
                                                  .facility
                                                  ?.facilityName ??
                                              '',
                                          status: items[index].status,
                                          state: locality.state,
                                          district: locality.district,
                                          block: locality.block,
                                          dateAssigned:
                                              items[index].scheduledDate ??
                                                  DateTime.now(),
                                          onPress: () async {
                                            final scheduledVisit = items[index];

                                            context
                                                .read<
                                                    SelectedScheduledVisitBloc>()
                                                .add(SelectedScheduledVisitEvent
                                                    .select(scheduledVisit));

                                            context
                                                .read<SelectedAmcOriginBloc>()
                                                .add(
                                                    const SelectedAmcOriginEvent
                                                        .select(FormOrigin
                                                            .overallSummary));

                                            context.router.push(
                                              AmcDynamicFormRoute(
                                                pageName: 'AMC_Report',
                                                uniqueIdentifier:
                                                    'AssetForm.AMC_SCHEDULED_MAINTENANCE',
                                                schemaName: schemaKey,
                                                scheduledVisit: items[index],
                                                origin:
                                                    FormOrigin.overallSummary,
                                              ),
                                            );
                                          });
                                    }),
                                const SizedBox(height: spacer2),
                                if (index != items.length - 1)
                                  const DigitDivider(
                                    dividerType: DividerType.small,
                                  ),
                              ],
                              if (isLoadingMore)
                                const Padding(
                                  padding:
                                      EdgeInsets.symmetric(vertical: spacer2),
                                  child: Center(
                                    child: CircularProgressIndicator(),
                                  ),
                                ),
                            ],
                          ),
                        );
                      },
                      orElse: () => const SizedBox.shrink(),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildSearchAndSortControls(
      DigitTextTheme textTheme, ThemeData theme) {
    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Select Health Facility',
              style: textTheme.bodyL
                  .copyWith(color: theme.colorTheme.text.primary),
            ),
            const SizedBox(height: spacer1),
            Row(
              children: [
                Expanded(
                  child: DigitSearchFormInput(
                    suffixIcon: Icons.search,
                    onChange: (text) {
                      setState(() {
                        _searchQuery = text;
                        _sortDirection = null;
                      });
                      if (text.isEmpty ||
                          text.length >= minFacilitySearchQueryLength) {
                        _fetchVisits();
                      }
                    },
                  ),
                ),
                const SizedBox(width: spacer2),
                GestureDetector(
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

  void _showSortPopup(DigitTextTheme textTheme, ThemeData theme) {
    showCustomPopup(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, popupSetState) => Popup(
          onCrossTap: () => Navigator.of(ctx).pop(),
          title: 'Sort by',
          type: PopUpType.simple,
          actionAlignment: MainAxisAlignment.center,
          additionalWidgets: [
            Text('Submission Date',
                style: textTheme.headingS
                    .copyWith(color: theme.colorTheme.text.primary)),
            RadioList(
              groupValue: _sortDirection ?? '',
              containerPadding:
                  const EdgeInsets.symmetric(horizontal: 0, vertical: spacer2),
              onChanged: (value) =>
                  popupSetState(() => _sortDirection = value.code),
              radioDigitButtons: [
                RadioButtonModel(code: 'DESC', name: 'Newest first'),
                RadioButtonModel(code: 'ASC', name: 'Oldest first'),
              ],
            ),
            Row(
              children: [
                Expanded(
                  child: DigitButton(
                    label: 'Clear',
                    onPressed: () {
                      setState(() {
                        _sortDirection = null;
                      });
                      Navigator.of(ctx).pop();
                      _fetchVisits();
                    },
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                    mainAxisSize: MainAxisSize.min,
                  ),
                ),
                const SizedBox(width: spacer5),
                Expanded(
                  child: DigitButton(
                    label: 'Sort',
                    isDisabled: _sortDirection == null,
                    onPressed: () {
                      Navigator.of(ctx).pop();
                      _fetchVisits();
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
}

class AMCInstallationReportCard extends StatefulWidget {
  final String? scheduledVisitId;
  final String? title;
  final String? status;
  final String? label;
  final String? state;
  final String? district;
  final String? block;
  final DateTime dateAssigned;
  final String? systemDesignCode;
  final Function() onPress;

  const AMCInstallationReportCard({
    super.key,
    this.scheduledVisitId,
    this.title,
    this.status,
    this.label,
    this.state,
    this.district,
    this.block,
    required this.dateAssigned,
    this.systemDesignCode,
    required this.onPress,
  });

  @override
  State<AMCInstallationReportCard> createState() =>
      _AMCInstallationReportCardState();
}

class _AMCInstallationReportCardState extends State<AMCInstallationReportCard> {
  late Future<bool> _isFailedFuture;

  void _loadIsFailedFuture() {
    _isFailedFuture = ScheduledVisitRepository(
      context.read<ScheduledVisitBloc>().isar,
    ).isFailedScheduledVisitInCache(
      scheduledVisitId: widget.scheduledVisitId ?? '',
    );
  }

  @override
  void initState() {
    super.initState();
    _loadIsFailedFuture();
  }

  @override
  void didUpdateWidget(covariant AMCInstallationReportCard oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.scheduledVisitId != widget.scheduledVisitId) {
      _loadIsFailedFuture();
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final effectiveLabel = widget.label ?? '';
    String formattedDate = DateFormat('dd/MM/yy').format(widget.dateAssigned);

    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.title ?? '',
              style: textTheme.headingL.copyWith(
                color: theme.colorTheme.text.primary,
              ),
            ),
            const SizedBox(height: spacer4),
            const DigitDivider(dividerType: DividerType.small),
            ReportDetailRow(
              label: 'Status',
              value: _detailText(
                context.translate(widget.status ?? ''),
                textTheme,
                theme,
              ),
            ),
            ReportDetailRow(
              label: 'AMC Date',
              value: _detailText(formattedDate, textTheme, theme),
            ),
            ReportDetailRow(
              label: 'State',
              value: _detailText(_displayValue(widget.state), textTheme, theme),
            ),
            ReportDetailRow(
              label: 'District',
              value:
                  _detailText(_displayValue(widget.district), textTheme, theme),
            ),
            ReportDetailRow(
              label: 'Block',
              value: _detailText(_displayValue(widget.block), textTheme, theme),
            ),
            const SizedBox(height: spacer4),
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label:
                  effectiveLabel.isEmpty ? 'Report' : '$effectiveLabel Report',
              onPressed: widget.onPress,
              type: DigitButtonType.primary,
              size: DigitButtonSize.large,
            ),
            const SizedBox(height: spacer4),
            FutureBuilder<bool>(
              future: _isFailedFuture,
              builder: (context, snapshot) {
                final existsInFailedCache = snapshot.data == true;
                final canResume = effectiveLabel.contains("Resume");

                return DigitButton(
                  mainAxisSize: MainAxisSize.max,
                  label: 'Submit For Approval',
                  onPressed: () {
                    context.router.push(const AmcOtpRoute());
                  },
                  isDisabled: existsInFailedCache || !canResume,
                  type: DigitButtonType.secondary,
                  size: DigitButtonSize.large,
                );
              },
            ),
          ],
        )
      ],
    );
  }

  String _displayValue(String? value) {
    final normalized = value?.trim() ?? '';
    return normalized.isEmpty ? '---' : normalized;
  }

  Widget _detailText(String value, dynamic textTheme, ThemeData theme) {
    return Text(
      value,
      style: textTheme.bodyL.copyWith(color: theme.colorTheme.text.primary),
      softWrap: true,
      overflow: TextOverflow.visible,
    );
  }
}
