import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:camera/camera.dart';
// Platform interfaces exercise the real camera/picker widgets without hardware.
// ignore: depend_on_referenced_packages
import 'package:camera_platform_interface/camera_platform_interface.dart'
    as camera_platform;
import 'package:digit_scanner/blocs/app_localization.dart';
import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/widgets/vision_detector_views/detector_view.dart';
import 'package:digit_ui_components/theme/digit_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_mlkit_barcode_scanning/google_mlkit_barcode_scanning.dart';
// ignore: depend_on_referenced_packages
import 'package:image_picker_platform_interface/image_picker_platform_interface.dart'
    as picker_platform;
import 'package:isar_community/isar.dart';

import '../../lib/blocs/localization/app_localization.dart';
import '../../lib/data/nosql/localization.dart';
import '../../lib/utils/scanner_localization.dart';
import '../../lib/widgets/customized_digit_widget/qr_scanner.dart';

class UnusedIsar implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected cache access');
}

class BackendLocalization extends AppLocalizations {
  BackendLocalization(Locale locale, this.messages)
      : super(locale, UnusedIsar());
  final Map<String, String> messages;
  @override
  String translate(String key) => messages[key] ?? key;
}

class BackendDelegate extends LocalizationsDelegate<AppLocalizations> {
  const BackendDelegate(this.messages);
  final Map<String, String> messages;
  @override
  bool isSupported(Locale locale) => true;
  @override
  bool shouldReload(BackendDelegate old) => true;
  @override
  Future<AppLocalizations> load(Locale locale) async =>
      BackendLocalization(locale, messages);
}

class ScannerDelegate extends LocalizationsDelegate<ScannerLocalization> {
  const ScannerDelegate(this.messages);
  final Map<String, String> messages;
  @override
  bool isSupported(Locale locale) => true;
  @override
  bool shouldReload(ScannerDelegate old) => true;
  @override
  Future<ScannerLocalization> load(Locale locale) async {
    final scanner = ScannerLocalization(
        locale,
        Future.value([
          for (final row in messages.entries)
            Localization()
              ..code = row.key
              ..message = row.value
              ..locale = '${locale.languageCode}_${locale.countryCode}'
              ..module = 'rainmaker-common'
        ]),
        const []);
    await scanner.load();
    return scanner;
  }
}

class ScannerCamera extends camera_platform.CameraPlatform {
  int nextId = 0;
  @override
  Future<List<CameraDescription>> availableCameras() async => const [
        CameraDescription(
            name: 'rear',
            lensDirection: CameraLensDirection.back,
            sensorOrientation: 90)
      ];
  @override
  Future<int> createCameraWithSettings(CameraDescription description,
          camera_platform.MediaSettings settings) async =>
      ++nextId;
  @override
  Future<void> initializeCamera(int id,
      {ImageFormatGroup imageFormatGroup = ImageFormatGroup.unknown}) async {}
  @override
  Stream<camera_platform.CameraInitializedEvent> onCameraInitialized(int id) =>
      Stream.value(camera_platform.CameraInitializedEvent(
          id, 1080, 1440, ExposureMode.auto, true, FocusMode.auto, true));
  @override
  Stream<camera_platform.DeviceOrientationChangedEvent>
      onDeviceOrientationChanged() => const Stream.empty();
  @override
  Stream<camera_platform.CameraImageData> onStreamedFrameAvailable(int id,
          {camera_platform.CameraImageStreamOptions? options}) =>
      const Stream.empty();
  @override
  Future<double> getMinZoomLevel(int id) async => 1;
  @override
  Future<double> getMaxZoomLevel(int id) async => 4;
  @override
  Future<double> getMinExposureOffset(int id) async => 0;
  @override
  Future<double> getMaxExposureOffset(int id) async => 1;
  @override
  Future<void> dispose(int id) async {}
  @override
  Future<void> setFlashMode(int id, FlashMode mode) async {}
  @override
  Widget buildPreview(int id) => const ColoredBox(color: Colors.black);
}

class ScannerPicker extends picker_platform.ImagePickerPlatform {
  @override
  Future<XFile?> getImageFromSource(
          {required picker_platform.ImageSource source,
          picker_platform.ImagePickerOptions options =
              const picker_platform.ImagePickerOptions()}) async =>
      XFile('/tmp/scanner-photo.png');
}

void main() {
  final messages = {
    for (final row in jsonDecode(
        File('docs/localization/scanner_messages.en_IN.json')
            .readAsStringSync()) as List)
      row['code'] as String: row['message'] as String
  };
  late camera_platform.CameraPlatform oldCamera;
  late picker_platform.ImagePickerPlatform oldPicker;
  late DigitScannerBloc bloc;
  late List<Map<String, dynamic>> detections;
  late List<dynamic> results;
  Completer<List<Map<String, dynamic>>>? detectionWait;
  bool failDecode = false;
  const mlkit = MethodChannel('google_mlkit_barcode_scanning');

  setUp(() {
    oldCamera = camera_platform.CameraPlatform.instance;
    camera_platform.CameraPlatform.instance = ScannerCamera();
    oldPicker = picker_platform.ImagePickerPlatform.instance;
    picker_platform.ImagePickerPlatform.instance = ScannerPicker();
    bloc = DigitScannerBloc(const DigitScannerState());
    results = [];
    detections = [];
    detectionWait = null;
    failDecode = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(mlkit, (call) async {
      if (call.method == 'vision#startBarcodeScanner') {
        if (failDecode) throw PlatformException(code: 'private-plugin-detail');
        return detectionWait?.future ?? detections;
      }
      return null;
    });
  });
  tearDown(() async {
    await bloc.close();
    camera_platform.CameraPlatform.instance = oldCamera;
    picker_platform.ImagePickerPlatform.instance = oldPicker;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(mlkit, null);
  });

  Map<String, dynamic> barcode(String value) => {
        'type': BarcodeType.text.index,
        'format': BarcodeFormat.qrCode.rawValue,
        'displayValue': value,
        'rawValue': value,
        'rect': {'left': 0.0, 'top': 0.0, 'right': 1.0, 'bottom': 1.0},
        'points': [
          {'x': 0, 'y': 0},
          {'x': 1, 'y': 0},
          {'x': 1, 'y': 1},
          {'x': 0, 'y': 1}
        ]
      };

  Future<void> settle(WidgetTester tester) async {
    await tester.runAsync(() => Future<void>.delayed(Duration.zero));
    await tester.pumpAndSettle();
  }

  Future<void> open(WidgetTester tester,
      {List<dynamic>? validations,
      String? regex,
      bool gs1 = false,
      Map<String, String>? translations,
      Size size = const Size(400, 800)}) async {
    final text = translations ?? messages;
    tester.view.physicalSize = size;
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(MaterialApp(
      theme: DigitTheme.instance.mobileTheme,
      locale: const Locale('en', 'IN'),
      supportedLocales: const [Locale('en', 'IN')],
      localizationsDelegates: [BackendDelegate(text), ScannerDelegate(text)],
      home: Builder(
          builder: (context) => Scaffold(
              body: TextButton(
                  child: const Text('Open scanner'),
                  onPressed: () async {
                    final result = await Navigator.of(context).push(
                        MaterialPageRoute(
                            builder: (_) => BlocProvider.value(
                                value: bloc,
                                child: DigitScannerPage(
                                    quantity: 10,
                                    isGS1code: gs1,
                                    regex: regex,
                                    validations: validations))));
                    results.add(result);
                  }))),
    ));
    await settle(tester);
    await tester.tap(find.text('Open scanner'));
    await settle(tester);
  }

  Future<void> scan(WidgetTester tester, String value) async {
    detections = [barcode(value)];
    final onImage =
        tester.widget<DetectorView>(find.byType(DetectorView)).onImage;
    await tester.runAsync(() async => await onImage(InputImage.fromBytes(
        bytes: Uint8List(4),
        metadata: InputImageMetadata(
            size: const Size(1, 1),
            rotation: InputImageRotation.rotation0deg,
            format: InputImageFormat.bgra8888,
            bytesPerRow: 4))));
    await tester.pump();
  }

  testWidgets('counts, deletion, and Submit do not re-add accepted scans',
      (tester) async {
    await open(tester);
    expect(find.text('0 codes scanned'), findsOneWidget);
    await scan(tester, 'one');
    expect(find.text('1 code scanned'), findsOneWidget);
    await scan(tester, 'two');
    expect(find.text('2 codes scanned'), findsOneWidget);
    await tester.tap(find.byIcon(Icons.delete).first);
    await settle(tester);
    expect(bloc.state.qrCodes, ['two']);
    expect(find.text('1 code scanned'), findsOneWidget);
    final sequence = bloc.state.errorSeq;
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.text('Save and close'), findsOneWidget);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    expect(bloc.state.errorSeq, sequence);
    await tester.tap(find.text('Save and close'));
    await settle(tester);
    expect(results, ['two']);
    expect(tester.takeException(), isNull);
  });

  testWidgets(
      'legacy duplicate toast is translated and cleared before confirmation',
      (tester) async {
    await open(tester, validations: [
      {'type': 'duplicate', 'message': 'Common_error_scan_duplicate'}
    ]);
    await scan(tester, 'one');
    await scan(tester, 'one');
    expect(find.text('This code has already been scanned.'), findsOneWidget);
    expect(find.textContaining('Common_error'), findsNothing);
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    await tester.tap(find.text('Keep scanning'));
    await settle(tester);
    expect(results, isEmpty);
    expect(find.text('1 code scanned'), findsOneWidget);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    await scan(tester, 'two');
    expect(find.text('2 codes scanned'), findsOneWidget);
  });

  testWidgets(
      'manual validation rejects blank and invalid input before confirmation',
      (tester) async {
    await open(tester, regex: r'^CODE\d+$');
    await tester.tap(find.text('Enter serial number'));
    await settle(tester);
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.text('This field is required.'), findsOneWidget);
    expect(find.text('Save and close'), findsNothing);
    await tester.enterText(find.byType(TextField).first, 'invalid');
    await tester.pump();
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(bloc.state.error, 'COMMON_ERROR_SCAN_PATTERN');
    expect(
        find.text('The scanned code has an invalid format.'), findsOneWidget);
    expect(find.text('Save and close'), findsNothing);
    expect(bloc.state.qrCodes, isEmpty);
    await tester.enterText(find.byType(TextField).first, 'CODE1');
    await tester.pump();
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.text('Save and close'), findsOneWidget);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    await tester.tap(find.text('Save and close'));
    await settle(tester);
    expect(results, ['CODE1']);
  });

  testWidgets(
      'incomplete dialog cannot save, dismissal resumes, and late gallery results are ignored',
      (tester) async {
    await open(tester);
    detectionWait = Completer();
    await tester.tap(find.text('Scan from photo'));
    await tester.pump();
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.text('Scan 1 code before submitting.'), findsOneWidget);
    expect(find.text('Save and close'), findsNothing);
    detectionWait!.complete([barcode('late')]);
    await tester.pump();
    expect(bloc.state.qrCodes, isEmpty);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    await tester.binding.handlePopRoute();
    await settle(tester);
    detectionWait = null;
    await scan(tester, 'accepted');
    expect(find.text('1 code scanned'), findsOneWidget);
  });

  testWidgets(
      'gallery errors are localized, wrap on a narrow screen, and dismiss with the route',
      (tester) async {
    final text = {
      ...messages,
      'NO_CODE_FOUND_IN_PHOTO':
          'A long backend photo error message that must wrap across multiple lines without clipping or leaking a localization key.'
    };
    await open(tester, translations: text, size: const Size(320, 700));
    await tester.tap(find.text('Scan from photo'));
    await settle(tester);
    expect(find.text(text['NO_CODE_FOUND_IN_PHOTO']!), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.tap(find.byTooltip('Close'));
    await tester.pump();
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    failDecode = true;
    await tester.tap(find.text('Scan from photo'));
    await settle(tester);
    expect(find.text('Failed to read the photo. Please try again.'),
        findsOneWidget);
    await tester.binding.handlePopRoute();
    await settle(tester);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    expect(results, [null]);
  });

  testWidgets('backend wording and count templates wrap on narrow screens',
      (tester) async {
    final text = {
      ...messages,
      ScannerKeys.codesScanned:
          'Codes sélectionnés pour cet équipement : {count} codes'
    };
    await open(tester, translations: text, size: const Size(320, 700));
    expect(find.text('Codes sélectionnés pour cet équipement : 0 codes'),
        findsOneWidget);
    expect(tester.takeException(), isNull);
  });
  testWidgets('GS1 parse errors never confirm and valid GS1 values return once',
      (tester) async {
    await open(tester, gs1: true);
    await scan(tester, 'not-a-gs1-code');
    expect(find.text('Invalid GS1 code.'), findsOneWidget);
    expect(bloc.state.barCodes, isEmpty);
    expect(find.text('Save and close'), findsNothing);
    await scan(tester, '10BATCH\u001d21SERIAL');
    expect(find.text('1 code scanned'), findsOneWidget);
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.byKey(const Key('scanner-error-toast')), findsNothing);
    await tester.tap(find.text('Save and close'));
    await settle(tester);
    expect(results, hasLength(1));
    expect(results.single, hasLength(1));
    expect(results.single.single.elements['21'].data, 'SERIAL');
  });

  testWidgets(
      'accepted manual entry can keep scanning and save the accepted selection later',
      (tester) async {
    await open(tester);
    await tester.tap(find.text('Enter serial number'));
    await settle(tester);
    await tester.enterText(find.byType(TextField).first, 'MANUAL');
    await tester.pump();
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(bloc.state.qrCodes, ['MANUAL']);
    await tester.tap(find.text('Keep scanning'));
    await settle(tester);
    expect(find.byType(DetectorView), findsOneWidget);
    expect(find.text('1 code scanned'), findsOneWidget);
    expect(results, isEmpty);
    await tester.tap(find.text('Submit'));
    await settle(tester);
    await tester.tap(find.text('Save and close'));
    await settle(tester);
    expect(results, ['MANUAL']);
  });

  testWidgets(
      'long backend dialog translations fit a narrow screen with exact capitalization',
      (tester) async {
    final text = {
      ...messages,
      'SCANNER_DIALOG_TITLE':
          'Vérifiez les codes sélectionnés pour cet équipement',
      'SCANNER_DIALOG_CONTENT':
          'Vous pouvez enregistrer cette sélection ou continuer à scanner les codes de votre équipement.',
      'SCANNER_DIALOG_PRIMARY_ACTION': 'Continuer le scan',
      'SCANNER_DIALOG_SECONDARY_ACTION': 'Enregistrer',
    };
    await open(tester, translations: text, size: const Size(320, 700));
    await scan(tester, 'one');
    await tester.tap(find.text('Submit'));
    await settle(tester);
    expect(find.text(text['SCANNER_DIALOG_TITLE']!), findsOneWidget);
    expect(find.text('Continuer le scan'), findsOneWidget);
    expect(tester.takeException(), isNull);
    await tester.tap(find.text('Continuer le scan'));
    await settle(tester);
    expect(find.text('1 code scanned'), findsOneWidget);
  });
}
