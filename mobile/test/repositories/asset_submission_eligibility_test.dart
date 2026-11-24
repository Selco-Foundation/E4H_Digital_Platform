import 'package:flutter_test/flutter_test.dart';
import '../../lib/repositories/asset_submission_eligibility_repo.dart';
import '../support/asset_store.dart';

void main() {
  for (final types in [
    ['battery'],
    ['inverter'],
    ['panel'],
    ['battery', 'inverter'],
    ['battery', 'panel'],
    ['inverter', 'panel'],
    ['battery', 'inverter', 'panel'],
  ]) {
    test(
        '${types.join('+')} readiness requires all three types, including retries',
        () async {
      final store = AssetStore();
      store.counts.addAll(types.map((t) => count(t)));
      store.assets.addAll(types.map((t) => asset(t)));
      final repo = AssetSubmissionEligibilityRepository(store);
      expect(await repo.hasReadyAssets('facility'), types.length == 3);
      expect((await repo.savedAssetsByType('facility')).keys.toSet(),
          types.toSet());
      expect(await repo.hasReadyAssets('facility'), types.length == 3);
      expect((await repo.savedAssetsByType('facility')).keys.toSet(),
          types.toSet());
    });
  }
  for (final type in ['battery', 'inverter', 'panel']) {
    for (final missing in ['count', 'positive count', 'saved asset']) {
      test('$type requires $missing even when other types are ready', () async {
        final store = AssetStore();
        for (final current in ['battery', 'inverter', 'panel']) {
          if (current != type || missing != 'count') {
            store.counts.add(count(current,
                quantity:
                    current == type && missing == 'positive count' ? 0 : 1));
          }
          if (current != type || missing != 'saved asset') {
            store.assets.add(asset(current));
          }
        }
        expect(
            await AssetSubmissionEligibilityRepository(store)
                .hasReadyAssets('facility'),
            isFalse);
      });
    }
  }
  test('one saved entry per type qualifies with higher declared quantities',
      () async {
    final store = AssetStore();
    store.counts.addAll(['battery', 'inverter', 'panel']
        .map((type) => count(type, quantity: 3)));
    store.assets.addAll(['battery', 'inverter', 'panel'].map(asset));
    expect(
        await AssetSubmissionEligibilityRepository(store)
            .hasReadyAssets('facility'),
        isTrue);
  });
  test('zero assets, count-only and row-only entries are not ready', () async {
    final store = AssetStore();
    final repo = AssetSubmissionEligibilityRepository(store);
    expect(await repo.hasReadyAssets('facility'), isFalse);
    expect(await repo.savedAssetsByType('facility'), isEmpty);
    store.counts.add(count('battery'));
    expect(await repo.hasReadyAssets('facility'), isFalse);
    store.counts.clear();
    store.assets.add(asset('battery'));
    expect(await repo.hasReadyAssets('facility'), isFalse);
    store.counts.add(count('battery', quantity: 0));
    expect(await repo.hasReadyAssets('facility'), isFalse);
  });
  test('one ready type does not qualify when another type is only started',
      () async {
    final store = AssetStore();
    store.counts.addAll([count('battery'), count('panel')]);
    store.assets.add(asset('battery'));
    expect(
        await AssetSubmissionEligibilityRepository(store)
            .hasReadyAssets('facility'),
        isFalse);
  });
  test('latest count wins by timestamp then id; progress is not a gate',
      () async {
    final store = AssetStore();
    store.assets.addAll(['battery', 'inverter', 'panel'].map(asset));
    store.counts.addAll(['inverter', 'panel'].map(count));
    final old = count('battery')
      ..createdAt = DateTime(2026)
      ..id = 1
      ..progress = 6;
    final latest = count('battery', quantity: 0)
      ..createdAt = DateTime(2026)
      ..id = 2;
    store.counts.addAll([latest, old]);
    final repo = AssetSubmissionEligibilityRepository(store);
    expect(await repo.hasReadyAssets('facility'), isFalse);
    old.updatedAt = DateTime(2026, 2);
    old.progress = 0;
    expect(await repo.hasReadyAssets('facility'), isTrue);
  });
  test('other facilities and other asset types do not qualify', () async {
    final store = AssetStore();
    store.counts.addAll([count('battery', facility: 'other'), count('pump')]);
    store.assets.addAll([asset('battery', facility: 'other'), asset('pump')]);
    final repo = AssetSubmissionEligibilityRepository(store);
    expect(await repo.hasReadyAssets('facility'), isFalse);
    expect(await repo.hasReadyAssets(''), isFalse);
    expect(await repo.savedAssetsByType('facility'), isEmpty);
  });
  test('rectification sites may submit with no assets declared', () async {
    final store = AssetStore();
    final repo = AssetSubmissionEligibilityRepository(store);
    expect(
        await repo.hasReadyAssets('facility', allowZeroAssets: true), isTrue);
    store.counts.add(count('battery', quantity: 0));
    expect(
        await repo.hasReadyAssets('facility', allowZeroAssets: true), isTrue);
    expect(await repo.hasReadyAssets(''), isFalse);
    expect(await repo.hasReadyAssets('', allowZeroAssets: true), isFalse);
  });
  test('rectification sites must still complete a declared asset type',
      () async {
    final store = AssetStore();
    store.counts.addAll([count('panel'), count('battery')]);
    final repo = AssetSubmissionEligibilityRepository(store);
    expect(
        await repo.hasReadyAssets('facility', allowZeroAssets: true), isFalse);
    store.assets.add(asset('panel'));
    expect(
        await repo.hasReadyAssets('facility', allowZeroAssets: true), isTrue);
  });
}
