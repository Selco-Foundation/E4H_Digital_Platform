import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:camera/camera.dart';
// Camera's platform interface lets tests exercise the real controller without hardware.
// ignore: depend_on_referenced_packages
import 'package:camera_platform_interface/camera_platform_interface.dart'
    as platform;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/widgets/customized_digit_widget/android_camera_capture_page.dart';

const rear = CameraDescription(
    name: 'rear',
    lensDirection: CameraLensDirection.back,
    sensorOrientation: 90);
const front = CameraDescription(
    name: 'front',
    lensDirection: CameraLensDirection.front,
    sensorOrientation: 90);

class TestCameraPlatform extends platform.CameraPlatform {
  TestCameraPlatform(this.photo);
  final XFile photo;
  List<CameraDescription> cameras = [rear, front];
  final created = <CameraDescription>[];
  final disposed = <int>[];
  int captures = 0;
  Completer<void>? initializeWait;
  Completer<void>? disposeWait;
  bool failDispose = false;

  @override
  Future<List<CameraDescription>> availableCameras() async => cameras;
  @override
  Future<int> createCameraWithSettings(
      CameraDescription description, platform.MediaSettings settings) async {
    assert(settings.resolutionPreset == ResolutionPreset.high);
    assert(settings.enableAudio == false);
    created.add(description);
    return created.length;
  }

  @override
  Future<void> initializeCamera(int cameraId,
      {ImageFormatGroup imageFormatGroup = ImageFormatGroup.unknown}) async {
    assert(imageFormatGroup == ImageFormatGroup.jpeg);
    await initializeWait?.future;
  }

  @override
  Stream<platform.CameraInitializedEvent> onCameraInitialized(int cameraId) =>
      Stream.value(platform.CameraInitializedEvent(
          cameraId, 1440, 1080, ExposureMode.auto, true, FocusMode.auto, true));
  @override
  Stream<platform.DeviceOrientationChangedEvent> onDeviceOrientationChanged() =>
      const Stream.empty();
  @override
  Widget buildPreview(int cameraId) => const ColoredBox(color: Colors.black);
  @override
  Future<XFile> takePicture(int cameraId) async {
    captures++;
    return photo;
  }

  @override
  Future<void> dispose(int cameraId) async {
    disposed.add(cameraId);
    await disposeWait?.future;
    if (failDispose) throw CameraException('cleanup-failed', 'test');
  }
}

void main() {
  late platform.CameraPlatform original;
  late TestCameraPlatform camera;
  late Directory directory;

  setUp(() async {
    original = platform.CameraPlatform.instance;
    directory = await Directory.systemTemp.createTemp('camera-capture-');
    final photo = File('${directory.path}/photo.png');
    await photo.writeAsBytes(base64Decode(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='));
    camera = TestCameraPlatform(XFile(photo.path));
    platform.CameraPlatform.instance = camera;
  });
  tearDown(() async {
    platform.CameraPlatform.instance = original;
    await directory.delete(recursive: true);
  });

  Future<void> openCamera(WidgetTester tester) async {
    await tester.pumpWidget(MaterialApp(
        home: Builder(
            builder: (context) => Scaffold(
                  body: TextButton(
                      onPressed: () => Navigator.of(context).push<XFile>(
                          MaterialPageRoute(
                              builder: (_) =>
                                  const AndroidCameraCapturePage())),
                      child: const Text('Open')),
                ))));
    await tester.tap(find.text('Open'));
    await tester.pumpAndSettle();
  }

  testWidgets(
      'switches lenses once and retains front camera for Retry and resume',
      (tester) async {
    await openCamera(tester);
    expect(camera.created, [rear]);
    camera.initializeWait = Completer<void>();
    final switchButton =
        tester.widget<IconButton>(find.byKey(const Key('camera-switch')));
    switchButton.onPressed!();
    switchButton
        .onPressed!(); // A rapid second tap must not open another controller.
    await tester.pump();
    expect(
        tester
            .widget<IconButton>(find.byKey(const Key('camera-switch')))
            .onPressed,
        isNull);
    camera.initializeWait!.complete();
    await tester.pumpAndSettle();
    expect(camera.created, [rear, front]);
    await tester.tap(find.byKey(const Key('camera-capture')));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('camera-switch')), findsNothing);
    await tester.tap(find.byKey(const Key('camera-retry')));
    await tester.pumpAndSettle();
    expect(camera.created.last, front);
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.inactive);
    await tester.pump();
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
    await tester.pumpAndSettle();
    expect(camera.created.last, front);
    expect(tester.takeException(), isNull);
  });

  for (final lens in [rear, front]) {
    testWidgets('single ${lens.name} camera captures without a switch button',
        (tester) async {
      camera.cameras = [lens];
      await openCamera(tester);
      expect(camera.created, [lens]);
      expect(find.byKey(const Key('camera-switch')), findsNothing);
      await tester.tap(find.byKey(const Key('camera-cancel')));
      await tester.pumpAndSettle();
      expect(find.text('Open'), findsOneWidget);
      expect(camera.captures, 0);
    });
  }

  for (final cleanupFails in [false, true]) {
    testWidgets(
        'review stays valid during delayed disposal; cleanup failure: $cleanupFails',
        (tester) async {
      await openCamera(tester);
      camera.disposeWait = Completer<void>();
      camera.failDispose = cleanupFails;
      await tester.tap(find.byKey(const Key('camera-capture')));
      await tester.pump();
      await tester.pump(const Duration(milliseconds: 50));
      expect(find.byType(CapturedPhotoReview), findsOneWidget);
      expect(find.byType(CameraPreview), findsNothing);
      expect(find.text('Retry camera'), findsNothing);
      expect(
          tester
              .widget<FilledButton>(find.byKey(const Key('camera-confirm')))
              .onPressed,
          isNull);
      expect(
          tester
              .widget<OutlinedButton>(find.byKey(const Key('camera-retry')))
              .onPressed,
          isNull);
      expect(tester.takeException(), isNull);
      camera.disposeWait!.complete();
      await tester.pumpAndSettle();
      expect(
          tester
              .widget<FilledButton>(find.byKey(const Key('camera-confirm')))
              .onPressed,
          isNotNull);
      await tester.tap(find.byKey(const Key('camera-confirm')));
      await tester.pumpAndSettle();
      expect(find.text('Open'), findsOneWidget);
      expect(await tester.runAsync(() => File(camera.photo.path).exists()),
          isTrue);
      expect(camera.captures, 1);
      expect(tester.takeException(), isNull);
    });
  }
}
