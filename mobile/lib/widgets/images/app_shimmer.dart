import 'package:flutter/material.dart';
import 'package:shimmer/shimmer.dart';

Widget AppShimmer({height, width}) {
  return Shimmer.fromColors(
    baseColor: Colors.grey[300]!,
    highlightColor: Colors.grey[100]!,
    child: Container(
      height: height is num ? height.toDouble() : 100,
      width: width is num ? width.toDouble() : 100,
      color: Colors.white,
    ),
  );
}
