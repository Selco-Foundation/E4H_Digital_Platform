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
import 'package:http/http.dart' as http;
import 'package:path/path.dart' show basename;
import 'package:path_provider/path_provider.dart';
import 'package:recase/recase.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/cache_asset_count/cache_asset_count.dart';
import '../blocs/cache_media_upload/cache_media_upload.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
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
  int _imageKeyCounter = 0;
  int _videoKeyCounter = 0;
  List<PlatformFile> _selectedImages = [];
  List<PlatformFile> _selectedVideos = [];
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;
  String userType = "";

  // ─── cache for downloaded files ───
  final Map<String, File> _fileCache = {};
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

    // 2) grab projectId & dispatch initial load:
    assetType = context.read<AssetTypeBloc>().state.when(
          initial: () => '',
          inverter: () => 'inverter',
          battery: () => 'battery',
          panel: () => 'panel',
        );
    context.read<SelectedProjectBloc>().state.whenOrNull(selected: (proj) {
      _currentProjectId = proj.project.id;
      // update progress
      context
          .read<CacheAssetCountBloc>()
          .add(CacheAssetCountEvent.update(CacheAssetCount(
            projectId: proj.project.id,
            assetType: assetType,
            progress: 6,
          )));

      // fetch any previously cached media for this project/type
      context.read<CacheMediaUploadBloc>().add(
            CacheMediaUploadEvent.get(proj.project.id, assetType),
          );
    });
  }

  @override
  void dispose() {
    _locSub?.cancel();
    _fileCache.clear();
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

  /// Downloads a remote file once or returns a cached local File.
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

  /// Populate `_selectedImages` & `_selectedVideos` from cached entries.
  Future<void> _populateFromCache(List<CacheMediaUpload> entries) async {
    final images = <PlatformFile>[];
    final videos = <PlatformFile>[];

    for (final e in entries) {
      final file = await _getCachedFile(e.filePath);
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
                  if (_currentProjectId == null) return;
                  context.read<CacheMediaUploadBloc>().add(
                        CacheMediaUploadEvent.deleteAll(
                          _currentProjectId!,
                          assetType.toLowerCase(),
                        ),
                      );

                  // wait for deletion to finish before re-adding:
                  await context.read<CacheMediaUploadBloc>().stream.firstWhere(
                        (state) => state.maybeWhen(
                          deleted: () => true,
                          error: (_) => true,
                          orElse: () => false,
                        ),
                      );
                  // Save new entries
                  for (final file in _selectedImages) {
                    final copied = await copyFileToLocalDir(File(file.path!));
                    final entry = CacheMediaUpload(
                      projectId: _currentProjectId!,
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
                      projectId: _currentProjectId!,
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
                          key: ValueKey('images-$_imageKeyCounter'),
                          label: 'Upload Images',
                          allowMultiples: true,
                          showPreview: true,
                          initialFiles: _selectedImages,
                          onFilesSelected: (files) {
                            setState(() {
                              _selectedImages = files;
                              _imageKeyCounter++;
                            });
                            // DEBUG: Print current state
                            print("Images: ${_selectedImages.length}");
                            print("Videos: ${_selectedVideos.length}");
                            print("Files: ${files.length}");
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
                            // return <PlatformFile, String?>{};
                            return {for (final f in files) f: null};
                          },
                        ),
                      ]),

                      const SizedBox(height: spacer4),

                      // ── Videos Card ──
                      DigitCard(children: [
                        Text(
                          '$assetType Videos',
                          style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const SizedBox(height: spacer2),
                        FileUploadWidget(
                          key: ValueKey('videos-$_videoKeyCounter'),
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
                              _videoKeyCounter++;
                            });
                            // DEBUG: Print current state
                            debugPrint("Images: ${_selectedImages.length}");
                            debugPrint("Videos: ${_selectedVideos.length}");
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
                            //return <PlatformFile, String?>{};
                            // **Return a map of the newly selected files** (no errors):
                            return {for (final f in files) f: null};
                          },
                        ),
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
