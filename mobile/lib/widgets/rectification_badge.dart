import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;

// Mirrors the web RectificationBadge (installation-ui pm/qc/fa components/Custom) — keep the colours in sync.
class RectificationColors {
  static const badgeBackground = Color(0xFFF3EFFA);
  static const badgeBorder = Color(0xFFD8CCF0);
  static const badgeText = Color(0xFF5B3A8E);
  static const cardBackground = Color(0xFFFAF8FD);
}

class RectificationBadge extends StatelessWidget {
  const RectificationBadge({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(6, 2, 8, 2),
      decoration: BoxDecoration(
        color: RectificationColors.badgeBackground,
        border: Border.all(color: RectificationColors.badgeBorder),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.build_outlined,
              size: 12, color: RectificationColors.badgeText),
          const SizedBox(width: 4),
          Text(
            context.translate(i18.common.rectification),
            style: const TextStyle(
              fontSize: 12,
              height: 16 / 12,
              fontWeight: FontWeight.w500,
              letterSpacing: 0.2,
              color: RectificationColors.badgeText,
            ),
          ),
        ],
      ),
    );
  }
}

/// A [DigitCard] that, for rectification sites, gets the tinted treatment and
/// the badge pinned top-left above [child]. DigitCard paints its own white
/// background, so the tint lives on an inner container with zero card padding.
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
            color: RectificationColors.cardBackground,
            border: Border.all(color: RectificationColors.badgeBorder),
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
