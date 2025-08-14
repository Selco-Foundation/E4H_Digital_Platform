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
//
//     /// We'll store fileStore from either `fileStore` or `fileStoreId`
//     String? fileStore,
//     @JsonKey(name: 'documentUid', fromJson: _anyToString) String? documentUid,
//     Map<String, dynamic>? additionalDetails,
//     @JsonKey(name: 'geoLocation') GeoLocation? geoLocation,
//   }) = _Document;
//
//   const Document._();
//
//   /// Custom fromJson to handle both "fileStore" and "fileStoreId"
//   factory Document.fromJson(Map<String, dynamic> json) {
//     return Document(
//       id: _anyToString(json['id']),
//       documentType: _anyToString(json['documentType']),
//       fileStore: _anyToString(json['fileStore'] ?? json['fileStoreId']),
//       documentUid: _anyToString(json['documentUid']),
//       additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
//       geoLocation: json['geoLocation'] != null
//           ? GeoLocation.fromJson(json['geoLocation'] as Map<String, dynamic>)
//           : null,
//     );
//   }
//
//   /// Default toJson (for asset create)
//   Map<String, dynamic> toJson() {
//     return {
//       'id': id,
//       'documentType': documentType,
//       'fileStore': fileStore,
//       'documentUid': documentUid,
//       'additionalDetails': additionalDetails,
//       'geoLocation': geoLocation?.toJson(),
//     };
//   }
//
//   /// Custom serialization for workflow API
//   Map<String, dynamic> toJsonForWorkflow() {
//     return {
//       'id': id,
//       'documentType': documentType,
//       'fileStoreId': fileStore, // use fileStoreId instead
//       'documentUid': documentUid,
//       'additionalDetails': additionalDetails,
//       'geoLocation': geoLocation?.toJson(),
//     };
//   }
// }

import 'dart:convert';

import 'package:isar/isar.dart';

part 'document.g.dart'; // optional if you want Isar adapters for this file (not required)

@Embedded()
class GeoLocation {
  /// Stored as strings (API may send numbers or strings)
  String? latitude;
  String? longitude;

  /// Arbitrary additionalDetails persisted as JSON string so Isar can store it
  String? additionalDetailsJson;

  GeoLocation({
    this.latitude,
    this.longitude,
    this.additionalDetailsJson,
  });

  /// Isar generator must ignore this getter — use @ignore
  @ignore
  Map<String, dynamic>? get additionalDetails =>
      additionalDetailsJson == null ? null : jsonDecode(additionalDetailsJson!);

  factory GeoLocation.fromJson(Map<String, dynamic> json) {
    String? lat;
    String? lon;
    if (json['latitude'] != null) lat = json['latitude'].toString();
    if (json['longitude'] != null) lon = json['longitude'].toString();
    final addJson = json['additionalDetails'] != null
        ? jsonEncode(json['additionalDetails'])
        : null;
    return GeoLocation(
        latitude: lat, longitude: lon, additionalDetailsJson: addJson);
  }

  Map<String, dynamic> toJson() => {
        'latitude': latitude,
        'longitude': longitude,
        'additionalDetails': additionalDetails,
      };
}

@Embedded()
class Document {
  String? id;
  String? documentType;
  String? fileStore; // canonical fileStore or fileStoreId
  String? documentUid;

  /// Arbitrary additionalDetails persisted as JSON string
  String? additionalDetailsJson;

  @Embedded()
  GeoLocation? geoLocation;

  Document({
    this.id,
    this.documentType,
    this.fileStore,
    this.documentUid,
    this.additionalDetailsJson,
    this.geoLocation,
  });

  @ignore
  Map<String, dynamic>? get additionalDetails =>
      additionalDetailsJson == null ? null : jsonDecode(additionalDetailsJson!);

  factory Document.fromJson(Map<String, dynamic> json) {
    final fileStore = json['fileStore'] ?? json['fileStoreId'];
    final addJson = json['additionalDetails'] != null
        ? jsonEncode(json['additionalDetails'])
        : null;
    return Document(
      id: json['id']?.toString(),
      documentType: json['documentType']?.toString(),
      fileStore: fileStore?.toString(),
      documentUid: json['documentUid']?.toString(),
      additionalDetailsJson: addJson,
      geoLocation: json['geoLocation'] != null
          ? GeoLocation.fromJson(Map<String, dynamic>.from(json['geoLocation']))
          : null,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'documentType': documentType,
        'fileStore': fileStore,
        'documentUid': documentUid,
        'additionalDetails': additionalDetails,
        'geoLocation': geoLocation?.toJson(),
      };

  Map<String, dynamic> toJsonForWorkflow() => {
        'id': id,
        'documentType': documentType,
        'fileStoreId': fileStore,
        'documentUid': documentUid,
        'additionalDetails': additionalDetails,
        'geoLocation': geoLocation?.toJson(),
      };
}
