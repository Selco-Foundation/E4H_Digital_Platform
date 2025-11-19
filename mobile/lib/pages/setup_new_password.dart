import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
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
class SetupNewPasswordPage extends StatefulWidget {
  const SetupNewPasswordPage({super.key});

  @override
  State<SetupNewPasswordPage> createState() => _SetupNewPasswordPageState();
}

class _SetupNewPasswordPageState extends State<SetupNewPasswordPage> {
  var passwordVisible = false;
  static const _password = 'password';
  static const _reEnteredPassword = 're-entered password';

  FormGroup buildForm() => fb.group(<String, Object>{
        _reEnteredPassword: FormControl<String>(
          value: '',
          validators: [Validators.required],
        ),
        _password: FormControl<String>(
          validators: [Validators.required],
          value: '',
        ),
      }, [
        Validators.mustMatch(_password, _reEnteredPassword)
      ]);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(showMenu: false, showLeading: false),
      body: ReactiveFormBuilder(
        form: buildForm,
        builder: (context, form, child) {
          return ScrollableContent(
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
                    success: () => context.router.replace(const LoginRoute()));
              },
              builder: (context, state) {
                return state.maybeWhen(
                    loading: () => LoadingFooterButton(),
                    orElse: () {
                      return FooterButton(
                        showSuffixIcon: false,
                        isDisabled: !form.valid,
                        text: "Save",
                        onPress: () {
                          form.markAllAsTouched();
                          if (!form.valid) return;

                          FocusManager.instance.primaryFocus?.unfocus();
                          context.read<UserOtpBloc>().add(
                              UserOtpEvent.resetPassword(
                                  newPassword: (form
                                          .control(_reEnteredPassword)
                                          .value as String)
                                      .trim()));
                        },
                      );
                    });
              },
            ),
            children: [
              DigitCard(
                margin: const EdgeInsets.all(spacer2),
                children: [
                  Text(
                    "Setup your new password",
                    style: textTheme.headingXl.copyWith(
                      color: theme.colorTheme.primary.primary2,
                    ),
                  ),
                  ReactiveWrapperField(
                    formControlName: _password,
                    validationMessages: {
                      "required": (control) => context.translate(
                            '${i18.login.passwordPlaceholder}_IS_REQUIRED',
                          ),
                    },
                    builder: (field) => LabeledField(
                      label: "Enter new password",
                      isRequired: true,
                      child: DigitPasswordFormInput(
                        errorMessage: field.errorText,
                        onChange: (value) =>
                            form.control(_password).value = value,
                        keyboardType: TextInputType.text,
                      ),
                    ),
                  ),
                  ReactiveWrapperField(
                    formControlName: _reEnteredPassword,
                    validationMessages: {
                      "required": (control) => context.translate(
                            '${i18.login.passwordPlaceholder}_IS_REQUIRED',
                          ),
                    },
                    builder: (field) => LabeledField(
                      label: "Re-enter new password",
                      isRequired: true,
                      child: DigitPasswordFormInput(
                        errorMessage: field.errorText,
                        onChange: (value) =>
                            form.control(_reEnteredPassword).value = value,
                        keyboardType: TextInputType.text,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          );
        },
      ),
    );
  }
}
