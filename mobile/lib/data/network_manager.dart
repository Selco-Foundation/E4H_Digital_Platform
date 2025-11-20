import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:internet_connection_checker_plus/internet_connection_checker_plus.dart';

class NetworkService {
  static final NetworkService _i = NetworkService._();
  NetworkService._();
  factory NetworkService() => _i;

  final Connectivity _connectivity = Connectivity();
  final InternetConnection _internet = InternetConnection();

  /// Returns true if any network interface is up (Wi-Fi/cellular).
  Future<bool> hasNetwork() async {
    final result = await _connectivity.checkConnectivity();
    return result != ConnectivityResult.none;
  }

  /// Returns true if the internet is reachable (opens sockets to known hosts).
  Future<bool> hasInternet(
      {Duration timeout = const Duration(seconds: 3)}) async {
    return _internet.hasInternetAccess.timeout(timeout, onTimeout: () => false);
  }

  Future<void> ensureOnlineOrThrow() async {
    final hasIface = await hasNetwork();
    if (!hasIface) {
      throw const NetworkException('No network connection');
    }
    final reachable = await hasInternet();
    if (!reachable) {
      throw const NetworkException('No internet access');
    }
  }
}

class NetworkException implements Exception {
  final String message;
  const NetworkException(this.message);
  @override
  String toString() => message;
}
