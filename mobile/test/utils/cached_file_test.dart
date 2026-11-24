import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import '../../lib/utils/utils.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late Directory root;
  late String supportPath;
  late String originalUrl;
  const id = '8f267afc-f270-4c77-97d1-2999dbffdd97';

  setUp(() async {
    root = await Directory.systemTemp.createTemp('filestore-test-');
    supportPath = '${root.path}/support';
    // Set before reading the lazy URL, so no local environment is needed.
    fileStoreFileUrl = 'https://server-one/files?tenantId=one&fileStoreId=';
    originalUrl = fileStoreFileUrl;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
            const MethodChannel('plugins.flutter.io/path_provider'),
            (_) async => supportPath);
  });

  tearDown(() async {
    fileStoreFileUrl = originalUrl;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
            const MethodChannel('plugins.flutter.io/path_provider'), null);
    await root.delete(recursive: true);
  });

  test('download survives loss of its in-memory path and reopens offline',
      () async {
    var requests = 0;
    final file = await http.runWithClient(
        () => getCachedFile(id),
        () => MockClient((_) async {
              requests++;
              return http.Response.bytes([1, 2, 3], 200);
            }));
    expect(await file!.readAsBytes(), [1, 2, 3]);
    expect(requests, 1);
    expect(
        await root
            .list(recursive: true)
            .where((entry) => entry.path.endsWith('.tmp'))
            .isEmpty,
        isTrue);

    // Move the disk cache so the remembered path no longer exists. The helper
    // must discover the persistent file, as it would after a fresh app launch.
    final moved = '${root.path}/reopened';
    await Directory(supportPath).rename(moved);
    supportPath = moved;
    var offlineRequests = 0;
    final reopened = await http.runWithClient(
        () => getCachedFile(id),
        () => MockClient((_) async {
              offlineRequests++;
              throw const SocketException('Offline');
            }));
    expect(reopened, isNotNull);
    expect(await reopened!.readAsBytes(), [1, 2, 3]);
    expect(offlineRequests, 0);
  });

  test('server and tenant each isolate disk and memory caches', () async {
    var requests = 0;
    Future<File?> load() => http.runWithClient(() => getCachedFile(id),
        () => MockClient((_) async => http.Response.bytes([++requests], 200)));
    final first = await load();
    fileStoreFileUrl = 'https://server-two/files?tenantId=one&fileStoreId=';
    final otherServer = await load();
    fileStoreFileUrl = 'https://server-two/files?tenantId=two&fileStoreId=';
    final otherTenant = await load();
    expect(requests, 3);
    expect({first!.path, otherServer!.path, otherTenant!.path}.length, 3);
    expect(await otherTenant.readAsBytes(), [3]);
  });

  test('deleted cached files are retried; failed downloads return null',
      () async {
    final file = await http.runWithClient(() => getCachedFile(id),
        () => MockClient((_) async => http.Response('image', 200)));
    await file!.delete();
    expect(
        await http.runWithClient(() => getCachedFile(id),
            () => MockClient((_) async => http.Response('error', 500))),
        isNull);
    expect(
        await http.runWithClient(
            () => getCachedFile(id),
            () => MockClient(
                (_) async => throw const SocketException('Offline'))),
        isNull);
  });

  test('local draft paths still work and deleted drafts return null', () async {
    final local = File('${root.path}/draft.jpg');
    await local.writeAsBytes([4, 5]);
    expect((await getCachedFile(local.path))!.path, local.path);
    await local.delete();
    expect(await getCachedFile(local.path), isNull);
  });
}
