import 'dart:async';
import 'dart:io';
import 'dart:math';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

@visibleForTesting
Size calculateCameraPreviewCoverSize({
  required Size viewportSize,
  required Size sensorPreviewSize,
}) {
  if (viewportSize.isEmpty || sensorPreviewSize.isEmpty) return viewportSize;

  final isPortrait = viewportSize.height >= viewportSize.width;
  final orientedPreviewSize = isPortrait
      ? Size(sensorPreviewSize.height, sensorPreviewSize.width)
      : sensorPreviewSize;
  final scale = max(
    viewportSize.width / orientedPreviewSize.width,
    viewportSize.height / orientedPreviewSize.height,
  );
  return Size(
    orientedPreviewSize.width * scale,
    orientedPreviewSize.height * scale,
  );
}

@visibleForTesting
class CameraPreviewCover extends StatelessWidget {
  const CameraPreviewCover({
    super.key,
    required this.previewSize,
    required this.child,
  });

  final Size previewSize;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final viewportSize = constraints.biggest;
        final fittedSize = calculateCameraPreviewCoverSize(
          viewportSize: viewportSize,
          sensorPreviewSize: previewSize,
        );
        return ClipRect(
          key: const Key('camera-preview-viewport'),
          child: OverflowBox(
            alignment: Alignment.center,
            minWidth: fittedSize.width,
            maxWidth: fittedSize.width,
            minHeight: fittedSize.height,
            maxHeight: fittedSize.height,
            child: SizedBox(
              key: const Key('camera-preview-content'),
              width: fittedSize.width,
              height: fittedSize.height,
              child: child,
            ),
          ),
        );
      },
    );
  }
}

@visibleForTesting
class CapturedPhotoReview extends StatelessWidget {
  const CapturedPhotoReview({
    super.key,
    required this.photo,
    required this.errorBuilder,
  });

  final File photo;
  final ImageErrorWidgetBuilder errorBuilder;
  static const imageFit = BoxFit.contain;

  @override
  Widget build(BuildContext context) {
    return Image.file(
      photo,
      fit: imageFit,
      errorBuilder: errorBuilder,
    );
  }
}

@visibleForTesting
class CameraControlsSafeArea extends StatelessWidget {
  const CameraControlsSafeArea({
    super.key,
    required this.child,
  });

  final Widget child;

  @override
  Widget build(BuildContext context) => SafeArea(child: child);
}

class AndroidCameraCapturePage extends StatefulWidget {
  const AndroidCameraCapturePage({super.key});

  @override
  State<AndroidCameraCapturePage> createState() =>
      _AndroidCameraCapturePageState();
}

class _AndroidCameraCapturePageState extends State<AndroidCameraCapturePage>
    with WidgetsBindingObserver {
  CameraController? _controller;
  XFile? _capturedPhoto;
  String? _errorMessage;
  bool _initializing = true;
  bool _capturing = false;
  bool _cancelling = false;
  bool _returningPhoto = false;
  int _initializationGeneration = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    unawaited(SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge));
    unawaited(_initializeCamera());
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.inactive ||
        state == AppLifecycleState.paused ||
        state == AppLifecycleState.detached ||
        state == AppLifecycleState.hidden) {
      unawaited(_disposeController());
    } else if (state == AppLifecycleState.resumed && _capturedPhoto == null) {
      unawaited(_initializeCamera());
    }
  }

  Future<void> _initializeCamera() async {
    final generation = ++_initializationGeneration;
    if (mounted) {
      setState(() {
        _initializing = true;
        _errorMessage = null;
      });
    }

    await _disposeController(invalidateInitialization: false);

    try {
      final cameras = await availableCameras();
      if (!mounted || generation != _initializationGeneration) return;

      CameraDescription? rearCamera;
      for (final camera in cameras) {
        if (camera.lensDirection == CameraLensDirection.back) {
          rearCamera = camera;
          break;
        }
      }
      if (rearCamera == null) {
        throw CameraException(
          'NoRearCamera',
          'No rear camera is available on this device.',
        );
      }

      final controller = CameraController(
        rearCamera,
        ResolutionPreset.high,
        enableAudio: false,
        imageFormatGroup: ImageFormatGroup.jpeg,
      );
      await controller.initialize();
      if (!mounted || generation != _initializationGeneration) {
        await controller.dispose();
        return;
      }

      setState(() {
        _controller = controller;
        _initializing = false;
      });
    } on CameraException catch (error) {
      if (!mounted || generation != _initializationGeneration) return;
      setState(() {
        _initializing = false;
        _errorMessage = _cameraErrorMessage(error);
      });
    } catch (_) {
      if (!mounted || generation != _initializationGeneration) return;
      setState(() {
        _initializing = false;
        _errorMessage =
            'Camera could not be started. Retry or select a photo from the gallery.';
      });
    }
  }

  String _cameraErrorMessage(CameraException error) {
    switch (error.code) {
      case 'CameraAccessDenied':
      case 'CameraAccessDeniedWithoutPrompt':
      case 'CameraAccessRestricted':
        return 'Camera permission is required. Allow camera access in Settings, then retry.';
      default:
        return 'Camera could not be started. Retry or select a photo from the gallery.';
    }
  }

  Future<void> _disposeController(
      {bool invalidateInitialization = true}) async {
    if (invalidateInitialization) ++_initializationGeneration;
    final controller = _controller;
    _controller = null;
    if (controller != null) {
      await controller.dispose();
    }
  }

  Future<void> _capturePhoto() async {
    final controller = _controller;
    if (_capturing ||
        controller == null ||
        !controller.value.isInitialized ||
        controller.value.isTakingPicture) {
      return;
    }

    setState(() {
      _capturing = true;
      _errorMessage = null;
    });
    try {
      final photo = await controller.takePicture();
      if (!mounted) return;
      await _disposeController();
      if (!mounted) return;
      setState(() {
        _capturedPhoto = photo;
        _capturing = false;
      });
    } on CameraException catch (error) {
      if (!mounted) return;
      setState(() {
        _capturing = false;
        _errorMessage = _cameraErrorMessage(error);
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _capturing = false;
        _errorMessage = 'Photo capture failed. Please retry.';
      });
    }
  }

  Future<void> _retryPhoto() async {
    final capturedPhoto = _capturedPhoto;
    setState(() {
      _capturedPhoto = null;
      _errorMessage = null;
    });
    await _deleteTemporaryPhoto(capturedPhoto);
    await _initializeCamera();
  }

  Future<void> _cancel() async {
    if (_cancelling || _returningPhoto) return;
    _cancelling = true;
    final capturedPhoto = _capturedPhoto;
    _capturedPhoto = null;
    await _deleteTemporaryPhoto(capturedPhoto);
    if (mounted) Navigator.of(context).pop<XFile>();
  }

  void _confirmPhoto() {
    final capturedPhoto = _capturedPhoto;
    if (_returningPhoto || capturedPhoto == null) return;
    _returningPhoto = true;
    Navigator.of(context).pop<XFile>(capturedPhoto);
  }

  Future<void> _deleteTemporaryPhoto(XFile? photo) async {
    if (photo == null) return;
    try {
      final file = File(photo.path);
      if (await file.exists()) await file.delete();
    } catch (_) {
      // The cache directory will remove an unconfirmed photo later if needed.
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    ++_initializationGeneration;
    final controller = _controller;
    _controller = null;
    unawaited(controller?.dispose());
    if (!_returningPhoto) unawaited(_deleteTemporaryPhoto(_capturedPhoto));
    unawaited(
      SystemChrome.setEnabledSystemUIMode(
        SystemUiMode.manual,
        overlays: SystemUiOverlay.values,
      ),
    );
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: true,
      onPopInvoked: (didPop) {
        if (!didPop) unawaited(_cancel());
      },
      child: AnnotatedRegion<SystemUiOverlayStyle>(
        value: const SystemUiOverlayStyle(
          statusBarColor: Colors.transparent,
          statusBarIconBrightness: Brightness.light,
          statusBarBrightness: Brightness.dark,
          systemNavigationBarColor: Colors.transparent,
          systemNavigationBarDividerColor: Colors.transparent,
          systemNavigationBarIconBrightness: Brightness.light,
        ),
        child: Scaffold(
          backgroundColor: Colors.black,
          body: Stack(
            fit: StackFit.expand,
            children: [
              _buildCameraBody(),
              CameraControlsSafeArea(
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    Positioned(
                      top: 8,
                      left: 8,
                      child: IconButton.filledTonal(
                        key: const Key('camera-cancel'),
                        onPressed: _cancel,
                        icon: const Icon(Icons.close),
                        tooltip: 'Cancel',
                      ),
                    ),
                    if (_capturedPhoto == null && _canCapture)
                      Positioned(
                        left: 0,
                        right: 0,
                        bottom: 24,
                        child: Center(
                          child: IconButton.filled(
                            key: const Key('camera-capture'),
                            onPressed: _capturing ? null : _capturePhoto,
                            iconSize: 42,
                            padding: const EdgeInsets.all(18),
                            icon: _capturing
                                ? const SizedBox.square(
                                    dimension: 30,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 3,
                                      color: Colors.white,
                                    ),
                                  )
                                : const Icon(Icons.camera_alt),
                            tooltip: 'Take photo',
                          ),
                        ),
                      ),
                    if (_capturedPhoto != null)
                      Positioned(
                        left: 20,
                        right: 20,
                        bottom: 24,
                        child: Row(
                          children: [
                            Expanded(
                              child: OutlinedButton.icon(
                                key: const Key('camera-retry'),
                                onPressed: _retryPhoto,
                                icon: const Icon(Icons.refresh),
                                label: const Text('Retry'),
                                style: OutlinedButton.styleFrom(
                                  foregroundColor: Colors.white,
                                  side: const BorderSide(color: Colors.white),
                                ),
                              ),
                            ),
                            const SizedBox(width: 16),
                            Expanded(
                              child: FilledButton.icon(
                                key: const Key('camera-confirm'),
                                onPressed: _confirmPhoto,
                                icon: const Icon(Icons.check),
                                label: const Text('Use photo'),
                              ),
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  bool get _canCapture {
    final controller = _controller;
    return !_initializing &&
        _errorMessage == null &&
        controller != null &&
        controller.value.isInitialized;
  }

  Widget _buildCameraBody() {
    final capturedPhoto = _capturedPhoto;
    if (capturedPhoto != null) {
      return CapturedPhotoReview(
        photo: File(capturedPhoto.path),
        errorBuilder: (_, __, ___) => _buildError(
          'The captured photo could not be previewed. Please retry.',
        ),
      );
    }

    final controller = _controller;
    if (_initializing) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null) return _buildError(_errorMessage!);
    if (controller == null || !controller.value.isInitialized) {
      return _buildError(
        'Camera could not be started. Retry or select a photo from the gallery.',
      );
    }

    return CameraPreviewCover(
      previewSize: controller.value.previewSize!,
      child: CameraPreview(controller),
    );
  }

  Widget _buildError(String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.camera_alt_outlined,
                color: Colors.white, size: 48),
            const SizedBox(height: 16),
            Text(
              message,
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.white),
            ),
            const SizedBox(height: 20),
            FilledButton.icon(
              key: const Key('camera-initialize-retry'),
              onPressed: _initializeCamera,
              icon: const Icon(Icons.refresh),
              label: const Text('Retry camera'),
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: _cancel,
              child: const Text('Choose from gallery instead'),
            ),
          ],
        ),
      ),
    );
  }
}
