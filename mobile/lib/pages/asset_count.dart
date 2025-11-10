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
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_activity_facility_asset/cache_activity_facility_asset.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../data/nosql/cache_activity_facility_asset.dart';
import '../data/nosql/cache_asset_count.dart';
import '../model/asset_count/asset_count.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
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
  String? _currentActivityFacilityId;
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
    final sel = context.read<SelectedActivityFacilityBloc>().state;
    sel.whenOrNull(selected: (proj) {
      _currentActivityFacilityId = proj.activityFacility.id;
      _dispatchInitialLoad(proj.activityFacility.id);
    });

    _countSub = context.read<CacheAssetCountBloc>().stream.listen((state) {
      state.maybeWhen(
        loaded: (entries) {
          if (!mounted) return;
          setState(() {
            _inverterCount = entries
                    .firstWhereOrNull((e) =>
                        e.assetType == ASSET_TYPES.INVERTER.name.toLowerCase())
                    ?.count ??
                0;
            _batteryCount = entries
                    .firstWhereOrNull((e) =>
                        e.assetType == ASSET_TYPES.BATTERY.name.toLowerCase())
                    ?.count ??
                0;
            _panelCount = entries
                    .firstWhereOrNull((e) =>
                        e.assetType == ASSET_TYPES.PANEL.name.toLowerCase())
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
    context.read<CacheActivityFacilityAssetBloc>().add(
          CacheActivityFacilityAssetEvent.update(
            CacheActivityFacilityAsset(
                activityFacilityId: projectId, progress: 1),
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
            initialized:
                (_, list, __, ___, ____, _____, solutionDesign, ______) {
              final inv = list.first.data.assetCount.firstWhere((e) =>
                  e.assetTypeCode.toUpperCase() == ASSET_TYPES.INVERTER.name);
              inverterData = inv;
              final bat = list.first.data.assetCount.firstWhere((e) =>
                  e.assetTypeCode.toUpperCase() == ASSET_TYPES.BATTERY.name);
              batteryData = bat;
              final pnl = list.first.data.assetCount.firstWhere((e) =>
                  e.assetTypeCode.toUpperCase() == ASSET_TYPES.PANEL.name);
              panelData = pnl;
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
                  context
                      .read<AssetTypeBloc>()
                      .add(const AssetTypeEvent.typeSelected(""));
                  await context.router
                      .push(const SelectAssetTypeRoute())
                      .then((_) {
                    if (!mounted) return;
                    _dispatchInitialLoad(_currentActivityFacilityId!);
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
                            if (_currentActivityFacilityId != null) {
                              context
                                  .read<CacheAssetCountBloc>()
                                  .add(CacheAssetCountEventAdd(CacheAssetCount(
                                    activityFacilityId:
                                        _currentActivityFacilityId!,
                                    assetType: 'inverter',
                                    count: c,
                                  )));
                            }
                          },
                        ),
                      ),
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
                            if (_currentActivityFacilityId != null) {
                              context
                                  .read<CacheAssetCountBloc>()
                                  .add(CacheAssetCountEventAdd(CacheAssetCount(
                                    activityFacilityId:
                                        _currentActivityFacilityId!,
                                    assetType: 'battery',
                                    count: c,
                                  )));
                            }
                          },
                        ),
                      ),
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
                            if (_currentActivityFacilityId != null) {
                              context
                                  .read<CacheAssetCountBloc>()
                                  .add(CacheAssetCountEventAdd(CacheAssetCount(
                                    activityFacilityId:
                                        _currentActivityFacilityId!,
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
