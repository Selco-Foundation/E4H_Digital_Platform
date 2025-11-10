import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_asset_detail/cache_asset_detail.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../model/asset_type/asset_type.dart';
import '../model/brand/brand.dart';
import '../model/mdms/mdms.dart';
import '../model/warranty/warranty.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssetTypeDetailPage extends StatefulWidget {
  const AssetTypeDetailPage({super.key});

  @override
  State<AssetTypeDetailPage> createState() => _AssetTypeDetailPageState();
}

class _AssetTypeDetailPageState extends State<AssetTypeDetailPage> {
  String? _currentProjectId;
  String assetTypeTitle = "";
  late List<Warranty> assetWarranties = [];
  late List<Brand> assetBrands = [];
  final List<Mdms<AssetType>> assetTypeList = [];

  String? selectedWarranty;
  String? selectedBrandCode;
  String? selectedBrandName;

  final TextEditingController modelController = TextEditingController();

  @override
  void initState() {
    super.initState();
    assetTypeTitle = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );

    context.read<AppInitialization>().state.maybeWhen(
        orElse: () => [],
        initialized: (appConfig, assetCount, assetType, system, warranty, brand,
            solutionDesign, _) {
          assetWarranties = warranty.first.data.warrantyDuration
              .map((w) => w)
              .where((w) =>
                  w.assetTypeCode.toUpperCase() == assetTypeTitle.toUpperCase())
              .toList();

          assetBrands = brand.first.data.brand
              .map((b) => b)
              .where((w) =>
                  w.assetTypeCode.toUpperCase() == assetTypeTitle.toUpperCase())
              .toList();
          return assetType;
        });

    final selState = context.read<SelectedActivityFacilityBloc>().state;
    selState.whenOrNull(selected: (project) {
      _currentProjectId = project.activityFacility.id;
      _updateProgress(project.activityFacility.id, assetTypeTitle);
      context.read<CacheAssetDetailBloc>().add(
            CacheAssetDetailEvent.get(
                project.activityFacility.id, assetTypeTitle),
          );
    });
  }

  @override
  void dispose() {
    modelController.dispose();
    super.dispose();
  }

  void _updateProgress(String projectId, assetType) {
    context
        .read<CacheAssetCountBloc>()
        .add(CacheAssetCountEvent.update(CacheAssetCount(
          activityFacilityId: projectId,
          assetType: assetType,
          progress: 4,
        )));
  }

  String _brandNameFor(String? code) {
    if (code == null || code.isEmpty) return '';
    final b = assetBrands.firstWhereOrNull((x) => x.code == code);
    return b?.name ?? '';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<CacheAssetDetailBloc, CacheAssetDetailState>(
      listener: (context, state) {
        state.maybeWhen(
          loaded: (entries) {
            final entry = entries.firstOrNull;
            if (entry != null) {
              setState(() {
                selectedBrandCode = entry.brand;
                selectedBrandName = _brandNameFor(selectedBrandCode);
                selectedWarranty = entry.warranty;
                modelController.text = entry.model ?? '';
              });
            }
          },
          orElse: () {},
        );
      },
      child: BlocBuilder<AssetTypeBloc, AssetTypeState>(
        builder: (context, state) {
          final detailHeading = state.when(
            initial: () => 'Details',
            inverter: () => 'Inverter Details',
            battery: () => 'Battery Details',
            panel: () => 'Panel Details',
          );

          final bool isEnabledSupervisor = selectedWarranty != null &&
              selectedBrandCode != null &&
              modelController.text.trim().isNotEmpty;

          final bool isEnabledFieldUser = selectedBrandCode != null;

          return BlocBuilder<UserTypeBloc, UserTypeState>(
            builder: (context, userState) {
              final isSupervisor = userState.maybeWhen(
                orElse: () => false,
                supervisor: () => true,
              );
              return Scaffold(
                  body: ScrollableContent(
                      header: const BackNavigationHelpHeaderWidget(
                        showBackNavigation: true,
                        showHelp: false,
                      ),
                      enableFixedDigitButton: true,
                      backgroundColor: theme.colorTheme.generic.background,
                      footer: FooterButton(
                        isDisabled: isSupervisor
                            ? !isEnabledSupervisor
                            : !isEnabledFieldUser,
                        showSuffixIcon: false,
                        text: i18.common.coreCommonNext,
                        onPress: () {
                          if (isSupervisor && !isEnabledSupervisor)
                            return;
                          else if (!isEnabledFieldUser) return;
                          final newDetail = CacheAssetDetail(
                            activityFacilityId: _currentProjectId!,
                            assetType: assetTypeTitle,
                            warranty: isSupervisor ? selectedWarranty : null,
                            brand: selectedBrandCode!,
                            model: isSupervisor
                                ? modelController.text.trim()
                                : null,
                          );

                          context
                              .read<CacheAssetDetailBloc>()
                              .add(CacheAssetDetailEvent.add(newDetail));
                          context.router.push(const AddNewAssetRoute());
                        },
                      ),
                      children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: spacer2, vertical: spacer4),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              AppStepper(context: context, activeIndex: 3)
                            ],
                          ),
                          const SizedBox(height: spacer4),
                          DigitCard(children: [
                            Text(
                              detailHeading,
                              style: textTheme.headingXl.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                            if (isSupervisor)
                              LabeledField(
                                label: 'Warranty Start Date',
                                labelStyle: textTheme.headingS.copyWith(
                                    color: theme.colorTheme.text.primary),
                                capitalizedFirstLetter: false,
                                child: DigitDateFormInput(
                                  controller: TextEditingController(),
                                  initialValue: 'Default Today Date',
                                  isDisabled: true,
                                  readOnly: true,
                                ),
                              ),
                            if (isSupervisor)
                              LabeledField(
                                  label: 'Warranty Duration',
                                  labelStyle: textTheme.headingS.copyWith(
                                      color: theme.colorTheme.text.primary),
                                  capitalizedFirstLetter: false,
                                  child: DigitDropdown(
                                    sentenceCaseEnabled: false,
                                    selectedOption: DropdownItem(
                                      name: (selectedWarranty == null ||
                                              selectedWarranty!.isEmpty)
                                          ? ''
                                          : (parseWarrantyYears(
                                                      selectedWarranty!)
                                                  ?.toString() ??
                                              ''),
                                      code: selectedWarranty ?? "",
                                    ),
                                    items: assetWarranties
                                        .map((type) => DropdownItem(
                                              name: parseWarrantyYears(
                                                      type.duration)
                                                  .toString(),
                                              code: type.duration,
                                            ))
                                        .toList(),
                                    onSelect: (DropdownItem selected) {
                                      setState(() {
                                        selectedWarranty = selected.code;
                                      });
                                    },
                                  )),
                            LabeledField(
                                label: 'Brand',
                                labelStyle: textTheme.headingS.copyWith(
                                    color: theme.colorTheme.text.primary),
                                capitalizedFirstLetter: false,
                                child: DigitDropdown(
                                  sentenceCaseEnabled: false,
                                  selectedOption: DropdownItem(
                                    name: selectedBrandName ?? "",
                                    code: selectedBrandCode ?? "",
                                  ),
                                  items: assetBrands
                                      .map((type) => DropdownItem(
                                            name: type.name,
                                            code: type.code,
                                          ))
                                      .toList(),
                                  onSelect: (DropdownItem selected) {
                                    setState(() {
                                      selectedBrandCode = selected.code;
                                      selectedBrandName = selected.name;
                                    });
                                  },
                                )),
                            if (isSupervisor)
                              LabeledField(
                                label: 'Model Number',
                                labelStyle: textTheme.headingS.copyWith(
                                    color: theme.colorTheme.text.primary),
                                capitalizedFirstLetter: false,
                                child: DigitTextFormInput(
                                  controller: modelController,
                                  innerLabel: 'SR45934295',
                                  keyboardType: TextInputType.text,
                                  onChange: (value) {
                                    setState(() {});
                                  },
                                ),
                              ),
                          ])
                        ],
                      ),
                    )
                  ]));
            },
          );
        },
      ),
    );
  }
}
