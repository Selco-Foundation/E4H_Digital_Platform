import 'dart:io';

import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/DropdownModels.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_dropdown_input.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/upload_image.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/widgets/header/back_navigation_help_header.dart';
import 'package:selco/widgets/navigation/navbar.dart';

@RoutePage()
class AddNewAssetPage extends StatefulWidget {
  const AddNewAssetPage({super.key});

  @override
  State<AddNewAssetPage> createState() => _AddNewAssetPageState();
}

class _AddNewAssetPageState extends State<AddNewAssetPage> {
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
                        context.router.push(const AssetTypeDetailRoute()),
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
                        activeIndex: 3,
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
                    DigitCard(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              'Inverter 1',
                              style: textTheme.headingXl.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                          ],
                        ),
                        LabeledField(
                          label: 'Scan Serial No',
                          child: ImageUploader(
                            onImagesSelected: (List<File> imageFile) {},
                          ),
                        ),
                        LabeledField(
                          label: 'Serial Number',
                          capitalizedFirstLetter: false,
                          child: DigitTextFormInput(
                            controller: TextEditingController(),
                            isDisabled: true,
                            innerLabel: 'SR5955340958',
                            keyboardType: TextInputType.none,
                          ),
                        ),
                        Row(
                          children: [
                            const Expanded(
                              flex: 3,
                              child: LabeledField(
                                label: 'Capacity',
                                capitalizedFirstLetter: false,
                                child: DigitDropdown(items: [
                                  DropdownItem(name: '1', code: '1'),
                                  DropdownItem(name: '2', code: '2'),
                                  DropdownItem(name: '3', code: '3')
                                ]),
                              ),
                            ),
                            const SizedBox(width: spacer6),
                            Expanded(
                              flex: 1,
                              child: LabeledField(
                                label: 'Unit',
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
                      ],
                    ),
                    const SizedBox(height: spacer3),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          'Add New Asset',
                          style: textTheme.headingM.copyWith(
                              color: theme.colorTheme.primary.primary1),
                        ),
                      ],
                    ),
                  ],
                ),
              )
            ]));
  }
}
