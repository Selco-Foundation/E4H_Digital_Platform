import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image_picker/image_picker.dart';

import '../../lib/widgets/customized_digit_widget/image_uploader.dart';

void main() {
  late Directory temporaryDirectory;
  late File capturedImage;

  setUp(() async {
    temporaryDirectory = await Directory.systemTemp.createTemp('camera-test-');
    capturedImage = File('${temporaryDirectory.path}/captured.png');
    await capturedImage.writeAsBytes(
      base64Decode(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=',
      ),
    );
  });

  tearDown(() async {
    if (await temporaryDirectory.exists()) {
      await temporaryDirectory.delete(recursive: true);
    }
  });

  Future<void> pumpUploader(
    WidgetTester tester, {
    required CameraCaptureLauncher launcher,
    required FutureOr<void> Function(List<File>) onImagesSelected,
  }) async {
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(400, 800);
    addTearDown(tester.view.reset);
    await tester.pumpWidget(
      MaterialApp(
        theme: DigitTheme.instance.mobileTheme,
        home: Scaffold(
          body: ImageUploader(
            cameraCaptureLauncher: launcher,
            onImagesSelected: onImagesSelected,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  Future<void> openCamera(WidgetTester tester) async {
    await tester.tap(find.text('Click to add photo'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Camera'));
    await tester.pump(const Duration(milliseconds: 350));
  }

  testWidgets('delivers one confirmed camera photo exactly once',
      (tester) async {
    var launches = 0;
    final deliveries = <List<File>>[];
    await pumpUploader(
      tester,
      launcher: (_) async {
        launches++;
        return XFile(capturedImage.path);
      },
      onImagesSelected: (files) => deliveries.add(files),
    );

    await openCamera(tester);
    await tester.pumpAndSettle();

    expect(launches, 1);
    expect(deliveries, hasLength(1));
    expect(deliveries.single.single.path, capturedImage.path);
  });

  testWidgets('camera cancellation does not deliver a photo', (tester) async {
    var deliveries = 0;
    await pumpUploader(
      tester,
      launcher: (_) async => null,
      onImagesSelected: (_) => deliveries++,
    );

    await openCamera(tester);
    await tester.pumpAndSettle();

    expect(deliveries, 0);
    expect(find.text('Click to add photo'), findsOneWidget);
  });

  testWidgets('can retry camera after cancellation', (tester) async {
    var launches = 0;
    var deliveries = 0;
    await pumpUploader(
      tester,
      launcher: (_) async {
        launches++;
        return launches == 1 ? null : XFile(capturedImage.path);
      },
      onImagesSelected: (_) => deliveries++,
    );

    await openCamera(tester);
    await tester.pumpAndSettle();
    await openCamera(tester);
    await tester.pumpAndSettle();

    expect(launches, 2);
    expect(deliveries, 1);
  });

  testWidgets('shows an error when the upload callback fails', (tester) async {
    await pumpUploader(
      tester,
      launcher: (_) async => XFile(capturedImage.path),
      onImagesSelected: (_) => throw StateError('upload failed'),
    );

    await openCamera(tester);
    await tester.pumpAndSettle();

    expect(find.text('Failed to process selected image'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('duplicate taps cannot launch or deliver twice', (tester) async {
    final result = Completer<XFile?>();
    var launches = 0;
    var deliveries = 0;
    await pumpUploader(
      tester,
      launcher: (_) {
        launches++;
        return result.future;
      },
      onImagesSelected: (_) => deliveries++,
    );

    await openCamera(tester);
    expect(launches, 1);
    await tester.tap(find.text('Click to add photo'));
    await tester.pump();
    expect(find.text('Camera is still opening, please wait'), findsOneWidget);

    result.complete(XFile(capturedImage.path));
    await tester.pumpAndSettle();

    expect(launches, 1);
    expect(deliveries, 1);
  });
  for (final systemBack in [true, false]) {
    testWidgets(
        'confirmed camera photo preserves nested report navigation; system Back: $systemBack',
        (tester) async {
      final root = GlobalKey<NavigatorState>();
      final reports = GlobalKey<NavigatorState>();
      final platformCalls = <MethodCall>[];
      var deliveries = 0;
      debugDefaultTargetPlatformOverride = TargetPlatform.android;
      tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
      tester.binding.defaultBinaryMessenger
          .setMockMethodCallHandler(SystemChannels.platform, (call) async {
        platformCalls.add(call);
        return null;
      });
      addTearDown(() {
        debugDefaultTargetPlatformOverride = null;
        tester.binding.defaultBinaryMessenger
            .setMockMethodCallHandler(SystemChannels.platform, null);
      });
      tester.view.devicePixelRatio = 1;
      tester.view.physicalSize = const Size(400, 800);
      addTearDown(tester.view.reset);
      await tester.pumpWidget(MaterialApp(
        navigatorKey: root,
        theme: DigitTheme.instance.mobileTheme,
        home: NavigatorPopHandler(
          onPop: () => reports.currentState!.pop(),
          child: Navigator(
            key: reports,
            onGenerateRoute: (_) => MaterialPageRoute<void>(
                builder: (_) => const Scaffold(body: Text('Previous report'))),
          ),
        ),
      ));
      reports.currentState!.push(MaterialPageRoute<void>(
          builder: (reportContext) => Scaffold(
                appBar: AppBar(
                    leading: IconButton(
                        key: const Key('report-back'),
                        onPressed: () => Navigator.of(reportContext).maybePop(),
                        icon: const Icon(Icons.arrow_back))),
                body: ImageUploader(
                  cameraCaptureLauncher: (context) =>
                      Navigator.of(context).push<XFile>(MaterialPageRoute(
                    builder: (cameraContext) => Scaffold(
                        body: TextButton(
                      onPressed: () => Navigator.of(cameraContext)
                          .pop(XFile(capturedImage.path)),
                      child: const Text('Use photo'),
                    )),
                  )),
                  onImagesSelected: (_) => deliveries++,
                ),
              )));
      await tester.pumpAndSettle();
      await openCamera(tester);
      await tester.pumpAndSettle();
      await tester.tap(find.text('Use photo'));
      await tester.pumpAndSettle();
      expect(deliveries, 1);
      expect(root.currentState!.canPop(), isFalse);
      expect(reports.currentState!.canPop(), isTrue);
      expect(
          platformCalls
              .where((call) =>
                  call.method == 'SystemNavigator.setFrameworkHandlesBack')
              .last
              .arguments,
          isTrue);
      if (systemBack) {
        await tester.binding.handlePopRoute();
      } else {
        await tester.tap(find.byKey(const Key('report-back')));
      }
      await tester.pumpAndSettle();
      expect(find.text('Previous report'), findsOneWidget);
      expect(tester.takeException(), isNull);
      debugDefaultTargetPlatformOverride = null;
    });
  }
}
