import 'package:isar/isar.dart';

part 'cache_add_new_asset.g.dart';

@Collection()
class CacheAddNewAsset {
  Id id = Isar.autoIncrement;

  @Index()
  late String activityFacilityId;

  @Index()
  late String assetType;

  @Index(caseSensitive: false)
  String? documentId;

  late String? documentType = "ASSET";
  late String? assetId;
  late String itemNumber;
  late String serialNumber;
  late String photoPath;
  late String latitude;
  late String longitude;

  late String capacity;
  late String? capacityUnit;
  late String? panelCapacity;
  late String? batteryCapacity;
  late String? batteryVoltage;
  late String? batteryType;
  late String? voltageUnit;
  late String? inverterCapacity;
  late String? inverterCapacityUnit;
  late String? currentUnit;

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAddNewAsset({
    required this.activityFacilityId,
    this.assetId,
    this.documentId,
    this.documentType,
    required this.assetType,
    required this.itemNumber,
    required this.serialNumber,
    required this.photoPath,
    required this.latitude,
    required this.longitude,
    this.capacity = '1',
    this.capacityUnit,
    this.panelCapacity,
    this.batteryCapacity,
    this.batteryVoltage,
    this.batteryType,
    this.voltageUnit,
    this.inverterCapacity,
    this.inverterCapacityUnit,
    this.currentUnit,
  });
}
