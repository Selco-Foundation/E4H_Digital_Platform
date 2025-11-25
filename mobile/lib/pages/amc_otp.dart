import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_otp.dart';
import 'package:digit_ui_components/widgets/atoms/reactive_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';

@RoutePage()
class AmcOtpPage extends StatefulWidget {
  const AmcOtpPage({super.key});

  @override
  State<AmcOtpPage> createState() => _AmcOtpPageState();
}

class _AmcOtpPageState extends State<AmcOtpPage> {
  static const _otp = "otp";
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

    return ReactiveFormBuilder(
        form: buildForm,
        builder: (context, form, child) {
          return Scaffold(
            body: ScrollableContent(
              enableFixedDigitButton: true,
              backgroundColor: theme.colorTheme.generic.background,
              // footer: FooterButton(
              //   isDisabled: !form.valid,
              //   showSuffixIcon: false,
              //   text: context.translate(i18.common.coreCommonSubmit),
              //   onPress: () {
              //     form.markAllAsTouched();
              //     if (!form.valid) return;
              //
              //     FocusManager.instance.primaryFocus?.unfocus();
              //     // context.read<UserOtpBloc>().add(UserOtpEvent.storeOtp(
              //     //     otp: (form.control(_otp).value as String).trim()));
              //   },
              // ),
              footer: ReactiveFormConsumer(
                builder: (context, form, child) {
                  return FooterButton(
                    isDisabled: !form.valid,
                    showSuffixIcon: false,
                    text: context.translate(i18.common.coreCommonSubmit),
                    onPress: () {
                      form.markAllAsTouched();
                      if (!form.valid) return;

                      FocusManager.instance.primaryFocus?.unfocus();
                      context.router.push(const AmcHomeRoute());
                      // context.read<UserOtpBloc>().add(
                      //   UserOtpEvent.storeOtp(
                      //     otp: (form.control(_otp).value as String).trim(),
                      //   ),
                      // );
                    },
                  );
                },
              ),
              children: [
                DigitCard(
                  margin: const EdgeInsets.all(spacer2),
                  children: [
                    const SizedBox(height: spacer6),
                    SizedBox(
                      width: context.width,
                      child: ReactiveWrapperField(
                        formControlName: _otp,
                        validationMessages: {
                          "required": (control) {
                            return context.translate(
                              '${i18.login.otpPlaceholder}_IS_REQUIRED',
                            );
                          },
                        },
                        builder: (field) => DigitOTPInput(
                          label: "Enter OTP",
                          inputFormatter: [
                            FilteringTextInputFormatter.digitsOnly
                          ],
                          errorMessage: field.errorText,
                          onChanged: (input) {
                            // form.control(_otp).value = input;
                            field.didChange(input);
                          },
                        ),
                      ),
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          textAlign: TextAlign.end,
                          "Resend OTP",
                          style: textTheme.linkM
                              .copyWith(color: theme.colorTheme.alert.error),
                        ),
                      ],
                    ),
                  ],
                )
              ],
            ),
          );
        });
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _otp: FormControl<String>(
          value: '',
          validators: [
            Validators.required,
            Validators.minLength(4),
            Validators.maxLength(4)
          ],
        ),
      });
}
