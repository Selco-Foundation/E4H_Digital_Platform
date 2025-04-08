import 'package:auto_route/annotations.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/navigation/navbar.dart';

@RoutePage()
class SelectAssetTypePage extends StatefulWidget {
  const SelectAssetTypePage({super.key});
  @override
  State<SelectAssetTypePage> createState() {
    return _SelectAssetTypePageState();
  }
}

class _SelectAssetTypePageState extends State<SelectAssetTypePage> {
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
            footer: DigitCard(
                margin: const EdgeInsets.only(top: spacer2),
                children: [
                  DigitButton(
                    mainAxisSize: MainAxisSize.max,
                    label: 'Next',
                    type: DigitButtonType.primary,
                    size: DigitButtonSize.large,
                    onPressed: () {},
                  ),
                ]),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    horizontal: spacer2, vertical: spacer4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    SizedBox(
                      height: spacer8,
                      width: MediaQuery.of(context).size.width,
                      child: DigitStepper(
                        activeIndex: 0,
                        stepperList: [
                          StepperData(
                            onStepTap: () {},
                          ),
                          const StepperData(),
                          const StepperData(),
                          const StepperData(),
                          const StepperData(),
                        ],
                        stepperDirection: Axis.horizontal,
                        inverted: true,
                      ),
                    ),
                    const SizedBox(
                      height: spacer4,
                    ),
                    DigitCard(children: [
                      Text(
                        'Asset Type',
                        style: textTheme.headingXl
                            .copyWith(color: theme.colorTheme.primary.primary2),
                      ),
                      Text(
                        'Choose the asset type',
                        style: textTheme.bodyL
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                      Text(
                        'Select Asset Type',
                        style: textTheme.headingM
                            .copyWith(color: theme.colorTheme.text.primary),
                      ),
                      const DigitDropdown(items: [
                        DropdownItem(name: 'Inverter', code: 'inverter'),
                        DropdownItem(name: 'Battery', code: 'Battery'),
                        DropdownItem(name: 'Panel', code: 'Panel')
                      ])
                    ])
                  ],
                ),
              )
            ]));
  }
}
