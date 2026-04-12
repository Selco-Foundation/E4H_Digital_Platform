import 'package:audioplayers/audioplayers.dart';
import 'package:camera/camera.dart';
import 'package:digit_scanner/blocs/app_localization.dart';
import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/utils/constants.dart';
import 'package:digit_scanner/utils/extensions/extensions.dart';
import 'package:digit_scanner/utils/i18_key_constants.dart' as i18;
import 'package:digit_scanner/widgets/vision_detector_views/painters/barcode_detector_painter.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_mlkit_barcode_scanning/google_mlkit_barcode_scanning.dart';
import 'package:gs1_barcode_parser/gs1_barcode_parser.dart';
import 'package:intl/intl.dart';

class DigitScannerUtils {
  void buildDialog(
    BuildContext context,
    ScannerLocalization localizations,
    int quantity, [
    VoidCallback? onKeepScanning,
  ]
  ) async {
    var contentLocalization = localizations
        .translate(
          i18.scanner.scannerDialogContent,
        )
        .replaceAll('{quantity}', quantity.toString());
    await showCustomPopup(
      context: context,
      builder: (popupContext) => Popup(
        title: localizations.translate(
          i18.scanner.scannerDialogTitle,
        ),
        onOutsideTap: () {
          Navigator.of(popupContext).pop();
        },
        description: contentLocalization,
        type: PopUpType.simple,
        actions: [
          DigitButton(
            label: localizations.translate(
              i18.scanner.scannerDialogSecondaryAction,
            ),
            onPressed: () {
              Navigator.of(
                context,
                rootNavigator: true,
              ).pop(true);

              Navigator.of(
                context,
              ).pop();
            },
            type: DigitButtonType.primary,
            size: DigitButtonSize.large,
          ),
          DigitButton(
            label: localizations.translate(
              i18.scanner.scannerDialogPrimaryAction,
            ),
            onPressed: () {
              Navigator.of(
                popupContext,
                rootNavigator: true,
              ).pop(false);
              onKeepScanning?.call();
            },
            type: DigitButtonType.secondary,
            size: DigitButtonSize.large,
          ),
        ],
      ),
    );
  }

  String trimString(String input) {
    return input.length > 20 ? '${input.substring(0, 20)}...' : input;
  }

  String? gtinFrom(GS1Barcode b) {
    // GTIN lives in AI (01). If you’re scanning “contained items” it might be (02).
    return b.elements['01']?.data ?? b.elements['02']?.data;
  }

  // Put somewhere in utils
  String normalizeGs1(String s) {
    if (s.isEmpty) return s;

    // If the scan already contains ASCII 29 (FNC1), just strip whitespace.
    const gs = '\u001d';
    if (s.contains(gs)) {
      return s.replaceAll(RegExp(r'\s+'), '');
    }

    // Human form: "(AI) value (AI) value ..." possibly with spaces
    if (!s.contains('(')) {
      // Plain Code-128/EAN text (no AIs) – return cleaned, parser will error if not GS1.
      return s.replaceAll(RegExp(r'\s+'), '');
    }

    // AIs that are variable-length → need FNC1 *after the value* if followed by another AI
    const varAIs = {
      '10',
      '21',
      '22',
      '235',
      '240',
      '241',
      '242',
      '243',
      '250',
      '251',
      '254',
      '255',
      '30',
      '37',
      '3922',
      '3932',
      '3933',
      '41',
      '42',
      '421',
      '422',
      '423',
      '710',
      '711',
      '712',
      '713',
      '714',
      '715',
      '7031',
      '8200',
      '90',
      '91',
      '92',
      '93',
      '94',
      '95',
      '96',
      '97',
      '98',
      '99'
    };

    // Remove all spaces, then parse “(AI)VALUE” chunks
    final compact = s.replaceAll(RegExp(r'\s+'), '');
    final re = RegExp(r'\((\d{2,4})\)([^()]+)'); // (AI)VALUE
    final matches = re.allMatches(compact).toList();
    if (matches.isEmpty) return compact;

    final buf = StringBuffer();
    for (var i = 0; i < matches.length; i++) {
      final ai = matches[i].group(1)!;
      final val = matches[i].group(2)!;
      buf.write(ai);
      buf.write(val);
      final hasNext = i < matches.length - 1;
      if (hasNext && varAIs.contains(ai))
        buf.write(gs); // FNC1 after variable-length AI
    }
    return buf.toString();
  }

  // Returns the *value* of the first AI parsed from the barcode.
// Dart maps preserve insertion order, so `.entries.first` is the left-most AI.
  String? firstAIValue(GS1Barcode p) {
    if (p.elements.isEmpty) return null;
    final first = p.elements.entries.first.value;
    // For all the AIs you listed, `.data` is the printable value
    final v = first.data?.toString().trim();
    return (v == null || v.isEmpty) ? null : v;
  }

  Future<void> processImage({
    required BuildContext context,
    required InputImage inputImage,
    required bool canProcess,
    required bool isBusy,
    required Function(bool) setBusy,
    required Function(String) setText,
    required Function(CustomPaint?) updateCustomPaint,
    required bool isGS1code,
    required int quantity,
    required List<dynamic> result, // not used (ok to keep)
    required Future<void> Function(String) handleError,
    required CameraLensDirection cameraLensDirection,
    required BarcodeScanner barcodeScanner,
    required ScannerLocalization localizations,
    Map<String, String>? validations,
    String? regex, // pass-through to bloc
  }) async {
    if (!canProcess || isBusy) return;
    setBusy(true);
    setText('');

    final List<Barcode> barcodes;
    try {
      barcodes = await barcodeScanner.processImage(inputImage);
    } catch (_) {
      setBusy(false);
      return;
    }

    if (inputImage.metadata?.size != null &&
        inputImage.metadata?.rotation != null) {
      if (barcodes.isNotEmpty) {
        final bloc = context.read<DigitScannerBloc>();
        final first =
            (barcodes.first.rawValue ?? barcodes.first.displayValue)?.trim();

        if (first != null && first.isNotEmpty) {
          if (isGS1code) {
            try {
              // Prefer rawValue for machine data; fall back to displayValue
              final data =
                  (barcodes.first.rawValue ?? barcodes.first.displayValue)
                      ?.trim();
              if (data == null || data.isEmpty) {
                await handleError(
                    localizations.translate(i18.scanner.resourcesScanFailed));
              } else {
                final parser = GS1BarcodeParser.defaultParser();
                final parsed = parser.parse(data); // ← keep the full object
                bloc.add(DigitScannerEvent.handleScanner(
                  barCode: [parsed], // ← store GS1Barcode, not GTIN
                  isGS1: true,
                  quantity: quantity,
                  regex: regex,
                ));
              }
            } catch (e) {
              await handleError(
                  localizations.translate(i18.scanner.resourcesScanFailed));
            }
          } else {
            // QR-mode unchanged
            final first =
                (barcodes.first.displayValue ?? barcodes.first.rawValue)
                    ?.trim();
            if (first != null && first.isNotEmpty) {
              bloc.add(DigitScannerEvent.handleScanner(
                qrCode: [first],
                isGS1: false,
                quantity: quantity,
                regex: regex,
              ));
            }
          }
        }
      }

      // draw overlays as you already do...
      final painter = BarcodeDetectorPainter(
        barcodes,
        inputImage.metadata!.size,
        inputImage.metadata!.rotation,
        cameraLensDirection,
      );
      updateCustomPaint(CustomPaint(painter: painter));
    } else {
      String text =
          '${localizations.translate(i18.scanner.barCodesFound)}: ${barcodes.length}\n\n';
      for (final b in barcodes) {
        text +=
            '${localizations.translate(i18.scanner.barCode)}: ${b.rawValue}\n\n';
      }
      setText(text);
      updateCustomPaint(null);
    }

    setBusy(false);
  }

  Future<void> handleError({
    required BuildContext context,
    required String message,
    required AudioPlayer player,
    required List<dynamic> result,
    required Function setStateCallback,
    required ScannerLocalization localizations,
  }) async {
    // Play the buzzer sound to indicate an error
    player.play(AssetSource(DigitScannerConstants().errorFilePath));

    // Check if the player has completed playing or if the result list is empty
    if (player.state == PlayerState.completed || result.isEmpty) {
      // Display a toast message with the provided error message
      Toast.showToast(
        context,
        type: ToastType.error,
        message: localizations.translate(message),
      );
    }

    // Wait for 2 seconds before proceeding
    await Future.delayed(const Duration(seconds: 2));

    // Update the state to allow processing again and indicate not busy
    setStateCallback();
  }

  Future<void> storeCode({
    required BuildContext context,
    required String code,
    required AudioPlayer player,
    required bool singleValue,
    required Function(List<String>) updateCodes,
    required List<String> initialCodes,
  }) async {
    // Play the add sound to indicate a successful scan
    player.play(AssetSource(DigitScannerConstants().audioFilePath));

    // Access the DigitScannerBloc from the context
    final bloc = context.read<DigitScannerBloc>();

    // Make a copy of the current QR codes from the bloc state
    List<String> codes = List.from(initialCodes);

    // If the widget is supposed to handle a single value, clear the codes list
    if (singleValue) {
      codes = [];
    }

    // Add the new code to the list
    codes.add(code);

    // Update the state with the new list of codes
    updateCodes(codes);

    // Dispatch an event to update the bloc with the new barcode and QR code lists
    bloc.add(DigitScannerEvent.handleScanner(
      barCode: bloc.state.barCodes, // Keep existing barcodes
      qrCode: codes, // Update QR codes with the new list
    ));

    // Wait for 5 seconds before completing the function
    await Future.delayed(const Duration(seconds: 5));
  }

  Future<void> storeValue({
    required BuildContext context,
    required GS1Barcode scanData,
    required AudioPlayer player,
    required Function(List<GS1Barcode>) updateResult,
    required List<GS1Barcode> initialResult,
  }) async {
    // Assign the scanned data to a local variable
    final parsedResult = scanData;

    // Access the DigitScannerBloc from the context
    final bloc = context.read<DigitScannerBloc>();

    // Play the add sound to indicate a successful scan
    player.play(AssetSource(DigitScannerConstants().audioFilePath));

    // Wait for 3 seconds before proceeding
    await Future.delayed(const Duration(seconds: 3));

    // Make a copy of the current barcodes from the bloc state
    List<GS1Barcode> result = List.from(initialResult);

    // Remove duplicate entries based on the last value in the elements map
    result.removeDuplicates(
      (element) => element.elements.entries.last.value.data,
    );

    // Add the new parsed result to the list
    result.add(parsedResult);

    // Dispatch an event to update the bloc with the new barcode and existing QR code lists
    bloc.add(DigitScannerEvent.handleScanner(
      barCode: result, // Update barcodes with the new list
      qrCode: bloc.state.qrCodes, // Keep existing QR codes
    ));

    // Update the state with the new list of results
    updateResult(result);

    // Wait for 5 seconds before completing the function
    await Future.delayed(const Duration(seconds: 5));
  }

  Map<String, dynamic> getGs1CodeFormattedString(List<GS1Barcode> barCodes) {
    final elements = barCodes.last;

    return elements.getAIsData;
  }

  String generateGS1Barcode({
    required String batchNumber,
    required DateTime expiryDate,
    required String serialNumber,
  }) {
    const groupSeparator = '\u001d'; // ASCII 29
    final formattedDate = DateFormat('yyMMdd').format(expiryDate);

    return '10$batchNumber${groupSeparator}17${formattedDate}21$serialNumber$groupSeparator';
  }
}
