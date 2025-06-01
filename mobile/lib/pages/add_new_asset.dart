import 'dart:io';

import 'package:collection/collection.dart';
import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/pages/qr_scanner.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/DropdownModels.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/colors.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_dropdown_input.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/upload_image.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_asset_count.dart';
import '../model/asset_type/asset_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

class AssetModel {
  String serialNumber;
  String capacity;
  String unit;
  AssetModel(
      {required this.serialNumber, this.capacity = '1', this.unit = 'KvA'});
}

@RoutePage()
class AddNewAssetPage extends StatefulWidget {
  const AddNewAssetPage({super.key});
  @override
  State<AddNewAssetPage> createState() => _AddNewAssetPageState();
}

class _AddNewAssetPageState extends State<AddNewAssetPage> {
  String? _currentProjectId;
  final List<AssetModel> _assets = [AssetModel(serialNumber: '')];
  String assetTypeTitle = "";
  late List<String> assetCapacity = [];
  late String assetCapacityUom = "";
  late List<AssetType> assetTypeList = [];
  late List<String> typesField = [];
  late AssetType? selectedAssetType;
  int? _scanningIndex;

  @override
  void initState() {
    super.initState();
    assetTypeTitle = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );
    context.read<SelectedProjectBloc>().state.whenOrNull(selected: (proj) {
      _currentProjectId = proj.id;
      // Only load existing count:
      context
          .read<CacheAssetCountBloc>()
          .add(CacheAssetCountEvent.get(proj.id, assetTypeTitle));
    });

    context.read<AppInitialization>().state.maybeWhen(
        orElse: () => [],
        initialized:
            (appConfig, assetCount, assetType, system, warranty, brand) {
          assetTypeList = assetType
              .map((at) => at.data)
              .where(
                  (at) => at.code.toUpperCase() == assetTypeTitle.toUpperCase())
              // .map((w) => w.duration)
              .toList();

          final systemCode = system.lastOrNull?.data.code;

          // Find the asset type
          selectedAssetType = assetTypeList
              //.map((e) => e.data)
              .firstWhereOrNull((asset) =>
                  asset.code.toUpperCase() == assetTypeTitle.toUpperCase());

          final fields = selectedAssetType!.formFields;

          // From that asset type, get the total_capacity form field
          final assetCapacityField = fields.firstWhereOrNull(
            (field) => field.key == "capacity" && field.system == systemCode,
          );

          // Get the total_capacity_uom form field
          final assetCapacityUomField = fields.firstWhereOrNull(
            (field) =>
                field.key == "capacity_uom" && field.system == systemCode,
          );

          typesField =
              fields.firstWhereOrNull((field) => field.types != null)?.types ??
                  [];

          // Use first option or fallback to empty string
          assetCapacity = assetCapacityField!.options!;
          assetCapacityUom = assetCapacityUomField!.options!.firstOrNull ?? '';

          return assetType;
        });
  }

  void _addNewAsset(int maxAssets) {
    if (_assets.length < maxAssets) {
      setState(() => _assets.add(AssetModel(serialNumber: '')));
    } else {
      context.showSnackBar(
        SnackBar(
          content: Text('Maximum of $maxAssets assets reached'),
          backgroundColor: const Light().alertError,
        ),
      );
    }
  }

  void _updateAsset(int index, String serial) {
    setState(() => _assets[index].serialNumber = serial);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<DigitScannerBloc, DigitScannerState>(
      listener: (ctx, scanState) {
        if (scanState.qrCodes.isNotEmpty && _scanningIndex != null) {
          _updateAsset(_scanningIndex!, scanState.qrCodes.last);
          ctx
              .read<DigitScannerBloc>()
              .add(const DigitScannerEvent.handleScanner(qrCode: []));
          _scanningIndex = null;
        }
      },
      child: BlocBuilder<AssetTypeBloc, AssetTypeState>(
        builder: (ctx, assetTypeState) {
          final assetType = assetTypeState.when(
            initial: () => '',
            inverter: () => 'inverter',
            battery: () => 'battery',
            panel: () => 'panel',
          );
          final heading = assetTypeState.when(
            initial: () => 'Asset',
            inverter: () => 'Inverter',
            battery: () => 'Battery',
            panel: () => 'Panel',
          );

          return BlocSelector<CacheAssetCountBloc, CacheAssetCountState, int>(
            selector: (st) => st.maybeWhen(
              loaded: (entries) =>
                  entries
                      .firstWhereOrNull((e) => e.assetType == assetType)
                      ?.count ??
                  0,
              orElse: () => 0,
            ),
            builder: (ctx, maxAssets) {
              return Scaffold(
                appBar: const Navbar(),
                drawer: const CustomDrawer(),
                body: ScrollableContent(
                  header: const BackNavigationHelpHeaderWidget(
                      showBackNavigation: true, showHelp: false),
                  enableFixedDigitButton: true,
                  backgroundColor: theme.colorTheme.generic.background,
                  footer: FooterButton(
                    showSuffixIcon: false,
                    text: context.translate(i18.common.coreCommonNext),
                    isDisabled: _assets.length != maxAssets ||
                        _assets.any((a) => a.serialNumber.isEmpty),
                    onPress: () {
                      if (_currentProjectId != null) {
                        context.read<CacheAssetCountBloc>().add(
                              CacheAssetCountEvent.update(
                                CacheAssetCount(
                                  projectId: _currentProjectId!,
                                  assetType: assetType,
                                  progress: 5,
                                ),
                              ),
                            );
                      }
                      context.router.push(const MediaUploadRoute());
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
                                AppStepper(context: context, activeIndex: 4),
                              ]),
                          const SizedBox(height: spacer4),
                          assetTypeState.maybeWhen(
                            battery: () =>
                                _batteryCapacity(theme, textTheme, heading),
                            panel: () =>
                                _panelCapacity(theme, textTheme, heading),
                            orElse: () => const SizedBox(),
                          ),
                          ..._assets.asMap().entries.map((e) {
                            return Padding(
                              padding: const EdgeInsets.only(bottom: spacer4),
                              child: _buildAssetCard(
                                context: context,
                                theme: theme,
                                textTheme: textTheme,
                                heading: heading,
                                index: e.key,
                                asset: e.value,
                                maxAsset: maxAssets,
                                assetType: assetType,
                              ),
                            );
                          }),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              GestureDetector(
                                onTap: () => _addNewAsset(maxAssets),
                                child: Text(
                                  'Add New Asset',
                                  style: textTheme.headingM.copyWith(
                                      color: theme.colorTheme.primary.primary1),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            },
          );
        },
      ),
    );
  }

  Widget _buildAssetCard({
    required BuildContext context,
    required ThemeData theme,
    required DigitTextTheme textTheme,
    required String heading,
    required int index,
    required int maxAsset,
    required AssetModel asset,
    required String assetType,
  }) {
    return DigitCard(
      key: ValueKey(index),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              '$heading ${index + 1}',
              style: textTheme.headingXl
                  .copyWith(color: theme.colorTheme.primary.primary2),
            ),
            Text("${index + 1}/$maxAsset",
                style: textTheme.bodyL
                    .copyWith(color: theme.colorTheme.text.secondary))
          ],
        ),
        LabeledField(
          label: 'Serial Number',
          capitalizedFirstLetter: false,
          child: Row(
            children: [
              Expanded(
                flex: 5,
                child: GestureDetector(
                  onTap: () {
                    setState(() => _scanningIndex = index);
                    context
                        .read<DigitScannerBloc>()
                        .add(const DigitScannerEvent.handleScanner(qrCode: []));
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (ctx) => BlocProvider.value(
                          value: context.read<DigitScannerBloc>(),
                          child: const DigitScannerPage(
                            quantity: 1,
                            isGS1code: false,
                          ),
                        ),
                      ),
                    );
                  },
                  child: DigitTextFormInput(
                    initialValue: asset.serialNumber,
                    isDisabled: true,
                    innerLabel: asset.serialNumber.isEmpty
                        ? 'Scan serial number'
                        : asset.serialNumber,
                    keyboardType: TextInputType.none,
                  ),
                ),
              ),
              const SizedBox(width: spacer4),
              Expanded(
                flex: 2,
                child: DigitButton(
                  label: 'Scan',
                  type: DigitButtonType.primary,
                  onPressed: () {
                    setState(() => _scanningIndex = index);
                    context
                        .read<DigitScannerBloc>()
                        .add(const DigitScannerEvent.handleScanner(qrCode: []));
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (ctx) => BlocProvider.value(
                          value: context.read<DigitScannerBloc>(),
                          child: const DigitScannerPage(
                            quantity: 1,
                            isGS1code: false,
                          ),
                        ),
                      ),
                    );
                  },
                  size: DigitButtonSize.large,
                  mainAxisSize: MainAxisSize.max,
                ),
              ),
            ],
          ),
        ),
        LabeledField(
          label: 'Supporting Photo',
          capitalizedFirstLetter: false,
          child: ImageUploader(
            onImagesSelected: (List<File> imageFile) {
              // Handle the selected image file here
              // print('Image selected: ${imageFile.path}');
            },
          ),
        ),
        if (assetType == 'inverter') ...[
          const SizedBox(height: spacer4),
          Row(
            children: [
              Expanded(
                flex: 3,
                child: LabeledField(
                  label: 'Capacity',
                  capitalizedFirstLetter: false,
                  child: DigitDropdown(
                      items: assetCapacity
                          .map((type) => DropdownItem(
                                name: type,
                                code: type,
                              ))
                          .toList()),
                ),
              ),
              const SizedBox(width: spacer6),
              Expanded(
                flex: 1,
                child: LabeledField(
                  label: 'Unit',
                  capitalizedFirstLetter: false,
                  child: DigitTextFormInput(
                    controller: TextEditingController(text: assetCapacityUom),
                    isDisabled: true,
                    readOnly: true,
                    keyboardType: TextInputType.text,
                  ),
                ),
              ),
            ],
          ),
        ],
      ],
    );
  }

  Widget _batteryCapacity(
          ThemeData theme, DigitTextTheme textTheme, String heading) =>
      Column(
        children: [
          DigitCard(
            children: [
              Text(
                '$heading Capacity',
                style: textTheme.headingXl
                    .copyWith(color: theme.colorTheme.primary.primary2),
              ),
              LabeledField(
                label: '$heading Type',
                capitalizedFirstLetter: false,
                child: DigitDropdown(
                    items: typesField
                        .map((type) => DropdownItem(
                              name: type,
                              code: type,
                            ))
                        .toList()
                    // [
                    //   DropdownItem(name: 'Lead acid', code: 'lead'),
                    //   DropdownItem(name: 'Pure acid', code: 'acid'),
                    // ]
                    ),
              ),
              Row(
                children: [
                  Expanded(
                    flex: 3,
                    child: LabeledField(
                      label: 'Voltage',
                      capitalizedFirstLetter: false,
                      child: DigitDropdown(
                          items: assetCapacity
                              .map((type) => DropdownItem(
                                    name: type,
                                    code: type,
                                  ))
                              .toList()),
                    ),
                  ),
                  const SizedBox(width: spacer6),
                  Expanded(
                    flex: 1,
                    child: LabeledField(
                      label: 'Unit',
                      capitalizedFirstLetter: false,
                      child: DigitTextFormInput(
                        controller: TextEditingController(),
                        isDisabled: true,
                        initialValue: assetCapacityUom,
                        keyboardType: TextInputType.text,
                      ),
                    ),
                  ),
                ],
              ),
              Row(
                children: [
                  const Expanded(
                    flex: 3,
                    child: LabeledField(
                      label: 'Current',
                      capitalizedFirstLetter: false,
                      child: DigitDropdown(items: [
                        DropdownItem(name: '12', code: '12'),
                        DropdownItem(name: '12.8', code: '12.8'),
                        DropdownItem(name: '24', code: '24'),
                      ]),
                    ),
                  ),
                  const SizedBox(width: spacer6),
                  Expanded(
                    flex: 1,
                    child: LabeledField(
                      label: 'Unit',
                      capitalizedFirstLetter: false,
                      child: DigitTextFormInput(
                        controller: TextEditingController(),
                        isDisabled: true,
                        initialValue: 'Amps',
                        keyboardType: TextInputType.text,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: spacer8),
        ],
      );

  Widget _panelCapacity(
          ThemeData theme, DigitTextTheme textTheme, String heading) =>
      Column(
        children: [
          DigitCard(
            children: [
              Text(
                '$heading Capacity',
                style: textTheme.headingXl
                    .copyWith(color: theme.colorTheme.primary.primary2),
              ),
              Row(
                children: [
                  Expanded(
                    flex: 3,
                    child: LabeledField(
                      label: 'Voltage',
                      capitalizedFirstLetter: false,
                      child: DigitDropdown(
                          items: assetCapacity
                              .map((type) => DropdownItem(
                                    name: type,
                                    code: type,
                                  ))
                              .toList()),
                    ),
                  ),
                  const SizedBox(width: spacer6),
                  Expanded(
                    flex: 1,
                    child: LabeledField(
                      label: 'Unit',
                      capitalizedFirstLetter: false,
                      child: DigitTextFormInput(
                        controller: TextEditingController(),
                        isDisabled: true,
                        initialValue: assetCapacityUom,
                        keyboardType: TextInputType.text,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: spacer8),
        ],
      );
}
