import 'package:digit_ui_components/digit_components.dart';
import 'package:flutter/material.dart';

Widget AppNetworkImageError({height, width, color}) {
  return Container(
    height: height is num ? height.toDouble() : 100,
    width: width is num ? width.toDouble() : 100,
    decoration: BoxDecoration(
      color: const Light().primary1,
    ),
    child: Icon(
      Icons.error,
      size: height.toInt() ?? 50,
      color: Light().primary1,
    ),
  );
}
