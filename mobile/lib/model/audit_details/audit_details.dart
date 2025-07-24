import 'package:freezed_annotation/freezed_annotation.dart';

part 'audit_details.freezed.dart';
part 'audit_details.g.dart';

@freezed
class AuditDetails with _$AuditDetails {
  const factory AuditDetails({
    @JsonKey(fromJson: _anyToString) String? createdBy,
    @JsonKey(fromJson: _anyToString) String? lastModifiedBy,
    @JsonKey(
        name: 'createdTime', fromJson: _intToDateTime, toJson: _dateTimeToInt)
    DateTime? createdTime,
    @JsonKey(
        name: 'lastModified', fromJson: _intToDateTime, toJson: _dateTimeToInt)
    DateTime? lastModified,
  }) = _AuditDetails;

  factory AuditDetails.fromJson(Map<String, dynamic> json) =>
      _$AuditDetailsFromJson(json);
}

DateTime? _intToDateTime(Object? v) {
  if (v is num) return DateTime.fromMillisecondsSinceEpoch(v.toInt());
  if (v is String) {
    final parsed = int.tryParse(v);
    if (parsed != null) return DateTime.fromMillisecondsSinceEpoch(parsed);
  }
  return null;
}

int? _dateTimeToInt(DateTime? dt) => dt?.millisecondsSinceEpoch;

String? _anyToString(Object? v) => v?.toString();
