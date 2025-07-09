// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'document.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$GeoLocationImpl _$$GeoLocationImplFromJson(Map<String, dynamic> json) =>
    _$GeoLocationImpl(
      latitude: _anyToString(json['latitude']),
      longitude: _anyToString(json['longitude']),
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
    );

Map<String, dynamic> _$$GeoLocationImplToJson(_$GeoLocationImpl instance) =>
    <String, dynamic>{
      'latitude': instance.latitude,
      'longitude': instance.longitude,
      'additionalDetails': instance.additionalDetails,
    };

_$DocumentImpl _$$DocumentImplFromJson(Map<String, dynamic> json) =>
    _$DocumentImpl(
      id: _anyToString(json['id']),
      documentType: _anyToString(json['documentType']),
      fileStore: _anyToString(json['fileStore']),
      documentUid: _anyToString(json['documentUid']),
      additionalDetails: json['additionalDetails'] as Map<String, dynamic>?,
      geoLocation: json['geoLocation'] == null
          ? null
          : GeoLocation.fromJson(json['geoLocation'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$DocumentImplToJson(_$DocumentImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'documentType': instance.documentType,
      'fileStore': instance.fileStore,
      'documentUid': instance.documentUid,
      'additionalDetails': instance.additionalDetails,
      'geoLocation': instance.geoLocation,
    };
