import 'package:freezed_annotation/freezed_annotation.dart';

part 'asset_count.freezed.dart';
part 'asset_count.g.dart';

@freezed
class AssetCount with _$AssetCount {
  const factory AssetCount({
    required int max,
    required int min,
    required bool active,
    @JsonKey(name: 'asset_type_code') required String assetTypeCode,
  }) = _AssetCount;

  factory AssetCount.fromJson(Map<String, dynamic> json) =>
      _$AssetCountFromJson(json);
}
