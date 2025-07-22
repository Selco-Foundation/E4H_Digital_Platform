import 'dart:async';

import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_project_asset/cache_project_asset.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_project_asset.dart';
import '../model/asset_count/asset_count.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssetCountPage extends StatefulWidget {
  const AssetCountPage({super.key});
  @override
  State<AssetCountPage> createState() => _AssetCountPageState();
}

class _AssetCountPageState extends State<AssetCountPage> {
  String? _currentProjectId;
  AssetCount? inverterData, batteryData, panelData;

  int _inverterCount = 0;
  int _batteryCount = 0;
  int _panelCount = 0;

  late final StreamSubscription<CacheAssetCountState> _countSub;

  @override
  void initState() {
    super.initState();
    _setupInitial();
  }

  void _setupInitial() {
    // Grab selected project and dispatch initial load
    final sel = context.read<SelectedProjectBloc>().state;
    sel.whenOrNull(selected: (proj) {
      _currentProjectId = proj.project.id;
      _dispatchInitialLoad(proj.project.id);
    });

    // Listen for loaded counts to seed local values
    _countSub = context.read<CacheAssetCountBloc>().stream.listen((state) {
      state.maybeWhen(
        loaded: (entries) {
          if (!mounted) return;
          setState(() {
            _inverterCount = entries
                    .firstWhereOrNull((e) => e.assetType == 'inverter')
                    ?.count ??
                0;
            _batteryCount = entries
                    .firstWhereOrNull((e) => e.assetType == 'battery')
                    ?.count ??
                0;
            _panelCount = entries
                    .firstWhereOrNull((e) => e.assetType == 'panel')
                    ?.count ??
                0;
          });
        },
        orElse: () {},
      );
    });
  }

  @override
  void dispose() {
    _countSub.cancel();
    super.dispose();
  }

  void _dispatchInitialLoad(String projectId) {
    context.read<CacheProjectAssetBloc>().add(
          CacheProjectAssetEvent.update(
            CacheProjectAsset(projectId: projectId, progress: 1),
          ),
        );
    context.read<CacheAssetCountBloc>().add(
          CacheAssetCountEvent.getAll(projectId),
        );
  }

  bool get _disableFooter =>
      _inverterCount == 0 || _batteryCount == 0 || _panelCount == 0;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final txt = theme.digitTextTheme(context);

    return Scaffold(
      body: BlocBuilder<AppInitialization, InitState>(
        builder: (_, init) {
          init.maybeWhen(
            initialized: (_, list, __, ___, ____, _____) {
              // set min/max for each from list
              final inv = list.firstWhere(
                  (e) => e.data.assetTypeCode.toUpperCase() == 'INVERTER');
              inverterData = inv.data;
              final bat = list.firstWhere(
                  (e) => e.data.assetTypeCode.toUpperCase() == 'BATTERY');
              batteryData = bat.data;
              final pnl = list.firstWhere(
                  (e) => e.data.assetTypeCode.toUpperCase() == 'PANEL');
              panelData = pnl.data;
            },
            orElse: () {},
          );

          return ScrollableContent(
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            enableFixedDigitButton: true,
            footer: FooterButton(
              showSuffixIcon: false,
              text: context.translate(i18.common.coreCommonNext),
              isDisabled: _disableFooter,
              onPress: () async {
                if (!_disableFooter) {
                  await context.router
                      .push(const SelectAssetTypeRoute())
                      .then((_) {
                    // this callback runs when SelectAssetTypeRoute is popped off to refresh the page for the counts
                    _dispatchInitialLoad(_currentProjectId!);
                  });
                }
              },
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    vertical: spacer4, horizontal: spacer2),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Center(child: AppStepper(context: context)),
                    const SizedBox(height: spacer4),
                    DigitCard(children: [
                      Text('Asset Count',
                          style: txt.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2)),
                      Text('Choose the asset type',
                          style: txt.bodyL
                              .copyWith(color: theme.colorTheme.text.primary)),
                      const SizedBox(height: spacer2),

                      // ─ Inverter ───────────────────────────────
                      LabeledField(
                        label: 'Inverters',
                        labelStyle: txt.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        child: InputField(
                          minValue: inverterData?.min ?? 0,
                          maxValue: inverterData?.max ?? 0,
                          type: InputType.numeric,
                          editable: false,
                          initialValue: _inverterCount.toString(),
                          inputFormatters: [
                            FilteringTextInputFormatter.digitsOnly
                          ],
                          onChange: (val) {
                            final c = int.tryParse(val) ?? 0;
                            setState(() => _inverterCount = c);
                            if (_currentProjectId != null) {
                              context
                                  .read<CacheAssetCountBloc>()
                                  .add(CacheAssetCountEventAdd(CacheAssetCount(
                                    projectId: _currentProjectId!,
                                    assetType: 'inverter',
                                    count: c,
                                  )));
                            }
                          },
                        ),
                      ),

                      // ─ Battery ────────────────────────────────
                      LabeledField(
                        label: 'Batteries',
                        labelStyle: txt.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        child: InputField(
                          minValue: batteryData?.min ?? 0,
                          maxValue: batteryData?.max ?? 0,
                          type: InputType.numeric,
                          editable: false,
                          initialValue: _batteryCount.toString(),
                          inputFormatters: [
                            FilteringTextInputFormatter.digitsOnly
                          ],
                          onChange: (val) {
                            final c = int.tryParse(val) ?? 0;
                            setState(() => _batteryCount = c);
                            if (_currentProjectId != null) {
                              context
                                  .read<CacheAssetCountBloc>()
                                  .add(CacheAssetCountEventAdd(CacheAssetCount(
                                    projectId: _currentProjectId!,
                                    assetType: 'battery',
                                    count: c,
                                  )));
                            }
                          },
                        ),
                      ),

                      // ─ Panel ─────────────────────────────────
                      LabeledField(
                        label: 'Panels',
                        labelStyle: txt.headingS
                            .copyWith(color: theme.colorTheme.text.primary),
                        child: InputField(
                          minValue: panelData?.min ?? 0,
                          maxValue: panelData?.max ?? 0,
                          type: InputType.numeric,
                          editable: false,
                          initialValue: _panelCount.toString(),
                          inputFormatters: [
                            FilteringTextInputFormatter.digitsOnly
                          ],
                          onChange: (val) {
                            final c = int.tryParse(val) ?? 0;
                            setState(() => _panelCount = c);
                            if (_currentProjectId != null) {
                              context
                                  .read<CacheAssetCountBloc>()
                                  .add(CacheAssetCountEventAdd(CacheAssetCount(
                                    projectId: _currentProjectId!,
                                    assetType: 'panel',
                                    count: c,
                                  )));
                            }
                          },
                        ),
                      ),
                    ]),
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
