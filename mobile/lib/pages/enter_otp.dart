import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_otp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:selco/blocs/auth/user_otp.dart';

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

    return ReactiveFormBuilder(
        form: buildForm,
        builder: (context, form, child) {
          return Scaffold(
            appBar: const Navbar(showMenu: false, showLeading: false),
            body: ScrollableContent(
              enableFixedDigitButton: true,
              backgroundColor: theme.colorTheme.generic.background,
              footer: BlocListener<UserOtpBloc, UserOtpState>(
                listener: (context, state) {
                  state.whenOrNull(
                      error: (message) {
                        context.showSnackBar(SnackBar(
                          content: Text(message),
                          backgroundColor: const Light().alertError,
                        ));
                      },
                      otpStored: (otp) => context.router
                          .replace(const SetupNewPasswordRoute()));
                },
                child: FooterButton(
                  isDisabled: !form.valid,
                  showSuffixIcon: false,
                  text: "Submit",
                  onPress: () {
                    form.markAllAsTouched();
                    if (!form.valid) return;

                    FocusManager.instance.primaryFocus?.unfocus();
                    context.read<UserOtpBloc>().add(UserOtpEvent.storeOtp(
                        otp: (form.control(_otp).value as String).trim()));
                  },
                ),
              ),
              children: [
                DigitCard(
                  margin: const EdgeInsets.all(spacer2),
                  children: [
                    Text(
                      "OTP Verification",
                      style: textTheme.headingXl.copyWith(
                          color: theme
                              .colorTheme.primary.primary2 // Use theme color
                          ),
                    ),
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
                          errorMessage: field.errorText,
                          onChanged: (input) {
                            form.control(_otp).value = input;
                          },
                        ),
                      ),
                    )
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
