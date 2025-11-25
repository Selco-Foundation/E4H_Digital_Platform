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
import 'package:selco/utils/utils.dart';

import '../blocs/scheduled_visit/scheduled_visit.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcSelectFacilityPage extends StatefulWidget {
  const AmcSelectFacilityPage({super.key});

  @override
  State<AmcSelectFacilityPage> createState() => _AmcSelectFacilityPageState();
}

class _AmcSelectFacilityPageState extends State<AmcSelectFacilityPage> {
  String? _sortDirection;
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();

    context.read<ScheduledVisitBloc>().add(ScheduledVisitEvent.loadInitial(
        status: WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<ScheduledVisitBloc, ScheduledVisitState>(
      builder: (context, state) {
        return Scaffold(
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
                        horizontal: spacer4, vertical: spacer2),
                    child: _buildSearchAndSortControls(textTheme, theme),
                  ),
                  const SizedBox(height: spacer2),
                  Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: spacer4, vertical: spacer2),
                    child: AMCInstallationReportCard(
                      label: "Resume",
                      title: 'Dharnal PHC',
                      status: 'Scheduled',
                      dateAssigned: DateTime.now(),
                      onPress: () {
                        context.router.push(AmcDynamicFormRoute(
                            pageName: "AMC_Report",
                            uniqueIdentifier: "AMC.SCHEDULED_MAINTENANCE",
                            schemaName: "SELCO.AMC_SCHEDULED_MAINTENANCE",
                            scheduledVisitId: '12345678',
                            origin: FormOrigin.overallSummary));
                      },
                    ),
                  )
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  // @override
  // Widget build(BuildContext context) {
  //   final theme = Theme.of(context);
  //   final textTheme = theme.digitTextTheme(context);
  //
  //   final isar = context.read<ScheduledVisitBloc>().isar;
  //   const schemaKey = 'SELCO.AMC_SCHEDULED_MAINTENANCE';
  //
  //   return BlocBuilder<ScheduledVisitBloc, ScheduledVisitState>(
  //     builder: (context, state) {
  //       return NotificationListener<ScrollNotification>(
  //         onNotification: (notification) {
  //           if (notification is ScrollUpdateNotification) {
  //             final max = notification.metrics.maxScrollExtent;
  //             final current = notification.metrics.pixels;
  //
  //             if (current > max - 200) {
  //               final bloc = context.read<ScheduledVisitBloc>();
  //               bloc.state.maybeWhen(
  //                 loaded:
  //                     (items, hasMore, totalCount, fromCache, isLoadingMore) {
  //                   if (hasMore && !isLoadingMore) {
  //                     bloc.add(
  //                       ScheduledVisitEvent.loadMore(
  //                         status:
  //                             WORKFLOW_STATUS_AMC_FIELD_STAFF.SCHEDULED.name,
  //                       ),
  //                     );
  //                   }
  //                 },
  //                 orElse: () {},
  //               );
  //             }
  //           }
  //           return false; // let scroll events continue bubbling
  //         },
  //         child: Scaffold(
  //           body: ScrollableContent(
  //             backgroundColor: theme.colorTheme.generic.background,
  //             children: [
  //               const BackNavigationHelpHeaderWidget(
  //                 showBackNavigation: true,
  //                 showHelp: false,
  //               ),
  //               Column(
  //                 children: [
  //                   Padding(
  //                     padding: const EdgeInsets.symmetric(
  //                       horizontal: spacer4,
  //                       vertical: spacer2,
  //                     ),
  //                     child: _buildSearchAndSortControls(textTheme, theme),
  //                   ),
  //                   const SizedBox(height: spacer2),
  //                   state.maybeWhen(
  //                     loading: () => const Center(
  //                       child: Padding(
  //                         padding: EdgeInsets.only(top: spacer8),
  //                         child: CircularProgressIndicator(),
  //                       ),
  //                     ),
  //                     failure: (msg) => Padding(
  //                       padding: const EdgeInsets.all(spacer2),
  //                       child: Text(
  //                         'Failed to load scheduled visits',
  //                         style: textTheme.bodyS
  //                             .copyWith(color: theme.colorTheme.alert.error),
  //                       ),
  //                     ),
  //                     loaded: (items, hasMore, totalCount, fromCache,
  //                         isLoadingMore) {
  //                       if (items.isEmpty) {
  //                         return Padding(
  //                           padding: const EdgeInsets.all(spacer4),
  //                           child: Text(
  //                             'No scheduled visits found.',
  //                             style: textTheme.bodyS.copyWith(
  //                               color: theme.colorTheme.text.secondary,
  //                             ),
  //                           ),
  //                         );
  //                       }
  //
  //                       return Padding(
  //                         padding: const EdgeInsets.symmetric(
  //                           horizontal: spacer4,
  //                           vertical: spacer2,
  //                         ),
  //                         child: Column(
  //                           children: [
  //                             for (int index = 0;
  //                                 index < items.length;
  //                                 index++) ...[
  //                               FutureBuilder(
  //                                   future: AmcDynamicFormRepository()
  //                                       .resolveFormActionLabel(
  //                                     isar: isar,
  //                                     scheduledVisitId: items[index].id ?? '',
  //                                     schemaKey: schemaKey,
  //                                     origin: FormOrigin.overallSummary,
  //                                   ),
  //                                   builder: (context, snapshot) {
  //                                     final label = snapshot.data ?? 'Start';
  //                                     return AMCInstallationReportCard(
  //                                         label: label,
  //                                         title: items[index]
  //                                                 .facility
  //                                                 ?.facilityName ??
  //                                             '',
  //                                         status: items[index].status,
  //                                         dateAssigned:
  //                                             items[index].scheduledDate ??
  //                                                 DateTime.now(),
  //                                         onPress: () async {
  //                                           final scheduledVisit = items[index];
  //
  //                                           context
  //                                               .read<
  //                                                   SelectedScheduledVisitBloc>()
  //                                               .add(SelectedScheduledVisitEvent
  //                                                   .select(scheduledVisit));
  //
  //                                           final userType =
  //                                               USER_TYPES.AMC.name;
  //
  //                                           final formRepo =
  //                                               AmcDynamicFormRepository();
  //                                           final initialValues = await formRepo
  //                                               .getInitialFormValues(
  //                                             isar: isar,
  //                                             scheduledVisitId:
  //                                                 scheduledVisit.id!,
  //                                             responsesFromModel: scheduledVisit
  //                                                 .visitReport?.responses,
  //                                             userType: userType,
  //                                           );
  //
  //                                           context.router.push(
  //                                             AmcDynamicFormRoute(
  //                                               pageName: 'AMC_Report',
  //                                               uniqueIdentifier:
  //                                                   'AMC.SCHEDULED_MAINTENANCE',
  //                                               schemaName: schemaKey,
  //                                               scheduledVisitId:
  //                                                   items[index].id ?? '',
  //                                               origin:
  //                                                   FormOrigin.overallSummary,
  //                                               initialFormValues:
  //                                                   initialValues,
  //                                             ),
  //                                           );
  //                                         });
  //                                   }),
  //                               const SizedBox(height: spacer2),
  //                               if (index != items.length - 1)
  //                                 const DigitDivider(
  //                                   dividerType: DividerType.small,
  //                                 ),
  //                             ],
  //                             if (isLoadingMore)
  //                               const Padding(
  //                                 padding:
  //                                     EdgeInsets.symmetric(vertical: spacer2),
  //                                 child: Center(
  //                                   child: CircularProgressIndicator(),
  //                                 ),
  //                               ),
  //                           ],
  //                         ),
  //                       );
  //                     },
  //                     orElse: () => const SizedBox.shrink(),
  //                   ),
  //                 ],
  //               ),
  //             ],
  //           ),
  //         ),
  //       );
  //     },
  //   );
  // }

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
                      // _fetchProject();
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
                    onPressed: () => Navigator.of(ctx).pop(),
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

class AMCInstallationReportCard extends StatelessWidget {
  final String? scheduledVisitId;
  final String? title;
  final String? status;
  final String? label;
  final DateTime dateAssigned;
  final String? systemDesignCode;
  final Function() onPress;

  const AMCInstallationReportCard({
    super.key,
    this.scheduledVisitId,
    this.title,
    this.status,
    this.label,
    required this.dateAssigned,
    this.systemDesignCode,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    String formattedDate = DateFormat('dd/MM/yy').format(dateAssigned);

    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              "$title",
              style: textTheme.headingL.copyWith(
                color: theme.colorTheme.text.primary,
              ),
            ),
            const SizedBox(height: spacer4),
            const DigitDivider(dividerType: DividerType.small),
            Row(
              children: [
                Expanded(
                  flex: 2,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: spacer4),
                      Text(
                        'Status',
                        style: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                      const SizedBox(height: spacer4),
                      Text(
                        'AMC Date',
                        style: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                      )
                    ],
                  ),
                ),
                const SizedBox(width: spacer12),
                Expanded(
                  flex: 3,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: spacer4),
                      Text(
                        context.translate('$status'),
                        style: textTheme.bodyL.copyWith(
                          color: theme.colorTheme.text.primary,
                        ),
                        softWrap: true,
                        overflow: TextOverflow.visible,
                      ),
                      const SizedBox(height: spacer4),
                      Text(
                        formattedDate,
                        style: textTheme.bodyL.copyWith(
                          color: theme.colorTheme.text.primary,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: spacer4),
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: "$label Report",
              onPressed: onPress,
              type: DigitButtonType.primary,
              size: DigitButtonSize.large,
            ),
            const SizedBox(height: spacer4),
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'Submit For Approval',
              onPressed: () {
                context.router.push(const AmcOtpRoute());
              },
              isDisabled: label!.contains("Resume") ? false : true,
              type: DigitButtonType.secondary,
              size: DigitButtonSize.large,
            ),
          ],
        )
      ],
    );
  }
}
