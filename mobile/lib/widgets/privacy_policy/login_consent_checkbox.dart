import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_checkbox_icon.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

class LoginConsentCheckbox extends StatefulWidget {
  final bool value;
  final ValueChanged<bool> onChanged;
  final VoidCallback onPrivacyPolicyTap;
  final VoidCallback onTermsAndConditionsTap;
  final String prefixText;
  final String privacyPolicyText;
  final String connectorText;
  final String termsAndConditionsText;

  const LoginConsentCheckbox({
    super.key,
    required this.value,
    required this.onChanged,
    required this.onPrivacyPolicyTap,
    required this.onTermsAndConditionsTap,
    required this.prefixText,
    required this.privacyPolicyText,
    required this.connectorText,
    required this.termsAndConditionsText,
  });

  @override
  State<LoginConsentCheckbox> createState() => _LoginConsentCheckboxState();
}

class _LoginConsentCheckboxState extends State<LoginConsentCheckbox> {
  late final TapGestureRecognizer _privacyPolicyRecognizer;
  late final TapGestureRecognizer _termsAndConditionsRecognizer;

  @override
  void initState() {
    super.initState();
    _privacyPolicyRecognizer = TapGestureRecognizer()
      ..onTap = widget.onPrivacyPolicyTap;
    _termsAndConditionsRecognizer = TapGestureRecognizer()
      ..onTap = widget.onTermsAndConditionsTap;
  }

  @override
  void didUpdateWidget(covariant LoginConsentCheckbox oldWidget) {
    super.didUpdateWidget(oldWidget);
    _privacyPolicyRecognizer.onTap = widget.onPrivacyPolicyTap;
    _termsAndConditionsRecognizer.onTap = widget.onTermsAndConditionsTap;
  }

  @override
  void dispose() {
    _privacyPolicyRecognizer.dispose();
    _termsAndConditionsRecognizer.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final bodyStyle = textTheme.bodyL.copyWith(
      color: theme.colorTheme.text.primary,
    );
    final linkStyle = bodyStyle.copyWith(
      color: theme.colorTheme.primary.primary1,
      decoration: TextDecoration.underline,
      decorationColor: theme.colorTheme.primary.primary1,
    );

    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        InkWell(
          key: const ValueKey('login-consent-checkbox'),
          onTap: () => widget.onChanged(!widget.value),
          child: DigitCheckboxIcon(
            state: widget.value
                ? DigitCheckboxState.checked
                : DigitCheckboxState.unchecked,
          ),
        ),
        const SizedBox(width: spacer2),
        Expanded(
          child: Text.rich(
            TextSpan(
              style: bodyStyle,
              children: [
                TextSpan(text: '${widget.prefixText.trim()} '),
                TextSpan(
                  text: widget.privacyPolicyText.trim(),
                  style: linkStyle,
                  recognizer: _privacyPolicyRecognizer,
                ),
                TextSpan(text: ' ${widget.connectorText.trim()} '),
                TextSpan(
                  text: widget.termsAndConditionsText.trim(),
                  style: linkStyle,
                  recognizer: _termsAndConditionsRecognizer,
                ),
                const TextSpan(text: '.'),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
