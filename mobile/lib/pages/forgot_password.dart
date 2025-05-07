import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
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

    return Scaffold(
      appBar: const Navbar(showMenu: false, showLeading: false),
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        footer: FooterButton(
          text: i18.common.coreCommonNext,
          onPress: () {
            context.router.replace(const EnterOtpRoute());
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
                      "Enter your mobile number",
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
