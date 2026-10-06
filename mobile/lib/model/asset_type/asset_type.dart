import 'package:freezed_annotation/freezed_annotation.dart';

part 'asset_type.freezed.dart';
part 'asset_type.g.dart';

@freezed
class AssetType with _$AssetType {
  const factory AssetType({
    required String code,
    required String name,
    required bool active,
    @JsonKey(name: 'form_fields') required List<FormField> formFields,
  }) = _AssetType;

  factory AssetType.fromJson(Map<String, dynamic> json) =>
      _$AssetTypeFromJson(json);
}

@freezed
class FormField with _$FormField {
  const factory FormField({
    String? key,
    String? name,
    String? system,
    List<String>? options,
    List<String>? types,
  }) = _FormField;

  factory FormField.fromJson(Map<String, dynamic> json) =>
      _$FormFieldFromJson(json);
}

@freezed
class AssetTypeData with _$AssetTypeData {
  const factory AssetTypeData({
    required int id,
    required String module,
    required String tenantId,
    @JsonKey(name: 'AssetType') required List<AssetType> assetType,
  }) = _AssetTypeData;

  factory AssetTypeData.fromJson(Map<String, dynamic> json) =>
      _$AssetTypeDataFromJson(json);
}
