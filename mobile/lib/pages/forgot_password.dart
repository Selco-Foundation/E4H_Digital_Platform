import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/reactive_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../blocs/auth/user_otp.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class ForgotPasswordPage extends StatefulWidget {
  const ForgotPasswordPage({super.key});

  @override
  State<ForgotPasswordPage> createState() => _ForgotPasswordPageState();
}

class _ForgotPasswordPageState extends State<ForgotPasswordPage> {
  static const _mobileNumber = '';
  final TextEditingController otpController = TextEditingController();
  bool next = false;
  final FocusNode pinFocusNode = FocusNode();

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
              footer: BlocConsumer<UserOtpBloc, UserOtpState>(
                listener: (context, state) {
                  state.whenOrNull(
                      error: (message) {
                        context.showSnackBar(SnackBar(
                          content: Text(message),
                          backgroundColor: const Light().alertError,
                        ));
                      },
                      sent: () =>
                          context.router.replace(const EnterOtpRoute()));
                },
                builder: (context, state) {
                  return state.maybeWhen(
                      loading: () => LoadingFooterButton(),
                      orElse: () => FooterButton(
                            isDisabled: !form.valid,
                            text: context.translate(i18.common.coreCommonNext),
                            onPress: () {
                              form.markAllAsTouched();
                              if (!form.valid) return;

                              FocusManager.instance.primaryFocus?.unfocus();
                              context.read<UserOtpBloc>().add(
                                  UserOtpEvent.sendOtp(
                                      phone: (form.control(_mobileNumber).value
                                              as String)
                                          .trim()));
                            },
                          ));
                },
              ),
              children: [
                DigitCard(margin: const EdgeInsets.all(spacer2), children: [
                  Text(
                    "Enter your mobile number",
                    style: textTheme.headingXl.copyWith(
                      color: theme.colorTheme.primary.primary2,
                    ),
                  ),
                  ReactiveWrapperField(
                    formControlName: _mobileNumber,
                    validationMessages: {
                      "required": (control) {
                        return context.translate(
                          '${i18.login.mobileNumberPlaceholder}_IS_REQUIRED',
                        );
                      },
                    },
                    builder: (field) => LabeledField(
                      label: "Registered mobile number",
                      capitalizedFirstLetter: false,
                      isRequired: true,
                      child: InputField(
                        type: InputType.text,
                        controller: TextEditingController(),
                        prefixText: '+91',
                        inputFormatters: [
                          FilteringTextInputFormatter.digitsOnly
                        ],
                        editable: true,
                        errorMessage: field.errorText,
                        onChange: (value) {
                          form.control(_mobileNumber).value = value;
                        },
                      ),
                    ),
                  ),
                ])
              ],
            ),
          );
        });
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _mobileNumber: FormControl<String>(
          value: '',
          validators: [
            Validators.required,
            Validators.minLength(10),
            Validators.maxLength(10)
          ],
        ),
      });
}
