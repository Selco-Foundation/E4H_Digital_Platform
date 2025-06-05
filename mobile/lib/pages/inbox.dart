import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/blocs/user_type/user_type.dart';

import '../blocs/inbox_type/inbox_type.dart';
import '../router/app_router.dart';
import '../widgets/cards/inbox_report_card.dart';
import '../widgets/cards/inbox_report_rejected_card.dart';
import '../widgets/header/back_navigation_help_header.dart';

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

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
        return Scaffold(
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
                        Row(
                          children: [
                            Text(
                              'Inbox',
                              style: textTheme.headingXl.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                            const SizedBox(width: spacer8),
                            GestureDetector(
                              onTap: () {
                                context.read<UserTypeBloc>().add(
                                    const UserTypeEvent.typeSelected("user"));
                              },
                              child: Text(
                                'User',
                                style: textTheme.bodyS.copyWith(
                                    color: theme.colorTheme.paper.secondary),
                              ),
                            ),
                            const SizedBox(width: spacer4),
                            GestureDetector(
                              onTap: () {
                                context.read<UserTypeBloc>().add(
                                    const UserTypeEvent.typeSelected(
                                        "supervisor"));
                              },
                              child: Text(
                                'Supervisor',
                                style: textTheme.bodyS.copyWith(
                                    color: theme.colorTheme.paper.secondary),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: spacer4),
                        SizedBox(
                          height: spacer12 + spacer1,
                          child: LayoutBuilder(
                            builder: (context, constraints) {
                              return DigitTabBar(
                                tabs: userState.maybeWhen(
                                  orElse: () => ['Rejected', 'Approved'],
                                  supervisor: () =>
                                      ['For Review', 'Rejected', 'Approved'],
                                ),
                                initialIndex: 0,
                                onTabSelected: (index) {
                                  userState.maybeWhen(
                                    supervisor: () {
                                      context.read<InboxTypeBloc>().add(
                                          InboxTypeEvent.typeSelected(index));
                                    },
                                    orElse: () {
                                      context.read<InboxTypeBloc>().add(
                                          InboxTypeEvent.typeSelected(
                                              index + 1));
                                    },
                                  );
                                },
                                tabBarThemeData:
                                    DigitTabBarThemeData.defaultTheme(context)
                                        .copyWith(
                                            tabWidth: constraints.maxWidth /
                                                userState.maybeWhen(
                                                    supervisor: () => 3,
                                                    orElse: () => 2),
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
                                      borderRadius:
                                          BorderRadius.circular(spacer1),
                                      borderSide: BorderSide(
                                          color:
                                              theme.colorTheme.text.secondary),
                                    ),
                                    focusBorder: OutlineInputBorder(
                                      borderRadius:
                                          BorderRadius.circular(spacer1),
                                      borderSide: BorderSide(
                                          color:
                                              theme.colorTheme.text.secondary),
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
                                        color:
                                            theme.colorTheme.primary.primary1))
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
                            // reason:
                            //     "1. Battery S No Incorrect, None \n2. Battery S No Incorrect, Battery Sno Incorrect \n3. SYSTEM FUNCTIONALITY PARAMETERS, Record the Battery Voltage",
                            status: 'Rejected',
                            dateAssigned: DateTime(2024, 1, 25),
                            onPress: () => context.router
                                .push(const SubmitForApprovalRoute()),
                          ),
                          approved: () => InboxReportCard(
                              onPress: () => context.router
                                  .push(const SelectAssetTypeRoute()),
                              title: 'Alkod',
                              dateAssigned: DateTime(2024, 1, 25),
                              status: 'Pending Approval'),
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
      },
    );
  }
}
