import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_add_new_asset.dart';
import '../../data/nosql/cache_asset_count.dart';
import '../../data/nosql/cache_asset_detail.dart';
import '../../data/nosql/cache_media_upload.dart';
import '../../data/nosql/cache_specification.dart';
import '../../model/asset_summary/asset_summary.dart';

part 'asset_summary.freezed.dart';

@freezed
class AssetSummaryEvent with _$AssetSummaryEvent {
  const factory AssetSummaryEvent.load({
    required String activityFacilityId,
    required String assetType,
  }) = AssetSummaryEventLoad;
}

@freezed
class AssetSummaryState with _$AssetSummaryState {
  const factory AssetSummaryState.initial() = _Initial;
  const factory AssetSummaryState.loading() = _Loading;
  const factory AssetSummaryState.loaded(AssetSummaryModel summary) = _Loaded;
  const factory AssetSummaryState.error(String message) = _Error;
}

class AssetSummaryBloc extends Bloc<AssetSummaryEvent, AssetSummaryState> {
  final Isar isar;

  AssetSummaryBloc(this.isar) : super(const AssetSummaryState.initial()) {
    on<AssetSummaryEventLoad>(_onLoad);
  }

  Future<void> _onLoad(
    AssetSummaryEventLoad event,
    Emitter<AssetSummaryState> emit,
  ) async {
    emit(const AssetSummaryState.loading());

    try {
      final String activityFacilityId = event.activityFacilityId;
      final String assetType = event.assetType;

      /// 1) Fetch count entry (if any)
      final CacheAssetCount? countEntry = await isar.cacheAssetCounts
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(assetType)
          .findFirst();

      /// 2) Fetch specification entry (if any)
      final CacheSpecification? specEntry = await isar.cacheSpecifications
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(assetType)
          .findFirst();

      /// 3) Fetch asset detail (warranty/brand/model)
      final CacheAssetDetail? detailEntry = await isar.cacheAssetDetails
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(assetType)
          .findFirst();

      final List<CacheAddNewAsset> addedAssets = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(assetType)
          .findAll();

      final List<CacheMediaUpload> mediaEntries = await isar.cacheMediaUploads
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(assetType)
          .findAll();

      print("mediaEntries length ${mediaEntries.length}");

      final summary = AssetSummaryModel(
        countEntry: countEntry,
        specEntry: specEntry,
        detailEntry: detailEntry,
        mediaEntries: mediaEntries,
        addedAssets: addedAssets,
      );

      emit(AssetSummaryState.loaded(summary));
    } catch (e) {
      emit(AssetSummaryState.error(e.toString()));
    }
  }
}
