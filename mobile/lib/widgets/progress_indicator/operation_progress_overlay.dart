import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../../utils/operation_progress.dart';
import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;

class OperationProgressOverlay extends StatelessWidget {
  final OperationProgressModel? progress;
  final VoidCallback? onClose;

  const OperationProgressOverlay({
    super.key,
    required this.progress,
    this.onClose,
  });

  @override
  Widget build(BuildContext context) {
    final model = progress;
    if (model == null) return const SizedBox.shrink();
    final closeHandler = onClose;

    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final progressValue =
        (model.progressPercent.clamp(0, 100).toDouble() / 100).clamp(0.0, 1.0);
    final showIndeterminate = model.isActive && model.progressPercent <= 0;

    return Positioned.fill(
      child: ColoredBox(
        color: Colors.black45,
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 360),
            child: DigitCard(
              margin: const EdgeInsets.all(spacer1),
              children: [
                Text(
                  operationTitle(model.operationType),
                  textAlign: TextAlign.center,
                  style: textTheme.headingL.copyWith(
                    color: theme.colorTheme.primary.primary2,
                  ),
                ),
                const SizedBox(height: spacer1),
                Text(
                  model.stageLabel,
                  textAlign: TextAlign.center,
                  style: textTheme.bodyL.copyWith(
                    color: theme.colorTheme.text.primary,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: spacer1),
                LinearProgressIndicator(
                  value: showIndeterminate ? null : progressValue,
                  minHeight: spacer3,
                ),
                const SizedBox(height: spacer1),
                Text(
                  '${model.progressPercent}%',
                  textAlign: TextAlign.center,
                  style: textTheme.headingL.copyWith(
                    color: theme.colorTheme.primary.primary2,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: spacer1),
                Text(
                  model.isFailure
                      ? (model.errorMessage ??
                          context
                              .translate(i18.progressOverlay.somethingWentWrong))
                      : context.translate(i18.progressOverlay.pleaseWait),
                  textAlign: TextAlign.center,
                  style: textTheme.bodyS.copyWith(
                    color: model.isFailure
                        ? theme.colorTheme.alert.error
                        : theme.colorTheme.text.secondary,
                  ),
                ),
                if (model.isFailure && closeHandler != null)
                  DigitButton(
                    label: context.translate(i18.progressOverlay.close),
                    type: DigitButtonType.secondary,
                    size: DigitButtonSize.large,
                    mainAxisSize: MainAxisSize.max,
                    onPressed: closeHandler,
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
