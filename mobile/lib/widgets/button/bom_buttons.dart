import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/project/project.dart';
import '../../repositories/bom_repo.dart';
import '../../router/app_router.dart';
import '../../utils/utils.dart';
import '../summary/summary.dart';

class BomButtonsSection extends StatefulWidget {
  const BomButtonsSection({
    super.key,
    required this.solutionDesignBom,
    required this.solutionDesignTypeCode,
    required this.projectId,
    required this.origin,
  });

  final List<dynamic> solutionDesignBom;
  final String solutionDesignTypeCode;
  final String projectId;
  final FormOrigin origin;

  @override
  State<BomButtonsSection> createState() => _BomButtonsSectionState();
}

class _BomButtonsSectionState extends State<BomButtonsSection>
    with AutomaticKeepAliveClientMixin {
  @override
  bool get wantKeepAlive => true;

  late final List<dynamic> _entries;
  Future<List<_BtnModel>>? _buttonsFuture;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_buttonsFuture != null) return;

    final matches = widget.solutionDesignBom.where(
      (e) => e.data.solutionDesignTypeCode == widget.solutionDesignTypeCode,
    );
    final matching = matches.isNotEmpty ? matches.first : null;
    _entries = matching?.data.bomForms ?? const [];

    final isar = context.read<ProjectBloc>().isar;
    _buttonsFuture = Future.wait(
      _entries.map((entry) async {
        final r = await bomRouteAndLabel(entry.name);
        final action = await BomRepository().resolveBomActionLabel(
          isar: isar,
          projectId: widget.projectId,
          schemaKey: r.schemaName,
          origin: widget.origin,
        );
        return _BtnModel(
          actionWord: action,
          label: r.label,
          schemaName: r.schemaName,
          pageName: r.pageName,
        );
      }),
      eagerError: true,
    );
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);

    if (_entries.isEmpty) return const SizedBox.shrink();

    return FutureBuilder<List<_BtnModel>>(
      future: _buttonsFuture,
      builder: (context, snap) {
        if (snap.connectionState == ConnectionState.waiting || !snap.hasData) {
          return const SizedBox.shrink();
        }
        final models = snap.data!;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            for (final m in models) ...[
              DigitButton(
                capitalizeLetters: false,
                mainAxisSize: MainAxisSize.max,
                label: '${m.actionWord} ${m.label}',
                onPressed: () {
                  context.router.push(DynamicFormsRoute(
                    pageName: m.pageName,
                    schemaName: m.schemaName,
                    projectId: widget.projectId,
                    origin: widget.origin,
                  ));
                },
                type: DigitButtonType.secondary,
                size: DigitButtonSize.large,
              ),
              const SizedBox(height: spacer4),
            ],
          ],
        );
      },
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
