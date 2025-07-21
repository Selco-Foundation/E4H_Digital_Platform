import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/theme.dart';
import 'package:flutter/material.dart';
import 'package:selco/utils/utils.dart';

Widget pdfCard(
    {required BuildContext context, String? filePath, String? fileSize}) {
  final theme = Theme.of(context);
  final textTheme = theme.digitTextTheme(context);
  return GestureDetector(
    onTap: () => {}, // OpenFile.open(filePath),
    child: Container(
      decoration: BoxDecoration(
        color: theme.colorTheme.generic.background,
        border: Border.all(color: theme.colorTheme.generic.divider),
        borderRadius: BorderRadius.circular(spacer1),
      ),
      child: Padding(
        padding: const EdgeInsets.all(spacer4),
        child: Row(
          children: [
            Icon(Icons.picture_as_pdf,
                color: theme.colorTheme.primary.primary1, size: spacer9),
            const SizedBox(width: spacer3),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "${truncateTextFromStart(filePath ?? "", maxLength: 32)}",
                  style: textTheme.headingM.copyWith(
                      color: theme.colorTheme.text.primary, fontSize: spacer4),
                ),
                const SizedBox(height: spacer1),
                Text(
                  fileSize ?? "",
                  style: textTheme.headingM.copyWith(
                      color: theme.colorTheme.text.secondary,
                      fontSize: spacer3),
                ),
              ],
            )
          ],
        ),
      ),
    ),
  );
}
