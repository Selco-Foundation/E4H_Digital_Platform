import 'package:digit_ui_components/digit_components.dart';
import 'package:flutter/material.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = DigitTheme.instance;
    final textTheme = theme.mobileTheme.textTheme;

    return Drawer(
      child: ScrollableContent(
        header: Container(
          color: Light().genericBackground,
          padding: const EdgeInsets.all(spacer1),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: spacer1 * 13),
            child: Column(
              children: [
                Text("First Last", style: textTheme.displayMedium),
                Text("1234567890", style: textTheme.headlineLarge),
              ],
            ),
          ),
        ),
        footer: Padding(
          padding: const EdgeInsets.all(spacer1),
          child: const PoweredByDigit(
            version: '',
          ),
        ),
        children: [
          // DigitIconTile(
          //   title: AppTranslation.HOME.tr,
          //   onPressed: () {
          //     if (Get.currentRoute != HOME) {
          //       Get.toNamed(HOME);
          //     } else {
          //       Get.back();
          //     }
          //   },
          //   icon: Icons.home,
          // ),
          // DigitIconTile(
          //   title: Get.locale == ENG_LOCALE ? AppTranslation.ENGLISH.tr : AppTranslation.ODIA.tr,
          //   onPressed: () {},
          //   content: const LanguageButtonsWidget(),
          //   icon: Icons.language,
          // ),
          // DigitIconTile(
          //   title: AppTranslation.LOGOUT.tr,
          //   onPressed: logout,
          //   icon: Icons.logout,
          // ),
        ],
      ),
    );
  }
}

class LanguageButtonsWidget extends StatelessWidget {
  const LanguageButtonsWidget({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        DigitButton(
          type: DigitButtonType.secondary,
          label: "ENGLISH",
          onPressed: () {},
          size: DigitButtonSize.large,
        ),
      ],
    );
  }
}
