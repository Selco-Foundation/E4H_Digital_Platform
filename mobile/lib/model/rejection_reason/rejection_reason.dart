class RejectionReasonData {
  const RejectionReasonData({
    required this.code,
    required this.name,
  });

  final String code;
  final String name;

  factory RejectionReasonData.fromJson(Map<String, dynamic> json) {
    return RejectionReasonData(
      code: json['code'] as String,
      name: json['name'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'code': code,
      'name': name,
    };
  }
}
