import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/reactive_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/extensions.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;

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
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: AppBar(
        foregroundColor: theme.colorTheme.paper.primary,
        backgroundColor: theme.colorTheme.primary.primary2,
      ),
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        footer:
            DigitCard(margin: const EdgeInsets.only(top: spacer2), children: [
          DigitButton(
            suffixIcon: Icons.arrow_forward_outlined,
            mainAxisSize: MainAxisSize.max,
            label: context.translate(
              i18.common.coreCommonNext,
            ),
            type: DigitButtonType.primary,
            size: DigitButtonSize.large,
            onPressed: () {
              context.router.replace(const EnterOtpRoute());
            },
          ),
        ]),
        children: [
          ReactiveFormBuilder(
            form: buildForm,
            builder: (context, form, child) {
              return DigitCard(
                  margin: const EdgeInsets.all(spacer2),
                  children: [
                    Text(
                      context.translate(
                        i18.forgotPassword.enterMobileNumberText,
                      ),
                      style: textTheme.headingXl.copyWith(
                        color: theme
                            .colorTheme.primary.primary2, // Use theme color
                      ),
                    ),
                    ReactiveWrapperField(
                      formControlName: _mobileNumber,
                      validationMessages: {
                        "required": (control) {
                          return context.translate(
                            '${i18.login.userIdPlaceholder}_IS_REQUIRED',
                          );
                        },
                      },
                      builder: (field) => LabeledField(
                        label: context.translate(
                          i18.forgotPassword.registeredMobileNumberLabel,
                        ),
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
                        ),
                      ),
                    ),
                  ]);
            },
          ),
        ],
      ),
    );
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _mobileNumber: FormControl<String>(
          value: '',
          validators: [Validators.required],
        ),
      });
}
