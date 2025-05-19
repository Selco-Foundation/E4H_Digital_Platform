import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_asset_count.dart';
import '../router/app_router.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class MediaUploadPage extends StatefulWidget {
  const MediaUploadPage({super.key});

  @override
  State<MediaUploadPage> createState() => _MediaUploadPageState();
}

class _MediaUploadPageState extends State<MediaUploadPage> {
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
          progress: 6,
        )));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final heading = state.when(
          initial: () => '',
          inverter: () => 'Inverter',
          battery: () => 'Battery',
          panel: () => 'Panel',
        );
        return Scaffold(
          appBar: const Navbar(),
          drawer: const CustomDrawer(),
          body: ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            header: const BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
            ),
            footer: FooterButton(
              showSuffixIcon: false,
              text: i18.common.coreCommonNext,
              onPress: () {
                context.router.push(const AssetSummaryRoute());
              },
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    horizontal: spacer2, vertical: spacer4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                      AppStepper(context: context, activeIndex: 6),
                    ]),
                    const SizedBox(height: spacer4),
                    DigitCard(
                      children: [
                        Text(
                          '$heading Images',
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        Text(
                          'Please upload images of the installed asset.',
                          style: textTheme.bodyL
                              .copyWith(color: theme.colorTheme.text.primary),
                        ),
                        LabeledField(
                          label: 'Upload images',
                          child: FileUploadWidget(
                            label: 'Upload',
                            onFilesSelected: (List<PlatformFile> files) {
                              Map<PlatformFile, String?> fileErrors = {};
                              return fileErrors;
                            },
                            allowMultiples: true,
                            showPreview: true,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: spacer4),
                    DigitCard(
                      children: [
                        Text(
                          '$heading Videos',
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        Text(
                          'Please upload videos of the installed asset.',
                          style: textTheme.bodyL
                              .copyWith(color: theme.colorTheme.text.primary),
                        ),
                        LabeledField(
                          label: 'Upload videos',
                          child: FileUploadWidget(
                            allowedExtensions: const ['mp4'],
                            label: 'Upload',
                            onFilesSelected: (List<PlatformFile> files) {
                              Map<PlatformFile, String?> fileErrors = {};
                              return fileErrors;
                            },
                            allowMultiples: true,
                            showPreview: true,
                          ),
                        ),
                      ],
                    )
                  ],
                ),
              )
            ],
          ),
        );
      },
    );
  }
}
