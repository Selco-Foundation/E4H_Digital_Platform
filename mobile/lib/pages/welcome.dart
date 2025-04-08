import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:selco/router/app_router.dart';
import 'package:selco/utils/asset_images.dart';
import 'package:selco/utils/extensions.dart';
import 'package:selco/utils/i18_key_constants.dart' as i18;
import 'package:selco/widgets/navigation/navbar.dart';

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
      appBar: const Navbar(),
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        footer:
            DigitCard(margin: const EdgeInsets.only(top: spacer2), children: [
          DigitButton(
            suffixIcon: Icons.arrow_forward_outlined,
            mainAxisSize: MainAxisSize.max,
            label: context.translate(i18.common.proceed),
            type: DigitButtonType.primary,
            size: DigitButtonSize.large,
            onPressed: () {
              context.router.replace(const LoginRoute());
            },
          ),
        ]),
        children: const [
          Expanded(
            child: WelcomeContent(),
          )
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
        imagePath: AssetImages.WELCOME_2,
      ),
      MenuItem(
        title: "Submit for Approval",
        description:
            "Save installation reports offline until ready for submission",
        imagePath: AssetImages.WELCOME_2,
      ),
      MenuItem(
        title: "Edit Reports",
        description:
            "Save installation reports offline until ready for submission",
        imagePath: AssetImages.WELCOME_2,
      ),
    ];

    return Padding(
      padding:
          const EdgeInsets.symmetric(vertical: spacer5, horizontal: spacer3),
      child: DigitCard(
        // padding: const EdgeInsets.all(spacer4),
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Welcome!',
                  style: textTheme.headingL
                      .copyWith(color: const DigitColors().light.primary2)),
              const SizedBox(height: spacer3),
              Text(
                'Through this application you will be able to:',
                style: textTheme.bodyS,
              ),
              ...menuItems.map(
                (item) => Container(
                  margin: const EdgeInsets.symmetric(vertical: spacer5),
                  child: Column(
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          Image.asset(
                            item.imagePath,
                            height: 90,
                            width: 90,
                          ),
                          const SizedBox(width: spacer6),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(item.title,
                                    style: textTheme.headingS.copyWith(
                                        color: const DigitColors()
                                            .light
                                            .primary2)),
                                const SizedBox(height: spacer1),
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
                      const SizedBox(
                        height: spacer5,
                      ),
                      const DigitDivider(
                        dividerType: DividerType.small,
                      )
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
