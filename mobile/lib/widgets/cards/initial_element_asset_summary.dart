import 'dart:async';

import 'package:collection/collection.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_divider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:isar/isar.dart';

import '../../blocs/app_init/app_init.dart';
import '../../blocs/cache_asset_count/cache_asset_count.dart';
import '../../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../../data/nosql/cache_add_new_asset.dart';
import '../../data/nosql/cache_asset_count.dart';
import '../../model/asset_count/asset_count.dart';
import '../../model/mdms/mdms.dart';
import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;

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

class _InitialElementAssetSummaryState
    extends State<InitialElementAssetSummary> {
  String? _currentActivityFacilityId;
  int _count = 0;
  int _minCount = 0;
  int _maxCount = 0;
  bool _hasObservedCachedCount = false;
  bool _hasAddNewAssetCache = false;
  int _loadGeneration = 0;
  int _assetCacheLoadGeneration = 0;

  StreamSubscription<CacheAssetCountState>? _countSub;
  StreamSubscription<SelectedActivityFacilityState>? _facilitySub;
  StreamSubscription<InitState>? _appInitSub;

  String get _normalizedAssetType => widget.assetTypeCode.trim().toLowerCase();

  @override
  void initState() {
    super.initState();
    _syncSelectedFacilityFromState();
    _syncLimitsFromAppInit();
    _countSub =
        context.read<CacheAssetCountBloc>().stream.listen(_onCountState);
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

    _loadOrSeedCount();
    _loadHasAddNewAssetCache();
  }

  @override
  void dispose() {
    _countSub?.cancel();
    _facilitySub?.cancel();
    _appInitSub?.cancel();
    super.dispose();
  }

  void _syncSelectedFacilityFromState() {
    context.read<SelectedActivityFacilityBloc>().state.whenOrNull(
        selected: (project) {
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
        _hasObservedCachedCount = false;
        _hasAddNewAssetCache = false;
      });
      _loadOrSeedCount();
      _loadHasAddNewAssetCache();
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

    final nextLimit = assetCountList.firstOrNull?.data.assetCount
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
    _loadOrSeedCount();
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

  int _clampInitialCount(int value, {int? max}) {
    final resolvedMax = max ?? _maxCount;
    if (resolvedMax <= 0) return 0;
    return value.clamp(0, resolvedMax);
  }

  int get _activationCount => _minCount > 0 ? _minCount : 1;

  int get _effectiveLowerBound =>
      _count == 0 && !_hasObservedCachedCount ? 0 : _activationCount;

  void _applyEntries(List<CacheAssetCount> entries) {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    final latest = entries
        .where((entry) =>
            entry.activityFacilityId == projectId &&
            entry.assetType.trim().toLowerCase() == _normalizedAssetType)
        .sorted(_compareEntries)
        .lastOrNull;

    if (latest == null) return;

    _hasObservedCachedCount = true;
    if (!mounted) return;

    final clampedCount = _clampToBounds(latest.count);
    if (_count == clampedCount) return;

    setState(() {
      _count = clampedCount;
    });
    _loadHasAddNewAssetCache();
  }

  void _applySingleEntry(CacheAssetCount entry) {
    if (!mounted) return;
    if (entry.activityFacilityId != _currentActivityFacilityId) return;
    if (entry.assetType.trim().toLowerCase() != _normalizedAssetType) return;

    _hasObservedCachedCount = true;
    final clampedCount = _clampToBounds(entry.count);
    if (_count == clampedCount) return;

    setState(() {
      _count = clampedCount;
    });
    _loadHasAddNewAssetCache();
  }

  Future<void> _loadOrSeedCount() async {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    final generation = ++_loadGeneration;
    final bloc = context.read<CacheAssetCountBloc>();
    final entries = await bloc.isar.cacheAssetCounts
        .where()
        .activityFacilityIdEqualTo(projectId)
        .findAll();

    if (!mounted || generation != _loadGeneration) return;

    final latest = entries
        .where((entry) =>
            entry.assetType.trim().toLowerCase() == _normalizedAssetType)
        .sorted(_compareEntries)
        .lastOrNull;

    if (latest != null) {
      _hasObservedCachedCount = true;
      final clampedCount = _clampToBounds(latest.count);
      if (_count != clampedCount) {
        setState(() {
          _count = clampedCount;
        });
      }
      _loadHasAddNewAssetCache();
      return;
    }

    _hasObservedCachedCount = false;
    if (_count != 0) {
      setState(() {
        _count = 0;
      });
    }
    _loadHasAddNewAssetCache();
  }

  Future<void> _loadHasAddNewAssetCache() async {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    final generation = ++_assetCacheLoadGeneration;
    final isar = context.read<CacheAssetCountBloc>().isar;
    final count = await isar.cacheAddNewAssets
        .where()
        .activityFacilityIdEqualTo(projectId)
        .filter()
        .assetTypeEqualTo(_normalizedAssetType)
        .count();

    if (!mounted || generation != _assetCacheLoadGeneration) return;

    final hasCache = count > 0;
    if (_hasAddNewAssetCache == hasCache) return;

    setState(() {
      _hasAddNewAssetCache = hasCache;
    });
  }

  void _updateCount(int nextCount) {
    final projectId = _currentActivityFacilityId;
    if (projectId == null) return;

    final normalizedCount = nextCount <= 0
        ? _clampInitialCount(nextCount, max: _maxCount)
        : _clampToBounds(
            nextCount,
            min: _activationCount,
            max: _maxCount,
          );
    setState(() {
      _count = normalizedCount;
      if (normalizedCount > 0) {
        _hasObservedCachedCount = true;
      }
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
                    enabled: _count > _effectiveLowerBound,
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
                    onTap: () => _updateCount(
                      _count == 0 ? _activationCount : _count + 1,
                    ),
                  )
                ],
              ),
            ),
            if (_count > 0 && _hasAddNewAssetCache)
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
              label: context.translate(i18.sharedCards.addDetails),
              type: DigitButtonType.secondary,
              size: DigitButtonSize.medium,
              isDisabled: _count <= 0,
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
