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
import '../model/brand/brand.dart';
import '../model/mdms/mdms.dart';
import '../model/warranty/warranty.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/navbar.dart';

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
        initialized:
            (appConfig, assetCount, assetType, system, warranty, brand) {
          assetWarranties = warranty
              .map((w) => w.data)
              .where((w) =>
                  w.assetTypeCode.toUpperCase() == assetTypeTitle.toUpperCase())
              // .map((w) => w.duration)
              .toList();

          assetBrands = brand
              .map((b) => b.data)
              .where((w) =>
                  w.assetTypeCode.toUpperCase() == assetTypeTitle.toUpperCase())
              .toList();
          return assetType;
        });

    final selState = context.read<SelectedProjectBloc>().state;
    selState.whenOrNull(selected: (project) {
      _currentProjectId = project.id;
      _updateProgress(project.id, assetTypeTitle);
    });
  }

  void _updateProgress(String projectId, assetType) {
    context
        .read<CacheAssetCountBloc>()
        .add(CacheAssetCountEvent.update(CacheAssetCount(
          projectId: projectId,
          assetType: assetType,
          progress: 4,
        )));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final detailHeading = state.when(
          initial: () => 'Details',
          inverter: () => 'Inverter Details',
          battery: () => 'Battery Details',
          panel: () => 'Panel Details',
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
                    context.router.push(const AddNewAssetRoute());
                  },
                ),
                children: [
                  // BlocBuilder<AppInitialization, InitState>(
                  //   builder: (initContext, initState) {
                  //     return
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
                          LabeledField(
                            label: 'Warranty Start Date',
                            labelStyle: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                            capitalizedFirstLetter: false,
                            child: DigitDateFormInput(
                              controller: TextEditingController(),
                              initialValue: 'Default Today Date',
                              isDisabled: true,
                              readOnly: true,
                            ),
                          ),
                          LabeledField(
                              label: 'Warranty Duration',
                              labelStyle: textTheme.headingS.copyWith(
                                  color: theme.colorTheme.text.primary),
                              capitalizedFirstLetter: false,
                              child: DigitDropdown(
                                  items: assetWarranties
                                      .map((type) => DropdownItem(
                                            name: type.duration,
                                            code: type.duration,
                                          ))
                                      .toList())),
                          LabeledField(
                              label: 'Brand',
                              labelStyle: textTheme.headingS.copyWith(
                                  color: theme.colorTheme.text.primary),
                              capitalizedFirstLetter: false,
                              child: DigitDropdown(
                                  items: assetBrands
                                      .map((type) => DropdownItem(
                                            name: type.name,
                                            code: type.code,
                                          ))
                                      .toList())),
                          LabeledField(
                            label: 'Model Number',
                            labelStyle: textTheme.headingS
                                .copyWith(color: theme.colorTheme.text.primary),
                            capitalizedFirstLetter: false,
                            child: DigitTextFormInput(
                              controller: TextEditingController(),
                              innerLabel: 'SR45934295',
                              keyboardType: TextInputType.text,
                            ),
                          ),
                        ])
                      ],
                    ),
                  )
                  //   },
                  // )
                ]));
      },
    );
  }
}
