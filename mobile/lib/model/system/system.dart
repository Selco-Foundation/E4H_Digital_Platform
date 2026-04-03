import 'package:freezed_annotation/freezed_annotation.dart';

part 'system.freezed.dart';
part 'system.g.dart';

@freezed
class System with _$System {
  const factory System({
    required String code,
    required String name,
    required bool active,
  }) = _System;

  factory System.fromJson(Map<String, dynamic> json) => _$SystemFromJson(json);
}

@freezed
class SystemData with _$SystemData {
  const factory SystemData({
    required int id,
    @JsonKey(name: 'System') required List<System> system,
    required String module,
    required String tenantId,
  }) = _SystemData;

  factory SystemData.fromJson(Map<String, dynamic> json) =>
      _$SystemDataFromJson(json);
}
