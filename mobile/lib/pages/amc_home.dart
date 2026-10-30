import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../blocs/app_init/app_init.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../repositories/app_init_repo.dart';
import '../repositories/cache_fallback.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';
import '../widgets/mdms/mdms_gate.dart';

@RoutePage()
class AmcHomePage extends StatefulWidget {
  const AmcHomePage({super.key});

  @override
  State<AmcHomePage> createState() => _AmcHomePageState();
}

class _AmcHomePageState extends State<AmcHomePage> {
  Future<void> _prefetchAmcSchemas() async {
    final repo = AppInitRepo();
    const request = MdmsRequestModel(
      mdmsCriteria: MdmsCriteriaModel(
        tenantId: 'in',
        schemaCode: 'common-masters.AMCFormSchema',
        moduleDetails: [],
      ),
    );
    List<Map<String, dynamic>> rawDocs;
    try {
      rawDocs = await repo.searchAMCFormConfigsRaw(request);
    } catch (error) {
      if (isAuthenticationFailure(error)) rethrow;
      rawDocs = await repo.searchAMCFormConfigsRaw(request, cacheOnly: true);
    }
    for (final doc in rawDocs) {
      if (!mounted) return;
      final transformed = transformSelcoFormMdmsDocToSchema(doc);
      final uniqueId = doc['uniqueIdentifier']?.toString();
      if (uniqueId != null && uniqueId.isNotEmpty) {
        transformed['uniqueIdentifier'] = uniqueId;
      }
      await repo.upsertTransformedSchema(transformed);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final screenWidth = context.width;

    final List<Map<String, dynamic>> _homeItems = [
      {
        'icon': Icons.text_snippet_outlined,
        'label': context.translate(i18.amcHome.amcReport),
        'onPressed': () => context.router.push(const AmcReportHomeRoute())
      },
      {
        'icon': Icons.autorenew,
        'label': context.translate(i18.amcHome.dataSync),
        'onPressed': () => context.router.push(const AmcDraftRoute()),
      },
    ];

    return MdmsGate(
      module: MdmsModule.amc,
      load: _prefetchAmcSchemas,
      child: Scaffold(
        backgroundColor: DigitTheme.instance.colorScheme.surface,
        body: Padding(
          padding: const EdgeInsets.symmetric(horizontal: spacer2),
          child: ScrollableContent(
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: false,
              showHelp: true,
            ),
            footer: const PoweredByDigit(version: ''),
            slivers: [
              SliverPadding(
                padding: const EdgeInsets.only(top: spacer6),
                sliver: SliverGrid(
                  delegate: SliverChildBuilderDelegate(
                    (context, index) {
                      final item = _homeItems[index];
                      return HomeItemCard(
                        icon: item['icon'],
                        label: item['label'],
                        onPressed: item['onPressed'],
                      );
                    },
                    childCount: _homeItems.length,
                  ),
                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    mainAxisSpacing: spacer4,
                    childAspectRatio:
                        (screenWidth / 2) / (170 * (screenWidth / 375)),
                  ),
                ),
              ),
            ],
            children: [],
          ),
        ),
      ),
    );
  }
}
