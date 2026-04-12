import 'dart:async';

import 'package:collection/collection.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/app_init/app_init.dart';
import '../../blocs/cache_asset_count/cache_asset_count.dart';
import '../../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../../data/nosql/cache_asset_count.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/mdms/mdms.dart';

class AssetCounter extends StatelessWidget {
  final String symbol;
  final VoidCallback? onTap;
  final bool enabled;

  const AssetCounter({
    super.key,
    required this.symbol,
    this.onTap,
    this.enabled = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final foregroundColor = enabled
        ? theme.colorTheme.text.secondary
        : theme.colorTheme.text.disabled;

    return GestureDetector(
      onTap: enabled ? onTap : null,
      child: Container(
        height: spacer9,
        width: spacer9,
        decoration: BoxDecoration(
          color: theme.colorTheme.generic.background,
          border: Border.all(color: theme.colorTheme.generic.inputBorder),
        ),
        child: Center(
          child: Text(
            symbol,
            textAlign: TextAlign.center,
            style: TextStyle(
              color: foregroundColor,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }
}

class InitialElementAssetSummary extends StatefulWidget {
  final String text;
  final String assetTypeCode;
  final bool lastCard;
  final VoidCallback? onPress;
  final VoidCallback? onAddDetailPress;

  const InitialElementAssetSummary({
    super.key,
    required this.text,
    required this.assetTypeCode,
    this.lastCard = false,
    this.onPress,
    this.onAddDetailPress,
  });

  @override
  State<InitialElementAssetSummary> createState() =>
      _InitialElementAssetSummaryState();
}

class _InitialElementAssetSummaryState extends State<InitialElementAssetSummary> {
  String? _currentActivityFacilityId;
  int _count = 0;
  int _minCount = 0;
  int _maxCount = 0;

  StreamSubscription<CacheAssetCountState>? _countSub;
  StreamSubscription<SelectedActivityFacilityState>? _facilitySub;
  StreamSubscription<InitState>? _appInitSub;

  String get _normalizedAssetType => widget.assetTypeCode.trim().toLowerCase();

  @override
  void initState() {
    super.initState();
    _syncSelectedFacilityFromState();
    _syncLimitsFromAppInit();
    _countSub = context.read<CacheAssetCountBloc>().stream.listen(_onCountState);
    _facilitySub = context
        .read<SelectedActivityFacilityBloc>()
        .stream
        .listen(_onSelectedFacilityState);
    _appInitSub = context.read<AppInitialization>().stream.listen((state) {
      _applyLimits(state);
    });

    context.read<CacheAssetCountBloc>().state.maybeWhen(
          loaded: _applyEntries,
          added: _applySingleEntry,
          updated: _applySingleEntry,
          orElse: () {},
        );

    _loadCount();
  }

  @override
  void dispose() {
    _countSub?.cancel();
    _facilitySub?.cancel();
    _appInitSub?.cancel();
    super.dispose();
  }

  void _syncSelectedFacilityFromState() {
    context
        .read<SelectedActivityFacilityBloc>()
        .state
        .whenOrNull(selected: (project) {
      _currentActivityFacilityId = project.activityFacility.id;
    });
  }

  void _onSelectedFacilityState(SelectedActivityFacilityState state) {
    state.whenOrNull(selected: (project) {
      final nextId = project.activityFacility.id;
      if (_currentActivityFacilityId == nextId) return;

      setState(() {
        _currentActivityFacilityId = nextId;
        _count = 0;
      });

      _loadCount();
    });
  }

  void _onCountState(CacheAssetCountState state) {
    state.maybeWhen(
      loaded: _applyEntries,
      added: _applySingleEntry,
      updated: _applySingleEntry,
      orElse: () {},
    );
  }

  void _syncLimitsFromAppInit() {
    _applyLimits(context.read<AppInitialization>().state);
  }

  void _applyLimits(InitState state) {
    final assetCountList = state.maybeWhen<List<Mdms<AssetCountData>>>(
      initialized: (appConfig, assetCount, assetType, system, warranty, brand,
              solutionDesign, solutionDesignBom) =>
          assetCount,
      orElse: () => const [],
    );

    final nextLimit = assetCountList
        .firstOrNull
        ?.data
        .assetCount
        .firstWhereOrNull((entry) =>
            entry.assetTypeCode.toUpperCase() ==
            widget.assetTypeCode.toUpperCase());

    final nextMin = nextLimit?.min ?? 0;
    final nextMax = nextLimit?.max ?? 0;
    final clampedCount = _clampToBounds(_count, min: nextMin, max: nextMax);

    if (!mounted) {
      _minCount = nextMin;
      _maxCount = nextMax;
      _count = clampedCount;
      return;
    }

    if (_minCount == nextMin &&
        _maxCount == nextMax &&
        _count == clampedCount) {
      return;
    }

    setState(() {
      _minCount = nextMin;
      _maxCount = nextMax;
      _count = clampedCount;
    });
  }

  int _compareEntries(CacheAssetCount a, CacheAssetCount b) {
    final aTime = a.updatedAt ?? a.createdAt;
    final bTime = b.updatedAt ?? b.createdAt;
    final byTime = aTime.compareTo(bTime);
    if (byTime != 0) return byTime;
    return a.id.compareTo(b.id);
  }

  int _clampToBounds(int value, {int? min, int? max}) {
    final resolvedMin = min ?? _minCount;
    final resolvedMax = max ?? _maxCount;
    if (resolvedMax < resolvedMin) return resolvedMin;
    return value.clamp(resolvedMin, resolvedMax);
  }

  void _applyEntries(List<CacheAssetCount> entries) {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    final latest = entries
        .where((entry) =>
            entry.activityFacilityId == projectId &&
            entry.assetType.trim().toLowerCase() == _normalizedAssetType)
        .sorted(_compareEntries)
        .lastOrNull;

    if (latest == null || !mounted) return;

    final clampedCount = _clampToBounds(latest.count);
    if (_count == clampedCount) return;

    setState(() {
      _count = clampedCount;
    });
  }

  void _applySingleEntry(CacheAssetCount entry) {
    if (!mounted) return;
    if (entry.activityFacilityId != _currentActivityFacilityId) return;
    if (entry.assetType.trim().toLowerCase() != _normalizedAssetType) return;

    final clampedCount = _clampToBounds(entry.count);
    if (_count == clampedCount) return;

    setState(() {
      _count = clampedCount;
    });
  }

  void _loadCount() {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    context
        .read<CacheAssetCountBloc>()
        .add(CacheAssetCountEvent.get(projectId, _normalizedAssetType));
  }

  void _updateCount(int nextCount) {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    final normalizedCount = _clampToBounds(nextCount);
    setState(() {
      _count = normalizedCount;
    });

    context.read<CacheAssetCountBloc>().add(
          CacheAssetCountEvent.add(
            CacheAssetCount(
              activityFacilityId: projectId,
              assetType: _normalizedAssetType,
              count: normalizedCount,
            ),
          ),
        );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Stack(
          alignment: Alignment.center,
          children: [
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                widget.text,
                style: textTheme.headingS,
              ),
            ),
            Positioned(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  AssetCounter(
                    symbol: '-',
                    enabled: _count > _minCount,
                    onTap: () => _updateCount(_count - 1),
                  ),
                  Container(
                    height: spacer9,
                    width: spacer9,
                    decoration: BoxDecoration(
                      color: theme.colorTheme.generic.transparent,
                      border: Border(
                        top: BorderSide(
                            color: theme.colorTheme.generic.inputBorder),
                        bottom: BorderSide(
                            color: theme.colorTheme.generic.inputBorder),
                      ),
                    ),
                    child:
                        Center(child: Text('$_count', style: textTheme.bodyL)),
                  ),
                  AssetCounter(
                    symbol: '+',
                    enabled: _count < _maxCount,
                    onTap: () => _updateCount(_count + 1),
                  )
                ],
              ),
            ),
            Align(
              alignment: Alignment.centerRight,
              child: GestureDetector(
                onTap: widget.onPress ?? () {},
                child: Text(
                  "Summary",
                  style: textTheme.bodyS.copyWith(
                    color: theme.colorTheme.primary.primary1,
                  ),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: spacer2),
        Column(
          children: [
            DigitButton(
              mainAxisSize: MainAxisSize.max,
              label: 'Add Details',
              type: DigitButtonType.secondary,
              size: DigitButtonSize.medium,
              onPressed: widget.onAddDetailPress ?? () {},
            ),
            const SizedBox(height: spacer2),
          ],
        ),
        widget.lastCard == true
            ? const SizedBox.shrink()
            : const Column(
                children: [
                  SizedBox(height: spacer2),
                  DigitDivider(dividerType: DividerType.small),
                ],
              ),
      ],
    );
  }
}
