import 'dart:async';

import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/activity_facility/activity_facility.dart';
import '../../repositories/dynamic_form_repo.dart';
import '../../router/app_router.dart';
import '../../utils/utils.dart';
import '../summary/summary.dart';

class BomButtonsSection extends StatefulWidget {
  const BomButtonsSection({
    super.key,
    required this.solutionDesignBom,
    required this.systemCode,
    required this.projectId,
    required this.origin,
  });

  final List<dynamic> solutionDesignBom;
  final String systemCode;
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
    final sig = '${widget.projectId}|${widget.origin}|${widget.systemCode}';
    if (sig != _lastSig && !_loading) {
      _refreshModels();
    }
    if (oldWidget.projectId != widget.projectId) {
      _restartBomWatcherForSchemas(const []);
    }
  }

  @override
  void didPopNext() {
    _refreshModels();
  }

  @override
  void dispose() {
    _bomWatchSub?.cancel();
    super.dispose();
  }

  void _restartBomWatcherForSchemas(List<String> schemaKeys) {
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
