import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:selco/utils/asset_images.dart';
import 'package:selco/utils/extensions.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;

class Navbar extends StatelessWidget implements PreferredSizeWidget {
  const Navbar({super.key});

  @override
  Size get preferredSize => const Size.fromHeight(spacer12);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return AppBar(
      foregroundColor: theme.colorTheme.paper.primary,
      backgroundColor: theme.colorTheme.primary.primary2,
      leading: IconButton(
        icon: const Icon(Icons.menu, color: Colors.white, size: spacer6),
        onPressed: () {},
      ),
      title: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Image.asset(
            AssetImages.DIGIT_LOGO,
            height: spacer4,
          ),
          const SizedBox(width: spacer2),
          Container(
            width: 1, // to-do Need to ask about this vertical line
            height: spacer6,
            color: const DigitColors().light.paperPrimary,
          ),
          const SizedBox(width: spacer2), // Reduced spacing
          Text(context.translate(i18.detail.appName),
              style: textTheme.bodyS.copyWith(
                color: const DigitColors().light.paperPrimary,
              )),
        ],
      ),
    );
  }
}
