import 'package:flutter/material.dart';

import 'element_asset_summary.dart';
import 'initial_element_asset_summary.dart';

class DynamicElementAssetSummary extends StatelessWidget {
  final String text;
  final String assetTypeCode;
  final int count;
  final bool lastCard;
  final VoidCallback? onPress;
  final VoidCallback? onAddDetailPress;
  final bool isInitial;

  const DynamicElementAssetSummary({
    super.key,
    required this.text,
    required this.assetTypeCode,
    required this.count,
    this.lastCard = false,
    this.onPress,
    this.onAddDetailPress,
    this.isInitial = false,
  });

  @override
  Widget build(BuildContext context) {
    return isInitial
        ? InitialElementAssetSummary(
            key: key,
            text: text,
            assetTypeCode: assetTypeCode,
            lastCard: lastCard,
            onPress: onPress,
            onAddDetailPress: onAddDetailPress,
          )
        : ElementAssetSummary(
            key: key,
            text: text,
            count: count,
            lastCard: lastCard,
            onPress: onPress,
          );
  }
}
