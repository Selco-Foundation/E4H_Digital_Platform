import 'package:auto_route/auto_route.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/navigation/navbar.dart';

@RoutePage()
class SpecificationPage extends StatefulWidget {
  const SpecificationPage({super.key});

  @override
  State<SpecificationPage> createState() => _SpecificationPageState();
}

class _SpecificationPageState extends State<SpecificationPage> {
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
                        activeIndex: 1,
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
                        'Inverter Specifications',
                        style: textTheme.headingXl
                            .copyWith(color: theme.colorTheme.primary.primary2),
                      ),
                      LabeledField(
                        label: 'System',
                        labelStyle: textTheme.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        capitalizedFirstLetter: false,
                        child: DigitTextFormInput(
                          controller: TextEditingController(),
                          isDisabled: true,
                          initialValue: 'AC',
                          keyboardType: TextInputType.none,
                        ),
                      ),
                      Row(
                        children: [
                          Expanded(
                            flex: 3,
                            child: LabeledField(
                              label: 'Total Capacity',
                              labelStyle: textTheme.headingS.copyWith(
                                  color: theme.colorTheme.text.primary),
                              capitalizedFirstLetter: false,
                              child: DigitTextFormInput(
                                keyboardType: TextInputType.none,
                                controller: TextEditingController(),
                                isDisabled: true,
                                initialValue: '1',
                              ),
                            ),
                          ),
                          const SizedBox(width: spacer6),
                          Expanded(
                            flex: 1,
                            child: LabeledField(
                              label: 'Unit',
                              labelStyle: textTheme.headingS.copyWith(
                                  color: theme.colorTheme.text.primary),
                              capitalizedFirstLetter: false,
                              child: DigitTextFormInput(
                                controller: TextEditingController(),
                                isDisabled: true,
                                initialValue: 'KvA',
                                keyboardType: TextInputType.text,
                                onChange: (value) {},
                              ),
                            ),
                          ),
                        ],
                      ),
                    ])
                  ],
                ),
              )
            ]));
  }
}
