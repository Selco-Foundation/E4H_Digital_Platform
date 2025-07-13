import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';

Widget videoCard({required BuildContext context, filePath}) {
  final textTheme = Theme.of(context).digitTextTheme(context);
  return Padding(
    padding: const EdgeInsets.only(bottom: spacer3),
    child: Row(
      children: [
        Icon(Icons.play_circle_fill,
            color: Theme.of(context).colorTheme.primary.primary1),
        const SizedBox(width: spacer2),
        Text(
          filePath,
          style: textTheme.bodyS.copyWith(
            color: Theme.of(context).colorTheme.text.primary,
          ),
        ),
      ],
    ),
  );
}
