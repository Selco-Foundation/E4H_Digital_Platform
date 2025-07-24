// import 'package:freezed_annotation/freezed_annotation.dart';
//
// part 'document.freezed.dart';
// part 'document.g.dart';
//
// String? _anyToString(Object? v) => v?.toString();
//
// @freezed
// class GeoLocation with _$GeoLocation {
//   const factory GeoLocation({
//     @JsonKey(fromJson: _anyToString) String? latitude,
//     @JsonKey(fromJson: _anyToString) String? longitude,
//     Map<String, dynamic>? additionalDetails,
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
//     Map<String, dynamic>? additionalDetails,
//     @JsonKey(name: 'geoLocation') GeoLocation? geoLocation,
//   }) = _Document;
//
//   factory Document.fromJson(Map<String, dynamic> json) =>
//       _$DocumentFromJson(json);
// }

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

    /// We'll store fileStore from either `fileStore` or `fileStoreId`
    String? fileStore,
    @JsonKey(name: 'documentUid', fromJson: _anyToString) String? documentUid,
    Map<String, dynamic>? additionalDetails,
    @JsonKey(name: 'geoLocation') GeoLocation? geoLocation,
  }) = _Document;

  const Document._();

  /// Custom fromJson to handle both "fileStore" and "fileStoreId"
  factory Document.fromJson(Map<String, dynamic> json) {
    return Document(
      id: _anyToString(json['id']),
      documentType: _anyToString(json['documentType']),
      fileStore: _anyToString(json['fileStore'] ?? json['fileStoreId']),
      documentUid: _anyToString(json['documentUid']),
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
      geoLocation: json['geoLocation'] != null
          ? GeoLocation.fromJson(json['geoLocation'] as Map<String, dynamic>)
          : null,
    );
  }

  /// Default toJson (for asset create)
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'documentType': documentType,
      'fileStore': fileStore,
      'documentUid': documentUid,
      'additionalDetails': additionalDetails,
      'geoLocation': geoLocation?.toJson(),
    };
  }

  /// Custom serialization for workflow API
  Map<String, dynamic> toJsonForWorkflow() {
    return {
      'id': id,
      'documentType': documentType,
      'fileStoreId': fileStore, // use fileStoreId instead
      'documentUid': documentUid,
      'additionalDetails': additionalDetails,
      'geoLocation': geoLocation?.toJson(),
    };
  }
}
