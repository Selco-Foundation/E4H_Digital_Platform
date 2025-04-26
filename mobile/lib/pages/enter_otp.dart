import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_otp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class EnterOtpPage extends StatefulWidget {
  const EnterOtpPage({super.key});

  @override
  State<EnterOtpPage> createState() => _EnterOtpPageState();
}

class _EnterOtpPageState extends State<EnterOtpPage> {
  static const _otp = "";
  final TextEditingController otpController = TextEditingController();
  bool next = false;
  final FocusNode pinFocusNode = FocusNode();

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(showMenu: false, showLeading: false),
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        footer: FooterButton(
          showSuffixIcon: false,
          text: i18.common.coreCommonSubmit,
          onPress: () {
            context.router.replace(const SetupNewPasswordRoute());
          },
        ),
        children: [
          ReactiveFormBuilder(
            form: buildForm,
            builder: (context, form, child) {
              return DigitCard(
                  margin: const EdgeInsets.all(spacer2),
                  children: [
                    Text(
                      context.translate(
                        i18.forgotPassword.otpVerfication,
                      ),
                      style: textTheme.headingXl.copyWith(
                          color: theme
                              .colorTheme.primary.primary2 // Use theme color
                          ),
                    ),
                    SizedBox(
                      width: context.width,
                      child: DigitOTPInput(
                        label: context.translate(i18.forgotPassword.enterOtp),
                        onChanged: (input) {},
                      ),
                    )
                  ]);
            },
          ),
        ],
      ),
    );
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _otp: FormControl<String>(
          value: '',
          validators: [Validators.required],
        ),
      });
}
