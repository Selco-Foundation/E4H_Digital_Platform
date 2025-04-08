import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_password_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/reactive_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/extensions.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;
import 'package:selco/widgets/navigation/navbar.dart';

@RoutePage()
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  var passwordVisible = false;
  bool isPrivacyEnabled = false;
  static const _userId = 'userId';
  static const _password = 'password';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        footer: const Padding(
          padding: EdgeInsets.only(bottom: spacer2),
          child: PoweredByDigit(
            version: '',
          ),
        ),
        children: [
          ReactiveFormBuilder(
            form: buildForm,
            builder: (context, form, child) {
              return DigitCard(
                  margin: const EdgeInsets.all(spacer2),
                  children: [
                    Text(
                      context.translate(i18.login.labelText),
                      style: textTheme.headingXl.copyWith(
                        color: theme
                            .colorTheme.primary.primary2, // Use theme color
                      ),
                    ),
                    ReactiveWrapperField(
                      formControlName: _userId,
                      validationMessages: {
                        "required": (control) {
                          return context.translate(
                            '${i18.login.userIdPlaceholder}_IS_REQUIRED',
                          );
                        },
                      },
                      builder: (field) => LabeledField(
                        label: context.translate(
                          i18.login.userIdPlaceholder,
                        ),
                        capitalizedFirstLetter: false,
                        isRequired: true,
                        child: DigitTextFormInput(
                          keyboardType: TextInputType.text,
                          errorMessage: field.errorText,
                          onChange: (value) {
                            form.control(_userId).value = value;
                          },
                        ),
                      ),
                    ),
                    ReactiveWrapperField(
                      formControlName: _password,
                      validationMessages: {
                        "required": (control) {
                          return context.translate(
                            '${i18.login.passwordPlaceholder}_IS_REQUIRED',
                          );
                        },
                      },
                      builder: (field) => LabeledField(
                        label: context.translate(
                          i18.login.passwordPlaceholder,
                        ),
                        isRequired: true,
                        child: DigitPasswordFormInput(
                          errorMessage: field.errorText,
                          onChange: (value) {
                            form.control(_password).value = value;
                          },
                          keyboardType: TextInputType.text,
                        ),
                      ),
                    ),
                    DigitButton(
                      label: context.translate(i18.login.actionLabel),
                      type: DigitButtonType.primary,
                      onPressed: () {
                        form.markAllAsTouched();
                        if (!form.valid) return;

                        FocusManager.instance.primaryFocus?.unfocus();
                        context.router.push(const HomeRoute());
                      },
                      size: DigitButtonSize.large,
                      mainAxisSize: MainAxisSize.max,
                    ),
                    DigitButton(
                        label: context.translate(
                          i18.forgotPassword.actionLabel,
                        ),
                        mainAxisSize: MainAxisSize.max,
                        type: DigitButtonType.tertiary,
                        size: DigitButtonSize.medium,
                        onPressed: () =>
                            context.router.push(const ForgotPasswordRoute())),
                  ]);
            },
          ),
        ],
      ),
    );
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _userId: FormControl<String>(
          value: '',
          validators: [Validators.required],
        ),
        _password: FormControl<String>(
          validators: [Validators.required],
          value: '',
        ),
      });
}
