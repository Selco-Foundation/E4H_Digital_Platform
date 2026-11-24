import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/widgets/customized_digit_widget/android_camera_capture_page.dart';

void main() {
  Future<void> pumpPreview(
    WidgetTester tester, {
    required Size viewportSize,
    required Size sensorPreviewSize,
  }) async {
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(1000, 1000);
    addTearDown(tester.view.reset);
    await tester.pumpWidget(
      MaterialApp(
        home: Center(
          child: SizedBox.fromSize(
            size: viewportSize,
            child: CameraPreviewCover(
              previewSize: sensorPreviewSize,
              child: const ColoredBox(
                key: Key('preview-test-content'),
                color: Colors.blue,
              ),
            ),
          ),
        ),
      ),
    );
  }

  testWidgets('4:3 sensor fills a tall portrait viewport without stretching',
      (tester) async {
    await pumpPreview(
      tester,
      viewportSize: const Size(400, 800),
      sensorPreviewSize: const Size(1440, 1080),
    );

    expect(
      tester.getSize(find.byKey(const Key('camera-preview-viewport'))),
      const Size(400, 800),
    );
    final contentSize =
        tester.getSize(find.byKey(const Key('preview-test-content')));
    expect(contentSize, const Size(600, 800));
    expect(contentSize.aspectRatio, closeTo(3 / 4, 0.0001));
  });

  testWidgets('16:9 sensor fills a tall portrait viewport without stretching',
      (tester) async {
    await pumpPreview(
      tester,
      viewportSize: const Size(400, 800),
      sensorPreviewSize: const Size(1920, 1080),
    );

    final contentSize =
        tester.getSize(find.byKey(const Key('preview-test-content')));
    expect(contentSize, const Size(450, 800));
    expect(contentSize.aspectRatio, closeTo(9 / 16, 0.0001));
  });

  testWidgets('landscape viewport is covered without changing sensor ratio',
      (tester) async {
    await pumpPreview(
      tester,
      viewportSize: const Size(800, 400),
      sensorPreviewSize: const Size(1920, 1080),
    );

    expect(
      tester.getSize(find.byKey(const Key('camera-preview-viewport'))),
      const Size(800, 400),
    );
    final contentSize =
        tester.getSize(find.byKey(const Key('preview-test-content')));
    expect(contentSize, const Size(800, 450));
    expect(contentSize.aspectRatio, closeTo(16 / 9, 0.0001));
  });

  test('captured photo review preserves the complete image', () {
    expect(CapturedPhotoReview.imageFit, BoxFit.contain);
  });

  testWidgets('camera controls remain inside system safe insets',
      (tester) async {
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(400, 800);
    tester.view.viewPadding =
        const FakeViewPadding(top: 24, left: 8, right: 12, bottom: 48);
    tester.view.padding =
        const FakeViewPadding(top: 24, left: 8, right: 12, bottom: 48);
    addTearDown(tester.view.reset);

    await tester.pumpWidget(
      const MaterialApp(
        home: CameraControlsSafeArea(
          child: ColoredBox(
            key: Key('safe-controls-content'),
            color: Colors.transparent,
          ),
        ),
      ),
    );

    final rect = tester.getRect(find.byKey(const Key('safe-controls-content')));
    expect(rect, const Rect.fromLTRB(8, 24, 388, 752));
  });
}
