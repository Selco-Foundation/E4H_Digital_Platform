import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/navigation/navbar.dart';

@RoutePage()
class AssetTypeDetailPage extends StatefulWidget {
  const AssetTypeDetailPage({super.key});

  @override
  State<AssetTypeDetailPage> createState() => _AssetTypeDetailPageState();
}

class _AssetTypeDetailPageState extends State<AssetTypeDetailPage> {
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
                    onPressed: () =>
                        context.router.push(const AddNewAssetRoute()),
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
                        activeIndex: 2,
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
                    const SizedBox(height: spacer4),
                    DigitCard(children: [
                      Text(
                        'Inverter Details',
                        style: textTheme.headingXl
                            .copyWith(color: theme.colorTheme.primary.primary2),
                      ),
                      LabeledField(
                        label: 'Count',
                        labelStyle: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        capitalizedFirstLetter: false,
                        child: const DigitDropdown(items: [
                          DropdownItem(name: '1', code: '1'),
                          DropdownItem(name: '2', code: '2'),
                          DropdownItem(name: '3', code: '3')
                        ]),
                      ),
                      LabeledField(
                        label: 'Warranty Start Date',
                        labelStyle: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        capitalizedFirstLetter: false,
                        child: DigitDateFormInput(
                          controller: TextEditingController(),
                          initialValue: 'Default Today Date',
                          isDisabled: true,
                        ),
                      ),
                      LabeledField(
                        label: 'Warranty Duration',
                        labelStyle: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        capitalizedFirstLetter: false,
                        child: const DigitDropdown(items: [
                          DropdownItem(name: '15 Years', code: '15'),
                          DropdownItem(name: '16 Years', code: '16'),
                          DropdownItem(name: '17 Years', code: '17')
                        ]),
                      ),
                      LabeledField(
                        label: 'Brand',
                        labelStyle: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        capitalizedFirstLetter: false,
                        child: const DigitDropdown(items: [
                          DropdownItem(name: 'Brand 1', code: '1'),
                          DropdownItem(name: 'Brand 2', code: '2'),
                          DropdownItem(name: 'Brand 3', code: '3')
                        ]),
                      ),
                      LabeledField(
                        label: 'Model Number',
                        labelStyle: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        capitalizedFirstLetter: false,
                        child: DigitTextFormInput(
                          controller: TextEditingController(),
                          innerLabel: 'SR45934295',
                          keyboardType: TextInputType.text,
                        ),
                      ),
                    ])
                  ],
                ),
              )
            ]));
  }
}
