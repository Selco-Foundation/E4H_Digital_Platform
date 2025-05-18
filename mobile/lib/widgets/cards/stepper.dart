import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:flutter/material.dart';
import 'package:selco/utils/extensions.dart';

Widget AppStepper({activeIndex = 0, required BuildContext context}) {
  return SizedBox(
    height: spacer8,
    width: context.width * 0.9,
    child: DigitStepper(
      activeIndex: activeIndex,
      stepperList: [
        StepperData(
          onStepTap: () {},
        ),
        const StepperData(),
        const StepperData(),
        const StepperData(),
        const StepperData(),
        const StepperData(),
      ],
      stepperDirection: Axis.horizontal,
      inverted: true,
    ),
  );
}
