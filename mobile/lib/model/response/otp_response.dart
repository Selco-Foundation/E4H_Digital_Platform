import 'package:freezed_annotation/freezed_annotation.dart';

part 'otp_response.freezed.dart';
part 'otp_response.g.dart';

@freezed
class SendOtpResponse with _$SendOtpResponse {
  const factory SendOtpResponse({required bool? isSuccessful}) =
      _SendOtpResponse;

  factory SendOtpResponse.fromJson(Map<String, Object?> json) =>
      _$SendOtpResponseFromJson(json);
}
