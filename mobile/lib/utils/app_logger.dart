import 'dart:async';

import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

class AppLogger {
  static AppLogger get instance => _instance;
  static const _instance = AppLogger._();

  static FirebaseAnalytics? _analytics;
  static bool _analyticsEnabled = false;
  static const int _maxParamLength = 100;

  const AppLogger._();

  static Future<void> initAnalytics() async {
    try {
      _analytics = FirebaseAnalytics.instance;
      _analyticsEnabled = true;
    } catch (_) {
      _analyticsEnabled = false;
    }
  }

  void debug(dynamic input, {String? title}) {
    _printMessage(input, title: title, level: Level.CONFIG);
    unawaited(_sendLogEvent(level: 'debug', input: input, title: title));
  }

  void info(dynamic input, {String? title}) {
    _printMessage(input, title: title, level: Level.INFO);
    unawaited(_sendLogEvent(level: 'info', input: input, title: title));
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
      _sendLogEvent(
        level: 'error',
        input: message,
        title: title,
        hasStack: stackTrace != null,
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

  Future<void> _sendLogEvent({
    required String level,
    required dynamic input,
    String? title,
    bool? hasStack,
  }) async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      return;
    }
    if (!_analyticsEnabled || _analytics == null) {
      return;
    }

    try {
      final resolvedTitle = _toAnalyticsValue(title ?? runtimeType.toString());
      final resolvedMessage = _toAnalyticsValue(input?.toString());

      await _analytics!.logEvent(
        name: 'app_log',
        parameters: <String, Object>{
          'level': _toAnalyticsValue(level),
          'title': resolvedTitle,
          'message': resolvedMessage,
          if (hasStack != null) 'has_stack': hasStack ? 1 : 0,
        },
      );
    } catch (_) {
      // Logging should never fail app flows.
    }
  }

  String _toAnalyticsValue(String? value) {
    final sanitized = (value ?? '').replaceAll(RegExp(r'[\u0000-\u001F]'), ' ');
    if (sanitized.length <= _maxParamLength) {
      return sanitized;
    }
    return sanitized.substring(0, _maxParamLength);
  }
}
