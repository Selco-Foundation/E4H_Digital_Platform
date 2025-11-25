import 'package:isar/isar.dart';

part 'workflow_audit_details.g.dart';

@Embedded()
class WorkflowAuditDetails {
  String? createdBy;
  String? lastModifiedBy;
  DateTime? createdTime;
  DateTime? lastModifiedTime;

  WorkflowAuditDetails({
    this.createdBy,
    this.lastModifiedBy,
    this.createdTime,
    this.lastModifiedTime,
  });

  factory WorkflowAuditDetails.fromJson(Map<String, dynamic> json) {
    DateTime? _toDate(Object? v) {
      if (v == null) return null;
      if (v is int) return DateTime.fromMillisecondsSinceEpoch(v);
      if (v is String) {
        final n = int.tryParse(v);
        if (n != null) return DateTime.fromMillisecondsSinceEpoch(n);
      }
      return null;
    }

    return WorkflowAuditDetails(
      createdBy: json['createdBy']?.toString(),
      lastModifiedBy: json['lastModifiedBy']?.toString(),
      createdTime: _toDate(json['createdTime']),
      lastModifiedTime:
          _toDate(json['lastModifiedTime'] ?? json['lastModified']),
    );
  }

  Map<String, dynamic> toJson() => {
        'createdBy': createdBy,
        'lastModifiedBy': lastModifiedBy,
        'createdTime': createdTime?.millisecondsSinceEpoch,
        'lastModifiedTime': lastModifiedTime?.millisecondsSinceEpoch,
      };
}
