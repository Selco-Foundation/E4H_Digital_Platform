import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../model/appconfig/mdmsRequest.dart';
import '../repositories/app_init_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';

@RoutePage()
class AmcHomePage extends StatefulWidget {
  const AmcHomePage({super.key});

  @override
  State<AmcHomePage> createState() => _AmcHomePageState();
}

class _AmcHomePageState extends State<AmcHomePage> {
  late String _userType;
  late String pendingRecords = "0";
  late String assignedFacility = "0";
  bool _prefetchStarted = false;
  bool _loaderVisible = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _prefetchStarted) return;
      _prefetchStarted = true;
      _prefetchAmcSchemas();
    });
  }

  Future<void> _prefetchAmcSchemas() async {
    _openBlockingLoader();
    final repo = AppInitRepo();
    try {
      final rawDocs = await repo.searchAMCFormConfigsRaw(
        const MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: 'in',
            schemaCode: "common-masters.AMCFormSchema",
            moduleDetails: [],
          ),
        ),
        useCacheRead: false,
      );

      for (final doc in rawDocs) {
        final transformed = transformSelcoFormMdmsDocToSchema(doc);
        final uniqueId = doc['uniqueIdentifier']?.toString();
        if (uniqueId != null && uniqueId.isNotEmpty) {
          transformed['uniqueIdentifier'] = uniqueId;
        }
        await repo.upsertTransformedSchema(transformed);
      }
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(context.translate(i18.amcHome.formSchemaLoadFailed)),
          action: SnackBarAction(
            label: context.translate(i18.common.retry),
            onPressed: () {
              if (!mounted) return;
              _prefetchAmcSchemas();
            },
          ),
        ),
      );
    } finally {
      _closeBlockingLoader();
    }
  }

  void _openBlockingLoader() {
    if (_loaderVisible || !mounted) return;
    _loaderVisible = true;
    showDialog(
      context: context,
      barrierDismissible: false,
      useRootNavigator: true,
      builder: (_) => PopScope(
        canPop: false,
        onPopInvoked: (didPop) {},
        child: Dialog(
          insetPadding: const EdgeInsets.all(24),
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                const CircularProgressIndicator(),
                const SizedBox(width: 16),
                Flexible(child: Text(context.translate(i18.common.loadingAppData))),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _closeBlockingLoader() {
    if (!_loaderVisible || !mounted) return;
    _loaderVisible = false;
    Navigator.of(context, rootNavigator: true).pop();
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

    return Scaffold(
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
    );
  }
}
