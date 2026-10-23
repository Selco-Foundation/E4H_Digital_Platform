import 'package:isar/isar.dart';

import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_asset_count.dart';

class AssetSubmissionEligibilityRepository {
  static const assetTypes = ['inverter', 'battery', 'panel'];
  final Isar isar;

  AssetSubmissionEligibilityRepository(this.isar);

  /// [allowZeroAssets] is for rectification sites, where a visit may install
  /// nothing (e.g. wiring only): they qualify when no asset type has a
  /// non-zero count. A type declared with a count still has to be completed.
  Future<bool> hasReadyAssets(String activityFacilityId,
      {bool allowZeroAssets = false}) async {
    if (activityFacilityId.isEmpty) return false;
    final counts = await isar.cacheAssetCounts
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();
    var anyDeclared = false;
    for (final type in assetTypes) {
      final entries = counts
          .where((entry) => entry.assetType.trim().toLowerCase() == type)
          .toList()
        ..sort((a, b) {
          final order = (a.updatedAt ?? a.createdAt)
              .compareTo(b.updatedAt ?? b.createdAt);
          return order != 0 ? order : a.id.compareTo(b.id);
        });
      if (entries.isEmpty || entries.last.count <= 0) continue;
      anyDeclared = true;
      final saved = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .count();
      if (saved > 0) return true;
    }
    return allowZeroAssets && !anyDeclared;
  }

  Future<Map<String, List<CacheAddNewAsset>>> savedAssetsByType(
      String activityFacilityId) async {
    final result = <String, List<CacheAddNewAsset>>{};
    for (final type in assetTypes) {
      final assets = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findAll();
      if (assets.isNotEmpty) result[type] = assets;
    }
    return result;
  }
}
