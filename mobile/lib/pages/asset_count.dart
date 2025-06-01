import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/blocs/app_init/app_init.dart';

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
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

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
      _currentProjectId = project.id;
      _dispatchInitialLoad(project.id);
    });

    // 2) Listen to any future changes in the selected project
    // context.read<SelectedProjectBloc>().stream.listen((state) {
    //   state.whenOrNull(selected: (project) {
    //     if (_currentProjectId != project.id) {
    //       _currentProjectId = project.id;
    //       _dispatchInitialLoad(project.id);
    //     }
    //   });
    // });
  }

  void _dispatchInitialLoad(String projectId) {
    // Cache project progress step
    context.read<CacheProjectAssetBloc>().add(
          CacheProjectAssetEvent.update(
            CacheProjectAsset(projectId: projectId, progress: 1),
          ),
        );

    // Load inverter count
    context
        .read<CacheAssetCountBloc>()
        .add(CacheAssetCountEvent.get(projectId, 'inverter'));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(),
      drawer: const CustomDrawer(),
      body: BlocBuilder<AppInitialization, InitState>(
        builder: (initContext, initState) {
          final counts = initState.maybeWhen(
              orElse: () => 0,
              initialized: (appConfig, assetCount, assetType) {
                final inverterEntry = assetCount.firstWhere(
                    (entry) => entry.data.assetTypeCode == "INVERTER");
                inverterData = inverterEntry.data;
                final batteryEntry = assetCount.firstWhere(
                    (entry) => entry.data.assetTypeCode == "BATTERY");
                batteryData = batteryEntry.data;

                final panelEntry = assetCount
                    .firstWhere((entry) => entry.data.assetTypeCode == "PANEL");
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

                        // Batteries (static for now)
                        LabeledField(
                          label: 'Batteries',
                          labelStyle: textTheme.headingS.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                          capitalizedFirstLetter: false,
                          child: InputField(
                            type: InputType.numeric,
                            initialValue: '0',
                            inputFormatters: [
                              FilteringTextInputFormatter.digitsOnly
                            ],
                            editable: true,
                          ),
                        ),

                        // Panels (static for now)
                        LabeledField(
                          label: 'Panels',
                          labelStyle: textTheme.headingS.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                          capitalizedFirstLetter: false,
                          child: InputField(
                            type: InputType.numeric,
                            minValue: 0,
                            maxValue: 100,
                            initialValue: '0',
                            inputFormatters: [
                              FilteringTextInputFormatter.digitsOnly
                            ],
                            editable: true,
                          ),
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
