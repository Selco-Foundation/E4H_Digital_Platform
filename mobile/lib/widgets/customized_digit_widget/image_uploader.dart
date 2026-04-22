import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/ComponentTheme/pop_up_card_theme.dart';
import 'package:digit_ui_components/utils/utils.dart';
import 'package:digit_ui_components/utils/validators/file_validator.dart';
import 'package:digit_ui_components/utils/validators/image_validator.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/helper_widget/camera_handler.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

import '../../utils/app_logger.dart';

enum _PickerSessionPhase { idle, launching, awaitingResult, recovering }

class ImageUploader extends StatefulWidget {
  final Function(List<File>) onImagesSelected;
  final bool allowMultiples;
  final int? maxImages;
  final bool isDisabled;
  final String? errorMessage;
  final List<File>? initialImages;
  final String? label;
  final String? cameraTitle;
  final String? galleryTitle;
  final String? captureText;
  final String? cancelText;
  final String? chooseOptionLabel;
  final List<FileValidator>? validators;

  final int imageQuality;
  final double? maxWidth;
  final double? maxHeight;

  final bool recoverLostDataOnResume;
  final bool requestFullMetadata;

  const ImageUploader({
    super.key,
    required this.onImagesSelected,
    this.allowMultiples = false,
    this.maxImages,
    this.isDisabled = false,
    this.initialImages,
    this.errorMessage,
    this.label,
    this.cameraTitle,
    this.galleryTitle,
    this.captureText,
    this.cancelText,
    this.chooseOptionLabel,
    this.validators,
    this.imageQuality = 75,
    this.maxWidth,
    this.maxHeight,
    this.recoverLostDataOnResume = true,
    this.requestFullMetadata = false,
  });

  @override
  _ImageUploaderState createState() => _ImageUploaderState();
}

class _ImageUploaderState extends State<ImageUploader>
    with WidgetsBindingObserver {
  static const MethodChannel _imageToolsChannel =
      MethodChannel('org.e4h.asset/image_tools');

  late final List<File> _imageFiles;
  late DigitTypography currentTypography;
  late bool isMobile;
  late bool isTab;
  String? capitalizedErrorMessage;
  String fileError = '';
  String? _lastRecoveredPath;
  String? _lastDeliveredPath;

  final ImagePicker _picker = ImagePicker();
  _PickerSessionPhase _pickerPhase = _PickerSessionPhase.idle;
  ImageSource? _activeSource;
  int _sessionToken = 0;
  int? _activeSessionToken;
  int? _lastCompletedSessionToken;
  bool _recoveryAttemptedSinceResume = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _imageFiles = List<File>.from(widget.initialImages ?? const []);
    unawaited(
      _recoverLostDataIfNeeded(
        trigger: 'init',
        allowWithoutActiveSession: true,
      ),
    );
  }

  @override
  void didUpdateWidget(covariant ImageUploader oldWidget) {
    super.didUpdateWidget(oldWidget);

    final incoming = widget.initialImages ?? const <File>[];
    final previous = oldWidget.initialImages ?? const <File>[];

    if (!listEquals(incoming, previous)) {
      setState(() {
        _imageFiles
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
      AppLogger.instance.info(
        'ImageUploader lifecycle resumed phase=${_pickerPhase.name} session=${_activeSessionToken ?? -1}',
      );
      if (!widget.recoverLostDataOnResume) return;
      if (!_hasActivePickerSession || _recoveryAttemptedSinceResume) return;
      _recoveryAttemptedSinceResume = true;
      Future<void>.delayed(const Duration(milliseconds: 150), () {
        if (!mounted) return;
        unawaited(
          _recoverLostDataIfNeeded(
            trigger: 'resume',
            allowWithoutActiveSession: false,
          ),
        );
      });
    } else if (state == AppLifecycleState.inactive ||
        state == AppLifecycleState.paused) {
      _recoveryAttemptedSinceResume = false;
    }
  }

  bool get _hasActivePickerSession =>
      _pickerPhase == _PickerSessionPhase.launching ||
      _pickerPhase == _PickerSessionPhase.awaitingResult ||
      _pickerPhase == _PickerSessionPhase.recovering;

  bool get _hasReachedImageLimit {
    final limit = widget.maxImages;
    return limit != null && _imageFiles.length >= limit;
  }

  bool get _shouldShowUploadControl {
    if (widget.isDisabled) return false;
    if (!widget.allowMultiples && _imageFiles.isNotEmpty) return false;
    if (_hasReachedImageLimit) return false;
    return true;
  }

  Future<void> _startImagePick(ImageSource source) async {
    if (!mounted) return;
    if (_hasActivePickerSession) {
      setState(() {
        fileError = 'Camera is still opening, please wait';
      });
      AppLogger.instance.info(
        'ImageUploader ignored duplicate launch phase=${_pickerPhase.name} session=${_activeSessionToken ?? -1}',
      );
      return;
    }
    final sessionToken = ++_sessionToken;
    _activeSessionToken = sessionToken;
    _activeSource = source;
    _recoveryAttemptedSinceResume = false;
    setState(() {
      fileError = '';
      _pickerPhase = _PickerSessionPhase.launching;
    });
    AppLogger.instance.info(
      'ImageUploader start source=${source.name} session=$sessionToken',
    );
    await Future<void>.delayed(const Duration(milliseconds: 300));
    if (!mounted || _activeSessionToken != sessionToken) return;
    await _getImage(source, sessionToken);
  }

  Future<File> _normalizeImageFile(File source) async {
    if (!Platform.isAndroid) return source;
    try {
      final String? outPath = await _imageToolsChannel.invokeMethod<String>(
        'compressImage',
        <String, dynamic>{
          'path': source.path,
          'maxWidth': widget.maxWidth?.round() ?? 1280,
          'maxHeight': widget.maxHeight?.round() ?? 1280,
          'quality': widget.imageQuality,
        },
      );
      if (outPath != null && outPath.isNotEmpty) {
        final outFile = File(outPath);
        if (await outFile.exists()) return outFile;
      }
    } on PlatformException catch (e) {
      AppLogger.instance.info(
        'compressImage PlatformException: ${e.code} ${e.message}',
      );
    } catch (e) {
      AppLogger.instance.info('compressImage failed: $e');
    }
    return source;
  }

  Future<void> _getImage(ImageSource source, int sessionToken) async {
    if (!mounted || _activeSessionToken != sessionToken) return;
    try {
      if (kIsWeb && source == ImageSource.camera) {
        if (!mounted) return;
        showDialog(
          context: context,
          barrierColor: const DigitColors().overLayColor.withOpacity(.70),
          useSafeArea: false,
          builder: (BuildContext context) {
            CameraHandlerState? cameraHandlerState;
            return BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 2.0, sigmaY: 2.0),
              child: Popup(
                popupTheme: const DigitPopupTheme().copyWith(
                  context: context,
                  width: isMobile
                      ? 360
                      : isTab
                          ? 440
                          : 720,
                ),
                onCrossTap: () {
                  _completeSession(sessionToken);
                  Navigator.of(context).pop();
                },
                title: widget.cameraTitle ?? 'Camera',
                type: PopUpType.simple,
                actions: [
                  DigitButton(
                    mainAxisSize: MainAxisSize.max,
                    prefixIcon: Icons.camera_enhance,
                    label: widget.captureText ?? 'Capture',
                    size: DigitButtonSize.large,
                    type: DigitButtonType.primary,
                    onPressed: () {
                      Navigator.of(context).pop();
                      cameraHandlerState?.captureImage();
                    },
                  ),
                  DigitButton(
                    mainAxisSize: MainAxisSize.max,
                    label: widget.cancelText ?? 'Cancel',
                    size: DigitButtonSize.large,
                    type: DigitButtonType.secondary,
                    onPressed: () {
                      _completeSession(sessionToken);
                      Navigator.of(context).pop();
                    },
                  ),
                ],
                additionalWidgets: [
                  CameraHandler(
                    source: source,
                    onImageCaptured: (image) {
                      _handleImageCapture(image);
                    },
                    onCameraHandlerCreated: (CameraHandlerState state) {
                      cameraHandlerState = state;
                    },
                  ),
                ],
              ),
            );
          },
        );
        return;
      }

      // Mobile: give camera stack a brief moment to settle (helps on some Samsung devices).
      await Future.delayed(const Duration(milliseconds: 180));
      if (!mounted || _activeSessionToken != sessionToken) return;

      setState(() {
        _pickerPhase = _PickerSessionPhase.awaitingResult;
      });

      final pickedFile = await _picker.pickImage(
        source: source,
        imageQuality: widget.imageQuality,
        maxWidth: widget.maxWidth,
        maxHeight: widget.maxHeight,
        requestFullMetadata: widget.requestFullMetadata,
      );

      if (!mounted || _activeSessionToken != sessionToken) return;

      if (pickedFile == null) {
        if (kDebugMode) {
          AppLogger.instance.info('No image selected.');
        }
        _completeSession(sessionToken);
        return;
      }

      await _processPickedFile(
        pickedFile,
        sessionToken: sessionToken,
        fromRecovery: false,
      );
    } on PlatformException catch (e) {
      AppLogger.instance
          .info('ImagePicker PlatformException: ${e.code} ${e.message}');
      final recoverableCodes = <String>{
        'already_active',
        'channel-error',
      };
      if (mounted) {
        setState(() {
          fileError = recoverableCodes.contains(e.code)
              ? 'Camera is busy, please try again'
              : (e.message ?? 'Failed to open camera/gallery');
        });
      }
      _completeSession(sessionToken);
    } catch (e) {
      AppLogger.instance.info('Error picking image: $e');
      if (mounted) {
        setState(() {
          fileError = 'Failed to pick image';
        });
      }
      _completeSession(sessionToken);
    }
  }

  Future<void> _handleImageCapture(File image) async {
    if (!mounted) return;
    final sessionToken = _activeSessionToken;
    setState(() {
      fileError = '';
      if (widget.allowMultiples) {
        _imageFiles.add(image);
      } else {
        _imageFiles
          ..clear()
          ..add(image);
      }
    });
    widget.onImagesSelected(List<File>.from(_imageFiles));
    if (sessionToken != null) {
      _completeSession(sessionToken);
    }
  }

  Future<void> _processPickedFile(
    XFile pickedFile, {
    required int sessionToken,
    required bool fromRecovery,
  }) async {
    if (!mounted || _activeSessionToken != sessionToken) return;

    final normalizedFile = await _normalizeImageFile(File(pickedFile.path));
    if (!mounted || _activeSessionToken != sessionToken) return;

    final normalizedXFile = XFile(
      normalizedFile.path,
      name: pickedFile.name,
      mimeType: pickedFile.mimeType,
    );

    if (widget.validators != null) {
      final String? validationError =
          validateImage(normalizedXFile, widget.validators!, pickedFile.name);
      if (validationError != null) {
        setState(() {
          fileError = validationError;
        });
        _completeSession(sessionToken);
        return;
      }
    }

    if (fromRecovery && _lastRecoveredPath == normalizedFile.path) {
      AppLogger.instance.info(
        'ImageUploader suppress duplicate recovery path=${normalizedFile.path} session=$sessionToken',
      );
      _completeSession(sessionToken);
      return;
    }

    if (_lastDeliveredPath == normalizedFile.path &&
        _lastCompletedSessionToken == sessionToken) {
      AppLogger.instance.info(
        'ImageUploader suppress duplicate dispatch path=${normalizedFile.path} session=$sessionToken',
      );
      _completeSession(sessionToken);
      return;
    }

    setState(() {
      fileError = '';
      if (widget.allowMultiples) {
        _imageFiles.add(normalizedFile);
      } else {
        _imageFiles
          ..clear()
          ..add(normalizedFile);
      }
    });
    _lastRecoveredPath = fromRecovery ? normalizedFile.path : _lastRecoveredPath;
    _lastDeliveredPath = normalizedFile.path;
    AppLogger.instance.info(
      'ImageUploader dispatch source=${_activeSource?.name ?? 'unknown'} recovery=$fromRecovery session=$sessionToken',
    );
    widget.onImagesSelected(List<File>.from(_imageFiles));
    _completeSession(sessionToken);
  }

  Future<void> _recoverLostDataIfNeeded({
    required String trigger,
    required bool allowWithoutActiveSession,
  }) async {
    if (kIsWeb) return;
    if (!mounted) return;
    if (!allowWithoutActiveSession && !_hasActivePickerSession) return;

    final sessionToken = _activeSessionToken ?? ++_sessionToken;
    _activeSessionToken ??= sessionToken;

    if (!mounted) return;
    setState(() {
      _pickerPhase = _PickerSessionPhase.recovering;
    });
    AppLogger.instance.info(
      'ImageUploader recover trigger=$trigger session=$sessionToken active=${_activeSource?.name ?? 'none'}',
    );

    try {
      final response = await _picker.retrieveLostData();
      if (!mounted || _activeSessionToken != sessionToken) return;
      if (response.isEmpty) {
        if (allowWithoutActiveSession) {
          _completeSession(sessionToken);
        } else {
          setState(() {
            _pickerPhase = _PickerSessionPhase.awaitingResult;
          });
        }
        return;
      }

      final x = response.file;
      if (x == null) {
        if (response.exception != null) {
          AppLogger.instance
              .info('retrieveLostData exception: ${response.exception}');
        }
        if (mounted) {
          setState(() {
            fileError = 'Camera result was lost, please try again';
          });
        }
        _completeSession(sessionToken);
        return;
      }

      await _processPickedFile(
        x,
        sessionToken: sessionToken,
        fromRecovery: true,
      );
    } catch (e) {
      AppLogger.instance.info('Error recovering lost picker data: $e');
      if (mounted) {
        setState(() {
          fileError = 'Failed to restore camera result';
        });
      }
      _completeSession(sessionToken);
    }
  }

  void _completeSession(int sessionToken) {
    if (_activeSessionToken != sessionToken) return;
    _lastCompletedSessionToken = sessionToken;
    _activeSessionToken = null;
    _activeSource = null;
    if (mounted) {
      setState(() {
        _pickerPhase = _PickerSessionPhase.idle;
      });
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
        if (widget.isDisabled) {
          setState(() {
            fileError = '';
          });
          return;
        }
        if (_hasActivePickerSession) {
          setState(() {
            fileError = 'Camera is still opening, please wait';
          });
          return;
        }
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
                            _buildInkWell(Icons.camera_enhance,
                                widget.cameraTitle ?? "Camera", () {
                              Navigator.of(context).pop();
                              _startImagePick(ImageSource.camera);
                            }, currentTypography),
                            _buildInkWell(Icons.perm_media,
                                widget.galleryTitle ?? "My Files", () {
                              Navigator.of(context).pop();
                              _startImagePick(ImageSource.gallery);
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
                      topRight: Radius.circular(spacer2)),
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
      child: _buildImageDisplay(),
    );
  }

  Widget _buildBottomSheetContent() {
    return Center(
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: <Widget>[
          _buildInkWell(Icons.camera_enhance, widget.cameraTitle ?? "Camera",
              () {
            Navigator.of(context).pop();
            _startImagePick(ImageSource.camera);
          }, currentTypography),
          _buildInkWell(Icons.perm_media, widget.galleryTitle ?? "My Files",
              () {
            Navigator.of(context).pop();
            _startImagePick(ImageSource.gallery);
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

  Widget _buildImageDisplay() {
    if (fileError != '') {
      //_closeCamera();
    }
    return Column(
      mainAxisAlignment: MainAxisAlignment.start,
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (_shouldShowUploadControl)
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
                  Icon(Icons.camera_enhance,
                      size: spacer10,
                      color: const DigitColors().light.primary1),
                  Text(
                    widget.label ?? 'Click to add photo',
                    style: TextStyle(color: const DigitColors().light.primary1),
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
        if (_hasActivePickerSession)
          Padding(
            padding: const EdgeInsets.only(top: spacer1),
            child: Text(
              'Opening camera...',
              style: currentTypography.bodyS.copyWith(
                color: const DigitColors().light.primary1,
              ),
            ),
          ),
        if (_shouldShowUploadControl)
          const SizedBox(height: spacer2),
        Wrap(
          spacing: spacer2,
          runSpacing: spacer2,
          children: List.generate(_imageFiles.length, (index) {
            return _buildImageItem(index);
          }),
        ),
      ],
    );
  }

  Widget _buildImageItem(int index) {
    if ((_imageFiles.isNotEmpty && _imageFiles[index].path.isNotEmpty) ||
        fileError != '') {
      // _closeCamera();
    }

    final int thumb = Base.imageSize.toInt(); // decode smaller to reduce memory
    return _imageFiles.isNotEmpty
        ? Stack(
            children: [
              widget.allowMultiples
                  ? Container(
                      color: const DigitColors().light.genericDivider,
                      width: Base.imageSize,
                      height: Base.imageSize,
                      constraints:
                          const BoxConstraints(minWidth: Base.imageSize),
                      child: ClipRRect(
                        borderRadius: Base.radius,
                        child: Stack(
                          fit: StackFit.expand,
                          children: [
                            kIsWeb
                                ? Image.network(
                                    _imageFiles[index].path,
                                    width: Base.imageSize,
                                    height: Base.imageSize,
                                    fit: BoxFit.cover,
                                  )
                                : Image.file(
                                    _imageFiles[index],
                                    width: Base.imageSize,
                                    height: Base.imageSize,
                                    fit: BoxFit.cover,
                                    cacheWidth: thumb,
                                    cacheHeight: thumb,
                                  ),
                            if (!widget.isDisabled)
                              Positioned(
                                top: 0,
                                right: 0,
                                child: InkWell(
                                  hoverColor: const DigitColors().transparent,
                                  highlightColor:
                                      const DigitColors().transparent,
                                  splashColor:
                                      const DigitColors().transparent,
                                  onTap: () {
                                    _removeImage(index);
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
                                      color:
                                          const DigitColors().light.paperPrimary,
                                    ),
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                    )
                  : AspectRatio(
                      aspectRatio: 3 / 2,
                      child: ClipRRect(
                        borderRadius: Base.radius,
                        child: Stack(
                          fit: StackFit.expand,
                          children: [
                            kIsWeb
                                ? Image.network(
                                    _imageFiles[index].path,
                                    fit: BoxFit.fitHeight,
                                  )
                                : Image.file(
                                    _imageFiles[index],
                                    fit: BoxFit.cover,
                                    cacheWidth: (thumb *
                                        2), // slightly larger for wide preview
                                  ),
                            if (!widget.isDisabled)
                              Positioned(
                                top: 0,
                                right: 0,
                                child: InkWell(
                                  hoverColor: const DigitColors().transparent,
                                  highlightColor:
                                      const DigitColors().transparent,
                                  splashColor:
                                      const DigitColors().transparent,
                                  onTap: () {
                                    _removeImage(index);
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
                                      color:
                                          const DigitColors().light.paperPrimary,
                                    ),
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                    ),
            ],
          )
        : const SizedBox.shrink();
  }

  void _removeImage(int index) {
    if (widget.isDisabled) return;
    setState(() {
      _imageFiles.removeAt(index);
    });
    widget.onImagesSelected(List<File>.from(_imageFiles));
  }
}
