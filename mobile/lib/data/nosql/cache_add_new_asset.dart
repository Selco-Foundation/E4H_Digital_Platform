import 'package:isar/isar.dart';

part 'cache_add_new_asset.g.dart';

@Collection()
class CacheAddNewAsset {
  Id id = Isar.autoIncrement;

  @Index()
  late String projectId;

  @Index()
  late String assetType;

  late String itemNumber; // capacity;
  late String serialNumber;
  late String photoPath;
  late String latitude;
  late String longitude;

  // late String capacityUnit;
  // // inverter
  // late String? outputPhase;
  // late String? chargeControllerCurrent;
  // late String? chargeControllerVoltage;
  // late String? currentUnit;
  //
  // // battery
  // late String? batteryVoltage;
  // late String? voltageUnit;
  // late String? batteryType;
  //
  // // panel

  DateTime createdAt = DateTime.now();
  DateTime? updatedAt;

  CacheAddNewAsset({
    required this.projectId,
    required this.assetType,
    required this.itemNumber, // this.capacity,
    required this.serialNumber,
    required this.photoPath,
    required this.latitude,
    required this.longitude,
  });
}
