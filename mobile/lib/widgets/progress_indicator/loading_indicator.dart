import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';

Widget loadingIndicator() => const Center(
      child: Center(
        child: Padding(
          padding: EdgeInsets.only(top: spacer8),
          child: CircularProgressIndicator(),
        ),
      ),
    );
