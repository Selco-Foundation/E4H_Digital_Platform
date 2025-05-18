import 'dart:io';

import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/pages/qr_scanner.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/DropdownModels.dart';
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

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_asset_count.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class AddNewAssetPage extends StatefulWidget {
  const AddNewAssetPage({super.key});

  @override
  State<AddNewAssetPage> createState() => _AddNewAssetPageState();
}

class _AddNewAssetPageState extends State<AddNewAssetPage> {
  String? _currentProjectId;

  @override
  void initState() {
    super.initState();
    final assetType = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );
    final selState = context.read<SelectedProjectBloc>().state;
    selState.whenOrNull(selected: (project) {
      _currentProjectId = project.id;
      _updateProgress(project.id, assetType);
    });
  }

  void _updateProgress(String projectId, assetType) {
    context
        .read<CacheAssetCountBloc>()
        .add(CacheAssetCountEvent.update(CacheAssetCount(
          projectId: projectId,
          assetType: assetType,
          progress: 5,
        )));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final assetHeading = state.when(
          initial: () => 'Asset',
          inverter: () => 'Inverter',
          battery: () => 'Battery',
          panel: () => 'Panel',
        );
        return Scaffold(
          appBar: const Navbar(),
          drawer: const CustomDrawer(),
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
                onPress: () {
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
                          AppStepper(context: context, activeIndex: 4)
                        ],
                      ),
                      const SizedBox(height: spacer4),
                      state.maybeWhen(
                          battery: () => Column(
                                children: [
                                  DigitCard(
                                    children: [
                                      Text(
                                        '$assetHeading Capacity',
                                        style: textTheme.headingXl.copyWith(
                                            color: theme
                                                .colorTheme.primary.primary2),
                                      ),
                                      LabeledField(
                                        label: '$assetHeading Type',
                                        capitalizedFirstLetter: false,
                                        child: const DigitDropdown(items: [
                                          DropdownItem(
                                              name: 'Lead acid', code: 'lead'),
                                          DropdownItem(
                                              name: 'Pure acid', code: 'acid'),
                                        ]),
                                      ),
                                      Row(
                                        children: [
                                          const Expanded(
                                            flex: 3,
                                            child: LabeledField(
                                              label: 'Voltage',
                                              capitalizedFirstLetter: false,
                                              child: DigitDropdown(items: [
                                                DropdownItem(
                                                    name: '1', code: '1'),
                                                DropdownItem(
                                                    name: '2', code: '2'),
                                                DropdownItem(
                                                    name: '3', code: '3')
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
                                                controller:
                                                    TextEditingController(),
                                                isDisabled: true,
                                                readOnly: true,
                                                initialValue: 'Volts',
                                                keyboardType:
                                                    TextInputType.text,
                                                onChange: (value) {},
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
                                                DropdownItem(
                                                    name: '1', code: '1'),
                                                DropdownItem(
                                                    name: '2', code: '2'),
                                                DropdownItem(
                                                    name: '3', code: '3')
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
                                                controller:
                                                    TextEditingController(),
                                                isDisabled: true,
                                                readOnly: true,
                                                initialValue: 'Amps',
                                                keyboardType:
                                                    TextInputType.text,
                                                onChange: (value) {},
                                              ),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ],
                                  ),
                                  const SizedBox(height: spacer8),
                                ],
                              ),
                          panel: () => Column(
                                children: [
                                  DigitCard(
                                    children: [
                                      Text(
                                        '$assetHeading Capacity',
                                        style: textTheme.headingXl.copyWith(
                                            color: theme
                                                .colorTheme.primary.primary2),
                                      ),
                                      Row(
                                        children: [
                                          const Expanded(
                                            flex: 3,
                                            child: LabeledField(
                                              label: 'Voltage',
                                              capitalizedFirstLetter: false,
                                              child: DigitDropdown(items: [
                                                DropdownItem(
                                                    name: '1', code: '1'),
                                                DropdownItem(
                                                    name: '2', code: '2'),
                                                DropdownItem(
                                                    name: '3', code: '3')
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
                                                controller:
                                                    TextEditingController(),
                                                isDisabled: true,
                                                readOnly: true,
                                                initialValue: 'Volts',
                                                keyboardType:
                                                    TextInputType.text,
                                                onChange: (value) {},
                                              ),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ],
                                  ),
                                  const SizedBox(height: spacer8),
                                ],
                              ),
                          orElse: () => Container()),
                      DigitCard(
                        children: [
                          Text(
                            '$assetHeading 1',
                            style: textTheme.headingXl.copyWith(
                                color: theme.colorTheme.primary.primary2),
                          ),
                          LabeledField(
                            label: 'Serial Number',
                            capitalizedFirstLetter: false,
                            child: Row(
                              children: [
                                Expanded(
                                    flex: 5,
                                    child:
                                        // DigitTextFormInput(
                                        //   controller: TextEditingController(),
                                        //   isDisabled: true,
                                        //   innerLabel: 'SR5955340958',
                                        //   keyboardType: TextInputType.none,
                                        // ),
                                        BlocListener<DigitScannerBloc,
                                            DigitScannerState>(
                                      listener: (context, scannerState) {
                                        if (scannerState.qrCodes.isNotEmpty) {
                                          // scannerState.qrCodes.
                                        }
                                      },
                                      child: BlocBuilder<DigitScannerBloc,
                                          DigitScannerState>(
                                        builder: (context, scannerState) {
                                          if (scannerState.qrCodes.isNotEmpty) {
                                            return DigitTextFormInput(
                                              controller:
                                                  TextEditingController(),
                                              isDisabled: true,
                                              innerLabel:
                                                  scannerState.qrCodes.last,
                                              keyboardType: TextInputType.none,
                                            );
                                          } else {
                                            return DigitTextFormInput(
                                              controller:
                                                  TextEditingController(),
                                              isDisabled: true,
                                              innerLabel: "",
                                              keyboardType: TextInputType.none,
                                            );
                                          }
                                        },
                                      ),
                                    )),
                                const SizedBox(width: spacer4),
                                Expanded(
                                  flex: 2,
                                  child: DigitButton(
                                    label: 'Scan',
                                    type: DigitButtonType.primary,
                                    onPressed: () {
                                      Navigator.of(context).push(
                                        MaterialPageRoute(
                                          builder: (ctx) => BlocProvider.value(
                                            value: BlocProvider.of<
                                                DigitScannerBloc>(context),
                                            // or create a new one: DigitScannerBloc(...)
                                            child: const DigitScannerPage(
                                                quantity: 1, isGS1code: false),
                                          ),
                                        ),
                                      );
                                    },
                                    size: DigitButtonSize.large,
                                    mainAxisSize: MainAxisSize.max,
                                  ),
                                )
                              ],
                            ),
                          ),
                          state.maybeWhen(
                              inverter: () => Row(
                                    children: [
                                      const Expanded(
                                        flex: 3,
                                        child: LabeledField(
                                          label: 'Capacity',
                                          capitalizedFirstLetter: false,
                                          child: DigitDropdown(items: [
                                            DropdownItem(name: '1', code: '1'),
                                            DropdownItem(name: '2', code: '2'),
                                            DropdownItem(name: '3', code: '3')
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
                                            readOnly: true,
                                            initialValue: 'KvA',
                                            keyboardType: TextInputType.text,
                                            onChange: (value) {},
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                              orElse: () => Container()),
                          LabeledField(
                            label: 'Scan Serial No',
                            child: ImageUploader(
                              onImagesSelected: (List<File> imageFile) {},
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: spacer3),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            'Add New Asset',
                            style: textTheme.headingM.copyWith(
                                color: theme.colorTheme.primary.primary1),
                          ),
                        ],
                      ),
                      BlocListener<DigitScannerBloc, DigitScannerState>(
                        listener: (context, scannerState) {
                          if (scannerState.qrCodes.isNotEmpty) {
                            // Handle scanned codes
                          }
                        },
                        child: BlocBuilder<DigitScannerBloc, DigitScannerState>(
                          builder: (context, scannerState) {
                            if (scannerState.qrCodes.isNotEmpty) {
                              return Text(scannerState.qrCodes.last);
                            } else {
                              return Container();
                            }
                          },
                        ),
                      )
                    ],
                  ),
                )
              ]),
          floatingActionButton: FloatingActionButton(
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (ctx) => BlocProvider.value(
                    value: BlocProvider.of<DigitScannerBloc>(context),
                    // or create a new one: DigitScannerBloc(...)
                    child:
                        const DigitScannerPage(quantity: 1, isGS1code: false),
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }
}
