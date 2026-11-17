import 'package:flutter/material.dart';

import '../blocs/localization/app_localization.dart';
import '../blocs/localization/localization.dart';

class LocalizationGate extends StatelessWidget {
  final LocalizationState state;
  final VoidCallback onRetry;
  final Widget child;

  const LocalizationGate(
      {super.key,
      required this.state,
      required this.onRetry,
      required this.child});

  @override
  Widget build(BuildContext context) {
    final translations = AppLocalizations.of(context);
    final ready = state.maybeWhen(
      selected: (locale, _) =>
          translations.hasTranslations &&
          translations.locale.toString() == locale,
      orElse: () => false,
    );
    if (ready) return child;
    return LocalizationLoadingScreen(
      failed: state.maybeWhen(failure: (_) => true, orElse: () => false),
      onRetry: onRetry,
    );
  }
}

class LocalizationLoadingScreen extends StatefulWidget {
  final bool failed;
  final VoidCallback? onRetry;

  const LocalizationLoadingScreen(
      {super.key, this.failed = false, this.onRetry});

  @override
  State<LocalizationLoadingScreen> createState() =>
      _LocalizationLoadingScreenState();
}

class _LocalizationLoadingScreenState extends State<LocalizationLoadingScreen> {
  @override
  void initState() {
    super.initState();
    if (widget.failed) _showFailure();
  }

  @override
  void didUpdateWidget(LocalizationLoadingScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.failed && !oldWidget.failed) _showFailure();
  }

  void _showFailure() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !widget.failed) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to load localization')),
      );
    });
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        body: SafeArea(
            child: Center(
                child: widget.failed
                    ? FilledButton(
                        onPressed: () {
                          ScaffoldMessenger.of(context).hideCurrentSnackBar();
                          widget.onRetry?.call();
                        },
                        child: const Text('Retry'))
                    : const Column(mainAxisSize: MainAxisSize.min, children: [
                        CircularProgressIndicator(),
                        SizedBox(height: 16),
                        Text('Loading…'),
                      ]))),
      );
}
