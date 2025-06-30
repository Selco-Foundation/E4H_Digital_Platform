// Generated using mason. Do not modify by hand
import 'package:dart_mappable/dart_mappable.dart';
import 'package:isar/isar.dart';

import 'address_type.dart';
import 'locality.dart';

part 'address.g.dart';
part 'address.mapper.dart';

@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class AddressSearchModel with AddressSearchModelMappable {
  final String? id;
  final double? latitude;
  final double? longitude;
  final int? limit;
  final int? offset;
  final double? maxRadius;
  final String? tenantId;

  AddressSearchModel({
    this.id,
    this.latitude,
    this.longitude,
    this.limit,
    this.offset,
    this.maxRadius,
    this.tenantId,
  }) : super();

  @MappableConstructor()
  AddressSearchModel.ignoreDeleted({
    this.id,
    this.latitude,
    this.longitude,
    this.limit,
    this.offset,
    this.maxRadius,
    this.tenantId,
  }) : super();
}

@Embedded()
@MappableClass(ignoreNull: true, discriminatorValue: MappableClass.useAsDefault)
class AddressModel with AddressModelMappable {
  static const schemaName = 'Address';

  String? id;
  String? relatedClientReferenceId;
  String? doorNo;
  double? latitude;
  double? longitude;
  double? locationAccuracy;
  String? addressLine1;
  String? addressLine2;
  String? landmark;
  String? city;
  String? pincode;
  String? buildingName;
  String? street;
  String? boundaryType;
  String? boundary;
  bool? nonRecoverableError;
  String? tenantId;
  int? rowVersion;
  @ignore
  AddressType? type;
  @ignore
  LocalityModel? locality;

  AddressModel({
    this.id,
    this.relatedClientReferenceId,
    this.doorNo,
    this.latitude,
    this.longitude,
    this.locationAccuracy,
    this.addressLine1,
    this.addressLine2,
    this.landmark,
    this.city,
    this.pincode,
    this.buildingName,
    this.street,
    this.boundaryType,
    this.boundary,
    this.nonRecoverableError = false,
    this.tenantId,
    this.rowVersion,
    this.type,
    this.locality,
  }) : super();
}
