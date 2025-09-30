import 'dart:async';
import 'dart:io';

import 'package:collection/collection.dart';
import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/pages/qr_scanner.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/DropdownModels.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
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
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:recase/recase.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_add_new_asset/cache_add_new_asset.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_asset_count.dart';
import '../model/asset_type/asset_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';

class AssetModel {
  String serialNumber;
  String capacity;
  String? capacityUnit;
  String? panelCapacity;
  String? batteryCapacity;
  String? batteryVoltage;
  String? batteryType;
  String? voltageUnit;
  String? inverterCapacity;
  String? inverterCapacityUnit;
  String? currentUnit;
  String unit;
  String? photoPath;
  String? latitude;
  String? longitude;
  AssetModel({
    required this.serialNumber,
    this.capacity = '1',
    this.capacityUnit,
    this.panelCapacity,
    this.batteryVoltage,
    this.batteryType,
    this.batteryCapacity,
    this.voltageUnit,
    this.inverterCapacity,
    this.inverterCapacityUnit,
    this.currentUnit,
    this.unit = 'KvA',
    this.photoPath,
    this.longitude,
    this.latitude,
  });
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
  String currentAssetType = "";
  late List<String> assetCapacity = [];
  late String assetCapacityUom = "";
  late List<String> voltages = [];
  late String voltageUom = "";
  late List<AssetType> assetTypeList = [];
  late List<String> typesField = [];
  late AssetType? selectedAssetType;
  int? _scanningIndex;

  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;

  // Cache for downloaded files
  final Map<String, File> _fileCache = {};

  // map from asset‐index to Future<File?> so we only fetch once
  final Map<int, Future<File?>> _cachedImageFutures = {};

  @override
  void initState() {
    super.initState();
    final locBloc = context.read<LocationBloc>();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _requestPermissions();
      currentAssetType = context.read<AssetTypeBloc>().state.when(
            initial: () => '',
            inverter: () => 'inverter',
            battery: () => 'battery',
            panel: () => 'panel',
          );

      context.read<SelectedProjectBloc>().state.whenOrNull(selected: (proj) {
        _currentProjectId = proj.project.id;
        context
            .read<CacheAssetCountBloc>()
            .add(CacheAssetCountEvent.get(proj.project.id, currentAssetType));
        context.read<CacheAddNewAssetBloc>().add(
              CacheAddNewAssetEvent.get(proj.project.id, currentAssetType),
            );
      });

      context.read<AppInitialization>().state.maybeWhen(
            orElse: () => [],
            initialized: (appConfig, assetCount, assetType, system, warranty,
                brand, solutionDesign, _) {
              assetTypeList = assetType
                  .map((at) => at.data)
                  .where((at) =>
                      at.code.toUpperCase() == currentAssetType.toUpperCase())
                  .toList();

              final systemCode = system.lastOrNull?.data.code;
              selectedAssetType = assetTypeList.firstWhereOrNull((asset) =>
                  asset.code.toUpperCase() == currentAssetType.toUpperCase());

              final fields = selectedAssetType?.formFields ?? [];
              final assetCapacityField = fields.firstWhereOrNull(
                (field) =>
                    field.key == "capacity" && field.system == systemCode,
              );
              final assetCapacityUomField = fields.firstWhereOrNull((field) =>
                  field.key == "capacity_uom" && field.system == systemCode);
              final voltageField = fields.firstWhereOrNull((field) =>
                  field.key == "voltage" && field.system == systemCode);
              final voltageUomField = fields.firstWhereOrNull((field) =>
                  field.key == "voltage_uom" && field.system == systemCode);
              typesField = fields
                      .firstWhereOrNull((field) => field.types != null)
                      ?.types ??
                  [];
              assetCapacity = assetCapacityField?.options ?? [];
              assetCapacityUom =
                  assetCapacityUomField?.options?.firstOrNull ?? '';
              voltages = voltageField != null && voltageField.options != null
                  ? voltageField.options!
                  : [];
              voltageUom = voltageUomField?.options?.firstOrNull ?? '';

              print("voltageUom $voltageUom");

              return assetType;
            },
          );
    });

    _locSub = locBloc.stream.listen((locationState) {
      if (locationState.latitude != null && locationState.longitude != null) {
        setState(() {
          _latitude = locationState.latitude;
          _longitude = locationState.longitude;
        });
      }
    });
  }

  @override
  void dispose() {
    _locSub?.cancel();
    super.dispose();
  }

  Future<bool> _ensureLocationLoaded(
      {Duration timeout = const Duration(seconds: 10)}) async {
    final locBloc = context.read<LocationBloc>();
    if (locBloc.state.latitude != null && locBloc.state.longitude != null) {
      return true;
    }
    try {
      final state = await locBloc.stream
          .firstWhere((s) => s.latitude != null && s.longitude != null)
          .timeout(timeout);
      setState(() {
        _latitude = state.latitude;
        _longitude = state.longitude;
      });
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<void> _requestPermissions() async {
    // Request camera and location permissions together
    Map<Permission, PermissionStatus> statuses = await [
      Permission.camera,
      Permission.locationWhenInUse,
    ].request();

    if (statuses[Permission.camera] != PermissionStatus.granted) {
      context.showSnackBar(
        const SnackBar(
            content: Text('Camera permission is required to scan QR codes')),
      );
    }

    if (statuses[Permission.locationWhenInUse] != PermissionStatus.granted) {
      context.showSnackBar(
        const SnackBar(
            content: Text('Location permission is required to geotag photos')),
      );
    }

    // Request location service after permissions
    final locBloc = context.read<LocationBloc>();
    locBloc.add(const LocationEvent.requestPermission());
    locBloc.add(const LocationEvent.requestService());
  }

  void _addNewAsset(int maxAssets) {
    if (_assets.length < maxAssets) {
      setState(() {
        final newAsset = AssetModel(serialNumber: '');

        // Copy battery/panel properties from first asset to maintain consistency
        if (_assets.isNotEmpty &&
            (currentAssetType == 'battery' || currentAssetType == 'panel')) {
          newAsset.batteryType = _assets.first.batteryType;
          newAsset.batteryVoltage = _assets.first.batteryVoltage;
          newAsset.batteryCapacity = _assets.first.batteryCapacity;
          newAsset.panelCapacity = _assets.first.panelCapacity;
        }

        _assets.add(newAsset);
      });
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

  Future<File?> _getCachedFile(String path) async {
    if (_fileCache.containsKey(path)) {
      return _fileCache[path];
    }

    if (isValidUuid(path)) {
      try {
        final uri = Uri.parse("$fileStoreFileUrl$path");
        final response = await http.get(uri);
        if (response.statusCode == 200) {
          final dir = await getTemporaryDirectory();
          final file = File('${dir.path}/${uri.pathSegments.last}');
          await file.writeAsBytes(response.bodyBytes);
          _fileCache[path] = file;
          return file;
        }
      } catch (e) {
        print('Error downloading image: $e');
      }
    } else {
      final file = File(path);
      if (await file.exists()) {
        _fileCache[path] = file;
        return file;
      }
    }
    return null;
  }

  bool _isAssetComplete(AssetModel a, String assetType) {
    // must always have serial + photo
    if (a.serialNumber.isEmpty || a.photoPath == null) return false;

    switch (assetType.toLowerCase()) {
      case 'battery':
        return a.batteryType?.isNotEmpty == true &&
            a.batteryVoltage?.isNotEmpty == true &&
            a.batteryCapacity?.isNotEmpty == true;
      case 'panel':
        return a.panelCapacity?.isNotEmpty == true;
      case 'inverter':
        return a.inverterCapacity?.isNotEmpty == true;
      default:
        return true;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return MultiBlocListener(
      listeners: [
        BlocListener<DigitScannerBloc, DigitScannerState>(
          listener: (ctx, scanState) {
            if (scanState.qrCodes.isNotEmpty && _scanningIndex != null) {
              _updateAsset(_scanningIndex!, scanState.qrCodes.last);
              ctx
                  .read<DigitScannerBloc>()
                  .add(const DigitScannerEvent.handleScanner(qrCode: []));
              _scanningIndex = null;
            }
          },
        ),
        BlocListener<CacheAddNewAssetBloc, CacheAddNewAssetState>(
          listener: (context, state) {
            state.maybeWhen(
              loaded: (entries) {
                setState(() {
                  _assets.clear();
                  for (final entry in entries) {
                    _assets.add(AssetModel(
                      serialNumber: entry.serialNumber,
                      capacity: entry.itemNumber,
                      unit: assetCapacityUom,
                      latitude: entry.latitude,
                      longitude: entry.longitude,
                      photoPath: entry.photoPath,
                      capacityUnit: entry.capacityUnit ?? assetCapacityUom,
                      panelCapacity: entry.panelCapacity,
                      batteryCapacity: entry.batteryCapacity,
                      batteryVoltage: entry.batteryVoltage,
                      batteryType: entry.batteryType,
                      voltageUnit: entry.voltageUnit ?? voltageUom,
                      inverterCapacity: entry.inverterCapacity,
                      inverterCapacityUnit:
                          entry.inverterCapacityUnit ?? assetCapacityUom,
                      currentUnit: entry.currentUnit,
                    ));
                  }
                });

                // Prefill the futures map so each card has its future ready
                _cachedImageFutures.clear();
                for (var i = 0; i < _assets.length; i++) {
                  final path = _assets[i].photoPath;
                  if (path != null) {
                    _cachedImageFutures[i] = _getCachedFile(path);
                  }
                }
              },
              orElse: () {},
            );
          },
        ),
      ],
      child: BlocBuilder<AssetTypeBloc, AssetTypeState>(
        builder: (ctx, assetTypeState) {
          if (_currentProjectId != null && currentAssetType.isNotEmpty) {
            context.read<CacheAssetCountBloc>().add(
                CacheAssetCountEvent.get(_currentProjectId!, currentAssetType));
          }

          return BlocSelector<CacheAssetCountBloc, CacheAssetCountState, int>(
            selector: (st) => st.maybeWhen(
              loaded: (entries) =>
                  entries
                      .firstWhereOrNull((e) => e.assetType == currentAssetType)
                      ?.count ??
                  0,
              orElse: () => 0,
            ),
            builder: (ctx, maxAssets) {
              // Fixed validation logic
              final isDisabled = _assets.length != maxAssets ||
                  _assets.any((a) => !_isAssetComplete(a, currentAssetType));
              debugPrint(
                  "isDisable $isDisabled, assets: ${_assets.length}, max: $maxAssets");

              return Scaffold(
                body: ScrollableContent(
                  header: const BackNavigationHelpHeaderWidget(
                    showBackNavigation: true,
                    showHelp: false,
                  ),
                  enableFixedDigitButton: true,
                  backgroundColor: theme.colorTheme.generic.background,
                  footer: FooterButton(
                    showSuffixIcon: false,
                    text: context.translate(i18.common.coreCommonNext),
                    isDisabled: isDisabled,
                    onPress: () async {
                      if (isDisabled) return;
                      context.read<CacheAddNewAssetBloc>().add(
                          CacheAddNewAssetEvent.deleteAll(
                              _currentProjectId!, currentAssetType));

                      // 2) await completion (either deleted or error)
                      await context
                          .read<CacheAddNewAssetBloc>()
                          .stream
                          .firstWhere((state) => state.maybeWhen(
                                deleted: () => true,
                                error: (_) => true,
                                orElse: () => false,
                              ));

                      for (final asset in _assets) {
                        final newAsset = CacheAddNewAsset(
                          projectId: _currentProjectId!,
                          assetType: currentAssetType,
                          itemNumber: asset.capacity,
                          serialNumber: asset.serialNumber,
                          documentType: "ASSET",
                          photoPath: asset.photoPath!,
                          longitude: asset.longitude!,
                          latitude: asset.latitude!,
                          capacityUnit: asset.capacityUnit ?? assetCapacityUom,
                          panelCapacity: asset.panelCapacity,
                          batteryCapacity: asset.batteryCapacity,
                          batteryVoltage: asset.batteryVoltage,
                          batteryType: asset.batteryType,
                          voltageUnit: voltageUom ?? asset.voltageUnit,
                          inverterCapacity: asset.inverterCapacity,
                          inverterCapacityUnit:
                              asset.inverterCapacityUnit ?? assetCapacityUom,
                          currentUnit: asset.currentUnit,
                        );
                        context
                            .read<CacheAddNewAssetBloc>()
                            .add(CacheAddNewAssetEvent.add(newAsset));
                      }
                      if (_currentProjectId != null) {
                        context.read<CacheAssetCountBloc>().add(
                              CacheAssetCountEvent.update(
                                CacheAssetCount(
                                  projectId: _currentProjectId!,
                                  assetType: currentAssetType,
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
                            ],
                          ),
                          const SizedBox(height: spacer4),
                          assetTypeState.maybeWhen(
                              battery: () => _batteryCapacity(theme, textTheme,
                                  _assets, currentAssetType.titleCase),
                              panel: () => _panelCapacity(
                                    theme,
                                    textTheme,
                                    _assets,
                                    currentAssetType.titleCase,
                                  ),
                              orElse: () => const SizedBox()),
                          ..._assets.asMap().entries.map((e) {
                            return Padding(
                              padding: const EdgeInsets.only(bottom: spacer4),
                              child: _buildAssetCard(
                                context: context,
                                theme: theme,
                                textTheme: textTheme,
                                heading: currentAssetType.titleCase,
                                index: e.key,
                                asset: e.value,
                                maxAsset: maxAssets,
                                assetType: currentAssetType,
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
      key: ValueKey(asset.serialNumber.isEmpty ? index : asset.serialNumber),
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
                flex: 6,
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
              const SizedBox(width: spacer2),
              Expanded(
                flex: 3,
                child: DigitButton(
                  label: 'Scan',
                  type: DigitButtonType.secondary,
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
        BlocBuilder<LocationBloc, LocationState>(
          builder: (context, locationState) {
            return LabeledField(
              label: 'Supporting Photo',
              capitalizedFirstLetter: false,
              child: FutureBuilder<File?>(
                future: _cachedImageFutures.putIfAbsent(
                  index,
                  () => asset.photoPath != null
                      ? _getCachedFile(asset.photoPath!)
                      : Future.value(null),
                ),
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Center(child: CircularProgressIndicator());
                  }

                  final file = snapshot.data;
                  return ImageUploader(
                    initialImages: file != null ? [file] : [],
                    onImagesSelected: (List<File> imageFile) async {
                      if (imageFile.isEmpty) return;
                      final ok = await _ensureLocationLoaded();
                      if (!ok) {
                        context.showSnackBar(
                          const SnackBar(
                              content: Text('Could not fetch location')),
                        );
                        return;
                      }
                      final copiedPath =
                          await copyFileToLocalDir(imageFile.first);
                      setState(() {
                        asset.photoPath = copiedPath;
                        asset.latitude = _latitude.toString();
                        asset.longitude = _longitude.toString();
                        // invalidate this card's future so it reloads the new local file
                        _cachedImageFutures.remove(index);
                      });
                    },
                  );
                },
              ),
            );
          },
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
                    sentenceCaseEnabled: false,
                    items: assetCapacity
                        .map((type) => DropdownItem(name: type, code: type))
                        .toList(),
                    selectedOption: DropdownItem(
                      name: asset.capacity ?? '',
                      code: asset.capacity ?? '',
                    ),
                    onSelect: (DropdownItem selected) {
                      setState(() {
                        asset.capacity = selected.code;
                        asset.inverterCapacity = selected.code;
                      });
                    },
                  ),
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

  Widget _batteryCapacity(ThemeData theme, DigitTextTheme textTheme,
      List<AssetModel> assets, String heading) {
    // Use first asset to control the values for all assets
    final firstAsset =
        assets.isNotEmpty ? assets.first : AssetModel(serialNumber: '');

    return Column(
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
                  sentenceCaseEnabled: false,
                  items: typesField
                      .map((type) => DropdownItem(name: type, code: type))
                      .toList(),
                  selectedOption: DropdownItem(
                    name: firstAsset.batteryType ?? '',
                    code: firstAsset.batteryType ?? '',
                  ),
                  onSelect: (DropdownItem sel) {
                    setState(() {
                      // Update all assets with the same value
                      for (var asset in assets) {
                        asset.batteryType = sel.code;
                      }
                    });
                  }),
            ),
            Row(
              children: [
                Expanded(
                  flex: 3,
                  child: LabeledField(
                    label: 'Voltage',
                    capitalizedFirstLetter: false,
                    child: DigitDropdown(
                      sentenceCaseEnabled: false,
                      items: voltages
                          .map((type) => DropdownItem(name: type, code: type))
                          .toList(),
                      selectedOption: DropdownItem(
                        name: firstAsset.batteryVoltage ?? '',
                        code: firstAsset.batteryVoltage ?? '',
                      ),
                      onSelect: (DropdownItem sel) {
                        setState(() {
                          // Update all assets with the same value
                          for (var asset in assets) {
                            asset.batteryVoltage = sel.code;
                          }
                        });
                      },
                    ),
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
                      initialValue: '$voltageUom',
                      keyboardType: TextInputType.text,
                    ),
                  ),
                ),
              ],
            ),
            Row(
              children: [
                Expanded(
                  flex: 3,
                  child: LabeledField(
                    label: 'Current',
                    capitalizedFirstLetter: false,
                    child: DigitDropdown(
                      sentenceCaseEnabled: false,
                      items: assetCapacity
                          .map((type) => DropdownItem(name: type, code: type))
                          .toList(),
                      selectedOption: DropdownItem(
                        name: firstAsset.batteryCapacity ?? '',
                        code: firstAsset.batteryCapacity ?? '',
                      ),
                      onSelect: (DropdownItem sel) {
                        setState(() {
                          // Update all assets with the same value
                          for (var asset in assets) {
                            asset.batteryCapacity = sel.code;
                          }
                        });
                      },
                    ),
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

  Widget _panelCapacity(ThemeData theme, DigitTextTheme textTheme,
      List<AssetModel> assets, String heading) {
    // Use first asset to control the values for all assets
    final firstAsset =
        assets.isNotEmpty ? assets.first : AssetModel(serialNumber: '');

    return Column(
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
                        sentenceCaseEnabled: false,
                        items: assetCapacity
                            .map((type) => DropdownItem(name: type, code: type))
                            .toList(),
                        selectedOption: DropdownItem(
                          name: firstAsset.panelCapacity ?? '',
                          code: firstAsset.panelCapacity ?? '',
                        ),
                        onSelect: (DropdownItem sel) {
                          setState(() {
                            // Update all assets with the same value
                            for (var asset in assets) {
                              asset.panelCapacity = sel.code;
                            }
                          });
                        }),
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
}
