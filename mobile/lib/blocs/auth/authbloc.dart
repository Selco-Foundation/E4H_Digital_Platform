import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../data/secure_storage/secureStore.dart';
import '../../model/dataModel.dart';
import '../../model/login/loginModel.dart';
import '../../model/response/responsemodel.dart';
import '../../repositories/app_init_repo.dart';
import '../../repositories/auth_repo.dart';

part 'authbloc.freezed.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  late String _accesstoken;
  late UserRequest _userRequest;
  late String _refreshtoken;
  final authRepository = AuthRepository();

  AuthBloc() : super(const AuthState.unauthenticated()) {
    on<_AuthLoginEvent>(_onLogin);
    on<_AuthLogoutEvent>(_onLogout);
    on<AuthLoadEvent>(_onLoad);
  }

  FutureOr<void> _onLogin(
      _AuthLoginEvent event, Emitter<AuthState> emit) async {
    ResponseModel response;
    final secureStore = SecureStore();
    emit(const AuthState.loading());
    try {
      response = await authRepository.validateLogin(LoginModel(
        username: event.username,
        password: event.password,
        tenantId: envConfig.variables.tenantId,
        grant_type: 'password',
        userType: 'EMPLOYEE',
      ));

      _accesstoken = response.access_token;
      _refreshtoken = response.refresh_token ?? '';
      _userRequest = response.userRequest!;

      secureStore.setAccessToken(_accesstoken);

      secureStore.setAccessInfo(ResponseModel(
          access_token: _accesstoken,
          token_type: response.token_type,
          refresh_token: _refreshtoken,
          scope: response.scope,
          userRequest: _userRequest));

      emit(AuthState.authenticated(
          accesstoken: _accesstoken,
          refreshtoken: _refreshtoken,
          userRequest: _userRequest));

      final actionsWrapper = await authRepository.searchRoleActions({
        "roleCodes": response.userRequest?.roles.map((e) => e.code).toList(),
        "tenantId": envConfig.variables.tenantId,
        "actionMaster": "actions-test",
        "enabled": true,
      });

      await secureStore.setRoleActions(actionsWrapper);

      secureStore.setSelectedIndividual(_userRequest.userName);
    } catch (err) {
      String errorMessage = 'Unknown error occurred';
      if (err is DioException) {
        errorMessage = err.response?.data?['error_description'] ??
            err.response?.data?['error'] ??
            err.message ??
            'Network error occurred';
      } else if (err is Exception) {
        errorMessage = err.toString();
      }
      emit(AuthState.error(errorMessage));
    }
  }

  FutureOr<void> _onLogout(
      _AuthLogoutEvent event, Emitter<AuthState> emit) async {
    await authRepository.logout();
    emit(const AuthState.unauthenticated());
  }

  Future<void> _onLoad(AuthLoadEvent event, Emitter<AuthState> emit) async {
    final secureStore = SecureStore();

    ResponseModel? accessInfo;
    accessInfo = await secureStore.getAccessInfo();

    if (accessInfo != null) {
      if (accessInfo.refresh_token == null || accessInfo.userRequest == null) {
        emit(const AuthState.unauthenticated());
        return;
      }
      _accesstoken = accessInfo.access_token;
      _refreshtoken = accessInfo.refresh_token!;
      _userRequest = accessInfo.userRequest!;

      emit(AuthState.authenticated(
          accesstoken: _accesstoken,
          refreshtoken: _refreshtoken,
          userRequest: _userRequest));
    } else {
      emit(const AuthState.unauthenticated());
    }
  }
}

@freezed
class AuthEvent with _$AuthEvent {
  const factory AuthEvent.login(
          {String? username,
          String? password,
          Map<DataModelType, Map<ApiOperation, String>>? actionMap}) =
      _AuthLoginEvent;
  const factory AuthEvent.logout() = _AuthLogoutEvent;
  const factory AuthEvent.attemptLoad() = AuthLoadEvent;
}

@freezed
class AuthState with _$AuthState {
  const factory AuthState.error(String message) = _ErrorState;
  const factory AuthState.unauthenticated() = _UnauthenticatedState;
  const factory AuthState.authenticated({
    required String accesstoken,
    required String? refreshtoken,
    required UserRequest? userRequest,
  }) = _AuthenticatedState;
  const factory AuthState.loading() = _LoadingState;
}
