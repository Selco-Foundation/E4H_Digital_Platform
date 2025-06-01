import 'package:collection/collection.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_text_form_input.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/blocs/cache_asset_count/cache_asset_count.dart';
import 'package:selco/data/nosql/cache_asset_count.dart';

import '../blocs/app_init/app_init.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../model/asset_type/asset_type.dart';
import '../model/mdms/mdms.dart';
import '../model/system/system.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class SpecificationPage extends StatefulWidget {
  const SpecificationPage({super.key});

  @override
  State<SpecificationPage> createState() => _SpecificationPageState();
}

class _SpecificationPageState extends State<SpecificationPage> {
  String? _currentProjectId;
  String assetType = "";

  @override
  void initState() {
    super.initState();
    assetType = context.read<AssetTypeBloc>().state.when(
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
          progress: 3,
        )));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final specHeading = state.when(
          initial: () => 'Specification',
          inverter: () => 'Inverter Specifications',
          battery: () => 'Battery Specifications',
          panel: () => 'Panel Specifications',
        );

        return Scaffold(
            appBar: const Navbar(),
            body: ScrollableContent(
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                footer: FooterButton(
                  showSuffixIcon: false,
                  text: i18.common.coreCommonNext,
                  onPress: () {
                    context.router.push(const AssetTypeDetailRoute());
                  },
                ),
                children: [
                  BlocBuilder<AppInitialization, InitState>(
                    builder: (initContext, initState) {
                      final List<Mdms<System>> systemList = initState.maybeWhen(
                          orElse: () => [],
                          initialized:
                              (appConfig, assetCount, assetType, system) =>
                                  system);

                      final List<Mdms<AssetType>> assetTypeList =
                          initState.maybeWhen(
                        orElse: () => [],
                        initialized:
                            (appConfig, assetCount, assetType, system) =>
                                assetType,
                      );

                      final systemCode = systemList.lastOrNull?.data.code;

                      // Find the BATTERY asset type
                      final selectedAssetType = assetTypeList
                          .map((e) => e.data)
                          .firstWhereOrNull((asset) =>
                              asset.code.toUpperCase() ==
                              assetType.toUpperCase());

                      // From that battery, get the total_capacity form field
                      final totalCapacityField =
                          selectedAssetType?.formFields.firstWhereOrNull(
                        (field) =>
                            field.key == "total_capacity" &&
                            field.system == systemCode,
                      );

                      // Get the total_capacity_uom form field
                      final totalCapacityUomField =
                          selectedAssetType?.formFields.firstWhereOrNull(
                        (field) =>
                            field.key == "total_capacity_uom" &&
                            field.system == systemCode,
                      );

                      // Use first option or fallback to empty string
                      final String totalCapacity =
                          totalCapacityField?.options?.firstOrNull ?? '';
                      final String totalCapacityUom =
                          totalCapacityUomField?.options?.firstOrNull ?? '';

                      return Padding(
                        padding: const EdgeInsets.symmetric(
                            horizontal: spacer2, vertical: spacer4),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                AppStepper(context: context, activeIndex: 2)
                              ],
                            ),
                            const SizedBox(height: spacer4),
                            DigitCard(children: [
                              Text(
                                specHeading,
                                style: textTheme.headingXl.copyWith(
                                    color: theme.colorTheme.primary.primary2),
                              ),
                              LabeledField(
                                label: 'System',
                                labelStyle: textTheme.headingS.copyWith(
                                    color: theme.colorTheme.text.primary),
                                capitalizedFirstLetter: false,
                                child: DigitTextFormInput(
                                  controller: TextEditingController(),
                                  isDisabled: true,
                                  readOnly: true,
                                  initialValue: systemList.last.data.name,
                                  keyboardType: TextInputType.none,
                                ),
                              ),
                              Row(
                                children: [
                                  Expanded(
                                    flex: 3,
                                    child: LabeledField(
                                      label: 'Total Capacity',
                                      labelStyle: textTheme.headingS.copyWith(
                                          color: theme.colorTheme.text.primary),
                                      capitalizedFirstLetter: false,
                                      child: DigitTextFormInput(
                                        keyboardType: TextInputType.none,
                                        controller: TextEditingController(),
                                        isDisabled: true,
                                        readOnly: true,
                                        initialValue: totalCapacity,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: spacer6),
                                  Expanded(
                                    flex: 1,
                                    child: LabeledField(
                                      label: 'Unit',
                                      labelStyle: textTheme.headingS.copyWith(
                                          color: theme.colorTheme.text.primary),
                                      capitalizedFirstLetter: false,
                                      child: DigitTextFormInput(
                                        controller: TextEditingController(),
                                        isDisabled: true,
                                        readOnly: true,
                                        initialValue: totalCapacityUom,
                                        keyboardType: TextInputType.text,
                                        onChange: (value) {},
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ])
                          ],
                        ),
                      );
                    },
                  )
                ]));
      },
    );
  }
}
