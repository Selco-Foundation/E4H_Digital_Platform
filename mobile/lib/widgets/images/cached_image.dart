import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import 'app_network_image_error.dart';
import 'app_shimmer.dart';

class CachedImage extends StatelessWidget {
  final String? imageUrl;
  final bool isRound;
  final double radius;
  final double? height;
  final double? width;
  final Function()? onClick;

  final BoxFit fit;

  const CachedImage(
    this.imageUrl, {
    Key? key,
    this.isRound = false,
    this.radius = 0,
    this.height,
    this.width,
    this.fit = BoxFit.cover,
    this.onClick,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onClick,
      child: SizedBox(
        height: isRound ? radius : height,
        width: isRound ? radius : width,
        child: ClipRRect(
          borderRadius: BorderRadius.circular(isRound ? 50 : radius),
          child: CachedNetworkImage(
            imageUrl: imageUrl!,
            fit: fit,
            placeholder: (context, url) =>
                AppShimmer(height: height ?? 150, width: width),
            errorWidget: (context, url, error) =>
                AppNetworkImageError(height: height ?? 150, width: width),
          ),
        ),
      ),
    );
  }
}
