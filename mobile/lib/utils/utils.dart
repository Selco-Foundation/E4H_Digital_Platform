library app_utils;

import 'dart:async';
import 'dart:io';

import 'package:attendance_management/attendance_management.dart'
    as attendance_mappers;
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:digit_data_model/data_model.dart' as data_model;
import 'package:digit_data_model/data_model.init.dart' as data_model_mappers;
import 'package:digit_dss/digit_dss.dart' as dss_mappers;
import 'package:digit_ui_components/digit_components.dart';
import 'package:disable_battery_optimization/disable_battery_optimization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../data/local_store/secure_store/secure_store.dart';

// export 'app_exception.dart';
export 'constants.dart';
// export 'extensions/extensions.dart';

class CustomValidator {
  /// Validates that control's value must be `true`
  static Map<String, dynamic>? requiredMin(
    AbstractControl<dynamic> control,
  ) {
    return control.value == null ||
            control.value.toString().length >= 2 ||
            control.value.toString().trim().isEmpty
        ? null
        : {'required': true};
  }

  static Map<String, dynamic>? validMobileNumber(
    AbstractControl<dynamic> control,
  ) {
    if (control.value == null || control.value.toString().isEmpty) {
      return null;
    }

    const pattern = r'^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\s\./0-9]*$';

    if (RegExp(pattern).hasMatch(control.value.toString())) return null;

    if (control.value.toString().length < 10) return {'mobileNumber': true};

    return {'mobileNumber': true};
  }
}

Future<void> requestDisableBatteryOptimization() async {
  bool isIgnoringBatteryOptimizations =
      await DisableBatteryOptimization.isBatteryOptimizationDisabled ?? false;

  if (!isIgnoringBatteryOptimizations) {
    await DisableBatteryOptimization.showDisableBatteryOptimizationSettings();
  }
}

setBgRunning(bool isBgRunning) async {
  final localSecureStore = LocalSecureStore.instance;
  await localSecureStore.setBackgroundService(isBgRunning);
}

performBackgroundService({
  BuildContext? context,
  required bool stopService,
  required bool isBackground,
}) async {
  final connectivityResult = await (Connectivity().checkConnectivity());

  final isOnline = connectivityResult.firstOrNull == ConnectivityResult.wifi ||
      connectivityResult.firstOrNull == ConnectivityResult.mobile;
  final service = FlutterBackgroundService();
  var isRunning = await service.isRunning();

  if (stopService) {
    if (isRunning) {
      if (!isBackground && context != null && context.mounted) {
        if (context.mounted) {
          Toast.showToast(
            context,
            message: 'Background Service Stopped',
            type: ToastType.error,
          );
        }
      }
    }
  } else {
    if (!isRunning && isOnline) {
      service.startService();
      if (context != null && context.mounted) {
        requestDisableBatteryOptimization();
        Toast.showToast(
          context,
          message: 'Background Service Started',
          type: ToastType.success,
        );
      }
    }
  }
}

String maskString(String input) {
  // Define the character to use for masking (e.g., "*")
  const maskingChar = '*';

  // Create a new string with the same length as the input string
  final maskedString =
      List<String>.generate(input.length, (index) => maskingChar).join();

  return maskedString;
}

Timer makePeriodicTimer(
  Duration duration,
  void Function(Timer timer) callback, {
  bool fireNow = false,
}) {
  var timer = Timer.periodic(duration, callback);
  if (fireNow) {
    callback(timer);
  }

  return timer;
}

final requestData = {
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "age": 30,
      "email": "johndoe@example.com",
      "address": {
        "street": "123 Main Street",
        "city": "New York",
        "state": "NY",
        "zipcode": "10001",
      },
      "orders": [
        {
          "id": 101,
          "product": "Widget A",
          "quantity": 2,
          "price": 10.99,
        },
        {
          "id": 102,
          "product": "Widget B",
          "quantity": 1,
          "price": 19.99,
        },
      ],
    },
    {
      "id": 2,
      "name": "Jane Smith",
      "age": 25,
      "email": "janesmith@example.com",
      "address": {
        "street": "456 Elm Street",
        "city": "Los Angeles",
        "state": "CA",
        "zipcode": "90001",
      },
      "orders": [
        {
          "id": 201,
          "product": "Widget C",
          "quantity": 3,
          "price": 15.99,
        },
        {
          "id": 202,
          "product": "Widget D",
          "quantity": 2,
          "price": 12.99,
        },
      ],
    },
    // ... Repeat the above structure to reach approximately 100KB in size
  ],
};

Future<bool> getIsConnected() async {
  try {
    final result = await InternetAddress.lookup('example.com');
    if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {
      return true;
    }

    return false;
  } on SocketException catch (_) {
    return false;
  }
}

// Existing _findLeastLevelBoundaryCode method remains unchanged
String _findLeastLevelBoundaryCode(List<data_model.BoundaryModel> boundaries) {
  data_model.BoundaryModel? highestBoundary;

  // Find the boundary with the highest boundaryNum
  for (var boundary in boundaries) {
    if (highestBoundary == null ||
        (boundary.boundaryNum ?? 0) > (highestBoundary.boundaryNum ?? 0)) {
      highestBoundary = boundary;
    }
  }

  // If the highest boundary is a leaf node (no children), it is the least-level boundary
  if (highestBoundary?.children.isEmpty ?? true) {
    // Return the boundary type if available, otherwise fallback to the label or an empty string
    return highestBoundary?.boundaryType ?? highestBoundary?.label ?? "";
  }

  // If the highest boundary has children, recursively search in them
  if (highestBoundary?.children != null) {
    for (var child in highestBoundary!.children) {
      String leastCode = _findLeastLevelBoundaryCode(
          [child]); // Recursively find the least level
      if (leastCode.isNotEmpty) {
        return leastCode;
      }
    }
  }

  // If no boundary found
  return "";
}

// Recursive function to find the least level boundary codes
List<String> findLeastLevelBoundaries(
    List<data_model.BoundaryModel> boundaries) {
  // Find the least level boundary type
  String leastLevelType = _findLeastLevelBoundaryCode(boundaries);

  // Initialize a list to store the matching boundary codes with lowest level boundary type
  List<String> leastLevelBoundaryCodes = [];

  // Iterate through the boundaries to find matching codes
  if (leastLevelType.isNotEmpty) {
    for (var boundary in boundaries) {
      // Check if the boundary matches the least-level type and has no children (leaf node)
      if ((boundary.boundaryType == leastLevelType ||
              boundary.label == leastLevelType) &&
          boundary.children.isEmpty) {
        // Found a least level boundary with no children (leaf node), add its code
        leastLevelBoundaryCodes.add(boundary.code!);
      } else if (boundary.children.isNotEmpty) {
        // Recursively search in the children
        List<String> childVillageCodes =
            findLeastLevelBoundaries(boundary.children);
        leastLevelBoundaryCodes.addAll(childVillageCodes);
      }
    }
  }

  // Return the list of matching boundary codes
  return leastLevelBoundaryCodes;
}

List<dss_mappers.DashboardConfigSchema?> filterDashboardConfig(
    List<dss_mappers.DashboardConfigSchema?>? dashboardConfig,
    String projectTypeCode) {
  return dashboardConfig
          ?.where((element) =>
              element != null && element.projectTypeCode == projectTypeCode)
          .toList() ??
      [];
}

initializeAllMappers() async {
  List<Future> initializations = [
    Future(() => data_model_mappers.initializeMappers()),
    Future(() => attendance_mappers.initializeMappers()),
    Future(() => data_model_mappers.initializeMappers()),
    Future(() => dss_mappers.initializeMappers()),
  ];
  await Future.wait(initializations);
}

class LocalizationParams {
  static final LocalizationParams _singleton = LocalizationParams._internal();

  factory LocalizationParams() {
    return _singleton;
  }

  LocalizationParams._internal();

  List<String>? _code;
  String? _module;
  Locale? _locale;
  bool? _exclude = true;

  void setCode(List<String>? code) {
    _code = code;
  }

  void setModule(String? module, bool? exclude) {
    _module = module;
    _exclude = exclude;
  }

  void setLocale(Locale locale) {
    _locale = locale;
  }

  void clear() {
    _code = null;
    _module = null;
  }

  List<String>? get code => _code;

  String? get module => _module;

  Locale? get locale => _locale;

  bool? get exclude => _exclude;
}
