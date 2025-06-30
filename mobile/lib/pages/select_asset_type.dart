import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_asset_count.dart';
import '../model/asset_type/asset_type.dart';
import '../model/mdms/mdms.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class SelectAssetTypePage extends StatefulWidget {
  const SelectAssetTypePage({super.key});

  @override
  State<SelectAssetTypePage> createState() {
    return _SelectAssetTypePageState();
  }
}

class _SelectAssetTypePageState extends State<SelectAssetTypePage> {
  String? _currentProjectId;
  String? selectedAssetType;

  @override
  void initState() {
    super.initState();
    _currentProjectId = context
        .read<SelectedProjectBloc>()
        .state
        .whenOrNull(selected: (project) => project.project.id);
  }

  CacheAssetCount? currentCacheEntryFor(
    BuildContext context, {
    required String projectId,
    required String assetType,
  }) {
    final state = context.read<CacheAssetCountBloc>().state;
    return state.maybeWhen(
      loaded: (entries) => entries.firstWhereOrNull(
        (e) => e.projectId == projectId && e.assetType == assetType,
      ),
      orElse: () => null,
    );
  }

  void _handleNavigation(BuildContext context) {
    final assetType = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );

    CacheAssetCount? cacheEntry = currentCacheEntryFor(context,
        projectId: _currentProjectId!, assetType: assetType);
    switch (cacheEntry?.progress) {
      case 3:
        context.router.push(const SpecificationRoute());
        break;
      case 4:
        context.router.push(const AssetTypeDetailRoute());
        break;
      case 5:
        context.router.push(const AddNewAssetRoute());
        break;
      case 6:
        context.router.push(const MediaUploadRoute());
        break;
      default:
        context.router.push(const SpecificationRoute());
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(body: BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        return ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            footer: FooterButton(
              showSuffixIcon: false,
              text: i18.common.coreCommonNext,
              isDisabled: state is AssetTypeInitial,
              onPress: () {
                _handleNavigation(context);
              },
            ),
            children: [
              BlocBuilder<AppInitialization, InitState>(
                builder: (initContext, initState) {
                  final List<Mdms<AssetType>> assetTypeList =
                      initState.maybeWhen(
                          orElse: () => [],
                          initialized: (appConfig, assetCount, assetType,
                                  system, warranty, brand) =>
                              assetType);

                  return Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: spacer2, vertical: spacer4),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              AppStepper(context: context, activeIndex: 1),
                            ]),
                        const SizedBox(height: spacer4),
                        DigitCard(children: [
                          Text(
                            'Asset Type',
                            style: textTheme.headingXl.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                          Text(
                            'Choose the asset type',
                            style: textTheme.bodyL
                                .copyWith(color: theme.colorTheme.text.primary),
                          ),
                          LabeledField(
                            label: 'Select Asset Type',
                            labelStyle: textTheme.label.copyWith(
                              color: theme.colorTheme.text.primary,
                            ),
                            capitalizedFirstLetter: false,
                            child: DigitDropdown(
                              onSelect: (DropdownItem selected) {
                                selectedAssetType = selected.code;
                                context.read<AssetTypeBloc>().add(
                                    AssetTypeEvent.typeSelected(selected.code));
                              },
                              selectedOption: DropdownItem(
                                name: selectedAssetType ?? "",
                                code: selectedAssetType ?? "",
                              ),
                              items: assetTypeList
                                  .map((type) => DropdownItem(
                                        name: type.data.name,
                                        code: type.data.code,
                                      ))
                                  .toList(),
                            ),
                          ),
                        ])
                      ],
                    ),
                  );
                },
              )
            ]);
      },
    ));
  }
}
