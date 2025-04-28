import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
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

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final specHeading = state.when(
          initial: () => 'Specification',
          inverter: () => 'Inverter Specifications',
          battery: () => 'Battery Specifications',
          panel: () => 'Panel Specifications',
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
                    context.router.push(const AssetTypeDetailRoute());
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
                            activeIndex: 1,
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
                            specHeading,
                            style: textTheme.headingXl.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                          LabeledField(
                            label: 'System',
                            labelStyle: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                            capitalizedFirstLetter: false,
                            child: DigitTextFormInput(
                              controller: TextEditingController(),
                              isDisabled: true,
                              readOnly: true,
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
                                    readOnly: true,
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
                                    readOnly: true,
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
      },
    );
  }
}
