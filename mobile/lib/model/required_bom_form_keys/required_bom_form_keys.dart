class RequiredBomFormKeyRule {
  const RequiredBomFormKeyRule({
    required this.schemaName,
    required this.fieldName,
    required this.label,
    required this.message,
    required this.active,
  });

  final String schemaName;
  final String fieldName;
  final String label;
  final String message;
  final bool active;

  factory RequiredBomFormKeyRule.fromJson(Map<String, dynamic> json) {
    return RequiredBomFormKeyRule(
      schemaName: json['schemaName']?.toString() ?? '',
      fieldName: json['fieldName']?.toString() ?? '',
      label: json['label']?.toString() ?? '',
      message: json['message']?.toString() ?? '',
      active: json['active'] is bool ? json['active'] as bool : true,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'schemaName': schemaName,
      'fieldName': fieldName,
      'label': label,
      'message': message,
      'active': active,
    };
  }
}

class RequiredBomFormKeysData {
  const RequiredBomFormKeysData({
    required this.tenantId,
    required this.module,
    required this.systemCode,
    required this.active,
    required this.dialogTitle,
    required this.dialogMessage,
    required this.rules,
  });

  final String tenantId;
  final String module;
  final String systemCode;
  final bool active;
  final String dialogTitle;
  final String dialogMessage;
  final List<RequiredBomFormKeyRule> rules;

  factory RequiredBomFormKeysData.fromJson(Map<String, dynamic> json) {
    final rawRules = json['rules'];
    return RequiredBomFormKeysData(
      tenantId: json['tenantId']?.toString() ?? '',
      module: json['module']?.toString() ?? '',
      systemCode: json['systemCode']?.toString() ?? '',
      active: json['active'] is bool ? json['active'] as bool : true,
      dialogTitle: json['dialogTitle']?.toString() ?? 'Required BOM Details',
      dialogMessage: json['dialogMessage']?.toString() ??
          'Please fill the required BOM details before submitting.',
      rules: rawRules is List
          ? rawRules
              .whereType<Map>()
              .map((item) => RequiredBomFormKeyRule.fromJson(
                    Map<String, dynamic>.from(item),
                  ))
              .toList()
          : const <RequiredBomFormKeyRule>[],
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'tenantId': tenantId,
      'module': module,
      'systemCode': systemCode,
      'active': active,
      'dialogTitle': dialogTitle,
      'dialogMessage': dialogMessage,
      'rules': rules.map((rule) => rule.toJson()).toList(),
    };
  }
}
