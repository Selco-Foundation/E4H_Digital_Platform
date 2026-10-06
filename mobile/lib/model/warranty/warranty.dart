import 'package:freezed_annotation/freezed_annotation.dart';

part 'warranty.freezed.dart';
part 'warranty.g.dart';

@freezed
class Warranty with _$Warranty {
  const factory Warranty({
    required bool active,
    required String duration,
    required String format,
    @JsonKey(name: 'asset_type_code') required String assetTypeCode,
  }) = _Warranty;

  factory Warranty.fromJson(Map<String, dynamic> json) =>
      _$WarrantyFromJson(json);
}

@freezed
class WarrantyData with _$WarrantyData {
  const factory WarrantyData({
    required int id,
    required String module,
    required String tenantId,
    @JsonKey(name: 'WarrantyDuration') required List<Warranty> warrantyDuration,
  }) = _WarrantyData;

  factory WarrantyData.fromJson(Map<String, dynamic> json) =>
      _$WarrantyDataFromJson(json);
}
