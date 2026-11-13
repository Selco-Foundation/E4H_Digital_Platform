import 'package:digit_ui_components/theme/ComponentTheme/toast_theme_data.dart';
import 'package:flutter/material.dart';

import '../../utils/extensions.dart';

/// Uses the existing Digit error-toast theme, but is owned by the scanner route.
/// Text wraps and keeps backend capitalization; closing only dismisses this toast.
class ScannerErrorToast extends StatelessWidget {
  final String messageKey;
  final VoidCallback onDismiss;
  const ScannerErrorToast(
      {super.key, required this.messageKey, required this.onDismiss});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final defaults = DigitToastThemeData.defaultTheme(context);
    final toast = theme.extension<DigitToastThemeData>();
    return Material(
      key: const Key('scanner-error-toast'),
      color: toast?.errorColor ?? defaults.errorColor,
      child: Padding(
        padding: toast?.padding ?? defaults.padding!,
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          toast?.errorIcon ?? defaults.errorIcon!,
          const SizedBox(width: 8),
          Expanded(
              child: Text(context.translate(messageKey),
                  style: toast?.textStyle ?? defaults.textStyle)),
          IconButton(
              tooltip: context.translate('CORE_COMMON_CLOSE'),
              onPressed: onDismiss,
              icon: toast?.cancelIcon ?? defaults.cancelIcon!),
        ]),
      ),
    );
  }
}
