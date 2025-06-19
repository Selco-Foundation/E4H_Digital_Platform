// import 'package:digit_ui_components/digit_components.dart';
// import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
// import 'package:digit_ui_components/theme/digit_extended_theme.dart';
// import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
// import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
// import 'package:flutter/material.dart';
// import 'package:flutter_bloc/flutter_bloc.dart';
// import 'package:selco/blocs/user_type/user_type.dart';
//
// import '../blocs/inbox_type/inbox_type.dart';
// import '../router/app_router.dart';
// import '../widgets/cards/inbox_report_card.dart';
// import '../widgets/cards/inbox_report_rejected_card.dart';
// import '../widgets/header/back_navigation_help_header.dart';
//
// @RoutePage()
// class InboxPage extends StatefulWidget {
//   const InboxPage({super.key});
//
//   @override
//   State<InboxPage> createState() => _InboxPageState();
// }
//
// class _InboxPageState extends State<InboxPage> {
//   @override
//   Widget build(BuildContext context) {
//     final theme = Theme.of(context);
//     final textTheme = theme.digitTextTheme(context);
//
//     return BlocBuilder<UserTypeBloc, UserTypeState>(
//       builder: (context, userState) {
//         return Scaffold(
//           body: BlocBuilder<InboxTypeBloc, InboxTypeState>(
//             builder: (ctx, state) {
//               return ScrollableContent(
//                 backgroundColor: theme.colorTheme.generic.background,
//                 header: const BackNavigationHelpHeaderWidget(
//                   showBackNavigation: true,
//                   showHelp: false,
//                 ),
//                 children: [
//                   Padding(
//                     padding: const EdgeInsets.symmetric(
//                         vertical: spacer2, horizontal: spacer4),
//                     child: Column(
//                       crossAxisAlignment: CrossAxisAlignment.start,
//                       children: [
//                         Row(
//                           children: [
//                             Text(
//                               'Inbox',
//                               style: textTheme.headingXl.copyWith(
//                                   color: theme.colorTheme.primary.primary2),
//                             ),
//                             const SizedBox(width: spacer8),
//                             GestureDetector(
//                               onTap: () {
//                                 context.read<UserTypeBloc>().add(
//                                     const UserTypeEvent.typeSelected("user"));
//                               },
//                               child: Text(
//                                 'User',
//                                 style: textTheme.bodyS.copyWith(
//                                     color: theme.colorTheme.paper.secondary),
//                               ),
//                             ),
//                             const SizedBox(width: spacer4),
//                             GestureDetector(
//                               onTap: () {
//                                 context.read<UserTypeBloc>().add(
//                                     const UserTypeEvent.typeSelected(
//                                         "supervisor"));
//                               },
//                               child: Text(
//                                 'Supervisor',
//                                 style: textTheme.bodyS.copyWith(
//                                     color: theme.colorTheme.paper.secondary),
//                               ),
//                             ),
//                           ],
//                         ),
//                         const SizedBox(height: spacer4),
//                         SizedBox(
//                           height: spacer12 + spacer1,
//                           child: LayoutBuilder(
//                             builder: (context, constraints) {
//                               return DigitTabBar(
//                                 tabs: userState.maybeWhen(
//                                   orElse: () => ['Rejected', 'Approved'],
//                                   supervisor: () =>
//                                       ['For Review', 'Rejected', 'Approved'],
//                                 ),
//                                 initialIndex: 0,
//                                 onTabSelected: (index) {
//                                   userState.maybeWhen(
//                                     supervisor: () {
//                                       context.read<InboxTypeBloc>().add(
//                                           InboxTypeEvent.typeSelected(index));
//                                     },
//                                     orElse: () {
//                                       context.read<InboxTypeBloc>().add(
//                                           InboxTypeEvent.typeSelected(
//                                               index + 1));
//                                     },
//                                   );
//                                 },
//                                 tabBarThemeData:
//                                     DigitTabBarThemeData.defaultTheme(context)
//                                         .copyWith(
//                                             tabWidth: constraints.maxWidth /
//                                                 userState.maybeWhen(
//                                                     supervisor: () => 3,
//                                                     orElse: () => 2),
//                                             padding: EdgeInsets.zero),
//                               );
//                             },
//                           ),
//                         ),
//                         DigitCard(
//                           children: [
//                             Row(
//                               children: [
//                                 Expanded(
//                                   child: DigitSearchFormInput(
//                                     innerLabel: "Search Health Facility",
//                                     suffixIcon: Icons.search,
//                                     iconColor: const Light().primary2,
//                                     enableBorder: OutlineInputBorder(
//                                       borderRadius:
//                                           BorderRadius.circular(spacer1),
//                                       borderSide: BorderSide(
//                                           color:
//                                               theme.colorTheme.text.secondary),
//                                     ),
//                                     focusBorder: OutlineInputBorder(
//                                       borderRadius:
//                                           BorderRadius.circular(spacer1),
//                                       borderSide: BorderSide(
//                                           color:
//                                               theme.colorTheme.text.secondary),
//                                     ),
//                                   ),
//                                 ),
//                                 Icon(
//                                   Icons.import_export,
//                                   color: theme.colorTheme.primary.primary1,
//                                   size: spacer8,
//                                 ),
//                                 Text("Sort",
//                                     style: textTheme.headingS.copyWith(
//                                         color:
//                                             theme.colorTheme.primary.primary1))
//                               ],
//                             )
//                           ],
//                         ),
//                         const SizedBox(height: spacer4),
//                         state.when(
//                           submitted: () => InboxReportCard(
//                               onPress: () => context.router
//                                   .push(const InboxAssetSummaryRoute()),
//                               title: 'Alkod',
//                               dateAssigned: DateTime(2024, 1, 25),
//                               status: 'Pending Installation'),
//                           rejected: () => InboxReportRejectedCard(
//                             title: 'Alkod',
//                             // reason:
//                             //     "1. Battery S No Incorrect, None \n2. Battery S No Incorrect, Battery Sno Incorrect \n3. SYSTEM FUNCTIONALITY PARAMETERS, Record the Battery Voltage",
//                             status: 'Rejected',
//                             dateAssigned: DateTime(2024, 1, 25),
//                             onPress: () => context.router
//                                 .push(const SubmitForApprovalRoute()),
//                           ),
//                           approved: () => InboxReportCard(
//                               onPress: () => context.router
//                                   .push(const SelectAssetTypeRoute()),
//                               title: 'Alkod',
//                               dateAssigned: DateTime(2024, 1, 25),
//                               status: 'Pending Approval'),
//                         ),
//                         const SizedBox(height: spacer5),
//                       ],
//                     ),
//                   )
//                 ],
//               );
//             },
//           ),
//         );
//       },
//     );
//   }
// }

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/digit_tab_bar_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_tab.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/blocs/user_type/user_type.dart';
import 'package:selco/utils/utils.dart';

import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/project/project.dart';
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
            builder: (ctx, inboxState) {
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
                                  // 1) determine workflowStatuses based on role & tab
                                  List<String> workflowStatuses = [];
                                  userState.maybeWhen(
                                    supervisor: () {
                                      if (index == 0) {
                                        workflowStatuses = [
                                          'ASSIGNED_TO_FIELD_STAFF',
                                          WORKFLOW_STATUS_FIELD_SUPERVISOR
                                              .SUBMITTED_BY_FIELD_STAFF.name,
                                          WORKFLOW_STATUS_FIELD_SUPERVISOR
                                              .SUBMITTED_BY_SUPERVISOR.name
                                        ];
                                      } else if (index == 1) {
                                        workflowStatuses = [
                                          WORKFLOW_STATUS_FIELD_SUPERVISOR
                                              .REJECTED_BY_QC_SPOC.name
                                        ];
                                      } else if (index == 2) {
                                        workflowStatuses = [
                                          WORKFLOW_STATUS_FIELD_SUPERVISOR
                                              .APPROVED_BY_QC_SPOC.name
                                        ];
                                      }
                                    },
                                    orElse: () {
                                      // User: tabs ['Rejected','Approved']
                                      if (index == 0) {
                                        workflowStatuses = [
                                          WORKFLOW_STATUS_FIELD_STAFF
                                              .REJECTED_BY_FIELD_SUPERVISOR.name
                                        ];
                                      } else if (index == 1) {
                                        workflowStatuses = [
                                          WORKFLOW_STATUS_FIELD_STAFF
                                              .APPROVED_BY_QC_SPOC.name
                                        ];
                                      }
                                    },
                                  );

                                  // 2) update InboxTypeBloc
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

                                  // 3) fetch projects by workflow
                                  context.read<ProjectBloc>().add(
                                      ProjectEvent.fetchProjectsByWorkflow(
                                          workflowStatuses: workflowStatuses));
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
                        // Display based on fetched projects or loading
                        BlocBuilder<ProjectBloc, ProjectState>(
                          builder: (context, projectState) {
                            return projectState.maybeWhen(
                              orElse: () => const Center(
                                  child: Padding(
                                padding: EdgeInsets.only(top: spacer8),
                                child: CircularProgressIndicator(),
                              )),
                              fetched: (projectsList) {
                                if (projectsList.isEmpty) {
                                  return const Text('No Projects to display');
                                }
                                return Column(
                                  children: [
                                    for (final project in projectsList)
                                      Column(
                                        children: [
                                          inboxState.when(
                                            submitted: () => InboxReportCard(
                                                onPress: () => context.router.push(
                                                    const InboxAssetSummaryRoute()),
                                                title: project.project.name ??
                                                    '---',
                                                dateAssigned:
                                                    DateTime(2024, 1, 25),
                                                status: project.state ?? '---'),
                                            rejected: () =>
                                                InboxReportRejectedCard(
                                              title:
                                                  project.project.name ?? '---',
                                              status: project.state ?? '---',
                                              dateAssigned:
                                                  DateTime(2024, 1, 25),
                                              onPress: () => context.router.push(
                                                  const SubmitForApprovalRoute()),
                                            ),
                                            approved: () => InboxReportCard(
                                                onPress: () => context.router.push(
                                                    const SelectAssetTypeRoute()),
                                                title: project.project.name ??
                                                    '---',
                                                dateAssigned:
                                                    DateTime(2024, 1, 25),
                                                status: project.state ?? '---'),
                                          ),
                                          const SizedBox(height: spacer5),
                                        ],
                                      ),
                                  ],
                                );
                              },
                              selected: (_) =>
                                  const SizedBox.shrink(), // not used here
                            );
                          },
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
