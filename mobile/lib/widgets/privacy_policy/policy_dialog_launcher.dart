import 'package:digit_ui_components/theme/colors.dart';
import 'package:flutter/material.dart';

import '../../utils/envConfig.dart';
import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;
import 'policy_webview_dialog.dart';

Future<void> showPrivacyPolicy(BuildContext context) {
  return _showPolicy(
    context,
    title: context.translate(i18.login.privacyPolicy),
    relativePath: envConfig.variables.privacyPolicyUrl,
  );
}

Future<void> showTermsAndConditions(BuildContext context) {
  return _showPolicy(
    context,
    title: context.translate(i18.login.termsAndConditions),
    relativePath: envConfig.variables.termsAndConditionsUrl,
  );
}

Future<void> _showPolicy(
  BuildContext context, {
  required String title,
  required String relativePath,
}) async {
  final uri = buildEnvironmentUrl(
    envConfig.variables.baseUrl,
    relativePath,
  );

  if (uri == null) {
    context.showSnackBar(
      SnackBar(
        content: Text(
          context.translate(i18.login.policyUrlNotConfigured),
        ),
        backgroundColor: const Light().alertError,
      ),
    );
    return;
  }

  await showDialog<void>(
    context: context,
    useSafeArea: false,
    builder: (_) => PolicyWebViewDialog(
      title: title,
      uri: uri,
    ),
  );
}
