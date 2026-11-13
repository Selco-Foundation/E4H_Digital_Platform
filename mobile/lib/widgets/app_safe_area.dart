import 'package:flutter/material.dart';

/// Keeps routed screens clear of system navigation without changing headers.
class AppSafeArea extends StatelessWidget {
  final Widget child;

  const AppSafeArea({super.key, required this.child});

  @override
  Widget build(BuildContext context) => SafeArea(top: false, child: child);
}
