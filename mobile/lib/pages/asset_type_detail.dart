import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

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

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final detailHeading = state.when(
          initial: () => 'Details',
          inverter: () => 'Inverter Details',
          battery: () => 'Battery Details',
          panel: () => 'Panel Details',
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
                    context.router.push(const AddNewAssetRoute());
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
                            detailHeading,
                            style: textTheme.headingXl.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                          LabeledField(
                            label: 'Count',
                            labelStyle: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                            capitalizedFirstLetter: false,
                            child: DigitDropdown(
                              onSelect: (DropdownItem selected) {
                                // debugPrint("selected ${selected.code}");
                              },
                              items: const [
                                DropdownItem(name: '1', code: '1'),
                                DropdownItem(name: '2', code: '2'),
                                DropdownItem(name: '3', code: '3')
                              ],
                            ),
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
                              readOnly: true,
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
      },
    );
  }
}
