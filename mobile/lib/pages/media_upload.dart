import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/upload_popUp.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_media_upload/cache_media_upload.dart';
import '../blocs/selected_project/selected_project.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_media_upload.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class MediaUploadPage extends StatefulWidget {
  const MediaUploadPage({super.key});

  @override
  State<MediaUploadPage> createState() => _MediaUploadPageState();
}

class _MediaUploadPageState extends State<MediaUploadPage> {
  String? _currentProjectId;
  List<PlatformFile> _selectedImages = [];
  List<PlatformFile> _selectedVideos = [];
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;

  @override
  void initState() {
    super.initState();
    final locBloc = context.read<LocationBloc>();
    locBloc.add(const LocationEvent.requestPermission());
    locBloc.add(const LocationEvent.requestService());
    // 2. Listen to updates so we keep _latitude/_longitude up to date:
    _locSub = locBloc.stream.listen((locationState) {
      if (locationState.latitude != null && locationState.longitude != null) {
        setState(() {
          _latitude = locationState.latitude;
          _longitude = locationState.longitude;
        });
      }
    });
    final assetType = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );
    final selState = context.read<SelectedProjectBloc>().state;
    selState.whenOrNull(selected: (project) {
      _currentProjectId = project.project.id;
      _updateProgress(project.project.id, assetType);
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
    // If already have coords, return immediately
    if (locBloc.state.latitude != null && locBloc.state.longitude != null) {
      return true;
    }
    try {
      final state = await locBloc.stream
          .firstWhere((s) => s.latitude != null && s.longitude != null)
          .timeout(timeout);
      // local vars already updated in listener above, but set again to be safe
      setState(() {
        _latitude = state.latitude;
        _longitude = state.longitude;
      });
      return true;
    } catch (_) {
      return false;
    }
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
        final assetType = state.when(
          initial: () => '',
          inverter: () => 'Inverter',
          battery: () => 'Battery',
          panel: () => 'Panel',
        );

        final bool isDisabled =
            _selectedImages.isEmpty && _selectedVideos.isEmpty;

        return Scaffold(
          body: ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            header: BackNavigationHelpHeaderWidget(
              showBackNavigation: true,
              showHelp: false,
              defaultPopRoute: false,
              handleback: () {
                context.router.replace(const AddNewAssetRoute());
              },
            ),
            footer: FooterButton(
              isDisabled: isDisabled,
              showSuffixIcon: false,
              text: i18.common.coreCommonNext,
              onPress: () async {
                if (_currentProjectId == null) return;

                for (final file in _selectedImages) {
                  final copiedPath = await copyFileToLocalDir(File(file.path!));
                  final newEntry = CacheMediaUpload(
                      projectId: _currentProjectId!,
                      assetType: assetType.toLowerCase(),
                      itemNumber: file.name,
                      itemType: 'image',
                      filePath: copiedPath,
                      longitude: _longitude.toString(),
                      latitude: _longitude.toString());
                  context
                      .read<CacheMediaUploadBloc>()
                      .add(CacheMediaUploadEvent.add(newEntry));

                  for (final file in _selectedVideos) {
                    final copiedPath =
                        await copyFileToLocalDir(File(file.path!));
                    final newEntry = CacheMediaUpload(
                      projectId: _currentProjectId!,
                      assetType: assetType.toLowerCase(),
                      itemNumber: file.name,
                      itemType: 'video',
                      filePath: copiedPath,
                      longitude: _longitude.toString() ?? '--',
                      latitude: _longitude.toString() ?? '--',
                    );
                    context
                        .read<CacheMediaUploadBloc>()
                        .add(CacheMediaUploadEvent.add(newEntry));
                  }
                }
                context.router.push(const AssetSummaryRoute());
              },
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    vertical: spacer2, horizontal: spacer4),
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
                          '$assetType Images',
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
                              _ensureLocationLoaded().then((ok) {
                                if (!ok) {
                                  context.showSnackBar(
                                    const SnackBar(
                                        content:
                                            Text('Could not fetch location')),
                                  );
                                }
                                setState(() {
                                  _selectedImages = files;
                                });
                                debugPrint(
                                    "latitude $_latitude, longitude $_longitude");
                              });

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
                          '$assetType Videos',
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
                            allowedExtensions: const [
                              'mp4',
                              'mov',
                              'mkv',
                              'avi',
                              'webm',
                              'flv',
                              'vob',
                              'ts',
                              'm2ts'
                            ],
                            label: 'Upload',
                            onFilesSelected: (List<PlatformFile> files) {
                              _ensureLocationLoaded().then((ok) {
                                if (!ok) {
                                  context.showSnackBar(
                                    const SnackBar(
                                        content:
                                            Text('Could not fetch location')),
                                  );
                                }
                                setState(() {
                                  _selectedVideos = files;
                                });
                                debugPrint(
                                    "latitude $_latitude, longitude $_longitude");
                              });
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
