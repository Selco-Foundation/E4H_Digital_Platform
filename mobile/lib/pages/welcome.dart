import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/asset_images.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class WelcomePage extends StatefulWidget {
  const WelcomePage({super.key});
  @override
  State<WelcomePage> createState() => _WelcomePageState();
}

class _WelcomePageState extends State<WelcomePage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: const Navbar(showMenu: false),
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        footer: FooterButton(
            text: "Proceed",
            onPress: () {
              context.router.replace(const LoginRoute());
            }),
        children: const [
          Expanded(
            child: WelcomeContent(),
          ),
        ],
      ),
    );
  }
}

class MenuItem {
  final String title;
  final String description;
  final String imagePath;

  const MenuItem(
      {required this.title,
      required this.description,
      required this.imagePath});
}

class WelcomeContent extends StatelessWidget {
  const WelcomeContent({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    const List<MenuItem> menuItems = [
      MenuItem(
        title: "View Health Facilities",
        description:
            "You can view the health facilities assigned to you for installation",
        imagePath: AssetImages.WELCOME_1,
      ),
      MenuItem(
        title: "Create Reports",
        description:
            "Create installation reports for the health facilities assigned to you (online and offline)",
        imagePath: AssetImages.WELCOME_2,
      ),
      MenuItem(
        title: "Save Reports",
        description:
            "Save installation reports offline until ready for submission",
        imagePath: AssetImages.WELCOME_3,
      ),
      MenuItem(
        title: "Submit for Approval",
        description:
            "Save installation reports offline until ready for submission",
        imagePath: AssetImages.WELCOME_4,
      ),
      MenuItem(
        title: "Edit Reports",
        description:
            "Save installation reports offline until ready for submission",
        imagePath: AssetImages.WELCOME_5,
      ),
    ];
    final lastIndex = menuItems.length - 1;

    return Padding(
      padding:
          const EdgeInsets.symmetric(vertical: spacer4, horizontal: spacer2),
      child: DigitCard(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Welcome!',
                  style: textTheme.headingXl
                      .copyWith(color: const DigitColors().light.primary2)),
              const SizedBox(height: spacer3),
              Text(
                'Through this application you will be able to:',
                style: textTheme.bodyL,
              ),
              // ...menuItems.map((item) => Column(
              ...menuItems.asMap().entries.map((entry) {
                final index = entry.key;
                final item = entry.value;

                return Column(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(vertical: spacer5),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          Image.asset(
                            item.imagePath,
                            height: spacer12 * 2,
                            width: spacer12 * 2,
                          ),
                          const SizedBox(width: spacer6),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(item.title,
                                    style: textTheme.headingS.copyWith(
                                        color: DigitColors().light.primary2)),
                                const SizedBox(height: spacer3),
                                Padding(
                                  padding:
                                      const EdgeInsets.only(right: spacer7),
                                  child: Text(
                                    item.description,
                                    style: textTheme.headingXS.copyWith(
                                        color: const DigitColors()
                                            .light
                                            .textSecondary),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    if (index != lastIndex)
                      const DigitDivider(dividerType: DividerType.small),
                  ],
                );
              })
            ],
          ),
        ],
      ),
    );
  }
}
