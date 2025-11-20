import 'package:freezed_annotation/freezed_annotation.dart';

part 'brand.freezed.dart';
part 'brand.g.dart';

@freezed
class Brand with _$Brand {
  const factory Brand({
    required bool active,
    required String code,
    required String name,
    @JsonKey(name: 'asset_type_code') required String assetTypeCode,
  }) = _Brand;

  factory Brand.fromJson(Map<String, dynamic> json) => _$BrandFromJson(json);
}

@freezed
class BrandData with _$BrandData {
  const factory BrandData({
    required int id,
    @JsonKey(name: 'Brand') required List<Brand> brand,
    required String module,
    required String tenantId,
  }) = _BrandData;

  factory BrandData.fromJson(Map<String, dynamic> json) =>
      _$BrandDataFromJson(json);
}
