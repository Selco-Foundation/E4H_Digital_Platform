import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;

// Uses DIGIT's Info pair (alert.info / alert.infoBg), like the web
// RectificationBadge (installation-ui pm/qc/fa components/Custom): rectification
// is extra information, not a status.
class RectificationBadge extends StatelessWidget {
  const RectificationBadge({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final alert = theme.colorTheme.alert;

    return Container(
      padding:
          const EdgeInsets.symmetric(horizontal: spacer2, vertical: spacer1),
      decoration: BoxDecoration(
        color: alert.infoBg,
        border: Border.all(color: alert.info),
        borderRadius: BorderRadius.circular(spacer1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.build_outlined, size: spacer3, color: alert.info),
          const SizedBox(width: spacer1),
          Text(
            context.translate(i18.common.rectification),
            style: theme
                .digitTextTheme(context)
                .bodyXS
                .copyWith(color: alert.info),
          ),
        ],
      ),
    );
  }
}

/// A [DigitCard] that, for rectification sites, gets an Info border and the
/// badge pinned top-left above [child]. The card stays white: the orange
/// secondary buttons on these cards fall below AA contrast on alert.infoBg
/// (the same reason the web leaves table rows untinted). DigitCard only draws
/// a border for its flat secondary type, so the border lives on an inner
/// container with zero card padding.
class RectificationAwareCard extends StatelessWidget {
  final bool isRectification;
  final Widget child;

  const RectificationAwareCard({
    super.key,
    required this.isRectification,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    if (!isRectification) return DigitCard(children: [child]);

    return DigitCard(
      padding: EdgeInsets.zero,
      children: [
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(spacer4),
          decoration: BoxDecoration(
            border: Border.all(color: Theme.of(context).colorTheme.alert.info),
            borderRadius: BorderRadius.circular(spacer1),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const RectificationBadge(),
              const SizedBox(height: spacer2),
              child,
            ],
          ),
        ),
      ],
    );
  }
}
