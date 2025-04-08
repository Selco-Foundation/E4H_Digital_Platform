import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/navigation/navbar.dart';
import 'package:selco/widgets/progress_indicator/progress_indicator.dart';

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
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            children: [
              Padding(
                  padding: const EdgeInsets.symmetric(
                      horizontal: spacer2, vertical: spacer4),
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
                                  height: spacer4,
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
                          title: 'Something',
                          dateAssigned: DateTime(2024, 1, 25),
                          status: 'Pending Installation',
                          solutionDocPath: 'somedocumnet',
                        ),
                        const SizedBox(
                          height: spacer5,
                        ),
                        InstallationReportCard(
                          title: 'Something',
                          dateAssigned: DateTime(2024, 1, 25),
                          status: 'Pending Installation',
                          solutionDocPath: 'somedocumnet',
                        )
                      ])),
            ]));
  }
}

class InstallationReportCard extends StatelessWidget {
  final String? title;
  final String? status;
  final DateTime? dateAssigned;
  final String? solutionDocPath;
  final Function()? onPress;

  const InstallationReportCard({
    super.key,
    this.title,
    this.status,
    this.dateAssigned,
    this.solutionDocPath,
    this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return DigitCard(onPressed: onPress, children: [
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
            dividerType: DividerType.medium,
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
                    '$dateAssigned',
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
                      Text(
                        '$solutionDocPath',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                    ],
                  )
                ],
              ),
            ],
          ),
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              const Expanded(
                child: ProgressIndicatorContainer(
                    label: '', prefixLabel: '', suffixLabel: '', value: 0.4),
              ),
              Text(
                '40%',
                style: textTheme.bodyL
                    .copyWith(color: theme.colorTheme.text.secondary),
              )
            ],
          ),
          SizedBox(
            height: spacer10,
            width: double.infinity,
            child: DigitButton(
                label: 'Start Insallation Report',
                onPressed: () {},
                type: DigitButtonType.primary,
                size: DigitButtonSize.large),
          ),
          const SizedBox(
            height: spacer6,
          ),
          SizedBox(
            height: spacer10,
            width: double.infinity,
            child: DigitButton(
                label: 'Submit For Approval',
                onPressed: () {},
                isDisabled: true,
                type: DigitButtonType.secondary,
                size: DigitButtonSize.large),
          ),
        ],
      )
    ]);
  }
}
