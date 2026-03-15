import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/pop_up_card_theme.dart';
import 'package:digit_ui_components/utils/utils.dart';
import 'package:digit_ui_components/utils/validators/file_validator.dart'
    as file_validator;
import 'package:digit_ui_components/utils/validators/file_validator.dart'
    show validateFile;
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:file_picker/file_picker.dart' show PlatformFile;
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path/path.dart' show basename;

import '../../utils/app_logger.dart';

class VideoUploader extends StatefulWidget {
  final Function(List<File>) onVideosSelected;
  final bool allowMultiples;
  final List<File>? initialVideos;
  final String? errorMessage;
  final String? label;
  final String? cameraTitle;
  final String? galleryTitle;
  final String? captureText;
  final String? cancelText;
  final String? chooseOptionLabel;
  final List<file_validator.FileValidator>? validators;

  const VideoUploader({
    super.key,
    required this.onVideosSelected,
    this.allowMultiples = false,
    this.initialVideos,
    this.errorMessage,
    this.label,
    this.cameraTitle,
    this.galleryTitle,
    this.captureText,
    this.cancelText,
    this.chooseOptionLabel,
    this.validators,
  });

  @override
  State<VideoUploader> createState() => _VideoUploaderState();
}

class _VideoUploaderState extends State<VideoUploader>
    with WidgetsBindingObserver {
  final ImagePicker _picker = ImagePicker();
  late final List<File> _videoFiles;
  late DigitTypography currentTypography;
  late bool isMobile;
  late bool isTab;
  String? capitalizedErrorMessage;
  String fileError = '';
  bool _picking = false;
  String? _lastRecoveredPath;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _videoFiles = List<File>.from(widget.initialVideos ?? const []);
    _recoverLostData();
  }

  @override
  void didUpdateWidget(covariant VideoUploader oldWidget) {
    super.didUpdateWidget(oldWidget);

    final incoming = widget.initialVideos ?? const <File>[];
    final previous = oldWidget.initialVideos ?? const <File>[];

    if (!listEquals(incoming, previous)) {
      setState(() {
        _videoFiles
          ..clear()
          ..addAll(incoming);
      });
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      Future<void>.delayed(const Duration(milliseconds: 150), () {
        if (!mounted) return;
        _recoverLostData();
      });
    }
  }

  Future<void> _startVideoPick(ImageSource source) async {
    if (!mounted || _picking) return;
    await Future<void>.delayed(const Duration(milliseconds: 300));
    if (!mounted) return;
    await _getVideo(source);
  }

  Future<void> _getVideo(ImageSource source) async {
    if (_picking || !mounted) return;
    _picking = true;

    try {
      await Future.delayed(const Duration(milliseconds: 180));

      final pickedFile = await _picker.pickVideo(source: source);
      if (!mounted || pickedFile == null) return;

      final file = File(pickedFile.path);

      if (widget.validators != null) {
        final size = await file.length();
        final validationError = validateFile(
          PlatformFile(
            name: basename(file.path),
            path: file.path,
            size: size,
          ),
          widget.validators!,
        );
        if (validationError != null) {
          setState(() {
            fileError = validationError;
          });
          return;
        }
      }

      setState(() {
        fileError = '';
        if (widget.allowMultiples) {
          _videoFiles.add(file);
        } else {
          _videoFiles
            ..clear()
            ..add(file);
        }
      });
      widget.onVideosSelected(List<File>.from(_videoFiles));
    } on PlatformException catch (e) {
      AppLogger.instance
          .info('VideoPicker PlatformException: ${e.code} ${e.message}');
      if (mounted) {
        setState(() {
          fileError = e.message ?? 'Failed to open camera/gallery';
        });
      }
    } catch (e) {
      AppLogger.instance.info('Error picking video: $e');
      if (mounted) {
        setState(() {
          fileError = 'Failed to pick video';
        });
      }
    } finally {
      _picking = false;
    }
  }

  Future<void> _recoverLostData() async {
    if (kIsWeb || _picking) return;

    try {
      final response = await _picker.retrieveLostData();
      if (response.isEmpty) return;

      final x = response.file;
      if (x == null || !mounted || _lastRecoveredPath == x.path) return;

      final file = File(x.path);

      if (widget.validators != null) {
        final size = await file.length();
        final validationError = validateFile(
          PlatformFile(
            name: basename(file.path),
            path: file.path,
            size: size,
          ),
          widget.validators!,
        );
        if (validationError != null) {
          setState(() {
            fileError = validationError;
          });
          return;
        }
      }

      setState(() {
        fileError = '';
        if (widget.allowMultiples) {
          _videoFiles.add(file);
        } else {
          _videoFiles
            ..clear()
            ..add(file);
        }
      });
      _lastRecoveredPath = x.path;
      widget.onVideosSelected(List<File>.from(_videoFiles));
    } catch (e) {
      AppLogger.instance.info('Error recovering lost video data: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    currentTypography = getTypography(context, false);
    isMobile = AppView.isMobileView(MediaQuery.of(context).size);
    isTab = AppView.isTabletView(MediaQuery.of(context).size);
    capitalizedErrorMessage = convertInToSentenceCase(widget.errorMessage);

    return InkWell(
      hoverColor: const DigitColors().transparent,
      highlightColor: const DigitColors().transparent,
      splashColor: const DigitColors().transparent,
      onTap: () {
        setState(() {
          fileError = '';
        });
        !isMobile
            ? showDialog(
                context: context,
                builder: (BuildContext context) {
                  return BackdropFilter(
                    filter: ImageFilter.blur(sigmaX: 2.0, sigmaY: 2.0),
                    child: Popup(
                      popupTheme: const DigitPopupTheme().copyWith(
                        context: context,
                        width: isTab ? 440 : 600,
                        height: isTab ? 228 : 240,
                      ),
                      title: widget.chooseOptionLabel ??
                          'Choose an option to upload',
                      onCrossTap: () {
                        Navigator.of(context).pop();
                      },
                      additionalWidgets: [
                        Row(
                          mainAxisSize: MainAxisSize.min,
                          mainAxisAlignment: MainAxisAlignment.center,
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: <Widget>[
                            _buildInkWell(
                                Icons.videocam, widget.cameraTitle ?? "Camera",
                                () {
                              Navigator.of(context).pop();
                              _startVideoPick(ImageSource.camera);
                            }, currentTypography),
                            _buildInkWell(Icons.video_library,
                                widget.galleryTitle ?? "My Files", () {
                              Navigator.of(context).pop();
                              _startVideoPick(ImageSource.gallery);
                            }, currentTypography),
                          ],
                        ),
                      ],
                    ),
                  );
                },
              )
            : showModalBottomSheet(
                context: context,
                isScrollControlled: true,
                shape: const RoundedRectangleBorder(
                  borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(spacer2),
                    topRight: Radius.circular(spacer2),
                  ),
                ),
                constraints: BoxConstraints(
                  minWidth: MediaQuery.of(context).size.width,
                  minHeight: 120,
                  maxHeight: 120,
                ),
                backgroundColor: const DigitColors().light.paperPrimary,
                builder: (BuildContext context) {
                  return _buildBottomSheetContent();
                },
              );
      },
      child: _buildVideoDisplay(),
    );
  }

  Widget _buildBottomSheetContent() {
    return Center(
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: <Widget>[
          _buildInkWell(Icons.videocam, widget.cameraTitle ?? "Camera", () {
            Navigator.of(context).pop();
            _startVideoPick(ImageSource.camera);
          }, currentTypography),
          _buildInkWell(Icons.video_library, widget.galleryTitle ?? "My Files",
              () {
            Navigator.of(context).pop();
            _startVideoPick(ImageSource.gallery);
          }, currentTypography),
        ],
      ),
    );
  }

  Widget _buildInkWell(
    IconData icon,
    String label,
    VoidCallback onTap,
    DigitTypography typography,
  ) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.only(top: spacer8),
        child: InkWell(
          hoverColor: const DigitColors().transparent,
          highlightColor: const DigitColors().transparent,
          splashColor: const DigitColors().transparent,
          onTap: onTap,
          child: Column(
            children: [
              Icon(icon,
                  size: spacer10, color: const DigitColors().light.primary1),
              const SizedBox(height: spacer2),
              Text(
                label,
                style: typography.bodyL
                    .copyWith(color: const DigitColors().light.primary1),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildVideoDisplay() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.start,
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (!(widget.allowMultiples == false && _videoFiles.isNotEmpty))
          Container(
            width: MediaQuery.of(context).size.width,
            height: 120,
            decoration: BoxDecoration(
              border: Border.all(
                color: widget.errorMessage != null || fileError != ''
                    ? const DigitColors().light.alertError
                    : const DigitColors().light.genericInputBorder,
                width: 1,
              ),
            ),
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.videocam,
                      size: spacer10,
                      color: const DigitColors().light.primary1),
                  Text(
                    widget.label ?? 'Click to add video',
                    style: const TextStyle(
                      color: Color(0xFFCC4C02),
                    ),
                  ),
                ],
              ),
            ),
          ),
        if (widget.errorMessage != null || fileError != '')
          const SizedBox(height: spacer1),
        if (widget.errorMessage != null || fileError != '')
          Row(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Column(
                children: [
                  const SizedBox(height: spacer1 / 2),
                  Icon(
                    Icons.info,
                    color: const DigitColors().light.alertError,
                    size: BaseConstants.errorIconSize,
                  ),
                ],
              ),
              const SizedBox(width: spacer1),
              Flexible(
                fit: FlexFit.tight,
                child: fileError != ''
                    ? Text(
                        fileError.length > 256
                            ? '${fileError.substring(0, 256)}...'
                            : fileError,
                        style: currentTypography.bodyS.copyWith(
                          color: const DigitColors().light.alertError,
                        ),
                      )
                    : Text(
                        capitalizedErrorMessage!.length > 256
                            ? '${capitalizedErrorMessage?.substring(0, 256)}...'
                            : capitalizedErrorMessage!,
                        style: currentTypography.bodyS.copyWith(
                          color: const DigitColors().light.alertError,
                        ),
                      ),
              ),
            ],
          ),
        if (!(widget.allowMultiples == false && _videoFiles.isNotEmpty))
          const SizedBox(height: spacer2),
        Wrap(
          spacing: spacer2,
          runSpacing: spacer2,
          children: List.generate(_videoFiles.length, (index) {
            return _buildVideoItem(index);
          }),
        ),
      ],
    );
  }

  Widget _buildVideoItem(int index) {
    final file = _videoFiles[index];

    return Stack(
      children: [
        Container(
          width: widget.allowMultiples
              ? Base.imageSize
              : MediaQuery.of(context).size.width,
          constraints: const BoxConstraints(minHeight: Base.imageSize),
          padding: const EdgeInsets.all(spacer3),
          decoration: BoxDecoration(
            borderRadius: Base.radius,
            border: Border.all(
              color: const DigitColors().light.genericDivider,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.video_file,
                color: const DigitColors().light.primary1,
                size: spacer8,
              ),
              const SizedBox(height: spacer2),
              Text(
                basename(file.path),
                style: currentTypography.bodyS.copyWith(
                  color: const DigitColors().light.textPrimary,
                ),
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
        Positioned(
          top: 0,
          right: 0,
          child: InkWell(
            hoverColor: const DigitColors().transparent,
            highlightColor: const DigitColors().transparent,
            splashColor: const DigitColors().transparent,
            onTap: () {
              _removeVideo(index);
            },
            child: Container(
              width: spacer6,
              height: spacer6,
              decoration: BoxDecoration(
                color: const DigitColors().light.primary2,
              ),
              child: Icon(
                Icons.close,
                size: spacer4,
                color: const DigitColors().light.paperPrimary,
              ),
            ),
          ),
        ),
      ],
    );
  }

  void _removeVideo(int index) {
    setState(() {
      _videoFiles.removeAt(index);
    });
    widget.onVideosSelected(List<File>.from(_videoFiles));
  }
}
