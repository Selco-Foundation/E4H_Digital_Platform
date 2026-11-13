import 'package:digit_scanner/blocs/scanner.dart';
import 'package:flutter_test/flutter_test.dart';
// digit_scanner's public GS1 model.
// ignore: depend_on_referenced_packages
import 'package:gs1_barcode_parser/gs1_barcode_parser.dart';

import '../../lib/utils/error_localization.dart';
import '../../lib/utils/scanner_localization.dart';
import '../../lib/utils/scanner_session.dart';

void main() {
  late DigitScannerBloc bloc;
  setUp(() {
    bloc = DigitScannerBloc(const DigitScannerState());
  });
  tearDown(() => bloc.close());

  Future<ScannerSession> start(
      {bool gs1 = false, int limit = 3, String? regex}) async {
    final next = bloc.stream.first;
    bloc.add(DigitScannerEvent.handleScanner(
        overwrite: true,
        isGS1: gs1,
        quantity: limit,
        regex: regex,
        messages: scannerErrorDefaults));
    await next;
    return ScannerSession(
        bloc: bloc, isGS1: gs1, limit: limit, singleValue: true, regex: regex);
  }

  test(
      'QR admissions validate duplicate, pattern and limit before returning accepted state',
      () async {
    final session = await start(limit: 2, regex: r'^CODE\d+$');
    final accepted =
        await session.add(codes: ['CODE1'], messages: scannerErrorDefaults);
    expect(accepted?.qrCodes, ['CODE1']);
    expect(await session.add(codes: ['CODE1'], messages: scannerErrorDefaults),
        isNull);
    expect(bloc.state.error, ErrorKeys.scanDuplicate);
    expect(await session.add(codes: ['bad'], messages: scannerErrorDefaults),
        isNull);
    expect(bloc.state.error, ErrorKeys.scanPattern);
    expect(
        (await session.add(codes: ['CODE2'], messages: scannerErrorDefaults))
            ?.qrCodes,
        ['CODE1', 'CODE2']);
    expect(await session.add(codes: ['CODE3'], messages: scannerErrorDefaults),
        isNull);
    expect(bloc.state.error, ErrorKeys.scanLimit);
    expect(bloc.state.qrCodes, ['CODE1', 'CODE2']);
  });

  test('confirmation pauses admission without revalidating accepted codes',
      () async {
    final session = await start();
    await session.add(codes: ['accepted'], messages: scannerErrorDefaults);
    final sequence = bloc.state.errorSeq;
    expect(session.pause(), isTrue);
    expect(session.canSave(bloc.state), isTrue);
    expect(await session.add(codes: ['late'], messages: scannerErrorDefaults),
        isNull);
    expect(bloc.state.qrCodes, ['accepted']);
    expect(bloc.state.errorSeq, sequence);
    session.resume();
    expect(
        (await session.add(codes: ['next'], messages: scannerErrorDefaults))
            ?.qrCodes,
        ['accepted', 'next']);
    session.close();
    expect(
        await session
            .add(codes: ['after-exit'], messages: scannerErrorDefaults),
        isNull);
  });

  test(
      'concurrent detections and empty detections cannot create duplicate pending admissions',
      () async {
    final session = await start();
    expect(await session.add(codes: ['  '], messages: scannerErrorDefaults),
        isNull);
    final first = session.add(codes: ['one'], messages: scannerErrorDefaults);
    expect(await session.add(codes: ['one'], messages: scannerErrorDefaults),
        isNull);
    expect((await first)?.qrCodes, ['one']);
    expect(bloc.state.error, isNull);
  });

  test(
      'GS1 admissions return validated state and reject duplicates and empty GS1 values',
      () async {
    final session = await start(gs1: true);
    final code =
        GS1BarcodeParser.defaultParser().parse('10BATCH\u001d21SERIAL');
    expect(
        (await session.add(barCodes: [code], messages: scannerErrorDefaults))
            ?.barCodes,
        [code]);
    expect(await session.add(barCodes: [code], messages: scannerErrorDefaults),
        isNull);
    expect(bloc.state.error, ErrorKeys.scanDuplicate);
    final invalid = GS1Barcode(code: code.code, elements: const {});
    expect(
        await session.add(barCodes: [invalid], messages: scannerErrorDefaults),
        isNull);
    expect(bloc.state.error, ErrorKeys.invalidGs1);
    expect(bloc.state.barCodes, [code]);
  });

  test('multiple-value mode requires the configured count', () async {
    final next = bloc.stream.first;
    bloc.add(
        const DigitScannerEvent.handleScanner(overwrite: true, quantity: 2));
    await next;
    final session =
        ScannerSession(bloc: bloc, isGS1: false, limit: 2, singleValue: false);
    expect(session.canSave(bloc.state), isFalse);
    await session.add(codes: ['one'], messages: scannerErrorDefaults);
    expect(session.canSave(bloc.state), isFalse);
    await session.add(codes: ['two'], messages: scannerErrorDefaults);
    expect(session.canSave(bloc.state), isTrue);
  });
}
