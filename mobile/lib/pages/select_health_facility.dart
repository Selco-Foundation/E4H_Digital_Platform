import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../router/app_router.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class SelectHealthFacilityPage extends StatefulWidget {
  const SelectHealthFacilityPage({super.key});
  @override
  State<SelectHealthFacilityPage> createState() {
    return _SelectHealthFacilityPageState();
  }
}

class _SelectHealthFacilityPageState extends State<SelectHealthFacilityPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
        appBar: const Navbar(),
        body: ScrollableContent(
            backgroundColor: theme.colorTheme.generic.background,
            children: [
              const BackNavigationHelpHeaderWidget(
                showBackNavigation: true,
                showHelp: false,
              ),
              Padding(
                  padding: const EdgeInsets.symmetric(
                      horizontal: spacer4, vertical: spacer2),
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        DigitCard(
                          children: [
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Select Health Facility',
                                  style: textTheme.bodyL.copyWith(
                                      color: theme.colorTheme.text.primary),
                                ),
                                const SizedBox(
                                  height: spacer2,
                                ),
                                Row(
                                  children: [
                                    const Expanded(
                                      child: DigitSearchFormInput(
                                        suffixIcon: Icons.search,
                                      ),
                                    ),
                                    Icon(
                                      Icons.import_export,
                                      color: theme.colorTheme.primary.primary1,
                                      size: spacer8,
                                    ),
                                  ],
                                ),
                              ],
                            )
                          ],
                        ),
                        const SizedBox(height: spacer8),
                        InstallationReportCard(
                          onPress: () =>
                              context.router.push(const SelectAssetTypeRoute()),
                          title: 'Alkod',
                          dateAssigned: DateTime(2024, 1, 25),
                          status: 'Pending Installation',
                          solutionDocPath: 'Allepy Solution Doc',
                        ),
                        const SizedBox(
                          height: spacer5,
                        ),
                        InstallationReportCard(
                          onPress: () =>
                              context.router.push(const SelectAssetTypeRoute()),
                          title: 'Allepy',
                          dateAssigned: DateTime(2024, 1, 25),
                          status: 'Pending Installation',
                          solutionDocPath: 'Allepy Solution Doc',
                        )
                      ])),
            ]));
  }
}

class InstallationReportCard extends StatelessWidget {
  final String? title;
  final String? status;
  final DateTime dateAssigned;
  final String? solutionDocPath;
  final Function() onPress;

  const InstallationReportCard({
    super.key,
    this.title,
    this.status,
    required this.dateAssigned,
    this.solutionDocPath,
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
            style: textTheme.headingL
                .copyWith(color: theme.colorTheme.text.primary),
          ),
          const SizedBox(
            height: spacer4,
          ),
          const DigitDivider(
            dividerType: DividerType.small,
          ),
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
                    'Date Assigned',
                    style: textTheme.headingS
                        .copyWith(color: theme.colorTheme.text.primary),
                  ),
                  const SizedBox(height: spacer4),
                  Text(
                    'Solution Doc',
                    style: textTheme.headingS
                        .copyWith(color: theme.colorTheme.text.primary),
                  )
                ],
              ),
              const SizedBox(
                width: spacer12,
              ),
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
                  const SizedBox(height: spacer4),
                  Row(
                    children: [
                      Icon(
                        Icons.picture_as_pdf,
                        color: theme.colorTheme.primary.primary1,
                      ),
                      const SizedBox(width: spacer1),
                      Text(
                        '$solutionDocPath',
                        style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.text.disabled,
                            fontSize: spacer3),
                      ),
                    ],
                  )
                ],
              ),
            ],
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: spacer4),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Expanded(
                  child: LinearProgressIndicator(
                    borderRadius: BorderRadius.circular(spacer1),
                    backgroundColor: theme.colorTheme.generic.background,
                    valueColor: AlwaysStoppedAnimation<Color>(
                      theme.colorTheme.alert.success,
                    ),
                    value: 0.4,
                    minHeight: spacer3,
                  ),
                ),
                const SizedBox(width: spacer3),
                Text(
                  '40%',
                  style: textTheme.bodyS
                      .copyWith(color: theme.colorTheme.text.secondary),
                )
              ],
            ),
          ),
          // Row(
          //   children: [
          //     ProgressIndicatorContainer(label: '', prefixLabel: '', suffixLabel: '', value: 0.4),
          //   ],
          // ),
          DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'Start Installation Report',
              onPressed: onPress,
              type: DigitButtonType.primary,
              size: DigitButtonSize.large),
          const SizedBox(
            height: spacer4,
          ),
          DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'Submit For Approval',
              onPressed: () {},
              isDisabled: true,
              type: DigitButtonType.secondary,
              size: DigitButtonSize.large),
        ],
      )
    ]);
  }
}
