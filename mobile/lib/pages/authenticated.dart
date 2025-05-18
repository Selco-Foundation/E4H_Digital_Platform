import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/blocs/selected_project/selected_project.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/project/project.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/userbloc.dart';

@RoutePage()
class AuthenticatedScreenWrapper extends StatelessWidget {
  const AuthenticatedScreenWrapper({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) => MultiBlocProvider(
          providers: [
            BlocProvider(create: (context) => UserBloc()),
            BlocProvider(create: (context) => AssetTypeBloc()),
            BlocProvider(create: (context) => ReportTypeBloc()),
            BlocProvider(create: (context) => InboxTypeBloc()),
            BlocProvider(create: (context) => ProjectBloc()),
            BlocProvider(create: (context) => SelectedProjectBloc()),
          ],
          child: const Scaffold(
            body: AutoRouter(),
            // appBar: AppBar(),
            // drawer: const Drawer(
            //   child: SideBar(),
            // )
          ));
}
