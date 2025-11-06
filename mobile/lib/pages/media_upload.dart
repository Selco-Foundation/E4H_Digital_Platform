import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path/path.dart' show basename;
import 'package:recase/recase.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_media_upload/cache_media_upload.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_asset_count.dart';
import '../data/nosql/cache_media_upload.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/cards/stepper.dart';
import '../widgets/customized_digit_widget/file_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class MediaUploadPage extends StatefulWidget {
  const MediaUploadPage({super.key});

  @override
  State<MediaUploadPage> createState() => _MediaUploadPageState();
}

class _MediaUploadPageState extends State<MediaUploadPage> {
  String? _currentActivityFacilityId;
  int _imageKeyCounter = 0;
  int _videoKeyCounter = 0;
  List<PlatformFile> _selectedImages = [];
  List<PlatformFile> _selectedVideos = [];
  bool _isImagesInitLoading = true;
  bool _isVideosInitLoading = true;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;
  String userType = "";
  late String assetType = "";

  @override
  void initState() {
    super.initState();

    // 1) start location updates:
    final locBloc = context.read<LocationBloc>();
    locBloc.add(const LocationEvent.requestPermission());
    locBloc.add(const LocationEvent.requestService());
    _locSub = locBloc.stream.listen((st) {
      if (st.latitude != null && st.longitude != null) {
        setState(() {
          _latitude = st.latitude;
          _longitude = st.longitude;
        });
      }
    });

    userType = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => USER_TYPES.SUPERVISOR.name,
          orElse: () => USER_TYPES.FIELD_STAFF.name,
        );

    assetType = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );
    context.read<SelectedActivityFacilityBloc>().state.whenOrNull(
        selected: (proj) {
      _currentActivityFacilityId = proj.activityFacility.id;
      context
          .read<CacheAssetCountBloc>()
          .add(CacheAssetCountEvent.update(CacheAssetCount(
            activityFacilityId: proj.activityFacility.id,
            assetType: assetType,
            progress: 6,
          )));

      context.read<CacheMediaUploadBloc>().add(
            CacheMediaUploadEvent.get(proj.activityFacility.id, assetType),
          );
    });
  }

  @override
  void dispose() {
    _locSub?.cancel();
    super.dispose();
  }

  Future<bool> _ensureLocationLoaded({
    Duration timeout = const Duration(seconds: 10),
  }) async {
    final locBloc = context.read<LocationBloc>();
    if (locBloc.state.latitude != null && locBloc.state.longitude != null) {
      return true;
    }
    try {
      final st = await locBloc.stream
          .firstWhere((s) => s.latitude != null && s.longitude != null)
          .timeout(timeout);
      setState(() {
        _latitude = st.latitude;
        _longitude = st.longitude;
      });
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<void> _populateFromCache(List<CacheMediaUpload> entries) async {
    final images = <PlatformFile>[];
    final videos = <PlatformFile>[];

    final hasImageEntries = entries.any((e) => e.itemType == 'image');
    final hasVideoEntries = entries.any((e) => e.itemType == 'video');

    if (hasImageEntries || hasVideoEntries) {
      setState(() {
        _isImagesInitLoading = hasImageEntries;
        _isVideosInitLoading = hasVideoEntries;
      });
    }

    for (final e in entries) {
      final file = await getCachedFile(e.filePath);
      if (file == null) continue;
      final pf = PlatformFile(
        name: basename(file.path),
        path: file.path,
        size: await file.length(),
      );
      if (e.itemType == 'image') {
        images.add(pf);
      } else if (e.itemType == 'video') {
        videos.add(pf);
      }
    }

    setState(() {
      _selectedImages = images;
      _selectedVideos = videos;
      _imageKeyCounter++;
      _videoKeyCounter++;
      if (hasImageEntries) _isImagesInitLoading = false;
      if (hasVideoEntries) _isVideosInitLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<CacheMediaUploadBloc, CacheMediaUploadState>(
      listener: (ctx, state) {
        state.maybeWhen(
          loaded: (entries) => _populateFromCache(entries),
          orElse: () {},
        );
      },
      child: BlocBuilder<AssetTypeBloc, AssetTypeState>(
        builder: (ctx, state) {
          assetType = assetType.titleCase;

          final isDisabled = _selectedImages.isEmpty && _selectedVideos.isEmpty;

          return Scaffold(
            body: ScrollableContent(
              enableFixedDigitButton: true,
              backgroundColor: theme.colorTheme.generic.background,
              header: const BackNavigationHelpHeaderWidget(
                showBackNavigation: true,
                showHelp: false,
              ),
              footer: FooterButton(
                isDisabled: isDisabled,
                showSuffixIcon: false,
                text: i18.common.coreCommonNext,
                onPress: () async {
                  if (_currentActivityFacilityId == null) return;
                  context.read<CacheMediaUploadBloc>().add(
                        CacheMediaUploadEvent.deleteAll(
                          _currentActivityFacilityId!,
                          assetType.toLowerCase(),
                        ),
                      );

                  await context.read<CacheMediaUploadBloc>().stream.firstWhere(
                        (state) => state.maybeWhen(
                          deleted: () => true,
                          error: (_) => true,
                          orElse: () => false,
                        ),
                      );

                  for (final file in _selectedImages) {
                    final copied = await copyFileToLocalDir(File(file.path!));
                    final entry = CacheMediaUpload(
                      activityFacilityId: _currentActivityFacilityId!,
                      assetType: assetType.toLowerCase(),
                      itemNumber: file.name,
                      itemType: 'image',
                      userType: userType,
                      filePath: copied,
                      longitude: _longitude.toString(),
                      latitude: _latitude.toString(),
                    );
                    context
                        .read<CacheMediaUploadBloc>()
                        .add(CacheMediaUploadEvent.add(entry));
                  }
                  for (final file in _selectedVideos) {
                    final copied = await copyFileToLocalDir(File(file.path!));
                    final entry = CacheMediaUpload(
                      activityFacilityId: _currentActivityFacilityId!,
                      assetType: assetType.toLowerCase(),
                      itemNumber: file.name,
                      itemType: 'video',
                      filePath: copied,
                      userType: userType,
                      longitude: _longitude.toString(),
                      latitude: _latitude.toString(),
                    );
                    context
                        .read<CacheMediaUploadBloc>()
                        .add(CacheMediaUploadEvent.add(entry));
                  }

                  context
                      .read<InboxTypeBloc>()
                      .add(const InboxTypeEvent.typeSelected(0));
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
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          AppStepper(context: context, activeIndex: 6),
                        ],
                      ),
                      const SizedBox(height: spacer4),

                      // ── Images Card ──
                      DigitCard(children: [
                        Text(
                          '$assetType Images',
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const SizedBox(height: spacer2),
                        FileUploadWidget(
                          allowedExtensions: const [
                            "jpg",
                            'jpeg',
                            "png",
                            'JPG',
                            'JPEG',
                            'PNG'
                          ],
                          // key: ValueKey('images-$_imageKeyCounter'),
                          label: 'Upload Images',
                          allowMultiples: true,
                          showPreview: true,
                          initialFiles: _selectedImages,
                          onFilesSelected: (files) {
                            setState(() {
                              _selectedImages = files;
                              // _imageKeyCounter++;
                            });
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
                            return {for (final f in files) f: null};
                          },
                        ),
                        if (_isImagesInitLoading)
                          const Center(child: CircularProgressIndicator())
                      ]),

                      const SizedBox(height: spacer4),

                      DigitCard(children: [
                        Text(
                          '$assetType Videos',
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const SizedBox(height: spacer2),
                        FileUploadWidget(
                          // key: ValueKey('videos-$_videoKeyCounter'),
                          label: 'Upload Videos',
                          allowMultiples: true,
                          showPreview: false,
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
                          initialFiles: _selectedVideos,
                          onFilesSelected: (files) {
                            print("Files: ${files.length}");
                            setState(() {
                              _selectedVideos = files;
                              // _videoKeyCounter++;
                            });
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
                            return {for (final f in files) f: null};
                          },
                        ),
                        if (_isVideosInitLoading)
                          const Center(child: CircularProgressIndicator())
                      ]),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
