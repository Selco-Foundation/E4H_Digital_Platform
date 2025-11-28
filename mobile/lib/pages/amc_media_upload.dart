import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path/path.dart' show basename;

import '../blocs/cache_amc_media_upload/cache_amc_media_upload.dart';
import '../blocs/scheduled_visit_submission/scheduled_visit_submission.dart';
import '../blocs/selected_amc_origin/selected_amc_origin.dart';
import '../blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../data/nosql/cache_amc_media_upload.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/customized_digit_widget/file_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcMediaUploadPage extends StatefulWidget {
  const AmcMediaUploadPage({super.key});

  @override
  State<AmcMediaUploadPage> createState() => _AmcMediaUploadPageState();
}

class _AmcMediaUploadPageState extends State<AmcMediaUploadPage> {
  String? _currentScheduledVisitId;
  ScheduledVisit? scheduledVisit;
  FormOrigin? origin;
  String userType = USER_TYPES.AMC.name;
  List<PlatformFile> _selectedImages = [];
  bool _isImagesInitLoading = false;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;

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

    context.read<SelectedAmcOriginBloc>().state.whenOrNull(
        selected: (originAmc) {
      origin = originAmc;
    });

    context.read<SelectedScheduledVisitBloc>().state.whenOrNull(
        selected: (visit) {
      _currentScheduledVisitId = visit.id;
      scheduledVisit = visit;

      context.read<CacheAmcMediaUploadBloc>().add(
          CacheAmcMediaUploadEvent.get(_currentScheduledVisitId!, userType));
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

  Future<void> _populateFromCacheOrSubmittedDocuments({
    List<CacheAmcMediaUpload> cacheEntries = const [],
  }) async {
    setState(() {
      _isImagesInitLoading = true;
    });

    final List<PlatformFile> images = [];

    // --------------------------------------------------
    // 1) Try cache first if we were given entries
    // --------------------------------------------------
    if (cacheEntries.isNotEmpty) {
      final futures = cacheEntries.map((e) async {
        final file = await getCachedFile(e.filePath);
        if (file == null) return null;

        final size = await file.length();

        return PlatformFile(
          name: basename(file.path),
          path: file.path,
          size: size,
        );
      }).toList();

      final results = await Future.wait(futures);

      for (final pf in results) {
        if (pf == null) continue;
        images.add(pf);
      }
    } else {
      // ------------------------------------------------
      // 2) No cache → use ScheduledVisit.visitReport.documents
      // ------------------------------------------------

      final visitReport = scheduledVisit?.visitReport;
      final docs = visitReport?.documents ?? const [];

      if (docs.isEmpty) {
        if (mounted) {
          setState(() {
            _selectedImages = [];
            _isImagesInitLoading = false;
          });
        }
        return;
      }

      final futures = docs.map((e) async {
        final file = await getCachedFile(e.fileStore ?? '');
        if (file == null) return null;

        final size = await file.length();

        return PlatformFile(
          name: basename(file.path),
          path: file.path,
          size: size,
        );
      }).toList();

      final results = await Future.wait(futures);

      for (final pf in results) {
        if (pf == null) continue;
        images.add(pf);
      }
    }

    if (!mounted) return;

    setState(() {
      _selectedImages = images;
      _isImagesInitLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return MultiBlocListener(
      listeners: [
        BlocListener<CacheAmcMediaUploadBloc, CacheAmcMediaUploadState>(
          listener: (context, state) {
            state.maybeWhen(
              loaded: (entries) {
                _populateFromCacheOrSubmittedDocuments(cacheEntries: entries);
              },
              notFound: () => _populateFromCacheOrSubmittedDocuments(),
              orElse: () {},
            );
          },
        ),
        BlocListener<ScheduleVisitSubmitBloc, ScheduleVisitSubmitState>(
          listener: (context, state) {
            state.maybeWhen(
              success: () => context.router.push(const AmcOtpRoute()),
              failure: (error) {
                context.showSnackBar(SnackBar(content: Text("$error")));
              },
              orElse: () {},
            );
          },
        ),
      ],
      child: BlocBuilder<ScheduleVisitSubmitBloc, ScheduleVisitSubmitState>(
        builder: (context, scheduleState) {
          return Scaffold(
            body: ScrollableContent(
              enableFixedDigitButton: true,
              backgroundColor: theme.colorTheme.generic.background,
              header: const BackNavigationHelpHeaderWidget(
                showBackNavigation: true,
                showHelp: false,
              ),
              footer: FooterButton(
                isDisabled: scheduleState.maybeWhen(
                  loading: () => true,
                  orElse: () => false,
                ),
                showSuffixIcon: false,
                text: scheduleState.maybeWhen(
                  loading: () => "Loading...",
                  orElse: () => context.translate(i18.common.coreCommonSubmit),
                ),
                onPress: () async {
                  switch (origin) {
                    case FormOrigin.overallSummary:
                    case FormOrigin.submitForApproval:
                      if (_currentScheduledVisitId == null) return;
                      context.read<CacheAmcMediaUploadBloc>().add(
                          CacheAmcMediaUploadEvent.deleteAll(
                              _currentScheduledVisitId!, userType));
                      await context
                          .read<CacheAmcMediaUploadBloc>()
                          .stream
                          .firstWhere((state) => state.maybeWhen(
                                deleted: () => true,
                                error: (_) => true,
                                orElse: () => false,
                              ));
                      for (final file in _selectedImages) {
                        final copied =
                            await copyFileToLocalDir(File(file.path!));
                        final entry = CacheAmcMediaUpload(
                          scheduledVisitId: _currentScheduledVisitId!,
                          itemNumber: file.name,
                          itemType: 'image',
                          userType: userType,
                          filePath: copied,
                          longitude: _longitude.toString(),
                          latitude: _latitude.toString(),
                        );
                        context
                            .read<CacheAmcMediaUploadBloc>()
                            .add(CacheAmcMediaUploadEvent.add(entry));
                      }

                      context.read<ScheduleVisitSubmitBloc>().add(
                          ScheduleVisitSubmitEvent.submit(
                              scheduledVisitId: _currentScheduledVisitId!,
                              userType: userType));
                      break;
                    default:
                      context.router.push(const AmcHomeRoute());
                  }
                },
              ),
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(
                      vertical: spacer2, horizontal: spacer4),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: spacer4),
                      DigitCard(children: [
                        Text(
                          'Images',
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
                          label: 'Upload Images',
                          allowMultiples: true,
                          showPreview: true,
                          initialFiles: _selectedImages,
                          onFilesSelected: (files) {
                            setState(() {
                              _selectedImages = files;
                            });
                            _ensureLocationLoaded().then((ok) {
                              if (!ok) {
                                context.showSnackBar(const SnackBar(
                                    content: Text('Could not fetch location')));
                              }
                            });
                            return <PlatformFile, String?>{};
                          },
                        ),
                        if (_isImagesInitLoading)
                          const Center(child: CircularProgressIndicator())
                      ]),
                      const SizedBox(height: spacer4),
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
