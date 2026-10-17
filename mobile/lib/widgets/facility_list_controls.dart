import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/models/RadioButtonModel.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';

import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;

class RefreshableFacilityList extends StatelessWidget {
  final Future<void> Function() onRefresh;
  final Widget child;

  const RefreshableFacilityList(
      {super.key, required this.onRefresh, required this.child});

  @override
  Widget build(BuildContext context) => RefreshIndicator(
        onRefresh: onRefresh,
        child: ScrollConfiguration(
          behavior: const _FacilityListScrollBehavior(),
          child: child,
        ),
      );
}

class _FacilityListScrollBehavior extends MaterialScrollBehavior {
  const _FacilityListScrollBehavior();

  @override
  ScrollPhysics getScrollPhysics(BuildContext context) {
    final physics = super.getScrollPhysics(context);
    // Cards contain scroll views too; only the page needs short-list refresh.
    return context.findAncestorWidgetOfExactType<Scrollable>() == null
        ? AlwaysScrollableScrollPhysics(parent: physics)
        : physics;
  }
}

class FacilityListControls extends StatelessWidget {
  final String titleKey;
  final String? filter;
  final String bookmarkLabelKey;
  final ValueChanged<String> onSearch;
  final ValueChanged<String?> onFilter;

  const FacilityListControls(
      {super.key,
      required this.titleKey,
      required this.filter,
      required this.bookmarkLabelKey,
      required this.onSearch,
      required this.onFilter});

  void _showFilter(BuildContext context) {
    String? selected = filter;
    showCustomPopup(
        context: context,
        builder: (ctx) => StatefulBuilder(
              builder: (ctx, update) => Popup(
                title: context.translate(i18.common.sort),
                onCrossTap: () => Navigator.of(ctx).pop(),
                onOutsideTap: () => Navigator.of(ctx).pop(),
                additionalWidgets: [
                  RadioList(
                    groupValue: selected ?? '',
                    onChanged: (value) => update(() => selected = value.code),
                    radioDigitButtons: [
                      RadioButtonModel(
                          code: 'DESC',
                          name: context.translate(i18.common.newestFirst)),
                      RadioButtonModel(
                          code: 'ASC',
                          name: context.translate(i18.common.oldestFirst)),
                      RadioButtonModel(
                          code: 'BOOKMARKED',
                          name: context.translate(bookmarkLabelKey)),
                    ],
                  ),
                  Row(children: [
                    Expanded(
                        child: DigitButton(
                            label: context.translate(i18.common.clear),
                            type: DigitButtonType.secondary,
                            size: DigitButtonSize.large,
                            mainAxisSize: MainAxisSize.min,
                            onPressed: () {
                              Navigator.of(ctx).pop();
                              onFilter(null);
                            })),
                    const SizedBox(width: spacer4),
                    Expanded(
                        child: DigitButton(
                            label: context.translate(i18.common.sort),
                            type: DigitButtonType.primary,
                            size: DigitButtonSize.large,
                            mainAxisSize: MainAxisSize.min,
                            isDisabled: selected == null,
                            onPressed: () {
                              Navigator.of(ctx).pop();
                              onFilter(selected);
                            })),
                  ]),
                ],
              ),
            ));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return DigitCard(children: [
      Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(context.translate(titleKey),
            style: theme
                .digitTextTheme(context)
                .bodyL
                .copyWith(color: theme.colorTheme.text.primary)),
        const SizedBox(height: spacer1),
        Row(children: [
          Expanded(
              child: DigitSearchFormInput(
                  suffixIcon: Icons.search, onChange: onSearch)),
          const SizedBox(width: spacer2),
          IconButton(
              icon: const Icon(Icons.import_export),
              iconSize: spacer8,
              color: theme.colorTheme.primary.primary1,
              tooltip: context.translate(i18.common.sort),
              onPressed: () => _showFilter(context)),
        ]),
      ]),
    ]);
  }
}
