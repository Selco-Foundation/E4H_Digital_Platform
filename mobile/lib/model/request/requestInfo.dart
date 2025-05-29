import 'package:freezed_annotation/freezed_annotation.dart';

import '../response/responsemodel.dart';

part 'requestInfo.freezed.dart';
part 'requestInfo.g.dart';

@freezed
class RequestInfoModel with _$RequestInfoModel {
  @JsonSerializable(includeIfNull: false)
  const factory RequestInfoModel(
      {final String? apiId,
      final String? ver,
      final num? ts,
      final String? action,
      final String? did,
      final String? key,
      final String? msgId,
      final String? authToken,
      final UserRequest? userInfo}) = _RequestInfoModel;

  factory RequestInfoModel.fromJson(Map<String, dynamic> json) =>
      _$RequestInfoModelFromJson(json);
}
