import 'package:flutter_test/flutter_test.dart';
import 'package:selco/repositories/asset_submission_eligibility_repo.dart';
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
        '${types.join('+')} is ready and skips absent types, including retries',
        () async {
      final store = AssetStore();
      store.counts.addAll(types.map((t) => count(t)));
      store.assets.addAll(types.map((t) => asset(t)));
      final repo = AssetSubmissionEligibilityRepository(store);
      expect(await repo.hasReadyAssets('facility'), isTrue);
      expect((await repo.savedAssetsByType('facility')).keys.toSet(),
          types.toSet());
      expect(await repo.hasReadyAssets('facility'), isTrue);
      expect((await repo.savedAssetsByType('facility')).keys.toSet(),
          types.toSet());
    });
  }
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
  test('one ready type qualifies even when another type is only started',
      () async {
    final store = AssetStore();
    store.counts.addAll([count('battery'), count('panel')]);
    store.assets.add(asset('battery'));
    expect(
        await AssetSubmissionEligibilityRepository(store)
            .hasReadyAssets('facility'),
        isTrue);
  });
  test('latest count wins by timestamp then id; progress is not a gate',
      () async {
    final store = AssetStore();
    store.assets.add(asset('battery'));
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
}
