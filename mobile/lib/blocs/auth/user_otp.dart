import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../repositories/app_init_Repo.dart';
import '../../repositories/authRepo.dart';
import '../../repositories/user_repository.dart'; // your repository

part 'user_otp.freezed.dart';

class UserOtpBloc extends Bloc<UserOtpEvent, UserOtpState> {
  final authRepository = AuthRepository();

  UserOtpBloc() : super(const UserOtpState.initial()) {
    on<_SendOtpEvent>(_onSendOtp);
  }

  FutureOr<void> _onSendOtp(
      _SendOtpEvent event, Emitter<UserOtpState> emit) async {
    emit(const UserOtpState.loading());
    try {
      // Call your repository to send OTP to the given phone number.
      // Adjust return type as needed (e.g., you might return a message or an object).
      final Map<String, String> body = {
        'mobileNumber': event.phone,
        'tenantId': envConfig.variables.tenantId
      };
      await authRepository.sendOtp(body);

      // If repository returns some data (e.g., an OTP ID), include it in success:
      // final otpId = await userRepository.sendOtp(event.phone);
      // emit(UserOtpState.success(otpId: otpId));

      emit(const UserOtpState.success());
    } catch (err) {
      String message = 'Unknown error';
      if (err is Exception) {
        message = err.toString();
      }
      emit(UserOtpState.error(message));
    }
  }
}

@freezed
class UserOtpEvent with _$UserOtpEvent {
  /// Trigger sending an OTP for the given phone number.
  const factory UserOtpEvent.sendOtp({
    required String phone,
  }) = _SendOtpEvent;
}

@freezed
class UserOtpState with _$UserOtpState {
  const factory UserOtpState.initial() = _Initial;
  const factory UserOtpState.loading() = _Loading;
  const factory UserOtpState.success() = _Success;
  const factory UserOtpState.error(String message) = _Error;
}
