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
  AssetCount? inverterData;
  AssetCount? batteryData;
  AssetCount? panelData;

  @override
  void initState() {
    super.initState();

    // 1) If a project is already selected at startup, load its counts
    final selState = context.read<SelectedProjectBloc>().state;
    selState.whenOrNull(selected: (project) {
      _currentProjectId = project.project.id;
      _dispatchInitialLoad(project.project.id);
    });
  }

  void _dispatchInitialLoad(String projectId) {
    // Cache project progress step
    context.read<CacheProjectAssetBloc>().add(
          CacheProjectAssetEvent.update(
            CacheProjectAsset(projectId: projectId, progress: 1),
          ),
        );

    context.read<CacheAssetCountBloc>().add(
          CacheAssetCountEvent.getAll(projectId),
        );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: BlocBuilder<AppInitialization, InitState>(
        builder: (initContext, initState) {
          initState.maybeWhen(
              orElse: () => 0,
              initialized:
                  (appConfig, assetCount, assetType, system, warranty, brand) {
                final inverterEntry = assetCount.firstWhere((entry) =>
                    entry.data.assetTypeCode.toUpperCase() == "INVERTER");
                inverterData = inverterEntry.data;
                final batteryEntry = assetCount.firstWhere((entry) =>
                    entry.data.assetTypeCode.toUpperCase() == "BATTERY");
                batteryData = batteryEntry.data;

                final panelEntry = assetCount.firstWhere((entry) =>
                    entry.data.assetTypeCode.toUpperCase() == "PANEL");
                panelData = panelEntry.data;
              });

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
              onPress: () {
                context.router.push(const SelectAssetTypeRoute());
              },
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: spacer2,
                  vertical: spacer4,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [AppStepper(context: context)],
                    ),
                    const SizedBox(height: spacer4),

                    // Asset count card
                    DigitCard(
                      children: [
                        Text(
                          'Asset Count',
                          style: textTheme.headingXl.copyWith(
                            color: theme.colorTheme.primary.primary2,
                          ),
                        ),
                        Text(
                          'Choose the asset type',
                          style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                        ),
                        const SizedBox(height: spacer2),

                        // Inverter count with BlocSelector
                        BlocSelector<CacheAssetCountBloc, CacheAssetCountState,
                            String>(
                          selector: (state) => state.maybeWhen(
                            loaded: (entries) =>
                                entries
                                    .firstWhereOrNull(
                                        (e) => e.assetType == 'inverter')
                                    ?.count
                                    .toString() ??
                                '0',
                            orElse: () => '0',
                          ),
                          builder: (context, inverterCount) {
                            return LabeledField(
                              label: 'Inverters',
                              labelStyle: textTheme.headingS.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                              capitalizedFirstLetter: false,
                              child: InputField(
                                minValue: inverterData?.min ?? 0,
                                maxValue: inverterData?.max ?? 0,
                                type: InputType.numeric,
                                editable: false,
                                initialValue: inverterCount,
                                inputFormatters: [
                                  FilteringTextInputFormatter.digitsOnly
                                ],
                                onChange: (value) {
                                  if (_currentProjectId == null) return;
                                  final count = int.tryParse(value) ?? 0;
                                  context.read<CacheAssetCountBloc>().add(
                                        CacheAssetCountEventAdd(
                                          CacheAssetCount(
                                            projectId: _currentProjectId!,
                                            assetType: 'inverter',
                                            count: count,
                                          ),
                                        ),
                                      );
                                },
                              ),
                            );
                          },
                        ),

                        BlocSelector<CacheAssetCountBloc, CacheAssetCountState,
                            String>(
                          selector: (state) => state.maybeWhen(
                            loaded: (entries) =>
                                entries
                                    .firstWhereOrNull(
                                        (e) => e.assetType == 'battery')
                                    ?.count
                                    .toString() ??
                                '0',
                            orElse: () => '0',
                          ),
                          builder: (context, batteryCount) {
                            return LabeledField(
                              label: 'Batteries',
                              labelStyle: textTheme.headingS.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                              capitalizedFirstLetter: false,
                              child: InputField(
                                minValue: batteryData?.min ?? 0,
                                maxValue: batteryData?.max ?? 0,
                                type: InputType.numeric,
                                editable: false,
                                initialValue: batteryCount,
                                inputFormatters: [
                                  FilteringTextInputFormatter.digitsOnly
                                ],
                                onChange: (value) {
                                  if (_currentProjectId == null) return;
                                  final count = int.tryParse(value) ?? 0;
                                  context.read<CacheAssetCountBloc>().add(
                                        CacheAssetCountEventAdd(
                                          CacheAssetCount(
                                            projectId: _currentProjectId!,
                                            assetType: 'battery',
                                            count: count,
                                          ),
                                        ),
                                      );
                                },
                              ),
                            );
                          },
                        ),

                        BlocSelector<CacheAssetCountBloc, CacheAssetCountState,
                            String>(
                          selector: (state) => state.maybeWhen(
                            loaded: (entries) =>
                                entries
                                    .firstWhereOrNull(
                                        (e) => e.assetType == 'panel')
                                    ?.count
                                    .toString() ??
                                '0',
                            orElse: () => '0',
                          ),
                          builder: (context, panelCount) {
                            return LabeledField(
                              label: 'Panels',
                              labelStyle: textTheme.headingS.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                              capitalizedFirstLetter: false,
                              child: InputField(
                                minValue: panelData?.min ?? 0,
                                maxValue: panelData?.max ?? 0,
                                type: InputType.numeric,
                                editable: false,
                                initialValue: panelCount,
                                inputFormatters: [
                                  FilteringTextInputFormatter.digitsOnly
                                ],
                                onChange: (value) {
                                  if (_currentProjectId == null) return;
                                  final count = int.tryParse(value) ?? 0;
                                  context.read<CacheAssetCountBloc>().add(
                                        CacheAssetCountEventAdd(
                                          CacheAssetCount(
                                            projectId: _currentProjectId!,
                                            assetType: 'panel',
                                            count: count,
                                          ),
                                        ),
                                      );
                                },
                              ),
                            );
                          },
                        ),
                      ],
                    ),
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
