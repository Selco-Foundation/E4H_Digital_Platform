// import 'package:freezed_annotation/freezed_annotation.dart';
//
// part 'document.freezed.dart';
// part 'document.g.dart';
//
// String? _anyToString(Object? v) {
//   if (v == null) return null;
//   return v.toString();
// }
//
// @freezed
// class GeoLocation with _$GeoLocation {
//   const factory GeoLocation({
//     @JsonKey(fromJson: _anyToString) String? latitude,
//     @JsonKey(fromJson: _anyToString) String? longitude,
//     @JsonKey(name: 'additionalDetails') Map<String, dynamic>? additionalDetails,
//   }) = _GeoLocation;
//
//   factory GeoLocation.fromJson(Map<String, dynamic> json) =>
//       _$GeoLocationFromJson(json);
// }
//
// @freezed
// class Document with _$Document {
//   const factory Document({
//     @JsonKey(name: 'id', fromJson: _anyToString) String? id,
//     @JsonKey(name: 'documentType', fromJson: _anyToString) String? documentType,
//     @JsonKey(name: 'fileStore', fromJson: _anyToString) String? fileStore,
//     @JsonKey(name: 'documentUid', fromJson: _anyToString) String? documentUid,
//     @JsonKey(name: 'additionalDetails') Map<String, dynamic>? additionalDetails,
//     @JsonKey(name: 'geoLocation') GeoLocation? geoLocation,
//   }) = _Document;
//
//   factory Document.fromJson(Map<String, dynamic> json) =>
//       _$DocumentFromJson(json);
// }

// lib/model/document/document.dart

import 'package:freezed_annotation/freezed_annotation.dart';

part 'document.freezed.dart';
part 'document.g.dart';

String? _anyToString(Object? v) => v?.toString();

@freezed
class GeoLocation with _$GeoLocation {
  const factory GeoLocation({
    @JsonKey(fromJson: _anyToString) String? latitude,
    @JsonKey(fromJson: _anyToString) String? longitude,
    Map<String, dynamic>? additionalDetails,
  }) = _GeoLocation;

  factory GeoLocation.fromJson(Map<String, dynamic> json) =>
      _$GeoLocationFromJson(json);
}

@freezed
class Document with _$Document {
  const factory Document({
    @JsonKey(name: 'id', fromJson: _anyToString) String? id,
    @JsonKey(name: 'documentType', fromJson: _anyToString) String? documentType,
    @JsonKey(name: 'fileStore', fromJson: _anyToString) String? fileStore,
    @JsonKey(name: 'documentUid', fromJson: _anyToString) String? documentUid,
    Map<String, dynamic>? additionalDetails,
    GeoLocation? geoLocation,
  }) = _Document;

  factory Document.fromJson(Map<String, dynamic> json) =>
      _$DocumentFromJson(json);
}
