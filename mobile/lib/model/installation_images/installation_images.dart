import 'package:freezed_annotation/freezed_annotation.dart';

part 'installation_images.freezed.dart';
part 'installation_images.g.dart';

@freezed
class InstallationImageItem with _$InstallationImageItem {
  const InstallationImageItem._();

  const factory InstallationImageItem({
    required String code,
    required bool active,
    required String description,
    @JsonKey(name: 'required_count') required int requiredCount,
  }) = _InstallationImageItem;

  factory InstallationImageItem.fromJson(Map<String, dynamic> json) =>
      _$InstallationImageItemFromJson(json);

  bool get allowMultiples => requiredCount > 1;

  String get requiredLabel => requiredCount == 1
      ? 'Required: 1 image'
      : 'Required: $requiredCount images';
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
