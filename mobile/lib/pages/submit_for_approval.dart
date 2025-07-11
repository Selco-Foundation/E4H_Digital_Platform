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

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/overall_asset_summary/overall_asset_summary.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
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
    // Delay until after build so context.read<Isar>() is available
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final projectId = context
          .read<SelectedProjectBloc>()
          .state
          .whenOrNull(selected: (p) => p.project.id);
      if (projectId != null) {
        context.read<OverallAssetSummaryBloc>().add(
              OverallAssetSummaryEvent.loadCounts(projectId: projectId),
            );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<UserTypeBloc, UserTypeState>(
      builder: (context, userState) {
        return Scaffold(
          body: ScrollableContent(
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
              Padding(
                padding: const EdgeInsets.symmetric(
                    vertical: spacer2, horizontal: spacer4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Summary',
                      style: textTheme.headingXl.copyWith(
                        color: theme.colorTheme.primary.primary2,
                      ),
                    ),
                    const SizedBox(height: spacer4),
                    const RejectedEditAssetSummary(),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class RejectedEditAssetSummary extends StatelessWidget {
  const RejectedEditAssetSummary({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<OverallAssetSummaryBloc, OverallAssetSummaryState>(
      builder: (context, state) {
        // treat initial as loading
        final isLoading = state.maybeWhen(
          initial: () => true,
          loading: () => true,
          orElse: () => false,
        );

        final errorMessage = state.maybeWhen(
          error: (msg) => msg,
          orElse: () => null,
        );

        int battery = 0, inverter = 0, panel = 0;
        state.maybeWhen(
          loaded: (b, i, p) {
            battery = b;
            inverter = i;
            panel = p;
          },
          orElse: () {},
        );

        if (isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (errorMessage != null) {
          return DigitCard(
            children: [
              Center(
                child: Text(
                  'Error loading counts:\n$errorMessage',
                  style: textTheme.bodyL.copyWith(
                    color: theme.colorTheme.alert.error,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: spacer6),
            ],
          );
        }

        return DigitCard(
          children: [
            _rejectCard(
              context: context,
              assetType: 'Batteries',
              count: battery,
            ),
            _rejectCard(
              context: context,
              assetType: 'Inverters',
              count: inverter,
            ),
            _rejectCard(
              context: context,
              assetType: 'Panels',
              count: panel,
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
    bool isLast = false,
  }) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Stack(
          alignment: Alignment.center,
          children: [
            Align(
              alignment: Alignment.centerLeft,
              child: Text(assetType, style: textTheme.headingS),
            ),
            Center(child: Text('$count', style: textTheme.bodyL)),
          ],
        ),
        if (count > 0) ...[
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
                    "Rejection Reason(s)",
                    style: textTheme.headingM
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                  const SizedBox(height: spacer5),
                  _rejectionReason(context, "Serial Number incorrect", 1),
                  const SizedBox(height: spacer4),
                  _rejectionReason(context, "Additional Reason 2", 2),
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
                  label: "Edit",
                  onPressed: () {
                    // navigate to edit screen
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
            child: Text("Reason $index", style: labelStyle),
          ),
        ),
        const SizedBox(height: spacer2),
        Text(reason, style: valueStyle),
      ],
    );
  }
}
