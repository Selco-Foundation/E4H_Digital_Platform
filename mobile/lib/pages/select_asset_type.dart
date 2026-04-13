import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_specification/cache_specification.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/specification/specification.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_specification.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/asset_type/asset_type.dart';
import '../model/mdms/mdms.dart';
import '../model/solution_design_type/solution_design_type.dart';
import '../model/system/system.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
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
  String? _currentActivityFacilityId;
  ActivityFacilityWorkflow? project;
  String selectedAssetType = "";

  @override
  void initState() {
    super.initState();
    _currentActivityFacilityId = context
        .read<SelectedActivityFacilityBloc>()
        .state
        .whenOrNull(selected: (wf) {
      project = wf;
      return wf.activityFacility.id;
    });
  }

  void _saveCacheSpecification() {
    final initState = context.read<AppInitialization>().state;

    final systemList = initState.maybeWhen<List<Mdms<SystemData>>>(
      initialized: (_, __, ___, system, ____, _____, solutionDesign, ______) =>
          system,
      orElse: () => [],
    );
    final mdmsAssetTypes = initState.maybeWhen<List<Mdms<AssetTypeData>>>(
      initialized: (_, __, ___, ____, _____, ______, solutionDesign, _______) =>
          ___,
      orElse: () => [],
    );

    final solutionDesignList =
        initState.maybeWhen<List<Mdms<SolutionDesignType>>>(
      initialized: (_, __, ___, ____, _____, ______, solutionDesign, _______) =>
          solutionDesign,
      orElse: () => const [],
    );

    final selectedSolutionDesignCode = project?.activityFacility.facility
        ?.facilityDetails?.solar_solution_design_type;

    final matchedSystemCode = solutionDesignList
        .map((m) => m.data)
        .firstWhereOrNull((sd) => sd.code == selectedSolutionDesignCode)
        ?.systemCode;

    final systemCode =
        matchedSystemCode ?? systemList.first.data.system.lastOrNull?.code;
    final systemName = systemList.first.data.system
        .map((m) => m)
        .firstWhereOrNull((sd) => sd.code == systemCode)
        ?.name;

    final assetTypeModel = mdmsAssetTypes.first.data.assetType
        .map((m) => m)
        .firstWhereOrNull(
            (t) => t.code.toLowerCase() == selectedAssetType.toLowerCase());

    final capField = assetTypeModel?.formFields.firstWhereOrNull(
      (f) => f.key == 'total_capacity' && f.system == systemCode,
    );
    final uomField = assetTypeModel?.formFields.firstWhereOrNull(
      (f) => f.key == 'total_capacity_uom' && f.system == systemCode,
    );

    final rawCapacity = capField?.options?.firstOrNull ?? '0';
    final rawCapacityUom = uomField?.options?.firstOrNull ?? '';

    final parsedCapacity = double.tryParse(rawCapacity) ?? 0.0;

    final newSpec = CacheSpecification(
      activityFacilityId: _currentActivityFacilityId!,
      assetType: selectedAssetType.toLowerCase(),
      system: systemCode!,
      totalCapacity: parsedCapacity,
      totalCapacityUnit: rawCapacityUom,
    );

    context
        .read<CacheSpecificationBloc>()
        .add(CacheSpecificationEvent.add(newSpec));

    context.read<SpecificationBloc>().add(SpecificationEvent.save(
          systemName: systemName!,
          totalCapacity: parsedCapacity,
          totalCapacityUom: rawCapacityUom,
        ));
  }

  CacheAssetCount? currentCacheEntryFor(
    BuildContext context, {
    required String activityFacilityId,
    required String assetType,
  }) {
    final state = context.read<CacheAssetCountBloc>().state;
    return state.maybeWhen(
      loaded: (entries) => entries.firstWhereOrNull(
        (e) =>
            e.activityFacilityId == activityFacilityId &&
            e.assetType.toLowerCase() == assetType.toLowerCase(),
      ),
      orElse: () => null,
    );
  }

  void _handleNavigation(BuildContext context) {
    _saveCacheSpecification();
    final isSupervisor = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => true,
          orElse: () => false,
        );
    CacheAssetCount? cacheEntry = currentCacheEntryFor(context,
        activityFacilityId: _currentActivityFacilityId!,
        assetType: selectedAssetType);
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
        isSupervisor
            ? context.router.push(const SpecificationRoute())
            : context.router.push(const AssetTypeDetailRoute());
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
              text: context.translate(i18.common.coreCommonNext),
              isDisabled: state is AssetTypeInitial,
              onPress: () {
                _handleNavigation(context);
              },
            ),
            children: [
              BlocBuilder<AppInitialization, InitState>(
                builder: (initContext, initState) {
                  final List<Mdms<AssetTypeData>> assetTypeList =
                      initState.maybeWhen(
                          orElse: () => [],
                          initialized: (appConfig, assetCount, assetType,
                                  system, warranty, brand, solutionDesign, _) =>
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
                              sentenceCaseEnabled: false,
                              onSelect: (DropdownItem selected) {
                                selectedAssetType = selected.name;
                                context.read<AssetTypeBloc>().add(
                                    AssetTypeEvent.typeSelected(selected.code));
                              },
                              selectedOption: DropdownItem(
                                name: selectedAssetType ?? "",
                                code: selectedAssetType ?? "",
                              ),
                              items: assetTypeList.first.data.assetType
                                  .map((type) => DropdownItem(
                                        name: type.name,
                                        code: type.code,
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
