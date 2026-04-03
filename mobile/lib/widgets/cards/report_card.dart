import 'package:badges/badges.dart' as badges;
import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

class ReportCard extends StatelessWidget {
  final IconData icon;
  final String heading;
  final String description;
  final int? badgeCount;
  final Function() onPress;

  const ReportCard({
    super.key,
    required this.icon,
    required this.heading,
    required this.description,
    this.badgeCount = 0,
    required this.onPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return SizedBox(
      height: 3 * spacer11,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: spacer2),
        child: DigitCard(
          onPressed: onPress,
          margin: const EdgeInsets.only(bottom: spacer1),
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      icon,
                      color: const DigitColors().light.alertError,
                      size: spacer8,
                    ),
                    const SizedBox(width: spacer2),
                    Text(
                      heading,
                      style: textTheme.headingL.copyWith(
                        color: const DigitColors().light.primary2,
                      ),
                    ),
                    const Spacer(),
                    if (badgeCount! > 0)
                      badges.Badge(
                        badgeStyle: badges.BadgeStyle(
                          shape: badges.BadgeShape.square,
                          badgeColor: theme.colorTheme.alert.error,
                          padding: const EdgeInsets.symmetric(
                              horizontal: spacer3, vertical: spacer1),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        badgeContent: Text("$badgeCount",
                            style: textTheme.bodyS.copyWith(
                                color: theme.colorTheme.paper.primary)),
                      ),
                  ],
                ),
                const SizedBox(height: spacer3),
                Text(
                  description,
                  style: textTheme.bodyS
                      .copyWith(color: const DigitColors().light.textPrimary),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
