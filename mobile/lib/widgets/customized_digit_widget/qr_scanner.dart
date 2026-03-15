import 'dart:io';

import 'package:audioplayers/audioplayers.dart';
import 'package:auto_route/auto_route.dart';
import 'package:camera/camera.dart';
import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/utils/extensions/extensions.dart';
import 'package:digit_scanner/utils/i18_key_constants.dart' as i18;
import 'package:digit_scanner/utils/scanner_utils.dart';
import 'package:digit_scanner/widgets/localized.dart';
import 'package:digit_scanner/widgets/vision_detector_views/detector_view.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/TextTheme/digit_text_theme.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_mlkit_barcode_scanning/google_mlkit_barcode_scanning.dart';
import 'package:gs1_barcode_parser/gs1_barcode_parser.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as app_i18;

@RoutePage()
class DigitScannerPage extends LocalizedStatefulWidget {
  final bool? singleValue;
  final int? quantity;
  final bool? isGS1code;
  final bool isEditEnabled;
  final String? regex;
  final List<dynamic>? validations;
  final bool enableGalleryScan;

  const DigitScannerPage({
    super.key,
    super.appLocalizations,
    this.quantity = 1,
    this.isGS1code = false,
    this.singleValue = true,
    this.isEditEnabled = false,
    this.regex,
    this.validations,
    this.enableGalleryScan = true,
  });

  @override
  State<DigitScannerPage> createState() => _CustomDigitScannerPageState();
}

class _CustomDigitScannerPageState extends LocalizedState<DigitScannerPage> {
  final BarcodeScanner _barcodeScanner = BarcodeScanner();
  final ImagePicker _picker = ImagePicker();
  bool _canProcess = true;
  bool _isBusy = false;
  bool _isPickingImage = false;
  CustomPaint? _customPaint;
  String? _text;
  var _cameraLensDirection = CameraLensDirection.back;
  final AudioPlayer player = AudioPlayer();
  CameraController? _cameraController;
  static List<CameraDescription> _cameras = [];
  int _cameraIndex = -1;
  List<GS1Barcode> result = [];
  List<String> codes = [];
  bool manualCode = false;
  bool flashStatus = false;
  final GlobalKey qrKey = GlobalKey(debugLabel: 'QR');
  static const _manualCodeFormKey = 'manualCode';
  static const _manualSerialNoFormKey = 'serialNoCode';
  static const _manualExpiryDateFormKey = 'expiryDate';

  T? _val<T>(String type) {
    final list = widget.validations;
    if (list == null) return null;
    for (final v in list) {
      try {
        if (v is Map<String, dynamic>) {
          if (v['type'] == type) return v['value'] as T?;
        } else {
          final t = (v.type as Object?)?.toString();
          if (t == type) return (v.value as T?);
        }
      } catch (_) {}
    }
    return null;
  }

  bool? _valBool(String type) {
    final raw = _val<dynamic>(type);
    if (raw is bool) return raw;
    if (raw is String) {
      final s = raw.trim().toLowerCase();
      if (s == 'true') return true;
      if (s == 'false') return false;
    }
    return null;
  }

  int? _valInt(String type) {
    final raw = _val<dynamic>(type);
    if (raw is int) return raw;
    if (raw is String) return int.tryParse(raw.trim());
    return null;
  }

  String? _valString(String type) {
    final raw = _val<dynamic>(type);
    if (raw == null) return null;
    return raw.toString().trim().isEmpty ? null : raw.toString().trim();
  }

  String? _valMsg(String type) {
    final list = widget.validations;
    if (list == null) return null;
    for (final v in list) {
      try {
        if (v is Map<String, dynamic>) {
          if (v['type'] == type) return v['message']?.toString();
        } else {
          final t = (v.type as Object?)?.toString();
          if (t == type) return (v.message as Object?)?.toString();
        }
      } catch (_) {}
    }
    return null;
  }

  Map<String, String> _messagesFromValidations() {
    final m = <String, String>{};
    void put(String k, String? v) {
      if (v != null && v.trim().isNotEmpty) m[k] = v;
    }

    put('scanLimit', _valMsg('scanLimit'));
    put('pattern', _valMsg('pattern'));
    put('duplicate', _valMsg('duplicate'));
    put('invalidGS1', _valMsg('invalidGS1'));
    put('scannerFailed', _valMsg('scannerFailed'));
    return m;
  }

  bool get _isGS1 => _valBool('isGS1') ?? widget.isGS1code ?? false;
  int get _scanLimit => _valInt('scanLimit') ?? widget.quantity ?? 1;
  String? get _pattern => _valString('pattern') ?? widget.regex;

  @override
  void initState() {
    initializeCameras();
    if (!widget.isEditEnabled) {
      context.read<DigitScannerBloc>().add(
            DigitScannerEvent.handleScanner(
              barCode: const [],
              qrCode: const [],
              overwrite: true,
              isGS1: _isGS1,
              quantity: _scanLimit,
              regex: _pattern,
              messages: _messagesFromValidations(),
            ),
          );
    }
    super.initState();
  }

  @override
  void dispose() {
    _cameraController?.dispose();
    _barcodeScanner.close();
    super.dispose();
  }

  String _appLocalized(String key, String fallback) {
    final translated = context.translate(key);
    return translated == key ? fallback : translated;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: BlocConsumer<DigitScannerBloc, DigitScannerState>(
        listenWhen: (p, c) => p.errorSeq != c.errorSeq,
        listener: (context, state) {
          final msg = state.error?.trim();
          if (msg != null && msg.isNotEmpty) {
            Toast.showToast(
              context,
              type: ToastType.error,
              message: localizations.translate(msg),
            );
          }
        },
        builder: (context, state) {
          return _cameras.isNotEmpty
              ? !manualCode
                  ? scanWidget(context, theme, textTheme, state)
                  : manualEntryWidget(context, theme, textTheme)
              : const Center(child: CircularProgressIndicator());
        },
      ),
    );
  }

  Future<void> processImage(InputImage inputImage) async {
    final scannerState = context.read<DigitScannerBloc>().state;
    await DigitScannerUtils().processImage(
      context: context,
      inputImage: inputImage,
      canProcess: _canProcess,
      isBusy: _isBusy,
      setBusy: (busy) => mounted ? setState(() => _isBusy = busy) : null,
      setText: (text) => mounted ? setState(() => _text = text) : null,
      updateCustomPaint: (customPaint) => _customPaint = customPaint,
      isGS1code: _isGS1,
      quantity: _scanLimit,
      regex: _pattern,
      result: List<GS1Barcode>.from(scannerState.barCodes),
      handleError: handleErrorWrapper,
      cameraLensDirection: _cameraLensDirection,
      barcodeScanner: _barcodeScanner,
      localizations: localizations,
      validations: _messagesFromValidations(),
    );
  }

  Future<void> handleErrorWrapper(String message) async {
    final scannerState = context.read<DigitScannerBloc>().state;
    final List<dynamic> existing = scannerState.isGS1
        ? List<dynamic>.from(scannerState.barCodes)
        : List<dynamic>.from(scannerState.qrCodes);

    await DigitScannerUtils().handleError(
      context: context,
      message: message,
      player: player,
      result: existing,
      setStateCallback: () {
        setState(() {
          _canProcess = true;
          _isBusy = false;
        });
      },
      localizations: localizations,
    );
  }

  void initializeCameras() async {
    if (_cameras.isEmpty) {
      _cameras = await availableCameras();
    }
    for (var i = 0; i < _cameras.length; i++) {
      if (_cameras[i].lensDirection == _cameraLensDirection) {
        setState(() {
          _cameraIndex = i;
        });
        break;
      }
    }
    if (_cameraIndex == -1) return;
    final camera = _cameras[_cameraIndex];
    _cameraController = CameraController(
      camera,
      ResolutionPreset.high,
      enableAudio: false,
      imageFormatGroup: Platform.isAndroid
          ? ImageFormatGroup.nv21
          : ImageFormatGroup.bgra8888,
    );
  }

  Future<void> _pickAndScanFromGallery() async {
    if (_isPickingImage || _isBusy) return;

    setState(() {
      _isPickingImage = true;
    });

    try {
      final pickedFile = await _picker.pickImage(
        source: ImageSource.gallery,
        requestFullMetadata: false,
      );

      if (pickedFile == null || !mounted) return;

      final inputImage = InputImage.fromFilePath(pickedFile.path);
      final barcodes = await _barcodeScanner.processImage(inputImage);

      if (barcodes.isEmpty) {
        Toast.showToast(
          context,
          type: ToastType.error,
          message: _appLocalized(
            app_i18.scanner.noCodeFoundInPhoto,
            'No code found in photo',
          ),
        );
        return;
      }

      final bloc = context.read<DigitScannerBloc>();

      if (_isGS1) {
        try {
          final data =
              (barcodes.first.rawValue ?? barcodes.first.displayValue)?.trim();
          if (data == null || data.isEmpty) {
            Toast.showToast(
              context,
              type: ToastType.error,
              message: _appLocalized(
                app_i18.scanner.failedToReadPhoto,
                'Failed to read photo',
              ),
            );
            return;
          }

          final parsed = GS1BarcodeParser.defaultParser().parse(data);
          bloc.add(
            DigitScannerEvent.handleScanner(
              barCode: [parsed],
              qrCode: const [],
              isGS1: _isGS1,
              quantity: _scanLimit,
              regex: _pattern,
              messages: _messagesFromValidations(),
            ),
          );
          Navigator.of(context).pop();
          return;
        } catch (_) {
          Toast.showToast(
            context,
            type: ToastType.error,
            message: _appLocalized(
              app_i18.scanner.failedToReadPhoto,
              'Failed to read photo',
            ),
          );
          return;
        }
      }

      final code =
          (barcodes.first.displayValue ?? barcodes.first.rawValue)?.trim() ??
              '';
      if (code.isEmpty) {
        Toast.showToast(
          context,
          type: ToastType.error,
          message: _appLocalized(
            app_i18.scanner.noCodeFoundInPhoto,
            'No code found in photo',
          ),
        );
        return;
      }

      bloc.add(
        DigitScannerEvent.handleScanner(
          barCode: const [],
          qrCode: [code],
          isGS1: _isGS1,
          quantity: _scanLimit,
          regex: _pattern,
          messages: _messagesFromValidations(),
        ),
      );
      Navigator.of(context).pop();
    } catch (_) {
      if (mounted) {
        Toast.showToast(
          context,
          type: ToastType.error,
          message: _appLocalized(
            app_i18.scanner.failedToReadPhoto,
            'Failed to read photo',
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isPickingImage = false;
        });
      }
    }
  }

  FormGroup buildForm() {
    if (_isGS1) {
      return fb.group(<String, Object>{
        _manualCodeFormKey: FormControl<String>(
          validators: [Validators.required],
        ),
        _manualSerialNoFormKey: FormControl<String>(
          validators: [Validators.required],
        ),
        _manualExpiryDateFormKey: FormControl<DateTime>(
          value: DateTime.now(),
          validators: [Validators.required],
        ),
      });
    } else {
      return fb.group(<String, Object>{
        _manualCodeFormKey: FormControl<String>(),
      });
    }
  }

  Widget manualEntryWidget(
    BuildContext context,
    ThemeData theme,
    DigitTextTheme textTheme,
  ) {
    return _isGS1
        ? BlocBuilder<DigitScannerBloc, DigitScannerState>(
            builder: (context, state) {
              return ReactiveFormBuilder(
                form: () => buildForm(),
                builder: (context, form, child) {
                  return ScrollableContent(
                    backgroundColor: theme.colorScheme.onError,
                    header: GestureDetector(
                      onTap: () {
                        setState(() {
                          manualCode = false;
                          initializeCameras();
                        });
                      },
                      child: Align(
                        alignment: Alignment.topRight,
                        child: Icon(
                          Icons.close,
                          color: Theme.of(context).colorTheme.text.primary,
                        ),
                      ),
                    ),
                    footer: Padding(
                      padding: const EdgeInsets.all(spacer4),
                      child: DigitButton(
                        mainAxisSize: MainAxisSize.max,
                        onPressed: () async {
                          form.markAllAsTouched();
                          if (!form.valid) return;

                          final bloc = context.read<DigitScannerBloc>();

                          final barcodeString =
                              DigitScannerUtils().generateGS1Barcode(
                            serialNumber: form
                                .control(_manualSerialNoFormKey)
                                .value
                                .toString()
                                .trim(),
                            expiryDate: form
                                .control(_manualExpiryDateFormKey)
                                .value as DateTime,
                            batchNumber: form
                                .control(_manualCodeFormKey)
                                .value
                                .toString()
                                .trim(),
                          );

                          final parser = GS1BarcodeParser.defaultParser();
                          final parsed = parser.parse(barcodeString);

                          bloc.add(
                            DigitScannerEvent.handleScanner(
                              barCode: [parsed],
                              qrCode: const [],
                              isGS1: _isGS1,
                              quantity: _scanLimit,
                              regex: _pattern,
                              messages: _messagesFromValidations(),
                            ),
                          );

                          setState(() => manualCode = false);
                          initializeCameras();
                        },
                        type: DigitButtonType.primary,
                        size: DigitButtonSize.large,
                        label: localizations
                            .translate(i18.common.coreCommonSubmit),
                      ),
                    ),
                    children: [
                      DigitCard(
                        children: [
                          Align(
                            alignment: Alignment.topLeft,
                            child: Text(
                              localizations
                                  .translate(i18.scanner.enterManualCode),
                              style: textTheme.headingL.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                            ),
                          ),
                          ReactiveWrapperField(
                            formControlName: _manualCodeFormKey,
                            validationMessages: {
                              'required': (object) => localizations
                                  .translate(i18.scanner.batchNoRequired),
                            },
                            builder: (field) {
                              return LabeledField(
                                label: localizations
                                    .translate(i18.scanner.barCodeBatch),
                                capitalizedFirstLetter: false,
                                child: DigitTextFormInput(
                                  errorMessage: field.errorText,
                                  isRequired: true,
                                  onChange: (value) {
                                    form.control(_manualCodeFormKey).value =
                                        value;
                                  },
                                ),
                              );
                            },
                          ),
                          ReactiveWrapperField(
                            formControlName: _manualSerialNoFormKey,
                            validationMessages: {
                              'required': (object) => localizations
                                  .translate(i18.scanner.serialNoRequired),
                            },
                            builder: (field) {
                              return LabeledField(
                                label: localizations
                                    .translate(i18.scanner.barCodeSerial),
                                capitalizedFirstLetter: false,
                                child: DigitTextFormInput(
                                  errorMessage: field.errorText,
                                  isRequired: true,
                                  onChange: (value) {
                                    form.control(_manualSerialNoFormKey).value =
                                        value;
                                  },
                                ),
                              );
                            },
                          ),
                          ReactiveWrapperField(
                            formControlName: _manualExpiryDateFormKey,
                            validationMessages: {
                              'required': (object) => localizations
                                  .translate(i18.scanner.expiryDateRequired),
                            },
                            builder: (field) {
                              return LabeledField(
                                label: localizations
                                    .translate(i18.scanner.barCodeExpiry),
                                capitalizedFirstLetter: false,
                                child: DigitDateFormInput(
                                  firstDate: DateTime.now(),
                                  isRequired: true,
                                  confirmText: localizations
                                      .translate(i18.common.coreCommonOk),
                                  cancelText: localizations
                                      .translate(i18.common.coreCommonCancel),
                                  initialValue: DateFormat('dd/MM/yy')
                                      .format(field.control.value),
                                  readOnly: false,
                                  onChange: (value) {
                                    form
                                            .control(_manualExpiryDateFormKey)
                                            .value =
                                        DateFormat('dd/MM/yyyy').parse(value);
                                  },
                                ),
                              );
                            },
                          ),
                        ],
                      ),
                    ],
                  );
                },
              );
            },
          )
        : BlocBuilder<DigitScannerBloc, DigitScannerState>(
            builder: (context, state) {
              return ReactiveFormBuilder(
                form: () => buildForm(),
                builder: (context, form, child) {
                  return ScrollableContent(
                    backgroundColor: theme.colorScheme.onError,
                    header: GestureDetector(
                      onTap: () {
                        setState(() {
                          manualCode = false;
                          initializeCameras();
                        });
                      },
                      child: Align(
                        alignment: Alignment.topRight,
                        child: Icon(
                          Icons.close,
                          color: Theme.of(context).colorTheme.text.primary,
                        ),
                      ),
                    ),
                    footer: Padding(
                      padding: const EdgeInsets.all(spacer4),
                      child: DigitButton(
                        mainAxisSize: MainAxisSize.max,
                        onPressed: () async {
                          final value = form
                                  .control(_manualCodeFormKey)
                                  .value
                                  ?.toString()
                                  .trim() ??
                              '';
                          if (value.isEmpty) {
                            Toast.showToast(
                              context,
                              type: ToastType.error,
                              message: localizations
                                  .translate(i18.scanner.enterManualCode),
                            );
                            return;
                          }

                          final bloc = context.read<DigitScannerBloc>();
                          bloc.add(
                            DigitScannerEvent.handleScanner(
                              barCode: const [],
                              qrCode: [value],
                              isGS1: _isGS1,
                              quantity: _scanLimit,
                              regex: _pattern,
                              messages: _messagesFromValidations(),
                            ),
                          );

                          setState(() => manualCode = false);
                          initializeCameras();
                        },
                        type: DigitButtonType.primary,
                        size: DigitButtonSize.large,
                        label: localizations
                            .translate(i18.common.coreCommonSubmit),
                      ),
                    ),
                    children: [
                      DigitCard(
                        children: [
                          Align(
                            alignment: Alignment.topLeft,
                            child: Text(
                              localizations
                                  .translate(i18.scanner.enterManualCode),
                              style: textTheme.headingL.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                            ),
                          ),
                          ReactiveWrapperField(
                            formControlName: _manualCodeFormKey,
                            builder: (field) {
                              return InputField(
                                label: localizations
                                    .translate(i18.scanner.resourceCode),
                                errorMessage: field.errorText,
                                isRequired: true,
                                type: InputType.text,
                                onChange: (value) {
                                  form.control(_manualCodeFormKey).value =
                                      value;
                                },
                              );
                            },
                          ),
                        ],
                      ),
                    ],
                  );
                },
              );
            },
          );
  }

  Widget scanWidget(
    BuildContext context,
    ThemeData theme,
    DigitTextTheme textTheme,
    DigitScannerState state,
  ) {
    return Stack(
      children: <Widget>[
        Container(
          width: MediaQuery.of(context).size.width,
          height: MediaQuery.of(context).size.height,
          color: theme.colorScheme.onSurfaceVariant.withOpacity(0.5),
          child: DetectorView(
            cameraController: _cameraController,
            cameras: _cameras,
            title: localizations.translate(i18.scanner.barCodeScannerLabel),
            customPaint: _customPaint,
            text: _text,
            onImage: processImage,
            initialCameraLensDirection: _cameraLensDirection,
            onCameraLensDirectionChanged: (value) =>
                _cameraLensDirection = value,
            onBackButtonPressed: () {
              context.read<DigitScannerBloc>().add(
                    DigitScannerEvent.handleScanner(
                      barCode: const [],
                      qrCode: const [],
                      isGS1: _isGS1,
                      quantity: _scanLimit,
                      regex: _pattern,
                      overwrite: true,
                      messages: _messagesFromValidations(),
                    ),
                  );
              Navigator.of(context).pop();
            },
          ),
        ),
        Positioned(
          top: spacer1 * 1.5,
          left: spacer1,
          child: SizedBox(
            child: InkWell(
              onTap: () async {
                _cameraController?.setFlashMode(
                  flashStatus ? FlashMode.off : FlashMode.torch,
                );
                setState(() {
                  flashStatus = !flashStatus;
                });
              },
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  Icon(
                    flashStatus ? Icons.flashlight_off : Icons.flashlight_on,
                    color: theme.colorScheme.secondary,
                  ),
                  Text(
                    localizations.translate(
                      flashStatus ? i18.scanner.flashOff : i18.scanner.flashOn,
                    ),
                    style: TextStyle(
                      color: theme.colorScheme.secondary,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        Padding(
          padding: const EdgeInsets.only(top: spacer12),
          child: Align(
            alignment: Alignment.topCenter,
            child: Text(
              localizations.translate(i18.scanner.scannerLabel),
              style: TextStyle(
                color: theme.colorScheme.onError,
                fontSize: 16,
              ),
            ),
          ),
        ),
        Center(
          child: overlayForManualEntry(theme, textTheme),
        ),
        renderScannedResource(theme, textTheme, state),
      ],
    );
  }

  Widget overlayForManualEntry(ThemeData theme, DigitTextTheme textTheme) {
    return Align(
      alignment: Alignment.center,
      widthFactor: 2,
      child: Padding(
        padding: const EdgeInsets.only(top: spacer8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Padding(
              padding: const EdgeInsets.only(top: spacer1, bottom: spacer1),
              child: Text(
                localizations.translate(i18.scanner.manualScan),
                style: textTheme.bodyL.copyWith(
                  color: theme.colorTheme.paper.primary,
                ),
              ),
            ),
            GestureDetector(
              onTap: () {
                setState(() {
                  manualCode = true;
                });
              },
              child: Text(
                localizations.translate(i18.scanner.enterManualCode),
                style: textTheme.headingL.copyWith(
                    color: theme.colorTheme.primary.primary1,
                    decoration: TextDecoration.underline,
                    decorationColor: theme.colorTheme.primary.primary1),
              ),
            ),
            if (widget.enableGalleryScan) const SizedBox(height: spacer4),
            if (widget.enableGalleryScan)
              GestureDetector(
                onTap: _pickAndScanFromGallery,
                child: Text(
                  _appLocalized(
                    app_i18.scanner.scanFromPhoto,
                    'Upload from Gallery',
                  ),
                  style: textTheme.headingL.copyWith(
                      color: theme.colorTheme.primary.primary1,
                      decoration: TextDecoration.underline,
                      decorationColor: theme.colorTheme.primary.primary1),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget renderScannedResource(
    ThemeData theme,
    DigitTextTheme textTheme,
    DigitScannerState state,
  ) {
    return Stack(
      children: [
        Positioned(
          bottom: 0,
          width: MediaQuery.of(context).size.width,
          child: DigitCard(
            margin: const EdgeInsets.only(top: spacer1),
            padding:
                const EdgeInsets.fromLTRB(spacer3, spacer1, spacer3, spacer1),
            children: [
              DigitButton(
                label: localizations.translate(i18.common.coreCommonSubmit),
                size: DigitButtonSize.large,
                mainAxisSize: MainAxisSize.max,
                type: DigitButtonType.primary,
                onPressed: () async {
                  final scannedCount =
                      _isGS1 ? state.barCodes.length : state.qrCodes.length;
                  if (scannedCount < _scanLimit) {
                    DigitScannerUtils().buildDialog(
                      context,
                      localizations,
                      _scanLimit,
                    );
                    return;
                  }

                  final bloc = context.read<DigitScannerBloc>();
                  bloc.add(
                    DigitScannerEvent.handleScanner(
                      barCode: state.barCodes,
                      qrCode: state.qrCodes,
                      isGS1: _isGS1,
                      quantity: _scanLimit,
                      regex: _pattern,
                      messages: _messagesFromValidations(),
                    ),
                  );
                  Navigator.of(context).pop();
                },
              ),
            ],
          ),
        ),
        Positioned(
          bottom: (spacer1 * 10),
          height: _isGS1
              ? state.barCodes.length < 3
                  ? (state.barCodes.length * 60) + 80
                  : MediaQuery.of(context).size.height / 3
              : state.qrCodes.length < 2
                  ? ((state.qrCodes.length + 1) * 60)
                  : MediaQuery.of(context).size.height / 4,
          width: MediaQuery.of(context).size.width,
          child: Container(
            width: 100,
            height: 120,
            decoration: BoxDecoration(
              color: theme.colorScheme.onError,
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(spacer1 + 4),
                topRight: Radius.circular(spacer1 + 4),
              ),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              mainAxisSize: MainAxisSize.max,
              children: <Widget>[
                Container(
                  decoration: BoxDecoration(
                    color: theme.colorScheme.onError,
                    borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(spacer2),
                      topRight: Radius.circular(spacer2),
                    ),
                  ),
                  padding: const EdgeInsets.only(
                    bottom: spacer2,
                    top: spacer2,
                    left: spacer3,
                  ),
                  width: MediaQuery.of(context).size.width,
                  child: _isGS1
                      ? Text(
                          '${state.barCodes.length} ${localizations.translate(i18.scanner.resourcesScanned)}',
                          style: textTheme.headingM.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                        )
                      : Text(
                          '${state.qrCodes.length} ${localizations.translate(i18.scanner.resourcesScanned)}',
                          style: textTheme.headingM.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                        ),
                ),
                Expanded(
                  child: ListView.builder(
                    itemCount:
                        _isGS1 ? state.barCodes.length : state.qrCodes.length,
                    itemBuilder: (BuildContext context, int index) {
                      return ListTile(
                        shape: const Border(),
                        title: Container(
                          margin: const EdgeInsets.only(
                            left: spacer1,
                            right: spacer1,
                          ),
                          height: spacer9,
                          decoration: BoxDecoration(
                            color: DigitTheme.instance.colorScheme.surface,
                            border: Border.all(
                              color: DigitTheme.instance.colorScheme.outline,
                              width: 1,
                            ),
                            borderRadius: const BorderRadius.all(
                              Radius.circular(4.0),
                            ),
                          ),
                          padding: const EdgeInsets.all(spacer2),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Flexible(
                                child: Text(
                                  overflow: TextOverflow.ellipsis,
                                  _isGS1
                                      ? (state.barCodes[index].displayValue() ??
                                          'Invalid GS1')
                                      : DigitScannerUtils().trimString(
                                          state.qrCodes[index].toString(),
                                        ),
                                ),
                              ),
                              IconButton(
                                padding: const EdgeInsets.only(bottom: spacer2),
                                icon: Icon(
                                  Icons.delete,
                                  color: theme.colorScheme.error,
                                  size: 24,
                                ),
                                onPressed: () {
                                  final bloc = context.read<DigitScannerBloc>();
                                  if (_isGS1) {
                                    final updated = List<GS1Barcode>.from(
                                      bloc.state.barCodes,
                                    )..removeAt(index);
                                    bloc.add(
                                      DigitScannerEvent.handleScanner(
                                        barCode: updated,
                                        qrCode: const [],
                                        isGS1: _isGS1,
                                        quantity: _scanLimit,
                                        regex: _pattern,
                                        messages: _messagesFromValidations(),
                                        overwrite: true,
                                      ),
                                    );
                                  } else {
                                    final updated = List<String>.from(
                                      bloc.state.qrCodes,
                                    )..removeAt(index);
                                    bloc.add(
                                      DigitScannerEvent.handleScanner(
                                        barCode: const [],
                                        qrCode: updated,
                                        isGS1: _isGS1,
                                        quantity: _scanLimit,
                                        regex: _pattern,
                                        messages: _messagesFromValidations(),
                                        overwrite: true,
                                      ),
                                    );
                                  }
                                },
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
