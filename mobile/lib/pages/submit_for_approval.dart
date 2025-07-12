// import 'package:digit_ui_components/digit_components.dart';
// import 'package:digit_ui_components/theme/digit_extended_theme.dart';
// import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
// import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
// import 'package:flutter/material.dart';
//
// import '../router/app_router.dart';
// import '../widgets/button/footer_button.dart';
// import '../widgets/cards/rejected_edit_summary.dart';
// import '../widgets/header/back_navigation_help_header.dart';
//
// @RoutePage()
// class SubmitForApprovalPage extends StatefulWidget {
//   const SubmitForApprovalPage({super.key});
//
//   @override
//   State<SubmitForApprovalPage> createState() => _SubmitForApprovalPageState();
// }
//
// class _SubmitForApprovalPageState extends State<SubmitForApprovalPage> {
//   @override
//   Widget build(BuildContext context) {
//     final theme = Theme.of(context);
//     final textTheme = theme.digitTextTheme(context);
//
//     return Scaffold(
//       body: ScrollableContent(
//         enableFixedDigitButton: true,
//         backgroundColor: theme.colorTheme.generic.background,
//         header: const BackNavigationHelpHeaderWidget(
//           showBackNavigation: true,
//           showHelp: false,
//         ),
//         footer: FooterButton(
//           showSuffixIcon: false,
//           text: "Re-Submit for Approval",
//           onPress: () {
//             context.router.replace(const SubmittedSaveSuccessRoute());
//           },
//         ),
//         children: [
//           Padding(
//             padding: const EdgeInsets.symmetric(
//                 vertical: spacer2, horizontal: spacer4),
//             child: Column(
//               crossAxisAlignment: CrossAxisAlignment.start,
//               children: [
//                 Text(
//                   'Summary',
//                   style: textTheme.headingXl
//                       .copyWith(color: theme.colorTheme.primary.primary2),
//                 ),
//                 const SizedBox(height: spacer4),
//                 const RejectedEditAssetSummary(text: 'Battery', count: 2),
//                 const SizedBox(height: spacer4),
//                 DigitCard(children: [
//                   Text(
//                     'Installation Completion Report',
//                     style: textTheme.headingM
//                         .copyWith(color: theme.colorTheme.primary.primary2),
//                   ),
//                   Text(
//                     'Please scan and upload the installation completion report',
//                     style: textTheme.bodyS
//                         .copyWith(color: theme.colorTheme.text.secondary),
//                   ),
//                   FileUploadWidget(
//                     showPreview: true,
//                     allowMultiples: false,
//                     label: 'Upload',
//                     onFilesSelected: (files) {
//                       return {};
//                     },
//                   ),
//                 ]),
//                 const SizedBox(height: spacer4),
//                 Text(
//                   'Defect List',
//                   style: textTheme.headingXl
//                       .copyWith(color: theme.colorTheme.primary.primary2),
//                 ),
//                 const SizedBox(height: spacer4),
//                 DigitCard(children: [
//                   Padding(
//                     padding: const EdgeInsets.symmetric(horizontal: spacer2)
//                         .copyWith(bottom: spacer2),
//                     child: Column(
//                       children: [
//                         DigitCheckbox(
//                           label: "Inverter S.No Incorrect",
//                           onChanged: (value) {},
//                         ),
//                         const SizedBox(height: spacer6),
//                         DigitCheckbox(
//                           label: "Panel S No Incorrect",
//                           onChanged: (value) {},
//                         ),
//                         const SizedBox(height: spacer6),
//                         DigitCheckbox(
//                           label: "System Functionality Incorrect",
//                           onChanged: (value) {},
//                         ),
//                       ],
//                     ),
//                   ),
//                 ]),
//                 const SizedBox(height: spacer2),
//               ],
//             ),
//           ),
//         ],
//       ),
//     );
//   }
// }

// lib/pages/submit_for_approval_page.dart

// import 'package:digit_ui_components/digit_components.dart';
// import 'package:digit_ui_components/theme/digit_extended_theme.dart';
// import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
// import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
// import 'package:flutter/material.dart';
// import 'package:flutter_bloc/flutter_bloc.dart';
//
// import '../blocs/cache_asset/cache_asset.dart';
// import '../blocs/overall_asset_summary/overall_asset_summary.dart';
// import '../blocs/selected_project/selected_project.dart';
// import '../router/app_router.dart';
// import '../utils/extensions.dart';
// import '../widgets/button/footer_button.dart';
// import '../widgets/header/back_navigation_help_header.dart';
//
// @RoutePage()
// class SubmitForApprovalPage extends StatefulWidget {
//   const SubmitForApprovalPage({super.key});
//
//   @override
//   State<SubmitForApprovalPage> createState() => _SubmitForApprovalPageState();
// }
//
// class _SubmitForApprovalPageState extends State<SubmitForApprovalPage> {
//   @override
//   void initState() {
//     super.initState();
//     // 1) Kick off the CacheAssetBloc sync as soon as we have a selected project
//     WidgetsBinding.instance.addPostFrameCallback((_) {
//       final projectId = context
//           .read<SelectedProjectBloc>()
//           .state
//           .whenOrNull(selected: (p) => p.project.id);
//       if (projectId != null) {
//         context.read<CacheAssetBloc>().add(CacheAssetEvent.start(projectId));
//       }
//     });
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     final theme = Theme.of(context);
//     final textTheme = theme.digitTextTheme(context);
//
//     return Scaffold(
//       body: BlocListener<CacheAssetBloc, CacheAssetState>(
//         listener: (context, cacheState) {
//           cacheState.whenOrNull(
//             success: () {
//               // once cache sync is done, trigger overall counts load
//               final projectId = context
//                   .read<SelectedProjectBloc>()
//                   .state
//                   .whenOrNull(selected: (p) => p.project.id);
//               if (projectId != null) {
//                 context.read<OverallAssetSummaryBloc>().add(
//                       OverallAssetSummaryEvent.loadCounts(projectId: projectId),
//                     );
//               }
//             },
//             failure: (error) {
//               context.showSnackBar(
//                 SnackBar(content: Text("Sync failed: $error")),
//               );
//             },
//             loading: () {
//               // BuildContext? dialogCtx;
//               // showDialog(
//               //   context: context,
//               //   barrierDismissible: false,
//               //   builder: (ctx) {
//               //     dialogCtx = ctx;
//               //     return const Center(child: CircularProgressIndicator());
//               //   },
//               // );
//               return const Center(child: CircularProgressIndicator());
//             },
//             // we don't need to do anything on loading here
//           );
//         },
//         child: ScrollableContent(
//           enableFixedDigitButton: true,
//           backgroundColor: theme.colorTheme.generic.background,
//           header: const BackNavigationHelpHeaderWidget(
//               showBackNavigation: true, showHelp: false),
//           footer: FooterButton(
//             showSuffixIcon: false,
//             text: "Re-Submit for Approval",
//             onPress: () =>
//                 context.router.replace(const SubmittedSaveSuccessRoute()),
//           ),
//           children: [
//             Padding(
//               padding: const EdgeInsets.symmetric(
//                   vertical: spacer2, horizontal: spacer4),
//               child: Column(
//                 crossAxisAlignment: CrossAxisAlignment.start,
//                 children: [
//                   Text(
//                     'Summary',
//                     style: textTheme.headingXl.copyWith(
//                       color: theme.colorTheme.primary.primary2,
//                     ),
//                   ),
//                   const SizedBox(height: spacer4),
//                   const RejectedEditAssetSummary(),
//                 ],
//               ),
//             ),
//           ],
//         ),
//       ),
//     );
//   }
// }
//
// class RejectedEditAssetSummary extends StatelessWidget {
//   const RejectedEditAssetSummary({super.key});
//
//   @override
//   Widget build(BuildContext context) {
//     final theme = Theme.of(context);
//     final textTheme = theme.digitTextTheme(context);
//
//     return BlocBuilder<OverallAssetSummaryBloc, OverallAssetSummaryState>(
//       builder: (context, state) {
//         // treat initial as loading
//         final isLoading = state.maybeWhen(
//           initial: () => true,
//           loading: () => true,
//           orElse: () => false,
//         );
//         final errorMessage = state.maybeWhen(
//           error: (msg) => msg,
//           orElse: () => null,
//         );
//         int battery = 0, inverter = 0, panel = 0;
//         state.maybeWhen(
//           loaded: (b, i, p) {
//             battery = b;
//             inverter = i;
//             panel = p;
//           },
//           orElse: () {},
//         );
//
//         if (isLoading) {
//           return const Center(child: CircularProgressIndicator());
//         }
//         if (errorMessage != null) {
//           return DigitCard(
//             children: [
//               Center(
//                 child: Text(
//                   'Error loading counts:\n$errorMessage',
//                   style: textTheme.bodyL
//                       .copyWith(color: theme.colorTheme.alert.error),
//                   textAlign: TextAlign.center,
//                 ),
//               ),
//               const SizedBox(height: spacer6),
//             ],
//           );
//         }
//
//         return DigitCard(
//           children: [
//             _rejectCard(context, 'Inverters', inverter),
//             _rejectCard(context, 'Batteries', battery),
//             _rejectCard(context, 'Panels', panel, isLast: true),
//           ],
//         );
//       },
//     );
//   }
//
//   Widget _rejectCard(BuildContext context, String assetType, int count,
//       {bool isLast = false}) {
//     final theme = Theme.of(context);
//     final textTheme = Theme.of(context).digitTextTheme(context);
//
//     return Column(
//       crossAxisAlignment: CrossAxisAlignment.start,
//       children: [
//         Stack(
//           alignment: Alignment.center,
//           children: [
//             Align(
//               alignment: Alignment.centerLeft,
//               child: Text(assetType, style: textTheme.headingS),
//             ),
//             Center(child: Text('$count', style: textTheme.bodyL)),
//           ],
//         ),
//         if (count > 0) ...[
//           const SizedBox(height: spacer4),
//           Container(
//             decoration: BoxDecoration(
//               color: theme.colorTheme.generic.background,
//               border: Border.all(color: theme.colorTheme.generic.divider),
//               borderRadius: BorderRadius.circular(spacer1),
//             ),
//             child: Padding(
//               padding: const EdgeInsets.symmetric(
//                   horizontal: spacer3, vertical: spacer4),
//               child: Column(
//                 crossAxisAlignment: CrossAxisAlignment.start,
//                 children: [
//                   SizedBox(width: context.width),
//                   Text(
//                     'Rejection Reason(s)',
//                     style: textTheme.headingM
//                         .copyWith(color: theme.colorTheme.text.primary),
//                   ),
//                   const SizedBox(height: spacer5),
//                   _rejectionReason(context, 'Serial Number incorrect', 1),
//                   const SizedBox(height: spacer4),
//                   _rejectionReason(context, 'Additional Reason 2', 2),
//                 ],
//               ),
//             ),
//           ),
//           const SizedBox(height: spacer5),
//           Row(
//             mainAxisAlignment: MainAxisAlignment.center,
//             children: [
//               Expanded(
//                 child: DigitButton(
//                   label: 'Edit',
//                   onPressed: () {
//                     // navigate to your edit screen
//                   },
//                   type: DigitButtonType.secondary,
//                   size: DigitButtonSize.medium,
//                   prefixIcon: Icons.edit,
//                   mainAxisSize: MainAxisSize.min,
//                 ),
//               ),
//             ],
//           ),
//         ],
//         const SizedBox(height: spacer5),
//         if (!isLast) const DigitDivider(dividerType: DividerType.small),
//       ],
//     );
//   }
//
//   Widget _rejectionReason(BuildContext context, String reason, int index) {
//     final theme = Theme.of(context);
//     final labelStyle = Theme.of(context)
//         .digitTextTheme(context)
//         .label
//         .copyWith(color: theme.colorTheme.primary.primary2);
//     final valueStyle = Theme.of(context)
//         .digitTextTheme(context)
//         .label
//         .copyWith(color: theme.colorTheme.text.primary);
//     return Column(
//       crossAxisAlignment: CrossAxisAlignment.start,
//       children: [
//         Container(
//           decoration: BoxDecoration(
//             border: Border.all(color: theme.colorTheme.primary.primary2),
//             borderRadius: BorderRadius.circular(spacer2),
//             color: theme.colorTheme.generic.background,
//           ),
//           child: Padding(
//             padding: const EdgeInsets.symmetric(
//                 vertical: spacer1, horizontal: spacer3),
//             child: Text('Reason $index', style: labelStyle),
//           ),
//         ),
//         const SizedBox(height: spacer2),
//         Text(reason, style: valueStyle),
//       ],
//     );
//   }
// }

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/cache_asset/cache_asset.dart';
import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/selected_project/selected_project.dart';
import '../model/comment/comment.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class SubmitForApprovalPage extends StatefulWidget {
  const SubmitForApprovalPage({super.key});

  @override
  State<SubmitForApprovalPage> createState() => _SubmitForApprovalPageState();
}

class _SubmitForApprovalPageState extends State<SubmitForApprovalPage> {
  @override
  void initState() {
    super.initState();
    // Kick off the cache sync
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final projectId = context
          .read<SelectedProjectBloc>()
          .state
          .whenOrNull(selected: (wf) => wf.project.id);
      if (projectId != null) {
        context.read<CacheAssetBloc>().add(CacheAssetEvent.start(projectId));
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: BlocListener<CacheAssetBloc, CacheAssetState>(
        listener: (context, cacheState) {
          cacheState.whenOrNull(
            success: () {
              final projectId = context
                  .read<SelectedProjectBloc>()
                  .state
                  .whenOrNull(selected: (wf) => wf.project.id);
              if (projectId != null) {
                context.read<OverallAssetSummaryBloc>().add(
                      OverallAssetSummaryEvent.loadCounts(projectId: projectId),
                    );
              }
            },
            failure: (error) {
              context.showSnackBar(
                SnackBar(content: Text("Sync failed: $error")),
              );
            },
            // loading: (_) // we show loading in the summary widget itself
          );
        },
        child: ScrollableContent(
          enableFixedDigitButton: true,
          backgroundColor: theme.colorTheme.generic.background,
          header: const BackNavigationHelpHeaderWidget(
            showBackNavigation: true,
            showHelp: false,
          ),
          footer: FooterButton(
            showSuffixIcon: false,
            text: "Re-Submit for Approval",
            onPress: () =>
                context.router.replace(const SubmittedSaveSuccessRoute()),
          ),
          children: [
            const Padding(
              padding:
                  EdgeInsets.symmetric(vertical: spacer2, horizontal: spacer4),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Summary',
                      style: TextStyle(
                        // pick up your theme here
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                      )),
                  SizedBox(height: spacer4),
                  RejectedEditAssetSummary(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class RejectedEditAssetSummary extends StatelessWidget {
  const RejectedEditAssetSummary({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    // 1) Grab the ProjectWorkflow to extract comments
    final workflow = context
        .watch<SelectedProjectBloc>()
        .state
        .whenOrNull(selected: (wf) => wf);

    // 2) Build a map: AssetType (title case) → List<Comment>
    final commentsByType = <String, List<Comment>>{};
    if (workflow?.project.transactions != null) {
      for (final tx in workflow!.project.transactions!) {
        for (final c in tx.comments ?? []) {
          final t = c.assetType?.titleCase ?? 'Unknown';
          commentsByType.putIfAbsent(t, () => []).add(c);
        }
      }
    }

    return BlocBuilder<OverallAssetSummaryBloc, OverallAssetSummaryState>(
      builder: (context, state) {
        final isLoading = state.maybeWhen(
          initial: () => true,
          loading: () => true,
          orElse: () => false,
        );
        final error = state.maybeWhen(error: (msg) => msg, orElse: () => null);

        int battery = 0, inverter = 0, panel = 0;
        state.maybeWhen(
            loaded: (b, i, p) {
              battery = b;
              inverter = i;
              panel = p;
            },
            orElse: () {});

        if (isLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (error != null) {
          return DigitCard(
            children: [
              Center(
                child: Text(
                  'Error loading counts:\n$error',
                  style: textTheme.bodyL
                      .copyWith(color: theme.colorTheme.alert.error),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: spacer6),
            ],
          );
        }

        // 3) Render one _rejectCard per type
        return DigitCard(
          children: [
            _rejectCard(
              context: context,
              assetType: 'Inverter',
              count: inverter,
              comments: commentsByType['Inverter'],
            ),
            _rejectCard(
              context: context,
              assetType: 'Battery',
              count: battery,
              comments: commentsByType['Battery'],
            ),
            _rejectCard(
              context: context,
              assetType: 'Panel',
              count: panel,
              comments: commentsByType['Panel'],
              isLast: true,
            ),
          ],
        );
      },
    );
  }

  Widget _rejectCard({
    required BuildContext context,
    required String assetType,
    required int count,
    List<Comment>? comments,
    bool isLast = false,
  }) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    final hasComments = comments != null && comments.isNotEmpty;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Title + count
        Stack(
          alignment: Alignment.center,
          children: [
            Align(
              alignment: Alignment.centerLeft,
              child: Text(assetType + 's', style: textTheme.headingS),
            ),
            Center(child: Text('$count', style: textTheme.bodyL)),
          ],
        ),

        // Only if there are comments for this type, show reasons:
        if (hasComments) ...[
          const SizedBox(height: spacer4),
          Container(
            decoration: BoxDecoration(
              color: theme.colorTheme.generic.background,
              border: Border.all(color: theme.colorTheme.generic.divider),
              borderRadius: BorderRadius.circular(spacer1),
            ),
            child: Padding(
              padding: const EdgeInsets.symmetric(
                  horizontal: spacer3, vertical: spacer4),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Rejection Reason(s)',
                    style: textTheme.headingM
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                  const SizedBox(height: spacer5),
                  for (var i = 0; i < comments!.length; i++) ...[
                    _rejectionReason(
                        context, comments[i].commentMessage!, i + 1),
                    if (i < comments.length - 1)
                      const SizedBox(height: spacer4),
                  ],
                ],
              ),
            ),
          ),
          const SizedBox(height: spacer5),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Expanded(
                child: DigitButton(
                  label: 'Edit',
                  onPressed: () {
                    // navigate to your edit screen
                  },
                  type: DigitButtonType.secondary,
                  size: DigitButtonSize.medium,
                  prefixIcon: Icons.edit,
                  mainAxisSize: MainAxisSize.min,
                ),
              ),
            ],
          ),
        ],

        const SizedBox(height: spacer5),
        if (!isLast) const DigitDivider(dividerType: DividerType.small),
      ],
    );
  }

  Widget _rejectionReason(BuildContext context, String reason, int index) {
    final theme = Theme.of(context);
    final labelStyle = Theme.of(context)
        .digitTextTheme(context)
        .label
        .copyWith(color: theme.colorTheme.primary.primary2);
    final valueStyle = Theme.of(context)
        .digitTextTheme(context)
        .label
        .copyWith(color: theme.colorTheme.text.primary);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          decoration: BoxDecoration(
            border: Border.all(color: theme.colorTheme.primary.primary2),
            borderRadius: BorderRadius.circular(spacer2),
            color: theme.colorTheme.generic.background,
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer1, horizontal: spacer3),
            child: Text('Reason $index', style: labelStyle),
          ),
        ),
        const SizedBox(height: spacer2),
        Text(reason, style: valueStyle),
      ],
    );
  }
}
