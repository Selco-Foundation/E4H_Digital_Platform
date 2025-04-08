import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/extensions.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;

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
      });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: AppBar(
        foregroundColor: theme.colorTheme.paper.primary,
        backgroundColor: theme.colorTheme.primary.primary2,
      ),
      body: ReactiveFormBuilder(
        form: buildForm,
        builder: (context, form, child) {
          return ScrollableContent(
            backgroundColor: theme.colorTheme.generic.background,
            footer: DigitCard(
              margin: const EdgeInsets.only(top: spacer2),
              children: [
                DigitButton(
                  mainAxisSize: MainAxisSize.max,
                  label: context.translate(i18.common.coreCommonSave),
                  type: DigitButtonType.primary,
                  size: DigitButtonSize.large,
                  onPressed: () {
                    form.markAllAsTouched();
                    if (!form.valid) return;

                    FocusManager.instance.primaryFocus?.unfocus();
                    context.router.replace(const LoginRoute());
                  },
                ),
              ],
            ),
            children: [
              DigitCard(
                margin: const EdgeInsets.all(spacer2),
                children: [
                  Text(
                    context.translate(i18.forgotPassword.setUpNewPassword),
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
                      label: context.translate(
                        i18.forgotPassword.enterNewPassword,
                      ),
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
                      label: context.translate(
                        i18.forgotPassword.reEnterNewPassword,
                      ),
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
