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
        body: BlocBuilder<AssetTypeBloc, AssetTypeState>(
          builder: (context, state) {
            return ScrollableContent(
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                footer: FooterButton(
                  showSuffixIcon: false,
                  text: i18.common.coreCommonNext,
                  isDisabled: state is AssetTypeInitial,
                  onPress: () {
                    context.router.push(const SpecificationRoute());
                  },
                ),
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: spacer2, vertical: spacer4),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(
                          height: spacer8,
                          width: double.infinity,
                          child: DigitStepper(
                            activeIndex: 0,
                            stepperList: [
                              StepperData(),
                              StepperData(),
                              StepperData(),
                              StepperData(),
                              StepperData(),
                            ],
                            stepperDirection: Axis.horizontal,
                            inverted: true,
                          ),
                        ),
                        const SizedBox(height: spacer4),
                        DigitCard(children: [
                          Text(
                            'Asset Type',
                            style: textTheme.headingXl.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                          Text(
                            'Choose the asset type',
                            style: textTheme.bodyL
                                .copyWith(color: theme.colorTheme.text.primary),
                          ),
                          LabeledField(
                            label: 'Select Asset Type',
                            labelStyle: textTheme.label.copyWith(
                              color: theme.colorTheme.text.primary,
                            ),
                            capitalizedFirstLetter: false,
                            child: DigitDropdown(
                              onSelect: (DropdownItem selected) {
                                context.read<AssetTypeBloc>().add(
                                    AssetTypeEvent.typeSelected(selected.code));
                              },
                              items: const [
                                DropdownItem(
                                    name: 'Inverter', code: 'inverter'),
                                DropdownItem(name: 'Battery', code: 'Battery'),
                                DropdownItem(name: 'Panel', code: 'Panel'),
                              ],
                            ),
                          ),
                        ])
                      ],
                    ),
                  )
                ]);
          },
        ));
  }
}
