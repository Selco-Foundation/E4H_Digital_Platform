import 'package:freezed_annotation/freezed_annotation.dart';

part 'mdms.freezed.dart';
part 'mdms.g.dart';

@Freezed(genericArgumentFactories: true)
class Mdms<T> with _$Mdms<T> {
  @JsonSerializable(genericArgumentFactories: true)
  const factory Mdms({
    required String id,
    required String tenantId,
    required String schemaCode,
    required String uniqueIdentifier,
    required T data,
    required bool isActive,
    required AuditDetails auditDetails,
  }) = _Mdms<T>;

  // Freezed will now generate a _$MdmsFromJson that takes your T factory
  factory Mdms.fromJson(
    Map<String, dynamic> json,
    T Function(Object? json) fromJsonT,
  ) =>
      _$MdmsFromJson(json, fromJsonT);
}

@Freezed()
class AuditDetails with _$AuditDetails {
  const factory AuditDetails({
    required String createdBy,
    required String lastModifiedBy,
    required int createdTime,
    required int lastModifiedTime,
  }) = _AuditDetails;

  factory AuditDetails.fromJson(Map<String, dynamic> json) =>
      _$AuditDetailsFromJson(json);
}
