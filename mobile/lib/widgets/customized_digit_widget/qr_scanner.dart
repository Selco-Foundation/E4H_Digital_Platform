import 'dart:async';
import 'dart:io';
import 'dart:math' as math;

import 'package:auto_route/auto_route.dart';
import 'package:camera/camera.dart';
import 'package:digit_scanner/blocs/scanner.dart';
import 'package:digit_scanner/utils/extensions/extensions.dart';
import 'package:digit_scanner/utils/i18_key_constants.dart' as i18;
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
import './scanner_utils.dart';
import '../../utils/error_localization.dart';
import '../../utils/scanner_localization.dart';
import '../../utils/scanner_session.dart';
import 'scanner_error_toast.dart';

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

class _CustomDigitScannerPageState extends State<DigitScannerPage> {
  final BarcodeScanner _barcodeScanner = BarcodeScanner();
  final ImagePicker _picker = ImagePicker();
  bool _canProcess = true;
  bool _isBusy = false;
  bool _isPickingImage = false;
  CustomPaint? _customPaint;
  String? _text;
  var _cameraLensDirection = CameraLensDirection.back;
  CameraController? _cameraController;
  List<CameraDescription> _cameras = [];
  int _cameraIndex = -1;
  List<GS1Barcode> result = [];
  List<String> codes = [];
  bool manualCode = false;
  bool flashStatus = false;
  late final ScannerSession _session;
  bool _dialogOpen = false;
  bool _closing = false;
  String? _errorKey;
  Timer? _errorTimer;
  String? _cameraErrorKey;
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
    return {
      for (final type in scannerErrorDefaults.keys)
        type: scannerValidationKey(type, _valMsg(type), context.translate),
    };
  }

  bool get _isGS1 => _valBool('isGS1') ?? widget.isGS1code ?? false;
  int get _scanLimit => _valInt('scanLimit') ?? widget.quantity ?? 1;
  String? get _pattern => _valString('pattern') ?? widget.regex;
  bool get _isSingleValue => widget.singleValue ?? true;

  void _completeSelection(DigitScannerState state) {
    if (_closing || !_session.canSave(state)) return;
    _closing = true;
    _session.close();
    _errorTimer?.cancel();
    final result = _isGS1
        ? state.barCodes
        : (state.qrCodes.isEmpty ? null : state.qrCodes.last);
    Navigator.of(context).pop(result);
  }

  @override
  void initState() {
    super.initState();
    _session = ScannerSession(
        bloc: context.read<DigitScannerBloc>(),
        isGS1: _isGS1,
        limit: _scanLimit,
        singleValue: _isSingleValue,
        regex: _pattern);
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
              messages: scannerErrorDefaults,
            ),
          );
    }
  }

  @override
  void dispose() {
    _session.close();
    _errorTimer?.cancel();
    _cameraController?.dispose();
    _barcodeScanner.close();
    super.dispose();
  }

  void _dismissError() {
    _errorTimer?.cancel();
    if (mounted && _errorKey != null) setState(() => _errorKey = null);
  }

  void _showError(String message) {
    if (!mounted || _dialogOpen || _closing) return;
    final key = scannerErrorKey(message, context.translate);
    if (_errorKey == key) return;
    _errorTimer?.cancel();
    setState(() => _errorKey = key);
    _errorTimer = Timer(const Duration(seconds: 5), _dismissError);
  }

  Future<void> _confirm({bool fromManual = false}) async {
    if (!mounted || _dialogOpen || _closing || _session.isPending) return;
    final state = context.read<DigitScannerBloc>().state;
    if (!_isSingleValue && !fromManual && _session.canSave(state)) {
      _completeSelection(state);
      return;
    }
    if (!_session.pause()) return;
    _dismissError();
    setState(() {
      _dialogOpen = true;
      _canProcess = false;
    });
    try {
      final action = await DigitScannerUtils().buildDialog(context,
          scannedCount: _session.count(state),
          requiredCount: _session.requiredCount);
      if (!mounted || _closing) return;
      if (action == ScannerDialogAction.saveAndClose) {
        _completeSelection(context.read<DigitScannerBloc>().state);
      } else if (fromManual) {
        _resumeCamera();
      }
    } finally {
      if (mounted && !_closing) {
        _session.resume();
        setState(() {
          _dialogOpen = false;
          _canProcess = true;
        });
      }
    }
  }

  Future<void> _acceptDetected(
      List<GS1Barcode> barCodes, List<String> codes) async {
    if (!_canProcess || manualCode || _isPickingImage) return;
    await _session.add(
        barCodes: barCodes, codes: codes, messages: _messagesFromValidations());
  }

  Future<void> _submitManual(FormGroup form) async {
    if (!_session.canAccept || _dialogOpen || _closing) return;
    form.markAllAsTouched();
    if (!form.valid) return;
    final value =
        form.control(_manualCodeFormKey).value?.toString().trim() ?? '';
    if (value.isEmpty ||
        (_isGS1 &&
            (form
                    .control(_manualSerialNoFormKey)
                    .value
                    ?.toString()
                    .trim()
                    .isEmpty ??
                true))) {
      _showError(_isGS1 ? ErrorKeys.invalidGs1 : ErrorKeys.requiredField);
      return;
    }
    DigitScannerState? accepted;
    try {
      final barCodes = _isGS1
          ? [
              GS1BarcodeParser.defaultParser().parse(DigitScannerUtils()
                  .generateGS1Barcode(
                      serialNumber: form
                          .control(_manualSerialNoFormKey)
                          .value
                          .toString()
                          .trim(),
                      expiryDate: form.control(_manualExpiryDateFormKey).value
                          as DateTime,
                      batchNumber: value))
            ]
          : <GS1Barcode>[];
      accepted = await _session.add(
          barCodes: barCodes,
          codes: _isGS1 ? const [] : [value],
          messages: _messagesFromValidations());
    } catch (_) {
      _showError(_isGS1 ? ErrorKeys.invalidGs1 : ErrorKeys.scannerFailed);
    }
    if (accepted != null && mounted) await _confirm(fromManual: true);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return PopScope(
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) return;
        _closing = true;
        _session.close();
        _dismissError();
      },
      child: Scaffold(
        body: BlocConsumer<DigitScannerBloc, DigitScannerState>(
          listenWhen: (p, c) => p.errorSeq != c.errorSeq,
          listener: (context, state) {
            final msg = state.error?.trim();
            if (msg != null && msg.isNotEmpty) {
              _showError(msg);
            }
          },
          builder: (context, state) {
            return SafeArea(
                top: false,
                child: Stack(fit: StackFit.expand, children: [
                  if (manualCode)
                    manualEntryWidget(context, theme, textTheme)
                  else if (_cameraErrorKey != null)
                    Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(context.translate(_cameraErrorKey!)),
                          Expanded(
                              child: overlayForManualEntry(theme, textTheme))
                        ])
                  else if (_cameras.isNotEmpty && _cameraController != null)
                    scanWidget(context, theme, textTheme, state)
                  else
                    const Center(child: CircularProgressIndicator()),
                  if (_errorKey != null && !_dialogOpen)
                    Positioned(
                        left: 12,
                        right: 12,
                        bottom: 92,
                        child: ScannerErrorToast(
                            messageKey:
                                scannerErrorKey(_errorKey!, context.translate),
                            onDismiss: _dismissError)),
                ]));
          },
        ),
      ),
    );
  }

  Future<void> processImage(InputImage inputImage) async {
    await DigitScannerUtils().processImage(
      context: context,
      inputImage: inputImage,
      canProcess: _canProcess,
      isBusy: _isBusy,
      setBusy: (busy) => mounted ? setState(() => _isBusy = busy) : null,
      setText: (text) => mounted ? setState(() => _text = text) : null,
      updateCustomPaint: (customPaint) => _customPaint = customPaint,
      isGS1code: _isGS1,
      handleError: handleErrorWrapper,
      cameraLensDirection: _cameraLensDirection,
      barcodeScanner: _barcodeScanner,
      onDetected: _acceptDetected,
    );
  }

  Future<void> handleErrorWrapper(String message) async {
    _showError(message);
  }

  void _resumeCamera() {
    setState(() {
      manualCode = false;
      _cameraController = null;
      _canProcess = !_dialogOpen;
    });
    initializeCameras();
  }

  Future<void> initializeCameras() async {
    try {
      _cameras = await availableCameras();
      if (!mounted) return;
      if (_cameras.isEmpty) {
        setState(() => _cameraErrorKey = ErrorKeys.cameraStart);
        return;
      }
      _cameraIndex = _cameras
          .indexWhere((camera) => camera.lensDirection == _cameraLensDirection);
      if (_cameraIndex < 0) _cameraIndex = 0;
      final controller = CameraController(
          _cameras[_cameraIndex], ResolutionPreset.high,
          enableAudio: false,
          imageFormatGroup: Platform.isAndroid
              ? ImageFormatGroup.nv21
              : ImageFormatGroup.bgra8888);
      setState(() {
        _cameraController = controller;
        _cameraErrorKey = null;
      });
    } catch (_) {
      if (mounted) setState(() => _cameraErrorKey = ErrorKeys.cameraStart);
    }
  }

  Future<void> _pickAndScanFromGallery() async {
    if (_isPickingImage || !_session.canAccept || _dialogOpen || _closing) {
      return;
    }

    setState(() {
      _canProcess = false;
      _isBusy = false;
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
        _showError(app_i18.scanner.noCodeFoundInPhoto);
        return;
      }

      if (!mounted || _dialogOpen || _closing || manualCode) return;

      if (_isGS1) {
        final parser = GS1BarcodeParser.defaultParser();
        final parsedBarcodes = <GS1Barcode>[];

        for (final barcode in barcodes) {
          final data = (barcode.rawValue ?? barcode.displayValue)?.trim();
          if (data == null || data.isEmpty) continue;

          try {
            parsedBarcodes.add(parser.parse(data));
          } catch (_) {
            // Ignore unreadable detections and keep valid GS1 values from the same image.
          }
        }

        if (parsedBarcodes.isEmpty) {
          _showError(app_i18.scanner.failedToReadPhoto);
          return;
        }

        await _session.add(
            barCodes: parsedBarcodes, messages: _messagesFromValidations());
        return;
      }

      final codes = barcodes
          .map((barcode) => (barcode.displayValue ?? barcode.rawValue)?.trim())
          .whereType<String>()
          .where((code) => code.isNotEmpty)
          .toList(growable: false);

      if (codes.isEmpty) {
        _showError(app_i18.scanner.failedToReadPhoto);
        return;
      }

      await _session.add(codes: codes, messages: _messagesFromValidations());
    } catch (_) {
      if (mounted) {
        _showError(app_i18.scanner.failedToReadPhoto);
      }
    } finally {
      if (mounted) {
        setState(() {
          _canProcess = !_dialogOpen && !_closing;
          _isBusy = false;
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
        _manualCodeFormKey:
            FormControl<String>(validators: [Validators.required]),
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
                        _resumeCamera();
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
                        capitalizeLetters: false,
                        mainAxisSize: MainAxisSize.max,
                        onPressed: () async {
                          await _submitManual(form);
                        },
                        type: DigitButtonType.primary,
                        size: DigitButtonSize.large,
                        label: context.translate(i18.common.coreCommonSubmit),
                      ),
                    ),
                    children: [
                      DigitCard(
                        children: [
                          Align(
                            alignment: Alignment.topLeft,
                            child: Text(
                              context.translate(i18.scanner.enterManualCode),
                              style: textTheme.headingL.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                            ),
                          ),
                          ReactiveWrapperField(
                            formControlName: _manualCodeFormKey,
                            validationMessages: {
                              'required': (object) => context
                                  .translate(i18.scanner.batchNoRequired),
                            },
                            builder: (field) {
                              return LabeledField(
                                label:
                                    context.translate(i18.scanner.barCodeBatch),
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
                              'required': (object) => context
                                  .translate(i18.scanner.serialNoRequired),
                            },
                            builder: (field) {
                              return LabeledField(
                                label: context
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
                              'required': (object) => context
                                  .translate(i18.scanner.expiryDateRequired),
                            },
                            builder: (field) {
                              return LabeledField(
                                label: context
                                    .translate(i18.scanner.barCodeExpiry),
                                capitalizedFirstLetter: false,
                                child: DigitDateFormInput(
                                  firstDate: DateTime.now(),
                                  isRequired: true,
                                  confirmText: context
                                      .translate(i18.common.coreCommonOk),
                                  cancelText: context
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
                        _resumeCamera();
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
                        capitalizeLetters: false,
                        mainAxisSize: MainAxisSize.max,
                        onPressed: () async {
                          await _submitManual(form);
                        },
                        type: DigitButtonType.primary,
                        size: DigitButtonSize.large,
                        label: context.translate(i18.common.coreCommonSubmit),
                      ),
                    ),
                    children: [
                      DigitCard(
                        children: [
                          Align(
                            alignment: Alignment.topLeft,
                            child: Text(
                              context.translate(i18.scanner.enterManualCode),
                              style: textTheme.headingL.copyWith(
                                color: theme.colorTheme.text.primary,
                              ),
                            ),
                          ),
                          ReactiveWrapperField(
                            formControlName: _manualCodeFormKey,
                            validationMessages: {
                              'required': (_) =>
                                  context.translate(ErrorKeys.requiredField)
                            },
                            builder: (field) {
                              return InputField(
                                label:
                                    context.translate(i18.scanner.resourceCode),
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
            title: context.translate(i18.scanner.barCodeScannerLabel),
            customPaint: _customPaint,
            text: _text,
            onImage: processImage,
            initialCameraLensDirection: _cameraLensDirection,
            onCameraLensDirectionChanged: (value) =>
                _cameraLensDirection = value,
            onBackButtonPressed: () {
              _closing = true;
              _session.close();
              _canProcess = false;
              _dismissError();
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
                try {
                  await _cameraController?.setFlashMode(
                      flashStatus ? FlashMode.off : FlashMode.torch);
                  if (mounted) setState(() => flashStatus = !flashStatus);
                } catch (_) {
                  _showError(ErrorKeys.cameraStart);
                }
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
                    context.translate(
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
              context.translate(i18.scanner.scannerLabel),
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
                context.translate(i18.scanner.manualScan),
                style: textTheme.bodyL.copyWith(
                  color: _cameraErrorKey == null
                      ? theme.colorTheme.paper.primary
                      : theme.colorTheme.text.primary,
                ),
              ),
            ),
            GestureDetector(
              onTap: () {
                setState(() {
                  manualCode = true;
                  _canProcess = false;
                });
              },
              child: Text(
                context.translate(i18.scanner.enterManualCode),
                style: textTheme.headingL.copyWith(
                    color: theme.colorTheme.primary.primary1,
                    decoration: TextDecoration.underline,
                    decorationColor: theme.colorTheme.primary.primary1),
              ),
            ),
            if (widget.enableGalleryScan) const SizedBox(height: spacer4),
            if (widget.enableGalleryScan)
              InkWell(
                onTap: _pickAndScanFromGallery,
                child: Text(
                  context.translate(app_i18.scanner.scanFromPhoto),
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
    final label = scannedCountLabel(_session.count(state), context.translate);
    final labelStyle =
        textTheme.headingM.copyWith(color: theme.colorTheme.text.primary);
    final labelLayout = TextPainter(
        text: TextSpan(text: label, style: labelStyle),
        textDirection: Directionality.of(context),
        textScaler: MediaQuery.textScalerOf(context))
      ..layout(
          maxWidth: math.max(1, MediaQuery.sizeOf(context).width - spacer6));
    final headerHeight = labelLayout.height + spacer4;
    labelLayout.dispose();
    final panelHeight = math.max(
        headerHeight,
        math.min(headerHeight + _session.count(state) * 76,
            MediaQuery.sizeOf(context).height / 3));
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
                capitalizeLetters: false,
                label: context.translate(i18.common.coreCommonSubmit),
                size: DigitButtonSize.large,
                mainAxisSize: MainAxisSize.max,
                type: DigitButtonType.primary,
                onPressed: () async {
                  await _confirm();
                },
              ),
            ],
          ),
        ),
        Positioned(
          bottom: (spacer1 * 10),
          height: panelHeight,
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
                  child: Text(label, style: labelStyle),
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
                                          context
                                              .translate(ErrorKeys.invalidGs1))
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
