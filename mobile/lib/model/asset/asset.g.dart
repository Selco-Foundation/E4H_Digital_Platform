// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'asset.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AssetDetailsImpl _$$AssetDetailsImplFromJson(Map<String, dynamic> json) =>
    _$AssetDetailsImpl(
      totalCapacity: _stringOrNumToDouble(json['totalCapacity']),
      totalCapacityUnit: _anyToString(json['totalCapacityUnit']),
      totalCapacityUOM: _anyToString(json['totalCapacityUOM']),
      capacityUnit: _anyToString(json['capacityUnit']),
      panelCapacity: _stringOrNumToDouble(json['panelCapacity']),
      batteryType: _anyToString(json['batteryType']),
      batteryVoltage: _stringOrNumToDouble(json['batteryVoltage']),
      batteryCapacity: _stringOrNumToDouble(json['batteryCapacity']),
      voltageUnit: _anyToString(json['voltageUnit']),
      inverterCapacity: _stringOrNumToDouble(json['inverterCapacity']),
      inverterCapacityUnit: _anyToString(json['invertorCapacityUnit']),
      currentUnit: _anyToString(json['currentUnit']),
    );

Map<String, dynamic> _$$AssetDetailsImplToJson(_$AssetDetailsImpl instance) =>
    <String, dynamic>{
      'totalCapacity': instance.totalCapacity,
      'totalCapacityUnit': instance.totalCapacityUnit,
      'totalCapacityUOM': instance.totalCapacityUOM,
      'capacityUnit': instance.capacityUnit,
      'panelCapacity': instance.panelCapacity,
      'batteryType': instance.batteryType,
      'batteryVoltage': instance.batteryVoltage,
      'batteryCapacity': instance.batteryCapacity,
      'voltageUnit': instance.voltageUnit,
      'inverterCapacity': instance.inverterCapacity,
      'invertorCapacityUnit': instance.inverterCapacityUnit,
      'currentUnit': instance.currentUnit,
    };

_$AssetImpl _$$AssetImplFromJson(Map<String, dynamic> json) => _$AssetImpl(
      assetId: _anyToString(json['assetId']),
      tenantId: _anyToString(json['tenantId']),
      activityFacilityID: _anyToString(json['activityFacilityId']),
      facilityID: _anyToString(json['facilityID']),
      system: _anyToString(json['system']),
      serialNumber: _anyToString(json['serialNumber']),
      assetTypeID: _anyToString(json['assetTypeID']),
      assetDetails: json['assetDetails'] == null
          ? null
          : AssetDetails.fromJson(json['assetDetails'] as Map<String, dynamic>),
      brandID: _anyToString(json['brandID']),
      modelNumber: _anyToString(json['modelNumber']),
      warrantyStartDate: _anyToString(json['warrantyStartDate']),
      warrantyDuration: (json['warrantyDuration'] as num?)?.toInt(),
      warrantyEndDate: _anyToString(json['warrantyEndDate']),
      wfStatus: _anyToString(json['wfStatus']),
      isActive: json['isActive'] as bool?,
      documents: (json['documents'] as List<dynamic>?)
          ?.map((e) => Document.fromJson(e as Map<String, dynamic>))
          .toList(),
      auditDetails: json['auditDetails'] == null
          ? null
          : AuditDetails.fromJson(json['auditDetails'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$AssetImplToJson(_$AssetImpl instance) =>
    <String, dynamic>{
      'assetId': instance.assetId,
      'tenantId': instance.tenantId,
      'activityFacilityId': instance.activityFacilityID,
      'facilityID': instance.facilityID,
      'system': instance.system,
      'serialNumber': instance.serialNumber,
      'assetTypeID': instance.assetTypeID,
      'assetDetails': instance.assetDetails,
      'brandID': instance.brandID,
      'modelNumber': instance.modelNumber,
      'warrantyStartDate': instance.warrantyStartDate,
      'warrantyDuration': instance.warrantyDuration,
      'warrantyEndDate': instance.warrantyEndDate,
      'wfStatus': instance.wfStatus,
      'isActive': instance.isActive,
      'documents': instance.documents,
      'auditDetails': instance.auditDetails,
    };
