import 'dart:async';

import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

import 'log_sanitizer.dart';

class AppLogger {
  static AppLogger get instance => _instance;
  static const _instance = AppLogger._();

  static FirebaseCrashlytics? _crashlytics;
  static bool _crashlyticsEnabled = false;

  const AppLogger._();

  static Future<void> initCrashlytics() async {
    try {
      _crashlytics = FirebaseCrashlytics.instance;
      _crashlyticsEnabled = true;
    } catch (_) {
      _crashlyticsEnabled = false;
    }
  }

  @Deprecated('Use initCrashlytics instead.')
  static Future<void> initAnalytics() => initCrashlytics();

  void debug(dynamic input, {String? title}) {
    _printMessage(input, title: title, level: Level.CONFIG);
    unawaited(_sendLog(level: 'debug', input: input, title: title));
  }

  void info(dynamic input, {String? title}) {
    _printMessage(input, title: title, level: Level.INFO);
    unawaited(_sendLog(level: 'info', input: input, title: title));
  }

  void error({
    required String title,
    String? message,
    StackTrace? stackTrace,
  }) {
    _printError(
      message: message,
      title: title,
      stackTrace: stackTrace,
    );

    unawaited(
      _sendError(
        message: message,
        title: title,
        stackTrace: stackTrace,
      ),
    );
  }

  void _printError({
    required String title,
    String? message,
    StackTrace? stackTrace,
  }) {
    if (stackTrace != null) {
      debugPrintStack(
        label: title,
        stackTrace: stackTrace,
      );
      if (message != null) {
        _printMessage(message, title: title, level: Level.SEVERE);
      }
      return;
    }

    _printMessage(
      message,
      title: title,
      level: Level.SEVERE,
    );
  }

  void _printMessage(
    dynamic input, {
    String? title,
    required Level level,
  }) {
    debugPrint(
      [
        '[${level.name.padRight(4, ' ').substring(0, 4)}] ',
        '${(title ?? runtimeType.toString())}\n',
        '${input.toString()}\n',
      ].join(''),
      wrapWidth: 120,
    );
  }

  Future<void> _sendLog({
    required String level,
    required dynamic input,
    String? title,
  }) async {
    final crashlytics = _androidCrashlytics;
    if (crashlytics == null) {
      return;
    }

    try {
      final resolvedTitle = sanitizeRemoteLog(title ?? runtimeType.toString());
      final resolvedMessage = sanitizeRemoteLog(input);
      await crashlytics.log('[$level] $resolvedTitle: $resolvedMessage');
    } catch (_) {
      // Logging should never fail app flows.
    }
  }

  Future<void> _sendError({
    required String title,
    String? message,
    StackTrace? stackTrace,
  }) async {
    final crashlytics = _androidCrashlytics;
    if (crashlytics == null) {
      return;
    }

    try {
      final resolvedTitle = sanitizeRemoteLog(title);
      final resolvedMessage = sanitizeRemoteLog(message);
      await crashlytics.log('[error] $resolvedTitle: $resolvedMessage');
      await crashlytics.recordError(
        StateError(resolvedMessage.isEmpty ? resolvedTitle : resolvedMessage),
        stackTrace ?? StackTrace.current,
        reason: resolvedTitle,
        fatal: false,
      );
    } catch (_) {
      // Logging should never fail app flows.
    }
  }

  FirebaseCrashlytics? get _androidCrashlytics {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      return null;
    }
    if (!_crashlyticsEnabled) {
      return null;
    }
    return _crashlytics;
  }
}
