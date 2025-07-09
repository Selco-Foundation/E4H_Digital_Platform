// import 'package:freezed_annotation/freezed_annotation.dart';
//
// import '../document/document.dart';
//
// part 'asset.freezed.dart';
// part 'asset.g.dart';
//
// double? _stringOrNumToDouble(Object? input) {
//   if (input == null) return null;
//   if (input is num) return input.toDouble();
//   if (input is String) return double.tryParse(input);
//   return null;
// }
//
// String? _anyToString(Object? input) {
//   if (input == null) return null;
//   return input.toString();
// }
//
// Object? _doubleToString(double? d) => d;
//
// @freezed
// class AssetDetails with _$AssetDetails {
//   const factory AssetDetails({
//     @JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
//     double? totalCapacity,
//     @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
//     String? totalCapacityUnit,
//     @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
//     String? totalCapacityUOM,
//
//     // panel-and-battery‑specific
//     @JsonKey(name: 'capacityUnit', fromJson: _anyToString) String? capacityUnit,
//
//     // panel‑specific
//     @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
//     double? panelCapacity,
//
//     // battery‑specific
//     @JsonKey(name: 'batteryType', fromJson: _anyToString) String? batteryType,
//     @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
//     double? batteryVoltage,
//     @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
//     double? batteryCapacity,
//     @JsonKey(name: 'voltageUnit', fromJson: _anyToString) String? voltageUnit,
//
//     // inverter‑specific
//     @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
//     double? inverterCapacity,
//     @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
//     String? inverterCapacityUnit,
//     @JsonKey(name: 'currentUnit', fromJson: _anyToString) String? currentUnit,
//   }) = _AssetDetails;
//
//   factory AssetDetails.fromJson(Map<String, dynamic> json) =>
//       _$AssetDetailsFromJson(json);
// }
//
// @freezed
// class Asset with _$Asset {
//   const factory Asset({
//     @JsonKey(name: 'assetId', fromJson: _anyToString) String? assetId,
//     @JsonKey(name: 'tenantId', fromJson: _anyToString) String? tenantId,
//     @JsonKey(name: 'facilityID', fromJson: _anyToString) String? facilityID,
//     @JsonKey(name: 'system', fromJson: _anyToString) String? system,
//     @JsonKey(name: 'serialNumber', fromJson: _anyToString) String? serialNumber,
//     @JsonKey(name: 'assetTypeID', fromJson: _anyToString) String? assetTypeID,
//     @JsonKey(name: 'assetDetails') AssetDetails? assetDetails,
//     @JsonKey(name: 'brandID', fromJson: _anyToString) String? brandID,
//     @JsonKey(name: 'modelNumber', fromJson: _anyToString) String? modelNumber,
//     @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
//     String? warrantyStartDate,
//     int? warrantyDuration,
//     @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
//     String? warrantyEndDate,
//     @JsonKey(name: 'wfStatus', fromJson: _anyToString) String? wfStatus,
//     @JsonKey(name: 'isActive') bool? isActive,
//     @JsonKey(name: 'documents') List<Document>? documents,
//   }) = _Asset;
//
//   factory Asset.fromJson(Map<String, dynamic> json) => _$AssetFromJson(json);
// }

// lib/model/asset/asset.dart

import 'package:freezed_annotation/freezed_annotation.dart';

import '../document/document.dart';

part 'asset.freezed.dart';
part 'asset.g.dart';

double? _stringOrNumToDouble(Object? v) {
  if (v == null) return null;
  if (v is num) return v.toDouble();
  if (v is String) return double.tryParse(v);
  return null;
}

String? _anyToString(Object? v) => v?.toString();

@freezed
class AssetDetails with _$AssetDetails {
  const factory AssetDetails({
    @JsonKey(name: 'totalCapacity', fromJson: _stringOrNumToDouble)
    double? totalCapacity,
    @JsonKey(name: 'totalCapacityUnit', fromJson: _anyToString)
    String? totalCapacityUnit,
    @JsonKey(name: 'totalCapacityUOM', fromJson: _anyToString)
    String? totalCapacityUOM,
    @JsonKey(name: 'capacityUnit', fromJson: _anyToString) String? capacityUnit,
    @JsonKey(name: 'panelCapacity', fromJson: _stringOrNumToDouble)
    double? panelCapacity,
    @JsonKey(name: 'batteryType', fromJson: _anyToString) String? batteryType,
    @JsonKey(name: 'batteryVoltage', fromJson: _stringOrNumToDouble)
    double? batteryVoltage,
    @JsonKey(name: 'batteryCapacity', fromJson: _stringOrNumToDouble)
    double? batteryCapacity,
    @JsonKey(name: 'voltageUnit', fromJson: _anyToString) String? voltageUnit,
    @JsonKey(name: 'inverterCapacity', fromJson: _stringOrNumToDouble)
    double? inverterCapacity,
    @JsonKey(name: 'invertorCapacityUnit', fromJson: _anyToString)
    String? inverterCapacityUnit,
    @JsonKey(name: 'currentUnit', fromJson: _anyToString) String? currentUnit,
  }) = _AssetDetails;

  factory AssetDetails.fromJson(Map<String, dynamic> json) =>
      _$AssetDetailsFromJson(json);
}

@freezed
class Asset with _$Asset {
  const factory Asset({
    @JsonKey(name: 'assetId', fromJson: _anyToString) String? assetId,
    @JsonKey(name: 'tenantId', fromJson: _anyToString) String? tenantId,
    @JsonKey(name: 'facilityID', fromJson: _anyToString) String? facilityID,
    @JsonKey(name: 'system', fromJson: _anyToString) String? system,
    @JsonKey(name: 'serialNumber', fromJson: _anyToString) String? serialNumber,
    @JsonKey(name: 'assetTypeID', fromJson: _anyToString) String? assetTypeID,
    AssetDetails? assetDetails,
    @JsonKey(name: 'brandID', fromJson: _anyToString) String? brandID,
    @JsonKey(name: 'modelNumber', fromJson: _anyToString) String? modelNumber,
    @JsonKey(name: 'warrantyStartDate', fromJson: _anyToString)
    String? warrantyStartDate,
    int? warrantyDuration,
    @JsonKey(name: 'warrantyEndDate', fromJson: _anyToString)
    String? warrantyEndDate,
    @JsonKey(name: 'wfStatus', fromJson: _anyToString) String? wfStatus,
    bool? isActive,
    List<Document>? documents,
  }) = _Asset;

  factory Asset.fromJson(Map<String, dynamic> json) => _$AssetFromJson(json);
}
