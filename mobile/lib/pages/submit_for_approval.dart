import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/rejected_edit_summary.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class SubmitForApprovalPage extends StatefulWidget {
  const SubmitForApprovalPage({super.key});

  @override
  State<SubmitForApprovalPage> createState() => _SubmitForApprovalPageState();
}

class _SubmitForApprovalPageState extends State<SubmitForApprovalPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
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
          onPress: () {
            context.router.replace(const SubmittedSaveSuccessRoute());
          },
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
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),
                const RejectedEditAssetSummary(text: 'Battery', count: 2),
                const SizedBox(height: spacer4),
                DigitCard(children: [
                  Text(
                    'Installation Completion Report',
                    style: textTheme.headingM
                        .copyWith(color: theme.colorTheme.primary.primary2),
                  ),
                  Text(
                    'Please scan and upload the installation completion report',
                    style: textTheme.bodyS
                        .copyWith(color: theme.colorTheme.text.secondary),
                  ),
                  FileUploadWidget(
                    showPreview: true,
                    allowMultiples: false,
                    label: 'Upload',
                    onFilesSelected: (files) {
                      return {};
                    },
                  ),
                ]),
                const SizedBox(height: spacer4),
                Text(
                  'Defect List',
                  style: textTheme.headingXl
                      .copyWith(color: theme.colorTheme.primary.primary2),
                ),
                const SizedBox(height: spacer4),
                DigitCard(children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: spacer2)
                        .copyWith(bottom: spacer2),
                    child: Column(
                      children: [
                        DigitCheckbox(
                          label: "Inverter S.No Incorrect",
                          onChanged: (value) {},
                        ),
                        const SizedBox(height: spacer6),
                        DigitCheckbox(
                          label: "Panel S No Incorrect",
                          onChanged: (value) {},
                        ),
                        const SizedBox(height: spacer6),
                        DigitCheckbox(
                          label: "System Functionality Incorrect",
                          onChanged: (value) {},
                        ),
                      ],
                    ),
                  ),
                ]),
                const SizedBox(height: spacer2),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
