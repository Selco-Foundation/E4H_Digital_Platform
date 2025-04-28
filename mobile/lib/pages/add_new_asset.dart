import 'dart:io';

import 'package:digit_ui_components/models/DropdownModels.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_dropdown_input.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/upload_image.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

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

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final assetHeading = state.when(
          initial: () => 'Asset',
          inverter: () => 'Inverter',
          battery: () => 'Battery',
          panel: () => 'Panel',
        );
        return Scaffold(
            appBar: const Navbar(),
            body: ScrollableContent(
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                footer: FooterButton(
                  showSuffixIcon: false,
                  text: i18.common.coreCommonNext,
                  onPress: () {
                    context.router.push(const MediaUploadRoute());
                  },
                ),
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: spacer2, vertical: spacer4),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        SizedBox(
                          height: spacer8,
                          width: double.infinity,
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
                            Text(
                              '$assetHeading 1',
                              style: textTheme.headingXl.copyWith(
                                  color: theme.colorTheme.primary.primary2),
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
                                      readOnly: true,
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
      },
    );
  }
}
