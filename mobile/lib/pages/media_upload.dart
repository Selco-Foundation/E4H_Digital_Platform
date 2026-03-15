import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path/path.dart' show basename;
import 'package:recase/recase.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_media_upload/cache_media_upload.dart';
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
import '../widgets/customized_digit_widget/image_uploader.dart';
import '../widgets/customized_digit_widget/video_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class MediaUploadPage extends StatefulWidget {
  const MediaUploadPage({super.key});

  @override
  State<MediaUploadPage> createState() => _MediaUploadPageState();
}

class _MediaUploadPageState extends State<MediaUploadPage> {
  String? _currentActivityFacilityId;
  List<File> _selectedImages = [];
  List<File> _selectedVideos = [];
  bool _isImagesInitLoading = false;
  bool _isVideosInitLoading = false;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;
  String userType = "";
  late String assetType = "";

  @override
  void initState() {
    super.initState();

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
    final hasImageEntries = entries.any((e) => e.itemType == 'image');
    final hasVideoEntries = entries.any((e) => e.itemType == 'video');

    if (hasImageEntries || hasVideoEntries) {
      setState(() {
        _isImagesInitLoading = hasImageEntries;
        _isVideosInitLoading = hasVideoEntries;
      });
    }

    final futures = entries.map((e) async {
      final file = await getCachedFile(e.filePath);
      if (file == null) return null;

      return (
        entry: e,
        file: file,
      );
    }).toList();

    final results = await Future.wait(futures);
    final images = <File>[];
    final videos = <File>[];

    for (final res in results) {
      if (res == null) continue;
      final e = res.entry;
      final file = res.file;

      if (e.itemType == 'image') {
        images.add(file);
      } else if (e.itemType == 'video') {
        videos.add(file);
      }
    }
    if (mounted) {
      setState(() {
        _selectedImages = images;
        _selectedVideos = videos;
        if (hasImageEntries) _isImagesInitLoading = false;
        if (hasVideoEntries) _isVideosInitLoading = false;
      });
    }
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

          final isDisabled = _selectedImages.isEmpty;

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
                text: context.translate(i18.common.coreCommonNext),
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
                    final copied = await copyFileToLocalDir(file);
                    final entry = CacheMediaUpload(
                      activityFacilityId: _currentActivityFacilityId!,
                      assetType: assetType.toLowerCase(),
                      itemNumber: basename(file.path),
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
                    final copied = await copyFileToLocalDir(file);
                    final entry = CacheMediaUpload(
                      activityFacilityId: _currentActivityFacilityId!,
                      assetType: assetType.toLowerCase(),
                      itemNumber: basename(file.path),
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
                      DigitCard(children: [
                        Text(
                          '$assetType Images',
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        Text(
                          'Please add all images of the $assetType',
                          style: textTheme.bodyL.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const SizedBox(height: spacer2),
                        ImageUploader(
                          label: 'Upload Images',
                          allowMultiples: true,
                          initialImages: _selectedImages,
                          onImagesSelected: (files) {
                            setState(() {
                              _selectedImages = files;
                            });
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
                          },
                        ),
                        if (_isImagesInitLoading)
                          const Center(child: CircularProgressIndicator())
                      ]),
                      const SizedBox(height: spacer4),
                      DigitCard(children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              '$assetType Videos',
                              style: textTheme.headingXl.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                            Text(
                              '(Optional)',
                              style: textTheme.bodyL.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                          ],
                        ),
                        const SizedBox(height: spacer2),
                        VideoUploader(
                          label: 'Upload Videos',
                          allowMultiples: true,
                          initialVideos: _selectedVideos,
                          onVideosSelected: (files) {
                            setState(() {
                              _selectedVideos = files;
                            });
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
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
