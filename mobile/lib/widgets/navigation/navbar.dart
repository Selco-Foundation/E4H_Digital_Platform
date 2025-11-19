import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';

class Navbar extends StatelessWidget implements PreferredSizeWidget {
  final bool showMenu;
  final bool showLeading;
  const Navbar({super.key, this.showMenu = true, this.showLeading = true});

  @override
  Size get preferredSize => const Size.fromHeight(spacer12);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return AppBar(
      automaticallyImplyLeading: showLeading,
      foregroundColor: theme.colorTheme.paper.primary,
      backgroundColor: theme.colorTheme.primary.primary2,
      leading: showMenu
          ? GestureDetector(
              onTap: () {},
              child: IconButton(
                icon:
                    const Icon(Icons.menu, color: Colors.white, size: spacer6),
                onPressed: () {
                  Scaffold.of(context).openDrawer();
                },
              ),
            )
          : null,
      title: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Text("E4H",
              style: textTheme.headingM.copyWith(
                color: const DigitColors().light.paperPrimary,
              )),
          const SizedBox(width: spacer2),
          Container(
            width: 1,
            height: spacer6,
            color: const DigitColors().light.paperPrimary,
          ),
          const SizedBox(width: spacer2),
          Text("Asset Management",
              style: textTheme.bodyS.copyWith(
                color: const DigitColors().light.paperPrimary,
              )),
        ],
      ),
    );
  }
}
