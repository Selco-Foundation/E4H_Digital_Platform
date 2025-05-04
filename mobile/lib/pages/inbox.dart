import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';

import '../blocs/inbox_type/inbox_type.dart';
import '../router/app_router.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class InboxPage extends StatefulWidget {
  const InboxPage({super.key});

  @override
  State<InboxPage> createState() => _InboxPageState();
}

class _InboxPageState extends State<InboxPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
      body: BlocBuilder<InboxTypeBloc, InboxTypeState>(
        builder: (ctx, state) {
          return ScrollableContent(
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    vertical: spacer2, horizontal: spacer4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Inbox',
                      style: textTheme.headingXl
                          .copyWith(color: theme.colorTheme.primary.primary2),
                    ),
                    const SizedBox(height: spacer4),
                    SizedBox(
                      height: spacer12 + spacer1,
                      child: LayoutBuilder(
                        builder: (context, constraints) {
                          return DigitTabBar(
                            tabs: const ['Submitted', 'Rejected', 'Approved'],
                            initialIndex: 0,
                            onTabSelected: (index) {
                              context
                                  .read<InboxTypeBloc>()
                                  .add(InboxTypeEvent.typeSelected(index));
                            },
                            tabBarThemeData:
                                DigitTabBarThemeData.defaultTheme(context)
                                    .copyWith(
                                        tabWidth: constraints.maxWidth / 3,
                                        padding: EdgeInsets.zero),
                          );
                        },
                      ),
                    ),
                    DigitCard(
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: DigitSearchFormInput(
                                innerLabel: "Search Health Facility",
                                suffixIcon: Icons.search,
                                iconColor: const Light().primary2,
                                enableBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(spacer1),
                                  borderSide: BorderSide(
                                      color: theme.colorTheme.text.secondary),
                                ),
                                focusBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(spacer1),
                                  borderSide: BorderSide(
                                      color: theme.colorTheme.text.secondary),
                                ),
                              ),
                            ),
                            Icon(
                              Icons.import_export,
                              color: theme.colorTheme.primary.primary1,
                              size: spacer8,
                            ),
                            Text("Sort",
                                style: textTheme.headingS.copyWith(
                                    color: theme.colorTheme.primary.primary1))
                          ],
                        )
                      ],
                    ),
                    const SizedBox(height: spacer4),
                    state.when(
                      submitted: () => InboxReportCard(
                          onPress: () => context.router
                              .push(const InboxAssetSummaryRoute()),
                          title: 'Alkod',
                          dateAssigned: DateTime(2024, 1, 25),
                          status: 'Pending Installation'),
                      rejected: () => InboxReportRejectedCard(
                        title: 'Alkod',
                        reason:
                            "1. Battery S No Incorrect, None \n2. Battery S No Incorrect, Battery Sno Incorrect \n3. SYSTEM FUNCTIONALITY PARAMETERS, Record the Battery Voltage",
                        status: 'Pending Approval',
                        onPress: () =>
                            context.router.push(const SubmitForApprovalRoute()),
                      ),
                      approved: () => InboxReportCard(
                          onPress: () =>
                              context.router.push(const SelectAssetTypeRoute()),
                          title: 'Alkod',
                          dateAssigned: DateTime(2024, 1, 25),
                          status: 'Pending Installation'),
                    ),
                    const SizedBox(height: spacer5),
                  ],
                ),
              )
            ],
          );
        },
      ),
    );
  }
}

class InboxReportCard extends StatelessWidget {
  final String? title;
  final String? status;
  final DateTime dateAssigned;
  final Function() onPress;

  const InboxReportCard({
    super.key,
    this.title,
    this.status,
    required this.dateAssigned,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    String formattedDate = DateFormat('dd/MM/yy').format(dateAssigned);

    return DigitCard(children: [
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title!,
            style: textTheme.headingM
                .copyWith(color: theme.colorTheme.primary.primary2),
          ),
          const SizedBox(height: spacer4),
          const DigitDivider(dividerType: DividerType.small),
          Row(
            children: [
              Column(
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
                    'Submission Date',
                    style: textTheme.headingS
                        .copyWith(color: theme.colorTheme.text.primary),
                  )
                ],
              ),
              const SizedBox(width: spacer12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: spacer4),
                  Text(
                    '$status',
                    style: textTheme.bodyL
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                  const SizedBox(height: spacer4),
                  Text(
                    formattedDate,
                    style: textTheme.bodyL
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: spacer4),
          DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'View Summary',
              onPressed: onPress,
              type: DigitButtonType.secondary,
              size: DigitButtonSize.large),
        ],
      )
    ]);
  }
}

class InboxReportRejectedCard extends StatelessWidget {
  final String? title;
  final String? status;
  final String? reason;
  final Function() onPress;

  const InboxReportRejectedCard({
    super.key,
    this.title,
    this.status,
    this.reason,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return DigitCard(
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title!,
              style: textTheme.headingM
                  .copyWith(color: theme.colorTheme.primary.primary2),
            ),
            const SizedBox(height: spacer4),
            const DigitDivider(dividerType: DividerType.small),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  flex: 1,
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
                        'Rejection Reason',
                        style: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: spacer12),
                Expanded(
                  flex: 2,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: spacer4),
                      Text(
                        status ?? '',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                      const SizedBox(height: spacer4),
                      Text(
                        reason ?? '',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.text.primary),
                        softWrap: true,
                        overflow: TextOverflow.visible,
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: spacer8),
            DigitButton(
                mainAxisSize: MainAxisSize.max,
                label: 'Edit Asset Data',
                onPressed: onPress,
                type: DigitButtonType.primary,
                size: DigitButtonSize.large),
            const SizedBox(height: spacer4),
            DigitButton(
                mainAxisSize: MainAxisSize.max,
                label: 'Re-Submit for Approval',
                onPressed: onPress,
                type: DigitButtonType.secondary,
                size: DigitButtonSize.large),
          ],
        ),
      ],
    );
  }
}
