import 'package:collection/collection.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'installation_images.freezed.dart';
part 'installation_images.g.dart';

@freezed
class InstallationImageSystemType with _$InstallationImageSystemType {
  const factory InstallationImageSystemType({
    required String code,
    required num order,
  }) = _InstallationImageSystemType;

  factory InstallationImageSystemType.fromJson(Map<String, dynamic> json) =>
      _$InstallationImageSystemTypeFromJson(json);
}

@freezed
class InstallationImageItem with _$InstallationImageItem {
  const InstallationImageItem._();

  const factory InstallationImageItem({
    required String code,
    required bool active,
    required String description,
    @JsonKey(name: 'short_title') required String shortTitle,
    @JsonKey(name: 'system_types')
    required List<InstallationImageSystemType> systemTypes,
    @JsonKey(name: 'required_count') required int requiredCount,
  }) = _InstallationImageItem;

  factory InstallationImageItem.fromJson(Map<String, dynamic> json) =>
      _$InstallationImageItemFromJson(json);

  bool get allowMultiples => requiredCount > 1;

  String get requiredLabel => requiredCount == 1
      ? 'Required: 1 image'
      : 'Required: $requiredCount images';

  InstallationImageSystemType? systemTypeEntry(String systemType) =>
      systemTypes.firstWhereOrNull((s) => s.code == systemType);

  String? orderLabel(String systemType) {
    final order = systemTypeEntry(systemType)?.order;
    if (order == null) return null;
    return order == order.truncateToDouble()
        ? order.truncate().toString()
        : order.toString();
  }
}

@freezed
class InstallationImagesData with _$InstallationImagesData {
  const factory InstallationImagesData({
    required int id,
    required String tenantId,
    @JsonKey(name: 'InstallationImage')
    required List<InstallationImageItem> installationImage,
  }) = _InstallationImagesData;

  factory InstallationImagesData.fromJson(Map<String, dynamic> json) =>
      _$InstallationImagesDataFromJson(json);
}
