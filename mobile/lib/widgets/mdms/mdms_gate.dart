import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/app_init/app_init.dart';
import '../../blocs/auth/authbloc.dart';
import '../../router/app_router.dart';

class MdmsGate extends StatefulWidget {
  const MdmsGate({super.key});

  @override
  State<MdmsGate> createState() => _MdmsGateState();
}

class _MdmsGateState extends State<MdmsGate> {
  bool _dialogShown = false;
  bool _fetchScheduled = false;

  void _postFrame(VoidCallback fn) =>
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        fn();
      });

  void _openBlockingLoader() {
    if (_dialogShown) return;
    _dialogShown = true;
    _postFrame(() {
      if (!mounted) return;
      showDialog(
        context: context,
        barrierDismissible: false,
        useRootNavigator: true,
        builder: (_) => PopScope(
          canPop: false,
          onPopInvoked: (didPop) {},
          child: const Dialog(
            insetPadding: EdgeInsets.all(24),
            child: Padding(
              padding: EdgeInsets.all(24),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(width: 16),
                  Flexible(child: Text('Loading app data…')),
                ],
              ),
            ),
          ),
        ),
      );
    });
  }

  void _closeBlockingLoader() {
    if (!_dialogShown) return;
    _dialogShown = false;
    _postFrame(() {
      if (!mounted) return;
      Navigator.of(context, rootNavigator: true).pop();
    });
  }

  @override
  void initState() {
    super.initState();
    _postFrame(() {
      final s = context.read<AppInitialization>().state;
      s.maybeWhen(
        defaulted: (_) => _startMdms(),
        orElse: () {},
      );
    });
  }

  void _startMdms() {
    if (_fetchScheduled) return;
    _fetchScheduled = true;
    _openBlockingLoader();
    context.read<AppInitialization>().add(const InitEvent.fetchMdms());
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AppInitialization, InitState>(
      listenWhen: (p, c) =>
          c is LoadingMdms || c is Initialized || c is Error || c is Defaulted,
      listener: (context, state) async {
        state.map(
          uninitialized: (_) {},
          defaulted: (_) => _startMdms(),
          loadingMdms: (_) => _openBlockingLoader(),
          initialized: (_) => _closeBlockingLoader(),
          error: (err) async {
            _closeBlockingLoader();

            context.read<AuthBloc>().add(const AuthEvent.logout());

            _postFrame(() {
              if (!mounted) return;
              context.router.replaceAll([const UnauthenticatedRouteWrapper()]);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text(err.message)),
              );
            });
          },
        );
      },
      child: const SizedBox.shrink(),
    );
  }
}
