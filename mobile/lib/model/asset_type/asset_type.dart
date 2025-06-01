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

  /// Standard JsonSerializable factory
  factory AssetType.fromJson(Map<String, dynamic> json) =>
      _$AssetTypeFromJson(json);
}

@freezed
class FormField with _$FormField {
  const factory FormField({
    /// Present if this is a “normal” form field
    String? key,
    String? name,
    String? system,
    List<String>? options,

    /// Present if this entry is purely a “types” list
    List<String>? types,
  }) = _FormField;

  factory FormField.fromJson(Map<String, dynamic> json) =>
      _$FormFieldFromJson(json);
}
