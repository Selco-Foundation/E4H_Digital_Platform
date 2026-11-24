import 'package:digit_ui_components/theme/spacers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/app_init/app_init.dart';
import '../../data/api_interceptors.dart';
import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;

/// Home-only configuration loading. A failed load blocks entry until Retry succeeds.
class MdmsGate extends StatefulWidget {
  final Widget child;
  final MdmsModule module;
  final Future<void> Function()? load;

  const MdmsGate({
    super.key,
    required this.module,
    required this.child,
    this.load,
  });

  @override
  State<MdmsGate> createState() => _MdmsGateState();
}

class _MdmsGateState extends State<MdmsGate> {
  bool _loading = false;
  bool _ready = false;
  bool _failed = false;
  DialogRoute<void>? _dialog;

  @override
  void initState() {
    super.initState();
    _ready = context.read<AppInitialization>().isModuleReady(widget.module);
    if (_ready) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _load();
    });
  }

  Future<void> _loadInstallation() async {
    final bloc = context.read<AppInitialization>();
    final result = bloc.stream.firstWhere(
      (state) => state is Initialized || state is Error,
    );
    bloc.add(const InitEvent.fetchMdms());
    final state = await result;
    if (state is Error) throw StateError(state.message);
  }

  Future<void> _load() async {
    if (_loading) return;
    final initialization = context.read<AppInitialization>();
    final session = AuthTokenInterceptor.sessionGeneration;
    setState(() {
      _loading = true;
      _failed = false;
    });
    final navigator = Navigator.of(context, rootNavigator: true);
    final dialog = DialogRoute<void>(
      context: context,
      barrierDismissible: false,
      builder: (_) => PopScope(
        canPop: false,
        child: Dialog(
          child: Padding(
            padding: const EdgeInsets.all(spacer6),
            child: Row(children: [
              const CircularProgressIndicator(),
              const SizedBox(width: spacer4),
              Flexible(
                  child: Text(context.translate(i18.common.loadingAppData))),
            ]),
          ),
        ),
      ),
    );
    _dialog = dialog;
    navigator.push(dialog);
    var succeeded = false;
    try {
      await (widget.load ?? _loadInstallation)();
      succeeded = true;
      if (mounted) initialization.markModuleReady(widget.module, session);
    } catch (_) {
      // The auth interceptor handles expired sessions. Other errors stay retryable.
    } finally {
      _closeDialog();
      if (mounted && session == AuthTokenInterceptor.sessionGeneration) {
        setState(() {
          _loading = false;
          _ready = succeeded;
          _failed = !succeeded;
        });
      }
    }
  }

  void _closeDialog() {
    final dialog = _dialog;
    _dialog = null;
    if (dialog?.isActive == true) dialog!.navigator?.removeRoute(dialog);
  }

  @override
  void dispose() {
    // Removal can coincide with the auth router replacing the home route.
    final dialog = _dialog;
    _dialog = null;
    if (dialog != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (dialog.isActive) dialog.navigator?.removeRoute(dialog);
      });
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Stack(
        fit: StackFit.expand,
        children: [
          IgnorePointer(ignoring: !_ready, child: widget.child),
          if (_failed)
            Material(
              color: Theme.of(context).scaffoldBackgroundColor,
              child: Center(
                child: Column(mainAxisSize: MainAxisSize.min, children: [
                  Text(context.translate(i18.home.somethingWentWrong)),
                  const SizedBox(height: spacer4),
                  TextButton(
                    onPressed: _load,
                    child: Text(context.translate(i18.common.retry)),
                  ),
                ]),
              ),
            ),
        ],
      );
}
