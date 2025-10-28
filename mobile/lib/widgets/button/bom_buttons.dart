import 'dart:async';

import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/activity_facility/activity_facility.dart';
import '../../data/secure_storage/secureStore.dart';
import '../../repositories/bom_repo.dart';
import '../../router/app_router.dart';
import '../../utils/utils.dart';
import '../summary/summary.dart';

// ---------------------------
// Shared constants/utilities
// ---------------------------

const _kBomSystemKey = 'bom_system_code';
const _kDefaultSystem = 'DC';

const _systemOptions = <String>[
  'AC_OFF_GRID',
  'AC_OFF_GRID_THREE_PHASE',
  'HYBRID_RMS_SINGLE_PHASE',
  'HYBRID_RMS_THREE_PHASE',
  'DC',
];

String _prettySystemLabel(String v) {
  switch (v) {
    case 'AC_OFF_GRID':
      return 'AC Off-Grid';
    case 'AC_OFF_GRID_THREE_PHASE':
      return 'AC Off-Grid (3φ)';
    case 'HYBRID_RMS_SINGLE_PHASE':
      return 'Hybrid RMS (1φ)';
    case 'HYBRID_RMS_THREE_PHASE':
      return 'Hybrid RMS (3φ)';
    case 'DC':
      return 'DC';
    default:
      return v;
  }
}

// -----------------------------------------------------------------
// BomSystemSelector (COMPACT DROPDOWN VERSION)
// Loads initial value from SecureStore (defaults to DC), shows a small dropdown,
// saves on change, and calls onChanged(systemCode).
// -----------------------------------------------------------------
class BomSystemSelector extends StatefulWidget {
  const BomSystemSelector({
    super.key,
    required this.onChanged, // (String systemCode) -> void
  });

  final void Function(String systemCode) onChanged;

  @override
  State<BomSystemSelector> createState() => _BomSystemSelectorState();
}

class _BomSystemSelectorState extends State<BomSystemSelector>
    with AutomaticKeepAliveClientMixin {
  @override
  bool get wantKeepAlive => true;

  String _selected = _kDefaultSystem;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    // Load once from SecureStore and notify parent
    Future(() async {
      final stored = await SecureStore().storage.read(key: _kBomSystemKey);
      final resolved = (stored != null && _systemOptions.contains(stored))
          ? stored
          : _kDefaultSystem;
      if (!mounted) return;
      setState(() {
        _selected = resolved;
        _loading = false;
      });
      widget.onChanged(resolved); // initialize parent with stored/default
    });
  }

  @override
  Widget build(BuildContext context) {
    super.build(context); // keep-alive

    if (_loading) {
      return const SizedBox.shrink();
    }

    // Compact dropdown; minimal padding; no giant buttons.
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: DropdownButtonFormField<String>(
        value: _selected,
        isDense: true,
        isExpanded: false,
        decoration: const InputDecoration(
          labelText: 'System',
          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          border: OutlineInputBorder(),
        ),
        items: _systemOptions
            .map(
              (code) => DropdownMenuItem<String>(
                value: code,
                child: Text(_prettySystemLabel(code)),
              ),
            )
            .toList(),
        onChanged: (val) async {
          if (val == null) return;
          setState(() => _selected = val);
          await SecureStore().storage.write(key: _kBomSystemKey, value: val);
          widget.onChanged(val); // parent will refresh BOM list
        },
      ),
    );
  }
}

// -----------------------------------------------------------------
// BomButtonsSection
// Now manages its own async state. It never hides previous buttons while
// refreshing; it reuses last good data until the new data is ready.
// -----------------------------------------------------------------
class BomButtonsSection extends StatefulWidget {
  const BomButtonsSection({
    super.key,
    required this.solutionDesignBom,
    required this.systemCode,
    required this.projectId,
    required this.origin,
  });

  final List<dynamic> solutionDesignBom;
  final String systemCode; // 'DC', 'AC_OFF_GRID', etc.
  final String projectId;
  final FormOrigin origin;

  @override
  State<BomButtonsSection> createState() => _BomButtonsSectionState();
}

class _BomButtonsSectionState extends State<BomButtonsSection>
    with
        AutomaticKeepAliveClientMixin,
        AutoRouteAwareStateMixin<BomButtonsSection> {
  @override
  bool get wantKeepAlive => true;

  List<dynamic> _entries = const [];
  List<_BtnModel> _models = const [];
  bool _loading = false;
  String _lastSig = '';

  StreamSubscription<void>? _bomWatchSub;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_models.isEmpty && !_loading) {
      _refreshModels();
    }
  }

  @override
  void didUpdateWidget(covariant BomButtonsSection oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Recompute a signature and only refresh when inputs truly change.
    final sig = '${widget.projectId}|${widget.origin}|${widget.systemCode}';
    if (sig != _lastSig && !_loading) {
      _refreshModels();
    }
    if (oldWidget.projectId != widget.projectId) {
      _restartBomWatcherForSchemas(const []); // project changed; clear watcher
    }
  }

  @override
  void didPopNext() {
    _refreshModels();
  }

  @override
  void dispose() {
    _bomWatchSub?.cancel(); // <-- ADD THIS
    super.dispose();
  }

  void _restartBomWatcherForSchemas(List<String> schemaKeys) {
    // <-- ADD THIS
    _bomWatchSub?.cancel();
    if (schemaKeys.isEmpty) {
      _bomWatchSub = null;
      return;
    }
    final isar = context.read<ActivityFacilityBloc>().isar;
    _bomWatchSub = BomRepository()
        .watchBomForSchemas(
      isar: isar,
      projectId: widget.projectId,
      schemaKeys: schemaKeys,
    )
        .listen((_) {
      if (!mounted) return;
      _refreshModels();
    });
  }

  Future<void> _refreshModels() async {
    _loading = true;
    _lastSig = '${widget.projectId}|${widget.origin}|${widget.systemCode}';

    final matches = widget.solutionDesignBom.where(
      (e) => e.data.systemCode == widget.systemCode,
    );
    final matching = matches.isNotEmpty ? matches.first : null;
    final newEntries = matching?.data.bomForms ?? const [];

    if (!mounted) return;
    setState(() {
      _entries = newEntries;
    });

    if (_entries.isEmpty) {
      _loading = false;
      if (mounted) setState(() {});
      _restartBomWatcherForSchemas(const []);
      return;
    }

    final isar = context.read<ActivityFacilityBloc>().isar;
    try {
      final results = <_BtnModel>[];
      for (final entry in _entries) {
        final r = await bomRouteAndLabel(entry.name);
        final action = await BomRepository().resolveBomActionLabel(
          isar: isar,
          projectId: widget.projectId,
          schemaKey: r.schemaName,
          origin: widget.origin,
        );
        results.add(_BtnModel(
          actionWord: action,
          label: r.label,
          schemaName: r.schemaName,
          pageName: r.pageName,
        ));
      }
      if (!mounted) return;
      final schemaKeys = results.map((e) => e.schemaName).toList();
      _restartBomWatcherForSchemas(schemaKeys);
      setState(() => _models = results);
    } finally {
      _loading = false;
      if (mounted) setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);

    if (_entries.isEmpty && _models.isEmpty) {
      return const SizedBox.shrink();
    }

    final visible = _models;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        for (final m in visible) ...[
          DigitButton(
            capitalizeLetters: false,
            mainAxisSize: MainAxisSize.max,
            label: '${m.actionWord} ${m.label}',
            onPressed: () async {
              final result = await context.router.push(
                DynamicFormsRoute(
                  pageName: m.pageName,
                  schemaName: m.schemaName,
                  projectId: widget.projectId,
                  origin: widget.origin,
                ),
              );
              if (!mounted) return;
              _refreshModels();
            },
            type: DigitButtonType.secondary,
            size: DigitButtonSize.large,
          ),
          const SizedBox(height: spacer4),
        ],
        if (_loading && visible.isNotEmpty) const SizedBox.shrink(),
      ],
    );
  }
}

class _BtnModel {
  final String actionWord;
  final String label;
  final String schemaName;
  final String pageName;
  _BtnModel({
    required this.actionWord,
    required this.label,
    required this.schemaName,
    required this.pageName,
  });
}
