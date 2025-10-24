import 'dart:convert';

import 'package:isar/isar.dart';

part 'document.g.dart';

@Embedded()
class GeoLocation {
  String? latitude;
  String? longitude;

  String? additionalDetailsJson;

  GeoLocation({
    this.latitude,
    this.longitude,
    this.additionalDetailsJson,
  });

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
