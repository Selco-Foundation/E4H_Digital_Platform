import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/powered_by_digit.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';

import '../repositories/assessment_form_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/home/home_item_card.dart';

@RoutePage()
class AssessmentHomePage extends StatefulWidget {
  const AssessmentHomePage({super.key});

  @override
  State<AssessmentHomePage> createState() => _AssessmentHomePageState();
}

class _AssessmentHomePageState extends State<AssessmentHomePage> {
  final AssessmentFormRepository _repository = AssessmentFormRepository();
  bool _prefetchStarted = false;
  bool _prefetching = false;
  bool _loaderVisible = false;
  NavigatorState? _rootNavigator;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _prefetchStarted) return;
      _prefetchStarted = true;
      _prefetchSchemas();
    });
  }

  Future<void> _prefetchSchemas() async {
    if (_prefetching || !mounted) return;
    _prefetching = true;
    _openBlockingLoader();
    Object? failure;
    try {
      await _repository.preloadMobileSchemas(forceRefresh: true);
    } catch (error) {
      failure = error;
    } finally {
      _prefetching = false;
      _closeBlockingLoader();
    }
    if (failure == null || !mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(context.translate(i18.assessmentForm.unableToLoad)),
        action: SnackBarAction(
          label: context.translate(i18.common.retry),
          onPressed: _prefetchSchemas,
        ),
      ),
    );
  }

  void _openBlockingLoader() {
    if (_loaderVisible || !mounted) return;
    _loaderVisible = true;
    _rootNavigator = Navigator.of(context, rootNavigator: true);
    showDialog<void>(
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
                Flexible(
                  child: Text(
                    context.translate(i18.common.loadingAppData),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    ).whenComplete(() {
      _loaderVisible = false;
      _rootNavigator = null;
    });
  }

  void _closeBlockingLoader() {
    if (!_loaderVisible) return;
    _loaderVisible = false;
    final navigator = _rootNavigator;
    _rootNavigator = null;
    if (navigator?.mounted == true && navigator!.canPop()) {
      navigator.pop();
    }
  }

  @override
  void dispose() {
    _closeBlockingLoader();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final screenWidth = context.width;

    final homeItems = [
      (
        icon: Icons.business_center_outlined,
        label: context.translate(i18.assessmentHome.assessment),
        onPressed: () => context.router.push(
              const AssessmentWorkHomeRoute(),
            ),
      ),
      (
        icon: Icons.autorenew,
        label: context.translate(i18.home.dataSync),
        onPressed: () => context.router.push(
              const AssessmentDraftRoute(),
            ),
      ),
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
                    final item = homeItems[index];
                    return HomeItemCard(
                      icon: item.icon,
                      label: item.label,
                      onPressed: item.onPressed,
                      labelPadding: const EdgeInsets.symmetric(
                        horizontal: spacer2,
                      ),
                      fitLabelOnOneLine: true,
                    );
                  },
                  childCount: homeItems.length,
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
          children: const [],
        ),
      ),
    );
  }
}
