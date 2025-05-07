import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:selco/utils/extensions.dart';

import '../router/app_router.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class DownloadStatusPage extends StatefulWidget {
  const DownloadStatusPage({super.key});

  @override
  State<DownloadStatusPage> createState() => _DownloadStatusPageState();
}

class _DownloadStatusPageState extends State<DownloadStatusPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
      drawer: const CustomDrawer(),
      body: ScrollableContent(
        // backgroundColor: theme.colorTheme.generic.background,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer2, horizontal: spacer4),
            child: Column(
              children: [
                SizedBox(height: context.height * 0.3),
                Text(
                  "Download Successful!",
                  style: textTheme.headingS
                      .copyWith(color: const Light().alertSuccess),
                ),
                const SizedBox(height: spacer6),
                LinearProgressIndicator(
                  borderRadius: BorderRadius.circular(spacer2),
                  backgroundColor: theme.colorTheme.generic.background,
                  valueColor: AlwaysStoppedAnimation<Color>(
                    theme.colorTheme.alert.success,
                  ),
                  value: 1.0,
                  minHeight: spacer3,
                ),
                const SizedBox(height: spacer2),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      "Completed",
                      style: textTheme.bodyS
                          .copyWith(color: const Light().textDisabled),
                    ),
                    Text(
                      "100/100",
                      style: textTheme.headingS
                          .copyWith(color: const Light().primary2),
                    ),
                  ],
                )
              ],
            ),
          ),
        ],
      ),
    );
  }
}
