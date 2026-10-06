import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../data/network_manager.dart';
import '../../data/secure_storage/secureStore.dart';
import '../../model/dataModel.dart';
import '../../model/login/loginModel.dart';
import '../../model/response/responsemodel.dart';
import '../../repositories/app_init_repo.dart';
import '../../repositories/auth_repo.dart';
import '../../utils/app_logger.dart';
import '../../utils/i18_key_constants.dart' as i18;

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
        scope: 'read',
      ));

      _accesstoken = response.access_token;
      _refreshtoken = response.refresh_token ?? '';
      if (response.userRequest == null) {
        emit(const AuthState.error('Invalid response: missing user data'));
        return;
      }
      _userRequest = response.userRequest!;

      await Future.wait([
        secureStore.setAccessToken(_accesstoken),
        secureStore.setAccessInfo(ResponseModel(
            access_token: _accesstoken,
            token_type: response.token_type,
            refresh_token: _refreshtoken,
            scope: response.scope,
            userRequest: _userRequest)),
      ]);

      await authRepository.reportLogin(_userRequest);

      emit(AuthState.authenticated(
          accesstoken: _accesstoken,
          refreshtoken: _refreshtoken,
          userRequest: _userRequest));
      try {
        const String _ACTION_MASTER = 'actions-test';
        final actionsWrapper = await authRepository.searchRoleActions({
          "roleCodes": response.userRequest?.roles.map((e) => e.code).toList(),
          "tenantId": envConfig.variables.tenantId,
          "actionMaster": _ACTION_MASTER,
          "enabled": true,
        });

        await secureStore.setRoleActions(actionsWrapper);

        secureStore.setSelectedIndividual(_userRequest.userName);
      } catch (e) {
        AppLogger.instance.info(e, title: 'Action Wrapper error');
      }
    } catch (err) {
      emit(AuthState.error(_messageKeyFromError(err)));
    }
  }

  String _messageKeyFromError(Object err) {
    if (err is AppNetworkException) {
      return _keyFromCode(err.code);
    }

    if (err is DioException) {
      final data = err.response?.data;
      if (data is Map<String, dynamic>) {
        final apiErr = (data['error'] ?? '').toString().toLowerCase();
        final desc = (data['error_description'] ?? '').toString().toLowerCase();
        if (apiErr.contains('invalid_grant') ||
            desc.contains('bad credentials')) {
          return i18.login.errorInvalidCredentials;
        }
      }
    }

    return i18.login.errorUnknown;
  }

  String _keyFromCode(LoginErrorCode code) {
    switch (code) {
      case LoginErrorCode.noNetwork:
        return i18.login.errorNoNetwork;
      case LoginErrorCode.noInternet:
        return i18.login.errorNoInternet;
      case LoginErrorCode.connectionFailed:
        return i18.login.errorConnectionFailed;
      case LoginErrorCode.requestTimeout:
        return i18.login.errorRequestTimeout;
      case LoginErrorCode.serverError:
        return i18.login.errorServer;
      case LoginErrorCode.invalidCredentials:
        return i18.login.errorInvalidCredentials;
      case LoginErrorCode.unknown:
        return i18.login.errorUnknown;
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
