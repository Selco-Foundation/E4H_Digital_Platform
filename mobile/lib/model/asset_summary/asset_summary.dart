import '../../data/nosql/cache_add_new_asset.dart';
import '../../data/nosql/cache_asset_count.dart';
import '../../data/nosql/cache_asset_detail.dart';
import '../../data/nosql/cache_media_upload.dart';
import '../../data/nosql/cache_specification.dart';

/// Holds all cached values for a given projectId + assetType.
class AssetSummaryModel {
  final CacheAssetCount? countEntry;
  final CacheSpecification? specEntry;
  final CacheAssetDetail? detailEntry;
  final List<CacheMediaUpload> mediaEntries;
  final List<CacheAddNewAsset> addedAssets;

  AssetSummaryModel(
      {this.countEntry,
      this.specEntry,
      this.detailEntry,
      required this.mediaEntries,
      required this.addedAssets});
}
