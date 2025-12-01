import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/user_type/user_type.dart';
import '../../utils/extensions.dart';

Widget AppStepper({activeIndex = 0, required BuildContext context}) {
  return BlocBuilder<UserTypeBloc, UserTypeState>(
    builder: (context, state) {
      final isSupervisor = state.maybeWhen(
        supervisor: () => true,
        orElse: () => false,
      );
      int actualActiveIndex = activeIndex;
      if (!isSupervisor && activeIndex > 2) {
        actualActiveIndex = activeIndex - 1;
      }
      return SizedBox(
        height: spacer8,
        width: context.width * 0.9,
        child: DigitStepper(
          activeIndex: actualActiveIndex,
          stepperList: isSupervisor
              ? [
                  StepperData(onStepTap: () {}),
                  const StepperData(),
                  const StepperData(),
                  const StepperData(),
                  const StepperData(),
                  const StepperData(),
                ]
              : [
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
    },
  );
}
