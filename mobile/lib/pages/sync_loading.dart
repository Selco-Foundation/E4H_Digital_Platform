import 'dart:ui';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';

@RoutePage()
class SyncLoadingPage extends StatelessWidget {
  final int completed;
  final int total;
  const SyncLoadingPage(
      {super.key, required this.completed, required this.total});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final percent = ((completed / total) * 100).clamp(0, 100).toInt();
    final progress = (completed / total).clamp(0.0, 1.0);
    final isSuccessful = percent >= 98;

    return Scaffold(
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.transparent,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer2, horizontal: spacer6),
            child: Column(
              children: [
                SizedBox(height: context.height * 0.15),
                CloudProgressIndicator(
                  progress: progress,
                  size: const Size(120, 90),
                  strokeWidth: 3,
                  baseColor: theme.colorTheme.alert.infoBg,
                  //theme.colorTheme.text.disabled, // light gray
                  progressColor:
                      theme.colorTheme.primary.primary1, // your brand
                ),
                SizedBox(height: context.height * 0.03),
                Text(
                  isSuccessful ? "Download Successful!" : "Downloading data",
                  style: textTheme.headingS.copyWith(
                      color: isSuccessful
                          ? const Light().alertSuccess
                          : const Light().primary2),
                ),
                const SizedBox(height: spacer6),
                LinearProgressIndicator(
                  borderRadius: BorderRadius.circular(spacer2),
                  backgroundColor: theme.colorTheme.generic.background,
                  valueColor: AlwaysStoppedAnimation<Color>(
                    theme.colorTheme.alert.success,
                  ),
                  value: percent / 100,
                  minHeight: spacer3,
                ),
                const SizedBox(height: spacer2),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      isSuccessful ? "Completed" : "Download in progress...",
                      style: textTheme.bodyS
                          .copyWith(color: const Light().textDisabled),
                    ),
                    Text(
                      "$percent/100",
                      style: textTheme.headingS
                          .copyWith(color: const Light().primary2),
                    ),
                  ],
                )
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class CloudProgressIndicator extends StatelessWidget {
  /// 0.0 → 1.0
  final double progress;
  final double strokeWidth;
  final Color baseColor;
  final Color progressColor;
  final Size size;

  const CloudProgressIndicator({
    Key? key,
    required this.progress,
    this.strokeWidth = 6.0,
    this.baseColor = const Color(0xFFE0E0E0),
    this.progressColor = const Color(0xFF4CAF50),
    this.size = const Size(200, 120),
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: size,
      painter: _CloudPainter(
        progress: progress.clamp(0.0, 1.0),
        strokeWidth: strokeWidth,
        baseColor: baseColor,
        progressColor: progressColor,
      ),
    );
  }
}

class _CloudPainter extends CustomPainter {
  final double progress;
  final double strokeWidth;
  final Color baseColor;
  final Color progressColor;

  _CloudPainter({
    required this.progress,
    required this.strokeWidth,
    required this.baseColor,
    required this.progressColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;

    // Create cloud path with gap at bottom center
    final path = Path()
      // Start at bottom left of cloud
      ..moveTo(w * 0.15, h * 0.65)

      // Curve 1: Left side curve
      ..cubicTo(
        w * 0.05, h * 0.65, // Control point 1
        w * 0.05, h * 0.45, // Control point 2
        w * 0.20, h * 0.40, // End point
      )

      // Curve 2: Transition to large hump
      ..cubicTo(
        w * 0.23, h * 0.25, // Control point 1
        w * 0.33, h * 0.20, // Control point 2
        w * 0.40, h * 0.25, // End point
      )

      // Curve 3: Large prominent hump
      ..cubicTo(
        w * 0.45, h * 0.02, // Higher control point 1
        w * 0.70, h * 0.02, // Wider control point 2
        w * 0.80, h * 0.40, // End point
      )

      // Curve 4: Mirror image of Curve 1
      ..cubicTo(
        w * 0.95, h * 0.45, // Control point 1
        w * 0.95, h * 0.65, // Control point 2
        w * 0.85, h * 0.65, // End point
      )

      // Draw right side of base to gap start
      ..lineTo(w * 0.60, h * 0.65)

      // Move to left side of gap without drawing
      ..moveTo(w * 0.40, h * 0.65)

      // Draw left side of base to starting point
      ..lineTo(w * 0.15, h * 0.65);

    // Draw base outline
    final basePaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..color = baseColor
      ..strokeCap = StrokeCap.round;
    canvas.drawPath(path, basePaint);

    // Calculate path metrics
    final metrics = path.computeMetrics().toList();
    final totalLength = metrics.fold(0.0, (sum, metric) => sum + metric.length);

    // NEW: Precisely locate the top of Curve 3
    // The top of Curve 3 is at the midpoint of the curve's control points
    final curve3Top = Offset(w * 0.575, h * 0.02); // (45%+70%)/2 = 57.5%
    double? startDistance;

    // NEW: Improved search algorithm to find exact top point
    double currentDistance = 0;
    double minDistance = double.infinity;

    for (final metric in metrics) {
      for (double d = 0; d < metric.length; d += 1) {
        final tangent = metric.getTangentForOffset(d)!;
        final distance = (tangent.position - curve3Top).distance;

        if (distance < minDistance) {
          minDistance = distance;
          startDistance = currentDistance + d;
        }
      }
      currentDistance += metric.length;
    }

    // Fallback to approximate start if needed
    startDistance ??= totalLength * 0.55;

    // Draw progress clockwise starting from top of Curve 3
    final extractPath = Path();
    if (progress > 0) {
      // Calculate the progress length
      final progressLength = progress * totalLength;

      // Start from curve3Top and go clockwise
      final start = startDistance!;
      final end = start + progressLength;

      if (end > totalLength) {
        // Draw from start to end of path
        addPathSegment(metrics, start, totalLength, extractPath);

        // Draw from start of path to remaining progress
        final remaining = end - totalLength;
        addPathSegment(metrics, 0, remaining, extractPath);
      } else {
        // Draw continuous segment
        addPathSegment(metrics, start, end, extractPath);
      }
    }

    // Draw progress segment
    final progressPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..color = progressColor
      ..strokeCap = StrokeCap.round;
    canvas.drawPath(extractPath, progressPaint);
  }

  // Helper function to add path segments from metrics
  void addPathSegment(
      List<PathMetric> metrics, double start, double end, Path destPath) {
    double current = 0;
    for (final metric in metrics) {
      final metricLength = metric.length;
      final metricStart = current;
      final metricEnd = current + metricLength;

      if (start < metricEnd && end > metricStart) {
        final segStart = (start - metricStart).clamp(0.0, metricLength);
        final segEnd = (end - metricStart).clamp(0.0, metricLength);
        if (segStart < segEnd) {
          destPath.addPath(metric.extractPath(segStart, segEnd), Offset.zero);
        }
      }

      current += metricLength;
      if (current > end) break;
    }
  }

  @override
  bool shouldRepaint(covariant _CloudPainter old) {
    return old.progress != progress ||
        old.baseColor != baseColor ||
        old.progressColor != progressColor;
  }
}
