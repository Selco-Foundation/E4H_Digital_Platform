import 'package:digit_scanner/blocs/scanner.dart';
// digit_scanner exposes GS1Barcode in its public API.
// ignore: depend_on_referenced_packages
import 'package:gs1_barcode_parser/gs1_barcode_parser.dart';

/// Serializes scanner admissions and stops late camera/gallery results from
/// changing the accepted selection while a confirmation is open or after exit.
class ScannerSession {
  final DigitScannerBloc bloc;
  final bool isGS1;
  final int limit;
  final bool singleValue;
  final String? regex;
  bool _pending = false;
  bool _paused = false;
  bool _closed = false;

  ScannerSession(
      {required this.bloc,
      required this.isGS1,
      required this.limit,
      required this.singleValue,
      this.regex});

  bool get isPending => _pending;
  bool get canAccept => !_pending && !_paused && !_closed;
  int get requiredCount => singleValue ? 1 : limit.clamp(1, 1000);
  int count(DigitScannerState state) =>
      isGS1 ? state.barCodes.length : state.qrCodes.length;
  bool canSave(DigitScannerState state) => count(state) >= requiredCount;

  bool pause() {
    if (!canAccept) return false;
    _paused = true;
    return true;
  }

  void resume() {
    if (!_closed) _paused = false;
  }

  void close() {
    _closed = true;
  }

  Future<DigitScannerState?> add(
      {List<GS1Barcode> barCodes = const [],
      List<String> codes = const [],
      required Map<String, String> messages}) async {
    codes = codes
        .map((code) => code.trim())
        .where((code) => code.isNotEmpty)
        .toList();
    if (!canAccept || (isGS1 ? barCodes.isEmpty : codes.isEmpty)) return null;
    _pending = true;
    final previous = bloc.state;
    // Subscribe before dispatch: failed validation emits an error sequence;
    // successful admission changes the accepted list. Both are real bloc results.
    final next = bloc.stream.firstWhere((state) =>
        state.errorSeq != previous.errorSeq || count(state) != count(previous));
    bloc.add(DigitScannerEvent.handleScanner(
        barCode: barCodes,
        qrCode: codes,
        isGS1: isGS1,
        quantity: limit,
        regex: regex,
        messages: messages));
    try {
      final state = await next;
      if (_closed ||
          _paused ||
          state.errorSeq != previous.errorSeq ||
          state.error != null) {
        return null;
      }
      return state;
    } on StateError {
      // The owning bloc may close while the page is leaving.
      return null;
    } finally {
      _pending = false;
    }
  }
}
