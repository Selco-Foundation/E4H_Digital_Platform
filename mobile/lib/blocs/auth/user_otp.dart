import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../repositories/app_init_repo.dart';
import '../../repositories/auth_repo.dart';

part 'user_otp.freezed.dart';

class UserOtpBloc extends Bloc<UserOtpEvent, UserOtpState> {
  final authRepository = AuthRepository();
  String? _storedPhone;
  String? _storedOtp;

  UserOtpBloc() : super(const UserOtpState.initial()) {
    on<_SendOtpEvent>(_onSendOtp);
    on<_StoreOtpEvent>(_onStoreOtp);
    on<_GetOtpEvent>(_onGetOtp);
    on<_ClearOtpEvent>(_onClearOtp);
    on<_StorePhoneEvent>(_onStorePhone);
    on<_ResetPasswordEvent>(_onResetPassword);
  }

  FutureOr<void> _onSendOtp(
      _SendOtpEvent event, Emitter<UserOtpState> emit) async {
    _storedPhone = event.phone;
    emit(UserOtpState.phoneStored(event.phone));
    emit(const UserOtpState.loading());
    try {
      final Map<String, String> body = {
        'mobileNumber': event.phone,
        'tenantId': envConfig.variables.tenantId
      };
      await authRepository.sendOtp(body);
      emit(const UserOtpState.sent());
    } catch (err) {
      String message = 'Unknown error';
      if (err is Exception) {
        message = err.toString();
      }
      emit(UserOtpState.error(message));
    }
  }

  FutureOr<void> _onStoreOtp(_StoreOtpEvent event, Emitter<UserOtpState> emit) {
    _storedOtp = event.otp;
    emit(UserOtpState.otpStored(event.otp));
  }

  FutureOr<void> _onGetOtp(_GetOtpEvent event, Emitter<UserOtpState> emit) {
    final current = state;
    if (current is _OtpStored) {
      emit(UserOtpState.otpStored(current.otp));
    } else {
      emit(const UserOtpState.error('No OTP stored'));
    }
  }

  FutureOr<void> _onClearOtp(_ClearOtpEvent event, Emitter<UserOtpState> emit) {
    emit(const UserOtpState.initial());
  }

  FutureOr<void> _onStorePhone(
      _StorePhoneEvent event, Emitter<UserOtpState> emit) {
    _storedPhone = event.phone;
    emit(UserOtpState.phoneStored(event.phone));
  }

  FutureOr<void> _onResetPassword(
      _ResetPasswordEvent event, Emitter<UserOtpState> emit) async {
    if (_storedPhone == null) {
      emit(const UserOtpState.error('Mobile number not set'));
      return;
    }
    if (_storedOtp == null) {
      emit(const UserOtpState.error('Otp not set'));
      return;
    }
    emit(const UserOtpState.loading());
    try {
      final body = {
        'userName': _storedPhone!,
        'otpReference': _storedOtp!,
        'newPassword': event.newPassword,
        'tenantId': envConfig.variables.tenantId,
        "type": "EMPLOYEE"
      };
      await authRepository.resetPassword(body);
      emit(const UserOtpState.success());
    } catch (err) {
      String message = 'Unknown error';
      if (err is Exception) {
        message = err.toString().replaceFirst('Exception: ', '');
      }
      emit(UserOtpState.error(message));
    }
  }
}

@freezed
class UserOtpEvent with _$UserOtpEvent {
  const factory UserOtpEvent.sendOtp({
    required String phone,
  }) = _SendOtpEvent;

  const factory UserOtpEvent.storeOtp({
    required String otp,
  }) = _StoreOtpEvent;
  const factory UserOtpEvent.getOtp() = _GetOtpEvent;
  const factory UserOtpEvent.clearOtp() = _ClearOtpEvent;
  const factory UserOtpEvent.storePhone({
    required String phone,
  }) = _StorePhoneEvent;
  const factory UserOtpEvent.resetPassword({
    required String newPassword,
  }) = _ResetPasswordEvent;
}

@freezed
class UserOtpState with _$UserOtpState {
  const factory UserOtpState.initial() = _Initial;
  const factory UserOtpState.loading() = _Loading;

  const factory UserOtpState.sent() = _Sent;
  const factory UserOtpState.otpStored(String otp) = _OtpStored;
  const factory UserOtpState.success() = _Success;
  const factory UserOtpState.error(String message) = _Error;
  const factory UserOtpState.phoneStored(String phone) = _PhoneStored;
}
